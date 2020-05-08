 package logica;
 
 import java.sql.Date;
 
 public class Gestor {
 	
 	/////////////////////////
 	/////// Escuela /////////
 	/////////////////////////
 	public static void crearEscuela(String pNombre, String pPais, String pFecha, String pCaracteristicas) throws Exception {
 	
 		DAOEscuela.crear( ( new Escuela(pNombre, pPais, pFecha, pCaracteristicas) ) );
 		
 	}
 	
 	
 	public static Escuela consultarEscuela(int pId) throws Exception {
 	
 		Escuela escuela = DAOEscuela.buscar(pId);
 		return escuela;
 		
 	}
 	
 	
 	public static void actualizarEscuela(int pId, String pNombre, String pPais, String pFecha, String pCaracteristicas) throws Exception{
 	
 		Escuela escuela = DAOEscuela.buscar(pId);
 		
 		escuela.setNombre(pNombre);
 		escuela.setPaisOrigen(pPais);
 		escuela.setFechaOrigen(pFecha);
 		escuela.setCaracteristicas(pCaracteristicas);
 		
 		DAOEscuela.actualizar( escuela );
 		
 	}
 	
 	
 	public static void borrarEscuela(int pId) throws java.sql.SQLException, Exception {
 	
 		DAOEscuela.borrar( DAOEscuela.buscar( pId ) );
 		
 	}
 	
 	/////////////////////////
 	/////// Pintor  /////////
 	/////////////////////////
 	public static void crearPintor(String pId, String pNombre, String pNombreArtistico, String pCiudadNacimiento, String pPaisNacimiento, String pFechaNacimiento, String pFechaMuerte, String pTipo) throws Exception{
 	
		DAOPintor.crear( new Pintor(pId, pNombre, pNombreArtistico, pCiudadNacimiento, pPaisNacimiento, pFechaNacimiento, pFechaMuerte, pTipo) );
 		
 	}
 	
 	
 	public static Pintor consultarPintor(String pId) throws Exception {
 	
 		Pintor pintor = DAOPintor.buscar(pId);
 		return pintor;
 		
 	}
 	
 	
 	public static void actualizarPintor (String pId, String pNombre, String pNombreArtistico, String pCiudadNacimiento, String pPaisNacimiento, String pFechaNacimiento, String pFechaMuerte, String pTipo) throws Exception{
 		
 		Pintor pintor = DAOPintor.buscar(pId);
 		
 		pintor.setNombre(pNombre);
 		pintor.setNombreArtistico(pNombreArtistico);
 		pintor.setNacionalidad(pPaisNacimiento);
 		pintor.setCiudadNacimiento(pCiudadNacimiento);
 		pintor.setFechaNacimiento(pFechaNacimiento);
 		pintor.setFechaMuerte(pFechaMuerte);
 		pintor.setTipo(pTipo);
 		
 		DAOPintor.actualizar( pintor );
 		
 	}
 	
 	
 	public static void borrarPintor(String pId) throws java.sql.SQLException, Exception {
 	
 		DAOPintor.borrar( DAOPintor.buscar(pId));
 		
 	}
 	
 		/////////////////////////
 		/////// Pintura  /////////
 		/////////////////////////
 	public static void crearPintura(String pCodigo, String pNombre, String pidPintor, String pDimensiones, String pFechaCreacion, String pTiempoTardado, String pTecnicaCreacion, int pCondicionActual, int pFamosa, String pHistoria) throws Exception {
 		Pintor pintor = DAOPintor.buscar(pidPintor);
 		DAOPintura.crear( ( new Pintura(pCodigo, pNombre, pDimensiones, pFechaCreacion, pTiempoTardado, pTecnicaCreacion, pCondicionActual, pFamosa, pHistoria, pintor ) ) );
 	}
 	
 	public static Pintura consultarPintura(String pId) throws Exception {
 	
 		Pintura pintura = DAOPintura.buscar(pId);
 		return pintura;
 		
 	}
 	
 	
 	public static void actualizarPintura(String pCodigo, String pNombre, String pDimensiones, String pFechaCreacion, String pTiempoTardado, String pTecnicaCreacion, int pCondicionActual,  int pFamosa, String pHistoria) throws Exception{
 		
 		Pintura pintura = DAOPintura.buscar(pCodigo);
 		
 		pintura.setCodigo(pCodigo);
 		pintura.setNombre(pNombre);
 		pintura.setDimensiones(pDimensiones);
 		pintura.setFechaCreacion(pFechaCreacion);
 		pintura.setTiempoTardado(pTiempoTardado);
 		pintura.setTecnica(pTecnicaCreacion);
 		pintura.setCondicionActual(pCondicionActual);
 		pintura.setEsFamosa(pFamosa);
 		pintura.setHistoria(pHistoria);
 		
 		DAOPintura.actualizar(pintura);
 		
 	}
 	
 	
 	public static void borrarPintura(int pCodigo) throws java.sql.SQLException, Exception {
 		
 		DAOPintura.borrar(DAOPintura.buscar(pCodigo));
 		
 	}
 	
 	
 	/////////////////////////
 	/////// Mecenas /////////
 	/////////////////////////
 	public static void crearMecenas(String pId, String pNombre, String pNacionalidad, String pCiudadNacimiento, String pFechaMuerte) throws Exception{
 	
 		DAOMecenas.crear( new Mecenas(pId, pNombre, pNacionalidad, pCiudadNacimiento, pFechaMuerte) );
 		
 	}
 	
 	
 	public static Mecenas consultarMecenas(String pId) throws Exception {
 	
 		Mecenas mecenas = DAOMecenas.buscar(pId);
 		return mecenas;
 		
 	}
 	
 	
 	public static void actualizarMecenas (String pId, String pNombre, String pNacionalidad, String pCiudadNacimiento, String pFechaMuerte) throws Exception{
 		
 		Mecenas mecenas = DAOMecenas.buscar(pId);
 		
 		mecenas.setNombre(pNombre);
 		mecenas.setNacionalidad(pNacionalidad);
 		mecenas.setCiudadNacimiento(pCiudadNacimiento);
 		mecenas.setFechaMuerte( Date.valueOf(pFechaMuerte) );
 		
 		DAOMecenas.actualizar(mecenas);
 		
 	}
 	
 	
 	public static void borrarMecenas(String pId) throws java.sql.SQLException, Exception {
 	
 		DAOMecenas.borrar( DAOMecenas.buscar(pId));
 		
 	}
 	
 	/////////////////////////
 	/////// Mecenazgo ///////
 	/////////////////////////
 	public static void crearMecenazgo(String pFechaInicio, String pFechaFin, String pIdMecenas, String pIdPintor) throws Exception{
 	
 		DAOMecenazgo.crear( new Mecenazgo(pFechaInicio, pFechaFin, DAOPintor.buscar(pIdPintor), DAOMecenas.buscar(pIdMecenas)) );
 		
 	}
 	
 	
 	public static String actualizarMecenazgo (String pFechaInicio, String pFechaFin, int pIdMecenas, int pIdPintor) throws Exception{
 	
 		(new DAOMecenazgo()).actualizar(pIdMecenas, pIdPintor, pFechaInicio, pFechaFin);
 		return "El mecenazgo ha sido actualizado.";
 		
 	}
 	
 	
 	public static void borrarMecenazgo(int pId) throws java.sql.SQLException, Exception {
 	
 		DAOMecenazgo.borrar( DAOMecenazgo.buscar(pId) );
 		
 	}
 	
 	/////////////////////////
 	/////// Galeria /////////
 	/////////////////////////
 	public static void crearGaleria(String pId, String pNombre, String pDireccion, String pTelefono, String pFechaInauguracion, String pNombreEncargado, String pArea) throws Exception {
 		
 		DAOGaleria.crear( new Galeria(pId, pNombre, pDireccion, pTelefono, pFechaInauguracion, pNombreEncargado, pArea));
 		
 	}
 	
 	
 	public static Galeria consultarGaleria(String pId) throws Exception {
 	
 		Galeria galeria = DAOGaleria.buscar(pId);
 		return galeria;
 		
 	}
 	
 	
 	public static void actualizarGaleria(String pId, String pNombre, String pDireccion, String pTelefono, String pFechaInauguracion, String pNombreEncargado, String pArea) throws Exception{
 		
 		Galeria galeria = DAOGaleria.buscar(pId);
 		
 		galeria.setNombre(pNombre);
 		galeria.setDireccion(pDireccion);
 		galeria.setTelefono(pTelefono);
 		galeria.setFechaInaguracion( Date.valueOf(pFechaInauguracion) );
 		galeria.setEncargado(pNombreEncargado);
 		galeria.setMetrosCuadrados(pArea);
 		
 		DAOGaleria.actualizar(galeria);
 		
 	}
 	
 	
 	public static void borrarGaleria(String pId) throws java.sql.SQLException, Exception {
 	
 		DAOGaleria.borrar( DAOGaleria.buscar(pId) );
 		
 	}
 	
 	/////////////////////////
 	//// Coleccionista //////
 	/////////////////////////
 	public static void crearColeccionista(String pId, String pNombre, String pDireccion, String pTelefono, String pFechaInicio) throws Exception {
 	
 		DAOColeccionista.crear( new Coleccionista(pId, pNombre, pDireccion, pTelefono, pFechaInicio) );
 		
 	}
 	
 	
 	public static Coleccionista consultarColeccionista(String pId) throws Exception {
 	
 		Coleccionista coleccionista = DAOColeccionista.buscar(pId);
 		return coleccionista;
 		
 	}
 	
 	
 	public static void actualizarColeccionista(String pId, String pNombre, String pDireccion, String pTelefono, String pFechaInicio) throws Exception{
 	
 		Coleccionista coleccionista = DAOColeccionista.buscar(pId);
 		
 		coleccionista.setNombre(pNombre);
 		coleccionista.setDireccion(pDireccion);
 		coleccionista.setTelefono(pTelefono);
 		coleccionista.setFechaInicio( Date.valueOf(pFechaInicio) );
 		
 		DAOColeccionista.actualizar(coleccionista);
 		
 	}
 	
 	
 	public static void borrarColeccionista(String pId) throws java.sql.SQLException, Exception {
 	
 		DAOColeccionista.borrar( DAOColeccionista.buscar(pId) );
 		
 	}
 	
 
 }
