 /**
  * 
  */
 package de.webshop.artikelverwaltung.domain;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.Date;
 
 import javax.validation.constraints.DecimalMin;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * @author Stephan-PC
  * 
  */
 @XmlRootElement
 public class Artikelpreishistorie implements Serializable {
 
 	private static final long serialVersionUID = -2471255330875233292L;
 
 	// Eigenschaften
 	@NotNull(message = "{artikelverwaltung.artikelpreishistorie.id.notNull}")
	@Min(value = 1, message = "{artikelverwaltung.artikelpreishistorie.id.min}")
 	private Long id;
 	@NotNull(message = "{artikelverwaltung.artikelpreishistorie.artikelID.notNul}")
	@Min(value = 1, message = "{artikelverwaltung.artikelpreishistorie.artikelID.min}")
 	private Long artikelID;
 	@NotNull(message = "{artikelverwaltung.artikelpreishistorie.gueltigVon.notNull}")
 	private Date gueltigVon;
 	@NotNull(message = "{artikelverwaltung.artikelpreishistorie.preis.notNull}")
	@DecimalMin(value = "0.0", message = "{artikelverwaltung.artikelpreishistorie.preis.decimalMin}")
 	private BigDecimal preis;
 
 	// Konstruktor
 	public Artikelpreishistorie(Long artikelid, Date gueltigVon,
 			BigDecimal preis) {
 		super();
 		this.artikelID = artikelid;
 		this.gueltigVon = gueltigVon;
 		this.preis = preis;
 	}
 
 	// get/set-Methoden
 	public Long getID() {
 		return id;
 	}
 
 	public void setID(Long id) {
 		this.id = id;
 	}
 
 	public Long getArtikelid() {
 		return artikelID;
 	}
 
 	public void setArtikelid(Long artikelid) {
 		this.artikelID = artikelid;
 	}
 
 	public Date getGueltigVon() {
 		return gueltigVon;
 	}
 
 	public void setGueltigVon(Date gueltigVon) {
 		this.gueltigVon = gueltigVon;
 	}
 
 	public BigDecimal getPreis() {
 		return preis;
 	}
 
 	public void setPreis(BigDecimal preis) {
 		this.preis = preis;
 	}
 
 	// BasisMethoden
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (int) (artikelID ^ (artikelID >>> 32));
 		result = prime * result
 				+ ((gueltigVon == null) ? 0 : gueltigVon.hashCode());
 		result = prime * result + (int) (id ^ (id >>> 32));
 		result = prime * result + ((preis == null) ? 0 : preis.hashCode());
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
 		Artikelpreishistorie other = (Artikelpreishistorie) obj;
 		if (artikelID != other.artikelID)
 			return false;
 		if (gueltigVon == null) {
 			if (other.gueltigVon != null)
 				return false;
 		} else if (!gueltigVon.equals(other.gueltigVon))
 			return false;
 		if (id != other.id)
 			return false;
 		if (preis == null) {
 			if (other.preis != null)
 				return false;
 		} else if (!preis.equals(other.preis))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Artikelpreishistorie [id=" + id + ", artikelid=" + artikelID
 				+ ", gueltigVon=" + gueltigVon + ", preis=" + preis + "]";
 	}
 }
