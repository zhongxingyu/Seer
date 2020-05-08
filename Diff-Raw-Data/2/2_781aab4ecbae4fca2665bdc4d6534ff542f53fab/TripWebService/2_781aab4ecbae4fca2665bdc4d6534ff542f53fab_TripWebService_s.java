 package com.techiekernel.easylocate.ws;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.techiekernel.easylocate.pojo.Trip;
 import com.techiekernel.easylocate.service.TripService;
 
 @Controller
@RequestMapping("/trip")
 public class TripWebService {
 
 	@Autowired
 	TripService tripService;
 
 	@RequestMapping(value = "/{tripId}", method = RequestMethod.GET, headers = "Accept=application/json", produces = { "application/json" })
 	@ResponseBody
 	public Trip getTrip(@PathVariable int tripId) {
 		return tripService.getTrip(tripId);
 	}
 
 	@RequestMapping(method = RequestMethod.GET, headers = "Accept=application/json", produces = { "application/json" })
 	@ResponseBody
 	public List<Trip> getTrips() {
 		return tripService.getTrips();
 	}
 
 	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json", produces = { "application/json" }, consumes = { "application/json" })
 	@ResponseBody
 	public boolean createTrip(@RequestBody Trip trip) {
 		return tripService.saveOrUpdateTrip(trip);
 	}
 
 	@RequestMapping(value = "/{tripId}", method = RequestMethod.PUT, headers = "Accept=application/json", produces = { "application/json" }, consumes = { "application/json" })
 	@ResponseBody
 	public boolean editFoobar(@RequestBody Trip trip, @PathVariable int tripId) {
 		if (trip.getTripId() != null)
 			return tripService.saveOrUpdateTrip(trip);
 		else
 			return false;
 	}
 
 	@RequestMapping(value = "/{tripId}", method = RequestMethod.DELETE, headers = "Accept=application/json", produces = { "application/json" })
 	@ResponseBody
 	public boolean deleteTrip(@PathVariable int tripId) {
 		return tripService.deleteTrip(tripId);
 	}
 
 }
