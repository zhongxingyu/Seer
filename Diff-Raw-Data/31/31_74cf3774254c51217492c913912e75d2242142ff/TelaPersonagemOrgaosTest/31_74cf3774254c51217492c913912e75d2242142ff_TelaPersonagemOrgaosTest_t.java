 package jogoanatomia.telas;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 
 import jogoanatomia.entidades.Organ;
 import jogoanatomia.entidades.User;
 import jogoanatomia.entidades.User.Character;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  *
  * @author Jessica
  */
 public class TelaPersonagemOrgaosTest {
 	private TelaPersonagemOrgaos window;
 	
 	User usuario;
 	
 	List<Organ> orgaos = new ArrayList<Organ>();
 	
 	Character personagem;
 		@Before
 	    public void setUp() {
 			personagem = new Character();
 			personagem.setKind("male");
 			personagem.setCurrentStage(7);
 			usuario = new User();
 			usuario.setId("abcd");
 			usuario.setCharacter(personagem);
 			Organ orgao = new Organ();
 			orgao.setId("asdfg");
 			orgao.setImageFileName("teeth");
 			orgao.setName("Dentes");
 			orgao.setStage(7);
 			Organ orgao1 = new Organ();
 			orgao1.setId("fdsas");
 			orgao1.setImageFileName("eyes");
 			orgao1.setName("Olhos");
 			orgao1.setStage(8);
 			orgaos.add(orgao);
 			orgaos.add(orgao1);
 	        window = new TelaPersonagemOrgaos(usuario, orgaos);
 	        window.setVisible(true);
 	    }
 	    
 	    @After
 	    public void tearDown() {
 	        window.dispose();
 	    }
 	
 	    /**
 	     * Test of main method, of class TelaInicial.
 	     */
 	    @Test
 	    public void testMain() {
 	        System.out.println("main");
 	        String[] args = null;
 	        TelaInicial.main(args);     
 	    }
 	    
 	    /**
 	     * Testa se a tela está sendo exibida
 	     */
 	    @Test
 	    public void testIsShowing(){
 	        assertTrue(window.isShowing());
 	    }
 	    
 	    /**
 	     * Testa se a tela está sendo exibida
 	     */
 	    @Test
 	    public void testOrgansOk(){
 	    	ImageIcon icon = new ImageIcon(
 					TelaSelecionaPersonagens.class
 					.getResource("/images/imagem8-homem.png"));
//	        assertTrue(window.jLabelPersonagens.getIcon().toString().equals(icon.toString()));
 	    }
 	
 }
