 /**
  * $$\\ToureNPlaner\\$$
  */
 package computecore;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Map;
 import java.util.Map.Entry;
 
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

import server.Responder;

 /**
  * This class is used to represent a request for computation
  * 
  * @author Niklas Schnelle, Peter Vollmer, Sascha Meusel
  * 
  */
 public class ComputeRequest {
 
 	private final RequestPoints points;
 	private final Points resultWay;
 	private final Map<String, Object> constraints;
 	private Map<String, Object> misc;
 	private final String algName;
 	private final Responder responder;
 	private int requestID;
 
 	/**
 	 * Constructs a new ComputeRequest using the given Responder, Points and
 	 * Constraints. The requestID of the constructed ComputeRequest object is
 	 * -1, must be set with {@link #setRequestID(int)} if server is private. If
 	 * server is not private the requestID must remain -1.
 	 * 
 	 * @param responder
 	 * @param algName
	 * @param request
 	 */
 	public ComputeRequest(Responder responder, String algName,
 			RequestPoints points, Map<String, Object> constraints) {
 		this.algName = algName;
 		this.points = points;
 		this.resultWay = new Points();
 		this.constraints = constraints;
 		this.responder = responder;
 		this.misc = null;
 		this.requestID = -1;
 	}
 
 	/**
 	 * Gets the responder object which is used to send the result to the correct
 	 * client connection
 	 * 
 	 * @return
 	 */
 	public Responder getResponder() {
 		return responder;
 	}
 
 	/**
 	 * Gets the URLSuffix for the requested algorithmm e.g. "sp" for a shortest
 	 * path algorithm
 	 * 
 	 * @return
 	 */
 	public String getAlgorithmURLSuffix() {
 		return algName;
 	}
 
 	/**
 	 * Returns the Points object associated with this request
 	 * 
 	 * @return
 	 */
 	public RequestPoints getPoints() {
 		return points;
 	}
 
 	/**
 	 * Returns the Points object used to store the result of this request
 	 * 
 	 * @return
 	 */
 	public Points getResulWay() {
 		return resultWay;
 	}
 
 	/**
 	 * Returns the constraints associated with this request
 	 * 
 	 * @return
 	 */
 	public Map<String, Object> getConstraints() {
 		return constraints;
 	}
 
 	/**
 	 * Returns the misc field used to store results, initially this is null
 	 * 
 	 * @return
 	 */
 	public Map<String, Object> getMisc() {
 		return misc;
 	}
 
 	public void setMisc(Map<String, Object> misc) {
 		this.misc = misc;
 	}
 
 	/**
 	 * Sets the requestID. The requestID must be -1 if server is not in private
 	 * mode. If the requestID is not explicitly set, it is -1. </br> This
 	 * attribute should cointain the requestID of the corresponding
 	 * RequestDataset within the database. Must be set after construction of the
 	 * ComputeRequest object if server is in private mode.
	 * 
	 * @param requestDataset
 	 */
 	public void setRequestID(int requestID) {
 		this.requestID = requestID;
 	}
 
 	/**
 	 * Gets the requestID, should be -1 if server is not in private mode. This
 	 * attribute should cointain the requestID of the corresponding
 	 * RequestDataset within the database.
 	 * 
 	 * @return
 	 */
 	public int getRequestID() {
 		return this.requestID;
 	}
 
 	/**
 	 * Writes a json representation of the result of this request to the given
 	 * stream
 	 * 
 	 * @param mapper
 	 * @param stream
 	 * @throws IOException
 	 */
 	public void writeToStream(ObjectMapper mapper, OutputStream stream)
 			throws IOException {
 
 		JsonGenerator gen = mapper.getJsonFactory().createJsonGenerator(stream);
 		Map<String, Object> pconsts;
 
 		gen.setCodec(mapper);
 		gen.writeStartObject();
 
 		gen.writeArrayFieldStart("points");
 		RequestPoints points = this.getPoints();
 		for (int i = 0; i < points.size(); i++) {
 			pconsts = points.getConstraints(i);
 			gen.writeStartObject();
 			gen.writeNumberField("lt", points.getPointLat(i));
 			gen.writeNumberField("ln", points.getPointLon(i));
 			if (pconsts != null) {
 				for (Entry<String, Object> entry : pconsts.entrySet()) {
 					gen.writeObjectField(entry.getKey(), entry.getValue());
 				}
 			}
 			gen.writeEndObject();
 		}
 		gen.writeEndArray();
 
 		gen.writeArrayFieldStart("way");
 		Points way = this.getResulWay();
 		for (int i = 0; i < way.size(); i++) {
 			gen.writeStartObject();
 			gen.writeNumberField("lt", way.getPointLat(i));
 			gen.writeNumberField("ln", way.getPointLon(i));
 			gen.writeEndObject();
 		}
 		gen.writeEndArray();
 		gen.writeObjectField("misc", this.getMisc());
 		gen.writeEndObject();
 		gen.close();
 	}
 }
