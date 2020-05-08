 package cidc.proyectos.servlet;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.AddressException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.fileupload.DiskFileUpload;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 import cidc.adminInformes.obj.Parametros;
 import cidc.general.db.BaseDB;
 import cidc.general.db.CursorDB;
 import cidc.general.login.Usuario;
 import cidc.general.mails.EnvioMail2;
 import cidc.general.obj.CargarDocumento;
 import cidc.general.obj.Globales;
 import cidc.general.servlet.ServletGeneral;
 import cidc.proyectos.db.ProyectosInvestigadorDB;
 import cidc.proyectos.obj.ProyectoGenerico;
 import cidc.proyectosGeneral.db.ProyectosGeneralDB;
 import cidc.proyectosGeneral.obj.ExtraDocProyecto;
 import cidc.proyectosGeneral.obj.Proyecto;
 
 public class CargarInformes extends ServletGeneral{
	public static final Locale supportedLocales = new Locale("en", "US");
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	Usuario usuario = null;
 	CargarDocumento cargarDocumento=new CargarDocumento();
 	ProyectosGeneralDB proyectoGeneralDB = null;
 
 	public String [] operaciones(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
 		String irA="/grupos/proyectos/ListaProyectos.jsp";
 		cursor=new CursorDB();
 		int accion=0;
 		HttpSession sesion=req.getSession();
 		usuario=(Usuario)sesion.getAttribute("loginUsuario");
 		ProyectosGeneralDB proyGeneral=new ProyectosGeneralDB(cursor, usuario.getPerfil());
 		ProyectosInvestigadorDB proyectosDB=new ProyectosInvestigadorDB(cursor,usuario.getPerfil());
 		ProyectoGenerico proyecto =null;
 		String path=super.context.getRealPath(req.getContextPath()).substring(0,req.getRealPath(req.getContextPath()).lastIndexOf(sep));
 		if(sesion.getAttribute("proyectoInvestigador")!=null)
 			proyecto = (ProyectoGenerico)sesion.getAttribute("proyectoInvestigador");
 		ExtraDocProyecto docNuevo=new ExtraDocProyecto();
         DiskFileUpload fu = new DiskFileUpload();
 		FileItem archivoAdj=null;
         cursor=new CursorDB();
         proyectoGeneralDB=new ProyectosGeneralDB(cursor,usuario.getPerfil());
         mensaje="";        	
 
 		if (ServletFileUpload.isMultipartContent(req)){
 			List items=new ArrayList();
 			try {
 				items = fu.parseRequest(req);
 		        FileItem item=null;
 		        if(items.size()>0){
 			        Iterator iter = items.iterator();
 			        while (iter.hasNext()) {
 			            item = (FileItem) iter.next();
 			            if (item.isFormField()) {
 			            	if(item.getFieldName().equals("accion"))
 			            		accion=Integer.parseInt(item.getString());
 			            		docNuevo.setIdProyecto(proyecto.getIdProyecto());
 				            	if(item.getFieldName().equals("tipo")){
 				            		docNuevo.setTipo(Integer.parseInt(item.getString()));
 				            	}
 				            	if(item.getFieldName().equals("observaciones"))
 				            		docNuevo.setObservaciones(item.getString());
 			            }else{
 			            	archivoAdj=item;
 			            }
 			        }
 		        }
 			}catch (FileUploadException e) {
 				baseDB=new BaseDB(cursor,1);
 				baseDB.lanzaExcepcion(e);
 			} catch (Exception e) {
 				baseDB=new BaseDB(cursor,1);
 				baseDB.lanzaExcepcion(e);
 			}
         }else{
         	accion=Integer.parseInt(req.getParameter("accion"));
         }
 		String carpeta="";
 		String asunto="";
 		if(proyecto.getTipo()==1){
 			carpeta="Proyectos/Informes";
 		}
 		else {
 			if (docNuevo.getTipo()==3) {
 				carpeta = "ProyectosAntiguos/Informes";
 			}if (docNuevo.getTipo()==2)
 				carpeta = "ProyectosAntiguos/InformesFinales";
 		}
 		switch(accion){
 			case cidc.proyectos.obj.Parametros.cargarInforme:
 				String nombre="Informe_"+proyecto.getIdProyecto()+"_";
 				Proyecto proy =new Proyecto();
 				proy.setClaseProyecto(proyecto.getTipo());
 				proy.setId((int)proyecto.getIdProyecto());
 				docNuevo.setFechaDoc(new Globales().getFechaSimpleHoy());
 				if(proyectoGeneralDB.nuevaCargaDocProyecto(cargaDocumento(path,nombre, carpeta,archivoAdj,docNuevo,Parametros.insertarDocumentoActaFinalizacion,proy),proy,usuario.getIdUsuario())){
 					sesion.setAttribute("proyectoDocumentos", proyGeneral.getListaDocAnexos(((proyecto.getIdProyecto())),proyecto.getTipo()));
 					sesion.setAttribute("proyectoInvestigador", proyectosDB.getProyecto(String.valueOf(proyecto.getIdProyecto()),String.valueOf(proyecto.getTipo())));
 					mensaje="Documento Cargado Satisfactoriamente";
 				}else
 					mensaje="No se pudo completar la carga del documento \nFavor volver a intentar";
				ResourceBundle rb=ResourceBundle.getBundle("cidc.general.mails.NotificacionInforme",supportedLocales);
 				EnvioMail2 envioMail=new EnvioMail2("siciud");
 				StringBuffer texto=new StringBuffer();
 				texto.append(rb.getString("t1")+"");
 				texto.append(rb.getString("t2")+"");
 				texto.append(rb.getString("t3")+"");
 				texto.append(rb.getString("t4")+usuario.getNombre());
 				texto.append(rb.getString("t10"));
 				texto.append(rb.getString("t5")+proyecto.getNombre());
 				texto.append(rb.getString("t10"));
 				texto.append(rb.getString("t6")+proyecto.getCodigo());
 				texto.append(rb.getString("t10"));
 				texto.append(rb.getString("t7"));
 				texto.append(rb.getString("t8"));
 				texto.append(rb.getString("t9"));
 				String[] destinaratio= new String[1];
 				destinaratio[0]=rb.getString("t11");
 				if (docNuevo.getTipo()==2)
 					asunto="Informe Final";
 				if(docNuevo.getTipo()==3)
 					asunto="Informe Parcial";
 			try {
 				envioMail.enviar(destinaratio,asunto,""+texto);
 			} catch (AddressException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (MessagingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			irA="/grupos/proyectos/VerProyecto.jsp";
 				break;
 		}
 
 		retorno[0]="desviar";
 		retorno[1]=irA;
 		retorno[2]=mensaje;
 		return retorno;
 		}
 	
 	public ExtraDocProyecto cargaDocumento(String path, String nombre,String carpeta,FileItem archivoAdj, ExtraDocProyecto documento, int tipo, Proyecto proyecto){ 
 		cursor=new CursorDB();
 		if(documento!=null){			
 			 try {
 				 documento.setNombreArchivo(cargarDocumento.cargarGenerico(path,archivoAdj,carpeta,nombre,proyectoGeneralDB.getIdNuevoDoc(tipo,proyecto.getClaseProyecto())));
 				  mensaje="Documento Cargado Satisfactoriamente";
 		     } catch (Exception e) {
 				// TODO Auto-generated catch block
 				baseDB=new BaseDB(cursor,1);
 				baseDB.lanzaExcepcion(e);
 				mensaje="No se pudo completar la carga del documento\nFavor volver a intentar";
 			}
 	       // req.setAttribute("archivos",inscripcionConvDB.getInfoArchivos(""+idProp));		        
 		}
 		return documento;
 	}
 
 }
