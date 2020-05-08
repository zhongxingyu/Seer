 package org.ph0.civichack;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.xml.bind.JAXB;
 
 import org.ph0.civichack.bikeshare.BikeshareStations;
 import org.ph0.civichack.bikeshare.BikeshareStations.Station;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
 import com.google.common.collect.Lists;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 @Path("/bike")
 public class BikeshareService {
 	private static final Logger log = LoggerFactory
 			.getLogger(BikeshareService.class);
 	private Client client;
 	private ObjectMapper mapper;
 	private GeoService geo;
 
 	private List<Station> stations;
 
 	public BikeshareService() {
 		ClientConfig cfg = new DefaultClientConfig();
 		// cfg.getClasses().add(JacksonJsonProvider.class);
 		client = Client.create(cfg);
 		mapper = new ObjectMapper();
 		mapper.registerModule(new JaxbAnnotationModule());
 		geo = new GeoService();
 		stations = JAXB.unmarshal(
 				BikeshareService.class.getResource("/bikeStations.xml"),
 				BikeshareStations.class).getStations();
 	}
 
 	@GET
 	@Path("geo")
 	@Produces("application/json")
 	public BikeshareStations findStationsByGeoCoordinate(
 			@QueryParam("lat") final Double lat,
 			@QueryParam("lon") final Double lon) throws Exception {
 		
 		List<Station> sorted = Lists.newArrayList();
 		sorted.addAll(stations);
 		Collections.sort(sorted, new StationComparator(lat,lon));
 		BikeshareStations ret = new BikeshareStations();
		ret.setStations(sorted);
 		return ret;
 	}
 
 	public static class StationComparator implements Comparator<Station> {
 		private double lat;
 		private double lon;
 
 		public StationComparator(double lat, double lon) {
 			this.lat = lat;
 			this.lon = lon;
 		}
 
 		@Override
 		public int compare(Station o1, Station o2) {
 			double o1la = Math.abs(o1.getLat() - lat);
 			double o1lo = Math.abs(o1.getLon() - lon);
 			double o2la = Math.abs(o2.getLat() - lat);
 			double o2lo = Math.abs(o2.getLon() - lon);
 
 			return Double.compare(o1la * o1la + o1lo * o1lo, o2la * o2la + o2lo
 					* o2lo);
 		}
 	}
 
 }
