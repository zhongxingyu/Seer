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
 @Table(name = "obecnosc")
 public class Obecnosc implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3238099816394367608L;
 
 	// ****************************************************************************************
 	// ***************************************Attributes***************************************
 	// ****************************************************************************************
 	@Id
 	@Column(name = "idobecnosc")
 	@GeneratedValue
 	private Integer idObecnosc;
 
 	@Column(name = "stan")
 	private boolean stan;
 
	@Column(name = "nazwa")
	private String nazwa;

 	@Column(name = "data_wprowadzenia", columnDefinition = "DATETIME")
 	private Date dataWprowadzenia;
 
 	@Column(name = "data_modyfikacji", columnDefinition = "DATETIME")
 	private Date dataModyfikacji;
 
 	@ManyToOne(fetch = FetchType.EAGER)
 	@JoinColumn(name = "id_studenta", nullable = false)
 	private Studenci idStudenta;
 
 	@ManyToOne(fetch = FetchType.EAGER)
 	@JoinColumn(name = "is_spotkania", nullable = false)
 	private Spotkania isSpotkania;
 
 	// *************************************************************************************************
 	// ***************************************Getters and
 	// Setters***************************************
 	// *************************************************************************************************
 
 	public Integer getIdObecnosc() {
 		return idObecnosc;
 	}
 
 	public void setIdObecnosc(Integer idObecnosc) {
 		this.idObecnosc = idObecnosc;
 	}
 
 	public boolean isStan() {
 		return stan;
 	}
 
 	public void setStan(boolean stan) {
 		this.stan = stan;
 	}
 
	public String getNazwa() {
		return nazwa;
	}

	public void setNazwa(String nazwa) {
		this.nazwa = nazwa;
	}

 	public Date getDataWprowadzenia() {
 		return dataWprowadzenia;
 	}
 
 	public void setDataWprowadzenia(Date dataWprowadzenia) {
 		this.dataWprowadzenia = dataWprowadzenia;
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
 
 	public Spotkania getIsSpotkania() {
 		return isSpotkania;
 	}
 
 	public void setIsSpotkania(Spotkania isSpotkania) {
 		this.isSpotkania = isSpotkania;
 	}
 
 }
