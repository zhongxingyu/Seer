 /*
  * ValidateChangePropagation.java
  *
  * Copyright (c) 2009 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the suite it is a part of, are made available
  * to you by the authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 package quill.ui;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import nu.xom.ParsingException;
 import nu.xom.ValidityException;
 
 import org.gnome.gdk.Keyval;
 import org.gnome.gdk.ModifierType;
 import org.gnome.gtk.Container;
 import org.gnome.gtk.Test;
 import org.gnome.gtk.TextBuffer;
 import org.gnome.gtk.TextIter;
 import org.gnome.gtk.Widget;
 
 import quill.textbase.Change;
 import quill.textbase.Common;
 import quill.textbase.DataLayer;
 import quill.textbase.Extract;
 import quill.textbase.Folio;
 import quill.textbase.FormatTextualChange;
 import quill.textbase.InsertTextualChange;
 import quill.textbase.Segment;
 import quill.textbase.Span;
 import quill.textbase.TextChain;
 
 import static quill.client.Quill.ui;
 import static quill.textbase.Span.createSpan;
 
 public class ValidateChangePropagation extends GraphicalTestCase
 {
     public final void testSetupBlank() throws ValidityException, ParsingException, IOException {
         final DataLayer data;
         final Folio folio1, folio2;
 
         data = new DataLayer();
 
         data.createDocument();
         folio1 = data.getActiveDocument();
         assertNotNull(folio1);
 
         data.createDocument();
         folio2 = data.getActiveDocument();
         assertNotNull(folio2);
 
         assertNotSame(folio1, folio2);
     }
 
     public final void testInsertText() throws ValidityException, ParsingException, IOException {
         final DataLayer data;
         final Folio folio;
         final Segment segment;
         final TextChain chain;
         final Change change;
         final Span span;
         final OutputStream out;
         final String expected;
 
         data = new DataLayer();
         ui = new UserInterface(data);
 
         data.createDocument();
         folio = data.getActiveDocument();
         ui.displayDocument(folio);
 
         segment = folio.get(0).get(1);
         chain = segment.getText();
         span = createSpan('h', null);
 
         change = new InsertTextualChange(chain, 0, span);
         ui.apply(change);
 
         out = new ByteArrayOutputStream();
         data.saveDocument(out);
 
         expected = combine(new String[] {
                 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                 "<chapter schema=\"0.1\" xmlns=\"http://operationaldynamics.com/quack\">",
                 "<text>",
                 "h",
                 "</text>",
                 "</chapter>"
         });
         assertEquals(expected, out.toString());
     }
 
     public final void skipReplaceText() throws ValidityException, ParsingException, IOException {
         final DataLayer data;
         final Folio folio;
         final Segment segment;
         final TextChain chain;
         Change change;
         final Span span;
         OutputStream out;
         String expected;
         final EditorTextView editor;
         final TextBuffer buffer;
         TextIter start, end;
 
         data = new DataLayer();
         ui = new UserInterface(data);
 
         data.createDocument();
         folio = data.getActiveDocument();
         ui.displayDocument(folio);
 
         /*
          * Establish some starting text.
          */
 
         segment = folio.get(0).get(1);
         chain = segment.getText();
         span = createSpan("This is a test of the emergency broadcast system", null);
 
         change = new InsertTextualChange(chain, 0, span);
         ui.apply(change);
 
         out = new ByteArrayOutputStream();
         data.saveDocument(out);
 
         expected = combine(new String[] {
                 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                 "<chapter schema=\"0.1\" xmlns=\"http://operationaldynamics.com/quack\">",
                 "<text>",
                 "This is a test of the emergency broadcast",
                 "system",
                 "</text>",
                 "</chapter>"
         });
         assertEquals(expected, out.toString());
 
         /*
          * Now attempt to simulate the user replacing some of the text by
          * doing a recursive descent to find the editor then using the
          * interactive methods on its TextBuffer. Our code makes the
          * assumption that calls within the user-action pairs result from,
          * well, user action :) and so this causes the editor to raise and
          * apply Changes.
          */
 
         editor = (EditorTextView) findEditor(ui.primary.getChild());
 
         buffer = editor.getBuffer();
         start = buffer.getIter(9);
         end = buffer.getIter(21);
         buffer.selectRange(start, end);
         assertEquals(" test of the", buffer.getText(start, end, true));
         Test.cycleMainLoop();
         Test.sendKey(editor, Keyval.n, ModifierType.NONE);
         Test.cycleMainLoop();
 
        /*
         * We've still got the TextChain backing the Segment of this
         * EditorTextView, so first check to make sure it actually did what we
         * want (thereby passing this test fixture)
         */

        assertEquals("This is an emergency broadcast system", chain.toString());

        /*
         * Finally, just check the rest of the stack; the code was already
         * here.
         */

         out = new ByteArrayOutputStream();
         data.saveDocument(out);
 
         expected = combine(new String[] {
                 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                 "<chapter schema=\"0.1\" xmlns=\"http://operationaldynamics.com/quack\">",
                 "<text>",
                 "This is an emergency broadcast system",
                 "</text>",
                 "</chapter>"
         });
         assertEquals(expected, out.toString());
     }
 
     public final void skipFormatOnInsertion() throws ValidityException, ParsingException, IOException {
         final DataLayer data;
         final Folio folio;
         final Segment segment;
         final TextChain chain;
         Change change;
         Span span;
         Extract extract;
         OutputStream out;
         String expected;
         final EditorTextView editor;
         final TextBuffer buffer;
         TextIter start;
 
         data = new DataLayer();
         ui = new UserInterface(data);
 
         data.createDocument();
         folio = data.getActiveDocument();
         ui.displayDocument(folio);
 
         /*
          * Establish some starting text.
          */
 
         segment = folio.get(0).get(1);
         chain = segment.getText();
 
         span = createSpan("This is a test of the emergency broadcast system", null);
         change = new InsertTextualChange(chain, 0, span);
         ui.apply(change);
 
         span = createSpan("emergency", null);
         extract = TextChain.extractFor(span);
         change = new FormatTextualChange(chain, 22, extract, Common.ITALICS);
         ui.apply(change);
 
         out = new ByteArrayOutputStream();
         data.saveDocument(out);
 
         expected = combine(new String[] {
                 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                 "<chapter schema=\"0.1\" xmlns=\"http://operationaldynamics.com/quack\">",
                 "<text>",
                "This is a test of the <italics>emergency</italics>",
                "broadcast system",
                 "</text>",
                 "</chapter>",
         });
         assertEquals(expected, out.toString());
 
         /*
          * Now attempt to simulate the user inserting text before. Moving the
          * cursor first is important, as it fires off the logic that sets
          * insertMarkup in EditorTextView.
          */
 
         editor = (EditorTextView) findEditor(ui.primary.getChild());
 
         buffer = editor.getBuffer();
         start = buffer.getIter(22);
         assertEquals('e', start.getChar());
 
         buffer.beginUserAction();
         buffer.placeCursor(start);
         buffer.endUserAction();
 
         buffer.beginUserAction();
         buffer.insertAtCursor("a");
         buffer.endUserAction();
 
         out = new ByteArrayOutputStream();
         data.saveDocument(out);
 
         expected = combine(new String[] {
                 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                 "<chapter schema=\"0.1\" xmlns=\"http://operationaldynamics.com/quack\">",
                 "<text>",
                 "This is a test of the",
                 "a<italics>emergency</italics> broadcast",
                 "system",
                 "</text>",
                 "</chapter>"
         });
         assertEquals(expected, out.toString());
 
         /*
          * And now, the other side.
          */
 
         start = buffer.getIter(32);
         assertEquals(' ', start.getChar());
 
         buffer.beginUserAction();
         buffer.placeCursor(start);
         buffer.endUserAction();
 
         buffer.beginUserAction();
         buffer.insertAtCursor("a");
         buffer.endUserAction();
 
         out = new ByteArrayOutputStream();
         data.saveDocument(out);
 
         expected = combine(new String[] {
                 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                 "<chapter version=\"5.0\" xmlns=\"http://docbook.org/ns/docbook\">",
                 "<para>",
                 "This is a test of the",
                 "a<emphasis>emergencya</emphasis> broadcast",
                 "system",
                 "</para>",
                 "</chapter>"
         });
         assertEquals(expected, out.toString());
 
     }
 
     // recursive
     private static Widget findEditor(Widget widget) {
         final Container container;
         final Widget[] children;
         Widget child, result;
         int i;
 
         assertTrue(widget instanceof Container);
         container = (Container) widget;
         children = container.getChildren();
 
         for (i = 0; i < children.length; i++) {
             child = children[i];
 
             if (child instanceof NormalEditorTextView) {
                 return child;
             }
 
             if (child instanceof Container) {
                 result = findEditor(child);
                 if (result != null) {
                     return result;
                 }
             }
 
         }
         return null;
     }
 
     private static String combine(String[] elements) {
         StringBuilder buf;
 
         buf = new StringBuilder(128);
 
         for (String element : elements) {
             buf.append(element);
             buf.append('\n');
         }
 
         return buf.toString();
     }
 }
