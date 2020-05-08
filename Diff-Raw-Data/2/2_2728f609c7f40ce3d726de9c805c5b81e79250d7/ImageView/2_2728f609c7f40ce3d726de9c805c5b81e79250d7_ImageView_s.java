 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.views.imageview;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.utils.JobUtils;
 import org.eclipse.jubula.client.ui.views.IJBPart;
 import org.eclipse.jubula.client.ui.views.NonSortedPropertySheetPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.ISelectionService;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 
 
 /**
  * @author BREDEX GmbH
  * @created Apr 14, 2010
  */
 public class ImageView extends ViewPart implements IJBPart, ISelectionProvider {
     /**
      * <code>image</code>
      */
     private Label m_imgWidget;
     
     /**
      * the image
      */
     private Image m_img;
     
     /**
      * <code>m_oldSelection</code>
      */
     private ISelection m_currSelection = null;
     
     /**
      * the scrolled composite
      */
     private ScrolledComposite m_scrollComposite;
     
     /**
      * the child
      */
     private Composite m_child;
     
     /**
      * The selectionListener listens for changes in the workbench's selection
      * service.
      */
     private ISelectionListener m_selectionListener = new ISelectionListener() {
         public void selectionChanged(IWorkbenchPart part, 
                 ISelection selection) {
             handleSelection(selection);
         }
     };
 
     /**
      * @param selection
      *            the selection
      */
     private void handleSelection(ISelection selection) {
         ImageProvider provider = null;
         if (selection != null && selection instanceof IStructuredSelection) {
             IStructuredSelection ss = (IStructuredSelection)selection;
             if (ss.size() > 0) {
                 Object object = ss.getFirstElement();
 
                 if (m_currSelection != null
                         && ObjectUtils.equals(object,
                                 ((IStructuredSelection)m_currSelection)
                                         .getFirstElement())) {
                     return;
                 }
                 m_currSelection = ss;
                 // First, if the object is adaptable, ask it to get an adapter.
                 if (object instanceof IAdaptable) {
                     provider = (ImageProvider)((IAdaptable)object)
                             .getAdapter(ImageProvider.class);
                 }
 
                 // If we haven't found an adapter yet, try asking the
                 // AdapterManager.
                 if (provider == null) {
                     provider = (ImageProvider)Platform.getAdapterManager()
                             .getAdapter(object, ImageProvider.class);
                 }
             }
         }
         
         if (provider == null) {
             clearImage();
         } else {
             handleSelection(provider);
         }
     }
 
     /**
      * @param provider
      *            the provider
      */
     private void handleSelection(final ImageProvider provider) {
         final String jobName = Messages.UIJobLoadingImage;
         Job job = new Job(jobName) {
             public IStatus run(IProgressMonitor monitor) {
                 monitor.beginTask(jobName, IProgressMonitor.UNKNOWN);
                 setImage(provider);
                 monitor.done();
                 return Status.OK_STATUS;
             }
         };
         JobUtils.executeJob(job, null);
     }
 
     /**
      * {@inheritDoc}
      */
     public void createPartControl(Composite parent) {
         parent.setLayout(new FillLayout());
         m_scrollComposite = new ScrolledComposite(parent,
                 SWT.V_SCROLL | SWT.H_SCROLL);
 
         m_child = new Composite(m_scrollComposite, SWT.NONE);
         m_child.setLayout(new FillLayout());
         
         m_imgWidget = new Label(m_child, SWT.NONE);
         m_scrollComposite.setExpandHorizontal(true);
         m_scrollComposite.setExpandVertical(true);
         m_scrollComposite.setMinSize(m_child.computeSize(
                 SWT.DEFAULT, SWT.DEFAULT));
         m_scrollComposite.setContent(m_child);
         
         getSelectionService().addSelectionListener(m_selectionListener);
         handleSelection(getSelectionService().getSelection());
         getSite().setSelectionProvider(this);
     }
 
     /**
      * @param provider the provider
      */
     protected void setImage(final ImageProvider provider) {
         m_scrollComposite.getDisplay().syncExec(new Runnable() {
 
             public void run() {
                 clearImage();
                 m_img = provider.getImage(m_scrollComposite.getDisplay());
                 m_imgWidget.setImage(m_img);
                 if (m_img != null) {
                     m_imgWidget.setSize(
                             m_img.getBounds().width,
                             m_img.getBounds().height);
                 }
                 m_scrollComposite.setMinSize(m_child.computeSize(SWT.DEFAULT,
                         SWT.DEFAULT));
             }
         });
     }
 
     /**
      * make image invisible and dispose it
      */
     protected void clearImage() {
         Image oldImage = m_imgWidget.getImage();
         m_imgWidget.setImage(null);
         if (oldImage != null) {
             oldImage.dispose();
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void dispose() {
         getSelectionService().removeSelectionListener(m_selectionListener);
        if (!m_img.isDisposed()) {
             m_img.dispose();
         }
         super.dispose();
     }
 
     /**
      * {@inheritDoc}
      */
     public void setFocus() {
         m_imgWidget.setFocus();
     }
 
     /**
      * @return the selection service
      */
     private ISelectionService getSelectionService() {
         return getSite().getWorkbenchWindow().getSelectionService();
     }
     
     /**
      * {@inheritDoc}
      */
     public void addSelectionChangedListener(
         ISelectionChangedListener listener) {
         // empty
     }
 
     /**
      * {@inheritDoc}
      */
     public ISelection getSelection() {
         return m_currSelection;
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeSelectionChangedListener(
             ISelectionChangedListener listener) {
         // empty        
     }
 
     /**
      * {@inheritDoc}
      */
     public void setSelection(ISelection selection) {
         // empty
     }
     
     /** {@inheritDoc} */
     public Object getAdapter(Class key) {
         if (key.equals(IPropertySheetPage.class)) {
             return new NonSortedPropertySheetPage();
         }
         return super.getAdapter(key);
     }
 }
