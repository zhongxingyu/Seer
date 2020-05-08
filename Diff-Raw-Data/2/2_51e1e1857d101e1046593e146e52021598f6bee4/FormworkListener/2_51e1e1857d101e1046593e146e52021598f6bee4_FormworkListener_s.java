 package edu.uoc.pfc.formwork.infraestructura;
 
 import java.io.InputStream;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.xml.bind.JAXBException;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 import org.drools.KnowledgeBase;
 import org.drools.KnowledgeBaseFactory;
 import org.drools.builder.KnowledgeBuilder;
 import org.drools.builder.KnowledgeBuilderError;
 import org.drools.builder.KnowledgeBuilderFactory;
 import org.drools.builder.ResourceType;
 import org.drools.io.ResourceFactory;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.xml.sax.SAXException;
 
 import edu.uoc.pfc.formwork.xml.TipoFW;
 import edu.uoc.pfc.formwork.xml.XMLLoader;
 
 
 /**
  * @author cachocenso
  *
  */
 public class FormworkListener implements ServletContextListener {
 	static {
 		BasicConfigurator.configure();
 	}
 
 	private static Logger logger = Logger.getLogger(FormworkListener.class);
 	
 	public void contextDestroyed(ServletContextEvent event) {
 		ServletContext servletContext = event.getServletContext();
 		
 		FormworkContext fwCtx = (FormworkContext) servletContext.getAttribute(Attributes.FWCONTEXT);
 		for (StatefulKnowledgeSession session:
 							fwCtx.getKnowledgeBase().getStatefulKnowledgeSessions()) {
 			session.dispose();
 		}
 	}
 
 	public void contextInitialized(ServletContextEvent event) {
 		// Obtener el ServletContext
 		ServletContext servletContext = event.getServletContext();
 
 		// Cargar la configurción del framework.
 		loadFrameWorkConfig(servletContext);
 
 		// Crear contexto
 		FormworkContext formworkContext = new FormworkContext();
 
 		// Cargar fichero de reglas.
 		KnowledgeBase base = createKnowledgeBase(servletContext);
 
 		// Almacenar KnowledgeBase en contexto.
 		formworkContext.setKnowledgeBase(base);
 
 		// Almacenar contexto en ServletContext.
 		servletContext.setAttribute(Attributes.FWCONTEXT, formworkContext);
 		
 		logger.info("Aplicación iniciada correctamente");
 	}
 
 	private void loadFrameWorkConfig(ServletContext servletContext) {
 		InputStream thePage = servletContext
 				.getResourceAsStream("/WEB-INF/fw.xml");
 
 		// Cargar el fichero XML de configuración.
 		try {
 			TipoFW config = XMLLoader.parseFile(TipoFW.class, thePage);
 			servletContext.setAttribute(Attributes.FWCONFIG, config);
			logger.info(">>> Configuración cagada");
 		} catch (JAXBException e) {
 			logger.error("Error cargando configuración", e);
 		} catch (SAXException e) {
 			logger.error("Error cargando configuración", e);
 		}
 	}
 
 	private KnowledgeBase createKnowledgeBase(ServletContext servletContext) {
 		TipoFW config = (TipoFW) servletContext
 				.getAttribute(Attributes.FWCONFIG);
 
 		String rulesFile = config.getRules().getRulesFile();
 
 		InputStream rulesIStream = servletContext
 				.getResourceAsStream(rulesFile);
 
 		final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
 				.newKnowledgeBuilder();
 		kbuilder.add(ResourceFactory.newInputStreamResource(rulesIStream),
 				ResourceType.DRL);
 
 		if (kbuilder.hasErrors()) {
 			for (KnowledgeBuilderError error : kbuilder.getErrors()) {
 				logger.error(error);
 			}
 			
 			throw new IllegalArgumentException(
 					"Imposible crear knowledge");
 		}
 		
 		final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
 		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
 		
 		logger.info("Base de conocimiento cargada");
 		return kbase;
 	}
 
 }
