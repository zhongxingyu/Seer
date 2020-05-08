 package com.axiomalaska.sos.xmlbuilder;
 
 import java.util.Calendar;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import com.axiomalaska.phenomena.Phenomenon;
 import com.axiomalaska.sos.data.SosSensor;
 import com.axiomalaska.sos.data.SosStation;
 import com.axiomalaska.sos.data.ObservationCollection;
 import com.axiomalaska.sos.tools.IdCreator;
 
 /**
  * Builds a SOS InsertObservation XML String with the ObservationCollection and station
  * 
  * @author Lance Finfrock
  */
 public class InsertObservationBuilder extends SosXmlBuilder {
 
 	// -------------------------------------------------------------------------
 	// Private Data
 	// -------------------------------------------------------------------------
 	
 	private SosStation station;
 	private SosSensor sensor;
 	private ObservationCollection observationCollection;
 	private IdCreator idCreator;
 	private Phenomenon phenomenon;
 	private Double depth;
 	
 	// -------------------------------------------------------------------------
 	// Constructor
 	// -------------------------------------------------------------------------
 	
 	public InsertObservationBuilder(SosStation station, SosSensor sensor, 
 			Phenomenon phenomenon, ObservationCollection observationCollection, 
 			IdCreator idCreator, Double depth){
 		this.station = station;
 		this.sensor = sensor;
 		this.phenomenon = phenomenon;
 		this.observationCollection = observationCollection;
 		this.idCreator = idCreator;
 		this.depth = depth;
 	}
 	
 	// -------------------------------------------------------------------------
 	// Override Members
 	// -------------------------------------------------------------------------
 	
 	/**
 	 * Create InsertObservation XML
 	 * example:
 	 * <InsertObservation service="SOS" version="1.0.0" xsi:schemaLocation="http://www.opengis.net/sos/1.0 http://schemas.opengis.net/sos/1.0.0/sosInsert.xsd http://www.opengis.net/sampling/1.0 http://schemas.opengis.net/sampling/1.0.0/sampling.xsd http://www.opengis.net/om/1.0 http://schemas.opengis.net/om/1.0.0/extensions/observationSpecialization_override.xsd">
 	 * 	<AssignedSensorId>urn:ioos:sensor:aoos:pilotrock:airtemp</AssignedSensorId>
 	 * 	<om:Observation>
 	 * 		<om:samplingTime>
 	 * 			<gml:TimePeriod xsi:type="gml:TimePeriodType">
 	 * 				<gml:beginPosition>2012-04-17T12:02:05-0000</gml:beginPosition>
 	 * 				<gml:endPosition>2012-04-17T20:02:05-0000</gml:endPosition>
 	 * 			</gml:TimePeriod>
 	 * 		</om:samplingTime>
 	 * 		<om:procedure xlink:href="urn:ogc:object:feature:Sensor:3234"/>
 	 *      <om:observedProperty>
    	 *			<swe:CompositePhenomenon gml:id="cpid0" dimension="1">
      *				<gml:name>resultComponents</gml:name>
      *				<swe:component xlink:href="http://www.opengis.net/def/uom/ISO-8601/0/Gregorian" />
      *				<swe:component xlink:href="urn:x-ogc:def:phenomenon:IOOS:0.0.1:air_temperature" />
      * 			</swe:CompositePhenomenon>
   	 *		</om:observedProperty>
 	 * 		<om:featureOfInterest>
 	 * 			<gml:FeatureCollection>
 	 * 				<gml:featureMember>
 	 * 					<sa:SamplingPoint gml:id="foi_3234">
 	 * 						<gml:name>The sampling point at station: Anchorage Hillside - AOOS</gml:name>
 	 * 						<sa:sampledFeature xlink:href=""/>
 	 * 						<sa:position>
 	 * 							<gml:Point>
 	 * 								<gml:pos srsName="urn:ogc:def:crs:EPSG::4326">-143.0 63.0</gml:pos>
 	 * 							</gml:Point>
 	 * 						</sa:position>
 	 * 					</sa:SamplingPoint>
 	 * 				</gml:featureMember>
 	 * 			</gml:FeatureCollection>
 	 * 		</om:featureOfInterest>
 	 * 		<om:result>
 	 * 			<swe:DataArray>
 	 * 				<swe:elementCount>
 	 * 					<swe:Count>
 	 * 						<swe:value>3</swe:value>
 	 * 					</swe:Count>
 	 * 				</swe:elementCount>
 	 * 				<swe:elementType name="Components">
 	 * 					<swe:DataRecord>
 	 * 						<swe:field name="feature">
 	 * 							<swe:Text definition="http://www.opengis.net/def/property/OGC/0/FeatureOfInterest"/>
 	 * 						</swe:field>
 	 * 						<swe:field name="Time">
 	 * 							<swe:Time definition="http://www.opengis.net/def/uom/ISO-8601/0/Gregorian"/>
 	 * 						</swe:field>
 	 * 						<swe:field name="Air Temperature">
 	 * 							<swe:Quantity definition="urn:x-ogc:def:phenomenon:IOOS:0.0.1:air_temperature">
 	 * 								<swe:uom code="C"/>
 	 * 							</swe:Quantity>
 	 * 						</swe:field>
 	 * 					</swe:DataRecord>
 	 * 				</swe:elementType>
 	 * 				<swe:encoding>
 	 * 					<swe:TextBlock blockSeparator=";" decimalSeparator="." tokenSeparator=","/>
 	 * 				</swe:encoding>
 	 * 				<swe:values>
 	 * 					foi_3234,2012-04-17T20:02:05-0000,10.0;foi_3234,2012-04-17T16:02:05-0000,11.0;foi_3234,2012-04-17T12:02:05-0000,12.0;
 	 * 				</swe:values>
 	 * 			</swe:DataArray>
 	 * 		</om:result>
 	 * 	</om:Observation>
 	 * </InsertObservation>
 	 */
     @Override
 	public String build() {
 		try{
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 		
 		Document doc = docBuilder.newDocument();
 		Element insertObservation = doc.createElement("InsertObservation");
 		insertObservation.setAttribute("xmlns", "http://www.opengis.net/sos/1.0");
 		insertObservation.setAttribute("xmlns:ows", "http://www.opengis.net/ows/1.1");
 		insertObservation.setAttribute("xmlns:ogc", "http://www.opengis.net/ogc");
 		insertObservation.setAttribute("xmlns:om", "http://www.opengis.net/om/1.0");
 		insertObservation.setAttribute("xmlns:sos", "http://www.opengis.net/sos/1.0");
 		insertObservation.setAttribute("xmlns:sa", "http://www.opengis.net/sampling/1.0");
 		insertObservation.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
 		insertObservation.setAttribute("xmlns:swe", "http://www.opengis.net/swe/1.0.1");
 		insertObservation.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
 		insertObservation.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
 		insertObservation.setAttribute("xsi:schemaLocation", "http://www.opengis.net/sos/1.0 http://schemas.opengis.net/sos/1.0.0/sosInsert.xsd http://www.opengis.net/sampling/1.0 http://schemas.opengis.net/sampling/1.0.0/sampling.xsd http://www.opengis.net/om/1.0 http://schemas.opengis.net/om/1.0.0/extensions/observationSpecialization_override.xsd");
 		insertObservation.setAttribute("service", "SOS");
 		insertObservation.setAttribute("version", "1.0.0");
 		doc.appendChild(insertObservation);
 		
 		String procedureId = idCreator.createSensorId(station, sensor);
 		
 		Element assignedSensorId = doc.createElement("AssignedSensorId");
 		assignedSensorId.appendChild(doc.createTextNode(procedureId));
 		insertObservation.appendChild(assignedSensorId);
 		
 		Element observation = doc.createElement("om:Observation");
 		observation.appendChild(getSamplingTime(doc));
 		insertObservation.appendChild(observation);
 		
 		Element procedure = doc.createElement("om:procedure");
 		procedure.setAttribute("xlink:href", procedureId);
 		observation.appendChild(procedure);
 		
 		observation.appendChild(createObservedProperty(doc));
 		
 		observation.appendChild(createFeatureOfInterest(doc));
 		
 		observation.appendChild(createResult(doc, station));
 		
 		String xmlString = getString(doc);
                 
                System.out.println(xmlString);
                
         return xmlString;
 	  } catch (Exception ex) {
 		System.err.println(ex.getMessage());
 	  }
 		return null;
 	}
 	
 	// -------------------------------------------------------------------------
 	// Private Members
 	// -------------------------------------------------------------------------
 	
 	/**
 	 *      <om:observedProperty>
    	 *			<swe:CompositePhenomenon gml:id="cpid0" dimension="1">
      *				<gml:name>resultComponents</gml:name>
      *				<swe:component xlink:href="http://www.opengis.net/def/uom/ISO-8601/0/Gregorian" />
      *				<swe:component xlink:href="urn:x-ogc:def:phenomenon:IOOS:0.0.1:air_temperature" />
      * 			</swe:CompositePhenomenon>
   	 *		</om:observedProperty>
   	 */
 	private Node createObservedProperty(Document doc){
 		Element observedProperty = doc.createElement("om:observedProperty");
 		
 		Element compositePhenomenon = doc.createElement("swe:CompositePhenomenon");
 		compositePhenomenon.setAttribute("gml:id", "cpid0");
 		compositePhenomenon.setAttribute("dimension", "1");
 		observedProperty.appendChild(compositePhenomenon);
 		
 		Element name = doc.createElement("gml:name");
 		name.appendChild(doc.createTextNode("resultComponents"));
 		compositePhenomenon.appendChild(name);
 		
 		Element timeComponent = doc.createElement("swe:component");
 		timeComponent.setAttribute("xlink:href", "http://www.opengis.net/def/uom/ISO-8601/0/Gregorian");
 		compositePhenomenon.appendChild(timeComponent);
 		
 		Element dataComponent = doc.createElement("swe:component");
 
 	    dataComponent.setAttribute("xlink:href", phenomenon.getId());
 		
 		compositePhenomenon.appendChild(dataComponent);
 		
 		return observedProperty;
 	}
 	
 	/**
 	 * Create the Result node
 	 * example:
 	 * <om:result>
 	 * 	<swe:DataArray>
 	 * 		<swe:elementCount>
 	 * 			<swe:Count>
 	 * 				<swe:value>3</swe:value>
 	 * 			</swe:Count>
 	 * 		</swe:elementCount>
 	 * 		<swe:elementType name="Components">
 	 * 			<swe:DataRecord>
 	 * 				<swe:field name="feature">
 	 * 					<swe:Text definition="http://www.opengis.net/def/property/OGC/0/FeatureOfInterest"/>
 	 * 				</swe:field>
 	 * 				<swe:field name="Time">
 	 * 					<swe:Time definition="urn:ogc:data:time:iso8601"/>
 	 * 				</swe:field>
 	 * 				<swe:field name="Air Temperature">
 	 * 					<swe:Quantity definition="urn:x-ogc:def:phenomenon:IOOS:0.0.1:air_temperature">
 	 * 						<swe:uom code="C"/>
 	 * 					</swe:Quantity>
 	 * 				</swe:field>
 	 * 			</swe:DataRecord>
 	 * 		</swe:elementType>
 	 * 		<swe:encoding>
 	 * 			<swe:TextBlock blockSeparator=";" decimalSeparator="." tokenSeparator=","/>
 	 * 		</swe:encoding>
 	 * 		<swe:values>
 	 * 			foi_3234,2012-04-17T20:02:05-0000,10.0;foi_3234,2012-04-17T16:02:05-0000,11.0;foi_3234,2012-04-17T12:02:05-0000,12.0;
 	 * 		</swe:values>
 	 * 	</swe:DataArray>
 	 * </om:result>
 	 */
 	private Node createResult(Document doc, SosStation station) {
 		Element result = doc.createElement("om:result");
 		
 		Element dataArray = doc.createElement("swe:DataArray");
 		result.appendChild(dataArray);
 		
 		Element elementCount = doc.createElement("swe:elementCount");
 		dataArray.appendChild(elementCount);
 		
 		Element count = doc.createElement("swe:Count");
 		elementCount.appendChild(count);
 		
 		Element value = doc.createElement("swe:value");
 		value.appendChild(doc.createTextNode(getNumberOfValues() + ""));
 		count.appendChild(value);
 		
 		dataArray.appendChild(buildElementTypeComponents(doc));
 		
 		Element encoding = doc.createElement("swe:encoding");
 		dataArray.appendChild(encoding);
 		
 		Element textBlock = doc.createElement("swe:TextBlock");
 		textBlock.setAttribute("decimalSeparator", ".");
 		textBlock.setAttribute("tokenSeparator", ",");
 		textBlock.setAttribute("blockSeparator", ";");
 		encoding.appendChild(textBlock);
 		
 		Element values = doc.createElement("swe:values");
 		values.appendChild(buildValues(doc));
 		
 		dataArray.appendChild(values);
 		
 		return result;
 	}
 
 	/**
 	 * Get the number of values to be entered. 
 	 */
 	private int getNumberOfValues() {
 		return observationCollection.getObservationDates().size();
 	}
 	
 	/**
 	 * Builds the values list. Each set of values is separated by a semicolon. 
 	 * Each value in the set of values is separated by a comma. 
 	 * example:
 	 * foi_3234,2012-04-17T20:02:05-0000,10.0;foi_3234,2012-04-17T16:02:05-0000,11.0;foi_3234,2012-04-17T12:02:05-0000,12.0;
 	 */
 	private Node buildValues(Document doc) {
 		
 		List<Calendar> dates = observationCollection.getObservationDates();
 		List<Double> values = observationCollection.getObservationValues();
 		
 		String text = "";
 		int size = getNumberOfValues();
 		
 		String featureOfInterestId = 
 				idCreator.createObservationFeatureOfInterestId(
 						station, sensor, depth);
 		
 		for(int index = 0; index < size; index++){
 			Calendar date = dates.get(index);
 			Double value = values.get(index);
 			
 			text += featureOfInterestId + "," + 
 					formatCalendarIntoGMTTime(date) + "," + value + ";"; 
 		}
 		
 		return doc.createTextNode(text);
 	}
 
 	/**
 	 * Build the Element Type Components node
 	 * 
 	 * <swe:elementType name="Components">
 	 * 	<swe:DataRecord>
 	 * 		<swe:field name="feature">
 	 * 			<swe:Text definition="http://www.opengis.net/def/property/OGC/0/FeatureOfInterest"/>
 	 * 		</swe:field>
 	 * 		<swe:field name="Time">
 	 * 			<swe:Time definition="urn:ogc:data:time:iso8601"/>
 	 * 		</swe:field>
 	 * 		<swe:field name="Air Temperature">
 	 * 			<swe:Quantity definition="urn:x-ogc:def:phenomenon:IOOS:0.0.1:air_temperature">
 	 * 			<swe:uom code="C"/></swe:Quantity>
 	 * 		</swe:field>
 	 * 	</swe:DataRecord>
 	 * </swe:elementType>
 	 */
 	private Node buildElementTypeComponents(Document doc) {
 		Element elementType = doc.createElement("swe:elementType");
 		elementType.setAttribute("name", "Components");
 		
 		Element dataRecord = doc.createElement("swe:DataRecord");
 		elementType.appendChild(dataRecord);
 		
 		Element fieldFeature = doc.createElement("swe:field");
 		fieldFeature.setAttribute("name", "feature");
 		dataRecord.appendChild(fieldFeature);
 		
 		Element text = doc.createElement("swe:Text");
 		text.setAttribute("definition", "http://www.opengis.net/def/property/OGC/0/FeatureOfInterest");
 		fieldFeature.appendChild(text);
 		
 		Element fieldTime = doc.createElement("swe:field");
 		fieldTime.setAttribute("name", "Time");
 		dataRecord.appendChild(fieldTime);
 		
 		Element time = doc.createElement("swe:Time");
 		time.setAttribute("definition", "http://www.opengis.net/def/uom/ISO-8601/0/Gregorian");
 		fieldTime.appendChild(time);
 		
 		Element field = doc.createElement("swe:field");
 		field.setAttribute("name", phenomenon.getName());
 		dataRecord.appendChild(field);
 
 		Element quantity = doc.createElement("swe:Quantity");
 		quantity.setAttribute("definition", phenomenon.getId());
 		field.appendChild(quantity);
 
 		String unitString = "";
 		if(phenomenon.getUnit() != null){
                     // have to remove whitespace from the unit symbol -- Sean Cowan
 			unitString = phenomenon.getUnit().toString().replaceAll("\\s+", "");
 		}
 		
 		Element uom = doc.createElement("swe:uom");
 		uom.setAttribute("code", unitString);
 		quantity.appendChild(uom);
 		
 		return elementType;
 	}
 	
 	/**
 	 * Create the Feature of Interest Node
 	 * example
 	   <om:featureOfInterest>
 	     <sa:SamplingPoint gml:id="urn:ioos:sensor:aoos:pilotrock:seawatertemp-10m">
 	       <gml:description>Pilot Rock Station, AK Seawater Temperature Sensor (-10 meters)</gml:description>
 	       <gml:name>Pilot Rock, AK Seawater Temp (-10 meters)</gml:name>
 	       <sa:sampledFeature/>
 	       <sa:position>
 	         <gml:Point>
 	           <gml:pos srsName="http://www.opengis.net/def/crs/EPSG/0/4979">59.742 -149.470 -10</gml:pos>
 	         </gml:Point>
 	       </sa:position>
 	     </sa:SamplingPoint>
 	   </om:featureOfInterest>
 	 * 
 	 * @param station - station to get information from
 	 */
 	private Node createFeatureOfInterest(Document doc) {
 		Element featureOfInterest = doc.createElement("om:featureOfInterest");
 		
 		Element samplingPoint = doc.createElement("sa:SamplingPoint");
 
 		String featureOfInterestId = 
 				idCreator.createObservationFeatureOfInterestId(station, sensor, depth);
 
 		samplingPoint.setAttribute("gml:id", featureOfInterestId);
 		featureOfInterest.appendChild(samplingPoint);
 		
 		String featureOfInterestDescription = 
 				idCreator.createObservationFeatureOfInterestName(station, sensor, depth);
 		
 		Element description = doc.createElement("gml:description");
 		description.appendChild(doc.createTextNode(featureOfInterestDescription));
 		samplingPoint.appendChild(description);
 		
 		Element gmlName = doc.createElement("gml:name");
 		gmlName.appendChild(doc.createTextNode(featureOfInterestDescription));
 		samplingPoint.appendChild(gmlName);
 		
 		Element sampledFeature = doc.createElement("sa:sampledFeature");
 		samplingPoint.appendChild(sampledFeature);
 		
 		Element position = doc.createElement("sa:position");
 		samplingPoint.appendChild(position);
 		
 		Element point = doc.createElement("gml:Point");
 		position.appendChild(point);
 		
 		Element pos = doc.createElement("gml:pos");
 		pos.setAttribute("srsName", "http://www.opengis.net/def/crs/EPSG/0/4979");
 		
 		String locationText = "";
 		if (depth != null && depth != 0.0) {
 			locationText = station.getLocation().getLatitude() + " "
 					+ station.getLocation().getLongitude() + " " + depth;
 		} else {
 			locationText = station.getLocation().getLatitude() + " "
 					+ station.getLocation().getLongitude() + " 0.0";
 		}
 		
 		pos.appendChild(doc.createTextNode(locationText));
 		
 		point.appendChild(pos);
 		
 		return featureOfInterest;
 	}
 
 	/**
 	 * Create the samplingTime section
 	 * example:
 	 * <om:samplingTime>
 	 * 		<gml:TimePeriod xsi:type="gml:TimePeriodType">
 	 * 			<gml:beginPosition>2012-04-17T12:02:05-0000</gml:beginPosition>
 	 * 			<gml:endPosition>2012-04-17T20:02:05-0000</gml:endPosition>
 	 * 		</gml:TimePeriod>
 	 * </om:samplingTime>
 	 */
 	private Node getSamplingTime(Document doc){
 		List<Calendar> dates = observationCollection.getObservationDates();
 		
 		Calendar firstDate = dates.get(0);
 		Calendar lastDate = dates.get(dates.size() - 1);
 		
 		Calendar startDate = null;
 		if (firstDate.after(lastDate)){
 			startDate = lastDate;
 		}
 		else{
 			startDate = firstDate;
 		}
 		
 		Calendar endDate = null;
 		if (firstDate.after(lastDate)) {
 			endDate = firstDate;
 		}
 		else {
 			endDate = lastDate;
 		}
 		
 		Element samplingTime = doc.createElement("om:samplingTime");
 		
 		Element timePeriod = doc.createElement("gml:TimePeriod");
 		timePeriod.setAttribute("xsi:type", "gml:TimePeriodType");
 		samplingTime.appendChild(timePeriod);
 		
 		Element beginPosition = doc.createElement("gml:beginPosition");
 		beginPosition.appendChild(doc.createTextNode(formatCalendarIntoGMTTime(startDate)));
 		timePeriod.appendChild(beginPosition);
 		
 		Element endPosition = doc.createElement("gml:endPosition");
 		endPosition.appendChild(doc.createTextNode(formatCalendarIntoGMTTime(endDate)));
 		timePeriod.appendChild(endPosition);
 		
 		return samplingTime;
 	}
 }
