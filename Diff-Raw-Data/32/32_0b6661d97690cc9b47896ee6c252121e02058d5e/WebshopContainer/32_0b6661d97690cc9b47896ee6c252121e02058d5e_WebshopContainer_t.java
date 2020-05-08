 package de.behrfriedapp.webshop.client.view;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.inject.Inject;
 import de.behrfriedapp.webshop.client.MainServiceAsync;
 import de.behrfriedapp.webshop.shared.data.ShortProductInfo;
 import de.behrfriedapp.webshop.shared.data.WProductGroupInfo;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: rofriedr
  * Date: 10.08.13
  * Time: 10:19
  * To change this template use File | Settings | File Templates.
  */
 public class WebshopContainer extends VerticalPanel {
 
     private VerticalPanel webshobContainer;
     private Label webshopTitle;
     private ProductSearchBar productSearchBar;
     private SearchedProductView searchedProductView;
     private final MainServiceAsync mainService;
 
     @Inject
     public WebshopContainer(final MainServiceAsync mainService) {
         this.webshopTitle = new Label("Wäpschob");
         this.productSearchBar = new ProductSearchBar();
         this.searchedProductView = new SearchedProductView();
         this.add(this.webshopTitle);
         this.add(this.productSearchBar);
         this.add(this.searchedProductView);
 
         this.mainService = mainService;
         this.bind();
         this.addClickHandler();
         this.addChangeHandler();
     }
 
     private void addClickHandler() {
         this.productSearchBar.getSearchButton().addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 if (WebshopContainer.this.productSearchBar.getCategoryBox().getItemText(WebshopContainer.this.productSearchBar.getCategoryBox().getSelectedIndex()).equals("Alles")) {
                     WebshopContainer.this.mainService.getAllProducts(new AsyncCallback<List<ShortProductInfo>>() {
                         public void onFailure(Throwable caught) {
                             //To change body of implemented methods use File | Settings | File Templates.
                         }
 
                         public void onSuccess(List<ShortProductInfo> result) {
                             while (WebshopContainer.this.searchedProductView.iterator().hasNext()) {
                                 WebshopContainer.this.searchedProductView.remove(0);
                             }
                             if (result.isEmpty()) {
                                 Label noEntry = new Label("kein Eintrag gefunden!");
                                 noEntry.setHorizontalAlignment(ALIGN_CENTER);
                                 WebshopContainer.this.searchedProductView.add(noEntry);
                             } else {
                                 for (ShortProductInfo productInfo : result) {
                                     WebshopContainer.this.searchedProductView.add(new ProductRow(productInfo.getName(), productInfo.getName(), productInfo.getName()));
                                 }
                             }
                         }
                     });
                 } else {
 
                 }
             }
         });
     }
 
     private void addChangeHandler() {
         this.productSearchBar.getSuggestBox().addValueChangeHandler(new ValueChangeHandler<String>() {
             public void onValueChange(ValueChangeEvent<String> event) {
                 if (WebshopContainer.this.productSearchBar.getSuggestBox().getValue().equals("")) {
                     WebshopContainer.this.productSearchBar.getSearchButton().setEnabled(false);
                 } else {
                     WebshopContainer.this.productSearchBar.getSearchButton().setEnabled(true);
                 }
             }
         });
     }
 
     private void bind() {
         this.productSearchBar.getCategoryBox().addItem("Alles");
         WebshopContainer.this.mainService.getAllProductGroups(new AsyncCallback<List<WProductGroupInfo>>() {
             public void onFailure(Throwable caught) {
                 Window.alert(caught.toString());
             }
 
             public void onSuccess(List<WProductGroupInfo> result) {
                 for (WProductGroupInfo str : result) {
                     for (int i = 0; i < productSearchBar.getCategoryBox().getItemCount(); i++) {
                         if (productSearchBar.getCategoryBox().getItemText(i).equals(str.getGroupName())) {
                             break;
                         } else if (productSearchBar.getCategoryBox().getItemCount() - 1 == i) {
                             productSearchBar.getCategoryBox().addItem(str.getGroupName());
                         }
 
                     }
                 }
             }
         });
 
         WebshopContainer.this.mainService.getAllProducts(new AsyncCallback<List<ShortProductInfo>>() {
             public void onFailure(Throwable caught) {
                 Window.alert(caught.toString());
             }
 
             public void onSuccess(List<ShortProductInfo> result) {
                final MultiWordSuggestOracle oracle = productSearchBar.getOracle();
                 for (ShortProductInfo info : result) {
                     oracle.add(info.getName());
                 }
             }
         });
     }
 }
