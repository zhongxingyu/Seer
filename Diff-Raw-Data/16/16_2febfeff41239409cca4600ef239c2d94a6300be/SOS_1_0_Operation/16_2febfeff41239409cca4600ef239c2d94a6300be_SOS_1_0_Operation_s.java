 package gov.usgs.cida.ogc.specs;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Based on OGC Sensor Observation Service 1.0 standard (document 06-009r6) 
  * at  http://www.opengeospatial.org/standards/sos
  * @author ilinkuo
  *
  */
 public enum SOS_1_0_Operation {
 	GetCapabilities("core", true, new String[] {"service", "request"}, new String[] {"Sections","updateSequence","AcceptVersions","AcceptFormats","AcceptLanguages"}),
         DescribeSensor("core", true, new String[] {"service", "version", "request", "SensorId", "outputFormat"}, null),
         MasterFeatureList("core", true, null, null),
         wml2_Example("core", true, null, null),
 	GetObservation("core", true, null, null),
	GetDataAvailablity("core", true, null, null),
         GetHistoricalData("core", true, null, null),
         RegisterSensor("trans", false, null, null),
 	InsertObservation("opt", false, null, null),
 	GetResult("opt", false, null, null),
 	GetFeatureOfInterest("opt", false, null, null),
 	GetFeatureOfInterestTime("opt", false, null, null),
 	GetObservationById("opt", false, null, null),
 	DescribeFeatureOfInterest("opt", false, null, null),
 	DescribeObservationType("opt", false, null, null),
 	DescribeResultModel("opt", false, null, null),
 	GetProfile("ext", false, null, null)
 	;
 	
 	public final String profile;
 	public final boolean isMandatory;
 	public final List<String> opArguments;
 	
 	SOS_1_0_Operation(String profile, boolean isRequired, String[] args, String[] optArgs){
 		this.profile = profile;
 		this.isMandatory = isRequired;
 		if (args != null) {
 			List<String> list = new ArrayList<String>();
 			list.addAll(Arrays.asList(args));
 			this.opArguments = Collections.unmodifiableList(list);
 		} else {
 			this.opArguments = Collections.emptyList();
 		}
 	}
 	
 	public static SOS_1_0_Operation parse(String... value) {
 		String operationString = (value == null)? null: value[0];
 		if (operationString != null) {
 			for (SOS_1_0_Operation op: SOS_1_0_Operation.values()) {
 				if (op.name().equalsIgnoreCase(operationString)) return op;
 			}
 		}
 		return null;
 	}
 }
