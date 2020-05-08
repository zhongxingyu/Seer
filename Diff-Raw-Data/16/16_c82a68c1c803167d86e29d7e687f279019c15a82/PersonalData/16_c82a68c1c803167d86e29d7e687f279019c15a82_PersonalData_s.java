 package com.sk.util;
 
 import java.net.URL;
 import java.util.HashMap;
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
  * <td>profile-picture-url</td>
  * <td>A {@link URL} to the individual's profile picture</td>
  * </tr>
  * <tr>
  * <td>education</td>
  * <td>A summary of the individual's education</td>
  * </tr>
  * <tr>
  * <td>title</td>
  * <td>The individual's job title</td>
  * </tr>
  * <tr>
  * <td>location</td>
  * <td>The individual's location</td>
  * </tr>
  * </table>
  * 
  * @author Strikeskids
  * 
  */
 public class PersonalData extends HashMap<String, String> {
 
	private String webId;
 
 	public PersonalData(String source) {
 	}
 
 	public PersonalData(String source, int initialCapacity) {
 		super(initialCapacity);
 	}
 
 	public PersonalData(String source, Map<? extends String, ? extends String> m) {
 		super(m);
 	}
 
 	public PersonalData(String source, int initialCapacity, float loadFactor) {
 		super(initialCapacity, loadFactor);
 	}
 
 	public Optional<String> get(String attribute) {
 		if (containsKey(attribute))
 			return Optional.of(super.get(attribute));
 		else
 			return Optional.absent();
 	}
 
 	/**
 	 * Get the website id of the source of the {@link PersonalData}
 	 * 
 	 * @return The website id
 	 */
 	public String getWebsiteId() {
		return webId;
 	}
 
 	@Override
 	public String toString() {
		return "PersonalData (" + webId + ", " + super.toString() + ")";
 	}
 
 	private static final long serialVersionUID = 4259139787451939836L;
 }
