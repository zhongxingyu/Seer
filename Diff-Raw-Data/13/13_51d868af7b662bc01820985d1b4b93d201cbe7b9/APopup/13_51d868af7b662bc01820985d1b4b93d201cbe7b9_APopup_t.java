 package com.eagerlogic.cubee.client.components;
 
 import com.eagerlogic.cubee.client.events.ClickEventArgs;
 import com.eagerlogic.cubee.client.events.IEventListener;
 import com.eagerlogic.cubee.client.properties.AExpression;
 import com.eagerlogic.cubee.client.properties.BooleanProperty;
 import com.eagerlogic.cubee.client.properties.IntegerProperty;
 import com.eagerlogic.cubee.client.style.AStyleClass;
 import com.eagerlogic.cubee.client.style.Style;
 import com.eagerlogic.cubee.client.style.styles.Color;
 import com.eagerlogic.cubee.client.style.styles.ColorBackground;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.RootPanel;
 
 /**
  *
  * @author dipacs
  */
 public abstract class APopup {
 
     public static class StyleClass<T extends APopup> extends AStyleClass<T> {
 
         private final Style<Integer> translateX = new Style<Integer>(null, false);
         private final Style<Integer> translateY = new Style<Integer>(null, false);
         private final Style<Boolean> center = new Style<Boolean>(null, false);
 
         @Override
         public void apply(T component) {
             translateX.apply(component.translateXProperty());
             translateY.apply(component.translateYProperty());
             center.apply(component.centerProperty());
         }
 
         public Style<Integer> getTranslateX() {
             return translateX;
         }
 
         public Style<Integer> getTranslateY() {
             return translateY;
         }
 
         public Style<Boolean> getCenter() {
             return center;
         }
 
     }
 
     private final boolean modal;
     private final boolean autoClose;
     private final Color glassColor;
 
     private final IntegerProperty translateX = new IntegerProperty(0, false, false);
     private final IntegerProperty translateY = new IntegerProperty(0, false, false);
     private final BooleanProperty center = new BooleanProperty(false, false, false);
 
     private Panel popupRoot;
     private Panel rootComponentContainer;
     private AComponent rootComponent;
 
     private boolean visible = false;
 
     public APopup() {
         this(true, true, Color.getArgbColor(0x00000000));
     }
 
     public APopup(boolean modal, boolean autoClose) {
         this(modal, autoClose, Color.getArgbColor(0x00000000));
     }
 
     public APopup(boolean modal, boolean autoClose, Color glassColor) {
         this.modal = modal;
         this.autoClose = autoClose;
         this.glassColor = glassColor;
         this.popupRoot = new Panel();
         this.popupRoot.getElement().getStyle().setLeft(0.0, com.google.gwt.dom.client.Style.Unit.PX);
         this.popupRoot.getElement().getStyle().setTop(0.0, com.google.gwt.dom.client.Style.Unit.PX);
         this.popupRoot.getElement().getStyle().setRight(0.0, com.google.gwt.dom.client.Style.Unit.PX);
         this.popupRoot.getElement().getStyle().setBottom(0.0, com.google.gwt.dom.client.Style.Unit.PX);
         this.popupRoot.getElement().getStyle().setPosition(com.google.gwt.dom.client.Style.Position.FIXED);
         if (glassColor != null) {
             this.popupRoot.backgroundProperty().set(new ColorBackground(glassColor));
         }
         if (modal || autoClose) {
             this.popupRoot.getElement().getStyle().setProperty("pointerEvents", "all");
         } else {
             this.popupRoot.getElement().getStyle().setProperty("pointerEvents", "none");
             this.popupRoot.pointerTransparentProperty().set(true);
         }
 
         this.rootComponentContainer = new Panel();
         this.rootComponentContainer.translateXProperty().bind(new AExpression<Integer>() {
 
             {
                this.bind(center, popupRoot.clientWidthProperty(), translateX,
                         rootComponentContainer.boundsWidthProperty());
             }
 
             @Override
             public Integer calculate() {
                 int baseX = 0;
                 if (center.get()) {
                     baseX = (popupRoot.clientWidthProperty().get() - rootComponentContainer.boundsWidthProperty().get())
                             / 2;
                 }
                 return baseX + translateX.get();
             }
         });
         this.rootComponentContainer.translateYProperty().bind(new AExpression<Integer>() {
 
             {
                this.bind(center, popupRoot.clientHeightProperty(), translateY,
                         rootComponentContainer.boundsHeightProperty());
             }
 
             @Override
             public Integer calculate() {
                 int baseY = 0;
                 if (center.get()) {
                    baseY = (popupRoot.clientHeightProperty().get()
                             - rootComponentContainer.boundsHeightProperty().get()) / 2;
                 }
                 return baseY + translateY.get();
             }
         });
         this.popupRoot.getChildren().add(rootComponentContainer);
 
         if (autoClose) {
             this.popupRoot.onClickEvent().addListener(new IEventListener<ClickEventArgs>() {
 
                 @Override
                 public void onFired(ClickEventArgs args) {
                     close();
                 }
             });
         }
     }
 
     final Panel getPopupRoot() {
         return popupRoot;
     }
 
     protected AComponent getRootComponent() {
         return rootComponent;
     }
 
     protected void setRootComponent(AComponent rootComponent) {
         this.rootComponentContainer.getChildren().clear();
         this.rootComponent = null;
         if (rootComponent != null) {
             this.rootComponentContainer.getChildren().add(rootComponent);
         }
         this.rootComponent = rootComponent;
     }
 
     protected void show() {
         if (visible) {
             throw new IllegalStateException("This popup is already shown.");
         }
         RootPanel.get().getElement().appendChild(this.popupRoot.getElement());
         Popups.addPopup(this);
         this.visible = true;
     }
 
     protected void close() {
         if (!visible) {
             throw new IllegalStateException("This popup isn't shown.");
         }
         RootPanel.get().getElement().removeChild(this.popupRoot.getElement());
         Popups.removePopup(this);
         this.visible = false;
         onClosed();
     }
 
     protected void onClosed() {
 
     }
 
     public final boolean isModal() {
         return modal;
     }
 
     public final boolean isAutoClose() {
         return autoClose;
     }
 
     public final Color getGlassColor() {
         return glassColor;
     }
 
     protected IntegerProperty translateXProperty() {
         return translateX;
     }
 
     protected IntegerProperty translateYProperty() {
         return translateY;
     }
 
     protected BooleanProperty centerProperty() {
         return center;
     }
 
     protected int getTranslateX() {
         return translateXProperty().get();
     }
 
     protected void setTranslateX(int translateX) {
         this.translateXProperty().set(translateX);
     }
 
     protected int getTranslateY() {
         return translateYProperty().get();
     }
 
     protected void setTranslateY(int translateY) {
         this.translateYProperty().set(translateY);
     }
 
     protected boolean isCenter() {
         return centerProperty().get();
     }
 
     protected void setCenter(boolean center) {
         this.centerProperty().set(center);
     }
     
     boolean doPointerEventClimbingUp(int screenX, int screenY, int x, int y, int wheelVelocity,
             boolean altPressed, boolean ctrlPressed, boolean shiftPressed, boolean metaPressed, int type) {
         return this.popupRoot.doPointerEventClimbingUp(screenX, screenY, x, y, wheelVelocity, altPressed, ctrlPressed, shiftPressed, metaPressed, type);
     }
     
     final void layout() {
         this.popupRoot.widthProperty().set(Window.getClientWidth());
         this.popupRoot.heightProperty().set(Window.getClientHeight());
        this.popupRoot.layout();
     }
 
 }
