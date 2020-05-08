 package com.cellcity.citiguide.util;
 
 public class Constants {
 	
 	public static final int SPLASH_WAIT_MILLISEC = 2000; //  sec
 	public static final String DEFAUL_SHARE_DATA = "DEFAUL_SHARE_DATA";
 	public static final String SHARE_ID = "ID";
 	//Debug
 	public static final String KEY_MAP = "0o_1qZVZbZZh-5KqNtCjZdddrt5sJKMQZqMxZYQ";
 	//Release
 	//public static final String KEY_MAP = "0o_1qZVZbZZiKr2Z1YiMtpheACJKvtndHOFP70A";
 
 	// groovy type
 
 	public static final int TYPE_GOURMET = 1;
 	public static final int TYPE_PROMOTION = 101;
 	public static final int TYPE_SHOPPING = 2;
 	public static final int TYPE_BARS = 3;
 	public static final int TYPE_BANK = 6;
 	public static final int TYPE_MOVIE = 8;
 	public static final int TYPE_HOTEL = 13;	
 	public static final int TYPE_FLIGHT = 103;
 	public static final int TYPE_WEATHER = 102;
 	public static final int TYPE_NONE = -1;
 	public static final int TYPE_AR = 7;
 	
 	public static final double SINGAPORE_LATITUDE = 1.3667;
 	public static final double SINGAPORE_LONGITUDE = 103.8;
 	
 	public static final double BANGKOK_LAT = 13.758605718612671; 
 	public static final double BANGKOK_LONG = 100.56467413902283;
 	
 	// URL
 	public static final String URL_WEATHER_TODAY = "http://www.dc2go.net/api/weather/getTodayWeather.php?a=groovyNokia&cy=Singapore&cn=Singapore&l=en";
 	public static final String URL_WEATHER_FORECAST = "http://www.dc2go.net/api/weather/get7dayWeather.php?a=groovyNokia&cy=Singapore&cn=Singapore&l=en";
 	public static final String URL_CATEGORY = "http://www.dc2go.net/api/groovymap/getCategories.php?cn=1&ct=1&ca=";
 	public static final String URL_FLIGHT = "http://www.dc2go.net/api/flight/getFlightStatus.php?l=en";
 	public static final String URL_MERCHANT_LIST = "http://www.dc2go.net/api/groovymap/search.php?cn=1&ct=1&ca=";
 	public static final String URL_MOVIE = "http://www.dc2go.net/api/go/getMovies.php?city=Singapore";
 	public static final String URL_SHOPPING1 = "http://www.dc2go.net/api/go/getList.php?result=20&city=Singapore&state=&country=Singapore";//start=0&category=";
 	public static final int ITEMS_PER_PAGE = 10;
 	public static final String SPACE_HORIZONTAL = "   ";
 	
 	public static final String URL_DINING = "http://www.citiworldprivileges.com/mobile/?country=Singapore&city=Singapore&offer_type=Dining&sort_by=distance&sort_order=desc&num_offers=10&current_lat=";
 	//public static final String URL_DINING = "http://www.citiworldprivileges.com/mobile/?country=Singapore&offer_type=Dining";
 	public static final String URL_SHOPPING = "http://www.citiworldprivileges.com/mobile/?country=Singapore&city=Singapore&offer_type=Shopping&sort_by=distance&sort_order=desc&num_offers=10&current_lat=";
 	//public static final String URL_SHOPPING = "http://www.citiworldprivileges.com/mobile/?country=Singapore&offer_type=Shopping";
 	public static final String URL_SEARCH = "http://www.citiworldprivileges.com/mobile/?country=Singapore&city=Singapore&keywords=";
 	public static final String URL_IMAGE = "http://www.citiworldprivileges.com/images/banners/";
 	public static final String URL_HOTELS = "http://www.citiworldprivileges.com/mobile/?country=Singapore&offer_type=Dining&keywords=hotel&sort_by=distance&sort_order=asc&current_lat=";
 	//public static final String URL_HOTELS = "http://www.citiworldprivileges.com/mobile/?country=Singapore&offer_type=Dining&keywords=hotel";
 	public static final String URL_BANK = "http://www.citiworldprivileges.com/mobile/?country=Singapore&offer_type=Shopping&keywords=bank&sort_by=distance&sort_order=asc&current_lat=";
 	//public static final String URL_BANK = "http://www.citiworldprivileges.com/mobile/?country=Singapore&offer_type=Shopping&keywords=bank";
 	public static final String URL_BAR = "http://www.citiworldprivileges.com/mobile/?country=Singapore&city=Singapore&offer_type=Dining&keywords=bar&sort_by=distance&sort_order=desc&num_offers=10&current_lat=";
 	//public static final String URL_BAR = "http://www.citiworldprivileges.com/mobile/?country=Singapore&offer_type=Dining&keywords=bar";
 	
 	public static final String URL_BANK_HOME = "https://mobile.citibank.com.sg/";
 	public static final String URL_BANK_PROMO = "http://www.citibank.com.sg/mbol/promo/index.htm";
 	public static final String URL_BANK_GOURMET = "http://www.citibank.com.sg/mbol/gp/index.htm";
 	public static final String RESTAURANT_CUISINE_TYPES = "http://singtel.dc2go.net/singtel/get_cuisine.php?a=b&bank=Citibank";
 	public static final String RESTAURANT_CUISINE_LISTING = "http://singtel.dc2go.net/singtel/get_restaurant_by_cuisine_type.php?resultsPerPage=10&bank=Citibank&cuisineTypeID=";
 	public static final String RESTAURANT_DETAIL = "http://singtel.dc2go.net/singtel/get_detail.php?id=";
 	public static final String RESTAURANT_LOCATION_PAGE="http://singtel.dc2go.net/singtel/get_restaurant_by_location.php?resultsPerPage=20&pageNum=1&bank=Citibank&latitude=";
	public static final String RESTAURANT_LOCATION_AR="http://singtel.dc2go.net/singtel/get_restaurant_by_location.php?resultsPerPage=15&pageNum=1&bank=Citibank&latitude=";
 	public static final String RESTAURANT_SEARCH = "http://singtel.dc2go.net/singtel/search.php?bank=Citibank&resultsPerPage=10&keyword=";
 	
 	public static final String URL_ARSEARCH = "http://www.citiworldprivileges.com/mobile/?country=Singapore&city=Singapore&sort_by=distance&sort_order=desc&num_offers=10&current_lat=";
 }
