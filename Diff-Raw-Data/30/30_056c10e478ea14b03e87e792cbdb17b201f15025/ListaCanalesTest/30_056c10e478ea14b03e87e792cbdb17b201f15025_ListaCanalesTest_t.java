 package panchat.testunits;
 
import java.util.Collections;
 import java.util.LinkedList;
 
 import panchat.addressing.Canal;
 import panchat.addressing.ListaCanales;
 import panchat.addressing.ListaUsuarios;
 import panchat.addressing.Usuario;
 
 import junit.framework.TestCase;
 
 public class ListaCanalesTest extends TestCase {
 
 	LinkedList<Usuario> listaUsuarios;
 
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		// Creamos un listado de usuarios
 		listaUsuarios = new LinkedList<Usuario>();
 		listaUsuarios.add(new Usuario("127.0.0.1", 50000, "Dennis"));
 		listaUsuarios.add(new Usuario("127.0.0.1", 50001, "Imanol"));
 		listaUsuarios.add(new Usuario("127.0.0.1", 50002, "Javier"));
 		listaUsuarios.add(new Usuario("127.0.0.1", 50003, "JonAn"));
 		listaUsuarios.add(new Usuario("127.0.0.1", 50004, "Nagore"));
		
		Collections.sort(listaUsuarios);
 	}
 
 	public void testAñadirCanal() {
 
 		ListaCanales canales = new ListaCanales();
 		ListaUsuarios usuarios = new ListaUsuarios();
 
 		usuarios.añadirUsuario(listaUsuarios.get(0));
 		usuarios.añadirUsuario(listaUsuarios.get(1));
 		usuarios.añadirUsuario(listaUsuarios.get(2));
 
 		Canal canal1 = new Canal("Frikis", usuarios);
 
 		canales.añadirCanal(canal1);
 
 		assertEquals(3, canal1.getSize());
 	}
 	
 	public void testAñadirCanal2() {
 
 		ListaCanales canales = new ListaCanales();
 		ListaUsuarios usuarios = new ListaUsuarios();
 
 		usuarios.añadirUsuario(listaUsuarios.get(0));
 		usuarios.añadirUsuario(listaUsuarios.get(1));
 		usuarios.añadirUsuario(listaUsuarios.get(2));
 		
 		Canal canal1 = new Canal("Frikis", usuarios);
 		canal1.anyadirUsuarioConectado(listaUsuarios.get(0));
 		
 		canales.añadirCanal(canal1);
 
 		assertEquals(2, canal1.getSize());
 	}
 	
 	public void testAñadirCanal3() {
 
 		ListaCanales canales = new ListaCanales();
 		ListaUsuarios usuarios = new ListaUsuarios();
 
 		usuarios.añadirUsuario(listaUsuarios.get(0));
 		usuarios.añadirUsuario(listaUsuarios.get(1));
 		usuarios.añadirUsuario(listaUsuarios.get(2));
 		
 		Canal canal1 = new Canal("Frikis", usuarios);
 		canal1.anyadirUsuarioConectado(listaUsuarios.get(0));
 		
 		canales.añadirCanal(canal1);
 
 		canal1.getElementAt(0);
 		
 		assertEquals(listaUsuarios.get(1).nickName, canal1.getElementAt(0));
 		assertEquals(listaUsuarios.get(2).nickName, canal1.getElementAt(1));
 	}
 
 
 }
