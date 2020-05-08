 /*******************************************************************************
  * Copyright (c) 2008, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.widgets;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.Enumerator;
 import org.eclipse.emf.eef.runtime.ui.utils.EditingUtils;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 /**
  * A viewer composed of button to handles EEnum as Radio. It will display a radio for each element of the
  * enum.
  * 
  * @author <a href="mailto:stephane.bouchet@obeo.fr">Stephane Bouchet</a>
  */
 public class RadioViewer extends Composite implements ISelectionProvider {
 
 	/**
 	 * Listeners for selection changes
 	 */
 	private List<ISelectionChangedListener> listeners;
 
 	private RadioSelectionChangedListener radioSelectionChangedListener;
 
 	/**
 	 * the map of buttons used.
 	 */
 	private Map<Object, Button> buttons;
 
 	public RadioViewer(Composite parent, int style) {
 		super(parent, style);
 		buttons = new HashMap<Object, Button>();
 		this.setLayout(new GridLayout());
 		listeners = new ArrayList<ISelectionChangedListener>();
 		EditingUtils.setEEFtype(this, "eef::RadioViewer");
 		radioSelectionChangedListener = new RadioSelectionChangedListener();
 	}
	
	/**
	 * Clear all the graphical elements of the view
	 */
	private void clear() {
		while (this.getChildren().length > 0) {
			Control next = this.getChildren()[0];
			next.dispose();
		}
	}
 
 	private void createButton(Object value) {
 		Button b = new Button(this, SWT.RADIO);
 		if (value instanceof Enumerator) {
 			b.setText(((Enumerator)value).getLiteral());
 		}
 		GridData data = new GridData(GridData.FILL_BOTH);
 		b.setLayoutData(data);
 		b.addSelectionListener(radioSelectionChangedListener);
 		buttons.put(value, b);
 		EditingUtils.setEEFtype(b, "eef::RadioViewer::radio");
 		if (EditingUtils.getID(this) != null)
 			EditingUtils.setID(b, EditingUtils.getID(this));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
 	 */
 	@Override
 	public void setEnabled(boolean enabled) {
 		for (Button button : buttons.values()) {
 			button.setEnabled(enabled);
 		}
 		super.setEnabled(enabled);
 	}
 
 	/**
 	 * @param newValue
 	 */
 	public void setSelection(ISelection selection) {
 		if (selection instanceof StructuredSelection) {
 			StructuredSelection sSelection = (StructuredSelection)selection;
 			Object newValue = sSelection.getFirstElement();
 			for (Object value : buttons.keySet()) {
 				buttons.get(value).setSelection(false);
 				if (value instanceof Enumerator && newValue instanceof Enumerator) {
 					if (((Enumerator)value).getValue() == ((Enumerator)newValue).getValue()) {
 						buttons.get(value).setSelection(true);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
 	 */
 	public ISelection getSelection() {
 		for (Object value : buttons.keySet()) {
 			if (buttons.get(value).getSelection())
 				return new StructuredSelection(value);
 		}
 		return null;
 	}
 
 	public void setInput(Object input) {
		clear();
 		if (input instanceof Collection) {
 			for (Object value : (Collection<?>)input) {
 				createButton(value);
 			}
 		}
 		// refresh Layout
 		GridLayout l = (GridLayout)this.getLayout();
 		l.numColumns = buttons.size() == 0 ? 1 : buttons.size();
 		this.setLayout(l);
 		layout();
 	}
 
 	/**
 	 * Sets the given ID to the EObjectFlatComboViewer
 	 * 
 	 * @param id
 	 *            the id of the widget
 	 */
 	public void setID(Object id) {
 		EditingUtils.setID(this, id);
 		for (Control control : getChildren()) {
 			EditingUtils.setID(control, id);
 		}
 	}
 
 	/**
 	 * @return the ID of the EObjectFlatComboViewer
 	 */
 	public Object getID() {
 		return EditingUtils.getID(this);
 	}
 
 	/**
 	 * Sets the tooltip text for the viewer
 	 * 
 	 * @param tooltip
 	 *            the tooltip text
 	 */
 	public void setToolTipText(String tooltip) {
 		for (Button button : buttons.values()) {
 			button.setToolTipText(tooltip);
 		}
 	}
 
 	/**
 	 * Add a new SelectionChangedListener
 	 * 
 	 * @param listener
 	 *            the listener to add
 	 */
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		listeners.add(listener);
 	}
 
 	/**
 	 * remove a SelectionChanged listener
 	 * 
 	 * @param listener
 	 *            the listener to remove
 	 */
 	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 		listeners.remove(listener);
 	}
 
 	private class RadioSelectionChangedListener extends SelectionAdapter {
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 		 */
 		public void widgetSelected(SelectionEvent e) {
 			Object value = null;
 			for (Object tmp : buttons.keySet()) {
 				if (buttons.get(tmp).getSelection())
 					value = tmp;
 			}
 			if (value != null) {
 				for (ISelectionChangedListener listener : listeners) {
 					listener.selectionChanged(new SelectionChangedEvent(RadioViewer.this,
 							new StructuredSelection(value)));
 				}
 			}
 		}
 
 	}
 }
