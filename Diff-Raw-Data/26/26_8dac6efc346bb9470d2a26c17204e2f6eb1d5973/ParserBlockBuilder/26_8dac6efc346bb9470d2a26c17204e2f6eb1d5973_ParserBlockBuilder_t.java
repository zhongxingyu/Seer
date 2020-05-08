 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.  
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or 
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)  
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.  
  */
 
 /**
  * @author Brian Goetz
  */
 
 package org.webmacro.parser;
 
 import org.webmacro.*;
 import org.webmacro.engine.*;
 
 /** 
  * ParserBlockBuilder extends BlockBuilder, and should only be used by
  * the WMParser_impl parser.  It adds methods for eating trailing 
  * <WHITESPACE>  or  <WHITESPACE> <NL> <WHITESPACE>  that precede directives.
  * It assumes that literal text will not span elements (true for the 
  * current parser) so if the parser changes, this will need to change too.
  */
 
 
 public class ParserBlockBuilder extends BlockBuilder {
   private int literalMark = 0;
 
  /** Mark the last character in the block as being a literal (quoted
   * with backslash) so we don't eat trailing quoted whitespace.
   */
   final public void markLiteral() {
     literalMark = size();
   } 
 
   final public void eatTrailingWs() {
     int i, j;
 
     i = size() - 1;
     if ( i < 0 || i+1 == literalMark ) 
       return;
 
     Object o = elementAt(i);
     if (! (o instanceof String)) 
       return;
     String s = (String) o;
     j = s.length() - 1;
     while (j >= 0 && Character.isSpaceChar(s.charAt(j)))
       j--;
 
     if (j < 0)
       remove(i);
     else if (j < s.length() - 1)
       setElementAt(s.substring(0, j+1), i);
     markLiteral();
   }
 
 
   final public void eatTrailingWsNl() {
     int i, j;
 
     i = size() - 1;
     if ( i < 0 || i+1 == literalMark ) 
       return;
 
     Object o = elementAt(i);
     if (! (o instanceof String)) 
       return;
     String s = (String) o;
     j = s.length() - 1;
     while (j >= 0 && Character.isSpaceChar(s.charAt(j)))
       j--;
     if (j >= 0) {
       if (s.charAt(j) == '\r')
         j--;
       else if (s.charAt(j) == '\n') {
         j--;
         if (j >= 0 && s.charAt(j) == '\r')
           j--;
       }
       while (j >= 0 && Character.isSpaceChar(s.charAt(j)))
         j--;
     }
 
     if (j < 0) 
       remove(i);
     else if (j < s.length() - 1)
       setElementAt(s.substring(0, j+1), i);
     markLiteral();
   }
 
   final public void eatLeadingWsNl() {
     int i, j, l;
 
     i = size() - 1;
     if ( i < 0 || i+1 == literalMark ) 
       return;
 
     Object o = elementAt(i);
     if (! (o instanceof String)) 
       return;
     String s = (String) o;
     j = 0; l = s.length();
     while (j < l && Character.isSpaceChar(s.charAt(j)))
       j++;
     if (j < l) {
       if (s.charAt(j) == '\r')
         j++;
       else if (s.charAt(j) == '\n') {
         j++;
         if (j < l && s.charAt(j) == '\r')
           j++;
       }
     }
 
     if (j >= l) 
       remove(i);
     else if (j > 0)
       setElementAt(s.substring(j), i);
     markLiteral();
   }
 
   final public boolean directiveOk() {
     if (size() == 0 || size() == literalMark) 
       return true;
     else {
       Object o = elementAt(size() - 1);
       if (!(o instanceof String))
         return true;
       else {
         String s = (String) o;
         char ch = s.charAt(s.length()-1);
        if (ch == '=' || ch == '\'' || ch == '\"' || ch == ':'
             || Character.isLetterOrDigit(ch))
           return false;
       }
     }
     return true;
   }
 }
