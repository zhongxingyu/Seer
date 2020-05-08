 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 /**
  * 
  */
 package org.eclipse.dltk.internal.javascript.typeinference;
 
 import org.eclipse.dltk.core.IOpenable;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.core.SourceField;
 import org.eclipse.dltk.internal.core.SourceRange;
 
 public class FakeField extends SourceField implements IProposalHolder {
 
 	private int offset;
 	private int length;
 	private String snippet;
 	private String proposalInfo;
 
 	public FakeField(ModelElement parent, String name, int offset, int length) {
 		super(parent, name);
 		this.offset = offset;
 		this.length = length;
 	}
 
 	public ISourceRange getNameRange() throws ModelException {
 		if (offset == 0 && length == 0) {
 			return super.getNameRange();
 		}
 		return new SourceRange(offset, length);
 	}
 
 	public ISourceRange getSourceRange() throws ModelException {
 		if (offset == 0 && length == 0) {
 			return super.getSourceRange();
 		}
 		return new SourceRange(offset, length);
 	}
 
 	public String getSnippet() {
 		return snippet;
 	}
 
 	public void setSnippet(String snippet) {
 		this.snippet = snippet;
 	}
 
 	/**
 	 * @param proposalInfo
 	 */
 	public void setProposalInfo(String proposalInfo) {
 		this.proposalInfo = proposalInfo;
 
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.typeinference.IProposalHolder#getProposalInfo()
 	 */
 	public String getProposalInfo() {
 		return proposalInfo;
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.core.SourceRefElement#getOpenableParent()
 	 */
 	public IOpenable getOpenableParent() {
 
 		IOpenable openableParent = super.getOpenableParent();
 		try {
 			// test if a buffer can be made, if not then just return null.
 			// Is there a better way? (if it is not a script file)
 			openableParent.getBuffer();
 		} catch (Exception e) {
 			return null;
 		}
 		return openableParent;
 	}
 }
