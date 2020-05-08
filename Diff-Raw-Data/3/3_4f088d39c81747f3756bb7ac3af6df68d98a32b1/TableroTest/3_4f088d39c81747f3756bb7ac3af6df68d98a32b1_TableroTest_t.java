 package pruebas;
 
 import static org.junit.Assert.*;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.LinkedList;
 
 import naves.Nave;
 import naves.Sentido;
 
 import org.junit.Test;
 
 import excepciones.ErrorIdCasilleroInvalido;
 
 import tablero.*;
 
 public class TableroTest {
 
 	@Test
 	public void testCrear() {
 		Tablero tablero = new Tablero();
 		assertTrue(tablero != null);
 	}
 
 	@Test
 	public void testSeCreaSinNaves() {
 		Tablero tablero = new Tablero();
 		assertFalse(tablero.tieneNaves());
 	}
 
 	@Test
 	public void testPonerNavesAleatoriamente() {
 		// Se testea la existencia de las naves, y su tipo
 		// no la aleatoreidad de sus posiciones.
 		Tablero tablero = new Tablero();
 		tablero.posicionarNavesAleatoriamente();
 		assertTrue(tablero.cantidadTotalDeNaves() == 7);
 		LinkedList<Nave> naves = tablero.devolverNaves();
 
 		for (Nave nave : naves) {
 			assertTrue(nave instanceof Nave);
 		}
 	}
 
 	@Test
 	public void testPedirCasilleroValido() {
 		Tablero tablero = new Tablero();
 		int[] id = { 0, 0 };
 		Casillero casillero = tablero.obtenerCasillero(id);
 		assertTrue(casillero instanceof Casillero);
 	}
 
 	@Test(expected = ErrorIdCasilleroInvalido.class)
 	public void testPedirCasilleroInvalido() {
 		Tablero tablero = new Tablero();
 		int[] id = { -10, 10 };
 		tablero.obtenerCasillero(id);
 
 	}
 
 	@Test
 	public void patronDeSumaParaUbicarNaveSur() throws NoSuchMethodException,
 			IllegalAccessException, IllegalArgumentException,
 			InvocationTargetException {
 
 		// pruebas por reflexion
 		Tablero tablero = new Tablero();
 		Sentido unSentido = Sentido.SUR;
 		int[] patronSur = { 0, -1 };
 
 		Method patronDeSuma = Tablero.class.getDeclaredMethod(
 				"patronDeSumaParaTrayectoriaDeNave", Sentido.class);
 		patronDeSuma.setAccessible(true);
 
 		int[] patron = (int[]) patronDeSuma.invoke(tablero, unSentido);
 
 		assertTrue(patron[0] == patronSur[0] && patron[1] == patronSur[1]);
 	}
 
 	@Test
 	public void patronDeSumaParaUbicarNaveNoreste()
 			throws NoSuchMethodException, IllegalAccessException,
 			IllegalArgumentException, InvocationTargetException {
 
 		// pruebas por reflexion
 		Tablero tablero = new Tablero();
 		Sentido unSentido = Sentido.NORESTE;
 		int[] patronNorEste = { 1, 1 };
 
 		Method patronDeSuma = Tablero.class.getDeclaredMethod(
 				"patronDeSumaParaTrayectoriaDeNave", Sentido.class);
 		patronDeSuma.setAccessible(true);
 
 		int[] patron = (int[]) patronDeSuma.invoke(tablero, unSentido);
 
 		assertTrue(patron[0] == patronNorEste[0]
 				&& patron[1] == patronNorEste[1]);
 	}
 
 	@Test
 	public void buscarCasilleroParaProa() throws NoSuchMethodException,
 			IllegalAccessException, IllegalArgumentException,
 			InvocationTargetException {
 		// pruebas por reflexion
 		// Se testea que este dentro de los limites de seguridad para la
 		// ubicacion de naves
 		Tablero tablero = new Tablero();
 		int limiteInferior = 3;
 		int limiteSuperior = 6;
 
 		Method buscarCasilleroParaProa = Tablero.class
 				.getDeclaredMethod("buscarCasilleroParaProa");
 		buscarCasilleroParaProa.setAccessible(true);
 
 		for (int i = 0; i < 100; i++) {
 			int[] idCasillero = (int[]) buscarCasilleroParaProa.invoke(tablero);
 
 			assertTrue(idCasillero[0] > limiteInferior
 					&& idCasillero[0] < limiteSuperior
 					&& idCasillero[1] > limiteInferior
 					&& idCasillero[1] < limiteSuperior);
 		}
 	}
 	@Test
 	public void sumarPatronDeSumaEId() throws NoSuchMethodException,
 			IllegalAccessException, IllegalArgumentException,
 			InvocationTargetException {
 		// pruebas por reflexion
	
 		Tablero tablero = new Tablero();
 		int[] id = {1,0};
 		int[] patronDeSuma = {2,1};
 
 		Method sumarPatronDeSumaEId = Tablero.class
 				.getDeclaredMethod("sumarPatronDeSumaEId", int[].class, int[].class);
 		sumarPatronDeSumaEId.setAccessible(true);
 
 			int[] idResultante = (int[]) sumarPatronDeSumaEId.invoke(tablero, id, patronDeSuma);
 
 			assertTrue(idResultante[0] == 3 && idResultante[1] == 1 );
 		}
 
 }
 
