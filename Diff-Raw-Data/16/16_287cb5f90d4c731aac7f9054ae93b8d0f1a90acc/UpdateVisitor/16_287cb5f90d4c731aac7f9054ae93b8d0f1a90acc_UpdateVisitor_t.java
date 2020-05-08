 /*
  * Copyright 2012 jccastrejon
  *  
  * This file is part of ExSchema.
  *
  * ExSchema is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * ExSchema is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with ExSchema. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.imag.exschema;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 
 /**
  * Generic visitor for classes that need to identify update operations,
  * associated to a particular Method binding and with a specific name.
  * 
  * @author jccastrejon
  * 
  */
 public abstract class UpdateVisitor extends ASTVisitor {
 
     /**
      * Update's variable name.
      */
     private String variableName;
 
     /**
      * Matching invocations.
      */
     private List<MethodInvocation> updateInvocations;
 
     /**
      * Default constructor.
      */
     public UpdateVisitor() {
         this.updateInvocations = new ArrayList<MethodInvocation>();
     }
 
     /**
      * Full constructor.
      * 
      * @param variableName
      */
     public UpdateVisitor(final String variableName) {
         this.variableName = variableName;
         this.updateInvocations = new ArrayList<MethodInvocation>();
     }
 
     @Override
     public boolean visit(final MethodInvocation invocation) {
         if (this.isUpdateMethod(invocation)) {
             this.updateInvocations.add(invocation);
         }
 
         return super.visit(invocation);
     }
 
     /**
      * Determine if the specified method matches with the ones we're looking
      * for.
      * 
      * @param invocation
      * @return
      */
     private boolean isUpdateMethod(final MethodInvocation invocation) {
         boolean returnValue;
 
         returnValue = false;
        if (invocation.resolveMethodBinding() != null) {
            if (this.getMethodBinding()
                    .equals(invocation.resolveMethodBinding().getDeclaringClass().getQualifiedName())) {
                if (invocation.getName().toString().equals(this.getMethodName())) {
                    if (this.variableName != null) {
                        if (this.variableName.equals(invocation.getExpression().toString())) {
                            returnValue = true;
                        }
                    } else {
                         returnValue = true;
                     }
                 }
             }
         }
 
         return returnValue;
     }
 
     /**
      * 
      * @return
      */
     public List<MethodInvocation> getUpdateInvocations() {
         return this.updateInvocations;
     }
 
     /**
      * 
      * @param variableName
      */
     public void setVariableName(final String variableName) {
         this.variableName = variableName;
     }
 
     /**
      * The fully qualified name of the expected method binding.
      * 
      * @return
      */
     protected abstract String getMethodBinding();
 
     /**
      * Expected method name.
      * 
      * @return
      */
     protected abstract String getMethodName();
 }
