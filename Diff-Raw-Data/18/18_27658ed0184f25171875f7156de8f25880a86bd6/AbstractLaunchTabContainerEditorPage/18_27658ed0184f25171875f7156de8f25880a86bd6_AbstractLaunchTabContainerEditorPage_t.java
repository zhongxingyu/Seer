 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.launch.ui.editor;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.tcf.te.launch.ui.tabs.AbstractFormsLaunchConfigurationTab;
 import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
 import org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage;
 import org.eclipse.ui.forms.AbstractFormPart;
 
 /**
  * Abstract editor page implementation serving as container for a launch tab.
  */
 public abstract class AbstractLaunchTabContainerEditorPage extends AbstractCustomFormToolkitEditorPage {
 	// Reference to the launch configuration tab
 	private final AbstractLaunchConfigurationTab launchTab;
 
 	boolean isDirty = false;
 
 	/**
 	 * Constructor.
 	 */
 	public AbstractLaunchTabContainerEditorPage() {
 		super();
 
 		// Create the launch configuration tab instance
 		launchTab = createLaunchConfigurationTab();
 		Assert.isNotNull(launchTab);
 	}
 
 	/**
 	 * Creates a new instance of the launch configuration tab to associate.
 	 *
 	 * @return The new launch configuration tab instance.
 	 */
 	protected abstract AbstractLaunchConfigurationTab createLaunchConfigurationTab();
 
 	/**
 	 * Returns the associated launch configuration tab.
 	 *
 	 * @return The launch configuration tab or <code>null</code>.
 	 */
 	protected final AbstractLaunchConfigurationTab getLaunchConfigurationTab() {
 		return launchTab;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#dispose()
 	 */
 	@Override
 	public void dispose() {
 		launchTab.dispose();
 		super.dispose();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#getContextHelpId()
 	 */
 	@Override
 	protected String getContextHelpId() {
 		return launchTab.getHelpContextId();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#getFormTitle()
 	 */
 	@Override
 	protected String getFormTitle() {
 		return launchTab.getName();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#getFormImage()
 	 */
 	@Override
 	protected Image getFormImage() {
 		return launchTab.getImage();
 	}
 
 	/**
 	 * Set the dirty state for this editor page.
 	 * @param dirty The dirty state.
 	 */
 	public void setDirty(boolean dirty) {
 		isDirty = dirty;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#doCreateFormContent(org.eclipse.swt.widgets.Composite, org.eclipse.tcf.te.ui.forms.CustomFormToolkit)
 	 */
 	@Override
 	protected void doCreateFormContent(Composite parent, CustomFormToolkit toolkit) {
 		Assert.isNotNull(parent);
 		Assert.isNotNull(toolkit);
 
 		// Create the launch tab content
 		if (launchTab instanceof AbstractFormsLaunchConfigurationTab) {
 			((AbstractFormsLaunchConfigurationTab)launchTab).createFormContent(getManagedForm());
 		} else {
 			launchTab.createControl(parent);
 		}
 
 		getManagedForm().addPart(new AbstractFormPart() {
 			@Override
 			public boolean isDirty() {
 				return isDirty;
 			}
 
 			@Override
 			public void commit(boolean onSave) {
 				super.commit(onSave);
 
 				if (onSave) {
 					extractData();
 				}
 			}
 		});
 
 		// Fix the background color of the launch tab controls
 		Color bg = parent.getBackground();
 		Control[] children = parent.getChildren();
 		if (bg != null && children != null && children.length > 0) {
 			fixBackgrounds(children, bg);
 		}
 	}
 
 	/**
 	 * Set the data to the page.
 	 * @param input The editor input.
 	 * @return <code>true</code> if data was set.
 	 */
 	public abstract boolean setupData(Object input);
 
 	/**
 	 * Extract the data from the page.
 	 * @return <code>true</code> if data was set.
 	 */
 	public abstract boolean extractData();
 
 	@Override
 	public void setActive(boolean active) {
 		super.setActive(active);
		if (active) setupData(getEditorInput());
 	}
 
 	/**
 	 * Set the background color of the given controls and their children
 	 * to the given color.
 	 *
 	 * @param controls The list of controls. Must not be <code>null</code>.
 	 * @param bg The background color. Must not be <code>null</code>.
 	 */
 	protected final void fixBackgrounds(Control[] controls, Color bg) {
 		Assert.isNotNull(controls);
 		Assert.isNotNull(bg);
 		for (Control c : controls) {
 			if (!(c instanceof Composite) && !(c instanceof Label) && !(c instanceof Button)) {
 				continue;
 			}
 			if (c instanceof Button) {
 				int style = ((Button)c).getStyle();
 				if ((style & SWT.RADIO) == 0 && (style & SWT.CHECK) == 0) {
 					continue;
 				}
 			}
 			if (!bg.equals(c.getBackground())) {
 				c.setBackground(bg);
 			}
 			if (c instanceof Composite) {
 				Control[] children = ((Composite)c).getChildren();
 				if (children != null && children.length > 0) {
 					fixBackgrounds(children, bg);
 				}
 			}
 		}
 	}
 }
