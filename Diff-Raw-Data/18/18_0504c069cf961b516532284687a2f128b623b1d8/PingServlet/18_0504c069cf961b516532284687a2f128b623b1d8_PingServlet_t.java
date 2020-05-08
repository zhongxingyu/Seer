 /*
 
 Copyright (C) 2010 Steffen Dienst
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package de.elatexam;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.TaskOptions;
 
 import de.elatexam.util.Tools;
 
 /**
  * Add queue every 10s to keep the gae vm running.... dirty hack , I know.
 *
  * @author Steffen Dienst
 *
  */
 public class PingServlet extends HttpServlet {
 
   private static final String NEXT_TOKEN = "curtoken";
   private static final String REQ_TOKEN = "token";
 
   @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    handleRequest(req, resp);
  };

  @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    handleRequest(req, resp);
  }

  private void handleRequest(HttpServletRequest req, HttpServletResponse resp) {
     String token = req.getParameter(REQ_TOKEN);
     String curToken = (String) Tools.c().get(NEXT_TOKEN);
     if (curToken == null) {
       curToken = token;
     }
     if ("start".equals(token) || token.equals(curToken)) {
       Queue queue = QueueFactory.getDefaultQueue();
       String nextToken = Double.toString(Math.random());
       Tools.c().put(NEXT_TOKEN, nextToken);
       queue.add(TaskOptions.Builder.withParam(REQ_TOKEN, nextToken).countdownMillis(10000));
     }
     resp.setStatus(200);

   }
 
 }
