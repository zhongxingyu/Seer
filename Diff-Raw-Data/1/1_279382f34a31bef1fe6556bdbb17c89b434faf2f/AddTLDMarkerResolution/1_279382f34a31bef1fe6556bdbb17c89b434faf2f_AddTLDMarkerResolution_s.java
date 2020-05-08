 /******************************************************************************* 
  * Copyright (c) 2010 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.jst.web.ui.action;
 
 import java.util.Properties;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.IMarkerResolution2;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.DocumentProviderRegistry;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.jboss.tools.jst.jsp.jspeditor.dnd.PaletteTaglibInserter;
 import org.jboss.tools.jst.web.ui.Messages;
 import org.jboss.tools.jst.web.ui.WebUiPlugin;
 
 /**
  * The Marker Resolution that adds tag lib declaration to jsp or xhtml file
  * 
  * @author Daniel Azarov
  *
  */
 public class AddTLDMarkerResolution implements IMarkerResolution2{
 	private IFile file;
 	private Properties properties;
 	private String resolutionName;
 	
 	public AddTLDMarkerResolution(IFile file, String name, Properties properties){
 		this.file = file;
 		this.properties = properties;
 		this.resolutionName = name;
 	}
 
 	public String getLabel() {
 		return NLS.bind(Messages.AddTLDMarkerResolution_Name, resolutionName);
 	}
 
 	public void run(IMarker marker) {
 		FileEditorInput input = new FileEditorInput(file);
 		IDocumentProvider provider = DocumentProviderRegistry.getDefault().getDocumentProvider(input);
 		try {
 			provider.connect(input);
 		
 			IDocument document = provider.getDocument(input);
 			
 			PaletteTaglibInserter inserter = new PaletteTaglibInserter();
 			inserter.inserTaglib(document, properties);
 			
 			provider.aboutToChange(input);
 			provider.saveDocument(new NullProgressMonitor(), input, document, true);
 			provider.disconnect(input);
 		}catch(CoreException ex){
 			WebUiPlugin.getPluginLog().logError(ex);
 		}
 	}
 
 	public String getDescription() {
 		return getLabel();
 	}
 
 	public Image getImage() {
 		return null;//ImageDescriptor.createFromFile(AddTLDMarkerResolution.class,	"images/xstudio/editors/taglibs_file.gif").createImage(); //$NON-NLS-1$
 	}
 }
