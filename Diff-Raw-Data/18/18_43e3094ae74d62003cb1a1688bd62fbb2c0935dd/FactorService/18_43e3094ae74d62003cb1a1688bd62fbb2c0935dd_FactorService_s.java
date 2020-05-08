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
