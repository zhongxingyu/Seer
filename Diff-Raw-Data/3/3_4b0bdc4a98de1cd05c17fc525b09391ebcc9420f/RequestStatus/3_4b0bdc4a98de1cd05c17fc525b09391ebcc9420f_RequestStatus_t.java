 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.shared;
 
 public enum RequestStatus {
 	DRAFT("Borrador", "images/borrador.png"),
 	NEW("Pendiente", "images/pendiente.png"),
 	PENDING("Pendiente", "images/pendiente.png"),
 	EXPIRED("Vencida", "images/vencida.png"),
 	CLOSED("Cerrada", "images/cerrada.png"),
	DERIVED("Derivada", "images/derivada.png"),
	ERROR("Pendiente", "images/pendiente.png");
 
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
