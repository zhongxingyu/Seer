 package es.ubu.agenda.beans;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.ManagedBean;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.naming.NamingException;
 
 import org.primefaces.event.RowEditEvent;
 
 import es.ubu.agenda.modelo.Usuario;
 import es.ubu.agenda.persistencia.Fachada;
 
 
 @ManagedBean
 public class AdminUserBean implements Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -9109630369006996807L;
 	private List<Usuario> listaUsuarios;
 	private List<Usuario> listaFiltrada;
 	private Fachada fachada;
 	private String usuario_id;
 
 	public String getUsuario_id() {
 		return usuario_id;
 	}
 
 	public void setUsuario_id(String usuario_id) {
 		this.usuario_id = usuario_id;
 	}
 	
 	public AdminUserBean() throws NamingException{
 		fachada=Fachada.getInstance();
 		listaUsuarios=fachada.obtenerListaUsuarios();
 		listaFiltrada=fachada.obtenerListaUsuarios();
 	}
 
 	public List<Usuario> getListaUsuarios() {
 		return listaUsuarios;
 	}
 
 	public void setListaUsuarios(List<Usuario> listaUsuarios) {
 		this.listaUsuarios = listaUsuarios;
 	}
 	
 	public void onEdit(RowEditEvent event){
 		Usuario usu;
 		usu=(Usuario)event.getObject();
 		if(usu.getId()!=null)
 		  fachada.actualizarUsuario(usu);
 		else
 		  usu.setId(fachada.insertarUsuario(usu));
 	}
 	
     public void onCancel(RowEditEvent event){
 		
 		
 	}
     
    public void onDelete(String id) throws NamingException{
 		fachada.eliminarUsuario(id);
 		int contador=0;
 		for(Usuario u:listaUsuarios){
 			if(u.getId().compareTo(id)==0)
 				break;
 			contador++;
 		}
 		listaUsuarios.remove(contador);
		ExternalContext tmpEC;
		Map<?, ?> sMap;
	    tmpEC = FacesContext.getCurrentInstance().getExternalContext();
	    sMap = tmpEC.getSessionMap();
	    AdminEventoBean adminEvento = (AdminEventoBean) sMap.get("adminEventoBean");
	    adminEvento.updateEvents();
     }
 
 	public List<Usuario> getListaFiltrada() {
 		return listaFiltrada;
 	}
 
 	public void setListaFiltrada(List<Usuario> listaFiltrada) {
 		this.listaFiltrada = listaFiltrada;
 	}
     
 	
 	public void anadirUsuario(){
 		listaUsuarios.add(new Usuario());
 	}
     
 	
 	public String desconectar(){
 		ExternalContext tmpEC;
 	    Map<?, ?> sMap;
 	    tmpEC = FacesContext.getCurrentInstance().getExternalContext();
 	    sMap = tmpEC.getSessionMap();
 	    UserBean user = (UserBean) sMap.get("UserBean");
 	    return user.logout();
 	}
 	
 	public String irAlCalendario(){
 		return "calendario";
 	}
 	
 }
