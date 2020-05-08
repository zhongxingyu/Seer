 package rjson.domain;
 
import rjson.Rjson;
import rjson.printer.Printer;
import rjson.transformer.ObjectToJsonTransformer;
import rjson.transformer.ToJsonTransformationUtils;
 
public class IgnoreDateTransformer implements ObjectToJsonTransformer {
 	public boolean canConvertToJson(Object object) {
		if (object instanceof java.util.Date)
 			return true;
 		return false;
 	}
	public void transformToJson(Object object, Printer printer, Rjson rjson) {
		ToJsonTransformationUtils.printData(null, printer);
 	}
 }
