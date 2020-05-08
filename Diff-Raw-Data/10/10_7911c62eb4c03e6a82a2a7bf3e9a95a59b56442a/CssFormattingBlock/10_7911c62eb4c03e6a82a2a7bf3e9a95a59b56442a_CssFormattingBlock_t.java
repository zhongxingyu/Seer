 package org.consulo.css.lang.formatting;
 
 import com.intellij.formatting.*;
 import com.intellij.lang.ASTNode;
 import com.intellij.psi.formatter.common.AbstractBlock;
 import com.intellij.psi.tree.IElementType;
 import org.consulo.css.lang.CssPsiTokens;
 import org.consulo.css.lang.CssTokens;
 import org.consulo.css.lang.parser.CssParserDefinition;
 import org.consulo.css.lang.psi.CssBlock;
 import org.consulo.css.lang.psi.CssFile;
 import org.consulo.css.lang.psi.CssProperty;
 import org.consulo.css.lang.psi.CssRule;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author VISTALL
  * @since 08.07.13.
  */
 public class CssFormattingBlock extends AbstractBlock {
 
   public CssFormattingBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment) {
     super(node, wrap, alignment);
   }
 
   @Override
   protected List<Block> buildChildren() {
     IElementType elementType = getNode().getElementType();
     List<Block> blocks = new ArrayList<Block>();
     if (elementType == CssParserDefinition.FILE_ELEMENT) {
       CssFile psi = getNode().getPsi(CssFile.class);
       for (CssRule cssRule : psi.getRules()) {
         blocks.add(new CssFormattingBlock(cssRule.getNode(), null, null));
       }
     } else if (elementType == CssPsiTokens.RULE) {
       CssRule psi = getNode().getPsi(CssRule.class);
       blocks.add(new CssFormattingBlock(psi.getSelectorReference().getNode(), null, null));
 
       CssBlock block = psi.getBlock();
       if (block != null) {
         blocks.add(new CssFormattingBlock(block.getNode(), null, null));
       }
     } else if (elementType == CssPsiTokens.BLOCK) {
       CssBlock psi = getNode().getPsi(CssBlock.class);
       ASTNode temp = getNode().findChildByType(CssTokens.LBRACE);
       if (temp != null) {
         blocks.add(new CssFormattingBlock(temp, null, null));
       }
 
       for (CssProperty cssProperty : psi.getProperties()) {
         blocks.add(new CssFormattingBlock(cssProperty.getNode(), null, null));
       }
 
       temp = getNode().findChildByType(CssTokens.RBRACE);
       if (temp != null) {
         blocks.add(new CssFormattingBlock(temp, null, null));
       }
     }
     return blocks;
   }
 
   @Nullable
   @Override
   public Spacing getSpacing(@Nullable Block block, @NotNull Block block2) {
     return null;
   }
 
   @Override
   public Indent getIndent() {
     IElementType elementType = getNode().getElementType();
     if (elementType == CssParserDefinition.FILE_ELEMENT) {
       return Indent.getAbsoluteNoneIndent();
     } else if (elementType == CssPsiTokens.RULE || elementType == CssPsiTokens.BLOCK || elementType == CssPsiTokens.SELECTOR_REFERENCE) {
       return Indent.getNoneIndent();
     } else if (elementType == CssTokens.LBRACE || elementType == CssTokens.RBRACE) {
       return Indent.getNoneIndent();
     } else if (elementType == CssPsiTokens.PROPERTY) {
       return Indent.getNormalIndent();
     }
     throw new IllegalArgumentException(elementType.toString());
   }
 
  @Nullable
  @Override
  protected Indent getChildIndent() {
    IElementType elementType = getNode().getElementType();
    if (elementType == CssPsiTokens.BLOCK) {
      return Indent.getNormalIndent();
    }
    return null;
  }

   @Override
   public boolean isLeaf() {
     IElementType elementType = getNode().getElementType();
     return elementType == CssTokens.LBRACE || elementType == CssTokens.RBRACE;
   }
 }
