 package ideah.psi.impl;
 
 import com.intellij.extapi.psi.ASTDelegatePsiElement;
 import com.intellij.lang.ASTNode;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.impl.source.tree.SharedImplUtil;
 import ideah.psi.api.util.HaskellPsiElement;
 import org.jetbrains.annotations.NotNull;
 
 public abstract class HaskellBaseElementImpl extends ASTDelegatePsiElement implements HaskellPsiElement {
 
     @NotNull
     private final ASTNode node;
 
     protected HaskellBaseElementImpl(@NotNull ASTNode node) {
         this.node = node;
     }
 
     public final PsiElement getParent() {
         return SharedImplUtil.getParent(getNode());
     }
 
     @NotNull
     @Override
     public final ASTNode getNode() {
         return node;
     }
 
     protected final String getSrcSpan() {
         return node.getTextRange().toString();
     }
 
     @Override
     public String toString() {
        return getSrcSpan();
     }
 }
