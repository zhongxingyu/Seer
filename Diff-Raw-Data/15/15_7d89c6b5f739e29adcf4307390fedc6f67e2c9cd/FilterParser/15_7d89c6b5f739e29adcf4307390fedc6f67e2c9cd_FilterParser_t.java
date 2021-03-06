 /*
  * Copyright 2006 Amazon Technologies, Inc. or its affiliates.
  * Amazon, Amazon.com and Carbonado are trademarks or registered trademarks
  * of Amazon Technologies, Inc. or its affiliates.  All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.amazon.carbonado.filter;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.amazon.carbonado.Storable;
 import com.amazon.carbonado.MalformedFilterException;
 
 import com.amazon.carbonado.info.ChainedProperty;
 import com.amazon.carbonado.info.StorableInfo;
 import com.amazon.carbonado.info.StorableIntrospector;
 import com.amazon.carbonado.info.StorableProperty;
 
 /**
  *
  *
  * @author Brian S O'Neill
  */
 class FilterParser<S extends Storable> {
     private final Class<S> mType;
     private final String mFilter;
     private int mPos;
 
     FilterParser(Class<S> type, String filter) {
         if (type == null) {
             throw new IllegalArgumentException();
         }
         if (filter == null) {
             throw new IllegalArgumentException("Query filter must not be null");
         }
         mType = type;
         mFilter = filter;
     }
 
     // Design note: This parser is actually a scanner, parser, and type checker
     // all rolled into one. This is okay since the grammar is so simple.
 
     Filter<S> parseRoot() {
         Filter<S> filter = parseFilter();
         int c = nextCharIgnoreWhitespace();
         if (c >= 0) {
             mPos--;
             throw error("Unexpected trailing characters");
         }
         return filter;
     }
 
     private Filter<S> parseFilter() {
         return parseOrFilter();
     }
 
     private Filter<S> parseOrFilter() {
         Filter<S> filter = parseAndFilter();
         while (true) {
             int c = nextCharIgnoreWhitespace();
             if (c == '|') {
                 operatorCheck();
                 filter = filter.or(parseAndFilter());
             } else {
                 mPos--;
                 break;
             }
         }
         return filter;
     }
 
     private Filter<S> parseAndFilter() {
         Filter<S> filter = parseNotFilter();
         while (true) {
             int c = nextCharIgnoreWhitespace();
             if (c == '&') {
                 operatorCheck();
                 filter = filter.and(parseNotFilter());
             } else {
                 mPos--;
                 break;
             }
         }
         return filter;
     }
 
     private Filter<S> parseNotFilter() {
         int c = nextCharIgnoreWhitespace();
         if (c == '!') {
             return parseEntityFilter().not();
         } else {
             mPos--;
             return parseEntityFilter();
         }
     }
 
     private Filter<S> parseEntityFilter() {
         int c = nextCharIgnoreWhitespace();
         if (c == '(') {
             Filter<S> test = parseFilter();
             c = nextCharIgnoreWhitespace();
             if (c != ')') {
                 mPos--;
                 throw error("Right paren expected");
             }
             return test;
         } else {
             mPos--;
             return parsePropertyFilter();
         }
     }
 
     private PropertyFilter<S> parsePropertyFilter() {
         ChainedProperty<S> chained = parseChainedProperty();
         int c = nextCharIgnoreWhitespace();
 
         RelOp op;
         switch (c) {
         case '=':
             op = RelOp.EQ;
             operatorCheck();
             break;
         case '!':
             c = nextChar();
             if (c == '=') {
                 op = RelOp.NE;
             } else {
                 mPos--;
                 throw error("Inequality operator must be specified as '!='");
             }
             operatorCheck();
             break;
         case '<':
             c = nextChar();
             if (c == '=') {
                 op = RelOp.LE;
             } else {
                 mPos--;
                 op = RelOp.LT;
             }
             operatorCheck();
             break;
         case '>':
             c = nextChar();
             if (c == '=') {
                 op = RelOp.GE;
             } else {
                 mPos--;
                 op = RelOp.GT;
             }
             operatorCheck();
             break;
         case '?':
             mPos--;
             throw error("Relational operator missing");
         case -1:
             throw error("Relational operator expected");
         default:
             mPos--;
             throw error("Unexpected operator character: '" + (char)c + '\'');
         }
 
         c = nextCharIgnoreWhitespace();
 
         if (c != '?') {
             mPos--;
             throw error("Parameter placeholder '?' required");
         }
 
         return PropertyFilter.getCanonical(chained, op, 0);
     }
 
     private void operatorCheck() {
         int c = nextChar();
         mPos--;
         switch (c) {
         case -1:
         case '?':
         case '(': case ')':
         case ' ': case '\r': case '\n': case '\t': case '\0':
             return;
         default:
             if (Character.isWhitespace(c)) {
                 return;
             }
         }
         if (!Character.isJavaIdentifierStart(c)) {
             throw error("Unknown operator");
         }
     }
 
     @SuppressWarnings("unchecked")
     ChainedProperty<S> parseChainedProperty() {
         String ident = parseIdentifier();
         StorableProperty<S> prime =
             StorableIntrospector.examine(mType).getAllProperties().get(ident);
         if (prime == null) {
             mPos -= ident.length();
             throw error("Property \"" + ident + "\" not found for type: \"" +
                         mType.getName() + '"');
         }
 
         if (nextCharIgnoreWhitespace() != '.') {
             mPos--;
             return ChainedProperty.get(prime);
         }
 
         List<StorableProperty<?>> chain = new ArrayList<StorableProperty<?>>(4);
        Class<?> type = prime.getType();
 
         while (true) {
             ident = parseIdentifier();
             if (Storable.class.isAssignableFrom(type)) {
                 StorableInfo<?> info =
                     StorableIntrospector.examine((Class<? extends Storable>) type);
                 Map<String, ? extends StorableProperty<?>> props = info.getAllProperties();
                 StorableProperty<?> prop = props.get(ident);
                 if (prop == null) {
                     mPos -= ident.length();
                     throw error("Property \"" + ident + "\" not found for type: \"" +
                                 type.getName() + '"');
                 }
                 chain.add(prop);
                 type = prop.isJoin() ? prop.getJoinedType() : prop.getType();
             } else {
                 throw error("Property \"" + ident + "\" not found for type \"" +
                            type.getName() + "\" because it has no properties");
             }
             if (nextCharIgnoreWhitespace() != '.') {
                 mPos--;
                 break;
             }
         }
 
         return ChainedProperty.get(prime, (StorableProperty<?>[]) chain.toArray(new StorableProperty[chain.size()]));
     }
 
     private String parseIdentifier() {
         int start = mPos;
         int c = nextChar();
         if (c < 0) {
             throw error("Identifier expected");
         }
         if (!Character.isJavaIdentifierStart(c)) {
             mPos--;
             throw error("Not a valid character for start of identifier: '" + (char)c + '\'');
         }
         do {
             c = nextChar();
         } while (Character.isJavaIdentifierPart(c));
 
         return mFilter.substring(start, --mPos);
     }
 
     /**
      * Returns -1 if no more characters.
      */
     private int nextChar() {
         String filter = mFilter;
         int pos = mPos;
         int c = (pos >= filter.length()) ? -1 : mFilter.charAt(pos);
         mPos = pos + 1;
         return c;
     }
 
     private int nextCharIgnoreWhitespace() {
         int c;
         while ((c = nextChar()) >= 0) {
             switch (c) {
             case ' ': case '\r': case '\n': case '\t': case '\0':
                 break;
             default:
                 if (Character.isWhitespace(c)) {
                     break;
                 }
                 return c;
             }
         }
         return c;
     }
 
     private MalformedFilterException error(String message) {
         return error(message, mPos);
     }
 
     private MalformedFilterException error(String message, int pos) {
         if (pos <= 0 || mFilter.length() == 0) {
             message += " (at start of filter expession)";
         } else if (pos >= mFilter.length()) {
             message += " (at end of filter expression)";
         } else {
             // Show the next 20 characters, or show 17 + ellipsis if more than 20.
             int remaining = mFilter.length() - pos;
             if (remaining <= 20) {
                 message = message + " (at \"" + mFilter.substring(pos) + "\")";
             } else {
                 message = message + " (at \"" + mFilter.substring(pos, pos + 17) + "...\")";
             }
         }
         return new MalformedFilterException(mFilter, message, pos);
     }
 }
