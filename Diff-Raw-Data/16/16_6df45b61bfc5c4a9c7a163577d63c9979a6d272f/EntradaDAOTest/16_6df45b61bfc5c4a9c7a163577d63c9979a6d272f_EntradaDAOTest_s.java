 package com.odea;
 
 import java.util.Collection;
 import java.util.Date;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.odea.dao.EntradaDAO;
 import com.odea.domain.Actividad;
 import com.odea.domain.Entrada;
 import com.odea.domain.Proyecto;
 import com.odea.domain.Usuario;
 
 public class EntradaDAOTest extends AbstractTestCase {
 
 	@Autowired
 	private EntradaDAO dao;
 	private Date antes;
 	private Date despues;
 	private Date fechaExterna;
 	private Proyecto proyecto;
 	private Usuario usuario;
 	private Actividad actividad;
 	private Entrada entrada;
 
 	
 	@Override
 	@Before
 	public void setUp() {
 		super.setUp();
 		
 		proyecto = new Proyecto(1, "Proyecto 1");
 		usuario = new Usuario(356, "Test", "mi contraseña");
 		actividad = new Actividad(1,"Actividad 1");
 		
 		antes = new Date(System.currentTimeMillis() - 1000000000);
 		despues = new Date(System.currentTimeMillis() + 1000000000);
 		fechaExterna = new Date(System.currentTimeMillis() + 2000000000);
 		
 		int ticketBZ = 1;
 		String ticketExt = "1";
 		String sistemaExt = "1";
 		
 		Date fecha = new Date(System.currentTimeMillis());
 		Date fecha2 = new Date(System.currentTimeMillis()+900000000);
 		Proyecto proyecto2 = new Proyecto(2,"otroProyecto");
 		Usuario usuario2 = new Usuario(54, "unNombre", "algunPassword");
 		double duracion = 3;
 		
		entrada = new Entrada(proyecto, actividad, "1,25", "Nota", ticketBZ, ticketExt, sistemaExt, usuario, fecha);
 		
 //		dao.agregarEntrada(new Entrada(2, proyecto, actividad, duracion, "Nota", ticketBZ, ticketExt, sistemaExt, usuario2, fecha2));
 //		dao.agregarEntrada(new Entrada(3, proyecto2, actividad, duracion, "Nota", ticketBZ, ticketExt, sistemaExt, usuario, fechaExterna));
 		
 	}
 
 	@Test
 	public void getEntradasTest(){
 		dao.agregarEntrada(entrada);
 		Collection<Entrada> col = dao.getEntradas(antes, despues);
 		Assert.assertNotNull("La cantidad de entradas encontradas no es la esperada", col);
 	}
 	
 	@Test
 	public void getEntradasDeProyectoTest(){
 		Collection<Entrada> col = dao.getEntradas(proyecto, antes, despues);
 		Assert.assertNotNull("La cantidad de entradas encontradas no es la esperada", col);
 	}
 	
 	@Test
 	public void getEntradasDeUsuarioTest(){
 		Collection<Entrada> col = dao.getEntradas(usuario, antes, despues);
 		Assert.assertNotNull("La cantidad de entradas encontradas no es la esperada", col);
 	}
 	
 	@Test
 	public void getTotalHoras(){
 		Usuario user= new Usuario(57,"invitado","invitado");
 		int total = dao.getHorasSemanales(user);
 		Assert.assertFalse("mal calculado", total<1);
 	}
 
 	
 
 
 
 }
