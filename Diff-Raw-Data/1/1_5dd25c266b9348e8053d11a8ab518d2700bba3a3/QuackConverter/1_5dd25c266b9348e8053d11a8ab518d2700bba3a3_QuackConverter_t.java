 /*
  * QuackConverter.java
  *
  * Copyright (c) 2009 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the program it is a part of, are made available
  * to you by its authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  * 
  * Forked from src/quill/docbook/DocBookConverter.java
  */
 package quill.quack;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import quill.textbase.Common;
 import quill.textbase.ComponentSegment;
 import quill.textbase.Extract;
 import quill.textbase.HeadingSegment;
 import quill.textbase.MarkerSpan;
 import quill.textbase.Markup;
 import quill.textbase.NormalSegment;
 import quill.textbase.Preformat;
 import quill.textbase.PreformatSegment;
 import quill.textbase.QuoteSegment;
 import quill.textbase.Segment;
 import quill.textbase.Span;
 import quill.textbase.Special;
 import quill.textbase.TextChain;
 
 /**
  * Build a DocBook XOM tree equivalent to the data in our textbase, ready for
  * subsequent serialization (and thence saving to disk).
  * 
  * @author Andrew Cowie
  */
 /*
  * Build up Elements character by character. While somewhat plodding, this
  * allows us to create new Paragraphs etc as newlines are encountered.
  */
 public class QuackConverter
 {
     private Component component;
 
     private final StringBuilder buf;
 
     /**
      * The current internal block we are working through
      */
     private Segment segment;
 
     /**
      * Current output block we are building up.
      */
 
     private Block block;
 
     private Inline inline;
 
     public QuackConverter() {
         buf = new StringBuilder();
     }
 
     /**
      * Append a Segment.
      */
     public void append(final Segment segment) {
         final TextChain chain;
 
         this.segment = segment;
 
         if (segment instanceof ComponentSegment) {
             component = new ChapterElement();
             block = new TitleElement();
         } else if (segment instanceof HeadingSegment) {
             block = new HeadingElement();
         } else if (segment instanceof PreformatSegment) {
             block = new CodeElement();
         } else if (segment instanceof QuoteSegment) {
             block = new QuoteElement();
         } else if (segment instanceof NormalSegment) {
             block = new TextElement();
         }
        inline = null;
 
         chain = segment.getText();
         if ((chain == null) || (chain.length() == 0)) {
             return;
         }
 
         component.add(block);
         append(chain);
     }
 
     private void append(final TextChain chain) {
         final Extract entire;
         final int num;
         int i, j, len;
         Span span;
         Markup previous, markup;
 
         if (chain == null) {
             return;
         }
 
         entire = chain.extractAll();
         if (entire == null) {
             return;
         }
 
         num = entire.size();
         previous = entire.get(0).getMarkup();
         start(previous);
 
         for (i = 0; i < num; i++) {
             span = entire.get(i);
 
             markup = span.getMarkup();
             if (markup != previous) {
                 finish();
                 start(markup);
                 previous = markup;
             }
 
             if (span instanceof MarkerSpan) {
                 process(span.getText());
             } else {
                 len = span.getWidth();
                 for (j = 0; j < len; j++) {
                     process(span.getChar(j));
                 }
             }
         }
 
         /*
          * Finally, we need to deal with the fact that TextStacks (like the
          * TextBuffers they back) do not end with a paragraph separator, so we
          * need to act to close out the last block.
          */
         finish();
     }
 
     /**
      * Start a new element. This is a somewhat complicated expression, as it
      * counts for the case of returning from Inline to Block as well as
      * nesting Inlines into Blocks.
      */
     private void start(Markup format) {
         /*
          * Are we returning from an inline to block level? If so, we're
          * already nested and can just reset the state and escape.
          */
         if (inline != null) {
             inline = null;
             return;
         }
 
         /*
          * Otherwise, we're either starting a new block or a new inline. Deal
          * with the Block cases first:
          */
 
         if (format == null) {
             return;
         }
 
         /*
          * Failing that, we cover off all the the Inline cases:
          */
 
         if (inline == null) {
             if (format == Common.FILENAME) {
                 inline = new FilenameElement();
             } else if (format == Common.TYPE) {
                 inline = new TypeElement();
             } else if (format == Common.FUNCTION) {
                 inline = new FunctionElement();
             } else if (format == Common.ITALICS) {
                 inline = new ItalicsElement();
             } else if (format == Common.BOLD) {
                 inline = new BoldElement();
             } else if (format == Common.LITERAL) {
                 inline = new LiteralElement();
             } else if (format == Common.APPLICATION) {
                 inline = new ApplicationElement();
             } else if (format == Common.COMMAND) {
                 inline = new CommandElement();
             } else if (format == Preformat.USERINPUT) {
                 // boom?
             } else if (format == Special.NOTE) {
                 inline = new NoteElement();
             } else if (format == Special.CITE) {
                 inline = new CiteElement();
             } else {
                 throw new IllegalStateException();
             }
         }
     }
 
     /**
      * Add accumulated text to the pending element. Reset the accumulator.
      */
     private void finish() {
         if (buf.length() == 0) {
             /*
              * At the moment we have no empty tags, and so nothing that would
              * cause us to flush something with no content. When we do, we'll
              * handle it here.
              */
             return;
         }
 
         if (inline != null) {
             inline.add(buf.toString());
             block.add(inline);
         } else {
             block.add(buf.toString());
         }
         buf.setLength(0);
     }
 
     /**
      * Accumulate a character. If the character is '\n' and we're not in a
      * PreformatSegment then we create a new Block. Setting segment to null is
      * how we signal the end.
      */
     private void process(int ch) {
         if (ch == '\n') {
             finish();
             if (inline != null) {
                 inline = null;
             }
             if (segment instanceof NormalSegment) {
                 block = new TextElement();
             } else if (segment instanceof PreformatSegment) {
                 buf.append('\n');
                 return;
             } else if (segment instanceof QuoteSegment) {
                 block = new QuoteElement();
             } else {
                 throw new IllegalStateException("\n" + "Newlines aren't allowed in " + block.toString());
             }
             component.add(block);
         } else {
             buf.appendCodePoint(ch);
         }
     }
 
     /**
      * Special case for handling the bodies of MarkerSpans -> empty Elements'
      * attributes.
      */
     private void process(String str) {
         buf.append(str);
     }
 
     /**
      * Create a <code>&lt;chapter&gt;</code> object based on what has been fed
      * to the converter, and write it to the given stream.
      */
     public void writeChapter(OutputStream out) throws IOException {
         final ChapterElement chapter;
 
         chapter = (ChapterElement) component;
         chapter.toXML(out);
     }
 
     public void writeArticle(OutputStream out) throws IOException {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 }
