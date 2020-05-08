 /*
  * ComponentEditorWidget.java
  *
  * Copyright (c) 2009 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the program it is a part of, are made available
  * to you by its authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 package quill.ui;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.gnome.gtk.Adjustment;
 import org.gnome.gtk.Container;
 import org.gnome.gtk.PolicyType;
 import org.gnome.gtk.ScrolledWindow;
 import org.gnome.gtk.VBox;
 import org.gnome.gtk.Widget;
 
 import quill.textbase.Change;
 import quill.textbase.ComponentSegment;
 import quill.textbase.HeadingSegment;
 import quill.textbase.NormalSegment;
 import quill.textbase.PreformatSegment;
 import quill.textbase.QuoteSegment;
 import quill.textbase.Segment;
 import quill.textbase.Series;
 import quill.textbase.StructuralChange;
 import quill.textbase.TextualChange;
 
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
 
     /**
      * A map going from user interface layer deeper into the internal
      * representation layer.
      */
     private Map<Widget, Segment> deeper;
 
     /**
      * A map going from internal representations out to the corresponding user
      * interface element; the opposite of deeper.
      */
     private Map<Segment, Widget> rising;
 
     ComponentEditorWidget() {
         super();
         scroll = this;
         setupMaps();
 
         setupScrolling();
         hookupAdjustmentReactions();
     }
 
     private void setupMaps() {
         deeper = new HashMap<Widget, Segment>(16);
         rising = new HashMap<Segment, Widget>(16);
     }
 
     private void setupScrolling() {
         box = new VBox(false, 3);
 
         scroll.setPolicy(PolicyType.NEVER, PolicyType.ALWAYS);
         scroll.addWithViewport(box);
 
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
 
     private Series series;
 
     void initializeSeries(Series series) {
         Segment segment;
         int i;
         Widget widget;
 
         this.series = series;
 
         for (i = 0; i < series.size(); i++) {
             segment = series.get(i);
 
             widget = createEditorForSegment(segment);
 
             box.packStart(widget, false, false, 0);
         }
 
         box.showAll();
     }
 
     private void associate(Segment segment, Widget widget) {
         rising.put(segment, widget);
         deeper.put(widget, segment);
     }
 
     private void disassociate(Segment segment, Widget widget) {
         rising.remove(segment);
         deeper.remove(widget);
     }
 
     private Segment lookup(Widget editor) {
         return deeper.get(editor);
     }
 
     private Widget lookup(Segment segment) {
         return rising.get(segment);
     }
 
     private Widget createEditorForSegment(Segment segment) {
         Widget result;
         EditorTextView editor;
         HeadingBox heading;
         ScrolledWindow wide;
 
         if (segment instanceof NormalSegment) {
             editor = new NormalEditorTextView(segment);
 
             result = editor;
         } else if (segment instanceof QuoteSegment) {
             editor = new QuoteEditorTextView(segment);
 
             result = editor;
         } else if (segment instanceof PreformatSegment) {
             editor = new PreformatEditorTextView(segment);
 
             wide = new ScrolledWindow();
             wide.setPolicy(PolicyType.AUTOMATIC, PolicyType.NEVER);
             wide.add(editor);
 
             result = wide;
         } else if (segment instanceof HeadingSegment) {
             heading = new SectionHeadingBox(segment);
 
             editor = heading.getEditor();
             result = heading;
         } else if (segment instanceof ComponentSegment) {
             heading = new ChapterHeadingBox(segment);
 
             editor = heading.getEditor();
             result = heading;
         } else {
 
             throw new IllegalStateException("Unknown Segment type");
         }
 
         associate(segment, editor);
 
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
      * Given a StructuralChange, figure out what it means in terms of the UI
      * in this ComponentEditorWidget.
      */
     void affect(Change change) {
         final StructuralChange structural;
         final Segment first;
         final Segment added;
         final Segment third;
         final Series series;
         final Widget[] children;
         int i;
         final Widget view;
         Widget widget;
         EditorTextView editor;
 
         if (change instanceof TextualChange) {
             first = change.affects();
             editor = (EditorTextView) lookup(first);
             editor.affect(change);
 
         } else if (change instanceof StructuralChange) {
 
             /*
              * Find the index of the view into the VBox.
              */
             structural = (StructuralChange) change;
 
             first = structural.getInto();
             added = structural.getAdded();
 
             view = lookup(first);
 
             children = box.getChildren();
 
             for (i = 0; i < children.length; i++) {
                 if (doesContainerHaveChild(children[i], view)) {
                     break;
                 }
             }
 
             if (i == children.length) {
                 throw new IllegalArgumentException("\n" + "view not in this ComponentEditorWidget");
             }
 
             /*
              * Create the new editor
              */
 
             widget = createEditorForSegment(added);
 
             box.packStart(widget, false, false, 0);
             i++;
             box.reorderChild(widget, i);
             widget.showAll();
             editor = (EditorTextView) lookup(added);
             editor.grabFocus();
 
             /*
              * Split the old one in two pieces, adding a new editor for the
              * second piece... unless we did the split at the end of the last
              * segment.
              */
 
             series = first.getParent();
 
             if (children.length + 1 == series.size()) {
                 return;
             }
 
             if (i + 1 == series.size()) {
                 return;
             }
 
             third = series.get(i + 1);
             widget = createEditorForSegment(third);
             box.packStart(widget, false, false, 0);
             i++;
             box.reorderChild(widget, i);
             widget.showAll();
 
             /*
              * Delete the third text out of the first.
              */
 
             editor = (EditorTextView) view;
             editor.affect(change);
         }
     }
 
     void reverse(Change change) {
         final Segment first;
         final EditorTextView editor;
 
         if (change instanceof TextualChange) {
             first = change.affects();
             editor = (EditorTextView) lookup(first);
             editor.reverse(change);
 
         } else if (change instanceof StructuralChange) {
             // FIXME
             throw new UnsupportedOperationException("\n" + "Not yet implemented");
         }
     }
 
     public void grabFocus() {
        final Widget first;
 
        first = lookup(series.get(0));
         first.grabFocus();
     }
 
     void moveCursorUp(final Widget from, final int position) {
         int i;
         final Widget above;
         Segment segment;
         final EditorTextView editor;
 
         segment = lookup(from);
         i = series.indexOf(segment);
 
         if (i < 1) {
             return;
         }
         i--;
 
         segment = series.get(i);
         above = lookup(segment);
 
         editor = (EditorTextView) above;
         editor.placeCursorLastLine(position);
         editor.grabFocus();
     }
 
     void moveCursorDown(final Widget from, final int position) {
         int i;
         final Widget below;
         Segment segment;
         final EditorTextView editor;
 
         segment = lookup(from);
         i = series.indexOf(segment);
 
         i++;
         if (i == series.size()) {
             return;
         }
 
         segment = series.get(i);
         below = lookup(segment);
 
         editor = (EditorTextView) below;
         editor.placeCursorFirstLine(position);
         editor.grabFocus();
     }
 }
