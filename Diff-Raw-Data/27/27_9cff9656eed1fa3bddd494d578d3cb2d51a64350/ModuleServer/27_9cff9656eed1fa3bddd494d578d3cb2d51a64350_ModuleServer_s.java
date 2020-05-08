 package org.data2semantics.server;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.data2semantics.modules.AbstractModule;
 import org.data2semantics.modules.ModuleWrapper;
 import org.data2semantics.util.RepositoryWriter;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.rio.RDFParseException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ModuleServer extends HttpServlet {
 
 
 	private static final long serialVersionUID = 4287719806326260156L;
 	private static Logger log = LoggerFactory.getLogger(ModuleWrapper.class);
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		String moduleName = request.getParameter("module");
 		String fileName = request.getParameter("file");
 		String graph = request.getParameter("graph");
 		String resource = request.getParameter("resource");
 
 		try {
 			AbstractModule module = ModuleWrapper.constructModule(moduleName,
 					fileName, graph, resource);
 
 			log.info("Starting module");
 			Repository outputRepository = module.start();
 			log.info("Module run completed");
 
 			// TODO Add provenance information about ModuleWrapper run
 			
 			String outputFileName = "output.n3";
 			try {
 				log.info("Starting RepositoryWriter (writing to " + outputFileName
 						+ ")");
 
 				FileOutputStream outputStream = new FileOutputStream(new File(
 						outputFileName));
 				OutputStreamWriter streamWriter = new OutputStreamWriter(
 						outputStream);
 				RepositoryWriter rw = new RepositoryWriter(outputRepository, streamWriter);
 
 				rw.write();
 				log.info("Done");
 
 			} catch (FileNotFoundException e) {
 				log.error("Failed to create output file " + outputFileName);
 			}
 			
 			
			log.info("Starting RepositoryWriter (writing to string)");

			StringWriter stringWriter = new StringWriter();
			RepositoryWriter rw = new RepositoryWriter(outputRepository,
					stringWriter);

			rw.write();
			log.info("Done");

			String output = stringWriter.toString();
 
 			response.setContentType("text/html");
 			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println("<pre>");
			response.getWriter().print(StringEscapeUtils.escapeHtml(output));
			response.getWriter().println("</pre>");
 			response.getWriter().println(
 					"session=" + request.getSession(true).getId());
 
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (RepositoryException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (RDFParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 }
