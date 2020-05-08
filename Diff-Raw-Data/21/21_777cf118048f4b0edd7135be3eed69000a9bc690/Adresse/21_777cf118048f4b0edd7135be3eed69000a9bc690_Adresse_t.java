 package de.webshop.kundenverwaltung.domain;
 
 import java.io.Serializable;
 
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XmlRootElement
 public class Adresse implements Serializable {
 
 	private static final long serialVersionUID = -6129881639235747224L;
 	// Eigenschaften
	@Min(value = 1, message = "{kundenverwaltung.adresse.id.min}")
 	@NotNull(message = "{kundenverwaltung.adresse.id.notNull }")
 	private Long id;
 	@NotNull(message = "{kundenverwaltung.adresse.strasse.notNull}")
 	private String strasse;
 	@Size(max = 4, message = "{kundenverwaltung.adresse.hausnummer.size}")
 	@NotNull(message = "{kundenverwaltung.adresse.hausnummer.notNull}")
 	private String hausnummer;
 	@Pattern(regexp = "\\d{4,5}", message = "{kundenverwaltung.adresse.plz.pattern}")
 	@NotNull(message = "{kundenverwaltung.adresse.plz.notNull }")
 	private String plz;
 	@NotNull(message = "{kundenverwaltung.adresse.stadt.notNull}")
 	private String stadt;
 	private Land land;
 
 	// Lnderkrzel nach ISO 3166
 	public enum Land {
 		AT, CH, DE
 	}
 
 	// get/set-Methoden
 	public Long getID() {
 		return id;
 	}
 
 	public void setID(Long id) {
 		this.id = id;
 	}
 
 	public String getStrasse() {
 		return strasse;
 	}
 
 	public void setStrasse(String strasse) {
 		this.strasse = strasse;
 	}
 
 	public String getHausnummer() {
 		return hausnummer;
 	}
 
 	public void setHausnummer(String hausnummer) {
 		this.hausnummer = hausnummer;
 	}
 
 	public String getPlz() {
 		return plz;
 	}
 
 	public void setPlz(String plz) {
 		this.plz = plz;
 	}
 
 	public String getStadt() {
 		return stadt;
 	}
 
 	public void setStadt(String stadt) {
 		this.stadt = stadt;
 	}
 
 	public Land getLand() {
 		return land;
 	}
 
 	public void setLand(Land land) {
 		this.land = land;
 	}
 
 	// Basismethoden
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((hausnummer == null) ? 0 : hausnummer.hashCode());
 		result = prime * result + (int) (id ^ (id >>> 32));
 		result = prime * result + ((land == null) ? 0 : land.hashCode());
 		result = prime * result + ((plz == null) ? 0 : plz.hashCode());
 		result = prime * result + ((stadt == null) ? 0 : stadt.hashCode());
 		result = prime * result + ((strasse == null) ? 0 : strasse.hashCode());
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
 		Adresse other = (Adresse) obj;
 		if (hausnummer == null) {
 			if (other.hausnummer != null)
 				return false;
 		} else if (!hausnummer.equals(other.hausnummer))
 			return false;
 		if (id != other.id)
 			return false;
 		if (land != other.land)
 			return false;
 		if (plz == null) {
 			if (other.plz != null)
 				return false;
 		} else if (!plz.equals(other.plz))
 			return false;
 		if (stadt == null) {
 			if (other.stadt != null)
 				return false;
 		} else if (!stadt.equals(other.stadt))
 			return false;
 		if (strasse == null) {
 			if (other.strasse != null)
 				return false;
 		} else if (!strasse.equals(other.strasse))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Adresse [id=" + id + ", strasse=" + strasse + ", hausnummer="
 				+ hausnummer + ", plz=" + plz + ", stadt=" + stadt + ", land="
 				+ land + "]";
 	}
 }
