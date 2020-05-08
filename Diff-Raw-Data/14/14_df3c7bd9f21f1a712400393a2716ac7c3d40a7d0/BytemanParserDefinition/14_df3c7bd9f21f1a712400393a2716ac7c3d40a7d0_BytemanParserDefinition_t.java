 /*
  * Copyright (c) 2011 Julien Nicoulaud <julien.nicoulaud@gmail.com>
  *
  * This file is part of idea-byteman.
  *
  * idea-byteman is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * idea-byteman is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with idea-byteman.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.nicoulaj.idea.byteman.lang.parser;
 
 import com.intellij.lang.ASTNode;
 import com.intellij.lang.ParserDefinition;
 import com.intellij.lang.PsiParser;
 import com.intellij.lexer.Lexer;
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.FileViewProvider;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.tree.IFileElementType;
 import com.intellij.psi.tree.TokenSet;
 import net.nicoulaj.idea.byteman.file.BytemanFileElementType;
 import net.nicoulaj.idea.byteman.lang.BytemanTokenTypeSets;
 import net.nicoulaj.idea.byteman.lang.lexer.BytemanLexer;
 import net.nicoulaj.idea.byteman.lang.psi.impl.BytemanFileImpl;
 import net.nicoulaj.idea.byteman.lang.psi.impl.BytemanPsiElementImpl;
 import org.jetbrains.annotations.NotNull;
 
 /**
  * The parser implementation for Byteman.
  *
  * @author Julien Nicoulaud <julien.nicoulaud@gmail.com>
  * @since 0.1
  */
 public class BytemanParserDefinition implements ParserDefinition {
 
     /**
      * The {@link BytemanFileElementType} instance.
      *
      * @see #getFileNodeType()
      */
     protected static final BytemanFileElementType FILE_ELEMENT_TYPE = new BytemanFileElementType();
 
     /**
      * Get the lexer for lexing files in the specified project.
      *
      * @param project the project to which the lexer is connected.
      * @return a {@link BytemanLexer} instance.
      */
     @NotNull
     public Lexer createLexer(Project project) {
         return new BytemanLexer();
     }
 
     /**
      * Get the parser for parsing files in the specified project.
      *
      * @param project the project to which the parser is connected.
      * @return a {@link BytemanParser} instance.
      */
     public PsiParser createParser(Project project) {
         return new BytemanParser();
     }
 
     /**
      * Get the element type of the node describing a Byteman file.
      *
      * @return {@link #FILE_ELEMENT_TYPE}
      */
     public IFileElementType getFileNodeType() {
         return FILE_ELEMENT_TYPE;
     }
 
     /**
      * Get the set of token types which are treated as whitespace by the PSI builder.
      *
     * @return {@link BytemanTokenTypeSets#WHITE_SPACE_SET}
      */
     @NotNull
     public TokenSet getWhitespaceTokens() {
        return BytemanTokenTypeSets.WHITE_SPACE_SET;
     }
 
     /**
      * Get the set of token types which are treated as comments by the PSI builder.
      *
     * @return {@link BytemanTokenTypeSets#COMMENT_SET}
      */
     @NotNull
     public TokenSet getCommentTokens() {
         return BytemanTokenTypeSets.COMMENT_SET;
     }
 
     /**
      * Get the set of element types which are treated as string literals.
      *
     * @return {@link BytemanTokenTypeSets#STRING_SET}
      */
     @NotNull
     public TokenSet getStringLiteralElements() {
        return BytemanTokenTypeSets.STRING_SET;
     }
 
     /**
      * Create a PSI element for the specified AST node.
      *
      * @param node the AST node.
      * @return the PSI element matching the element type of the AST node.
      */
     @NotNull
     public PsiElement createElement(ASTNode node) {
         return new BytemanPsiElementImpl(node);
     }
 
     /**
      * Create a PSI element for the specified virtual file.
      *
      * @param viewProvider virtual file.
      * @return the PSI file element.
      */
     public PsiFile createFile(FileViewProvider viewProvider) {
         return new BytemanFileImpl(viewProvider);
     }
 
     /**
      * Check if the specified two token types need to be separated by a space according to the language grammar.
      *
      * @param left  the first token to check.
      * @param right the second token to check.
      * @return {@link SpaceRequirements#MAY}
      */
     public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
         return SpaceRequirements.MAY;
     }
 }
