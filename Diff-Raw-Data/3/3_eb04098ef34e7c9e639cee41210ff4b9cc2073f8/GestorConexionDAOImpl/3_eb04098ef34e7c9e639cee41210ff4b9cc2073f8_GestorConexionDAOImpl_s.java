 package edu.uoc.tdp.pac4.dao;
 
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.LinkedList;
 import java.util.List;
 
 
 import edu.uoc.tdp.pac4.beans.Mecanic;
 import edu.uoc.tdp.pac4.beans.Taller;
 import edu.uoc.tdp.pac4.beans.Usuari;
 import edu.uoc.tdp.pac4.exception.DAOException;
 
 
 /**
  * Smart Repair 
  * ETIG - TDP PAC 4 Primavera 2013
  * Grup: FiveCoreDumped
  */
 public class GestorConexionDAOImpl extends ConnectionPostgressDB implements GestorConexionDAO {
 
 	/**
 	 * Implementar las operaciones de base de datos
 	 * para el subsistema de Conexion y Mantenimiento
 	 */
 	
 	private static final String QUERY_ALL_USUARIS = 
 			"SELECT * FROM usuari";
 	
 	private static final String QUERY_USUARI_BY_ID = 
 			"SELECT * FROM usuari WHERE id = ?";
 	
 	private static final String QUERY_USUARI_BY_NIF = 
 			"SELECT * FROM usuari WHERE nif = ?";
 	
 	private static final String QUERY_USUARI_BY_USUARI = 
 			"SELECT * FROM usuari WHERE UPPER(usuari) = ?";
 	
 	private static final String QUERY_USUARIS_BY_FILTER = 
 			"SELECT * FROM usuari WHERE 1=1 ";
 	
 	private static final String UPDATE_DISABLE_USER = 
 			"UPDATE usuari " +
 			"   SET actiu = FALSE, " +
 			"       databaixa = now (), " +
 			"       datamodificacio = now () " +
 			" WHERE id = ?";
 	
 	private static final String QUERY_ALL_USUARIS_CAPTALLER = 
 			"SELECT * FROM usuari " +
 			" WHERE perfil LIKE '%JefeTaller%' ";
 	
 	private static final String QUERY_CAPTALLERS_DISPONIBLES = 
 			"SELECT * FROM usuari" +
 			" WHERE perfil LIKE '%JefeTaller%' " +
 			"   AND ((NOT EXISTS (SELECT 1 FROM taller " +
 			"                      WHERE taller.captaller = usuari.id " +
 			"                        AND actiu IS TRUE))" +
 			"    OR (taller = ?))";
 	
 	private static final String EXIST_USUARI = 
 			"SELECT * FROM usuari WHERE UPPER(nif)=? OR UPPER(usuari)=?";
 	
 	private static final String EXIST_USUARI_ID = 
 			"SELECT * FROM usuari WHERE id != ? AND (UPPER(nif)=? OR UPPER(usuari)=?)";
 	
 	private static final String ALTA_USUARI = 
 			"INSERT INTO usuari (nif, nom, cognoms, taller, usuari, perfil, contrasenya, actiu) " + 
 					" values (?,?,?,?,?,?,?,?)";
 	
 	private static final String UPDATE_USUARI =
 			"UPDATE USUARI " + 
 			   "SET nif = ?, " +
 			   "    nom = ?, " +
 			   "    cognoms = ?, " +
 			   "    taller = ?, " +
 			   "    usuari= ?, " +
 			   "    contrasenya = ?, " + 
 			   "    datamodificacio = now(), " +
 			   "    actiu = ? ," +
 			   "    databaixa = ? " +
 			 "WHERE id = ? ";
 	
 	private static final String CHANGE_PASSWORD = 
 			"UPDATE usuari " +
 			"   SET contrasenya= ? , " + 
 			"	    datamodificacio=now() " +
 			" WHERE id = ? ";
 
 	private static final String QUERY_MECANIC_BY_ID = 
 			"SELECT * FROM mecanic WHERE idmecanic = ?";
 	
 	private static final String ALTA_MECANIC = 
 			"INSERT INTO mecanic(idmecanic, disponible, idrep1, idrep2) " +
 			"VALUES (?, ?, ?, ?)";
 
 	private static final String UPDATE_ESTADO_MECANIC = 
 			"UPDATE mecanic " +
 			"   SET disponible = ? " +
 			" WHERE idmecanic = ?";
 
 	private static final String QUERY_ALL_TALLERS = 
 			"SELECT * FROM taller";
 	
 	private static final String QUERY_TALLER_BY_ID = 
 			"SELECT * FROM taller WHERE id = ?";
 	
 	private static final String QUERY_TALLER_BY_CIF = 
 			"SELECT * FROM taller WHERE cif = ?";
 	
 	private static final String QUERY_TALLERS_BY_FILTER = 
 			"SELECT * FROM taller WHERE 1=1 ";
 	
 	private static final String ALTA_TALLER = 
 			"INSERT INTO taller (cif, adreca, capacitat, captaller, telefon, web, actiu, dataapertura) " +
 			"VALUES ( ?, ?, ?, ?, ?, ?, ?, now())";
 	
 	private static final String UPDATE_TALLER = 
 			"UPDATE taller " +
 			"   SET cif = ?, " +
 			"       adreca = ?, " +
 			"       capacitat = ?, " +
 			"       captaller = ?, " +
 			"       telefon = ?, " +
 			"       web = ?, " +
 			"       actiu = ?,  " +
 			"       datamodificacio = now (), " +
 			"       databaixa = ? " +
 			" WHERE id = ?";
 
 	private static final String QUERY_ALTA_TALLER_BY_CAPTALLER = 
 			"SELECT * FROM taller " +
 			" WHERE captaller =  ? " +
 			"   AND actiu IS TRUE ";
 
 	private static final String QUERY_UPDATE_TALLER_BY_CAPTALLER = 
 			"SELECT * FROM taller " +
 			" WHERE captaller =  ? " +
 			"   AND actiu IS TRUE " +
 			"   AND id != ?  ";
 
 	private static final String QUERY_REPPENDIENTES_BY_TALLER = 
 			"SELECT COUNT(1) FROM  solicitud, reparacio, taller, usuari " +
 			" WHERE solicitud.numreparacio = reparacio.ordrereparacio " +
 			"   AND  reparacio.captaller = usuari.id " +
 			"   AND  usuari.taller = taller.id " +
 			"   AND  solicitud.finalitzada = FALSE " +
 			"   AND  taller.id = ?";
 	private static final String UPDATE_DISABLE_TALLER = 
 			"UPDATE taller " +
 			"   SET actiu = FALSE, " +
 			"       captaller = null, " +
 			"       databaixa = now (), " +
 			"       datamodificacio = now () " +
 			" WHERE id = ?";
 	
 	public GestorConexionDAOImpl (){
 		super();
 	}
 	
 	public Usuari getUsuariById(int id) throws DAOException {
 		getConnectionDB();	
 		Usuari result = null;
 		PreparedStatement ps = createPrepareStatment(QUERY_USUARI_BY_ID, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try{
 			ps.setInt(1, id);	
 			rs = ps.executeQuery();
 			if (rs.next()){
 				result = new Usuari(rs.getInt("id"), rs.getInt("taller"), rs.getString("usuari"), rs.getString("perfil"),
 						rs.getString("nif"), rs.getString("nom"), rs.getString("cognoms"),
 						rs.getString("contrasenya"), rs.getBoolean("actiu"), rs.getDate("dataAlta"), rs.getDate("dataModificacio"), 
 						rs.getDate("dataBaixa"), rs.getInt("reparacionsAssignades"));				
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}	
 	}
 	
 	public Usuari getUsuariByUsuari(String usuari) throws DAOException {
 		getConnectionDB();	
 		Usuari result = null;
 		PreparedStatement ps = createPrepareStatment(QUERY_USUARI_BY_USUARI, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try{
 			ps.setString(1, usuari);	
 			rs = ps.executeQuery();
 			if (rs.next()){
 				result = new Usuari(rs.getInt("id"), rs.getInt("taller"), rs.getString("usuari"), rs.getString("perfil"),
 						rs.getString("nif"), rs.getString("nom"), rs.getString("cognoms"),
 						rs.getString("contrasenya"), rs.getBoolean("actiu"), rs.getDate("dataAlta"), rs.getDate("dataModificacio"), 
 						rs.getDate("dataBaixa"), rs.getInt("reparacionsAssignades"));				
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}	
 	}
 
 	public Usuari getUsuariByNif(String nif) throws DAOException {
 		getConnectionDB();	
 		Usuari result = null;
 		PreparedStatement ps = createPrepareStatment(QUERY_USUARI_BY_NIF, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try{
 			ps.setString(1, nif);	
 			rs = ps.executeQuery();
 			if (rs.next()){
 				result = new Usuari(rs.getInt("id"), rs.getInt("taller"), rs.getString("usuari"), rs.getString("perfil"),
 						rs.getString("nif"), rs.getString("nom"), rs.getString("cognoms"),
 						rs.getString("contrasenya"), rs.getBoolean("actiu"), rs.getDate("dataAlta"), rs.getDate("dataModificacio"), 
 						rs.getDate("dataBaixa"), rs.getInt("reparacionsAssignades"));				
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}	
 	}
 	
 	public List<Usuari> getAllUsuaris() throws DAOException {
 		List<Usuari> result = new LinkedList<Usuari>();
 		getConnectionDB();	
 		
 		Statement stm = createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try {
 			rs = stm.executeQuery(QUERY_ALL_USUARIS);
 			while (rs.next()){
 				result.add(
 						new Usuari(rs.getInt("id"), rs.getInt("taller"), rs.getString("usuari"), rs.getString("perfil"), 
 								rs.getString("nif"), rs.getString("nom"), rs.getString("cognoms"),
 								rs.getString("contrasenya"), rs.getBoolean("actiu"), rs.getDate("dataAlta"), rs.getDate("dataModificacio"), 
 								rs.getDate("dataBaixa"), rs.getInt("reparacionsAssignades")));	
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(),  e);
 		} finally {
 			if (stm!=null) {
 				try {
 					stm.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs!=null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}
 	}
 
 	public void disableUser (int id) throws DAOException {
 		getConnectionDB();
 		PreparedStatement ps = createPrepareStatment(UPDATE_DISABLE_USER, ResultSet.CONCUR_UPDATABLE);
 		try {
 			ps.setInt(1, id);
 			ps.execute();
 		} catch (SQLException e){
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage() ,e);
 				}
 			}
 		}
 	}
 	
 	public List<Usuari> getUsuarisByFilter (String id, String nif, String nombre, String apellidos, String perfil)
 			throws DAOException {
 		getConnectionDB();
 		List<Usuari> result = new LinkedList<Usuari>();
 		String sql = QUERY_USUARIS_BY_FILTER;
 		if (id.length() > 0)
 			sql += " AND id = " + id;
 		if (nif.length() > 0)
 			sql += " AND UPPER(nif) like '" + nif.replace("*", "%").toUpperCase() +"'";
 		if ((nombre.length() > 0))
 			sql += " AND UPPER(nom) like '" + nombre.replace("*", "%").toUpperCase() +"'";
 		if (apellidos.length() > 0)
 			sql += " AND UPPER(cognoms) like '" + apellidos.replace("*", "%").toUpperCase() +"'";
 		if (perfil.length() > 0)
 			sql += " AND perfil like '%" + perfil +"%'";
 		getConnectionDB();	
 		
 		Statement stm = createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try {
 			rs = stm.executeQuery(sql);
 			while (rs.next()){
 				result.add(
 						new Usuari(rs.getInt("id"), rs.getInt("taller"), rs.getString("usuari"), rs.getString("perfil"), 
 								rs.getString("nif"), rs.getString("nom"), rs.getString("cognoms"),
 								rs.getString("contrasenya"), rs.getBoolean("actiu"), rs.getDate("dataAlta"), rs.getDate("dataModificacio"), 
 								rs.getDate("dataBaixa"), rs.getInt("reparacionsAssignades")));	
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (stm!=null) {
 				try {
 					stm.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs!=null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}
 		
 	}
 	
 	public List<Usuari> getUsuarisCapTaller () throws DAOException {
 		getConnectionDB();
 		List<Usuari> result = new LinkedList<Usuari> ();
 		Statement stm = createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try {
 			rs = stm.executeQuery(QUERY_ALL_USUARIS_CAPTALLER);
 			while (rs.next()){
 				result.add(
 						new Usuari(rs.getInt("id"), rs.getInt("taller"), rs.getString("usuari"), rs.getString("perfil"), 
 								rs.getString("nif"), rs.getString("nom"), rs.getString("cognoms"),
 								rs.getString("contrasenya"), rs.getBoolean("actiu"), rs.getDate("dataAlta"), rs.getDate("dataModificacio"), 
 								rs.getDate("dataBaixa"), rs.getInt("reparacionsAssignades")));	
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(),  e);
 		} finally {
 			if (stm!=null) {
 				try {
 					stm.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs!=null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}
 	}
 
 	public List<Usuari> getUsuarisCapTallerDisponbiles(int idTaller) throws DAOException{
 		getConnectionDB();
 		List<Usuari> result = new LinkedList<Usuari> ();
 		PreparedStatement ps = createPrepareStatment(QUERY_CAPTALLERS_DISPONIBLES, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try {
 			ps.setInt(1, idTaller);
 			rs = ps.executeQuery();
 			while (rs.next()){
 				result.add(
 						new Usuari(rs.getInt("id"), rs.getInt("taller"), rs.getString("usuari"), rs.getString("perfil"), 
 								rs.getString("nif"), rs.getString("nom"), rs.getString("cognoms"),
 								rs.getString("contrasenya"), rs.getBoolean("actiu"), rs.getDate("dataAlta"), rs.getDate("dataModificacio"), 
 								rs.getDate("dataBaixa"), rs.getInt("reparacionsAssignades")));	
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(),  e);
 		} finally {
 			if (ps!=null) {
 				try {
 					ps.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs!=null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}
 	}
 	
 	public boolean usuariExist (String nif, String usuari)
 			throws DAOException {
 		getConnectionDB();	
 		boolean result = false;
 		PreparedStatement ps = createPrepareStatment(EXIST_USUARI, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try{
 			ps.setString(1, nif.toUpperCase());	
 			ps.setString(2, usuari.toUpperCase());
 			rs = ps.executeQuery();
 			if (rs.next()){
 				result = true;
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}	
 	}
 
 	public boolean usuariExist (String nif, String usuari, int id)
 			throws DAOException{
 			getConnectionDB();	
 		boolean result = false;
 		PreparedStatement ps = createPrepareStatment(EXIST_USUARI_ID, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try{
 			ps.setInt(1, id);
 			ps.setString(2, nif.toUpperCase());	
 			ps.setString(3, usuari.toUpperCase());
 			rs = ps.executeQuery();
 			if (rs.next()){
 				result = true;
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}	
 	}	
 	public void altaUsuari(Usuari usuari)
 		throws DAOException {
 		getConnectionDB();
 		PreparedStatement ps = createPrepareStatment(ALTA_USUARI,ResultSet.CONCUR_UPDATABLE);
 		try {
 			ps.setString(1, usuari.getNif());
 			ps.setString(2, usuari.getNom());
 			ps.setString(3, usuari.getCognoms());
 			
 			if (usuari.getTaller() !=0)
 				ps.setInt(4, usuari.getTaller());
 			else
 				ps.setNull(4, Types.INTEGER);
 			
 			ps.setString(5, usuari.getUsuari());
 			ps.setString(6, usuari.getPerfil());
 			ps.setString(7, usuari.getContrasenya());
 			ps.setBoolean(8, usuari.isActiu());		
 			ps.execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 			
 		} finally {
 			if (ps != null) {
 				try {
 					ps.close();
 				} catch (SQLException e){
 					e.printStackTrace();
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 					
 				}
 			}
 		}
 	}
 	
 	public void modificarUsuari (Usuari usuari)
 			throws DAOException{
 		getConnectionDB();
 		PreparedStatement ps = createPrepareStatment(UPDATE_USUARI,ResultSet.CONCUR_UPDATABLE);
 
 		try {
 			ps.setString(1, usuari.getNif().toUpperCase());
 			ps.setString(2, usuari.getNom());
 			ps.setString(3, usuari.getCognoms());
 			if (usuari.getTaller() != 0) {
 				ps.setInt(4, usuari.getTaller());
 			} else {
 				ps.setNull(4, Types.INTEGER);
 			}
 			ps.setString(5,usuari.getUsuari().toUpperCase());
 			ps.setString(6,usuari.getContrasenya());
 			ps.setBoolean(7, usuari.isActiu());
 			if (usuari.isActiu()) {
 				ps.setNull(8, Types.DATE);
 			} else {
 				ps.setDate(8, (java.sql.Date) usuari.getDataBaixa());
 			}
 			ps.setInt(9,usuari.getId());
 			ps.execute();
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);				
 		} finally {
 			if (ps != null) {
 				try {
 					ps.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}
 	}
 	
 	public void changePassword (Usuari usuari, String password)
 			throws DAOException {
 		getConnectionDB();
 		PreparedStatement ps = createPrepareStatment(CHANGE_PASSWORD,ResultSet.CONCUR_UPDATABLE);
 
 		try {
 			ps.setString(1, password);
 			ps.setInt(2, usuari.getId());
 			ps.execute();
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);				
 		} finally {
 			if (ps != null) {
 				try {
 					ps.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}
 	}
 
 /*	public Mecanic getMecanicById (int idMecanic)
 			throws DAOException {
 		getConnectionDB();	
 		Mecanic result = null;
 		PreparedStatement ps = createPrepareStatment(QUERY_MECANIC_BY_ID, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try{
 			ps.setInt(1, idMecanic);	
 			rs = ps.executeQuery();
 			if (rs.next()){
 				result = new Mecanic(rs.getInt("idMecanic"), rs.getBoolean("disponible"), 
 						rs.getInt("idrep1"), rs.getInt("idrep2"));				
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}	
 	}*/
 	
 	public void altaMecanic(Mecanic mecanic)
 			throws DAOException {
 		getConnectionDB();
 		PreparedStatement ps = createPrepareStatment(ALTA_MECANIC,ResultSet.CONCUR_UPDATABLE);
 		try {
			ps.setInt(1, mecanic.getId());
 			ps.setBoolean(2, mecanic.isActiu());
 			ps.setNull(3, Types.INTEGER);
 			ps.setNull(4, Types.INTEGER);
 	
 			ps.execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 
 		} finally {
 			if (ps != null) {
 				try {
 					ps.close();
 				} catch (SQLException e){
 					e.printStackTrace();
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 
 				}
 			}
 		}			
 	}
 	
 	
 	public void estadoMecanic (int id, boolean disponible) throws DAOException {
 		getConnectionDB();
 		PreparedStatement ps = createPrepareStatment(UPDATE_ESTADO_MECANIC, ResultSet.CONCUR_UPDATABLE);
 		try {
 			ps.setBoolean(1, disponible);
 			ps.setInt(2, id);
 			ps.execute();
 		} catch (SQLException e){
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage() ,e);
 				}
 			}
 		}
 	}
 	
 	public Taller getTallerById (int id) throws DAOException {
 		Taller result = null;
 		getConnectionDB();	
 		PreparedStatement ps = createPrepareStatment(QUERY_TALLER_BY_ID, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try{
 			ps.setInt(1, id);	
 			rs = ps.executeQuery();
 			if (rs.next()){
 				result = new Taller(rs.getInt("id"), rs.getString("cif"), rs.getString("adreca"), rs.getInt("capacitat"), 
 						rs.getInt("capTaller"), rs.getString("telefon"), rs.getString("web"), rs.getBoolean("actiu"), 
 						rs.getDate("dataApertura"), rs.getDate("dataModificacio"), rs.getDate("dataBaixa"));				
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}	
 		
 	}
 
 	public Taller getTallerByCif(String cif) throws DAOException {
 		Taller result = null;
 		getConnectionDB();	
 		PreparedStatement ps = createPrepareStatment(QUERY_TALLER_BY_CIF, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try{
 			ps.setString(1, cif);	
 			rs = ps.executeQuery();
 			if (rs.next()){
 				result = new Taller(rs.getInt("id"), rs.getString("cif"), rs.getString("adreca"), rs.getInt("capacitat"), 
 						rs.getInt("capTaller"), rs.getString("telefon"), rs.getString("web"), rs.getBoolean("actiu"), 
 						rs.getDate("dataApertura"), rs.getDate("dataModificacio"), rs.getDate("dataBaixa"));				
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}	
 	}
 	
 	public List<Taller> getAllTallers () throws DAOException {
 		List<Taller> result = new LinkedList<Taller>();
 		getConnectionDB();	
 		
 		Statement stm = createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try {
 			rs = stm.executeQuery(QUERY_ALL_TALLERS);
 			while (rs.next()){			
 				result.add(
 						new Taller(rs.getInt("id"), rs.getString("cif"), rs.getString("adreca"), rs.getInt("capacitat"), 
 								rs.getInt("capTaller"), rs.getString("telefon"), rs.getString("web"), rs.getBoolean("actiu"), 
 								rs.getDate("dataApertura"), rs.getDate("dataModificacio"), rs.getDate("dataBaixa")));	
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(),  e);
 		} finally {
 			if (stm!=null) {
 				try {
 					stm.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs!=null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}
 	}
 	
 	public List<Taller>getTallersByFilter (String id, String cif, String adreca, String capacitat, String idCapTaller)
 			throws DAOException {
 		getConnectionDB();
 		List<Taller> result = new LinkedList<Taller>();
 		String sql = QUERY_TALLERS_BY_FILTER;
 		if (id.length() > 0)
 			sql += " AND id = " + id;
 		if (cif.length() > 0)
 			sql += " AND UPPER(cif) like '" + cif.replace("*", "%").toUpperCase() +"'";
 		if ((adreca.length() > 0))
 			sql += " AND UPPER(adreca) like '" + adreca.replace("*", "%").toUpperCase() +"'";
 		if (capacitat.length() > 0)
 			sql += " AND capacitat = " + capacitat;
 		if (idCapTaller.length() > 0)
 			sql += " AND captaller = " + idCapTaller;
 		getConnectionDB();	
 		
 		Statement stm = createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try {
 			rs = stm.executeQuery(sql);
 			while (rs.next()){
 				result.add(
 						new Taller(rs.getInt("id"), rs.getString("cif"), rs.getString("adreca"), rs.getInt("capacitat"), 
 								rs.getInt("capTaller"), rs.getString("telefon"), rs.getString("web"), rs.getBoolean("actiu"), 
 								rs.getDate("dataApertura"), rs.getDate("dataModificacio"), rs.getDate("dataBaixa")));	
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (stm!=null) {
 				try {
 					stm.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs!=null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}
 	}
 	
 	public Taller getTallersByCapTaller (int idUsuari)
 			throws DAOException {
 		Taller result = null;
 		getConnectionDB();
 		PreparedStatement ps = createPrepareStatment(QUERY_ALTA_TALLER_BY_CAPTALLER, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try{
 			ps.setInt(1, idUsuari);	
 			rs = ps.executeQuery();
 			if (rs.next()){
 				result = new Taller(rs.getInt("id"), rs.getString("cif"), rs.getString("adreca"), rs.getInt("capacitat"), 
 						rs.getInt("capTaller"), rs.getString("telefon"), rs.getString("web"), rs.getBoolean("actiu"), 
 						rs.getDate("dataApertura"), rs.getDate("dataModificacio"), rs.getDate("dataBaixa"));				
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}	
 		
 	}
 	
 	public Taller getTallersByCapTaller (int idUsuari, int idTaller)
 			throws DAOException {
 		Taller result = null;
 		getConnectionDB();
 		PreparedStatement ps = createPrepareStatment(QUERY_UPDATE_TALLER_BY_CAPTALLER, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try{
 			ps.setInt(1, idUsuari);
 			ps.setInt(2, idTaller);	
 			rs = ps.executeQuery();
 			if (rs.next()){
 				result = new Taller(rs.getInt("id"), rs.getString("cif"), rs.getString("adreca"), rs.getInt("capacitat"), 
 						rs.getInt("capTaller"), rs.getString("telefon"), rs.getString("web"), rs.getBoolean("actiu"), 
 						rs.getDate("dataApertura"), rs.getDate("dataModificacio"), rs.getDate("dataBaixa"));				
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}
 	}
 	
 	public void altaTaller (Taller taller) throws DAOException {
 		getConnectionDB();
 		PreparedStatement ps = createPrepareStatment(ALTA_TALLER);
 		try {
 			ps.setString(1, taller.getCif());
 			ps.setString(2, taller.getAdreca());
 			ps.setInt(3, taller.getCapacitat());
 			ps.setInt(4, taller.getCapTaller());
 			ps.setString(5, taller.getTelefon());
 			ps.setString(6, taller.getWeb());
 			ps.setBoolean(7, taller.isActiu());
 			ps.execute();
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null) {
 				try {
 					ps.close();				
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}
 		
 	}
 	
 	public void modificarTaller (Taller taller) throws DAOException {
 		getConnectionDB();
 		PreparedStatement ps = createPrepareStatment(UPDATE_TALLER, ResultSet.CONCUR_UPDATABLE);
 		try {
 			ps.setString(1, taller.getCif());
 			ps.setString(2, taller.getAdreca());
 			ps.setInt(3, taller.getCapacitat());
 			
 			if (taller.getCapTaller() == 0) {
 				ps.setNull(4, Types.INTEGER);
 			} else { 
 				ps.setInt(4, taller.getCapTaller());
 			}
 			ps.setString(5, taller.getTelefon());
 			ps.setString(6, taller.getWeb());
 			ps.setBoolean(7, taller.isActiu());
 			
 			if (taller.isActiu()) {
 				ps.setNull(8, Types.DATE);
 			} else {
 				ps.setDate(8, (java.sql.Date) taller.getDataBaixa()); 
 			}
 			
 			ps.setInt (9, taller.getId());
 			ps.execute();
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null) {
 				try {
 					ps.close();					
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}
 	}
 	
 	public int getNumRepPendTaller (int idTaller) throws DAOException {
 		getConnectionDB();
 		int result = 0;
 		PreparedStatement ps = createPrepareStatment(QUERY_REPPENDIENTES_BY_TALLER, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try {
 			ps.setInt(1, idTaller);
 			rs = ps.executeQuery();
 			if (rs.next()) {
 				result =  rs.getInt(1);
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}		
 	}
 	
 	public void disableTaller (int id) throws DAOException {
 		getConnectionDB();
 		PreparedStatement ps = createPrepareStatment(UPDATE_DISABLE_TALLER, ResultSet.CONCUR_UPDATABLE);
 		try {
 			ps.setInt(1, id);
 			ps.execute();
 		} catch (SQLException e){
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage() ,e);
 				}
 			}
 		}
 	}	
 }
 
