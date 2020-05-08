 package com.twistlet.falcon.model.service;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.support.DataAccessUtils;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.twistlet.falcon.model.entity.FalconLocation;
 import com.twistlet.falcon.model.entity.FalconUser;
 import com.twistlet.falcon.model.repository.FalconLocationRepository;
 
 @Service
 public class LocationServiceImpl implements LocationService {
 
 	private final FalconLocationRepository falconLocationRepository;
 	
 	@Autowired
 	public LocationServiceImpl(FalconLocationRepository falconLocationRepository) {
 		this.falconLocationRepository = falconLocationRepository;
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public List<FalconLocation> listAdminLocations(FalconUser admin) {
 		List<FalconLocation> locations = falconLocationRepository.findByFalconUser(admin);
 		for(FalconLocation location : locations){
 			location.setFalconAppointments(null);
 			location.setFalconUser(null);
 		}
 		return locations;
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void saveLocation(FalconLocation falcLocation) {
 		falconLocationRepository.save(falcLocation);		
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public Set<FalconLocation> listAvailableLocations(FalconUser admin, Date start, Date end) {
 		List<FalconLocation> locations = falconLocationRepository.findByFalconUser(admin);
 		Set<FalconLocation> occupiedLocations = falconLocationRepository.findLocationDateRange(admin, start, end);
 		Set<FalconLocation> availableLocations = new HashSet<>();
 		for(FalconLocation location : locations){
 			boolean found = false;
 			for(FalconLocation occupiedLocation : occupiedLocations){
 				if(location.getId().equals(occupiedLocation.getId())){
 					found = true;
 					break;
 				}
 			}
 			if(!found){
				if(location.getValid()){
 					availableLocations.add(location);
 				}
 			}
 		}
 		return availableLocations;
 	}
 
 	@Override
 	@Transactional
 	public List<FalconLocation> listAdminLocationLike(FalconLocation location) {
 		List<FalconLocation> locations = falconLocationRepository.findByFalconUserLike(location);
 		return locations;
 	}
 
 	@Override
 	public void deleteLocation(FalconLocation falconLocation) {
 		List<FalconLocation> locations = falconLocationRepository.findByFalconUserLike(falconLocation);
 		DataAccessUtils.singleResult(locations);
 		
 	}
 
 }
