 /*******************************************************************************
  * Copyright (c) 2013 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.swt.forms.views;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.forms.widgets.FormText;
 import org.eclipse.wazaabi.engine.swt.commons.editparts.stylerules.managers.ImageRuleManager;
 import org.eclipse.wazaabi.engine.swt.forms.editparts.LabelEditPart;
 import org.eclipse.wazaabi.mm.core.styles.BlankRule;
 import org.eclipse.wazaabi.mm.core.styles.BooleanRule;
 import org.eclipse.wazaabi.mm.core.styles.ColorRule;
 import org.eclipse.wazaabi.mm.core.styles.CoreStylesPackage;
 import org.eclipse.wazaabi.mm.core.styles.FontRule;
 import org.eclipse.wazaabi.mm.core.styles.ImageRule;
 import org.eclipse.wazaabi.mm.core.styles.StringRule;
 import org.eclipse.wazaabi.mm.core.styles.StyleRule;
 import org.eclipse.wazaabi.mm.core.styles.StyledElement;
 
 public class SWTLabelView extends
 		org.eclipse.wazaabi.engine.swt.commons.views.SWTLabelView {
 
 	private final SWTContainerView containingForm;
 	private HashMap<String, Color> colors = new HashMap<String, Color>();
 	private HashMap<String, Font> fonts = new HashMap<String, Font>();
 	private HashMap<String, Image> images = new HashMap<String, Image>();
 
 	public SWTLabelView(SWTContainerView containingForm) {
 		this.containingForm = containingForm;
 	}
 
 	/**
 	 * private for avoiding the use of this constructor
 	 */
 	@SuppressWarnings("unused")
 	private SWTLabelView() {
 		this.containingForm = null;
 	}
 
 	@Override
 	protected Control createLabel(Composite parent, int style) {
 		if (containingForm == null || containingForm.getFormToolkit() == null)
 			return super.createLabel(parent, style);
 		BooleanRule renderXMLStyle = (BooleanRule) ((StyledElement) getHost()
 				.getModel()).getFirstStyleRule(LabelEditPart.RENDER_XML,
 				CoreStylesPackage.Literals.BOOLEAN_RULE);
 		if (renderXMLStyle != null && renderXMLStyle.isValue())
 			return containingForm.getFormToolkit()
 					.createFormText(parent, false);
 		return containingForm.getFormToolkit().createLabel((Composite) parent,
 				null, computeSWTCreationStyle(getHost()));
 	}
 
 	@Override
 	protected Control createLink(Composite parent, int style) {
 		if (containingForm == null || containingForm.getFormToolkit() == null)
 			return super.createLink(parent, style);
 		return containingForm.getFormToolkit().createHyperlink(
 				((org.eclipse.swt.widgets.Composite) parent), null,
 				computeSWTCreationStyle(getHost()));
 	}
 
 	@Override
 	protected Control wrapForSpecificParent(Composite parent, Control widget) {
 		if (SWTFormsUtils.isDirectChildOfForm(getHost()))
 			return widget;
 		return super.wrapForSpecificParent(parent, widget);
 	}
 
 	@Override
 	public void setText(StringRule rule) {
 		if (getSWTWidget() instanceof FormText) {
 			if (rule == null)
 				((FormText) getSWTWidget()).setText("", false, false);//$NON-NLS-1$
 			else
 				((FormText) getSWTWidget())
 						.setText(
 								rule.getValue() != null ? rule.getValue() : "", true, true); //$NON-NLS-1$
 			revalidate();
 		} else
 			super.setText(rule);
 	}
 
 	@Override
 	protected void setImage(ImageRule rule) {
 		if (getSWTWidget() instanceof FormText)
 			return;
 		super.setImage(rule);
 	}
 
 	@Override
 	public void updateStyleRule(StyleRule rule) {
 		if (rule == null)
 			return;
 		if (getSWTWidget() instanceof FormText)
 			if (rule.getPropertyName() != null
 					&& rule.getPropertyName().length() > LabelEditPart._KEY_PREFIX_LENGHT
 					&& rule.getPropertyName().startsWith(
 							LabelEditPart._KEY_PREFIX)) {
 				String key = rule.getPropertyName().substring(
 						LabelEditPart._KEY_PREFIX_LENGHT);
 				if (rule instanceof ColorRule)
 					setXMLColor(key, (ColorRule) rule);
 				else if (rule instanceof FontRule)
 					setXMLFont(key, (FontRule) rule);
 				else if (rule instanceof ImageRule)
 					setXMLImage(key, (ImageRule) rule);
 			}
 		super.updateStyleRule(rule);
 	}
 
 	@Override
 	protected boolean needReCreateWidgetView(StyleRule rule, Widget widget) {
 		if (rule == null)
 			return false;
 		if (LabelEditPart.RENDER_XML.equals(rule.getPropertyName())) {
 			if (rule instanceof BooleanRule)
 				return !(((BooleanRule) rule).isValue() == (widget instanceof FormText));
 			else if (rule instanceof BlankRule)
 				return widget instanceof FormText;
 			else
 				return false;
 		}
 		return super.needReCreateWidgetView(rule, widget);
 	}
 
 	public void updateXMLStyles(HashMap<String, List<StyleRule>> xmlStyles) {
 		if (!(getSWTWidget() instanceof FormText))
 			return;
 		for (String key : xmlStyles.keySet()) {
 			for (StyleRule rule : xmlStyles.get(key))
 				if (rule instanceof ColorRule)
 					setXMLColor(key, (ColorRule) rule);
 				else if (rule instanceof FontRule)
 					setXMLFont(key, (FontRule) rule);
 				else if (rule instanceof ImageRule)
 					setXMLImage(key, (ImageRule) rule);
 		}
 	}
 
 	public void setXMLColor(String key, ColorRule colorRule) {
 		if (key == null || key.isEmpty())
 			return;
 		Color color = colors.get(key);
 		if (color != null && !color.isDisposed())
 			color.dispose();
 		RGB rgb = new RGB(colorRule.getRed(), colorRule.getGreen(),
 				colorRule.getBlue());
 		Color newColor = new Color(getSWTWidget().getDisplay(), rgb);
 		colors.put(key, newColor);
 		((FormText) getSWTWidget()).setColor(key, newColor);
 	}
 
 	public void removeXMLColor(String key) {
 		if (key == null || key.isEmpty())
 			return;
 		Color color = colors.get(key);
 		if (color != null && !color.isDisposed())
 			color.dispose();
 		((FormText) getSWTWidget()).setColor(key, null);
 	}
 
 	public void setXMLFont(String key, FontRule fontRule) {
 		if (key == null || key.isEmpty())
 			return;
 		Font font = fonts.get(key);
 		if (font != null && !font.isDisposed())
 			font.dispose();
 		FontData fontData = new FontData(
 				fontRule.getName() != null ? fontRule.getName()
 						: getSWTWidget().getDisplay().getSystemFont()
 								.getFontData()[0].getName(),
 				fontRule.getHeight() != 0 ? fontRule.getHeight()
 						: getSWTWidget().getDisplay().getSystemFont()
 								.getFontData()[0].getHeight(),
 				(fontRule.isBold() ? SWT.BOLD : 0)
 						| (fontRule.isItalic() ? SWT.ITALIC : 0));
 		Font newFont = new Font(getSWTWidget().getDisplay(), fontData);
 		fonts.put(key, newFont);
 		((FormText) getSWTWidget()).setFont(key, newFont);
 	}
 
 	public void removeXMLFont(String key) {
 		if (key == null || key.isEmpty())
 			return;
 		Font font = fonts.get(key);
 		if (font != null && !font.isDisposed())
 			font.dispose();
 		((FormText) getSWTWidget()).setFont(key, null);
 	}
 
 	public void setXMLImage(String key, ImageRule imageRule) {
 		if (key == null || key.isEmpty())
 			return;
 		Image image = images.get(key);
 		if (image != null && !image.isDisposed())
 			image.dispose();
 		Image newImage = ImageRuleManager.convertToPlatformSpecificObject(this,
 				imageRule);
		images.put(key, newImage);
		((FormText) getSWTWidget()).setImage(key, newImage);
 	}
 
 	public void removeXMLImage(String key) {
 		if (key == null || key.isEmpty())
 			return;
 		Image image = images.get(key);
 		if (image != null && !image.isDisposed())
 			image.dispose();
		((FormText) getSWTWidget()).setImage(key, null);
 	}
 
 	@Override
 	protected void widgetDisposed() {
 		for (Color color : colors.values())
 			if (color != null && !color.isDisposed())
 				color.dispose();
 		for (Font font : fonts.values())
 			if (font != null && !font.isDisposed())
 				font.dispose();
 		for (Image image : images.values())
 			if (image != null && !image.isDisposed())
 				image.dispose();
 		super.widgetDisposed();
 	}
 }
