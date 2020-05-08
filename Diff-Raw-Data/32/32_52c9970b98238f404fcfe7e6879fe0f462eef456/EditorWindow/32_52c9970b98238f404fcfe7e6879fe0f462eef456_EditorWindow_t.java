 /*
  * EditorWindow.java
  *
  * Copyright (c) 2008 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the program it is a part of, are made available
  * to you by its authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 package com.operationaldynamics.markerpen;
 
 import java.util.HashSet;
 
 import org.gnome.gdk.Event;
 import org.gnome.gdk.EventKey;
 import org.gnome.gdk.Keyval;
 import org.gnome.gdk.ModifierType;
 import org.gnome.gtk.Gtk;
 import org.gnome.gtk.TextBuffer;
 import org.gnome.gtk.TextIter;
 import org.gnome.gtk.TextMark;
 import org.gnome.gtk.TextTag;
 import org.gnome.gtk.TextView;
 import org.gnome.gtk.VBox;
 import org.gnome.gtk.Widget;
 import org.gnome.gtk.Window;
 
 class EditorWindow extends Window
 {
     private Window window;
 
     private VBox top;
 
     private TextBuffer buffer;
 
     private TextMark selectionBound, insertBound;
 
     private TextView view;
 
     EditorWindow() {
         super();
 
         setupWindow();
         setupEditor();
 
         installKeybindings();
         hookupFormatManagement();
 
         completeWindow();
     }
 
     private void setupWindow() {
         window = this;
         window.setDefaultSize(200, 300);
 
         top = new VBox(false, 6);
         window.add(top);
 
         window.connect(new DeleteEvent() {
             public boolean onDeleteEvent(Widget source, Event event) {
                 Gtk.mainQuit();
                 return false;
             }
         });
     }
 
     private void completeWindow() {
         window.showAll();
     }
 
     private void setupEditor() {
         buffer = new TextBuffer();
 
         selectionBound = buffer.getSelectionBound();
         insertBound = buffer.getInsert();
 
         view = new TextView(buffer);
         top.packStart(view);
     }
 
     private void installKeybindings() {
         view.connect(new Widget.KeyPressEvent() {
             public boolean onKeyPressEvent(Widget source, EventKey event) {
                 final Keyval key;
                 final ModifierType mod;
 
                 key = event.getKeyval();
                 mod = event.getState();
 
                 if (mod == ModifierType.CONTROL_MASK) {
                     if (key == Keyval.i) {
                         toggleFormat(Format.italics);
                         return true;
                     } else if (key == Keyval.b) {
                         toggleFormat(Format.bold);
                         return true;
                     } else if (key == Keyval.m) {
                         toggleFormat(Format.mono);
                         return true;
                    } else if (key == Keyval.s) {
                        System.out.println(Serializer.extractToFile(buffer));
                     }
                 }
                 return false;
             }
         });
     }
 
     private HashSet<TextTag> insertTags;
 
     /**
      * Hookup signals to aggregate formats to be used on a subsequent
      * insertion. The insertTags Set starts empty, and builds up as formats
      * are toggled by the user. When the cursor moves, the Set is changed to
      * the formatting applying on character back.
      */
     private void hookupFormatManagement() {
         insertTags = new HashSet<TextTag>(4);
 
         buffer.connectAfter(new TextBuffer.InsertText() {
             public void onInsertText(TextBuffer source, TextIter end, String text) {
                 final TextIter start;
 
                 start = end.copy();
                 start.backwardChars(text.length());
 
                 for (TextTag tag : insertTags) {
                     buffer.applyTag(tag, start, end);
                 }
             }
         });
 
         buffer.connect(new TextBuffer.MarkSet() {
             public void onMarkSet(TextBuffer source, TextIter location, TextMark mark) {
                 if (mark != insertBound) {
                     return;
                 }
 
                 /*
                  * Forget previous insert formatting.
                  */
                 insertTags.clear();
 
                 /*
                  * But having moved, pickup the formatting of the character
                  * preceeding the cursor.
                  */
 
                 location.backwardChar();
 
                 for (TextTag tag : location.getTags()) {
                     insertTags.add(tag);
                 }
             };
         });
     }
 
     private void toggleFormat(TextTag format) {
         final TextIter start, end, iter;
 
         if (buffer.getHasSelection()) {
             start = selectionBound.getIter();
             end = insertBound.getIter();
 
             /*
              * There is a subtle bug that if you have selected moving
              * backwards, selectionBound will be at a point where the
              * formatting ends, with the result that the second toggling will
              * fail. Work around this by ensuring we work from the earlier of
              * the two TextIters.
              */
             if (start.getOffset() > end.getOffset()) {
                 iter = end;
             } else {
                 iter = start;
             }
 
             if (iter.hasTag(format)) {
                 buffer.removeTag(format, start, end);
             } else {
                 buffer.applyTag(format, start, end);
             }
         } else {
             if (insertTags.contains(format)) {
                 insertTags.remove(format);
             } else {
                 insertTags.add(format);
             }
         }
     }
 
     /**
      * Enable unit tests to get to the underlying TextBuffer
      */
     TextBuffer getBuffer() {
         return buffer;
     }
 }
