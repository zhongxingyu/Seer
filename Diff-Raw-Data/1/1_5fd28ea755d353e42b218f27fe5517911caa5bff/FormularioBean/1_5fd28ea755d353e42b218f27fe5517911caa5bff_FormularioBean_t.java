 package ar.com.cuyum.cnc.view;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.ejb.SessionContext;
 import javax.ejb.Stateful;
 import javax.enterprise.context.Conversation;
 import javax.enterprise.context.ConversationScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceContextType;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import org.apache.log4j.Logger;
 import org.primefaces.event.FileUploadEvent;
 import org.primefaces.model.DefaultStreamedContent;
 import org.primefaces.model.StreamedContent;
 import org.primefaces.model.UploadedFile;
 
 import ar.com.cuyum.cnc.domain.Formulario;
 import ar.com.cuyum.cnc.domain.Xsl;
 import ar.com.cuyum.cnc.service.TransformationService;
 import ar.com.cuyum.cnc.utils.DomUtils;
 import ar.com.cuyum.cnc.utils.FormRenderProperties;
 
 /**
  * Backing bean for Formulario entities.
  * <p>
  * This class provides CRUD functionality for all Formulario entities. It focuses
  * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
  * state management, <tt>PersistenceContext</tt> for persistence,
  * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
  * custom base class.
  */
 
 @Named
 @Stateful
 @ConversationScoped
 public class FormularioBean implements Serializable
 {
 	public static Logger log = Logger.getLogger(FormularioBean.class);
 
    private static final long serialVersionUID = 1L;
    
    private FacesMessage msgArchivoSubida;
    
    @Inject
    private FormRenderProperties formRenderProperties;
    
    @Inject 
    private TransformationService ts;
    
    @Inject 
    private DomUtils fileUtils;
 
    private String formDom;
    
    private Long idXsl;
    
    public Long getIdXsl() {
 	   return idXsl;
    }
 	
    public void setIdXsl(Long idXsl) {
 	   this.idXsl = idXsl;
    }
    
    /*
     * Support creating and retrieving Formulario entities
     */ 
 
    public String getFormDom() {
 	   return formDom;
    }
 	
    public void setFormDom(String formDom) {
 	   this.formDom = formDom;
    }
 
    private Long id;
 
    public Long getId()
    {
       return this.id;
    }
 
    public void setId(Long id)
    {
       this.id = id;
    }
 
    private Formulario formulario;
 
    public Formulario getFormulario()
    {
       return this.formulario;
    }
    
    private UploadedFile file;
    
    public UploadedFile getFile() {  
        return file;  
    }  
  
    public void setFile(UploadedFile file) {  
        this.file = file;  
    }
    
    private StreamedContent downloadFile;
 
    public StreamedContent getDownloadFile() {
 	   getXmlFile();
 	   return downloadFile;
    }
 	
    public void setDownloadFile(StreamedContent downloadFile) {
 	   this.downloadFile = downloadFile;
    }
 
 	@Inject
    private Conversation conversation;
 
    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private EntityManager entityManager;
    
    public String create()
    {
 
       this.conversation.begin();
       return "create?faces-redirect=true";
    }
 
    public void retrieve()
    {
 
       if (FacesContext.getCurrentInstance().isPostback())
       {
          return;
       }
 
       if (this.conversation.isTransient())
       {
          this.conversation.begin();
       }
 
       if (this.id == null)
       {
          this.formulario = this.example;
       }
       else
       {
          this.formulario = findById(getId());
          if (null != this.formulario && null != this.formulario.getXslTransform()){
         	 this.idXsl = this.formulario.getXslTransform().getId();
          }
       }
    }
 
    public Formulario findById(Long id)
    {
       return this.entityManager.find(Formulario.class, id);
    }
    
    private boolean verificarDuplicado(String codigo, Long id){
 	   boolean seguir = true;
 	   Formulario existentForm = null;
 	   try {
 		   existentForm = findByCode(codigo);
 	   } catch (NoResultException e) {		   
 		   //No hay ningun registro con ese codigo, puedo continuar;
 		   seguir = true;		   
 	   } catch (Exception e1) {		   
 		   //Trato de hacer update y ya hay un form con ese codigo;
 		   seguir = false;		   
 	   }  
 	   if (null != existentForm) {
 		   if (null == id){
 			  //Trato de crear nuevo form y ya hay un form con ese codigo;
 			  seguir = false; 
 		   }
 		   if (null != id){
 			   if (!existentForm.getId().equals(id)){
 				   seguir = false;
 			   }
 		   }
 	   }	   
 	   return seguir;
    }
    
    public Formulario findByCode(String code)
    {	
 	   Formulario formSelected =null;	   
 	   if (null != code && !code.trim().isEmpty()){
 		   formSelected = (Formulario) this.entityManager
 				.createQuery(
 						"select f from Formulario f where f.codigo = :code")
 				.setParameter("code", code.trim())
 				.getSingleResult();
 	   }
 	   return formSelected;
    }
    
    public String getXmlFile() {	  
 	   InputStream xmlStream=null;	   	  
 	   xmlStream = fileUtils.getXmlInputStream(formulario);
 	   if (null != xmlStream){	   
 		   downloadFile = new DefaultStreamedContent(xmlStream, "application/xml", formulario.getArchivo()); 
 	   } else {
 		   FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", 
 					  "Archivo no encontrado en !");  
 	       FacesContext.getCurrentInstance().addMessage(null, msg); 	       
 	   }
 	   return null;	   
    }
    
    public boolean copyFile(UploadedFile file, String destination) {
 	  
 	   boolean copiado = false;
 	   boolean procesar = true;
 	   try {
 		   
 		   InputStream in = file.getInputstream();
 		   
 		   if (null != destination && !destination.isEmpty()){
 			   destination = destination.trim();
 			   if (!destination.endsWith(System.getProperty("file.separator"))){
 				   //Agrego separador de fin
 				   destination = destination + System.getProperty("file.separator");
 			   }
 			   File carpeta = new File(destination);
 			   if (!carpeta.exists()){
 				   msgArchivoSubida = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Carga de Archivo Fallida",
 						   "Destino incorrecto o no existente: "+ destination);
 				   procesar = false;
 				   copiado = false;
 			   }  
 		   } else {
 			   msgArchivoSubida = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Carga de Archivo Fallida",
 					   "Destino incorrecto o no existente: "+ destination);
 			   procesar = false;
 			   copiado = false;
 		   }
 		   
 		   if (procesar){				   
 			   OutputStream out = new FileOutputStream(destination + this.file.getFileName());
 			   int read = 0;
 			   byte[] bytes = new byte[1024];
 			   while ((read = in.read(bytes)) != -1) {
 				   out.write(bytes, 0, read);
 			   }	     
 			   in.close();
 			   out.flush();
 			   out.close();	    
 		       copiado = true;
 		   }
 
 	   } catch (IOException e) {
 		   msgArchivoSubida = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
 				   "Carga de Archivo Fallida",  e.getMessage());		   
 		   copiado = false;		   
 	   }
 	   return copiado;
    }
    
    public String getPathXml(){
 	   String path = formRenderProperties.getDestinationXml();
 	   path = path.trim();
 	   if (!path.endsWith(System.getProperty("file.separator"))){
 		   //Agrego separador de fin
 		   path = path + System.getProperty("file.separator");
 	   }
 	   return path;
    }
 
    /*
     * Support updating and deleting Formulario entities
     */
 
    public String update()
    {
 	  boolean copiado;
 	  if(this.file != null && !file.getFileName().isEmpty()) {
 		  String nameFile = file.getFileName();
 		  if (nameFile.endsWith("xml")){
 			  String destination = formRenderProperties.getDestinationXml();			  
 		  	  copiado = copyFile(file, destination);
 		  	  if (copiado) {
 		  		this.formulario.setArchivo(file.getFileName());
 		  		this.formulario.setUrl(destination);
 		  	  }	
 		  	  else {		  		
 				   FacesContext.getCurrentInstance().addMessage(null, msgArchivoSubida);
 				   return null;
 		  	  }
 		  } else {
 			  FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Carga de Archivo Fallida", 
 					  "Solo archivos con extensi\u00F3n xml!");  
 	          FacesContext.getCurrentInstance().addMessage(null, msg); 
 	          return null;
 		  }
 	  } else if (this.id == null){
     	  FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Sr. Usuario: ", "Adjunte el xml de definici\u00F3n del formulario");  
           FacesContext.getCurrentInstance().addMessage(null, msg);  
           return null;
       }
 	  
 	  //Consulta si tiene el xsl de transformacion asociado
 	  
 	  if (null != this.idXsl){
 		  //Obtener el Xsl y asignarlo al formulario
 		  Xsl xslTransf = findXdlById(this.idXsl);
 		  if (null != xslTransf) {
 			  this.formulario.setXslTransform(xslTransf);
 		  } else {
 			  FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Sr. Usuario: ", "Debe seleccionar el xsl de transformaci\u00F3n");  
 	          FacesContext.getCurrentInstance().addMessage(null, msg);  
 	          return null;
 		  }
 	  }	  
 	  
 	  //Verificamos que no exista ya un formulario con el Codigo repetido	  
 	  boolean seguir = verificarDuplicado(this.formulario.getCodigo(), this.formulario.getId());
 	  
 	  if (!seguir) {		  
 		  FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ya existe un formulario con ese c\u00F3digo. ", 
 			  "Debe ser único.");  
 		   FacesContext.getCurrentInstance().addMessage(null, msg);
 		   return null;	
 	  }	 
 	  
       this.conversation.end();
       
       try
       {
          if (this.id == null)
          {        	
         	this.entityManager.persist(this.formulario);          
         	return "search?faces-redirect=true";
          }
          else
          {
             this.entityManager.merge(this.formulario);
             return "view?faces-redirect=true&id=" + this.formulario.getId();
          }
       }
       catch (Exception e)
       {
          FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
          return null;        
       }
    }
    
    private Xsl findXdlById(Long id)
    {
       return this.entityManager.find(Xsl.class, id);
    }
 
    public String delete()
    {
       this.conversation.end();
 
       try
       {
          this.entityManager.remove(findById(getId()));
          this.entityManager.flush();
          return "search?faces-redirect=true";
       }
       catch (Exception e)
       {
          FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
          return null;
       }
    }
 
    /*
     * Support searching Formulario entities with pagination
     */
 
    private int page;
    private long count;
    private List<Formulario> pageItems;
 
    private Formulario example = new Formulario();
 
    public int getPage()
    {
       return this.page;
    }
 
    public void setPage(int page)
    {
       this.page = page;
    }
 
    public int getPageSize()
    {
       return 10;
    }
 
    public Formulario getExample()
    {
       return this.example;
    }
 
    public void setExample(Formulario example)
    {
       this.example = example;
    }
 
    public void search()
    {
       this.page = 0;
    }
 
    public void paginate()
    {
 
       CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
 
       // Populate this.count
 
       CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
       Root<Formulario> root = countCriteria.from(Formulario.class);
       countCriteria = countCriteria.select(builder.count(root)).where(
             getSearchPredicates(root));
       this.count = this.entityManager.createQuery(countCriteria)
             .getSingleResult();
 
       // Populate this.pageItems
 
       CriteriaQuery<Formulario> criteria = builder.createQuery(Formulario.class);
       root = criteria.from(Formulario.class);
       TypedQuery<Formulario> query = this.entityManager.createQuery(criteria
             .select(root).where(getSearchPredicates(root)));
       this.pageItems = query.getResultList();
       
    }
 
    private Predicate[] getSearchPredicates(Root<Formulario> root)
    {
 	   Map<String, String> filters = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();		
 	    	      
 
 	   if (filters != null){
 		   
     	 if (filters.get("search:formularioBeanExampleFormCodigo") !=null && !filters.get("search:formularioBeanExampleFormCodigo").isEmpty()){
 			   this.example.setCodigo(filters.get("search:formularioBeanExampleFormCodigo"));		 
     	 }		 
     	 if (filters.get("search:formularioBeanExampleNombre") !=null && !filters.get("search:formularioBeanExampleNombre").isEmpty()){
 			   this.example.setNombre(filters.get("search:formularioBeanExampleNombre"));		 
     	 }		 
     	 if (filters.get("search:formularioBeanArchivo") !=null && !filters.get("search:formularioBeanArchivo").isEmpty()){
 			   this.example.setArchivo(filters.get("search:formularioBeanArchivo"));
 		 
     	 }		
       }	
 
       CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
       List<Predicate> predicatesList = new ArrayList<Predicate>();
 
       String codigo = this.example.getCodigo();
       if (codigo != null && !"".equals(codigo))
       {
          predicatesList.add(builder.like(builder.lower(root.<String> get("codigo")), '%' + codigo.toLowerCase() + '%'));         
       }
       
       String nombre = this.example.getNombre();
       if (nombre != null && !"".equals(nombre))
       {
          predicatesList.add(builder.like(builder.lower(root.<String> get("nombre")), '%' + nombre.toLowerCase() + '%'));         
       }
       String archivo = this.example.getArchivo();
       if (archivo != null && !"".equals(archivo))
       {
          predicatesList.add(builder.like(builder.lower(root.<String> get("archivo")), '%' + archivo.toLowerCase() + '%'));
       }
 
       return predicatesList.toArray(new Predicate[predicatesList.size()]); 
    }
 
    public List<Formulario> getPageItems()
    {
       return this.pageItems;
    }
 
    public long getCount()
    {
       return this.count;
    }
 
    /*
     * Support listing and POSTing back Formulario entities (e.g. from inside an
     * HtmlSelectOneMenu)
     */
 
    public List<Formulario> getAll()
    {
 
       CriteriaQuery<Formulario> criteria = this.entityManager
             .getCriteriaBuilder().createQuery(Formulario.class);
       Root<Formulario> root = criteria.from(Formulario.class);
       return this.entityManager.createQuery(
             criteria.select(criteria.from(Formulario.class)).where(
 					getSearchPredicates(root))).getResultList();      
    }
 
    @Resource
    private SessionContext sessionContext;
 
    public Converter getConverter()
    {
 
       final FormularioBean ejbProxy = this.sessionContext.getBusinessObject(FormularioBean.class);
 
       return new Converter()
       {
 
          @Override
          public Object getAsObject(FacesContext context,
                UIComponent component, String value)
          {
 
             return ejbProxy.findById(Long.valueOf(value));
          }
 
          @Override
          public String getAsString(FacesContext context,
                UIComponent component, Object value)
          {
 
             if (value == null)
             {
                return "";
             }
 
             return String.valueOf(((Formulario) value).getId());
          }
       };
    }
    
    public String xmlView (){
 	   InputStream xmlStream=null;	    
 	   xmlStream = fileUtils.getXmlInputStream(formulario);
 	   if (null != xmlStream){		   
 		   formDom = fileUtils.format(xmlStream);		   
 	   } else {
 		   String msgText = "Archivo "+ formulario.getArchivo() +" no encontrado en: ";
 		   if (null != formulario.getUrl() && !formulario.getUrl().isEmpty())
 			   msgText = msgText.concat(formulario.getUrl());	
 		   else msgText = msgText.concat(DomUtils.getXMLFROM());
 		   FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", 
 					  msgText);  
 	       FacesContext.getCurrentInstance().addMessage(null, msg);	       
 	   }
 	   return null;	   	   
    }
 
    /*
     * Support adding children to bidirectional, one-to-many tables
     */
 
    private Formulario add = new Formulario();
 
    public Formulario getAdd()
    {
       return this.add;
    }
 
    public Formulario getAdded()
    {
       Formulario added = this.add;
       this.add = new Formulario();
       return added;
    }
 
    public void handleFileUpload(FileUploadEvent event) {  
        FacesMessage msg = new FacesMessage(event.getFile().getFileName() + " subido correctamente!");  
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void transform(){
 	   InputStream xmlStream=null;   
 	   InputStream xslStream=null;
 	   FacesContext fc = FacesContext.getCurrentInstance();
 	   
 	   Map<String,String> requestParams = fc.getExternalContext().getRequestParameterMap();	   
        String idForm = requestParams.get("id");
        
 	   ExternalContext ec = fc.getExternalContext();
 	   try {
 		    String transformedHtml="";
 		    if (null != idForm && !idForm.isEmpty()) {
 			    if (isNum(idForm)){
 			    	Long xmlId = Long.valueOf(idForm);
 			    	this.formulario = findById(xmlId);
 			    } else {
 			    	try {
 						this.formulario = findByCode(idForm);
 					} catch (NoResultException e) {
 						//No existe un formulario con ese codigo
 						log.error("Formulario no existente");					
 					}
 			    }
 		    }
 	    	if (null != this.formulario) {
 		 	    xmlStream = fileUtils.getXmlInputStream(formulario);
 		 	    xslStream = fileUtils.loadXsl(formulario.getXslTransform()); 
 		    	if (null != xmlStream && null != xslStream) {
 		 	    	ts.setRemoteTransformation(false);
 		 	    	transformedHtml = ts.transform(xmlStream, xslStream, formulario.getFormVersion());
		 	    	transformedHtml = transformedHtml.replaceAll("___context___", fc.getExternalContext().getContextName());
 		        } else {
 		        	transformedHtml = "<html><head><meta content='text/html;charset=UTF-8' http-equiv='Content-Type'><title>Error</title>" +
 		        			"</head><body><h1>Se ha producido un error</h1><p> No es posible renderizar el html del formulario debido a que uno/s de los archivos " +
 		        			"<em>xml o xsl</em> no se ha/n encontrado.</p><hr/></body></html>";
 		        }
 	    	} else {
 	    		transformedHtml = "<html><head><meta content='text/html;charset=UTF-8' http-equiv='Content-Type'><title>Error</title>" +
 	        			"</head><body><h1>Formulario no encontrado</h1><p> No se ha encontrado formulario con c&oacute;digo/id: " +
 	        			"<em><b>'"+idForm+"'</em></b>.</p><hr/></body></html>";
 	    	}
 			ec.responseReset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
 			OutputStream output = ec.getResponseOutputStream();
 			output.write(transformedHtml.getBytes()); 
 		} catch (MalformedURLException e1) {
 			e1.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	    fc.responseComplete(); // Important! Otherwise JSF will attempt to render the response which obviously will fail since it's already written with a file and closed.
    }
 	    
 	private static boolean isNum(String strNum) {
 	    boolean ret = true;
 	    try {	
 	        Double.parseDouble(strNum);	
 	    } catch (NumberFormatException e) {
 	        ret = false;
 	    }
 	    return ret;
 	}
 }
