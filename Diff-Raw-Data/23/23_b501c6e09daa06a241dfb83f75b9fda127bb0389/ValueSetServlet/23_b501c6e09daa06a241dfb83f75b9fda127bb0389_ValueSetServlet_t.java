 package gov.nih.nci.evs.browser.servlet;
 
 import org.json.*;
 import gov.nih.nci.evs.browser.utils.*;
 
 import java.io.*;
 import java.net.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import org.apache.log4j.*;
 
 import gov.nih.nci.evs.browser.properties.*;
 import static gov.nih.nci.evs.browser.common.Constants.*;
 import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
 import org.lexgrid.valuesets.LexEVSValueSetDefinitionServices;
 
 
 /**
  * <!-- LICENSE_TEXT_START -->
  * Copyright 2008,2009 NGIT. This software was developed in conjunction
  * with the National Cancer Institute, and so to the extent government
  * employees are co-authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *   1. Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the disclaimer of Article 3,
  *      below. Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials provided
  *      with the distribution.
  *   2. The end-user documentation included with the redistribution,
  *      if any, must include the following acknowledgment:
  *      "This product includes software developed by NGIT and the National
  *      Cancer Institute."   If no such end-user documentation is to be
  *      included, this acknowledgment shall appear in the software itself,
  *      wherever such third-party acknowledgments normally appear.
  *   3. The names "The National Cancer Institute", "NCI" and "NGIT" must
  *      not be used to endorse or promote products derived from this software.
  *   4. This license does not authorize the incorporation of this software
  *      into any third party proprietary programs. This license does not
  *      authorize the recipient to use any trademarks owned by either NCI
  *      or NGIT
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
  *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
  *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
  *      NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
  *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  *      POSSIBILITY OF SUCH DAMAGE.
  * <!-- LICENSE_TEXT_END -->
  */
 
 /**
  * @author EVS Team
  * @version 1.0
  *
  * Modification history
  *     Initial implementation kim.ong@ngc.com
  *
  */
 
 public final class ValueSetServlet extends HttpServlet {
     private static Logger _logger = Logger.getLogger(ValueSetServlet.class);
     /**
      * local constants
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Validates the Init and Context parameters, configures authentication URL
      *
      * @throws ServletException if the init parameters are invalid or any other
      *         problems occur during initialisation
      */
     public void init() throws ServletException {
 
     }
 
     /**
      * Route the user to the execute method
      *
      * @param request The HTTP request we are processing
      * @param response The HTTP response we are creating
      *
      * @exception IOException if an input/output error occurs
      * @exception ServletException if a servlet exception occurs
      */
     public void doGet(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         execute(request, response);
     }
 
     /**
      * Route the user to the execute method
      *
      * @param request The HTTP request we are processing
      * @param response The HTTP response we are creating
      *
      * @exception IOException if an input/output error occurs
      * @exception ServletException if a Servlet exception occurs
      */
     public void doPost(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         execute(request, response);
     }
 
     /**
      * Process the specified HTTP request, and create the corresponding HTTP
      * response (or forward to another web component that will create it).
      *
      * @param request The HTTP request we are processing
      * @param response The HTTP response we are creating
      *
      * @exception IOException if an input/output error occurs
      * @exception ServletException if a servlet exception occurs
      */
 
     public void execute(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         // Determine request by attributes
        String uri = HTTPUtils.cleanXSS(request.getParameter("valueset"));
 
         long ms = System.currentTimeMillis();
 
         LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
 
         String s = "";
         String valueSetDefinitionRevisionId = null;
         try {
 			URI valueSetDefinitionURI = new URI(uri);
 
 			StringBuffer sb = new StringBuffer();
 			sb.append("<html>\n");
 			sb.append("\n");
 			sb.append("<head>\n");
 			sb.append("<title>" + uri + "</title>\n");
 			sb.append("</head>\n");
 			sb.append("\n");
 			sb.append("<body>\n");
 			sb.append("\n");
 	        StringBuffer buf = vsd_service.exportValueSetDefinition(valueSetDefinitionURI, valueSetDefinitionRevisionId);
 	        sb.append(buf.toString());
             response.getWriter().write(sb.toString());
 
             sb = new StringBuffer();
             sb.append("</body></html>\n");
 
             response.getWriter().write(sb.toString());
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 		_logger.debug("Run time (milliseconds): "
 			+ (System.currentTimeMillis() - ms));
 
     }
 }
 
