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
 
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.jface.fieldassist.FieldDecoration;
 import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.CoolBar;
 import org.eclipse.swt.widgets.CoolItem;
 import org.eclipse.swt.widgets.ExpandBar;
 import org.eclipse.swt.widgets.ExpandItem;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.ProgressBar;
 import org.eclipse.swt.widgets.Scale;
 import org.eclipse.swt.widgets.Slider;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.wazaabi.engine.core.CoreSingletons;
 import org.eclipse.wazaabi.engine.core.editparts.AbstractComponentEditPart;
 import org.eclipse.wazaabi.engine.core.editparts.ContainerEditPart;
 import org.eclipse.wazaabi.engine.core.views.AbstractComponentView;
 import org.eclipse.wazaabi.engine.core.views.WidgetView;
 import org.eclipse.wazaabi.engine.swt.editparts.stylerules.managers.ImageRuleManager;
 import org.eclipse.wazaabi.engine.swt.viewers.AbstractSWTViewer;
 import org.eclipse.wazaabi.mm.core.Direction;
 import org.eclipse.wazaabi.mm.core.styles.BlankRule;
 import org.eclipse.wazaabi.mm.core.styles.BooleanRule;
 import org.eclipse.wazaabi.mm.core.styles.ColorRule;
 import org.eclipse.wazaabi.mm.core.styles.CoreStylesPackage;
 import org.eclipse.wazaabi.mm.core.styles.DirectionRule;
 import org.eclipse.wazaabi.mm.core.styles.ExpandRule;
 import org.eclipse.wazaabi.mm.core.styles.FontRule;
 import org.eclipse.wazaabi.mm.core.styles.SashRule;
 import org.eclipse.wazaabi.mm.core.styles.StringRule;
 import org.eclipse.wazaabi.mm.core.styles.StyleRule;
 import org.eclipse.wazaabi.mm.core.styles.StyledElement;
 import org.eclipse.wazaabi.mm.core.styles.TabRule;
 import org.eclipse.wazaabi.mm.core.styles.TabbedLayoutRule;
 import org.eclipse.wazaabi.mm.core.widgets.AbstractComponent;
 import org.eclipse.wazaabi.mm.core.widgets.Container;
 import org.eclipse.wazaabi.mm.core.widgets.Spinner;
 
 public abstract class SWTControlView extends SWTWidgetView implements
 		AbstractComponentView {
 
 	private Color backgroundColor = null;
 
 	private Font font = null;
 
 	private Color foregroundColor = null;
 
 	private boolean valid = false;
 
 	public void addNotify() {
 		assert getHost() != null;
 		if (getSWTWidget() != null && !getSWTWidget().isDisposed())
 			getSWTWidget().setData(WAZAABI_HOST_KEY, getHost());
 	}
 
 	protected abstract Widget createSWTWidget(Widget parent, int swtStyle,
 			int index);
 
 	protected Control checkParentLayout(Composite parent, Control widget) {
 		if (parent instanceof ToolBar) {
 			ToolItem sep = new ToolItem((ToolBar) parent, SWT.SEPARATOR);
 			sep.setControl(widget);
 			if (widget instanceof ProgressBar || widget instanceof Scale
 					|| widget instanceof Slider || widget instanceof Spinner) {
 				Point size = ((Control) widget).computeSize(SWT.DEFAULT,
 						SWT.DEFAULT);
 				sep.setWidth(size.x);
 			}
 		} else if (parent instanceof CoolBar) {
 			CoolItem sep = new CoolItem((CoolBar) parent, SWT.SEPARATOR);
 			sep.setControl(widget);
 			if (widget instanceof ProgressBar || widget instanceof Scale
 					|| widget instanceof Slider || widget instanceof Spinner) {
 				Point size = ((Control) widget).computeSize(SWT.DEFAULT,
 						SWT.DEFAULT);
 				sep.setPreferredSize(sep.computeSize(size.x, size.y));
 			}
 		} else if (parent instanceof CTabFolder) {
 			CTabItem item = new CTabItem((CTabFolder) parent,
 					computeSWTCreationStyle(getHost()));
 			item.setControl(widget);
 		} else if (parent instanceof ExpandBar) {
 			ExpandItem item = new ExpandItem((ExpandBar) parent,
 					computeSWTCreationStyle(getHost()));
 			item.setControl(widget);
 		}
 		return widget;
 	}
 
 	protected int computeSWTCreationStyle(StyleRule rule) {
 		final String propertyName = rule.getPropertyName();
 		if (rule instanceof DirectionRule
 				&& AbstractComponentEditPart.DIRECTION_PROPERTY_NAME
 						.equals(propertyName))
 			if (((DirectionRule) rule).getValue() == Direction.LEFT_TO_RIGHT)
 				return SWT.LEFT_TO_RIGHT;
 			else
 				return SWT.RIGHT_TO_LEFT;
 		// we catch the border rule since apparently this SWT widget does not
 		// manage it
 		else if (AbstractComponentEditPart.BORDER_PROPERTY_NAME
 				.equals(propertyName) && ((BooleanRule) rule).isValue())
 			return SWT.BORDER;
 		else if (AbstractComponentEditPart.LAYOUT_DATA_PROPERTY_NAME
 				.equals(rule.getPropertyName()) && rule instanceof TabRule) {
 			if (((TabRule) rule).isClosable()) {
 				return SWT.CLOSE;
 			}
 		}
 		return super.computeSWTCreationStyle(rule);
 	}
 
 	protected final org.eclipse.swt.widgets.Control getSWTControl() {
 		org.eclipse.swt.widgets.Widget swtWidget = getSWTWidget();
 		return (org.eclipse.swt.widgets.Control) swtWidget;
 	}
 
 	final org.eclipse.swt.widgets.Item getSWTItem() {
 		org.eclipse.swt.widgets.Widget swtWidget = getSWTWidget();
 		if (getSWTControl().getParent() instanceof CTabFolder) {
 			CTabFolder folder = (CTabFolder) getSWTControl().getParent();
 			for (CTabItem item : folder.getItems()) {
 				if (item.getControl() == swtWidget)
 					return item;
 			}
 		} else if (getSWTControl().getParent() instanceof ToolBar) {
 			ToolBar bar = (ToolBar) getSWTControl().getParent();
 			for (ToolItem item : bar.getItems()) {
 				if (item.getControl() == swtWidget)
 					return item;
 			}
 		} else if (getSWTControl().getParent() instanceof CoolBar) {
 			CoolBar bar = (CoolBar) getSWTControl().getParent();
 			for (CoolItem item : bar.getItems()) {
 				if (item.getControl() == swtWidget)
 					return item;
 			}
 		} else if (getSWTControl().getParent() instanceof ExpandBar) {
 			ExpandBar bar = (ExpandBar) getSWTControl().getParent();
 			for (ExpandItem item : bar.getItems()) {
 				if (item.getControl() == swtWidget)
 					return item;
 			}
 		}
 		return null;
 	}
 
 	protected boolean isValidationRoot() {
 		return false;
 	}
 
 	public UpdateManager getUpdateManager() {
 		return ((AbstractSWTViewer) getHost().getViewer()).getUpdateManager();
 	}
 
 	public void revalidate() {
 		invalidate();
 		if (getParent() == null || isValidationRoot())
 			getUpdateManager().addInvalidFigure(this);
 		else
 			getParent().revalidate();
 	}
 
 	public void validate() {
 		// System.out.print("++++ validating "
 		// + getSWTWidget().getClass().getSimpleName() + "[");
 		//
 		// if (getSWTWidget() instanceof Button)
 		// System.out.print(((Button) getSWTWidget()).getText());
 		// else if (getSWTWidget() instanceof Text)
 		// System.out.print(((Text) getSWTWidget()).getText());
 		// System.out.println("], (" + System.identityHashCode(this) + ")");
 
 		if (isValid())
 			return;
 		setValid(true);
 
 		if (getSWTWidget() instanceof Composite) {
 			final Composite composite = ((Composite) getSWTWidget());
 			if (composite.isDisposed())
 				return;
 			org.eclipse.swt.widgets.Control[] children = composite
 					.getChildren();
 
 			composite.layout();
 
 			for (int i = 0; i < children.length; i++)
 				if (children[i].getData(WAZAABI_HOST_KEY) instanceof AbstractComponentEditPart)
 					((WidgetView) ((AbstractComponentEditPart) children[i]
 							.getData(WAZAABI_HOST_KEY)).getWidgetView())
 							.validate();
 		}
 		refreshWidgetAfterCreation();
 		fireWidgetViewValidated();
 	}
 
 	public void refreshWidgetAfterCreation() {
 		if (getHost() != null
 				&& getHost().getParent() != null
 				&& ((ContainerEditPart) getHost().getParent()).getModel() != null) {
 			if (getSWTControl().getParent() instanceof ExpandBar) {
 				ExpandBar bar = (ExpandBar) getSWTControl().getParent();
 				for (ExpandItem tab : bar.getItems()) {
 					if (tab.getControl() == getSWTControl()) {
 						tab.setHeight(getSWTControl().computeSize(SWT.DEFAULT,
 								SWT.DEFAULT).y);
 						break;
 					}
 				}
 			} else if (getSWTControl().getParent() instanceof CTabFolder) {
 				StyleRule parentRule = ((Container) ((ContainerEditPart) getHost()
 						.getParent()).getModel()).getFirstStyleRule(
 						ContainerEditPart.LAYOUT_PROPERTY_NAME,
 						CoreStylesPackage.Literals.TABBED_LAYOUT_RULE);
				int selectedTab = ((TabbedLayoutRule) parentRule).getTop();
				if (selectedTab > 0
 						&& ((CTabFolder) getSWTControl().getParent())
								.getItems().length > selectedTab
 						&& ((CTabFolder) getSWTControl().getParent())
								.getItems()[selectedTab] == getSWTItem())
 					((CTabFolder) getSWTControl().getParent())
 							.setSelection((CTabItem) getSWTItem());
 			}
 		}
 	}
 
 	public void invalidate() {
 		// if (layoutManager != null)
 		// layoutManager.invalidate();
 		setValid(false);
 	}
 
 	public void setValid(boolean value) {
 		valid = value;
 	}
 
 	protected boolean isValid() {
 		return valid;
 	}
 
 	@Override
 	public boolean needReCreateWidgetView(StyleRule styleRule) {
 		if (styleRule == null)
 			return false;
 		org.eclipse.swt.widgets.Widget widget = getSWTWidget();
 		if (AbstractComponentEditPart.BORDER_PROPERTY_NAME.equals(styleRule
 				.getPropertyName()) && styleRule instanceof BooleanRule) {
 			return !(isStyleBitCorrectlySet(widget, org.eclipse.swt.SWT.BORDER,
 					((BooleanRule) styleRule).isValue()));
 		} else if (AbstractComponentEditPart.DIRECTION_PROPERTY_NAME
 				.equals(styleRule.getPropertyName())
 				&& styleRule instanceof DirectionRule) {
 			return !(isStyleBitCorrectlySet(widget,
 					org.eclipse.swt.SWT.LEFT_TO_RIGHT,
 					Direction.LEFT_TO_RIGHT == ((DirectionRule) styleRule)
 							.getValue()) & isStyleBitCorrectlySet(widget,
 					org.eclipse.swt.SWT.RIGHT_TO_LEFT,
 					Direction.RIGHT_TO_LEFT == ((DirectionRule) styleRule)
 							.getValue()));
 		} else
 			return super.needReCreateWidgetView(styleRule);
 	}
 
 	public void removeNotify() {
 	}
 
 	protected void setBackgroundColor(ColorRule colorRule) {
 
 		org.eclipse.swt.graphics.RGB oldRGBValue = null;
 		org.eclipse.swt.graphics.RGB newRGBValue = null;
 
 		if (backgroundColor != null)
 			oldRGBValue = backgroundColor.getRGB();
 		if (colorRule != null)
 			newRGBValue = new org.eclipse.swt.graphics.RGB(colorRule.getRed(),
 					colorRule.getGreen(), colorRule.getBlue());
 
 		if (oldRGBValue == null && newRGBValue == null)
 			return;
 		if (oldRGBValue != null && oldRGBValue.equals(newRGBValue))
 			return;
 
 		if (backgroundColor != null && !backgroundColor.isDisposed())
 			backgroundColor.dispose();
 
 		if (colorRule == null)
 			backgroundColor = null;
 		else
 			backgroundColor = new org.eclipse.swt.graphics.Color(
 					getSWTControl().getDisplay(), newRGBValue);
 		getSWTControl().setBackground(backgroundColor);
 	}
 
 	public void setFont(FontRule fontRule) {
 		// System.out.println("setFont " + fontRule);
 		if (font == null && fontRule == null)
 			return;
 
 		org.eclipse.swt.graphics.FontData oldFontData = null;
 
 		if (getSWTControl().getFont() != null
 				&& !getSWTControl().getFont().isDisposed())
 			oldFontData = getSWTControl().getFont().getFontData()[0];
 		else
 			oldFontData = getSWTControl().getDisplay().getSystemFont()
 					.getFontData()[0];
 
 		if (fontRule == null) {
 			if (font != null && !font.isDisposed())
 				font.dispose();
 			font = null;
 			getSWTControl().setFont(null);
 			revalidate();
 			return;
 		}
 
 		String oldFontName = oldFontData.getName();
 		int oldFontHeight = oldFontData.getHeight();
 		boolean isOldFontItalic = (oldFontData.getStyle() & org.eclipse.swt.SWT.ITALIC) != 0;
 		boolean isOldFontBold = (oldFontData.getStyle() & org.eclipse.swt.SWT.BOLD) != 0;
 
 		String newFontName = oldFontName;
 		int newFontHeight = oldFontHeight;
 		boolean isNewFontItalic = isOldFontItalic;
 		boolean isNewFontBold = isOldFontBold;
 
 		if (fontRule.isSetName() && fontRule.getName() != null)
 			newFontName = fontRule.getName();
 		if (fontRule.isSetHeight() && fontRule.getHeight() != 0)
 			newFontHeight = fontRule.getHeight();
 		if (fontRule.isSetItalic())
 			isNewFontItalic = fontRule.isItalic();
 		if (fontRule.isSetBold())
 			isNewFontBold = fontRule.isBold();
 
 		// if nothing has changed, then simply return
 		if (oldFontName.equals(newFontName) && oldFontHeight == newFontHeight
 				&& isOldFontItalic == isNewFontItalic
 				&& isOldFontBold == isNewFontBold)
 			return;
 
 		int newFontStyle = 0;
 		if (isNewFontItalic)
 			newFontStyle |= org.eclipse.swt.SWT.ITALIC;
 		if (isNewFontBold)
 			newFontStyle |= org.eclipse.swt.SWT.BOLD;
 
 		org.eclipse.swt.graphics.FontData newFontData = new FontData(
 				newFontName, newFontHeight, newFontStyle);
 
 		if (font != null && !font.isDisposed())
 			font.dispose();
 		font = new org.eclipse.swt.graphics.Font(getSWTControl().getDisplay(),
 				newFontData);
 
 		// TODO : what do we do if the fontData does not drive to a correct Font
 		// ??
 		((org.eclipse.swt.widgets.Control) getSWTControl()).setFont(font);
 		revalidate();
 	}
 
 	protected void setForegroundColor(ColorRule colorRule) {
 
 		org.eclipse.swt.graphics.RGB oldRGBValue = null;
 		org.eclipse.swt.graphics.RGB newRGBValue = null;
 
 		if (foregroundColor != null)
 			oldRGBValue = foregroundColor.getRGB();
 		if (colorRule != null)
 			newRGBValue = new org.eclipse.swt.graphics.RGB(colorRule.getRed(),
 					colorRule.getGreen(), colorRule.getBlue());
 
 		if (oldRGBValue == null && newRGBValue == null)
 			return;
 		if (oldRGBValue != null && oldRGBValue.equals(newRGBValue))
 			return;
 
 		if (foregroundColor != null && !foregroundColor.isDisposed())
 			foregroundColor.dispose();
 
 		if (colorRule == null)
 			foregroundColor = null;
 		else
 			foregroundColor = new org.eclipse.swt.graphics.Color(
 					getSWTControl().getDisplay(), newRGBValue);
 		getSWTControl().setForeground(foregroundColor);
 	}
 
 	protected void setTooltip(StringRule rule) {
 		if (rule != null)
 			((org.eclipse.swt.widgets.Control) getSWTControl())
 					.setToolTipText(rule.getValue());
 	}
 
 	private ControlDecoration controlDecoration = null;
 
 	protected ControlDecoration getControlDecoration() {
 		if (controlDecoration == null) {
 			controlDecoration = new ControlDecoration(getSWTControl(),
 					SWT.RIGHT | SWT.TOP);
 			FieldDecoration fieldDecoration = FieldDecorationRegistry
 					.getDefault().getFieldDecoration(
 							FieldDecorationRegistry.DEC_ERROR);
 			controlDecoration.setImage(fieldDecoration.getImage());
 		}
 		return controlDecoration;
 	}
 
 	protected void updateControlDecoration(String errorMessage) {
 		if (controlDecoration == null)
 			controlDecoration = getControlDecoration();
 		if (errorMessage != null)
 			controlDecoration.setDescriptionText(errorMessage);
 		else
 			controlDecoration.setDescriptionText(""); //$NON-NLS-1$
 	}
 
 	protected void disposeControlDecoration() {
 		if (controlDecoration != null) {
 			controlDecoration.hide();
 			controlDecoration.dispose();
 		}
 		controlDecoration = null;
 	}
 
 	protected void setError(StringRule rule) {
 		if (rule == null)
 			disposeControlDecoration();
 		else
 			updateControlDecoration(rule.getValue());
 	}
 
 	@Override
 	public void updateStyleRule(StyleRule rule) {
 		if (rule == null)
 			return;
 		if (AbstractComponentEditPart.ENABLED_PROPERTY_NAME.equals(rule
 				.getPropertyName())) {
 			if (rule instanceof BooleanRule)
 				setEnabled((BooleanRule) rule);
 			else
 				setBackgroundColor(null);
 		} else if (AbstractComponentEditPart.BACKGROUND_COLOR_PROPERTY_NAME
 				.equals(rule.getPropertyName())) {
 			if (rule instanceof ColorRule)
 				setBackgroundColor((ColorRule) rule);
 			else
 				setBackgroundColor(null);
 		} else if (AbstractComponentEditPart.FOREGROUND_COLOR_PROPERTY_NAME
 				.equals(rule.getPropertyName())) {
 			if (rule instanceof ColorRule)
 				setForegroundColor((ColorRule) rule);
 			else
 				setForegroundColor(null);
 		} else if (AbstractComponentEditPart.FONT_PROPERTY_NAME.equals(rule
 				.getPropertyName())) {
 			if (rule instanceof FontRule)
 				setFont((FontRule) rule);
 			else
 				setFont(null);
 		} else if (AbstractComponentEditPart.TOOLTIP_TEXT_PROPERTY_NAME
 				.equals(rule.getPropertyName())) {
 			if (rule instanceof StringRule)
 				setTooltip((StringRule) rule);
 			else
 				setTooltip(null);
 		} else if (AbstractComponentEditPart.ERROR_TEXT_PROPERTY_NAME
 				.equals(rule.getPropertyName())) {
 			if (rule instanceof StringRule)
 				setError((StringRule) rule);
 			else
 				setError(null);
 		} else if (AbstractComponentEditPart.LAYOUT_DATA_PROPERTY_NAME
 				.equals(rule.getPropertyName())) {
 			if (rule instanceof BlankRule) {
 				getSWTControl().setLayoutData(null);
 				CoreSingletons
 						.getComposedStyleRuleManagerFactory()
 						.platformSpecificRefresh(
 								getParent(),
 								((StyledElement) getParent().getHost()
 										.getModel())
 										.getFirstStyleRule(
 												ContainerEditPart.LAYOUT_PROPERTY_NAME,
 												CoreStylesPackage.Literals.LAYOUT_DATA_RULE));
 			} else if (rule instanceof TabRule) {
 				setTabDecoration((TabRule) rule);
 			} else if (rule instanceof ExpandRule) {
 				setExpandDecoration((ExpandRule) rule);
 			} else if (rule instanceof SashRule) {
 				setSashDecoration((SashRule) rule);
 			} else
 				CoreSingletons.getComposedStyleRuleManagerFactory()
 						.platformSpecificRefresh(this, rule);
 
 			revalidate();
 		} else
 			super.updateStyleRule(rule);
 	}
 
 	public void processPostControlCreation() {
 
 	}
 
 	private void setExpandDecoration(ExpandRule rule) {
 		org.eclipse.swt.widgets.Control control = getSWTControl();
 		if (getSWTControl().getParent() instanceof ExpandBar) {
 			ExpandBar bar = (ExpandBar) getSWTControl().getParent();
 			for (ExpandItem tab : bar.getItems()) {
 				if (tab.getControl() == control) {
 					tab.setText(rule.getLabel());
 					tab.setExpanded(rule.isExpanded());
 					setImageOnItem(tab, rule.getImage());
 					break;
 				}
 			}
 		}
 	}
 
 	protected void setTabDecoration(TabRule rule) {
 		org.eclipse.swt.widgets.Control control = getSWTControl();
 		if (getSWTControl().getParent() instanceof CTabFolder) {
 			CTabFolder folder = (CTabFolder) getSWTControl().getParent();
 			for (CTabItem tab : folder.getItems()) {
 				if (tab.getControl() == control) {
 					tab.setText(rule.getLabel());
 					setImageOnItem(tab, rule.getImage());
 					break;
 				}
 			}
 		}
 	}
 
 	protected void setSashDecoration(SashRule rule) {
 		org.eclipse.swt.widgets.Control control = getSWTControl();
 		if (control.getParent() instanceof SashForm) {
 
 			int minWeight = 0;
 			int[] weights = new int[control.getParent().getChildren().length];
 			for (AbstractComponent sibling : ((Container) ((AbstractComponent) getHost()
 					.getModel()).eContainer()).getChildren()) {
 				SashRule sRule = (SashRule) sibling.getFirstStyleRule(
 						AbstractComponentEditPart.LAYOUT_DATA_PROPERTY_NAME,
 						CoreStylesPackage.Literals.SASH_RULE);
 				if (sRule != null) {
 					if (minWeight == 0 || minWeight > sRule.getWeight())
 						minWeight = sRule.getWeight();
 				}
 			}
 			for (int j = 0; j < control.getParent().getChildren().length; j++) {
 				AbstractComponent sibling = ((Container) ((AbstractComponent) getHost()
 						.getModel()).eContainer()).getChildren().get(j);
 				SashRule sRule = (SashRule) sibling.getFirstStyleRule(
 						AbstractComponentEditPart.LAYOUT_DATA_PROPERTY_NAME,
 						CoreStylesPackage.Literals.SASH_RULE);
 				if (sRule == null) {
 					weights[j] = minWeight;
 				} else {
 					weights[j] = sRule.getWeight();
 				}
 			}
 
 			SashForm form = (SashForm) getSWTControl().getParent();
 			form.setWeights(weights);
 		}
 	}
 
 	protected void setImageOnItem(Item item, String imageName) {
 		Image image = item.getImage();
 		if (!("".equals(imageName))) {
 			Image newImage = ImageRuleManager.convertToPlatformSpecificObject(
 					this, imageName);
 			if (image == null || !image.equals(newImage)) {
 				item.setImage(newImage);
 				getSWTControl().update();
 
 				System.out.println("setImage " + newImage);
 				if (image != null) {
 					System.out.println("disposing image from "
 							+ System.identityHashCode(this));
 					image.dispose();
 				}
 				revalidate();
 			}
 		} else if (image != null) {
 			image.dispose();
 			item.setImage(null);
 		}
 
 	}
 
 	protected void widgetDisposed() {
 		super.widgetDisposed();
 		if (backgroundColor != null && !backgroundColor.isDisposed()) {
 			backgroundColor.dispose();
 			System.out.println("background color disposed");
 		}
 		if (foregroundColor != null && !foregroundColor.isDisposed()) {
 			foregroundColor.dispose();
 			System.out.println("foreground color disposed");
 		}
 		if (font != null && !font.isDisposed()) {
 			font.dispose();
 			System.out.println("font disposed");
 		}
 	}
 
 	protected void setEnabled(BooleanRule rule) {
 		getSWTControl().setEnabled(rule.isValue());
 	}
 }
