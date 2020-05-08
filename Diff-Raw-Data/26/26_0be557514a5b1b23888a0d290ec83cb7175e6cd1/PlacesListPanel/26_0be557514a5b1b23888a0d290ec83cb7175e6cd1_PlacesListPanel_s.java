 package croo.szakdolgozat.client.presenter;
 
 import java.util.ArrayList;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.maps.client.InfoWindow;
 import com.google.gwt.maps.client.InfoWindowContent;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import croo.szakdolgozat.client.view.ElementCreatorView;
 import croo.szakdolgozat.client.view.InterestingPlacePopup;
 import croo.szakdolgozat.shared.InterestingPlace;
 import croo.szakdolgozat.shared.Town;
 
 public class PlacesListPanel extends VerticalPanel
 {
 	public PlacesListPanel(final Town endTown, final InfoWindow infoWindow)
 	{
 		final ArrayList<InterestingPlace> places = endTown.getInterestingPlaces();
 		for (final InterestingPlace place : places) {
 			if (place != Town.EMPTY_PLACE)
 				add(listElement(endTown.getTownCoordinateInJSO(), infoWindow, place));
 			else
 				add(emptyElement());
 		}
 		add(newInterestingPlaceButton(endTown.getTownCoordinateInJSO(), infoWindow));
 	}
 
 	private HTML newInterestingPlaceButton(final LatLng coordinateInJSO, final InfoWindow infoWindow)
 	{
 		final HTML elementCreatorButton = new HTML("Hozzáadás...");
 		elementCreatorButton.setStylePrimaryName("placeButton-link");
 
 		elementCreatorButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				InfoWindowContent content = new InfoWindowContent(PlacesListPanel.this);
 				content.setMaxContent(new ElementCreatorView());
 				infoWindow.open(coordinateInJSO, content);
 				infoWindow.maximize();
 			}
 		});
 		return elementCreatorButton;
 	}
 
 	private HTML emptyElement()
 	{
 		final HTML emptyElement = new HTML(Town.EMPTY_PLACE.getName());
 		emptyElement.setStylePrimaryName("placeButton-readonly");
 		return emptyElement;
 	}
 
	public Widget createMaxContent(InterestingPlace place)
 	{
 		Widget interestingPage = new InterestingPlacePopup(place.getName(), place.getDescription(), place.getURL(),
 				place.getImageUrl());
 		interestingPage.setHeight("98%");
 		interestingPage.setWidth("98%");
 		return interestingPage;
 	}
 
 	private HTML listElement(final LatLng coordinateInJSO, final InfoWindow infoWindow, final InterestingPlace place)
 	{
 		final HTML listElement = new HTML(place.getName());
 		listElement.setStylePrimaryName("placeButton");
 		addHoverEffectToElement(listElement);
 
 		listElement.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				InfoWindowContent content = new InfoWindowContent(PlacesListPanel.this);
 				content.setMaxContent(createMaxContent(place));
 				infoWindow.open(coordinateInJSO, content);
 				infoWindow.maximize();
 			}
 		});
 		return listElement;
 	}
 
 	private void addHoverEffectToElement(final HTML listElement)
 	{
 		listElement.addMouseOverHandler(new MouseOverHandler() {
 			@Override
 			public void onMouseOver(MouseOverEvent event)
 			{
 				listElement.setStyleName("placeButton-hover", true);
 			}
 		});
 		listElement.addMouseOutHandler(new MouseOutHandler() {
 
 			@Override
 			public void onMouseOut(MouseOutEvent event)
 			{
 				listElement.setStyleName("placeButton-hover", false);
 			}
 		});
 	}
 }
