 package com.veilingsite.client.widgets;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.veilingsite.client.controllers.UC;
 import com.veilingsite.shared.ServerService;
 import com.veilingsite.shared.ServerServiceAsync;
 import com.veilingsite.shared.domain.Auction;
 import com.veilingsite.shared.domain.Bid;
 
 public class AuctionBidWidget extends VerticalPanel {
 
 	private Auction auction;
 	private Button addBid = new Button("Place bid");
 	private TextBox bidamount = new TextBox();
 	private FlexTable table = new FlexTable();
 	private int countBids;
 	private FlexTable tableBids = new FlexTable();
 	private Label systemStatus = new Label();
 	private Timer systemStatusTimer;
 	private NumberFormat format = NumberFormat.getFormat( "#.##" );
 		
 		public AuctionBidWidget(Auction a) {
 			
 			Label title = new Label();
 			title.setText("Place a bid");
 			title.setStyleName("heading");
 			add(title);
 			
 			//add class for styling
 			this.addStyleName("widget");
 			
 			auction = a;
 			
 			systemStatusTimer = new Timer() {
 			      public void run() {
 						systemStatus.setVisible(false);
 			      }
 			 };
 			if(UC.getLoggedIn().getUserName().equals(a.getOwner().getUserName())){
 				HorizontalPanel ymboutbec = new HorizontalPanel();
 				Label ymboutbe = new Label("You cannot bid on your own auctions.");
 				ymboutbe.setStyleName("status");
 				ymboutbec.add(ymboutbe);			
 				
 				bidamount.setEnabled(false);
 				addBid.setEnabled(false);
 				
 				table.setWidget(0, 1, ymboutbec);
 				table.setWidget(1, 0, new Label("Amount: "));
 				table.setWidget(1, 1, bidamount);
 				table.setWidget(2, 1, addBid);
 			}else if(UC.getLoggedIn() == null){
 				HorizontalPanel ymbliec = new HorizontalPanel();
 				Label ymblie = new Label("You must be logged in to bid on auctions.");
 				ymblie.setStyleName("status");
 				ymbliec.add(ymblie);			
 				
 				bidamount.setEnabled(false);
 				addBid.setEnabled(false);
 				
 				table.setWidget(0, 1, ymbliec);
 				table.setWidget(1, 0, new Label("Amount: "));
 				table.setWidget(1, 1, bidamount);
 				table.setWidget(2, 1, addBid);
 			}else{
 				table.setWidget(0, 0, new Label("Amount: "));
 				table.setWidget(0, 1, bidamount);
 				table.setWidget(1, 1, addBid);
 			}
 			
 			
 			tableBids.setWidget(0, 0, new Label("User"));
 			tableBids.setWidget(0, 1, new Label("Amount"));
 			
 			ArrayList<Bid> bids = auction.getBidList();
 			countBids = bids.size()+2;
 			Collections.sort(bids);
 			for(Bid b : bids) {
 				tableBids.setWidget(countBids, 0, new Label(b.getMyUser().getUserName()));
 				tableBids.setWidget(countBids, 1, new Label(format.format(b.getAmount())));
 				countBids = countBids - 1;
 			}
 			
 			if(bids.isEmpty()) {
 				tableBids.setWidget(1, 0, new Label("None was found"));
 			}
 			
 			add(table);
 			add(new Label("Last bids:"));
 			add(tableBids);
 			add(systemStatus);
 			
 			addBid.addClickHandler(new ClickHandler(){
 				@Override
 				public void onClick(ClickEvent event) {
 					systemStatusTimer.cancel();
 					double d;
 					try {
 						d = Double.parseDouble(bidamount.getText());
 						bidamount.setText(format.format(d));
 						d = Double.parseDouble(bidamount.getText());
 					} catch(NumberFormatException nfe) {
 						systemStatus.setText("Bid must be a numerical value");			
 						systemStatus.setStyleName("error");
 						systemStatus.setVisible(true);
 						systemStatusTimer.schedule(3000);
 						return;
 					}
 					if(UC.getLoggedIn() == null)
 						return;
 					if(auction.getHighestBid() == null) {
 						if(auction.getStartAmount() > d) {
 							systemStatus.setText("Bid must be higher than start amount");			
 							systemStatus.setStyleName("error");
 							systemStatus.setVisible(true);
 							systemStatusTimer.schedule(3000);
 						} else {
 							addBid(new Bid(UC.getLoggedIn(),d,auction));
 						}
 					} else {
 						if(auction.getHighestBid().getAmount()+1 > d) {
 							systemStatus.setText("Bid must be higher than highest bid");			
 							systemStatus.setStyleName("error");
 							systemStatus.setVisible(true);
 							systemStatusTimer.schedule(3000);
 						} else {
 							addBid(new Bid(UC.getLoggedIn(),d,auction));
 						}
 					}
 				}
 			});
 			
 			
 		}
 		
 		private void addBid(Bid b) {
 			ServerServiceAsync myService = (ServerServiceAsync) GWT.create(ServerService.class);
 			AsyncCallback<Bid> callback = new AsyncCallback<Bid>() {		
 				@Override
 				public void onFailure(Throwable caught) {}	
 				
 				@Override
 				public void onSuccess(Bid result) {
 					updateAuction(result);
 				}
 			};
 			myService.addBid(b, callback);	
 		}
 		
 		private void updateAuction(Bid b){	
 			ServerServiceAsync myService = (ServerServiceAsync) GWT.create(ServerService.class);
 			AsyncCallback<Void> callback = new AsyncCallback<Void>() {		
 				@Override
 				public void onFailure(Throwable caught) {}	
 				
 				@Override
 				public void onSuccess(Void result) {
					tableBids.clearCell(1, 0);
 					systemStatus.setText("Bid has been placed succesfully");			
 					systemStatus.setStyleName("succesfull");
 					systemStatus.setVisible(true);
 					systemStatusTimer.schedule(3000);
 					tableBids.setWidget(countBids, 0, new Label(UC.getLoggedIn().getUserName()));
 					tableBids.setWidget(countBids, 1, new Label(bidamount.getText()));
 					countBids = countBids - 1;
 					refreshInput();
 				}
 			};
 			auction.addBid(b);
 			myService.updateAuction(auction, callback);
 		}
 		
 		private void refreshInput() {
 			bidamount.setText("");
 		}
 }
