 /**
  * 
  */
 package no.ovitas.compass2.webapp.action;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.servlet.ServletOutputStream;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import no.ovitas.compass2.Constants;
 import no.ovitas.compass2.dao.xml.KBBuilderXtmDaoXml;
 import no.ovitas.compass2.model.KnowledgeBaseHolder;
 import no.ovitas.compass2.service.ConfigurationManager;
 import no.ovitas.compass2.service.ExportDomainModelManager;
 import no.ovitas.compass2.service.factory.KBFactory;
 import no.ovitas.compass2.service.impl.ExportDomainModel2PlainTextManagerImpl;
 
 import com.opensymphony.xwork2.Preparable;
 
 /**
  * @author magyar
  *
  */
 public class KBModelExportAction extends BaseAction implements Preparable {
 
 	private static final Log log = LogFactory.getLog(KBModelExportAction.class);
 	
 	protected ExportDomainModelManager exportDomainModelManager;
 	
 	public void setExportDomainModelManager(
 			ExportDomainModelManager exportDomainModelManager) {
 		this.exportDomainModelManager = exportDomainModelManager;
 	}
 
 	/**
 	 * 
 	 */
 	public KBModelExportAction() {
 		// TODO Auto-generated constructor stub
 	}
 
 	/* (non-Javadoc)
 	 * @see com.opensymphony.xwork2.Preparable#prepare()
 	 */
 	@Override
 	public void prepare() throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 	
 	public String execute(){
 		log.info("KBModelExportAction.execute()");
 		String fileName = null;
 		try {
 			fileName = this.exportDomainModelManager.exportModel();
 			File f = new File(fileName);
 			
 			FileInputStream fis = new FileInputStream(fileName);
 			BufferedInputStream bi = new BufferedInputStream(fis);
 		    f.length();
 		    byte[] b = new byte[(int) f.length()];
 		    bi.read(b);
 		    bi.close();
 			
 			getResponse().setContentType("application/plain");
 			getResponse().setContentLength(b.length);
 			getResponse().setHeader("Expires", "0");
 			getResponse().setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
 			getResponse().setHeader("Content-Disposition", "attachment; filename=\"model.txt\"");
 			getResponse().setHeader("Pragma", "public");
 			getResponse().getOutputStream().write(b);
 			
 		} catch (FileNotFoundException fnfe) {
 			log.error("File not found: " + fileName + ", " + fnfe.getMessage());
 		} catch (Exception e) {
 			log.error("Error occured: " + e.getMessage());
 		}
 		return NONE;
 	}
 
 }
