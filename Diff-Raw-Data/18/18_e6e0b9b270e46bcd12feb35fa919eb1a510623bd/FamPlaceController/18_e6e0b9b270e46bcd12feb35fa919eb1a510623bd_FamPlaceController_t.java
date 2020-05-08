 package org.fam.jsf.controller;
 
 import com.google.code.geocoder.Geocoder;
 import com.google.code.geocoder.GeocoderRequestBuilder;
 import com.google.code.geocoder.model.GeocodeResponse;
 import com.google.code.geocoder.model.GeocoderRequest;
 import com.google.code.geocoder.model.GeocoderResult;
 import lombok.Getter;
 import lombok.Setter;
 import org.fam.ejb.model.FamPlace;
 import org.fam.ejb.session.FamPlaceFacade;
 import org.primefaces.event.map.OverlaySelectEvent;
 import org.primefaces.model.map.DefaultMapModel;
 import org.primefaces.model.map.LatLng;
 import org.primefaces.model.map.MapModel;
 import org.primefaces.model.map.Marker;
 import org.slf4j.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.inject.Inject;
 import java.io.IOException;
 import java.util.List;
 
 @ManagedBean(name = "famPlaceController")
 @ViewScoped
 @Getter
 @Setter
 public class FamPlaceController extends AbstractController<FamPlace> {
 
     @Inject
     private Logger LOGGER;
     @Inject
     private FamPlaceFacade ejbFacade;
 
     private MapModel simpleModel = new DefaultMapModel();
     private Marker marker;
 
     public FamPlaceController() {
     }
 
     @PostConstruct
     private void postConstruct() {
 
 //        findAll();
     }
 
     @PreDestroy
     private void preDestroy() {
 
     }
 
     @Override
     public FamPlace getSelected() {
         if (current == null) {
             current = new FamPlace();
             selectedItemIndex = -1;
         }
         return current;
     }
 
     @Override
     public FamPlaceFacade getFacade() {
         return ejbFacade;
     }
 
     @Override
     public String prepareEdit() {
         id = current.getIdPlace();
         return "pretty:editPlace";
     }
 
     @Override
     public String prepareView() {
         id = current.getIdPlace();
         return "pretty:viewPlace";
     }
 
     @Override
     public String prepareList() {
         return "pretty:listPlace";
     }
 
     @Override
     public String prepareCreate() {
         current = new FamPlace();
         selectedItemIndex = -1;
         return "pretty:createPlace";
     }
 
     //    public String geocode() {
 //        
 //        final String fullAdress = current.getFullAddress();
 //        geocoder.getLatLng(fullAdress, new LatLngCallback() {
 //            
 //            @Override
 //            public void onFailure() {
 //
 //            }
 //            
 //            @Override
 //            public void onSuccess(LatLng point) {
 //                
 //                current.setLatitude(point.getLatitude());
 //                current.setLongitude(point.getLongitude());
 //                
 //            }
 //        });
 //        return null;
 //    }
     public MapModel getSimpleModel() {
         if (current.getLatitude() == null) {
             simpleModel = null;
         } else {
             LatLng coord = new LatLng(current.getLatitude().doubleValue(), current.getLongitude().doubleValue());
             marker = new Marker(coord, current.getLibPlace());
 //            marker.setDraggable(true);
             simpleModel.addOverlay(marker);
         }
 
         return simpleModel;
     }
 
     public void onMarkerSelect(OverlaySelectEvent event) {
         LOGGER.info("BEFORE " + marker.getLatlng().toString());
         this.marker = (Marker) event.getOverlay();
         LOGGER.info("AFTER " + marker.getLatlng().toString());
     }
 
     public String getLatLong() throws IOException {
         String details;
 //        if (current.getFullAddress().isEmpty()) {
         StringBuilder builder = new StringBuilder();
         builder.append(current.getAddress()).append(",").append(current.getZipcode()).append(",").append(current.getCity());
         details = builder.toString();
 //        } else {
 //            details = current.getFullAddress();
 //        }
         LOGGER.debug("adresse:" + details);
 
         Geocoder geocoder = new Geocoder();
         GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress(details).getGeocoderRequest();
 //       setLanguage("en")
         GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
 
         List<GeocoderResult> results = geocoderResponse.getResults();
         for (GeocoderResult result : results) {
             current.setLatitude(result.getGeometry().getLocation().getLat());
             current.setLongitude(result.getGeometry().getLocation().getLng());
         }
 
         LatLng coord = new LatLng(current.getLatitude().doubleValue(), current.getLongitude().doubleValue());
         if (coord == null) {
 
             coord = new LatLng(42.0, 1.43);
         }
         if (simpleModel == null) {
 
             simpleModel = new DefaultMapModel();
         }
        simpleModel.addOverlay(new Marker(coord, current.getLibPlace()));
 
 //        String msg = "OK!";
 //        FacesContext.getCurrentInstance().addMessage(null, 
 //                    new FacesMessage(FacesMessage.SEVERITY_INFO, this.getClass().getName(), msg));
        return PRETTY_PREFIX;//"map";
     }
 
 }
