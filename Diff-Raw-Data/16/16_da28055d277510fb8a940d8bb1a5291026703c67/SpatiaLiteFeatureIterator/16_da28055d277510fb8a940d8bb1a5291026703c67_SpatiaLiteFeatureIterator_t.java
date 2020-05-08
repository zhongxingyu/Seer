 /*
  * Created on 11-mar-2005
  *
  * gvSIG. Sistema de Informacin Geogrfica de la Generalitat Valenciana
  *
  * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  *
  * For more information, contact:
  *
  *  Generalitat Valenciana
  *   Conselleria d'Infraestructures i Transport
  *   Av. Blasco Ibez, 50
  *   46010 VALENCIA
  *   SPAIN
  *
  *      +34 963862235
  *   gvsig@gva.es
  *      www.gvsig.gva.es
  *
  *    or
  *
  *   IVER T.I. S.A
  *   Salamanca 50
  *   46005 Valencia
  *   Spain
  *
  *   +34 963163400
  *   dac@iver.es
  */
 package es.udc.cartolab.cit.gvsig.fmap.drivers.jdbc.spatialite;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.hardcode.gdbms.engine.values.DoubleValue;
 import com.hardcode.gdbms.engine.values.FloatValue;
 import com.hardcode.gdbms.engine.values.Value;
 import com.iver.cit.gvsig.fmap.core.DefaultFeature;
 import com.iver.cit.gvsig.fmap.core.IFeature;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
 import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
 import com.iver.cit.gvsig.fmap.drivers.WKBParser3;
 
 public class SpatiaLiteFeatureIterator implements IFeatureIterator {
 
 	private static int FETCH_SIZE = 5000;
 	private WKBParser3 parser = new WKBParser3();
 	private ResultSetMetaData metaData = null;
 	ResultSet rs;
 	Statement st;
 	String sql;
 	IGeometry geom;
 	int numColumns;
 	Value[] columnValues;
 
 	/**
 	 * Array con la correspondencia entre un campo de la consulta y el campo
 	 * dentro de regAtt
 	 */
 	int[] columnIndexes;
 	int numReg = 0;
 	int idFieldID = -1;
 
 	public SpatiaLiteFeatureIterator(Connection conn, String sql)
 			throws SQLException {
 
 		st = conn.createStatement();
 
 		try{
 			st.execute("BEGIN");  
 		} catch(SQLException e) {
 			st.execute("END");
 			st.execute("BEGIN");
 		}
 
 		System.out.println(sql + " LIMIT " + FETCH_SIZE + ";");
 		rs = st.executeQuery(sql + " LIMIT " + FETCH_SIZE + ";");
 		this.sql = sql;
 		numColumns = rs.getMetaData().getColumnCount();
 		metaData = rs.getMetaData();
 		numReg = 0;
 
 	}
 
 	@Override
 	public boolean hasNext() throws ReadDriverException {
 		try {
 			if (numReg > 0)
 				if ((numReg % FETCH_SIZE) == 0) {
 					rs = st.executeQuery(sql + " LIMIT " + FETCH_SIZE + " OFFSET " + numReg + ";");
 				}
 			if (rs.next())
 				return true;
 			else {
 				closeIterator();
 				return false;
 			}
 		} catch (SQLException e) {
             throw new ReadDriverException("SpatiaLite Driver",e);
 		}
 
 	}
 
 	@Override
 	public IFeature next() throws ReadDriverException {
 		byte[] data;
 		try {
 			data = rs.getBytes(1);
 			geom = parser.parse(data);
 			for (int fieldId = 2; fieldId <= numColumns; fieldId++) {
 				Value val = SpatiaLiteDriver.getFieldValue(rs, fieldId);
 				columnValues[columnIndexes[fieldId - 2]] = val;
 			}
 
 			IFeature feat = null;
 			if (idFieldID != -1) {
 				String theID;
 				Value auxVal = columnValues[idFieldID];
 				// There is a minor problem with the id value, because it's read
 				// as a double (e.g. 1.0) but it was stored inside the hashmap
 				// as an integer, so here we have to transform it into an int
				if ((auxVal instanceof DoubleValue)
						&& (auxVal.toString().length() > 0)) {
 					theID = new Integer(((DoubleValue) auxVal).intValue())
 							.toString();
				} else if ((auxVal instanceof FloatValue)
						&& (auxVal.toString().length() > 0)) {
 					theID = new Integer(((FloatValue) auxVal).intValue())
 							.toString();
 				} else {
 					theID = auxVal.toString();
 				}
 				feat = new DefaultFeature(geom, columnValues.clone(), theID);
 			} else {
 				throw new ReadDriverException("SpatiaLite Driver",null);
 			}
 			numReg++;
 			return feat;
 		} catch (SQLException e) {
             throw new ReadDriverException("SpatiaLite Driver",e);
 		}
 
 	}
 
 	@Override
 	public void closeIterator() throws ReadDriverException {
 		try {
 			st.execute("END");
 			st.close();
 		} catch (SQLException e) {
 		}
 		try {
 			numReg = 0;
 			rs.close();
 		} catch (SQLException e) {
 	        throw new ReadDriverException("SpatiaLite Driver",e);
 		}
 	}
 
 	public void setLyrDef(DBLayerDefinition lyrDef) {
 		columnValues = new Value[lyrDef.getFieldNames().length];
 		columnIndexes = new int[numColumns - 1];
 
 		try {
 			for (int i = 2; i <= metaData.getColumnCount(); i++) {
 				int idRel = lyrDef.getFieldIdByName(metaData.getColumnName(i));
 				if (idRel == -1)
 				{
 					throw new RuntimeException("No se ha encontrado el nombre de campo " + metaData.getColumnName(i));
 				}
 				columnIndexes[i - 2] = idRel;
 			}
 			idFieldID = lyrDef.getIdFieldID();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 }
