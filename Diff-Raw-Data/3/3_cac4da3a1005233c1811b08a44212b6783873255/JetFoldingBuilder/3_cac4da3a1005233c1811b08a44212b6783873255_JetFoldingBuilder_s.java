 /*
  * Copyright 2010-2012 JetBrains s.r.o.
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
 
 package org.napile.idea.plugin;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.jetbrains.annotations.NotNull;
 import org.napile.compiler.lang.lexer.NapileNodes;
 import org.napile.compiler.lang.lexer.NapileTokens;
 import org.napile.compiler.lang.psi.NapileExpression;
 import org.napile.compiler.lang.psi.NapileFile;
 import org.napile.compiler.lang.psi.NapileImportDirective;
 import org.napile.compiler.lang.psi.stubs.elements.NapileStubElementTypes;
 import com.intellij.codeInsight.folding.CodeFoldingSettings;
 import com.intellij.lang.ASTNode;
 import com.intellij.lang.folding.FoldingBuilderEx;
 import com.intellij.lang.folding.FoldingDescriptor;
 import com.intellij.openapi.editor.Document;
 import com.intellij.openapi.project.DumbAware;
 import com.intellij.openapi.util.TextRange;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.tree.IElementType;
 
 /**
  * @author yole
  */
 public class JetFoldingBuilder extends FoldingBuilderEx implements DumbAware
 {
 	@NotNull
 	@Override
 	public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick)
 	{
 		List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();
 		appendDescriptors(root.getNode(), document, descriptors);
 		return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
 	}
 
 	private static void appendDescriptors(ASTNode node, Document document, List<FoldingDescriptor> descriptors)
 	{
 		TextRange textRange = node.getTextRange();
 		IElementType type = node.getElementType();
 		if((type == NapileNodes.BLOCK || type == NapileNodes.CLASS_BODY) && !isOneLine(textRange, document))
 		{
 			descriptors.add(new FoldingDescriptor(node, textRange));
 		}
 		else if(node.getElementType() == NapileStubElementTypes.FILE)
 		{
 			NapileFile napileFile = ((NapileFile) node.getPsi());
 			PsiElement firstChild = napileFile.getFirstChild();
 			if(firstChild.getNode().getElementType() == NapileTokens.BLOCK_COMMENT)
 				descriptors.add(new FoldingDescriptor(firstChild, firstChild.getTextRange()));
 
 			List<NapileImportDirective> imports = napileFile.getImportDirectives();
 			if(!imports.isEmpty())
 			{
 				NapileImportDirective first = imports.get(0);
 				NapileImportDirective last = imports.get(imports.size() - 1);
 				NapileExpression importExp = first.getImportedReference();
 				// if import exp is null - get last end offset of directive
 				int startOffset = importExp == null ? first.getNode().getStartOffset() + first.getNode().getTextLength() : importExp.getNode().getStartOffset();
 
 				TextRange range = new TextRange(startOffset, last.getNode().getStartOffset() + last.getNode().getTextLength());
 				descriptors.add(new FoldingDescriptor(last, range));
 			}
 		}
 		else if(node.getElementType() == NapileTokens.IDE_TEMPLATE_START)
 		{
 			ASTNode next = node.getTreeNext();
 			if(next != null)
 			{
 				ASTNode nextNext = next.getTreeNext();
 				if(nextNext != null && nextNext.getElementType() == NapileTokens.IDE_TEMPLATE_END)
 				{
 					TextRange range = new TextRange(node.getStartOffset(), nextNext.getStartOffset() + nextNext.getTextLength());
 					descriptors.add(new FoldingDescriptor(next, range, null, Collections.<Object>emptySet(), true));
 				}
 			}
 		}
 		ASTNode child = node.getFirstChildNode();
 		while(child != null)
 		{
 			appendDescriptors(child, document, descriptors);
 			child = child.getTreeNext();
 		}
 	}
 
 	private static boolean isOneLine(TextRange textRange, Document document)
 	{
 		return document.getLineNumber(textRange.getStartOffset()) == document.getLineNumber(textRange.getEndOffset());
 	}
 
 	@Override
 	public String getPlaceholderText(@NotNull ASTNode astNode, @NotNull TextRange range)
 	{
 		ASTNode prev = astNode.getTreePrev();
 		ASTNode next = astNode.getTreeNext();
 		if(prev != null && next != null && prev.getElementType() == NapileTokens.IDE_TEMPLATE_START && next.getElementType() == NapileTokens.IDE_TEMPLATE_END)
 		{
 			return astNode.getText();
 		}
 
 		if(astNode.getElementType() == NapileNodes.IMPORT_DIRECTIVE)
 			return "...";
 		else if(astNode.getElementType() == NapileTokens.BLOCK_COMMENT && astNode.getTreeParent().getElementType() == NapileStubElementTypes.FILE)
 			return "/.../";
 		return super.getPlaceholderText(astNode, range);
 	}
 
 	@Override
 	public String getPlaceholderText(@NotNull ASTNode node)
 	{
 		return "{...}";
 	}
 
 	@Override
 	public boolean isCollapsedByDefault(@NotNull ASTNode astNode)
 	{
 		if(astNode.getElementType() == NapileNodes.IMPORT_DIRECTIVE)
 			return CodeFoldingSettings.getInstance().COLLAPSE_IMPORTS;
 		else if(astNode.getElementType() == NapileTokens.BLOCK_COMMENT && astNode.getTreeParent().getElementType() == NapileStubElementTypes.FILE)
 			return CodeFoldingSettings.getInstance().COLLAPSE_FILE_HEADER;
 		return false;
 	}
 }
