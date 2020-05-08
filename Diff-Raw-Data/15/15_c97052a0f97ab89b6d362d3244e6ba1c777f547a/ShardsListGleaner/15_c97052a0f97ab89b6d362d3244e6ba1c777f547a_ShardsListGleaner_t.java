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
 import esg.common.generated.whitelist.*;
 import esg.common.util.ESGFProperties;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.StringReader;
 import java.util.Properties;
 import javax.xml.transform.stream.StreamSource;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 import static esg.node.components.registry.NodeTypes.*;
 import static esg.common.Utils.*;
 
 /**
    Description:
    
    Simple xml list generator.  Creates an xml list of search urls
    (shards) for "trusted" federation members.
 
 **/
 public class ShardsListGleaner {
     
     private static final Log log = LogFactory.getLog(ShardsListGleaner.class);
 
     private static final String  regex      = "http[s]?://([^:/]*)(:(?:[0-9]*))?/(.*/)*(.*$)";
     private static final Pattern urlPattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
     private Matcher urlMatcher = null;
 
     private Shards shardlist = null;
     private final String shardsListFile = "esgf_shards.xml";
     private String shardsListPath = null;
     private Properties props = null;
     private String defaultLocation = null;
     private ExclusionListReader.ExclusionList exList = null;
 
     public ShardsListGleaner() { this(null); }
     public ShardsListGleaner(Properties props) { init(); }
 
     private void init() {
         try {
             if(props == null) this.props = new ESGFProperties();
             else this.props = props;
 
             if (null != (shardsListPath = System.getenv().get("ESGF_HOME"))) {
                 shardsListPath = shardsListPath+"/config/";
             }
 
             if (!(new File(shardsListPath).exists())) {
                 log.warn("Could not locate ["+shardsListPath+"] - will try to put in /tmp");
                 shardsListPath = "/tmp/";
                 (new File(shardsListPath)).mkdirs();
             }
                 
             exList = ExclusionListReader.getInstance().getExclusionList().useType(INDEX_BIT);
             shardlist = new Shards();
             urlMatcher = urlPattern.matcher("");
             
         } catch(Exception e) {
             log.error(e);
         }
     }
     
     public Shards getMyShards() { return shardlist; }
     
     public boolean saveShardsList() { return saveShardsList(shardlist); }
     public synchronized boolean saveShardsList(Shards shardlist) {
         boolean success = false;
         if (shardlist == null) {
             log.error("shardlist is null ? ["+shardlist+"]"); 
             return success;
         }
         log.trace("Saving SHARDS list information to "+shardsListPath+shardsListFile);
         try{
             JAXBContext jc = JAXBContext.newInstance(Shards.class);
             Marshaller m = jc.createMarshaller();
             m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
             m.marshal(shardlist, new FileOutputStream(shardsListPath+shardsListFile));
             success = true;
         }catch(Exception e) {
             log.error(e);
         }
 	
         return success;
     }
 
     
     /**
        Looks through the current system and gathers the configured
        node service information.  Takes that information and
        creates a local representation of this node's registration.
     */
     public synchronized ShardsListGleaner appendToMyShardsListFromRegistration(Registration registration) {
         return appendToMyShardsListFromRegistration(registration, false);
     }
     public synchronized ShardsListGleaner appendToMyShardsListFromRegistration(Registration registration, boolean doChecks) {
         log.trace("Creating my SHARDS list representation... "+(doChecks ? "" : "NOT ")+"doing port checks");
         log.trace("Registration is ["+registration+"]");
         try{
             IndexService indexes = null; //The Authorization service entry from registration
 
             //NOTE: Entries stored in the registration are dedup'ed so no worries here ;-)
             int numNodes = registration.getNode().size();
             int indexNodes = 0;
             String endpointBase = null;
             String port = null;
             log.trace("Registration has ("+numNodes+") nodes");
             for(Node node : registration.getNode()) {
                 //TODO - put in sanity check for nodeType integrity
                 indexes = node.getIndexService();
                 if (null == indexes) {
                     log.trace(node.getShortName()+" skipping... does not run an Index service.");
                     continue;
                 }
                 if(exList.isExcluded(node.getHostname())) {
                     log.trace(node.getHostname()+" skipping... found in excludes list!!");
                     continue;
                 }
                try{
                    if(doChecks && !poke(node.getHostname(),
                                         Integer.parseInt(indexes.getPort()), //standard search port 8983
                                         Integer.parseInt(props.getProperty("node.poke.timeout","200")) ) ) { //timeout in millis
                        log.trace(node.getHostname()+" skipping... could not connect to search port!!");
                        continue;
                    }
                }catch(Exception e) {
                    log.error(e);
                    e.printStackTrace();
                    if(log.isTraceEnabled()) e.printStackTrace();
                     continue;
                 }
                 urlMatcher.reset(indexes.getEndpoint());
                 if(urlMatcher.find()) {
                     endpointBase = urlMatcher.group(1);
                     port = ":"+indexes.getPort();
                     shardlist.getValue().add(endpointBase+port+"/solr");
                     indexNodes++;
                 }
             }
             log.trace(indexNodes+" of "+numNodes+" gleaned");
         } catch(Exception e) {
             log.error(e);
             e.printStackTrace();
             if(log.isTraceEnabled()) e.printStackTrace();
         }
         
         return this;
     }
     
     public ShardsListGleaner clear() {
         if(this.shardlist != null) this.shardlist = new Shards();
         return this;
     }
 
     public synchronized ShardsListGleaner loadMyShardsWhitelist() {
         log.info("Loading my SHARDS Whitelist info from "+shardsListPath+shardsListFile);
         try{
             JAXBContext jc = JAXBContext.newInstance(Shards.class);
             Unmarshaller u = jc.createUnmarshaller();
             JAXBElement<Shards> root = u.unmarshal(new StreamSource(new File(shardsListPath+shardsListFile)),Shards.class);
             shardlist = root.getValue();
         }catch(Exception e) {
             log.error(e);
         }
         return this;
     }
 
     //****************************************************************
     // Not really used methods but here for completeness
     //****************************************************************
 
     public Shards createShardsWhitelistFromString(String shardsListContentString) {
         log.info("Loading my SHARDS info from \n"+shardsListContentString+"\n");
         Shards fromContentShardsList = null;
         try{
             JAXBContext jc = JAXBContext.newInstance(Shards.class);
             Unmarshaller u = jc.createUnmarshaller();
             JAXBElement<Shards> root = u.unmarshal(new StreamSource(new StringReader(shardsListContentString)),Shards.class);
             fromContentShardsList = root.getValue();
         }catch(Exception e) {
             log.error(e);
         }
         return fromContentShardsList;
     }
     
 }
