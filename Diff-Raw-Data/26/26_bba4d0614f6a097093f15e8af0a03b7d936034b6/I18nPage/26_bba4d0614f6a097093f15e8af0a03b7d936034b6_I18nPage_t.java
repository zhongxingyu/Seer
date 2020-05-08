 /*
  * Copyright (C) 2003, 2004  Pascal Essiembre, Essiembre Consultant Inc.
  * 
  * This file is part of Essiembre ResourceBundle Editor.
  * 
  * Essiembre ResourceBundle Editor is free software; you can redistribute it 
  * and/or modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * Essiembre ResourceBundle Editor is distributed in the hope that it will be 
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with Essiembre ResourceBundle Editor; if not, write to the 
  * Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
  * Boston, MA  02111-1307  USA
  */
 package com.essiembre.eclipse.rbe.ui.editor.i18n;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Locale;
 
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 
 import com.essiembre.eclipse.rbe.ui.editor.i18n.tree.KeyTreeComposite;
 import com.essiembre.eclipse.rbe.ui.editor.resources.ResourceManager;
 
 /**
  * Internationalization page where one can edit all resource bundle entries 
  * at once for all supported locales.
  * @author Pascal Essiembre (essiembre@users.sourceforge.net)
  * @version $Author$ $Revision$ $Date$
  */
 public class I18nPage extends ScrolledComposite {
 
     /** Minimum height of text fields. */
     private static final int TEXT_MIN_HEIGHT = 90;
 
     private final ResourceManager resourceMediator;
     private final KeyTreeComposite keysComposite;
     private final Collection entryComposites = new ArrayList(); 
     
     /*default*/ BundleEntryComposite activeEntry;
     
     /**
      * Constructor.
      * @param parent parent component.
      * @param style  style to apply to this component
      * @param resourceMediator resource manager
      */
     public I18nPage(
             Composite parent, int style, 
             final ResourceManager resourceMediator) {
         super(parent, style);
         this.resourceMediator = resourceMediator; 
 
         // Create screen        
         SashForm sashForm = new SashForm(this, SWT.NONE);
 
         setContent(sashForm);
         setExpandHorizontal(true);
         setExpandVertical(true);
         setMinWidth(400);
 
         keysComposite = new KeyTreeComposite(
                sashForm, 
                resourceMediator.getKeyTree());
         keysComposite.getTreeViewer().addSelectionChangedListener(
                 new ISelectionChangedListener() {
                     public void selectionChanged(SelectionChangedEvent event) {
                         refreshTextBoxes();
                     }
         });
         
         createSashRightSide(sashForm);
                 
         sashForm.setWeights(new int[]{25, 75});
     }
 
 
     /**
      * Creates right side of main sash form.
      * @param sashForm parent sash form
      */
     private void createSashRightSide(SashForm sashForm) {
        ScrolledComposite scrolledComposite =
                new ScrolledComposite(sashForm, SWT.V_SCROLL | SWT.H_SCROLL);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setAlwaysShowScrollBars(true);
        scrolledComposite.setSize(SWT.DEFAULT, 100);
        Composite rightComposite = new Composite(scrolledComposite, SWT.BORDER);
        scrolledComposite.setContent(rightComposite);
        scrolledComposite.setMinSize(rightComposite.computeSize(
                SWT.DEFAULT,
                resourceMediator.getLocales().size() * TEXT_MIN_HEIGHT));
         rightComposite.setLayout(new GridLayout(1, false));
         for (Iterator iter = resourceMediator.getLocales().iterator();
                 iter.hasNext();) {
             Locale locale = (Locale) iter.next();
             BundleEntryComposite entryComposite = new BundleEntryComposite(
                     rightComposite, resourceMediator, locale);
             entryComposite.addFocusListener(new FocusListener() {
                 public void focusGained(FocusEvent event) {
                     activeEntry = (BundleEntryComposite) event.widget;
                 }
                 public void focusLost(FocusEvent event) {
                     activeEntry = null;
                 }
             });
             entryComposites.add(entryComposite);
         }
     }
     
 
 	/**
 	 * Refreshes the editor associated with the active text box (if any)
      * if it has changed.
 	 */
 	public void refreshEditorOnChanges(){
         if (activeEntry != null) {
             activeEntry.updateBundleOnChanges();
         }
 	}
 	    
     /**
      * Refreshes all value-holding text boxes in this page.
      */
     public void refreshTextBoxes() {
         String key = keysComposite.getSelectedKey();
         for (Iterator iter = entryComposites.iterator(); iter.hasNext();) {
             BundleEntryComposite entryComposite = 
                     (BundleEntryComposite) iter.next();
             entryComposite.refresh(key);
         }
     }
     
     
     /**
      * @see org.eclipse.swt.widgets.Widget#dispose()
      */
     public void dispose() {
         keysComposite.dispose();
         for (Iterator iter = entryComposites.iterator(); iter.hasNext();) {
             ((BundleEntryComposite) iter.next()).dispose();
         }
         super.dispose();
     }
 }
