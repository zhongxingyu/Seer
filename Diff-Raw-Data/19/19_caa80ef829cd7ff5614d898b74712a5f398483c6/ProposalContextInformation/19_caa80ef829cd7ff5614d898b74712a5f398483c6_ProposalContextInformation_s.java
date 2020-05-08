 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.text.completion;
 
 
 
 import org.eclipse.dltk.core.CompletionProposal;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.jface.text.contentassist.IContextInformationExtension;
 import org.eclipse.swt.graphics.Image;
 
 
 
 /**
  * Implementation of the <code>IContextInformation</code> interface.
  */
 public class ProposalContextInformation implements IContextInformation, IContextInformationExtension {
 
 	private final String fContextDisplayString;
 	private final String fInformationDisplayString;
 	private final Image fImage;
 	private int fPosition;
 
 	/**
 	 * Creates a new context information.
 	 */
 	public ProposalContextInformation(CompletionProposal proposal) {
 		// don't cache the core proposal because the ContentAssistant might
 		// hang on to the context info.
 		//TODO REVIEW THIS
 		String res = createParametersList(proposal);		
 		fInformationDisplayString= res;//labelProvider.createParameterList(proposal);
 		//ImageDescriptor descriptor= labelProvider.createImageDescriptor(proposal);
 		//if (descriptor != null)
 		//	fImage= JavaPlugin.getImageDescriptorRegistry().get(descriptor);
 		//else
 			fImage= null;
 		if (proposal.getCompletion().length() == 0)
			fPosition= proposal.getCompletionLocation() + 1;
 		else
 			fPosition= -1;
 		fContextDisplayString= res;//labelProvider.createLabel(proposal);
 	}
 
 	private String createParametersList(CompletionProposal proposal) {
 		StringBuffer bf=new StringBuffer();
 		String[] pNames = proposal.findParameterNames(null);
 		for (int a=0;a<pNames.length;a++){
 			bf.append(pNames[a]);
 			if (a!=pNames.length-1)bf.append(',');
 		}
 		String res=bf.toString();
 		return res;
 	}
 
 	/*
 	 * @see IContextInformation#equals
 	 */
 	public boolean equals(Object object) {
 		if (object instanceof IContextInformation) {
 			IContextInformation contextInformation= (IContextInformation) object;
 			boolean equals= getInformationDisplayString().equalsIgnoreCase(contextInformation.getInformationDisplayString());
 			if (getContextDisplayString() != null)
 				equals= equals && getContextDisplayString().equalsIgnoreCase(contextInformation.getContextDisplayString());
 			return equals;
 		}
 		return false;
 	}
 
 	/*
 	 * @see IContextInformation#getInformationDisplayString()
 	 */
 	public String getInformationDisplayString() {
 		return fInformationDisplayString;
 	}
 
 	/*
 	 * @see IContextInformation#getImage()
 	 */
 	public Image getImage() {
 		return fImage;
 	}
 
 	/*
 	 * @see IContextInformation#getContextDisplayString()
 	 */
 	public String getContextDisplayString() {
 		return fContextDisplayString;
 	}
 
 	/*
 	 * @see IContextInformationExtension#getContextInformationPosition()
 	 */
 	public int getContextInformationPosition() {
 		return fPosition;
 	}
 	
 	/**
 	 * Sets the context information position.
 	 * 
 	 * @param position the new position, or -1 for unknown.
 	 *
 	 */
 	public void setContextInformationPosition(int position) {
 		fPosition= position;
 	}
 }
