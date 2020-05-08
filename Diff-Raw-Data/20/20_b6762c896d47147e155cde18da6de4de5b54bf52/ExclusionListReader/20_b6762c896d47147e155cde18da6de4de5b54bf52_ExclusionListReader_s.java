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
 
 import java.io.File;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 import static esg.node.components.registry.NodeTypes.*;
 
 /**
    Description:
 
    Reads exclusion list of nodes not to include in the registry
    (esgf_excludes.xml)
 
 */
 
 public class ExclusionListReader {
     
     private static final Log log = LogFactory.getLog(ExclusionListReader.class);
     private static final String exclusionListFilename = "esgf_excludes.txt";
     private Map<Pattern,Integer> excludePatTypeMap = new ConcurrentHashMap<Pattern,Integer>();
     
     private String configDir = null;
     private String nodeTypeValue = "-1"; //TODO: yes, yes... turn this into enums strings in xsd - later.
 
     private Object lock = new Object();
 
     //---- Singleton ------
     private static ExclusionListReader instance = null;
 
     public static ExclusionListReader getInstance() {
         return (null == instance) ? instance = new ExclusionListReader() : instance;
     }
 
     private ExclusionListReader() { log.info("Instantiating ExclusionListReader..."); }
     //---- Singleton ------
     
     public boolean loadExclusionList() {
         return this.loadExclusionList(exclusionListFilename);
     }
     
     boolean loadExclusionList(String exclusionListFilename_) {
         boolean ret=false;
         synchronized(lock) {
             excludePatTypeMap.clear();
             log.info("loading excusion list "+exclusionListFilename_);
             
             if (null != (configDir = System.getenv().get("ESGF_HOME"))) {
                 configDir = configDir+File.separator+"config";
                 try {
                     File excludesFile = new File(configDir+File.separator+exclusionListFilename_);
                     log.trace(excludesFile);
                     if(excludesFile.exists()) {
                         BufferedReader in = new BufferedReader(new FileReader(excludesFile));
                         try{
                             String entry = null;
                             while(null != (entry = in.readLine())) {
                                 if (entry.isEmpty()) continue;
                                 log.info("exclusion entry = ["+entry+"]");
                                 String[] keyVal = entry.trim().split("\\s|[=]",2);
                                 String key =keyVal[0];
                                 String val = null;
                                 if(keyVal.length == 1) {
                                    val = "ALL";
                                 }else{
                                     val = keyVal[1];
                                 }
                                 addExclusionEntry(key,val);
                             }
                         }catch(java.io.IOException ex) {
                             log.error(ex);
                             ex.printStackTrace();
                         }finally {
                             if(null != in) in.close();
                         }
                     }else{
                         log.warn("Sorry can't find the file "+excludesFile.getAbsolutePath());
                     }
                 }catch(Throwable t) {
                     log.error(t);
                     t.printStackTrace();
                 }
                 ret=true;
             }
         }
         return ret;
     }
     
     
     //-----------------
     // Construction Method
     //-----------------
     //NOTE: Yes, I know this would have been easier using enums... I'll have to read that chapter again.
     //For now I am going with what I know.  Then can come back and make sexy with enums.
     private void addExclusionEntry(String entry, String types) {
         log.trace("Examining entry: ["+entry+"] Types: ["+types+"]");
         int mask=0;
         for(String type : types.split("(\\s+|[,|+])")) {
             log.trace("Type = "+type);
             if (type.equalsIgnoreCase("ALL"))     { mask += ALL_BIT; }
             else if (type.equalsIgnoreCase("DATA"))    { mask += DATA_BIT; }
             else if (type.equalsIgnoreCase("INDEX"))   { mask += INDEX_BIT; }
             else if (type.equalsIgnoreCase("IDP"))     { mask += IDP_BIT; }
             else if (type.equalsIgnoreCase("COMPUTE")) { mask += COMPUTE_BIT; }
             else { log.trace("unmatched type: "+type); }
         }
        if (mask == 0) mask=ALL_BIT;
         log.trace("map entry: ["+entry+"] -> ["+mask+"]");
         excludePatTypeMap.put(Pattern.compile(entry),mask);
         mask=0;
     }
 
     public ExclusionList getExclusionList() { return new ExclusionList(); }
     
     public class ExclusionList {
         
         //-----------------
         // Query Method
         //-----------------
         int type = 0;
 
         public ExclusionList useType(Integer type) { this.type=type; return this; }
         public boolean isExcluded(String input) { return this.isExcluded(input,this.type); }
         public boolean isExcluded(String input, Integer type) {
             synchronized(ExclusionListReader.this.lock) {
                 log.trace("Testing ["+input+", "+type+"]");
                 for(Pattern pat : ExclusionListReader.this.excludePatTypeMap.keySet()) {
                     if(pat.matcher(input).find()) {
                         log.info("Matched ["+input+"] on Regex");
                         int entryType = ((int)ExclusionListReader.this.excludePatTypeMap.get(pat));
                         log.trace("Testing types: entry:"+entryType+" & "+type+" != 0 ?");
                         if (( entryType & type) != 0) {
                             log.trace("GOT A HIT!!!");
                             return true;
                         }
                     }
                 }
             }
             return false;
         }
         
     }
     
     //----------------- 
     // Main
     //-----------------
     public static void main (String[] args) {
         System.out.println("Starting Exlcusion List Reader");
 
         ExclusionListReader elr = ExclusionListReader.getInstance();
         elr.loadExclusionList();
         ExclusionListReader.ExclusionList exList = elr.getExclusionList().useType(INDEX_BIT);
         
         boolean result = exList.isExcluded(args[0]);
         if(result) {
             System.out.println("EXCLUDED: "+args[0]);
         }else{
             System.out.println("INCLUDED: "+args[0]);
         }
         System.out.println("test: "+result);
 
         System.exit(0);
     }
     
 }
