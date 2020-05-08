 import static org.junit.Assert.*;
 
 import java.io.File;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 
 //HAY QUE PULSAR 3 VECES ENTER PORQUE ES UNA PETICION DE ROSCO Y LOS MOCKS
 //SOLO LOS HEMOS PUESTO EN ESTA PRUEBA EN EL PASAPALABRA, SINO HABRIA QUE
 // PASAR LOS MOCKS DESDE LA MAE HACIA EL RESTO DE CLASES
 
 //LO QUE HACEMOS ES QUITAR EL ARCHIVO DE PREGUNTAS RENOMBRANDOLO PARA QUE SE GENERE 
 //UN ROSCO VACIO, Y ASI PROBAMOS RAPIDO SI EL JUGAR OTRA PARTIDA FUNCIONA O NO
 
 public class PasapalabraTest {
 
 	private static Pasapalabra miPasapalabra;
 	
     /** scanner especfico para el test */
     private MockScanner2 scanner;
     /** MuestraResultados especfico para el test */
     private MockMuestraResultados2 muestraResultados;
     private File archivo;
 	
 	@Before
 	public void setUp() throws Exception {
 		miPasapalabra = Pasapalabra.getMiPasapalabra();
         scanner = new MockScanner2();
         muestraResultados = new MockMuestraResultados2();
         miPasapalabra.setIfzScanner(scanner);
         miPasapalabra.setIfzMuestraResultados(muestraResultados);
         archivo=new File("preguntasbasico.dat");
         archivo.renameTo(new File("preguntasbasico2.dat"));
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		miPasapalabra = null;
 		scanner = null;
 		muestraResultados = null;
 		archivo.renameTo(new File("preguntasbasico.dat"));
 	}
 
 	@Test
 	public void testGetMiPasapalabra() {
 		assertNotNull(Pasapalabra.getMiPasapalabra());
 	}
 
 	@Test
 	public void testEmpezarPartida() {		
 		
 		//HABRA QUE PULSAR INTRO UN PAR DE VECES, PORQUE CORRESPONDE
 		//A SOLICITUDES DE ROSCO EN LAS QUE NO PODEMOS DESDE AQUI
 		//IMPLEMENTAR LOS INTERFACES PORQUE ES EL QUE SOLICITA PASAPALABRA
 
 		miPasapalabra.jugar();
 		
 	}
 
 }
