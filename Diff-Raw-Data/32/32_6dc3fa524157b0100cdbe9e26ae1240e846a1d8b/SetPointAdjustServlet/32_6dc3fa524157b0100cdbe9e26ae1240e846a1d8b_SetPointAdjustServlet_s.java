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
 
 import com.controlj.green.addonsupport.access.*;
 import com.controlj.green.addonsupport.access.aspect.SetPointAdjust;
 import com.controlj.green.addonsupport.access.util.Acceptors;
 import com.controlj.green.addonsupport.access.value.FloatValue;
 import org.jetbrains.annotations.NotNull;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Collection;
 
 public class SetPointAdjustServlet extends HttpServlet {
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         doGet(request, response);
     }
 
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         resp.setHeader("Expires", "Wed, 01 Jan 2003 12:00:00 GMT");
         resp.setHeader("Cache-Control", "no-cache");
 
         String location = req.getParameter("loc");
         String newValue = req.getParameter("value");
 
         if (location == null || newValue == null)
         {
             throw new ServletException("Error adjusting setpoint, location or value not set");
         }
 
         writeAdjustment(location, newValue, req);
     }
 
     private void writeAdjustment(final String locationString, final String newValue, HttpServletRequest req) throws ServletException {
 
         try {
             SystemConnection connection = DirectAccess.getDirectAccess().getUserSystemConnection(req);
             connection.runWriteAction(FieldAccessFactory.newFieldAccess(), "Setpoint adjusted from iStat", new WriteAction() {
 
                 public void execute(@NotNull WritableSystemAccess access) throws Exception {
                     Location location = access.getGeoRoot().getTree().resolve(locationString);
                     Collection<SetPointAdjust> adjusts = location.find(SetPointAdjust.class, Acceptors.acceptAll());
                     if (adjusts.isEmpty()) {
                         throw new Exception("Invalid location, no SetPointAdjusts here: "+location);
                     }
 
                     SetPointAdjust adjust = adjusts.iterator().next();
                     FloatValue hsp = adjust.getHeatingSetpointAdjust();
                     FloatValue csp = adjust.getCoolingSetpointAdjust();
 
                     if (!hsp.getValidator().isValid(newValue) || !csp.getValidator().isValid(newValue))
                     {
                         throw new ServletException("Error, trying to set setpoint to invalid value of:"+newValue);
                     }
 
                     csp.set(Float.parseFloat(newValue));
                     if (!adjust.isHeatingAndCoolingLinked())
                     {
                         hsp.set(Float.parseFloat(newValue));
                     }
                 }
             });
         } catch (Exception e) {
             throw new ServletException(e);
         }
     }
 }
