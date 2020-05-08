 package com.winterwell.jgeoplanet;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * A collection of {@link Place} objects.
  * <p>
  * A {@link PlaceCollection} represents a <em>potential</em> collection of places
  * rather than the collection of places themselves.
  * </p>
  * <p>
  * Use {@link #get()} and friends to get hold of the actual Places involved.
  * </p>
  * <p>
  * PlaceCollection returns Places in long form by default. Use {@link #shortForm(boolean)}
  * to change this behaviour.
  * </p>
  * @author Joe Halliwell <joe@winterwell.com>
  */
 public class PlaceCollection extends GeoPlanetResource {
 	Place base;
 	/** Can be a test string to search for, or a relation e.g. children, belongtos **/
 	String query;
 	PlaceType[] types;
 	boolean useShortForm = false;
 	int total = -1;
 
 	PlaceCollection(GeoPlanet client, String query) {
 		super(client);
 		this.query = query;
 	}
 
 	PlaceCollection(Place place, String relation) {
 		super(place.getClient());
 		this.base = place;
 		this.query = relation;
 	}
 
 	PlaceCollection(PlaceCollection other) {
 		super(other.getClient());
 		this.base = other.base;
 		this.query = other.query;
 		this.useShortForm = other.useShortForm;
 		this.total = other.total;
 		this.types = other.types;
 	}
 
 	/**
 	 * Set the type filter.
 	 * <p>
	 * A call to type() i.e. with no arguments unsets the type filter.
	 * Example usage:
 	 * <pre>
 	 * Place earth = g.getPlace(1);
 	 * PlaceType country = g.getPlaceType("Country");
 	 * PlaceType town = g.getPlaceType("Town");
 	 * List<Place> countries = earth.getChildren().type(country, town).get();
 	 * </pre>
 	 * </p>
 	 * @param types... The place types to filter on. If empty, unset the type.
 	 * @return a version of this collection filtered by the specified type.
 	 * @throws GeoPlanetException
 	 */
 	public PlaceCollection type(PlaceType... types) throws GeoPlanetException {
 		assert types != null;
 		if (types.length > 7) {
 			throw new GeoPlanetException("Cannot specify more than 7 types");
 		}
 		if (types.length == 0) types = null;
 		PlaceCollection variant;
 		variant = new PlaceCollection(this);
 		variant.types = types;
 		variant.total = -1;
 		return variant;
 	}
 
 	/**
 	 * Convenience wrapper for {@link #type(PlaceType)}.
	 * <p>
	 * A call to typename() i.e. with no arguments resets the type filter.
	 * </p>
 	 * <p>
 	 * Valid place types names include "County", "Region", "Town" and "Ward"
 	 * </p>
	 * 
 	 * @param placeTypeNames the place type name or names. These must be valid.
 	 * @return a version of this collection filtered by the specified type.
 	 * @throws GeoPlanetException
 	 * @see #type(PlaceType)
 	 */
 	public PlaceCollection typename(String... placeTypeNames) throws GeoPlanetException {
 		assert placeTypeNames != null;
 		if (placeTypeNames.length > 7) {
 			throw new GeoPlanetException("Cannot specify more than 7 types");
 		}
 		PlaceType[] placeTypes = new PlaceType[placeTypeNames.length];
 		for (int i = 0; i < placeTypeNames.length; i++) {
 			placeTypes[i] = getClient().getPlaceType(placeTypeNames[i].trim());
 		}
 		return type(placeTypes);
 	}
 
 	/**
 	 * If this collection is filtered by a single place type, return that type
 	 * If it is unfiltered, return null.
 	 * If it is filtered on more than one place type throw an exception
 	 * @deprecated Use getTypes() instead
 	 * @return the place type filter
 	 * @throws GeoPlanetException if more than one place type has been specified
 	 */
 	@Deprecated
 	public PlaceType getType() throws GeoPlanetException {
 		if (this.types == null) return null;
 		if (this.types.length != 1) throw new GeoPlanetException("More than one type in filter");
 		return this.types[0];
 	}
 
 	/**
 	 * If this collection is filtered by place type, return the types
 	 * otherwise return null.
 	 * @return
 	 */
 	public PlaceType[] getTypes() {
 		return this.types;
 	}
 
 	/**
 	 * Return short form places from this query. The default is to use long form.
 	 * @see #isShortForm()
 	 * @param useShortForm
 	 */
 	public PlaceCollection shortForm(boolean useShortForm) {
 		if (useShortForm == this.useShortForm) return this;
 		PlaceCollection variant;
 		variant = new PlaceCollection(this);
 		variant.useShortForm = useShortForm;
 		return variant;
 	}
 	/**
 	 * @return true if this place collection will return short form Places; false otherwise
 	 */
 	public boolean isShortForm() {
 		return useShortForm;
 	}
 
 	private void appendTypes(StringBuilder sb) {
 		assert types != null;
 		assert types.length > 0;
 		sb.append(".type('");
 		sb.append(types[0].getName());
 		for (int i = 1; i < types.length; i++) {
 			sb.append(",");
 			sb.append(types[i].getName());
 		}
 		sb.append("')");
 	}
 
 	private void appendQuery(StringBuilder sb) {
 		assert query != null;
 		sb.append(".q(");
 		sb.append(query);
 		sb.append(")");
 	}
 
 	/**
 	 * Returns the total number of places in this collection if a get() has occurred, or -1
 	 * to indicate that no get has occurred.
 	 * @return the total number of place in this collection if known, or -1
 	 */
 	public int size() {
 		return total;
 	}
 
 	/**
 	 * Get a list of (some of) the places contained in this collection.
 	 * Requires network access. Returns an empty list if no results were found.
 	 * @param start The first result to get indexed from 0
 	 * @param count The maximum number of results to return. Zero (0) returns all results.
 	 * @throws GeoPlanetException for general errors
 	 */
 	public List<Place> get(int start, int count) throws GeoPlanetException {
 		if (start < 0) throw new IllegalArgumentException("start parameter must be >= 0");
 		if (count < 0) throw new IllegalArgumentException("count parameter must be >= 0");
 		assert count >=0 ;
 		StringBuilder uri = new StringBuilder();
 		if (base == null) {
 			uri = new StringBuilder("/places");
 			if (types != null && query != null) {
 				uri.append("$and(");
 				appendQuery(uri);
 				uri.append(",");
 				appendTypes(uri);
 				uri.append(")");
 			}
 			else if (query != null) {
 				appendQuery(uri);
 			}
 			else if (types != null) {
 				appendTypes(uri);
 			}
 		}
 		else {
 			uri = new StringBuilder("/place/");
 			uri.append(base.getWoeId());
 			uri.append("/");
 			uri.append(query); // A relation in this case...
 			if (types != null) {
 				appendTypes(uri);
 			}
 		}
 		// Matrix parameters
 		uri.append(";start="); uri.append(start);
 		uri.append(";count="); uri.append(count);
 		// Results
 		JSONObject tmp;
 		tmp = getClient().doGet(uri.toString(), useShortForm);
 		return processResults(tmp);
 	}
 
 	/**
 	 * Get all places in this collection.
 	 * Cosmetic method calling <code>get(0,0)</code>.
 	 * Requires network access.
 	 * @throws GeoPlanetException for general errors
 	 */
 	public List<Place> get() throws GeoPlanetException {
 		return get(0, 0);
 	}
 
 	/**
 	 * Get a specific place from this collection.
 	 * Cosmetic method calling <code>get(index, 1).get(0)</code>
 	 * Not usually used with index != 0. Requires network access.
 	 * @throws GeoPlanetException for general errors
 	 * @throws ArrayIndexOutOfBoundsException for invalid indices
 	 */
 	public Place get(int index) throws GeoPlanetException {
 		return get(index, 1).get(0);
 	}
 
 	private List<Place> processResults(JSONObject tmp) throws GeoPlanetException {
 		try {
 			tmp = tmp.getJSONObject("places");
 			total = tmp.getInt("total");
 			if (total == 0) return new ArrayList<Place>(0);
 
 			int start = tmp.getInt("start");
 			int count = tmp.getInt("count");
 			assert start >= 0;
 			assert (count >= 0);
 			assert (start + count) <= total;
 			List<Place> results = new ArrayList<Place>(count);
 			JSONArray array = tmp.getJSONArray("place");
 			for (int i = 0; i<count; i++) {
 				results.add(new Place(getClient(), array.getJSONObject(i)));
 			}
 			return results;
 		} catch (JSONException e) {
 			throw new GeoPlanetException(e);
 		}
 	}
 }
