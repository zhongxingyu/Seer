 /*******************************************************************************
  * Copyright (c) 2004 - 2006 University Of British Columbia and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     University Of British Columbia - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.mylar.internal.tasks.ui.views;
 
 import java.lang.reflect.Field;
 
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.mylar.context.core.MylarStatusHandler;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.dialogs.FilteredTree;
 import org.eclipse.ui.dialogs.PatternFilter;
 
 /**
  * @author Mik Kersten
  */
 public abstract class AbstractMylarFilteredTree extends FilteredTree {
 
 	private static final int filterWidth = 70;
 
 	public static final String LABEL_FIND = " Find:";
 
 	private Job refreshJob;
 	
 	private AdaptiveRefreshPolicy refreshPolicy;
 	
 	/**
	 * HACK: using reflection to gain access
 	 */
 	public AbstractMylarFilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
 		super(parent, treeStyle, filter);
 		Field refreshField;
 		try {
 			refreshField = FilteredTree.class.getDeclaredField("refreshJob");
 			refreshField.setAccessible(true);
 			refreshJob = (Job) refreshField.get(this);
 			refreshPolicy = new AdaptiveRefreshPolicy(refreshJob, super.getFilterControl());
 		} catch (Exception e) {
 			MylarStatusHandler.fail(e, "Could not get refresh job", false);
 		}
 		setInitialText("");
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 	}
 
 	@Override
 	protected Composite createFilterControls(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
 		container.setLayoutData(gridData);
 		GridLayout gridLayout = new GridLayout(4, false);
 		gridLayout.marginHeight = 0;
 		gridLayout.marginWidth = 0;
 		container.setLayout(gridLayout);
 
 		Label label = new Label(container, SWT.LEFT);
 		label.setText(LABEL_FIND);
 
 		super.createFilterControls(container);
 
 		filterText.addKeyListener(new KeyListener() {
 
 			public void keyPressed(KeyEvent e) {
 				if (e.character == SWT.ESC) {
 					setFilterText("");
 				}
 			}
 
 			public void keyReleased(KeyEvent e) {
 				// ignore
 			}
 		});
 
 		Composite status = createStatusComposite(container);
 		if (status != null) {
 			filterText.setLayoutData(new GridData(filterWidth, label.getSize().y));
 		}
 		return container;
 	}
 
 	protected abstract Composite createStatusComposite(Composite container);
 
 	protected void textChanged() {
 		if (refreshPolicy != null) {
 			refreshPolicy.textChanged(filterText.getText());
 		}
 	}
 
 	protected Job getRefreshJob() {
 		return refreshJob;
 	}
 
 	public AdaptiveRefreshPolicy getRefreshPolicy() {
 		return refreshPolicy;
 	}
 }
