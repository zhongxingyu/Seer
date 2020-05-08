 package org.accesointeligente.shared;
 
 public enum RequestStatus {
	NEW("Nueva", "images/pendiente.png"),
 	PENDING("Pendiente", "images/pendiente.png"),
 	EXPIRED("Vencida", "images/vencida.png"),
 	CLOSED("Cerrada", "images/cerrada.png"),
 	DERIVED("Derivada", "images/derivada.png");
 
 	private String name;
 	private String url;
 
 	private RequestStatus(String name, String url) {
 		this.name = name;
 		this.url = url;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 }
