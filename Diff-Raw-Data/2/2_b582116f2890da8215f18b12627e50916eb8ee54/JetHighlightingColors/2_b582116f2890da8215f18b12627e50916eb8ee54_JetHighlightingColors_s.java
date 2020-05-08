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
 
 package org.napile.idea.plugin.highlighter;
 
 import java.awt.Color;
 import java.awt.Font;
 
 import org.jetbrains.annotations.NotNull;
 import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
 import org.napile.compiler.lang.descriptors.LocalVariableDescriptor;
 import org.napile.compiler.lang.descriptors.PropertyParameterDescriptorImpl;
 import org.napile.compiler.lang.descriptors.VariableDescriptor;
 import com.intellij.openapi.editor.HighlighterColors;
 import com.intellij.openapi.editor.SyntaxHighlighterColors;
 import com.intellij.openapi.editor.colors.CodeInsightColors;
 import com.intellij.openapi.editor.colors.TextAttributesKey;
 import com.intellij.openapi.editor.markup.EffectType;
 import com.intellij.openapi.editor.markup.TextAttributes;
 import com.intellij.ui.Gray;
 
 public class JetHighlightingColors
 {
 	public final static TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey("NAPILE_KEYWORD", SyntaxHighlighterColors.KEYWORD.getDefaultAttributes());
 
 	public static final TextAttributesKey BUILTIN_ANNOTATION = TextAttributesKey.createTextAttributesKey("NAPILE_BUILTIN_ANNOTATION", SyntaxHighlighterColors.KEYWORD.getDefaultAttributes());
 
 	public static final TextAttributesKey NUMBER = TextAttributesKey.createTextAttributesKey("NAPILE_NUMBER", SyntaxHighlighterColors.NUMBER.getDefaultAttributes());
 
 	public static final TextAttributesKey STRING = TextAttributesKey.createTextAttributesKey("NAPILE_STRING", SyntaxHighlighterColors.STRING.getDefaultAttributes());
 
 	public static final TextAttributesKey STRING_ESCAPE = TextAttributesKey.createTextAttributesKey("NAPILE_STRING_ESCAPE", SyntaxHighlighterColors.VALID_STRING_ESCAPE.getDefaultAttributes());
 
 	public static final TextAttributesKey INVALID_STRING_ESCAPE = TextAttributesKey.createTextAttributesKey("NAPILE_INVALID_STRING_ESCAPE", new TextAttributes(null, HighlighterColors.BAD_CHARACTER.getDefaultAttributes().getBackgroundColor(), Color.RED, EffectType.WAVE_UNDERSCORE, 0));
 
 	public static final TextAttributesKey OPERATOR_SIGN = TextAttributesKey.createTextAttributesKey("NAPILE_OPERATION_SIGN", SyntaxHighlighterColors.OPERATION_SIGN.getDefaultAttributes());
 
 	public static final TextAttributesKey PARENTHESIS = TextAttributesKey.createTextAttributesKey("NAPILE_PARENTHESIS", SyntaxHighlighterColors.PARENTHS.getDefaultAttributes());
 
 	public static final TextAttributesKey BRACES = TextAttributesKey.createTextAttributesKey("NAPILE_BRACES", SyntaxHighlighterColors.BRACES.getDefaultAttributes());
 
 	public static final TextAttributesKey BRACKETS = TextAttributesKey.createTextAttributesKey("NAPILE_BRACKETS", SyntaxHighlighterColors.BRACKETS.getDefaultAttributes());
 
 	public static final TextAttributesKey FUNCTION_LITERAL_BRACES_AND_ARROW = TextAttributesKey.createTextAttributesKey("NAPILE_FUNCTION_LITERAL_BRACES_AND_ARROW", new TextAttributes(null, null, null, null, Font.BOLD));
 
 	public static final TextAttributesKey COMMA = TextAttributesKey.createTextAttributesKey("NAPILE_COMMA", SyntaxHighlighterColors.COMMA.getDefaultAttributes());
 
 	public static final TextAttributesKey SEMICOLON = TextAttributesKey.createTextAttributesKey("NAPILE_SEMICOLON", SyntaxHighlighterColors.JAVA_SEMICOLON.getDefaultAttributes());
 
 	public static final TextAttributesKey DOT = TextAttributesKey.createTextAttributesKey("NAPILE_DOT", SyntaxHighlighterColors.DOT.getDefaultAttributes());
 
 	public static final TextAttributesKey SAFE_ACCESS = TextAttributesKey.createTextAttributesKey("NAPILE_SAFE_ACCESS", SyntaxHighlighterColors.DOT.getDefaultAttributes());
 
 	public static final TextAttributesKey ARROW = TextAttributesKey.createTextAttributesKey("NAPILE_ARROW", SyntaxHighlighterColors.PARENTHS.getDefaultAttributes());
 
 	public static final TextAttributesKey LINE_COMMENT = TextAttributesKey.createTextAttributesKey("NAPILE_LINE_COMMENT", SyntaxHighlighterColors.LINE_COMMENT.getDefaultAttributes());
 
 	public static final TextAttributesKey BLOCK_COMMENT = TextAttributesKey.createTextAttributesKey("NAPILE_BLOCK_COMMENT", SyntaxHighlighterColors.JAVA_BLOCK_COMMENT.getDefaultAttributes());
 
 	public static final TextAttributesKey DOC_COMMENT = TextAttributesKey.createTextAttributesKey("NAPILE_DOC_COMMENT", SyntaxHighlighterColors.DOC_COMMENT.getDefaultAttributes());
 
 	public static final TextAttributesKey DOC_COMMENT_TAG = TextAttributesKey.createTextAttributesKey("NAPILE_DOC_COMMENT_TAG", SyntaxHighlighterColors.DOC_COMMENT_TAG.getDefaultAttributes());
 
 	public static final TextAttributesKey DOC_COMMENT_TAG_VALUE = TextAttributesKey.createTextAttributesKey("NAPILE_DOC_COMMENT_TAG_VALUE", CodeInsightColors.DOC_COMMENT_TAG_VALUE.getDefaultAttributes());
 
 	public static final TextAttributesKey DOC_COMMENT_MARKUP = TextAttributesKey.createTextAttributesKey("NAPILE_DOC_COMMENT_MARKUP", SyntaxHighlighterColors.DOC_COMMENT_MARKUP.getDefaultAttributes());
 
 	public static final TextAttributesKey CLASS = TextAttributesKey.createTextAttributesKey("NAPILE_CLASS", CodeInsightColors.CLASS_NAME_ATTRIBUTES.getDefaultAttributes());
 
 	public static final TextAttributesKey TYPE_PARAMETER = TextAttributesKey.createTextAttributesKey("NAPILE_TYPE_PARAMETER", CodeInsightColors.TYPE_PARAMETER_NAME_ATTRIBUTES.getDefaultAttributes());
 
 	public static final TextAttributesKey ABSTRACT_CLASS = TextAttributesKey.createTextAttributesKey("NAPILE_ABSTRACT_CLASS", CodeInsightColors.ABSTRACT_CLASS_NAME_ATTRIBUTES.getDefaultAttributes());
 
 	public static final TextAttributesKey ANNOTATION = TextAttributesKey.createTextAttributesKey("NAPILE_ANNOTATION", CodeInsightColors.ANNOTATION_NAME_ATTRIBUTES.getDefaultAttributes());
 
 	public static final TextAttributesKey MUTABLE_VARIABLE = TextAttributesKey.createTextAttributesKey("NAPILE_MUTABLE_VARIABLE", new TextAttributes(null, null, Color.BLACK, EffectType.LINE_UNDERSCORE, 0));
 
 	public static final TextAttributesKey LOCAL_VARIABLE = TextAttributesKey.createTextAttributesKey("NAPILE_LOCAL_VARIABLE", CodeInsightColors.LOCAL_VARIABLE_ATTRIBUTES.getDefaultAttributes());
 
 	public static final TextAttributesKey PARAMETER = TextAttributesKey.createTextAttributesKey("NAPILE_PARAMETER", CodeInsightColors.PARAMETER_ATTRIBUTES.getDefaultAttributes());
 
 	public static final TextAttributesKey WRAPPED_INTO_REF = TextAttributesKey.createTextAttributesKey("NAPILE_WRAPPED_INTO_REF", CodeInsightColors.IMPLICIT_ANONYMOUS_CLASS_PARAMETER_ATTRIBUTES.getDefaultAttributes());
 
 	public static final TextAttributesKey INSTANCE_PROPERTY = TextAttributesKey.createTextAttributesKey("NAPILE_INSTANCE_PROPERTY", CodeInsightColors.INSTANCE_FIELD_ATTRIBUTES.getDefaultAttributes());
 
 	public static final TextAttributesKey STATIC_PROPERTY = TextAttributesKey.createTextAttributesKey("NAPILE_NAMESPACE_PROPERTY", CodeInsightColors.STATIC_FIELD_ATTRIBUTES.getDefaultAttributes());
 
 	public static final TextAttributesKey BACKING_FIELD_ACCESS = TextAttributesKey.createTextAttributesKey("NAPILE_BACKING_FIELD_ACCESS", new TextAttributes());
 
 	public static final TextAttributesKey EXTENSION_PROPERTY = TextAttributesKey.createTextAttributesKey("NAPILE_EXTENSION_PROPERTY", new TextAttributes());
 
 	public static final TextAttributesKey FUNCTION_LITERAL_DEFAULT_PARAMETER = TextAttributesKey.createTextAttributesKey("NAPILE_CLOSURE_DEFAULT_PARAMETER", new TextAttributes(null, null, null, null, Font.BOLD));
 
 	public static final TextAttributesKey FUNCTION_DECLARATION = TextAttributesKey.createTextAttributesKey("NAPILE_FUNCTION_DECLARATION", CodeInsightColors.METHOD_DECLARATION_ATTRIBUTES.getDefaultAttributes());
 
 	public static final TextAttributesKey METHOD_CALL = TextAttributesKey.createTextAttributesKey("NAPILE_FUNCTION_CALL", CodeInsightColors.METHOD_CALL_ATTRIBUTES.getDefaultAttributes());
 
 	public static final TextAttributesKey STATIC_METHOD_CALL = TextAttributesKey.createTextAttributesKey("NAPILE_NAMESPACE_FUNCTION_CALL", CodeInsightColors.STATIC_METHOD_ATTRIBUTES.getDefaultAttributes());
 
 	public static final TextAttributesKey EXTENSION_FUNCTION_CALL = TextAttributesKey.createTextAttributesKey("NAPILE_EXTENSION_FUNCTION_CALL", new TextAttributes());
 
 	public static final TextAttributesKey CONSTRUCTOR_CALL = TextAttributesKey.createTextAttributesKey("NAPILE_CONSTRUCTOR", CodeInsightColors.CONSTRUCTOR_CALL_ATTRIBUTES.getDefaultAttributes());
 
 	public static final TextAttributesKey BAD_CHARACTER = TextAttributesKey.createTextAttributesKey("NAPILE_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER.getDefaultAttributes());
 
	public static final TextAttributesKey MACRO_CALL = TextAttributesKey.createTextAttributesKey("NAPILE_AUTO_CASTED_VALUE", new TextAttributes(null, new Color(-4930817), null, null, Font.PLAIN));
 
 	public static final TextAttributesKey AUTO_CASTED_VALUE = TextAttributesKey.createTextAttributesKey("NAPILE_AUTO_CASTED_VALUE", new TextAttributes(null, new Color(0xdbffdb), null, null, Font.PLAIN));
 
 	public static final TextAttributesKey INJECTION_BLOCK = TextAttributesKey.createTextAttributesKey("NAPILE_INJECTION_BLOCK", new TextAttributes(null, Gray._238, null, null, Font.PLAIN));
 
 	public static final TextAttributesKey LABEL = TextAttributesKey.createTextAttributesKey("NAPILE_LABEL", new TextAttributes(new Color(0x4a86e8), null, null, null, Font.PLAIN));
 
 	public static final TextAttributesKey DEBUG_INFO = TextAttributesKey.createTextAttributesKey("NAPILE_DEBUG_INFO", new TextAttributes(null, null, Color.BLACK, EffectType.ROUNDED_BOX, Font.PLAIN));
 
 	public static final TextAttributesKey RESOLVED_TO_ERROR = TextAttributesKey.createTextAttributesKey("NAPILE_RESOLVED_TO_ERROR", new TextAttributes(null, null, Color.RED, EffectType.ROUNDED_BOX, Font.PLAIN));
 
 	private JetHighlightingColors()
 	{
 	}
 
 	@NotNull
 	protected static TextAttributesKey getAttributes(DeclarationDescriptor declarationDescriptor)
 	{
 		if(declarationDescriptor instanceof LocalVariableDescriptor)
 			return LOCAL_VARIABLE;
 		if(declarationDescriptor instanceof PropertyParameterDescriptorImpl)
 			return PARAMETER;
 		if(declarationDescriptor instanceof VariableDescriptor)
 			return ((VariableDescriptor) declarationDescriptor).isStatic() ? STATIC_PROPERTY : INSTANCE_PROPERTY;
 		throw new IllegalArgumentException("invalid : " + declarationDescriptor);
 	}
 }
