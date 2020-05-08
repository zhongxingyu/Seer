 package org.esupportail.opi.services.export;
 
 import static fj.P.p;
 import static fj.Unit.unit;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 import javax.management.RuntimeErrorException;
 
 import org.esupportail.opi.utils.exceptions.ExportException;
 import org.exolab.castor.mapping.Mapping;
 import org.exolab.castor.xml.Marshaller;
 import org.exolab.castor.xml.XMLContext;
 import org.springframework.core.io.ClassPathResource;
 import org.xml.sax.InputSource;
 
 import fj.Effect;
 import fj.F;
 import fj.P2;
 import fj.Unit;
 
 public class CastorService implements ISerializationService {
 
 	final Marshaller marshaller;
 	
 	final String xslXmlPath;
 
 	final Effect<P2<String, F<Writer, Unit>>> writeToFile = 
 			new Effect<P2<String, F<Writer,Unit>>>() {
 				public void e(P2<String, F<Writer, Unit>> tuple) {
 					Writer w = null;
 					try {
 						w = new FileWriter(tuple._1());
 						tuple._2().f(w);
 						w.flush();
 					} catch (Exception e) {
 						throw new ExportException("Problem opening or using a java.io.Writer : ", e);
 					} finally {
 						try {
							w.close();
 						} catch (IOException e) {
 							throw new ExportException("Problem closing a java.io.Writer : ", e);
 						}	
 					}}};
 				
 	
 	
 	public CastorService(final String xslXmlPath, final String castorMapping) {
 		this.xslXmlPath = xslXmlPath; 
 		final Mapping mp = new Mapping();
 		try {
 			mp.loadMapping(new InputSource(
 					new ClassPathResource(castorMapping).getInputStream()));
 			final XMLContext ctx = new XMLContext();
 			ctx.addMapping(mp);
 			marshaller = ctx.createMarshaller();
 			marshaller.setProperty("org.exolab.castor.indent", "false");
 			marshaller.setProperty("org.exolab.castor.xml.proxyInterfaces",
 					"net.sf.cglib.proxy.Factory, org.hibernate.proxy.HibernateProxy");
 		} catch (Exception e) {
 			throw new RuntimeErrorException(new Error(e));
 		} 
 	}
 	
 
 	/**
 	 * Generate the xml in the file.
 	 * @param object 
 	 * @param fileName 
 	 */
 	public void objectToFileXml(final Object object, final String fileName ) {
 		writeToFile.e(p(xslXmlPath + fileName, (F<Writer, Unit>) new F<Writer, Unit>() {
 			public Unit f(Writer w) {
 				try {
 					marshaller.setWriter(w);
 					marshaller.marshal(object);
 				} catch (Exception e) {
 					throw new ExportException("Problem while marshalling using castor : ", e);					
 				}
 				return unit();
 			}}));
 	}
 	
 	
 	public String getXslXmlPath() {
 		return xslXmlPath;
 	}
 }
