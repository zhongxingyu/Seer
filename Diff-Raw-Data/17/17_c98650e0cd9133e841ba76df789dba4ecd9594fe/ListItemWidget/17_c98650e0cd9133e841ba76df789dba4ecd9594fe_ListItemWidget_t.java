 package me.deal.client.view.main;
 
 import java.util.Date;
 
 import me.deal.client.events.DealsEvent;
 import me.deal.client.model.Deals;
 import me.deal.shared.BusinessInfo;
 import me.deal.shared.Deal;
 import me.deal.shared.LatLngCoor;
 import me.deal.shared.Location;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerManager;
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
 	final int MILLSECS_PER_DAY = 86400000;
 	
 	public ListItemWidget() {
 		initWidget(uiBinder.createAndBindUi(this));
 		this.deal = new Deal();
 	}
 	
 	public ListItemWidget(final Deal deal) {
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		this.deal = deal;
 		initialize();
 	}
 
 	@UiField
 	//Button formatMapButton;
 	Image formatMapButton;
 	@UiField
 	Image dealImage;
 	@UiField
 	Anchor dealTitle;
 	// @UiField
 	// Label dealSubtitle;
 	
 	@UiField
 	Image avgRating;
 	@UiField
 	Anchor numReviews;
 	@UiField
 	Anchor businessName;
 	@UiField
 	Label addressLine1;
 	@UiField
 	Label addressLine2;
 	//@UiField
 	//Button getDirectionsButton;
 	@UiField
 	Anchor getDirectionsLink;
 	
 	@UiField
 	Anchor facebookLink;
 	@UiField
 	Anchor twitterLink;
 	@UiField
 	Anchor googlePlusLink;
 	
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
 	private void initialize() {
 		/*
 		 * TODO: Use the data from the deal and businessInfo variables to fill in the
 		 * UI with the relevant data.  Add UiHandlers to handle clicks on buttons.
 		 * For the formatMapButton you will have to use the eventBus to signal to
 		 * the MapWidget the change.  Use the DirectionsService to get directions.
 		 * After getting directions, dynamically add the directions to the bottom of
 		 * the ListItem and add a close button so that the user can easily close
 		 * get rid of the directions.
 		 */
 		
 		if (deal.getDealBusinessInfo() != null)
 		{
 			setAvgRatingImageUrl(deal.getDealBusinessInfo().getAvgRatingImageUrl());
 			setBusinessName(deal.getDealBusinessInfo().getName());
 			setNumReviews(deal.getDealBusinessInfo().getNumReviews());
 			setReviewsUrl(deal.getDealBusinessInfo().getWebUrl());
 		}
 		
 		setTitle(deal.getTitle());
 		//setSubtitle(deal.getSubtitle());
 		setPrice(deal.getPrice());
 		setDiscountPercentage(deal.getDiscountPercentage());
 		setBusinessAddress(deal.getBusinessAddress());
 		setBigImageUrl(deal.getBigImageUrl());
 		setYipitUrl(deal.getYipitWebUrl());
 		setEndDate(deal.getEndDate());
 		setDealSource(deal.getDealSource());
 	}
 	
 	public void setMapButton(final int i, final HandlerManager eventBus){
 		
 		formatMapButton.setTitle("Test title");
 		if(i < 26)
 			formatMapButton.setUrl("http://www.google.com/mapfiles/marker" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(i,i+1) + ".png" );
		//formatMapButton.setUrl(deal.getIDUrl());
 		formatMapButton.addClickHandler(new ClickHandler() {
 			      @Override
 			      public void onClick(ClickEvent event) {
 			    	  
 			       System.out.println("clicked button " + String.valueOf(i));
 			       Deals deals = Deals.getInstance();
 			       Location loc = new Location();
 			       loc.setLatLng(deal.getBusinessAddress().getLatLng());
 			       deals.setLocation(loc);
 					
 					eventBus.fireEvent(new DealsEvent());
 			        
 			      }
 			    });
 	}
 	
 	public void setAvgRatingImageUrl(String avgRatingImageUrl) {
 		if(deal.getDealBusinessInfo() == null)
 			deal.setDealBusinessInfo(new BusinessInfo());
 		deal.getDealBusinessInfo().setAvgRatingImageUrl(avgRatingImageUrl);
 		if(deal.getDealBusinessInfo().getAvgRatingImageUrl() != null)
 			avgYelpRating.setUrl(deal.getDealBusinessInfo().getAvgRatingImageUrl());
 	}
 	
 	public void setBusinessName(String businessNameStr) {
 		if(deal.getDealBusinessInfo() == null)
 			deal.setDealBusinessInfo(new BusinessInfo());
 		deal.getDealBusinessInfo().setName(businessNameStr);
 		businessName.setText(deal.getDealBusinessInfo().getName());		
 	}
 	
 	public void setNumReviews(Integer numReviewsInt) {
 		if(deal.getDealBusinessInfo() == null)
 			deal.setDealBusinessInfo(new BusinessInfo());
 		deal.getDealBusinessInfo().setNumReviews(numReviewsInt);
		if(numReviewsInt != null) {
			String reviewText = numReviewsInt == 1 ? "Review" : "Reviews";
			numReviews.setText(deal.getDealBusinessInfo().getNumReviews() + " " + reviewText);
		}			
 	}
 	
 	public void setReviewsUrl(String reviewsUrl) {
 		if(deal.getDealBusinessInfo() == null)
 			deal.setDealBusinessInfo(new BusinessInfo());
 		deal.getDealBusinessInfo().setWebUrl(reviewsUrl);
 		numReviews.setHref(deal.getDealBusinessInfo().getWebUrl());
 		numReviews.setTarget("_blank");
 	}
 	
 	public void setBusinessAddress(Location businessAddress) {
 		deal.setBusinessAddress(businessAddress);
 		addressLine1.setText(deal.getBusinessAddress().getAddress());
 		addressLine2.setText(deal.getBusinessAddress().getCity() + ", " +
 				deal.getBusinessAddress().getState());
 		
 		LatLngCoor latlng = deal.getBusinessAddress().getLatLng();
 		Location userLoc = Deals.getInstance().getUserLocation();
 		LatLngCoor userLatLng = userLoc.getLatLng(); 
 		String directionsURL = "http://maps.google.com/maps?saddr="
 				+ userLatLng.getLatitude() + "," +  userLatLng.getLongitude() +
 				"&daddr=" +latlng.getLatitude()+","+latlng.getLongitude();
 		getDirectionsLink.setHref(directionsURL);
 		getDirectionsLink.setText("Directions");
 	}
 	
 	public void setBigImageUrl(String bigImageUrl) {
 		deal.setBigImageUrl(bigImageUrl);
 		dealImage.setUrl(deal.getBigImageUrl());
 		int centeredLeft = dealImage.getWidth() > 300 ? (dealImage.getWidth() - 300)/2 : 0;
 		int centeredTop = dealImage.getHeight() > 200 ? (dealImage.getHeight() - 200)/2 : 0;		
 		dealImage.setVisibleRect(centeredLeft, centeredTop, 300, 200);
 	}
 	
 	public void setSubtitle(String subtitle) {
 		deal.setSubtitle(subtitle);
 		dealTitle.setText(deal.getSubtitle());
 	}
 	
 	public void setTitle(String title) {
 		deal.setTitle(title);
 	//	dealSubtitle.setText(deal.getTitle());
 	}
 	
 	public void setYipitUrl(String yipitWebUrl) {
 		deal.setYipitWebUrl(yipitWebUrl);
 		dealTitle.setHref(deal.getYipitWebUrl());
 		dealTitle.setTarget("_blank");
 		businessName.setHref(yipitWebUrl);
 		
 		//social media integration
 		String yipitURL= deal.getYipitWebUrl();
 		String facebookLinkURL="https://www.facebook.com/sharer.php?u=" + yipitURL;
 		String twitterLinkURL="http://twitter.com/share?count=horiztonal&url=" + yipitURL + "&text="
 				+ (deal.getTitle() == null ? "" : deal.getTitle());
 		String googlePlusLinkURL="https://plusone.google.com/_/+1/confirm?hl=en-US&url=" + yipitURL;
 		facebookLink.setHref(facebookLinkURL);
 		Image facebookImage = new Image("images/facebook.png");
 		if(facebookLink.getElement().getChildCount()==0) // add image only if not existing
 			facebookLink.getElement().appendChild(facebookImage.getElement());
 		
 		twitterLink.setHref(twitterLinkURL);
 		
 		Image twitterImage = new Image("images/twitter.png");
 		if(twitterLink.getElement().getChildCount()==0)
 			twitterLink.getElement().appendChild(twitterImage.getElement());
 		googlePlusLink.setHref(googlePlusLinkURL);
 		Image googlePlusImage= new Image("images/google.png");
 		if(googlePlusLink.getElement().getChildCount()==0)
 			googlePlusLink.getElement().appendChild(googlePlusImage.getElement());
 	}
 	
 	public void setPrice(Double price) {
 		deal.setPrice(price);
 		dealPrice.setText(deal.getPrice() + "");
 	}
 	
 	public void setDiscountPercentage(Double discountPercentageDbl) {
 		deal.setDiscountPercentage(discountPercentageDbl);
 		discountPercentage.setText(deal.getDiscountPercentage() + "%");
 	}
 	
 	public void setEndDate(String endDate) {
 		//parse date strings to determine how many days are left compared to current date
 		Date today = new Date();
 		deal.setEndDate(endDate);
 		Date endDay = new Date();
 		endDay.setYear(Integer.parseInt(endDate.substring(0, 4))-1900);
 		endDay.setMonth(Integer.parseInt(endDate.substring(5, 7))-1);
 		endDay.setDate(Integer.parseInt(endDate.substring(8, 10)));
 		long deltaDays = (endDay.getTime() - today.getTime()) / MILLSECS_PER_DAY;		
 		numDaysLeft.setText(String.valueOf(deltaDays));
 		//set days to plural if needed
 		if (deltaDays > 1){
 			daysText.setText("\tdays left on");
 		}
 		else {
 			daysText.setText("\tday left on ");
 		}
 	}
 	
 	public void setDealSource(String dealSourceStr) {
 		deal.setDealSource(dealSourceStr);
 		dealSource.setText(deal.getDealSource());
 	}
 }
