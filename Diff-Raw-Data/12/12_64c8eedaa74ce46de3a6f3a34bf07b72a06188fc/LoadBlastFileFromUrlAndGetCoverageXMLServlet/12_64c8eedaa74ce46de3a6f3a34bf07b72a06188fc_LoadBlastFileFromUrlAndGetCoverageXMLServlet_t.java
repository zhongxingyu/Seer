 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.era7.bioinfo.blastxviewer7.server.servlet;
 
 import com.era7.bioinfo.blastxviewer7.server.RequestList;
 import com.era7.lib.bioinfo.bioinfoutil.blast.BlastExporter;
 import com.era7.lib.bioinfoxml.BlastOutput;
 import com.era7.lib.communication.xml.Request;
 import com.era7.lib.communication.xml.Response;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.URL;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.jdom.Element;
 
 /**
  *
  * @author Pablo Pareja Tobes <ppareja@era7.com>
  */
 public class LoadBlastFileFromUrlAndGetCoverageXMLServlet extends HttpServlet {
 
     @Override
     public void init() {
     }
 
     @Override
     public void doPost(javax.servlet.http.HttpServletRequest request,
             javax.servlet.http.HttpServletResponse response)
             throws javax.servlet.ServletException, java.io.IOException {
         //System.out.println("doPost !");
         servletLogic(request, response);
 
     }
 
     @Override
     public void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         //System.out.println("doGet !");
         servletLogic(request, response);
 
 
     }
 
     private void servletLogic(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
 
         OutputStream out = response.getOutputStream();
 
         String responseSt = "";
        String requestId = "";
 
         try {
 
             String temp = request.getParameter(Request.TAG_NAME);
             Request myReq = new Request(temp);
            requestId = myReq.getId();
 
 
             System.out.println("myReq = " + myReq);
 
             if (myReq.getMethod().equals(RequestList.LOAD_BLAST_FILE_FROM_URL_AND_GET_COVERAGE_XML_REQUEST)) {
 
                 Element urlElem = myReq.getParameters().getChild("url");
 
                 if (urlElem != null) {
 
                     try {
                         URL url = new URL(urlElem.getText());
                         InputStream inputStream = url.openStream();
 
                         System.out.println("lalala");
 
                         BufferedReader inBuff = new BufferedReader(new InputStreamReader(inputStream));
                         String line = null;
                         StringBuilder stBuilder = new StringBuilder();
                         while((line = inBuff.readLine()) != null){
                             stBuilder.append(line);
                         }
                         String resultExport = BlastExporter.exportBlastXMLtoIsotigsCoverage(new BlastOutput(stBuilder.toString()));
 
                         inBuff.close();
                         inputStream.close();
 
                         responseSt = "<response status=\"" + Response.SUCCESSFUL_RESPONSE
                                 + "\" " + "id=\"" + requestId + "\" method=\"" + RequestList.LOAD_BLAST_FILE_FROM_URL_AND_GET_COVERAGE_XML_REQUEST
                                 + "\" >\n" + resultExport + "\n</response>";
 
 
                     } catch (IOException e) {
                         Response tempResp = new Response();
                        tempResp.setId(requestId);
                         tempResp.setError("The url provided is not valid");
                         responseSt = tempResp.toString();
                     }
 
 
                 } else {
                     Response tempResp = new Response();
                    tempResp.setId(requestId);
                     tempResp.setError("There was no url specified");
                     responseSt = tempResp.toString();
                 }
 
 
             } else {
                 Response tempResp = new Response();
                tempResp.setId(requestId);
                 tempResp.setError("There is no such method");
                 responseSt = tempResp.toString();
             }
 
 
         } catch (Exception e) {
             Response tempResp = new Response();
            tempResp.setId(requestId);
             tempResp.setError("There was an error...\n" + e.getStackTrace()[0].toString());
             responseSt = tempResp.toString();
         }
 
         response.setContentType("text/html");
         out.write(responseSt.getBytes());
 
         System.out.println("end reached!");
 
         out.flush();
         out.close();
     }
 }
