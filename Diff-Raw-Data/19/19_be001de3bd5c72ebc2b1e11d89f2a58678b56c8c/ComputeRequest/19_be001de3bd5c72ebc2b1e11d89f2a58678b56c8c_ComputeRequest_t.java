 /**
  * $$\\ToureNPlaner\\$$
  */
 package de.tourenplaner.computecore;
 
 import de.tourenplaner.server.Responder;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * This class is used to represent a request for computation
  * 
  * @author Niklas Schnelle, Peter Vollmer, Sascha Meusel
  * 
  */
 public class ComputeRequest {
 
 	private final de.tourenplaner.computecore.RequestPoints points;
 	private final List<de.tourenplaner.computecore.Way> resultWays;
 	private final Map<String, Object> constraints;
 	private Map<String, Object> misc;
 	private final String algName;
 	private final Responder responder;
 	private int requestID;
     private final boolean acceptsSmile;
 
 	/**
 	 * Constructs a new ComputeRequest using the given Responder, Points and
 	 * Constraints. The requestID of the constructed ComputeRequest object is
 	 * -1, must be set with {@link #setRequestID(int)} if server is private. If
 	 * server is not private the requestID must remain -1.
 	 *
      * @param responder The to this compute request corresponding Responder
      * @param algName The algorithm name
      * @param points RequestPoints
      * @param constraints map with constraints
      * @param acceptsSmile Flag if client accepts Smile
      */
 	public ComputeRequest(Responder responder, String algName,
                           de.tourenplaner.computecore.RequestPoints points, Map<String, Object> constraints, boolean acceptsSmile) {
 		this.algName = algName;
 		this.points = points;
 		this.resultWays = new ArrayList<de.tourenplaner.computecore.Way> (1);
 		this.constraints = constraints;
 		this.responder = responder;
 		this.misc = null;
 		this.requestID = -1;
         this.acceptsSmile = acceptsSmile;
 	}
 
 	/**
 	 * Gets the responder object which is used to send the result to the correct
 	 * client connection
 	 * 
 	 * @return Returns the Responder object
 	 */
 	public Responder getResponder() {
 		return responder;
 	}
 
 	/**
 	 * Gets the URLSuffix for the requested algorithm e.g. "sp" for a shortest
 	 * path algorithm
 	 * 
 	 * @return Returns the URLSuffix
 	 */
 	public String getAlgorithmURLSuffix() {
 		return algName;
 	}
 
 	/**
 	 * Returns the Points associated with this request
 	 * 
 	 * @return RequestPoints
 	 */
 	public RequestPoints getPoints() {
 		return points;
 	}
 
 	/**
 	 * Returns the list of Ways making up the result of the computation <br />
      * It's an Algorithms job to ensure that after the computation the list contains all
      * the ways connecting the Points
 	 * 
 	 * @return A List with the result ways
 	 */
 	public List<Way> getResultWays() {
 		return resultWays;
 	}
 
 	/**
 	 * Returns the constraints associated with this request
 	 * 
 	 * @return A Map representing the constraints
 	 */
 	public Map<String, Object> getConstraints() {
 		return constraints;
 	}
 
 	/**
 	 * Returns the misc field used to store results, initially this is null
 	 * 
 	 * @return A Map representing the misc field
 	 */
 	public Map<String, Object> getMisc() {
 		return misc;
 	}
 
     /**
      * Sets the misc field used to store results, initially this is null
      *
      * @param misc A Map representing the misc field
      */
 	public void setMisc(Map<String, Object> misc) {
 		this.misc = misc;
 	}
 
 	/**
 	 * Sets the requestID. The requestID must be -1 if server is not in private
 	 * mode. If the requestID is not explicitly set, it is -1. <br /> This
 	 * attribute should cointain the requestID of the corresponding
 	 * RequestDataset within the database. Must be set after construction of the
 	 * ComputeRequest object if server is in private mode.
 	 *
      * @param requestID The requestID to set
      */
 	public void setRequestID(int requestID) {
 		this.requestID = requestID;
 	}
 
 	/**
 	 * Gets the requestID, should be -1 if server is not in private mode. This
 	 * attribute should contain the requestID of the corresponding
 	 * RequestDataset within the database.
 	 * 
 	 * @return Returns the requestID
 	 */
 	public int getRequestID() {
 		return this.requestID;
 	}
 
     /**
      * Returns if request was constructed in private or public mode
      * @return true if request was constructed in private mode
      */
     public boolean isPrivate() {
         return requestID != -1;
     }
 
     /**
      * Returns if request comes from a client accepting "application/x-jackson-smile"
      * @return Returns if client is accepting smile
      */
     public boolean isAcceptsSmile() {
         return acceptsSmile;
     }
 
 	/**
 	 * Writes a json representation of the result of this request to the given
 	 * stream
 	 * 
 	 * @param mapper Jackson ObjectMapper
 	 * @param stream OutputStream
      * @param writePath Determines if the result path should be written into the stream or
      *                  if no path but the start and end points of the sub paths should be written into the stream
 	 * @throws JsonGenerationException Thrown if generating json fails
      * @throws JsonProcessingException Thrown if json generation processing fails
      * @throws IOException Thrown if writing json onto the stream fails
 	 */
 	public void writeToStream(ObjectMapper mapper, OutputStream stream, boolean writePath) throws IOException {
 
 		JsonGenerator gen = mapper.getJsonFactory().createJsonGenerator(stream);
 		Map<String, Object> pconsts;
 
 		gen.setCodec(mapper);
 		gen.writeStartObject();
         if (isPrivate()) {
             gen.writeNumberField("requestid", this.requestID);
         }
         
         gen.writeObjectField("constraints", this.constraints);
 
 		gen.writeArrayFieldStart("points");
 		RequestPoints points = this.points;
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
         if (writePath) {
             for(Way way : this.resultWays){
                 gen.writeStartArray();
                 for (int i = 0; i < way.size(); i++) {
                     gen.writeStartObject();
                     gen.writeNumberField("lt", way.getPointLat(i));
                     gen.writeNumberField("ln", way.getPointLon(i));
                     gen.writeEndObject();
                 }
                 gen.writeEndArray();
             }
         } else {
            // do not use  this.points to determine airlines between the points
            // some algorithms would maybe not compute paths between the points in this.points
            // example: search a bicycle tour within a certain radius of a point
            for(Way way : this.resultWays){
                 gen.writeStartArray();

                gen.writeStartObject();
                gen.writeNumberField("lt", way.getPointLat(0));
                gen.writeNumberField("ln", way.getPointLon(0));
                gen.writeEndObject();

                 gen.writeStartObject();
                gen.writeNumberField("lt", way.getPointLat(way.size() - 1));
                gen.writeNumberField("ln", way.getPointLon(way.size() - 1));
                 gen.writeEndObject();

                 gen.writeEndArray();
             }
         }
 		gen.writeEndArray();
 		gen.writeObjectField("misc", this.misc);
 		gen.writeEndObject();
 		gen.close();
 	}
 }
