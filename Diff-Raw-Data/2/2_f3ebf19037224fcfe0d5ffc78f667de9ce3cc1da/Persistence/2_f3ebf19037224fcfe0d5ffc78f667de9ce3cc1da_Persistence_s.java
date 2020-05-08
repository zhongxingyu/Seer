 package persistence;
 
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import net.rim.device.api.database.Cursor;
 import net.rim.device.api.database.Database;
 import net.rim.device.api.database.DatabaseFactory;
 import net.rim.device.api.database.Row;
 import net.rim.device.api.database.Statement;
 
 import core.*;
 import ehmsoft.Cargado;
 import ehmsoft.Guardado;
 
 public class Persistence implements Cargado, Guardado {
 	private ConnectionManager connMgr;
 	public static final int LISTADO_ACTUACIONES = 0;
 	public static final int LISTADO_CAMPOS = 0;
 	public static final int LISTADO_CATEGORIAS = 0;
 	public static final int LISTADO_JUZGADOS = 0;
 	public static final int LISTADO_PERSONAS = 0;
 	public static final int LISTADO_PROCESOS = 0;
 	public static final int LISTA_LISTAS = 0;
 	public static final int NUEVA_ACTUACION = 0;
 	public static final int NUEVA_CATEGORIA = 0;
 	public static final int NUEVA_CITA = 0;
 	public static final int NUEVA_PERSONA = 0;
 	public static final int NUEVO_CAMPO = 0;
 	public static final int NUEVO_JUZGADO = 0;
 	public static final int NUEVO_PROCESO = 0;
 	public static final int VER_ACTUACION = 0;
 	public static final int VER_CAMPO = 0;
 	public static final int VER_CATEGORIA = 0;
 	public static final int VER_CITA = 0;
 	public static final int VER_JUZGADO = 0;
 	public static final int VER_PERSONA = 0;
 	public static final int VER_PROCESO = 0;
 	
 	
 	
 	
 
 	public Persistence() throws Exception {
 		connMgr = new ConnectionManager();
 	}
 
 	public void actualizarPersona(Persona persona) throws Exception {
 		Database d = null;
 		Statement stAcPersona1;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			if (persona.getTipo() == 1) {
 				stAcPersona1 = d
 						.createStatement("UPDATE demandantes SET cedula = ?,"
 								+ " nombre = ?," + " telefono = ?,"
 								+ " direccion = ?," + "correo= ?,"
 								+ "notas = ? WHERE id_demandante = ?");
 			} else if (persona.getTipo() == 2) {
 				stAcPersona1 = d
 						.createStatement("UPDATE demandados SET cedula = ?,"
 								+ " nombre = ?," + " telefono = ?,"
 								+ " direccion = ?," + "correo= ?,"
 								+ " notas = ? WHERE id_demandado = ?");
 			} else {
 				throw new Exception("Tipo persona invalido");
 			}
 
 			stAcPersona1.prepare();
 			stAcPersona1.bind(1, persona.getId());
 			stAcPersona1.bind(2, persona.getNombre());
 			stAcPersona1.bind(3, persona.getTelefono());
 			stAcPersona1.bind(4, persona.getDireccion());
 			stAcPersona1.bind(5, persona.getCorreo());
 			stAcPersona1.bind(6, persona.getNotas());
 			stAcPersona1.bind(7, persona.getId_persona());
 			stAcPersona1.execute();
 			stAcPersona1.close();
 
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 	}
 
 	public void guardarPersona(Persona persona) throws Exception {
 		/**
 		 * @param tipo
 		 *            1 para demandante, 2 para demandado
 		 **/
 		Database d = null;
 		Statement stPersona1;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			if (persona.getTipo() == 1) {
 				stPersona1 = d
 						.createStatement("INSERT INTO demandantes (id_demandante,cedula,nombre,telefono,direccion,correo,notas) VALUES(NULL,?,?,?,?,?,?)");
 			} else if (persona.getTipo() == 2) {
 				stPersona1 = d
 						.createStatement("INSERT INTO demandados (id_demandado,cedula,nombre,telefono,direccion,correo,notas)  VALUES(NULL,?,?,?,?,?,?)");
 			} else {
 				throw new Exception("Tipo persona invalido");
 			}
 			stPersona1.prepare();
 			stPersona1.bind(1, persona.getId());
 			stPersona1.bind(2, persona.getNombre());
 			stPersona1.bind(3, persona.getTelefono());
 			stPersona1.bind(4, persona.getDireccion());
 			stPersona1.bind(5, persona.getCorreo());
 			stPersona1.bind(6, persona.getNotas());
 			stPersona1.execute();
 			stPersona1.close();
 			persona.setId_persona((Long.toString(d.lastInsertedRowID())));
 
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 
 	}
 
 	public void borrarPersona(Persona persona) throws Exception {
 		Database d = null;
 		Statement stDelPersona1;
 		Statement stDelPersona2;
 		Statement stDelPersona3;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			if (persona.getTipo() == 1) {
 				stDelPersona1 = d
 						.createStatement("DELETE FROM demandantes WHERE id_demandante = ? ");
 				stDelPersona2 = d
 						.createStatement("UPDATE procesos SET id_demandante = 1 WHERE id_demandante = ?");
 				stDelPersona3 = d
 				.createStatement("UPDATE pantillas SET id_demandante = 1 WHERE id_demandante = ?");
 
 				
 			} else if (persona.getTipo() == 2) {
 				stDelPersona1 = d
 						.createStatement("DELETE FROM demandados WHERE id_demandado = ? ");
 				stDelPersona2 = d
 						.createStatement("UPDATE procesos SET id_demandado = 1 WHERE id_demandado = ?");
 				stDelPersona3 = d
 				.createStatement("UPDATE plantillas SET id_demandado = 1 WHERE id_demandado = ?");
 	
 			} else {
 				throw new Exception("Tipo persona invalido");
 			}
 
 			stDelPersona1.prepare();
 			stDelPersona2.prepare();
 			stDelPersona3.prepare();
 			stDelPersona1.bind(1, persona.getId_persona());
 			stDelPersona2.bind(1, persona.getId_persona());
 			stDelPersona3.bind(1, persona.getId_persona());
 			stDelPersona1.execute();
 			stDelPersona2.execute();
 			stDelPersona3.execute();
 			stDelPersona1.close();
 			stDelPersona2.close();
 			stDelPersona3.close();
 
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 
 	}
 
 	public void actualizarJuzgado(Juzgado juzgado) throws Exception {
 		Database d = null;
 		Statement stAcJuzgado;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			stAcJuzgado = d.createStatement("UPDATE juzgados SET nombre = ?,"
 					+ " ciudad = ?," + " telefono = ?," + " direccion=?,"
 					+ " tipo = ? WHERE id_juzgado = ?");
 			stAcJuzgado.prepare();
 			stAcJuzgado.bind(1, juzgado.getNombre());
 			stAcJuzgado.bind(2, juzgado.getCiudad());
 			stAcJuzgado.bind(3, juzgado.getTelefono());
 			stAcJuzgado.bind(4, juzgado.getDireccion());
 			stAcJuzgado.bind(5, juzgado.getTipo());
 			stAcJuzgado.bind(6, juzgado.getId_juzgado());
 			stAcJuzgado.execute();
 			stAcJuzgado.close();
 
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 
 	}
 
 	public void guardarJuzgado(Juzgado juzgado) throws Exception {
 		Database d = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement stJuzgado = d
 					.createStatement("INSERT INTO juzgados (id_juzgado,nombre,ciudad,telefono,direccion,tipo) VALUES( NULL,?,?,?,?,?)");
 			stJuzgado.prepare();
 			stJuzgado.bind(1, juzgado.getNombre());
 			stJuzgado.bind(2, juzgado.getCiudad());
 			stJuzgado.bind(3, juzgado.getTelefono());
 			stJuzgado.bind(4, juzgado.getDireccion());
 			stJuzgado.bind(5, juzgado.getTipo());
 			stJuzgado.execute();
 			stJuzgado.close();
 			juzgado.setId_juzgado(Long.toString(d.lastInsertedRowID()));
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 
 	}
 
 	public void borrarJuzgado(Juzgado juzgado) throws Exception {
 		Database d = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement stDelJuzgado1 = d
 					.createStatement("DELETE FROM juzgados WHERE id_juzgado = ?");
 			Statement stDelJuzgado2 = d
 					.createStatement("UPDATE procesos SET id_juzgado = 1 WHERE id_juzgado = ?");
 			Statement stDelJuzgado3 = d
 					.createStatement("UPDATE actuaciones SET id_juzgado = 1 WHERE id_juzgado = ?");
 			Statement stDelJuzgado4 = d
 			.createStatement("UPDATE plantillas SET id_juzgado = 1 WHERE id_juzgado = ?");
 	
 			stDelJuzgado1.prepare();
 			stDelJuzgado2.prepare();
 			stDelJuzgado3.prepare();
 			stDelJuzgado4.prepare();
 			stDelJuzgado1.bind(1, juzgado.getId_juzgado());
 			stDelJuzgado2.bind(1, juzgado.getId_juzgado());
 			stDelJuzgado3.bind(1, juzgado.getId_juzgado());
 			stDelJuzgado4.bind(1, juzgado.getId_juzgado());
 			stDelJuzgado1.execute();
 			stDelJuzgado2.execute();
 			stDelJuzgado3.execute();
 			stDelJuzgado4.execute();
 			stDelJuzgado1.close();
 			stDelJuzgado2.close();
 			stDelJuzgado3.close();
 			stDelJuzgado4.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 
 	}
 
 	public void actualizarActuacion(Actuacion actuacion) throws Exception { // id_proceso
 																			// no
 																			// se
 																			// puede
 																			// cambiar
 		Database d = null;
 		Statement stAcActuacion;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			stAcActuacion = d
 					.createStatement("UPDATE actuaciones SET id_juzgado = ?,"
 							+ " fecha_creacion = datetime(?),"
 							+ " fecha_proxima = datetime(?),"
 							+ " descripcion = (?),"
 							+ " uid = ? WHERE id_actuacion = ?");
 			stAcActuacion.prepare();
 			stAcActuacion.bind(1, actuacion.getJuzgado().getId_juzgado());
 			stAcActuacion.bind(2, calendarToString(actuacion.getFecha()));
 			stAcActuacion
 					.bind(3, calendarToString(actuacion.getFechaProxima()));
 			stAcActuacion.bind(4, actuacion.getDescripcion());
 			stAcActuacion.bind(5, actuacion.getUid());
 			stAcActuacion.bind(6, actuacion.getId_actuacion());
 			
 			stAcActuacion.execute();
 			stAcActuacion.close();
 
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 	}
 
 	public void guardarActuacion(Actuacion actuacion, String id_proceso)
 			throws Exception {
 		Database d = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement stActuacion = d
 					.createStatement("INSERT INTO actuaciones (id_actuacion,id_proceso, id_juzgado, fecha_creacion, fecha_proxima, descripcion, uid) VALUES( NULL,?,?,datetime(?),datetime(?),?,?)");
 			stActuacion.prepare();
 			stActuacion.bind(1, Integer.parseInt(id_proceso));
 			stActuacion.bind(2, actuacion.getJuzgado().getId_juzgado());
 			stActuacion.bind(3, calendarToString(actuacion.getFecha()));
 			stActuacion.bind(4, calendarToString(actuacion.getFechaProxima()));
 			stActuacion.bind(5, actuacion.getDescripcion());
 			stActuacion.bind(6, actuacion.getUid());
 			stActuacion.execute();
 			stActuacion.close();
 			actuacion.setId_actuacion(Long.toString(d.lastInsertedRowID()));
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 
 	}
 
 	public void borrarActuacion(Actuacion actuacion) throws Exception {
 		Database d = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement stDelActuacion = d
 					.createStatement("DELETE FROM actuaciones WHERE id_actuacion = ?");
 			stDelActuacion.prepare();
 			stDelActuacion.bind(1, actuacion.getId_actuacion());
 			stDelActuacion.execute();
 			stDelActuacion.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 
 	}
 
 	public void actualizarCampoPersonalizado(CampoPersonalizado campo)
 			throws Exception {
 		Database d = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());			
 			Statement stAcAtributoProceso = d.createStatement("UPDATE atributos_proceso SET valor = ? WHERE id_atributo_proceso = ?");
 			stAcAtributoProceso.prepare();
 			stAcAtributoProceso.bind(1, campo.getValor());
 			stAcAtributoProceso.bind(2, campo.getId_campo());
 			stAcAtributoProceso.execute();
 			stAcAtributoProceso.close();
 
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		
 
 	}
 
 	public void guardarCampoPersonalizado(CampoPersonalizado campo,
 			String id_proceso) throws Exception {
 		Database d = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement stAtributosProceso = d.createStatement("INSERT INTO atributos_proceso (id_atributo_proceso, id_atributo, id_proceso, valor) VALUES( NULL,?,?,?)");
 			stAtributosProceso.prepare();
 			stAtributosProceso.bind(1, Integer.parseInt(campo.getId_atributo()));
 			stAtributosProceso.bind(2, Integer.parseInt(id_proceso));
 			stAtributosProceso.bind(3, campo.getValor());
 			stAtributosProceso.execute();
 			campo.setId_campo(Long.toString(d.lastInsertedRowID()));
 			stAtributosProceso.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 
 	}
 
 	public void borrarCampoPersonalizado(CampoPersonalizado campo)
 			throws Exception {
 		Database d = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement stDelCampoPersonalizado = d
 					.createStatement("DELETE FROM atributos_proceso WHERE id_atributo_proceso = ?");
 			stDelCampoPersonalizado.prepare();
 			stDelCampoPersonalizado.bind(1, campo.getId_campo());
 			stDelCampoPersonalizado.execute();
 			stDelCampoPersonalizado.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 
 	}
 
 	public void actualizarAtributo(CampoPersonalizado campo) throws Exception {
 		Database d = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement stAcAtributo = d.createStatement("UPDATE atributos SET nombre = ?,"
 							+ " obligatorio = ?,"
 							+ " longitud_max = ?,"
 							+ " longitud_min = ? WHERE id_atributo = ?");
 			stAcAtributo.prepare();
 			stAcAtributo.bind(1, campo.getNombre());
 			int obligatorio = 0;
 			if(campo.isObligatorio().booleanValue()){
 				obligatorio = 1;
 			}
 			stAcAtributo.bind(2, obligatorio);
 			stAcAtributo.bind(3, campo.getLongitudMax());
 			stAcAtributo.bind(4, campo.getLongitudMin());
			stAcAtributo.bind(5, campo.getId_campo());
 			stAcAtributo.execute();
 			stAcAtributo.close();
 			
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		
 	}
 
 	public void guardarAtributo(CampoPersonalizado campo) throws Exception {
 			Database d = null;
 			try {
 				connMgr.prepararBD();
 				d = DatabaseFactory.open(connMgr.getDbLocation());
 				Statement stAtributos = d.createStatement("INSERT INTO atributos (id_atributo, nombre, obligatorio, longitud_max, longitud_min) VALUES( NULL,?,?,?,?)");
 				stAtributos.prepare();
 				stAtributos.bind(1, campo.getNombre());
 				int obligatorio = 0;
 				if(campo.isObligatorio().booleanValue()){
 					obligatorio = 1;
 				}
 				stAtributos.bind(2, obligatorio);
 				stAtributos.bind(3, campo.getLongitudMax());
 				stAtributos.bind(4, campo.getLongitudMin());
 				stAtributos.execute();
 				campo.setId_atributo(Long.toString(d.lastInsertedRowID()));
 				stAtributos.close();
 				
 			} catch (Exception e) {
 				throw e;
 			} finally {
 				if (d != null) {
 					d.close();
 				}
 			}
 		
 	}
 
 	public void borrarAtributo(CampoPersonalizado campo) throws Exception {
 		Database d = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement stDelAtributo = d.createStatement("DELETE FROM atributos WHERE id_atributo = ?");
 			Statement stDelCampoPersonalizado = d.createStatement("DELETE FROM atributos_proceso WHERE id_atributo = ?");
 			stDelCampoPersonalizado.prepare();
 			stDelAtributo.prepare();
 			stDelCampoPersonalizado.bind(1, campo.getId_atributo());
 			stDelAtributo.bind(1, campo.getId_atributo());
 			stDelCampoPersonalizado.execute();
 			stDelAtributo.execute();
 			stDelCampoPersonalizado.close();
 			stDelAtributo.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		
 	}	
 	
 	public void actualizarProceso(Proceso proceso) throws Exception {
 		Database d = null;
 		Statement stAcProceso;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			stAcProceso = d
 					.createStatement("UPDATE procesos SET id_demandante = ?,"
 							+ " id_demandado = ?,"
 							+ " fecha_creacion = datetime(?),"
 							+ " radicado = ?," + " radicado_unico = ?,"
 							+ " estado = ?," + " tipo = ?," + " notas = ?,"
 							+ " prioridad = ?," + " id_juzgado = ?,"
 							+ " id_categoria = ? WHERE id_proceso = ?");
 			stAcProceso.prepare();
 			if (proceso.getDemandante() == null) {
 				stAcProceso.bind(1, "1");
 			} else {
 				stAcProceso.bind(1, proceso.getDemandante().getId_persona());
 			}
 			if (proceso.getDemandado() == null) {
 				stAcProceso.bind(2, "1");
 			} else {
 				stAcProceso.bind(2, proceso.getDemandado().getId_persona());
 			}
 
 			stAcProceso.bind(3, calendarToString(proceso.getFecha()));
 			stAcProceso.bind(4, proceso.getRadicado());
 			stAcProceso.bind(5, proceso.getRadicadoUnico());
 			stAcProceso.bind(6, proceso.getEstado());
 			stAcProceso.bind(7, proceso.getTipo());
 			stAcProceso.bind(8, proceso.getNotas());
 			stAcProceso.bind(9, proceso.getPrioridad());
 			if (proceso.getJuzgado() == null) {
 				stAcProceso.bind(10, "1");
 			} else {
 				stAcProceso.bind(10, proceso.getJuzgado().getId_juzgado());
 			}
 
 			stAcProceso.bind(11, proceso.getCategoria().getId_categoria());
 			stAcProceso.bind(12, proceso.getId_proceso());
 			stAcProceso.execute();
 			stAcProceso.close();
 
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 			if (proceso.getDemandante() != null) {
 				proceso.setDemandante(consultarPersona(proceso.getDemandante()
 						.getId_persona(), 1));
 			} else {
 				proceso.setDemandante(consultarPersona("1", 1));
 			}
 			if (proceso.getDemandado() != null) {
 				proceso.setDemandado(consultarPersona(proceso.getDemandado()
 						.getId_persona(), 2));
 			} else {
 				proceso.setDemandado(consultarPersona("1", 2));
 			}
 			if (proceso.getJuzgado() != null) {
 				proceso.setJuzgado(consultarJuzgado(proceso.getJuzgado()
 						.getId_juzgado()));
 			} else {
 				proceso.setJuzgado(consultarJuzgado("1"));
 			}
 
 		}
 		Vector cp = new Vector();
 		cp = proceso.getCampos();
 		if (cp != null) {
 			Enumeration e = cp.elements();
 			while (e.hasMoreElements()) {
 				actualizarCampoPersonalizado((CampoPersonalizado) e.nextElement());
 			}
 
 		}
 
 	}
 
 	public void guardarProceso(Proceso proceso) throws Exception {
 		Database d = null;
 		long IDproceso = -1;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement stProceso = d
 					.createStatement("INSERT INTO procesos (id_proceso,id_demandante,id_demandado,fecha_creacion,radicado,radicado_unico,estado,tipo,notas,prioridad,id_juzgado,id_categoria) VALUES(NULL,?,?,datetime(?),?,?,?,?,?,?,?,?)");
 			stProceso.prepare();
 			stProceso.bind(1, proceso.getDemandante().getId_persona()); // ingresa
 																		// el id
 																		// del
 																		// demandante
 			stProceso.bind(2, proceso.getDemandado().getId_persona());
 			stProceso.bind(3, calendarToString(proceso.getFecha()));
 			stProceso.bind(4, proceso.getRadicado());
 			stProceso.bind(5, proceso.getRadicadoUnico());
 			stProceso.bind(6, proceso.getEstado());
 			stProceso.bind(7, proceso.getTipo());
 			stProceso.bind(8, proceso.getNotas());
 			stProceso.bind(9, proceso.getPrioridad());
 			stProceso.bind(10, proceso.getJuzgado().getId_juzgado());
 			stProceso.bind(11, proceso.getCategoria().getId_categoria());
 			stProceso.execute();
 			IDproceso = d.lastInsertedRowID();
 			stProceso.close();
 			proceso.setId_proceso(Long.toString(d.lastInsertedRowID()));
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 
 		String id_proceso = Long.toString(IDproceso);
 		Vector cp = new Vector();
 		cp = proceso.getCampos();
 		if (cp != null) {
 			Enumeration e = cp.elements();
 			while (e.hasMoreElements()) {
 				guardarCampoPersonalizado((CampoPersonalizado) e.nextElement(),
 						id_proceso);
 			}
 
 		}
 
 		Vector ac = new Vector();
 		ac = proceso.getActuaciones();
 		if (ac != null) {
 			Enumeration e = ac.elements();
 			while (e.hasMoreElements()) {
 				guardarActuacion((Actuacion) e.nextElement(), id_proceso);
 
 			}
 
 		}
 
 	}
 
 	public void borrarProceso(Proceso proceso) throws Exception {
 		Database d = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement stDelProceso = d
 					.createStatement("DELETE FROM procesos WHERE id_proceso = ?");
 			Statement stDelActuaciones = d
 					.createStatement("DELETE FROM actuaciones WHERE id_proceso = ?");
 			Statement stDelCampoPersonalizado = d.createStatement("DELETE FROM atributos_proceso WHERE id_proceso = ?");
 			stDelProceso.prepare();
 			stDelActuaciones.prepare();
 			stDelCampoPersonalizado.prepare();
 			stDelProceso.bind(1, proceso.getId_proceso());
 			stDelActuaciones.bind(1, proceso.getId_proceso());
 			stDelCampoPersonalizado.bind(1, proceso.getId_proceso());
 			stDelProceso.execute();
 			stDelActuaciones.execute();
 			stDelCampoPersonalizado.execute();
 			stDelProceso.close();
 			stDelActuaciones.close();
 			stDelCampoPersonalizado.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 
 	}
 
 	public void actualizarCategoria(Categoria categoria) throws Exception {
 		if (Integer.parseInt(categoria.getId_categoria()) != 1) {
 			Database d = null;
 			try {
 				connMgr.prepararBD();
 				d = DatabaseFactory.open(connMgr.getDbLocation());
 				Statement stCategoria = d
 						.createStatement("UPDATE categorias SET descripcion = ? WHERE id_categoria = ?");
 				stCategoria.prepare();
 				stCategoria.bind(1, categoria.getDescripcion());
 				stCategoria.bind(2, categoria.getId_categoria());
 				stCategoria.execute();
 				stCategoria.close();
 			} catch (Exception e) {
 				throw e;
 			} finally {
 				if (d != null) {
 					d.close();
 				}
 			}
 		} else {
 			throw new NullPointerException();
 		}
 
 	}
 
 	public void guardarCategoria(Categoria categoria) throws Exception {
 		Database d = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement stCategoria = d
 					.createStatement("INSERT INTO categorias (id_categoria,descripcion) VALUES( NULL,?)");
 			stCategoria.prepare();
 			stCategoria.bind(1, categoria.getDescripcion());
 			stCategoria.execute();
 			stCategoria.close();
 			categoria.setId_categoria(Long.toString(d.lastInsertedRowID()));
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 
 	}
 
 	public void borrarCategoria(Categoria categoria) throws Exception {
 		// TODO Auto-generated method stub
 		if (Integer.parseInt(categoria.getId_categoria()) != 1) {
 			Database d = null;
 			try {
 				connMgr.prepararBD();
 				d = DatabaseFactory.open(connMgr.getDbLocation());
 				Statement stDelCategoria1 = d
 						.createStatement("DELETE FROM categorias WHERE id_categoria = ?");
 				Statement stDelCategoria2 = d
 						.createStatement("UPDATE procesos SET id_categoria = 1 WHERE id_categoria = ?");
 				Statement stDelCategoria3 = d
 					.createStatement("UPDATE plantillas SET id_categoria = 1 WHERE id_categoria = ?");
 		
 				stDelCategoria1.prepare();
 				stDelCategoria2.prepare();
 				stDelCategoria3.prepare();
 				stDelCategoria1.bind(1, categoria.getId_categoria());
 				stDelCategoria2.bind(1, categoria.getId_categoria());
 				stDelCategoria3.bind(1, categoria.getId_categoria());
 				stDelCategoria1.execute();
 				stDelCategoria2.execute();
 				stDelCategoria3.execute();
 				stDelCategoria1.close();
 				stDelCategoria2.close();
 				stDelCategoria3.close();
 			} catch (Exception e) {
 				throw e;
 			} finally {
 				if (d != null) {
 					d.close();
 				}
 			}
 		} else {
 			throw new NullPointerException();
 		}
 
 	}
 	public void actualizarPlantilla(Plantilla plantilla) throws Exception {
 		Database d = null;
 		Statement stAcPlantilla;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			stAcPlantilla = d
 					.createStatement("UPDATE plantillas SET id_demandante = ?,"
 							+ " id_demandado = ?,"
 							+ " radicado = ?," + " radicado_unico = ?,"
 							+ " estado = ?," + " tipo = ?," + " notas = ?,"
 							+ " prioridad = ?," + " id_juzgado = ?,"
 							+ " nombre = ?,"
 							+ " id_categoria = ? WHERE id_plantilla = ?");
 			stAcPlantilla.prepare();
 			if (plantilla.getDemandante() == null) {
 				stAcPlantilla.bind(1, "1");
 			} else {
 				stAcPlantilla.bind(1, plantilla.getDemandante().getId_persona());
 			}
 			if (plantilla.getDemandado() == null) {
 				stAcPlantilla.bind(2, "1");
 			} else {
 				stAcPlantilla.bind(2, plantilla.getDemandado().getId_persona());
 			}
 
 			stAcPlantilla.bind(3, plantilla.getRadicado());
 			stAcPlantilla.bind(4, plantilla.getRadicadoUnico());
 			stAcPlantilla.bind(5, plantilla.getEstado());
 			stAcPlantilla.bind(6, plantilla.getTipo());
 			stAcPlantilla.bind(7, plantilla.getNotas());
 			stAcPlantilla.bind(8, plantilla.getPrioridad());
 			if (plantilla.getJuzgado() == null) {
 				stAcPlantilla.bind(9, "1");
 			} else {
 				stAcPlantilla.bind(9, plantilla.getJuzgado().getId_juzgado());
 			}
 
 			stAcPlantilla.bind(10, plantilla.getNombre());
 			stAcPlantilla.bind(11, plantilla.getCategoria().getId_categoria());
 			stAcPlantilla.bind(12, plantilla.getId_plantilla());
 			stAcPlantilla.execute();
 			stAcPlantilla.close();
 
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 			if (plantilla.getDemandante() != null) {
 				plantilla.setDemandante(consultarPersona(plantilla.getDemandante()
 						.getId_persona(), 1));
 			} else {
 				plantilla.setDemandante(consultarPersona("1", 1));
 			}
 			if (plantilla.getDemandado() != null) {
 				plantilla.setDemandado(consultarPersona(plantilla.getDemandado()
 						.getId_persona(), 2));
 			} else {
 				plantilla.setDemandado(consultarPersona("1", 2));
 			}
 			if (plantilla.getJuzgado() != null) {
 				plantilla.setJuzgado(consultarJuzgado(plantilla.getJuzgado()
 						.getId_juzgado()));
 			} else {
 				plantilla.setJuzgado(consultarJuzgado("1"));
 			}
 
 		}
 		Vector cp = new Vector();
 		cp = plantilla.getCampos();
 		if (cp != null) {
 			Enumeration e = cp.elements();
 			while (e.hasMoreElements()) {
 				actualizarCampoPlantilla((CampoPersonalizado) e.nextElement());
 			}
 
 		}
 
 	}
 
 	public void guardarPlantilla(Plantilla plantilla) throws Exception {
 		Database d = null;
 		long IDplantilla = -1;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement stPlantilla = d
 					.createStatement("INSERT INTO plantillas (id_plantilla,id_demandante,id_demandado,radicado,radicado_unico,estado,tipo,notas,prioridad,id_juzgado,id_categoria,nombre) VALUES(NULL,?,?,?,?,?,?,?,?,?,?,?)");
 			stPlantilla.prepare();
 			stPlantilla.bind(1, plantilla.getDemandante().getId_persona()); 
 			stPlantilla.bind(2, plantilla.getDemandado().getId_persona());
 			stPlantilla.bind(3, plantilla.getRadicado());
 			stPlantilla.bind(4, plantilla.getRadicadoUnico());
 			stPlantilla.bind(5, plantilla.getEstado());
 			stPlantilla.bind(6, plantilla.getTipo());
 			stPlantilla.bind(7, plantilla.getNotas());
 			stPlantilla.bind(8, plantilla.getPrioridad());
 			stPlantilla.bind(9, plantilla.getJuzgado().getId_juzgado());
 			stPlantilla.bind(10, plantilla.getCategoria().getId_categoria());
 			stPlantilla.bind(11, plantilla.getNombre());
 			stPlantilla.execute();
 			IDplantilla = d.lastInsertedRowID();
 			stPlantilla.close();
 			plantilla.setId_plantilla(Long.toString(d.lastInsertedRowID()));
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 
 		String id_plantilla = Long.toString(IDplantilla);
 		Vector cp = new Vector();
 		cp = plantilla.getCampos();
 		if (cp != null) {
 			Enumeration e = cp.elements();
 			while (e.hasMoreElements()) {
 				guardarCampoPlantilla((CampoPersonalizado) e.nextElement(),
 						id_plantilla);
 			}
 
 		}
 
 	}
 
 	public void borrarPlantilla(Plantilla plantilla) throws Exception {
 		Database d = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement stDelPlantilla = d
 					.createStatement("DELETE FROM plantillas WHERE id_plantilla = ?");
 			Statement stDelCampoPersonalizado = d.createStatement("DELETE FROM atributos_plantilla WHERE id_plantilla = ?");
 			stDelPlantilla.prepare();
 			stDelCampoPersonalizado.prepare();
 			stDelPlantilla.bind(1, plantilla.getId_plantilla());
 			stDelCampoPersonalizado.bind(1, plantilla.getId_plantilla());
 			stDelPlantilla.execute();
 			stDelCampoPersonalizado.execute();
 			stDelPlantilla.close();
 			stDelCampoPersonalizado.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 
 	}
 	public void actualizarCampoPlantilla(CampoPersonalizado campo)
 	throws Exception {
 Database d = null;
 try {
 	connMgr.prepararBD();
 	d = DatabaseFactory.open(connMgr.getDbLocation());			
 	Statement stAcAtributoProceso = d.createStatement("UPDATE atributos_plantilla SET valor = ? WHERE id_atributo_plantilla = ?");
 	stAcAtributoProceso.prepare();
 	stAcAtributoProceso.bind(1, campo.getValor());
 	stAcAtributoProceso.bind(2, campo.getId_campo());
 	stAcAtributoProceso.execute();
 	stAcAtributoProceso.close();
 
 } catch (Exception e) {
 	throw e;
 } finally {
 	if (d != null) {
 		d.close();
 	}
 }
 
 
 }
 
 public void guardarCampoPlantilla(CampoPersonalizado campo,
 	String id_plantilla) throws Exception {
 Database d = null;
 try {
 	connMgr.prepararBD();
 	d = DatabaseFactory.open(connMgr.getDbLocation());
 	Statement stAtributosProceso = d.createStatement("INSERT INTO atributos_plantilla (id_atributo_plantilla, id_atributo, id_plantilla, valor) VALUES( NULL,?,?,?)");
 	stAtributosProceso.prepare();
 	stAtributosProceso.bind(1, Integer.parseInt(campo.getId_atributo()));
 	stAtributosProceso.bind(2, Integer.parseInt(id_plantilla));
 	stAtributosProceso.bind(3, campo.getValor());
 	stAtributosProceso.execute();
 	campo.setId_campo(Long.toString(d.lastInsertedRowID()));
 	stAtributosProceso.close();
 } catch (Exception e) {
 	throw e;
 } finally {
 	if (d != null) {
 		d.close();
 	}
 }
 
 }
 
 public void borrarCampoPlantilla(CampoPersonalizado campo)
 	throws Exception {
 Database d = null;
 try {
 	connMgr.prepararBD();
 	d = DatabaseFactory.open(connMgr.getDbLocation());
 	Statement stDelCampoPersonalizado = d
 			.createStatement("DELETE FROM atributos_plantilla WHERE id_atributo_plantilla = ?");
 	stDelCampoPersonalizado.prepare();
 	stDelCampoPersonalizado.bind(1, campo.getId_campo());
 	stDelCampoPersonalizado.execute();
 	stDelCampoPersonalizado.close();
 } catch (Exception e) {
 	throw e;
 } finally {
 	if (d != null) {
 		d.close();
 	}
 }
 
 }
 
 public void actualizarPreferencia(int id_preferencia, long valor)
 throws Exception {
 	Database d = null;
 	try {
 		connMgr.prepararBD();
 		d = DatabaseFactory.open(connMgr.getDbLocation());
 		Statement stPreferencias = d
 				.createStatement("UPDATE preferencias SET valor = ? WHERE id_preferencia = ?");
 		stPreferencias.prepare();
 		stPreferencias.bind(1, valor);
 		stPreferencias.bind(2, id_preferencia);
 		stPreferencias.execute();
 		stPreferencias.close();
 		
 	} catch (Exception e) {
 		throw e;
 	} finally {
 		if (d != null) {
 			d.close();
 		}
 	}
 
 }
 
 public void borrarPreferencia(int id_preferencia) throws Exception {
 // TODO Auto-generated method stub
 	Database d = null;
 	try {
 		connMgr.prepararBD();
 		d = DatabaseFactory.open(connMgr.getDbLocation());
 		Statement stPreferencias = d
 				.createStatement("UPDATE preferencias SET valor = 0 WHERE id_preferencia = ?");
 		stPreferencias.prepare();
 		stPreferencias.bind(1, id_preferencia);
 		stPreferencias.execute();
 		stPreferencias.close();
 		
 	} catch (Exception e) {
 		throw e;
 	} finally {
 		if (d != null) {
 			d.close();
 		}
 	}
 
 }
 	public Vector consultarDemandantes() throws Exception {// Devuelve una
 															// vector iterable
 															// de todos los
 															// demandantes
 		Database d = null;
 		Vector demandantes = new Vector();
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT * FROM demandantes order by nombre");
 			st.prepare();
 			Cursor cursor = st.getCursor();
 			while (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_demandante = row.getInteger(0);
 				String cedula = row.getString(1);
 				String nombre = row.getString(2);
 				String telefono = row.getString(3);
 				String direccion = row.getString(4);
 				String correo = row.getString(5);
 				String notas = row.getString(6);
 				Persona per = new Persona(1, cedula, nombre, telefono,
 						direccion, correo, notas,
 						Integer.toString(id_demandante));
 				if (id_demandante != 1) {
 					demandantes.addElement(per);
 				}
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		return demandantes;
 	}
 
 	public Vector consultarDemandados() throws Exception {// Devuelve un vector
 															// iterable con
 															// todos los
 															// demandados
 		Database d = null;
 		Vector demandados = new Vector();
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT * FROM demandados order by nombre");
 			st.prepare();
 			Cursor cursor = st.getCursor();
 			while (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_demandado = row.getInteger(0);
 				String cedula = row.getString(1);
 				String nombre = row.getString(2);
 				String telefono = row.getString(3);
 				String direccion = row.getString(4);
 				String correo = row.getString(5);
 				String notas = row.getString(6);
 				Persona per = new Persona(2, cedula, nombre, telefono,
 						direccion, correo, notas,
 						Integer.toString(id_demandado));
 				if (id_demandado != 1) {
 					demandados.addElement(per);
 				}
 
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		return demandados;
 	}
 
 	public Vector consultarPersonas() throws Exception {// Devuelve un vector
 														// iterable con todos
 														// los demandantes y
 														// demandados
 		Database d = null;
 		Vector pers = new Vector();
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT * FROM demandados order by nombre");
 			st.prepare();
 			Cursor cursor = st.getCursor();
 			while (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_demandado = row.getInteger(0);
 				String cedula = row.getString(1);
 				String nombre = row.getString(2);
 				String telefono = row.getString(3);
 				String direccion = row.getString(4);
 				String correo = row.getString(5);
 				String notas = row.getString(6);
 				Persona per = new Persona(2, cedula, nombre, telefono,
 						direccion, correo, notas,
 						Integer.toString(id_demandado));
 				if (id_demandado != 1) {
 					pers.addElement(per);
 				}
 			}
 			st.close();
 			cursor.close();
 			st = d.createStatement("SELECT * FROM demandantes order by nombre");
 			st.prepare();
 			cursor = st.getCursor();
 			while (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_demandante = row.getInteger(0);
 				String cedula = row.getString(1);
 				String nombre = row.getString(2);
 				String telefono = row.getString(3);
 				String direccion = row.getString(4);
 				String correo = row.getString(5);
 				String notas = row.getString(6);
 				Persona per = new Persona(1, cedula, nombre, telefono,
 						direccion, correo, notas,
 						Integer.toString(id_demandante));
 				if (id_demandante != 1) {
 					pers.addElement(per);
 				}
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		return pers;
 	}
 
 	public Persona consultarPersona(String id_persona, int tipo)
 			throws Exception {// Devuelve una persona especifica
 		Database d = null;
 		Persona per = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			if (tipo == 2) {
 				Statement st = d
 						.createStatement("SELECT * FROM demandados where id_demandado = ?");
 				st.prepare();
 				st.bind(1, id_persona);
 				Cursor cursor = st.getCursor();
 				if (cursor.next()) {
 					Row row = cursor.getRow();
 					int id_demandado = row.getInteger(0);
 					String cedula = row.getString(1);
 					String nombre = row.getString(2);
 					String telefono = row.getString(3);
 					String direccion = row.getString(4);
 					String correo = row.getString(5);
 					String notas = row.getString(6);
 					per = new Persona(2, cedula, nombre, telefono, direccion,
 							correo, notas, Integer.toString(id_demandado));
 				}
 				st.close();
 				cursor.close();
 			} else if (tipo == 1) {
 				Statement st = d
 						.createStatement("SELECT * FROM demandantes where id_demandante = ?");
 				st.prepare();
 				st.bind(1, id_persona);
 				Cursor cursor = st.getCursor();
 				if (cursor.next()) {
 					Row row = cursor.getRow();
 					int id_demandante = row.getInteger(0);
 					String cedula = row.getString(1);
 					String nombre = row.getString(2);
 					String telefono = row.getString(3);
 					String direccion = row.getString(4);
 					String correo = row.getString(5);
 					String notas = row.getString(6);
 					per = new Persona(1, cedula, nombre, telefono, direccion,
 							correo, notas, Integer.toString(id_demandante));
 				}
 				st.close();
 				cursor.close();
 			} else {
 				throw new Exception("tipo incorrecto para consultar Persona");
 			}
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		return per;
 	}
 
 	public Vector consultarProcesos() throws Exception {
 		Database d = null;
 		Vector procesos = new Vector();
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			// Statement st =
 			// d.createStatement("SELECT p.id_proceso, p.fecha_creacion, p.radicado, p.radicado_unico, p.estado, p.tipo, p.notas, p.prioridad, demandante.id_demandante, demandante.cedula, demandante.nombre, demandante.telefono, demandante.direccion, demandante.correo, demandante.notas, demandado.id_demandado, demandado.cedula, demandado.nombre, demandado.telefono, demandado.direccion, demandado.correo, demandado.notas, j.id_juzgado, j.nombre, j.ciudad, j.telefono, j.direccion, j.tipo, c.descripcion FROM procesos p, demandantes demandante, demandados demandado, juzgados j, categorias c WHERE p.id_demandante = demandante.id_demandante AND p.id_demandado = demandado.id_demandado AND p.id_juzgado = j.id_juzgado AND p.id_categoria = c.id_categoria ");
 			Statement st = d
 					.createStatement("SELECT p.id_proceso, p.id_demandante, p.id_demandado, p.fecha_creacion, p.radicado, p.radicado_unico, p.estado, p.tipo, p.notas, p.prioridad, p.id_juzgado, p.id_categoria FROM procesos p");
 			st.prepare();
 			Cursor cursor = st.getCursor();
 			while (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_proceso = row.getInteger(0);
 				int id_demandante = row.getInteger(1);
 				int id_demandado = row.getInteger(2);
 				String fecha_creacion = row.getString(3);
 				String radicado = row.getString(4);
 				String radicado_unico = row.getString(5);
 				String estado = row.getString(6);
 				String tipo = row.getString(7);
 				String notas = row.getString(8);
 				String prioridad = row.getString(9);
 				int id_juzgado = row.getInteger(10);
 				int id_categoria = row.getInteger(11);
 				Persona demandante = new Persona(1);
 				Persona demandado = new Persona(2);
 				Juzgado juzgado = new Juzgado();
 				Categoria categoria = new Categoria();
 				demandante.setId_persona(Integer.toString(id_demandante));
 				demandado.setId_persona(Integer.toString(id_demandado));
 				juzgado.setId_juzgado(Integer.toString(id_juzgado));
 				categoria.setId_categoria(Integer.toString(id_categoria));
 				Proceso proceso = new Proceso(Integer.toString(id_proceso),
 						demandante, demandado,
 						stringToCalendar(fecha_creacion), juzgado, radicado,
 						radicado_unico, new Vector(), estado, categoria, tipo,
 						notas, new Vector(), Integer.parseInt(prioridad));
 				procesos.addElement(proceso);
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		Enumeration e = procesos.elements();
 		while (e.hasMoreElements()) {
 			Proceso proceso_act = (Proceso) e.nextElement();
 			proceso_act.setDemandante(consultarPersona(proceso_act
 					.getDemandante().getId_persona(), 1));
 			proceso_act.setDemandado(consultarPersona(proceso_act
 					.getDemandado().getId_persona(), 2));
 			proceso_act.setJuzgado(consultarJuzgado(proceso_act.getJuzgado()
 					.getId_juzgado()));
 			proceso_act.setActuaciones(consultarActuaciones(proceso_act));
 			proceso_act.setCampos(consultarCampos(proceso_act));
 			proceso_act.setCategoria(consultarCategoria(proceso_act
 					.getCategoria().getId_categoria()));
 		}
 		return procesos;
 	}
 
 	public Proceso consultarProceso(String id_proceso) throws Exception {
 		Database d = null;
 		Proceso proceso = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT p.id_proceso, p.id_demandante, p.id_demandado, p.fecha_creacion, p.radicado, p.radicado_unico, p.estado, p.tipo, p.notas, p.prioridad, p.id_juzgado, p.id_categoria FROM procesos p WHERE p.id_proceso = ?");
 			st.prepare();
 			st.bind(1, id_proceso);
 			Cursor cursor = st.getCursor();
 			if (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_demandante = row.getInteger(1);
 				int id_demandado = row.getInteger(2);
 				String fecha_creacion = row.getString(3);
 				String radicado = row.getString(4);
 				String radicado_unico = row.getString(5);
 				String estado = row.getString(6);
 				String tipo = row.getString(7);
 				String notas = row.getString(8);
 				String prioridad = row.getString(9);
 				int id_juzgado = row.getInteger(10);
 				int id_categoria = row.getInteger(11);
 				Persona demandante = new Persona(1);
 				Persona demandado = new Persona(2);
 				Juzgado juzgado = new Juzgado();
 				Categoria categoria = new Categoria();
 				demandante.setId_persona(Integer.toString(id_demandante));
 				demandado.setId_persona(Integer.toString(id_demandado));
 				juzgado.setId_juzgado(Integer.toString(id_juzgado));
 				categoria.setId_categoria(Integer.toString(id_categoria));
 				proceso = new Proceso(id_proceso, demandante, demandado,
 						stringToCalendar(fecha_creacion), juzgado, radicado,
 						radicado_unico, new Vector(), estado, categoria, tipo,
 						notas, new Vector(), Integer.parseInt(prioridad));
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		proceso.setDemandante(consultarPersona(proceso.getDemandante()
 				.getId_persona(), 1));
 		proceso.setDemandado(consultarPersona(proceso.getDemandado()
 				.getId_persona(), 2));
 		proceso.setJuzgado(consultarJuzgado(proceso.getJuzgado()
 				.getId_juzgado()));
 		proceso.setActuaciones(consultarActuaciones(proceso));
 		proceso.setCampos(consultarCampos(proceso));
 		proceso.setCategoria(consultarCategoria(proceso.getCategoria()
 				.getId_categoria()));
 		return proceso;
 	}
 
 	public Vector consultarActuaciones(Proceso proceso) throws Exception {
 		Database d = null;
 		Vector actuaciones = new Vector();
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT id_actuacion, id_proceso, id_juzgado, fecha_creacion, fecha_proxima, descripcion, uid FROM actuaciones WHERE id_proceso = ? ORDER BY fecha_creacion, fecha_proxima");
 			st.prepare();
 			st.bind(1, proceso.getId_proceso());
 			Cursor cursor = st.getCursor();
 			while (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_actuacion = row.getInteger(0);
 				int id_juzgado = row.getInteger(2);
 				Calendar fecha_creacion = stringToCalendar(row.getString(3));
 				Calendar fecha_proxima = stringToCalendar(row.getString(4));
 				String descripcion = row.getString(5);
 				String uid = row.getString(6);
 				Juzgado juzgado = new Juzgado();
 				juzgado.setId_juzgado(Integer.toString(id_juzgado));
 				Actuacion actuacion = new Actuacion(juzgado, fecha_creacion,
 						fecha_proxima, descripcion,
 						Integer.toString(id_actuacion),uid);
 				actuaciones.addElement(actuacion);
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		Enumeration e = actuaciones.elements();
 		while (e.hasMoreElements()) {
 			Actuacion actuacion_act = (Actuacion) e.nextElement();
 			actuacion_act.setJuzgado(consultarJuzgado(actuacion_act
 					.getJuzgado().getId_juzgado()));
 		}
 		return actuaciones;
 	}
 
 	public Actuacion consultarActuacion(String id_actuacion) throws Exception {
 		Database d = null;
 		Actuacion actuacion = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT id_actuacion, id_proceso, id_juzgado, fecha_creacion, fecha_proxima, descripcion, uid FROM actuaciones WHERE id_actuacion = ?");
 			st.prepare();
 			st.bind(1, id_actuacion);
 			Cursor cursor = st.getCursor();
 			if (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_juzgado = row.getInteger(2);
 				Calendar fecha_creacion = stringToCalendar(row.getString(3));
 				Calendar fecha_proxima = stringToCalendar(row.getString(4));
 				String descripcion = row.getString(5);
 				String uid = row.getString(6);
 				Juzgado juzgado = new Juzgado();
 				juzgado.setId_juzgado(Integer.toString(id_juzgado));
 				actuacion = new Actuacion(juzgado, fecha_creacion,
 						fecha_proxima, descripcion, id_actuacion, uid);
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		actuacion.setJuzgado(consultarJuzgado(actuacion.getJuzgado()
 				.getId_juzgado()));
 		return actuacion;
 	}
 	public Vector consultarActuacionesCriticas(int cantidad) throws Exception {
 		Database d = null;
 		Vector actuaciones = new Vector();
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT id_actuacion, id_proceso, id_juzgado, fecha_creacion, fecha_proxima, descripcion, uid FROM actuaciones WHERE fecha_proxima >= date() ORDER BY fecha_proxima LIMIT ?");
 			st.prepare();
 			st.bind(1, cantidad);
 			Cursor cursor = st.getCursor();
 			while (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_actuacion = row.getInteger(0);
 				int id_juzgado = row.getInteger(2);
 				Calendar fecha_creacion = stringToCalendar(row.getString(3));
 				Calendar fecha_proxima = stringToCalendar(row.getString(4));
 				String descripcion = row.getString(5);
 				String uid = row.getString(6);
 				Juzgado juzgado = new Juzgado();
 				juzgado.setId_juzgado(Integer.toString(id_juzgado));
 				Actuacion actuacion = new Actuacion(juzgado, fecha_creacion,
 						fecha_proxima, descripcion,
 						Integer.toString(id_actuacion),uid);
 				actuaciones.addElement(actuacion);
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		Enumeration e = actuaciones.elements();
 		while (e.hasMoreElements()) {
 			Actuacion actuacion_act = (Actuacion) e.nextElement();
 			actuacion_act.setJuzgado(consultarJuzgado(actuacion_act
 					.getJuzgado().getId_juzgado()));
 		}
 		return actuaciones;
 	}
 	// Devuelve la lista de todos los juzgados
 	public Vector consultarJuzgados() throws Exception {
 		Database d = null;
 		Vector juzgados = new Vector();
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT * FROM juzgados order by nombre");
 			st.prepare();
 			Cursor cursor = st.getCursor();
 			while (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_juzgado = row.getInteger(0);
 				String nombre = row.getString(1);
 				String ciudad = row.getString(2);
 				String tipo = row.getString(3);
 				String direccion = row.getString(4);
 				String telefono = row.getString(5);
 				Juzgado juz = new Juzgado(nombre, ciudad, direccion, telefono,
 						tipo, Integer.toString(id_juzgado));
 				if (id_juzgado != 1) {
 					juzgados.addElement(juz);
 				}
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		return juzgados;
 	}
 
 	public Juzgado consultarJuzgado(String id_juzgado) throws Exception {
 		Database d = null;
 		Juzgado juz = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT * FROM juzgados where id_juzgado = ?");
 			st.prepare();
 			st.bind(1, id_juzgado);
 			Cursor cursor = st.getCursor();
 			if (cursor.next()) {
 				Row row = cursor.getRow();
 				String nombre = row.getString(1);
 				String ciudad = row.getString(2);
 				String tipo = row.getString(3);
 				String direccion = row.getString(4);
 				String telefono = row.getString(5);
 				juz = new Juzgado(nombre, ciudad, direccion, telefono, tipo,
 						id_juzgado);
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		return juz;
 	}
 
 	public Categoria consultarCategoria(String id_categoria) throws Exception {
 		Database d = null;
 		Categoria cat = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT id_categoria, descripcion FROM categorias where id_categoria = ?");
 			st.prepare();
 			st.bind(1, id_categoria);
 			Cursor cursor = st.getCursor();
 			if (cursor.next()) {
 				Row row = cursor.getRow();
 				String descripcion = row.getString(1);
 				cat = new Categoria(id_categoria, descripcion);
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		return cat;
 	}
 
 	public Vector consultarCategorias() throws Exception {
 		Database d = null;
 		Vector categorias = new Vector();
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT id_categoria, descripcion FROM categorias order by descripcion");
 			st.prepare();
 			Cursor cursor = st.getCursor();
 			while (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_categoria = row.getInteger(0);
 				String descripcion = row.getString(1);
 				Categoria cat = new Categoria(Integer.toString(id_categoria),
 						descripcion);
 				categorias.addElement(cat);
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		return categorias;
 	}
 
 
 	public Vector consultarCampos(Proceso proceso) throws Exception {
 		Database d = null;
 		Vector campos = new Vector();
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT at.id_atributo_proceso, at.id_atributo, at.valor, a.nombre,a.obligatorio,a.longitud_max, a.longitud_min FROM atributos_proceso at, atributos a WHERE at.id_atributo = a.id_atributo AND at.id_proceso = ?");
 			st.prepare();
 			st.bind(1, proceso.getId_proceso());
 			Cursor cursor = st.getCursor();
 			while (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_atributo_proceso = row.getInteger(0);
 				int id_atributo = row.getInteger(1);
 				String valor = row.getString(2);
 				String nombre = row.getString(3);
 				boolean obligatorio = row.getBoolean(4);
 				int longitud_max = row.getInteger(5);
 				int longitud_min = row.getInteger(6);
 				CampoPersonalizado campo = new CampoPersonalizado(Integer.toString(id_atributo_proceso), Integer.toString(id_atributo), nombre, valor, new Boolean(obligatorio), longitud_max, longitud_min);
 				campos.addElement(campo);
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		return campos;
 	}
 
 	public CampoPersonalizado consultarCampo(String id_campo) throws Exception {
 		Database d = null;
 		CampoPersonalizado campo = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT at.id_atributo_proceso, at.id_atributo, at.valor, a.nombre,a.obligatorio,a.longitud_max, a.longitud_min FROM atributos_proceso at, atributos a WHERE at.id_atributo = a.id_atributo AND at.id_atributo_proceso = ?");
 			st.prepare();
 			st.bind(1, id_campo);
 			Cursor cursor = st.getCursor();
 			if (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_atributo_proceso = row.getInteger(0);
 				int id_atributo = row.getInteger(1);
 				String valor = row.getString(2);
 				String nombre = row.getString(3);
 				boolean obligatorio = row.getBoolean(4);
 				int longitud_max = row.getInteger(5);
 				int longitud_min = row.getInteger(6);
 				campo = new CampoPersonalizado(Integer.toString(id_atributo_proceso), Integer.toString(id_atributo), nombre, valor, new Boolean(obligatorio), longitud_max, longitud_min);
 				
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		return campo;
 	}
 	public Vector consultarAtributos() throws Exception {
 		Database d = null;
 		Vector campos = new Vector();
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT id_atributo, nombre,obligatorio,longitud_max, longitud_min FROM  atributos");
 			st.prepare();
 			Cursor cursor = st.getCursor();
 			while (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_atributo = row.getInteger(0);
 				String nombre = row.getString(1);
 				boolean obligatorio = row.getBoolean(2);
 				int longitud_max = row.getInteger(3);
 				int longitud_min = row.getInteger(4);
 				String id_campo = null;
 				String valor = null;
 				CampoPersonalizado campo = new CampoPersonalizado(id_campo,Integer.toString(id_atributo),nombre,valor,new Boolean(obligatorio),longitud_max,longitud_min);
 				campos.addElement(campo);
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		return campos;
 	}
 	public Vector consultarPlantillas() throws Exception {
 		Database d = null;
 		Vector plantillas = new Vector();
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT p.id_plantilla, p.id_demandante, p.id_demandado, p.radicado, p.radicado_unico, p.estado, p.tipo, p.notas, p.prioridad, p.id_juzgado, p.id_categoria, p.nombre FROM plantillas p");
 			st.prepare();
 			Cursor cursor = st.getCursor();
 			while (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_plantilla = row.getInteger(0);
 				int id_demandante = row.getInteger(1);
 				int id_demandado = row.getInteger(2);
 				String radicado = row.getString(3);
 				String radicado_unico = row.getString(4);
 				String estado = row.getString(5);
 				String tipo = row.getString(6);
 				String notas = row.getString(7);
 				String prioridad = row.getString(8);
 				int id_juzgado = row.getInteger(9);
 				int id_categoria = row.getInteger(10);
 				String nombre = row.getString(11);
 				Persona demandante = new Persona(1);
 				Persona demandado = new Persona(2);
 				Juzgado juzgado = new Juzgado();
 				Categoria categoria = new Categoria();
 				demandante.setId_persona(Integer.toString(id_demandante));
 				demandado.setId_persona(Integer.toString(id_demandado));
 				juzgado.setId_juzgado(Integer.toString(id_juzgado));
 				categoria.setId_categoria(Integer.toString(id_categoria));
 				Plantilla plantilla = new Plantilla(nombre,Integer.toString(id_plantilla),demandante, demandado,juzgado,radicado,radicado_unico,estado,categoria,tipo,notas,new Vector(),Integer.parseInt(prioridad));
 				plantillas.addElement(plantilla);
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		Enumeration e = plantillas.elements();
 		while (e.hasMoreElements()) {
 			Plantilla plantilla_act = (Plantilla) e.nextElement();
 			plantilla_act.setDemandante(consultarPersona(plantilla_act
 					.getDemandante().getId_persona(), 1));
 			plantilla_act.setDemandado(consultarPersona(plantilla_act
 					.getDemandado().getId_persona(), 2));
 			plantilla_act.setJuzgado(consultarJuzgado(plantilla_act.getJuzgado()
 					.getId_juzgado()));
 			plantilla_act.setCampos(consultarCamposPlantilla(plantilla_act));
 			plantilla_act.setCategoria(consultarCategoria(plantilla_act
 					.getCategoria().getId_categoria()));
 		}
 		return plantillas;
 	}
 
 	public Plantilla consultarPlantilla(String id_plantilla) throws Exception {
 		Database d = null;
 		Plantilla plantilla = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT p.id_plantilla, p.id_demandante, p.id_demandado, p.radicado, p.radicado_unico, p.estado, p.tipo, p.notas, p.prioridad, p.id_juzgado, p.id_categoria, p.nombre FROM plantillas p WHERE p.id_plantilla = ?");
 			st.prepare();
 			st.bind(1, id_plantilla);
 			Cursor cursor = st.getCursor();
 			if (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_demandante = row.getInteger(1);
 				int id_demandado = row.getInteger(2);
 				String radicado = row.getString(3);
 				String radicado_unico = row.getString(4);
 				String estado = row.getString(5);
 				String tipo = row.getString(6);
 				String notas = row.getString(7);
 				String prioridad = row.getString(8);
 				int id_juzgado = row.getInteger(9);
 				int id_categoria = row.getInteger(10);
 				String nombre = row.getString(11);
 				Persona demandante = new Persona(1);
 				Persona demandado = new Persona(2);
 				Juzgado juzgado = new Juzgado();
 				Categoria categoria = new Categoria();
 				demandante.setId_persona(Integer.toString(id_demandante));
 				demandado.setId_persona(Integer.toString(id_demandado));
 				juzgado.setId_juzgado(Integer.toString(id_juzgado));
 				categoria.setId_categoria(Integer.toString(id_categoria));
 				plantilla = new Plantilla(nombre,id_plantilla,demandante, demandado,juzgado,radicado,radicado_unico,estado,categoria,tipo,notas,new Vector(),Integer.parseInt(prioridad));
 
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		plantilla.setDemandante(consultarPersona(plantilla.getDemandante()
 				.getId_persona(), 1));
 		plantilla.setDemandado(consultarPersona(plantilla.getDemandado()
 				.getId_persona(), 2));
 		plantilla.setJuzgado(consultarJuzgado(plantilla.getJuzgado()
 				.getId_juzgado()));
 		plantilla.setCampos(consultarCamposPlantilla(plantilla));
 		plantilla.setCategoria(consultarCategoria(plantilla.getCategoria()
 				.getId_categoria()));
 		return plantilla;
 	}
 	public Vector consultarCamposPlantilla(Plantilla plantilla) throws Exception {
 		Database d = null;
 		Vector campos = new Vector();
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT at.id_atributo_plantilla, at.id_atributo, at.valor, a.nombre,a.obligatorio,a.longitud_max, a.longitud_min FROM atributos_plantilla at, atributos a WHERE at.id_atributo = a.id_atributo AND at.id_plantilla = ?");
 			st.prepare();
 			st.bind(1, plantilla.getId_plantilla());
 			Cursor cursor = st.getCursor();
 			while (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_atributo_plantilla = row.getInteger(0);
 				int id_atributo = row.getInteger(1);
 				String valor = row.getString(2);
 				String nombre = row.getString(3);
 				boolean obligatorio = row.getBoolean(4);
 				int longitud_max = row.getInteger(5);
 				int longitud_min = row.getInteger(6);
 				CampoPersonalizado campo = new CampoPersonalizado(Integer.toString(id_atributo_plantilla), Integer.toString(id_atributo), nombre, valor, new Boolean(obligatorio), longitud_max, longitud_min);
 				campos.addElement(campo);
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		return campos;
 	}
 
 	public CampoPersonalizado consultarCampoPlantilla(String id_campo) throws Exception {
 		Database d = null;
 		CampoPersonalizado campo = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT at.id_atributo_plantilla, at.id_atributo, at.valor, a.nombre,a.obligatorio,a.longitud_max, a.longitud_min FROM atributos_plantilla at, atributos a WHERE at.id_atributo = a.id_atributo AND at.id_atributo_plantilla = ?");
 			st.prepare();
 			st.bind(1, id_campo);
 			Cursor cursor = st.getCursor();
 			if (cursor.next()) {
 				Row row = cursor.getRow();
 				int id_atributo_plantilla = row.getInteger(0);
 				int id_atributo = row.getInteger(1);
 				String valor = row.getString(2);
 				String nombre = row.getString(3);
 				boolean obligatorio = row.getBoolean(4);
 				int longitud_max = row.getInteger(5);
 				int longitud_min = row.getInteger(6);
 				campo = new CampoPersonalizado(Integer.toString(id_atributo_plantilla), Integer.toString(id_atributo), nombre, valor, new Boolean(obligatorio), longitud_max, longitud_min);
 				
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		return campo;
 	}
 	public long consultarPreferencia(int id_preferencia) throws Exception {
 		long valor = 0;
 		Database d = null;
 		try {
 			connMgr.prepararBD();
 			d = DatabaseFactory.open(connMgr.getDbLocation());
 			Statement st = d
 					.createStatement("SELECT valor FROM preferencias WHERE id_preferencia = ? ");
 			st.prepare();
 			st.bind(1, id_preferencia);
 			Cursor cursor = st.getCursor();
 			if (cursor.next()) {
 				Row row = cursor.getRow();
 				valor = row.getLong(0);
 				
 			}
 			st.close();
 			cursor.close();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 		}
 		return valor;	
 	}
 	private Calendar stringToCalendar(String fecha) {
 		Calendar calendar_return = Calendar.getInstance();
 		calendar_return.set(Calendar.YEAR,
 				Integer.parseInt(fecha.substring(0, 4)));
 		calendar_return.set(Calendar.MONTH,
 				Integer.parseInt(fecha.substring(5, 7)) - 1);
 		calendar_return.set(Calendar.DAY_OF_MONTH,
 				Integer.parseInt(fecha.substring(8, 10)));
 		if (fecha.length() > 10) {
 			calendar_return.set(Calendar.HOUR_OF_DAY,
 					Integer.parseInt(fecha.substring(11, 13)));
 			calendar_return.set(Calendar.MINUTE,
 					Integer.parseInt(fecha.substring(14, 16)));
 		}
 		return calendar_return;
 	}
 
 	private String calendarToString(Calendar fecha) {
 		String dia, mes, hora, minuto, nuevafecha;
 
 		if ((fecha.get(Calendar.MONTH) + 1) < 10) {
 			mes = "0" + (fecha.get(Calendar.MONTH) + 1);
 		} else {
 			mes = Integer.toString((fecha.get(Calendar.MONTH) + 1));
 		}
 		if (fecha.get(Calendar.DAY_OF_MONTH) < 10) {
 			dia = "0" + fecha.get(Calendar.DAY_OF_MONTH);
 		} else {
 			dia = Integer.toString(fecha.get(Calendar.DAY_OF_MONTH));
 		}
 		if (fecha.get(Calendar.HOUR_OF_DAY) < 10) {
 			hora = "0" + fecha.get(Calendar.HOUR_OF_DAY);
 		} else {
 			hora = Integer.toString(fecha.get(Calendar.HOUR_OF_DAY));
 		}
 		if (fecha.get(Calendar.MINUTE) < 10) {
 			minuto = "0" + fecha.get(Calendar.MINUTE);
 		} else {
 			minuto = Integer.toString(fecha.get(Calendar.MINUTE));
 		}
 		nuevafecha = fecha.get(Calendar.YEAR) + "-" + mes + "-" + dia + " "
 				+ hora + ":" + minuto;
 		return nuevafecha;
 	}
 
 	
 }
