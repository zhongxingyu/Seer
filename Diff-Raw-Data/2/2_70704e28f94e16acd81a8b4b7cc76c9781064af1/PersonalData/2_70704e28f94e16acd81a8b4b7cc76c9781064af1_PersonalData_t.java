 package com.sk.util;
 
 import java.net.URL;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import com.google.common.base.Optional;
 
 /**
  * This contains all the data about an individual garnered from a specific website <br />
  * Keys used
  * <table>
  * <tr>
  * <th>Key</th>
  * <th>Purpose</th>
  * </tr>
  * <tr>
  * <td>name</td>
  * <td>Individual's name</td>
  * </tr>
  * <tr>
  * <td>first-name</td>
  * <td>Individual's first name (or given name)</td>
  * </tr>
  * <tr>
  * <td>last-name</td>
  * <td>Individual's last name (or family name)</td>
  * </tr>
  * <tr>
  * <td>industry</td>
  * <td>The industry where the individual works</td>
  * </tr>
  * <tr>
 * <td>profilePictureUrl</td>
  * <td>A {@link URL} to the individual's profile picture</td>
  * </tr>
  * <tr>
  * <td>education</td>
  * <td>A summary of the individual's education</td>
  * </tr>
  * <tr>
  * <td>job-title</td>
  * <td>The individual's job title</td>
  * </tr>
  * <tr>
  * <td>location</td>
  * <td>The individual's location</td>
  * </tr>
  * <tr>
  * <td>phone</td>
  * <td>Individual's phone</td>
  * </tr>
  * <tr>
  * <td>address</td>
  * <td>Individual's home address. Similar to location</td>
  * </tr>
  * <tr>
  * <td>age</td>
  * <td>individual's age</td>
  * </tr>
  * <tr>
  * <td>home-page</td>
  * <td>Individual's home page</td>
  * </tr>
  * <tr>
  * <td>house-price</td>
  * <td>Estimate house price</td>
  * </tr>
  * <tr>
  * <td>num-bedrooms</td>
  * <td></td>
  * </tr>
  * <tr>
  * <td>num-bathrooms</td>
  * <td></td>
  * </tr>
  * <tr>
  * <td>property-type</td>
  * <td></td>
  * </tr>
  * <tr>
  * <td>house-size</td>
  * <td></td>
  * </tr>
  * <tr>
  * <td>lot-size</td>
  * <td></td>
  * </tr>
  * <tr>
  * <td>year-built</td>
  * <td></td>
  * </tr>
  * <tr>
  * <td></td>
  * <td></td>
  * </tr>
  * </table>
  * 
  * @author Strikeskids
  * 
  */
 public class PersonalData extends LinkedHashMap<String, String> {
 
 	private final String siteId;
 
 	public PersonalData(String source) {
 		this.siteId = source;
 	}
 
 	public PersonalData(String source, int initialCapacity) {
 		super(initialCapacity);
 		this.siteId = source;
 	}
 
 	public PersonalData(String source, Map<? extends String, ? extends String> m) {
 		super(m);
 		this.siteId = source;
 	}
 
 	public PersonalData(String source, int initialCapacity, float loadFactor) {
 		super(initialCapacity, loadFactor);
 		this.siteId = source;
 	}
 
 	public Optional<String> get(String attribute) {
 		if (containsKey(attribute))
 			return Optional.of(super.get(attribute));
 		else
 			return Optional.absent();
 	}
 
 	public String[] getAllValues(String attribute) {
 		if (containsKey(attribute)) {
 			return super.get(attribute).split("[|]");
 		} else {
 			return new String[0];
 		}
 	}
 
 	/**
 	 * Get the website id of the source of the {@link PersonalData}
 	 * 
 	 * @return The website id
 	 */
 	public String getWebsiteId() {
 		return siteId;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder ret = new StringBuilder();
 		ret.append("PersonalData (");
 		ret.append(siteId);
 		ret.append(", ");
 		ret.append(super.toString());
 		ret.append(")");
 		return ret.toString();
 	}
 
 	private static final long serialVersionUID = 4259139787451939836L;
 }
