 package org.uncertweb.et.json;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.lang.reflect.Type;
 import java.util.Map.Entry;
 
 import org.uncertweb.et.design.Design;
 import org.uncertweb.et.emulator.EmulatorEvaluationResult;
 import org.uncertweb.et.parameter.Input;
 import org.uncertweb.et.parameter.Output;
 import org.uncertweb.et.parameter.ParameterDescription;
 import org.uncertweb.et.process.ProcessEvaluationResult;
 import org.uncertweb.et.request.Request;
 import org.uncertweb.et.response.Response;
 import org.uncertweb.et.sensitivity.AnalysisInputResult;
 import org.uncertweb.et.validation.Validator;
 import org.uncertweb.et.value.DistributionValues;
 import org.uncertweb.et.value.SampleValues;
 import org.uncertweb.et.value.ScalarValues;
 import org.uncertweb.et.value.Values;
 import org.uncertweb.imagestorage.Base64ImageStorage;
 import org.uncertweb.imagestorage.ImageStorage;
 import org.uncertweb.imagestorage.ImageStorageException;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonPrimitive;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 
 /**
  * By default, images will be encoded using {@link Base64ImageStorage}.
  * 
  * @author Richard Jones
  *
  */
 public class JSON {
 
 	private GsonBuilder gsonBuilder;
 	private Gson gson;
 	private ImageStorage is;
 
 	public JSON() {
 		this(new Base64ImageStorage());
 	}
 	
 	public JSON(ImageStorage is) {
 		// setup gson builder
 		gsonBuilder = new GsonBuilder();
 		registerSerializers(gsonBuilder);
 		registerAlizers(gsonBuilder);
 		registerInstanceCreators(gsonBuilder);
 		
 		// set image storage
 		this.is = is;
 
 		// create gson
 		gson = gsonBuilder.create();
 	}
 
 	public <T> T parse(Reader reader, Class<T> classOfT) {
 		// FIXME: this should throw parse exceptions
 		return gson.fromJson(reader, classOfT);
 	}
 	
 	public String encode(Object obj) {
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		OutputStreamWriter osw = new OutputStreamWriter(os);
 		encode(obj, osw);
 		try {
 			osw.close();
 		}
 		catch (IOException e) {	}
 		return os.toString(); // no need to close byte array output stream
 	}
 
 	public void encode(Object obj, Appendable appendable) {
 		if (obj instanceof Exception) {
 			Exception src = (Exception) obj;
 			JsonObject object = new JsonObject();
 			object.add("type", new JsonPrimitive("Exception"));
 			object.add("source", new JsonPrimitive(src.getClass().getSimpleName()));
 			if (src.getMessage() != null) {
 				object.add("message", new JsonPrimitive(src.getMessage()));
 			}
 			gson.toJson(object, appendable);
 		}
 		else if (obj instanceof Response) {
 			JsonObject object = new JsonObject();
 			object.addProperty("type", obj.getClass().getSimpleName());
 			for (Entry<String, JsonElement> property : gson.toJsonTree(obj).getAsJsonObject().entrySet()) {
 				object.add(property.getKey(), property.getValue());
 			}
 			gson.toJson(object, appendable);
 		}
 		else {
 			gson.toJson(obj, appendable);
 		}
 	}
 
 	private void registerSerializers(GsonBuilder gsonBuilder) {
 		gsonBuilder.registerTypeAdapter(BufferedImage.class, new JsonSerializer<BufferedImage>() {
 			public JsonElement serialize(BufferedImage src, Type typeOfSrc, JsonSerializationContext context) {
 				try {
 					String stored = is.storeImage(src);
 					return new JsonPrimitive(stored);
 				}
 				catch (ImageStorageException e) {
 					// FIXME: need exception
 				}
 				return null;
 			}			
 		});
 	}
 
 	private void registerAlizers(GsonBuilder gsonBuilder) {
 		gsonBuilder.registerTypeAdapter(JsonElement.class, new JsonDeserializer<JsonElement>() {
 			public JsonElement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
 				return json;
 			}
 		});
 		gsonBuilder.registerTypeAdapter(Input.class, new InputSerializer());
 		gsonBuilder.registerTypeAdapter(Input.class, new InputDeserializer());
 		gsonBuilder.registerTypeAdapter(Request.class, new RequestDeserializer());
 		gsonBuilder.registerTypeAdapter(Design.class, new DesignSerializer());
 		gsonBuilder.registerTypeAdapter(Design.class, new DesignDeserializer());		
 		gsonBuilder.registerTypeAdapter(ProcessEvaluationResult.class, new ProcessEvaluationResultSerializer());
 		gsonBuilder.registerTypeAdapter(ProcessEvaluationResult.class, new ProcessEvaluationResultDeserializer());		
 		gsonBuilder.registerTypeAdapter(EmulatorEvaluationResult.class, new EmulatorEvaluationResultSerializer());
 		gsonBuilder.registerTypeAdapter(EmulatorEvaluationResult.class, new EmulatorEvaluationResultDeserializer());	
 		gsonBuilder.registerTypeAdapter(AnalysisInputResult.class, new AnalysisInputResultSerializer());
 		
 		// for respondables
 		gsonBuilder.registerTypeAdapter(Validator.class, new RespondableSerializer());
 		
 		// for values
 		gsonBuilder.registerTypeAdapter(Values.class, new ValuesDeserializer());
 		gsonBuilder.registerTypeAdapter(SampleValues.class, new SampleValuesDeserializer());
 		gsonBuilder.registerTypeAdapter(ScalarValues.class, new ScalarValuesDeserializer());
 		gsonBuilder.registerTypeAdapter(DistributionValues.class, new DistributionValuesDeserializer());
 		gsonBuilder.registerTypeAdapter(SampleValues.class, new SampleValuesSerializer());
 		gsonBuilder.registerTypeAdapter(ScalarValues.class, new ScalarValuesSerializer());
 		gsonBuilder.registerTypeAdapter(DistributionValues.class, new DistributionValuesSerializer());
 	}
 
 	private void registerInstanceCreators(GsonBuilder gsonBuilder) {
 		gsonBuilder.registerTypeAdapter(Output.class, new OutputInstanceCreator());
 		gsonBuilder.registerTypeAdapter(ParameterDescription.class, new ParameterDescriptionInstanceCreator());
 	}
 
 }
