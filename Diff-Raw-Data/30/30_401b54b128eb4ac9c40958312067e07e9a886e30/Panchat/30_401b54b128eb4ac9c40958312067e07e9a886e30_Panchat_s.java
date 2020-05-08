 package panchat;
 
 import panchat.addressing.channels.ListaCanales;
 import panchat.addressing.users.ListaUsuarios;
 import panchat.addressing.users.Usuario;
 import panchat.linker.CausalLinker;
 import panchat.linker.Connector;
 import panchat.linker.Linker;
 
 public class Panchat {
 	private ListaUsuarios listaUsuarios;
 	private ListaCanales listaCanales;
 	private Usuario usuario;
 	private CausalLinker causalLinker;
 	private Linker linker;
 	private Connector connector;
 
 	/**
 	 * 
 	 * @param usuario
 	 */
 	public Panchat(Usuario usuario) {
 		this.usuario = usuario;
		this.listaUsuarios = new ListaUsuarios();
 		this.listaCanales = new ListaCanales();
 		// Nos añadimos a nuestra propia lista de usuarios
 		this.listaUsuarios.añadirUsuario(usuario);
 
 		this.connector = new Connector(this);
 		this.causalLinker = new CausalLinker(this);
 		this.linker = new Linker(this);
 
 		// Como el hilo MulticastListenerThread depende de causalLinker, lo
 		// arrancamos después para evitar una condicción de carrera al
 		// instanciar las clases.
 		this.connector.arrancarMulticastListenerThread();
 	}
 
 	/**
 	 * Devuelve la lista de usuarios del chat
 	 * 
 	 * @return
 	 */
 	public ListaUsuarios getListaUsuarios() {
 		return listaUsuarios;
 	}
 
 	/**
 	 * Devuelve la lista de canales del chat
 	 * 
 	 * @return
 	 */
 	public ListaCanales getListaCanales() {
 		return listaCanales;
 	}
 
 	/**
 	 * Devuelve la lista de canales del chat
 	 * 
 	 * @return
 	 */
 	public Usuario getUsuario() {
 		return usuario;
 	}
 
 	/**
 	 * Devuelve el causal linker asociado al chat
 	 * 
 	 * @return
 	 */
 	public CausalLinker getCausalLinker() {
 		return causalLinker;
 	}
 
 	/**
 	 * Devuelve el linker asociado al chat
 	 * 
 	 * @return
 	 */
 	public Linker getLinker() {
 		return linker;
 	}
 
 	/**
 	 * Devuelve la clase connector
 	 * 
 	 * @return
 	 */
 	public Connector getConnector() {
 		return connector;
 	}
 
 	public void desegistrarCliente() {
 		connector.enviarSaludo(false);
 
 		connector.closeSockets();
 	}
 
 }
