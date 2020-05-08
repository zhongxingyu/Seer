 package com.bookspicker.client.view.widgets;
 
 import java.util.List;
 
 import com.bookspicker.client.event.Analytics;
 import com.bookspicker.client.view.Resources;
 import com.bookspicker.client.view.Resources.Style;
 import com.bookspicker.client.view.widgets.buttons.BuyOfferButton;
 import com.bookspicker.client.view.widgets.buttons.SelectOfferButton;
 import com.bookspicker.shared.Book;
 import com.bookspicker.shared.NumberUtil;
 import com.bookspicker.shared.Offer;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.FocusPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
 import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
 
 public class ModernOfferTablePanel extends FlowPanel {
 	List<Offer> offers;
 	Offer selectedOffer;
 	FlexTable expandedOffersTable;
 	FlexTable collapsedOffersTable;
 	FlexCellFormatter expandedCellFormatter;
 	FlexCellFormatter collapsedCellFormatter;
 	RowFormatter expandedRowFormatter;
 	RowFormatter collapsedRowFormatter;
 	
 	SimplePanel tableWrapper;
 	FocusPanel buttonWrapper;
 
 	Style style = Resources.INSTANCE.style();
 	private final String EXPAND_TEXT = "Show More Offers";
 	private final String COLLAPSE_TEXT = "Show Fewer Offers";
	Label expandCollapseLabel;
 	
 	private Book book;
 	
 	private boolean collapsed = true;
 
 	public ModernOfferTablePanel(Book book, Offer selectedOffer, List<Offer> list) {
 		super();
 		
 		offers = list;
 		this.selectedOffer = selectedOffer;
 		this.book = book;
 		
 		// create both offer tables
 		expandedOffersTable = new FlexTable();
 		expandedOffersTable.setStylePrimaryName(style.offersTable());
 		collapsedOffersTable = new FlexTable();
 		collapsedOffersTable.setStylePrimaryName(style.offersTable());
 
 		// grab its formatters
 		expandedCellFormatter = expandedOffersTable.getFlexCellFormatter();
 		expandedRowFormatter = expandedOffersTable.getRowFormatter();
 		collapsedCellFormatter = collapsedOffersTable.getFlexCellFormatter();
 		collapsedRowFormatter = collapsedOffersTable.getRowFormatter();
 		
 		initOffersTables(); // create headers
 		updateExpandedTable(true);
 		updateCollapsedTable();
 		
 		add(collapsedOffersTable);
 		
 		if (list.size() > 1) {
			expandCollapseLabel = new Label(EXPAND_TEXT);
 			buttonWrapper = new FocusPanel(expandCollapseLabel);
 			expandCollapseLabel.addStyleName(style.bpYellowButton());
 			buttonWrapper.addStyleName(style.collapseToggleButton());
 			buttonWrapper.addClickHandler(new ClickHandler(){
 	
 				@Override
 				public void onClick(ClickEvent event) {
 					if (collapsed) {
 						Analytics.trackExpandOffersButton(offers.size(), ModernOfferTablePanel.this.book);
 						expandCollapseLabel.setText(COLLAPSE_TEXT);
 						remove(collapsedOffersTable);
 						remove(buttonWrapper);
 						add(expandedOffersTable);
 						add(buttonWrapper);
 						collapsed = false;
 					} else {
 						Analytics.trackCollapsedOffersButton(offers.size(), ModernOfferTablePanel.this.book);
 						expandCollapseLabel.setText(EXPAND_TEXT);
 						remove(expandedOffersTable);
 						remove(buttonWrapper);
 						add(collapsedOffersTable);
 						add(buttonWrapper);
 						collapsed = true;
 					}
 				}
 			});
 			this.add(buttonWrapper);
 		}
 	}
 	
 	private void initOffersTables() {
 		//String[] columnHeaders = new String[] { "Store", "Seller", "Condition",
 		//		"Price", "Shipping", "Total", "" };
 		String[] columnHeaders = new String[] { "Store", "Condition", 
 						"Price", "Shipping", "Total", "" };
 		expandedRowFormatter.setStyleName(0, style.headerRow());
 		collapsedRowFormatter.setStyleName(0, style.headerRow());
 		for (int c = 0; c < columnHeaders.length; c++) {
 			expandedOffersTable.setWidget(0, c, new Label(columnHeaders[c]));
 			collapsedOffersTable.setWidget(0, c, new Label(columnHeaders[c]));
 		}
 		expandedOffersTable.getCellFormatter().setStylePrimaryName(0, 0, style.leftAligned());
 		collapsedOffersTable.getCellFormatter().setStylePrimaryName(0, 0, style.leftAligned());
 		
 		//set condition style
 		expandedOffersTable.getCellFormatter().setStylePrimaryName(0, 1, style.leftAligned());
 		collapsedOffersTable.getCellFormatter().setStylePrimaryName(0, 1, style.leftAligned());
 		expandedOffersTable.getCellFormatter().setWidth(0, 1, "100%");
 		collapsedOffersTable.getCellFormatter().setWidth(0, 1, "100%");
 	}
 	
 	private void updateCollapsedTable() {
 		addOfferToRow(collapsedOffersTable, collapsedRowFormatter,
 				collapsedCellFormatter, 1, selectedOffer, true);
 //		if (offers.size() > 1) {
 //			addOfferToRow(collapsedOffersTable, collapsedRowFormatter,
 //					collapsedCellFormatter, 2, offers.get(1), false);
 //		}
 	}
 	
 	private void updateExpandedTable(boolean makeSelectedTopOffer) {
 		
 //		if (expandedOffersTable.getRowCount()-1 != offers.size()) {
 			// remove all but the first (header) row
 			while (expandedOffersTable.getRowCount() > 1) 
 				expandedOffersTable.removeRow(expandedOffersTable.getRowCount() - 1);
 //		}
 		
 		for (Offer offer : offers) {
 			int row = expandedOffersTable.getRowCount();
 			
 			if (offer.equals(selectedOffer)) {
 				if (makeSelectedTopOffer && row > 1) {
 					row = 1;
 					expandedOffersTable.insertRow(row);
 				}
 				addOfferToRow(expandedOffersTable, expandedRowFormatter,
 						expandedCellFormatter, row, offer, true);
 			} else {
 				addOfferToRow(expandedOffersTable, expandedRowFormatter,
 						expandedCellFormatter, row, offer, false);
 			}
 		}
 	}
 	
 	private void addOfferToRow(FlexTable table,
 			RowFormatter rowFormatter, FlexCellFormatter cellFormatter,
 			int row, Offer offer, boolean selected) {
 		
 		rowFormatter.setVerticalAlign(row, HasVerticalAlignment.ALIGN_MIDDLE);
 		if (offer != null) {
 			table.setText(row, 0, offer.getStoreName().getName());
 			cellFormatter.setStylePrimaryName(row, 0, style.leftAligned());
 			
 			table.setWidget(row, 1, new HTML(offer.getCondition()));
 			cellFormatter.setStylePrimaryName(row, 1, style.leftAligned());
 			cellFormatter.setWidth(row, 1, "100%");
 			table.setText(row, 2, NumberUtil.getDisplayPrice(offer.getPrice()));
 			table.setText(row, 3, NumberUtil.getDisplayPrice(offer.getShipping()));
 			table.setText(row, 4, NumberUtil.getDisplayPrice(offer.getTotalPrice()));
 			cellFormatter.setStylePrimaryName(row, 4, style.selectedOfferPrice());
 
 			HorizontalPanel buttons = new HorizontalPanel();
 			buttons.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 			buttons.setStylePrimaryName(style.offerButtonPanel());
 
 			buttons.add(new BuyOfferButton(book, offer, true));
 			table.setWidget(row, 5, buttons);
 		}
 	}
 	
 	public void updateSelected(Offer offer){
 		// offers table is expanded
 		selectedOffer = offer;
 		updateExpandedTable(false);
 		updateCollapsedTable();
 	}
 	
 	public Book getBook(){
 		return book;
 	}
 }
