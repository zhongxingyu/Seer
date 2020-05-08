 /***************************************************************************
 *                                                                          *
 *  Organization: Lawrence Livermore National Lab (LLNL)                    *
 *   Directorate: Computation                                               *
 *    Department: Computing Applications and Research                       *
 *      Division: S&T Global Security                                       *
 *        Matrix: Atmospheric, Earth and Energy Division                    *
 *       Program: PCMDI                                                     *
 *       Project: Earth Systems Grid Federation (ESGF) Data Node Software   *
 *  First Author: Gavin M. Bell (gavin@llnl.gov)                            *
 *                                                                          *
 ****************************************************************************
 *                                                                          *
 *   Copyright (c) 2009, Lawrence Livermore National Security, LLC.         *
 *   Produced at the Lawrence Livermore National Laboratory                 *
 *   Written by: Gavin M. Bell (gavin@llnl.gov)                             *
 *   LLNL-CODE-420962                                                       *
 *                                                                          *
 *   All rights reserved. This file is part of the:                         *
 *   Earth System Grid Federation (ESGF) Data Node Software Stack           *
 *                                                                          *
 *   For details, see http://esgf.org/esg-node/                             *
 *   Please also read this link                                             *
 *    http://esgf.org/LICENSE                                               *
 *                                                                          *
 *   * Redistribution and use in source and binary forms, with or           *
 *   without modification, are permitted provided that the following        *
 *   conditions are met:                                                    *
 *                                                                          *
 *   * Redistributions of source code must retain the above copyright       *
 *   notice, this list of conditions and the disclaimer below.              *
 *                                                                          *
 *   * Redistributions in binary form must reproduce the above copyright    *
 *   notice, this list of conditions and the disclaimer (as noted below)    *
 *   in the documentation and/or other materials provided with the          *
 *   distribution.                                                          *
 *                                                                          *
 *   Neither the name of the LLNS/LLNL nor the names of its contributors    *
 *   may be used to endorse or promote products derived from this           *
 *   software without specific prior written permission.                    *
 *                                                                          *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS    *
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT      *
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS      *
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL LAWRENCE    *
 *   LIVERMORE NATIONAL SECURITY, LLC, THE U.S. DEPARTMENT OF ENERGY OR     *
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,           *
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT       *
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF       *
 *   USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND    *
 *   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,     *
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT     *
 *   OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF     *
 *   SUCH DAMAGE.                                                           *
 *                                                                          *
 ***************************************************************************/
 package esg.node.components.registry;
 
 import esg.common.generated.registration.*;
 import esg.common.util.ESGFProperties;
 import esg.common.QuickHash;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.Date;
 import java.util.Properties;
 import java.util.HashMap;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 /**
    Description:
    
    Encapsulates the logic for fetching and generating this local
    node's registration form, as defined by the registration.xsd.  (The
    LasSistersGleaner is also used to further create the
    las_servers.xml derivative file.)
 
 */
 public class RegistrationGleaner {
 
     private static final Log log = LogFactory.getLog(RegistrationGleaner.class);
 
     //NOTE: IF OTHER STATES BECOME APPARENT MAKE AN ENUM...
     public static final String NOT_AVAILABLE = "NOT_AVAILABLE";
     
     private static final String registrationFile = "registration.xml";
     private static final boolean DEBUG=true;
     private String registrationPath = null;
     private Registration myRegistration = null;
     private HashMap<String,Node> myNodeMap = null;
     private QuickHash quickHash = null;
     private String myChecksum = null;
     private Properties props = null;
     private RegistrationGleanerHelperDAO helperDAO = null;
     private String configDir = null;
     private String nodeTypeValue = "-1"; //TODO: yes, yes... turn this into enums strings in xsd - later.
     private boolean dirty = false;
 
     public RegistrationGleaner() { this(null); }
     public RegistrationGleaner(Properties props) { 
         this.props = props;
         this.init(); 
     }
 
     public void init() {
         try {
             if(props == null) this.props = new ESGFProperties();
         } catch(Exception e) {
             log.error(e);
         }
         registrationPath = props.getProperty("node.manager.app.home",".")+File.separator;
         readMyNodeType();
     }
 
     /**
        The node type value returned is a string of an int
        that represents a bit vector corresponding to an installation type
        see the esg-node install script for the values.
        <p>
        DATA_BIT=4<br>
        INDEX_BIT=8<br>
        IDP_BIT=16<br>
        COMPUTE_BIT=32<br>
     */
     public String getMyNodeType() { return nodeTypeValue; }
 
     /**
        Reads the node type from the configuration file that contains it
      */
     private String readMyNodeType() {
         nodeTypeValue = null;
         if (null != (configDir = System.getenv().get("ESGF_HOME"))) {
             configDir = configDir+File.separator+"config";
             try {
                 quickHash = new QuickHash("SHA1");
                 File configTypeFile = new File(configDir+File.separator+"config_type");
                 if(configTypeFile.exists()) {
                     BufferedReader in = new BufferedReader(new FileReader(configTypeFile));
                     try{
                         nodeTypeValue = in.readLine().trim();
                         log.trace("node type = "+nodeTypeValue);
                     }catch(java.io.IOException ex) {
                         log.error(ex);
                     }finally {
                         if(null != in) in.close();
                     }
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
         }
         return nodeTypeValue;
     }
     
     
     public Registration getMyRegistration() { return myRegistration; }
     public String getMyChecksum() { return myChecksum; }
 
     public synchronized boolean saveRegistration() { sync(); return saveRegistration(myRegistration); }
     public synchronized boolean saveRegistration(Registration registration) {
         boolean success = false;
         if (registration == null) {
             log.error("Registration is null ? ["+registration+"]"); 
             return success;
         }
         log.info("Saving registration information to "+ registrationPath+this.registrationFile);
         try{
             JAXBContext jc = JAXBContext.newInstance(Registration.class);
             Marshaller m = jc.createMarshaller();
             m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
             m.marshal(registration, new FileOutputStream(registrationPath+this.registrationFile));
             success = true;
             dirty=false;
         }catch(Exception e) {
             log.error(e);
         }
         
         //-----------------------------------------------
         //Derivative xml file generation... (should be refactored, but in brody mode right now)
         //-----------------------------------------------
 
         //pull from registry to create las sisters file.
         try{
             String endpoint=null;
             if( (null != (endpoint=props.getProperty("las.endpoint"))) &&
                 (new File(props.getProperty("las.app.home"))).exists() ) {
                 LasSistersGleaner lasSisterGleaner = new LasSistersGleaner(props); 
                 log.trace("My LAS endpoint = ["+endpoint+"]");
                 log.trace("Current Registration = ["+registration+"]");
                 log.trace("registration has ("+registration.getNode().size()+") nodes");
                 log.trace("lasSisterGleaner="+lasSisterGleaner);
                 lasSisterGleaner.appendToMyLasServersFromRegistration(registration).saveLasServers();
             }else{
                 log.warn("Could not get las information to save for some reason");
             }
         }catch(Exception e) {
             log.error(e);
             log.trace("props="+props);
             if(log.isTraceEnabled()) e.printStackTrace();
         }
         
         //pull from registry to create idp whilelist file.
         try{
             IdpWhitelistGleaner idpWhitelistGleaner = new IdpWhitelistGleaner(props); 
             log.trace("registration="+registration);
             log.trace("idpWhitelistGleaner="+idpWhitelistGleaner);
             idpWhitelistGleaner.appendToMyIdpWhitelistFromRegistration(registration).saveIdpWhitelist();
         }catch(Exception e) {
             log.error(e);
             log.trace("props="+props);
             if(log.isTraceEnabled()) e.printStackTrace();
         }
 
         //pull from registry to create azs whilelist file.
         try{
             AzsWhitelistGleaner azsWhitelistGleaner = new AzsWhitelistGleaner(props);
             log.trace("registration="+registration);
             log.trace("azsWhitelistGleaner="+azsWhitelistGleaner);
             azsWhitelistGleaner.appendToMyAzsWhitelistFromRegistration(registration).saveAzsWhitelist();
         }catch(Exception e) {
             log.error(e);
             log.trace("props="+props);
             if(log.isTraceEnabled()) e.printStackTrace();
         }
 
         //pull from registry to create ats whilelist file.
         try{
             AtsWhitelistGleaner atsWhitelistGleaner = new AtsWhitelistGleaner(props);
             log.trace("registration="+registration);
             log.trace("atsWhitelistGleaner="+atsWhitelistGleaner);
             atsWhitelistGleaner.appendToMyAtsWhitelistFromRegistration(registration).saveAtsWhitelist();
         }catch(Exception e) {
             log.error(e);
             log.trace("props="+props);
             if(log.isTraceEnabled()) e.printStackTrace();
         }
         
         return success;
     }
 
     //NOTE: In anticipation that checksumming for every call to
     //toString maybe a bit laborious and slow I am breaking up this
     //call into a "regular" toString and on that does the checksum
     //calcuation in addition to that. I don't want to optimize too
     //early, but the nature of toString and how frequenly it is
     //potentially called makes this a relatively justified defensive
     //maneuver, right? :-) The real thing I wanted to do was to put
     //this checksumming in the saveRegistration... however, currently
     //saveRegistration streams to the file and thus not holding the
     //whole file in memory, which for large files may be desireable.
     //However, we are not talking about huge files, Howevever, this
     //call could be done frequenly enough that constantly allocating
     //and gc'ing space for this string may make the JVM not so happy?
     //I am not sure, so I am leaving these options still open.  Time
     //vs. Space...  Thus... optimize this out later.... -gavin
     //public String toCheckedString() {
     //    String out = this.toString();
     //    if(out == null) return out;
     //
     //    myChecksum = quickHash.sum(sw.toString());
     //    log.trace("Checksum of xml string is: "+myChecksum);
     //    return out;
     //}
     //
     //public String toString() {
     //    StringWriter sw = null;
     //    if (myRegistration == null) {
     //        log.error("Registration is ["+myRegistration+"]");
     //        return null;
     //    }
     //    log.info("Writing registration information to String, for "+myRegistration.getNode().get(0).getHostname());
     //    sw = new StringWriter();
     //    try{
     //        JAXBContext jc = JAXBContext.newInstance(Registration.class);
     //        Marshaller m = jc.createMarshaller();
     //        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
     //        m.marshal(myRegistration, sw);
     //    }catch(Exception e) {
     //        log.error(e);
     //    }
     //    return sw.toString();
     //}
 
     public String toString() {
         String out = null;
         if (myRegistration == null) {
             log.error("Registration is ["+myRegistration+"]"); 
             return null;
         }
         log.trace("Writing registration information to String, for "+myRegistration.getNode().get(0).getHostname());
         try{
             StringWriter sw = new StringWriter();
             JAXBContext jc = JAXBContext.newInstance(Registration.class);
             Marshaller m = jc.createMarshaller();
             m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
             m.marshal(myRegistration, sw);
 
             out = sw.toString();
             sync();
             //NOTE: Hopefully this isn't to slow... may have to move
             //to saveRegistration since that would potentially be
             //called much less often
             log.trace("Checksumming xml content...");
             String lastChecksum = myChecksum;
             myChecksum = quickHash.sum(sw.toString());
             dirty = (!myChecksum.equals(lastChecksum));
             log.debug("Checksum of xml string is: "+myChecksum+(dirty ? " (modified)" : " (unchanged"));
         }catch(Exception e) {
             log.error(e);
         }
         return out;
     }
 
     public boolean isDirty() { return dirty; }
     
     /**
        Looks through the current system and gathers the configured
        node service information.  Takes that information and
        creates a local representation of this node's registration.
     */
     public synchronized RegistrationGleaner createMyRegistration() {
         log.info("Creating my registration representation...");
         String endpointDir = null;
         String endpoint = null;
         Node node = new Node();
         long timestamp=(new Date()).getTime();
 
         try{
             String nodeHostname =props.getProperty("esgf.host");
 
             //************************************************
             //CORE
             //************************************************
             
             //Query the user for...
             node.setOrganization(props.getProperty("esg.root.id"));
             node.setLongName(props.getProperty("node.long.name"));
             node.setShortName(props.getProperty("node.short.name"));
             node.setSupportEmail(props.getProperty("mail.admin.address"));
             node.setNamespace(props.getProperty("node.namespace"));
             node.setNodePeerGroup(props.getProperty("node.peer.group"));
             
             //Pulled from system or install
             node.setHostname(nodeHostname);
             node.setIp(props.getProperty("esgf.host.ip"));
             node.setDn(props.getProperty("node.dn")); //zoiks
             node.setNamespace(props.getProperty("node.namespace")); //zoiks
             node.setTimeStamp(timestamp);
             node.setVersion(props.getProperty("version"));
             node.setRelease(props.getProperty("release"));
             node.setNodeType(readMyNodeType());
             node.setDefaultPeer(props.getProperty("esgf.default.peer","pcmdi3.llnl.gov"));
             node.setAdminPeer(props.getProperty("myproxy.endpoint").split(":",2)[0]); //remove port if present
 
             //What is this ?
             CA ca = new CA();
             ca.setEndpoint(props.getProperty("esgf.host","dunno"));
             ca.setHash(props.getProperty("security.ca.hash","dunno"));
             ca.setDn(props.getProperty("security.ca.dn","dunno"));
             node.setCA(ca);
 
             try{
                 if( (null != (endpoint=props.getProperty("node.manager.endpoint"))) &&
                     (new File(props.getProperty("node.manager.app.home"))).exists() ) {
                     NodeManager nodeManager = new NodeManager();
                     nodeManager.setEndpoint(endpoint);
                     node.setNodeManager(nodeManager);
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
 
             //************************************************
             //Data
             //************************************************
 
             try{
                 if( (null != (endpoint=props.getProperty("thredds.endpoint"))) &&
                     (new File(props.getProperty("thredds.app.home"))).exists() ) {
                     ThreddsService tds = new ThreddsService();
                     tds.setEndpoint(endpoint);
                     node.setThreddsService(tds);
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
 
             try{
                 if( (null != (endpoint=props.getProperty("relyingparty.endpoint"))) &&
                     (new File(props.getProperty("relyingparty.app.home"))).exists() ) {
                     RelyingPartyService orp = new RelyingPartyService();
                     orp.setEndpoint(endpoint);
                     node.setRelyingPartyService(orp);
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
 
 
             //------------------------------------------------
             //GEO LOCATION Information (used by dashboard)
             //------------------------------------------------
 
             try{
                 String geoLat = null;
                 String geoLon = null;
                 String city = null;
 
                 //You must at very least have lat and lon set to even set up this entry
                 if( (null != (geoLat=props.getProperty("node.geolocation.lat"))) &&
                    (null != (geoLon=props.getProperty("node.geolocation.lat")))) {
 
                     GeoLocation geoLocation = new GeoLocation();
                     geoLocation.setLat(geoLat);
                     geoLocation.setLon(geoLon);
 
                     if(null != (city=props.getProperty("node.geolocation.city"))) geoLocation.setCity(city);
 
                     node.setGeoLocation(geoLocation);
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
 
 
             //------------------------------------------------
             //GLOBUS SUPPORT TOOLS
             //------------------------------------------------
 
             try{
                 if( (null != (endpoint=props.getProperty("myproxy.endpoint"))) &&
                     (new File(props.getProperty("myproxy.app.home"))).exists() ) { //zoiks
                     MyProxyService mproxy = new MyProxyService();
                     mproxy.setEndpoint(endpoint);
                     mproxy.setDn(props.getProperty("mproxy.dn"));
                     node.setMyProxyService(mproxy);
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
             
             try{
                 if( (null != (endpoint=props.getProperty("gridftp.endpoint"))) &&
                     (new File(props.getProperty("gridftp.app.home"))).exists() ) {
                     GridFTPService gftp = new GridFTPService();
                     gftp.setEndpoint(endpoint);
                     
                     //GridFTPServiceType.REPLICATION (BDM)
                     //GridFTPServiceType.DOWNLOAD (END-USER)
                     String configLabels = null;
                     if( null != (configLabels=props.getProperty("gridftp.config"))) {
                         for(String configLabel : configLabels.split("\\s+")) {
                             Configuration gftpConfig = new Configuration();
                             if(configLabel.equalsIgnoreCase("bdm")) {
                                 gftpConfig.setServiceType(GridFTPServiceType.REPLICATION);
                                 gftpConfig.setPort(props.getProperty("gridftp.bdm.server.port","2812"));
                             }
                             if(configLabel.equalsIgnoreCase("end-user")) {
                                 gftpConfig.setServiceType(GridFTPServiceType.DOWNLOAD);
                                 gftpConfig.setPort(props.getProperty("gridftp.server.port","2811"));//(standard gsiftp port)
                             }
                             gftp.getConfiguration().add(gftpConfig);
                         }
                     }
 
                     node.setGridFTPService(gftp);
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
             
             //************************************************
             //INDEX (search)
             //************************************************
             
             try{
                 if( (null != (endpoint=props.getProperty("index.endpoint"))) &&
                     (new File(props.getProperty("index.app.home"))).exists() ) {
                     IndexService idx = new IndexService();
                     idx.setEndpoint(endpoint);
                     node.setIndexService(idx);
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
 
             try{
                 if( (null != (endpoint=props.getProperty("publishing.service.endpoint"))) &&
                     (new File(props.getProperty("publishing.service.app.home"))).exists() ) {
                     PublishingService pub = new PublishingService();
                     pub.setEndpoint(endpoint);
                     node.setPublishingService(pub);
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
 
             //************************************************
             //COMPUTE
             //************************************************
 
             try{
                 if( (null != (endpoint=props.getProperty("las.endpoint"))) &&
                     (new File(props.getProperty("las.app.home"))).exists() ) {
                     LASService las = new LASService();
                     log.trace("Setting LAS endpoint to "+endpoint);
                     las.setEndpoint(endpoint);
                     log.trace("Setting LAS service to "+las);
                     node.setLASService(las);
                 }else {
                     log.trace("Could not set las information in node ["+node+"]");
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
 
             //************************************************
             //Web Front-End
             //************************************************
 
             //esgf-web-fe
             try{
                 if( (null != (endpoint=props.getProperty("web.fe.service.endpoint"))) &&
                     (new File(props.getProperty("web.fe.app.home"))).exists() ) {
                     FrontEnd webfe = new FrontEnd();
                     webfe.setEndpoint(endpoint);
                     node.setFrontEnd(webfe);
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
 
             //************************************************
             //IDP (security)
             //************************************************
 
             //esgf-idp
             try{
                 if( (null != (endpoint=props.getProperty("idp.service.endpoint"))) &&
                     (new File(props.getProperty("idp.app.home"))).exists() ) {
                     OpenIDProvider openid = new OpenIDProvider();
                     openid.setEndpoint(endpoint);
                     node.setOpenIDProvider(openid);
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
 
             //esgf-security
             try{
                 if( (null != (endpoint=props.getProperty("security.authz.service.endpoint"))) &&
                     (new File(props.getProperty("security.app.home"))).exists() ) {
                     AuthorizationService authzSvc = new AuthorizationService();
                     authzSvc.setEndpoint(endpoint);
                     node.setAuthorizationService(authzSvc);
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
 
             //esgf-security
             try{
                 if( (null != (endpoint=props.getProperty("security.attribute.service.endpoint"))) &&
                     (new File(props.getProperty("security.app.home"))).exists() ) {
                     AttributeService attrSvc = new AttributeService();
                     attrSvc.setEndpoint(endpoint);
                     loadAttributeServiceGroups(attrSvc);
                     node.setAttributeService(attrSvc);
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
 
             //esgf-security
             try{
                 if( (null != (endpoint=props.getProperty("security.registration.service.endpoint"))) &&
                     (new File(props.getProperty("security.app.home"))).exists() ) {
                     RegistrationService regSvc = new RegistrationService();
                     regSvc.setEndpoint(endpoint);
                     node.setRegistrationService(regSvc);
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
 	    
             PEMCert cert = new PEMCert();
             cert.setCert(fetchMyPemCert());
             node.setPEMCert(cert);
             
             
             //************************************************
             //INDEX DEPRECATED SERVICE
             //************************************************
             //OAIRepository oaiRepo = new OAIRepository();
             //oaiRepo.setEndpoint("oairepo-endpoint");
             //node.setOAIRepository(oaiRepo);			
 
         } catch(Exception e) {
             log.error(e);
         }
 
         if(null == myRegistration) myRegistration = new Registration();
         myRegistration.getNode().add(node);
         myRegistration.setTimeStamp(timestamp); //touch'ing the registration...
         return this;
     }
     
     private String fetchMyPemCert() {
         log.trace("Fetching PEM Certificate...");
         
         byte[] buffer = null;
         java.io.BufferedInputStream f = null;
 
         try {
             
             String certfilename = "/etc/grid-security/hostcert.pem";
             File certfile = new File(certfilename);
             if(!certfile.exists()) {
                 log.warn("Could not find "+certfile.getCanonicalPath());
                 return NOT_AVAILABLE;
             }
             
             //NOTE: should do this with NIO buffers and specify the right charset
             //so that the buffer size is accurate... put on list of TOODs -gavin
             buffer = new byte[(int) certfile.length()];
             f = new java.io.BufferedInputStream(new java.io.FileInputStream(certfile));
             f.read(buffer);
         }catch(java.io.IOException ex) {
             log.error(ex);
             buffer = new byte[0];
         } finally {
             if (f != null) try { f.close(); } catch (java.io.IOException ignored) { }
         }
         log.trace("Certificate Fetched!");
         return new String(buffer);
     }
     
     //NOTE: May have to synchronized his method...
     public void sync() {
         log.trace("sync'ing...");
         if(null == myRegistration) return;
         if(null == myNodeMap) myNodeMap = new HashMap<String,Node>();
         myNodeMap.clear();
         for(Node node : myRegistration.getNode()) {
             myNodeMap.put(node.getHostname(),node);
         }
     }
     
     public synchronized RegistrationGleaner loadMyRegistration() throws ESGFRegistryException {
         return loadMyRegistration(registrationPath+this.registrationFile);
     }
 
     public synchronized RegistrationGleaner loadMyRegistration(String filename) throws ESGFRegistryException {
         log.info("Loading my registration info from "+filename);
         try{
             JAXBContext jc = JAXBContext.newInstance(Registration.class);
             Unmarshaller u = jc.createUnmarshaller();
             JAXBElement<Registration> root = u.unmarshal(new StreamSource(new File(filename)),Registration.class);
             myRegistration = root.getValue();
             sync();
         }catch(Exception e) {
             throw new ESGFRegistryException("Unable to properly load local Registration from ["+filename+"]", e);
         }
         return this;
     }
 
     public synchronized boolean removeNode(String nodeHostname) {
         sync();
         if (myRegistration.getNode().remove(myNodeMap.remove(nodeHostname))) {
             dirty = true;
             touch();
         }
         return dirty;
     }
 
     protected void touch() { myRegistration.setTimeStamp((new Date()).getTime()); }
 
     public Registration createRegistrationFromString(String registrationContent) {
         log.trace("Creating registration info from String:\n"+registrationContent+"\n");
         Registration fromContentRegistration = null;
         try{
             JAXBContext jc = JAXBContext.newInstance(Registration.class);
             Unmarshaller u = jc.createUnmarshaller();
             JAXBElement<Registration> root = u.unmarshal(new StreamSource(new StringReader(registrationContent)),Registration.class);
             fromContentRegistration = root.getValue();
         }catch(Exception e) {
             log.error(e);
         }
         return fromContentRegistration;
     }
 
     //Delegate out to our helper so we can get things out of the
     //esgf_security.group database table
     private void loadAttributeServiceGroups(AttributeService attrSvc) {
         //lazy instantiate this guy... Because it is only used when
         //this service is available, which is not on every node, so
         //don't waste memory :-) -gavin
         if (helperDAO == null) {
             helperDAO = new RegistrationGleanerHelperDAO(props);
         }
         helperDAO.loadAttributeServiceGroups(attrSvc);
     }
 
     //Allow this class to be used as a command line tool for bootstrapping this node.
     public static void main(String[] args) {
         if(args.length > 0) {
             if(args[0].equals("bootstrap")) {
                 System.out.println(args[0]+"ing...");
                 (new RegistrationGleaner()).createMyRegistration().saveRegistration();
             }else if(args[0].equals("load")) {
                 System.out.println(args[0]+"ing...");
                 //(new RegistrationGleaner()).loadMyRegistration().saveRegistration();
                 if(args.length == 2){
                     System.out.println((new RegistrationGleaner()).loadMyRegistration(args[1]));
                 }else{
                     System.out.println((new RegistrationGleaner()).loadMyRegistration());
                 }
             }else {
                 System.out.println("illegal arg: "+args[0]);
             }
         }
     }
 
 }
