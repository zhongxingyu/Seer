 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.parser.mixin;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.parser.ISourceParserConstants;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceModuleInfoCache;
 import org.eclipse.dltk.core.SourceParserUtil;
 import org.eclipse.dltk.core.ISourceModuleInfoCache.ISourceModuleInfo;
 import org.eclipse.dltk.core.mixin.IMixinParser;
 import org.eclipse.dltk.core.mixin.IMixinRequestor;
 import org.eclipse.dltk.internal.core.BuiltinSourceModule;
 import org.eclipse.dltk.internal.core.ModelManager;
 import org.eclipse.dltk.ruby.core.RubyNature;
 
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

 public class RubyMixin implements IMixinParser {
 
 	public final static String INSTANCE_SUFFIX = "%"; // suffix for instance //$NON-NLS-1$
 	// classes
 
 	public final static String VIRTUAL_SUFFIX = "%v"; // suffix for virtual //$NON-NLS-1$
 	// classes
 
 	private IMixinRequestor requestor;
 
     private static char[] loadContent(ISourceModule module) throws CoreException {
       char[] content = null;
 
       IPath absolutePath;
       if (module.getResource() != null) {
         absolutePath = module.getResource().getLocation();
       }
       else {
         absolutePath = module.getPath();
       }
       if (absolutePath.toFile().exists()) {
         InputStreamReader reader = null;
         try {
          StringBuilder builder = new StringBuilder();
           reader = new InputStreamReader(new FileInputStream(absolutePath.toFile()));
 
           char[] cbuf = new char[12 * 1024];
           while (reader.ready() == true) {
             int read = reader.read(cbuf);
             builder.append(cbuf, 0, read);
           }
 
           content = new char[builder.length()];
           builder.getChars(0, content.length, content, 0);
         }
         catch (IOException ixcn) {
           ixcn.printStackTrace();
         }
         finally {
           if (reader != null) {
             try {
               reader.close();
             }
             catch (IOException ixcn) {
               ixcn.printStackTrace();
             }
           }
         }
       }
       else if (module instanceof BuiltinSourceModule) {
         content = ((BuiltinSourceModule)module).getSourceAsCharArray();
       }
       else {
         content = module.getSourceAsCharArray();
       }
 
       return content;
     }
 
 	public void parserSourceModule(boolean signature, ISourceModule module) {
 		// if( module != null ) {
 		// RubyMixinModel.getRawInstance().remove(module);
 		// }
 		// long start = System.currentTimeMillis();
 
 	    try {
 	      //ssanders - Avoid using Buffer in order to improve build performance
           ISourceModuleInfoCache sourceModuleInfoCache = ModelManager.getModelManager().getSourceModuleInfoCache();
           ISourceModuleInfo sourceModuleInfo = sourceModuleInfoCache.get(module);
           ModuleDeclaration moduleDeclaration = SourceParserUtil.getModuleFromCache(sourceModuleInfo, ISourceParserConstants.DEFAULT);
           if (moduleDeclaration == null) {
             // ssanders: PERFORMANCE - Avoid using a buffer
             char[] content = loadContent(module);
 		    moduleDeclaration = SourceParserUtil
 				.getModuleDeclaration(module.getPath().toOSString().toCharArray(),
 				                      content, RubyNature.NATURE_ID, null, sourceModuleInfo);
           }
 
 		// long end = System.currentTimeMillis();
 		// System.out.println("RubyMixin: parsing took " + (end - start));
 		// start = end;
 		  RubyMixinBuildVisitor visitor = new RubyMixinBuildVisitor(
 				moduleDeclaration, module, signature, requestor);
 			moduleDeclaration.traverse(visitor);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		// end = System.currentTimeMillis();
 		// System.out.println("RubyMixin: traversing took " + (end - start) + "
 		// signature=" + signature);
 	}
 
 	public void setRequirestor(IMixinRequestor requestor) {
 		this.requestor = requestor;
 	}
 }
