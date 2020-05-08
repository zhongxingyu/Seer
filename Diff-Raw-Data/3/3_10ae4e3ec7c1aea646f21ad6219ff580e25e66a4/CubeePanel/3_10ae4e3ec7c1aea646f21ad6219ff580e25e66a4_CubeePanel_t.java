 package com.eagerlogic.cubee.client.components;
 
 import com.eagerlogic.cubee.client.properties.BackgroundProperty;
 import com.eagerlogic.cubee.client.utils.ARunOnce;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.EventListener;
 import com.google.gwt.user.client.Window;
 
 /**
  *
  * @author dipacs
  */
 public final class CubeePanel extends ALayout {
 
     private static CubeePanel instance;
 
     public static CubeePanel getInstance() {
         if (instance == null) {
             instance = new CubeePanel();
         }
         return instance;
     }
 
     private ARunOnce layoutRunOnce;
 
     private final Panel contentPanel;
     private final Panel popupPanel;
     private AComponent rootComponent;
 
     private CubeePanel() {
         super(DOM.createDiv());
         getElement().getStyle().setPosition(Position.FIXED);
         getElement().getStyle().setLeft(0, Style.Unit.PX);
         getElement().getStyle().setTop(0, Style.Unit.PX);
         getElement().getStyle().setRight(0, Style.Unit.PX);
         getElement().getStyle().setBottom(0, Style.Unit.PX);
         getElement().getStyle().setBackgroundColor("#f0f0f0");
         Window.addResizeHandler(new ResizeHandler() {
 
             @Override
             public void onResize(ResizeEvent event) {
                 requestLayout();
             }
         });
 
         this.contentPanel = new Panel();
         this.contentPanel.getElement().getStyle().setProperty("pointerEvents", "none");
         this.contentPanel.widthProperty().bind(this.clientWidthProperty());
         this.contentPanel.heightProperty().bind(this.clientHeightProperty());
         this.contentPanel.pointerTransparentProperty().set(true);
         this.getChildren().add(this.contentPanel);
 
         this.popupPanel = new Panel();
         this.popupPanel.getElement().getStyle().setProperty("pointerEvents", "none");
         this.popupPanel.widthProperty().bind(this.clientWidthProperty());
         this.popupPanel.heightProperty().bind(this.clientHeightProperty());
         this.popupPanel.pointerTransparentProperty().set(true);
         this.getChildren().add(this.popupPanel);
 
         requestLayout();
     }
 
     @Override
     public void requestLayout() {
         if (layoutRunOnce == null) {
             layoutRunOnce = new ARunOnce() {
                 @Override
                 protected void onRun() {
 					// TODO remove sout
                     //long ss = System.currentTimeMillis();
                     layout();
 					//long es = System.currentTimeMillis();
                     //System.out.println("!!! LAYING OUT !!!" + (es - ss) + "ms.");
                 }
             };
         }
         layoutRunOnce.run();
     }
 
     public AComponent getRootComponent() {
         return rootComponent;
     }
 
     public void setRootComponent(AComponent rootComponent) {
         this.contentPanel.getChildren().clear();
         this.rootComponent = null;
         if (rootComponent != null) {
             this.contentPanel.getChildren().add(rootComponent);
            this.rootComponent = rootComponent;
         }
     }
 
     @Override
     protected final void onChildAdded(AComponent child) {
         if (child != null) {
             getElement().appendChild(child.getElement());
         }
     }
 
     @Override
     protected final void onChildRemoved(AComponent child, int index) {
         if (child != null) {
             getElement().removeChild(child.getElement());
         }
     }
 
     @Override
     protected final void onChildrenCleared() {
         Element root = getElement();
         Element e = getElement().getFirstChildElement();
         while (e != null) {
             root.removeChild(e);
             e = root.getFirstChildElement();
         }
     }
 
     @Override
     protected void onLayout() {
         // nothing to do here
     }
 
     void showPopup(APopup popup) {
         this.popupPanel.getChildren().add(popup.getPopupRoot());
     }
 
     void closePopup(APopup popup) {
         this.popupPanel.getChildren().remove(popup.getPopupRoot());
     }
 
     public BackgroundProperty backgroundProperty() {
         return contentPanel.backgroundProperty();
     }
 
 }
