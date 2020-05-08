 package federacao.idpmais;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import keystone.FederatedKeystoneTest;
 
 import org.junit.Before;
 
 public class AuthFederatedKeystonePingaIDPMaisTest extends FederatedKeystoneTest{
 
 	static String PINGA_ENDPOINT = "https://pinga.ect.ufrn.br:5000/v2.0";
 	static String PINGA_ENTITY_ID = "https://pinga.ect.ufrn.br:5000";
 	
	static String PILOTO_ENTITY_ID = "https://gt-cnc2.pop-pa.rnp.br:5000/";
 	
 	static String IDPMAIS_REALM = "IdP+ STCFED";
 	static String IDPMAIS_ENDPOINT = "http://idpstcfed.sj.ifsc.edu.br/RNPSecurityTokenService/RNPSTS";
 	static String USERNAME1 = "funcionario";
 	static String PASSWORD1 = "funcionario123";
 	
 	static String IDPMAIS_EXPRESSO_REALM = "IdP IDP1.cafeexpresso"; 
 	static String IDPMAIS_EXPRESSO_ENDPOINT = "https://idp-mais.cafeexpresso.rnp.br/RNPSecurityTokenService/RNPSTS";
 	static String USERNAME_EXPRESSO = "joaogt";
 	static String PASSWORD_EXPRESSO = "joao123";
 	
 
 	static String TENANTID = "312e06f400834de395ce41e7ac28e0cc";
 	
 	
 	@Before
 	public void setUp() throws Exception {
		keystoneClient = new AuthFederatedKeystonePingaIDPMais(PINGA_ENDPOINT);
		this.KEYSTONE_ENDPOINT = PINGA_ENDPOINT;
 		this.REALM = IDPMAIS_REALM;
 		this.IDP_ENDPOINT = IDPMAIS_ENDPOINT;
		this.ENTITY_ID = PINGA_ENTITY_ID;
 		
 		keystoneClient.setUsername(USERNAME1);
 		keystoneClient.setPassword(PASSWORD1);
 	}
 
 	public void processRealm(List<String> realms) {
 		boolean found = false;
 		selectedRealm = null;
 		for (String realm : realms) {
 			if (realm.contains(REALM)) {
 				found = true;
 				selectedRealm = realm;
 			}
 			System.out.println("Realm: " + realm);
 		}
 		assertTrue("Expected realm \"" + REALM + "\" not found.", found);
 		assertNotNull(selectedRealm);
 		
 	}
 }
