 package iaws.NBMR.ws.contractfirst;
 
 import static org.springframework.ws.test.server.RequestCreators.withPayload;
 import static org.springframework.ws.test.server.ResponseMatchers.payload;
 import iaws.NBMR.domaines.Coordonnees;
 import iaws.NBMR.domaines.Utilisateur;
 import iaws.NBMR.exception.CustomException;
 import iaws.NBMR.service.UtilisateurService;
 import iaws.NBMR.services.impl.DataServiceImpl;
 import iaws.NBMR.services.impl.UtilisateurServiceImpl;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.ws.test.server.MockWebServiceClient;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("application-context.xml")
 public class TestIntegrationRechercheVoisinsEndpoint {
 	@Autowired
     private ApplicationContext applicationContext;
 
     private MockWebServiceClient mockClient;
 
     @Before
     public void createClient() {
         mockClient = MockWebServiceClient.createClient(applicationContext);
     }
 
     @Before
     public void insertMock() throws CustomException{
     	UtilisateurService us = new UtilisateurServiceImpl();
    	Utilisateur u1 = new Utilisateur("user1", "prenom1", "user1@exemple.com", "10, Route de narbonne, Toulouse");
     	us.inscrireUtilisateur(u1);
    	Utilisateur u2 = new Utilisateur("user2", "prenom2", "user2@exemple.com", "50, Route de narbonne, Toulouse");
     	us.inscrireUtilisateur(u2);
    	Utilisateur u3 = new Utilisateur("user3", "prenom3", "user3@exemple.com", "20, Route de narbonne, Toulouse");
     	us.inscrireUtilisateur(u3);
     }
     
     @Test
     public void rechercheVoisinsEndpoint() throws Exception {
     	
         Source requestPayload = new StreamSource(new ClassPathResource("RechercheVoisinsRequest.xml").getInputStream() );
         Source responsePayload = new StreamSource(new ClassPathResource("RechercheVoisinsResponse.xml").getInputStream());
         
         mockClient.sendRequest(withPayload(requestPayload)).andExpect(payload(responsePayload));
     }
 }
