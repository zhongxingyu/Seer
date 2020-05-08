 /*
  * Copyright 2011 napile
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
 
 package org.napile.cpp4idea.lang.parser;
 
 import org.jetbrains.annotations.NotNull;
 import org.napile.cpp4idea.CLanguage;
 import org.napile.cpp4idea.lang.psi.CPsiTokenImpl;
 import org.napile.cpp4idea.lang.psi.CPsiTokens;
 import org.napile.cpp4idea.lang.psi.impl.CPsiFileImpl;
 import org.napile.cpp4idea.lang.psiInitial.CPsiSharpTokenImpl;
 import org.napile.cpp4idea.lang.psiInitial.impl.CPsiSharpFileImpl;
 import com.intellij.lang.ASTNode;
 import com.intellij.lang.LanguageVersion;
 import com.intellij.lang.ParserDefinition;
 import com.intellij.lang.PsiParser;
 import com.intellij.lexer.EmptyLexer;
 import com.intellij.lexer.Lexer;
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.FileViewProvider;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.tree.IFileElementType;
 import com.intellij.psi.tree.IStubFileElementType;
 import com.intellij.psi.tree.TokenSet;
 
 /**
  * @author VISTALL
  * @date 14:26/18.12.2011
  */
 public class CParserDefinitionImpl implements ParserDefinition
 {
 	public static final IStubFileElementType C_FILE_TYPE = new IStubFileElementType(CLanguage.INSTANCE);
 
 	@NotNull
 	@Override
 	public Lexer createLexer(Project project, LanguageVersion languageVersion)
 	{
 		return new EmptyLexer();
 	}
 
 	@Override
 	public PsiParser createParser(Project project, LanguageVersion languageVersion)
 	{
 		return new CPsiParserImpl();
 	}
 
 	@Override
 	public IFileElementType getFileNodeType()
 	{
 		return C_FILE_TYPE;
 	}
 
 	@NotNull
 	@Override
	public TokenSet getWhitespaceTokens(LanguageVersion languageVersion)
 	{
 		return CPsiTokens.WHITE_SPACE_SET;
 	}
 
 	@NotNull
 	@Override
	public TokenSet getCommentTokens(LanguageVersion languageVersion)
 	{
 		return CPsiTokens.COMMENT_SET;
 	}
 
 	@NotNull
 	@Override
	public TokenSet getStringLiteralElements(LanguageVersion languageVersion)
 	{
 		return CPsiTokens.STRING_LITERAL_SET;
 	}
 
 	@NotNull
 	@Override
 	public PsiElement createElement(ASTNode node)
 	{
 		if(node.getElementType() instanceof CPsiTokenImpl)
 			return ((CPsiTokenImpl) node.getElementType()).createPsi(node);
 		else if(node.getElementType() instanceof CPsiSharpTokenImpl)
 			return ((CPsiSharpTokenImpl) node.getElementType()).createPsi(node);
 		else if(node.getElementType() == CPsiTokens.C_PROCESSED_FILE_TYPE)
 			return new CPsiFileImpl(node);
 
 		throw new IllegalArgumentException("Illegal argument : " + node.getElementType());
 	}
 
 	@Override
 	public PsiFile createFile(FileViewProvider viewProvider)
 	{
 		return new CPsiSharpFileImpl(viewProvider);
 	}
 
 	@Override
 	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right)
 	{
 		return SpaceRequirements.MAY;
 	}
 }
