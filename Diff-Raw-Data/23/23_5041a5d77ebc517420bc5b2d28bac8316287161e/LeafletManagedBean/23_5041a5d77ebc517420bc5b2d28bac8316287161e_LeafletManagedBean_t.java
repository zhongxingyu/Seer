 package org.risney.adf.view;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 
 import oracle.adf.model.BindingContext;
 import oracle.adf.view.rich.component.rich.input.RichInputText;
 import oracle.adf.view.rich.context.AdfFacesContext;
 import oracle.adf.view.rich.render.ClientEvent;
 
 import oracle.binding.BindingContainer;
 import oracle.binding.OperationBinding;
 
 import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
 import org.apache.myfaces.trinidad.util.Service;
 
 import org.risney.adf.model.Locations;
 import org.risney.adf.model.Locations.Location;
 
 
 public class LeafletManagedBean {
     private RichInputText locationInputText;
 
     public LeafletManagedBean() {
     }
 
     /**
      *
      * @param searchString
      * @return List<javax.faces.model.SelectItem>
      *
      * An array is created from candiates, derived  from the
      * the dataControl method 'fuzzySearch' passing, in whatever searchString is keyed
      * into the RichInputText locationInputText. 
      * Not much to see here, this is standard AutoComplete JSF pattern.
      * 
      */
     public List getSuggestedLocations(String searchString) {
         List<SelectItem> suggestedLocations = new ArrayList<SelectItem>();
         BindingContainer bindings = BindingContext.getCurrent().getCurrentBindingsEntry();
         OperationBinding operationBinding = bindings.getOperationBinding("fuzzySearch");
         operationBinding.getParamsMap().put("searchString", searchString);
         operationBinding.execute();
 
         Locations locations = (Locations)operationBinding.getResult();
         if ((null != locations) && (null != locations.getLocations())) {
             for (Locations.Location location : locations.getLocations()) {
                 if (null != location) {
                     suggestedLocations.add(new SelectItem(location.getPlaceName()));
                 }
             }
         }
         return suggestedLocations;
     }
 
     /**
      *
      * @param valueChangeEvent
      * Capture the auto-completed value,
      * executing the DataControl method 'getLocationAndWeatherWithPlaceName'
      * getting back a Location object.
      * Then calling the method 'zoomToLocation' in adf-leaflet.js
      *
      */
     public void onLocationSearchValueChange(ValueChangeEvent valueChangeEvent) {
         if (valueChangeEvent.getNewValue() != null) {
             String placeName = valueChangeEvent.getNewValue().toString();
             BindingContainer bindings = BindingContext.getCurrent().getCurrentBindingsEntry();
             OperationBinding operationBinding = bindings.getOperationBinding("getLocationAndWeatherWithPlaceName");
             operationBinding.getParamsMap().put("placeName", placeName);
             operationBinding.execute();
             Location location = (Location)operationBinding.getResult();
             executeJavaScript("zoomToLocation('" + location.toJSON() + "')");
         }
     }
 
     /**
      *
      * @param clientEvent
      *
      * This is essentially a proxy method, that the AJAX/XMLHTTPRequest passes information into
      * in ad-leaflet.js, the method onMapClick passes in the coordinates as a Map of Double
      * coordinates. Then the data control method is called 'getLocationAndWeatherWithCoordinates'
      * returning the geonames.org webservice data (the location/ the weather). Then a JSON object
      * is passed back to the  ad-leaflet.js method 'zoomToLocation'
      * To some degree this could be considered unnecessary newtork hops. But this is more for demonstration
      * purposes. This pattern could be used for integrating enterprise data with a .js framwework.
      * 
      */
 
     public void serverEventHandler(ClientEvent clientEvent) {
 
         // Retrieve the parameters passed in from the adf-leaflet.js method onMapClick
         Map LatLong = (Map)clientEvent.getParameters().get("LatLong");
         Double latitude = (Double)LatLong.get("lat");
         Double longitude = (Double)LatLong.get("lng");
      
         // Execute the DataControl method 'getLocationAndWeatherWithCoordinates', getting back a Location object
         BindingContainer bindings = BindingContext.getCurrent().getCurrentBindingsEntry();
         OperationBinding operationBinding = bindings.getOperationBinding("getLocationAndWeatherWithCoordinates");
         operationBinding.getParamsMap().put("latitude", latitude);
         operationBinding.getParamsMap().put("longitude", longitude);
         operationBinding.execute();
         Location location = (Location)operationBinding.getResult();
 
         // Refresh the adf input text
         AdfFacesContext adfFacesContext = AdfFacesContext.getCurrentInstance();
         locationInputText.setValue(location.getPlaceName());
         adfFacesContext.addPartialTarget(locationInputText);
 
         // Execute the javascript
         String javascriptCommand = "zoomToLocation('" + location.toJSON() + "')";
         executeJavaScript(javascriptCommand);
     }
 
     /**
      *
      * @param script
      *
      * This generic helper method, allows arbitray javascript to be executed from the
      * managed bean, instead of a specific Rich Faces Partial Page Rendering, the
      * ExtendedRenderKitService allows methods to call any third party JS methods.
      * Extremely useful method.
      * example: executeJavaScript("alert('hello world')");
      * 
      */
     public void executeJavaScript(final String script) {
         ExtendedRenderKitService service = (ExtendedRenderKitService)Service.getRenderKitService(FacesContext.getCurrentInstance(), ExtendedRenderKitService.class);
         service.addScript(FacesContext.getCurrentInstance(), script);
     }
 
 
     public void setLocationInputText(RichInputText locationInputText) {
         this.locationInputText = locationInputText;
     }
 
     public RichInputText getLocationInputText() {
         return locationInputText;
     }
 }
