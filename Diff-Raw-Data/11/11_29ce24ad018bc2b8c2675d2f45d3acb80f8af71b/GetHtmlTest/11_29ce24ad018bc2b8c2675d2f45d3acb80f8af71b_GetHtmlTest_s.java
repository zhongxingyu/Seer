 /*  Copyright (C) 2012  Nicholas Wright
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class GetHtmlTest {
 	GetHtml getHtml;
 	static String refData = "<html><head><title>Test Page</title></head><body><p>Test Page</p></body></html>";
 	static String refData2 = "<!DOCTYPE html><html><head><title>Bestellformular</title></head><body><h1>Bestellung</h1><fieldset><legend>Kundendaten</legend></fieldset><fieldset><legend>Artikel</legend></fieldset></body></html>";
 	String testString = null;
 	static Server server;
 
 	@BeforeClass
 	public static void startServer() throws Exception{
 		server  = new Server(80);
 		server.setHandler(new TestHandler());
 		server.start();
 	}
 
 	@AfterClass
 	public static void stopServer() throws Exception{
 		server.stop();
 	}
 
 	@Before
 	public void setUp(){
 		getHtml = new GetHtml();
 	}
 
 	@Test(timeout=1000)
 	public void testGetString() throws Exception {
 		testString = getHtml.get("http://localhost/");
 		assertThat(testString,is(refData));
 	}
 
 	@Test(timeout=1000)
 	public void testReUse() throws Exception{
 		assertThat(getHtml.get("http://localhost/"), is(refData));;
 		assertThat(getHtml.get("http://localhost/2"), is(refData2));
 		assertThat(getHtml.get("http://localhost/"), is(refData));
 	}
 
 	static class TestHandler extends AbstractHandler{
 		@Override
 		public void handle(String arg0, Request baseRequest, HttpServletRequest request,
 				HttpServletResponse response) throws IOException, ServletException {
 
 			response.setContentType("text/html;charset=utf-8");
 			response.setStatus(HttpServletResponse.SC_OK);
 			baseRequest.setHandled(true);
 
			if(request.getRequestURI().equals("http://localhost/2")){
 				response.getWriter().println(refData2);
 			}else{
 				response.getWriter().println(refData);
 			}
 		}
 	}
 }
