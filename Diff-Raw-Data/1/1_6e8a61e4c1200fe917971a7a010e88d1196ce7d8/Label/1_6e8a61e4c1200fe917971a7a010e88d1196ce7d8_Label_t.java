 package com.eagerlogic.cubee.client.components;
 
 import com.eagerlogic.cubee.client.styles.Color;
 import com.eagerlogic.cubee.client.styles.EVAlign;
 import com.eagerlogic.cubee.client.styles.ETextOverflow;
 import com.eagerlogic.cubee.client.styles.ETextAlign;
 import com.eagerlogic.cubee.client.styles.FontFamily;
 import com.eagerlogic.cubee.client.properties.BorderProperty;
 import com.eagerlogic.cubee.client.properties.BooleanProperty;
 import com.eagerlogic.cubee.client.properties.ColorProperty;
 import com.eagerlogic.cubee.client.properties.PaddingProperty;
 import com.eagerlogic.cubee.client.properties.IChangeListener;
 import com.eagerlogic.cubee.client.properties.IntegerProperty;
 import com.eagerlogic.cubee.client.properties.Property;
 import com.eagerlogic.cubee.client.properties.StringProperty;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.user.client.DOM;
 
 /**
  *
  * @author dipacs
  */
 public final class Label extends AComponent {
 	
 	private final IntegerProperty width = new IntegerProperty(null, true, false);
 	private final IntegerProperty height = new IntegerProperty(null, true, false);
 	private final StringProperty text = new StringProperty("", false, false);
 	private final Property<ETextOverflow> textOverFlow = new Property<ETextOverflow>(ETextOverflow.CLIP, false, false);
 	private final ColorProperty foreColor = new ColorProperty(Color.BLACK, true, false);
 	private final Property<ETextAlign> textAlign = new Property<ETextAlign>(ETextAlign.LEFT, false, false);
 	private final Property<EVAlign> verticalAlign = new Property<EVAlign>(EVAlign.TOP, false, false);
 	private final BooleanProperty bold = new BooleanProperty(false, false, false);
 	private final BooleanProperty italic = new BooleanProperty(false, false, false);
 	private final BooleanProperty underline = new BooleanProperty(false, false, false);
 	private final IntegerProperty fontSize = new IntegerProperty(12, false, false);
 	private final Property<FontFamily> fontFamily = new Property<FontFamily>(FontFamily.Arial, false, false);
 	
 	public Label() {
 		super(DOM.createDiv());
 		width.addChangeListener(new IChangeListener() {
 			@Override
 			public void onChanged(Object sender) {
 				if (width.get() == null) {
 					getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
 					getElement().getStyle().setOverflowX(Style.Overflow.VISIBLE);
					getElement().getStyle().clearWidth();
 				} else {
 					getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NORMAL);
 					getElement().getStyle().setWidth(width.get(), Style.Unit.PX);
 					getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);
 				}
 				requestLayout();
 			}
 		});
 		width.invalidate();
 		height.addChangeListener(new IChangeListener() {
 			@Override
 			public void onChanged(Object sender) {
 				if (height.get() == null) {
 					getElement().getStyle().clearHeight();
 					getElement().getStyle().setOverflowY(Style.Overflow.VISIBLE);
 				} else {
 					getElement().getStyle().setHeight(height.get(), Style.Unit.PX);
 					getElement().getStyle().setOverflowY(Style.Overflow.HIDDEN);
 				}
 				requestLayout();
 			}
 		});
 		height.invalidate();
 		text.addChangeListener(new IChangeListener() {
 
 			@Override
 			public void onChanged(Object sender) {
 				getElement().setInnerHTML(text.get());
 				requestLayout();
 			}
 		});
 		text.invalidate();
 		textOverFlow.addChangeListener(new IChangeListener() {
 
 			@Override
 			public void onChanged(Object sender) {
 				textOverFlow.get().apply(getElement());
 				requestLayout();
 			}
 		});
 		textOverFlow.invalidate();
 		foreColor.addChangeListener(new IChangeListener() {
 
 			@Override
 			public void onChanged(Object sender) {
 				if (foreColor.get() == null) {
 					getElement().getStyle().setColor("rgba(0, 0, 0, 0.0)");
 				} else {
 					getElement().getStyle().setColor(foreColor.get().toCSS());
 				}
 			}
 		});
 		foreColor.invalidate();
 		textAlign.addChangeListener(new IChangeListener() {
 
 			@Override
 			public void onChanged(Object sender) {
 				textAlign.get().apply(getElement());
 			}
 		});
 		textAlign.invalidate();
 		verticalAlign.addChangeListener(new IChangeListener() {
 
 			@Override
 			public void onChanged(Object sender) {
 				EVAlign ta = verticalAlign.get();
 				if (ta == EVAlign.TOP) {
 					getElement().getStyle().setVerticalAlign(Style.VerticalAlign.TOP);
 				} else if (ta == EVAlign.MIDDLE) {
 					getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);
 				} else if (ta == EVAlign.BOTTOM) {
 					getElement().getStyle().setVerticalAlign(Style.VerticalAlign.BOTTOM);
 				}
 			}
 		});
 		verticalAlign.invalidate();
 		underline.addChangeListener(new IChangeListener() {
 
 			@Override
 			public void onChanged(Object sender) {
 				if (underline.get()) {
 					getElement().getStyle().setTextDecoration(Style.TextDecoration.UNDERLINE);
 				} else {
 					getElement().getStyle().setTextDecoration(Style.TextDecoration.NONE);
 				}
 				requestLayout();
 			}
 		});
 		underline.invalidate();
 		bold.addChangeListener(new IChangeListener() {
 
 			@Override
 			public void onChanged(Object sender) {
 				if (bold.get()) {
 					getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
 				} else {
 					getElement().getStyle().setFontWeight(Style.FontWeight.NORMAL);
 				}
 				requestLayout();
 			}
 		});
 		bold.invalidate();
 		italic.addChangeListener(new IChangeListener() {
 
 			@Override
 			public void onChanged(Object sender) {
 				if (italic.get()) {
 					getElement().getStyle().setFontStyle(Style.FontStyle.ITALIC);
 				} else {
 					getElement().getStyle().setFontStyle(Style.FontStyle.NORMAL);
 				}
 				requestLayout();
 			}
 		});
 		italic.invalidate();
 		fontSize.addChangeListener(new IChangeListener() {
 
 			@Override
 			public void onChanged(Object sender) {
 				getElement().getStyle().setFontSize(fontSize.get(), Style.Unit.PX);
 				requestLayout();
 			}
 		});
 		fontSize.invalidate();
 		fontFamily.addChangeListener(new IChangeListener() {
 
 			@Override
 			public void onChanged(Object sender) {
 				fontFamily.get().apply(getElement());
 				requestLayout();
 			}
 		});
 		fontFamily.invalidate();
 	}
 
 	public final IntegerProperty widthProperty() {
 		return width;
 	}
 
 	public final IntegerProperty heightProperty() {
 		return height;
 	}
 
 	public final StringProperty textProperty() {
 		return text;
 	}
 
 	public final Property<ETextOverflow> textOverflowProperty() {
 		return textOverFlow;
 	}
 
 	@Override
 	public PaddingProperty paddingProperty() {
 		return super.paddingProperty();
 	}
 
 	@Override
 	public BorderProperty borderProperty() {
 		return super.borderProperty();
 	}
 
 	public ColorProperty foreColorProperty() {
 		return foreColor;
 	}
 
 	public Property<EVAlign> verticalAlignProperty() {
 		return verticalAlign;
 	}
 
 	public BooleanProperty boldProperty() {
 		return bold;
 	}
 
 	public BooleanProperty italicProperty() {
 		return italic;
 	}
 
 	public BooleanProperty underlineProperty() {
 		return underline;
 	}
 
 	public Property<ETextAlign> textAlignProperty() {
 		return textAlign;
 	}
 
 	public IntegerProperty fontSizeProperty() {
 		return fontSize;
 	}
 
 	public Property<FontFamily> fontFamilyProperty() {
 		return fontFamily;
 	}
 
 	@Override
 	public BooleanProperty selectableProperty() {
 		return super.selectableProperty();
 	}
 
 	@Override
 	public final IntegerProperty minWidthProperty() {
 		return super.minWidthProperty();
 	}
 
 	@Override
 	public final IntegerProperty minHeightProperty() {
 		return super.minHeightProperty();
 	}
 
 	@Override
 	public final IntegerProperty maxWidthProperty() {
 		return super.maxWidthProperty();
 	}
 
 	@Override
 	public final IntegerProperty maxHeightProperty() {
 		return super.maxHeightProperty();
 	}
 
 }
