 package es.miguelgonzalezgomez.dataBaseFun.bd;
 
 import es.miguelgonzalezgomez.dataBaseFun.bd.domain.TiposBasesDeDatos;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Miguel González
  */
 public class TextAnalizadorTextoConsulta {
     
     private AnalizadorTextoConsulta analizadorTextoConsulta;
     
     public TextAnalizadorTextoConsulta() {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
     
     @Test
     public void consultaSimpleOracle() {
         String consultaSQLAnalizar = "SELECT * FROM TABLA";
         analizadorTextoConsulta = new AnalizadorTextoConsulta(
                 TiposBasesDeDatos.TIPO_BASE_DATOS.ORACLE,
                 consultaSQLAnalizar);
         
         assertEquals(
                 "Se esperaba obtener una consulta SQL",
                 1,
                 analizadorTextoConsulta.numConsultasExistentes()
         );
         
         assertEquals(
                 "Se esperaba obtener la consulta SQL inicial",
                 consultaSQLAnalizar,
                 analizadorTextoConsulta.getConsulta(0)
         );
         
     }
     
     @Test
     public void consultaDobleOracle() {
         String consultaSQLAnalizar = "SELECT * FROM TABLA;SELECT * FROM TABLA";
         analizadorTextoConsulta = new AnalizadorTextoConsulta(
                 TiposBasesDeDatos.TIPO_BASE_DATOS.ORACLE,
                 consultaSQLAnalizar);
         
         assertEquals(
                 "Se esperaba obtener dos consultas SQL",
                 2,
                 analizadorTextoConsulta.numConsultasExistentes()
         );
         
         assertEquals(
                 "Se esperaba obtener la primera consulta SQL inicial",
                 "SELECT * FROM TABLA",
                 analizadorTextoConsulta.getConsulta(0)
         );
         
         assertEquals(
                 "Se esperaba obtener la segunda consulta SQL inicial",
                 "SELECT * FROM TABLA",
                 analizadorTextoConsulta.getConsulta(1)
         );
     }
 }
