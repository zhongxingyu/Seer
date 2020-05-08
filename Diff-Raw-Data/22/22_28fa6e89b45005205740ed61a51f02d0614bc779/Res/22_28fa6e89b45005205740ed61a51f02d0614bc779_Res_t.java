 package ch.hsr.bieridee.config;
 
 import ch.hsr.bieridee.domain.Beer;
 import ch.hsr.bieridee.domain.Beertype;
 import ch.hsr.bieridee.domain.Tag;
 import ch.hsr.bieridee.domain.User;
 import ch.hsr.bieridee.domain.interfaces.IDomain;
 
 /**
  * Resources.
  * 
  * 
  */
 public final class Res {
 
 	private Res() {
 		// do not instanciate.
 	}
 
 	public static final String API_URL = "http://localhost:8080";
 
 	// beer
 	public static final String BEER_COLLECTION = "/beers";
 	public static final String BEER_REQ_ATTR = "beer-id";
 	public static final String BEER_DOCUMENT = "/beers/{" + BEER_REQ_ATTR + "}";
 
 	// beertype
 	public static final String BEERTYPE_COLLECTION = "/beertypes";
 	public static final String BEERTYPE_REQ_ATTR = "beertype-id";
 	public static final String BEERTYPE_DOCUMENT = "/beertypes/{" + BEERTYPE_REQ_ATTR + "}";
 
 	// tag
 	public static final String TAG_COLLECTION = "/tags";
 	public static final String TAG_REQ_ATTR = "tag-name";
 	public static final String TAG_DOCUMENT = "/tags/{" + TAG_REQ_ATTR + "}";
 
 	// user
 	public static final String USER_COLLECTION = "/users";
 	public static final String USER_REQ_ATTR = "user-name";
 	public static final String USER_DOCUMENT = "/users/{" + USER_REQ_ATTR + "}";
 
 	/**
 	 * Returns the resource URI of the given domain object.
 	 * 
 	 * @param domainObject
 	 *            Domain object
 	 * @return The resource URI
 	 */
 	public static String getResourceUri(IDomain domainObject) {
 		final Class<? extends IDomain> c = domainObject.getClass();
 		String uri = null;
		final String pattern = "\\{.*\\}";
		
 		if (c == Beer.class) {
 			final long id = ((Beer) domainObject).getId();
			uri = API_URL + BEER_DOCUMENT.replaceAll(pattern, Long.toString(id));
 		}
 		if (c == Beertype.class) {
 			final long id = ((Beertype) domainObject).getId();
			uri = API_URL + BEERTYPE_DOCUMENT.replaceAll(pattern, Long.toString(id));
 		}
 		if (c == Tag.class) {
 			final String name = ((Tag) domainObject).getName();
			uri = API_URL + TAG_DOCUMENT.replaceAll(pattern, name);
 		}
 		if (c == User.class) {
 			final String name = ((User) domainObject).getUsername();
			uri = Res.API_URL + USER_DOCUMENT.replaceAll(pattern, name);
 		}
 
 		return uri;
 	}
 
 }
