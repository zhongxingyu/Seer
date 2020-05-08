 package panchat.testunits;
 
import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import panchat.addressing.ListaUsuarios;
 import panchat.addressing.Usuario;
 import junit.framework.TestCase;
 
 public class ListaUsuariosTest extends TestCase {
 
 	public static boolean DEBUG;;
 	LinkedList<Usuario> lista;
 
 	ListaUsuarios listaUsuarios;
 
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		// Creamos un listado de usuarios
 		lista = new LinkedList<Usuario>();
 		lista.add(new Usuario("127.0.0.1", 50000, "Dennis"));
 		lista.add(new Usuario("127.0.0.1", 50001, "Imanol"));
 		lista.add(new Usuario("127.0.0.1", 50002, "Javier"));
 		lista.add(new Usuario("127.0.0.1", 50003, "JonAn"));
 		lista.add(new Usuario("127.0.0.1", 50004, "Nagore"));
		
		Collections.sort(lista);
 
 		listaUsuarios = new ListaUsuarios();
 	}
 
 	/**
 	 * Verificamos el funcionamiento de registrar y el iterador
 	 */
 	public void testAñadirUsuarioAndGetIterator() {
 		listaUsuarios.añadirUsuario(lista.get(0));
 		listaUsuarios.añadirUsuario(lista.get(1));
 
 		Iterator<Usuario> iter = listaUsuarios.getIterator();
 		int i;
 
 		for (i = 0; i < 2; i++) {
 			iter.next();
 		}
 		assertEquals(i, 2);
 	}
 
 	/**
 	 * Comprobamos que dos listas de usuarios con 0 elementos son iguales
 	 */
 	public void testEqualsObject() {
 		ListaUsuarios lista1 = new ListaUsuarios();
 		ListaUsuarios lista2 = new ListaUsuarios();
 		assertEquals(lista1, lista2);
 	}
 
 	/**
 	 * Comprobamos que dos listas de usuarios con el mismo elemento son
 	 * equivalentes
 	 */
 	public void testEqualsObject2() {
 		ListaUsuarios lista1 = new ListaUsuarios();
 		ListaUsuarios lista2 = new ListaUsuarios();
 		lista1.añadirUsuario(lista.get(0));
 		lista2.añadirUsuario(lista.get(0));
 		assertEquals(lista1, lista2);
 	}
 
 	/**
 	 * Comprobamos que dos listas de usuarios con distintos elementos son
 	 * distintas
 	 */
 	public void testEqualsObject3() {
 		ListaUsuarios lista1 = new ListaUsuarios();
 		ListaUsuarios lista2 = new ListaUsuarios();
 		lista1.añadirUsuario(lista.get(0));
 		lista2.añadirUsuario(lista.get(1));
 		assertFalse(lista1.equals(lista2));
 	}
 
 	/**
 	 * Comprobamos que dos listas de usuarios con distintos elementos son
 	 * distintas
 	 */
 	@SuppressWarnings("unchecked")
 	public void testDiferenciaUsuarios() {
 		ListaUsuarios lista1 = new ListaUsuarios();
 
 		for (Usuario usuario : lista)
 			lista1.añadirUsuario(usuario);
 
 		LinkedList<Usuario> lista2 = (LinkedList<Usuario>) lista.clone();
 		lista2.removeAll(lista.subList(2, 5));

 		assertEquals(lista.subList(2, 5), lista1.diferenciaUsuarios(lista2));
 	}
 }
