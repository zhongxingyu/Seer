 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.pror.editor.actions;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.rmf.reqif10.Specification;
import org.eclipse.rmf.reqif10.pror.editor.presentation.ReqifSpecificationEditorInput;
 import org.eclipse.rmf.reqif10.pror.editor.util.ProrEditorUtil;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
 
 /**
  * @author jastram
  * 
  */
 public class SpecificationWebPrintAction extends Action {
 
 	private EditingDomain editingDomain;
 	private AdapterFactory adapterFactory;
 
 	/**
 	 */
	public SpecificationWebPrintAction(
 			EditingDomain editingDomain, AdapterFactory adapterFactory) {
 		this.editingDomain = editingDomain;
 		this.adapterFactory = adapterFactory;
 	}
 
 	@Override
 	public void run() {
		// Find the active editor
		IEditorInput input = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
		if (! (input instanceof ReqifSpecificationEditorInput)) {
			return;
		}
		
		Specification spec = ((ReqifSpecificationEditorInput)input).getSpec();
 		
 		try {
 			File folder = File.createTempFile("pror-", "");
 			folder.delete();
 			folder = new File(folder.getAbsolutePath() + "/");
 			folder.mkdir();
 
 			String createHtmlContent = ProrEditorUtil.createHtmlContent(spec,
 					editingDomain, adapterFactory);
 
 			File htmlFile = new File(folder, "pror.html");
 			FileWriter writer = new FileWriter(htmlFile);
 			writer.write(createHtmlContent);
 			writer.close();
 			htmlFile.deleteOnExit();
 			Program.launch(htmlFile.toURI().toString());
 			System.out.println("Launched: " + htmlFile.toURI());
 			MessageDialog.openInformation(
 					Display.getCurrent().getActiveShell(), "Output produced",
 					"Switch to your web browser to see the output.");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
