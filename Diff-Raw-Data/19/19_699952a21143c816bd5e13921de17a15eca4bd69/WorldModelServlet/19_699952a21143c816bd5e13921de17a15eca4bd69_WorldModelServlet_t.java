 package org.rulez.magwas.worldmodel;
 
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.transform.TransformerException;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.rulez.magwas.worldmodel.BaseObject;
 import org.rulez.magwas.worldmodel.InputParseException;
 import org.rulez.magwas.worldmodel.Util;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import org.apache.commons.io.IOUtils;
 
 public class WorldModelServlet extends HttpServlet {
     /**
 	 * serial version just to get rid of warning
 	 */
 	private static final long serialVersionUID = 1L;
 
 	
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		/*
 		 * expects a xml containing a BaseObject entity in the same form as get returns it back
 		 * 
 		 */
 		BufferedReader reader = null;
 		String str = "";
		response.setCharacterEncoding("UTF-8");
		response.addHeader("Content-Type", "text/xml;charset=utf-8");
 		PrintWriter out = response.getWriter();
 		Session session = Util.getSession();
 		Transaction tx = session.beginTransaction();	
 		try {
 			reader = request.getReader();
 			String xmlstring = IOUtils.toString(reader);
 			//FIXME schema validation (maybe in upper layers)
 			ByteArrayInputStream bis = new ByteArrayInputStream(xmlstring.getBytes());
 			Document doc = Util.newDocument(bis);
 
 			NodeList objs = doc.getElementsByTagName("BaseObject");
 			Document outdoc = Util.newDocument();
 			Element rootnode = outdoc.createElement("objects");
 			outdoc.appendChild(rootnode);
 			for(int i = 0; i < objs.getLength(); i++) {
 				BaseObject obj = new BaseObject((Element) objs.item(i), session);
 				session.save(obj);
 				Element xml;
 				xml = obj.toXML(outdoc);
 				rootnode.appendChild(xml);
 			}
 			
 			str = Util.dom2String(outdoc);
 			tx.commit();
 		
 		} catch (SAXException e) {
 			str = "<exception>"+e.getMessage()+"</exception>";
 		} catch (InputParseException e) {
 			str = "<exception>"+e.getMessage()+"</exception>";
 		} catch (TransformerException e) {
 			str = "<exception>"+e.getMessage()+"</exception>";
 		} 
 		
 		session.close();
 		out.write(str);			
 		out.flush();
         out.close();
 
 	}
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		/*
 		 * expects one parameter: id
 		 * 
 		 * returns an xml containing one BaseObject entity, with the following attributes:
 		 * 	- id
 		 *  - version
 		 *  - type
 		 *  - source
 		 *  - dest
 		 *  
 		 *  id is mandatory, others only exists if the corresponding field is not null
 		 *  all attributes are in form <id>[:<version>]
 		 *  if there is a value field, then a textnode containing the value is also added
 		 */
 		
 		String id=request.getParameter("id");
 		Session session = Util.getSession();
 		BaseObject obj;
		response.setCharacterEncoding("UTF-8");
		response.addHeader("Content-Type", "text/xml;charset=utf-8");
 		PrintWriter out = response.getWriter();
 
 		Transaction tx = session.beginTransaction();
 		if(id != null) {
 			try {
 				obj = BaseObject.getBaseObjectByCompositeId(id, session);
 				out.println( Util.xml2String(obj) );
 			} catch (InputParseException e) {
 				out.println("<exception>"+e.getMessage()+"</exception>");
 			}
 		} else {
 			out.println("<null>");
 		}
     	tx.commit();
     	session.close();		
 		out.flush();
         out.close();
 	}
 
 	
 }
