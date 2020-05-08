 /*
  * Copyright 2013, Bruce Mitchener, Jr.
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
 
 package org.dylanfoundry.deft.filetypes.dylan.formatter;
 
 import com.intellij.formatting.*;
 import com.intellij.lang.ASTNode;
 import com.intellij.openapi.util.TextRange;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiErrorElement;
 import com.intellij.psi.PsiWhiteSpace;
 import com.intellij.psi.TokenType;
 import com.intellij.psi.tree.IElementType;
 import com.intellij.psi.tree.TokenSet;
 import com.intellij.psi.util.PsiTreeUtil;
 import org.dylanfoundry.deft.filetypes.dylan.psi.DylanTypes;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class DylanFormattingBlock implements ASTBlock {
   private final DylanFormattingBlock myParent;
   private final Alignment _alignment;
   private final Indent _indent;
   private final ASTNode _node;
   private final Wrap _wrap;
   private final DylanFormattingBlockContext myContext;
   private List<DylanFormattingBlock> _subBlocks = null;
   private Alignment myChildAlignment;
 
   private static final TokenSet BLOCK_TOKEN_SET = TokenSet.create(
     DylanTypes.IF_STATEMENT,
     DylanTypes.ELSE_STATEMENT,
     DylanTypes.ELSEIF_STATEMENT,
     DylanTypes.BEGIN_STATEMENT,
     DylanTypes.BLOCK_STATEMENT,
     DylanTypes.AFTERWARDS_STATEMENT,
     DylanTypes.CLEANUP_STATEMENT,
     DylanTypes.EXCEPTION_STATEMENT,
     DylanTypes.FOR_STATEMENT,
     DylanTypes.FINALLY_CLAUSE,
     DylanTypes.METHOD_STATEMENT,
     DylanTypes.UNLESS_STATEMENT,
     DylanTypes.UNTIL_STATEMENT,
     DylanTypes.WHEN_STATEMENT,
     DylanTypes.WHILE_STATEMENT,
     DylanTypes.SELECT_STATEMENT,
     DylanTypes.CASE_STATEMENT,
     DylanTypes.MACRO_STATEMENT
   );
 
   private static final TokenSet DEFINITION_TOKEN_SET = TokenSet.create(
     DylanTypes.DEFINITION_CLASS_DEFINER,
     DylanTypes.DEFINITION_CONSTANT_DEFINER,
     DylanTypes.DEFINITION_COPY_DOWN_METHOD_DEFINER,
     DylanTypes.DEFINITION_DOMAIN_DEFINER,
     DylanTypes.DEFINITION_FUNCTION_DEFINER,
     DylanTypes.DEFINITION_GENERIC_DEFINER,
     DylanTypes.DEFINITION_LIBRARY_DEFINER,
     DylanTypes.DEFINITION_MODULE_DEFINER,
     DylanTypes.DEFINITION_MACRO_DEFINER,
     DylanTypes.DEFINITION_METHOD_DEFINER,
     DylanTypes.DEFINITION_SHARED_SYMBOLS_DEFINER,
     DylanTypes.DEFINITION_SUITE_DEFINER,
     DylanTypes.DEFINITION_TABLE_DEFINER,
     DylanTypes.DEFINITION_TEST_DEFINER,
     DylanTypes.DEFINITION_VARIABLE_DEFINER,
     DylanTypes.DEFINITION_MACRO_CALL
   );
   private static final TokenSet ourListElementTypes = TokenSet.create(
     DylanTypes.ARGUMENT,
     DylanTypes.REQUIRED_PARAMETER,
     DylanTypes.NEXT_REST_KEY_PARAMETER_LIST,
     DylanTypes.VARIABLE
   );
 
   public DylanFormattingBlock(final DylanFormattingBlock parent,
                               final ASTNode node,
                               final Alignment alignment,
                               final Indent indent,
                               final Wrap wrap,
                               final DylanFormattingBlockContext context) {
     myParent = parent;
     _alignment = alignment;
     _node = node;
     _indent = indent;
     _wrap = wrap;
     myContext = context;
   }
 
   @NotNull
   @Override
   public ASTNode getNode() {
     return _node;
   }
 
   @NotNull
   @Override
   public TextRange getTextRange() {
     return _node.getTextRange();
   }
 
   @NotNull
   @Override
   public List<Block> getSubBlocks() {
     if (_subBlocks == null) {
       _subBlocks = buildSubBlocks();
     }
     return new ArrayList<Block>(_subBlocks);
   }
 
   private List<DylanFormattingBlock> buildSubBlocks() {
     List<DylanFormattingBlock> blocks = new ArrayList<DylanFormattingBlock>();
     for (ASTNode child = _node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
       IElementType childType = child.getElementType();
 
       if (child.getTextRange().getLength() == 0) continue;
       if (childType == TokenType.WHITE_SPACE) continue;
 
       blocks.add(buildSubBlock(child));
     }
     return Collections.unmodifiableList(blocks);
   }
 
   private DylanFormattingBlock buildSubBlock(ASTNode child) {
     IElementType parentType = _node.getElementType();
     IElementType grandParentType = _node.getTreeParent() == null ? null : _node.getTreeParent().getElementType();
     IElementType childType = child.getElementType();
     Wrap wrap = null;
     Indent childIndent = Indent.getNoneIndent();
     Alignment childAlignment = null;
 
     if (BLOCK_TOKEN_SET.contains(parentType)) {
       if ((childType == DylanTypes.BODY) || (childType == DylanTypes.COMMENT)) {
         childIndent = Indent.getNormalIndent();
       }
     }
 
    if ((DEFINITION_TOKEN_SET.contains(parentType)) && ((childType == DylanTypes.BODY) || (childType == DylanTypes.COMMENT))) {
       childIndent = Indent.getNormalIndent();
     }
 
     // TODO: Add settings to set this type of indent.
     if ((DEFINITION_TOKEN_SET.contains(grandParentType)) && (parentType == DylanTypes.PARAMETER_LIST)) {
       if (childType == DylanTypes.EQUAL_ARROW) {
         childIndent = Indent.getSpaceIndent(1);
       } else if (childType == DylanTypes.LPAREN) {
         childIndent = Indent.getSpaceIndent(4);
       }
     }
 
     if ((childType == DylanTypes.PARAMETERS) || (childType == DylanTypes.ARGUMENTS) || (childType == DylanTypes.VALUES_LIST)) {
       childAlignment = getAlignmentForChildren();
     }
 
     if (parentType == DylanTypes.DEFINITION_CLASS_DEFINER) {
       if (childType == DylanTypes.SLOT_DECLARATIONS) {
         childIndent = Indent.getNormalIndent();
       }
       if (childType == DylanTypes.SUPERS) {
         childAlignment = getAlignmentForChildren();
         childIndent = Indent.getSpaceIndent(4);
       }
       if (childType == DylanTypes.LPAREN) {
         childIndent = Indent.getSpaceIndent(4);
       }
     }
     if ((grandParentType == DylanTypes.SLOT_OPTIONS) || (grandParentType == DylanTypes.INIT_EXPRESSION) ||
         (grandParentType == DylanTypes.INIT_ARG_OPTIONS) || (grandParentType == DylanTypes.INHERITED_OPTIONS)) {
       childIndent = Indent.getNormalIndent();
     }
 
     return new DylanFormattingBlock(this, child, childAlignment, childIndent, wrap, myContext);
   }
 
   @Nullable
   @Override
   public Wrap getWrap() {
     return _wrap;
   }
 
   @Nullable
   @Override
   public Indent getIndent() {
     assert _indent != null;
     return _indent;
   }
 
   @Nullable
   @Override
   public Alignment getAlignment() {
     return _alignment;
   }
 
   @Nullable
   @Override
   public Spacing getSpacing(@Nullable Block block, @NotNull Block block2) {
     return myContext.getSpacingBuilder().getSpacing(this, block, block2);
   }
 
   @NotNull
   @Override
   public ChildAttributes getChildAttributes(int newChildIndex) {
     Indent childIndent = getChildIndent(newChildIndex);
     Alignment childAlignment = getChildAlignment();
     return new ChildAttributes(childIndent, childAlignment);
   }
 
   private Indent getChildIndent(int newChildIndex) {
     return Indent.getNoneIndent();
   }
 
   @Nullable
   private Alignment getChildAlignment() {
     if (ourListElementTypes.contains(_node.getElementType())) {
       return getAlignmentForChildren();
     }
     return null;
   }
 
   @Override
   public boolean isIncomplete() {
     // If there's something following, it's not incomplete
     if (!PsiTreeUtil.hasErrorElements(_node.getPsi())) {
       PsiElement element = _node.getPsi().getNextSibling();
       while (element instanceof PsiWhiteSpace) {
         element = element.getNextSibling();
       }
       if (element != null) {
         return false;
       }
     }
 
     ASTNode lastChild = getLastNonSpaceChild(_node, false);
     if (lastChild != null) {
       // TODO: do this for real
     }
     return false; // TODO: Until we properly implement this method we always consider it as incomplete
   }
 
   private static ASTNode getLastNonSpaceChild(ASTNode node, boolean acceptError) {
     ASTNode lastChild = node.getLastChildNode();
     while (lastChild != null &&
       (lastChild.getElementType() == TokenType.WHITE_SPACE || (!acceptError && lastChild.getPsi() instanceof PsiErrorElement))) {
       lastChild = lastChild.getTreePrev();
     }
     return lastChild;
   }
 
   @Override
   public boolean isLeaf() {
     return _node.getFirstChildNode() == null;
   }
 
   public Alignment getAlignmentForChildren() {
     if (myChildAlignment == null) {
       myChildAlignment = Alignment.createAlignment();
     }
     return myChildAlignment;
   }
 }
