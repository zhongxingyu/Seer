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
 
 package org.napile.idea.plugin.completion;
 
 import static org.napile.compiler.lang.lexer.NapileTokens.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.jetbrains.annotations.NotNull;
 import org.napile.compiler.lang.psi.*;
 import org.napile.compiler.lang.lexer.NapileToken;
 import org.napile.compiler.lang.lexer.NapileTokens;
 import org.napile.compiler.lang.psi.NapileFile;
 import org.napile.compiler.lang.psi.NapileMethod;
 import org.napile.compiler.lang.psi.NapileVariable;
 import org.napile.idea.plugin.completion.handlers.JetFunctionInsertHandler;
 import org.napile.idea.plugin.completion.handlers.JetKeywordInsertHandler;
 import org.napile.idea.plugin.completion.handlers.JetTemplateInsertHandler;
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.intellij.codeInsight.CommentUtil;
 import com.intellij.codeInsight.completion.CompletionContributor;
 import com.intellij.codeInsight.completion.CompletionParameters;
 import com.intellij.codeInsight.completion.CompletionProvider;
 import com.intellij.codeInsight.completion.CompletionResultSet;
 import com.intellij.codeInsight.completion.CompletionType;
 import com.intellij.codeInsight.completion.InsertHandler;
 import com.intellij.codeInsight.completion.PrefixMatcher;
 import com.intellij.codeInsight.lookup.LookupElement;
 import com.intellij.codeInsight.lookup.LookupElementBuilder;
 import com.intellij.openapi.util.text.StringUtil;
 import com.intellij.patterns.ElementPattern;
 import com.intellij.patterns.PlatformPatterns;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiWhiteSpace;
 import com.intellij.psi.filters.AndFilter;
 import com.intellij.psi.filters.ClassFilter;
 import com.intellij.psi.filters.ElementFilter;
 import com.intellij.psi.filters.NotFilter;
 import com.intellij.psi.filters.OrFilter;
 import com.intellij.psi.filters.TextFilter;
 import com.intellij.psi.filters.position.FilterPattern;
 import com.intellij.psi.filters.position.LeftNeighbour;
 import com.intellij.psi.filters.position.PositionElementFilter;
 import com.intellij.psi.impl.source.tree.LeafPsiElement;
 import com.intellij.psi.util.PsiTreeUtil;
 import com.intellij.util.ArrayUtil;
 import com.intellij.util.ProcessingContext;
 
 /**
  * A keyword contributor for Kotlin
  *
  * @author Nikolay Krasko
  */
 public class JetKeywordCompletionContributor extends CompletionContributor
 {
 
 	private final static InsertHandler<LookupElement> KEYWORDS_INSERT_HANDLER = new JetKeywordInsertHandler();
 	private final static InsertHandler<LookupElement> FUNCTION_INSERT_HANDLER = new JetFunctionInsertHandler(JetFunctionInsertHandler.CaretPosition.AFTER_BRACKETS, JetFunctionInsertHandler.BracketType.PARENTHESIS);
 
 	private final static ElementFilter GENERAL_FILTER = new NotFilter(new OrFilter(new CommentFilter(), // or
 			new ParentFilter(new ClassFilter(NapileConstantExpression.class)), // or
 			new LeftNeighbour(new TextFilter("."))));
 
 	private final static ElementFilter NOT_IDENTIFIER_FILTER = new NotFilter(new AndFilter(new LeafElementFilter(NapileTokens.IDENTIFIER), new NotFilter(new ParentFilter(new ClassFilter(NapileReferenceExpression.class)))));
 
	private static final String IF_TEMPLATE = "if (<#<condition>#>) {\n<#<block>#>\n}";
	private static final String IF_ELSE_TEMPLATE = "if (<#<condition>#>) {\n<#<block>#>\n} else {\n<#<block>#>\n}";
	private static final String IF_ELSE_ONELINE_TEMPLATE = "if (<#<condition>#>) <#<value>#> else <#<value>#>";
	private static final String METH_TEMPLATE = "meth <#<name>#>(<#<params>#>) : <#<returnType>#>\n{\n<#<body>#>\n}";
	private static final String MACRO_TEMPLATE = "macro <#<name>#>(<#<params>#>) : <#<returnType>#>\n{\n<#<body>#>\n}";
	private static final String METH_NO_RETURN_TEMPLATE = "meth <#<name>#>(<#<params>#>)\n{\n<#<body>#>\n}";
	private static final String VAR_SIMPLE_TEMPLATE = "var <#<name>#> = <#<value>#>";
	private static final String VAR_WITH_TYPE_TEMPLATE = "var <#<name>#> : <#<varType>#> = <#<initial>#>";
	private static final String CLASS_TEMPLATE = "class <#<name>#> {\n<#<body>#>\n}";
	private static final String FOR_TEMPLATE = "for (<#<i>#> in <#<elements>#>) {\n<#<body>#>\n}";
	private static final String WHEN_TEMPLATE = "when (<#<expression>#>) {\n<#<condition>#> -> <#<value>#>\n" + "else -> <#<elseValue>#>\n}";
	private static final String WHEN_ENTRY_TEMPLATE = "<#<condition>#> -> <#<value>#>";
	private static final String WHILE_TEMPLATE = "while (<#<condition>#>) {\n<#<body>#>\n}";
	private static final String DO_WHILE_TEMPLATE = "do {\n<#<body>#>\n} while (<#<condition>#>)";

 	private static class CommentFilter implements ElementFilter
 	{
 		@Override
 		public boolean isAcceptable(Object element, PsiElement context)
 		{
 			if(!(element instanceof PsiElement))
 			{
 				return false;
 			}
 
 			return CommentUtil.isComment((PsiElement) element);
 		}
 
 		@Override
 		public boolean isClassAcceptable(Class hintClass)
 		{
 			return true;
 		}
 	}
 
 	private static class ParentFilter extends PositionElementFilter
 	{
 		public ParentFilter(ElementFilter filter)
 		{
 			setFilter(filter);
 		}
 
 		@Override
 		public boolean isAcceptable(Object element, PsiElement context)
 		{
 			if(!(element instanceof PsiElement))
 			{
 				return false;
 			}
 			PsiElement parent = ((PsiElement) element).getParent();
 			return parent != null && getFilter().isAcceptable(parent, context);
 		}
 	}
 
 	private static class InTopFilter implements ElementFilter
 	{
 		@Override
 		public boolean isAcceptable(Object element, PsiElement context)
 		{
 			//noinspection unchecked
 			return PsiTreeUtil.getParentOfType(context, NapileFile.class, false, NapileClassBody.class, NapileBlockExpression.class, NapileMethod.class) != null && PsiTreeUtil.getParentOfType(context, NapileCallParameterList.class, NapileTypeParameterListImpl.class) == null;
 		}
 
 		@Override
 		public boolean isClassAcceptable(Class hintClass)
 		{
 			return true;
 		}
 	}
 
 	private static class InNonClassBlockFilter implements ElementFilter
 	{
 		@Override
 		public boolean isAcceptable(Object element, PsiElement context)
 		{
 			//noinspection unchecked
 			return PsiTreeUtil.getParentOfType(context, NapileBlockExpression.class, true, NapileClassBody.class) != null;
 		}
 
 		@Override
 		public boolean isClassAcceptable(Class hintClass)
 		{
 			return true;
 		}
 	}
 
 	private static class InParametersFilter implements ElementFilter
 	{
 		@Override
 		public boolean isAcceptable(Object element, PsiElement context)
 		{
 			return PsiTreeUtil.getParentOfType(context, NapileCallParameterList.class, false) != null;
 		}
 
 		@Override
 		public boolean isClassAcceptable(Class hintClass)
 		{
 			return true;
 		}
 	}
 
 	private static class InClassBodyFilter implements ElementFilter
 	{
 		@Override
 		public boolean isAcceptable(Object element, PsiElement context)
 		{
 			//noinspection unchecked
 			return PsiTreeUtil.getParentOfType(context, NapileClassBody.class, true, NapileBlockExpression.class, NapileVariable.class, NapileCallParameterList.class) != null;
 		}
 
 		@Override
 		public boolean isClassAcceptable(Class hintClass)
 		{
 			return true;
 		}
 	}
 
 	private static class AfterClassInClassBodyFilter extends InClassBodyFilter
 	{
 		@Override
 		public boolean isAcceptable(Object element, PsiElement context)
 		{
 			if(super.isAcceptable(element, context))
 			{
 				PsiElement ps = context.getPrevSibling();
 				if(ps instanceof PsiWhiteSpace)
 				{
 					ps = ps.getPrevSibling();
 				}
 				if(ps instanceof LeafPsiElement)
 				{
 					return ((LeafPsiElement) ps).getElementType() == NapileTokens.CLASS_KEYWORD;
 				}
 			}
 			return false;
 		}
 	}
 
 	private static class InPropertyBodyFilter implements ElementFilter
 	{
 		@Override
 		public boolean isAcceptable(Object element, PsiElement context)
 		{
 			if(!(element instanceof PsiElement))
 				return false;
 			NapileVariable property = PsiTreeUtil.getParentOfType(context, NapileVariable.class, false);
 			return property != null && isAfterName(property, (PsiElement) element);
 		}
 
 		@Override
 		public boolean isClassAcceptable(Class hintClass)
 		{
 			return true;
 		}
 
 		private static boolean isAfterName(@NotNull NapileVariable property, @NotNull PsiElement element)
 		{
 			for(PsiElement child = property.getFirstChild(); child != null; child = child.getNextSibling())
 			{
 				if(PsiTreeUtil.isAncestor(child, element, false))
 				{
 					break;
 				}
 
 				if(child.getNode().getElementType() == IDENTIFIER)
 				{
 					return true;
 				}
 			}
 
 			return false;
 		}
 	}
 
 	private static class InWhenFilter implements ElementFilter
 	{
 		@Override
 		public boolean isAcceptable(Object element, PsiElement context)
 		{
 			return PsiTreeUtil.getParentOfType(context, NapileWhenExpression.class, false) != null;
 		}
 
 		@Override
 		public boolean isClassAcceptable(Class hintClass)
 		{
 			return true;
 		}
 	}
 
 	private static class SimplePrefixMatcher extends PrefixMatcher
 	{
 		protected SimplePrefixMatcher(String prefix)
 		{
 			super(prefix);
 		}
 
 		@Override
 		public boolean prefixMatches(@NotNull String name)
 		{
 			return StringUtil.startsWithIgnoreCase(name, getPrefix());
 		}
 
 		@NotNull
 		@Override
 		public PrefixMatcher cloneWithPrefix(@NotNull String prefix)
 		{
 			return new SimplePrefixMatcher(prefix);
 		}
 	}
 
 	public static class KeywordsCompletionProvider extends CompletionProvider<CompletionParameters>
 	{
 
 		private final Collection<LookupElement> elements;
 
 		public KeywordsCompletionProvider(String... keywords)
 		{
 			List<String> elementsList = Lists.newArrayList(keywords);
 			elements = Collections2.transform(elementsList, new Function<String, LookupElement>()
 			{
 				@Override
 				public LookupElement apply(String keyword)
 				{
 					final LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(keyword).setBold();
 
 					if(keyword.contains("<#<"))
 					{
 						return JetTemplateInsertHandler.lookup(keyword);
 					}
 
 					return lookupElementBuilder.withInsertHandler(KEYWORDS_INSERT_HANDLER);
 				}
 			});
 		}
 
 		@Override
 		protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull final CompletionResultSet result)
 		{
 			result.withPrefixMatcher(new SimplePrefixMatcher(result.getPrefixMatcher().getPrefix())).addAllElements(elements);
 		}
 	}
 
 	public JetKeywordCompletionContributor()
 	{
 		registerScopeKeywordsCompletion(new InTopFilter(), ABSTRACT_KEYWORD, FINAL_KEYWORD, GET_KEYWORD, IMPORT_KEYWORD, PACKAGE_KEYWORD, LOCAL_KEYWORD, COVERED_KEYWORD, HERITABLE_KEYWORD, SET_KEYWORD);
 
 		registerScopeKeywordsCompletion(new InClassBodyFilter(), ABSTRACT_KEYWORD, STATIC_KEYWORD, FINAL_KEYWORD, GET_KEYWORD, OVERRIDE_KEYWORD, LOCAL_KEYWORD, COVERED_KEYWORD, HERITABLE_KEYWORD, SET_KEYWORD);
 
 		registerScopeKeywordsCompletion(new InNonClassBlockFilter(), AS_KEYWORD, BREAK_KEYWORD, CATCH_KEYWORD, CONTINUE_KEYWORD, ELSE_KEYWORD, FALSE_KEYWORD, FINALLY_KEYWORD, GET_KEYWORD, IN_KEYWORD, IS_KEYWORD, NULL_KEYWORD, ANONYM_KEYWORD, LOCAL_KEYWORD, COVERED_KEYWORD, HERITABLE_KEYWORD, RETURN_KEYWORD, SET_KEYWORD, SUPER_KEYWORD, THIS_KEYWORD, THROW_KEYWORD, TRUE_KEYWORD, TRY_KEYWORD, VARARG_KEYWORD);
 
 		registerScopeKeywordsCompletion(new InPropertyBodyFilter(), ELSE_KEYWORD, FALSE_KEYWORD, NULL_KEYWORD, THIS_KEYWORD, TRUE_KEYWORD);

		// templates
		registerScopeKeywordsCompletion(new InWhenFilter(), WHEN_ENTRY_TEMPLATE);
		registerScopeKeywordsCompletion(new InTopFilter(), CLASS_TEMPLATE);
		registerScopeKeywordsCompletion(new InClassBodyFilter(), METH_TEMPLATE, METH_NO_RETURN_TEMPLATE, MACRO_TEMPLATE, VAR_WITH_TYPE_TEMPLATE, CLASS_TEMPLATE);
		registerScopeKeywordsCompletion(new InNonClassBlockFilter(), IF_TEMPLATE, IF_ELSE_TEMPLATE, IF_ELSE_ONELINE_TEMPLATE, METH_TEMPLATE, VAR_SIMPLE_TEMPLATE, CLASS_TEMPLATE, FOR_TEMPLATE, WHEN_TEMPLATE, WHILE_TEMPLATE, DO_WHILE_TEMPLATE);
		registerScopeKeywordsCompletion(new InPropertyBodyFilter(), IF_ELSE_ONELINE_TEMPLATE, WHEN_TEMPLATE);
 	}
 
 	private void registerScopeKeywordsCompletion(final ElementFilter placeFilter, boolean notIdentifier, String... keywords)
 	{
 		extend(CompletionType.BASIC, getPlacePattern(placeFilter, notIdentifier), new KeywordsCompletionProvider(keywords));
 	}
 
 	private void registerScopeKeywordsCompletion(final ElementFilter placeFilter, String... keywords)
 	{
 		registerScopeKeywordsCompletion(placeFilter, true, keywords);
 	}
 
 	private void registerScopeKeywordsCompletion(final ElementFilter placeFilter, NapileToken... keywords)
 	{
 		registerScopeKeywordsCompletion(placeFilter, convertTokensToStrings(keywords));
 	}
 
 	private static String[] convertTokensToStrings(NapileToken... keywords)
 	{
 		final ArrayList<String> strings = new ArrayList<String>(keywords.length);
 		for(NapileToken keyword : keywords)
 		{
 			strings.add(keyword.toString());
 		}
 
 		return ArrayUtil.toStringArray(strings);
 	}
 
 	private static ElementPattern<PsiElement> getPlacePattern(final ElementFilter placeFilter, boolean notIdentifier)
 	{
 		if(notIdentifier)
 		{
 			return PlatformPatterns.psiElement().and(new FilterPattern(new AndFilter(GENERAL_FILTER, NOT_IDENTIFIER_FILTER, placeFilter)));
 		}
 		else
 		{
 			return PlatformPatterns.psiElement().and(new FilterPattern(new AndFilter(GENERAL_FILTER, placeFilter)));
 		}
 	}
 }
