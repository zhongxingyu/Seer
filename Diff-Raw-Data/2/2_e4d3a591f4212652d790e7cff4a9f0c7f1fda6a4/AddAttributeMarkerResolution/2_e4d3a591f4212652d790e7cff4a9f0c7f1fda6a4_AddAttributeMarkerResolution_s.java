 /******************************************************************************* 
  * Copyright (c) 2012 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.jst.web.ui.action;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.DocumentProviderRegistry;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.jboss.tools.common.model.ui.ModelUIImages;
 import org.jboss.tools.common.quickfix.IQuickFix;
 import org.jboss.tools.common.ui.CommonUIPlugin;
 import org.jboss.tools.jst.web.ui.Messages;
 import org.jboss.tools.jst.web.ui.WebUiPlugin;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  * The Marker Resolution that adds missing attribute to the tag in jsp or xhtml file
  * 
  * @author Daniel Azarov
  *
  */
 public class AddAttributeMarkerResolution implements IQuickFix{
 	private IFile file;
 	
 	private int start, end;
 	private String attributeName;
 	private Node node;
 	
 	public AddAttributeMarkerResolution(IFile file, Node node, String attributeName, int start, int end){
 		this.file = file;
 		this.node = node;
 		this.attributeName = attributeName;
 		this.start = start;
 		this.end = end;
 	}
 	
 	@Override
 	public String getLabel() {
 		return NLS.bind(Messages.AddAttributeMarkerResolution_Name, attributeName, node.getNodeName());
 	}
 	
 	@Override
 	public void run(IMarker marker) {
 		FileEditorInput input = new FileEditorInput(file);
 		IDocumentProvider provider = DocumentProviderRegistry.getDefault().getDocumentProvider(input);
 		try {
 			provider.connect(input);
 			
 			boolean dirty = provider.canSaveDocument(input);
 		
 			IDocument document = provider.getDocument(input);
 			
 			apply(document);
 			
 			if(!dirty){
 				provider.aboutToChange(input);
 				provider.saveDocument(new NullProgressMonitor(), input, document, true);
 				provider.changed(input);
 			}
 			
 			provider.disconnect(input);
 		}catch(CoreException ex){
 			WebUiPlugin.getPluginLog().logError(ex);
 		}
 	}
 
 	@Override
 	public String getDescription() {
 		return getLabel();
 	}
 
 	@Override
 	public Image getImage() {
		return CommonUIPlugin.getImageDescriptorRegistry().get(ModelUIImages.getImageDescriptor(ModelUIImages.TAGLIB_FILE));
 	}
 
 	@Override
 	public void apply(IDocument document) {
 		String text = "<"+node.getNodeName()+" ";
 		
 		NamedNodeMap attributes = node.getAttributes();
 		for(int i = 0; i < attributes.getLength(); i++){
 			Node att = attributes.item(i);
 			text += att.getNodeName()+"=\""+att.getNodeValue()+"\" ";
 		}
 		
 		text += attributeName+"=\"\">";
 		try {
 			document.replace(start, end-start, text);
 		} catch (BadLocationException ex) {
 			WebUiPlugin.getPluginLog().logError(ex);
 		}
 	}
 
 	@Override
 	public Point getSelection(IDocument document) {
 		return null;
 	}
 
 	@Override
 	public String getAdditionalProposalInfo() {
 		return getDescription();
 	}
 
 	@Override
 	public String getDisplayString() {
 		return getLabel();
 	}
 
 	@Override
 	public IContextInformation getContextInformation() {
 		return null;
 	}
 
 	@Override
 	public int getRelevance() {
 		return 0;
 	}
 }
