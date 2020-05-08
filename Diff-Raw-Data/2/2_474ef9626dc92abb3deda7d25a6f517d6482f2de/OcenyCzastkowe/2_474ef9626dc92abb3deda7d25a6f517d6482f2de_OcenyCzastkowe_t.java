 package com.project.data;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "oceny_czastkowe")
 public class OcenyCzastkowe implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8429902717274323822L;
 
 	// ****************************************************************************************
 	// ***************************************Attributes***************************************
 	// ****************************************************************************************
 	@Id
 	@Column(name = "idoceny_czastkowe")
 	@GeneratedValue
 	private Integer idOcenyCzastkowe;
 
 	@Column(name = "ocena")
 	private String ocena;
 
 	@Column(name = "data_dodania", columnDefinition = "DATETIME")
 	private Date dataDodania;
 
 	@Column(name = "data_modyfikacji", columnDefinition = "DATETIME")
 	private Date dataModyfikacji;
 
 	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idstudenta", nullable = false)
 	private Studenci idStudenta;
 
 	@ManyToOne(fetch = FetchType.EAGER)
 	@JoinColumn(name = "idspotkania", nullable = false)
 	private Spotkania idSpotkania;
 
 	// *************************************************************************************************
 	// ***************************************Getters and
 	// Setters***************************************
 	// *************************************************************************************************
 
 	public Integer getIdOcenyCzastkowe() {
 		return idOcenyCzastkowe;
 	}
 
 	public void setIdOcenyCzastkowe(Integer idOcenyCzastkowe) {
 		this.idOcenyCzastkowe = idOcenyCzastkowe;
 	}
 
 	public String getOcena() {
 		return ocena;
 	}
 
 	public void setOcena(String ocena) {
 		this.ocena = ocena;
 	}
 
 	public Date getDataDodania() {
 		return dataDodania;
 	}
 
 	public void setDataDodania(Date dataDodania) {
 		this.dataDodania = dataDodania;
 	}
 
 	public Date getDataModyfikacji() {
 		return dataModyfikacji;
 	}
 
 	public void setDataModyfikacji(Date dataModyfikacji) {
 		this.dataModyfikacji = dataModyfikacji;
 	}
 
 	public Studenci getIdStudenta() {
 		return idStudenta;
 	}
 
 	public void setIdStudenta(Studenci idStudenta) {
 		this.idStudenta = idStudenta;
 	}
 
 	public Spotkania getIdSpotkania() {
 		return idSpotkania;
 	}
 
 	public void setIdSpotkania(Spotkania idSpotkania) {
 		this.idSpotkania = idSpotkania;
 	}
 
 }
