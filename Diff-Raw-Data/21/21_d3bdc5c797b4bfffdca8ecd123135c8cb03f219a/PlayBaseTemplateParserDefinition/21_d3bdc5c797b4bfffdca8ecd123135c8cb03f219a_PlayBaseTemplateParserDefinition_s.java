 /*
  * Copyright 2013 Consulo.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.consulo.play1.template.base;
 
 import org.consulo.play1.template.base.parser.PlayBaseTemplateTokenSets;
 import org.consulo.play1.template.base.parser.PlayBaseTemplateTokens;
 import org.consulo.play1.template.base.parser.lexer.PlayBaseTemplateElementType;
 import org.consulo.play1.template.base.parser.lexer.PlayBaseTemplateLexer;
 import org.consulo.play1.template.base.psi.PlayBaseTemplateFile;
 import org.jetbrains.annotations.NotNull;
 import com.intellij.lang.ASTNode;
 import com.intellij.lang.LanguageVersion;
 import com.intellij.lang.ParserDefinition;
 import com.intellij.lang.PsiParser;
 import com.intellij.lexer.Lexer;
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.FileViewProvider;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.tree.IElementType;
 import com.intellij.psi.tree.IFileElementType;
 import com.intellij.psi.tree.TokenSet;
 
 /**
  * @author VISTALL
  * @since 12:07/18.03.13
  */
 public class PlayBaseTemplateParserDefinition implements ParserDefinition
 {
 	@NotNull
 	@Override
 	public Lexer createLexer(Project project, LanguageVersion languageVersion)
 	{
 		return new PlayBaseTemplateLexer();
 	}
 
 	@Override
 	public PsiParser createParser(Project project, LanguageVersion languageVersion)
 	{
 		return PlayBaseTemplateParser.INSTANCE;
 	}
 
 	@Override
 	public IFileElementType getFileNodeType()
 	{
 		return PlayBaseTemplateTokens.PLAY_BASE_TEMPLATE_FILE;
 	}
 
 	@NotNull
 	@Override
	public TokenSet getWhitespaceTokens()
 	{
 		return PlayBaseTemplateTokenSets.WHITESPACE_SET;
 	}
 
 	@NotNull
 	@Override
	public TokenSet getCommentTokens()
 	{
 		return PlayBaseTemplateTokenSets.COMMENT_SET;
 	}
 
 	@NotNull
 	@Override
	public TokenSet getStringLiteralElements()
 	{
 		return PlayBaseTemplateTokenSets.STRING_SET;
 	}
 
 	@NotNull
 	@Override
 	public PsiElement createElement(ASTNode node)
 	{
 		final IElementType elementType = node.getElementType();
 
 		if(elementType instanceof PlayBaseTemplateElementType)
 		{
 			return ((PlayBaseTemplateElementType) elementType).createPsiElement(node);
 		}
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public PsiFile createFile(FileViewProvider viewProvider)
 	{
 		return new PlayBaseTemplateFile(viewProvider);
 	}
 
 	@Override
 	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right)
 	{
 		return SpaceRequirements.MAY;
 	}
 }
