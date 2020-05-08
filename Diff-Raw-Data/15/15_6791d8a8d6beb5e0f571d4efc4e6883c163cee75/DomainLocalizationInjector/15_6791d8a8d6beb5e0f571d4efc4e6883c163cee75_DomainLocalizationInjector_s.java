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
 
 import grails.util.GrailsConfig;
 
 import java.lang.reflect.Modifier;
 
 import org.codehaus.groovy.ast.ClassNode;
 import org.codehaus.groovy.control.SourceUnit;
 import org.codehaus.groovy.grails.compiler.injection.DefaultGrailsDomainClassInjector;
 import org.codehaus.groovy.grails.compiler.injection.GrailsASTUtils;
 
 /**
  * The injector of the domain classes' localization attributes.
  * 
 * @author Izzet Mustafayev
  * @since 0.1
  * 
  */
 public class DomainLocalizationInjector extends DefaultGrailsDomainClassInjector {
 
 	@Override
 	public void performInjection(SourceUnit source, ClassNode classNode) {
 		if (isDomainClass(classNode, source) && shouldInjectClass(classNode)) {
 			performInjectionOnAnnotatedEntity(classNode);
 		}
 	}
 
 	@Override
 	public void performInjectionOnAnnotatedEntity(ClassNode classNode) {
 		injectLocalizableProperties(classNode);
 	}
 
 	private void injectLocalizableProperties(ClassNode classNode) {
 		
 		String defLocale = GrailsConfig.get("config.grails.glangs.locale");
 		if(null == defLocale) defLocale = "En";
 		String propName = "name"+defLocale;
 		final boolean hasPropery = GrailsASTUtils.hasOrInheritsProperty(classNode, propName);
 
 		if (!hasPropery) {
 			classNode.addProperty(propName, Modifier.PUBLIC, new ClassNode(String.class), null, null, null);
 			classNode.addProperty(propName+"Gb", Modifier.PUBLIC, new ClassNode(String.class), null, null, null);
 		}
 	}
 
 }
