 package com.aap.bean.partidas;
 
 import java.io.Serializable;
import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 
 import org.hibernate.Session;
 
import com.aap.dto.Competidores;
import com.aap.dto.Eventos;
import com.aap.dto.Mensajes;
 import com.aap.dto.Partidas;
import com.aap.dto.PuntosPosicion;
 import com.aap.dto.Usuarios;
 import com.aap.util.jsf.Contexts;
 import com.aap.util.jsf.FuncionesJSF;
 
 @ManagedBean
 @ViewScoped
 public class CrearPartidaBean implements Serializable {
 	
     private static final long serialVersionUID = -7685451850610125693L;
 	
     private Partidas partida = new Partidas();
     
     public String guardarPartida() {
     	if(validaGuardarPartida()) {
     		Session session = Contexts.getHibernateSession();
    		partida.setListaEventos(new ArrayList<Eventos>());
			partida.setListaCompetidores(new ArrayList<Competidores>());
			partida.setListaMensajes(new ArrayList<Mensajes>());
			partida.setListaPuntosPosicion(new ArrayList<PuntosPosicion>());
     		if(partida.getPa_id() == null) {
     			Usuarios usuario = (Usuarios) Contexts.getSessionAttribute("usuario");
     			Set<Usuarios> administradores = new HashSet<Usuarios>();
     			administradores.add(usuario);
     			partida.setAdministradores(administradores);
     			partida.setUsuarios(administradores);
     			session.save(partida);
     		} else {
     			session.merge(partida);
     		}
     		session.flush();
     		Contexts.addInfoMessage("Partida guardada correctamente.");
     	}
     	return null;
     }
     
     private boolean validaGuardarPartida() {
     	if(partida == null) {
     		Contexts.addErrorMessage("No hay ninguna partida para guardar.");
     	} else {
     		if(partida.getPa_nombre() == null || partida.getPa_nombre().isEmpty()) {
     			Contexts.addErrorMessage("El nombre de la partida es obligatorio.");
     		}
     		if(partida.getPa_descripcion() == null || partida.getPa_descripcion().isEmpty()) {
     			Contexts.addErrorMessage("La descripción de la partida es obligatorio.");
     		}
     	}
     	return !FuncionesJSF.hayErrores();
     }
 
 	public Partidas getPartida() {
 		return partida;
 	}
 
 	public void setPartida(Partidas partida) {
 		this.partida = partida;
 	}
 
 	
 }
