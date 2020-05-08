 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2011 2012  Moritz Bürger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi.communication.mack;
 
 /**
  * This class represents a xmpp identifier that is used by the
  * {@link MACKNetworkHandler}. Instances of this class represent an entry in the
  * MACK database table {@code mate_userdevices}
  * 
  * @author Tobias Mende
  * 
  */
 public class XmppIdentifier {
 	/** the {@code username} */
 	private String componentOwner;
 	/** the {@code channel} */
 	private String componentType;
 	/** the {@code jid} */
 	private String id;
 
 	/**
 	 * Constructor for a new xmpp identifier.
 	 * 
 	 * @param compOwner
 	 *            the user
 	 * @param compType
 	 *            the channel
 	 * @param jabberUser
 	 *            the jabber identifier
 	 */
 	public XmppIdentifier(String compOwner, String compType, String jabberUser) {
 		componentType = compType;
 		componentOwner = compOwner;
 		this.id = jabberUser;
 	}
 
 	/**
 	 * Just here to fit the java beans requirements. Should not be used in other
 	 * cases. Use {@link XmppIdentifier#XmppIdentifier(String, String, String)}
 	 * instead.
 	 */
 	public XmppIdentifier() {
 	}
 
 	/**
 	 * Getter for the username
 	 * 
 	 * @return the username
 	 */
 	public String getComponentOwner() {
 		return componentOwner;
 	}
 
 	/**
 	 * Getter for the channel
 	 * 
 	 * @return the channel
 	 */
 	public String getComponentType() {
 		return componentType;
 	}
 
 	/**
 	 * Getter for the jabber user name
 	 * 
 	 * @return the jabber user name (without server)
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
	 * Setter for the agent identifier of the {@link Agent} to which this identfier belongs.
 	 * @param componentOwner
 	 *            the componentOwner to set
 	 */
 	public void setComponentOwner(String componentOwner) {
 		this.componentOwner = componentOwner;
 	}
 
 	/**
 	 * Setter for the tye of the component.
 	 * @param componentType
 	 *            the componentType to set
 	 */
 	public void setComponentType(String componentType) {
 		this.componentType = componentType;
 	}
 
 	/**
 	 * Setter for the jabber identifier to set.
 	 * @param id
 	 *            the id to set
 	 */
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((componentOwner == null) ? 0 : componentOwner.hashCode());
 		result = prime * result
 				+ ((componentType == null) ? 0 : componentType.hashCode());
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		XmppIdentifier other = (XmppIdentifier) obj;
 		if (componentOwner == null) {
 			if (other.componentOwner != null)
 				return false;
 		} else if (!componentOwner.equals(other.componentOwner))
 			return false;
 		if (componentType == null) {
 			if (other.componentType != null)
 				return false;
 		} else if (!componentType.equals(other.componentType))
 			return false;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 }
