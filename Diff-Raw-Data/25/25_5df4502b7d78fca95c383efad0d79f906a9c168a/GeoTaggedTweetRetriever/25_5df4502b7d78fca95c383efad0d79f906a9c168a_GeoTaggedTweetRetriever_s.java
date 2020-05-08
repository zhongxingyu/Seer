 package toctep.skynet.backend.bll;
 
 import twitter4j.FilterQuery;
 
 public class GeoTaggedTweetRetriever extends TweetRetriever {
 
 	private static final double[][] NETHERLANDS_COORDS = new double[][] { {3.39, 51.17}, {7.29, 53.51} };
 	//private final static double[][] GRONINGEN_PROVINCE_COORDS = { {6.19, 53.09}, {7.22, 53.51} };
 	//private final static double[][] GRONINGEN_CITY_COORDS = new double[][] { {6.45, 53.16}, {6.65, 53.26} };
 	//private final static double[][] GRONINGEN_ZERNIKE_COORDS = new double[][] { {6.52, 53.23}, {6.55, 53.25} };
 	
 	@Override
 	public void run() {
 		this.getTwitterStream().filter(new FilterQuery(0, null, null, NETHERLANDS_COORDS));
 	}
 
 }
