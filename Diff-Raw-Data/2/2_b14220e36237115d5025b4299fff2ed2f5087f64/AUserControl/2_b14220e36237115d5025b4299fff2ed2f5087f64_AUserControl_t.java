 package com.eagerlogic.cubee.client.components;
 
 import com.eagerlogic.cubee.client.properties.BackgroundProperty;
 import com.eagerlogic.cubee.client.properties.IChangeListener;
 import com.eagerlogic.cubee.client.properties.IntegerProperty;
 import com.eagerlogic.cubee.client.properties.Property;
 import com.eagerlogic.cubee.client.style.Style;
 import com.eagerlogic.cubee.client.style.styles.ABackground;
 import com.eagerlogic.cubee.client.style.styles.BoxShadow;
 import com.eagerlogic.cubee.client.style.styles.Color;
 import com.eagerlogic.cubee.client.style.styles.ColorBackground;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.client.DOM;
 
 /**
  *
  * @author dipacs
  */
 public abstract class AUserControl extends ALayout {
 
     public static class StyleClass<T extends AUserControl> extends AComponent.StyleClass<T> {
 
         private final Style<Integer> width = new Style<Integer>(null, true);
         private final Style<Integer> height = new Style<Integer>(null, true);
         private final Style<ABackground> background = new Style<ABackground>(null, true);
         private final Style<BoxShadow> shadow = new Style<BoxShadow>(null, true);
 
         public StyleClass() {
 
         }
 
         @Override
         public void apply(T component) {
             super.apply(component);
 
             width.apply(component.widthProperty());
             height.apply(component.heightProperty());
             background.apply(component.backgroundProperty());
             shadow.apply(component.shadowProperty());
         }
 
         protected Style<Integer> getWidth() {
             return width;
         }
 
         protected Style<Integer> getHeight() {
             return height;
         }
 
         protected Style<ABackground> getBackground() {
             return background;
         }
 
         protected Style<BoxShadow> getShadow() {
             return shadow;
         }
 
     }
 
     private IntegerProperty width = new IntegerProperty(null, true, false);
     private IntegerProperty height = new IntegerProperty(null, true, false);
     private BackgroundProperty background = new BackgroundProperty(new ColorBackground(Color.TRANSPARENT), true, false);
     private Property<BoxShadow> shadow = new Property<BoxShadow>(null, true, false);
 
     public AUserControl() {
         super(DOM.createDiv());
         width.addChangeListener(new IChangeListener() {
             @Override
             public void onChanged(Object sender) {
                 if (width.get() == null) {
                     getElement().getStyle().clearWidth();
                     getElement().getStyle().setOverflowX(com.google.gwt.dom.client.Style.Overflow.HIDDEN);
                 } else {
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
                     getElement().getStyle().setOverflowY(com.google.gwt.dom.client.Style.Overflow.HIDDEN);
                 } else {
                     getElement().getStyle().setHeight(height.get(), com.google.gwt.dom.client.Style.Unit.PX);
                     getElement().getStyle().setOverflowY(com.google.gwt.dom.client.Style.Overflow.HIDDEN);
                 }
                 requestLayout();
             }
         });
         height.invalidate();
         background.addChangeListener(new IChangeListener() {
             @Override
             public void onChanged(Object sender) {
                 getElement().getStyle().clearBackgroundColor();
                 getElement().getStyle().clearBackgroundImage();
                 getElement().getStyle().setProperty("background", "none");
                 if (background.get() != null) {
                     background.get().apply(getElement());
                 }
             }
         });
         background.invalidate();
         shadow.addChangeListener(new IChangeListener() {
 
             @Override
             public void onChanged(Object sender) {
                 if (shadow.get() == null) {
                     getElement().getStyle().clearProperty("boxShadow");
                 } else {
                     shadow.get().apply(getElement());
                 }
             }
         });
 
         this.applyDefaultStyle(AUserControl.class);
     }
 
     protected IntegerProperty widthProperty() {
         return width;
     }
 
     protected IntegerProperty heightProperty() {
         return height;
     }
 
     protected BackgroundProperty backgroundProperty() {
         return background;
     }
 
     protected Property<BoxShadow> shadowProperty() {
         return shadow;
     }
 
     @Override
     protected final void onChildAdded(AComponent child) {
         if (child != null) {
             getElement().appendChild(child.getElement());
         }
         requestLayout();
     }
 
     @Override
     protected final void onChildRemoved(AComponent child, int index) {
         if (child != null) {
             getElement().removeChild(child.getElement());
         }
         requestLayout();
     }
 
     @Override
     protected final void onChildrenCleared() {
         Element root = getElement();
         Element e = getElement().getFirstChildElement();
         while (e != null) {
             root.removeChild(e);
             e = root.getFirstChildElement();
         }
         requestLayout();
     }
 
     @Override
     protected final void onLayout() {
         if (width.get() != null && height.get() != null) {
             setSize(width.get(), height.get());
         } else {
             int maxW = 0;
             int maxH = 0;
             for (AComponent component : getChildren()) {
                 int cW = component.boundsWidthProperty().get() + component.boundsLeftProperty().get() + component
                         .translateXProperty().get();
                 int cH = component.boundsHeightProperty().get() + component.boundsTopProperty().get() + component
                         .translateYProperty().get();
 
                 if (cW > maxW) {
                     maxW = cW;
                 }
 
                 if (cH > maxH) {
                     maxH = cH;
                 }
             }
 
             if (width.get() != null) {
                 maxW = widthProperty().get();
             }
 
             if (height.get() != null) {
                 maxH = heightProperty().get();
             }
 
             setSize(maxW, maxH);
         }
     }
 
     @Override
     protected void onDestroy() {
        super.onDestroy();
         
         width.destroy();
         height.destroy();
         background.destroy();
         shadow.destroy();
     }
 
 }
