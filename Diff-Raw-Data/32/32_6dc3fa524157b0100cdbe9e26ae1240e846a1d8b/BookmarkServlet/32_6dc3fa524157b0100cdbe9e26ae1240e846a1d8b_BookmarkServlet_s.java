 /*
  * Copyright (c) 2010 Automated Logic Corporation
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.controlj.green.istat.web;
 
 import com.controlj.green.addonsupport.AddOnInfo;
 import com.controlj.green.addonsupport.FileLogger;
 import com.controlj.green.addonsupport.InvalidConnectionRequestException;
 import com.controlj.green.addonsupport.access.*;
 import org.jetbrains.annotations.NotNull;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 public class BookmarkServlet extends HttpServlet {
     public BookmarkServlet() {
         super();
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         //System.out.println("Area Servlet called at:"+new Date());
 
         resp.setHeader("Expires", "Wed, 01 Jan 2003 12:00:00 GMT");
         resp.setHeader("Cache-Control", "no-cache");
         ServletOutputStream out = resp.getOutputStream();
         try {
             writeLocations(out, req.getParameterValues("bookmarks"), req);
         } catch (Exception e) {
             throw new ServletException(e);
         }
         /*
         out.println("[");
         out.println("{ display:'Main Conf Room', id:'mainconf'},");
         out.println("{ display:'Board Room', id:'boardroom'},");
         out.println("{ display:'Room 235', id:'room235'}");
         out.println("]");
         */
     }
 
     public void writeLocations(final ServletOutputStream out, final String locations[], HttpServletRequest req) throws IOException, SystemException, ActionExecutionException, InvalidConnectionRequestException {
         final JSONArray arrayData = new JSONArray();
 
         if (locations != null) {
             SystemConnection connection = DirectAccess.getDirectAccess().getUserSystemConnection(req);
 
             connection.runReadAction(new ReadAction(){
                 public void execute(@NotNull SystemAccess access) throws Exception {
                     for (String location : locations) {
                         JSONObject next = new JSONObject();
                         Location loc = access.getGeoRoot().getTree().resolve(location);
                         next.put("display", loc.getDisplayName());
                         next.put("id", location);
                         arrayData.put(next);
                     }
                 }
             });
         }
 
         try {
             PrintWriter writer = new PrintWriter(out);
             arrayData.write(writer);
             writer.flush();
         } catch (JSONException e) {
             FileLogger logger = AddOnInfo.getAddOnInfo().getDateStampLogger();
             logger.println("Unexpected exception:");
             logger.println(e);
         }
     }
 
 }
