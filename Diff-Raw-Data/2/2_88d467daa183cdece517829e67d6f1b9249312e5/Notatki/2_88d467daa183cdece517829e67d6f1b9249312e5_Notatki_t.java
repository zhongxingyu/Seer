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
 @Table(name = "notatki")
 public class Notatki implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3948589674643072587L;
 
 	// ****************************************************************************************
 	// ***************************************Attributes***************************************
 	// ****************************************************************************************
 	@Id
 	@Column(name = "idnotatki")
 	@GeneratedValue
 	private Integer idNotatki;
 
 	@ManyToOne(fetch = FetchType.EAGER)
 	@JoinColumn(name = "id_grupy_projektowej", nullable = false)
 	private GrupyProjektowe idGrupyProjektowej;
 
 	@Column(name = "tresc")
 	private String tresc;
 
 	@Column(name = "plik")
 	private Integer plik;
 
	@Column(name = "data_dodatania", columnDefinition = "DATETIME")
 	private Date dataDodania;
 
 	@Column(name = "data_modyfikacji", columnDefinition = "DATETIME")
 	private Date dataModyfikacji;
 
 	@ManyToOne(fetch = FetchType.EAGER)
 	@JoinColumn(name = "id_prowadzacego", nullable = false)
 	private Prowadzacy idProwadzacego;
 
 	@ManyToOne(fetch = FetchType.EAGER)
 	@JoinColumn(name = "id_studenta", nullable = false)
 	private Studenci idStudenta;
 
 	// *************************************************************************************************
 	// ***************************************Getters and
 	// Setters***************************************
 	// *************************************************************************************************
 	public Integer getIdNotatki() {
 		return idNotatki;
 	}
 
 	public void setIdNotatki(Integer idNotatki) {
 		this.idNotatki = idNotatki;
 	}
 
 	public GrupyProjektowe getIdGrupyProjektowej() {
 		return idGrupyProjektowej;
 	}
 
 	public void setIdGrupyProjektowej(GrupyProjektowe idGrupyProjektowej) {
 		this.idGrupyProjektowej = idGrupyProjektowej;
 	}
 
 	public String getTresc() {
 		return tresc;
 	}
 
 	public void setTresc(String tresc) {
 		this.tresc = tresc;
 	}
 
 	public Integer getPlik() {
 		return plik;
 	}
 
 	public void setPlik(Integer plik) {
 		this.plik = plik;
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
 
 	public Prowadzacy getIdProwadzacego() {
 		return idProwadzacego;
 	}
 
 	public void setIdProwadzacego(Prowadzacy idProwadzacego) {
 		this.idProwadzacego = idProwadzacego;
 	}
 
 	public Studenci getIdStudenta() {
 		return idStudenta;
 	}
 
 	public void setIdStudenta(Studenci idStudenta) {
 		this.idStudenta = idStudenta;
 	}
 
 }
