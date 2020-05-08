 package cz.cvut.fel.jee.labEshop.model;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
import javax.persistence.Lob;
 import javax.persistence.Table;
 
import org.hibernate.validator.constraints.NotBlank;
 import org.jboss.solder.core.Veto;
 
 /**
  * Brand represents manufacturer of product. Can be used to specify workflow
 * during complain processes or contain additional data about brands in
 * {@linkplain #comment} field.
  * 
  * <p>
  * Example: Sony, Cisco, LG, ...
  * 
  * @author Kamil Prochazka (<a href="mailto:prochka6@fel.cvut.cz">prochka6</a>)
  */
 @Veto
 @Entity
 @Table(name = "brand")
 public class Brand extends BaseEntity {
 
 	private static final long serialVersionUID = 1L;
 
	@NotBlank
 	@Column(unique = true)
 	private String name;
 
	@Lob
	private String comment;

 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
	/**
	 * Contains Brand contract, description or any other relevant informations.
	 * This field is optional.
	 * 
	 * @return the brand notes and other information.
	 */
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Brand other = (Brand) obj;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
		return name;
 	}
 
 }
