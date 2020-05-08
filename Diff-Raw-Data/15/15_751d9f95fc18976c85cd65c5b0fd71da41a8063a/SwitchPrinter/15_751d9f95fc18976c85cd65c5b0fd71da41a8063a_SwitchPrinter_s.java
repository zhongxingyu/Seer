 /*
  * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. Neither the name of the Jalopy project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
  * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * $Id$
  */
 package de.hunsicker.jalopy.printer;
 
 import de.hunsicker.antlr.collections.AST;
 import de.hunsicker.jalopy.parser.JavaNode;
 import de.hunsicker.jalopy.parser.JavaTokenTypes;
 import de.hunsicker.jalopy.prefs.Defaults;
 import de.hunsicker.jalopy.prefs.Keys;
 
 import java.io.IOException;
 
 
 /**
 * Printer for switch selection statements (<code>LITERAL_switch</code>).
  * <pre style="background:lightgrey">
  * <strong>switch</strong> (<em>integral-selector</em>)
  * {
  *    case <em>integral-value1</em>:
  *        <em>statement</em>
  *        // ...
  *        break;
  *    case <em>integral-value2</em>:
  *        <em>statement</em>
  *        // ...
  *        break;
  *    case <em>integral-value3</em>:
  *        <em>statement</em>
  *        // ...
  *        break;
  * // ...
  *    default:
  *        <em>statement</em>
  * }
  * </pre>
  * which translates to:
  * <pre style="background:lightgrey">
  * LITERAL_switch
  *      + -- EXPR
  *      + ...
  *      + -- LCURLY
  *      + -- CASE_GROUP
  *      + -- LITERAL_case
  *      + -- EXPR
  *      + -- ...
  *      + -- SLIST
  *      + -- ...
  *      + -- CASE_GROUP
  *      + -- CASE_GROUP ...
  *      + -- RCURLY
  * </pre>
  *
  * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
  * @version $Revision$
  */
 final class SwitchPrinter
     extends AbstractPrinter
 {
     //~ Static variables/initializers 
 
     /** Singleton. */
     private static final Printer INSTANCE = new SwitchPrinter();
 
     //~ Constructors 
 
     /**
      * Creates a new SwitchPrinter object.
      */
     protected SwitchPrinter()
     {
     }
 
     //~ Methods 
 
     /**
      * Returns the sole instance of this class.
      *
      * @return the sole instance of this class.
      */
     public static final Printer getInstance()
     {
         return INSTANCE;
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void print(AST        node,
                       NodeWriter out)
         throws IOException
     {
         printCommentsBefore(node, out);
 
         out.print(SWITCH, JavaTokenTypes.LITERAL_switch);
 
         if (this.prefs.getBoolean(Keys.SPACE_BEFORE_STATEMENT_PAREN,
                                   Defaults.SPACE_BEFORE_STATEMENT_PAREN))
         {
             out.print(SPACE, JavaTokenTypes.LITERAL_switch);
         }
 
         AST lparen = node.getFirstChild();
         PrinterFactory.create(lparen).print(lparen, out);
 
         AST expr = lparen.getNextSibling();
         PrinterFactory.create(expr).print(expr, out);
 
         JavaNode rparen = (JavaNode)expr.getNextSibling();
         PrinterFactory.create(rparen).print(rparen, out);
 
         AST lcurly = rparen.getNextSibling();
         boolean leftBraceNewline = this.prefs.getBoolean(Keys.BRACE_NEWLINE_LEFT,
                                                          Defaults.BRACE_NEWLINE_LEFT);
 
         boolean commentsAfter = ((JavaNode)lcurly).hasCommentsAfter();
 
        if (out.column != 1)
         {
            out.printLeftBrace(leftBraceNewline, !commentsAfter,
                               !leftBraceNewline);
         }
         else
         {
            out.printLeftBrace(NodeWriter.NEWLINE_NO, !commentsAfter,
                               NodeWriter.INDENT_NO);
         }
 
         if (commentsAfter)
         {
             printCommentsAfter(lcurly, NodeWriter.NEWLINE_YES,
                                NodeWriter.NEWLINE_YES, out);
         }
 
         boolean indentCaseFromSwitch = this.prefs.getBoolean(Keys.INDENT_CASE_FROM_SWITCH,
                                                              Defaults.INDENT_CASE_FROM_SWITCH);
 
         if (!indentCaseFromSwitch)
         {
             out.unindent();
         }
 
         JavaNode rcurly = null;
 LOOP:
         for (AST child = lcurly.getNextSibling();
              child != null;
              child = child.getNextSibling())
         {
             switch (child.getType())
             {
                 case JavaTokenTypes.RCURLY :
                     rcurly = (JavaNode)child;
 
                     break LOOP;
 
                 default :
                     PrinterFactory.create(child).print(child, out);
             }
         }
 
         if (!indentCaseFromSwitch)
         {
             out.indent();
         }
 
         printCommentsBefore(rcurly, out);
         out.printRightBrace(NodeWriter.NEWLINE_NO);
 
         if (!printCommentsAfter(rcurly, NodeWriter.NEWLINE_NO,
                                 NodeWriter.NEWLINE_YES, out))
         {
             out.printNewline();
         }
 
         out.last = JavaTokenTypes.RCURLY;
     }
 }
