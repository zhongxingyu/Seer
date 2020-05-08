 package cidc.certificaciones.servlet;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import cidc.certificaciones.db.CertificadoDB;
 import cidc.certificaciones.obj.CertificacionesOBJ;
 import cidc.certificaciones.obj.Parametros;
 import cidc.certificaciones.obj.ProyectoActividad;
 import cidc.general.db.CursorDB;
 import cidc.general.db.UsuarioDB;
 import cidc.general.login.Usuario;
 import cidc.general.servlet.ServletGeneral;
 import cidc.inscripSistema.obj.Persona;
 
 public class AdminCertificados extends ServletGeneral{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public String [] operaciones(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
 		//context=config.getServletContext();
 		int accion=0;
 		int perteneciaGrupo = 1;
 		int pazSalvo = 2;
 		mensaje="";
 		System.out.println(mensaje);
 		cursor=new CursorDB();
 		HttpSession sesion=req.getSession();
 		Persona persona=(Persona)sesion.getAttribute("persona");
 		Usuario usuario=(Usuario)sesion.getAttribute("loginUsuario");
 		UsuarioDB usuarioDB=new UsuarioDB(cursor,usuario.getPerfil());		
 		CertificacionesOBJ certificado=(CertificacionesOBJ)sesion.getAttribute("certificado");		
 		CertificadoDB certificadodb=new CertificadoDB(cursor,usuario.getPerfil()); 
 		if(req.getParameter("accion")!=null)
 			accion=Integer.parseInt(req.getParameter("accion"));
		else
			accion=5;
	
		String irA="/Certificados/GenerarCertificado.jsp";
 		switch(accion){
 		case Parametros.BUSCARCERTIFICADOSPERSONA: 
 			System.out.println("ID PERSONA --->"+usuario.getIdUsuario());
 			long personaid= buscarPersona(usuario.getIdUsuario(), persona, usuarioDB, sesion);
 			System.out.println(personaid);
 			//sesion.setAttribute("listacertificados",certificadodb.buscarCertificadosPersona(personaid,perteneciaGrupo));
 			irA="/Certificados/GenerarCertificado.jsp";
 			mensaje="Case Pertenencia a Grupo";
 			System.out.println(mensaje);
 		break;
 		case Parametros.PAZYSALVO: 
 			long personaid1= buscarPersona(usuario.getIdUsuario(), persona, usuarioDB, sesion);
 			if(certificadodb.consultarProyectosPaz(personaid1))
 				sesion.setAttribute("paz", true);
 			else
 				sesion.setAttribute("paz", false);
 			sesion.setAttribute("listacertificados",certificadodb.buscarCertificadosPersona(personaid1,pazSalvo));
 			irA="/Certificados/PazySalvo.jsp";
 			mensaje="Case Paz y Salvo";
 			System.out.println(mensaje);
 		break;
 		case Parametros.ACTIVIDADESINVESTIGACION:
 			personaid1= buscarPersona(usuario.getIdUsuario(), persona, usuarioDB, sesion);
 			List<ProyectoActividad> lista=new ArrayList<ProyectoActividad>();
 			lista=certificadodb.BuscarProyectoActividades((int)personaid1);
 			if(!lista.isEmpty()){
 				sesion.setAttribute("actividad", true);
 			}else{
 				sesion.setAttribute("actividad", false);
 			}
 			sesion.setAttribute("listaAct", lista);
 			irA="/Certificados/Actividades.jsp";
 			mensaje="Case Actividades de Investigacin";
 			System.out.println(mensaje);
 		break;		
 		case Parametros.CertPertenencia:
 			sesion.setAttribute("persona",persona);
 			irA="/certificaciones/CertificadosDoc.x?accion=1&accion2="+persona.getIdPersona();
 			mensaje="Hizo el mapeo del Certificado";	
 			System.out.println(irA);
 		break;
		case 5:
//			System.out.println("caso 05 --->");
//			personaid1= buscarPersona(usuario.getIdUsuario(), persona, usuarioDB, sesion);
//			System.out.println(personaid1);
			irA="/Certificados/BuscarCertificados.jsp";
//			mensaje="Busqueda de Certificados";		
			sesion.removeAttribute("listacertificados");
		break;
 		case Parametros.BUSCARCERTIFICADOS:
 			personaid1= buscarPersona(usuario.getIdUsuario(), persona, usuarioDB, sesion);
 			//persona=usuarioDB.getPersona(usuario.getIdUsuario());
 			sesion.setAttribute("persona",persona);
 			//personaid1= persona.getIdPersona();
 			String cedula=req.getParameter("cedula");
 			String cod_ver=req.getParameter("cod_verificacion");
 			String nombre=req.getParameter("nombre");
 			String apellido=req.getParameter("apellidos");
 			int tipoCert=Integer.parseInt(req.getParameter("tipos"));
 			System.out.println("CEDULA---->"+cedula+ " COD_VERIFICACIN--->"+cod_ver+ " NOMBRE--->"+nombre+ " APELLIDO--->"+apellido+ " TIPO_CERT--->"+tipoCert);
 			sesion.setAttribute("listacertificados",certificadodb.buscarCertificados(cedula, cod_ver,nombre,apellido,tipoCert));
 			System.out.println("Todo Bien");
 			//persona.setEstado(true);
 			//sesion.setAttribute("persona",persona);
 			irA="/Certificados/BuscarCertificados.jsp";
 			mensaje="Busqueda de Certificados";			
 		break;
 		case 7:		
 			personaid1= buscarPersona(usuario.getIdUsuario(), persona, usuarioDB, sesion);
 			irA="/Certificados/BuscarCertificados.jsp";
 			mensaje="Busqueda de Certificados";			
 		break;
 		case Parametros.DOCPAZYSALVO:
 			sesion.setAttribute("persona",persona);
 			irA="/certificaciones/CertificadosDoc.x?accion=5&accion2="+persona.getIdPersona();
 			mensaje="Hizo el mapeo del Certificado";	
 			System.out.println(irA);
 		break;
 		case Parametros.CREARCERTACTIVIDADES:
 			sesion.setAttribute("persona",persona);
 			System.out.println(req.getParameter("codProy"));
 			irA="/certificaciones/CertificadosDoc.x?accion=9&accion2="+req.getParameter("codProy");
 			mensaje="Hizo el mapeo del Certificado";	
 			System.out.println(irA);
 		}
 		accion=0;
 		retorno[0]="";
 		retorno[1]=irA;
 		retorno[2]=mensaje;
 		return retorno;	
 	}
 	
 	public long buscarPersona(long idUsuario,Persona persona, UsuarioDB usuarioDB, HttpSession sesion){
 		persona=usuarioDB.getPersona(idUsuario);
 		persona.setEstado(true);
 		sesion.setAttribute("persona",persona);
 		return persona.getIdPersona();
 		
 	}
 
 }
