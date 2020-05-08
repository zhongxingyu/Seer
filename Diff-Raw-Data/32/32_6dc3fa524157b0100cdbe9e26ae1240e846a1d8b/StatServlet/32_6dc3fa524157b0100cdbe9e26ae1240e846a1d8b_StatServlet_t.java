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
 import com.controlj.green.addonsupport.access.aspect.SetPoint;
 import com.controlj.green.addonsupport.access.aspect.SetPointAdjust;
 import com.controlj.green.addonsupport.access.util.Acceptors;
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
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Collection;
 
 public class StatServlet extends HttpServlet {
     private static final DecimalFormat nFormat = new DecimalFormat("#.0");
 
     public StatServlet() {
         super();
     }
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
         resp.setHeader("Expires", "Wed, 01 Jan 2003 12:00:00 GMT");
         resp.setHeader("Cache-Control", "no-cache");
         ServletOutputStream out = resp.getOutputStream();
 
         try {
             writeStats(out, req.getParameter("loc"), req);
         } catch (Exception e) {
             resp.sendError(500, e.getMessage());
            FileLogger logger = AddOnInfo.getAddOnInfo().getDateStampLogger();
            logger.println("Unexpected exception:");
            logger.println(e);
         }
 
         /*
         out.println("[{ hsp:67,");
         out.println("csp: 74,");
         out.println("off: 1.5,");
         out.println("lim: 3.0,");
         out.println("current:71,");
         out.println("loc:'"+req.getParameter("loc")+"',");
         out.println("time:'"+new Date().getSeconds()+"'");
         out.println("}]");
         */
     }
 
     public void writeStats(final ServletOutputStream out, final String location, HttpServletRequest req) throws IOException, SystemException, ActionExecutionException, InvalidConnectionRequestException {
         final JSONArray arrayData = new JSONArray();
 
         SystemConnection connection = DirectAccess.getDirectAccess().getUserSystemConnection(req);
 
         connection.runReadAction(FieldAccessFactory.newFieldAccess(), new ReadAction(){
             public void execute(@NotNull SystemAccess access) throws Exception {
                 Location eqLoc = access.getGeoRoot().getTree().resolve(location);
                 Collection<SetPoint> setPoints = eqLoc.find(SetPoint.class, Acceptors.acceptAll());
                 if (setPoints.isEmpty()) {
                     throw new Exception("Invalid location, no SetPoints here: "+location);
                 }
                 Collection<SetPointAdjust> setPointAdjusts = eqLoc.find(SetPointAdjust.class, Acceptors.acceptAll());
                 if (setPointAdjusts.isEmpty()) {
                     throw new Exception("Invalid location, no SetPointAdjusts here: "+location);
                 }
 
 
                 SetPoint setPoint = setPoints.iterator().next();
                 SetPointAdjust setPointAdjust = setPointAdjusts.iterator().next();
                 JSONObject next = new JSONObject();
 
                 next.put("hsp",         nFormat.format(setPoint.getEffectiveHeating()));
                 next.put("csp",         nFormat.format(setPoint.getEffectiveCooling()));
                 next.put("current",     nFormat.format(setPoint.getZoneTemp()));
                 next.put("off",         nFormat.format(setPointAdjust.getCoolingSetpointAdjust().getValue()));
                 next.put("lim",         nFormat.format(setPointAdjust.getCoolingSetpointAdjust().getMaximum()));
                 next.put("loc",         location);
                 next.put("adjustable",  setPointAdjust.getCoolingSetpointAdjust().isWritable());
                 arrayData.put(next);
             }
         });
 
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
