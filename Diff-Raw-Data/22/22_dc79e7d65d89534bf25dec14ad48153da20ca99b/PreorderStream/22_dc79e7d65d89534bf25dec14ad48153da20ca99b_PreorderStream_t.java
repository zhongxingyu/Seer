 /**
  *
  * Copyright 2010-2011 (C) The original author or authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.toolazydogs.aunit.internal;
 
 /**
  *
  */
 public class PreorderStream
 {
     private final String characters;
     private int ptr = 0;
     private StringBuilder pretty = new StringBuilder();
     private int indent = -1;
     private boolean tokenRead = false;
 
     public PreorderStream(String characters)
     {
         this.characters = characters;
     }
 
     public String token()
     {
         while (ptr < characters.length() && Character.isWhitespace(characters.charAt(ptr))) ptr++;
 
         if (tokenRead) pretty.append(" ");
         else tokenRead = true;
 
         String token;
         boolean quoted = characters.charAt(ptr) == '\'';
         if (quoted)
         {
            pretty.append("'");

             ptr++;
             StringBuilder builder = new StringBuilder();
 
             while (ptr < characters.length()
                    && characters.charAt(ptr) != '\'')
             {
                if (characters.charAt(ptr) == '\\')
                {
                    pretty.append('\\');
                    ptr++;
                }
                pretty.append(characters.charAt(ptr));
                 builder.append(characters.charAt(ptr++));
             }
             ptr++;
 
             token = builder.toString();
 
            pretty.append("'");
         }
         else
         {
             int start = ptr;
             while (ptr < characters.length()
                    && !Character.isWhitespace(characters.charAt(ptr))
                    && characters.charAt(ptr) != '('
                    && characters.charAt(ptr) != ')')
             {
                 ptr++;
             }
             token = characters.substring(start, ptr);
 
             pretty.append(token);
         }
 
 
         return token;
     }
 
     public void leftparen() throws PreorderException
     {
         while (ptr < characters.length() && Character.isWhitespace(characters.charAt(ptr)) && characters.charAt(ptr) != '(') ptr++;
         if (characters.charAt(ptr) != '(') throw new PreorderException(prettyString(), "(", erroneousToken());
         ptr++;
 
         tokenRead = false;
         if (pretty.length() != 0) pretty.append("\n");
         indent++;
         printIndent(pretty).append("(");
     }
 
     public void rightparen() throws PreorderException
     {
         while (ptr < characters.length() && Character.isWhitespace(characters.charAt(ptr)) && characters.charAt(ptr) != ')') ptr++;
         if (characters.charAt(ptr) != ')') throw new PreorderException(prettyString(), ")", erroneousToken());
         ptr++;
 
         indent--;
         pretty.append(")");
     }
 
     public void done() throws PreorderException
     {
         while (ptr < characters.length() && Character.isWhitespace(characters.charAt(ptr))) ptr++;
         if (ptr < characters.length()) throw new PreorderException(prettyString(), "<EOD>", erroneousToken());
     }
 
     @Override
     public String toString()
     {
         return characters.substring(ptr);
     }
 
     public String prettyString()
     {
         return pretty.toString();
     }
 
     private StringBuilder printIndent(StringBuilder sb)
     {
         for (int i = 0; i < indent; i++) sb.append("  ");
         return sb;
     }
 
     private String erroneousToken()
     {
         StringBuilder builder = new StringBuilder();
 
         int index = ptr;
         if (characters.charAt(index) == '\'')
         {
             index++;
 
             while (index < characters.length()
                    && characters.charAt(index) != '\'')
             {
                 if (characters.charAt(index) == '\\') index++;
                 builder.append(characters.charAt(index++));
             }
         }
         else if (characters.charAt(index) == '(' || characters.charAt(index) == ')')
         {
             builder.append(characters.charAt(index));
         }
         else
         {
             int start = index;
             while (index < characters.length()
                    && !Character.isWhitespace(characters.charAt(index))
                    && characters.charAt(index) != '('
                    && characters.charAt(index) != ')')
             {
                 index++;
             }
             builder.append(characters.substring(start, index));
         }
 
         return builder.toString();
     }
 }
