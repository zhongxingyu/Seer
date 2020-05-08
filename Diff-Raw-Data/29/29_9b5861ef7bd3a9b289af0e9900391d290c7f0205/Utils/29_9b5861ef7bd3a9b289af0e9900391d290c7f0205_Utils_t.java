 package linkedbrainz.d2rs.translator.util;
 
 import java.net.URI;
 
 /**
  * A utils class for the translator. Includes methods for, e.g., base-URI
  * clean-up tasks.
  * 
  * @author zazi
  * 
  */
 public class Utils
 {
 	public static String cleanUpBaseURI(String uriString, String baseUriString)
 	{
 		URI baseUri = URI.create(baseUriString);
 		URI uri = null;
 
 		try
 		{
 			uri = URI.create(uriString);
 		} catch (IllegalArgumentException e)
 		{
 			System.out
 					.println("[EXEC] Exception while processing a URI for a Translator clean-up task: "
 							+ e.getMessage());
 
 			// the URI is not a valid HTTP URI, so just return what you got (it
 			// wouldn't be recognized by further transformation)
 			return null;
 		}
 
 		// divide URI hosts into their parts String[] baseUriHostParts =
 		String baseUriHost = baseUri.getHost();
 		String uriHost = uri.getHost();
 		String[] baseUriHostParts = baseUriHost.split("\\.");
 		String[] uriHostParts = uriHost.split("\\.");
 
 		/*
 		 * System.out.println("[EXEC]  URI string: " + uriString +
 		 * " :: URI host: " + uri.getHost() + " :: uriHostParts.length: " +
 		 * uriHostParts.length + " :: baseUri string: " + baseUriString +
 		 * " :: base URI host: " + baseUri.getHost() +
 		 * " :: baseUriHostParts.length: " + baseUriHostParts.length);
 		 */
 
 		// int counter = 1;
 
 		/*
 		 * for (int i = baseUriHostParts.length; i > 0; i--) { // URI host part
 		 * does not match the base URI host part if
 		 * (!uriHostParts[uriHostPartsLength - counter]
 		 * .equals(baseUriHostParts[baseUriHostPartsLength - counter])) { //
 		 * replace URI host part uriHostParts[uriHostPartsLength - counter] =
 		 * baseUriHostParts[baseUriHostPartsLength - counter]; }
 		 * 
 		 * counter++;
 		 * 
 		 * // base URI host has more parts as the URI // stop checking, every
 		 * possible replacements are be d if (counter > uriHostPartsLength) {
 		 * break; } }
 		 * 
 		 * if(uriHostPartsLength > baseUriHostPartsLength) {
 		 * 
 		 * } else if(baseUriHostPartsLength > uriHostPartsLength) {
 		 * 
 		 * }
 		 */
 
 		// compare second top level domain
 		if (!uriHostParts[uriHostParts.length - 2]
 				.equals(baseUriHostParts[baseUriHostParts.length - 2]))
 		{
 			System.out
 					.println("[EXEC]  non-valid URI for this information service: "
 							+ uriString
 							+ " :: 2nd top-level uriHostPart: "
 							+ uriHostParts[uriHostParts.length - 2]
 							+ " + 2nd top-level baseUriHostPart: "
 							+ baseUriHostParts[baseUriHostParts.length - 2]);
 
 			// the URI couldn't be from this information service
 			return null;
 		}
 
 		// construct a consistent URI
 		// use the consistent base URI host
 		String tempUri = "http://" + baseUri.getHost();
 
 		if (baseUri.getPort() != -1)
 		{
 			// use the consistent base URI port
 			tempUri = tempUri + ":" + baseUri.getPort();
 		}
 
 		if (uri.getPath() != null)
 		{
 			// use the URI path part
 			tempUri = tempUri + uri.getPath();
 		}
 
 		if (uri.getQuery() != null)
 		{
 			// use the URI query part
			tempUri = tempUri + "?" + uri.getQuery();
 		}
 
 		if (uri.getFragment() != null)
 		{
 			// use the URI fragment id
			tempUri = tempUri + "#" + uri.getFragment();
 		}
 
 		// System.out.println("[EXEC]  transformed URI: " + tempUri);
 
 		return tempUri;
 	}
 
 }
