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
 import com.controlj.green.addonsupport.access.util.LocationSort;
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
 import java.util.Collection;
 
 public class TreeServlet extends HttpServlet {
     private static final String LOCATION_PARAM = "LOC";
 
     public TreeServlet() {
         super();
     }
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
         resp.setHeader("Expires", "Wed, 01 Jan 2003 12:00:00 GMT");
         resp.setHeader("Cache-Control", "no-cache");
         ServletOutputStream out = resp.getOutputStream();
         try {
             writeLevel(out, req.getParameter(LOCATION_PARAM), req);
         } catch (Exception e) {
            throw new ServletException(e);
         }
         out.flush();
         /*
         out.println("[");
         out.println("{ display:'Main Conf Room', id:'mainconf'},");
         out.println("{ display:'Board Room', id:'boardroom'},");
         out.println("{ display:'Room 235', id:'room235'}");
         out.println("]");
         */
     }
 
     public void writeLevel(final ServletOutputStream out, final String location, HttpServletRequest req) throws IOException, SystemException, ActionExecutionException, InvalidConnectionRequestException {
         final JSONArray arrayData = new JSONArray();
 
         SystemConnection connection = DirectAccess.getDirectAccess().getUserSystemConnection(req);
 
         connection.runReadAction(new ReadAction(){
             public void execute(@NotNull SystemAccess access) throws Exception {
                 Location parent;
                 if (location == null || location.length()==0) {
                     parent = access.getGeoRoot();
                 } else
                 {
                     parent = access.getGeoRoot().getTree().resolve(location);
                 }
                 Collection<Location> childrenLocs = parent.getChildren(LocationSort.PRESENTATION);
                 HasDecendentAspectsAcceptor acceptor = new HasDecendentAspectsAcceptor(SetPoint.class, SetPointAdjust.class);
 
                 for (Location nextLoc : childrenLocs)
                 {
                     if (!acceptor.accept(nextLoc)) {
                         continue;
                     }
                     JSONObject next = new JSONObject();
                     next.put("display", nextLoc.getDisplayName());
                     next.put("id", nextLoc.getPersistentLookupString(false));
                     next.put("area", (nextLoc.getType() == LocationType.Area));
                     arrayData.put(next);
                 }
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
 
     private class HasDecendentAspectsAcceptor
     {
         private HasAllAspectsAcceptor acceptor;
 
         HasDecendentAspectsAcceptor(Class<? extends Aspect>... aspects)
         {
             acceptor = new HasAllAspectsAcceptor(aspects);
         }
 
         public boolean accept(@NotNull Location loc) {
             if (acceptor.accept(loc)) {
                 return true;
             } else if (loc.getType() == LocationType.Microblock) {
                 return false;
             } else {
                 Collection<Location> children = loc.getChildren();
                 for (Location child : children) {
                     if (accept(child)) {
                         return true;
                     }
                 }
                 return false;
             }
         }
 
     }
 
 
     private class HasAllAspectsAcceptor {
         Class<? extends Aspect> testAspects[];
 
         HasAllAspectsAcceptor(Class<? extends Aspect>... aspects)
         {
             this.testAspects = aspects;
         }
 
         public boolean accept(@NotNull Location loc) {
             for (Class<? extends Aspect> testAspect : testAspects) {
                 if (!loc.hasAspect(testAspect)) {
                     return false;
                 }
             }
             return true;
         }
     }
 
     
 
 }
