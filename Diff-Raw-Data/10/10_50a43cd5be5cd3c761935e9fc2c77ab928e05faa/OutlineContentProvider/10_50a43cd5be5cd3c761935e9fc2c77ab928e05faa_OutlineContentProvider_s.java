 /*=============================================================================#
  # Copyright (c) 2014 Stephan Wahlbrink (WalWare.de) and others.
  # All rights reserved. This program and the accompanying materials
  # are made available under the terms of the Eclipse Public License v1.0
  # which accompanies this distribution, and is available at
  # http://www.eclipse.org/legal/epl-v10.html
  # 
  # Contributors:
  #     Stephan Wahlbrink - initial API and implementation
  #=============================================================================*/
 
 package de.walware.ecommons.ltk.ui.sourceediting;
 
 import java.util.List;
 
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 
 import de.walware.ecommons.ltk.IModelElement.Filter;
 import de.walware.ecommons.ltk.ISourceStructElement;
 import de.walware.ecommons.ltk.ISourceUnit;
 import de.walware.ecommons.ltk.ISourceUnitModelInfo;
 
 
 public class OutlineContentProvider implements ITreeContentProvider {
 	
 	
 	public interface IOutlineContent {
 		
 		ISourceUnitModelInfo getModelInfo(Object inputElement);
 		
 		Filter getContentFilter();
 		
 	}
 	
 	
 	private final IOutlineContent content;
 	
 	private long currentModelStamp;
 	
 	
 	public OutlineContentProvider(final IOutlineContent content) {
 		this.content= content;
 	}
 	
 	
 	protected final IOutlineContent getContent() {
 		return this.content;
 	}
 	
 	public long getStamp(final Object inputElement) {
 		final ISourceUnitModelInfo modelInfo= getContent().getModelInfo(inputElement);
 		if (modelInfo != null) {
 			return modelInfo.getStamp();
 		}
 		return ISourceUnit.UNKNOWN_MODIFICATION_STAMP;
 	}
 	
 	@Override
 	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
 	}
 	
 	@Override
 	public Object[] getElements(final Object inputElement) {
 		final ISourceUnitModelInfo modelInfo= getContent().getModelInfo(inputElement);
 		if (modelInfo != null) {
 			this.currentModelStamp= modelInfo.getStamp();
 			final List<? extends ISourceStructElement> children= modelInfo.getSourceElement().getSourceChildren(getContent().getContentFilter());
 			return children.toArray(new ISourceStructElement[children.size()]);
 		}
 		return new ISourceStructElement[0];
 	}
 	
 	@Override
 	public void dispose() {
 	}
 	
 	@Override
 	public Object getParent(final Object element) {
 		final ISourceStructElement o= (ISourceStructElement) element;
 		return o.getSourceParent();
 	}
 	
 	@Override
 	public boolean hasChildren(final Object element) {
 		final ISourceStructElement o= (ISourceStructElement) element;
 		return o.hasSourceChildren(getContent().getContentFilter());
 	}
 	
 	@Override
 	public Object[] getChildren(final Object parentElement) {
		final ISourceStructElement o= (ISourceStructElement) parentElement;
		final List<? extends ISourceStructElement> children= o.getSourceChildren(getContent().getContentFilter());
		return children.toArray(new ISourceStructElement[children.size()]);
 	}
 	
 }
