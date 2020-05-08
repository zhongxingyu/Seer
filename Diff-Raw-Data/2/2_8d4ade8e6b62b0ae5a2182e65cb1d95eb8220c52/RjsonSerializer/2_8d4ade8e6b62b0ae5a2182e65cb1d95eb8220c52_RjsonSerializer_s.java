 package testrj.serializer;
 
 import rjson.Rjson;
 import testrj.domain.SerializedData;
 
 @SuppressWarnings("rawtypes")
 public class RjsonSerializer implements Serializer {
 	public SerializedData serialize(Object inputObject) {
 		return new SerializedData(serializer.toJson(inputObject));
 	}
 
 	public Object deSerialize(SerializedData serializedInput) {
 		 return serializer.toObject(serializedInput.toString());
 	}
 
 	private static Rjson serializer;
 	static {
		serializer = Rjson.newInstance().andIgnoreModifiers();
 	}
 }
