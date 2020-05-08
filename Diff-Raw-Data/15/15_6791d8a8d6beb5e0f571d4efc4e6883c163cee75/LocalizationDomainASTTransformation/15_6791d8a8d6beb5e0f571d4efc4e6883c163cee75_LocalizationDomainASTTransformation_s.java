/* Copyright 2010 Izzet Mustafayev
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package name.webdizz.grails.glangs.ast;
 
 import java.util.List;
 
 import org.codehaus.groovy.ast.ASTNode;
 import org.codehaus.groovy.ast.ClassNode;
 import org.codehaus.groovy.ast.ModuleNode;
 import org.codehaus.groovy.control.CompilePhase;
 import org.codehaus.groovy.control.SourceUnit;
 import org.codehaus.groovy.grails.compiler.injection.GrailsDomainClassInjector;
 import org.codehaus.groovy.transform.ASTTransformation;
 import org.codehaus.groovy.transform.GroovyASTTransformation;
 
 /**
  * A global AST transformation that injects localization attributes to the
  * domain classes.
  * 
 * @author Izzet Mustafayev
  * @since 0.1
  * 
  */
 @GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
 public class LocalizationDomainASTTransformation implements ASTTransformation {
 
 	private GrailsDomainClassInjector injector = new DomainLocalizationInjector();
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.codehaus.groovy.transform.ASTTransformation#visit(org.codehaus.groovy
 	 * .ast.ASTNode[], org.codehaus.groovy.control.SourceUnit)
 	 */
 	public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
 
 		ASTNode astNode = astNodes[0];
 
 		if (astNode instanceof ModuleNode) {
 			ModuleNode moduleNode = (ModuleNode) astNode;
 
 			List classes = moduleNode.getClasses();
 			if (classes.size() > 0) {
 				ClassNode classNode = (ClassNode) classes.get(0);
 				injector.performInjection(sourceUnit, classNode);
 			}
 
 		}
 	}
 
 }
