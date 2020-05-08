 /*******************************************************************************
  * Copyright (c) 2009 Florian Pirchner and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Florian Pirchner  initial API and implementation (based on other ridgets of 
  *                    compeople AG)
  * compeople AG     - adjustments for Riena v1.2
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.util.Arrays;
 import java.util.Collections;
 
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Link;
 
 import org.eclipse.riena.core.util.ListenerList;
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.ui.ridgets.AbstractMarkerSupport;
 import org.eclipse.riena.ui.ridgets.ILinkRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.listener.ISelectionListener;
 import org.eclipse.riena.ui.ridgets.swt.AbstractSWTRidget;
 import org.eclipse.riena.ui.ridgets.swt.AbstractValueRidget;
 import org.eclipse.riena.ui.ridgets.swt.BasicMarkerSupport;
 
 /**
  * Ridget for an SWT {@link Link} widget.
  * 
  * @since 1.2
  */
 public class LinkRidget extends AbstractValueRidget implements ILinkRidget {
 
 	private final LinkSelectionObserver selectionObserver;
 
 	private String text;
 	private boolean textAlreadyInitialized;
 
 	public LinkRidget() {
 		selectionObserver = new LinkSelectionObserver(this);
 	}
 
 	@Override
 	protected void checkUIControl(final Object uiControl) {
 		AbstractSWTRidget.assertType(uiControl, Link.class);
 	}
 
 	@Override
 	protected void bindUIControl() {
 		final Link control = getUIControl();
 		if (control != null) {
 			control.addSelectionListener(selectionObserver);
 			initText();
 			updateUIText();
 		}
 	}
 
 	@Override
 	protected void unbindUIControl() {
 		super.unbindUIControl();
 		final Link control = getUIControl();
 		if (control != null) {
 			control.removeSelectionListener(selectionObserver);
 		}
 	}
 
 	@Override
 	protected AbstractMarkerSupport createMarkerSupport() {
 		return new BasicMarkerSupport(this, propertyChangeSupport);
 	}
 
 	@Override
 	protected IObservableValue getRidgetObservable() {
 		return BeansObservables.observeValue(this, ILinkRidget.PROPERTY_TEXT);
 	}
 
 	public void addSelectionListener(final ISelectionListener listener) {
 		selectionObserver.addListener(listener);
 	}
 
 	@Override
 	public Link getUIControl() {
 		return (Link) super.getUIControl();
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	/**
 	 * Always returns true because mandatory markers do not make sense for this
 	 * ridget.
 	 */
 	@Override
 	public boolean isDisableMandatoryMarker() {
		return false;
 	}
 
 	public void removeSelectionListener(final ISelectionListener listener) {
 		selectionObserver.removeListener(listener);
 	}
 
 	public void setText(final String text) {
 		final String oldText = this.text;
 		this.text = text;
 		updateUIText();
 		firePropertyChange(ILinkRidget.PROPERTY_TEXT, oldText, this.text);
 	}
 
 	public void setText(final String text, final String link) {
 		String mergedText;
 		if (StringUtils.isDeepEmpty(text) && StringUtils.isDeepEmpty(link)) {
 			mergedText = ""; //$NON-NLS-1$
 		} else if (StringUtils.isDeepEmpty(link)) {
 			mergedText = String.format("<a>%s</a>", convertNullToEmpty(text)); //$NON-NLS-1$
 		} else {
 			mergedText = String.format("<a href=\"%s\">%s</a>", convertNullToEmpty(link), convertNullToEmpty(text)); //$NON-NLS-1$
 		}
 		setText(mergedText);
 	}
 
 	private String convertNullToEmpty(final String value) {
 		return StringUtils.isDeepEmpty(value) ? "" : value; //$NON-NLS-1$
 	}
 
 	// helping methods
 	//////////////////
 
 	private void updateUIText() {
 		final Link control = getUIControl();
 		if (control != null) {
 			control.setText(convertNullToEmpty(text));
 		}
 	}
 
 	/**
 	 * If the text of the ridget has no value, initialize it with the text of
 	 * the UI control.
 	 */
 	private void initText() {
 		if (text == null && !textAlreadyInitialized) {
 			final Link control = getUIControl();
 			if (control != null && !control.isDisposed()) {
 				text = control.getText();
 				textAlreadyInitialized = true;
 			}
 		}
 	}
 
 	// helping classes
 	//////////////////
 
 	/**
 	 * Forwards selection events from the Link widget to a collection of
 	 * ISelectionListeners.
 	 */
 	private static final class LinkSelectionObserver extends AbstractObserver<ISelectionListener> {
 		public LinkSelectionObserver(final IRidget source) {
 			super(source);
 		}
 
 		@Override
 		protected ListenerList<ISelectionListener> createList() {
 			return new ListenerList<ISelectionListener>(ISelectionListener.class);
 		}
 
 		@Override
 		protected void fireAction(final SelectionEvent evt) {
 			final ListenerList<ISelectionListener> listeners = getListeners();
 			if (listeners != null) {
 				final org.eclipse.riena.ui.ridgets.listener.SelectionEvent event = new org.eclipse.riena.ui.ridgets.listener.SelectionEvent(
 						getSource(), Collections.EMPTY_LIST, Arrays.asList(evt.text));
 				for (final ISelectionListener listener : listeners.getListeners()) {
 					listener.ridgetSelected(event);
 				}
 			}
 		}
 	}
 
 }
