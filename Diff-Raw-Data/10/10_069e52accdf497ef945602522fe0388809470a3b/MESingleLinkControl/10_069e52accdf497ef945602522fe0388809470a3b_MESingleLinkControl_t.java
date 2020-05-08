 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.ecp.editor.mecontrols.melinkcontrol;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecp.editor.ModelElementChangeListener;
 import org.eclipse.emf.ecp.editor.mecontrols.AbstractMEControl;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 
 /**
  * GUI Control for the ME reference single links.
  * 
  * @author helming
  */
 public class MESingleLinkControl extends AbstractMEControl {
 
 	private Composite composite;
 
 	private EReference eReference;
 
 	private Composite linkArea;
 
 	private Composite parent;
 
 	private int style;
 
 	private MELinkControl meControl;
 
 	private Label labelWidget;
 
 	private static final int PRIORITY = 1;
 
 	private ModelElementChangeListener modelElementChangeListener;
 
 	/**
 	 * Standard Constructor.
 	 */
 	public MESingleLinkControl() {
 		super();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Control createControl(final Composite parent, int style) {
 		Object feature = getItemPropertyDescriptor().getFeature(getModelElement());
 		this.eReference = (EReference) feature;
 		composite = getToolkit().createComposite(parent, style);
		List<Action> initActions = initActions();
		int columns = initActions.size()+1;
		composite.setLayout(new GridLayout(columns, false));
 		if (!getItemPropertyDescriptor().canSetProperty(getModelElement())) {
 			composite.setEnabled(false);
 		}
		GridLayoutFactory.fillDefaults().spacing(0, 0).numColumns(columns).equalWidth(false).applyTo(composite);
 		this.parent = parent;
 		this.style = style;
 		linkArea = getToolkit().createComposite(composite);
 		linkArea.setLayout(new FillLayout());
 		updateLink();
 
		for (Action action : initActions) {
 			createButtonForAction(action);
 		}
 
 		modelElementChangeListener = new ModelElementChangeListener(getModelElement()) {
 
 			@Override
 			public void onChange(Notification notification) {
 				if (notification.getFeature() == eReference) {
 					updateLink();
 				}
 
 			}
 		};
 
 		return composite;
 	}
 
 	/**
 	 * Creates the actions for the control.
 	 * 
 	 * @return list of actions
 	 */
 	protected List<Action> initActions() {
 		List<Action> result = new ArrayList<Action>();
 		AddReferenceAction addAction = new AddReferenceAction(getModelElement(), eReference,
 			getItemPropertyDescriptor(), getContext());
 		result.add(addAction);
 		ReferenceAction newAction = new NewReferenceAction(getModelElement(), eReference, getItemPropertyDescriptor(),
 			getContext());
 		result.add(newAction);
 		return result;
 	}
 
 	/**
 	 * @return the eReference
 	 */
 	protected EReference geteReference() {
 		return eReference;
 	}
 
 	/**
 	 * Creates a button for an action.
 	 * 
 	 * @param action the action
 	 */
 	protected void createButtonForAction(final Action action) {
 		Button selectButton = getToolkit().createButton(composite, "", SWT.PUSH);
 		selectButton.setImage(action.getImageDescriptor().createImage());
 		selectButton.setToolTipText(action.getToolTipText());
 		selectButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				action.run();
 			}
 
 		});
 	}
 
 	private void updateLink() {
 		if (meControl != null) {
 			meControl.dispose();
 		}
 		if (labelWidget != null) {
 			labelWidget.dispose();
 		}
 
 		EObject opposite = (EObject) getModelElement().eGet(eReference);
 		if (opposite != null) {
 			MELinkControlFactory meLinkControlFactory = new MELinkControlFactory();
 			meControl = meLinkControlFactory.createMELinkControl(getItemPropertyDescriptor(), opposite,
 				getModelElement(), getContext());
 			meControl.createControl(linkArea, style, getItemPropertyDescriptor(), opposite, getModelElement(),
 				getToolkit(), getContext());
 		} else {
 			labelWidget = getToolkit().createLabel(linkArea, "(Not Set)");
 			labelWidget.setBackground(parent.getBackground());
 			labelWidget.setForeground(parent.getShell().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
 		}
 		linkArea.layout(true);
 		composite.layout(true);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void dispose() {
 		modelElementChangeListener.remove();
 		if (meControl != null) {
 			meControl.dispose();
 		}
 
 	}
 
 	@Override
 	public int canRender(IItemPropertyDescriptor itemPropertyDescriptor, EObject modelElement) {
 		Object feature = itemPropertyDescriptor.getFeature(modelElement);
 		if (feature instanceof EReference && !((EReference) feature).isMany()
 			&& EObject.class.isAssignableFrom(((EReference) feature).getEType().getInstanceClass())) {
 			return PRIORITY;
 		}
 		return AbstractMEControl.DO_NOT_RENDER;
 	}
 }
