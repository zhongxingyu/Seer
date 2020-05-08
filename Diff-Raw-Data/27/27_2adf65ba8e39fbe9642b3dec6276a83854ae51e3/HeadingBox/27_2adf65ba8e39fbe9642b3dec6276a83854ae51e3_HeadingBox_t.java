 /*
  * SectionHeadingBox.java
  *
  * Copyright (c) 2009 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the program it is a part of, are made available
  * to you by its authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 package quill.ui;
 
 import org.gnome.gtk.HBox;
 import org.gnome.gtk.Label;
 
 import quill.textbase.Segment;
 import quill.textbase.TextChain;
 
 class HeadingBox extends HBox
 {
     private HBox box;
 
     protected HeadingEditorTextView title;
 
     protected Label label;
 
     private TextChain chain;
 
     public HeadingBox(Segment segment) {
         super(false, 0);
 
         setupBox(segment);
     }
 
     private void setupBox(Segment segment) {
         box = this;
 
         title = new HeadingEditorTextView(segment);
         box.packStart(title, true, true, 0);
 
         label = new Label();
         label.setWidthChars(20);
         box.packEnd(label, false, false, 0);
     }

    EditorTextView getEditor() {
        return title;
    }
 }
