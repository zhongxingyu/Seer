 package nz.co.searchwellington.filters;
 
 import javax.servlet.http.HttpServletRequest;
 
 import nz.co.searchwellington.geocoding.GeoCodeService;
 import nz.co.searchwellington.model.Geocode;
 
 import org.apache.log4j.Logger;
 
 public class LocationParameterFilter implements RequestAttributeFilter {
 
 	private static Logger log = Logger.getLogger(LocationParameterFilter.class);
 
 	public static final String LOCATION = "location";
 	public static String RADIUS = "radius";
 	
 	private static final String LONGITUDE = "longitude";
 	private static final String LATITUDE = "latitude";	
 	private GeoCodeService geoCodeService;
 	
 	public LocationParameterFilter(GeoCodeService geoCodeService) {
 		this.geoCodeService = geoCodeService;
 	}
 	
 	public void filter(HttpServletRequest request) {		
 		if(request.getParameter(LOCATION) != null) {
 			final String location = request.getParameter(LOCATION);
 			request.setAttribute("locationQuery", location);
 			Geocode resolvedGeocode = geoCodeService.resolveAddress(location);
 			if (resolvedGeocode != null && resolvedGeocode.isValid()) {
 				log.info("User supplied location '" + location + "' resolved to point: " + resolvedGeocode.getLatitude() + ", " + resolvedGeocode.getLongitude());				
 				request.setAttribute(LOCATION, resolvedGeocode);
 				
 			} else {
 				log.info("User supplied location '" + location + "' could not be resolved to a point; marking as invalid");
 				request.setAttribute(LOCATION, new Geocode(location));
 			}
 			return;
 		}
 		
 		Double latitude = processDoubleParameter(request, LATITUDE);
 		Double longitude = processDoubleParameter(request, LONGITUDE);
 		if (latitude != null && longitude != null) {
 			// TODO Should try todo a reverse lookup to name this location.
 			Geocode specificPointGeocode = new Geocode(latitude, longitude);
 			request.setAttribute(LOCATION, specificPointGeocode);
 		}
 		
		Double radius = processDoubleParameter(request, RADIUS);
		if (radius != null&& radius > 0) {
			request.setAttribute(RADIUS, radius);
		}
 	}
 	
 	private Double processDoubleParameter(HttpServletRequest request, String parameterName) {
 		if (request.getParameter(parameterName) != null) {
 			try {
 				return Double.parseDouble(request.getParameter(parameterName));
 			} catch (NumberFormatException e) {
 				log.warn("User supplied invalid double " + parameterName + " value: " + request.getParameter(parameterName));
 			}
 		}
 		return null;
 	}
 
 }
