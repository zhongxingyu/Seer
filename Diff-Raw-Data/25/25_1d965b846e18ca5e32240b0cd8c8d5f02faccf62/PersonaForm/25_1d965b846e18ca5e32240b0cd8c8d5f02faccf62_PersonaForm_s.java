 package com.country.form;
 
 
 public class PersonaForm extends Form {
 
 	private Integer id = 0;
 	private String nombre;
 	private String apellido;
 	private String nacimiento;
 	private String sexo;
 	private String nroDoc;
 	private String email;
 	private int tipoDocumento;
 	private String telefonos = "";
	
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getNombre() {
 		return nombre;
 	}
 
 	public void setNombre(String nombre) {
 		this.nombre = nombre;
 	}
 
 	public String getApellido() {
 		return apellido;
 	}
 
 	public void setApellido(String apellido) {
 		this.apellido = apellido;
 	}
 
 	public String getNacimiento() {
 		return nacimiento;
 	}
 
 	public void setNacimiento(String nacimiento) {
 		this.nacimiento = nacimiento;
 	}
 
 	public String getSexo() {
 		return sexo;
 	}
 
 	public void setSexo(String sexo) {
 		this.sexo = sexo;
 	}
 
 	public String getNroDoc() {
 		return nroDoc;
 	}
 
 	public void setNroDoc(String nroDoc) {
 		this.nroDoc = nroDoc;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public int getTipoDocumento() {
 		return tipoDocumento;
 	}
 
 	public void setTipoDocumento(int tipoDocumento) {
 		this.tipoDocumento = tipoDocumento;
 	}
 
 	public String getTelefonos() {
 		return telefonos;
 	}
 
 	public void setTelefonos(String telefonos) {
 		this.telefonos = telefonos;
 	}
 
 
 }
