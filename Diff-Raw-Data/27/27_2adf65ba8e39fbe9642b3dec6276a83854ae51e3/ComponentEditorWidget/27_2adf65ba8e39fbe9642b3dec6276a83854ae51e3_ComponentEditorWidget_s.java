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
 import org.gnome.gtk.PolicyType;
 import org.gnome.gtk.ScrolledWindow;
 import org.gnome.gtk.VBox;
 import org.gnome.gtk.Widget;
 
 import quill.textbase.BlockquoteSegment;
 import quill.textbase.Change;
 import quill.textbase.ComponentSegment;
 import quill.textbase.HeadingSegment;
 import quill.textbase.ParagraphSegment;
 import quill.textbase.PreformatSegment;
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
 
         if (segment instanceof ParagraphSegment) {
             editor = new ParagraphEditorTextView(segment);
 
             result = editor;
         } else if (segment instanceof BlockquoteSegment) {
             editor = new BlockquoteEditorTextView(segment);
 
             result = editor;
         } else if (segment instanceof PreformatSegment) {
             editor = new PreformatEditorTextView(segment);
 
             wide = new ScrolledWindow();
             wide.setPolicy(PolicyType.AUTOMATIC, PolicyType.NEVER);
             wide.add(editor);
 
             result = wide;
         } else if (segment instanceof HeadingSegment) {
             heading = new SectionHeadingBox(segment);
 
             result = heading;
         } else if (segment instanceof ComponentSegment) {
             heading = new ChapterHeadingBox(segment);
 
             result = heading;
         } else {
 
             throw new IllegalStateException("Unknown Segment type");
         }
 
        associate(segment, result);
 
         return result;
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
         final EditorTextView editor;
 
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
 
             children = box.getChildren();
             view = lookup(first);
 
             for (i = 0; i < children.length; i++) {
                 if (children[i] == view) {
                     break;
                 }
             }
             if (i == children.length) {
                 throw new IllegalArgumentException("view not in this ComponentEditorWidget");
             }
 
             /*
              * Create the new editor
              */
 
             widget = createEditorForSegment(added);
 
             box.packStart(widget, false, false, 0);
             i++;
             box.reorderChild(widget, i);
             widget.showAll();
 
             /*
              * Split the old one in two pieces, adding a new editor for the
              * second piece.
              */
 
             series = first.getParent();
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
             throw new UnsupportedOperationException("Not yet implemented"); // FIXME
         }
     }
 }
