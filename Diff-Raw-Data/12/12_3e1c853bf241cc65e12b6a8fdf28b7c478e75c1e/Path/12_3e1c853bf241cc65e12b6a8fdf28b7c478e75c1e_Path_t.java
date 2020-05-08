 package org.caesarj.compiler.family;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.caesarj.compiler.ast.phylum.expression.JExpression;
 import org.caesarj.compiler.ast.phylum.expression.JFieldAccessExpression;
 import org.caesarj.compiler.ast.phylum.expression.JLocalVariableExpression;
 import org.caesarj.compiler.ast.phylum.expression.JMethodCallExpression;
 import org.caesarj.compiler.ast.phylum.expression.JOwnerExpression;
 import org.caesarj.compiler.ast.phylum.expression.JThisExpression;
 import org.caesarj.compiler.ast.phylum.variable.JFormalParameter;
 import org.caesarj.compiler.ast.phylum.variable.JLocalVariable;
 import org.caesarj.compiler.ast.phylum.variable.JVariableDefinition;
 import org.caesarj.compiler.constants.Constants;
 import org.caesarj.compiler.context.CBlockContext;
 import org.caesarj.compiler.context.CClassContext;
 import org.caesarj.compiler.context.CContext;
 import org.caesarj.compiler.context.CMethodContext;
 import org.caesarj.compiler.export.CClass;
 import org.caesarj.compiler.types.CReferenceType;
 import org.caesarj.compiler.types.CType;
 import org.caesarj.util.InconsistencyException;
 
 /**
  * ...
  * 
  * @author Karl Klose
  */
 public abstract class Path {       
     
     Path prefix;
     CReferenceType type;
     
     private Path getPrefix() {
         return prefix;
     }      
     
     public Path(Path prefix, CReferenceType type) {
         this.prefix = prefix;
         this.type = type;
     }
     
     public CReferenceType getType() {
         return type;
     }
 
     /**
      * Calculate <code>k</code> for an expression in a given context.
      * @param contextClass The context in which the expression should be analysed
      * @param expr	The expression containing the path
      */
     public static int calcK(CContext context, JExpression expr){
         Path p = createFrom(context, expr);
         return ((ContextExpression)p.getHead()).getK(); 
     }
     
     
     /**
      * Create a path from an expression.
      * @param contextClass The context in which the expression should be analysed
      * @param expr	The expression containing the path
      */
     public static Path createFrom(CContext context, JExpression expr){    	
         List 	l1 = new LinkedList();	// stores the field access identifiers (Strings)
         List 	l2 = new LinkedList();	// stores the field access identifiers (Strings)
         int 	k =0;
         JExpression tmp = expr;
         
         boolean done=false;
         
         while (!done){
             if (tmp instanceof JFieldAccessExpression){
                 // if tmp is a field-access...
                 JFieldAccessExpression fa = (JFieldAccessExpression)tmp;
                 if ( fa.getIdent().equals(Constants.JAV_OUTER_THIS) ){
                     // ... if it is this$0.xxx then increase k ...
                     context = context.getParentContext();
                     k++;
                 } else {
                     // ... else add identifier to path.
                     l1.add(0, fa.getIdent());    
                     l2.add(0, fa.getType(context.getTypeFactory()));
                 }
                 tmp = fa.getPrefix();
             
             }
            else if (tmp instanceof JThisExpression) {
                 while (! (context instanceof CClassContext)){
                     context = context.getParentContext();
                     k++;
                 }
                CClass owner = ((JThisExpression)tmp).getSelf();
                 CClass current = context.getClassContext().getCClass();
                 while(current != owner) {
                     if(current == null) throw new InconsistencyException();
                     current = current.getOwner();
                     k++;
                 }
                 done = true;
             }
//            else if (tmp instanceof JThisExpression ){
//                done = true;
//            } 
             else if (tmp instanceof JLocalVariableExpression) {
                 JLocalVariableExpression local = (JLocalVariableExpression) tmp;
                 JLocalVariable var = local.getVariable();
                 
                 l1.add(0, var.getIdent());
                 l2.add(0, var.getType());
                 
                 if (var instanceof JFormalParameter) {
                     // find next MethodContext
                     while (! (context instanceof CMethodContext)) {
                         context = context.getParentContext();
                         k++;
                     }
                 } 
                 else if (var instanceof JVariableDefinition) {
                     // find next block-context that declares this variable
                     boolean found = false;
                     do {
                         if (! (context instanceof CBlockContext)){
                             throw new RuntimeException("Cannot find "+var.getIdent());
                         }
                         CBlockContext block = (CBlockContext)context;
                         if (block.containsVariable(var.getIdent())){
                             found = true;
                         } else {
                             context = context.getParentContext();
                             k++;
                         }
                     } while (!found);                    
                 }
                 done = true;
             } else if (tmp instanceof JMethodCallExpression){
                 JMethodCallExpression me = (JMethodCallExpression) tmp;
                 if (me.getIdent().startsWith(Constants.JAV_ACCESSOR) ){
                     CType type = me.getType(null);
                     CClass clazz = type.getCClass();
                     // ... find first class context ... 
                     while (!(context instanceof CClassContext)){
                         context = context.getParentContext();
                         k++;
                     }
                     // ... and search for the correct outer class.
                     while ( ((CClassContext)context).getCClass() != clazz ){
                         context = context.getParentContext();
                         k++;
                         if ( context == null || !(context instanceof CClassContext) ){
                             throw new InconsistencyException("accessor method does not return outer class");
                         }
                     }
                     done = true;
                 } else {
                     throw new InconsistencyException("Path can not include method calls");
                 }
             } else {
                 throw new InconsistencyException("Illegal expression in dependent type path: "+tmp);
             }
         }
         
                 
         // Construct path        
         Path result = new ContextExpression(null, k, null);
         
         
         Iterator it1, it2;
         for (it1 = l1.iterator(), it2=l2.iterator(); it1.hasNext();) {
             String field = (String) it1.next();
             result = new FieldAccess(result, field, (CReferenceType)it2.next());            
         }        
         return result;        
     }
 
     public boolean equals(Path other) {
         return other.toString().equals(this.toString());
     }
     
     Path getHead() {
         if(prefix != null)
             return prefix.getHead();
         else 
             return this;
     }
     
     Path getHeadPred() {
         if(prefix == null) return null;
         if(prefix.prefix != null)
             return prefix.getHeadPred();
         else 
             return this;
     }
     
     public abstract Path normalize();
     
     protected abstract Path _normalize(Path pred, Path tail);
     
     protected abstract Path clonePath();    
 }
