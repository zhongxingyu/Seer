 package org.bluemagic.config.service.controller;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.bluemagic.config.api.service.PropertyService;
 import org.bluemagic.config.service.utils.ServletUtils;
 import org.bluemagic.config.service.utils.TagUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 @RequestMapping(value="/property")
 @Controller
 public class PropertyController {
 
     private static final Log LOG = LogFactory.getLog(PropertyController.class);
 	
 	@Autowired
 	private PropertyService propertyService;
 	
 	/**
 	 * The Default method for resolving properties.  This method will return back
 	 * the value of the property as a String.
 	 */
 	@RequestMapping(value="/**", method=RequestMethod.GET)
 	public @ResponseBody ResponseEntity<String> getProperty(HttpServletRequest request) {
 		
 		ResponseEntity<String> result = null;
 		
 		String property = getPropertyFromRequest(request);
 
 		URI propertyURI = generateUriFromProperty(property); // read takes a URI as a parameter
 		
 		// Retrieve the value for this Property.
 		String rval = propertyService.read(propertyURI);
 		
 		if (LOG.isInfoEnabled()) {
 			LOG.info("[GET] Resolved value: " + property + " ----> " + rval);
 		}
 
 		if (rval != null) {
 			
 			result = new ResponseEntity<String>(rval, HttpStatus.OK);
 		} else {
 			
 			result = new ResponseEntity<String>(HttpStatus.NOT_FOUND);
 		}
 
 		return result;
 	}
 	
 	/**
 	 * This method handles all requests to create a new property on the server.  The URL
 	 * itself is the property, and the value should be in the header.
 	 */
 	@RequestMapping(value="/**", method=RequestMethod.POST)
 	public @ResponseBody ResponseEntity<String> createProperty(HttpServletRequest request) {
 		
 		// This will be the response sent back to the agent.
 		ResponseEntity<String> result = null;
 		
 		String value = request.getParameter("propertyValue");
 		
 		if (value != null) {
 			
 			// Parse the HttpServletRequest to get the property out.
 			String property = getPropertyFromRequest(request);
 			
 			URI propertyURI = generateUriFromProperty(property); // read takes an URI as a parameter
 			
 			// Try to create the property.
 			boolean rval = propertyService.create(propertyURI, value);
 			
 			if (rval) {
 				
 				result = new ResponseEntity<String>(HttpStatus.CREATED);
 			} else {
 				
 				result = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
 			}
 			
 		} else {
 			
 			result = new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * This method handles all requests to remove a property from the server.
 	 */
 	@RequestMapping(value="/**", method=RequestMethod.DELETE)
 	public @ResponseBody ResponseEntity<String> deleteProperty(HttpServletRequest request) {
 		String property = getPropertyFromRequest(request);
 		
 		if (LOG.isInfoEnabled()) {
 			LOG.info("[DELETE] Deleted property: " + property.toString());
 		}
 		
 		// see http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
 		// 200 (OK) If the response includes an entity describing the status.
 		// 202 (ACCEPTED) If the action has not yet been enacted.
 		// 204 (NO CONTENT) If the action has been enacted but the response does not include an entity.
 		return new ResponseEntity<String>(HttpStatus.ACCEPTED);
 	}
 	
 	/**
 	 * This method handles all requests to update a property with new values.
 	 */
 	@RequestMapping(value="/**", method=RequestMethod.PUT)
 	public @ResponseBody ResponseEntity<String> updateProperty(@RequestBody String json) {
 		if (LOG.isInfoEnabled()) {
 			LOG.info("[PUT] Updating property: " + json);
 		}
 		
 		return new ResponseEntity<String>(json, HttpStatus.OK);
 	}
 	
 	/**
 	 * Helper method that extracts the full property name from the HTTP Request
 	 * object.
 	 * 
 	 * @param request
 	 *                 The Request to parse.
 	 * @return
 	 *                 A string that represents the property along with its
 	 *                 tags ordered and appended on.
 	 */
 	private String getPropertyFromRequest(HttpServletRequest request) {
 		
 		// Get the base property from the URI.
 		String baseProperty = ServletUtils.getProperty(request, "/property/");
 		
 		// Get the tags if there are any.
 		String orderedTags = getTagParameters(request);
 		
 		// Start building the full Property URI by adding on the tags.
 		StringBuilder property = new StringBuilder(baseProperty);
 		
 		// Append the tags if there are any.
 		if (orderedTags != null) {
 			property.append("?");
 			property.append(orderedTags);
 		}
 		return property.toString();
 	}
 	
         /**
 	 * Wrapper  method for the constructor URI(String str), creating a URI from a String. 
 	 * This function serves to catch the URISyntaxException error and handle it appropriately.
 	 * Catching the URISyntaxException is required to use URI constructors.
 	 * 
 	 * @param property
 	 *                 The property to convert to a URI.
 	 * @return
 	 *                 A URI that represents the property along with its
 	 *                 tags ordered and appended on.
 	 *                 Returns null if the conversion fails for any reason.
 	 **/
 
         private URI generateUriFromProperty(String property) {
 	    try {
 		
 		URI uri = new URI(property);
 
 		return uri;
 
 	    }
 	    catch (URISyntaxException uriException) {
 		// Do Something.
 	    }
 
 	    return null; // error
         }
 
 	/**
 	 * Retrieves the tag parameters from the Request object.
 	 * 
 	 * @param request
 	 *                 The Request object to parse.
 	 * @return
 	 *                 The tags as an ordered string.
 	 */
 	private String getTagParameters(HttpServletRequest request) {
 		// Get the tags from the query parameter.
 		String unorderedTags = request.getQueryString();
 		
 		String orderedTags = null;
 		// Order the tags if there are any.
 		if (unorderedTags != null) {
 			orderedTags = TagUtils.reorder(unorderedTags);
 		}
 		return orderedTags;
 	}
 
 	public PropertyService getPropertyService() {
 		return propertyService;
 	}
 
 	public void setPropertyService(PropertyService propertyService) {
 		this.propertyService = propertyService;
 	}
 }
