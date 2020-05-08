 package es.gob.catastro.service.rmi.pdf.server;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import es.gob.catastro.service.pdf.PDFService;
 import es.gob.catastro.service.pdf.PDFServiceException;
 import es.gob.catastro.service.pdf.util.PDFBuffer;
 
 import es.gob.catastro.service.rmi.pdf.server.translet.TransletProcessor;
 
 
 public class PDFPlugin implements PDFService {
 
 	private static final Log log = LogFactory.getLog(PDFPlugin.class);
 
 	public PDFPlugin() {
 		super();
 	}
 
 	@Override
 	public PDFBuffer generarPDF(String xml, String transformer) {
 		try {
        	TransletProcessor trans = new TransletProcessor(transformer);
         	byte[] pdfContent = trans.transform(xml);
         	return new PDFBuffer(pdfContent);
         	
         } catch (Exception e) {
         	log.error("Error en la generacion del PDF", e);
 			throw new PDFServiceException("Error en el plugin de PDF-FOP", e);
 		}
 	}
 
 
 }
