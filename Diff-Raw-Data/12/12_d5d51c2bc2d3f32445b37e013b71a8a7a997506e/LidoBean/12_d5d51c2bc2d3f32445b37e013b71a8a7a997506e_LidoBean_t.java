 package beans;
 
 import eu.dca.model.*;
 import java.io.*;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.File;
 import java.lang.Class;
 import java.net.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.ArrayList;
 import java.util.TreeSet;
 import java.util.Collections;
 import java.util.List;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.log4j.Logger;
 import org.richfaces.skin.SkinFactory;
 import javax.servlet.*;
 import org.apache.commons.lang3.*;
 
 @ManagedBean(name = "lidoBean")
 @RequestScoped
 public class LidoBean {
     //Logger
     private static Logger log = Logger.getLogger(LidoBean.class.getName());
 
     /**
      * @return the log
      */
     public static Logger getLog() {
         return log;
     }
 
     /**
      * @param aLog the log to set
      */
     public static void setLog(Logger aLog) {
         log = aLog;
     }
     private ResourceBundle res;
     //Base Object
     private Lido lido;
     private LidoElement lidoelement;
     private LidoWrap lidowrap; 
     //properties
     private String id="1";
     private String xmlData = "";
     private String baselang="en";    
     
     private String currentUrl ="";
     private String xmlstring="";
     
     //Record Data
     private String recordidlbl;    
     private String recordid;    
     private String recsourcelbl;
     private String recsource;
     private String rectypelbl;
     private String rectype;
     private String recpreflbl;
     private String recpref;
     private String recenclbl;
     private String recenc;
     private String reclabellbl;
     private String reclabel;
     
     //Category Data
     private String conceptid="";
     private String conceptidlbl="";
     private String categoryterm="";
     private String categorytermlbl="";
     
     //Repository
     private String repositoryname="";
     private String repositorynamelbl="";
     private String repositoryworkid="";
     private String repositoryworkidlbl="";
     private String odescvaluelbl ="";
     
     //Administrative Metadata
     private String reslink;
     private String reslinklbl=""; 
     private String otitlelbl="";    
     private String otitle="";    
     private String titlelbl="";    
     private String title="";
     private String typelbl="";
     private String type=""; 
     private String odescvalue ="";
     
     private String rightsWraplegalbodyname = "";
     private String rightsWraplegalbodyweblink = "";
     
     private String rightsWraplegalbodynamelbl = "";
     private String rightsWraplegalbodyweblinklbl = "";
     
     private String recWraprecid = "";
     private String recWraprecidlbl = "";
     private String recWraprectype = "";
     private String recWraprectypelbl = "";
     private String recWraprecsource = "";
     private String recWraprecsourcelbl = "";
     private String recWraprecrights = "";
     private String recWraprecrightslbl = "";
     
     private String measurementType="";
     private String measurementUnit="";
     private String measurementValue="";
     private String measurementlbl="";
     
     private String omeasurement = "";
     private String skin = "";
     private String logoImage = "dca_logo_3.png";
        
     private List subconceptterms = new ArrayList<String>();    
     private List events = new ArrayList<LidoEvent>(); 
     private List adminResources = new ArrayList<AdminResource>();
     private List adminRecWraps = new ArrayList<AdminResource>();
     private List adminRightsHolders = new ArrayList<String>();
     private List languagesList = new ArrayList<String>();
     private TreeSet<String> languagesTree = new TreeSet<String>();
     private List langList= new ArrayList<String>();
     
     public LidoBean() throws MalformedURLException, IOException{
         log.info("LidoBean() constructor called");
         HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
         //URL url = new URL("http://localhost/1.xml");
        request.setCharacterEncoding("UTF-8");
         //get POST XML data
         if (request.getParameter("xmlData")!=null) {            
             this.xmlData = request.getParameter("xmlData").trim();
             //log.info("LidoBean() New XML loaded:"+URLDecoder.decode( this.xmlData, "UTF-8" ).getBytes("UTF-8"));
         
         //reload POST XML data in case the languages are selected    
         }else if (request.getParameter("lidoFormXML:xmlData")!=null) {            
             this.xmlData = request.getParameter("lidoFormXML:xmlData");
             //log.info("LidoBean() New XML  loaded with POST:"+URLDecoder.decode( this.xmlData, "UTF-8" ).getBytes("UTF-8"));
         }
         //log.info("LidoBean() Lido Content:"+this.xmlData);       
         //Get Request ID XML
         if (request.getParameter("id")!=null) {
             id=request.getParameter("id");
             log.info("LidoBean() New id loaded:"+id);
         } else {
             log.info("LidoBean() Existing id:"+id);
         }
         //Get baselang
         if (request.getParameter("baselang")!=null) {
             baselang=request.getParameter("baselang");
             log.info("LidoBean() New baselang loaded:"+baselang);            
         }else if(request.getParameter("lidoFormXML:baselang")!=null){
             baselang=request.getParameter("lidoFormXML:baselang");
             log.info("LidoBean() New baselang loaded with POST:"+baselang);
         }else {
             log.info("LidoBean() Existing baselang:"+baselang );
         }
         
         this.currentUrl = request.getRequestURI();
         this.currentUrl += "?id="+id;        
        
         //Load Properties
         loadProperties(baselang);
         //Load XML
         loadXML(id, xmlData);
         //Detect available languages
         detectLanguages();
         //initlabels
         initLabels();
         //initFields
         initFields();
         
         //changeSkin(id);
         
     }//EoCon
     
     private void loadProperties(String lang){
         //getLog().info("LidoBean.loadProperties() called with base-lang:"+lang);
        String filename = "properties.lido"+lang.toLowerCase();
         try {
             setRes(ResourceBundle.getBundle(filename));
             getLog().info("LidoBean.loadProperties() Resources loaded successfully");  
         } catch (Exception ex) {
             ex.printStackTrace();
         }        
     }//EoM
     
     private void loadXML(String id, String xmlData) {
         getLog().info("LidoBean.loadXML() called with id:"+id);  
         String canonicalPath = "";
         try {
             canonicalPath =   ((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getRealPath("");
             //log.info("canonicalPath:"+canonicalPath);
         } catch (Exception ex) {
             java.util.logging.Logger.getLogger(LidoBean.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         getLog().info("LidoBean.loadXML() canonicalPath: "+canonicalPath);
         String filename = canonicalPath+"/resources/tempFiles/"+id + ".xml";
         //log.info("filename:"+filename);
         try {
             
             HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
             getLog().info("Skin factory:"+request.getSession().getServletContext().getInitParameter("org.richfaces.skin"));
             //Reader inp = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
             //Reader inp = new StringReader(xmlData.replace("&", "&amp;"));
             Reader inp = new StringReader(URLDecoder.decode(xmlData, "UTF-8" ));
             setLido(LidoWrap.unmarshal(inp).getLidoElement(0));            
             inp.close();
             getLog().info("LidoBean.loadXML() loaded successfully");    
 
             getLog().info("LidoBean.loadXML(): converting xml file to string");
             //this.xmlstring = readFile(filename);
             this.xmlstring = URLDecoder.decode(xmlData, "UTF-8" );
             //TODO load from URL also
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
     }//EoM 
     
     private void loadXMLInputStream(URL url) {
         
         getLog().info("LidoBean.loadXMLInputStream() called with id:"+id);        
         try {
             Reader inp = new BufferedReader(new InputStreamReader(url.openStream(), "UTF8"));
             //setLido(Lido.unmarshal(inp));
             
             inp.close();
             getLog().info("LidoBean.loadXML() loaded successfully");            
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
     
     private void changeSkin(String id){
         
         //set skin
         //SkinBean skinbean = new SkinBean();
             //this.logoImage = id+".jpg";
             getLog().info("Choosing skin...");
             if(id.equalsIgnoreCase("1")){
                 getLog().info("skin is set to classic");                
                 this.skin = "classic";
             }else if(id.equalsIgnoreCase("2")){
                 getLog().info("skin is set to wine");                
                 this.skin = "wine";
             }else if(id.equalsIgnoreCase("3")){
                 getLog().info("skin is set to blueSky");                
                 this.skin = "blueSky";
             }else{
                 getLog().info("skin is set to japanCherry");                
                 this.skin = "japanCherry";            
             }
     
     }
     
     
     private void initLabels() {
         getLog().info("LidoBean.initLabels() called with baselang:"+this.baselang);
         //Record
         this.recordidlbl = getRes().getString("recordid");         
         this.recpreflbl = getRes().getString("recpref");
         this.rectypelbl = getRes().getString("rectype");
         this.recsourcelbl = getRes().getString("recsource");
         this.recenclbl = getRes().getString("recenc");
         this.reclabellbl = getRes().getString("reclabel");
         //Administrative Metadata
         this.otitlelbl = getRes().getString("otitle");
         this.titlelbl = getRes().getString("title");
         this.typelbl = getRes().getString("type");
         this.odescvaluelbl = getRes().getString("noteValue");
         
         this.repositorynamelbl = getRes().getString("repositorynamelbl");
         this.repositoryworkidlbl = getRes().getString("repositoryworkidlbl");
         
         //Administrative data labels
         this.rightsWraplegalbodynamelbl = getRes().getString("legalBodyName");
         this.rightsWraplegalbodyweblinklbl = getRes().getString("legalBodyWeblink");
         
         this.recWraprecidlbl = getRes().getString("recWraprecidlbl");
         this.recWraprecrightslbl = getRes().getString("recWraprecrightslbl");
         this.recWraprecsourcelbl = getRes().getString("recWraprecsourcelbl");
         this.recWraprectypelbl = getRes().getString("recWraprectypelbl");
         
         this.reslinklbl = getRes().getString("reslinklbl");
         this.measurementlbl = getRes().getString("measurementlbl");
         
         this.conceptidlbl = getRes().getString("conceptidlbl");
         this.categorytermlbl = getRes().getString("categorytermlbl");
         
         this.measurementlbl = getRes().getString("measurementlbl");
         
         
         
         getLog().info("LidoBean.initLabels() loaded successfully");                    
     }//EoM initLabels    
     
     private void initFields() {
         getLog().info("LidoBean.initFields() called");
         if (getLido() != null) {
 
             //-----------Record Elements
             LidoRecID record = getLido().getLidoRecID(0);
             //recordid
             this.recordid = record.getContent();
             //pref
             this.recpref = record.getPref();
             //type
             this.rectype = record.getType();
             //source
             this.recsource = record.getSource();
             //encoding
             this.recenc = record.getEncodinganalog();
             //label      
             this.reclabel = record.getLabel();
             
             //-------------category Elements
             Category category = getLido().getCategory();
             //concept ID
             if(category != null){
                 
                 if(category.getConceptIDCount() > 0){
                     this.conceptid = category.getConceptID(0).getContent();
                 }
                 
                 if(category.getTermCount()>0){
                     
                     //term
                     this.categoryterm = category.getTerm(0).getContent();
                 }
             }
             
             
             
             //-------------Descriptive metadata            
             int descount = getLido().getLidoDescriptiveMetadataCount();            
             for (int dc=0;dc<descount;dc++){
                 LidoDescriptiveMetadata description = getLido().getLidoDescriptiveMetadata(dc);
                 //if (description.getLang().trim().equalsIgnoreCase(baselang)) {
                     /* get Title */
                    
                     TitleWrap twrap = description.getObjectIdentificationWrap().getTitleWrap();
                      
                     int tsc = twrap.getTitleSetCount();
                    
                     for (int ts=0;ts<tsc;ts++){
                         TitleSet titleset = twrap.getTitleSet(ts);
                         int avc = titleset.getAppellationValueCount();  
                         
                         this.langList.clear();
                         for (int av=0;av<avc;av++){
                            AppellationValue appelation = titleset.getAppellationValue(av);                           
                            this.langList.add(appelation.getLang());
                                
                            /*getLog().info("appelation lang: "+appelation.getLang());
                            getLog().info("appelation content: "+appelation.getContent());
                            if (appelation.getLang()==null) 
                                this.otitle = appelation.getContent(); 
                            else{
                               if (appelation.getLang().trim().equalsIgnoreCase(baselang)){
                                 this.title = appelation.getContent();                          
                               }//base-lang found
                                
                            }*/
                           
                         }//for AppellationValues
                         
                         if(langList.contains(baselang)){
                             for (int av=0;av<avc;av++){                                
                                 AppellationValue appelation = titleset.getAppellationValue(av);
                                 if(appelation.getLang() != null && appelation.getLang().trim().equalsIgnoreCase(baselang)){                                    
                                     this.title = appelation.getContent();
                                 }
                                                        
                             }                            
                         }else{                            
                             if(langList.size()>0){
                                 for (int av=0;av<avc;av++){                                
                                     AppellationValue appelation = titleset.getAppellationValue(av);
                                     this.title += ((title==null || title.equalsIgnoreCase(""))?getMultilingualContent(appelation.getLang(), appelation.getContent()): ","+getMultilingualContent(appelation.getLang(), appelation.getContent())); 
                                 }                             
                             }
                             
                             if(avc == 1){
                                 AppellationValue appelation = titleset.getAppellationValue(0);                                
                                 if(appelation.getLang()==null){
                                     this.otitle = appelation.getContent();                                
                                 }
                             }
                         }
                     }//for titleSets
                     
                     /*get objectIdentificationWrap.inscriptions*/
                     /*InscriptionsWrap iwrap = description.getObjectIdentificationWrap().getInscriptionsWrap();
                     if(iwrap.getInscriptionsCount()>0){ //cardinality: 0-inf
                         for(int ic=0; ic<iwrap.getInscriptionsCount(); ic++){
                             
                             
                             
                         }
                         
                     }*/
                     /*get objectIdentificationWrap.objectDescriptionWrap*/
                     /*ObjectDescriptionWrap owrap = description.getObjectIdentificationWrap().getObjectDescriptionWrap();
                      if(owrap.getObjectDescriptionSetCount()>0){ //cardinality: 0-inf
                         for(int odc=0; odc<owrap.getObjectDescriptionSetCount(); odc++){
                             DescriptiveNoteValue descNoteValue = owrap.getObjectDescriptionSet(odc).getDescriptiveNoteValue(0);
                             //type+= ( (type==null || type.equalsIgnoreCase(""))? getMultilingualContent(term.getLang(), term.getContent()) : ","+getMultilingualContent(term.getLang(), term.getContent()) );
                             this.odescvalue += ((type==null || type.equalsIgnoreCase(""))?getMultilingualContent(descNoteValue.getLang(), descNoteValue.getContent()): ","+getMultilingualContent(descNoteValue.getLang(), descNoteValue.getContent())); 
                             
                         }
                         
                     }*/
                     
                     /*get repository*/
                     RepositoryWrap repowrap = description.getObjectIdentificationWrap().getRepositoryWrap();
                     //suppose cardinality: 1
                     if(repowrap != null && repowrap.getRepositorySetCount() > 0){
                         RepositorySet reposet = repowrap.getRepositorySet(0);
                         RepositoryName reponame = reposet.getRepositoryName();
                         if(reponame != null){
                             AppellationValue appelation = reponame.getLegalBodyName(0).getAppellationValue(0);
                             this.repositoryname = appelation.getContent();
                         }
                         
                         if(reposet.getWorkIDCount()>0){
                             
                             WorkID wid = reposet.getWorkID(0);
                             this.repositoryworkid = wid.getContent();
                         
                         }
                     }
                     
                     /* get Measurements*/
                     ObjectMeasurementsWrap oMeasurementSet = description.getObjectIdentificationWrap().getObjectMeasurementsWrap();
                     //suppose cardinality: 1
                     if(oMeasurementSet !=  null && oMeasurementSet.getObjectMeasurementsSetCount() > 0){
                         if( oMeasurementSet.getObjectMeasurementsSetCount()>0){
                         ObjectMeasurementsSet MeasurementSet = oMeasurementSet.getObjectMeasurementsSet(0);
                         if(MeasurementSet.getDisplayObjectMeasurementsCount() > 0){
                             this.omeasurement = MeasurementSet.getDisplayObjectMeasurements(0).getContent();
                         }
                         
                         }
                     
                     }
                     
                     
                     /* get Type */
                     int owtc = description.getObjectClassificationWrap().getObjectWorkTypeWrap().getObjectWorkTypeCount();
                     for (int owt=0;owt<owtc;owt++){
                         ObjectWorkType objwt = description.getObjectClassificationWrap().getObjectWorkTypeWrap().getObjectWorkType(owt);
                         int termcount = objwt.getTermCount();
                         /*for (int termc=0;termc<termcount;termc++){
                             Term term = objwt.getTerm(termc);
                             type+= ( (type==null || type.equalsIgnoreCase(""))? getMultilingualContent(term.getLang(), term.getContent()) : ","+getMultilingualContent(term.getLang(), term.getContent()) );
                         }//for
                         */
                        
                         
                         this.langList.clear();
                         for(int termc=0;termc<termcount;termc++){
                             Term term = objwt.getTerm(termc);
                             this.langList.add(term.getLang());                        
                         }
                         
                         if(this.langList.contains(baselang)){
                             for(int termc=0;termc<termcount;termc++){
                                 Term term = objwt.getTerm(termc);
                                 if(term.getLang() != null && term.getLang().trim().equals(baselang)){
                                     type+= ( (type==null || type.equalsIgnoreCase(""))? term.getContent() : ","+term.getContent() );                        
                                 }                        
                             }
                         }else{
                             
                             for(int termc=0;termc<termcount;termc++){
                                 Term term = objwt.getTerm(termc);
                                 type+= ( (type==null || type.equalsIgnoreCase(""))? getMultilingualContent(term.getLang(), term.getContent()) : ","+getMultilingualContent(term.getLang(), term.getContent()) );
                                                        
                             }
                         
                         }
                         
                         
                         
                     }//for   
                     
                     /* Concept terms */
                     ObjectRelationWrap orw = description.getObjectRelationWrap();
                     if (orw!=null){
                         SubjectWrap swrap = orw.getSubjectWrap();
                         if (swrap!=null){
                             int sscount = swrap.getSubjectSetCount();
                             for (int ss=0;ss<sscount;ss++){
                                 SubjectSet subset = swrap.getSubjectSet(ss);
                                 Subject subject = subset.getSubject();
                                 if (subject!=null){
                                     int scc = subject.getSubjectConceptCount();
                                     for (int sc=0;sc<scc;sc++){
                                         SubjectConcept subconcept = subject.getSubjectConcept(sc);
                                         int tc = subconcept.getTermCount();
                                         for (int t=0;t<tc;t++){
                                             Term term = subconcept.getTerm(t);
                                             String str = term.getContent();
                                             if (!subconceptterms.contains(str)) getSubconceptterms().add(str);
                                         }//for
                                     }//for
                                 }//not null subject
                             }//for
                         }//not null swrap
                     }//not null ObjectRelationWrap
                     //Sort terms
                     //Collections.sort(subconceptterms);
                     
                     /* Events */
                     EventWrap eventwrap = description.getEventWrap();
                     if (eventwrap!=null){
                         int esc = eventwrap.getEventSetCount();
                         for (int es=0;es<esc;es++){
                             getLog().info("LidoBean.Events: no."+es);
                             EventSet eventset = eventwrap.getEventSet(es);
                             Event event = eventset.getEvent();
                             //declare new event
                             LidoEvent levent = new LidoEvent();  
                             //1 - EventType
                             int tc = event.getEventType().getTermCount();
                             /*for (int t=0;t<tc;t++){
                                Term term =  event.getEventType().getTerm(t);
                                levent.eventype += ( (levent.eventype==null || levent.eventype.equalsIgnoreCase(""))? term.getContent() : ","+term.getContent() );
                                //levent.eventype += ( (levent.eventype==null || levent.eventype.equalsIgnoreCase(""))? getMultilingualContent(term.getLang(), term.getContent()) : ","+getMultilingualContent(term.getLang(), term.getContent()) );
                             }//for
                             */
                             this.langList.clear();
                             for (int t=0;t<tc;t++){
                                 Term term =  event.getEventType().getTerm(t);
                                 this.langList.add(term.getLang());
                             }
                             
                              if(this.langList.contains(baselang)){
                                 for (int t=0;t<tc;t++){
                                     Term term = event.getEventType().getTerm(t);
                                     if(term.getLang() != null && term.getLang().trim().equals(baselang)){
                                         levent.eventype+= ( (levent.eventype==null || levent.eventype.equalsIgnoreCase(""))? term.getContent() : ","+term.getContent() );                        
                                     }                        
                                 }
                             }else{
 
                                 for (int t=0;t<tc;t++){
                                     Term term = event.getEventType().getTerm(t);
                                     levent.eventype+= ( (levent.eventype==null || levent.eventype.equalsIgnoreCase(""))? getMultilingualContent(term.getLang(), term.getContent()) : ","+getMultilingualContent(term.getLang(), term.getContent()) );
 
                                 }
 
                             }
 
                             //2 - Event Date
                             EventDate eventdate = event.getEventDate();
                             if (eventdate!=null){
                                 Date date = eventdate.getDate();
                                 if (date!=null){
                                     EarliestDate earliest = date.getEarliestDate();                                    
                                     if (earliest!=null){
                                         levent.date += ( (levent.date==null || levent.date.equalsIgnoreCase(""))? earliest.getContent() : ","+earliest.getContent() );                                        
                                     }
                                     LatestDate latest = date.getLatestDate();
                                     if (latest!=null){
                                         levent.date += ( (levent.date==null || levent.date.equalsIgnoreCase(""))? latest.getContent() : ","+latest.getContent() );                                                        
                                     }
                                     getLog().info("LidoBean.getDisplayDateCount(): no."+eventdate.getDisplayDateCount());
                                     /*if(eventdate.getDisplayDateCount()>0){
                                         
                                         DisplayDate ddate = eventdate.getDisplayDate(0);
                                         levent.date += ( (levent.date==null || levent.date.equalsIgnoreCase(""))? ddate.getContent() : ","+ddate.getContent() );
                                         getLog().info("LidoBean levent.date: "+levent.date);
                                     }*/
                                     
                                     
                                 }//not null date
                                 
                                 if(eventdate.getDisplayDateCount()>0){
                                     for(int dd=0;dd<eventdate.getDisplayDateCount();dd++){
                                         DisplayDate ddate = eventdate.getDisplayDate(dd);
                                         if(ddate!=null){
                                              levent.date += ( (levent.date==null || levent.date.equalsIgnoreCase(""))? ddate.getContent() : ","+ddate.getContent() );                                        
                                         }
                                     }
                                 }
                                 
                             }//not null EventDate
                             //3 - Event Place
                             int epc = event.getEventPlaceCount();
                             for (int ep=0;ep<epc;ep++){
                                 EventPlace eventplace = event.getEventPlace(ep);
                                 if(eventplace.getPlace() != null){
                                         int npsc = eventplace.getPlace().getNamePlaceSetCount();
                                         for (int nps=0;nps<npsc;nps++){
                                         NamePlaceSet nameplaceset = eventplace.getPlace().getNamePlaceSet(nps);
                                         int avc = nameplaceset.getAppellationValueCount();
                                         /*for(int av=0;av<avc;av++){
                                             AppellationValue appellation = nameplaceset.getAppellationValue(av);
                                              levent.place += ( (levent.place==null || levent.place.equalsIgnoreCase(""))? getMultilingualContent(appellation.getLang(), appellation.getContent()) : ","+getMultilingualContent(appellation.getLang(), appellation.getContent()) );
                                         }//for
                                         */
                                         this.langList.clear();
                                         for(int av=0;av<avc;av++){
                                             AppellationValue appellation = nameplaceset.getAppellationValue(av);
                                             this.langList.add(appellation.getLang());
                                         }
                                         if(this.langList.contains(baselang)){
 
                                             for(int av=0;av<avc;av++){
                                                 AppellationValue appellation = nameplaceset.getAppellationValue(av);
                                                 if(appellation.getLang() != null && appellation.getLang().trim().equals(baselang)){
                                                     levent.place+= ( (levent.place==null || levent.place.equalsIgnoreCase(""))? appellation.getContent() : ","+appellation.getContent() );                        
                                                 }  
                                             }
 
                                         }else{
 
                                              for(int av=0;av<avc;av++){
                                                 AppellationValue appellation = nameplaceset.getAppellationValue(av);
                                                 levent.place += ( (levent.place==null || levent.place.equalsIgnoreCase(""))? getMultilingualContent(appellation.getLang(), appellation.getContent()) : ","+getMultilingualContent(appellation.getLang(), appellation.getContent()) );                      
 
                                             }
 
                                         }
 
                                     }//for                                
                                 }
                             }//for
                             
                             //displayMaterialsTech
                             int dmcount = event.getEventMaterialsTechCount();                            
                             if(dmcount>0){                                
                                 for(int dmc=0;dmc<dmcount;dmc++){
                                     int materialCount = event.getEventMaterialsTech(dmc).getDisplayMaterialsTechCount();
                                     
                                     this.langList.clear();
                                     for(int mc=0;mc<materialCount; mc++){
                                         
                                         DisplayMaterialsTech material = event.getEventMaterialsTech(dmc).getDisplayMaterialsTech(mc);                                        
                                         this.langList.add(material.getLang());
                                     }
                                     
                                     if(this.langList.contains(baselang)){
                                             for (int mc=0;mc<materialCount; mc++){
                                                 DisplayMaterialsTech material = event.getEventMaterialsTech(dmc).getDisplayMaterialsTech(mc);
                                                 if(material.getLang() != null && material.getLang().trim().equals(baselang)){
                                                     levent.material+= ( (levent.material==null || levent.material.equalsIgnoreCase(""))? material.getContent() : ","+material.getContent() );                        
                                                 }                        
                                             }
                                     }else{
 
                                         for (int mc=0;mc<materialCount; mc++){
                                             DisplayMaterialsTech material = event.getEventMaterialsTech(dmc).getDisplayMaterialsTech(mc);
                                             levent.material+= ( (levent.material==null || levent.material.equalsIgnoreCase(""))? getMultilingualContent(material.getLang(), material.getContent()) : ","+getMultilingualContent(material.getLang(), material.getContent()) );
 
                                         }
 
                                     }
                                 }//eof for
                             }//eof Material section
                             
                             //4 - Event Actor
                             int ac = event.getEventActorCount();
                             for (int a=0;a<ac;a++){
                                 EventActor eventactor = event.getEventActor(a);
                                 if (eventactor!=null){
                                     ActorInRole actorinrole = eventactor.getActorInRole();                                    
                                     if(actorinrole != null){
                                         InRoleActor inroleactor = actorinrole.getInRoleActor();
                                         if (inroleactor!=null){
                                             int nasc = inroleactor.getNameActorSetCount();
                                             for (int nas=0;nas<nasc;nas++){
                                                 NameActorSet namaeactorset = inroleactor.getNameActorSet(nas);
                                                 int avc = namaeactorset.getAppellationValueCount();
                                                 /*for(int av=0;av<avc;av++){
                                                     AppellationValue appellation = namaeactorset.getAppellationValue(av);
                                                     levent.actors += ( (levent.actors==null || levent.actors.equalsIgnoreCase(""))? getMultilingualContent(appellation.getLang(), appellation.getContent()) : ","+getMultilingualContent(appellation.getLang(), appellation.getContent()) );
                                                 }//for
                                                 */
                                                                                                
                                                 this.langList.clear();
                                                 for(int av=0;av<avc;av++){
                                                     AppellationValue appellation = namaeactorset.getAppellationValue(av);
                                                     this.langList.add(appellation.getLang());                                                
                                                 }
                                                 
                                                 if(this.langList.contains(baselang)){
 
                                                     for(int av=0;av<avc;av++){
                                                         AppellationValue appellation = namaeactorset.getAppellationValue(av);
                                                         if(appellation.getLang() != null && appellation.getLang().trim().equals(baselang)){
                                                             levent.actors+= ( (levent.actors==null || levent.actors.equalsIgnoreCase(""))? appellation.getContent() : ","+appellation.getContent() );                        
                                                         }  
                                                     }
 
                                                 }else{
 
                                                      for(int av=0;av<avc;av++){
                                                         AppellationValue appellation = namaeactorset.getAppellationValue(av);
                                                         levent.actors += ( (levent.actors==null || levent.actors.equalsIgnoreCase(""))? getMultilingualContent(appellation.getLang(), appellation.getContent()) : ","+getMultilingualContent(appellation.getLang(), appellation.getContent()) );                      
                                                     }
                                                 }
                                             }//for                                        
                                         }//if
                                     }
                                     int dispcnt = eventactor.getDisplayActorInRoleCount();
                                     if(dispcnt >0){
                                        /*for(int da=0; da<dispcnt; da++){
                                            DisplayActorInRole displayactor = eventactor.getDisplayActorInRole(da);
                                            levent.actors += ( (levent.actors==null || levent.actors.equalsIgnoreCase(""))?getMultilingualContent(displayactor.getLang(), displayactor.getContent()) : ","+getMultilingualContent(displayactor.getLang(), displayactor.getContent()) ) ;
                                        }*/
                                         
                                         this.langList.clear();
                                         for(int da=0; da<dispcnt; da++){
                                             DisplayActorInRole displayactor = eventactor.getDisplayActorInRole(da);
                                             this.langList.add(displayactor.getLang());                                        
                                         }
                                         
                                         if(this.langList.contains(baselang)){
 
                                                  for(int da=0; da<dispcnt; da++){
                                                         DisplayActorInRole displayactor = eventactor.getDisplayActorInRole(da);
                                                         if(displayactor.getLang() != null && displayactor.getLang().trim().equals(baselang)){
                                                             levent.actors+= ( (levent.actors==null || levent.actors.equalsIgnoreCase(""))? displayactor.getContent() : ","+displayactor.getContent() );                        
                                                         }  
                                                     }
 
                                                 }else{
 
                                                      for(int da=0; da<dispcnt; da++){
                                                         DisplayActorInRole displayactor = eventactor.getDisplayActorInRole(da);
                                                         levent.actors += ( (levent.actors==null || levent.actors.equalsIgnoreCase(""))?getMultilingualContent(displayactor.getLang(), displayactor.getContent()) : ","+getMultilingualContent(displayactor.getLang(), displayactor.getContent()) ) ;                      
                                                     }
                                                 }
                                     }
                                     
                                 }//if                                
                             }//for
                             //Add to Gridview
                             this.events.add(levent);                            
                         }//for
                     }//not null eventwrap                    
                   
                 //}//baselang found
             }//for-master iteration
             
            //-------------Administrative metadata
             int admincount = getLido().getLidoAdministrativeMetadataCount();
             log.info("Administrative Metadata:"+admincount);
             for (int ac = 0; ac < admincount; ac++) {
                 LidoAdministrativeMetadata admindata = getLido().getLidoAdministrativeMetadata(ac);
                 //if (admindata.getLang().trim().equalsIgnoreCase(baselang)) {
                 
                 
                     /*get rightsWrap*/
                     RightsWorkWrap rightswrap = admindata.getRightsWorkWrap();
                     if(rightswrap != null){
                         if(rightswrap.getRightsWorkSetCount()>0){
                             for(int rs=0; rs<rightswrap.getRightsWorkSetCount(); rs++){
                                 RightsWorkSet rightsSet = rightswrap.getRightsWorkSet(rs);
                                 //create new LidoRightsHolder object
                                 for(int rh=0; rh<rightsSet.getRightsHolderCount(); rh++){
                                     
                                     RightsHolder rightsHolder = rightsSet.getRightsHolder(rh);
                                     LidoRightsHolder lidoRightsHolder = new LidoRightsHolder();
                                     lidoRightsHolder.legalBodyName = getMultilingualContent(rightsHolder.getLegalBodyName(0).getAppellationValue(0).getLang(), rightsHolder.getLegalBodyName(0).getAppellationValue(0).getContent());
                                    
                                     if(rightsHolder.getLegalBodyWeblinkCount() > 0){
                                        
                                         lidoRightsHolder.legalBodyWeblink = getMultilingualContent(rightsHolder.getLegalBodyWeblink(0).getLang(), rightsHolder.getLegalBodyWeblink(0).getContent());
                                        log.info("legalBodyWeblink:"+lidoRightsHolder.legalBodyWeblink);
                                     } 
                                     this.adminRightsHolders.add(lidoRightsHolder);
 
                                 }
                             }
                         }
                     }
                     /*get recordWrap*/
                     RecordWrap recwrap = admindata.getRecordWrap();
                     if (recwrap != null){
                         log.info("Administrative Metadata/Record wrap:"+admincount);
                         if(recwrap.getRecordIDCount()>0){ 
                             this.recWraprecid = recwrap.getRecordID(0).getContent();
                             
                             if(recwrap.getRecordType().getTermCount()>0){
                                 
                                 this.recWraprectype = recwrap.getRecordType().getTerm(0).getContent();
                             
                             }
                             if(recwrap.getRecordSource(0).getLegalBodyIDCount() > 0){
                                 this.recWraprecsource = recwrap.getRecordSource(0).getLegalBodyID(0).getContent()+"<br />";
                                 
                                 if(recwrap.getRecordSource(0).getLegalBodyNameCount() > 0){                                    
                                                                         
                                     this.recWraprecsource += getMultilingualContent(recwrap.getRecordSource(0).getLegalBodyName(0).getAppellationValue(0).getLang(), recwrap.getRecordSource(0).getLegalBodyName(0).getAppellationValue(0).getContent())+"<br />";                                
                                 }
                                 
                                 if(recwrap.getRecordSource(0).getLegalBodyWeblinkCount() > 0){
                                     this.recWraprecsource += recwrap.getRecordSource(0).getLegalBodyWeblink(0).getContent();                              
                                 }
                             }
                             
                             if(recwrap.getRecordRightsCount() > 0){
                                 
                                 if(recwrap.getRecordRights(0).getRightsHolderCount() > 0){                                    
                                     if(recwrap.getRecordRights(0).getRightsHolder(0).getLegalBodyIDCount() > 0){
                                         this.recWraprecrights = recwrap.getRecordRights(0).getRightsHolder(0).getLegalBodyID(0).getContent()+"<br />"; 
                                     } 
                                     
                                     if(recwrap.getRecordRights(0).getRightsHolder(0).getLegalBodyNameCount() > 0){
                                         if(recwrap.getRecordRights(0).getRightsHolder(0).getLegalBodyName(0).getAppellationValueCount() > 0){
                                             this.recWraprecrights += getMultilingualContent(recwrap.getRecordRights(0).getRightsHolder(0).getLegalBodyName(0).getAppellationValue(0).getLang(), recwrap.getRecordRights(0).getRightsHolder(0).getLegalBodyName(0).getAppellationValue(0).getContent())+"<br />"; 
                                         }
                                         
                                     }
                                     
                                     if(recwrap.getRecordRights(0).getRightsHolderCount() > 0){
                                         if(recwrap.getRecordRights(0).getRightsHolder(0).getLegalBodyWeblinkCount() > 0){
                                             this.recWraprecrights += recwrap.getRecordRights(0).getRightsHolder(0).getLegalBodyWeblink(0).getContent()+"<br />"; 
                                         }
                                     }
                                 }
                             }
                         }
                     }
                     /*get ResourceWrap*/
                     ResourceWrap rwap = admindata.getResourceWrap();
                     if (rwap != null) {
                         if (rwap.getResourceSetCount() > 0) {
                             log.info("getResourceSetCount called..."+rwap.getResourceSetCount() );
                             for(int rsc = 0; rsc<rwap.getResourceSetCount(); rsc++){                                
                                 //ResourceSet rset = rwap.getResourceSet(0);
                                 ResourceSet rset = rwap.getResourceSet(rsc);
                                 if (rset.getResourceRepresentationCount() > 0) {
                                     log.info("getResourceRepresentationCount() called..."+rset.getResourceRepresentationCount());
                                     int adminresourcescount = rset.getResourceRepresentationCount();
                                     for(int arc = 0; arc<adminresourcescount; arc++){
 
                                         log.info("Admin resource no.:"+arc);
                                         //get Resource representation set
                                         ResourceRepresentation rrep = rset.getResourceRepresentation(arc);
 
                                         //create new admin resource 
                                         AdminResource aResource = new AdminResource();
                                         
 
                                         //get resource link
                                         LinkResource linkres =  rrep.getLinkResource();
                                         if(arc == 0 && rsc == 0){ //assign resLink                                        
                                             this.reslink = linkres.getContent();
                                         }
                                         aResource.resourceLink = linkres.getContent();
                                         log.info("aResource.resourceLink: "+aResource.resourceLink);
 
                                         //get into ResourceMeasurementsSet
                                         if(rrep.getResourceMeasurementsSetCount()>0){
                                             ResourceMeasurementsSet mset = rrep.getResourceMeasurementsSet(arc);
                                             MeasurementType mtype = mset.getMeasurementType(0);
                                             aResource.measurementType = getMultilingualContent(mtype.getLang(),mtype.getContent());
                                             log.info("mtype:"+aResource.measurementType);
 
                                             MeasurementUnit mUnit = mset.getMeasurementUnit(0);
                                             aResource.measurementUnit = mUnit.getContent();
                                             log.info("munit:"+aResource.measurementUnit);
 
                                             MeasurementValue mValue = mset.getMeasurementValue();
                                             aResource.measurementValue = mValue.getContent();
                                             log.info("mvalue:"+aResource.measurementValue);
                                             
                                         }
                                         
                                         //add it in the list!                                    
                                             this.adminResources.add(aResource);
                                         
                                     }
 
                                     /*ResourceRepresentation rrep = rset.getResourceRepresentation(0);
                                     LinkResource linkres =  rrep.getLinkResource();
                                     this.reslink = linkres.getContent();
 
                                     ResourceMeasurementsSet mset = rrep.getResourceMeasurementsSet(0);
                                     MeasurementType mtype = mset.getMeasurementType(0);
                                     this.measurementType = mtype.getContent();
                                     */
                                 }//ResourceRepresentationCount()>0                            
                             }                            
                         }//ResourceSetCount()>0
                     }//ResourceWrap!=null
                 //}//baselang found
             }//for-master iteration
             
 
         }//lido!=null
     }//EoM initFields
     
     private void detectLanguages() {
         
         getLog().info("LidoBean.detectLanguages() called");
         
         //open xml file and 
         //String langstring = "<lido:descriptiveMetadata xml:lang = \'deuh\'>";
         //this.langstring = xmlstring;
         String langarray[];
         
         Pattern pattern = Pattern.compile("xml:lang(\\s)*=(\\s)*[\"\'].*[\"\']");
         //Pattern pattern = Pattern.compile("deu");
         Matcher matcher = pattern.matcher(this.xmlstring);
 
         List<String> listMatches = new ArrayList<String>();
 
         while(matcher.find())
         {
             listMatches.add(matcher.group(0));
         }
 
         for(String s : listMatches)
         {
             langarray = s.split("[\"\']");
             this.languagesTree.add(langarray[1]);
         }
         
         //parse xml and search for xml:lang attribute
         /*if (getLido() != null) {
             
             int admincount = getLido().getLidoAdministrativeMetadataCount();            
             for (int ac = 0; ac < admincount; ac++) {
                 LidoAdministrativeMetadata admindata = getLido().getLidoAdministrativeMetadata(ac);
                 if (admindata.getLang().trim()!= null) {
                     this.languagesTree.add(admindata.getLang().trim());
                 }
             }
             
             int descount = getLido().getLidoDescriptiveMetadataCount();
             for (int dc = 0; dc < descount; dc++) {
                 LidoDescriptiveMetadata descrdata = getLido().getLidoDescriptiveMetadata(dc);
                 if (descrdata.getLang().trim()!= null) {
                     this.languagesTree.add(descrdata.getLang().trim());
                 } 
             }
             
             this.languagesList.addAll(this.languagesTree);
         }*/
         this.languagesList.addAll(this.languagesTree);
         
         
     }//EoM detectLanguages
     
     private String readFile( String file ) throws IOException {
         
         BufferedReader reader = new BufferedReader( new FileReader (file));
         String         line = null;
         StringBuilder  stringBuilder = new StringBuilder();
         String         ls = System.getProperty("line.separator");
 
         while( ( line = reader.readLine() ) != null ) {
             stringBuilder.append( line );
             stringBuilder.append( ls );
         }
 
         return stringBuilder.toString();
 }
     
     /**
      * @return the lido
      */
     public LidoElement getLido() {
         return lidoelement;
     }
 
     /**
      * @param lido the lido to set
      */
     public void setLido(LidoElement lidoelement) {
         this.lidoelement = lidoelement;
     }
 //----------------------------------------------------
 
     /**
      * @return the res
      */
     public ResourceBundle getRes() {
         return res;
     }
 
     /**
      * @param res the res to set
      */
     public void setRes(ResourceBundle res) {
         this.res = res;
     }
 
     /**
      * @return the id
      */
     public String getId() {
         return id;
     }
 
     /**
      * @param id the id to set
      */
     public void setId(String id) {
         this.id = id;
     }
 
     /**
      * @return the baselang
      */
     public String getBaselang() {
         return baselang;
     }
 
     /**
      * @param baselang the baselang to set
      */
     public void setBaselang(String baselang) {
         this.baselang = baselang;
     }
 
     /**
      * @return the titlelbl
      */
     public String getTitlelbl() {
         return titlelbl;
     }
 
     /**
      * @param titlelbl the titlelbl to set
      */
     public void setTitlelbl(String titlelbl) {
         this.titlelbl = titlelbl;
     }
 
     /**
      * @return the title
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * @param title the title to set
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     /**
      * @return the recordidlbl
      */
     public String getRecordidlbl() {
         return recordidlbl;
     }
 
     /**
      * @param recordidlbl the recordidlbl to set
      */
     public void setRecordidlbl(String recordidlbl) {
         this.recordidlbl = recordidlbl;
     }
 
     /**
      * @return the recordid
      */
     public String getRecordid() {
         return recordid;
     }
 
     /**
      * @param recordid the recordid to set
      */
     public void setRecordid(String recordid) {
         this.recordid = recordid;
     }
 
     /**
      * @return the recsourcelbl
      */
     public String getRecsourcelbl() {
         return recsourcelbl;
     }
 
     /**
      * @param recsourcelbl the recsourcelbl to set
      */
     public void setRecsourcelbl(String recsourcelbl) {
         this.recsourcelbl = recsourcelbl;
     }
 
     /**
      * @return the recsource
      */
     public String getRecsource() {
         return recsource;
     }
 
     /**
      * @param recsource the recsource to set
      */
     public void setRecsource(String recsource) {
         this.recsource = recsource;
     }
 
     /**
      * @return the rectypelbl
      */
     public String getRectypelbl() {
         return rectypelbl;
     }
 
     /**
      * @param rectypelbl the rectypelbl to set
      */
     public void setRectypelbl(String rectypelbl) {
         this.rectypelbl = rectypelbl;
     }
 
     /**
      * @return the rectype
      */
     public String getRectype() {
         return rectype;
     }
 
     /**
      * @param rectype the rectype to set
      */
     public void setRectype(String rectype) {
         this.rectype = rectype;
     }
 
     /**
      * @return the recpreflbl
      */
     public String getRecpreflbl() {
         return recpreflbl;
     }
 
     /**
      * @param recpreflbl the recpreflbl to set
      */
     public void setRecpreflbl(String recpreflbl) {
         this.recpreflbl = recpreflbl;
     }
 
     /**
      * @return the recpref
      */
     public String getRecpref() {
         return recpref;
     }
 
     /**
      * @param recpref the recpref to set
      */
     public void setRecpref(String recpref) {
         this.recpref = recpref;
     }
 
     /**
      * @return the recenclbl
      */
     public String getRecenclbl() {
         return recenclbl;
     }
 
     /**
      * @param recenclbl the recenclbl to set
      */
     public void setRecenclbl(String recenclbl) {
         this.recenclbl = recenclbl;
     }
 
     /**
      * @return the recenc
      */
     public String getRecenc() {
         return recenc;
     }
 
     /**
      * @param recenc the recenc to set
      */
     public void setRecenc(String recenc) {
         this.recenc = recenc;
     }
 
     /**
      * @return the reclabellbl
      */
     public String getReclabellbl() {
         return reclabellbl;
     }
 
     /**
      * @param reclabellbl the reclabellbl to set
      */
     public void setReclabellbl(String reclabellbl) {
         this.reclabellbl = reclabellbl;
     }
 
     /**
      * @return the reclabel
      */
     public String getReclabel() {
         return reclabel;
     }
 
     /**
      * @param reclabel the reclabel to set
      */
     public void setReclabel(String reclabel) {
         this.reclabel = reclabel;
     }
 
     /**
      * @return the reslink
      */
     public String getReslink() {
         return reslink;
     }
 
     /**
      * @param reslink the reslink to set
      */
     public void setReslink(String reslink) {
         this.reslink = reslink;
     }
 
     public String getReslinklbl() {
         return reslinklbl;
     }
 
     public void setReslinklbl(String reslinklbl) {
         this.reslinklbl = reslinklbl;
     }
     
     
 
     /**
      * @return the otitlelbl
      */
     public String getOtitlelbl() {
         return otitlelbl;
     }
 
     /**
      * @param otitlelbl the otitlelbl to set
      */
     public void setOtitlelbl(String otitlelbl) {
         this.otitlelbl = otitlelbl;
     }
 
     /**
      * @return the otitle
      */
     public String getOtitle() {
         return otitle;
     }
 
     /**
      * @param otitle the otitle to set
      */
     public void setOtitle(String otitle) {
         this.otitle = otitle;
     }
 
     /**
      * @return the typelbl
      */
     public String getTypelbl() {
         return typelbl;
     }
 
     /**
      * @param typelbl the typelbl to set
      */
     public void setTypelbl(String typelbl) {
         this.typelbl = typelbl;
     }
 
     /**
      * @return the type
      */
     public String getType() {
         return type;
     }
 
     /**
      * @param type the type to set
      */
     public void setType(String type) {
         this.type = type;
     }
 
     /**
      * @return the subconceptterms
      */
     public List getSubconceptterms() {
         return subconceptterms;
     }
 
     /**
      * @param subconceptterms the subconceptterms to set
      */
     public void setSubconceptterms(List subconceptterms) {
         this.subconceptterms = subconceptterms;
     }
 
     /**
      * @return the events
      */
     public List getEvents() {
         return events;
     }
 
     /**
      * @param events the events to set
      */
     public void setEvents(List events) {
         this.events = events;
     }
 
     public String getMeasurementType() {
         return measurementType;
     }
 
     public void setMeasurementType(String measurementType) {
         this.measurementType = measurementType;
     }
 
     public String getMeasurementUnit() {
         return measurementUnit;
     }
 
     public void setMeasurementUnit(String measurementUnit) {
         this.measurementUnit = measurementUnit;
     }
 
     public String getMeasurementValue() {
         return measurementValue;
     }
 
     public void setMeasurementValue(String measurementValue) {
         this.measurementValue = measurementValue;
     }
 
     public String getMeasurementlbl() {
         return measurementlbl;
     }
 
     public void setMeasurementlbl(String measurementlbl) {
         this.measurementlbl = measurementlbl;
     }
 
     public List getAdminResources() {
         return adminResources;
     }
 
     public void setAdminResources(List adminResources) {
         this.adminResources = adminResources;
     }
 
     public String getCategoryterm() {
         return categoryterm;
     }
 
     public void setCategoryterm(String categoryterm) {
         this.categoryterm = categoryterm;
     }
 
     public String getConceptid() {
         return conceptid;
     }
 
     public void setConceptid(String conceptid) {
         this.conceptid = conceptid;
     }
 
     public String getCategorytermlbl() {
         return categorytermlbl;
     }
 
     public void setCategorytermlbl(String categorytermlbl) {
         this.categorytermlbl = categorytermlbl;
     }
 
     public String getConceptidlbl() {
         return conceptidlbl;
     }
 
     public void setConceptidlbl(String conceptidlbl) {
         this.conceptidlbl = conceptidlbl;
     }
 
     public String getRepositoryname() {
         return repositoryname;
     }
 
     public void setRepositoryname(String repositoryname) {
         this.repositoryname = repositoryname;
     }
 
     public String getRepositoryworkid() {
         return repositoryworkid;
     }
 
     public void setRepositoryworkid(String repositoryworkid) {
         this.repositoryworkid = repositoryworkid;
     }
 
     public String getRepositorynamelbl() {
         return repositorynamelbl;
     }
 
     public void setRepositorynamelbl(String repositorynamelbl) {
         this.repositorynamelbl = repositorynamelbl;
     }
 
     public String getRepositoryworkidlbl() {
         return repositoryworkidlbl;
     }
 
     public void setRepositoryworkidlbl(String repositoryworkidlbl) {
         this.repositoryworkidlbl = repositoryworkidlbl;
     }
 
     public String getOmeasurement() {
         return omeasurement;
     }
 
     public void setOmeasurement(String omeasurement) {
         this.omeasurement = omeasurement;
     }
 
     public List getAdminRecWraps() {
         return adminRecWraps;
     }
 
     public void setAdminRecWraps(List adminRecWraps) {
         this.adminRecWraps = adminRecWraps;
     }
 
     public String getRecWraprecid() {
         return recWraprecid;
     }
 
     public void setRecWraprecid(String recWraprecid) {
         this.recWraprecid = recWraprecid;
     }
 
     public String getRecWraprecidlbl() {
         return recWraprecidlbl;
     }
 
     public void setRecWraprecidlbl(String recWraprecidlbl) {
         this.recWraprecidlbl = recWraprecidlbl;
     }
 
     public String getRecWraprecrights() {
         return recWraprecrights;
     }
 
     public void setRecWraprecrights(String recWraprecrights) {
         this.recWraprecrights = recWraprecrights;
     }
 
     public String getRecWraprecrightslbl() {
         return recWraprecrightslbl;
     }
 
     public void setRecWraprecrightslbl(String recWraprecrightslbl) {
         this.recWraprecrightslbl = recWraprecrightslbl;
     }
 
     public String getRecWraprecsource() {
         return recWraprecsource;
     }
 
     public void setRecWraprecsource(String recWraprecsource) {
         this.recWraprecsource = recWraprecsource;
     }
 
     public String getRecWraprecsourcelbl() {
         return recWraprecsourcelbl;
     }
 
     public void setRecWraprecsourcelbl(String recWraprecsourcelbl) {
         this.recWraprecsourcelbl = recWraprecsourcelbl;
     }
 
     public String getRecWraprectype() {
         return recWraprectype;
     }
 
     public void setRecWraprectype(String recWraprectype) {
         this.recWraprectype = recWraprectype;
     }
 
     public String getRecWraprectypelbl() {
         return recWraprectypelbl;
     }
 
     public void setRecWraprectypelbl(String recWraprectypelbl) {
         this.recWraprectypelbl = recWraprectypelbl;
     }
 
     public String getCurrentUrl() {
         return currentUrl;
     }
 
     public void setCurrentUrl(String currentUrl) {
         this.currentUrl = currentUrl;
     }
 
     public List getLanguagesList() {
         return languagesList;
     }
 
     public void setLanguagesList(List languagesList) {
         this.languagesList = languagesList;
     }
 
     public TreeSet<String> getLanguagesTree() {
         return languagesTree;
     }
 
     public void setLanguagesTree(TreeSet<String> languagesTree) {
         this.languagesTree = languagesTree;
     }
 
     public String getXmlstring() {
         return xmlstring;
     }
 
     public void setXmlstring(String xmlstring) {
         this.xmlstring = xmlstring;
     }
 
     public String getSkin() {
         return skin;
     }
 
     public void setSkin(String skin) {
         this.skin = skin;
     }
 
     public String getLogoImage() {
         return logoImage;
     }
 
     public void setLogoImage(String logoImage) {
         this.logoImage = logoImage;
     }
      public String getXmlData() {
         return xmlData;
     }
 
     public void setXmlData(String xmlData) {
         this.xmlData = xmlData;
     }
 
     public LidoWrap getLidowrap() {
         return lidowrap;
     }
 
     public void setLidowrap(LidoWrap lidowrap) {
         this.lidowrap = lidowrap;
     }
 
     public String getRightsWraplegalbodyname() {
         return rightsWraplegalbodyname;
     }
 
     public void setRightsWraplegalbodyname(String rightsWraplegalbodyname) {
         this.rightsWraplegalbodyname = rightsWraplegalbodyname;
     }
 
     public String getRightsWraplegalbodyweblink() {
         return rightsWraplegalbodyweblink;
     }
 
     public void setRightsWraplegalbodyweblink(String rightsWraplegalbodyweblink) {
         this.rightsWraplegalbodyweblink = rightsWraplegalbodyweblink;
     }
 
     public List getAdminRightsHolders() {
         return adminRightsHolders;
     }
 
     public void setAdminRightsHolders(List adminRightsHolders) {
         this.adminRightsHolders = adminRightsHolders;
     }
 
     public String getRightsWraplegalbodynamelbl() {
         return rightsWraplegalbodynamelbl;
     }
 
     public void setRightsWraplegalbodynamelbl(String rightsWraplegalbodynamelbl) {
         this.rightsWraplegalbodynamelbl = rightsWraplegalbodynamelbl;
     }
 
     public String getRightsWraplegalbodyweblinklbl() {
         return rightsWraplegalbodyweblinklbl;
     }
 
     public void setRightsWraplegalbodyweblinklbl(String rightsWraplegalbodyweblinklbl) {
         this.rightsWraplegalbodyweblinklbl = rightsWraplegalbodyweblinklbl;
     }
 
     public String getOdescvaluelbl() {
         return odescvaluelbl;
     }
 
     public void setOdescvaluelbl(String odescvaluelbl) {
         this.odescvaluelbl = odescvaluelbl;
     }
 
     public String getOdescvalue() {
         return odescvalue;
     }
 
     public void setOdescvalue(String odescvalue) {
         this.odescvalue = odescvalue;
     }
 
     public List getLangList() {
         return langList;
     }
 
     public void setLangList(List langList) {
         this.langList = langList;
     }
     
     
     
     public String getMultilingualContent(String language, String lidoElementContent){
         log.info("getMultilingualContent:"+language+" "+lidoElementContent);
         //if language is the same, display value
         if (language == null || language != null && language.trim().equalsIgnoreCase(baselang)) {
             
             return lidoElementContent;
         
         }else{
         
             return lidoElementContent+"("+language+")";
         }
     
     
     }
     
     
 
    
     
     
     
     
     
 
 
 }//EoC
