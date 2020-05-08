 package es.icarto.gvsig.fonsagua.reports.utils;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import es.udc.cartolab.gvsig.fonsagua.forms.alternativas.AlternativasForm;
import es.udc.cartolab.gvsig.fonsagua.forms.alternativas.PreferenciasForm;
import es.udc.cartolab.gvsig.fonsagua.forms.alternativas.PresupuestoForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.comunidades.ComunidadesForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.fuentes.FuentesForm;
 import es.udc.cartolab.gvsig.fonsagua.utils.FonsaguaConstants;
 import es.udc.cartolab.gvsig.users.utils.DBSession;
 
 public class ReportDAO {
 
     private static Connection connection = DBSession.getCurrentSession()
 	    .getJavaConnection();
 
     public static ResultSet getCommunityValues(String communityCode) {
 	PreparedStatement statement = null;
 
 	try {
 	    String query = "SELECT * FROM " + FonsaguaConstants.dataSchema
 		    + "." + ComunidadesForm.NAME + " WHERE "
 		    + ComunidadesForm.PKFIELD + " = ?";
 	    statement = connection.prepareStatement(query);
 	    statement.setString(1, communityCode);
 	    statement.execute();
 	    ResultSet rs = statement.getResultSet();
 	    return rs;
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return null;
     }
 
     public static String getCommunityValueByColumnName(String columnName,
 	    String communityCode) {
 	ResultSet rs = getCommunityValues(communityCode);
 	String value;
 	try {
 	    rs.next();
 	    value = rs.getString(columnName);
 	    return value;
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return null;
     }
 
     public static ResultSet getAlternativeValues(String alternativeCode) {
 	PreparedStatement statement = null;
 
 	try {
 	    String query = "SELECT * FROM " + FonsaguaConstants.dataSchema
 		    + "." + AlternativasForm.NAME + " WHERE "
 		    + AlternativasForm.PKFIELD + " = ?";
 	    statement = connection.prepareStatement(query);
 	    statement.setString(1, alternativeCode);
 	    statement.execute();
 	    ResultSet rs = statement.getResultSet();
 	    return rs;
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return null;
     }
 
     public static String getAlternativeValueByColumnName(String columnName,
 	    String alternativeCode) {
 	ResultSet rs = getAlternativeValues(alternativeCode);
 	String value;
 	try {
 	    rs.next();
 	    value = rs.getString(columnName);
 	    return value;
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return null;
     }
 
     public static ResultSet getPresupuestoValues(String alternativeCode) {
 	PreparedStatement statement = null;
 
 	try {
 	    String query = "SELECT * FROM " + FonsaguaConstants.dataSchema
		    + "." + PresupuestoForm.NAME + " WHERE "
 		    + AlternativasForm.PKFIELD + " = ?";
 	    statement = connection.prepareStatement(query);
 	    statement.setString(1, alternativeCode);
 	    statement.execute();
 	    ResultSet rs = statement.getResultSet();
 	    return rs;
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return null;
     }
 
     public static ResultSet getFonsaguaTableValues(String tableName,
 	    String pkName, String pkValue) {
 	PreparedStatement statement = null;
 
 	try {
 	    String query = "SELECT * FROM " + FonsaguaConstants.dataSchema
 		    + "." + tableName + " WHERE " + pkName + " = ?";
 	    statement = connection.prepareStatement(query);
 	    statement.setString(1, pkValue);
 	    statement.execute();
 	    ResultSet rs = statement.getResultSet();
 	    return rs;
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return null;
     }
 
     public static String getFonsaguaTableValueByColumnName(String tableName,
 	    String pkName, String pkValue, String columnName) {
 	ResultSet rs = getFonsaguaTableValues(tableName, pkName, pkValue);
 	String value;
 	try {
 	    rs.next();
 	    value = rs.getString(columnName);
 	    return value;
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return null;
     }
 
     public static String[][] getDataForCommunityRelatedTable(String tableName,
 	    String[] fieldNames, String communityCode) {
 	String whereClause = ComunidadesForm.PKFIELD + " = '" + communityCode
 		+ "'";
 	try {
 	    return DBSession.getCurrentSession().getTable(tableName,
 		    FonsaguaConstants.dataSchema, fieldNames, whereClause,
 		    null, false);
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return null;
     }
 
     public static String[][] getDataForAlternativeRelatedTable(
 	    String tableName, String[] fieldNames, String alternativeCode) {
 	String whereClause = AlternativasForm.PKFIELD + " = '"
 		+ alternativeCode + "'";
 	try {
 	    return DBSession.getCurrentSession().getTable(tableName,
 		    FonsaguaConstants.dataSchema, fieldNames, whereClause,
 		    null, false);
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return null;
     }
 
     public static int getNumberOfElementsFromRelationshipTable(
 	    String relationshipTableName, String codeField, String codeValue) {
 	PreparedStatement statement = null;
 
 	try {
 	    String query = "SELECT count(*) FROM "
 		    + FonsaguaConstants.dataSchema + "."
 		    + relationshipTableName + " WHERE " + codeField + " = ?";
 	    statement = connection.prepareStatement(query);
 	    statement.setString(1, codeValue);
 	    statement.execute();
 	    ResultSet rs = statement.getResultSet();
 	    rs.next();
 	    return rs.getInt(1);
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return -1;
     }
 
     // TODO: Try to make a generic method
     public static String[][] getDataFromAbastecimientosTableByCommunity(
 	    String communityCode) {
 	PreparedStatement statement = null;
 	String[] colNames = { "abastecimiento", "a.cod_abastecimiento" };
 	try {
 	    String query = "SELECT ";
 	    for (String name : colNames) {
 		query = query + name + ", ";
 	    }
 	    query = query.substring(0, query.length() - 2);
 	    query = query + " FROM " + FonsaguaConstants.dataSchema + "."
 		    + "abastecimientos a, " + FonsaguaConstants.dataSchema
 		    + "." + "comunidades c, " + FonsaguaConstants.dataSchema
 		    + "." + "r_abastecimientos_comunidades r"
 		    + " WHERE a.cod_abastecimiento = r.cod_abastecimiento AND "
 		    + "c.cod_comunidad = r.cod_comunidad AND "
 		    + "c.cod_comunidad = ?";
 	    statement = connection.prepareStatement(query);
 	    statement.setString(1, communityCode);
 	    statement.execute();
 	    ResultSet rs = statement.getResultSet();
 
 	    ArrayList<String[]> rows = new ArrayList<String[]>();
 	    while (rs.next()) {
 		String[] row = new String[colNames.length];
 		for (int i = 0; i < colNames.length; i++) {
 		    String val = rs.getString(i + 1);
 		    if (val == null) {
 			val = "";
 		    }
 		    row[i] = val;
 		}
 		rows.add(row);
 	    }
 	    rs.close();
 
 	    return rows.toArray(new String[0][0]);
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return null;
     }
 
     public static String[][] getDataOfElementOfAbastecimientoByCommunity(
 	    String elementTableName, String[] colNames, String communityCode) {
 	PreparedStatement statement = null;
 
 	try {
 	    String query = "SELECT ";
 	    for (String name : colNames) {
 		query = query + name + ", ";
 	    }
 	    query = query.substring(0, query.length() - 2);
 	    query = query + " FROM " + FonsaguaConstants.dataSchema + "."
 		    + elementTableName + " t, " + FonsaguaConstants.dataSchema
 		    + "." + "abastecimientos a, "
 		    + FonsaguaConstants.dataSchema + "." + "comunidades c, "
 		    + FonsaguaConstants.dataSchema + "."
 		    + "r_abastecimientos_comunidades r"
 		    + " WHERE t.cod_abastecimiento = a.cod_abastecimiento AND "
 		    + "a.cod_abastecimiento = r.cod_abastecimiento AND "
 		    + "c.cod_comunidad = r.cod_comunidad AND "
 		    + "c.cod_comunidad = ? order by t.cod_abastecimiento";
 	    statement = connection.prepareStatement(query);
 	    statement.setString(1, communityCode);
 	    statement.execute();
 	    ResultSet rs = statement.getResultSet();
 
 	    ArrayList<String[]> rows = new ArrayList<String[]>();
 	    while (rs.next()) {
 		String[] row = new String[colNames.length];
 		for (int i = 0; i < colNames.length; i++) {
 		    String val = rs.getString(i + 1);
 		    if (val == null) {
 			val = "";
 		    }
 		    row[i] = val;
 		}
 		rows.add(row);
 	    }
 	    rs.close();
 
 	    return rows.toArray(new String[0][0]);
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return null;
     }
 
     public static String[][] getDataOfFuentesByCommunity(String communityCode) {
 	PreparedStatement statement = null;
 	String[] colNames = { "f.fuente", "f.cod_fuente" };
 	try {
 	    String query = "SELECT ";
 	    for (String name : colNames) {
 		query = query + name + ", ";
 	    }
 	    query = query.substring(0, query.length() - 2);
 	    query = query + " FROM " + FonsaguaConstants.dataSchema + "."
 		    + "fuentes f, " + FonsaguaConstants.dataSchema + "."
 		    + "r_abastecimientos_fuentes raf, "
 		    + FonsaguaConstants.dataSchema + "."
 		    + "r_abastecimientos_comunidades rac"
 		    + " WHERE f.cod_fuente = raf.cod_fuente AND "
 		    + "raf.cod_abastecimiento = rac.cod_abastecimiento AND "
 		    + "rac.cod_comunidad = ? group by f.fuente, f.cod_fuente";
 	    statement = connection.prepareStatement(query);
 	    statement.setString(1, communityCode);
 	    statement.execute();
 	    ResultSet rs = statement.getResultSet();
 
 	    ArrayList<String[]> rows = new ArrayList<String[]>();
 	    while (rs.next()) {
 		String[] row = new String[colNames.length];
 		for (int i = 0; i < colNames.length; i++) {
 		    String val = rs.getString(i + 1);
 		    if (val == null) {
 			val = "";
 		    }
 		    row[i] = val;
 		}
 		rows.add(row);
 	    }
 	    rs.close();
 
 	    return rows.toArray(new String[0][0]);
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return null;
     }
 
     public static String[][] getDataOfElementOfFuentesByCommunity(
 	    String elementTableName, String[] colNames, String communityCode) {
 	PreparedStatement statement = null;
 	ArrayList<String> fuentesCodes = new ArrayList<String>();
 	for (String[] row : getDataOfFuentesByCommunity(communityCode)) {
 	    fuentesCodes.add(row[1]);
 	}
 	ArrayList<String[]> rows = new ArrayList<String[]>();
 	for (String code : fuentesCodes) {
 	    try {
 		String query = "SELECT ";
 		for (String name : colNames) {
 		    query = query + name + ", ";
 		}
 		query = query.substring(0, query.length() - 2);
 		query = query + " FROM " + FonsaguaConstants.dataSchema + "."
 			+ elementTableName + " WHERE " + FuentesForm.PKFIELD
 			+ " = ?";
 		statement = connection.prepareStatement(query);
 		statement.setString(1, code);
 		statement.execute();
 		ResultSet rs = statement.getResultSet();
 
 		while (rs.next()) {
 		    String[] row = new String[colNames.length];
 		    for (int i = 0; i < colNames.length; i++) {
 			String val = rs.getString(i + 1);
 			if (val == null) {
 			    val = "";
 			}
 			row[i] = val;
 		    }
 		    rows.add(row);
 		}
 		rs.close();
 
 	    } catch (SQLException e) {
 		e.printStackTrace();
 	    }
 	}
 	return rows.toArray(new String[0][0]);
     }
 
     public static String getDotacion(String alternativeCode) {
 	String tipoDistribucion = getFonsaguaTableValueByColumnName(
 		AlternativasForm.NAME, AlternativasForm.PKFIELD,
 		alternativeCode, "tipo_distribucion");
 	if (tipoDistribucion.equalsIgnoreCase("Cantareras")) {
	    return getFonsaguaTableValueByColumnName(PreferenciasForm.NAME,
 		    AlternativasForm.PKFIELD, alternativeCode, "dot_cantareras");
 	} else if (tipoDistribucion.equalsIgnoreCase("Domiciliar")) {
	    return getFonsaguaTableValueByColumnName(PreferenciasForm.NAME,
 		    AlternativasForm.PKFIELD, alternativeCode, "dot_domiciliar");
 	} else {
 	    return null;
 	}
     }
 }
