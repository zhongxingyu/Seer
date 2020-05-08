 package cz.cvut.felk.via.gae.web;
 
 import java.net.URI;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 @Controller
 public class DemoController {
 
 	private Map<URI, PostalAddress> cache;
 	
 	@PostConstruct
 	public void initalize() {
 		cache = new HashMap<URI, PostalAddress>();
 		// ...
 	}
 	
 	@RequestMapping(value="/address/", method=RequestMethod.GET)
 	@ResponseBody
 	@SuppressWarnings("unchecked")
 	public URI[] list() {	
 		return cache.isEmpty() ? new URI[]{} : cache.keySet().toArray(new URI[cache.keySet().size()]);
 	}	
 
 	@RequestMapping(value="/address/{addressId}", method=RequestMethod.GET)
 	@ResponseBody
 	public PostalAddress read(@PathVariable String addressId) throws ResourceNotFoundException {
 		
 		final URI id = URI.create("/address/" + addressId);
 		if (!cache.containsKey(id)) {
 			throw new ResourceNotFoundException(id); 
 		}
 		
 		return (PostalAddress) cache.get(id);
 	}
 
 	@RequestMapping(value="/address/", method=RequestMethod.POST)
 	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
 	public PostalAddress create(@RequestBody @Valid PostalAddress address, HttpServletResponse response) {
 		
 		final URI id = URI.create("/address/" + (new Date()).getTime());	// use better id generator, this is just demo ....
 		
 		response.setHeader("Location", id.toString());		// tell client where the new resource resides
 		
 		cache.put(id, address);
 		
 		return address;
 	}
 	
 	
 	@RequestMapping(value="/address/{addressId}", method=RequestMethod.PUT)
 	@ResponseBody
 	public PostalAddress update(@RequestBody @Valid PostalAddress address, @PathVariable String addressId) {
 		cache.put(URI.create("/address/" + addressId), address);
 		return address;
 	}
 	
 	@RequestMapping(value="/address/{addressId}", method=RequestMethod.DELETE)
 	@ResponseStatus(HttpStatus.NO_CONTENT)
 	public void delete(@PathVariable String addressId) {
 		final URI key = URI.create("/address/" + addressId);
 		
 		if (cache.containsKey(key)) {
 			cache.remove(key);
 		}
 	}
 	
 	@ExceptionHandler(ResourceNotFoundException.class)
 	@ResponseStatus(HttpStatus.NOT_FOUND)
 	public void handleNotFoundException(ResourceNotFoundException ex, HttpServletResponse response) {
 		//...
 	}
 }
