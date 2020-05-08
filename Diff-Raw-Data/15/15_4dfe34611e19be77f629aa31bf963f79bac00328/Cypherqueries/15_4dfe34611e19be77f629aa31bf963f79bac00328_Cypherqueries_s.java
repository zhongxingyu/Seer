 package ch.hsr.bieridee.utils;
 
 /**
  * Utility Class providing predefined Cypher Query Strings.
  * 
 * @author chrigi
  */
 public final class Cypherqueries {
 	private Cypherqueries() {
 
 	}
 
 	public static final String GET_ALL_BEERS = "START HOME_NODE = node(0) MATCH HOME_NODE-[:INDEX_BEER]-BEER_INDEX-[:INDEXES]->Beer RETURN Beer";
 	public static final String GET_ALL_USERS = "START HOME_NODE = node(0) MATCH HOME_NODE-[:INDEX_USER]-USER_INDEX-[:INDEXES]->User RETURN User";
 	public static final String GET_ALL_TAGS = "START HOME_NODE = node(0) MATCH HOME_NODE-[:INDEX_TAG]-TAG_INDEX-[:INDEXES]->Tag RETURN Tag";
 	public static final String GET_ALL_BEERTYPES = "START HOME_NODE = node(0) MATCH HOME_NODE-[:INDEX_BEERTYPE]-BEERTYPE_INDEX-[:INDEXES]->Beertype RETURN Beertype";
 	public static final String GET_ALL_USER_CONSUMPTIONS = "START HOME_NODE = node(0) MATCH HOME_NODE-[:INDEX_USER]-USER_INDEX-[:INDEXES]-User-[:HAS_CONSUMED]-Beer RETURN Beer";
 	public static final String GET_BEER_BY_NAME = "START HOME_NODE = node(0) MATCH HOME_NODE-[:INDEX_BEER]-BEER_INDEX-[:INDEXES]->Beer RETURN Beer WHERE Beer.name = \'$1\'";
 
 }
