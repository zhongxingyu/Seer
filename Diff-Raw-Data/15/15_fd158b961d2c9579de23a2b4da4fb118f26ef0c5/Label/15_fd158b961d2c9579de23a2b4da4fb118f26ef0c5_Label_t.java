 package com.eagerlogic.cubee.client.components;
 
 import com.eagerlogic.cubee.client.properties.BooleanProperty;
 import com.eagerlogic.cubee.client.properties.BorderProperty;
 import com.eagerlogic.cubee.client.properties.ColorProperty;
 import com.eagerlogic.cubee.client.properties.IChangeListener;
 import com.eagerlogic.cubee.client.properties.IntegerProperty;
 import com.eagerlogic.cubee.client.properties.PaddingProperty;
 import com.eagerlogic.cubee.client.properties.Property;
 import com.eagerlogic.cubee.client.properties.StringProperty;
 import com.eagerlogic.cubee.client.style.Style;
 import com.eagerlogic.cubee.client.style.styles.Border;
 import com.eagerlogic.cubee.client.style.styles.Color;
 import com.eagerlogic.cubee.client.style.styles.ETextAlign;
 import com.eagerlogic.cubee.client.style.styles.ETextOverflow;
 import com.eagerlogic.cubee.client.style.styles.EVAlign;
 import com.eagerlogic.cubee.client.style.styles.FontFamily;
 import com.eagerlogic.cubee.client.style.styles.Padding;
 import com.google.gwt.user.client.DOM;
 
 /**
  *
  * @author dipacs
  */
 public final class Label extends AComponent {
 
     public static class StyleClass<T extends Label> extends AComponent.StyleClass<T> {
 
         private final Style<Integer> width = new Style<Integer>(null, true);
         private final Style<Integer> height = new Style<Integer>(null, true);
         private final Style<ETextOverflow> textOverflow = new Style<ETextOverflow>(null, false);
         private final Style<Color> foreColor = new Style<Color>(null, false);
         private final Style<ETextAlign> textAlign = new Style<ETextAlign>(null, false);
         private final Style<EVAlign> verticalAlign = new Style<EVAlign>(null, false);
         private final Style<Boolean> bold = new Style<Boolean>(null, false);
         private final Style<Boolean> italic = new Style<Boolean>(null, false);
         private final Style<Boolean> underline = new Style<Boolean>(null, false);
         private final Style<Integer> fontSize = new Style<Integer>(null, false);
         private final Style<FontFamily> fontFamily = new Style<FontFamily>(null, false);
 
         @Override
         public void apply(T component) {
             super.apply(component);
 
             width.apply(component.widthProperty());
             height.apply(component.heightProperty());
             textOverflow.apply(component.textOverflowProperty());
             foreColor.apply(component.foreColorProperty());
             textAlign.apply(component.textAlignProperty());
             verticalAlign.apply(component.verticalAlignProperty());
             bold.apply(component.boldProperty());
             italic.apply(component.italicProperty());
             underline.apply(component.underlineProperty());
             fontSize.apply(component.fontSizeProperty());
             fontFamily.apply(component.fontFamilyProperty());
         }
 
         @Override
         public Style<Integer> getMaxHeight() {
             return super.getMaxHeight();
         }
 
         @Override
         public Style<Integer> getMaxWidth() {
             return super.getMaxWidth();
         }
 
         @Override
         public Style<Integer> getMinHeight() {
             return super.getMinHeight();
         }
 
         @Override
         public Style<Integer> getMinWidth() {
             return super.getMinWidth();
         }
 
         @Override
         public Style<Border> getBorder() {
             return super.getBorder();
         }
 
         @Override
         public Style<Padding> getPadding() {
             return super.getPadding();
         }
 
         public Style<Integer> getWidth() {
             return width;
         }
 
         public Style<Integer> getHeight() {
             return height;
         }
 
         public Style<ETextOverflow> getTextOverflow() {
             return textOverflow;
         }
 
         public Style<Color> getForeColor() {
             return foreColor;
         }
 
         public Style<ETextAlign> getTextAlign() {
             return textAlign;
         }
 
         public Style<EVAlign> getVerticalAlign() {
             return verticalAlign;
         }
 
         public Style<Boolean> getBold() {
             return bold;
         }
 
         public Style<Boolean> getItalic() {
             return italic;
         }
 
         public Style<Boolean> getUnderline() {
             return underline;
         }
 
         public Style<Integer> getFontSize() {
             return fontSize;
         }
 
         public Style<FontFamily> getFontFamily() {
             return fontFamily;
         }
 
     }
 
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
                     getElement().getStyle().setWhiteSpace(com.google.gwt.dom.client.Style.WhiteSpace.NOWRAP);
                     getElement().getStyle().setOverflowX(com.google.gwt.dom.client.Style.Overflow.VISIBLE);
                     getElement().getStyle().clearWidth();
                 } else {
                     getElement().getStyle().setWhiteSpace(com.google.gwt.dom.client.Style.WhiteSpace.NORMAL);
                     getElement().getStyle().setWidth(width.get(), com.google.gwt.dom.client.Style.Unit.PX);
                     getElement().getStyle().setOverflowX(com.google.gwt.dom.client.Style.Overflow.HIDDEN);
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
                     getElement().getStyle().setOverflowY(com.google.gwt.dom.client.Style.Overflow.VISIBLE);
                 } else {
                     getElement().getStyle().setHeight(height.get(), com.google.gwt.dom.client.Style.Unit.PX);
                     getElement().getStyle().setOverflowY(com.google.gwt.dom.client.Style.Overflow.HIDDEN);
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
                if (textOverFlow.get() == ETextOverflow.ELLIPSIS) {
                	getElement().getStyle().setProperty("whiteSpace", "nowrap");
                	getElement().getStyle().setProperty("owerflow", "hidden");
                } else {
                	getElement().getStyle().clearProperty("whiteSpace");
                	width.invalidate();
                	height.invalidate();
                }
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
                     getElement().getStyle().setVerticalAlign(com.google.gwt.dom.client.Style.VerticalAlign.TOP);
                 } else if (ta == EVAlign.MIDDLE) {
                     getElement().getStyle().setVerticalAlign(com.google.gwt.dom.client.Style.VerticalAlign.MIDDLE);
                 } else if (ta == EVAlign.BOTTOM) {
                     getElement().getStyle().setVerticalAlign(com.google.gwt.dom.client.Style.VerticalAlign.BOTTOM);
                 }
             }
         });
         verticalAlign.invalidate();
         underline.addChangeListener(new IChangeListener() {
 
             @Override
             public void onChanged(Object sender) {
                 if (underline.get()) {
                     getElement().getStyle().setTextDecoration(com.google.gwt.dom.client.Style.TextDecoration.UNDERLINE);
                 } else {
                     getElement().getStyle().setTextDecoration(com.google.gwt.dom.client.Style.TextDecoration.NONE);
                 }
                 requestLayout();
             }
         });
         underline.invalidate();
         bold.addChangeListener(new IChangeListener() {
 
             @Override
             public void onChanged(Object sender) {
                 if (bold.get()) {
                     getElement().getStyle().setFontWeight(com.google.gwt.dom.client.Style.FontWeight.BOLD);
                 } else {
                     getElement().getStyle().setFontWeight(com.google.gwt.dom.client.Style.FontWeight.NORMAL);
                 }
                 requestLayout();
             }
         });
         bold.invalidate();
         italic.addChangeListener(new IChangeListener() {
 
             @Override
             public void onChanged(Object sender) {
                 if (italic.get()) {
                     getElement().getStyle().setFontStyle(com.google.gwt.dom.client.Style.FontStyle.ITALIC);
                 } else {
                     getElement().getStyle().setFontStyle(com.google.gwt.dom.client.Style.FontStyle.NORMAL);
                 }
                 requestLayout();
             }
         });
         italic.invalidate();
         fontSize.addChangeListener(new IChangeListener() {
 
             @Override
             public void onChanged(Object sender) {
                 getElement().getStyle().setFontSize(fontSize.get(), com.google.gwt.dom.client.Style.Unit.PX);
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
 
         this.applyDefaultStyle(Label.class);
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
