 package de.shop.artikelverwaltung.domain;
 
 import java.io.Serializable;
 import java.net.URI;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 public class Artikel implements Serializable {
 
 	private static final long serialVersionUID = 5787206691085007571L;
 	private Long id;
 	private String bezeichnung;
 	private Double preis;
 	private Kategorie kategorie;
 	
 	@JsonIgnore
 	private URI artikelUri;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getBezeichnung() {
 		return bezeichnung;
 	}
 
 	public void setBezeichnung(String bezeichnung) {
 		this.bezeichnung = bezeichnung;
 	}
 
 	public Double getPreis() {
 		return preis;
 	}
 
 	public void setPreis(Double preis) {
 		this.preis = preis;
 	}
 	
 	public Kategorie getKategorie() {
 		return kategorie;
 	}
 
 	public void setKategorie(Kategorie kategorie) {
 		this.kategorie = kategorie;
 	}
 
 	public URI getArtikelUri() {
 		return artikelUri;
 	}
 	
 	public void setArtikelUri(URI artikelUri) {
 		this.artikelUri = artikelUri;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((bezeichnung == null) ? 0 : bezeichnung.hashCode());
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result
 				+ ((kategorie == null) ? 0 : kategorie.hashCode());
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
		final Artikel other = (Artikel) obj;
 		if (bezeichnung == null) {
 			if (other.bezeichnung != null)
 				return false;
		} 
		else if (!bezeichnung.equals(other.bezeichnung))
 			return false;
 		if (id == null) {
 			if (other.id != null)
 				return false;
		}
		else if (!id.equals(other.id))
 			return false;
 		if (kategorie == null) {
 			if (other.kategorie != null)
 				return false;
		} 
		else if (!kategorie.equals(other.kategorie))
 			return false;
 		if (preis == null) {
 			if (other.preis != null)
 				return false;
		} 
		else if (!preis.equals(other.preis))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Artikel [id=" + id + ", bezeichnung=" + bezeichnung
 				+ ", preis=" + preis + ", kategorie=" + kategorie + "]";
 	}
 
 	
 }
