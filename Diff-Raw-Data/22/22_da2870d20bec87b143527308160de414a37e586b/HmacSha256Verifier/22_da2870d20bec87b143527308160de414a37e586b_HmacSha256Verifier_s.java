 package ch.hsr.bieridee.auth;
 
 import ch.hsr.bieridee.exceptions.WrongNodeTypeException;
 import ch.hsr.bieridee.models.UserModel;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.bouncycastle.util.encoders.Hex;
 import org.neo4j.graphdb.NotFoundException;
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.security.Verifier;
 import org.restlet.util.Series;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 import java.security.InvalidKeyException;
 import java.security.Key;
 import java.security.NoSuchAlgorithmException;
 
 /**
  * This verifier verifies the HMAC-SHA256 auth header.
  */
 public class HmacSha256Verifier implements Verifier {
 
 	private static final Logger LOG = Logger.getLogger(HmacSha256Verifier.class);
 
 	/**
 	 * Attempts to verify the credentials provided by the client user sending
 	 * the request.
 	 *
 	 * @param request  The request sent.
 	 * @param response The response to update.
 	 * @return Result of the verification based on the RESULT_* constants.
 	 */
 	@Override
 	public int verify(Request request, Response response) {
 
 		// Get authorization header
 		final Series reqHeaders = (Series) request.getAttributes().get("org.restlet.http.headers");
 		final String reqAuthorization = reqHeaders.getFirstValue("Authorization");
 
 		if (reqAuthorization == null) {
 			return RESULT_MISSING; // no credentials provided
 		}
 
 		// Process authorization header
 		final String[] reqAthorizationArray = reqAuthorization.split(":");
 		if (reqAthorizationArray.length != 2) {
 			return RESULT_INVALID; // invalid credentials
 		}
 
 		final String reqUsername = reqAthorizationArray[0];
 		final String reqHMAC = reqAthorizationArray[1];
 
 		// Get user model
 		UserModel userModel = null;
 		try {
 			userModel = new UserModel(reqUsername);
 		} catch (NotFoundException e) {
 			LOG.error("User " + reqUsername + " not found!");
 			return RESULT_UNKNOWN; // unknown user
 		} catch (WrongNodeTypeException e) {
 			e.printStackTrace();
 			throw new RuntimeException("Invalid node type.", e);
 		}
 
 		// Get request information for HMAC calculation
 		final String reqMethod = request.getMethod().getName();
 		final String reqUri = request.getOriginalRef().getIdentifier(true);
 		final String reqAccept = StringUtils.defaultString(reqHeaders.getFirstValue("Accept"));
 		final String reqContentLength = StringUtils.defaultString(reqHeaders.getFirstValue("Content-Length"), "0");
		final String reqBody = StringUtils.defaultString(request.getEntityAsText());
 
 		// Calculate HMAC
 		final String hmacInputString = reqMethod + reqUri + reqAccept + reqContentLength + reqBody;
 		final Key signingKey = new SecretKeySpec(userModel.getPassword().getBytes(), "HmacSHA256");
 		final String calculatedHMAC;
 		try {
 			calculatedHMAC = calculateHMAC(hmacInputString, signingKey);
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 			throw new RuntimeException("HmacSHA256 algorithm missing");
 		} catch (InvalidKeyException e) {
 			e.printStackTrace();
 			throw new RuntimeException("Invalid key for HmacSHA256 signing");
 		}
 
 		// Verify HMAC
 		if (reqHMAC.equals(calculatedHMAC)) {
 			return RESULT_VALID;
 		} else {
 			return RESULT_INVALID;
 		}
 	}
 
 	/**
 	 * Calculate the HMAC-SHA256 for the given input data and the given signing key.
 	 *
 	 * @param hmacInputString The input string that should be hashed
 	 * @param signingKey The signing key that is used to calculate the MAC
 	 * @return The calculated HMAC-SHA256 string
 	 * @throws java.security.NoSuchAlgorithmException Thrown if HMAC-SHA256 algorithm was not found.
 	 * @throws java.security.InvalidKeyException Thrown if key for HMAC-SHA256 signing is invalid.
 	 */
 	private String calculateHMAC(String hmacInputString, Key signingKey) throws NoSuchAlgorithmException, InvalidKeyException {
 		String calculatedHMAC = "";
 
 		final Mac m = Mac.getInstance("HmacSHA256");
 		m.init(signingKey);
 		m.update(hmacInputString.getBytes());
 		final byte[] macBytes = m.doFinal();
 		calculatedHMAC = new String(Hex.encode(macBytes));
 
 		return calculatedHMAC;
 	}
 }
