 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.ui.dialogfields;
 
import org.eclipse.core.runtime.Assert;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.DialogField;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogFieldApplyListener;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogFieldChangeListener;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.IStringButtonAdapter;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.ISupportTextValue;
 import org.eclipse.jst.pagedesigner.meta.IAttributeDescriptor;
 import org.eclipse.jst.pagedesigner.meta.IBindingHandler;
 import org.eclipse.jst.pagedesigner.properties.attrgroup.IElementContextable;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.forms.events.IHyperlinkListener;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 
 /**
  * This is a wrapper to a dialog field, by adding a small image button at the
  * end. The caller is responsible to provide the image and the button click
  * handler.
  * 
  * @author mengbo
  * @version 1.5
  * @see org.eclipse.jst.pagedesigner.properties.celleditors.CellEditorWrapper
  */
 // NOTE: currently this class is dedicated to page designer by using the
 // IElementContextable interface.
 // It should be very easy to make it standard alone and reused in other places.
 public class DialogFieldWrapper implements DialogField, ISupportTextValue,
 		IElementContextable {
 	private DialogField _wrapped;
 
 	private IDOMNode _ancester;
 
 	private IDOMElement _element;
 
 	private Button _databindingButton;
 
 	private boolean _databindingEnabled;
 
 	private Image _image;
 
 	private Image _disabledImage;
 
 	private IStringButtonAdapter _adapter;
 
 	private String _uri;
 
 	private String _tagName;
 
 	private IAttributeDescriptor _attr;
 
 	private IBindingHandler _handler;
 
 	/**
 	 * 
 	 */
 	public DialogFieldWrapper(DialogField field, Image image,
 			Image disabledImage, String uri, String tagName,
 			IAttributeDescriptor attr, IBindingHandler handler) {
 		super();
 		if (!(field instanceof ISupportTextValue)) {
 			throw new IllegalArgumentException(
 					"Field must be ISupportTextValue");
 		}
 		_wrapped = field;
 		this._image = image;
 		this._disabledImage = disabledImage;
 		this._uri = uri;
 		this._tagName = tagName;
 		this._attr = attr;
 		this._handler = handler;
 
 		setDatabindingPressedHandler(new IStringButtonAdapter() {
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.IStringButtonAdapter#changeControlPressed(org.eclipse.jst.jsf.common.ui.internal.dialogfield.DialogField)
 			 */
 			public void changeControlPressed(DialogField field1) {
 				Shell shell = field1.getLabelControl(null, null).getShell();
 				DialogFieldWrapper wrapper = (DialogFieldWrapper) field1;
 				String result = _handler
 						.handleBinding(shell, wrapper.getAncester(), wrapper
 								.getElement(), wrapper.getText());
 				if (result != null) {
 					wrapper.setText(result);
 				}
 			}
 		});
 	}
 
 	public void setDatabindingPressedHandler(IStringButtonAdapter adapter) {
 		this._adapter = adapter;
 		this.updateDatabindingControl();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.ISupportTextValue#setTextWithoutUpdate(java.lang.String)
 	 */
 	public void setTextWithoutUpdate(String value) {
 		((ISupportTextValue) _wrapped).setTextWithoutUpdate(value);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.ISupportTextValue#getText()
 	 */
 	public String getText() {
 		return ((ISupportTextValue) _wrapped).getText();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.ISupportTextValue#setText(java.lang.String)
 	 */
 	public void setText(String value) {
 		((ISupportTextValue) _wrapped).setText(value);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.properties.attrgroup.IElementContextable#setElementContext(org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode,
 	 *      org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement)
 	 */
 	public void setElementContext(IDOMNode ancester, IDOMElement element) {
 		if (_wrapped instanceof IElementContextable) {
 			((IElementContextable) _wrapped).setElementContext(ancester,
 					element);
 		}
 		this._ancester = ancester;
 		this._element = element;
 
 		boolean bindingEnabled = _handler.isEnabled(_ancester, _element, _uri,
 				_tagName, _attr);
 		this.setDatabindingEnabled(bindingEnabled);
 	}
 
 	// --------------------------------------------------------------------------------------------
 	// wrapped method to add the data binding browse button
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.DialogField#doFillIntoGrid(org.eclipse.ui.forms.widgets.FormToolkit,
 	 *      org.eclipse.swt.widgets.Composite, int)
 	 */
 	public Control[] doFillIntoGrid(FormToolkit toolkit, Composite parent,
 			int nColumns) {
 		Control[] wrappedControls = _wrapped.doFillIntoGrid(toolkit, parent,
 				nColumns - 1);
 		Control[] result = new Control[wrappedControls.length];
 
 		Control button = getDatabingingButton(toolkit, parent);
 		button.setLayoutData(gridDataForDatabindingButton(1));
 		button.setVisible(false);
 
 		System.arraycopy(wrappedControls, 0, result, 0, wrappedControls.length);
 		result[result.length - 1] = _databindingButton;
 		return result;
 	}
 
 	/**
 	 * @param span
 	 * @return
 	 */
 	private GridData gridDataForDatabindingButton(int span) {
 		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gd.horizontalSpan = span;
 		gd.widthHint = gd.heightHint = 18;
 		return gd;
 	}
 
 	/**
 	 * @param toolkit
 	 * @param parent
 	 * @return
 	 */
 	private Control getDatabingingButton(FormToolkit toolkit, Composite parent) {
 		if (_databindingButton == null) {
 			Assert.isNotNull(parent,
 					"uncreated control requested with composite null"); //$NON-NLS-1$
 			if (toolkit != null) {
 				_databindingButton = toolkit.createButton(parent, "", SWT.PUSH);
 				_databindingButton.setImage(getImage());
 			} else {
 				_databindingButton = new Button(parent, SWT.PUSH);
 				_databindingButton.setImage(getImage());
 			}
 			_databindingButton.addPaintListener(new PaintListener() {
 				public void paintControl(PaintEvent e) {
 					if (!_databindingButton.isEnabled()
 							&& getDisabledImage() != null) {
 						Rectangle buttonBounds = _databindingButton.getBounds();
 						Rectangle imageBounds = getDisabledImage().getBounds();
 						e.gc.drawImage(getDisabledImage(),
 								(buttonBounds.width - imageBounds.width) / 2,
 								(buttonBounds.height - imageBounds.height) / 2);
 					}
 				}
 			});
 			_databindingButton.setEnabled(isEnabled() && _databindingEnabled);
 			_databindingButton.addSelectionListener(new SelectionListener() {
 				public void widgetDefaultSelected(SelectionEvent e) {
 					databindingControlPressed();
 				}
 
 				public void widgetSelected(SelectionEvent e) {
 					databindingControlPressed();
 				}
 			});
 
 		}
 		return _databindingButton;
 	}
 
 	/**
 	 * @return
 	 */
 	private Image getImage() {
 		return _image;
 	}
 
 	private Image getDisabledImage() {
 		return _disabledImage;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.DialogField#getLabelControl(org.eclipse.ui.forms.widgets.FormToolkit,
 	 *      org.eclipse.swt.widgets.Composite)
 	 */
 	public Control getLabelControl(FormToolkit _formToolkit, Composite parent) {
 		return _wrapped.getLabelControl(_formToolkit, parent);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogField#setHyperLink(org.eclipse.ui.forms.events.IHyperlinkListener)
 	 */
 	public void setHyperLink(IHyperlinkListener listener) {
 		_wrapped.setHyperLink(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogField#setLabelText(java.lang.String)
 	 */
 	public void setLabelText(String labeltext) {
 		_wrapped.setLabelText(labeltext);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogField#setDialogFieldChangeListener(org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogFieldChangeListener)
 	 */
 	public void setDialogFieldChangeListener(IDialogFieldChangeListener listener) {
 		_wrapped.setDialogFieldChangeListener(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogField#setDialogFieldApplyListener(org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogFieldApplyListener)
 	 */
 	public void setDialogFieldApplyListener(IDialogFieldApplyListener listener) {
 		_wrapped.setDialogFieldApplyListener(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogField#setFocus()
 	 */
 	public boolean setFocus() {
 		return _wrapped.setFocus();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogField#getNumberOfControls()
 	 */
 	public int getNumberOfControls() {
 		return _wrapped.getNumberOfControls() + 1;
 	}
 
 	public void setDatabindingEnabled(boolean enabled) {
 		this._databindingEnabled = enabled;
 		updateDatabindingControl();
 	}
 
 	public boolean isDatabindingEnabled() {
 		return _databindingEnabled;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogField#setEnabled(boolean)
 	 */
 	public void setEnabled(boolean enabled) {
 		_wrapped.setEnabled(enabled);
 		updateDatabindingControl();
 	}
 
 	/**
 	 * 
 	 */
 	private void updateDatabindingControl() {
 		if (this._databindingButton != null && !_databindingButton.isDisposed()) {
 			this._databindingButton.setEnabled(this.isEnabled()
 					&& _databindingEnabled && _adapter != null);
 			_databindingButton.redraw();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogField#isEnabled()
 	 */
 	public boolean isEnabled() {
 		return _wrapped.isEnabled();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogField#getAttachedData(java.lang.Object)
 	 */
 	public Object getAttachedData(Object key) {
 		return _wrapped.getAttachedData(key);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogField#putAttachedData(java.lang.Object,
 	 *      java.lang.Object)
 	 */
 	public void putAttachedData(Object key, Object value) {
 		_wrapped.putAttachedData(key, value);
 	}
 
 	protected void databindingControlPressed() {
 		if (_adapter != null) {
 			_adapter.changeControlPressed(this);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.common.ui.internal.dialogfield.DialogField#handleGrabHorizontal()
 	 */
 	public void handleGrabHorizontal() {
 		_wrapped.handleGrabHorizontal();
 	}
 
 	public IDOMNode getAncester() {
 		return _ancester;
 	}
 
 	public IDOMElement getElement() {
 		return _element;
 	}
 
 	public DialogField getWrappedDialogField() {
 		return _wrapped;
 	}
 
 	public boolean isRequired() {
 		return _wrapped.isRequired();
 	}
 
 	public void setToolTip(String toolTip) {
 		_wrapped.setToolTip(toolTip);
 	}
 }
