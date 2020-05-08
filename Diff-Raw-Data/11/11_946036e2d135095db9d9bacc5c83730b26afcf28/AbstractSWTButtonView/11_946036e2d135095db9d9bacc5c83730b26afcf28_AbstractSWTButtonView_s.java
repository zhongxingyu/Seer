 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.swt.views;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.CoolItem;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.wazaabi.engine.core.editparts.AbstractButtonEditPart;
 import org.eclipse.wazaabi.engine.core.editparts.PushButtonEditPart;
 import org.eclipse.wazaabi.engine.core.views.AbstractButtonView;
 import org.eclipse.wazaabi.engine.swt.editparts.stylerules.managers.ImageRuleManager;
 import org.eclipse.wazaabi.mm.core.styles.BooleanRule;
 import org.eclipse.wazaabi.mm.core.styles.ImageRule;
 import org.eclipse.wazaabi.mm.core.styles.StringRule;
 import org.eclipse.wazaabi.mm.core.styles.StyleRule;
 
 public abstract class AbstractSWTButtonView extends SWTControlView implements
 		AbstractButtonView {
 
 	private Image image = null;
 
 	protected Widget createSWTWidget(Widget parent, int swtStyle, int index) {
 		int style = computeSWTCreationStyle(getHost());
 		return checkParentLayout((Composite) parent,new Button((org.eclipse.swt.widgets.Composite) parent,style));
 	}
 
 
 	protected void setText(StringRule rule) {
 		String currentText = ((Button) getSWTControl()).getText();
 		if (rule == null) {
 			if ("".equals(currentText)) //$NON-NLS-1$
 				return;
 			else {
 				((Button) getSWTControl()).setText(""); //$NON-NLS-1$
 				revalidate();
 			}
 		} else {
 			((Button) getSWTControl())
 					.setText(rule.getValue() == null ? "" : rule.getValue()); //$NON-NLS-1$
			revalidate();
 		}
 		if (getSWTItem() != null) {
 			Point size = ((Button) getSWTControl()).computeSize (SWT.DEFAULT, SWT.DEFAULT);
 			if (getSWTItem() instanceof ToolItem)
 				((ToolItem) getSWTItem()).setWidth(size.x);
 			if (getSWTItem() instanceof CoolItem)
 				((CoolItem)getSWTItem()).setPreferredSize(((CoolItem)getSWTItem()).computeSize(size.x, size.y));
 		}
 	}
 
 	protected void setImage(ImageRule rule) {
 		if (rule == null)
 			if (image == null)
 				return;
 			else {
 				System.out.println("disposing image from "
 						+ System.identityHashCode(this));
 				image.dispose();
 				image = null;
 			}
 		else {
 			Image newImage = ImageRuleManager.convertToPlatformSpecificObject(
 					this, rule);
 			if (image != null) {
 				if (newImage != null
 						&& image.getImageData().equals(newImage.getImageData()))
 					return;
 				System.out.println("disposing image from "
 						+ System.identityHashCode(this));
 				image.dispose();
 			}
 			image = newImage;
 		}
 		((Button) getSWTControl()).setImage(image);
 		getSWTControl().update();
 		System.out.println("setImage " + image);
 		revalidate();
 	}
 
 	@Override
 	public void updateStyleRule(StyleRule rule) {
 		if (rule == null)
 			return;
 		if (PushButtonEditPart.TEXT_PROPERTY_NAME
 				.equals(rule.getPropertyName()))
 			if (rule instanceof StringRule)
 				setText((StringRule) rule);
 			else
 				setText(null);
 		else if (PushButtonEditPart.IMAGE_PROPERTY_NAME.equals(rule
 				.getPropertyName()))
 			if (rule instanceof ImageRule)
 				setImage((ImageRule) rule);
 			else
 				setImage(null);
 
 		else
 			super.updateStyleRule(rule);
 	}
 	
 	protected int computeSWTCreationStyle(StyleRule rule) {
 		final String propertyName = rule.getPropertyName();
 		if (AbstractButtonEditPart.FLAT_PROPERTY_NAME.equals(propertyName)
 				&& ((BooleanRule) rule).isValue())
 			return SWT.FLAT;
 		return super.computeSWTCreationStyle(rule);
 	}
 	
 	@Override
 	public boolean needReCreateWidgetView(StyleRule styleRule, org.eclipse.swt.widgets.Widget widget) {
 		if (styleRule == null)
 			return false;
 		if (AbstractButtonEditPart.FLAT_PROPERTY_NAME.equals(styleRule.getPropertyName())
 				&& styleRule instanceof BooleanRule) {
 			return !(isStyleBitCorrectlySet(widget, org.eclipse.swt.SWT.FLAT,
 					((BooleanRule) styleRule).isValue()));
 		} else
 			return super.needReCreateWidgetView(styleRule, widget);
 	}
 
 	protected void widgetDisposed() {
 		super.widgetDisposed();
 		if (image != null && !image.isDisposed()) {
 			System.out.println("disposing image from "
 					+ System.identityHashCode(this));
 			image.dispose();
 		}
 	}
 
 }
