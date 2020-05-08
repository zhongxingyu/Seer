 package com.pmf.entities;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.*;
 
 import com.pmf.util.EjbConstants;
 
 @Entity
 @Table(name = EjbConstants.TABLE_PAIS)
 @NamedQueries( {
 		@NamedQuery(name = "Pais.findAll", query = "SELECT d FROM Pais d where d.status=1"),
 		@NamedQuery(name = "Pais.findByCode", query = "SELECT d FROM Pais d where d.paisid=:paisid and d.status=1")})
 public class Country implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	public Country() {
 		setCities(new ArrayList<City>());
 	}
 
 	@Id
 	@Column(name = "id")
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private int id;
 	@Column(name = "name")
 	private String name;
 	@Column(name = "status")
 	private int status;
	@OneToMany(mappedBy="country")
 	private List<City> cities;
 	
 	
 	public int getStatus() {
 		return status;
 	}
 
 	public void setStatus(int status) {
 		this.status = status;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public List<City> getCities() {
 		return cities;
 	}
 
 	public void setCities(List<City> cities) {
 		this.cities = cities;
 	}
 
 }
