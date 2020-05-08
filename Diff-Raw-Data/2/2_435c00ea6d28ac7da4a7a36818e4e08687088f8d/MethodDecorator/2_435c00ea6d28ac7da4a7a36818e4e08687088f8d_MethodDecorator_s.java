 package pl.projectspace.idea.plugins.commons.php.psi.element;
 
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.util.PsiTreeUtil;
 import com.jetbrains.php.lang.psi.elements.MethodReference;
 import com.jetbrains.php.lang.psi.elements.PhpClass;
 import pl.projectspace.idea.plugins.commons.php.psi.exceptions.InvalidArgumentException;
 import pl.projectspace.idea.plugins.commons.php.psi.exceptions.MissingElementException;
 
 /**
  * @author Michal Przytulski <michal@przytulski.pl>
  */
 public abstract class MethodDecorator {
 
     protected PhpClass target;
 
     protected final MethodReference element;
 
     protected Object returnType = null;
 
     public MethodDecorator(PsiElement element) throws InvalidArgumentException {
         if (!(element instanceof MethodReference)) {
             throw new InvalidArgumentException("Passed PsiElement should be an instance of MethodReference");
         }
 
         this.element = (MethodReference) element;
     }
 
     public PhpClass getTarget() {
         if (target == null) {
             target = PsiTreeUtil.getParentOfType(element, PhpClass.class);
         }
 
         return target;
     }
 
     public boolean hasParameter(int no) {
         return (element.getParameters().length > no);
     }
 
     public boolean hasParameter(int no, Class type) {
        return (hasParameter(no) && element.getParameters()[no].getClass().isInstance(type));
     }
 
     public PsiElement getParameter(int no) {
         return element.getParameters()[no];
     }
 
     public boolean isResolvableToType() {
         try {
             return (getReturnType() != null);
         } catch (MissingElementException e) {
             return false;
         }
     }
 
     public Object getReturnType() throws MissingElementException {
         if (returnType == null) {
             returnType = resolveType();
         }
 
         return returnType;
     }
 
     protected abstract Object resolveType() throws MissingElementException;
 
 }
