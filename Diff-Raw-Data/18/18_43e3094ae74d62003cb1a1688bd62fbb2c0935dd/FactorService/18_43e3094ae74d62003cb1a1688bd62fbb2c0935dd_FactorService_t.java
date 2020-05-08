 package edu.neumont.java3.factor.service;
 
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 
 import org.springframework.stereotype.Service;
 
 @Service
 @Path("/factor")
 public class FactorService {
 
 	@GET
 	public List<Long> factor(long number){
 		
 		return null;
 	}
 }


/*
 * interceptor/filter
 * request handlers (cxf)
 * http filters (j2ee)
 * 
 * webinf filter
 * request response and chain
 * chain.dochain or something like that
 * 
 * 
 * response www-authenticate:basic
 * 
 *  client
 *  jax-rs makes it easy on server side
 *  
 */
