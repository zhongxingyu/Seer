 /*
  * (copyright) 2012 United States Government, as represented by the 
  * Secretary of Defense.  All rights reserved.
  *
  * (copyright) 2013 Politecnico di Torino, Italy
  *                  TORSEC group -- http://security.polito.it
  * 
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions 
  * are met:
  * 
  * - Redistributions of source code must retain the above copyright 
  * notice, this list of conditions and the following disclaimer. 
  * 
  * - Redistributions in binary form must reproduce the above copyright 
  * notice, this list of conditions and the following disclaimer in the 
  * documentation and/or other materials provided with the distribution. 
  * 
  * - Neither the name of the U.S. Government nor the names of its 
  * contributors may be used to endorse or promote products derived from 
  * this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
  * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
  * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY 
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE. 
  */
 package gov.niarl.sal.webservices.hisWebService.server;
 
 import gov.niarl.hisAppraiser.hibernate.dao.HisAuditDao;
 import gov.niarl.hisAppraiser.hibernate.domain.AuditLog;
 import gov.niarl.hisAppraiser.hibernate.util.HibernateUtilHis;
 import gov.niarl.hisAppraiser.integrityReport.HisReportUtil;
 import gov.niarl.hisAppraiser.util.HisUtil;
 
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.security.NoSuchAlgorithmException;
 import java.util.InputMismatchException;
 
import javax.annotation.Resource;
 import javax.jws.WebParam;
 import javax.jws.WebResult;
 import javax.jws.WebService;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceContext;
 
 import org.apache.log4j.Logger;
 
 /**
  * The HisDownloadReportService answers clients with the integrity
  * report having the requested ID. 
  * @author Nicola Barresi
  */
 @WebService
 public class HisDownloadReportService {
 	private static Logger logger = Logger.getLogger(HisDownloadReportService.class);
 
	@Resource
    private WebServiceContext ctx;

 	/**
 	 * This function returns the integrity report having the given ID.
 	 * @param reportId ID of the report in DB
 	 * @return The string of report having the given ID.   
 	 */
 	@WebResult(name = "reportString")
 	public String fetchReport(@WebParam(name = "reportId") Long reportId) {
 		logger.debug("fetchReport called with reportId:" + reportId);
 		String reportXML = "";
 		HibernateUtilHis.beginTransaction();
 
		MessageContext msgCtx = ctx.getMessageContext();
		HttpServletResponse response =  (HttpServletResponse) msgCtx.get(MessageContext.SERVLET_RESPONSE);

 		try {
 			reportXML = HisReportUtil.fetchReport(reportId);
			if (reportXML.equals("")) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}

 			HibernateUtilHis.commitTransaction();
 			return reportXML;
 		} catch (Exception exception) {
 			HibernateUtilHis.rollbackTransaction();
 			exception.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
 			throw new RuntimeException(exception);
 		} finally {
 			HibernateUtilHis.closeSession();
 		}
 	}
 }
