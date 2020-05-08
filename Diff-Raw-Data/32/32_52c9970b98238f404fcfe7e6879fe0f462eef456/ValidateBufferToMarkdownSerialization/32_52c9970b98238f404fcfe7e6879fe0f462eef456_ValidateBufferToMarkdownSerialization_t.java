 /*
  * ValidateBufferToMarkdownSerialization.java
  *
  * Copyright (c) 2008 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the program it is a part of, are made available
  * to you by its authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 package com.operationaldynamics.markerpen;
 
 import org.gnome.gtk.TestCaseGtk;
 import org.gnome.gtk.TextBuffer;
 import org.gnome.gtk.TextIter;
 import org.gnome.gtk.TextMark;
 
 import static com.operationaldynamics.markerpen.Format.bold;
 import static com.operationaldynamics.markerpen.Format.italics;
 import static com.operationaldynamics.markerpen.Format.mono;
 import static org.gnome.gtk.TextMark.LEFT;
 
 public class ValidateBufferToMarkdownSerialization extends TestCaseGtk
 {
     public final void testExportItalics() {
         final TextBuffer buffer;
         final TextIter pointer;
         final String text;
 
         buffer = new TextBuffer();
         pointer = buffer.getIterStart();
         buffer.insert(pointer, "Hello ");
         buffer.insert(pointer, "world", italics);
 
         text = Serializer.extractToFile(buffer);
 
         assertEquals("Hello _world_", text);
     }
 
     public final void testExportBold() {
         final TextBuffer buffer;
         final TextIter pointer;
         final String text;
 
         buffer = new TextBuffer();
         pointer = buffer.getIterStart();
         buffer.insert(pointer, "A ");
         buffer.insert(pointer, "brighter", bold);
         buffer.insert(pointer, " future");
 
         text = Serializer.extractToFile(buffer);
 
         assertEquals("A **brighter** future", text);
     }
 
     public final void testExportMono() {
         final TextBuffer buffer;
         final TextIter pointer;
         final String text;
 
         buffer = new TextBuffer();
         pointer = buffer.getIterStart();
         buffer.insert(pointer, "Don't look in ");
         buffer.insert(pointer, "/etc/secret", mono);
         buffer.insert(pointer, " as it is secret!");
 
         text = Serializer.extractToFile(buffer);
 
         assertEquals("Don't look in `/etc/secret` as it is secret!", text);
     }
 
     public final void testExportSimultaneousMarkup() {
         final TextBuffer buffer;
         final TextIter pointer, start;
         final TextMark begin;
         final String text;
 
         buffer = new TextBuffer();
         pointer = buffer.getIterStart();
         buffer.insert(pointer, "Much ");
 
         begin = buffer.createMark(pointer, LEFT);
         buffer.insert(pointer, "emphasis");
         start = begin.getIter();
         buffer.applyTag(mono, start, pointer);
         buffer.applyTag(italics, start, pointer);
         buffer.applyTag(bold, start, pointer);
 
         buffer.insert(pointer, " needed");
 
         text = Serializer.extractToFile(buffer);
 
         assertEquals("Much _**`emphasis`**_ needed", text);
     }
 }
