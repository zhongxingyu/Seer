 package com.aap.bean.partidas;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import com.aap.dto.Competidores;
 import com.aap.dto.Eventos;
 import com.aap.dto.Partidas;
 import com.aap.dto.Pronosticos;
 import com.aap.dto.Usuarios;
 import com.aap.util.jsf.Contexts;
 
 @ManagedBean
 @ViewScoped
 public class ProximoEventoBean implements Serializable {
 
     private static final long serialVersionUID = 1081888871484697707L;
 
 	private Long idPartida;
     
     private Partidas partida = new Partidas();
     
     private Eventos evento = new Eventos();
     
     private Boolean hayCambios = Boolean.FALSE;
     
     private Boolean editable = Boolean.FALSE;
         
     private Pronosticos pronostico = new Pronosticos();
         
     List<Pronosticos> listaPronosticosSinAsignar = new ArrayList<Pronosticos>();
     
     List<Pronosticos> listaPronosticos = new ArrayList<Pronosticos>();
     
     List<Eventos> listaEventos = new ArrayList<Eventos>();
     
     public String cargarListaEventos() {
     	if(listaEventos.isEmpty()) {
 	    	Session session = Contexts.getHibernateSession();
 	    	listaEventos = session.createCriteria(Eventos.class)
 	    			.add(Restrictions.eq("ev_pa_id", partida))
 	    			.addOrder(Order.asc("ev_fecha_evento"))
 	    			.list();
     	}
     	return null;
     }
     
     public String cargarNuevoEvento() {
     	cargarEvento();
     	cargarPronosticosEvento();
     	cargarListaCompetidores();
     	return null;
     }
     
     public String guardarPronostico() {
     	Session session = Contexts.getHibernateSession();
     	Usuarios usuario = (Usuarios) Contexts.getSessionAttribute("usuario");
     	int posicion = 1;
     	for(Pronosticos pronostico:listaPronosticos) {
     		pronostico.setPr_posicion(Long.valueOf(posicion++));
     		if(pronostico.getPr_id() == null || pronostico.getPr_id().compareTo(Long.valueOf(0)) <= 0) {
     			pronostico.setPr_id(null);
     			session.save(pronostico);
     		} else {
     			session.merge(pronostico);
     		}
     	}
     	for(Pronosticos pronostico:listaPronosticosSinAsignar) {
     		if(pronostico.getPr_id() != null && pronostico.getPr_id().compareTo(Long.valueOf(0)) > 0) {
     			session.delete(pronostico);
     		}
     	}
     	
     	session.flush();
     	hayCambios = Boolean.FALSE;
     	Contexts.addInfoMessage("Tu pronóstico ha sido guardado correctamente.");
     	return null;
     }
     
     public String agregarPronostico() {    	
     	if(listaPronosticos.size() < evento.getEv_numero_pronosticos()) {
 	    	listaPronosticos.add(pronostico);
 	    	listaPronosticosSinAsignar.remove(pronostico);
 	    	
 	    	Long posicion = Long.valueOf(listaPronosticos.size());
 	    	pronostico.setPr_posicion(posicion);
 	    	
 	    	hayCambios = Boolean.TRUE;
     	} else {
     		Contexts.addErrorMessage("No se pueden agregar más pronósticos a este evento, el máximo es de " + evento.getEv_numero_pronosticos());
     	}
     	
     	return null;
     }
     
     public String subirPronostico() {
     	int posicion = pronostico.getPr_posicion().intValue();
     	if(posicion != 1) {
     		Pronosticos pronosticoAnterior = listaPronosticos.get(posicion-2);
     		pronosticoAnterior.setPr_posicion(Long.valueOf(posicion));
     		pronostico.setPr_posicion(Long.valueOf(posicion-1));
     		listaPronosticos.set(posicion-2, pronostico);
     		listaPronosticos.set(posicion-1, pronosticoAnterior);
     		hayCambios = Boolean.TRUE;
     	}
     	
     	return null;
     }
     
 
     public String bajarPronostico() {
     	int posicion = pronostico.getPr_posicion().intValue();
     	if(posicion != listaPronosticos.size()) {
     		Pronosticos pronosticoPosterior = listaPronosticos.get(posicion);
     		pronosticoPosterior.setPr_posicion(Long.valueOf(posicion));
     		pronostico.setPr_posicion(Long.valueOf(posicion+1));
     		listaPronosticos.set(posicion, pronostico);
     		listaPronosticos.set(posicion-1, pronosticoPosterior);
     		hayCambios = Boolean.TRUE;
     	}
     	
     	return null;
     }
     
     public String eliminarPronostico() {
     	listaPronosticos.remove(pronostico);
     	listaPronosticosSinAsignar.add(pronostico);
     	
     	int posicion = 1;
     	for(Pronosticos pronostico:listaPronosticos) {
     		pronostico.setPr_posicion(Long.valueOf(posicion++));
     	}
     	hayCambios = Boolean.TRUE;
     	
     	return null;
     }
     
     public Long getIdPartida() {
 		return idPartida;
 	}
 
 	public void setIdPartida(Long idPartida) {
 		this.idPartida = idPartida;
 		if(idPartida != null) {
 			Session session = Contexts.getHibernateSession();
 			partida = (Partidas) session.get(Partidas.class, idPartida);
 			cargarProximoEvento();
 			cargarPronosticosEvento();
 			cargarListaCompetidores();
 		}
 	}
 
 	private void cargarProximoEvento() {
 		Session session = Contexts.getHibernateSession();
 		String hql = "select EV " +
 				"from Eventos EV " +
 				"join EV.ev_pa_id PA " +
 				"where PA.pa_id = :ID_PARTIDA " +
 				"and EV.ev_fecha_evento = " +
 				"(select min(EV1.ev_fecha_evento) " +
 				"from Eventos EV1 " +
 				"join EV1.ev_pa_id PA1 " +
 				"where PA1.pa_id = :ID_PARTIDA " +
 				"and EV1.ev_fecha_evento >= :FECHA)";
 		Query hqlQ = session.createQuery(hql);
 		hqlQ.setLong("ID_PARTIDA", partida.getPa_id());
 		hqlQ.setDate("FECHA", new Date());
 		
 		evento = (Eventos) hqlQ.uniqueResult();
 		cargarEvento();
 		
 	}
 	
 	private void cargarEvento() {
 		if(evento != null) {
 			Date fechaInicio = evento.getEv_fecha_inicio_pronosticos();
 			Date fechaFin = evento.getEv_fecha_limite_pronosticos();
 			Date fechaActual = new Date();
 			if(fechaInicio.before(fechaActual) && fechaFin.after(fechaActual)) {
 				editable = Boolean.TRUE;
 			} else {
 				editable = Boolean.FALSE;
 			}
 		}
 	}
 	
 	private void cargarPronosticosEvento() {
 		Session session = Contexts.getHibernateSession();
 		Usuarios usuario = (Usuarios) Contexts.getSessionAttribute("usuario");
 		listaPronosticos = session.createCriteria(Pronosticos.class)
 				.add(Restrictions.eq("pr_ev_id", evento))
 				.add(Restrictions.eq("pr_usu_id", usuario))
 				.addOrder(Order.asc("pr_posicion"))
 				.list();
 	}
 	
 	private void cargarListaCompetidores() {
     	Session session = Contexts.getHibernateSession();
     	Usuarios usuario = (Usuarios) Contexts.getSessionAttribute("usuario");
     	if(usuario != null) {
 	    	String hql = "select CO " +
 	    			"from Competidores CO " +
 	    			"join CO.co_pa_id PA " +
 	    			"where PA.pa_id = :ID_PARTIDA " +
 	    			"and not exists (select CO1.co_id " +
 	    			"from Pronosticos PR " +
 	    			"join PR.pr_co_id CO1 " +
 	    			"where PR.pr_usu_id = :USUARIO " +
 	    			"and PR.pr_ev_id = :EVENTO " +
 	    			"and CO1.co_id = CO.co_id)";
 	    	Query hqlQ = session.createQuery(hql);
 	    	hqlQ.setLong("ID_PARTIDA", partida.getPa_id());
 	    	hqlQ.setParameter("USUARIO", usuario);
 	    	hqlQ.setParameter("EVENTO", evento);
 	    	List<Competidores> competidoresLibres = hqlQ.list();
 	    	int indice = -1;
 	    	for(Competidores competidor:competidoresLibres) {
 	    		
 	        	Pronosticos pronostico = new Pronosticos();
 	        	pronostico.setPr_id(Long.valueOf(indice--));
 	        	pronostico.setPr_ev_id(evento);
 	        	pronostico.setPr_usu_id(usuario);
 	        	pronostico.setPr_co_id(competidor);
 	        	
 	        	listaPronosticosSinAsignar.add(pronostico);
 	    	}
     	}
     }
 	
 	public Partidas getPartida() {
 		return partida;
 	}
 
 	public void setPartida(Partidas partida) {
 		this.partida = partida;
 	}
 
 	public Eventos getEvento() {
 		return evento;
 	}
 
 	public void setEvento(Eventos evento) {
 		this.evento = evento;
 	}
 
 	public List<Pronosticos> getListaPronosticos() {
 		return listaPronosticos;
 	}
 
 	public void setListaPronosticos(List<Pronosticos> listaPronosticos) {
 		this.listaPronosticos = listaPronosticos;
 	}
 
 	public Pronosticos getPronostico() {
 		return pronostico;
 	}
 
 	public void setPronostico(Pronosticos pronostico) {
 		this.pronostico = pronostico;
 	}
 
 	public List<Pronosticos> getListaPronosticosSinAsignar() {
 		return listaPronosticosSinAsignar;
 	}
 
 	public void setListaPronosticosSinAsignar(List<Pronosticos> listaPronosticosSinAsignar) {
 		this.listaPronosticosSinAsignar = listaPronosticosSinAsignar;
 	}
 
 	public Boolean getHayCambios() {
 		return hayCambios;
 	}
 
 	public void setHayCambios(Boolean hayCambios) {
 		this.hayCambios = hayCambios;
 	}
 
 	public Boolean getEditable() {
 		return editable;
 	}
 
 	public void setEditable(Boolean editable) {
 		this.editable = editable;
 	}
 
 	public List<Eventos> getListaEventos() {
 		return listaEventos;
 	}
 
 	public void setListaEventos(List<Eventos> listaEventos) {
 		this.listaEventos = listaEventos;
 	}
 	
 	
 }
