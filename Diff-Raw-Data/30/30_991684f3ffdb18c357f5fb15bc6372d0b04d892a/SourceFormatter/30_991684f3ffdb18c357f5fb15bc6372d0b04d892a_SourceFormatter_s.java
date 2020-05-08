 /*******************************************************************************
 * Copyright (c) IBM Corporation 2008 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jurgen Vinju (jurgenv@cwi.nl) - initial API and implementation
 *******************************************************************************/
 
 package org.eclipse.imp.formatting;
 
 import java.io.IOException;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.box.builders.BoxException;
 import org.eclipse.imp.box.builders.BoxFactory;
 import org.eclipse.imp.editor.UniversalEditor;
 import org.eclipse.imp.formatting.spec.ExtensionPointBinder;
 import org.eclipse.imp.formatting.spec.ParseException;
 import org.eclipse.imp.formatting.spec.Parser;
 import org.eclipse.imp.formatting.spec.Specification;
 import org.eclipse.imp.formatting.spec.Transformer;
 import org.eclipse.imp.language.ILanguageService;
 import org.eclipse.imp.language.Language;
 import org.eclipse.imp.language.LanguageRegistry;
 import org.eclipse.imp.model.ISourceProject;
 import org.eclipse.imp.model.ModelFactory;
 import org.eclipse.imp.model.ModelFactory.ModelException;
 import org.eclipse.imp.parser.IMessageHandler;
 import org.eclipse.imp.parser.IParseController;
 import org.eclipse.imp.services.IASTAdapter;
 import org.eclipse.imp.services.ISourceFormatter;
 import org.eclipse.imp.utils.LogMessageHandler;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.texteditor.AbstractTextEditor;
 
 public class SourceFormatter implements ISourceFormatter, ILanguageService {
 	private IASTAdapter adapter;
 
 	private AbstractTextEditor fActiveEditor;
 
 	private Language fLanguage;
 
 	private Transformer transformer;
 	
 	private IMessageHandler handler= new LogMessageHandler(Activator.getInstance().getLog());
 
 	private Parser parser;
 	
 	public void formatterStarts(String initialIndentation) {
 		initialize();
 	}
 
 	private void initialize() {
 		try {
 			UniversalEditor ue = (UniversalEditor) getActiveEditor();
 			fLanguage = LanguageRegistry.findLanguage(ue.getEditorInput(), ue.getDocumentProvider());
			ExtensionPointBinder b = new ExtensionPointBinder(fLanguage);
			
			adapter = b.getASTAdapter();
			IPath fsp = b.getSpecificationPath();
 			parser = new Parser(fsp, getActiveProject(), handler);
 			Specification spec = parser.parse(fsp);
 			transformer = new Transformer(spec, adapter);
 		} catch (ModelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private AbstractTextEditor getActiveEditor() {
		fActiveEditor = (AbstractTextEditor) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		return fActiveEditor;
 	}
 
 	private ISourceProject getActiveProject() throws ModelException {
		return ModelFactory.open(extractResource(getActiveEditor())
				.getProject());
 	}
 
 	private IResource extractResource(IEditorPart editor) {
 		IEditorInput input = editor.getEditorInput();
 		if (!(input instanceof IFileEditorInput))
 			return null;
 		return ((IFileEditorInput) input).getFile();
 	}
 
	public String format(IParseController ignored, String content,
			boolean isLineStart, String indentation, int[] positions) {
 		Object ast = parser.parseObject(content);
 
 		if (ast != null) {
 			String box = transformer.transformToBox(content, ast);
 			try {
				return BoxFactory.box2text(box);
			} catch (BoxException e) {
 				postError("Internal error: " + e.getMessage());
 				return content;
 			}
 		}
 		else {
 			postError("Code could not be formatted due to parse error(s)");
 			return content;
 		}
 	}
 
 	private void postError(String cause) {
 		MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Unable to format", cause);
 	}
 
 	public void formatterStops() {
 	}
 }
