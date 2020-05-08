 package org.pdc.nca;
 
 import com.sun.syndication.feed.synd.SyndEntry;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 import org.jdom.Attribute;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import org.pdc.vtec.*;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Thin serializable wrapper on SyndEntry for caching.  Also convenience methods for generating unique identifiers from contents of SyndEntry,
  * output contents of the <entry> as in the NWS Feed, breaking out VTEC codes, flattening out the embedded CAP namespace for better readability   
  * @author Uday
  *
  */
 public class Entry implements Serializable
 {
 
   private Logger logger = LoggerFactory.getLogger(Entry.class);
 
   SyndEntry syndEntry; 
   
   // these are the inner xml tags
   private String id ="";
   private String updated = "";
   private String published ="";
   private String author = "";
   private String title = "";
   private String link ="";
   private String summary = "";
   private String capEvent= "";
   private String capEffective="";
   private String capExpires="";
   private String capStatus="";
   private String capMsgType="";
   private String capCategory="";
   private String capUrgency="";
   private String capSeverity="";
   private String capCertainty="";
   private String capAreaDesc="";
   private String capPolygon="";
   private String key = "";
   
   private String[] capGeoCodeNames = {""};
   private String[] capGeoCodeValue = {""};
 
   private boolean haveGeoCodes = false;
 
   // VTEC Messages
   private List<Message> messages = new ArrayList<Message>(); 
 
   
   /*
   public String vtec = "";
   
   private String pVtecProductClass = "";
   private String pVtecActions = "";
   private String pVtecOfficeId = "";
   private String pVtecPhenomena = "";
   private String pVtecSignificance = "";
   private String pVtecEventTrackingNumber="";
   private String pVtecBeginDate="";
   private String pVtecEndDate="";
   
   private String hVtecNwsLocationId="";
   private String hVtecFloodSeverity="";
   private String hVtecBeginDate="";
   private String hVtecFloodCrestDate="";
   private String hVtecEndDate="";
   private String hVtecImmediateCause="";
   private String hVtecFloodRecordStatus="";
   
   private boolean havePvtec = false;
   private boolean haveHvtec = false;
   */
   
   //private DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();
   
   /**
    * Empty constructor for hibernate, java bean only - do NOT use in code, use the next one...
    */
   public Entry()
   {
   }
   
   /**
    * Build out of SyndEntry, copy, parse into itself 
    * @param entry
    */
   public Entry(SyndEntry entry) 
   {
     syndEntry = entry; 	  
     copySyndEntry();
     setMytKey();
   }
 
   public Logger getLogger() {
 	return logger;
 }
 
 public void setLogger(Logger logger) {
 	this.logger = logger;
 }
 
 public String getId() {
 	return id;
 }
 
 public void setId(String id) {
 	this.id = id;
 }
 
 public String getUpdated() {
 	return updated;
 }
 
 public void setUpdated(String updated) {
 	this.updated = updated;
 }
 
 public String getPublished() {
 	return published;
 }
 
 public void setPublished(String published) {
 	this.published = published;
 }
 
 public String getAuthor() {
 	return author;
 }
 
 public void setAuthor(String author) {
 	this.author = author;
 }
 
 public String getTitle() {
 	return title;
 }
 
 public void setTitle(String title) {
 	this.title = title;
 }
 
 public String getLink() {
 	return link;
 }
 
 public void setLink(String link) {
 	this.link = link;
 }
 
 public String getSummary() {
 	return summary;
 }
 
 public void setSummary(String summary) {
 	this.summary = summary;
 }
 
 public String getCapEvent() {
 	return capEvent;
 }
 
 public void setCapEvent(String capEvent) {
 	this.capEvent = capEvent;
 }
 
 public String getCapEffective() {
 	return capEffective;
 }
 
 public void setCapEffective(String capEffective) {
 	this.capEffective = capEffective;
 }
 
 public String getCapExpires() {
 	return capExpires;
 }
 
 public void setCapExpires(String capExpires) {
 	this.capExpires = capExpires;
 }
 
 public String getCapStatus() {
 	return capStatus;
 }
 
 public void setCapStatus(String capStatus) {
 	this.capStatus = capStatus;
 }
 
 public String getCapMsgType() {
 	return capMsgType;
 }
 
 public void setCapMsgType(String capMsgType) {
 	this.capMsgType = capMsgType;
 }
 
 public String getCapCategory() {
 	return capCategory;
 }
 
 public void setCapCategory(String capCategory) {
 	this.capCategory = capCategory;
 }
 
 public String getCapUrgency() {
 	return capUrgency;
 }
 
 public void setCapUrgency(String capUrgency) {
 	this.capUrgency = capUrgency;
 }
 
 public String getCapSeverity() {
 	return capSeverity;
 }
 
 public void setCapSeverity(String capSeverity) {
 	this.capSeverity = capSeverity;
 }
 
 public String getCapCertainty() {
 	return capCertainty;
 }
 
 public void setCapCertainty(String capCertainty) {
 	this.capCertainty = capCertainty;
 }
 
 public String getCapAreaDesc() {
 	return capAreaDesc;
 }
 
 public void setCapAreaDesc(String capAreaDesc) {
 	this.capAreaDesc = capAreaDesc;
 }
 
 public String getCapPolygon() {
 	return capPolygon;
 }
 
 public void setCapPolygon(String capPolygon) {
 	this.capPolygon = capPolygon;
 }
 
 public String[] getCapGeoCodeNames() {
 	return capGeoCodeNames;
 }
 
 public void setCapGeoCodeNames(String[] capGeoCodeNames) {
 	this.capGeoCodeNames = capGeoCodeNames;
 }
 
 public String[] getCapGeoCodeValue() {
 	return capGeoCodeValue;
 }
 
 public void setCapGeoCodeValue(String[] capGeoCodeValue) {
 	this.capGeoCodeValue = capGeoCodeValue;
 }
 
 
 public boolean isHaveGeoCodes() {
 	return haveGeoCodes;
 }
 
 public void setHaveGeoCodes(boolean haveGeoCodes) {
 	this.haveGeoCodes = haveGeoCodes;
 }
 
 public void setKey(String k)
 {
 	this.key = k;
 }
 
 public String getKey()
 {
 	return this.key;
 }
 /**
    * Provides access to syndEntry (and all this methods)
    * @return the internal SyndEntry member variable 
    */
   public SyndEntry getSyndEntry()
   {
 	  return syndEntry;
   }
 	 
  /**
   * This will serve as key for the entries cache or hash - (key, value), where value is PdcEntry 
   * TODO may be modified to any string like hash of various parameters  
   * @return  the CAP ID concatenated dot published date in milliseconds since java epoch  
   */
   private void setMytKey()
   {
 	
 	String myKey = syndEntry.getLink() + "." + syndEntry.getPublishedDate().getTime();
 	
 	try
 	{
 	
 		if (this.messages.size() > 0) 
 		{
 			  String [] vtecArr = StringUtils.split(this.messages.get(0).getVtec(), "/");
 			  myKey += "." + StringUtils.replace(vtecArr[0], "-", "."); // this will replace dash with dot for better filenaming
 	          
 	          if (this.messages.get(0).isHaveHvtec())
 	          {
 	        	  myKey += "." + vtecArr[2];
 	              logger.info("KEY = MD5(Full#title#updateMillis#P-VTEC#H-VTEC");
 	          }
 	          else
 	          {
 	          
 	            logger.info("KEY = MD5(Triple#title+updateMillis+P-VTEC#1");
 	          }
 	
 		}
 		else
 		{
 			logger.info("Will be just hashing title and updateMillis");
 		}
 	
 	}
 	catch(Exception e)
 	{
 		// just will not get VTEC info into the hash  key -- big deal, just ignore it, but log it - could be sign of trouble with VTEC parser
 		logger.warn("Unable to build VTEC into the Key " + e.toString());
 		if (this.messages.size() > 0)
 		{
 			// definitely error
 			logger.error("Even though this entries has " +  this.messages.size() + " VTECs embedded into it.  Most definitely a PARSE error");
 		}
 	}
 	
 	// try hashing it
 	try
 	{
 		logger.info("Attempting MD5 Hashing key = " + myKey);
 		myKey = getMD5hash(myKey);
 	}
 	catch (Exception e)
 	{
 		logger.warn("FAILED to Hash key " + e.toString() + ", will be generating filenames based on id, updateMillis");
 		myKey = StringUtils.substringAfterLast(myKey, "=");	
 	}
 	
 	
 	logger.info("Generated key " + myKey+ " for feed entry >> " + syndEntry.getTitle() + " << updated " + syndEntry.getUpdatedDate());
 	
 	
 	this.setKey(myKey);
   }
 	 
   public String getFilename(String ext)
   {
 	  return getKey() + "." + ext;
   }
   public String getFilename()
   {
 	  return getFilename("txt");
   }
   
   
   /**
    * Vomits self in properties format
    * @return String representation of PdcEntry, as written to feed entry file 
    */
   public String getProperties()
   {
 	  //copySyndEntry();
 	  java.io.StringWriter swr =  new java.io.StringWriter();
 	  try
 	  {
 		  write(swr);
 	  }
 	  catch (Exception e)
 	  {
 		  logger.info("PdcEntry getProperties failed to write into string writer " + e.toString() );
 	  }
 	  return swr.toString();
   }
 
   public String toString()
   {
 	  // for now just properties 
 	  return getProperties();
   }
 
   public String getXML()
   {
 	  //copySyndEntry();
 	  java.io.StringWriter swr =  new java.io.StringWriter();
 	  
 	  Element entry = new Element("entry");
 	  Document doc = new Document(entry);
 	  doc.setRootElement(entry);
 
 	  
 	  entry.addContent(new Element("title").setText(this.title));
 	  entry.addContent(new Element("updated").setText(this.syndEntry.getUpdatedDate().toString()));
 	  entry.addContent(new Element("published").setText(this.syndEntry.getPublishedDate().toString()));
 	  entry.addContent(new Element("link").setText(this.link));
 
 	  entry.addContent(new Element("id").setText(this.id));
 	  entry.addContent(new Element("updatedMillisZulu").setText(this.updated));
 	  entry.addContent(new Element("publishedMillisZulu").setText(this.published));
 
 	  entry.addContent(new Element("author").setText(this.author));
 	  
 	  entry.addContent(new Element("summary").setText(this.summary));
 	  
 	  entry.addContent(new Element("capEvent").setText(this.capEvent));
 	  entry.addContent(new Element("capEffective").setText(this.capEffective));
 	  entry.addContent(new Element("capExpires").setText(this.capExpires));
 	  entry.addContent(new Element("capStatus").setText(this.capStatus));
 	  entry.addContent(new Element("capMessageType").setText(this.capMsgType));
 	  entry.addContent(new Element("capCategory").setText(this.capCategory));
 	  entry.addContent(new Element("capUrgency").setText(this.capUrgency));
 	  entry.addContent(new Element("capSeverity").setText(this.capSeverity));
 	  entry.addContent(new Element("capCertainty").setText(this.capCertainty));
 	  entry.addContent(new Element("capAreaDescription").setText(this.capAreaDesc));
 	  entry.addContent(new Element("capPolygon").setText(this.capPolygon));
 	  
 	  Element geocodes = new Element("capGeoCode");
 	  if (haveGeoCodes)
 	  {
 	    for (int i=0; i < capGeoCodeNames.length; i++)
 	    {
 	    	Element geoElement = new Element("capGeoValue").setText(capGeoCodeValue[i]);
 	    	geoElement.setAttribute(new Attribute("name", capGeoCodeNames[i]));
 	    	geocodes.addContent(geoElement);
 	    }
 	  }
 	  entry.addContent(geocodes);
 	  
 	  // vtec
 	  Element vtecElement = new Element("vtec");
 	  
 	  for (Message message : messages)
 	  {
 		  
 		  String [] vtecArr = StringUtils.split(message.getVtec(), "/");
 		  logger.info("VTEC (clean) = " + message.getVtec());  logger.info("vtecArray SIZE created from split = " + vtecArr.length); 
 		  
 		  if (message.isHavePvtec())
 		  {
 			
 		    Element pVtecElement = new Element("p-vtec");
 		    pVtecElement.setAttribute(new Attribute("value", "/"+vtecArr[0]+"/"));
 
 			String [] pelements = StringUtils.split(vtecArr[0] , ".");
 	        
 	        // product class
 	        Element k = new Element("productClass").setText(message.getpVtecProductClass()); k.setAttribute(new Attribute("k", pelements[0]));
 	        pVtecElement.addContent(k);
 		    
 		    // actions 
 	        Element aaa = new Element("actions").setText(message.getpVtecActions()); aaa.setAttribute(new Attribute("aaa", pelements[1]));
 	        pVtecElement.addContent(aaa);
 		    
 	        // office 
 	        Element office = new Element("officeId").setText(message.getpVtecOfficeId()); 
 	        pVtecElement.addContent(office);
 
 	        // phenomena 
 	        Element pp = new Element("phenomena").setText(message.getpVtecPhenomena()); pp.setAttribute(new Attribute("pp", pelements[3]));
 	        pVtecElement.addContent(pp);
 	
 		    // significance
 	        Element sig = new Element("significance").setText(message.getpVtecSignificance()); sig.setAttribute(new Attribute("s", pelements[4]));
 	        pVtecElement.addContent(sig);
 	
 		    // etn
 	        pVtecElement.addContent(new Element("eventTrackingNumber").setText(message.getpVtecEventTrackingNumber()));
 		    
 		    // begin date/time
 		    Element b = new Element("eventBeginDateTime").setText(message.getpVtecBeginDate()); b.setAttribute(new Attribute("fmt","yymmddThhnnZ"));
 		    pVtecElement.addContent(b);
 		    // end date/time
 		    pVtecElement.addContent(new Element("eventEndDateTime").setText(message.getpVtecEndDate()));
 		    
 		    Element hydro = new Element("h-vtec");
 		    
 		    if (message.isHaveHvtec())
 		    {
 	    	  String [] helements = StringUtils.split(vtecArr[2] , ".");
 	    	  hydro.setAttribute(new Attribute("value", "/"+vtecArr[2]+"/"));
 
 	
 	          // nws loc id
 	          hydro.addContent(new Element("nwsLocationId").setText(message.gethVtecNwsLocationId())); 
 	
 	          // severity
 	          Element siv = new Element("floodSeverity").setText(message.gethVtecFloodSeverity()); siv.setAttribute(new Attribute("s", helements[1]));
 	          hydro.addContent(siv);
 	
 	          // immediate cause
 	          Element ic = new Element("immediateCause").setText(message.gethVtecImmediateCause()); ic.setAttribute(new Attribute("ic", helements[2]));
 	          hydro.addContent(ic);
 	
 	  	      // end date/time
 	          hydro.addContent(new Element("floodBeginDateTime").setText(message.gethVtecBeginDate()));
 	          hydro.addContent(new Element("floodCrestDateTime").setText(message.gethVtecFloodCrestDate()));
 	          hydro.addContent(new Element("floodEndDateTime").setText(message.gethVtecEndDate()));
 	
 	  	      // flood records status
 	          Element fr = new Element("floodRecordStatus").setText(message.gethVtecFloodRecordStatus()); fr.setAttribute(new Attribute("fr", helements[6]));
 	          hydro.addContent(fr);
 		    
 	          pVtecElement.addContent(hydro);	    
 		    } // end-if H-VTEC i.e. message has VTEC with hydrological component/extension
 		    
 		    // and the p-vtec element to vtec parent
 		    vtecElement.addContent(pVtecElement);
 		    
 		  } // end-if P-VTEC - if VTEC was non-empty and correctly parsed 
 		  
 		  
 	  } // end for (Message message in VTEC messages) 
 	  
 	  // add the vtec, empoty or not to entry 
 	  entry.addContent(vtecElement);
 	  
 	  try
 	  {
   	    XMLOutputter xmlOutput = new XMLOutputter();
 		xmlOutput.setFormat(Format.getPrettyFormat());
 		xmlOutput.output(doc, swr);	  }
 	  catch (Exception e)
 	  {
 		  logger.info("PdcEntry getXML failed to write into string writer " + e.toString() );
 	  }
 	  return swr.toString();
 	  
   }
   
   
   
   /**
    * 
    * @param string
    * @return NoSuchAlgorithm Exception - 
    * @throws Exception
    */
   public String getMD5hash (String s) throws Exception 
   {
       MessageDigest m= MessageDigest.getInstance("MD5");
       m.update(s.getBytes(),0,s.length());
       return new BigInteger(1,m.digest()).toString(16);
   }
 
   
   private void copySyndEntry()
   {
 	  
 	  logger.info("Entering copySyndEntry " + syndEntry.getTitle());
 	  
 	  
 	  this.id = StringUtils.substringAfterLast(syndEntry.getLink(), "=");
 	  this.link = syndEntry.getUri();
 	  
 	  //StringBuffer sb = new StringBuffer(); dateTimeFormatter.printTo(sb, syndEntry.getUpdatedDate().getTime());
 	  this.updated = syndEntry.getUpdatedDate().getTime() + "";
 	  
 	  //sb = new StringBuffer(); dateTimeFormatter.printTo(sb, syndEntry.getPublishedDate().getTime());
 	  this.published = syndEntry.getPublishedDate().getTime() + "";
 	  
 	  this.author = syndEntry.getAuthor();
 	  
 	  this.title = syndEntry.getTitle();
 	  
 	  this.summary = syndEntry.getDescription().getValue();
 
 	  // just brute-force the jdom elements being apparently returned as foreign markup
 	  List fms = (List) syndEntry.getForeignMarkup();
 	  logger.info("Entering foreign markup for loop.  Number of markups to be processed  " + fms.size());
 
 	  for (Object fm: fms)  
 	  {
 		  Element jdomElem = (Element) fm;
 		  
 		  if (StringUtils.containsIgnoreCase(jdomElem.getName(), "event" ))
 		  {
 			  capEvent=jdomElem.getValue();
 		  }
 		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "effective" ))
 		  {
 			  capEffective=jdomElem.getValue();
 		  }
 		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "expires" ))
 		  {
 			  capExpires=jdomElem.getValue();
 		  }
 		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "status" ))
 		  {
 			  capStatus=jdomElem.getValue();
 		  }
 		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "msgType" ))
 		  {
 			  capMsgType=jdomElem.getValue();
 		  }
 		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "category" ))
 		  {
 			  capCategory=jdomElem.getValue();
 		  }
 		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "urgency" ))
 		  {
 			  capUrgency=jdomElem.getValue();
 		  }
 		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "severity" ))
 		  {
 			  capSeverity=jdomElem.getValue();
 		  }
 		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "certainty" ))
 		  {
 			  capCertainty=jdomElem.getValue();
 		  }
 		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "areaDesc" ))
 		  {
 			  capAreaDesc=jdomElem.getValue();
 		  }
 		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "polygon" ))
 		  {
 			  capPolygon=jdomElem.getValue();
 		  }
 		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "geocode" ))
 		  {
 			  List gcodes = (List) jdomElem.getChildren();
 			  logger.info("Entering Geo Code.  Number of geocode children (names and values)  " + gcodes.size());
 
 			  // per jdom, an empty list is returned if no kids
 			  // capGeoCodeNames, capGeoCodeValue will remain empty 
 			  if (gcodes.isEmpty()) continue;
 			  else haveGeoCodes = true;
 			  
 			  List<String> gnames= new ArrayList <String>();
 			  List<String> gvalues= new ArrayList <String>();
 			  
 			  for (Object gcode: gcodes) 
 			  {
 				  Element glement = (Element) gcode; 
 				  if (StringUtils.containsIgnoreCase(glement.getName(), "name" )) gnames.add(glement.getValue());
 				  else gvalues.add(glement.getValue());
 			  }
 			  
 			  
 			  capGeoCodeNames = new String[gnames.size()];
 			  capGeoCodeValue = new String [gvalues.size()];
 			  
 			  for (int i=0; i < gnames.size(); i++)
 			  {
 				  capGeoCodeNames[i] = gnames.get(i);
 			  }
 			  for (int i=0; i < gvalues.size(); i++)
 			  {
 				  capGeoCodeValue[i] = gvalues.get(i);
 			  }
 			  
 			  for (int i=0; i < gnames.size(); i++)
 			  {
 				  logger.info(capGeoCodeNames[i] + ".value(s) = " + capGeoCodeValue[i] ); 	  
 			  }
 			  
 			  
 		  }
 		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "parameter" ))
 		  {
 			  List children = jdomElem.getChildren();
 			  
 			  // gotta have exactly two kids 
 			  if (children.isEmpty())
 			  {
 				  logger.info("The paramter elments was found empty - ignoring");
 				  continue;
 			  }
 			  else if (children.size() != 2)
 			  {
 				  logger.info("The paramter elments was found to NOT have exactly two children - did the NWS CAP ATOM VTEC release change, add more parameters ??????????");
 			  }
 			  
 			  logger.info("iterating through children of <cap:parameter> - VTEC codes are released by NWS here within this tag...");
 			  for (Object child: children)
 			  {
 				  Element paramElem = (Element) child;
 				  
 				  if (StringUtils.containsIgnoreCase(paramElem.getValue(), "VTEC" ))
 				  {
 					 // nothing to do - 
 					  logger.info("Found <valueName>VTEC</valueName> child of parameter");
 				  }
 				  else if (StringUtils.equalsIgnoreCase(paramElem.getName(), "value"))  // the VTEC value is within <value> named child element of <cap:parameter>   
 				  {
 					  logger.info("Found <value> named element child of parameter");
 					  String vtec = StringUtils.trimToEmpty(paramElem.getValue());
 					  if (vtec.length() == 0)
 					  {
 						  logger.info("VTEC <value> tag is EMPTY - ignoring");
 						  continue;
 					  }
 					  else
 					  {
 						  logger.info("Raw VTEC, warts and all, within <value> tag " + vtec);
 						  Parser parser = new Parser(vtec);
 						  messages = parser.getMessages();
 						  logger.info("Number VTEC Message Objects Generated " + messages.size());
 						  if (this.messages.size() > 2)
 				          {
 				        	  // this would be that rare entry more than two (or more?!) P-VTEC; log it, just for kicks - most likely a parse error
				        	  logger.info("Found a feed Entry with more than 2 VTECs, actually, in fact, " + messages.size() + " VTECs" +
				        	              this.getSyndEntry().getTitle() + ",  Last Updated " + this.getSyndEntry().getUpdatedDate());
 				          }
 					  }
 				  }
   
 			  }
 		  }
 		  
 	  }
   }
 		 
 
   public void write(java.io.Writer w) throws java.io.IOException
   {
 	  // why here and not constructor ??? because no need for operation except if PdCEntry is new. 
 	  //copySyndEntry();
 
 	  // this is where the PdcEntry itself writes to file
 	  w.write("title="+this.syndEntry.getTitle());
 	  writeln(w,"updatedDate="+this.syndEntry.getUpdatedDate());
 	  writeln(w,"publishedDate="+this.syndEntry.getPublishedDate());
 	  
 	  writeln(w,"#  Last Updated - milliseconds since Epoch January 1, 1970 00.00.00 UNIVERSAL Time (UTC/Zulu)");
 	  writeln(w,"updatedMillis="+this.updated);
 	  writeln(w,"publishedMillis="+this.published);
 	  
 	  writeln(w,"URL="+this.link);
 	  writeln(w,"author="+this.author);
 
 	  writeln(w,"#  This is the Embdedded CAP Feed Identifier extracted from the URL after the equal sign"); 
 	  writeln(w,"id="+this.id);
 	  writeln(w,"summary="+this.summary);
 	  
 	  
 	  writeln(w,"#  Foreign Markup (CAP info embedded in this Atom \"Index\") ------------------ ");
 	  writeln(w,"cap.event="+this.capEvent);
 	  writeln(w,"cap.effectiveDate="+this.capEffective);
 	  writeln(w,"cap.expireDate="+this.capExpires);
 	  writeln(w,"cap.status="+this.capStatus);
 	  writeln(w,"cap.messageType="+this.capMsgType);
 	  writeln(w,"cap.category="+this.capCategory);
 	  writeln(w,"cap.urgency="+this.capUrgency);
 	  writeln(w,"cap.severity="+this.capSeverity);
 	  writeln(w,"cap.certainty="+this.capCertainty);
 	  writeln(w,"cap.areaDescripton="+this.capAreaDesc);
 	  writeln(w,"cap.polygon="+this.capPolygon);
 
 	  writeln(w,"#  Geocodes, if available, omitted otherwise  ------------------ ");
 	  
 	  if (haveGeoCodes) 
 	  {
 		  writeln(w,"#  COMMA separated in key-value(s) format;  each key may have multiple values, which are separated by whitespace as-is from NWS feed------- ");
 
 		  for (int i=0; i < capGeoCodeNames.length; i++)
      	  {
 		    writeln(w,"cap.geocode."+i+"="+ capGeoCodeNames[i] + "," + capGeoCodeValue[i]);
 	      }
 	  }
 
 	  writeln(w,"# VTEC if available, omitted otherwise  ------------------  ");
 
 	  for (int k = 0; k < messages.size(); k++)
 	  {
 		  Message message = messages.get(k);
 		  
 		  String [] vtecArr = StringUtils.split(message.getVtec(), "/");
 		  logger.info("VTEC (clean) = " + message.getVtec());  logger.info("vtecArray SIZE created from split = " + vtecArr.length); 
 		  writeln(w,"vtec.constructor="+message.getVtec());
 		  writeln(w,"#");
 		  if (message.isHavePvtec())
 		  {
 			  String [] pelements = StringUtils.split(vtecArr[0] , ".");
 			  writeln(w,"vtec.ProductClass."+k+"="+ pelements[0] + ", " + message.getpVtecProductClass());
 			  writeln(w,"vtec.Actions."+k+"="+ pelements[1] + ", " + message.getpVtecActions());
 			  writeln(w,"vtec.OfficeId."+k+"="+message.getpVtecOfficeId());
 			  writeln(w,"vtec.Phenomena."+k+"="+ pelements[3] + ", " + message.getpVtecPhenomena());
 			  writeln(w,"vtec.Significance."+k+"="+ pelements[4] + ", " + message.getpVtecSignificance());
 			  writeln(w,"vtec.EventTrackingNumber."+k+"="+message.getpVtecEventTrackingNumber());
 			  writeln(w,"vtec.BeginDate."+k+"="+message.getpVtecBeginDate());
 			  writeln(w,"vtec.EndDate."+k+"="+message.getpVtecEndDate());
 	
 			  writeln(w,"#  Hydrological (H) VTEC if available ------------------ ");
 		
 			  if (message.isHaveHvtec())
 			  {
 				  String [] helements = StringUtils.split(vtecArr[2] , ".");
 				  writeln(w,"vtec.hydro.NwsLocationId."+k+"="+message.gethVtecNwsLocationId());
 				  writeln(w,"vtec.hydro.FloodSeverity."+k+"="+ helements[1] + ", " + message.gethVtecFloodSeverity());
 				  writeln(w,"vtec.hydro.ImmediateCause."+k+"="+ helements[2] + ", " + message.gethVtecImmediateCause());
 				  writeln(w,"vtec.hydro.BeginDate."+k+"="+message.gethVtecBeginDate());
 				  writeln(w,"vtec.hydro.FloodCrestDate."+k+"="+message.gethVtecFloodCrestDate());
 				  writeln(w,"vtec.hydro.EndDate."+k+"="+message.gethVtecEndDate());
 				  writeln(w,"vtec.hydro.FloodRecordStatus."+k+"="+ helements[6] + ", " + message.gethVtecFloodRecordStatus());
 			  }
 		  }
 	  }
   }
 		  
 	  
   private void writeln(java.io.Writer writer, String str) throws java.io.IOException
   {
 	  writer.write(System.getProperty("line.separator"));
 	  writer.write(StringUtils.trimToEmpty(str));   
   }
 
 	  
 	  
 	  
 }
   
 
