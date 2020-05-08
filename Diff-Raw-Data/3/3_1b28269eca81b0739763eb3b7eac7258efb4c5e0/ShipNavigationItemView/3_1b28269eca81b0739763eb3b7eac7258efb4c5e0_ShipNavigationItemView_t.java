 package com.appspot.evetool.client.view.ship;
 
 import com.appspot.evetool.client.proxy.ShipProxy;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.ImageElement;
 import com.google.gwt.dom.client.SpanElement;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ast
  * Date: Nov 12, 2010
  * Time: 2:57:22 PM
  */
 public class ShipNavigationItemView  extends Composite {
   interface MyUiBinder extends UiBinder<Widget, ShipNavigationItemView> {}
   private static MyUiBinder binder = GWT.create(MyUiBinder.class);
  @UiField ImageElement icon;
   @UiField SpanElement name;
 
   public ShipNavigationItemView(ShipProxy ship) {
     initWidget(binder.createAndBindUi(this));
     icon.setSrc("/images/ship?gameId=" + ship.getGameId());
     name.setInnerText(ship.getName());
   }
 }
