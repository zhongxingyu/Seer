 /**
  * MVEL 2.0
  * Copyright (C) 2007 The Codehaus
  * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.mvel2;
 
 import org.mvel2.compiler.AbstractParser;
 import static org.mvel2.util.ParseTools.isWhitespace;
 import static org.mvel2.util.ParseTools.isIdentifierPart;
 import org.mvel2.util.StringAppender;
 import org.mvel2.util.ParseTools;
 
 import java.util.Map;
 
 /**
  * A simple, fast, macro processor.  This processor works by simply replacing a matched identifier with a set of code.
  */
 public class MacroProcessor extends AbstractParser implements PreProcessor {
     private Map<String, Macro> macros;
 
     public MacroProcessor() {
     }
 
     public MacroProcessor(Map<String, Macro> macros) {
         this.macros = macros;
     }
 
     public char[] parse(char[] input) {
         setExpression(input);
 
         StringAppender appender = new StringAppender();
 
         int start;
         boolean macroArmed = true;
         String token;
 
         for (; cursor < length; cursor++) {
             start = cursor;
             while (cursor < length && isIdentifierPart(expr[cursor])) cursor++;
             if (cursor > start) {
                 if (macros.containsKey(token = new String(expr, start, cursor - start)) && macroArmed) {
                     appender.append(macros.get(token).doMacro());
                 }
                 else {
                     appender.append(token);
                 }
             }
 
             if (cursor < length) {
                 switch (expr[cursor]) {
                     case '\\':
                         cursor++;
                         break;
                     case '/':
                         start = cursor;
                        skipCommentBlock();
                         appender.append(new String(expr, start, cursor - start));
 
                        if (cursor >= length || isIdentifierPart(expr[cursor--])) break;
                         break;
 
                     case '"':
                     case '\'':
                         start = cursor;
                         cursor = ParseTools.captureStringLiteral(expr[cursor], expr, cursor, length);
                         appender.append(new String(expr, start, cursor - start));
 
                        if (cursor >= length || isIdentifierPart(expr[cursor])) break;
 
                     default:
                         switch (expr[cursor]) {
                             case '.':
                                 macroArmed = false;
                                 break;
                             case ';':
                                 macroArmed = true;
                                 break;
                         }
 
                         appender.append(expr[cursor]);
                 }
             }
         }
 
         return appender.toChars();
     }
 
     public String parse(String input) {
         return new String(parse(input.toCharArray()));
     }
 
     public Map<String, Macro> getMacros() {
         return macros;
     }
 
     public void setMacros(Map<String, Macro> macros) {
         this.macros = macros;
     }
 
     public void captureToWhitespace() {
         while (cursor < length && !isWhitespace(expr[cursor])) cursor++;
     }
 }
