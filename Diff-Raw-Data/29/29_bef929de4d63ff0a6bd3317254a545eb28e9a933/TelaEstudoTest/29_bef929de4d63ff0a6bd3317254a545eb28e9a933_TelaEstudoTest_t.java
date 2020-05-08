 package jogoanatomia.telas;
 
 import static org.junit.Assert.assertTrue;
 import jogoanatomia.entidades.Organ;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class TelaEstudoTest {
 	
 	private TelaEstudar window;
 
 	@Before
     public void setUp() {
 		Organ orgao = new Organ();
 		orgao.setId("asdfg");
 		orgao.setImageFileName("brain");
 		orgao.setName("Cérebro");
 		orgao.setStage(0);
         window = new TelaEstudar();
         window.setOrgao(orgao);
         window.iniciar();
         window.setVisible(true);
     }
     
     @After
     public void tearDown() {
         window.dispose();
     }
 
     /**
      * Test of main method, of class TelaInicial.
     
     @Test
     public void testMain() {
         System.out.println("main");
         String[] args = null;
         TelaInicial.main(args);     
    }*/
     
     /**
      * Testa se a tela está sendo exibida
     
     @Test
     public void testIsShowing(){
         assertTrue(window.isShowing());
    }*/
     
     /**
      * Testa se o jPanel está carregando
     
     @Test
     public void testIsPanelShowing(){
     	assertTrue(window.jPanelOrgao.isShowing());
    }*/
     
     /**
      * Testa se o jPanel está carregando
     
     @Test
     public void testIsNameOrgaoOk(){
     	assertTrue(window.jLabelOrgao.getText().equals("Cérebro"));
    }*/
     
     /**
      * Testa se o jPanel está carregando
     
     @Test
     public void testCarregaStereo(){
     	window.jRadioButtonStereo.setSelected(true);
     	assertTrue(window.jPanelOrgao.isShowing());
    }*/
     
     
 
 }
