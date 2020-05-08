 package com.genfersco.sepbas.web.json;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.genfersco.sepbas.domain.model.Jugador;
 
 public class JugadorJSONResponse extends DefaultJSONResponse {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1014666872301956967L;
 	private List<Jugador> jugadores;
 
 	public JugadorJSONResponse(String status, String message,
 			List<Jugador> jugadores) {
 		super(status, message);
 		this.jugadores = jugadores;
 	}

 	@Override
	public Object getContent() {
		// avoids complex json mapping for front-end devs.
 		List<JugadorVO> resultList = new ArrayList<JugadorJSONResponse.JugadorVO>();
		for (Jugador j : jugadores) {
 			resultList.add(new JugadorVO(j));
 		}
 		return resultList;
 	}
 
 	private class JugadorVO {
 
 		private Integer id;
 		private Date fechaNacimiento;
 		private String nombre;
 		private String apellido;
 		private Integer numero;
 
 		public JugadorVO(Jugador jugador) {
 			this.id = jugador.getId();
 			this.fechaNacimiento = jugador.getFechaNacimiento();
 			this.nombre = jugador.getNombre();
 			this.apellido = jugador.getApellido();
 			this.numero = jugador.getNumero();
 		}
 
 		public Integer getId() {
 			return id;
 		}
 
 		public void setId(Integer id) {
 			this.id = id;
 		}
 
 		public Date getFechaNacimiento() {
 			return fechaNacimiento;
 		}
 
 		public void setFechaNacimiento(Date fechaNacimiento) {
 			this.fechaNacimiento = fechaNacimiento;
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
 
 		public Integer getNumero() {
 			return numero;
 		}
 
 		public void setNumero(Integer numero) {
 			this.numero = numero;
 		}
 
 	}
 
 }
