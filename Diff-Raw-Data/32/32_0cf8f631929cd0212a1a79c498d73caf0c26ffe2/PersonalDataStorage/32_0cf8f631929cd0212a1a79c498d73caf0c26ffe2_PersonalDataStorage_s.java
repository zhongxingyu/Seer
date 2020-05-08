 package com.sk.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Stores personal data by site name. Used for json encoding <br/>
  * <br/>
  * <code>
  * Gson gson = new Gson();<br/>
  * String json = gson.toJson(PersonalData);
  * </code>
  * 
  * @author Strikeskids
  * 
  */
 public class PersonalDataStorage extends HashMap<String, Set<PersonalData>> {
 
 	private static final long serialVersionUID = 9177444068545121072L;
 
 	/**
 	 * Adds the {@link PersonalData} to this {@link PersonalDataStorage}
 	 * 
 	 * @param input
 	 *            The {@link PersonalData} to add
 	 */
 	public void add(PersonalData... input) {
 		main: for (PersonalData data : input) {
			String site = data.getWebsiteId();
			if (containsKey(site)) {
				get(site).add(data);
			} else {
				for (String attr : data.keySet()) {
					if (!attr.contains("name")) {
 						Set<PersonalData> toAdd = new LinkedHashSet<>();
 						toAdd.add(data);
 						put(site, toAdd);
						continue main;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Adds the contents of the {@link PersonalDataStorage} to this
 	 * 
 	 * @param pds
 	 *            The {@link PersonalDataStorage} to add
 	 */
 	public void addStorage(PersonalDataStorage pds) {
 		for (Set<PersonalData> lds : pds.values()) {
 			for (PersonalData d : lds) {
 				add(d);
 			}
 		}
 	}
 
 	/**
 	 * Converts this {@link PersonalDataStorage} to an array
 	 * 
 	 * @return An array of {@link PersonalData}
 	 */
 	public PersonalData[] toArray() {
 		List<PersonalData> ret = new ArrayList<PersonalData>();
 		for (Set<PersonalData> cur : values())
 			ret.addAll(cur);
 		return ret.toArray(new PersonalData[ret.size()]);
 	}
 
 	/**
 	 * Gets the number of elements in this {@link PersonalDataStorage}
 	 * 
 	 * @return the number of elements
 	 */
 	@Override
 	public int size() {
 		int ret = 0;
 		for (Collection<?> coll : values())
 			ret += coll.size();
 		return ret;
 	}
 
 }
