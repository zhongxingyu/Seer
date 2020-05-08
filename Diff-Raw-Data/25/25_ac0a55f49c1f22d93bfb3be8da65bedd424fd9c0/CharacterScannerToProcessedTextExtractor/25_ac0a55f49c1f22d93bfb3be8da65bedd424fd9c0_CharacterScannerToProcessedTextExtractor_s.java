 /*******************************************************************************
  * Copyright Technophobia Ltd 2012
  * 
  * This file is part of the Substeps Eclipse Plugin.
  * 
  * The Substeps Eclipse Plugin is free software: you can redistribute it and/or modify
  * it under the terms of the Eclipse Public License v1.0.
  * 
  * The Substeps Eclipse Plugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * Eclipse Public License for more details.
  * 
  * You should have received a copy of the Eclipse Public License
  * along with the Substeps Eclipse Plugin.  If not, see <http://www.eclipse.org/legal/epl-v10.html>.
  ******************************************************************************/
 package com.technophobia.substeps.document.content.feature.partiton;
 
 import org.eclipse.jface.text.rules.ICharacterScanner;
 import org.eclipse.jface.text.rules.IToken;
 
 public class CharacterScannerToProcessedTextExtractor implements TextExtractor<ICharacterScanner, IToken> {
 
     private static final int EOF = -1;
     private static final int NEWLINE = '\n';
     private static final int COMMENT = '#';
 
     private final TextProcessor<IToken> textProcessor;
 
 
     public CharacterScannerToProcessedTextExtractor(final TextProcessor<IToken> textProcessor) {
         this.textProcessor = textProcessor;
     }
 
 
     @Override
     public IToken extractText(final ICharacterScanner scanner) {
         final String text = readLine(scanner);
        final IToken token = textProcessor.doWithText(text.trim());
 
         // Didn't find it, so unread everything
        if (token == null) {
             unread(scanner, text.length());
         }
 
         return token;
     }
 
 
     private String readLine(final ICharacterScanner scanner) {
         final StringBuilder sb = new StringBuilder();
         int c = scanner.read();
         while (c != EOF && c != COMMENT) {
             sb.append((char) c);
             c = scanner.read();
 
             if (c == NEWLINE) {
                 sb.append((char) c);
                 scanWhitespace(scanner, sb);
                 break;
             }
         }
 
         // If we've got a comment, then unread it
         if (c == EOF || c == COMMENT) {
             scanner.unread();
         }

        final String text = sb.toString();
        return text;
     }
 
 
     private void unread(final ICharacterScanner scanner, final int length) {
         for (int i = 0; i < length; i++) {
             scanner.unread();
         }
     }
 
 
     // We need to scan any additional whitespace, as the next predicate rule
     // after this needs to start at the 1st char to be matched - no trimming by
     // the framework
     private void scanWhitespace(final ICharacterScanner scanner, final StringBuilder sb) {
         int c = scanner.read();
         while (c != EOF) {
             if (Character.isWhitespace(c)) {
                 sb.append((char) c);
                 c = scanner.read();
             } else {
                 scanner.unread();
                 break;
             }
         }
 
     }
 }
