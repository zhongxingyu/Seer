 package br.com.findplaces.ejb.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 
 import org.apache.log4j.Logger;
 
 import br.com.findplaces.ejb.PlaceConfigurations;
 import br.com.findplaces.jpa.dao.interfaces.CityDAO;
 import br.com.findplaces.jpa.dao.interfaces.FacilitiesDAO;
 import br.com.findplaces.jpa.dao.interfaces.NeighborhoodDAO;
 import br.com.findplaces.jpa.dao.interfaces.PlaceDAO;
 import br.com.findplaces.jpa.dao.interfaces.RegionDAO;
 import br.com.findplaces.jpa.dao.interfaces.StreetDAO;
 import br.com.findplaces.jpa.dao.spatial.interfaces.PlaceSpatialDAO;
 import br.com.findplaces.jpa.entity.Coment;
 import br.com.findplaces.jpa.entity.Place;
 import br.com.findplaces.jpa.entity.SellType;
 import br.com.findplaces.jpa.entity.geographic.City;
 import br.com.findplaces.jpa.entity.geographic.Neighborhood;
 import br.com.findplaces.jpa.entity.geographic.Region;
 import br.com.findplaces.jpa.entity.geographic.Street;
 import br.com.findplaces.jpa.entity.spatial.PlaceSpatial;
 import br.com.findplaces.jpa.exception.DAOException;
 import br.com.findplaces.model.geographic.to.CityTO;
 import br.com.findplaces.model.geographic.to.NeighborhoodTO;
 import br.com.findplaces.model.geographic.to.StreetTO;
 import br.com.findplaces.model.spatial.to.PlaceSpatialTO;
 import br.com.findplaces.model.to.ComentTO;
 import br.com.findplaces.model.to.FilterSearchRequest;
 import br.com.findplaces.model.to.PlaceTO;
 import br.com.findplaces.util.ConverterTO;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.Point;
 
 @Stateless(name = "PlaceConfigurationsEJB", mappedName = "PlaceConfigurationsImpl")
 public class PlaceConfigurationsImpl implements PlaceConfigurations {
 
 	private static final Logger logger = Logger
 			.getLogger(PlaceConfigurationsImpl.class);
 
 	private static final long serialVersionUID = 1L;
 
 	public static final int SRID = 4326;
 
 	@EJB
 	private CityDAO cityDAO;
 
 	@EJB
 	private FacilitiesDAO facilitiesDAO;
 
 	@EJB
 	private NeighborhoodDAO neighDAO;
 
 	@EJB
 	private PlaceDAO placeDAO;
 
 	@EJB
 	private PlaceSpatialDAO spatialDAO;
 
 	@EJB
 	private StreetDAO streetDAO;
 
 	@EJB
 	private RegionDAO regionDAO;
 
 	public PlaceTO createPlace(PlaceTO place) { // FIXME Should not convert and
 												// create in the same method!
 		try {
 
 			Double lat = place.getLat();
 			Double log = place.getLng();
 			GeometryFactory geoFactory = new GeometryFactory();
 			Coordinate coord = new Coordinate();
 			coord.x = lat;
 			coord.y = log;
 			Point point = geoFactory.createPoint(coord);
 			point.setSRID(SRID);
 
 			place.setSpatialTO(new PlaceSpatialTO());
//			place.getSpatialTO().setGeom(point);
 			place.getSpatialTO().setLat(lat);
 			place.getSpatialTO().setLon(log);
 
 			// String alias = place.getCity().getRegion().getAlias();
 			// Region region = regionDAO.findByAlias(alias);
 
 			// City city = cityDAO.findByName(place.getCity().getName());
 			// if (city == null) {
 			// place.getCity().setRegion(ConverterTO.converter(region));
 			// Long cityID =
 			// cityDAO.create(ConverterTO.converter(place.getCity()));
 			// city = cityDAO.findById(cityID);
 			// }
 			//
 			// Neighborhood neigh =
 			// neighDAO.findByName(place.getNeighborhood().getName());
 			// if(neigh == null){
 			// place.getNeighborhood().setCity(ConverterTO.converter(city));
 			// Long neighID =
 			// neighDAO.create(ConverterTO.converter(place.getNeighborhood()));
 			// neigh = neighDAO.findById(neighID);
 			// }
 			//
 			// Street street =
 			// streetDAO.findByName(place.getStreet().getStreetName());
 			// if(street == null){
 			// place.getStreet().setHood(ConverterTO.converter(neigh));
 			// Long streetID =
 			// streetDAO.create(ConverterTO.converter(place.getStreet()));
 			// street = streetDAO.findById(streetID);
 			// }
 			//
 			place.setCity(null);
 			place.setNeighborhood(null);
 			place.setStreet(null);
 
 			ArrayList<SellType> sellTypes = new ArrayList<SellType>();
 			if (place.getSellType() != null) {
 				for (Long sellType : place.getSellType()) {
 					SellType type = new SellType();
 					type.setId(sellType);
 
 					sellTypes.add(type);
 				}
 			}
 //			Long idFacilities = null;
 //			if (place.getFacilities() != null) {
 //				idFacilities = facilitiesDAO.create(ConverterTO
 //						.converter(place.getFacilities()));
 //
 //			}
 //			
 //			if(idFacilities != null){
 //				place.getFacilities().setId(idFacilities);
 //			}
 			Long id = placeDAO.create(ConverterTO.converter(place));
 			// PlaceSpatialTO spatialTO = place.getSpatialTO();
 			// spatialTO.setPlace(findPlaceById(id));
 			// Long fid = spatialDAO.create(ConverterTO.converter(spatialTO));
 
 			return this.findPlaceById(id);
 		} catch (Exception e) {
 
 		}
 		return place;
 	}
 
 	@Override
 	public List<PlaceTO> findPlaceByLatLogDistance(Double lat, Double log,
 			Double distance) {
 
 		List<PlaceSpatial> placesf = spatialDAO.findPlaceByLatLogDistance(lat, log,
 				distance);
 
 		if (placesf == null) {
 			logger.info("Não foi encontrado o lugar pela latitude e longitude ");
 			return null;
 		}
 
 		List<PlaceTO> placesTO = new ArrayList<PlaceTO>();
 		for (PlaceSpatial spatial : placesf) {
 			Place place = placeDAO.findBySpatial(spatial);
 			
 			PlaceTO to = ConverterTO.converter(place);
 			placesTO.add(to);
 		}
 
 		return placesTO;
 	}
 
 	@Override
 	public PlaceTO findPlaceById(Long id) {
 
 		try {
 			Place place = placeDAO.findById(id);
 
 			if (place == null) {
 				logger.info("Não foi encontrado o usuário " + id.toString());
 			}
 
 			return ConverterTO.converter(place);
 
 		} catch (DAOException e) {
 			// logger.error(e);
 		}
 		return null;
 	}
 
 	@Override
 	public CityTO findCityByName(String name) {
 
 		City city = cityDAO.findByName(name);
 
 		if (city == null) {
 			logger.info("Não foi encontrado o usuário ");
 		}
 
 		return ConverterTO.converter(city);
 	}
 
 	@Override
 	public StreetTO findStreetByName(String name) {
 		Street street = streetDAO.findByName(name);
 
 		if (street == null) {
 			// logger.info("Não foi encontrado o usuário "+ id.toString());
 		}
 
 		return ConverterTO.converter(street);
 	}
 
 	@Override
 	public NeighborhoodTO findNeighborhoodByName(String name) {
 		Neighborhood neigh = neighDAO.findByName(name);
 
 		if (neigh == null) {
 			logger.info("Não foi encontrado o usuário ");
 		}
 
 		return ConverterTO.converter(neigh);
 	}
 
 	/**
 	 * This method is very slow, but when it comes so slow that we can`t use any
 	 * more we will have to change to MapReduce design pattern.
 	 */
 	@Override
 	public List<PlaceTO> findByFilter(FilterSearchRequest filter) {
 
 		List<Place> allPlaces = placeDAO.findAll();
 
 		List<PlaceTO> placesMatchedWithUser = new ArrayList<PlaceTO>();
 		// FIXME OMG, how many ifs inside ifs are there? D:
 		for (Place place : allPlaces) {
 			Double points = 0d;
 			Integer numberOfFilters = 0;
 			if (filter.getSellType() != null) {
 				numberOfFilters++;
 				if (place.getSellType().indexOf(filter.getSellType()) != -1) {
 					points += filter.getWeightSellType();
 				}
 			}
 			if (filter.getPlaceType() != null) {
 				numberOfFilters++;
 				if (filter.getPlaceType().equals(place.getType().getId())) {
 					points += filter.getWeightPlaceType();
 				}
 			}
 
 			if (filter.getPriceMax() != null && filter.getPriceMin() != null) {
 				numberOfFilters++;
 				if (filter.getPriceMax() >= place.getPrice()
 						&& filter.getPriceMin() <= place.getPrice()) {
 					points += filter.getWeightPrice();
 				}
 			}
 
 			// if(filter.getNeibohoord()!=null){
 			// numberOfFilters++;
 			// if(filter.getNeibohoord().equals(place.getNeighborhood().getHoodName())){
 			// points+=filter.getWeightNeiborhood();
 			// }
 			// }
 
 			if (filter.getDistance() != null) {
 				numberOfFilters++;
 				PlaceSpatial spatial = place.getSpatial();
 				Geometry geom = spatial.getGeom();
 
 				Coordinate coord = new Coordinate();
 				GeometryFactory geoFactory = new GeometryFactory();
 				coord.x = filter.getLat();
 				coord.y = filter.getLog();
 				// Point = Geometry (extends)
 				Point point = geoFactory.createPoint(coord);
 
 				Float distance = distFrom(coord.x, coord.y,
 						geom.getCoordinate().x, geom.getCoordinate().y);
 				if (filter.getDistance() <= distance) {
 					points += filter.getWeightDistance();
 					// How we get the distance from here? :(
 				}
 			}
 
 			if (filter.getBedroom() != null) {
 				numberOfFilters++;
 				if (filter.getBedroom().equals(place.getBedroom())) {
 					points += filter.getWeightBedroom();
 				}
 			}
 
 			if (filter.getGarage() != null) {
 				numberOfFilters++;
 				if (filter.getGarage().equals(place.getGarage())) {
 					points += filter.getWeightGarage();
 				}
 			}
 
 			if ((points / numberOfFilters) > 0.3) {
 				placesMatchedWithUser.add(ConverterTO.converter(place));
 			}
 		}
 
 		return placesMatchedWithUser;
 	}
 
 	public float distFrom(double x, double y, double x2, double y2) {
 		double earthRadius = 3958.75;
 		double dLat = Math.toRadians(x2 - x);
 		double dLng = Math.toRadians(y2 - y);
 		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
 				+ Math.cos(Math.toRadians(x)) * Math.cos(Math.toRadians(x2))
 				* Math.sin(dLng / 2) * Math.sin(dLng / 2);
 		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
 		double dist = earthRadius * c;
 
 		int meterConversion = 1609;
 
 		return new Float(dist * meterConversion).floatValue();
 	}
 
 	@Override
 	public PlaceTO coment(ComentTO coment) {
 		Coment newComent = ConverterTO.converter(coment);
 		return ConverterTO.converter(placeDAO.save(newComent));
 	}
 
 	@Override
 	public ComentTO findComentByID(Long id) {
 		return ConverterTO.converter(placeDAO.findComentById(id));
 	}
 
 	/**
 	 * WTF, why dont you put this on a junit test?
 	 * 
 	 * FOR TEST USE GOOGLE API MAPS RIGHT BUTTON WHAT HAS THERE CATCH COORDINATE
 	 * AND TEST
 	 * 
 	 * 
 	 * 
 	 * 
 	 * public static void main(String[] args) {
 	 * 
 	 * GeometryFactory geoFactory = new GeometryFactory(); //
 	 * -22.909508,-47.057293 Coordinate coord = new Coordinate(); coord.x =
 	 * -22.909508; coord.y = -47.057293; Point point =
 	 * geoFactory.createPoint(coord);
 	 * 
 	 * // -22.912132,-47.055136 Coordinate coord2 = new Coordinate(); coord2.x =
 	 * -22.910793; coord2.y = -47.056638; Point point2 =
 	 * geoFactory.createPoint(coord);
 	 * 
 	 * DistanceOp distance = new DistanceOp(point, point2);
 	 * 
 	 * System.out.println(distFrom(coord.x, coord.y, coord2.x, coord2.y));
 	 * System.out.println(distance.closestLocations()); }
 	 * 
 	 * public static float distFrom(double x, double y, double x2, double y2) {
 	 * double earthRadius = 3958.75; double dLat = Math.toRadians(x2 - x);
 	 * double dLng = Math.toRadians(y2 - y); double a = Math.sin(dLat / 2) *
 	 * Math.sin(dLat / 2) + Math.cos(Math.toRadians(x))
 	 * Math.cos(Math.toRadians(x2)) * Math.sin(dLng / 2) Math.sin(dLng / 2);
 	 * double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)); double dist =
 	 * earthRadius * c;
 	 * 
 	 * int meterConversion = 1609;
 	 * 
 	 * return new Float(dist * meterConversion).floatValue(); }
 	 * 
 	 * 
 	 * 
 	 * 
 	 * 
 	 * 
 	 * 
 	 * 
 	 * 
 	 * 
 	 * 
 	 * 
 	 * 
 	 * 
 	 * 
 	 */
 
 }
