 package jsondto;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import play.mvc.Http.Response;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 /**
  * Utility methods for the JSON-DTO module.
  * 
  * @author Jarno Rantanen <jarno@jrw.fi>
  */
 public abstract class Util {
 
 	public static final String CHARSET = "UTF-8";
 	public static final String CONTENT_TYPE = "application/json; charset=utf-8";
 
 	/**
 	 * Helper method for converting a stream to a string.
 	 * 
 	 * @throws IOException
 	 */
 	public static String readStream(InputStream input) throws IOException {
 
 		if (input == null)
 			return "";
 
 		String linefeed = System.getProperty("line.separator");
 		StringBuilder stringBuilder = new StringBuilder();
 		String currentLine;
 
 		try {
 
 			BufferedReader reader = new BufferedReader(new InputStreamReader(input, CHARSET));
 
 			while ((currentLine = reader.readLine()) != null)
 				stringBuilder.append(currentLine).append(linefeed);
 
 		} finally {
 
 			input.close();
 
 		}
 
 		String result = stringBuilder.toString();
 
 		return result.substring(0, result.length() - linefeed.length());
 
 	}
 
 	/**
 	 * Renders the given JSON-representable Model object as a JSON string.
 	 * 
 	 * Also accepts a List of such objects, in which case a JSON list is
 	 * prepared for output.
 	 * 
 	 * TODO: Allow passing a JSONDTO directly as well..?
 	 * TODO: Add support for Collections..?
 	 * 
 	 * @throws Exception
 	 */
 	public static String getDTOsAsString(Object dtos) {
 
 		Gson gson = new GsonBuilder().create();
 		String jsonString;
 
 		if (dtos == null) {
 
 			jsonString = "null";
 
 		} else if (dtos instanceof JSONDTORepresentable) {
 
 			jsonString = gson.toJson(((JSONDTORepresentable) dtos).toDTO());
 
 		} else if (dtos instanceof List) {
 
 			ArrayList a = new ArrayList();
 
 			for (Object dto : (List) dtos)
 
 				if (dto instanceof JSONDTORepresentable)
 					a.add(((JSONDTORepresentable) dto).toDTO());
 				else
 					throw new IllegalArgumentException("Expecting a List of JSONDTORepresentable instances, " + dto.getClass() + " encountered");
 
 			jsonString = gson.toJson(a);
 
 		} else {
 
 			throw new IllegalArgumentException("Expecting (a list of) JSONDTORepresentable instances, " + dtos.getClass() + " encountered");
 
 		}
 
 		return jsonString;
 
 	}
 
 	/**
 	 * Helper for rendering the given JSON-representable Models to the given
 	 * request.
 	 * 
 	 * @see getDTOAsString()
 	 * 
 	 * @throws Exception
 	 */
 	public static void renderDTO(Object dtos, Response response) throws Exception {
 
 		String jsonString = getDTOsAsString(dtos);
 
 		response.contentType = CONTENT_TYPE;
 		response.out.write(jsonString.getBytes(CHARSET));
 
 	}
 
 }
