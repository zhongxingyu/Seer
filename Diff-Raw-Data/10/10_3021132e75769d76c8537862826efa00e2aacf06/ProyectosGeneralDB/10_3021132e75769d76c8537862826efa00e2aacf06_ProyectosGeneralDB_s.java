 package cidc.proyectosGeneral.db;
 /**
  * @author Oscar Javier ngel Snchez
 
  * @version 1.0
  * 
  * */
 
 import java.io.IOException;
 
 import java.math.BigInteger;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import cidc.adminGrupos.obj.GrupoInvestigacion;
 import cidc.proyectosGeneral.obj.ExtraDocProyecto;
 import cidc.general.db.BaseDB;
 import cidc.general.db.CursorDB;
 import cidc.general.login.Usuario;
 import cidc.general.obj.Globales;
 import cidc.general.pdf.DocumentosPDF;
 import cidc.inscripcionConv.obj.CoInvestigadorOBJ;
 //import cidc.inscripcionConv.obj.CompromisosOBJ;
 import cidc.inscripcionConv.obj.GruposOBJ;
 import cidc.inscripcionConv.obj.ProyCurOBJ;
 import cidc.inscripcionConv.obj.ResumenCompromOBJ;
 import cidc.inscripcionConv.obj.ResumenInscOBJ;
 import cidc.inscripcionConv.obj.ResumenRubrosOBJ;
 import cidc.proyectosAntiguos.obj.DatosAjax;
 import cidc.proyectosGeneral.obj.Devolutivo;
 import cidc.proyectosGeneral.obj.CompromisosOBJ;
 import cidc.proyectosGeneral.obj.BalanceGeneral;
 import cidc.proyectosGeneral.obj.GastosRubro;
 import cidc.proyectosGeneral.obj.GeneralOBJ;
 import cidc.proyectosGeneral.obj.ObservacionObj;
 import cidc.proyectosGeneral.obj.Rubros;
 import cidc.proyectosGeneral.obj.CoInvest;
 import cidc.proyectosGeneral.obj.Proyecto;
 import cidc.proyectosGeneral.obj.FiltroGeneralProyecto;
 import cidc.proyectosGeneral.obj.Tiempos;
 
 public class ProyectosGeneralDB extends BaseDB {
 
 	public static char sep=java.io.File.separatorChar;
 	public String nombreRubro=null;
 	
 	public String getNombreRubro() {
 		return nombreRubro;
 	}
 	
 	public ProyectosGeneralDB(CursorDB c, int perfil) {
 		super(c, perfil);
 		rb=ResourceBundle.getBundle("cidc.proyectosGeneral.consultas");
 	}
 
 	public List<Proyecto> getListaProyectos(FiltroGeneralProyecto filtro){
 		List<Proyecto> lista=new ArrayList<Proyecto>();
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		Proyecto datos=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("filtro_proyectos"));
 			
 			ps.setString(i++, filtro.getFacultad());
 			ps.setString(i++, filtro.getProyCur());
 			ps.setString(i++, filtro.getGrupo());
 			ps.setString(i++, filtro.getCodigo());
 			ps.setString(i++, filtro.getEstado());
 			ps.setString(i++, filtro.getTipoGrupo());
 			ps.setString(i++, filtro.getTipoProyecto());
 			ps.setString(i++, filtro.getNombreProyecto());	
 			ps.setString(i++, filtro.getPalabrasClaves());
 
 			ps.setString(i++, filtro.getFacultad());
 			ps.setString(i++, filtro.getProyCur());
 			ps.setString(i++, filtro.getGrupo());
 			ps.setString(i++, filtro.getCodigo());
 			ps.setString(i++, filtro.getEstado());
 			ps.setString(i++, filtro.getTipoGrupo());
 			ps.setString(i++, filtro.getTipoProyecto());
 			ps.setString(i++, filtro.getNombreProyecto());
 			ps.setString(i++, filtro.getPalabrasClaves());
 			
 			rs=ps.executeQuery();
 			i=1;
 			while(rs.next()){
 				i=1;
 				datos=new Proyecto();
 				datos.setClaseProyecto(rs.getInt(i++));
 				datos.setId(rs.getInt(i++));
 				datos.setCodigo(rs.getString(i++));
 				datos.setDirector(rs.getString(i++));
 				datos.setProyecto(rs.getString(i++));
 				datos.setFlag(rs.getInt(i++));				
 				lista.add(datos);
 	//			System.out.println("Apro");
 			}
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return lista;
 	}
 
 	public Proyecto buscarProyecto(String id,String tipo){
 		Proyecto proyecto=null;
 		
 		if(tipo.equals("1"))
 			proyecto=getVerProyectoNormal(id);
 		else
 			proyecto=getVerProyectoDigitalizado(id);
 		
 		if(proyecto!=null){
 			proyecto.setListaCoInvestigadores(getListaCoInvestigadores(proyecto.getIdPropuesta(),tipo));
 			proyecto.setListaObservaciones(getListaObservaciones(Long.parseLong(id),Integer.parseInt(tipo)));
 			proyecto.setListaCompromisos(getListaCompromisos(proyecto.getIdPropuesta()));
 			proyecto.setListaTiempos(getListaTiempos(proyecto));			
 		}
 		return proyecto;
 	}
 	
 	public Proyecto getVerProyectoDigitalizado(String id) {
 		Proyecto proyecto=null;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		Globales global=new Globales();
 		int i=1;
 		try {
             cn = cursor.getConnection(super.perfil);
             ps = cn.prepareStatement(rb.getString("verProyecto2"));
             ps.setLong(1, Long.parseLong(id));
             rs = ps.executeQuery();
             while (rs.next()){
             	i=1;
             	proyecto=new Proyecto();
             	proyecto.setClaseProyecto(2);            	
 				proyecto.setId(Integer.parseInt(id));				
 				proyecto.setIdPropuesta(Integer.parseInt(id));
 				proyecto.setTipoProyecto(rs.getInt(i++));
 				proyecto.setCodigo(rs.getString(i++));
 				proyecto.setProyecto(rs.getString(i++));
 				proyecto.setFacultad(rs.getString(i++));				
 				proyecto.setProyCurricular(rs.getString(i++));
 				proyecto.setDirector(rs.getString(i++));
 				proyecto.setAno(rs.getInt(i++));		
 				proyecto.setFecAprobacion(rs.getString(i++));
 				proyecto.setNumConvocatoria(rs.getString(i++));
 				proyecto.setConvocatoria(rs.getString(i++));				
 				proyecto.setValor(rs.getString(i++));
 				proyecto.setDuracion(rs.getString(i++));
 				proyecto.setIdGrupo(rs.getInt(i++));				
 				proyecto.setGrupoInvestigacion(rs.getString(i++));
 				proyecto.setEstado(rs.getInt(i++));				              
 				proyecto.setCompromisosConv(rs.getString(i++));
 				proyecto.setFlag(rs.getInt(i++));
 				
            }
        } catch (SQLException e) {lanzaExcepcion(e);}
 	       catch (Exception e) {lanzaExcepcion(e);}
        finally {
            try {
             rs.close();
             ps.close();
             cn.close();
            }
            catch (SQLException e){}
            }
 		return proyecto;
 	}
 	
 	public Proyecto getVerProyectoNormal(String id) {
 		Proyecto proyecto=null;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		Globales g= new Globales();
 		String fechas=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("verProyecto1"));
 			ps.setLong(1, Long.parseLong(id));
 			rs=ps.executeQuery();
 			while(rs.next()){
 				i=1;
 				proyecto=new Proyecto();
 				proyecto.setClaseProyecto(1);
 				proyecto.setTipoProyecto(1);
 				proyecto.setId(Integer.parseInt(id));
 				proyecto.setCodigo(rs.getString(i++));
 				proyecto.setIdPropuesta(rs.getInt(i++));
 				proyecto.setProyecto(rs.getString(i++));
 				proyecto.setFacultad(rs.getString(i++));
 				proyecto.setProyCurricular(rs.getString(i++));
 				proyecto.setDirector(rs.getString(i++));
 				proyecto.setCorreo(rs.getString(i++));
 				proyecto.setCelular(rs.getString(i++));
 				proyecto.setFecAprobacion(rs.getString(i++));
 				proyecto.setNumConvocatoria(rs.getString(i++));
 				proyecto.setConvocatoria(rs.getString(i++));				
 				proyecto.setDuracion(rs.getString(i++));
 				proyecto.setIdGrupo(rs.getInt(i++));
 				proyecto.setGrupoInvestigacion(rs.getString(i++));
 				proyecto.setArchivo(rs.getString(i++));
 				proyecto.setEstado(rs.getInt(i++));
 				proyecto.setConsecContrato(rs.getString(i++));
 				proyecto.setConsecActa(rs.getString(i++));
 				proyecto.setAno(rs.getInt(i++));
 				proyecto.setEjecucion(""+(Integer.parseInt(proyecto.getDuracion())-4));
 				proyecto.setInformes("1");
 				proyecto.setEvaluacion("3");
 				/********************************************/
 				
 				proyecto.setValor(calculoValor(cn,id));
 				
 				fechas=rs.getString(i++);//captura la fecha del contrato
 				if(fechas==null ||fechas.trim().equals(""))
 					proyecto.setFecContrato(g.getAnoHoy()+"-"+g.getMesHoy()+"-"+g.getDiaHoy());
 				else
 					proyecto.setFecContrato(fechas);
 				/********************************************/
 				fechas=null;
 				fechas=rs.getString(i++);//captura la fecha del acta de inicio
 				if(fechas==null ||fechas.trim().equals("")){
 					proyecto.setFecActaInicio(g.getAnoHoy()+"-"+g.getMesHoy()+"-"+g.getDiaHoy());
 				}
 				else{
 					proyecto.setFecActaInicio(fechas);
 				}
 				proyecto.setFlag(rs.getInt(i++));
 				proyecto.setTermRefConvo(rs.getString(i++));
 				proyecto.setFecActaFin(rs.getString(i++));
 				proyecto.setIdActaFin(rs.getInt(i++));
 				proyecto.setDocumento(rs.getString(i++));
 				/********************************************/		
 				
 				
 				
 			}
 			if(proyecto!=null){
 				String comp="";
 				if(proyecto.getNumConvocatoria().endsWith("2008"))
 					ps=cn.prepareStatement(rb.getString("compromisosConv2008"));
 				else
 					ps=cn.prepareStatement(rb.getString("compromisosConv2009"));
 				ps.setLong(1, Long.parseLong(id));
 				rs=ps.executeQuery();
 				while(rs.next()){
 					if(comp.equals(""))
 						comp=comp+rs.getString(1);
 					else
 						comp=comp+", "+rs.getString(1);
 				}
 				proyecto.setCompromisosConv(comp);
 			}
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 			cerrar(cn);
 		}
 
 		return proyecto;
 	}
 	
 	public List<CoInvest> getListaCoInvestigadores(int id,String tipo){
 		CoInvest coInvest=null;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		int i=1;
 		List<CoInvest> listaCoInve=new ArrayList <CoInvest>();
 		
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("getConInvestigadores"+tipo));
 			ps.setInt(1, id);
 			rs=ps.executeQuery();
 			//System.out.println("-->"+ps);
 			while(rs.next()){
 				i=1;
 				coInvest=new CoInvest();
 				coInvest.setId(rs.getInt(i++));
 				coInvest.setDocumento(rs.getString(i++));
 				coInvest.setNombre(rs.getString(i++));				
 				coInvest.setPapel(rs.getString(i++));
 				coInvest.setFechaInicio(rs.getString(i++));
 				coInvest.setFechaFin(rs.getString(i++));
 				listaCoInve.add(coInvest);
 			}
 		} catch (SQLException e) {
 			lanzaExcepcion(e);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return listaCoInve;
 	}
 	public List<CoInvest> getListaCoInvestigadorescontrato(int id,String tipo){
 		CoInvest coInvest=null;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		int i=1;
 		List<CoInvest> listaCoInve=new ArrayList <CoInvest>();
 		
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("getConInvestigadorescont"));
 			ps.setInt(1, id);
 			rs=ps.executeQuery();
 			//System.out.println("-->"+ps);
 			while(rs.next()){
 				i=1;
 				coInvest=new CoInvest();
 				coInvest.setId(rs.getInt(i++));
 				coInvest.setDocumento(rs.getString(i++));
 				coInvest.setNombre(rs.getString(i++));
 				coInvest.setApellido(rs.getString(i++));				
 				coInvest.setProyectocnombre(rs.getString(i++));
 				coInvest.setCodigo(rs.getString(i++));
 				listaCoInve.add(coInvest);
 			}
 		} catch (SQLException e) {
 			lanzaExcepcion(e);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return listaCoInve;
 	}
 	
 	public String calculoValor(Connection cn,String prop){
 		Globales g=new Globales();
 		BigInteger cidc=BigInteger.valueOf(0);
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		String dato="";
 		try {
 			ps=cn.prepareStatement(rb.getString("getResumenRubros"));
 			ps.setLong(1,Long.parseLong(prop));
 			rs=ps.executeQuery();
 			while(rs.next()){
 				dato=rs.getString(1);
 				if(dato!=null && !dato.equals("") && !dato.equals("0"))
 					cidc=cidc.add(new BigInteger(dato));
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 		}
 		System.out.println("Valor del proyecto de investigacin"+g.moneda(cidc.toString()));
 		return g.moneda(cidc.toString());
 	}
 	
 	
 	//Inserta la observacin del proyecto general
 	public boolean insertaObservacion(int idPro, int tipo, String observacion,long usuario) {
 		Connection cn=null;
 		PreparedStatement ps=null;
 		boolean retorno = false;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("insertaObservacion"+tipo));
 			ps.setLong(i++,idPro);
 			ps.setString(i++,observacion);
 			ps.setLong(i++,usuario);
 			ps.executeUpdate();
 			retorno = true;
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
 	}
 	
 	public List<ObservacionObj> getListaObservaciones(long idPro,int tipo) {
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		Connection cn=null;
 		List <ObservacionObj> lista=new ArrayList <ObservacionObj>();
 		ObservacionObj observ=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("getObservacionesProy"+tipo));
 			ps.setLong(1,idPro);
 			if(tipo==2)
 				ps.setLong(2,idPro);
 			rs=ps.executeQuery();
 			while(rs.next()){
 				i=1;
 				observ= new ObservacionObj();
 				observ.setIdObservacion(rs.getLong(i++));
 				observ.setFecha(rs.getString(i++));
 				observ.setObservacion(rs.getString(i++));
 				observ.setUsuario(rs.getString(i++));
 				lista.add(observ);
 			}
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return lista;
 	}
 	
 	public boolean cambiaEstado(int idPro, int tipo, String estado ) {
 		Connection cn=null;
 		PreparedStatement ps=null;
 		boolean retorno = false;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("cambiaEstadoProyecto"+tipo));
 			ps.setInt(i++,Integer.parseInt(estado));
 			ps.setLong(i++,idPro);
 			ps.executeUpdate();
 			retorno = true;
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
 	}
 
 	public boolean actualizarFlag(int idPro,int tipo, String flag) {
 		Connection cn=null;
 		PreparedStatement ps=null;
 		boolean retorno = false;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("actualizarFlag"+tipo));
 			ps.setInt(1,Integer.parseInt(flag));
 			ps.setLong(2,idPro);
 			ps.executeUpdate();
 			retorno = true;
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
 	}
 	
 	public int getIdNuevoDoc(int tipoId, int tipoProyecto){
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		int id=0;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("getIdDocumento"+tipoId+tipoProyecto));
 			rs=ps.executeQuery();
 			while(rs.next()){
 				id=rs.getInt(1)+1;
 			}
 			id=id-1;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return id;
 	}
 	
 	public boolean nuevaCargaDocProyecto(ExtraDocProyecto documento, Proyecto proyecto,long idUsuario) {
 		boolean retorno=false;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		boolean noGenerico=true;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			cn.setAutoCommit(false);
 			if(proyecto.getClaseProyecto()==2){
 				switch (documento.getTipo()) {
 					case 1:
 					case 3:
 						noGenerico=true;//el case 1 y 3 no son tan necesarios pero los dejo por si acaso		
 					break;
 					case 2:
 						ps=cn.prepareStatement(rb.getString("cargaInformeFinalProyecto2"));
 						ps.setString(i++, documento.getNombreArchivo());
 						ps.setString(i++, documento.getFechaDoc());
 						ps.setString(i++, documento.getObservaciones());
 						ps.setInt(i++, proyecto.getId());
 						ps.executeUpdate();
 						System.out.println(ps.toString());
 						retorno=true;
 						noGenerico=false;
 					break;
 					case 4://si es caso 3 o 4 es el mismo procedimiento
 					case 5:
 						ps=cn.prepareStatement(rb.getString("cargaActaCierre2"));
 						ps.setString(i++, documento.getNombreArchivo());
 						ps.setString(i++, documento.getFechaDoc());
 						ps.setString(i++, documento.getObservaciones());
 						ps.setInt(i++, proyecto.getId());
 						ps.executeUpdate();
 						retorno=true;
 						noGenerico=false;
 					break;
 				
 				}
 			}
 			i=1;
 			if(noGenerico){
 				if(documento.getTipo()==3)
 					documento.setNombreDocumento("Informe Parcial");
 				if(documento.getTipo()==2)
 					documento.setNombreDocumento("Informe Final");
 				
 				ps=cn.prepareStatement(rb.getString("nuevaCargaDocProyecto"+proyecto.getClaseProyecto()));
 				ps.setLong(i++, proyecto.getId());			
 				ps.setString(i++, documento.getNombreDocumento());
				ps.setString(i++, documento.getFechaDoc());
 				ps.setString(i++, documento.getNombreArchivo());						
 				ps.setString(i++, documento.getObservaciones());
 				ps.setInt(i++, documento.getTipo());
 				ps.setInt(i++, documento.getEstado());
 				ps.setLong(i++, idUsuario);
 				System.out.println("consulta: "+ps.toString());
 				ps.executeUpdate();
 				retorno=true;
 			}
 			i=1;
 			
 			if(documento.getTipo()==4 || documento.getTipo()==5){
 				ps=cn.prepareStatement(rb.getString("cambiaEstadoProyecto"+proyecto.getClaseProyecto()));
 				if(documento.getTipo()==4) 	ps.setInt(i++, 3);
 				if(documento.getTipo()==5) 	ps.setInt(i++, 4);
 				ps.setInt(i++, proyecto.getId());			
 				ps.executeUpdate();
 			//	System.out.println(".--- sentenci de cambio--->"+ps);
 			}
 			cn.commit();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
 	}
 	
 	public boolean actualizaEstadoInforme(String idInforme,String estado,Proyecto proyecto) {
 		boolean retorno=false;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("cambiaEstadoInforme"+proyecto.getClaseProyecto()));
 			ps.setInt(i++, Integer.parseInt(estado));
 			ps.setLong(i++, Integer.parseInt(idInforme));
 			ps.executeUpdate();
 			retorno=true;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
 	}
 	
 	public List<ExtraDocProyecto> getListaDocAnexos(long idProyecto,int tipoProyecto) {
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		List<ExtraDocProyecto> lista=new ArrayList<ExtraDocProyecto>();
 		ExtraDocProyecto documento= null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("listaDocsProyecto"+tipoProyecto));
 			ps.setLong(i++, idProyecto);
 			if(tipoProyecto==2){
 				ps.setLong(i++, idProyecto);
 				ps.setLong(i++, idProyecto);
 				ps.setLong(i++, idProyecto);
 				ps.setLong(i++, idProyecto);
 				ps.setLong(i++, idProyecto);
 				ps.setLong(i++, idProyecto);
 			}
 			
 			rs=ps.executeQuery();
 			while(rs.next()){
 				i=1;
 				documento=new ExtraDocProyecto();			
 				documento.setNombreDocumento(rs.getString(i++));
 				documento.setFechaDoc(rs.getString(i++));
 				documento.setNombreArchivo(rs.getString(i++));
 				documento.setObservaciones(rs.getString(i++));	
 				documento.setTipo(rs.getInt(i++));
 				documento.setEstado(rs.getInt(i++));
 				documento.setFechaCarga(rs.getString(i++));
 				documento.setUsuario(rs.getString(i++));
 				documento.setTipoProyecto(tipoProyecto);
 				lista.add(documento);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return lista;
 	}
 	
 	public BalanceGeneral getBalanceProyecto(Proyecto proyecto){
 		BalanceGeneral balance=null;
 		Rubros rubros=null;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		List <Rubros> listaRubros=new ArrayList <Rubros>();
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("getRubrosProyecto"+proyecto.getClaseProyecto()));
 			ps.setLong(1,proyecto.getId());
 			rs=ps.executeQuery();
 			balance=new BalanceGeneral();
 			balance.setIdProyecto(proyecto.getId());
 			while(rs.next()){
 				i=1;
 				rubros=new Rubros();
 				rubros.setIdRubro(rs.getLong(i++));
 				rubros.setNombreRubro(rs.getString(i++));
 				rubros.setValorRubro(rs.getString(i++));
 				rubros.setListaGastos(getGastosRubro(cn,rubros.getIdRubro(),proyecto,rubros));
 				listaRubros.add(rubros);
 			}
 			
 			if(listaRubros.size()==0 && proyecto.getClaseProyecto()==2){
 				ps=cn.prepareStatement(rb.getString("getRubrosProyecto"+proyecto.getClaseProyecto()+"Alterna"));
 				ps.setLong(1,proyecto.getId());
 				rs=ps.executeQuery();
 				while(rs.next()){
 					i=1;
 					rubros=new Rubros();
 					rubros.setIdRubro(rs.getLong(i++));
 					rubros.setNombreRubro(rs.getString(i++));
 					rubros.setValorRubro(rs.getString(i++));
 					rubros.setListaGastos(getGastosRubro(cn,rubros.getIdRubro(),proyecto,rubros));
 					listaRubros.add(rubros);
 				}
 			}
 			BigInteger totalAprobado=new BigInteger("0");
 			BigInteger totalEjecutado=new BigInteger("0");
 			BigInteger totalSaldo=new BigInteger("0");
 			for(int x=0;x<listaRubros.size();x++){
 				rubros=listaRubros.get(x);
 				
 				//MODIFIQU ESTA LNEA
 				totalAprobado=totalAprobado.add(new BigInteger(rubros.getValorRubro()));				
 				//totalAprobado=totalAprobado.add(new BigInteger(global.sinMoneda(rubros.getValorRubro())));
 				rubros.setValorRubro(global.moneda(rubros.getValorRubro()));
 				totalEjecutado=totalEjecutado.add(new BigInteger(global.sinMoneda(rubros.getValorEjecutado())));
 				totalSaldo=totalSaldo.add(new BigInteger(global.sinMoneda(rubros.getValorSaldo())));
 			}
 			balance.setTotalAprobado(global.moneda(totalAprobado.toString()));
 			balance.setTotalEjecutado(global.moneda(totalEjecutado.toString()));
 			balance.setTotalSaldo(global.moneda(totalSaldo.toString()));			
 			balance.setListaRubros(listaRubros);
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return balance;
 	}
 	public List<GastosRubro> getGastosRubro(Connection cn,long idRubro, Proyecto proyecto, Rubros rubros){
 		List<GastosRubro> listaRubros=new ArrayList<GastosRubro>();
 		GastosRubro gastos=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		BigInteger acumulado=BigInteger.valueOf(0);
 		BigInteger acumulado1=BigInteger.valueOf(0);
 		Globales global=new Globales();
 		int i=1;
 		try {
 			ps=cn.prepareStatement(rb.getString("getGastosRubro"+proyecto.getClaseProyecto()));
 			ps.setLong(i++,idRubro);
 			ps.setLong(i++,proyecto.getId());
 			rs=ps.executeQuery();
 			System.out.println("-->"+ps);
 			gastos=new GastosRubro();
 			gastos.setIdRubro(idRubro);
 			while(rs.next()){
 				i=1;
 				gastos=new GastosRubro();
 				gastos.setIdGasto(rs.getString(i++));
 				gastos.setValorGasto(rs.getString(i++));
 				gastos.setDescripcion(rs.getString(i++));
 				gastos.setFecha(rs.getString(i++));
 				gastos.setTipoGasto(rs.getInt(i++));
 				gastos.setCodigo(rs.getString(i++));
 				gastos.setObservaciones(rs.getString(i++));				
 				gastos.setUbicacion(rs.getString(i++));
 				gastos.setGrupoAcargo(rs.getInt(i++));
 				gastos.setObservacionEntrega(rs.getString(i++));
 				gastos.setFechaEntrega(rs.getString(i++));				
 				listaRubros.add(gastos);
 		//		if(gastos.getTipo()==1)
 				acumulado=acumulado.add(new BigInteger(""+(Integer.parseInt(global.sinMoneda(gastos.getValorGasto()))*gastos.getTipoGasto())));				
 			}
 			BigInteger a= new BigInteger(""+rubros.getValorRubro());			
 			//rubros.setValorRubro(global.moneda(""+rubros.getValorRubro()));
 			rubros.setValorSaldo(global.moneda(""+a.subtract(acumulado)));			
 			rubros.setValorEjecutado(global.moneda(""+acumulado));
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 		}
 		return listaRubros;
 	}
 	
 	
 	public List<Rubros> getGastosRubro(BalanceGeneral general,String idRub) {
 		Rubros rubro=null;		
 		List<Rubros> gastosRubro=null;
 		Iterator itRubros=general.getListaRubros().iterator();
 		while (itRubros.hasNext()) {
 			rubro=(Rubros)itRubros.next();
 			if(rubro.getIdRubro()==Long.parseLong(idRub)){
 				nombreRubro=rubro.getNombreRubro();
 				gastosRubro=rubro.getListaGastos();
 			}
 		}
 		return gastosRubro;
 	}
 	
 	public boolean eliminarGasto(String idGast,Proyecto proyecto) {
 		boolean retorno=false;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("eliminaGasto"+proyecto.getClaseProyecto()));
 			ps.setLong(i++,Long.parseLong(idGast));
 			ps.executeUpdate();
 			retorno=true;
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
 	}
 	
 	
 	//
 	public List<GastosRubro> getElementosInventarioRubro(BalanceGeneral general,String idRub) {
 		Rubros rubro=null;
 		List<GastosRubro> gastosRubro=null,retorno=new ArrayList<GastosRubro>();
 		Iterator itRubros=general.getListaRubros().iterator();
 		while (itRubros.hasNext()) {
 			rubro=(Rubros)itRubros.next();
 			if(rubro.getIdRubro()==Long.parseLong(idRub)){
 				gastosRubro=rubro.getListaGastos();
 				for(int i=0;i<gastosRubro.size();i++){					
 					GastosRubro gasto=(GastosRubro)gastosRubro.get(i);
 					//System.out.println("--encuentra devolutivo--->");
 					System.out.println("gasto " +gasto);
 					if(gasto.getTipoGasto()==1 && (gasto.getFechaEntrega()!=null || gasto.getFechaEntrega()!="")){
 						retorno.add(gasto);
 					}
 				}
 			}
 		}
 		return retorno;
 	}
 	
 	public List<GrupoInvestigacion> getListaTotalGrupos() {
 		List<GrupoInvestigacion> listaGrupos= new ArrayList <GrupoInvestigacion>();
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		GrupoInvestigacion grupo=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil); 
 			ps=cn.prepareStatement(rb.getString("listaTotalGrupos"));
 			rs=ps.executeQuery();
 			while(rs.next()){
 				i=1;
 				grupo=new GrupoInvestigacion();
 				grupo.setCodigo(rs.getLong(i++));
 				grupo.setNombre(rs.getString(i++).toLowerCase());
 				grupo.setDirNombre(rs.getString(i++));
 				grupo.setEstado(rs.getInt(i++));
 				listaGrupos.add(grupo);
 			}
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 			cerrar(cn);
 		}
 		
 		return listaGrupos;
 	}
 	
 	//Funcion que permite hacer entrega de documentos a proyectos
 	public boolean entregarElementosProyecto(Devolutivo listaElementos,Proyecto proyecto) {
 		Connection cn=null;
 		PreparedStatement ps=null;
 		boolean retorno = false;
 		int i=1;
 		try {
 			if(listaElementos.getIdGasto()!=null && listaElementos.getUbicar()!=null){
 				cn=cursor.getConnection(super.perfil);
 				ps=cn.prepareStatement(rb.getString("EntergaElementoProyecto"+proyecto.getClaseProyecto()));
 				for(int x=0;x<listaElementos.getIdGasto().length;x++){
 					i=1;
 					if(listaElementos.getFechaEntrega()[x]!=null && !listaElementos.getFechaEntrega()[x].equals("")&& !listaElementos.getFechaEntrega()[x].equals(" ")){
 						ps.setString(i++, listaElementos.getUbicar()[x]);
 						if(listaElementos.getGrupoAcargo()[x]!=0)
 							ps.setLong(i++, listaElementos.getGrupoAcargo()[x]);
 						else
 							ps.setLong(i++, Types.NULL);
 						ps.setString(i++, listaElementos.getObservacionEntrega()[x]);						
 						ps.setString(i++,listaElementos.getFechaEntrega()[x]);											
 						ps.setString(i++,listaElementos.getCodigo()[x]);
 						if(listaElementos.getIdGasto()[x]!=0)
 							ps.setLong(i++, listaElementos.getIdGasto()[x]);
 						else
 							ps.setLong(i++, Types.NULL);
 						ps.addBatch();
 					}
 				}
 				ps.executeBatch();
 				retorno = true;
 			}else
 				setMensaje("No hay elementos a Entregar");
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
 	}
 	
 	public List<GruposOBJ> AjaxGruposInvestigacion(int facultad){
 		List <GruposOBJ> l=new ArrayList<GruposOBJ>();
 		Connection cn=null;
 		PreparedStatement ps=null; 
 		ResultSet rs=null;
 		int i=0;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("ajaxGrupos"));
 			ps.setLong(1,facultad);
 			rs=ps.executeQuery();
 			while(rs.next()){
 				i=1;
 				GruposOBJ gruposOBJ=new GruposOBJ();
 				gruposOBJ.setCodigo(rs.getInt(i++));
 				gruposOBJ.setNombre(rs.getString(i++));
 				l.add(gruposOBJ);
 	//			System.out.println(gruposOBJ.getNombre());
 			}
 		//	System.out.println("la cantidad de grupos encontrados es:"+l.size());
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return l;
 	}
 	
 	public List<ProyCurOBJ> AjaxProyectoCur() {
 		List<ProyCurOBJ> l=new ArrayList <ProyCurOBJ>();
 		ProyCurOBJ proyCurOBJ=null;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		try {
 			cn = cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("ajaxProyectCur"));
 			rs=ps.executeQuery();
 			while(rs.next()){
 				proyCurOBJ=new ProyCurOBJ();
 				proyCurOBJ.setCodigo(rs.getInt(1));
 				proyCurOBJ.setNombre(rs.getString(2));
 				l.add(proyCurOBJ);
 			}
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return l;
 	}
 	public Rubros getRubro(BalanceGeneral general,String idRub){
 		Rubros rubro=null;
 		Iterator <Rubros> itRubros=general.getListaRubros().iterator();
 		while (itRubros.hasNext()) {
 			rubro=(Rubros)itRubros.next();
 			if(Long.parseLong(idRub)==rubro.getIdRubro())
 				break;
 			else
 				rubro=null;
 		}
 		return rubro;
 	}
 	
 	public boolean registraGasto(GastosRubro gasto,Proyecto proyecto) {
 		boolean retorno=false;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("registraGasto"+proyecto.getClaseProyecto()));
 			ps.setLong(i++,gasto.getIdProyecto());
 			ps.setLong(i++,gasto.getIdRubro());
 			ps.setDouble(i++,Double.parseDouble(gasto.getValorGasto()));
 			ps.setString(i++,gasto.getDescripcion());
 			ps.setInt(i++,gasto.getTipoGasto());
 			ps.setString(i++,gasto.getFecha());
 			ps.setInt(i++,gasto.getPara());
 			ps.setString(i++,gasto.getCodigo());
 			ps.setString(i++,gasto.getObservaciones());
 			ps.executeUpdate();
 		//	System.out.println("----->"+ps);
 			retorno=true;
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
 	}
 	public List<Rubros> consultarRubros(Proyecto proyecto) {
 		List<Rubros> listaRubros = new ArrayList<Rubros>();
 		 Connection cn = null;
 		 PreparedStatement ps = null;
 		 ResultSet rs = null;
 		 Rubros datos =null;
         try {
 			  cn = cursor.getConnection(super.perfil);
 			  ps = cn.prepareStatement(rb.getString("consultaRubros"));
 			  rs=ps.executeQuery();
 			  while(rs.next())
 			  {
 				  datos = new Rubros();
 				  datos.setIdRubro(rs.getInt(1));
 				  datos.setNombreRubro(rs.getString(2));
 				  listaRubros.add(datos);
 			  }
 		     } catch (SQLException e) {lanzaExcepcion(e);}
 		       catch (Exception e) {lanzaExcepcion(e);}
 		 finally
 		 {
 			 try{
 				 rs.close();
 				 ps.close();
 				 cn.close();
 			 }catch(Exception e){}
 		 }
 		return listaRubros;
 	}	
 	
 	public boolean modificarPresupuesto(Proyecto proyecto,String []idRubro,String []valorRubro){
 		boolean retorno=false;
 		 Connection cn = null;
 		 PreparedStatement ps = null;
 		 ResultSet rs = null;
 		 int i=1;
         try {
 			  cn = cursor.getConnection(super.perfil);
 			  cn.setAutoCommit(false);
 			  if(proyecto!=null)
 				  if(proyecto.getId()!=0){
 					  ps = cn.prepareStatement(rb.getString("eliminarRubros"+proyecto.getClaseProyecto()));
 					  ps.setInt(1, proyecto.getIdPropuesta());
 					  ps.executeUpdate();
 				  }
 			  ps = cn.prepareStatement(rb.getString("modificarRubros"+proyecto.getClaseProyecto()));
 			  for(int x=0;x<idRubro.length;x++){
 				  i=1;
 				  ps.setInt(i++, proyecto.getIdPropuesta());
 				  ps.setInt(i++, Integer.parseInt(idRubro[x]));
 				  ps.setDouble(i++, Double.parseDouble(global.sinMoneda(valorRubro[x])));
 				  ps.addBatch();
 			  }
 			  int [] x=ps.executeBatch();
 			  cn.commit();
 			  retorno=true;
 		     } catch (SQLException e) {lanzaExcepcion(e);}
 		       catch (Exception e) {lanzaExcepcion(e);}
 		 finally
 		 {
 			 try{
 				 rs.close();
 				 ps.close();
 				 cn.close();
 			 }catch(Exception e){}
 		 }
 		 return retorno;
 	}
 	
 	public List<Tiempos> getListaTiempos(Proyecto proyecto){
 		 List<Tiempos> listaTiempos = new ArrayList<Tiempos>();
 		 Connection cn = null;
 		 PreparedStatement ps = null;
 		 ResultSet rs = null;
 		 Tiempos tiempo =null;
 		 Globales global=new Globales();
 		 int i=1;
 		 if(proyecto.getClaseProyecto()==1){
 			 if(proyecto.getFecActaInicio()!=null && proyecto.getDuracion()!=null)
 					proyecto.setFecEstimadoFin(global.sumarMesesFecha(proyecto.getFecActaInicio(),Integer.parseInt(proyecto.getDuracion())));
 		 }else{
 			 if(proyecto.getFecAprobacion()!=null && proyecto.getDuracion()!=null)
 					proyecto.setFecEstimadoFin(global.sumarMesesFecha(proyecto.getFecAprobacion(),Integer.parseInt(proyecto.getDuracion())));
 		 }
        try {
 			  cn = cursor.getConnection(super.perfil);
 			  ps = cn.prepareStatement(rb.getString("getTiemposAdicionales"+proyecto.getClaseProyecto()));
 			  ps.setInt(1, proyecto.getId());
 			  rs=ps.executeQuery();
 			  while(rs.next())
 			  {
 				  i=1;
 				  tiempo = new Tiempos();
 				  tiempo.setIdTiempo(rs.getInt(i++));
 				  tiempo.setTipoTiempo(rs.getInt(i++));
 				  tiempo.setValorTiempo(rs.getInt(i++));
 				  tiempo.setDescripcion(rs.getString(i++));
 				  tiempo.setRegitradoPor(rs.getString(i++));
 				  tiempo.setFechaTiempo(rs.getString(i++));
 				  proyecto.setFecEstimadoFin(global.sumarMesesFecha(proyecto.getFecEstimadoFin(), tiempo.getValorTiempo()));
 				  listaTiempos.add(tiempo);
 			  }
 		     } catch (SQLException e) {lanzaExcepcion(e);}
 		       catch (Exception e) {lanzaExcepcion(e);}
 		 finally
 		 {
 			 try{
 				 rs.close();
 				 ps.close();
 				 cn.close();
 			 }catch(Exception e){}
 		 }
 		return listaTiempos;
 	}
 	
 	public boolean insertarTiempo(Tiempos tiempo,Proyecto proyecto,Usuario user) {
 		boolean retorno=false;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("insertarTiempo"+proyecto.getClaseProyecto()));
 			ps.setInt(i++,proyecto.getId());
 			ps.setInt(i++,tiempo.getTipoTiempo());
 			ps.setString(i++,tiempo.getFechaTiempo());
 			ps.setInt(i++,tiempo.getValorTiempo());
 			ps.setString(i++,tiempo.getDescripcion());
 			ps.setLong(i++,user.getIdUsuario());
 			ps.executeUpdate();
 		//	System.out.println("----->"+ps);
 			retorno=true;
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
 	}
 	
 	public boolean eliminarTiempo(String idTiempo,Proyecto proyecto) {
 		boolean retorno=false;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("eliminarTiempo"+proyecto.getClaseProyecto()));
 			ps.setInt(i++,Integer.parseInt(idTiempo));
 			ps.executeUpdate();
 		//	System.out.println("----->"+ps);
 			retorno=true;
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
 	}
 
 	public boolean registrarIntegrante(Proyecto proyecto, CoInvest integrante) {
 		System.out.println("Integrantes 1");
 		boolean retorno=false;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("registrarIntegrante"+proyecto.getClaseProyecto()));
 			ps.setInt(i++, proyecto.getIdPropuesta());
 			ps.setString(i++, integrante.getDocumento());
 			ps.setString(i++, integrante.getNombre());
 			ps.setString(i++, integrante.getApellido());
 			ps.setString(i++, integrante.getPapel());
 			ps.setString(i++, integrante.getFechaInicio());
 			ps.setString(i++, integrante.getFechaFin());
 			ps.executeUpdate();
 		//	System.out.println("----->"+ps);
 			retorno=true;
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
 	}
 	public boolean registrarIntegrante2(Proyecto proyecto, CoInvest integrante) {
 		System.out.println("Integrantes 2 Contrato");
 		boolean retorno=false;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("registrarIntegrantecontrato"));
 			ps.setInt(i++, proyecto.getIdPropuesta());
 			ps.setString(i++, integrante.getDocumento());
 			ps.setString(i++, integrante.getNombre());
 			ps.setString(i++, integrante.getApellido());
 			ps.setString(i++, integrante.getPapel());
 			//ps.setString(i++, integrante.getFechaInicio());
 			ps.setString(i++, integrante.getFechaFin());
 			ps.setInt(i++, integrante.getProyectocurricular());
 			ps.setString(i++, integrante.getCodigo());
 			ps.executeUpdate();
 		//	System.out.println("----->"+ps);
 			retorno=true;
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
 	}
 	
 	
 	public boolean eliminarIntegrante(String idCoinvest,Proyecto proyecto) {
 		boolean retorno=false;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("eliminaIntegrante"+proyecto.getClaseProyecto()));
 			ps.setInt(i++,Integer.parseInt(idCoinvest));
 			ps.executeUpdate();
 			retorno=true;
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
 	}
 	
 	public boolean actualizarIntegrante(Proyecto proyecto, CoInvest integrante) {
 		boolean retorno=false;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("actualizaIntegrante"+proyecto.getClaseProyecto()));
 			ps.setString(i++,integrante.getFechaInicio());
 			ps.setString(i++,integrante.getFechaFin());
 			ps.setInt(i++,integrante.getId());
 		//	System.out.println("---actualiza-->"+ps);
 			ps.executeUpdate();
 			retorno=true;
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
 	}
 	
 	
 	// Revisar esta funcion, para generar el acta de inicio sin caracteres extraos
 	public Proyecto crearActaInicio(Proyecto proyecto,String path) {
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		DocumentosPDF actaInicio=null;
 		String consec=null;
 		String tabla=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("datosActaInicio"));
 			ps.setLong(1,proyecto.getId());
 			rs=ps.executeQuery();
 			while(rs.next()){
 				consec=rs.getString(i++);
 				tabla=rs.getString(i++);
 				if(tabla!=null)
 					proyecto.setConsecActa(tabla);
 				else
 					proyecto.setConsecActa(consec);
 				proyecto.setConsecContrato(rs.getString(i++));
 				path+=sep+"Documentos"+sep+"Proyectos"+sep+"Actas"+sep+"ActaInicio_"+proyecto.getConsecActa()+"_"+proyecto.getAnCortoActa()+".pdf";
 				actaInicio=new DocumentosPDF();
 				actaInicio.crearActaInicio(proyecto, path);
 
 				ps=cn.prepareStatement(rb.getString("act_consecutivo_acta"));
 				ps.setLong(1, Long.parseLong(proyecto.getConsecActa()));
 				ps.setString(2, proyecto.getFecActaInicio());
 				ps.setLong(3, proyecto.getId());
 				ps.execute();
 				if(tabla==null){
 					ps=cn.prepareStatement(rb.getString("ActaInicio++"));
 					ps.execute();
 				}
 				break;
 			}
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return proyecto;
 	}
 	
 	
 	public Proyecto crearContrato(Proyecto proyecto, String path) {
 		DocumentosPDF contrato=new DocumentosPDF();
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		long secuencia;
 		String tabla=null;//hace referencia al consecutivo almacenado en la tabla.. es distinto de null en caso de ya haber contrado hecho
 		int ano=0;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("getConsecContrato"));
 			ps.setLong(1,proyecto.getId());
 			rs=ps.executeQuery();
 			while(rs.next()){
 				secuencia=rs.getLong(1);
 				tabla=rs.getString(2);
 				ano=rs.getInt(3);
 				if(tabla!=null)
 					proyecto.setConsecContrato(tabla);
 				else
 					proyecto.setConsecContrato(""+secuencia);
 
 				path+=sep+"Documentos"+sep+"Proyectos"+sep+"Contratos"+sep+"Contrato_"+proyecto.getConsecContrato()+"_"+proyecto.getAnCortoContrato()+".pdf";
 				System.out.println("Valor de la variable bandera "+proyecto.isGestorfinanciero());
 				if (proyecto.isGestorfinanciero())
 					contrato.crearContrato1(proyecto, path);	
 				else
 					contrato.crearContrato(proyecto, path);
 				
 
 				if(tabla==null){
 					ps=cn.prepareStatement(rb.getString("Contrato++"));
 					ps.execute();
 				}
 				ps=cn.prepareStatement(rb.getString("act_consecutivo_contrato"));
 				ps.setLong(1,Long.parseLong(proyecto.getConsecContrato()));
 				ps.setString(2,proyecto.getFecContrato());
 				ps.setLong(3,proyecto.getId());
 				ps.executeUpdate();
 				break;
 			}
 		}catch (SQLException e){
 			lanzaExcepcion(e);
 		}catch (IOException e){
 			lanzaExcepcion(e);
 		}catch (Exception e){
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return proyecto;
 	}
 	
 		
 	public List<CompromisosOBJ> getListaCompromisos(int id){
 		CompromisosOBJ compromisos=null;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		int i=1;
 		List<CompromisosOBJ> listaCompromisos=new ArrayList <CompromisosOBJ>();
 		
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("getResumenCompromisos"));
 			ps.setInt(1, id);
 			rs=ps.executeQuery();
 			//System.out.println("-->"+ps);
 			while(rs.next()){
 				i=1;
 				compromisos=new CompromisosOBJ();
 				compromisos.setNombre(rs.getString(i++));
 				compromisos.setIndicador(rs.getString(i++));							
 				compromisos.setCantidad(rs.getString(i++));
 				listaCompromisos.add(compromisos);
 			}
 		} catch (SQLException e) {
 			lanzaExcepcion(e);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return listaCompromisos;
 	}
 }
 
