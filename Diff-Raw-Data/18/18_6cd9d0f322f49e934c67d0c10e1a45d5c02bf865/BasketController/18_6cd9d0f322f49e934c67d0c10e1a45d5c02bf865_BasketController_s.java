 package org.fhw.asta.kasse.client.controller;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 
 import org.fhw.asta.kasse.client.common.EuroFormatter;
 import org.fhw.asta.kasse.client.common.PrintCustomsToken;
 import org.fhw.asta.kasse.client.common.PrintCustomsToken.PrintType;
 import org.fhw.asta.kasse.client.widget.basket.BasketWidget;
 import org.fhw.asta.kasse.shared.basket.BasketItem;
 import org.fhw.asta.kasse.shared.model.Article;
 import org.fhw.asta.kasse.shared.model.OrderState;
 import org.fhw.asta.kasse.shared.service.checkout.CheckoutServiceAsync;
 
 import com.google.common.collect.ComparisonChain;
 import com.google.common.collect.Lists;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.ProvidesKey;
 import com.google.inject.Inject;
 
 public class BasketController {
   private final ListDataProvider<BasketItem> basketDataProvider;
 
   private BasketWidget basketWidget;
 
   private FieldUpdater<BasketItem, String> deleteUpdater;
   private FieldUpdater<BasketItem, String> amountUpdater;
   private FieldUpdater<BasketItem, String> discountUpdater;
   private ValueChangeHandler<String> masterDiscountUpdater;
   private ValueChangeHandler<String> matrNrUpdater;
   private ClickHandler checkoutHandler;
   private ClickHandler checkoutHandlerUnPayed;
   private BasketComparator basketComparator = new BasketComparator();
 
   @Inject
   private CheckoutServiceAsync checkoutService;
 
   @Inject
   private PlaceController placeController;
 
   public BasketController() {
     this.basketDataProvider = new ListDataProvider<BasketItem>(new BasketItemKeyProvider());
   }
 
   @Inject
   public void init(BasketWidget basketWidget) {
 
     this.basketWidget = basketWidget;
 
     this.basketDataProvider.addDataDisplay(this.basketWidget.getBasketTable());
 
     this.deleteUpdater = new DeleteUpdater();
     basketWidget.getDeleteColumn().setFieldUpdater(this.deleteUpdater);
 
     this.amountUpdater = new AmountUpdater();
     basketWidget.getAmountColumn().setFieldUpdater(this.amountUpdater);
 
     this.discountUpdater = new DiscountUpdater();
     basketWidget.getDiscountColumn().setFieldUpdater(this.discountUpdater);
 
     this.masterDiscountUpdater = new MasterDiscountUpdater();
     basketWidget.getDiscountBox().addValueChangeHandler(this.masterDiscountUpdater);
 
     this.checkoutHandler = new CheckoutHandler();
     basketWidget.getCheckoutButton().addClickHandler(this.checkoutHandler);
     
     this.checkoutHandlerUnPayed = new CheckoutHandlerUnPayed();
     basketWidget.getCheckoutButtonUnPayed().addClickHandler(this.checkoutHandlerUnPayed);
 
     this.matrNrUpdater = new MatrNrUpdater();
     basketWidget.getMatrNrBox().addValueChangeHandler(this.matrNrUpdater);
   }
 
   public void addBasketPosition(BasketItem basketItem) {
 
     final Iterator<BasketItem> basketIterator = this.basketDataProvider.getList().iterator();
     while (basketIterator.hasNext()) {
       final BasketItem next = basketIterator.next();
       if (next.getArticleId() == basketItem.getArticleId()) {
         basketItem = new BasketItem(basketItem.getItemName(), basketItem.getItemPrice(), basketItem.getArticleId(),
             basketItem.getAmount() + next.getAmount(), next.getDiscount());
         this.basketDataProvider.getList().remove(next);
       }
     }
 
     this.basketDataProvider.getList().add(basketItem);
     this.flush();
 
   }
 
   public void addBasketPosition(Article article) {
 
     this.addBasketPosition(new BasketItem(article.getName(), article.getPrice(), article.getId(), 1));
   }
 
   private void flush() {
     Collections.sort(this.basketDataProvider.getList(), this.basketComparator);
 
     int sum = 0;
 
     final Iterator<BasketItem> basketIterator = this.basketDataProvider.getList().iterator();
     while (basketIterator.hasNext()) {
       final BasketItem next = basketIterator.next();
       sum += Math
           .round((next.getItemPrice().getCentAmount() * next.getAmount()) * ((100 - next.getDiscount()) / 100.0));
 
     }
 
     int discount = 0;
 
     try {
       discount = Integer.valueOf(this.basketWidget.getDiscountBox().getText());
     } catch (final NumberFormatException e) {
       discount = 0;
     }
 
     this.basketWidget.getSumLabel().setText(EuroFormatter.format((int) Math.round(sum * ((100 - discount) / 100.0))));
   }
 
   private class CheckoutHandler implements ClickHandler {
 
     @Override
     public void onClick(ClickEvent event) {
       BasketController.this.checkoutService.doCheckout(
           Lists.newArrayList(BasketController.this.basketDataProvider.getList()),
           Integer.valueOf(BasketController.this.basketWidget.getDiscountBox().getText()),
           BasketController.this.basketWidget.getMatrNrBox().getText(), OrderState.Paid, new CheckoutCallback());
     }
 
   }
   
   private class CheckoutHandlerUnPayed implements ClickHandler {
 
 	    @Override
 	    public void onClick(ClickEvent event) {
 	      BasketController.this.checkoutService.doCheckout(
 	          Lists.newArrayList(BasketController.this.basketDataProvider.getList()),
 	          Integer.valueOf(BasketController.this.basketWidget.getDiscountBox().getText()),
 	          BasketController.this.basketWidget.getMatrNrBox().getText(), OrderState.Ordered, new CheckoutCallback());
 	    }
 
 	  }
 
   private class AmountUpdater implements FieldUpdater<BasketItem, String> {
 
     @Override
     public void update(int index, BasketItem object, String value) {
       BasketItem toUpdate;
       if (value.matches("[1-9][0-9]*")) {
         toUpdate = new BasketItem(object.getItemName(), object.getItemPrice(), object.getArticleId(),
             Integer.valueOf(value), object.getDiscount());
 
       } else {
         toUpdate = new BasketItem(object.getItemName(), object.getItemPrice(), object.getArticleId(), 1,
             object.getDiscount());
       }
 
       BasketController.this.basketDataProvider.getList().remove(object);
       BasketController.this.basketDataProvider.getList().add(toUpdate);
       BasketController.this.flush();
 
     }
   }
 
   private class DeleteUpdater implements FieldUpdater<BasketItem, String> {
 
     @Override
     public void update(int index, BasketItem object, String value) {
       BasketController.this.basketDataProvider.getList().remove(object);
       BasketController.this.flush();
     }
   }
 
   private class DiscountUpdater implements FieldUpdater<BasketItem, String> {
 
     @Override
     public void update(int index, BasketItem object, String value) {
       BasketItem toUpdate;
 
       if (value.matches("[0-9]+")) {
         toUpdate = new BasketItem(object.getItemName(), object.getItemPrice(), object.getArticleId(),
             object.getAmount(), Integer.valueOf(value));
       } else {
         toUpdate = new BasketItem(object.getItemName(), object.getItemPrice(), object.getArticleId(),
             object.getAmount(), 0);
       }
 
       BasketController.this.basketDataProvider.getList().remove(object);
       BasketController.this.basketDataProvider.getList().add(toUpdate);
       BasketController.this.flush();
 
     }
 
   }
 
   private class MasterDiscountUpdater implements ValueChangeHandler<String> {
 
     @Override
     public void onValueChange(ValueChangeEvent<String> event) {
       if (!event.getValue().matches("[0-9]+")) {
         BasketController.this.basketWidget.getDiscountBox().setText("0");
       }
       BasketController.this.flush();
     }
 
   }
 
   private class MatrNrUpdater implements ValueChangeHandler<String> {
 
     @Override
     public void onValueChange(ValueChangeEvent<String> event) {
      if (!event.getValue().matches("[0-9]+")) {
        BasketController.this.basketWidget.getMatrNrBox().setText("0000");
      }
       BasketController.this.flush();
     }
 
   }
 
   private static final class BasketItemKeyProvider implements ProvidesKey<BasketItem> {
 
     @Override
     public Object getKey(BasketItem item) {
       return item.getArticleId();
     }
 
   }
 
   private final class BasketComparator implements Comparator<BasketItem> {
 
     @Override
     public int compare(BasketItem o1, BasketItem o2) {
       return ComparisonChain.start().compare(o1.getArticleId(), o2.getArticleId()).result();
     }
 
   }
 
   private class CheckoutCallback implements AsyncCallback<Integer> {
 
     @Override
     public void onFailure(Throwable caught) {
       // TODO FIXME
     }
 
     @Override
     public void onSuccess(Integer result) {
       BasketController.this.basketDataProvider.getList().clear();
       final PrintCustomsToken token = new PrintCustomsToken(PrintType.BILLORDER, result);
       Window.open(Window.Location.createUrlBuilder().setHash("PrintCustomsPlace:").buildString() + token.toString(),
           "_blank", "");
     }
 
   }
 
 }
