 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2009-2010 Operational Dynamics Consulting, Pty Ltd
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
 
 import org.freedesktop.cairo.Context;
 import org.freedesktop.cairo.Surface;
 import org.gnome.gdk.Color;
 import org.gnome.gdk.EventExpose;
 import org.gnome.gdk.EventFocus;
 import org.gnome.gtk.Adjustment;
 import org.gnome.gtk.Allocation;
 import org.gnome.gtk.Container;
 import org.gnome.gtk.HSeparator;
 import org.gnome.gtk.PolicyType;
 import org.gnome.gtk.Scrollbar;
 import org.gnome.gtk.ScrolledWindow;
 import org.gnome.gtk.StateType;
 import org.gnome.gtk.Test;
 import org.gnome.gtk.VBox;
 import org.gnome.gtk.Viewport;
 import org.gnome.gtk.Widget;
 
 import quill.textbase.AttributionSegment;
 import quill.textbase.ComponentSegment;
 import quill.textbase.EndnoteSegment;
 import quill.textbase.HeadingSegment;
 import quill.textbase.ImageSegment;
 import quill.textbase.NormalSegment;
 import quill.textbase.Origin;
 import quill.textbase.PoeticSegment;
 import quill.textbase.PreformatSegment;
 import quill.textbase.QuoteSegment;
 import quill.textbase.ReferenceSegment;
 import quill.textbase.Segment;
 import quill.textbase.Series;
 
 /**
  * Left hand side of a PrimaryWindow for editing a Component (Article or
  * Chapter).
  * 
  * @author Andrew Cowie
  */
 class ComponentEditorWidget extends ScrolledWindow
 {
     private ScrolledWindow scroll;
 
     private Adjustment adj;
 
     private VBox box;
 
     private LinkedList<EditorTextView> editors;
 
     /**
      * Which Segment currently has the cursor?
      */
     private Segment cursorSegment;
 
     /**
      * What is the top level UI holding this document?
      */
     private PrimaryWindow primary;
 
     private Series series;
 
     ComponentEditorWidget(PrimaryWindow primary) {
         super();
         scroll = this;
         this.primary = primary;
 
         setupScrolling();
         hookupAdjustmentReactions();
     }
 
     private void setupScrolling() {
         final Viewport port;
         box = new VBox(false, 3);
 
         scroll.setPolicy(PolicyType.NEVER, PolicyType.ALWAYS);
         scroll.addWithViewport(box);
 
         /*
          * Set the background color of the entire EditorWidget to white in
          * order to hide the upper side of the horizontal Scrollbars in the
          * preformatted blocks. Finding that this was the right place was
          * traumatic, but it turns out that the Viewport has the underlying
          * [org.gnome.gdk] Window where the drawing happens. Annoyingly,
          * calling modifyBackground() on the ScolledWindow didn't work.
          */
 
         port = (Viewport) scroll.getChild();
         port.modifyBackground(StateType.NORMAL, Color.WHITE);
 
         adj = scroll.getVAdjustment();
     }
 
     private void hookupAdjustmentReactions() {}
 
     /**
      * Tell the ComponentEditorWidget to ensure that the range from to
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
 
     void ensureVisible(Segment segment) {
         final EditorTextView editor;
         final Allocation alloc;
         int h, H;
         int y;
 
         editor = lookup(segment);
 
         alloc = editor.getAllocation();
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
 
         h = (int) adj.getPageSize();
         H = (int) adj.getUpper();
 
         if (y + h > H) {
             adj.setValue(H - h);
         } else {
             adj.setValue(y);
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
 
         this.editors = new LinkedList<EditorTextView>();
 
         for (i = 0; i < num; i++) {
             segment = series.getSegment(i);
 
             widget = createEditorForSegment(i, segment);
 
             box.packStart(widget, false, false, 0);
         }
 
         box.showAll();
 
         /*
          * And make sure the cursor is a Segment from this Series.
          */
 
         this.cursorSegment = series.getSegment(0);
     }
 
     private Segment lookup(Widget editor) {
         final int len;
         int i;
 
         len = editors.size();
 
         if (len != series.size()) {
             throw new AssertionError();
         }
 
         for (i = 0; i < len; i++) {
             if (editors.get(i) == editor) {
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
 
         i = series.indexOf(segment);
 
         return editors.get(i);
     }
 
     private Widget createEditorForSegment(int index, Segment segment) {
         final Widget result;
         final EditorTextView editor;
         final HeadingBox heading;
         final ImageDisplayBox image;
         final Scrollbar bar;
         final ScrolledWindow wide;
 
         if (segment instanceof NormalSegment) {
             editor = new NormalEditorTextView(this, segment);
 
             result = editor;
         } else if (segment instanceof QuoteSegment) {
             editor = new QuoteEditorTextView(this, segment);
 
             result = editor;
         } else if (segment instanceof PoeticSegment) {
             editor = new PoeticEditorTextView(this, segment);
 
             result = editor;
         } else if (segment instanceof AttributionSegment) {
             editor = new AttributionEditorTextView(this, segment);
 
             result = editor;
         } else if (segment instanceof PreformatSegment) {
             editor = new PreformatEditorTextView(this, segment);
 
             wide = new ScrolledWindow();
             wide.setPolicy(PolicyType.ALWAYS, PolicyType.NEVER);
             wide.add(editor);
 
             /*
              * Having set up horizontal scrollbars for code blocks, we want to
              * make them a bit less obtrusive in normal use.
              */
 
             bar = wide.getHScrollbar();
             editor.connect(new Widget.FocusInEvent() {
                 public boolean onFocusInEvent(Widget source, EventFocus event) {
                     bar.setSensitive(true);
                     return false;
                 }
             });
             editor.connect(new Widget.FocusOutEvent() {
                 public boolean onFocusOutEvent(Widget source, EventFocus event) {
                     bar.setValue(0);
                     bar.setSensitive(false);
                     return false;
                 }
             });
             bar.setSensitive(false);
 
             bar.connect(new ExposeEvent() {
                 public boolean onExposeEvent(Widget source, EventExpose event) {
                     final Context cr;
                     final Surface surface;
 
                     if (bar.getSensitive()) {
                         return false;
                     }
 
                     cr = new Context(event);
 
                     cr.setSource(Color.WHITE);
                     cr.paint();
 
                     surface = cr.getTarget();
                     surface.flush();
 
                     return true;
                 }
             });
 
             result = wide;
         } else if (segment instanceof ImageSegment) {
             image = new ImageDisplayBox(this, segment);
 
             editor = image.getEditor();
             result = image;
         } else if (segment instanceof HeadingSegment) {
             heading = new SectionHeadingBox(this, segment);
 
             editor = heading.getEditor();
             result = heading;
         } else if (segment instanceof ComponentSegment) {
             heading = new ChapterHeadingBox(this, segment);
 
             editor = heading.getEditor();
             result = heading;
         } else if (segment instanceof EndnoteSegment) {
             editor = null;
             result = new HSeparator();
         } else if (segment instanceof ReferenceSegment) {
             editor = null;
             result = new HSeparator();
         } else {
 
             throw new IllegalStateException("Unknown Segment type");
         }
 
         editors.add(index, editor);
 
         return result;
     }
 
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
         EditorTextView editor;
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
         EditorTextView editor;
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
     void propegateTextualChange(final EditorTextView editor, final Segment previous,
             final Segment segment) {
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
 
         primary.update(this, former, replacement);
     }
 
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
         int i;
 
         former = series;
 
         /*
          * Find the index of the view into the VBox.
          */
 
         for (i = 0; i < editors.size(); i++) {
             if (editors.get(i) == originating) {
                 break;
             }
         }
         if (i == editors.size()) {
             throw new AssertionError("originating EditorTextView not in this ComponentEditorWidget");
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
 
         primary.update(this, former, replacement);
     }
 
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
         final EditorTextView editor;
 
         segment = lookup(from);
         i = series.indexOf(segment);
 
         if (i < 1) {
             return;
         }
         i--;
 
         editor = editors.get(i);
         editor.placeCursorLastLine(position);
         editor.grabFocus();
 
         cursorSegment = segment;
     }
 
     void moveCursorDown(final Widget from, final int position) {
         int i;
         Segment segment;
         final EditorTextView editor;
 
         segment = lookup(from);
         i = series.indexOf(segment);
 
         i++;
         if (i == series.size()) {
             return;
         }
 
         segment = series.getSegment(i);
 
         editor = editors.get(i);
         editor.placeCursorFirstLine(position);
         editor.grabFocus();
 
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
 
     /**
      * @deprecated Replaced by editors[].
      */
     private EditorTextView[] findEditors() {
         final Widget[] children;
         final EditorTextView[] editors;
         final int num;
         int i;
         Widget child;
 
         children = box.getChildren();
         num = children.length;
         editors = new EditorTextView[num];
 
         for (i = 0; i < num; i++) {
             child = children[i];
             editors[i] = findEditorIn(child);
         }
 
         return editors;
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
         return editors.get(index);
     }
 
     void forceRecheck() {
         final int I;
         int i;
         EditorTextView editor;
 
         I = editors.size();
         for (i = 0; i < I; i++) {
             editor = editors.get(i);
             editor.forceRecheck();
         }
     }
 }
