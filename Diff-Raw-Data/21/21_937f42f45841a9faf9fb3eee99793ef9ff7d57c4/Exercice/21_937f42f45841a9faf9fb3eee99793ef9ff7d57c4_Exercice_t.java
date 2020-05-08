 /**
  * 
  */
 package be.lreenaers.lafay.beans;
 
 import com.google.code.morphia.annotations.Entity;
 
 /**
  * @author media
  * 
  */
 @Entity
 public class Exercice extends Bean {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private String nom;
 	private String description;
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Exercice other = (Exercice) obj;
 		if (description == null) {
 			if (other.description != null)
 				return false;
 		} else if (!description.equals(other.description))
 			return false;
 		if (nom == null) {
 			if (other.nom != null)
 				return false;
 		} else if (!nom.equals(other.nom))
 			return false;
 		return true;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public String getNom() {
		System.out.println("nom: "+this.nom);
 		return nom;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result
 				+ ((description == null) ? 0 : description.hashCode());
 		result = prime * result + ((nom == null) ? 0 : nom.hashCode());
 		return result;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public void setNom(String nom) {
 		this.nom = nom;
 	}
 }
