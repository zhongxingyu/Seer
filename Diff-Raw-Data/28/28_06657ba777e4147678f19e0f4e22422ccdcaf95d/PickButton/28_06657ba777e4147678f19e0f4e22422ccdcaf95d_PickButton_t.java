 package com.bookspicker.client.view.widgets.buttons;
 
 import com.bookspicker.client.view.Resources;
 import com.bookspicker.client.view.SearchPage;
 import com.bookspicker.client.view.SearchPageUnified;
 import com.bookspicker.shared.Book;
 import com.google.gwt.dom.client.Style.Cursor;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.FocusPanel;
 import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
 
 public class PickButton extends FocusPanel{
	InlineLabel normal = new InlineLabel("Check Prices");
 	Image disabled = new Image(Resources.INSTANCE.pickButtonDisabled());
 	
 	Book pickableBook;
 	boolean enabled = true;
 	
 	public PickButton() {
 		super();
 		this.setStylePrimaryName(Resources.INSTANCE.style().pickButton());
 		normal.addStyleName(Resources.INSTANCE.style().bpYellowButton());
 		
 		pickableBook = null;
 		
 		this.setWidget(normal);
 		setTitle("Add to bundle");
 		addClickHandler(SearchPageUnified.checkPricesHandler);
 	}
 	
 	public PickButton(Book book) {
 		super();
 		this.setStylePrimaryName(Resources.INSTANCE.style().pickButton());
 		normal.addStyleName(Resources.INSTANCE.style().bpYellowButton());
 		pickableBook = book;
 		this.setWidget(normal);
 		setTitle("Add to bundle");
 		addClickHandler(SearchPage.pickHandler);
 	}
 	
 	public void setDisabled(boolean disabled){
 		if(disabled){
 			this.getElement().getStyle().setCursor(Cursor.DEFAULT);
 			this.setWidget(this.disabled);
 			enabled = false;
 		} else {
 			this.getElement().getStyle().setCursor(Cursor.POINTER);
 			this.setWidget(normal);
 			enabled = true;
 		}
 	}
 	
 	public boolean isEnabled() {
 		return enabled;
 	}
 	
 	public Book getAssociatedBook(){
 		return pickableBook;
 	}
 
 }
