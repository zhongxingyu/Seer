 package edu.neumont.java3.factor.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.springframework.stereotype.Service;
 
 @Service
 @Path("/factor")
 public class FactorService {
 
 	@GET
 	@Produces({"application/xml", "application/JSON"})
 	public Container factor(@QueryParam("number") long number) {
		return primeFactors(number);
 	}
 
 	public static Container primeFactors(long number) {
 		long n = number;
 		Container returnObject = new Container();
 		ArrayList<Long> factors = new ArrayList<Long>();
 		for (long i = 2; i <= n / i; i++) {
 			while (n % i == 0) {
 				factors.add(i);
 				n /= i;
				if (factors.size() == 2) {
 					returnObject.yourlist = factors;
 					return returnObject;
 				}
 			}
 		}
 		if (n > 1) {
 			factors.add(n);
 		}
 		returnObject.yourlist = factors;
 		return returnObject;
 	}
 
 }
 
 /*
  * interceptor/filter request handlers (cxf) http filters (j2ee)
  * 
  * WHITELIST
  * 
  * webinf filter request response and chain chain.dochain or something like that
  * 
  * 
  * response www-authenticate:basic
  * 
  * client jax-rs makes it easy on server side
  */
