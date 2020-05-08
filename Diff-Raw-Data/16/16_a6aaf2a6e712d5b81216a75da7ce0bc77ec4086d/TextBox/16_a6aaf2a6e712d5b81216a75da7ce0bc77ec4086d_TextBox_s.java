 package com.eagerlogic.cubee.client.components;
 
 import com.eagerlogic.cubee.client.properties.BackgroundProperty;
 import com.eagerlogic.cubee.client.properties.BooleanProperty;
 import com.eagerlogic.cubee.client.properties.BorderProperty;
 import com.eagerlogic.cubee.client.properties.ColorProperty;
 import com.eagerlogic.cubee.client.properties.IChangeListener;
 import com.eagerlogic.cubee.client.properties.IntegerProperty;
 import com.eagerlogic.cubee.client.properties.PaddingProperty;
 import com.eagerlogic.cubee.client.properties.Property;
 import com.eagerlogic.cubee.client.properties.StringProperty;
 import com.eagerlogic.cubee.client.style.Style;
 import com.eagerlogic.cubee.client.style.styles.ABackground;
 import com.eagerlogic.cubee.client.style.styles.Border;
 import com.eagerlogic.cubee.client.style.styles.Color;
 import com.eagerlogic.cubee.client.style.styles.ColorBackground;
 import com.eagerlogic.cubee.client.style.styles.ETextAlign;
 import com.eagerlogic.cubee.client.style.styles.EVAlign;
 import com.eagerlogic.cubee.client.style.styles.FontFamily;
 import com.eagerlogic.cubee.client.style.styles.Padding;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.client.DOM;
 
 /**
  *
  * @author dipacs
  */
 public class TextBox extends AComponent {
 
     public static class StyleClass<T extends TextBox> extends AComponent.StyleClass<T> {
 
         private final Style<Integer> width = new Style<Integer>(null, true);
         private final Style<Integer> height = new Style<Integer>(null, true);
         private final Style<Color> foreColor = new Style<Color>(null, false);
         private final Style<ETextAlign> textAlign = new Style<ETextAlign>(null, false);
         private final Style<EVAlign> verticalAlign = new Style<EVAlign>(null, false);
         private final Style<Boolean> bold = new Style<Boolean>(null, false);
         private final Style<Boolean> italic = new Style<Boolean>(null, false);
         private final Style<Boolean> underline = new Style<Boolean>(null, false);
         private final Style<Integer> fontSize = new Style<Integer>(null, false);
         private final Style<FontFamily> fontFamily = new Style<FontFamily>(null, false);
         private final Style<ABackground> background = new Style<ABackground>(null, false);
 
         @Override
         public void apply(T component) {
             super.apply(component);
 
             width.apply(component.widthProperty());
             height.apply(component.heightProperty());
             foreColor.apply(component.foreColorProperty());
             textAlign.apply(component.textAlignProperty());
             verticalAlign.apply(component.verticalAlignProperty());
             bold.apply(component.boldProperty());
             italic.apply(component.italicProperty());
             underline.apply(component.underlineProperty());
             fontSize.apply(component.fontSizeProperty());
             fontFamily.apply(component.fontFamilyProperty());
             background.apply(component.backgroundProperty());
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
 
         public Style<ABackground> getBackground() {
             return background;
         }
 
     }
 
     private final IntegerProperty width = new IntegerProperty(null, true, false);
     private final IntegerProperty height = new IntegerProperty(null, true, false);
     private final StringProperty text = new StringProperty("", false, false);
     private final BackgroundProperty background = new BackgroundProperty(new ColorBackground(Color.WHITE), true, false);
     private final ColorProperty foreColor = new ColorProperty(Color.BLACK, true, false);
     private final Property<ETextAlign> textAlign = new Property<ETextAlign>(ETextAlign.LEFT, false, false);
     private final Property<EVAlign> verticalAlign = new Property<EVAlign>(EVAlign.TOP, false, false);
     private final BooleanProperty bold = new BooleanProperty(false, false, false);
     private final BooleanProperty italic = new BooleanProperty(false, false, false);
     private final BooleanProperty underline = new BooleanProperty(false, false, false);
     private final IntegerProperty fontSize = new IntegerProperty(12, false, false);
     private final Property<FontFamily> fontFamily = new Property<FontFamily>(FontFamily.Arial, false, false);
 
     public TextBox() {
         this(DOM.createInputText());
     }
 
     TextBox(Element e) {
         super(e);
        getElement().getStyle().setOutlineStyle(com.google.gwt.dom.client.Style.OutlineStyle.NONE);
         this.borderProperty().set(new Border(1, Color.LIGHT_GRAY, 0));
         width.addChangeListener(new IChangeListener() {
             @Override
             public void onChanged(Object sender) {
                 if (width.get() == null) {
                     getElement().getStyle().clearWidth();
                     getElement().getStyle().setOverflowX(com.google.gwt.dom.client.Style.Overflow.AUTO);
                 } else {
                     getElement().getStyle().setWidth(width.get(), com.google.gwt.dom.client.Style.Unit.PX);
                     getElement().getStyle().setOverflowX(com.google.gwt.dom.client.Style.Overflow.HIDDEN);
                 }
                 requestLayout();
             }
         });
         height.addChangeListener(new IChangeListener() {
             @Override
             public void onChanged(Object sender) {
                 if (height.get() == null) {
                     getElement().getStyle().clearHeight();
                     getElement().getStyle().setOverflowY(com.google.gwt.dom.client.Style.Overflow.AUTO);
                 } else {
                     getElement().getStyle().setHeight(height.get(), com.google.gwt.dom.client.Style.Unit.PX);
                     getElement().getStyle().setOverflowY(com.google.gwt.dom.client.Style.Overflow.HIDDEN);
                 }
                 requestLayout();
             }
         });
         text.addChangeListener(new IChangeListener() {
 
             @Override
             public void onChanged(Object sender) {
                 if (!text.get().equals(getElement().getPropertyString("value"))) {
                     getElement().setPropertyString("value", text.get());
                 }
             }
         });
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
         background.addChangeListener(new IChangeListener() {
 
             @Override
             public void onChanged(Object sender) {
                 if (background.get() == null) {
                     getElement().getStyle().clearBackgroundColor();
                     getElement().getStyle().clearBackgroundImage();
                 } else {
                     background.get().apply(getElement());
                 }
             }
         });
         background.invalidate();
 
         this.applyDefaultStyle(TextBox.class);
     }
 
     public int getCaretPosition() {
         return 0;
     }
 
     public void setCaretPosition(int pos) {
 
     }
 
     public final IntegerProperty widthProperty() {
         return width;
     }
 
     public final IntegerProperty heightProperty() {
         return height;
     }
 
     @Override
     public final PaddingProperty paddingProperty() {
         return super.paddingProperty();
     }
 
     @Override
     public final BorderProperty borderProperty() {
         return super.borderProperty();
     }
 
     public final StringProperty textProperty() {
         return text;
     }
 
     public final BackgroundProperty backgroundProperty() {
         return background;
     }
 
     public final ColorProperty foreColorProperty() {
         return foreColor;
     }
 
     public final Property<ETextAlign> textAlignProperty() {
         return textAlign;
     }
 
     public final Property<EVAlign> verticalAlignProperty() {
         return verticalAlign;
     }
 
     public final BooleanProperty boldProperty() {
         return bold;
     }
 
     public final BooleanProperty italicProperty() {
         return italic;
     }
 
     public final BooleanProperty underlineProperty() {
         return underline;
     }
 
     public final IntegerProperty fontSizeProperty() {
         return fontSize;
     }
 
     public final Property<FontFamily> fontFamilyProperty() {
         return fontFamily;
     }
 
 }
