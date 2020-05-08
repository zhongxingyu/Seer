 package it.ivncr.erp.jsf.managedbean.personale.gestioneaddetti;
 
 import it.ivncr.erp.model.personale.Addetto;
 import it.ivncr.erp.service.ServiceFactory;
 import it.ivncr.erp.service.addetto.AddettoService;
 import it.ivncr.erp.util.ImageUtil;
 
 import java.io.ByteArrayInputStream;
 import java.io.Serializable;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.RequestScoped;
 
 import org.primefaces.event.CaptureEvent;
 import org.primefaces.event.FileUploadEvent;
 import org.primefaces.model.DefaultStreamedContent;
 import org.primefaces.model.StreamedContent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @ManagedBean
 @RequestScoped
 public class DettaglioAddettoFoto implements Serializable {
 
 	private static final Logger logger = LoggerFactory.getLogger(DettaglioAddettoFoto.class);
 
 	private static final long serialVersionUID = 1L;
 
	@ManagedProperty("#{gestioneAddetti.edited.id}")
 	private Integer id;
 
 	private StreamedContent current;
 
 
 	@PostConstruct
 	public void init() {
 
 		// If we are editing an existing record, it is time to fetch
 		// it from the database and fill in the bean fields.
 		//
 		if(id != null) {
 
 			AddettoService as = ServiceFactory.createService("Addetto");
 			Addetto addetto = as.retrieve(id);
 
 			byte[] foto = addetto.getFoto();
 			if(foto != null) {
 
 				String mime = ImageUtil.getMimeType(foto);
 				current = new DefaultStreamedContent(new ByteArrayInputStream(foto), mime);
 			}
 		}
 	}
 
 	public void doClean() {
 
 		AddettoService as = ServiceFactory.createService("Addetto");
 		as.setFoto(id, null);
 		logger.debug("Picture successfully cleaned.");
 
 		current = null;
 	}
 
 	public void onCapture(CaptureEvent event) {
 
 		byte[] captured = event.getData();
 
 		AddettoService as = ServiceFactory.createService("Addetto");
 		as.setFoto(id, captured);
 		logger.debug("Picture successfully uploaded.");
 
 		String mime = ImageUtil.getMimeType(captured);
 		current = new DefaultStreamedContent(new ByteArrayInputStream(captured), mime);
 	}
 
 	public void onFileUpload(FileUploadEvent event) {
 
 		byte[] uploaded = event.getFile().getContents();
 
 		AddettoService as = ServiceFactory.createService("Addetto");
 		as.setFoto(id, uploaded);
 		logger.debug("Picture successfully uploaded.");
 
 		String mime = ImageUtil.getMimeType(uploaded);
 		current = new DefaultStreamedContent(new ByteArrayInputStream(uploaded), mime);
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public StreamedContent getCurrent() {
 		return current;
 	}
 
 	public void setCurrent(StreamedContent current) {
 		this.current = current;
 	}
 }
