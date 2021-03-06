 /*
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
 
 package com.google.devtools.j2objc.translate;
 
 import com.google.common.collect.Lists;
 import com.google.devtools.j2objc.types.GeneratedMethodBinding;
 import com.google.devtools.j2objc.types.NodeCopier;
 import com.google.devtools.j2objc.types.Types;
 import com.google.devtools.j2objc.util.ASTUtil;
 import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
 
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ClassInstanceCreation;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.IMethodBinding;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.IVariableBinding;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.Modifier;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
 import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
 import org.eclipse.jdt.core.dom.SuperMethodInvocation;
 import org.eclipse.jdt.core.dom.ThisExpression;
 
 import java.util.List;
 
 /**
  * Updates variable references outside an inner class to the new fields
  * injected into it.
  *
  * @author Keith Stanger
  */
 public class OuterReferenceFixer extends ErrorReportingASTVisitor {
 
   private IVariableBinding outerParam = null;
 
   @Override
   public boolean visit(MethodDeclaration node) {
     IMethodBinding binding = Types.getMethodBinding(node);
     if (binding.isConstructor()) {
       List<SingleVariableDeclaration> params = ASTUtil.getParameters(node);
       if (params.size() > 0) {
         IVariableBinding firstParam = Types.getVariableBinding(params.get(0));
         if (firstParam.getName().equals("outer$")) {
           outerParam = firstParam;
         }
       }
     }
     return true;
   }
 
   @Override
   public void endVisit(MethodDeclaration node) {
     outerParam = null;
   }
 
   @Override
   public boolean visit(ClassInstanceCreation node) {
     ITypeBinding newType = Types.getTypeBinding(node).getTypeDeclaration();
     ITypeBinding declaringClass = newType.getDeclaringClass();
     if (Modifier.isStatic(newType.getModifiers()) || declaringClass == null) {
       return true;
     }
 
     AST ast = node.getAST();
     GeneratedMethodBinding binding = Types.getGeneratedMethodBinding(node);
     addOuterArg(node, binding, declaringClass);
 
    for (IVariableBinding capturedVar : getCapturedVariables(node)) {
       ASTUtil.getArguments(node).add(ASTFactory.newSimpleName(ast, capturedVar));
       binding.addParameter(capturedVar.getType());
     }
 
     assert binding.isVarargs() || node.arguments().size() == binding.getParameterTypes().length;
     return true;
   }
 
  private List<IVariableBinding> getCapturedVariables(ClassInstanceCreation node) {
    ITypeBinding newType = Types.getTypeBinding(node).getTypeDeclaration();
    ITypeBinding owningType =
        Types.getTypeBinding(ASTUtil.getOwningType(node)).getTypeDeclaration();
    // Test for the recursive construction of a local class.
    if (owningType.isEqualTo(newType)) {
      return OuterReferenceResolver.getInnerFields(newType);
    }
    return OuterReferenceResolver.getCapturedVars(newType);
  }

   private void addOuterArg(
       ClassInstanceCreation node, GeneratedMethodBinding binding, ITypeBinding declaringClass) {
     ITypeBinding type = Types.getTypeBinding(node);
     if (!OuterReferenceResolver.needsOuterParam(type)) {
       return;
     }
 
     AST ast = node.getAST();
     Expression outerExpr = node.getExpression();
     List<IVariableBinding> path = OuterReferenceResolver.getPath(node);
     Expression outerArg = null;
 
     if (outerExpr != null) {
       node.setExpression(null);
       outerArg = NodeCopier.copySubtree(ast, outerExpr);
     } else if (path != null) {
       outerArg = ASTFactory.newName(ast, fixPath(path));
     } else {
       outerArg = ast.newThisExpression();
       Types.addBinding(outerArg, declaringClass);
     }
 
     ASTUtil.getArguments(node).add(0, outerArg);
     binding.addParameter(0, declaringClass);
   }
 
   @Override
   public boolean visit(MethodInvocation node) {
     List<IVariableBinding> path = OuterReferenceResolver.getPath(node);
     if (path != null) {
       node.setExpression(ASTFactory.newName(node.getAST(), fixPath(path)));
     }
     return true;
   }
 
   @Override
   public void endVisit(SuperMethodInvocation node) {
     List<IVariableBinding> path = OuterReferenceResolver.getPath(node);
     if (path != null) {
       // We substitute the qualifying type name with the outer variable name.
       node.setQualifier(ASTFactory.newName(node.getAST(), fixPath(path)));
     } else {
       node.setQualifier(null);
     }
   }
 
   @Override
   public boolean visit(SimpleName node) {
     List<IVariableBinding> path = OuterReferenceResolver.getPath(node);
     if (path != null) {
       AST ast = node.getAST();
       if (path.size() == 1 && path.get(0).getConstantValue() != null) {
         IVariableBinding var = path.get(0);
         ASTUtil.setProperty(node,
             ASTFactory.makeLiteral(ast, var.getConstantValue(), var.getType()));
       } else {
         ASTUtil.setProperty(node, ASTFactory.newName(ast, fixPath(path)));
       }
     }
     return true;
   }
 
   @Override
   public boolean visit(ThisExpression node) {
     List<IVariableBinding> path = OuterReferenceResolver.getPath(node);
     if (path != null) {
       ASTUtil.setProperty(node, ASTFactory.newName(node.getAST(), fixPath(path)));
     } else {
       node.setQualifier(null);
     }
     return true;
   }
 
   @Override
   public void endVisit(SuperConstructorInvocation node) {
     Expression outerExpression = node.getExpression();
     if (outerExpression == null) {
       return;
     }
     node.setExpression(null);
     ITypeBinding outerExpressionType = Types.getTypeBinding(outerExpression);
     GeneratedMethodBinding binding = Types.getGeneratedMethodBinding(node);
     ASTUtil.getArguments(node).add(0, outerExpression);
     binding.addParameter(0, outerExpressionType);
   }
 
   private List<IVariableBinding> fixPath(List<IVariableBinding> path) {
     if (path.get(0) == OuterReferenceResolver.OUTER_PARAMETER) {
       assert outerParam != null;
       path = Lists.newArrayList(path);
       path.set(0, outerParam);
     }
     return path;
   }
 }
