 package es.ubu.agenda.beans;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.annotation.ManagedBean;
 import javax.naming.NamingException;
 
 import org.primefaces.event.RowEditEvent;
 
 import es.ubu.agenda.modelo.Tarea;
 import es.ubu.agenda.modelo.Usuario;
 import es.ubu.agenda.persistencia.Fachada;
 
 @ManagedBean
 public class AdminEventoBean implements Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private List<Tarea> listaTareas;
 	private List<Usuario> listaUsuarios;
 	private Fachada fachada;
 	private String evento_id;
 
 
 	public String getEvento_id() {
 		return evento_id;
 	}
 
 	public void setEvento_id(String evento_id) {
 		this.evento_id = evento_id;
 	}
 
 	public AdminEventoBean() throws NamingException {
 		fachada = Fachada.getInstance();
 		listaTareas = fachada.obenerTareas();
 		listaUsuarios = fachada.obtenerListaUsuarios();
 
 	}
 
 	public List<Tarea> getListaTareas() {
 		return listaTareas;
 	}
 
 	public void setListaTareas(List<Tarea> listaTareas) {
 		this.listaTareas = listaTareas;
 	}
 
 	public List<Usuario> getListaUsuarios() {
 		return listaUsuarios;
 	}
 
 	public void setListaUsuarios(List<Usuario> listaUsuarios) {
 		this.listaUsuarios = listaUsuarios;
 	}
 
 	public void onEdit(RowEditEvent event) throws NamingException {
 		Tarea t;
 		t = (Tarea) event.getObject();
 		if(t.getId()!=null)
 		  fachada.actualizarTarea(t, t.getId());
 		else{
 		  t.rellenarIDUsuario();
 		  t.setId(fachada.insertarTarea(t));
 		}
 	}
 
 	public void onCancel(RowEditEvent event) {
 
 	}
 
 	public void onDelete(String id) {
 		fachada.eliminarEvento(id);
 		int contador = 0;
 		for (Tarea t : listaTareas) {
 			if (t.getId().compareTo(id) == 0)
 				break;
 			contador++;
 		}
 		listaTareas.remove(contador);
 	}
 
 	public void anadirEvento() {
 		listaTareas.add(new Tarea());
 	}
 
 }
