 
 /*
  * Copyright (c) 1998, 1999 Semiotek Inc. All Rights Reserved.
  *
  * This software is the confidential intellectual property of
  * of Semiotek Inc.; it is copyrighted and licensed, not sold.
  * You may use it under the terms of the GNU General Public License,
  * version 2, as published by the Free Software Foundation. If you 
  * do not want to use the GPL, you may still use the software after
  * purchasing a proprietary developers license from Semiotek Inc.
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See the attached License.html file for details, or contact us
  * by e-mail at info@semiotek.com to get a copy.
  */
 
 
 package org.webmacro.engine;
 import java.util.*;
 import java.io.*;
 import org.webmacro.broker.Config;
 import org.webmacro.util.*;
 import java.lang.reflect.*;
 import org.webmacro.*;
 
 /**
   * The WMParser implements the traditional WebMacro script syntax: it's
   * the first pass of a two-pass compiling routine. During this routine, 
   * the text of a template is read and transformed into a build tree. A
   * subsequent pass will translate the build tree into an executable 
   * template. 
   */
 public class WMParser implements Parser
 {
 
    static private final Log _log = new Log("parse", "WebMacro parser");
 
    private final Broker _broker;
    private final boolean _cstyle;
    private Hashtable _tools;
 
    final private static Builder END_BLOCK = new NullBuilder();
 
    public WMParser(Broker b) {
       _broker = b;
       boolean tmp = false;
       try {
          tmp = Config.isTrue( (String)
                _broker.getValue("config","C-Blocks"));
       } catch (Exception e) {
          tmp = true;
       } 
       _cstyle = tmp;
 
    }
 
 
    // Parser Interface
 
    /**
      * Return a short name that identifies this parser. This name could,
      * for example, be used as the extension for files which contain 
      * syntax parsable by this parser.
      */
    public final String getParserName() {
       return "wm";
    }
 
 
    /**
      * Parse everything up until one of the characters $ # { or }
      * appending everything read to the supplied stringbuffer. If
      * the # { } was preceeded by WS then that will not be appended
      * to buf, but rather will be appended to ws. The character read
      * will be returned.
      */
    private int parseUntilMacroChar(ParseTool in, 
          StringBuffer buf, StringBuffer ws) 
       throws IOException
    {
       boolean readSome = false;
       int start = buf.length();  
 
       int c = in.getChar();
       while (true) 
       {
          switch(c) {
 
             case '#': case '{': case '}': EOF:
                if ((start != -1) && !in.isEscaped()) {
                   String b = buf.toString();
                   ws.append(b.substring(start));
                   buf.setLength(start);
                   return c;
                } 
                break;
 
             case '$':
                if (!in.isEscaped()) {
                   return c; 
                }
                break;
 
             case ParseTool.EOF:
                return c;
 
             case '>':
                start = buf.length() + 1; // next char 1st possible start
                break;
 
             case '\n': 
                if (in.isEscaped()) {
                   start = buf.length() + 1; // next char 1st possible start
                } else {
                   start = buf.length(); // this char is 1st possible start
                }
                break;
             
             case ' ': case '\t':
                if (in.isEscaped()) {
                   start = buf.length() + 1; // next char 1st possible start
                } else {
                   if (start == -1) {
                      start = buf.length(); // this char is 1st possible start
                   }
                }
                break;
 
             default:
                start = -1;
                break;
 
          }
          buf.append((char) c);
          c = in.nextChar();
          readSome = true;
       }
    }
 
 
    /**
      * Parse a block that appears on the supplied input Reader. The 
      * name supplied is used in error messages to identify the source
      * being parsed.
      */
    public BlockBuilder parseBlock(String name, Reader in)
       throws ParseException, IOException
    {
       ParseTool pin = new ParseTool(name, in);
       BlockBuilder bb = new BlockBuilder();
       do {
          try {
             bb.addElement(parseBlock(pin)); 
          } catch (ParseException e) {
             _log.exception(e);
             if (! pin.isAtEOF()) {
                int c = pin.nextChar(); // skip a char before continuing
                bb.addElement(new Character((char) c).toString());
              }
          }
       } while (! pin.isAtEOF()); 
       return bb;
    }
 
 
    /**
      * Attempt to parse the block, or return null if what follows the 
      * current position is not actually a block. 
      * <p>
      * @exception ParseException if the sytax was invalid and we could not recover
      * @exception IOException if we could not successfullly read the parseTool
      */
    public BlockBuilder parseBlock(ParseTool in) 
       throws ParseException, IOException
    {
       return parseBlock(in, false);
    }
 
    private BlockBuilder parseBlock(ParseTool in, boolean expectEnd)
       throws ParseException, IOException
    {
       boolean parens;
 
       BlockBuilder b = new BlockBuilder();
 
       // eat open parens if in c-style
       parens = _cstyle && in.parseChar('{');
 
       // eat any opening spaces
       in.skipSpaces();
 
 
       // parse the block
 
       StringBuffer str = new StringBuffer(512); // start large to save mallocs
       Object child;
 
       boolean inBlock = (! in.isAtEOF()); // to start
       boolean lineStart;
       StringBuffer ws = new StringBuffer();
       while (inBlock)
       {
 
          ws.setLength(0);
          int cur = parseUntilMacroChar(in,str,ws);
          lineStart = ((ws.length() > 0) && (ws.charAt(0) == '\n'));
 
          // a block can contain strings, blocks, variables, and directives
          child = null;
 
          switch(cur) {
 
             case '#': // directive
                child = parseDirective(in);
                if (child == END_BLOCK) {
                   inBlock = false;
                   child = null;
                   if (! expectEnd) {
                      throw new ParseException(in, END_BLOCK + " unexpected");
                   }
                }
 
                // drop preceeding spaces
                // drop preceeding \n iff nothing on same line as end of dir
                if (lineStart && (in.getChar() != '\n')) {
                   str.append('\n'); 
                }
                break; 
 
             case '}': // end of this block
                if (_cstyle) {
                   if (parens) {
                      inBlock = false; // breaks the loop
                   } else {
                      throw new ParseException(in, "} unexpected");
                   }
                   // drop preceeding WS, '#' case already handles \n dropping
                   // (for \n dropping, assume blocks always follow directives)
                } else {
                   str.append(ws.toString());
                }
                break;
 
             case '{': // start new block
                if (_cstyle) {
                   in.skipSpaces();
                   // drop preceeding spaces
                   // drop preceeding \n if { is on its own line
                   if (lineStart && (in.getChar() != '\n')) {
                      str.append('\n');
                   }
                   child = parseBlock(in); // skips trailing spaces
                } else {
                   str.append(ws.toString());
                }
                break; 
                
 	    case ParseTool.EOF: // end this block
                str.append(ws.toString()); // preserve whitespace at EOF
 	       inBlock = false; // breaks the loop
 	       break; 
 
             case '$': // variable
                str.append(ws.toString()); // preserve whitespace before var
                child = parseVariable(in);
                break; 
 
             default: // error, we should have handled everything
                throw new ParseException(in,
                      "Parser bug: expected macro char, got " + (char) cur);
 
          }
     
          if (inBlock && (child == null)) {
             // failed to read a child, so append everything we read
             str.append((char) cur);
             in.nextChar(); 
          } else {
             // successfully read some kind of child, or we're quitting
             // so write the string we've built up 
             b.addElement(str.toString());
             str.setLength(0);
 
 	    if (child != null) {
                b.addElement(child);
 	    }
          }
       }
 
       // check for close parens
       if (parens && !in.parseChar('}')) {
          throw new ParseException(in, "expected } at end of block");
       }
 
       return b;
    }
 
 
    /**
      * Return the directive corresponding with the supplied name
      */
    final DirectiveBuilder getDirectiveBuilder(String name) 
       throws NotFoundException
    {
       try {
          return (DirectiveBuilder) _broker.getValue("directive",name);      
       } catch (Exception e) {
          throw new NotFoundException("Could not load directive " 
                + name + ": " + e);
       }
    }
 
 
    /**
      * Parse a directive. The syntax for a directive is determined by 
      * querying it to determine what data values it requires. See the
      * DirectiveBuilder class for more details. This routine also handles
      * other notations which begin with '#', the only other current on 
      * being a comment, which begins with ## and extends to the end of
      * the line. If a # is read but the preceding character does not 
      * permit its interpetation as a directive, then it will simply 
      * be returned as a string.
      */
    private Builder parseDirective(ParseTool in) 
       throws ParseException, IOException 
    {
       return parseDirective(in,false);
    }
 
    private Builder parseSubDirective(ParseTool in)
       throws ParseException, IOException
    {
       return parseDirective(in,true);
    }
 
    private Builder parseDirective(ParseTool in, boolean subDir)
       throws ParseException, IOException
    {
       Builder child = null;
       String dirName = "unknown";
       DirectiveBuilder dirB;
 
       try {
          int mark = in.mark();
          try {
             if (! in.parseChar('#')) {
                return null;
             }
 
             // try and get a name
             dirName = in.parseName();
             if (dirName == null) {
                if (in.getChar() == '#') {
                   // comment
                   if (Log.debug) _log.debug("Parsing comment");
                   in.skipToEOL(); 
                   return new NullBuilder();
                } else {
                   // just text
                   in.rewind(mark);
                   return null;
                }
             } 
 
             // special handling for "begin" and "end" directives
             if (dirName.equals("end")) {
                in.skipSpaces();
                return END_BLOCK;
             } else if (dirName.equals("begin")) {
                in.skipSpaces();
                return parseBlock(in,true);
             }
 
             // identify and validate the name
             dirB = getDirectiveBuilder(dirName);
             if (dirB == null) {
                throw new ParseException(in, "Unrecognized directive: " 
                      + dirName);
             }
             if (subDir && !dirB.isSubDirective()) {
                // we required a subdirective, but got a primary directive
                in.rewind(mark); 
                return null;
             }
 
          } finally {
             in.clearMark(mark);
          }
 
          if (Log.debug) _log.debug("Parsing #" + dirName);
 
          // if we're still here it is a directive
 
          if (dirB.hasCondition()) {
             in.skipSpaces();
             Builder cond = parseCondition(in); 
             dirB.setCondition(cond);
 
          } else if (dirB.hasTarget()) {
             in.skipSpaces();
             Object term = parseTerm(in); 
             dirB.setTarget(term);
 
             if (dirB.hasArguments()) {
 
                in.skipSpaces();
                String arg;
                Object obj;
 
                String[] args = dirB.getArgumentNames();
                while ((arg = in.parseStrings(args)) != null) 
                {
                   in.skipSpaces();
                   obj = parseTerm(in); 
                   dirB.addArgument(arg,obj);
                }
             }
          } 
 
          if (dirB.isParser()) {
             in.skipWhitespace();
             String marker = null;
             if (in.parseString("#begin")) {
                marker = "#end";
             } else if (in.parseString("{")) {
                marker = "}";
             } else {
                throw new ParseException(in, "Expected block after directive: " 
                      + dirB);
             }
             StringBuffer buf = new StringBuffer();
             in.parseUntil(buf,marker);
             buf.setLength(buf.length() - marker.length());
             dirB.setText(buf.toString());
          } else if (dirB.isContainer()) {
             in.skipWhitespace();
             if (_cstyle && in.getChar() == '{') {
                BlockBuilder b = parseBlock(in,false); 
                dirB.setContents(b);
             } else {
                if (in.parseString("#begin")) {
                   in.skipSpaces();
                   BlockBuilder b = parseBlock(in,true);
                   dirB.setContents(b);
                } else {
                   throw new ParseException(in, "Expected block after " 
                         + dirName);
                }
             }
          }
          if (dirB.isMulti()) {
             mark = in.mark();
             in.skipWhitespace(); 
             Builder dep = parseSubDirective(in); 
             if (dep instanceof DirectiveBuilder) { 
                in.clearMark(mark);
                dirB.setSubDirective(dep);
             } else {
                in.rewind(mark); // don't skip the whitespace
             }
          }
          in.skipSpaces();
          dirB.check();
          child = dirB;
       } catch (BuildException be) {
          _log.exception(be);
          throw new ParseException(in, "Error parsing directive: " 
                + be.getMessage());
       } catch (NotFoundException e) {
          _log.exception(e);
 e.printStackTrace();
          throw new ParseException(in, "Unrecognized directive: " 
                + dirName);
       }
 
       return child;
    }
 
 
    /**
      * Attempt to parse a variable name. The name will start with a '$',      
      * and the next thing on the parseTool is the variables name,
      * followed by an optional ';'. If the name begins with $$ it will be
      * considered a static parameter and must only contain a single name;
      * the value of the parameter will be returned as a string.
      * @exception ParseException on unrecoverable parse error
      * @exception IOException on failure to read from parseTool
      * @return a Variable object 
      */
    static public Object parseVariable(ParseTool in)
       throws ParseException, IOException
    {
       // check that we were called correctly 
       if (!in.parseChar('$')) {
          return null;
       }
       boolean isParam = in.parseChar('$'); // param is $$
 
       // check what kind of variable this is
 
       boolean isFiltered = false;
       char closeChar = 0;
 
       if ( in.parseChar('{') ) {
          isFiltered = true;
          closeChar = '}';
       } else if (in.parseChar('(') ) {
          closeChar = ')';
       } else if (! in.isNameStartChar()) {
          return (isParam) ? "$$" : "$";
       }
 
       Vector names = new Vector();
       Object name;
 
       while ((name = in.parseName()) != null) {
 
          // names can be method calls
          if (in.getChar() == '(') {
             ListBuilder args = parseList(in);
             name = new PropertyMethodBuilder((String) name, args);
          }
 
          names.addElement(name);
          if (! in.parseChar('.')) {
             break;
          }
       }
 
       Object[] oname = new Object[names.size()];
       names.copyInto(oname);
 
       if ((closeChar != 0) && ! in.parseChar(closeChar)) {
          throw new ParseException(in, "Expected closing " + closeChar + 
                " after variable name " + Variable.makeName(oname));
       } else {
          in.parseChar(';'); // eat optional ;
       }
 
       if (isParam) {
          if (Log.debug) 
             _log.debug("Parsed param:" + Variable.makeName(oname));
          return new ParamBuilder(oname, isFiltered); 
       } else {
          if (Log.debug) 
             _log.debug("Parsed var:" + Variable.makeName(oname));
          return new VariableBuilder(oname, isFiltered);
       }
    }
 
    /**
      * Parse a term. A term is a name or a quoted string, or 
      * a variable, or a number.  
      * <p>
      * @returns a String, Variable, or QuotedString 
      * @exception ParseException if the sytax was invalid and we could not recover
      * @exception IOException if we could not successfullly read the parseTool
      */
     static public Object parseTerm(ParseTool in)
       throws ParseException, IOException
    {
       Object term = null;;
       int last;
 
       switch(in.getChar()) {
          case '$': // variable
             term = parseVariable(in); 
             if (term instanceof String) {
                throw new ParseException(in, 
                      "Unexpected character after " + term + ": " 
                      + "expected variable name start, instead got " 
                      + in.getChar());
             }
             break;
 
          case '[': // list
             term = parseList(in);
             break;
 
          case '\"': case '\'':
             term = parseQuotedString(in);
             break;
 
          default:
             term = in.parseName();
             if (term == null) {
                term = in.parseNumber();
             } else if (term.equals("false") || term.equals("FALSE") || term.equals("False")) {
                return Boolean.FALSE;
             } else if (term.equals("true") || term.equals("TRUE") || term.equals("True")) {
                return Boolean.TRUE;
             } 
             break;
       }
       if (Log.debug) 
          _log.debug("Parsed term:" + term);
       return term;
    }
 
    
    /**
      * Beginning with the quotation mark, parse everything up until the 
      * close quotation mark. It is a parse error if EOL or EOF happen 
      * before the end of the close quotation mark. The quoted string can 
      * begin with either a single or double quotation mark, and then it 
      * must end with the same mark. Inside a quoted string, variables 
      * and parameters are recognized and properly substituted.
      * @exception ParseException on unrecoverable parse error
      * @exception IOException on failure to read from parseTool
      */
    static public Object parseQuotedString(ParseTool in) 
       throws ParseException, IOException
    {
 
       int quoteChar = in.getChar();
       boolean isMacro = false;
 
       if ((quoteChar != '\'') && (quoteChar != '\"')) {
          return null; // undefined quote char
       }
 
       StringBuffer str = new StringBuffer(96);
 
       QuotedStringBuilder qString = new QuotedStringBuilder();
 
       int c = in.nextChar();
       while ((c != quoteChar) && (c != in.EOF)) {
         if ((c == '$') && !in.isEscaped()) {
             Object child = parseVariable(in); // may return a string 
             c = in.getChar();
 
             if (child instanceof String) {
                str.append(child);
             } else {
                qString.addElement(str.toString());
                qString.addElement(child);
                str.setLength(0);
             }
          } else {
             str.append((char) c);
             c = in.nextChar();
          } 
       }
 
       if (str.length() != 0) {
          qString.addElement(str.toString());
       }
       str = null;
 
       if (c == quoteChar) {
          in.nextChar();
       } else {
          throw new ParseException(in, "Expected closing quote: " 
                + (char) quoteChar);
       }
 
       if ((qString.size() == 1) && !(qString.elementAt(0) instanceof Builder)) 
       {
          // XXX: #use directive fails without this, because it needs to 
          // access target and source during the parse phase, before the 
          // build has been completed. 
          return qString.elementAt(0); 
       } else {
          return qString;
       }
    }
 
    
    /**
      * Create a Macro representing a List. These evaluate to
      * type Objects[], after recursively resolving macros in the list.
      * This list values may optionally be separated by commas.
      */
    static public ListBuilder parseList(ParseTool in) 
       throws ParseException, IOException
    {
 
       // find the start char, identify end char
 
       int endChar;
       switch (in.getChar()) {
          case '(': endChar = ')'; break;
          case '[': endChar = ']'; break;
          default : return null;
       }
 
       // create the list object
       ListBuilder newList = new ListBuilder();
       in.nextChar(); // we ate it
       in.skipSpaces();
 
       Object term;
       while ((term = parseTerm(in)) != null) {
          newList.addElement(term);
          in.skipSpaces(); 
          if (in.parseChar(',')) { in.skipSpaces(); }
       }
 
       // find the end character
       if (! in.parseChar((char) endChar) ) {
          throw new ParseException(in, "Expected end of list, instead got " 
                + in.getChar());
       }
 
       return newList;
    }
 
 
    /** 
      * Parse the expression and return a macro that will evaluate
      * to Boolean.TRUE or Boolean.FALSE depending on the condition.
      * <p>
      * @return boolean
      * @exception ParseException if the sytax was invalid and we could not recover
      * @exception IOException if we could not successfullly read the parseTool
      */
    public Builder parseCondition(ParseTool in) 
       throws ParseException, IOException
    {
       Builder cond;
       boolean parens;
 
       // get parens
       parens = in.parseChar('(');
       in.skipSpaces();
 
       // get lhs and possibly only sub-expression
       switch(in.getChar())
       {
          case '!': cond = parseNotCondition(in); break;
          case '(': cond = parseCondition(in); break;
          default : cond = parseTermCondition(in); break;
       }
       if (null == cond) {
          throw new ParseException(in,"Expected term/expression, got: " 
                + (char) in.getChar());
       }
 
       // read left-associative operators
       boolean moreTerms = true;
       while(moreTerms) {
          Builder oper;
          in.skipSpaces();
          switch(in.getChar()) {
             case '!': oper = parseNotEqualCondition(cond,in); break;
             case '=': oper = parseEqualCondition(cond,in); break;
             case '&': oper = parseAndCondition(cond,in); break;
             case '|': oper = parseOrCondition(cond,in); break;
             default : oper = null;
          }
          if (oper == null) {
             moreTerms = false;
          } else {
             cond = oper;
          }
       }
      
       // check for close paren
       in.skipSpaces();
       if (parens && !in.parseChar(')')) {
          throw new ParseException(in,"Mismatched braces around expression.");
       }
       
       return cond;
    }
 
    /**
      * Utility function to parse the operator and right term of a binary cond
      */
    private Builder parseBinOp(char[] opChars, ParseTool in) 
       throws IOException, ParseException
    {
       for (int i = 0; i < opChars.length; i++){
          if (! in.parseChar(opChars[i])) {
             if (i == 0) {
                return null;
             } else {
                throw new ParseException(in, "Expected character " + opChars[i] 
                      + " after " + opChars[i - 1] + " but got " 
                      + (char) in.getChar());
             } 
          }
       }
       in.skipSpaces();
 
       Builder right = parseCondition(in); 
       if (null == right) {
          throw new ParseException(in,"Expected term/expression after operator "
                 +" but got: " + (char) in.getChar());
       }
       return right;
    }
    
    
    private Builder parseNotCondition( ParseTool in ) 
       throws ParseException, IOException
    {
       if (! in.parseChar('!')) {
          return null;
       }
       in.skipSpaces();
       Builder cond = parseCondition(in);
       return new NotConditionBuilder(cond);
    }
    
    
    private Builder parseTermCondition(ParseTool in) 
       throws ParseException, IOException
    {
       Object term = parseTerm(in);
       return new TermConditionBuilder(term);
    }
 
    private Builder parseEqualCondition(Builder left, ParseTool in)
       throws ParseException, IOException
    {
       final char[] opchars = { '=','=' };
       Builder right = parseBinOp(opchars, in);
       if (null == right) {
          return null;
       }
       return new EqualConditionBuilder(left,right);
    }
    
    private Builder parseNotEqualCondition(Builder left, ParseTool in)
       throws ParseException, IOException
    {
       final char[] opchars = { '!','=' };
       Builder right = parseBinOp(opchars, in);
       if (null == right) {
          return null;
       }
       Builder eq = new EqualConditionBuilder(left,right);
       return new NotConditionBuilder(eq);
    }
 
    private Builder parseAndCondition(Builder left, ParseTool in)
       throws ParseException, IOException
    {
       final char[] opchars = { '&','&' };
       Builder right = parseBinOp(opchars, in);
       if (null == right) {
          return null;
       }
       return new AndConditionBuilder(left,right);
    }
    
    private Builder parseOrCondition(Builder left, ParseTool in)
       throws ParseException, IOException
    {
       final char[] opchars = { '|','|' };
       Builder right = parseBinOp(opchars, in);
       if (null == right) {
          return null;
       }
       return new OrConditionBuilder(left,right);
    }
 
 }
 
 
