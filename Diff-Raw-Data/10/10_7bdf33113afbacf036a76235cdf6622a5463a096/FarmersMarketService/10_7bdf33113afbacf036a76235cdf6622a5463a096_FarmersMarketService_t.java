 package org.ph0.civichack;
 
 import java.net.URLEncoder;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import org.apache.commons.lang3.StringUtils;
 import org.ph0.civichack.usda.FarmersMarket;
 import org.ph0.civichack.usda.MarketDetails;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 @Path("/market")
 public class FarmersMarketService {
 	private static final Logger log = LoggerFactory.getLogger(FarmersMarketService.class);
 	private Client client;
 	private ObjectMapper mapper;
 	private GeoService geo;
 	
 	private LoadingCache<String, MarketDetails> marketDetailsById;
 
 	public FarmersMarketService() {
 		ClientConfig cfg = new DefaultClientConfig();
 		// cfg.getClasses().add(JacksonJsonProvider.class);
 		client = Client.create(cfg);
 		mapper = new ObjectMapper();
 		mapper.registerModule(new JaxbAnnotationModule());
 		geo = new GeoService();
 		
 		CacheLoader<String, MarketDetails> loader = new CacheLoader<String, MarketDetails>() {
 			@Override
 			public MarketDetails load(String id) throws Exception {
 				log.debug("Loading details for market with id {}",id);
 				WebResource webResource = client
 						   .resource("http://search.ams.usda.gov/FarmersMarkets/v1/data.svc/mktDetail?id=" + URLEncoder.encode(id));
 			    ClientResponse response = webResource.get(ClientResponse.class);
 				String body = response.getEntity(String.class);
 				Map<String, MarketDetails> map = mapper.readValue(body,
 						new TypeReference<Map<String, MarketDetails>>() {});
 				MarketDetails details = map.get("marketdetails");
 				if (!StringUtils.isEmpty(details.getGoogleLink())) {
 					details.setLocation(geo.fromGoogleLink(details.getGoogleLink()));
 				}
 				else if (!StringUtils.isEmpty(details.getAddress())) {
 					details.setLocation(geo.fromAddress(details.getAddress()));
 				}
 				
 				return details;
 			}
 		};
 		
 		marketDetailsById = CacheBuilder.newBuilder()
 			       .maximumSize(10000)
 			       .expireAfterWrite(10, TimeUnit.MINUTES)
 			       .build(loader);
 	}
 	
 	@GET
 	@Path("/geo")
     @Produces("application/json")
 	public List<FarmersMarket> findMarketsByGeoCoordinate(@QueryParam("lat") String lat, @QueryParam("lon") String lon) throws Exception {
 		GeoCoordinate coord = new GeoCoordinate();
 		coord.setLat(lat);
 		coord.setLon(lon);
		
		return findMarketsByGeoCoordinate(coord).subList(0, 5);
 	}
 	
 	public List<FarmersMarket> findMarketsByGeoCoordinate(GeoCoordinate coord) throws Exception {
 
 		String url = "http://search.ams.usda.gov/FarmersMarkets/v1/data.svc/locSearch?lat="
 				+ URLEncoder.encode(coord.getLat()) + "&lng=" + URLEncoder.encode(coord.getLon());
 		return retrieveMarketsFromUrl(url);
 	}
 
 	@GET
 	@Path("/zip")
     @Produces("application/json")
 	public List<FarmersMarket> findMarketsByZip(@QueryParam("zip") String zip) throws Exception {
 		String url = "http://search.ams.usda.gov/FarmersMarkets/v1/data.svc/zipSearch?zip="
 				+ zip;
 		
 		return retrieveMarketsFromUrl(url);
 	}
 	
 	public List<FarmersMarket> retrieveMarketsFromUrl(String url) throws Exception {
 
 		WebResource webResource = client
 				.resource(url);
 
 		ClientResponse response = webResource.accept("application/json").get(
 				ClientResponse.class);
 
 		if (response.getStatus() != 200) {
 			throw new RuntimeException("Failed : HTTP error code : "
 					+ response.getStatus());
 		}
 
 		String body = response.getEntity(String.class);
 
 		Map<String, List<FarmersMarket>> map = mapper.readValue(body,
 				new TypeReference<Map<String, List<FarmersMarket>>>() {});
 		List<FarmersMarket> markets = map.get("results");
 		int i=0;
 		for (FarmersMarket market : markets) {
 			i++;
 			String name = market.getMarketName();
 			if (!StringUtils.isEmpty(name)) {
 				Matcher m = Pattern.compile("^([0-9](\\.[0-9]+)?)\\s+(.*)$").matcher(name);
 				if (m.matches()) {
 					String distance = m.group(1);
 					String mname = m.group(3);
 					log.info("dist: {}; name: {}", distance, mname);
 					market.setMarketName(mname);
 				}
 			}
 			fillMarketDetails(market);
 //			log.info("Market {}: {}", i, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(market));
 		}
 		
 		return markets;
 		
 	}
 
 	public FarmersMarket fillMarketDetails(FarmersMarket market) throws Exception {
 		market.setMarketDetails(this.marketDetailsById.get(market.getId()));
 		return market;
 	}
 	
 }
