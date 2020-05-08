 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.eclipse.internal.codeassist.select;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.dltk.mod.ast.ASTNode;
 import org.eclipse.dltk.mod.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.mod.ast.expressions.Expression;
 import org.eclipse.dltk.mod.ast.parser.ISourceParserConstants;
 import org.eclipse.dltk.mod.codeassist.IAssistParser;
 import org.eclipse.dltk.mod.codeassist.ScriptSelectionEngine;
 import org.eclipse.dltk.mod.compiler.env.ISourceModule;
 import org.eclipse.dltk.mod.core.IModelElement;
 import org.eclipse.dltk.mod.core.ISourceModuleInfoCache.ISourceModuleInfo;
 import org.eclipse.dltk.mod.core.ModelException;
 import org.eclipse.dltk.mod.core.SourceParserUtil;
 import org.eclipse.dltk.mod.internal.core.ModelManager;
 import org.eclipse.dltk.mod.internal.core.VjoSourceModule;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.JstSource;
 import org.eclipse.vjet.dsf.jst.declaration.JstConstructor;
 import org.eclipse.vjet.dsf.jst.declaration.JstMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstProperty;
 import org.eclipse.vjet.dsf.jst.declaration.JstVars;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.dsf.jstojava.translator.JstUtil;
 import org.eclipse.vjet.eclipse.codeassist.CodeassistUtils;
 import org.eclipse.vjet.eclipse.core.IVjoSourceModule;
 import org.eclipse.vjet.eclipse.internal.codeassist.select.translator.IJstNodeTranslator;
 import org.eclipse.vjet.eclipse.internal.codeassist.select.translator.JstToDLTKNodeTranslator;
 import org.eclipse.vjet.vjo.tool.typespace.TypeSpaceMgr;
 
 /**
  * Engine for processing the selection.
  * 
  * 
  * 
  */
 public class VjoSelectionEngine extends ScriptSelectionEngine {
 
 	private SelectionParser m_parser;
 
 	public VjoSelectionEngine() {
 		super();
 		this.m_parser = new SelectionParser();
 	}
 
 	/**
 	 * Convert the given jst node to corresponding DLTK model elements. Or empty
 	 * array if no corresponding element.
 	 * 
 	 * @param node
 	 * @return
 	 */
 	public IModelElement[] convert(IVjoSourceModule module, IJstNode jstNode) {
 		IModelElement[] elems = JstNodeDLTKElementResolver.convert(module, jstNode);
 //		System.out.println(elems);
 		
 		return elems;
 		// return element != null ? new IModelElement[] { element }
 		// : new IModelElement[0];
 	}
 
 	public IJstNode convertSelection2JstNode(ISourceModule module,
 			int startOffset, int endOffset) {
 		IVjoSourceModule sourceModule = (IVjoSourceModule) module;
 		IJstType jstType;
 		jstType = sourceModule.getJstType();
 		if (jstType == null) {
 			IResource resource = sourceModule.getResource();
 			if (resource == null || !resource.exists()
 					|| !(resource instanceof IFile)) {
 				jstType = CodeassistUtils.findNativeJstType(sourceModule
 						.getElementName());
 			} else {
 				String typeName = CodeassistUtils
 						.getClassName((IFile) sourceModule.getResource());
 				jstType = TypeSpaceMgr.findType(sourceModule.getScriptProject()
 						.getElementName(), typeName);
 			}
 
 		}
 
 		// repair offset
 		startOffset = this.repairOffset(sourceModule, startOffset);
 		endOffset = startOffset;
 
 		IJstNode selection = JstUtil.getLeafNode(jstType, startOffset,
 				endOffset, true);
 		IJstNode jstBinding = JstNodeDLTKElementResolver
 				.lookupBinding(selection);
 		if(jstBinding==null){
 			return selection;
 		}
 		return jstBinding;
 	}
 
 	@Override
 	public IAssistParser getParser() {
 		return this.m_parser;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.dltk.mod.codeassist.ISelectionEngine#select(org.eclipse.dltk
 	 * .mod.compiler.env.ISourceModule, int, int)
 	 */
 	public IModelElement[] select(ISourceModule module, int startOffset,
 			int endOffset) {
 		IJstNode jstNode = convertSelection2JstNode(module, startOffset,
 				endOffset);
 		
 		
 		if(jstNode==null){
 			return new IModelElement[0];
 		}
 
 		// get identifier if JstVars
 		/// get name source 
 		JstSource nameSource= null;
 		if(jstNode instanceof JstProperty){
 			nameSource =( (JstProperty)jstNode).getName().getSource();
 		}else if(jstNode instanceof JstVars){
 			JstVars vars = (JstVars)jstNode;
 			JstIdentifier localVar = (JstIdentifier) vars.getAssignments().get(0)
 					.getLHS();
 			nameSource = localVar.getSource();
 		}else if(jstNode instanceof JstConstructor){
 			nameSource = ((JstConstructor)jstNode).getOwnerType().getConstructor().getName().getSource();
 			if(nameSource==null){
 				nameSource = ((JstConstructor)jstNode).getOwnerType().getSource();
 			}
 		}else if(jstNode instanceof IJstMethod){
 			nameSource = ((IJstMethod)jstNode).getName().getSource();
 		}
 		
 		if(nameSource!=null){
 			endOffset = nameSource.getEndOffSet();
 			startOffset = nameSource.getStartOffSet();
 		}else if (jstNode.getSource() != null) {
 			startOffset = jstNode.getSource().getStartOffSet();
 			endOffset = jstNode.getSource().getEndOffSet();
 		} else {
 			// should be error or problem with parser not adding jstsource
 			// correctly
 		}
 
 		// issue with nothing being found... offsets don't match?
 		IJstNodeTranslator nodeTranslator = JstToDLTKNodeTranslator
				.getNodeTranslator(jstNode.getRootType());
		IModelElement[] dltktypes = nodeTranslator.convert(jstNode.getRootType());
 		
 		if(dltktypes!=null && dltktypes.length>0){
 			IModelElement elem = visitAndFindModelElement(dltktypes[0], startOffset,
 					endOffset);
 			if(elem!=null){
 				return new IModelElement[]{elem};
 			}
 
 		}
 		
 		
 		// TODO remove this code since since visitor pattern should find most no
 		// used to create IModelElements where there is no DLTK model present
 		IVjoSourceModule vjoModule = null;
 		if (module instanceof IVjoSourceModule) {
 			vjoModule = (IVjoSourceModule) module;
 		}
 		return convert(vjoModule, jstNode);
 	}
 
 	private IModelElement visitAndFindModelElement(final IModelElement module, final int curStartOffset, final int curEndOffset) {
 
 		try {
 			ModelLeafElementVisitor visitor = new ModelLeafElementVisitor(curEndOffset, curStartOffset);
 			module.accept(visitor);
 			return visitor.elem;
 		} catch (ModelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return null;
 		
 		
 	}
 
 	// if select
 	private int repairOffset(IVjoSourceModule module, int offset) {
 		if (offset == 0)
 			return offset;
 
 		try {
 			char[] sourceChar = module.getSourceAsCharArray();
 			;
 			if (sourceChar == null || offset >= sourceChar.length) {
 				return offset;
 			}
 			char selectedChar = sourceChar[offset];
 			if (Character.isWhitespace(selectedChar))
 				--offset;
 		} catch (ModelException e) {
 			e.printStackTrace();
 		}
 
 		return offset;
 	}
 }
