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
 
 package org.napile.idea.plugin.quickfix;
 
 import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.psi.NapilePostfixExpression;
import org.napile.compiler.lang.psi.NapilePsiFactory;
 import org.napile.compiler.lang.lexer.NapileTokens;
 import org.napile.compiler.lang.psi.NapileExpression;
 import org.napile.compiler.lang.psi.NapileFile;
 import org.napile.idea.plugin.NapileBundle;
import com.intellij.codeInsight.CodeInsightUtilBase;
 import com.intellij.codeInsight.intention.IntentionAction;
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.impl.source.tree.LeafPsiElement;
 import com.intellij.psi.util.PsiTreeUtil;
 import com.intellij.util.IncorrectOperationException;
 
 /**
  * @author slukjanov aka Frostman
  */
 @SuppressWarnings("IntentionDescriptionNotFoundInspection")
 public class ExclExclCallFix implements IntentionAction
 {
 
 	private final boolean isRemove;
 
 	private ExclExclCallFix(boolean remove)
 	{
 		isRemove = remove;
 	}
 
 	public static ExclExclCallFix removeExclExclCall()
 	{
 		return new ExclExclCallFix(true);
 	}
 
 	public static ExclExclCallFix introduceExclExclCall()
 	{
 		return new ExclExclCallFix(false);
 	}
 
 	@NotNull
 	@Override
 	public String getText()
 	{
 		return isRemove ? NapileBundle.message("remove.unnecessary.non.null.assertion") : NapileBundle.message("introduce.non.null.assertion");
 	}
 
 	@NotNull
 	@Override
 	public String getFamilyName()
 	{
 		return getText();
 	}
 
 	@Override
 	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
 	{
 		if(file instanceof NapileFile)
 		{
 			if(!isRemove)
 			{
 				return isAvailableForIntroduce(editor, file);
 			}
 			else
 			{
 				return isAvailableForRemove(editor, file);
 			}
 		}
 
 		return false;
 	}
 
 	@Override
 	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
 	{
		if(!CodeInsightUtilBase.prepareFileForWrite(file))
 		{
 			return;
 		}
 
 		if(!isRemove)
 		{
 			NapileExpression modifiedExpression = getExpressionForIntroduceCall(editor, file);
 			NapileExpression exclExclExpression = NapilePsiFactory.createExpression(project, modifiedExpression.getText() + "!!");
 			modifiedExpression.replace(exclExclExpression);
 		}
 		else
 		{
 			NapilePostfixExpression postfixExpression = getExclExclPostfixExpression(editor, file);
 			NapileExpression expression = NapilePsiFactory.createExpression(project, postfixExpression.getBaseExpression().getText());
 			postfixExpression.replace(expression);
 		}
 	}
 
 	@Override
 	public boolean startInWriteAction()
 	{
 		return true;
 	}
 
 	private static boolean isAvailableForIntroduce(Editor editor, PsiFile file)
 	{
 		return getExpressionForIntroduceCall(editor, file) != null;
 	}
 
 	private static boolean isAvailableForRemove(Editor editor, PsiFile file)
 	{
 		return getExclExclPostfixExpression(editor, file) != null;
 	}
 
 	private static PsiElement getExclExclElement(Editor editor, PsiFile file)
 	{
 		final PsiElement elementAtCaret = file.findElementAt(editor.getCaretModel().getOffset());
 		if(elementAtCaret instanceof LeafPsiElement)
 		{
 			LeafPsiElement leafElement = (LeafPsiElement) elementAtCaret;
 			if(leafElement.getElementType() == NapileTokens.EXCLEXCL)
 			{
 				return elementAtCaret;
 			}
 
 			LeafPsiElement prevLeaf = (LeafPsiElement) PsiTreeUtil.prevLeaf(leafElement);
 			if(prevLeaf != null && prevLeaf.getElementType() == NapileTokens.EXCLEXCL)
 			{
 				return prevLeaf;
 			}
 		}
 
 		return null;
 	}
 
 	private static NapileExpression getExpressionForIntroduceCall(Editor editor, PsiFile file)
 	{
 		final PsiElement elementAtCaret = file.findElementAt(editor.getCaretModel().getOffset());
 		if(elementAtCaret != null)
 		{
 			NapileExpression expression = getExpressionForIntroduceCall(elementAtCaret);
 			if(expression != null)
 			{
 				return expression;
 			}
 
 			// Maybe caret is after error element
 			expression = getExpressionForIntroduceCall(PsiTreeUtil.prevLeaf(elementAtCaret));
 			if(expression != null)
 			{
 				return expression;
 			}
 		}
 
 		return null;
 	}
 
 	private static NapileExpression getExpressionForIntroduceCall(PsiElement problemElement)
 	{
 		if(problemElement instanceof LeafPsiElement && ((LeafPsiElement) problemElement).getElementType() == NapileTokens.DOT)
 		{
 			PsiElement sibling = problemElement.getPrevSibling();
 			if(sibling instanceof NapileExpression)
 			{
 				return (NapileExpression) sibling;
 			}
 		}
 
 		return null;
 	}
 
 	private static NapilePostfixExpression getExclExclPostfixExpression(Editor editor, PsiFile file)
 	{
 		PsiElement exclExclElement = getExclExclElement(editor, file);
 
 		if(exclExclElement != null)
 		{
 			PsiElement parent = exclExclElement.getParent();
 			if(parent != null)
 			{
 				PsiElement operationParent = parent.getParent();
 				if(operationParent instanceof NapilePostfixExpression)
 				{
 					return (NapilePostfixExpression) operationParent;
 				}
 			}
 		}
 
 		return null;
 	}
 }
