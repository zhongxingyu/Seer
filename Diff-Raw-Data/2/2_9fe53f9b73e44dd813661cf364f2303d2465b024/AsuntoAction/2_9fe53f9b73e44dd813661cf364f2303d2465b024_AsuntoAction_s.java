 package action;
 
 
 
 
 import java.util.Iterator;
 import java.util.List;
 
 import service.AsuntoService;
 import service.PaqueteBusinessDelegate;
 import service.RequisitoService;
 import bean.AsuntoDTO;
 import bean.RequisitoDTO;
 
 import com.opensymphony.xwork2.ActionSupport;
 
 
 public class AsuntoAction extends ActionSupport {
 
 	/**
 	declaracion de variables 
 	 */
 	private static final long serialVersionUID = 1L;
	 
 	private AsuntoDTO asunto;
 	private RequisitoDTO requisito;
 	private List<AsuntoDTO> asuntos;
 	private List<RequisitoDTO> requisitos;
 	private String codigo_tramite;
 	private AsuntoService servicioAsunto = PaqueteBusinessDelegate.getAsuntoService();
 	private RequisitoService servicioRequisito = PaqueteBusinessDelegate.getRequisitoService();
 	
 private String cod_requisito;
 	
 	public String getCod_requisito() {
 	return cod_requisito;
 }
 public void setCod_requisito(String cod_requisito) {
 	this.cod_requisito = cod_requisito;
 }
 	/**
 	 Declaracion de getters y setters
 	 */
 	
 	
 	
 	public AsuntoDTO getAsunto() {
 		return asunto;
 	}
 	public RequisitoDTO getRequisito() {
 		return requisito;
 	}
 	public void setRequisito(RequisitoDTO requisito) {
 		this.requisito = requisito;
 	}
 	public List<RequisitoDTO> getRequisitos() {
 		return requisitos;
 	}
 	public void setRequisitos(List<RequisitoDTO> requisitos) {
 		this.requisitos = requisitos;
 	}
 	public RequisitoService getServicioRequisito() {
 		return servicioRequisito;
 	}
 	public void setServicioRequisito(RequisitoService servicioRequisito) {
 		this.servicioRequisito = servicioRequisito;
 	}
 	public String getCodigo_tramite() {
 		return codigo_tramite;
 	}
 	public void setCodigo_tramite(String codigo_tramite) {
 		this.codigo_tramite = codigo_tramite;
 	}
 	public void setAsunto(AsuntoDTO asunto) {
 		this.asunto = asunto;
 	}
 	public List<AsuntoDTO> getAsuntos() {
 		return asuntos;
 	}
 	public void setAsuntos(List<AsuntoDTO> asuntos) {
 		this.asuntos = asuntos;
 	}
 	public AsuntoService getServicioAsunto() {
 		return servicioAsunto;
 	}
 	public void setServicioAsunto(AsuntoService servicioAsunto) {
 		this.servicioAsunto = servicioAsunto;
 	}
 	
 	/**
 	 Declaracion de metodo(registra)/service
 	 */
 	public String registra(){
 		String vista="exito";
 		
 		try{
 			
 			servicioAsunto.registraAsunto(asunto);
 		}catch (Exception e) {
 			e.printStackTrace();
 			
 		}
 		return vista;
 		
 	}
 	
 	public String insertaRequisitoAsunto(){
 		String vista="exito";
 		
 		try{
 			
 			
 			System.out.println("codigo asunto "+codigo_tramite);
 			System.out.println("codigo requisito "+cod_requisito);
 			//System.out.println(asunto.getCodigo_tramite()+"este es el codigo del asuntoDTO");
 		//	System.out.println(requisito.getCod_requisito()+"este es el codigo del requisitoDTO");
 			asunto=servicioAsunto.buscaAsunto(codigo_tramite);
 			requisito = servicioRequisito.buscaRequisito(cod_requisito);
 			
 		servicioAsunto.registraRequisitoAsunto(asunto,requisito);
 		}catch (Exception e) {
 			e.printStackTrace();
 			
 		}
 		return vista;
 		
 	}
 	
 	
 	/**
 	 Declaracion de metodo(lista)/service
 	 */
 	public String lista(){
 		String vista="exito";
 		try {
 			asuntos=servicioAsunto.listaAsuntoxNombre(asunto.getDescripcion());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return vista;
 	}
 
 	public String cargaModifica(){
 		String vista="exito";
 		try {
 			asunto = servicioAsunto.buscaAsunto(this.getCodigo_tramite());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return vista;
 	}
 	
 	public String cargaAgregaRequisito(){
 		String vista="exito";
 		try {
 			asunto = servicioAsunto.buscaAsunto(this.getCodigo_tramite());
 			requisitos= servicioRequisito.listaAreasxRequisito();
 			
 			if(requisitos==null){
 				System.out.println("no encontro requisitos");
 				System.out.println("tamao de la lista:"+requisitos.size());
 			}
 			if(requisitos!=null){
 				System.out.println("si encontro requisitos");
 				System.out.println("tamao de la lista:"+requisitos.size());
 				
 				Iterator it = requisitos.iterator();
 				while ( it.hasNext() ) {
 				Object objeto = it.next();
 				RequisitoDTO req = (RequisitoDTO)objeto;
 				
 				
 				System.out.println("Codigo :"+req.getCod_requisito());
 
 				System.out.println("Descripcion: "+req.getDesc_requisito());
 				
 				} 
 			} 
 			
 			
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return vista;
 	}
 	
 	  public String actualiza(){
 	    	String vista="exito";
 	    	try {
 				
 				
 				servicioAsunto.actualizaAsunto(asunto);
 			
 			} catch (Exception e) {
 				
 				e.printStackTrace();
 			}
 			return vista;
 	    }
 }
