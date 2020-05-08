 package com.japancuccok.main.shop;
 
 import com.japancuccok.common.pattern.ImageLoadStrategy;
import com.japancuccok.common.wicket.component.CustomPagingNavigator;
 import com.japancuccok.common.wicket.component.ProductListView;
 import com.japancuccok.common.wicket.template.ShopBasePage;
 import org.apache.wicket.markup.html.basic.Label;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Nagy Gergely
  * Date: 2012.04.26.
  * Time: 0:41
  */
 public class Shop extends ShopBasePage {
 
     private static final long serialVersionUID = -8717877930356537670L;
 
     public Shop() {
         ProductListView productListView = new ProductListView("productRow",
                 new ImageLoadStrategy());
        CustomPagingNavigator upperNavigator = new CustomPagingNavigator("upperNavigator",
                productListView);
        CustomPagingNavigator lowerNavigator = new CustomPagingNavigator("lowerNavigator",
                productListView);
        add(upperNavigator);
         add(productListView);
        add(lowerNavigator);
     }
 
     @Override
     public Label getHeaderTitle() {
         return new Label("headerTitle", "Itt aztán van minden!");
     }
 
 }
