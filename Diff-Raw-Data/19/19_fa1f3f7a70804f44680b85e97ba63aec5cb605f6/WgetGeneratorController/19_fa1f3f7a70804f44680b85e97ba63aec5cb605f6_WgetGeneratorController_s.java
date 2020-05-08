 /*****************************************************************************
  * Copyright  2011 , UT-Battelle, LLC All rights reserved
  *
  * OPEN SOURCE LICENSE
  *
  * Subject to the conditions of this License, UT-Battelle, LLC (the
  * Licensor) hereby grants to any person (the Licensee) obtaining a copy
  * of this software and associated documentation files (the "Software"), a
  * perpetual, worldwide, non-exclusive, irrevocable copyright license to use,
  * copy, modify, merge, publish, distribute, and/or sublicense copies of the
  * Software.
  *
  * 1. Redistributions of Software must retain the above open source license
  * grant, copyright and license notices, this list of conditions, and the
  * disclaimer listed below.  Changes or modifications to, or derivative works
  * of the Software must be noted with comments and the contributor and
  * organizations name.  If the Software is protected by a proprietary
  * trademark owned by Licensor or the Department of Energy, then derivative
  * works of the Software may not be distributed using the trademark without
  * the prior written approval of the trademark owner.
  *
  * 2. Neither the names of Licensor nor the Department of Energy may be used
  * to endorse or promote products derived from this Software without their
  * specific prior written permission.
  *
  * 3. The Software, with or without modification, must include the following
  * acknowledgment:
  *
  *    "This product includes software produced by UT-Battelle, LLC under
  *    Contract No. DE-AC05-00OR22725 with the Department of Energy.
  *
  * 4. Licensee is authorized to commercialize its derivative works of the
  * Software.  All derivative works of the Software must include paragraphs 1,
  * 2, and 3 above, and the DISCLAIMER below.
  *
  *
  * DISCLAIMER
  *
  * UT-Battelle, LLC AND THE GOVERNMENT MAKE NO REPRESENTATIONS AND DISCLAIM
  * ALL WARRANTIES, BOTH EXPRESSED AND IMPLIED.  THERE ARE NO EXPRESS OR
  * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE,
  * OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY PATENT, COPYRIGHT,
  * TRADEMARK, OR OTHER PROPRIETARY RIGHTS, OR THAT THE SOFTWARE WILL
  * ACCOMPLISH THE INTENDED RESULTS OR THAT THE SOFTWARE OR ITS USE WILL NOT
  * RESULT IN INJURY OR DAMAGE.  The user assumes responsibility for all
  * liabilities, penalties, fines, claims, causes of action, and costs and
  * expenses, caused by, resulting from or arising out of, in whole or in part
  * the use, storage or disposal of the SOFTWARE.
  *
  *
  ******************************************************************************/
 
 package org.esgf.web;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.esgf.metadata.JSONException;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 /**
  * Implementation of a controller that generates wget scripts on the fly.  A request is sent from the front end to generate this script.
  * Contained in its query string are the following parameters:
  * - id name of the dataset
  * - array of the individual file names contained in the batch of files requested for download
  * - a 'create' or 'delete' query variable (in lieu of http PUT and DELETE)
  *
  *
  * @author john.harney
  *
  */
 @Controller
 @RequestMapping("/wgetproxy")
 public class WgetGeneratorController {
 
     private final static Logger LOG = Logger.getLogger(WgetGeneratorController.class);
 
         //
         //The wget file is primarily created through the post method
         @RequestMapping(method=RequestMethod.GET)
         public @ResponseBody void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException, ParserConfigurationException {
             LOG.debug("doGet wgetproxy");
             createWGET(request, response);
         }
 
 
         @RequestMapping(method=RequestMethod.POST)
         public @ResponseBody void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException, ParserConfigurationException {
             LOG.debug("doPost wgetproxy");
 
             if(request.getParameter("type").equals("create")) {
                 createWGET(request, response);
             }
         }
 
 
         private void createWGET(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException, ParserConfigurationException {
 
 
          // file name
             String filename = request.getParameter("id") + ".sh";
 
             LOG.debug("filename = " + filename);
             // create content
 
             String wgetText = "#!/bin/sh\n";
 
             wgetText += "# ESG Federation download script\n";
             wgetText += "#\n";
             wgetText += "# Template version: 0.2\n";
             wgetText += "# Generated by the all new ESGF Gateway\n";
             wgetText += "#";
             wgetText += "##############################################################################\n\n\n";
 
             wgetText += "download() {\n";
 
             for(int i=0;i<request.getParameterValues("child_url").length;i++) {
            wgetText += "\twget '" + request.getParameterValues("child_url")[i] + "'\n";
             }
 
             wgetText += "}\n";
             wgetText += "#\n# MAIN \n#\n";
             wgetText += "download\n";
 
 
 
             response.setContentType("text/x-sh");
             response.addHeader("Content-Disposition", "attachment; filename=" + filename );
             response.setContentLength((int) wgetText.length());
             PrintWriter out = response.getWriter();
             out.print(wgetText);
 
             LOG.debug("Finishing writing wget stream");
 
 
         }
 
 
 
         private void queryStringInfo(HttpServletRequest request) {
             LOG.debug("Query parameters");
             LOG.debug("\tId");
             LOG.debug("\t\t" + request.getParameterValues("id")[0]);
             LOG.debug("\tType");
             LOG.debug("\t\t" + request.getParameterValues("type")[0]);
             LOG.debug("\tChild urls");
             for(int i=0;i<request.getParameterValues("child_url").length;i++) {
                 LOG.debug("\t\t" + request.getParameterValues("child_url")[i]);
             }
             LOG.debug("\tChild ids");
             for(int i=0;i<request.getParameterValues("child_id").length;i++) {
                 LOG.debug("\t\t" + request.getParameterValues("child_id")[i]);
             }
         }
 
 }
