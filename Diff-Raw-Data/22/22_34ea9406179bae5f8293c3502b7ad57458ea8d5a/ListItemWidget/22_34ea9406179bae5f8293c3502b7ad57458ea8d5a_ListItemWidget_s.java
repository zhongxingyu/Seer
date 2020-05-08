 package me.deal.client.view.main;
 
 import java.util.Date;
 
 import me.deal.shared.BusinessInfo;
 import me.deal.shared.Deal;
 import me.deal.shared.Location;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 
 public class ListItemWidget extends Composite {
 
 	private static ListItemWidgetUiBinder uiBinder = GWT
 			.create(ListItemWidgetUiBinder.class);
 
 	interface ListItemWidgetUiBinder extends UiBinder<Widget, ListItemWidget> {
 	}
 	
 	private Deal deal;
 	private BusinessInfo businessInfo;
 	final int MILLSECS_PER_DAY = 86400000;
 
 	public ListItemWidget(final Deal deal, final BusinessInfo businessInfo) {
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		this.deal = deal;
 		this.businessInfo = businessInfo;
		intialize();
 	}
 
 	//@UiField
 	//Button formatMapButton;
 	@UiField
 	Image dealImage;
 	@UiField
 	Label dealTitle;
 	@UiField
 	Label dealSubtitle;
 	
 	@UiField
 	Image avgRating;
 	@UiField
 	Anchor numReviews;
 	@UiField
 	Label businessName;
 	@UiField
 	Label addressLine1;
 	@UiField
 	Label addressLine2;
 	@UiField
 	Button getDirectionsButton;
 	
 	@UiField
 	Label dealPrice;
 	@UiField
 	Label discountPercentage;
 	
 	@UiField
 	Label numDaysLeft;
 	@UiField
 	Label daysText;
 	@UiField
 	Label dealSource;
 	@UiField
 	Image avgYelpRating;
 	
 	
 	@SuppressWarnings("deprecation")
	private void intialize() {
 		/*
 		 * TODO: Use the data from the deal and businessInfo variables to fill in the
 		 * UI with the relevant data.  Add UiHandlers to handle clicks on buttons.
 		 * For the formatMapButton you will have to use the eventBus to signal to
 		 * the MapWidget the change.  Use the DirectionsService to get directions.
 		 * After getting directions, dynamically add the directions to the bottom of
 		 * the ListItem and add a close button so that the user can easily close
 		 * get rid of the directions.
 		 */
 		
 		
 		
		String yelpRatingURL= deal.getDealBusinessInfo().getAvgRatingImageUrl();
 		
 		String url = deal.getBigImageUrl();
 		Location dealAddr = deal.getBusinessAddress();
 		dealImage.setUrl(deal.getBigImageUrl());
 		dealTitle.setText(deal.getTitle());
 		dealTitle.addStyleName("titleStyle");
 		dealSubtitle.setText(deal.getSubtitle());
		businessName.setText(deal.getDealBusinessInfo().getName());
 		addressLine1.setText(deal.getBusinessAddress().getAddress());
 		addressLine2.setText(dealAddr.getCity() + "," + dealAddr.getState());
 		System.out.println("City: " + dealAddr.getCity());
 				
 		dealPrice.setText(deal.getPrice().toString());
 		discountPercentage.setText(deal.getDiscountPercentage() + "%");
 		dealSource.setText("TempDealSource");		
 		
 		//parse date strings to determine how many days are left compared to current date
 		Date today = new Date();
 		String endDate = deal.getEndDate();
 		Date endDay = new Date();
 		endDay.setYear(Integer.parseInt(endDate.substring(0, 4))-1900);
 		endDay.setMonth(Integer.parseInt(endDate.substring(5, 7))-1);
 		endDay.setDate(Integer.parseInt(endDate.substring(8, 10)));
 		long deltaDays = (endDay.getTime() - today.getTime()) / MILLSECS_PER_DAY;		
 		numDaysLeft.setText(String.valueOf(deltaDays));
 		//set days to plural if needed
 		if (deltaDays > 1){
 			daysText.setText("\t     days left on");
 		}
 		else {
 			daysText.setText("\tday left on ");
 		}		
 		
 		if(yelpRatingURL!=null)
 			avgYelpRating.setUrl(deal.getDealBusinessInfo().getAvgRatingImageUrl());
 	}
 }
