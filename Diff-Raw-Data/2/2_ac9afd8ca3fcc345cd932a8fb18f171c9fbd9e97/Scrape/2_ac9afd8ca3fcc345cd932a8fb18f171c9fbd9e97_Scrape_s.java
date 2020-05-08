 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.tracker.frontend;
 
 import com.tracker.backend.Bencode;
 import com.tracker.backend.StringUtils;
 import com.tracker.backend.TrackerRequestParser;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author bo
  */
 public class Scrape extends HttpServlet {
 
     /**
      * the remote address the request originated from.
      */
     private InetAddress remoteAddress;
    
     /** 
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
         // store remote address in a useful form
         remoteAddress = InetAddress.getByName(request.getRemoteAddr());
 
         TrackerRequestParser trp = new TrackerRequestParser();
 
         // set remote address for logging purposes
         trp.setRemoteAddress(remoteAddress);
         
         TreeMap<String,TreeMap> innerDictionary = new TreeMap<String,TreeMap>();
         TreeMap<String,TreeMap> outerDictionary = new TreeMap<String,TreeMap>();
 
         TreeMap<String,String[]> requestMap = new TreeMap<String,String[]>(
                 request.getParameterMap());
         String responseString = new String();
 
         try {
             // is there a info_hash key present?
             if(requestMap.containsKey((String)"info_hash")) {
                 String[] value = requestMap.get((String)"info_hash");
                 // scrape all requested info hashes
                 for(int i = 0; i < value.length; i++) {
                     /**
                      * tomcat automatically decodes the request as it comes in
                      */
                     // encode the info hash again
                     byte[] rawInfoHash = new byte[20];
                    for(int j = 0; i < rawInfoHash.length; i++) {
                         rawInfoHash[j] = (byte) value[i].charAt(j);
                     }
                     String hexInfoHash = StringUtils.getHexString(rawInfoHash);
                     innerDictionary.put(StringUtils.URLEncodeFromHexString(hexInfoHash),
                             trp.scrape(value[i]));
                 }
             }
             
             // no info_hash key, scrape all torrents
             else {
                 innerDictionary = trp.scrape();
             }
 
             outerDictionary.put((String)"files", innerDictionary);
 
             responseString = Bencode.encode(outerDictionary);
         } catch(Exception ex) {
             Logger.getLogger(Scrape.class.getName()).log(Level.SEVERE,
                     "Exception caught", ex);
         }
 
         response.setContentType("text/plain");
         PrintWriter out = response.getWriter();
         try {
             out.print(responseString);
         } finally { 
             out.close();
         }
     } 
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /** 
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
         processRequest(request, response);
     } 
 
     /** 
      * Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 
 }
