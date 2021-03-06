 package edu.sc.seis.sod.subsetter.waveFormArm;
 
 import edu.sc.seis.sod.*;
 import edu.sc.seis.TauP.*;
 
 import edu.iris.Fissures.IfEvent.*;
 import edu.iris.Fissures.event.*;
 import edu.iris.Fissures.IfNetwork.*;
 import edu.iris.Fissures.network.*;
 import edu.iris.Fissures.Location;
 import edu.iris.Fissures.model.*;
 import edu.iris.Fissures.IfSeismogramDC.*;
 
 import java.util.*;
 
 import org.w3c.dom.*;
 
 /** 
  * sample xml file
  *<pre>
  *	&lt;phaseRequest&gt;
  *		&lt;beginPhase&gt;ttp&lt;/beginPhase&gt;
  *       	&lt;beginOffset&gt;
  *			&lt;unit&gt;SECOND&lt;/unit&gt;
  *			&lt;value&gt;-120&lt;/value&gt;
  *		&lt;/beginOffset&gt;
  *		&lt;endPhase&gt;tts&lt;/endPhase&gt;
  *		&lt;endOffset&gt;
  *			&lt;unit&gt;SECOND&lt;/unit&gt;
  *			&lt;value&gt;600&lt;/value&gt;
  *		&lt;/endOffset&gt;
  *	&lt;/phaseRequest&gt;
  *</pre>
  */
 
 
 
 public class PhaseRequest implements RequestGenerator{
     /**
      * Creates a new <code>PhaseRequest</code> instance.
      *
      * @param config an <code>Element</code> value
      */
     public PhaseRequest (Element config) throws ConfigurationException{
 
 	NodeList childNodes = config.getChildNodes();
 	Node node;
 	for(int counter = 0; counter < childNodes.getLength(); counter++) {
 	    node = childNodes.item(counter);
 	    if(node instanceof Element) {
 		Element element = (Element)node;
 		if(element.getTagName().equals("beginPhase")) {
 		    beginPhase = SodUtil.getNestedText(element);
 		} else if(element.getTagName().equals("beginOffset")) {
 		    SodElement sodElement = 
 			(SodElement) SodUtil.load(element,
 						   waveformArmPackage);
 		    beginOffset = (BeginOffset)sodElement;
 		} else if(element.getTagName().equals("endPhase")) {
 		    endPhase = SodUtil.getNestedText(element);
 		} else if(element.getTagName().equals("endOffset")) {
 		    SodElement sodElement = 
 			(SodElement) SodUtil.load(element,
 						  waveformArmPackage);
 		    endOffset = (EndOffset)sodElement;
 		}
 	    }
 	}
     }
     
     /**
      * Describe <code>generateRequest</code> method here.
      *
      * @param event an <code>EventAccessOperations</code> value
      * @param network a <code>NetworkAccess</code> value
      * @param channel a <code>Channel</code> value
      * @param cookies a <code>CookieJar</code> value
      * @return a <code>RequestFilter[]</code> value
      */
     public RequestFilter[] generateRequest(EventAccessOperations event, 
 			  NetworkAccess network, 
 			  Channel channel, 
 			  CookieJar cookies) throws Exception{
 	Origin origin = null;
 	double arrivalStartTime = -100.0;
 	double arrivalEndTime = -100.0;
 	origin = event.get_preferred_origin();
 	Properties props = Start.getProperties();
 	String tauPModel = new String();
 	try {
 	    tauPModel = props.getProperty("edu.sc.seis.sod.TaupModel");
 	    if(tauPModel == null) tauPModel = "prem";
 	    	   
 	} catch(Exception e) {
 	    
 	    tauPModel = "prem";
 	}
 	
 	String phaseNames= "";
 	if ( ! beginPhase.equals(ORIGIN)) {
 	    phaseNames += " "+beginPhase;
 	} // end of if (beginPhase.equals("origin"))
 	if ( ! endPhase.equals(ORIGIN)) {
 	    phaseNames += " "+endPhase;
 	} // end of if (beginPhase.equals("origin"))
 	
 
 	Arrival[] arrivals = calculateArrivals(tauPModel, 
 					       phaseNames,
 					       origin.my_location, 
 					       channel.my_site.my_location);
 
 	for(int counter = 0; counter < arrivals.length; counter++) {
 	    String arrivalName = arrivals[counter].getName();
 	    if(beginPhase.startsWith("tt")) {
 		if(beginPhase.equals("tts") 
 		   && arrivalName.toUpperCase().startsWith("S")) {
 		    arrivalStartTime = arrivals[counter].getTime();
 		    break;
 		} else if(beginPhase.equals("ttp") 
 			  && arrivalName.toUpperCase().startsWith("P")) {
 		    arrivalStartTime = arrivals[counter].getTime();
 		    break;
 		} 
 	    } else if(beginPhase.equals(arrivalName)) {
 		arrivalStartTime = arrivals[counter].getTime();
 		break;
 	    }
 	}
 	    
 	for(int counter = 0; counter < arrivals.length; counter++) {
 	    String arrivalName = arrivals[counter].getName();
 	    if(endPhase.startsWith("tt")) {
 		if(endPhase.equals("tts") 
 		   && arrivalName.toUpperCase().startsWith("S")) {
 		    arrivalEndTime = arrivals[counter].getTime();
 		    break;
 		} else if(endPhase.equals("ttp") 
 			  && arrivalName.toUpperCase().startsWith("P")) {
 		    arrivalEndTime = arrivals[counter].getTime();
 		    break;
 		} 
 	    } else if(endPhase.equals(arrivalName)) {
 		arrivalEndTime = arrivals[counter].getTime();
 		break;
 	    }
 	}
 
 	if (beginPhase.equals(ORIGIN)) {
 	    arrivalStartTime = 0;
 	}
 	if (endPhase.equals(ORIGIN)) {
 	    arrivalEndTime = 0;
 	}
 
 	if(arrivalStartTime == -100.0 || arrivalEndTime == -100.0) {
 	    // no arrivals found, return zero length request filters
 	    return new RequestFilter[0];
 	} 
 
 	/*System.out.println("originDpeth "+originDepth);
 	System.out.println("distance "+SphericalCoords.distance(origin.my_location.latitude, 
 		 				    origin.my_location.longitude,
 			 			    channel.my_site.my_station.my_location.latitude,
 				 			       channel.my_site.my_station.my_location.longitude));
 	System.out.println("arrivalStartTime = "+arrivalStartTime);
 	System.out.println("arrivalEndTime = "+arrivalEndTime);*/
 
 	// round to milliseconds
 	arrivalStartTime = Math.rint(1000*arrivalStartTime)/1000;
 	arrivalEndTime = Math.rint(1000*arrivalEndTime)/1000;
 
 	edu.iris.Fissures.Time originTime = origin.origin_time;
 	MicroSecondDate originDate = new MicroSecondDate(originTime);
	TimeInterval bInterval = 
	    new TimeInterval(beginOffset.getValue()+arrivalStartTime, 
			     UnitImpl.SECOND);
	TimeInterval eInterval = 
	    new TimeInterval(endOffset.getValue()+arrivalEndTime, 
			     UnitImpl.SECOND);
 	MicroSecondDate bDate = originDate.add(bInterval);
 	MicroSecondDate eDate = originDate.add(eInterval);
 	RequestFilter[] filters;
         filters = new RequestFilter[1];
         filters[0] = 
             new RequestFilter(channel.get_id(),
                               bDate.getFissuresTime(),
 			      eDate.getFissuresTime()
                               );
 	
 	return filters;
 
     }
 
     protected static TauP_Time tauPTime = new TauP_Time();
 
     protected synchronized static Arrival[] calculateArrivals(
                                               String tauPModelName, 
 					      String phases, 
 					      Location originLoc, 
 					      Location channelLoc)
 	throws java.io.IOException, TauModelException {
 	if (tauPTime.getTauModelName() != tauPModelName) {
 	    tauPTime.loadTauModel(tauPModelName);
 	}
 	tauPTime.clearPhaseNames();
 	tauPTime.parsePhaseList(phases);
 
 	double originDepth =
 	   ((QuantityImpl)originLoc.depth).convertTo(UnitImpl.KILOMETER).value;
 	tauPTime.setSourceDepth(originDepth);
 	tauPTime.calculate(SphericalCoords.distance(originLoc.latitude, 
 						    originLoc.longitude,
 						    channelLoc.latitude,
 						    channelLoc.longitude));
 			   
 	return tauPTime.getArrivals();
     }
 
     private BeginOffset beginOffset;
 
     private String beginPhase;
 
     private EndOffset endOffset;
 
     private String endPhase;
 
     private static final String ORIGIN = "origin";
     
 }// PhaseRequest
