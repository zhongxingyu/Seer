 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright  2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
 * $Id: ConstructorTransformVisitor.java,v 1.2 2005-06-17 16:17:46 gasiunas Exp $
  */
 
 package org.caesarj.compiler.contructors;
 
 import org.caesarj.compiler.CompilerBase;
 import org.caesarj.compiler.KjcEnvironment;
 import org.caesarj.compiler.ast.phylum.JPhylum;
 import org.caesarj.compiler.ast.phylum.declaration.CjInitMethodDeclaration;
 import org.caesarj.compiler.ast.phylum.declaration.CjMixinInterfaceDeclaration;
 import org.caesarj.compiler.ast.phylum.declaration.CjVirtualClassCtorDeclaration;
 import org.caesarj.compiler.ast.phylum.declaration.CjVirtualClassDeclaration;
 import org.caesarj.compiler.ast.phylum.declaration.JConstructorDeclaration;
 import org.caesarj.compiler.ast.phylum.declaration.JMemberDeclaration;
 import org.caesarj.compiler.ast.phylum.declaration.JMethodDeclaration;
 import org.caesarj.compiler.ast.phylum.statement.JBlock;
 import org.caesarj.compiler.ast.phylum.statement.JConstructorBlock;
 import org.caesarj.compiler.ast.visitor.IVisitor;
 import org.caesarj.compiler.ast.visitor.VisitorSupport;
 import org.caesarj.compiler.constants.CaesarConstants;
 import org.caesarj.compiler.types.CClassNameType;
 import org.caesarj.compiler.types.CReferenceType;
 import org.caesarj.compiler.types.CType;
 import org.caesarj.compiler.types.TypeFactory;
 
 /**
  * Visits all classthe AST down to expression granularity.
  * It extends the interface of advice methods, if they make use of Join Point Reflection.
  * It also determines the corresponding extraArgumentFlags for the advice.
  * 
  * Ivica> changed visitor
  * 
  * @author Jrgen Hallpap
  * @author Ivica Aracic
  */
 public class ConstructorTransformVisitor implements IVisitor, CaesarConstants  {
 
 	private CompilerBase compiler; 
 	private TypeFactory factory;
 	private VisitorSupport visitor = new VisitorSupport(this);
 	
 	public ConstructorTransformVisitor(CompilerBase compiler, KjcEnvironment environment) {
 		this.factory = environment.getTypeFactory();
 		this.compiler = compiler;	
 	}
 	
 	public boolean start(JPhylum node) {
         return visitor.start(node);
     }
 	
 	public void end() {
 	    visitor.end();
     }
 	
 	// recurse by default into all nodes...
 	public boolean visit(JPhylum self) {
 	    return true;
     }
     
 	// ...but not into member declaration...
 	public boolean visit(JMemberDeclaration self) {
 	    return false;
     }    
 
 	// except CjClassDeclaration
     public boolean visit(CjVirtualClassDeclaration self) {
     	transformConstructors(self);
         return true;
     }
     
     public void transformConstructors(CjVirtualClassDeclaration cd) {
     	// add outer variable
     	CType outerType = null;
     	
     	CjMixinInterfaceDeclaration mixinIfc = cd.getMixinIfcDeclaration();
     	
     	// create a type reference representing the class declaration
         CReferenceType selfType = new CClassNameType(mixinIfc.getIdent());
         
         JMethodDeclaration methods[] = cd.getMethods();
         
         // transform contructors to "init" methods
         int ctorIndex = -1;
         for (int i = 0; i < methods.length; i++) {
 			if (methods[i] instanceof JConstructorDeclaration) {
 				if (methods[i].getParameters().length == 0) {
 					ctorIndex = i;
 					methods[ctorIndex] = new CjVirtualClassCtorDeclaration(
 						methods[ctorIndex].getTokenReference(),
 						methods[ctorIndex].getModifiers(),
 						methods[ctorIndex].getIdent(),
 						outerType,
 						new JBlock(
 							methods[ctorIndex].getTokenReference(), 
 							((JConstructorBlock)methods[ctorIndex].getBlockBody()).getBody(), 
 							null
 						),
 						factory
 					);
 				}
 				else {
 					String methIdent = "init$"+mixinIfc.getIdent();
 			        
 					methods[i] = new CjInitMethodDeclaration(
 						methods[i].getTokenReference(),
 						methods[i].getModifiers(),
 						selfType,
 						methIdent,
 						methods[i].getParameters(),
 						methods[i].getExceptions(),
 						new JBlock(
 							methods[i].getTokenReference(), 
 							((JConstructorBlock)methods[i].getBlockBody()).getBody(), 
 							null
 						)
 					);
 				}
 			}
 		}
         
         cd.setMethods(methods);
                 
         if (ctorIndex == -1) {
 	        // add default ctor
 	        cd.addMethod(
 	    		new CjVirtualClassCtorDeclaration(
 					cd.getTokenReference(),
 					ACC_PUBLIC,
 					cd.getIdent(),
 					outerType,
 					factory
 				)
 			);
         }
     }
 }
