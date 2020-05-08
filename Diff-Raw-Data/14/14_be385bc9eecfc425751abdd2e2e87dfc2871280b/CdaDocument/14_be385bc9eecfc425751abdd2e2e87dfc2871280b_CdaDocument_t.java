 package at.mts.entity.cda;
 import at.mts.entity.Bodyparts;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.List;
 import java.util.UUID;
 import java.util.Date;
 
 import at.mts.entity.Condition;
 import at.mts.entity.Gender;
 import at.mts.entity.Patient;
 import at.mts.entity.PhaseOfLife;
 import at.mts.entity.Treatment;
 import at.mts.entity.TriageCategory;
 import at.mts.entity.xml.Comment;
 import at.mts.entity.xml.Document;
 import at.mts.entity.xml.Element;
 /**
  * CDA-Dokument.
  * Ermoeglicht das einlesen oder erzeugen eines CDA-Dokuments.
  * Eigenschaften koennen mittels get- und set-Methoden ausgelesen oder geandert werden.
  * @author Daniel Gehrer
  */
 public class CdaDocument {
 	
 	/**
 	 * Erzeugt ein neues, leeres CDA-Dokument
 	 */
 	public CdaDocument() {
 		setDocumentDate(new Date());
 		setIdV(new CdaIdV(UUID.randomUUID(), 1));
 	}
 	
 	/**
 	 * Erzeugt ein CdaDocument aus einem XML-String
 	 * @param document CDA Dokument als XML-String
 	 */
 	public CdaDocument(String document) {
 		setDocumentDate(new Date());
 		load(document);
 	}
 	
 	/**
 	 * Erzeugt ein CdaDocument aus einem Patient-Objekt
 	 * @param patient Patientendaten
 	 */
 	public CdaDocument(Patient patient) {
 		
 		setIdV(new CdaIdV(patient.getId(), patient.getVersion()));
 		
         setPatientNameGiven(patient.getNameGiven());
         setPatientNameFamily(patient.getNameFamily());
         setPatientId(patient.getId());
         setPatientGender(patient.getGender());
         setPatientBirthTime(patient.getBirthTime());
         
         setParentIdV(null);  //??
         setDocumentDate(patient.getTimestamp());
         
         //#############--BODY--#############*/
         body = new CdaBody();
         //-----VITALZEICHEN
         setBody(body, "gehfaehigkeit", CdaBoolean.asCdaValue(patient.getWalkable()));
         setBody(body, "respiration", Condition.asCdaValue(patient.getRespiration()));
         setBody(body, "perfusion", Condition.asCdaValue(patient.getPerfusion()));
         setBody(body, "mentalerstatus", Condition.asCdaValue(patient.getMentalStatus()));
         setBody(body, "triagekategorie", TriageCategory.asCdaValue(patient.getCategory()));
         setBody(body, "behandlung", Treatment.asCdaValue(patient.getTreatment()));
                 
        //-----DETAILS
         setBody(body, "gps", patient.getGps());
         setBody(body, "lebensphase",  patient.getPhaseOfLife(), PhaseOfLife.asCdaValue(patient.getPhaseOfLife()));
         setBody(body, "bergeinformation", patient.getSalvageInfoString());
         setBody(body, "hilfplatzposition", patient.getPlacePosition());
         setBody(body, "dringlichkeit", patient.getUrgency(), patient.getUrgency());
         setBody(body, "diagnose", patient.getDiagnosisString());
         
         if (patient.getBloodPressureSystolic() != null && patient.getBloodPressureDiastolic() != null) {
         	setBody(body, "blutdruck", patient.getBloodPressureSystolic()+":"+patient.getBloodPressureDiastolic());
         }
         setBody(body, "puls", patient.getPulse(), patient.getPulse());
         setBody(body, "behandlungsverlauf", patient.getCourseOfTreatmentString());
         setBody(body, "transportbereitschaft", patient.getReadyForTransport(), CdaBoolean.asCdaValue(patient.getReadyForTransport()));
         setBody(body, "zielkrankenhaus", patient.getHospital());
         setBody(body, "krankenkasse", patient.getHealthInsurance());
        
         //-----VERLETZUNGEN
         //set(String part, String information)
         Bodyparts bodyparts = patient.getBodyparts();
         if (bodyparts != null) {
         
         	for (String key : bodyparts.keySet()) {
 				body.set(key, bodyparts.get(key));
 			}
         }
 	}
 	
 	private void setBody(CdaBody body, String key, String value) {
 		setBody(body, key, value, value);
 	}
 	
 	private void setBody(CdaBody body, String key, Object value, Object cdaValue) {
 		if (value != null) {
 			body.set(key, cdaValue.toString());
 		}
 	}
 	
 	/**
 	 * Liest ein CdaDocument aus einem XML-String.
 	 * Die bestehenden Daten gehen dabei verloren.
 	 * @param document CDA Dokument als XML-String
 	 */
 	public void load(String document) {
 		
 		try{
             
 			body = new CdaBody(); // Body loeschen
 			Document doc = new Document(document);
 			Object NS = null;
 	        Element root = doc.getRootElement();
 
 	        setAuthorNameGiven(root.getChild("author", NS).getChild("assignedAuthor",NS).getChild("assignedPerson", NS).getChild("name", NS).getChild("given", NS).getText());
 	        setAuthorNameFamily(root.getChild("author", NS).getChild("assignedAuthor",NS).getChild("assignedPerson", NS).getChild("name", NS).getChild("family", NS).getText());
 	        setAuthorId(UUID.fromString((root.getChild("author", NS).getChild("assignedAuthor",NS).getChild("id", NS).getAttributeValue("extension"))));
 	        
 	        setPatientNameGiven(root.getChild("recordTarget", NS).getChild("patientRole",NS).getChild("patient", NS).getChild("name", NS).getChild("given", NS).getText());
 	        setPatientNameFamily(root.getChild("recordTarget", NS).getChild("patientRole",NS).getChild("patient", NS).getChild("name", NS).getChild("family", NS).getText());
 	        setPatientId(UUID.fromString((root.getChild("recordTarget", NS).getChild("patientRole",NS).getChild("id", NS).getAttributeValue("extension"))));
 	        setPatientGender(Gender.getValueOf(root.getChild("recordTarget", NS).getChild("patientRole",NS).getChild("patient", NS).getChild("administrativeGenderCode", NS).getAttributeValue("code")));
 
 	        String birthtime = root.getChild("recordTarget", NS).getChild("patientRole",NS).getChild("patient", NS).getChild("birthTime", NS).getAttributeValue("value");
 	        DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
 	        setPatientBirthTime((Date) formatter.parse(birthtime));
 	        
 	        UUID idFromCda =UUID.fromString(root.getChild("id", NS).getAttributeValue("extension"));
 	        int versionNr = Integer.parseInt(root.getChild("versionNumber", NS).getAttributeValue("value"));	
 	        setIdV(new CdaIdV(idFromCda,versionNr));
 	        
 	        if (root.getChild("relatedDocument", NS) != null && root.getChild("relatedDocument", NS).getChild("parentDocument", NS) != null) {
 		        UUID idFromCdaParent =UUID.fromString(root.getChild("relatedDocument", NS).getChild("parentDocument", NS).getChild("id", NS).getAttributeValue("extension"));
 		        int versionNrParent = Integer.parseInt(root.getChild("relatedDocument", NS).getChild("parentDocument", NS).getChild("versionNumber", NS).getAttributeValue("value"));	
 		        setParentIdV(new CdaIdV(idFromCdaParent, versionNrParent));
 	        }
 	        
 	        String effectiveTime = root.getChild("effectiveTime", NS).getAttributeValue("value"); 
 	        setDocumentDate((Date) formatter.parse(effectiveTime));
 	        
 	        
 	        
 	        //#############--BODY--#############*/
 	        //-----VITALZEICHEN
 
 	        Element textVital = root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text",NS);
 	        
 	        splitInfo(textVital);
 	        
 	        //-----DETAILS
 	        List <Element> liste = root.getChild("component", NS).getChild("structuredBody",NS).getChildren();
 	        Element textDetails =liste.get(1).getChild("section", NS).getChild("text",NS);
 
 	        splitInfo(textDetails);
 	        
 	        //-----VERLETZUNGEN
 	        Element textInjury =liste.get(1).getChild("section", NS).getChild("component",NS).getChild("section",NS).getChild("text",NS);
 
 	        splitInfo(textInjury);
 	        
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Setzt/Aktualisiert alle Property-Werte aus dem CDA-Body
 	 * @param info ist eine Section aus dem CDA-Body
 	 */
 	private void splitInfo(Element element){
 
 		String bodyValue = element.getText();
		String[] lines = bodyValue.split("\\r?\\n");
 		String prekey="";
 		for (String line : lines) {
 			String[] keypair = line.trim().split(":", 2);
 			
 			if (keypair.length > 1) {
 				String key = keypair[0].trim();
 				String value = keypair[1].trim();
 				body.set(key, value);
 				prekey=key;
 			}
 			else
 			{
 				if(keypair[0].length()>0)
 					body.set(prekey, body.get(prekey) + "\n"+ keypair[0]);
 			}
 		}
 	}
 	
 	public static String StreamToString(final InputStream is)
 	{
 	  final char[] buffer = new char[4*1024];
 	  final StringBuilder out = new StringBuilder();
 	  try {
 	    final Reader in = new InputStreamReader(is, "UTF-8");
 	    try {
 	      for (;;) {
 	        int rsz = in.read(buffer, 0, buffer.length);
 	        if (rsz < 0)
 	          break;
 	        out.append(buffer, 0, rsz);
 	      }
 	    }
 	    finally {
 	      in.close();
 	    }
 	  }
 	  catch (UnsupportedEncodingException ex) {
 	    /* ... */
 	  }
 	  catch (IOException ex) {
 	      /* ... */
 	  }
 	  return out.toString();
 	}
 	
 	/**
 	 * Gibt das CDA-Dokument als XML-String zurueck
 	 * @return CDA-Dokument als XML-String
 	 */
 	public String asXml() {
 		String xml="";
 		try{
 			InputStream stream = getClass().getResourceAsStream("/at/mts/entity/resources/blank.xml");
 			Document doc = new Document(StreamToString(stream));
 			Object NS = null;
 	        //Namespace NS = Namespace.getNamespace("urn:hl7-org:v3");     
 	        
 	        
 	        Element root = doc.getRootElement();
 	        
 	        root.getChild("author", NS).getChild("assignedAuthor",NS).getChild("assignedPerson", NS).getChild("name", NS).getChild("given", NS).setText("Max");
 	        root.getChild("author", NS).getChild("assignedAuthor",NS).getChild("assignedPerson", NS).getChild("name", NS).getChild("family", NS).setText("Mustermann");
 	        root.getChild("author", NS).getChild("assignedAuthor",NS).getChild("id", NS).setAttribute("extension", "f51e7681-fc6b-4032-8f28-008f471fe32f");
 	        root.getChild("author", NS).getChild("time", NS).setAttribute("value", "20130506"); 
 	        
 	        
 	        root.getChild("recordTarget", NS).getChild("patientRole",NS).getChild("patient", NS).getChild("name", NS).getChild("given", NS).setText(getPatientNameGiven());
 	        root.getChild("recordTarget", NS).getChild("patientRole",NS).getChild("patient", NS).getChild("name", NS).getChild("family", NS).setText(getPatientNameFamily());
 	        root.getChild("recordTarget", NS).getChild("patientRole",NS).getChild("id", NS).setAttribute("extension", getPatientId().toString());
 	        root.getChild("recordTarget", NS).getChild("patientRole",NS).getChild("patient", NS).getChild("administrativeGenderCode", NS).setAttribute("code", Gender.asCdaValue(getPatientGender()));
 
 	        DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
 	        Date birthTime = getPatientBirthTime();
 	        if (birthTime == null) { birthTime = formatter.parse("18000101"); }
 	        root.getChild("recordTarget", NS).getChild("patientRole",NS).getChild("patient", NS).getChild("birthTime", NS).setAttribute("value", formatter.format(birthTime));
 	        
 	        
 	        root.getChild("id", NS).setAttribute("extension", getIdV().getId().toString());
 	        root.getChild("versionNumber", NS).setAttribute("value", Integer.toString(getIdV().getVersion()));
 	        
 	        Element relatedDocument = root.getChild("relatedDocument", NS);
 	        if (getParentIdV() == null) {
	        	root.getChild("relatedDocument", NS).getChild("parentDocument", NS).getChild("id", NS).setAttribute("extension", getIdV().getId().toString());
		        root.getChild("relatedDocument", NS).getChild("parentDocument", NS).getChild("versionNumber", NS).setAttribute("value", Integer.toString(getIdV().getVersion()));	
 	        }
 	        else {
 		        root.getChild("relatedDocument", NS).getChild("parentDocument", NS).getChild("id", NS).setAttribute("extension", getParentIdV().getId().toString());
 		        root.getChild("relatedDocument", NS).getChild("parentDocument", NS).getChild("versionNumber", NS).setAttribute("value", Integer.toString(getParentIdV().getVersion()));	
 	        }
 	        
 	        Date effectiveTime = getDocumentDate();
 	        if (effectiveTime == null) { effectiveTime = new Date(); }
 	        root.getChild("effectiveTime", NS).setAttribute("value", formatter.format(effectiveTime)); 
 	        
 	        //#############--BODY--#############*/
 	        //-----VITALZEICHEN
 	        
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).removeContent();
 	        
 	        // System.out.println(vital);
 	        
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent(new Comment(" gehfaehigkeit? Werte: ja/nein "));
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent("\ngehfaehigkeit: "+body.get("gehfaehigkeit"));
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent("<br />\n"); // (new Element("br",NS));
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent(new Comment(" respiration? Werte: stabil/kritisch "));
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent("\nrespiration: "+body.get("respiration"));
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent("<br />\n"); // (new Element("br",NS));
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent(new Comment(" perfusion? Werte: stabil/kritisch "));
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent("\nperfusion: "+body.get("perfusion"));
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent("<br />\n"); // (new Element("br",NS));
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent(new Comment(" mentalerstatus? Werte: stabil/kritisch "));
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent("\nmentalerstatus: "+body.get("mentalerstatus"));
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent("<br />\n"); // (new Element("br",NS));
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent(new Comment(" Werte: immediate, delayed, minor, deceased "));
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent("\ntriagekategorie: "+body.get("triagekategorie"));
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent("<br />\n"); // (new Element("br",NS));
 	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent(new Comment(" Werte: undefiniert, gesichtet, geborgen, abtransportiert "));
	        root.getChild("component", NS).getChild("structuredBody",NS).getChild("component", NS).getChild("section", NS).getChild("text", NS).addContent("\nbehandlung: "+body.get("behandlung") + "\n	");
 	           
 	        //-----DETAILS
 	       
 	        List <Element> liste = root.getChild("component", NS).getChild("structuredBody",NS).getChildren();
 	        liste.get(1).getChild("section", NS).getChild("text",NS).removeContent();
 	        
 	        if (body.get("gps") != null) {
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent(new Comment(" Format: 46.992365,10.022476,5.0 wobei die erste Zahl die geographische Breite, die zweite Zahl die geographische Lnge in Grad und die dritte Zahl die Genauigkeit in Meter. Oder kA wenn keine Position vorhanden "));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("\ngps: "+body.get("gps"));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("<br />\n"); // (new Element("br",NS));
 	        }
 	        if (body.get("lebensphase") != null) {
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent(new Comment(" Werte: Kind, Erwachsen "));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("\nlebensphase: "+body.get("lebensphase"));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("<br />\n"); // (new Element("br",NS));
 	        }
 	        if (body.get("bergeinformation") != null) {
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent(new Comment(" Werte: Freitext String"));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("\nbergeinformation: "+body.get("bergeinformation"));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("<br />\n"); // (new Element("br",NS));
 	        }
 	        if (body.get("hilfplatzposition") != null) {
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent(new Comment(" Werte: Freitext String"));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("\nhilfplatzposition: "+body.get("hilfplatzposition"));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("<br />\n"); // (new Element("br",NS));
 	        }
 	        if (body.get("dringlichkeit") != null) {
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent(new Comment(" Werte: 1-5 (5 am Dringlichsten) "));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("\ndringlichkeit: "+body.get("dringlichkeit"));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("<br />\n"); // (new Element("br",NS));
 	        }
 	        if (body.get("diagnose") != null) {
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent(new Comment(" Werte: ICD10-Code - getrennt durch ; "));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("\ndiagnose: "+body.get("diagnose"));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("<br />\n"); // (new Element("br",NS));
 	        }
 	        if (body.get("blutdruck") != null) {
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent(new Comment(" Werte: int systolisch : int diastolisch "));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("\nblutdruck: "+body.get("blutdruck"));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("<br />\n"); // (new Element("br",NS));
 	        }
 	        if (body.get("puls") != null) {
 	        	liste.get(1).getChild("section", NS).getChild("text",NS).addContent(new Comment(" Werte: int bpm "));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("\npuls: "+body.get("puls"));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("<br />\n"); // (new Element("br",NS));
 	        }
 	        if (body.get("behandlungsverlauf") != null) {
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent(new Comment(" Werte: Freitext String - getrennt durch ; "));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("\nbehandlungsverlauf: "+body.get("behandlungsverlauf"));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("<br />\n"); // (new Element("br",NS));
 	        }
 	        if (body.get("transportbereitschaft") != null) {
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent(new Comment(" Werte: ja/nein "));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("\ntransportbereitschaft: "+body.get("transportbereitschaft"));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("<br />\n"); // (new Element("br",NS));
 	        }
 	        if (body.get("zielkrankenhaus") != null) {
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent(new Comment(" Freitext String "));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("\nzielkrankenhaus: "+body.get("zielkrankenhaus"));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("<br />\n"); // (new Element("br",NS));
 	        }
 	        if (body.get("krankenkasse") != null) {
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent(new Comment(" Werte: Freitext String "));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("\nkrankenkasse: "+body.get("krankenkasse"));
 		        liste.get(1).getChild("section", NS).getChild("text",NS).addContent("<br />\n"); // (new Element("br",NS));
 	        }
 	        //-----VERLETZUNGEN
 	        
 	        liste.get(1).getChild("section", NS).getChild("component",NS).getChild("section",NS).getChild("text",NS).setText("");
 	        liste.get(1).getChild("section", NS).getChild("component",NS).getChild("section",NS).getChild("text",NS).addContent(new Comment("Werte: Freitext String UEBERALL"));
 	        
 	        for (String key : Bodyparts.Keys) {
 	        	String text = body.get(key);
 				if (text != null) {
 					liste.get(1).getChild("section", NS).getChild("component",NS).getChild("section",NS).getChild("text",NS).addContent("		"+key+": "+text+"<br />"+"\n");
 				}
 			}
    
 	        xml = doc.asXml().replaceAll("&lt;br /&gt;", "<br />");
 		}
     	catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return xml;
 		
 	}
 	
 	/**
 	 * ID und Version des Dokuments
 	 */
 	private CdaIdV idV = null;
 	/**
 	 * @return ID und Version des Dokuments
 	 */
 	public CdaIdV getIdV() { return idV; }
 	/**
 	 * Setzt ID und Version des Dokuments
 	 * @param value neuer Wert
 	 */
 	public void setIdV(CdaIdV value) { idV = value; }
 	
 	/**
 	 * ID und Version des vorgaenger Dokuments
 	 */
 	private CdaIdV parentIdV = null;
 	/**
 	 * @return ID und Version des vorgaenger Dokuments
 	 */
 	public CdaIdV getParentIdV() { return parentIdV; }
 	/**
 	 * Setzt ID und Version des vorgaenger Dokuments 
 	 * @param value neuer Wert
 	 */
 	public void setParentIdV(CdaIdV value) { parentIdV = value; }
 	
 	/**
 	 * ID des Patienten im Header
 	 */
 	private UUID patientId = null;
 	/**
 	 * @return ID des Patienten im Header
 	 */
 	public UUID getPatientId() { return patientId; }
 	/**
 	 * Setzt ID des Patienten im Header
 	 * @param value neuer Wert
 	 */
 	public void setPatientId(UUID value) { patientId = value; }
 	
 	
 	/**
 	 * Vorname des Patienten im Header
 	 */
 	private String patientNameGiven;
 	/**
 	 * @return Vorname des Patienten im Header
 	 */
 	public String getPatientNameGiven(){  return patientNameGiven;  }
 	/**
 	 * Setzt den Vornamen des Patienten im Header
 	 * @param name neuer Name
 	 */
 	private void setPatientNameGiven(String name){
 		patientNameGiven=name;
 	}
 	
 	/**
 	 * Nachname des Patienten im Header
 	 */
 	private String patientNameFamily;
 	/**
 	 * @return Nachname des Patienten im Header
 	 */
 	public String getPatientNameFamily(){  return patientNameFamily;  }
 	/**
 	 * Setzt den Nachnamen des Patienten im Header
 	 * @param name neuer Name
 	 */
 	private void setPatientNameFamily(String name){
 		patientNameFamily=name;
 	}
 
 	/**
 	 * Geburtsdatum des Patienten im Header
 	 */
 	private Date patientBirthTime;
 	/**
 	 * @return Geburtsdatum des Patienten im Header
 	 */
 	public Date getPatientBirthTime(){  return patientBirthTime;  }
 	/**
 	 * Setzt das Geburtsdatum des Patienten im Header
 	 * @param date neues Datum
 	 */
 	public void setPatientBirthTime(Date date){
 		patientBirthTime = date;
 	}	
 
 	/**
 	 * Geschlecht des Patienten im Header
 	 */
 	private Gender patientGender;
 	/**
 	 * @return Geschlecht des Patienten im Header
 	 */
 	public Gender getPatientGender(){  return patientGender;  }
 	/**
 	 * Setzt das Geschlecht des Patienten im Header
 	 * @param gender neues Geschlecht
 	 */
 	private void setPatientGender(Gender gender){
 		patientGender = gender;
 	}
 	
 	/**
 	 * Nachname des Authors im Header
 	 */
 	private String authorNameFamily;
 	/**
 	 * @return Nachname des Authors im Header
 	 */
 	public String getAuthorNameFamily(){  return authorNameFamily;  }
 	/**
 	 * Setzt den Nachnamen des Authors im Header
 	 * @param name neuer Name
 	 */
 	private void setAuthorNameFamily(String name){
 		authorNameFamily=name;
 	}
 
 	/**
 	 * Vorname des Authors im Header
 	 */
 	private String authorNameGiven;
 	/**
 	 * @return Vorname des Authors im Header
 	 */
 	public String getAuthorNameGiven(){  return authorNameGiven;  }
 	/**
 	 * Setzt den Vornamen des Authors im Header
 	 * @param name neuer Name
 	 */
 	private void setAuthorNameGiven(String name){
 		authorNameGiven=name;
 	}
 	
 	/**
 	 * Erstell-Datum des Dokuments
 	 */
 	private Date documentDate;
 	/**
 	 * @return Erstell-Datum des Dokuments
 	 */
 	public Date getDocumentDate() { return documentDate; }
 	/**
 	 * Setzt das Erstell-Datum des Dokuments
 	 * @param newdate neues Datum
 	 */
 	public void setDocumentDate(Date newdate) {
 		documentDate = newdate;
 	}
 	
 	/**
 	 * ID des Authors im Header
 	 */
 	private UUID authorId;
 	/**
 	 * @return ID des Authors im Header
 	 */
 	public UUID getAuthorId(){  return authorId;  }
 	/**
 	 * Setzt die ID des Authors im Header
 	 * @param value neuer Wert
 	 */
 	public void setAuthorId(UUID value){
 		authorId = value;
 	}
 	
 
 	private CdaBody body = new CdaBody();
 	/**
 	 * @return CDA-Body
 	 */
 	public CdaBody getBody() { return body; }
 	
 }
