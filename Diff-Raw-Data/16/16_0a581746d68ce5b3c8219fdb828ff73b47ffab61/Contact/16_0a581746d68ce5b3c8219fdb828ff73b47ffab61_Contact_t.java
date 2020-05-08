 package ch.ffhs.esa.lifeguard.domain;
 
 import ch.ffhs.esa.lifeguard.persistence.Persistable;
 
 /**
  * Represents a single contact
  * 
  * @author Juerg Gutknecht <juerg.gutknecht@students.ffhs.ch>
  *
  */
 public class Contact
 	implements ContactInterface {
 
 	/*//////////////////////////////////////////////////////////////////////////
 	 * PROPERTIES
 	 */
 	
 	private static final long serialVersionUID = 6571898847981097051L;
 
 	private long id = 0;
 	
 	private String name = "";
 	
 	private String phone = "";
 	
 	private int position = 0;
 	
 	
 	/*//////////////////////////////////////////////////////////////////////////
 	 * PUBLIC INTERFACE
 	 */
 	
 	@Override
 	public String toString() {
 		return this.getName();
 	}
 	
 	/* (non-Javadoc)
 	 * @see ch.ffhs.esa.lifeguard.persistence.Persistable#getId()
 	 */
 	@Override
 	public long getId() {
 		return this.id;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.ffhs.esa.lifeguard.persistence.Persistable#setId(int)
 	 */
 	@Override
 	public Persistable setId(long id) {
 		this.id = id;
 		return this;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.ffhs.esa.lifeguard.domain.ContactInterface#getName()
 	 */
 	@Override
 	public String getName() {
 		return this.name;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.ffhs.esa.lifeguard.domain.ContactInterface#setName(java.lang.String)
 	 */
 	@Override
 	public ContactInterface setName(String name) {
		name = name.trim();
		if (name.length() < 1) {
			return this;
		}
 		this.name = name;
 		return this;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.ffhs.esa.lifeguard.domain.ContactInterface#getPhone()
 	 */
 	@Override
 	public String getPhone() {
 		return this.phone;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.ffhs.esa.lifeguard.domain.ContactInterface#setPhone(java.lang.String)
 	 */
 	@Override
 	public ContactInterface setPhone(String phone) {
		phone = phone.trim();
		if (phone.length() < 3) {
			return this;
		}
 		this.phone = phone;
 		return this;
 	}
 
 	@Override
 	public int getPosition() {
 		return this.position;
 	}
 	
 	public ContactInterface setPosition(int position) {
 		this.position = (position > 0) ? position : 0;
 		return this;
 	}
 }
