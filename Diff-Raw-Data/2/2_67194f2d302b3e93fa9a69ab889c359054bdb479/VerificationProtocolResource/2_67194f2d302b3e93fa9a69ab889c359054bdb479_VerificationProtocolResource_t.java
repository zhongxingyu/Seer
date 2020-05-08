 package org.irmacard.web.restapi.resources;
 
 import java.lang.reflect.Type;
 import java.math.BigInteger;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import net.sourceforge.scuba.smartcards.IResponseAPDU;
 import net.sourceforge.scuba.smartcards.ResponseAPDU;
 import net.sourceforge.scuba.util.Hex;
 
 import org.irmacard.web.restapi.IRMASetup;
 import org.irmacard.web.restapi.util.CommandSet;
 import org.irmacard.web.restapi.util.ProtocolCommandSerializer;
 import org.irmacard.web.restapi.util.ResponseAPDUDeserializer;
 import org.restlet.resource.Post;
 import org.restlet.resource.ServerResource;
 
 import service.ProtocolCommand;
 import service.ProtocolResponses;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 
 import credentials.Attributes;
 import credentials.CredentialsException;
 import credentials.Nonce;
 import credentials.idemix.IdemixCredentials;
 import credentials.idemix.IdemixNonce;
 import credentials.idemix.spec.IdemixVerifySpecification;
 
 import credentials.idemix.util.VerifyCredentialInformation;
 
 /**
  * Resource for the verification protocol.
  * @author Maarten Everts
  *
  */
 public class VerificationProtocolResource extends ServerResource {
 	private final String ISSUER = "MijnOverheid";
 	private final String CRED_NAME = "ageLower";
 	private final String VERIFIER = "UitzendingGemist";
 	private final String SPEC_NAME = "ageLowerOver16";
 
 	@Post("json")
 	public String handlePost (String value) {
 		Integer crednr = Integer.parseInt((String) getRequestAttributes().get("crednr"));
 		String id = (String) getRequestAttributes().get("id");
 		String round = (String) getRequestAttributes().get("round");
 		if (id == null) {
 			return step0(crednr,value);
 		} else if (round != null && round.equals("1")) {
 			return step1(crednr,value,id);
 		}
 		return null;
 	}
 	
 	/**
 	 * Start new verification protocol
 	 * @param crednr credential number
 	 * @param value request body
 	 * @return
 	 */
 	public String step0(int crednr, String value) {
 		Gson gson = new GsonBuilder().
 				setPrettyPrinting().
 				registerTypeAdapter(ProtocolCommand.class, new ProtocolCommandSerializer()).
 				create();
 		
 		VerifyCredentialInformation vci = new VerifyCredentialInformation(
 				ISSUER, CRED_NAME, VERIFIER, SPEC_NAME);
 		IdemixCredentials ic = new IdemixCredentials();
 		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();
 
 		try {
 			CommandSet cs = new CommandSet();
 			Nonce nonce = ic.generateNonce(vspec);
 			cs.commands = ic.requestProofCommands(vspec, nonce);
 			
 			// Save the state, use random id as key
 			UUID id = UUID.randomUUID();
 			BigInteger intNonce = ((IdemixNonce)nonce).getNonce();
 			
 			@SuppressWarnings("unchecked")
 			Map<String ,BigInteger> noncemap = (Map<String,BigInteger>)getContext().getAttributes().get("noncemap");
 			noncemap.put(id.toString(), intNonce);
 
 			cs.responseurl = getReference().getPath() + "/" + id.toString() + "/1";
 			return gson.toJson(cs);
 		} catch (CredentialsException e) {
 			e.printStackTrace();
 		}
 
 		return null; 		
 	}
 	
 	/**
 	 * Handle the next step of the verification protocol.
 	 * @param crednr credential number
 	 * @param value request body (with the card responses)
 	 * @param verificationId 
 	 * @return
 	 */
 	public String step1(int crednr, String value, String verificationId) {
 		Gson gson = new GsonBuilder().
 				setPrettyPrinting().
 				registerTypeAdapter(IResponseAPDU.class, new ResponseAPDUDeserializer()).
 				create();
 		
 		// Get the nonce based on the id
 		@SuppressWarnings("unchecked")
 		Map<String ,BigInteger> noncemap = (Map<String,BigInteger>)getContext().getAttributes().get("noncemap");
 		BigInteger intNonce = noncemap.get(verificationId);
 		IdemixNonce nonce = new IdemixNonce(intNonce);
 		
 		ProtocolResponses responses = gson.fromJson(value, ProtocolResponses.class);		
 
 		VerifyCredentialInformation vci = new VerifyCredentialInformation(
 				ISSUER, CRED_NAME, VERIFIER, SPEC_NAME);
 		IdemixCredentials ic = new IdemixCredentials();
 		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();
 
 		try {
 			Attributes attr = ic.verifyProofResponses(vspec, nonce, responses);
 
 			// TODO: do something with the results!
 			if (attr == null) {
 				return "{\"response\": \"invalid\"}";
 			} else {
 				attr.print();
				return "{\"response\": \"valid\", \"url\": \"http://spuitenenslikken.bnn.nl/\"}";
 			}
 		} catch (CredentialsException e) {
 			e.printStackTrace();
 			return "{\"response\": \"invalid\"}";
 		}
 	}
 
 }
