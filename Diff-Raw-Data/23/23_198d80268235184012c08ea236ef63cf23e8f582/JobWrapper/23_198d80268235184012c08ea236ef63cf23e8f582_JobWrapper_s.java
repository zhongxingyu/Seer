 /*
  * Copyright (c) Members of the EGEE Collaboration. 2004. 
  * See http://www.eu-egee.org/partners/ for details on the copyright
  * holders.  
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *     http://www.apache.org/licenses/LICENSE-2.0 
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 
 /*
  * 
  * Authors: Luigi Zangrando <zangrando@pd.infn.it>
  *
  */
 
 package org.glite.ce.creamapi.jobmanagement;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.glite.ce.commonj.utils.CEUtils;
 import org.glite.ce.creamapi.jobmanagement.cmdexecutor.AbstractJobExecutor;
 import org.glite.ce.creamapi.jobmanagement.jdl.OutputDataRecord;
 
 public class JobWrapper {
 	private static final Logger logger = Logger.getLogger(JobWrapper.class.getName());
 
     private static final String DELEGATION_TIME_SLOT_DEFAULT      = "3600"; //sec.
     private static final String COPY_PROXY_MIN_RETRY_WAIT_DEFAULT = "60"; //sec.
     private static final String COPY_RETRY_COUNT_ISB_DEFAULT      = "2";
     private static final String COPY_RETRY_FIRST_WAIT_ISB_DEFAULT = "60"; //sec.
     private static final String COPY_RETRY_COUNT_OSB_DEFAULT      = "6";
     private static final String COPY_RETRY_FIRST_WAIT_OSB_DEFAULT = "300"; //sec.
     
     private static final String JOB_WRAPPER_MPI_TEMPLATE_NAME = "jobwrapper-mpi.tpl";
     private static final String JOB_WRAPPER_TEMPLATE_NAME     = "jobwrapper.tpl";
     private static       Hashtable<String, String> wrapperTemplateHashTable = new Hashtable<String, String>(0);
 
 
     public static String filenameNorm(String str) {
         if (str == null || str.length() == 0)
             return "";
 
         StringBuffer buff = new StringBuffer();
         int start = 0;
         int end = str.length();
 
         if (str.charAt(0) == '"' && str.charAt(end - 1) == '"') {
             start++;
             end--;
         }
 
         for (int k = start; k < end; k++) {
             if (str.charAt(k) == ' ')
                 buff.append("\\ ");
             else
                 buff.append(str.charAt(k));
         }
         return buff.toString();
     }
 
     public static String stringNorm(String str) {
         StringBuffer buff = new StringBuffer("\"");
         for (int k = 0; k < str.length(); k++) {
             if ((str.charAt(k) == '"') && ((k==0) ||((k!=0) &&  (str.charAt(k-1)!='\\')))) {
                 buff.append("\\\"");
             } 
             else if (str.charAt(k) == '$')
                 buff.append("\\$");
             else
                 buff.append(str.charAt(k));
         }
         buff.append("\"");
         return buff.toString();
     }
 
     public static String buildWrapper(Job job) throws IOException {
     	String brokerInfoFile = ".BrokerInfo";
     	StringBuffer wrapper = new StringBuffer("#!/bin/sh -l\n");
     	wrapper.append("__create_subdir=1\n");
     	wrapper.append("export CE_ID=").append(job.getCeId()).append("\n");
 
     	String vo = job.getVirtualOrganization();
     	String gridJobId = job.getGridJobId();
     	String creamJobId = job.getId();
     	String creamURL = job.getCreamURL();
     	String executable = job.getExecutable();
     	String stdi = job.getStandardInput();
     	String stdo = job.getStandardOutput();
     	String stde = job.getStandardError();
     	String loggerDestURI = job.getLoggerDestURI();
     	String tokenURL = job.getTokenURL();
     	int nodes = job.getNodeNumber();
     	String perusalListFileURI = job.getPerusalListFileURI();
     	String perusalFilesDestURI = job.getPerusalFilesDestURI();
     	int perusalTimeInterval = job.getPerusalTimeInterval();
     	String prologue = job.getPrologue();
     	String prologueArgs = job.getPrologueArguments();
     	String epilogue = job.getEpilogue();
     	String epilogueArgs = job.getEpilogueArguments();
         List<String> arguments = job.getArguments();
         
     	String delegationProxyCertSandboxPath = (String)job.getVolatileProperty(AbstractJobExecutor.DELEGATION_PROXY_CERT_SANDBOX_URI);
         
     	if (creamJobId == null) {
     		throw new IllegalArgumentException("Missing cream job id");
     	}
 
     	if (creamURL == null) {
     		throw new IllegalArgumentException("Missing cream url");
     	}
     	String creamJobIdURI = creamURL.substring(0, creamURL.indexOf("ce-cream")) + creamJobId;
     	
     	String copyRetryCountISB =  JobWrapper.COPY_RETRY_COUNT_ISB_DEFAULT;
        	if (job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_COPY_RETRY_COUNT_ISB) != null) {
        		try{
        			Integer.parseInt((String)job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_COPY_RETRY_COUNT_ISB));
        			copyRetryCountISB = (String)job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_COPY_RETRY_COUNT_ISB);
        		} catch (NumberFormatException nfe){
        			logger.warn("JOB_WRAPPER_COPY_RETRY_COUNT_ISB must be integer. So it'll be replaced with the default value:" + JobWrapper.COPY_RETRY_COUNT_ISB_DEFAULT);
        		}
        	}
     	
     	String copyRetryFirstWaitISB =  JobWrapper.COPY_RETRY_FIRST_WAIT_ISB_DEFAULT;
         if (job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_ISB) != null) {
             try{
                 Integer.parseInt((String)job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_ISB));
                 copyRetryFirstWaitISB = (String)job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_ISB);
             } catch (NumberFormatException nfe){
                 logger.warn("JOB_WRAPPER_COPY_FIRST_WAIT_ISB must be integer. So it'll be replaced with the default value: JobWrapper.COPY_RETRY_FIRST_WAIT_ISB_DEFAULT");
             }
         }
          
         String copyRetryCountOSB =  JobWrapper.COPY_RETRY_COUNT_OSB_DEFAULT;
         if (job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_COPY_RETRY_COUNT_OSB) != null) {
             try{
                 Integer.parseInt((String)job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_COPY_RETRY_COUNT_OSB));
                 copyRetryCountOSB = (String)job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_COPY_RETRY_COUNT_OSB);
             } catch (NumberFormatException nfe){
                 logger.warn("JOB_WRAPPER_COPY_RETRY_COUNT_OSB must be integer. So it'll be replaced with the default value:" + JobWrapper.COPY_RETRY_COUNT_OSB_DEFAULT);
             }
         }
         
         String copyRetryFirstWaitOSB =  JobWrapper.COPY_RETRY_FIRST_WAIT_OSB_DEFAULT;
         if (job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_OSB) != null) {
             try{
                 Integer.parseInt((String)job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_OSB));
                 copyRetryFirstWaitOSB = (String)job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_OSB);
             } catch (NumberFormatException nfe){
                 logger.warn("JOB_WRAPPER_COPY_FIRST_WAIT_OSB must be integer. So it'll be replaced with the default value: JobWrapper.COPY_RETRY_FIRST_WAIT_OSB_DEFAULT");
             }
         }
     	
     	String copyProxyMinRetryWait =  JobWrapper.COPY_PROXY_MIN_RETRY_WAIT_DEFAULT;
     	if (job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT) != null) {
     		try{
     			Integer.parseInt((String)job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT));
     			copyProxyMinRetryWait = (String)job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT);
     		} catch (NumberFormatException nfe){
     			logger.warn("JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT must be integer. So it'll be replaced with the default value:" + JobWrapper.COPY_PROXY_MIN_RETRY_WAIT_DEFAULT);
     		}
     	}
 
     	String delegationTimeSlot =  null;
     	String delegationProxyCertSandboxFileName = null;
 
     	if ((delegationProxyCertSandboxPath == null) || ("".equals(delegationProxyCertSandboxPath))) {
     		//no ProxyRenewal
     		logger.debug("No PROXY_RENEWAL for jobid= " + job.getId());
     		delegationTimeSlot = "-1";
     	} else {
     		delegationTimeSlot =  JobWrapper.DELEGATION_TIME_SLOT_DEFAULT;
     		if (job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_DELEGATION_TIME_SLOT) != null) {
     			try{
     				Integer.parseInt((String)job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_DELEGATION_TIME_SLOT));
     				delegationTimeSlot = (String)job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_DELEGATION_TIME_SLOT);
     			} catch (NumberFormatException nfe){
     				logger.warn("JOB_WRAPPER_DELEGATION_TIME_SLOT must be integer. So it'll be replaced with the default value: " + JobWrapper.DELEGATION_TIME_SLOT_DEFAULT);
     			}
     		}
     		delegationProxyCertSandboxFileName = delegationProxyCertSandboxPath.substring(delegationProxyCertSandboxPath.lastIndexOf('/')) +
     		creamJobId.substring(5);
     		wrapper.append("export __delegationProxyCertSandboxPath=").append(delegationProxyCertSandboxPath).append("\n");
     		wrapper.append("export __delegationProxyCertSandboxPathTmp=").append("/tmp" + delegationProxyCertSandboxFileName).append("\n");
     	}
 
     	if (gridJobId == null || gridJobId.length() == 0){
     		gridJobId = "\"\"";
     	}
     	if (executable == null || executable.equals("")) {
     		throw new IllegalArgumentException("Missing executable");
     	}
     	if (nodes == 0 && job.isMpich()) {
     		throw new IllegalArgumentException("Missing node number for mpich job");
     	}
     	if (!executable.startsWith("/") && !executable.startsWith("./")) {
     		executable = "./" + executable;
     	}
     	if (loggerDestURI == null || loggerDestURI.length() == 0) {
     		loggerDestURI = "\"\"";
     	}
     	String tokenHost = null;
     	String tokenPath = null;
     	if (tokenURL == null || tokenURL.length() == 0) {
     		tokenURL = "\"\"";
     		tokenHost = "\"\"";
     		tokenPath = "\"\"";
     	} else {
     		try {
     			URI tmpuri = new URI(tokenURL);
     			tokenHost = tmpuri.getHost();
     			if (tmpuri.getPort() > 0)
     				tokenHost = tokenHost + ":" + tmpuri.getPort();
     			tokenPath = tmpuri.getPath();
     		} catch (Exception ex) {
     			throw new IOException("Wrong url format: " + tokenURL);
     		}
     	}
 
     	wrapper.append("export GRID_JOBID=").append(gridJobId).append("\n");
     	wrapper.append("export CREAM_JOBID=").append(creamJobIdURI).append("\n");
     	wrapper.append("__brokerinfo=").append(brokerInfoFile).append("\n");
     	wrapper.append("__vo=").append(vo).append("\n");
     	wrapper.append("__gridjobid=").append(gridJobId).append("\n");
     	wrapper.append("__creamjobid=").append(creamJobId).append("\n");
     	wrapper.append("__executable=").append(executable).append("\n");
     	wrapper.append("__working_directory=");
     	wrapper.append(creamJobId.substring(creamJobId.indexOf("CREAM")));
     	wrapper.append("\n__wms_hostname=");
     	
     	if (job.getVolatileProperty(Job.WMS_HOSTNAME) != null) {
     	    wrapper.append(job.getVolatileProperty(Job.WMS_HOSTNAME));
     	}
 
     	wrapper.append("\n__ce_hostname=");
     	try {
     	    wrapper.append(InetAddress.getLocalHost().getCanonicalHostName());
     	} catch (UnknownHostException uhEx) {
     	    //do nothing.
     	}
     	wrapper.append("\n");
 
     	StringBuffer cmdLine = new StringBuffer();
     	if (job.isInteractive()) {
     	    cmdLine.append("./glite-wms-job-agent $BYPASS_SHADOW_HOST $BYPASS_SHADOW_PORT \"");
     	    cmdLine.append(executable).append(" ");
     	    cmdLine.append(arguments).append(" $*\"");
     	} else {
     	    cmdLine.append("\"").append(executable).append("\" ");
     	    for(String argument : arguments) {
     	        if(argument != null) {
     	            cmdLine.append(argument);
     	        }
     	    }
     	    cmdLine.append("$* ");
 
     	    if (stdi != null && !stdi.equals("")) {
     	        cmdLine.append("< \"").append(stdi).append("\"");
     		}
 
     		if (stdo != null && !stdo.equals("")) {
     			cmdLine.append(" > \"").append(stdo).append("\"");
     			wrapper.append("__stdout_file=\"").append(stdo).append("\"\n");
     		} else {
     			cmdLine.append(" > /dev/null ");
     		}
 
     		if (stde != null && !stde.equals("")) {
     			cmdLine.append(stde.equals(stdo) ? " 2>&1" : " 2> \"" + stde + "\"");
     			wrapper.append("__stderr_file=\"").append(stde).append("\"\n");
     		} else {
     			cmdLine.append(" 2> /dev/null");
     		}
     	}
 
     	wrapper.append("__cmd_line=").append(stringNorm(cmdLine.toString())).append("\n");
     	wrapper.append("__logger_dest=").append(loggerDestURI).append("\n");
     	wrapper.append("__token_file=").append(tokenURL).append("\n");
     	wrapper.append("__token_hostname=").append(tokenHost).append("\n");
     	wrapper.append("__token_fullpath=").append(tokenPath).append("\n");
     	wrapper.append("__nodes=").append(nodes).append("\n");
     	wrapper.append("export __delegationTimeSlot=").append(delegationTimeSlot).append("\n");
     	wrapper.append("export __copy_proxy_min_retry_wait=").append(copyProxyMinRetryWait).append("\n");    	
     	wrapper.append("__copy_retry_count_isb=").append(copyRetryCountISB).append("\n");
     	wrapper.append("__copy_retry_first_wait_isb=").append(copyRetryFirstWaitISB).append("\n");
         wrapper.append("__copy_retry_count_osb=").append(copyRetryCountOSB).append("\n");
         wrapper.append("__copy_retry_first_wait_osb=").append(copyRetryFirstWaitOSB).append("\n");
 
     	if (perusalFilesDestURI != null) {
     		if (perusalFilesDestURI == null || perusalTimeInterval < 1) {
     			throw new IllegalArgumentException("Missing perusal parameters");
     		}
     		
     		wrapper.append("__perusal_filesdesturi=").append(perusalFilesDestURI).append("\n");
     		wrapper.append("__perusal_listfileuri=").append(perusalListFileURI).append("\n");
     		wrapper.append("__perusal_timeinterval=").append(perusalTimeInterval).append("\n");
     	}
 
     	if (prologue != null && prologue.length() > 0) {
     		if (!prologue.startsWith("/"))
     			prologue = "./" + prologue;
     		wrapper.append("__prologue=\"").append(prologue).append("\"\n");
     		if (prologueArgs == null)
     			prologueArgs = "";
     		wrapper.append("__prologue_arguments=\"").append(prologueArgs).append("\"\n");
     	}
 
     	if (epilogue != null && epilogue.length() > 0) {
     		if (!epilogue.startsWith("/"))
     			epilogue = "./" + epilogue;
     		wrapper.append("__epilogue=\"").append(epilogue).append("\"\n");
     		if (epilogueArgs == null)
     			epilogueArgs = "";
     		wrapper.append("__epilogue_arguments=\"").append(epilogueArgs).append("\"\n");
     	}
 
     	// environment
     	wrapper.append("declare -a __environment\n\n");
     	int counter = 0;
 
     	if (job.getHlrLocation() != null) {
     		wrapper.append("__environment[0]=\"HLR_LOCATION=");
     		wrapper.append(job.getHlrLocation()).append("\"\n");
     		counter++;
     	}
 
     	Hashtable<String, String> env = job.getEnvironment();
     	if (env != null) {
     		Enumeration<String> allKeys = env.keys();
     		while (allKeys.hasMoreElements()) {
     			String key = allKeys.nextElement();
     			wrapper.append("__environment[" + counter + "]=");
     			wrapper.append(stringNorm(key + "=" + env.get(key)));
     			wrapper.append("\n");
     			counter++;
     		}
     	}
 
     	// isb
     	List<String> fileNames = job.getInputFiles();
 
     	if (job.isInteractive()) {
             fileNames.add("gsiftp://${__ce_hostname}/${GLITE_WMS_LOCATION}/bin/glite-wms-pipe-input");
             fileNames.add("gsiftp://${__ce_hostname}/${GLITE_WMS_LOCATION}/bin/glite-wms-pipe-output");
             fileNames.add("gsiftp://${__ce_hostname}/${GLITE_WMS_LOCATION}/bin/glite-wms-job-agent");
             fileNames.add("gsiftp://${__ce_hostname}/${GLITE_WMS_LOCATION}/lib/libglite-wms-grid-console-agent.so.0");
     	}
 
     	if (fileNames.size() > 0) {
     		String prefix = job.getInputSandboxBaseURI();
     		logger.debug("getInputSandboxBaseURI = " + prefix);
     		
     		wrapper.append("declare -a __input_file_url\n");
     		wrapper.append("declare -a __input_file_dest\n");
     		wrapper.append("declare -a __input_transfer_cmd\n");
 
     		String dnFromPrefix = null;
     		String dnForFile = null; 
     		String fName = null;
     		
     		if (prefix == null) { 
     			prefix = "";
     		} else {
     			if (prefix.indexOf("?DN=") != -1) {
     				dnFromPrefix = prefix.substring(prefix.indexOf("?DN=") + 4);
     				prefix = CEUtils.getURIWithoutDN(prefix);
     			}
     			if (!prefix.endsWith("/")) {
     				prefix = prefix + "/";
     			}
     		}
     		
     		logger.debug("DN (getInputSandboxBaseURI)=" + dnFromPrefix);
 
     		int index = 0;
             
             for (String fileName : fileNames) {
                 dnForFile = null;
                 fName = fileName;
     			
     			if ((fName.startsWith("gsiftp") && (fName.indexOf("?DN=") != -1))) {
     				dnForFile = fName.substring(fName.indexOf("?DN=") + 4);
     				fName = CEUtils.getURIWithoutDN(fName);
     			}
 
     			fName = filenameNorm(fName);
     			if (fName.indexOf("://") < 0) {
     				if (fName.startsWith("/") || prefix.length() == 0) {
     					fName = "file://" + fName;
     				} else {
     					fName = prefix + fName;
     					dnForFile = dnFromPrefix;
     				}
     			}
 
     			String pName = fName.substring(fName.lastIndexOf("/") + 1);
     			if (fName.startsWith("file://")) {    				
     				String tmpURI = job.getCREAMInputSandboxURI();
     				if (tmpURI == null || tmpURI.equalsIgnoreCase("N/A")) {
     					throw new IllegalArgumentException("Missing CREAMInputSandboxURI");
     				}
     				fName = tmpURI + "/" + pName;
     			}
     			
     			wrapper.append("__input_file_url[").append(index).append("]=");
     			wrapper.append(stringNorm(fName)).append("\n");
     			wrapper.append("__input_file_dest[").append(index).append("]=");
     			wrapper.append(stringNorm(pName)).append("\n");
 
     			if (fName.startsWith("gsiftp")) {
     				if ((dnForFile != null) && (!"".equals(dnForFile.trim()))) {
     					wrapper.append("__input_transfer_cmd[").append(index).append("]=\"\\${globus_transfer_cmd} -ss \\\"" + dnForFile.trim() + "\\\" \"\n");
     				} else {
     					wrapper.append("__input_transfer_cmd[").append(index).append("]=\"\\${globus_transfer_cmd}\"\n");
     				}
     			} else if (fName.startsWith("file")) {
     				wrapper.append("__input_transfer_cmd[").append(index).append("]=\"\\${globus_transfer_cmd}\"\n");
     			} else if (fName.startsWith("https")) {
     				wrapper.append("__input_transfer_cmd[").append(index).append("]=\"\\${https_transfer_cmd}\"\n");
     			} else {
     				throw new IllegalArgumentException("Unsupported protocol");
     			}
 
     			index++;
     		}
 
     	}
     	wrapper.append("\n");
 
     	//isb lrms
     	 if (job.getVolatileProperty(AbstractJobExecutor.LRMS_INPUT_FILES) != null) {
     	     List<String> lrmsInputFileNames = (List<String>)job.getVolatileProperty(AbstractJobExecutor.LRMS_INPUT_FILES); 
              if (lrmsInputFileNames.size() > 0) { 
     	        wrapper.append("declare -a __lrms_input_file\n");
     	        
     	        for (int index=0; index<lrmsInputFileNames.size(); index++) {
     	            wrapper.append("__lrms_input_file[").append(index).append("]=");
                     wrapper.append(stringNorm(lrmsInputFileNames.get(index))).append("\n");
     	        }
              }
     	 }
     	 wrapper.append("\n");
     	
     	// osb
     	String maxOutputSandboxSize = (String)job.getVolatileProperty(Job.MAX_OUTPUT_SANDBOX_SIZE);
     	logger.debug("maxOutputSandboxSize = " + maxOutputSandboxSize);
     	long maxOutputSandboxSizeLong = -1;
     	if ((maxOutputSandboxSize != null) && (maxOutputSandboxSize.length() > 0)){
     		if (maxOutputSandboxSize.startsWith("(")){
     			maxOutputSandboxSize = maxOutputSandboxSize.substring(1);
     		}
     		if (maxOutputSandboxSize.endsWith(")")){
     			maxOutputSandboxSize = maxOutputSandboxSize.substring(0, maxOutputSandboxSize.length()-1);
     		}
     		logger.debug("maxOutputSandboxSize without parentheses = " + maxOutputSandboxSize);
     		try{
     			maxOutputSandboxSizeLong = Math.round(Double.parseDouble(maxOutputSandboxSize));
     		} catch (NumberFormatException nfe){
     			logger.error(" Number mismatch for maxOutputSandboxSize = " + maxOutputSandboxSize);
     			throw new IllegalArgumentException(" Number mismatch for maxOutputSandboxSize = " + maxOutputSandboxSize);
     		}
     	}
     	wrapper.append("__max_osb_size=").append("" + maxOutputSandboxSizeLong).append("\n");
 
         fileNames = job.getOutputFiles();
      	
     	if (fileNames.size() > 0) {
     		wrapper.append("declare -a __output_file\n");
     		wrapper.append("declare -a __output_transfer_cmd\n");
     		wrapper.append("declare -a __output_file_dest\n\n");
 
             List<String> sandboxDestURI = job.getOutputSandboxDestURI();
     		String sandboxBaseDestURI = job.getOutputSandboxBaseDestURI();
     		String dnFromsandboxBaseDestURI = null;
     		String dnFromSandboxDestURI = null;
     		String fName = null;
 
     		if (sandboxBaseDestURI != null) {
     			if (sandboxBaseDestURI.startsWith("gsiftp://")){
     				if (sandboxBaseDestURI.indexOf("?DN=") != -1){
     					dnFromsandboxBaseDestURI = sandboxBaseDestURI.substring(sandboxBaseDestURI.indexOf("?DN=") + 4);
     					sandboxBaseDestURI = CEUtils.getURIWithoutDN(sandboxBaseDestURI);
     				}
     				if (!sandboxBaseDestURI.endsWith("/")) {
     					sandboxBaseDestURI = sandboxBaseDestURI + "/";
     				}
     				wrapper.append("__gsiftp_dest_uri=").append(sandboxBaseDestURI).append("\n");
     			} else if (sandboxBaseDestURI.startsWith("https://")){
     				if (!sandboxBaseDestURI.endsWith("/")) {
     					sandboxBaseDestURI = sandboxBaseDestURI + "/";
     				}
     				wrapper.append("__https_dest_uri=").append(sandboxBaseDestURI).append("\n");
     			} else {
     				sandboxBaseDestURI = null;
     			}
     		}
 
     		if (sandboxDestURI.size() > 0 && sandboxDestURI.size() != fileNames.size()) {
     			throw new IllegalArgumentException("Number mismatch for OutputSandboxDestURI");
     		}
     		
     		if (!(sandboxBaseDestURI == null ^ sandboxDestURI.size() == 0)) {
     			throw new IllegalArgumentException("Missing or duplicate OutputSandboxBaseDestURI and OutputSandboxDestURI");
     		}
     		
     		for (int k = 0; k < fileNames.size(); k++) {
     			dnFromSandboxDestURI = null;
     			fName = filenameNorm(fileNames.get(k));
     			if (fName.indexOf('/') != 0) {
     				fName = "${workdir}/" + fName;
     			}
     			wrapper.append("__output_file[").append(k).append("]=");
     			wrapper.append(stringNorm(fName)).append("\n");
 
     			if (sandboxDestURI.size() > 0) {
     				if (sandboxDestURI.get(k).startsWith("gsiftp")) {
     					if (sandboxDestURI.get(k).indexOf("?DN=") != -1){
     						dnFromSandboxDestURI = sandboxDestURI.get(k).substring(sandboxDestURI.get(k).indexOf("?DN=") + 4);
     						sandboxDestURI.set(k, CEUtils.getURIWithoutDN(sandboxDestURI.get(k)));   					
    						}
     					if ((dnFromSandboxDestURI != null) && (!"".equals(dnFromSandboxDestURI.trim()))){
     						wrapper.append("__output_transfer_cmd[").append(k).append("]=\"\\${globus_transfer_cmd} -ds \\\"" + dnFromSandboxDestURI + "\\\" \"\n");
     					} else {
     						wrapper.append("__output_transfer_cmd[").append(k).append("]=\"\\${globus_transfer_cmd}\"\n");
     					}
     				} else if (sandboxDestURI.get(k).startsWith("https")) {
     					wrapper.append("__output_transfer_cmd[").append(k).append("]=\"\\${https_transfer_cmd}\"\n");
 
     				} else {
     					throw new IllegalArgumentException("Unsupported protocol");
     				}
     				wrapper.append("__output_file_dest[").append(k).append("]=");
     				wrapper.append(sandboxDestURI.get(k)).append("\n");
     			} else if ((sandboxBaseDestURI != null) && (sandboxBaseDestURI.startsWith("gsiftp://"))) {
     				if ((dnFromsandboxBaseDestURI != null) && (!"".equals(dnFromsandboxBaseDestURI.trim()))) {
     					wrapper.append("__output_transfer_cmd[").append(k).append("]=\"\\${globus_transfer_cmd} -ds \\\"" + dnFromsandboxBaseDestURI + "\\\" \"\n");
     				} else {
     					wrapper.append("__output_transfer_cmd[").append(k).append("]=\"\\${globus_transfer_cmd}\"\n");
     				}
     			}
 
     		}
     		wrapper.append("\n");
     	}
 
         //osb lrms
         if (job.getVolatileProperty(AbstractJobExecutor.LRMS_OUTPUT_FILES) != null) {
             List<String> lrmsOutputFileNames = (ArrayList<String>)job.getVolatileProperty(AbstractJobExecutor.LRMS_OUTPUT_FILES); 
             if (lrmsOutputFileNames.size() > 0) { 
                wrapper.append("declare -a __lrms_output_file\n");
                
                for (int index=0; index<lrmsOutputFileNames.size(); index++) {
                    wrapper.append("__lrms_output_file[").append(index).append("]=");
                    wrapper.append(stringNorm(lrmsOutputFileNames.get(index))).append("\n");
                }
             }
         }
         wrapper.append("\n");
 
         //Begin OutputData
         if (job.containsVolatilePropertyKeys(Job.OUTPUT_DATA)) {
             List<OutputDataRecord> outputData = (List<OutputDataRecord>)job.getVolatileProperty(Job.OUTPUT_DATA);
 
             if (outputData != null && outputData.size() > 0) {
                 logger.debug("OutputData size = " + outputData.size());
                 wrapper.append("__output_data=1").append("\n");
 
                 String DSUploadFile = (String) job.getVolatileProperty(Job.DS_UPLOAD_OUTPUT_FILE);
                 logger.debug("DSUploadFile = " + DSUploadFile);
 
                 wrapper.append("__dsupload=\"").append(DSUploadFile).append("\"\n");
                 wrapper.append("declare -a __OD_output_file\n");
                 wrapper.append("declare -a __OD_logical_filename\n");
                 wrapper.append("declare -a __OD_storage_element\n\n");
 
                 for (int index = 0; index< outputData.size(); index++){
                     wrapper.append("__OD_output_file[").append(index).append("]=\"");
                     wrapper.append(outputData.get(index).getODOutputFile()).append("\"\n");
                     wrapper.append("__OD_logical_filename[").append(index).append("]=\"");
                     wrapper.append(outputData.get(index).getODLogicalFilename()).append("\"\n");
                     wrapper.append("__OD_storage_element[").append(index).append("]=\"");
                     wrapper.append(outputData.get(index).getODStorageElement()).append("\"\n");
                 }
             }
         } else {
             logger.debug("OutputData attribute not specified.");
             wrapper.append("__output_data=0").append("\n");
         }
         wrapper.append("\n");
       //End OutputData
         String templatePathName = (String)job.getVolatileProperty(AbstractJobExecutor.JOB_WRAPPER_TEMPLATE_PATH) + File.separator;
         
         templatePathName += (job.isMpich()) ? JOB_WRAPPER_MPI_TEMPLATE_NAME : JOB_WRAPPER_TEMPLATE_NAME;
 
     	if (wrapperTemplateHashTable.get(templatePathName) == null) {
     		wrapperTemplateHashTable.put(templatePathName, getWrapperTemplate(templatePathName));
     	}
     	wrapper.append(wrapperTemplateHashTable.get(templatePathName));
     	return wrapper.toString();
     }
 
     private static String getWrapperTemplate(String templatePathName) throws IOException {
         FileReader templateFileReader = null;
 
         try {
             templateFileReader = new FileReader(templatePathName);
         } catch(FileNotFoundException fnf){
             throw new IOException("Cannot find jobwrapper template");
         }
 
         StringBuffer wrapperTemplate = new StringBuffer();
         BufferedReader in = new BufferedReader(templateFileReader);
         String line = in.readLine();
 
         while (line != null) {
             wrapperTemplate.append(line + "\n");
             line = in.readLine();
         }
  
         try {
             in.close();
             templateFileReader.close();
         } catch (IOException ioe) {
             //nothing.
         }
 
         return wrapperTemplate.toString();
     }
 
     public static void main(String[] args) throws java.net.MalformedURLException, Exception {
         if (args.length != 1) {
             System.out.println("Bad parameter");
             System.exit(1);
         }
 
         StringBuffer jdl = new StringBuffer();
         BufferedReader jdlReader = null;
 
         try {
             jdlReader = new BufferedReader(new FileReader(args[0]));
             String tmps = jdlReader.readLine();
             while (tmps != null) {
                 jdl.append(tmps);
                 tmps = jdlReader.readLine();
             }
             jdlReader.close();
         } catch (IOException ioEx) {
            ioEx.printStackTrace();
             if (jdlReader != null)
                 jdlReader.close();
             System.exit(1);
         }
 
         Job tmpJob = new Job(jdl.toString());
         // dummy CE gridftp site
         tmpJob.setCREAMInputSandboxURI("file:///tmp");
         tmpJob.setId("https://lxgianelle.pd.infn.it:9000/CREAM-542526256534");
         tmpJob.setGridJobId("https://lxgianelle.pd.infn.it:9000/GRID-542526256534");
         tmpJob.setHlrLocation("hlr.location.net");
 
         System.out.println(buildWrapper(tmpJob));
     }
 
 }
