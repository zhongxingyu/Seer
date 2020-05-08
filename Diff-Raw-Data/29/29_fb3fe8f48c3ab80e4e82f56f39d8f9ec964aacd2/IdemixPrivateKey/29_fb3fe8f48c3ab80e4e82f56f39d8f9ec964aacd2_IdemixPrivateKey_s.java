 package credentials.idemix;
 
 import java.net.URI;
 
 import com.ibm.zurich.idmx.key.IssuerKeyPair;
 import com.ibm.zurich.idmx.key.IssuerPrivateKey;
 import com.ibm.zurich.idmx.showproof.ProofSpec;
 import com.ibm.zurich.idmx.utils.StructureStore;
 
 import credentials.keys.PrivateKey;
 
 public class IdemixPrivateKey implements PrivateKey {
 	private IssuerPrivateKey privateKey;
 
 	public IdemixPrivateKey(IssuerPrivateKey privateKey) {
 		this.privateKey = privateKey;
 	}
 	
 	/**
 	 * Create an IdemixPrivateKey based on an Idemix Private Key Specification
 	 * XML file.
 	 * 
 	 * Note: for now we assume that the system parameters, group parameters and
 	 * issuer public key have already been loaded. Things do seem to work when
 	 * this is not the case, but keep in mind that the private key cannot be
 	 * used by the library until the public key and system and group parameters
 	 * have also been loaded.
 	 */
 	public static IdemixPrivateKey fromIdemixPrivateKey(URI privateKeyLoc) {
 		IssuerKeyPair ikp = (IssuerKeyPair) StructureStore.getInstance().get(
 				privateKeyLoc);
 		
 		return new IdemixPrivateKey(ikp.getPrivateKey());
 	}
	
 	public IssuerPrivateKey getPrivateKey() {
 		return privateKey;
 	}
 }
