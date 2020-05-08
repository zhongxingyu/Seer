 /*******************************************************************************
  * Copyright (c) 2011 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.text.ext.hyperlink;
 
 import java.text.MessageFormat;
 
 import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
 import org.jboss.tools.common.el.core.ELReference;
 import org.jboss.tools.common.el.core.resolver.ELSegment;
 import org.jboss.tools.common.el.core.resolver.IOpenableReference;
 import org.jboss.tools.common.el.core.resolver.JavaMemberELSegment;
 import org.jboss.tools.common.el.core.resolver.MessagePropertyELSegment;
import org.jboss.tools.common.text.ITextSourceReference;
 import org.jboss.tools.common.text.ext.hyperlink.AbstractHyperlink;
 import org.jboss.tools.common.text.ext.hyperlink.xpl.Messages;
import org.jboss.tools.common.text.ext.util.StructuredSelectionHelper;
 import org.jboss.tools.jst.text.ext.JSTExtensionsPlugin;
 
 public class ELHyperlink extends AbstractHyperlink{
 	private ELReference reference;
 	private ELSegment segment;
 	
 	public ELHyperlink(IDocument document, ELReference reference, ELSegment segment) {
 		this.reference = reference;
 		this.segment = segment;
 		setDocument(document);
 	}
 
 	@Override
 	protected IRegion doGetHyperlinkRegion(int offset) {
 		
 		return new IRegion(){
 			public int getLength() {
 				return segment.getSourceReference().getLength();
 			}
 
 			public int getOffset() {
 				return reference.getStartPosition()+segment.getSourceReference().getStartPosition();
 			}};
 	}
 
 	@Override
 	protected void doHyperlink(IRegion region) {
 		IOpenableReference[] openables = segment.getOpenable();
 		
 		if(openables.length > 0) {
 			if(!openables[0].open()) {
 				openFileFailed();
 			}
 			//If openables.length > 1 - show menu.
 			return;
 		}
 		
		// Open by ITextSourceReference
		ITextSourceReference ref = segment.getSourceReference();
		if (ref != null) {
			IResource r = ref.getResource();
			IFile file = r instanceof IFile ? (IFile)r : null;
			if (file == null || !file.exists() || !file.isAccessible()) {
				openFileFailed();
				return;
			}
			if (openFileInEditor(file) != null && ref.getStartPosition() >=0 && ref.getLength() > 0) {
				StructuredSelectionHelper.setSelectionAndRevealInActiveEditor(new Region(ref.getStartPosition(), ref.getLength()));
				return;
			}
		}
 	}
 	
 	private String trimQuotes(String value) {
 		if(value == null)
 			return null;
 
 		if(value.startsWith("'") || value.startsWith("\"")) {  //$NON-NLS-1$ //$NON-NLS-2$
 			value = value.substring(1);
 		} 
 		
 		if(value.endsWith("'") || value.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
 			value = value.substring(0, value.length() - 1);
 		}
 		return value;
 	}
 	
 //	private String getRequestMethod(Properties prop) {
 //		return prop != null && prop.getProperty(WebPromptingProvider.KEY) == null ? 
 //				WebPromptingProvider.JSF_OPEN_BUNDLE : WebPromptingProvider.JSF_OPEN_KEY;
 //	}
 
 	@Override
 	public String getHyperlinkText() {
 		IOpenableReference[] openables = segment.getOpenable();
 		if(openables.length > 0) {
 			return openables[0].getLabel();
 		}
 		if(segment instanceof JavaMemberELSegment){
 			return "Should not get here."; //$NON-NLS-1$
 		}else if(segment instanceof MessagePropertyELSegment){
 			//TODO move to getLabel() in openable in MessagePropertyELSegmentImpl
 			String baseName = ((MessagePropertyELSegment)segment).getBaseName();
 			String propertyName = ((MessagePropertyELSegment)segment).isBundle() ? null : trimQuotes(((MessagePropertyELSegment)segment).getToken().getText());
 			if (propertyName == null)
 				return  MessageFormat.format(Messages.Open, baseName);
 			
 			return MessageFormat.format(Messages.OpenBundleProperty, propertyName, baseName);
 		}
 		
 		return ""; //$NON-NLS-1$
 	}
 
 	@Override
 	public IFile getReadyToOpenFile() {
 		IFile file = null;
 		if(segment instanceof JavaMemberELSegment){
 			
 			try {
 				file = (IFile)((JavaMemberELSegment) segment).getJavaElement().getUnderlyingResource();
 			} catch (JavaModelException e) {
 				JSTExtensionsPlugin.getDefault().logError(e);
 			}
 		}else if(segment instanceof MessagePropertyELSegment){
 			file = (IFile)((MessagePropertyELSegment)segment).getMessageBundleResource();
 		}
 		return file;
 	}
 
 }
