 /*
  *  Copyright 2012 Axel Winkler, Daniel Dunér
  * 
  *  This file is part of Daxplore Presenter.
  *
  *  Daxplore Presenter is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 2.1 of the License, or
  *  (at your option) any later version.
  *
  *  Daxplore Presenter is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with Daxplore Presenter.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.daxplore.presenter.server.storage;
 
 import java.util.List;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import org.daxplore.presenter.server.throwable.BadRequestException;
 import org.daxplore.presenter.shared.QueryDefinition;
 import org.daxplore.presenter.shared.SharedTools;
 
 /**
  * A representation of a StatDataItem and it's key that can be
  * persisted (stored) using a {@link PersistenceManager}.
  * 
  * <p>This acts like an item in a hash table (represented by the
  * {@linkplain PersistenceManager}), making it possible to fetch data items
  * with a specific key.</p>
  */
 @PersistenceCapable
 public class StatDataItemStore {
 	@PrimaryKey
 	private String key;
 	@Persistent
 	private List<String> jsonChunks;
 	@Persistent
 	private String prefix;
 
 	/**
 	 * Instantiate a new stat data item, which is a piece of anonymized
 	 * statistical data that can be presented to users.
 	 * 
 	 * <p>The key should be on the format "prefix#name". The prefix defines
 	 * which presenter the setting belongs to and the name is the name
 	 * of the data item.</p>
 	 * 
 	 * @param key
 	 *            a key on the format "prefix#name"
 	 * @param json
 	 *            the data item as json
 	 */
 	public StatDataItemStore(String key, String json) {
 		this.key = key;
 		jsonChunks = SharedTools.splitString(json, 500); //JDO has a String length limit of 500
 		this.prefix = key.substring(0, key.indexOf('#'));
 	}
 
 	/**
 	 * Get the key.
 	 * 
 	 * @return the key
 	 */
 	public String getKey() {
 		return key;
 	}
 
 	/**
 	 * Get the stat data item as a json string.
 	 * 
 	 * @return the data
 	 */
 	public String getJson() {
 		return SharedTools.join(jsonChunks, "");
 	}
 	
 	public static String getStats(PersistenceManager pm, String prefix, QueryDefinition queryDefinition) throws BadRequestException {
 		String questionID = queryDefinition.getQuestionID();
 		String perspectiveID = queryDefinition.getPerspectiveID();
		String key = String.format("%s#Q=%s&P=%s", prefix, questionID, perspectiveID);
 		try {
 			return pm.getObjectById(StatDataItemStore.class, key).getJson();
 		} catch (Exception e) {
 			throw new BadRequestException("Could not read data item '" + key + "'", e);
 		}
 	}
 }
