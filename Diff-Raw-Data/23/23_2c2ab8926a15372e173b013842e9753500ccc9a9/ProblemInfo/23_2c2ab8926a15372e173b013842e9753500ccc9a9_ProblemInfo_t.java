 /**
  * Copyright 2011 Paweł Doleciński.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package pl.dolecinski.supdicium.client.model.problem;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import name.pehl.piriti.json.client.JsonReader;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.view.client.ProvidesKey;
 
 /**
  * @author Paweł Doleciński
  * 
  */
 @SuppressWarnings("serial")
 public class ProblemInfo implements Serializable {
 
 	/**
 	 * The key provider that provides the unique ID of a contact.
 	 */
 	public static final ProvidesKey<ProblemInfo> KEY_PROVIDER = new ProvidesKey<ProblemInfo>() {
 		public Object getKey(ProblemInfo item) {
 			return item == null ? null : item.getId();
 		}
 	};
 
 	public interface ItemReader extends JsonReader<ProblemInfo> {
 	}
 
 	public static final ItemReader JSON = GWT.create(ItemReader.class);
 
 	private static int nextId = 0;
 
	private String[] experts;
	private String description;
	private String state;
	private Date createDate;
	private int id;

	protected ProblemInfo() {

	}
 
 	public ProblemInfo(int id, String[] experts, String description,
 			String state) {
 		this.experts = experts;
 		this.description = description;
 		this.state = state;
 		this.id = nextId++;
 		this.createDate = new Date();
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (o instanceof ProblemInfo) {
 			return id == ((ProblemInfo) o).id;
 		}
 		return false;
 	}
 
 	/**
 	 * @return the experts
 	 */
 	public String[] getExperts() {
 		return experts;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @return the createDate
 	 */
 	public Date getCreateDate() {
 		return createDate;
 	}
 
 	/**
 	 * @return the state
 	 */
 	public String getState() {
 		return state;
 	}
 
 	/**
 	 * @return the unique ID of the contact
 	 */
 	public int getId() {
 		return this.id;
 	}
 
 	@Override
 	public int hashCode() {
 		return id;
 	}
 
 	/**
 	 * RequestStatus meaning:
 	 * <ul>
 	 * <li>{@link RequestStatus#UNSENT} - at default when request entity is
 	 * created, visible only to its author, can be edited & deleted by end user
 	 * <li>{@link RequestStatus#UNREAD} - unread by administrator, should be
 	 * e.g. bolded in administators user interfase
 	 * <li>{@link RequestStatus#READ} - read by administrator, but decission
 	 * hasn't been maded
 	 * <li>{@link RequestStatus#ACCEPTED} - accepted by administrator, resource
 	 * created
 	 * <li>{@link RequestStatus#REJECTED} - rejected by administrator, resource
 	 * not created
 	 */
 	public enum RequestStatus {
 
 		/**
 		 * At default when request entity is created, visible only to its
 		 * author, can be edited & deleted by end user
 		 */
 		UNSENT,
 		/**
 		 * Posted by end user, byt unread by any administrator, should be e.g.
 		 * bolded in administators user interfase
 		 */
 		UNREAD,
 		/**
 		 * Read by administrator, but decission hasn't been maded yet
 		 */
 		READ,
 		/**
 		 * Accepted by administrator, resource created
 		 */
 		ACCEPTED,
 		/**
 		 * Rejected by administrator, resource not created
 		 */
 		REJECTED
 	}
 
 }
