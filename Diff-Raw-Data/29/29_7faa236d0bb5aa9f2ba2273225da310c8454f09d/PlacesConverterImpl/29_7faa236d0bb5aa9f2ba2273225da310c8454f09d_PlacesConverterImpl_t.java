 package br.com.maplink.webservices.places.client.converter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import br.com.maplink.webservices.places.client.entity.Place;
 import br.com.maplink.webservices.places.client.entity.PlacesResult;
 import br.com.maplink.webservices.places.client.resource.AtomLink;
 import br.com.maplink.webservices.places.client.resource.Places;
 
 public class PlacesConverterImpl implements PlacesConverter {
 
 	@Override
 	public PlacesResult toEntity(Places resource) {
 		PlacesResult entity = new PlacesResult();
 		
 		entity.setPlaces(toEntity(resource.getPlaces()));
 		entity.setNextPagePath(extractAtomLinkPath(resource.getLinks(), "next"));
 		entity.setPreviousPagePath(extractAtomLinkPath(resource.getLinks(), "previous"));
 		entity.setTotalFound(resource.getTotalFound());
 		
 		return entity;
 	}
 	
 	private String extractAtomLinkPath(List<AtomLink> links, String rel) {
 		for(AtomLink link : links) {
			if(link.getRel().equalsIgnoreCase(rel)) {
				return link.getHref();
 			}
 		}
 		
		return "";
 	}
 
 	private List<Place> toEntity(List<br.com.maplink.webservices.places.client.resource.Place> resources) {
 		List<Place> places = new ArrayList<Place>();
 		
 		for (br.com.maplink.webservices.places.client.resource.Place resource : resources) {
 			Place entity = new Place();
 			entity.setId(resource.getId());
 			entity.setAddress(resource.getAddress());
 			entity.setCategory(resource.getCategory());
 			entity.setCity(resource.getCity());
 			entity.setCountry(resource.getCountry());
 			entity.setDescription(resource.getDescription());
 			entity.setDistance(resource.getDistance());
 			entity.setDistrict(resource.getDistrict());
 			entity.setLatitude(resource.getLatitude());
 			entity.setLongitude(resource.getLongitude());
 			entity.setName(resource.getName());
 			entity.setPrimaryPhone(resource.getPrimaryPhone());
 			entity.setSecondaryPhone(resource.getSecondaryPhone());
 			entity.setState(resource.getState());
 			entity.setSubCategory(resource.getSubCategory());
 			entity.setZipCode(resource.getZipCode());
 			
 			places.add(entity);
 		}
 		
 		return places;
 	}
 }
