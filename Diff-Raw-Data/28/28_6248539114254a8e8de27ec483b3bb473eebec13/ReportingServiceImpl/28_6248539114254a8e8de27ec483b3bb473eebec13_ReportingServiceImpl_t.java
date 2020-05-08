 /* ReportServiceImpl.java - created on Jul 15, 2011, Copyright (c) 2011 The European Library, all rights reserved */
 package eu.europeana.uim.gui.cp.server;
 
 import java.io.File;
 import java.util.List;

import javax.servlet.http.HttpServletRequest;

 import eu.europeana.uim.gui.cp.client.services.ReportingService;
 import eu.europeana.uim.gui.cp.server.ExecutionServiceImpl;
 import eu.europeana.uim.gui.cp.shared.ExecutionDTO;
 
 
 
 /**
  * The implementation of the reporting service. This is just a frontend to the ReportUtils.
  * 
  * @author Rene Wiermer (rene.wiermer@kb.nl)
  * @date Jul 15, 2011
  */
 public class ReportingServiceImpl extends ExecutionServiceImpl implements ReportingService {    
 
 	private static final long serialVersionUID = 1L;
 
	private String serverURL;
	
 	/**
      * Creates a new instance of this class.
      */
     public ReportingServiceImpl() {
         super();
     }
 
     /**
      * @param executionID
      * @param report
      * @return true, if the generation of all reports was successful
      */
     @Override
     public Boolean generateReport(String executionID, String report, String[] filetypes) {
         return ReportUtils.generateReport(report, executionID, filetypes);
     }
 
     @Override
     public String getReport(String reportDesign, String exeuctionID, String filetype) {
         File reportFile = ReportUtils.getReportFile(reportDesign, exeuctionID, filetype);
         if (!reportFile.exists()) { return null; }
        
        
		if(serverURL == null){
	    	HttpServletRequest request = this.getThreadLocalRequest();
	        String scheme = request.getScheme();            
	        String serverName = request.getServerName();     
	        int serverPort = request.getServerPort();        
	        String contextPath = request.getContextPath();   
	        serverURL = scheme+"://"+serverName+":"+serverPort+contextPath + "/";
			}
        
        return serverURL + ReportUtils.generateDownloadURL(reportDesign, exeuctionID, filetype);
     }
 
     @Override
     public List<ExecutionDTO> getPastExecutions(String[] workflows, String design,
             String[] filetypes) {
         List<ExecutionDTO> executions = super.getPastExecutions(workflows);
 
         for (ExecutionDTO execution : executions) {
             for (String filetype : filetypes) {
                 File reportFile = ReportUtils.getReportFile(design, "" + execution.getId(),
                         filetype);
                 execution.setValue(design + "." + filetype, "" + reportFile.exists());
             }
         }
 
         return executions;
     }
 }
