 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2009-2011 Operational Dynamics Consulting, Pty Ltd
  *
  * The code in this file, and the program it is a part of, is made available
  * to you by its authors as open source software: you can redistribute it
  * and/or modify it under the terms of the GNU General Public License version
  * 2 ("GPL") as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
  *
  * You should have received a copy of the GPL along with this program. If not,
  * see http://www.gnu.org/licenses/. The authors of this program may be
  * contacted through http://research.operationaldynamics.com/projects/quill/.
  */
 package quill.ui;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.gnome.gtk.Adjustment;
 import org.gnome.gtk.Allocation;
 import org.gnome.gtk.Container;
 import org.gnome.gtk.Label;
 import org.gnome.gtk.PolicyType;
 import org.gnome.gtk.ScrolledWindow;
 import org.gnome.gtk.Test;
 import org.gnome.gtk.VBox;
 import org.gnome.gtk.Widget;
 
 import quill.textbase.Component;
 import quill.textbase.Origin;
 import quill.textbase.Segment;
 import quill.textbase.Series;
 
 import static org.gnome.gtk.Alignment.LEFT;
 import static org.gnome.gtk.Alignment.TOP;
 
 /**
  * Code for editing a Series (of Segments), presenting them as a list of
  * EditorTextViews. This is primarily about the editor for the main body of
  * each chapter, but is also reused to power the notes and references editor.
  * 
  * <p>
  * <i>The name "component" derives from DocBook's generic name for its
  * articles and chapters, though it wasn't actually as strictly typed as
  * that.</i>
  * 
  * @author Andrew Cowie
  */
 abstract class SeriesEditorWidget extends ScrolledWindow
 {
     private ScrolledWindow scroll;
 
     private Adjustment adj;
 
     private VBox box;
 
     private LinkedList<Editor> editors;
 
     /**
      * Which Segment currently has the cursor?
      */
     private Segment cursorSegment;
 
     /**
      * What is the top level UI holding this document?
      */
     private final PrimaryWindow primary;
 
     private Series series;
 
     SeriesEditorWidget(PrimaryWindow primary) {
         this(primary, null);
     }
 
     SeriesEditorWidget(PrimaryWindow primary, String heading) {
         super();
         this.scroll = this;
         this.primary = primary;
 
         setupScrolling(heading);
         hookupAdjustmentReactions();
     }
 
     private void setupScrolling(String heading) {
         final VBox outer;
         final Label label;
 
         box = new VBox(false, 0);
 
         if (heading == null) {
             scroll.addWithViewport(box);
         } else {
             outer = new VBox(false, 0);
 
             label = new Label("<span size='xx-large'>" + heading + "</span>");
             label.setUseMarkup(true);
             label.setAlignment(LEFT, TOP);
             outer.packStart(label, false, false, 6);
             outer.packStart(box, true, true, 0);
             scroll.addWithViewport(outer);
         }
 
         scroll.setPolicy(PolicyType.NEVER, PolicyType.ALWAYS);
 
         /*
          * For reasons I don't entirely understand, the recursive showAll()
          * from PrimaryWindow isn't getting though this to the children
          * ReferenceEditorTextViews. Strange. Anyway, show()ing here fixes it,
          * though I hate unexplained workarounds.
          */
 
         scroll.show();
 
         adj = scroll.getVAdjustment();
     }
 
     /**
      * Get the VBox that the contents of this Widget are to be packed into;
      * this is scrolled.
      */
     protected VBox getTop() {
         return box;
     }
 
     private void hookupAdjustmentReactions() {}
 
     /**
      * Tell the SeriesEditorWidget to ensure that the range from to
      * from+height is scrolled to and within view. This is used by the
      * EditorTextViews to handle the cursor moving one line above or below the
      * current viewport.
      */
     void ensureVisible(int from, int height) {
         int v, h;
 
         if (from < 0) {
             return;
         }
 
         v = (int) adj.getValue();
         h = (int) adj.getPageSize();
 
         if (from < v) {
             adj.setValue(from);
         } else if (from + height > v + h) {
             adj.setValue(from + height - h);
         }
     }
 
     /**
      * Ensure the specified Segment is visible. Doing so in a non-jarring way
      * is rather tricky; there are a number of corner cases arising from the
      * fact that this is used both when inserting Segments, and when
      * navigating from the Outline.
      * 
      * @param force
      *            Force the segment to top of screen?
      */
     void ensureVisible(Segment segment, boolean force) {
         final Widget[] children;
         final Widget widget;
         final int i;
         final Allocation alloc;
         int v, h, H;
         int y, R;
 
         children = box.getChildren();
         i = series.indexOf(segment);
         widget = children[i];
 
         alloc = widget.getAllocation();
         y = alloc.getY();
 
         if (y < 0) {
             /*
              * Yet again we bump into the TextView doesn't know it's own hight
              * yet probem. Suprisingly, cycling the main loop appears to let
              * the idle handler run?!? FIXME Perhaps it's time to expose
              * mainIterationDo() in java-gnome for real, since we really
              * shouldn't be using Test as a workaround.
              */
 
             Test.cycleMainLoop();
             y = alloc.getY();
 
             /*
              * If we don't have a value, then we're screwed for real. Bail
              * out.
              */
 
             if (y < 0) {
                 return;
             }
         }
         R = alloc.getHeight();
 
         v = (int) adj.getValue();
         h = (int) adj.getPageSize();
         H = (int) adj.getUpper();
 
         if (force) {
             if (y + h > H) {
                 /*
                  * Target location beyond end of ScrolledWindow; bounce back
                  * from end by one scrollbar handle size. This happens when
                  * you navigate to end via Outline.
                  */
                 adj.setValue(H - h);
             } else {
                 adj.setValue(y);
             }
         } else {
             if (y < v) {
                 /*
                  * Clearly, we need to snap back to target.
                  */
                 adj.setValue(y);
             } else if (y + R < v + h) {
                 /*
                  * Target allocation already completely visible on screen;
                  * don't scroll.
                  */
                 return;
             } else if (y + h > H) {
                 /*
                  * Target location beyond end of ScrolledWindow; bounce back
                  * from end by one scrollbar handle size.
                  */
                 adj.setValue(H - h);
             } else if (y < v + h + R) {
                 /*
                  * If the target is off screen below, but by less than a
                  * scrollbar handle size, then position target at bottom. This
                  * happens inserting.
                  */
                 adj.setValue(y - (h - R));
             } else if (y > v + h) {
                 /*
                  * Pk, it's just out of sight below; snap the target location
                  * on screen.
                  */
                 adj.setValue(y);
             } else {
                 /*
                  * Target location already visible on screen; don't scroll.
                  */
                 return;
             }
         }
     }
 
     Series getSeries() {
         return series;
     }
 
     PrimaryWindow getPrimary() {
         return primary;
     }
 
     void initializeSeries(Series series) {
         Widget[] children;
         Segment segment;
         int i;
         final int num;
         Widget widget;
 
         /*
          * If loading a new document, there may be a chapter already
          * displayed; if so remove its children first.
          */
 
         children = box.getChildren();
 
         for (i = 0; i < children.length; i++) {
             widget = children[i];
             box.remove(widget);
         }
 
         /*
          * Now set up the new Series.
          */
         num = series.size();
         this.series = series;
 
         this.editors = new LinkedList<Editor>();
 
         for (i = 0; i < num; i++) {
             segment = series.getSegment(i);
 
             widget = createEditorForSegment(i, segment);
             if (widget == null) {
                 continue;
             }
 
             box.packStart(widget, false, false, 0);
         }
 
         box.showAll();
 
         /*
          * And make sure the cursor is a Segment from this Series.
          */
 
         if (series.size() > 0) {
             this.cursorSegment = series.getSegment(0);
         } else {
             this.cursorSegment = null;
         }
 
         /*
          * Once again, I'm not happy with this. The bug is that sometimes the
          * EndnoteSeriesEditorWidget and ReferencesSeriesEditorWidget are not
          * actually requesting the appropriate amount of vertical space
          * because they haven't calculated it yet. So annoying. Cycling the
          * main loop seems to allow the idle handler to run, working around
          * the problem Hopefully GTK 3.0 will be better about this.
          */
 
         Test.cycleMainLoop();
     }
 
    private Segment lookup(Widget widget) {
         final int len;
         int i;
        Editor editor;
        EditorTextView view;
 
         len = editors.size();
 
         if (len != series.size()) {
             throw new AssertionError();
         }
 
         for (i = 0; i < len; i++) {
            editor = editors.get(i);
            view = editor.getTextView();
            if (view == widget) {
                 return series.getSegment(i);
             }
         }
 
         throw new IllegalStateException("Can't find editor Widget in EditorTextView[]");
     }
 
     /**
      * Get the editor corresponding to the given Segment.
      */
     /*
      * This one is easy; we can just ask the series
      */
     private EditorTextView lookup(Segment segment) {
         final int i;
         final Editor editor;
 
         i = series.indexOf(segment);
         editor = editors.get(i);
         return editor.getTextView();
     }
 
     protected List<Editor> getEditors() {
         return editors;
     }
 
     /**
      * Given a Segment, create the user interface for it. To be implemented by
      * concrete subclasses.
      * 
      * @param index
      *            The position within the series that this Segment is found.
      * @param segment
      */
     abstract Widget createEditorForSegment(int index, Segment segment);
 
     /**
      * Initialize the editor with the appropriate Series from the given
      * Component.
      * 
      * @param component
      */
     abstract void initialize(Component component);
 
     abstract void advanceTo(Component replacement);
 
     abstract void reverseTo(Component replacement);
 
     abstract Component getComponent();
 
     /**
      * @deprecated
      */
     @SuppressWarnings("unused")
     private static boolean doesContainerHaveChild(Widget widget, Widget target) {
         final Widget[] children;
         Container parent;
         int i;
 
         if (widget == target) {
             return true;
         }
 
         if (widget instanceof Container) {
             parent = (Container) widget;
             children = parent.getChildren();
         } else {
             return false;
         }
 
         for (i = 0; i < children.length; i++) {
             if (children[i] == target) {
                 return true;
             }
             if (children[i] instanceof Container) {
                 if (doesContainerHaveChild(children[i], target)) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     /**
      * Given a [new] state, apply it!
      */
     void advanceTo(final Series replacement) {
         final int updated, added, third, deleted;
         int i;
         Widget widget;
         Widget[] children;
         Editor editor;
         Segment segment;
 
         if (this.series == replacement) {
             return;
         }
 
         updated = replacement.getIndexUpdated();
         added = replacement.getIndexAdded();
         third = replacement.getIndexThird();
         deleted = replacement.getIndexDeleted();
 
         if (updated >= 0) {
             segment = replacement.getSegment(updated);
             editor = editors.get(updated);
             editor.advanceTo(segment);
         }
 
         /*
          * Can't add at 0, can only update 0.
          */
 
         if (added > 0) {
             segment = replacement.getSegment(added);
             widget = createEditorForSegment(added, segment);
             box.packStart(widget, false, false, 0);
             box.reorderChild(widget, added);
 
             editor = editors.get(added);
             editor.advanceTo(segment);
 
             widget.showAll();
             cursorSegment = segment;
             editor.grabFocus();
         }
 
         if (third > 0) {
             segment = replacement.getSegment(third);
             widget = createEditorForSegment(third, segment);
             box.packStart(widget, false, false, 0);
             box.reorderChild(widget, third);
 
             editor = editors.get(third);
             editor.advanceTo(segment);
 
             widget.showAll();
         }
 
         // UNTRIED
         if (deleted >= 0) {
             segment = replacement.getSegment(deleted);
 
             children = box.getChildren();
             widget = children[deleted];
             box.remove(widget);
 
             editors.remove(deleted);
         }
 
         this.series = replacement;
 
         /*
          * TODO This is now a no-op (and waste) given everything should have
          * been applied courtesy of the above. Probably need to remove this,
          * but what about the case of applying a full Series state, not just a
          * known delta from a previous Series. Do we do that anywhere?
          */
 
         for (i = 0; i < replacement.size(); i++) {
             segment = replacement.getSegment(i);
             editor = editors.get(i);
             if (editor == null) {
                 continue;
             }
             editor.advanceTo(segment);
         }
 
         if (added > 0) {
             this.ensureVisible(cursorSegment, false);
         }
     }
 
     /**
      * Given the previous state, go back to it.
      * 
      * This is actually a misnomer; this method takes the currently active
      * state and undoes what happened to get from series to current.
      */
     void reveseTo(final Series series) {
         final Series current;
         final int updated, added, third, deleted;
         Widget widget;
         Widget[] children;
         Editor editor;
         Segment segment;
 
         if (this.series == series) {
             return;
         }
         current = this.series;
 
         updated = current.getIndexUpdated();
         added = current.getIndexAdded();
         third = current.getIndexThird();
         deleted = current.getIndexDeleted();
 
         if (updated >= 0) {
             segment = series.getSegment(updated);
             editor = editors.get(updated);
             editor.reverseTo(segment);
         }
 
         if (third > 0) {
             children = box.getChildren();
             widget = children[third];
             box.remove(widget);
 
             editors.remove(third);
         }
 
         if (added > 0) {
             children = box.getChildren();
             widget = children[added];
             box.remove(widget);
 
             editors.remove(added);
         }
 
         // UNTRIED
         if (deleted >= 0) {
             segment = current.getSegment(deleted);
             widget = createEditorForSegment(deleted, segment);
             box.packStart(widget, false, false, 0);
             box.reorderChild(widget, deleted);
 
             editor = editors.get(deleted);
             editor.advanceTo(segment);
 
             widget.showAll();
             editor.grabFocus();
         }
 
         this.series = series;
     }
 
     /**
      * Entry point for an EditorTextView to inform its parent that its state
      * has changed.
      * 
      * @param editor
      */
     void propegateTextualChange(final Editor editor, final Segment previous, final Segment segment) {
         final Series former, replacement;
         final int i;
 
         former = series;
 
         i = former.indexOf(previous);
 
         replacement = former.update(i, segment);
         cursorSegment = segment;
 
         /*
          * TODO Anything else to change?
          */
 
         /*
          * Now propegate that a state change has happened upwards.
          */
 
         propegateTextualChange(primary, former, replacement);
     }
 
     /**
      * Compose a new Component object from the given Series and propegate
      * upwards.
      */
     /*
      * First argument is just there to restrict visibility
      */
     abstract void propegateTextualChange(PrimaryWindow primary, Series former, Series replacement);
 
     /**
      * 
      * @param editor
      * @param first
      * @param added
      * @param third
      *            if null, then we are appending
      */
     void propegateStructuralChange(final EditorTextView originating, final Segment first,
             final Segment added, final Segment third) {
         Series former, replacement;
         final int I;
         int i;
 
         former = series;
 
         /*
          * Find the index of the view into the VBox.
          */
         I = editors.size();
 
         for (i = 0; i < I; i++) {
             if (editors.get(i) == originating) {
                 break;
             }
         }
         if (i == I) {
             throw new AssertionError("originating EditorTextView not in this SeriesEditorWidget");
         }
 
         /*
          * Split the old one in two pieces, adding a new editor for the second
          * piece... unless we did the split at the end of the last segment.
          */
 
         if (third == null) {
             i++;
             replacement = former.insert(i, added);
         } else {
             replacement = former.splice(i, first, added, third);
         }
 
         propegateStructuralChange(primary, former, replacement);
     }
 
     /**
      * Compose a new Component object from the given Series and propegate
      * upwards.
      */
     abstract void propegateStructuralChange(PrimaryWindow primary, Series former, Series replacement);
 
     public void grabFocus() {
         final Segment segment;
         final EditorTextView first;
 
         segment = series.getSegment(0);
         first = lookup(segment);
         first.placeCursorFirstLine(0);
         first.grabFocus();
 
         cursorSegment = segment;
     }
 
     Origin getCursor(final int folioPosition) {
         final Widget widget;
         final EditorTextView editor;
         final Origin result;
         final int seriesPosition, segmentOffset;
 
         if (cursorSegment == null) {
             return null;
         }
         seriesPosition = series.indexOf(cursorSegment);
 
         widget = lookup(cursorSegment);
         editor = (EditorTextView) widget;
         segmentOffset = editor.getInsertOffset();
 
         result = new Origin(folioPosition, seriesPosition, segmentOffset);
         return result;
     }
 
     void setCursor(Segment segment) {
         cursorSegment = segment;
     }
 
     void moveCursorUp(final Widget from, final int position) {
         int i;
         Segment segment;
         final Editor editor;
         final EditorTextView view;
 
         segment = lookup(from);
         i = series.indexOf(segment);
 
         if (i < 1) {
             return;
         }
         i--;
 
         editor = editors.get(i);
         view = editor.getTextView();
         view.placeCursorLastLine(position);
         view.grabFocus();
 
         cursorSegment = segment;
     }
 
     void moveCursorDown(final Widget from, final int position) {
         int i;
         Segment segment;
         final Editor editor;
         final EditorTextView view;
 
         segment = lookup(from);
         i = series.indexOf(segment);
 
         i++;
         if (i == series.size()) {
             return;
         }
 
         segment = series.getSegment(i);
 
         editor = editors.get(i);
         view = editor.getTextView();
         view.placeCursorFirstLine(position);
         view.grabFocus();
 
         cursorSegment = segment;
     }
 
     // page down written first. See there.
     void movePageUp(final int x, final int y) {
         final int v, h, aim;
         int t;
         final EditorTextView editor;
 
         v = (int) adj.getValue();
         h = (int) adj.getPageSize();
 
         if (v == 0) {
             editor = findEditorFirst();
             editor.placeCursorFirstLine(0);
         } else {
             aim = v - h;
 
             if (aim < 0) {
                 adj.setValue(0);
                 t = 0;
             } else {
                 adj.setValue(aim);
                 t = aim;
             }
 
             t += y - v;
 
             editor = findEditorAt(t);
             editor.placeCursorAtLocation(x, t);
         }
         editor.grabFocus();
 
         cursorSegment = lookup(editor);
     }
 
     void movePageDown(final int x, final int y) {
         final int v, h, H, max, aim;
         int t;
         final EditorTextView editor;
 
         v = (int) adj.getValue();
         h = (int) adj.getPageSize();
         H = (int) adj.getUpper();
 
         max = H - h;
 
         /*
          * If we're already on the last page, we jump to the last character of
          * the last editor. Otherwise we add a page size. If that's greater
          * than the maximum available, then place us on the last page.
          */
 
         if (v == max) {
             editor = findEditorLast();
             editor.placeCursorLastLine(-1);
         } else {
 
             aim = v + h;
 
             if (aim > max) {
                 adj.setValue(max);
                 /*
                  * In the case where y - v is 0 (or small), the cursor doesn't
                  * always make it onto a full line, and this causes the
                  * ScrolledWindow to jump back (care of the CursorPosition
                  * handler), inhibiting us from paging down to the end. This
                  * forces the cursor down far enough that its corresponding
                  * row is completely on screen, so no jump. This probably
                  * should be calculated based on font sizes, not hardcoded.
                  */
                 t = max + 15;
             } else {
                 adj.setValue(aim);
                 t = aim;
             }
 
             /*
              * Now find the Editor at the coordinate corresponding to where
              * the cursor was previously, and send the cursor there.
              */
 
             t += y - v;
 
             editor = findEditorAt(t);
             editor.placeCursorAtLocation(x, t);
         }
         editor.grabFocus();
 
         cursorSegment = lookup(editor);
     }
 
     /**
      * Some EditorTextViews are nested in other containers (notably
      * ScrolledWindows) to provide desired UI effects. So given a Widget that
      * is either a Container or an Editor, descend down the hierarchy until we
      * find the actual EditorTextView.
      */
     /*
      * Copied from GraphicalTestCase.
      */
     // recursive
     private static EditorTextView findEditorIn(Widget widget) {
         final Container container;
         final Widget[] children;
         Widget child, result;
         int i;
 
         if (widget instanceof EditorTextView) {
             return (EditorTextView) widget;
         }
 
         container = (Container) widget;
         children = container.getChildren();
 
         for (i = 0; i < children.length; i++) {
             child = children[i];
 
             if (child instanceof EditorTextView) {
                 return (EditorTextView) child;
             }
 
             if (child instanceof Container) {
                 result = findEditorIn(child);
                 if (result != null) {
                     return (EditorTextView) result;
                 }
             }
 
         }
         return null;
     }
 
     private EditorTextView findEditorAt(int y) {
         final Widget[] children;
         int i, Y;
         Widget child;
         Allocation alloc;
 
         children = box.getChildren();
         child = null;
 
         for (i = 0; i < children.length; i++) {
             child = children[i];
             alloc = child.getAllocation();
             Y = alloc.getY() + alloc.getHeight();
 
             if (Y > y) {
                 break;
             }
         }
 
         return findEditorIn(child);
     }
 
     private EditorTextView findEditorFirst() {
         final Widget[] children;
 
         children = box.getChildren();
         return findEditorIn(children[0]);
     }
 
     private EditorTextView findEditorLast() {
         final Widget[] children;
         final int i;
 
         children = box.getChildren();
         i = children.length - 1;
         return findEditorIn(children[i]);
     }
 
     void moveCursorStart() {
         final EditorTextView editor;
 
         editor = findEditorFirst();
         editor.placeCursorFirstLine(0);
         editor.grabFocus();
 
         cursorSegment = series.getSegment(0);
     }
 
     void moveCursorEnd() {
         final EditorTextView editor;
 
         editor = findEditorLast();
         editor.placeCursorLastLine(-1);
         editor.grabFocus();
 
         cursorSegment = series.getSegment(series.size() - 1);
     }
 
     /*
      * For testing only
      */
     final EditorTextView testGetEditor(int index) {
         final Editor editor;
 
         editor = editors.get(index);
         return editor.getTextView();
     }
 
     void forceRecheck() {
         final int I;
         int i;
         Editor editor;
         EditorTextView view;
 
         I = editors.size();
         for (i = 0; i < I; i++) {
             editor = editors.get(i);
             view = editor.getTextView();
             view.forceRecheck();
         }
     }
 
     /**
      * Get the data describing the popup menu appropriate for the Series being
      * edited.
      */
     abstract InsertMenuDetails[] getInsertMenuDetails();
 }
 
 class InsertMenuDetails
 {
     final Class<?> type;
 
     final String text;
 
     final String subtext;
 
     InsertMenuDetails(Class<?> type, String text, String subtext) {
         this.type = type;
         this.text = text;
         this.subtext = subtext;
     }
 }
