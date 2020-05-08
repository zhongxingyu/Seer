 package es.udc.cartolab.cit.gvsig.fmap.drivers.jdbc.spatialite;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.sql.SQLException;
 import java.sql.Types;
 
 import com.hardcode.gdbms.engine.values.NullValue;
 import com.hardcode.gdbms.engine.values.Value;
 import com.iver.cit.gvsig.exceptions.visitors.ProcessVisitorException;
 import com.iver.cit.gvsig.fmap.core.FPolygon2D;
 import com.iver.cit.gvsig.fmap.core.FPolyline2D;
 import com.iver.cit.gvsig.fmap.core.FShape;
 import com.iver.cit.gvsig.fmap.core.FShape3D;
 import com.iver.cit.gvsig.fmap.core.FShapeM;
 import com.iver.cit.gvsig.fmap.core.GeneralPathX;
 import com.iver.cit.gvsig.fmap.core.IFeature;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.core.IGeometry3D;
 import com.iver.cit.gvsig.fmap.core.IGeometryM;
 import com.iver.cit.gvsig.fmap.core.ShapeFactory;
 import com.iver.cit.gvsig.fmap.core.ShapeMFactory;
 import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
 import com.iver.cit.gvsig.fmap.drivers.DefaultJDBCDriver;
 import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
 import com.iver.cit.gvsig.fmap.drivers.XTypes;
 import com.vividsolutions.jts.geom.Geometry;
 
 /**
  * The whole class is based on the gvSIG PostGIS driver.
  * 
  * @author Jorge Lpez Fernndez (jlopez@cartolab.es)
  */
 public class SpatiaLite {
 
 	private String toEncode;
 
 	/**
 	 * @param val
 	 * @return
 	 */
 	public static boolean isNumeric(Value val) {
 
 		switch (val.getSQLType()) {
 		case Types.DOUBLE:
 		case Types.FLOAT:
 		case Types.INTEGER:
 		case Types.SMALLINT:
 		case Types.BIGINT:
 		case Types.NUMERIC:
 		case Types.REAL:
 		case Types.TINYINT:
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * @param val
 	 * @return
 	 */
 	public static boolean isGeometryType(String val) {
 
 		if (val.compareToIgnoreCase("POINT") == 0)
 			return true;
 		else if (val.compareToIgnoreCase("LINESTRING") == 0)
 			return true;
 		else if (val.compareToIgnoreCase("POLYGON") == 0)
 			return true;
 		else if (val.compareToIgnoreCase("MULTIPOINT") == 0)
 			return true;
 		else if (val.compareToIgnoreCase("MULTILINESTRING") == 0)
 			return true;
 		else if (val.compareToIgnoreCase("MULTILINESTRINGM") == 0) // MCoord
 			return true;
 		else if (val.compareToIgnoreCase("MULTIPOLYGON") == 0)
 			return true;
 
 		return false;
 	}
 
 	public String getConnectionStringBeginning() {
 		return "jdbc:sqlite:";
 	}
 
 	/**
 	 * @param dbLayerDef
 	 * @param fieldsDescr
 	 * @param bCreateGID
 	 * @DEPRECATED
 	 * @return
 	 */
 	public String getSqlCreateSpatialTable(DBLayerDefinition dbLayerDef,
 			boolean bCreateGID) {
 
 		FieldDescription[] fieldsDescr = dbLayerDef.getFieldsDesc();
 		String resul;
 		resul = "CREATE TABLE " + dbLayerDef.getTableName() + " ( " + "\""
 				+ dbLayerDef.getFieldID() + "\"" + " serial PRIMARY KEY ";
 		int j = 0;
 		for (int i = 0; i < dbLayerDef.getFieldNames().length; i++) {
 			int fieldType = fieldsDescr[i].getFieldType();
 			String strType = XTypes.fieldTypeToString(fieldType);
 			if (fieldsDescr[i].getFieldName().equalsIgnoreCase(
 					dbLayerDef.getFieldID()))
 				continue;
 			resul = resul + ", " + "\"" + dbLayerDef.getFieldNames()[i] + "\""
 					+ " " + strType;
 			j++;
 		}
 		resul = resul + ");";
 		return resul;
 	}
 
 	public String getSqlAddGeometryColumn(DBLayerDefinition dbLayerDef) {
 		String strGeometryFieldType;
 
 		switch (dbLayerDef.getShapeType()) {
 		case FShape.POINT:
 			strGeometryFieldType = XTypes.fieldTypeToString(XTypes.POINT2D);
 			break;
 		case FShape.LINE:
 			strGeometryFieldType = XTypes.fieldTypeToString(XTypes.LINE2D);
 			break;
 		case FShape.POLYGON:
 			strGeometryFieldType = XTypes.fieldTypeToString(XTypes.POLYGON2D);
 			break;
 		case FShape.MULTI:
 			strGeometryFieldType = XTypes.fieldTypeToString(XTypes.MULTI2D);
 			break;
 		case FShape.MULTIPOINT:
 			strGeometryFieldType = XTypes.fieldTypeToString(XTypes.MULTIPOINT);
 			break;
 		default:
 			strGeometryFieldType = "GEOMETRY";
 		}
 
 		String result = "SELECT AddGeometryColumn('"
 				+ dbLayerDef.getTableName() + "', '"
 				+ dbLayerDef.getFieldGeometry() + "', "
 				+ DefaultJDBCDriver.removePrefix(dbLayerDef.getSRID_EPSG())
 				+ ", '" + strGeometryFieldType + "', "
 				+ dbLayerDef.getDimension() + ");";
 
 		return result;
 	}
 
 	/**
 	 * From geotools Adds quotes to an object for storage in spatialite.
 	 * 
 	 * @param value
 	 *            The object to add quotes to.
 	 * 
 	 * @return a string representation of the object with quotes.
 	 */
 	protected String addQuotes(Object value) {
 		String retString;
 
 		if (value != null) {
 			if (value instanceof NullValue)
 				retString = "null";
 			else
 				retString = "'" + doubleQuote(value) + "'";
 
 		} else {
 			retString = "null";
 		}
 
 		return retString;
 	}
 
 	/**
 	 * @param obj
 	 * @return
 	 */
 	private String doubleQuote(Object obj) {
 		String aux = obj.toString().replaceAll("'", "''");
 		StringBuffer strBuf = new StringBuffer(aux);
 		ByteArrayOutputStream out = new ByteArrayOutputStream(strBuf.length());
 		String aux2 = "Encoding ERROR";
 
 		try {
 			PrintStream printStream = new PrintStream(out, true, toEncode);
 			printStream.print(aux);
 			aux2 = out.toString(toEncode);
 			// System.out.println(aux + " " + aux2);
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return aux2;
 	}
 
 	/**
 	 * @param dbLayerDef
 	 * @param feat
 	 * @return
 	 * @throws SQLException
 	 * @throws ProcessVisitorException
 	 */
 	public String getSqlInsertFeature(DBLayerDefinition dbLayerDef,
 			IFeature feat) throws ProcessVisitorException {
 		StringBuffer sqlBuf = new StringBuffer("INSERT INTO "
 				+ dbLayerDef.getTableName() + " (");
 		String sql = null;
 		int numAlphanumericFields = dbLayerDef.getFieldNames().length;
 		for (int i = 0; i < numAlphanumericFields; i++) {
 			String name = dbLayerDef.getFieldsDesc()[i].getFieldName();
 			if (name.equals(dbLayerDef.getFieldID()))
 				continue;
 			sqlBuf.append(" " + "\"" + name + "\"" + ",");
 		}
 		sqlBuf.append(" \"" + dbLayerDef.getFieldGeometry() + "\"");
 		sqlBuf.append(" ) VALUES (");
 		String insertQueryHead = sqlBuf.toString();
 		sqlBuf = new StringBuffer(insertQueryHead);
 		for (int j = 0; j < numAlphanumericFields; j++) {
 			String name = dbLayerDef.getFieldsDesc()[j].getFieldName();
 			if (name.equals(dbLayerDef.getFieldID()))
 				continue;
 
 			if (isNumeric(feat.getAttribute(j))) {
 				sqlBuf.append(feat.getAttribute(j) + ", ");
 			} else {
 				sqlBuf.append(addQuotes(feat.getAttribute(j)) + ", ");
 			}
 		}
 		IGeometry geometry = feat.getGeometry();
 		int type = dbLayerDef.getShapeType();
 		if (geometry.getGeometryType() != type) {
 			if (type == FShape.POLYGON) {
 				geometry = ShapeFactory.createPolygon2D(new GeneralPathX(
 						geometry.getInternalShape()));
 			} else if (type == FShape.LINE) {
 				geometry = ShapeFactory.createPolyline2D(new GeneralPathX(
 						geometry.getInternalShape()));
 			} else if (type == (FShape.POLYGON | FShape.Z)) {
 				geometry = ShapeFactory.createPolygon3D(new GeneralPathX(
 						geometry.getInternalShape()), ((IGeometry3D) geometry)
 						.getZs());
 			} else if (type == (FShape.LINE | FShape.Z)) {
 				geometry = ShapeFactory.createPolyline3D(new GeneralPathX(
 						geometry.getInternalShape()), ((IGeometry3D) geometry)
 						.getZs());
 			} else if (type == (FShape.LINE | FShape.M)) { // MCoord
 				geometry = ShapeMFactory.createPolyline2DM(new GeneralPathX(
 						geometry.getInternalShape()), ((IGeometryM) geometry)
 						.getMs()); // MCoord
 			}
 		}
 		if (!isCorrectGeometry(geometry, type))
 			throw new ProcessVisitorException("incorrect_geometry",
 					new Exception());
 		// MCoord
 		if (((type & FShape.M) != 0) && ((type & FShape.MULTIPOINT) == 0)) {
 			sqlBuf.append(" geomfromewkt( 'SRID="
 					+ DefaultJDBCDriver.removePrefix(dbLayerDef.getSRID_EPSG())
 					+ ";" + ((FShapeM) geometry.getInternalShape()).toText()
 					+ "')");
 		} else
 		// ZCoord
 		if ((type & FShape.Z) != 0) {
 			if ((type & FShape.MULTIPOINT) != 0) {
 				// TODO: Metodo toText 3D o 2DM
 			} else {
 				// Its not a multipoint
 				sqlBuf.append(" geomfromewkt( 'SRID="
 						+ DefaultJDBCDriver.removePrefix(dbLayerDef
 								.getSRID_EPSG())
 						+ ";"
 						+ ((FShape3D) feat.getGeometry().getInternalShape())
 								.toText() + "')");
 			}
 
 		}
 		// XYCoord
 		else {
 			Geometry jtsGeom = geometry.toJTSGeometry();
 			if (jtsGeom == null || !isCorrectType(jtsGeom, type)) {
 				throw new ProcessVisitorException("incorrect_geometry",
 						new Exception());
 			}
 			sqlBuf.append(" geomfromewkt( 'SRID="
 					+ DefaultJDBCDriver.removePrefix(dbLayerDef.getSRID_EPSG())
 					+ ";" + jtsGeom.toText() + "')");
 		}
 		sqlBuf.append(" );");
 		sql = sqlBuf.toString();
 		return sql;
 	}
 
 	private boolean isCorrectType(Geometry jtsGeom, int type) {
 		switch (type) {
 		case FShape.POLYGON:
 			return (jtsGeom.getGeometryType().equals("MultiPolygon") || jtsGeom
 					.getGeometryType().equals("Polygon"));
 		case FShape.LINE:
 			return (jtsGeom.getGeometryType().equals("MultiLineString") || jtsGeom
 					.getGeometryType().equals("LineString"));
 		case FShape.POINT:
 			return jtsGeom.getGeometryType().equals("Point");
 		case FShape.MULTIPOINT:
 			return jtsGeom.getGeometryType().equals("MultiPoint");
 		case FShape.MULTI:
 			return true;
 		}
 		return false;
 	}
 
 	private boolean isCorrectGeometry(IGeometry geometry, int type) {
 		if (FShape.POLYGON == type) {
 			FPolygon2D polygon = (FPolygon2D) geometry.getInternalShape();
 			if (!(polygon.getBounds2D().getWidth() > 0 && polygon.getBounds2D()
 					.getHeight() > 0))
 				return false;
 		} else if (FShape.LINE == type) {
 			FPolyline2D line = (FPolyline2D) geometry.getInternalShape();
 			if (!(line.getBounds2D().getWidth() > 0 || line.getBounds2D()
 					.getHeight() > 0))
 				return false;
 		}
 
 		return true;
 	}
 
 	public String getSqlCreateIndex(DBLayerDefinition lyrDef) {
 		String sql = "SELECT CreateSpatialIndex('" + lyrDef.getTableName()
 				+ "', '" + lyrDef.getFieldGeometry() + "');";
 
 		return sql;
 	}
 
 	public String getSqlModifyFeature(DBLayerDefinition dbLayerDef,
 			IFeature feat) {
 		StringBuffer sqlBuf = new StringBuffer("UPDATE "
 				+ dbLayerDef.getComposedTableName() + " SET");
 		String sql = null;
 		int numAlphanumericFields = dbLayerDef.getFieldsDesc().length;
 
 		for (int i = 0; i < numAlphanumericFields; i++) {
 			FieldDescription fldDesc = dbLayerDef.getFieldsDesc()[i];
 			if (fldDesc != null) {
 				String name = fldDesc.getFieldName();
 				// El campo gid no lo actualizamos.
 				if (name.equalsIgnoreCase(dbLayerDef.getFieldID()))
 					continue;
 				Value val = feat.getAttribute(i);
 				if (val != null) {
 					if (isNumeric(val)) {
 						sqlBuf.append(" " + "\"" + name + "\"" + " = " + val
 								+ " ,");
 					} else {
 						sqlBuf.append(" " + "\"" + name + "\"" + " = "
 								+ addQuotes(val) + " ,");
 					}
 				}
 			}
 		}
 		// If pos > 0 there is at least one field..
 		int pos = sqlBuf.lastIndexOf(",");
 		if (pos > -1) {
 			sqlBuf.deleteCharAt(pos);
 		}
 		if (feat.getGeometry() != null) {
 			if (pos > -1) {
 				sqlBuf.append(",");
 			}
 			sqlBuf.append(" \"" + dbLayerDef.getFieldGeometry() + "\"");
 			sqlBuf.append(" = ");
 			// MCoord
 			int type = feat.getGeometry().getGeometryType();
 			if ((type == FShape.M) && (type != FShape.MULTIPOINT)) {
 				sqlBuf.append(" geomfromewkt( 'SRID="
 						+ DefaultJDBCDriver.removePrefix(dbLayerDef
 								.getSRID_EPSG())
 						+ ";"
 						+ ((FShapeM) feat.getGeometry().getInternalShape())
 								.toText() + "')");
 			} else {
 				// ZCoord
 				if (type == FShape.Z) {
 					if (type == FShape.MULTIPOINT) {
 						// TODO: Metodo toText 3D o 2DM
 					} else {
 						// Its not a multipoint
 						sqlBuf.append(" geomfromewkt( 'SRID="
 								+ DefaultJDBCDriver.removePrefix(dbLayerDef
 										.getSRID_EPSG())
 								+ ";"
 								+ ((FShape3D) feat.getGeometry()
 										.getInternalShape()).toText() + "')");
 					}
 
 				}
 
 				// XYCoord
 				else {
 					sqlBuf.append(" geomfromewkt( 'SRID="
 							+ DefaultJDBCDriver.removePrefix(dbLayerDef
 									.getSRID_EPSG()) + ";"
 							+ feat.getGeometry().toJTSGeometry().toText()
 							+ "')");
 				}
 			}
 		}
 		sqlBuf.append(" WHERE ");
 		sqlBuf.append(dbLayerDef.getFieldID() + " = " + feat.getID() + ";");
 		sql = sqlBuf.toString();
 		return sql;
 
 	}
 
 	/**
 	 * @param dbLayerDef
 	 * @param feat
 	 * @return
 	 */
 	public String getSqlDeleteFeature(DBLayerDefinition dbLayerDef,
			IFeature feat) {
 		// DELETE FROM weather WHERE city = 'Hayward';
 		// TODO: NECESITAMOS OTRO MTODO PARA BORRAR CORRECTAMENTE.
 		// Esto provocar errores, ya que getID que viene en un row no
 		// nos sirve dentro de un writer para modificar y/o borrar entidades
 		// Por ahora, cojo el ID del campo que me indica el dbLayerDev
 		StringBuffer sqlBuf = new StringBuffer("DELETE FROM "
 				+ dbLayerDef.getTableName() + " WHERE ");
 		String sql = null;
 		int indexFieldId = dbLayerDef.getIdFieldID();
 		sqlBuf.append("\"" + dbLayerDef.getFieldID() + "\"" + " = "
				+ feat.getAttribute(indexFieldId).toString() + ";");
 		sql = sqlBuf.toString();
 
 		return sql;
 	}
 
 	public String getEncoding() {
 		return toEncode;
 	}
 
 	public void setEncoding(String toEncode) {
 		if (toEncode.compareToIgnoreCase("SQL_ASCII") == 0) {
 			this.toEncode = "ASCII";
 		} else if (toEncode.compareToIgnoreCase("WIN1252") == 0) {
 			this.toEncode = "Latin1";
 		} else if (toEncode.compareToIgnoreCase("UTF8") == 0) {
 			this.toEncode = "UTF-8";
 		} else {
 			this.toEncode = toEncode;
 		}
 	}
 
 	static String escapeFieldName(String name) {
 		if (!name.toLowerCase().equals(name)) {
 			return "\"" + name.trim() + "\"";
 		}
 		if (!name.matches("[a-z][\\d\\S\\w]*")) {
 			return "\"" + name.trim() + "\"";
 		}
 		if (name.indexOf(":") > 0) {
 			return "\"" + name.trim() + "\"";
 		}
 		// si es una palabra reservada lo escapamos
 		if (SQLiteReservedWords.isReserved(name)) {
 			return "\"" + name.trim() + "\"";
 		}
 		return name;
 	}
 }
