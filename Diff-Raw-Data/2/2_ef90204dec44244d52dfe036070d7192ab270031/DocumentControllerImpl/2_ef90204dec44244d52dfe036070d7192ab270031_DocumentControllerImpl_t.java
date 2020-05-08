 package de.mq.merchandise.controller;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.primefaces.model.DefaultStreamedContent;
 import org.primefaces.model.StreamedContent;
 import org.primefaces.model.UploadedFile;
 import org.springframework.http.MediaType;
 
 import de.mq.merchandise.opportunity.ResourceOperations;
 import de.mq.merchandise.opportunity.support.DocumentModelAO;
 import de.mq.merchandise.opportunity.support.DocumentService;
 import de.mq.merchandise.opportunity.support.DocumentsAware;
 
 
 
 public class DocumentControllerImpl {
 	
 	
 	private static final String PDF_EXT = ".pdf";
 
 
 	private final ResourceOperations resourceOperations; 
 	
 	
 	private final DocumentService documentService;
 	
 	
 	static final String UPLOAD_DOCUMENT_URL_REDIRECT = "uploadDocument.xhtml?faces-redirect=true&selectMode=true";
 	static final String SHOW_DOCUMENT_URL_REDIRECT = "showDocument.xhtml?faces-redirect=true&selectMode=true";
 	static final int MAX_HEIGHT = 800;
 	static final int MAX_WIDTH = 1625;
 	static final String URL_ROOT="http://localhost:8080/merchandise.web/attachements/%s"; 
 
 	
 	
 	
 	public DocumentControllerImpl(final DocumentService documentService, final ResourceOperations resourceOperations) {
 		this.documentService=documentService;
 		this.resourceOperations=resourceOperations;
 	}
 	
 	
 	
 	
 	void removeAllAndAddNew(final DocumentsAware document, final UploadedFile file) throws IOException {
 		for(final String name : document.documents().keySet()){
 			documentService.delete(document, name);
 			document.removeDocument(name);
 		}
 		addAttachement(document, file);
 	}
 	
 	
 	void addAttachement(final DocumentsAware document, final UploadedFile file ) throws IOException  {
 		System.out.println("addAttachement...");
 		try(final InputStream inputStream = file.getInputstream()) {
 			documentService.upload(document, file.getFileName(), inputStream, file.getContentType());
 			document.assignDocument(file.getFileName());
 		} 
 	}
 	
 	
 	String url(final  DocumentsAware documentAware, final String name) {
 		
 		
 		
 		final String url = documentAware.urlForName(name);
 		
 		if( url == null){
 			return null ;
 		}
 		
 		if(url.trim().toLowerCase().startsWith("http")){
 			return url;
 		}
 		System.out.println(String.format(URL_ROOT, url));
 		return String.format(URL_ROOT, url);
 	}
 	
 	
 	
 	void removeAttachement(final DocumentModelAO documentModelAO , final String name ) {
 		documentService.delete(documentModelAO.getDocument(), name);
 		documentModelAO.getDocument().removeDocument(name);
 		documentModelAO.setSelected(null);
 	}
 	
 	void addLink(final DocumentModelAO documentModelAO) {
 		if (documentModelAO.getLink() == null){
 			return;
 		}
 		
 		if (documentModelAO.getLink().trim().length() == 0 ) {
 			return;
 		}
 		
 		final String name = documentModelAO.getLink().trim().replaceFirst("(http|HTTP)([:]//){0,1}([wW]{3}[.]){0,1}",  "");
 		
 		
 		documentService.assignLink(documentModelAO.getDocument(), name);
 		
 		documentModelAO.getDocument().assignWebLink(name);
 		
 		documentModelAO.setLink(null);
 		
 		
 	}
 	
 	
 	void calculateSize(final DocumentModelAO documentModelAO)   {
 		
 		documentModelAO.setWidth(MAX_WIDTH);
 		documentModelAO.setHeight(MAX_HEIGHT);	
 		
 		
 		final String url = url(documentModelAO.getDocument(), documentModelAO.getSelected() );
 		
 		
 		if( url.toLowerCase().endsWith(PDF_EXT)){
 			return;
 		}
 		
 		final BufferedImage image =resourceOperations.readImage(url);
 		
 		
 		
 		if ( image == null ){
 			
 			return;
 		} 
 			
 		
 		if(image.getWidth()> MAX_WIDTH) {
 			final double scale = ((double)MAX_WIDTH) / image.getWidth();
 			documentModelAO.setWidth((int) (image.getWidth()*scale));
 			documentModelAO.setHeight((int) (image.getHeight()*scale));
 		} else {
 			documentModelAO.setWidth(image.getWidth());
 			documentModelAO.setHeight(image.getHeight());
 		}
 			
 	
 	}
 
 	
 	/* like a virgin touched for the very first time */
 	public final void init(final DocumentModelAO documentModelAO, final Long documentId, final String page, final String selected, final String returnPage) {
 		documentModelAO.setReturnFromUpload(page);
 		documentModelAO.setSelected(selected);
 		documentModelAO.setReturnFromShowAttachement(returnPage);
 		if(documentId==null){
 			return;
 		}
 		
 		
 		documentModelAO.setDocument(documentService.read(documentId));
 		
 		if( selected != null){
 			calculateSize(documentModelAO);
 		}
 	}
 	
 	
 	
 	
 	StreamedContent stream(final DocumentModelAO documentModelAO) throws IOException {
 		if ( documentModelAO.getDocument().documents().keySet().isEmpty() ) {
 			throw new IllegalArgumentException("Nothing attached, unable to download it");
 		}
 		final String name = documentModelAO.getDocument().documents().keySet().iterator().next();
 		/*final URL url = new URL(url(documentModelAO.getDocument(), name));
 		
 		InputStream content =  (InputStream) url.getContent() ;*/
		try (InputStream is = new ByteArrayInputStream(documentService.document(documentModelAO.getDocument().id()))) {
 			return new DefaultStreamedContent(is,MediaType.TEXT_PLAIN_VALUE ,name );
 		}
 		
 	}
 	 
 	
 	
 	
 
 }
