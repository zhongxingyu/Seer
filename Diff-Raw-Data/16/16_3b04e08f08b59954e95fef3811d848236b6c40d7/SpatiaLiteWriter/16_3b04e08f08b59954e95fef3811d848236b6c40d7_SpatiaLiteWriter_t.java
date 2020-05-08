 package es.udc.cartolab.cit.gvsig.fmap.drivers.jdbc.spatialite;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.apache.log4j.Logger;
 
 import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
 import com.hardcode.gdbms.driver.exceptions.WriteDriverException;
 import com.hardcode.gdbms.engine.data.driver.DriverException;
 import com.iver.cit.gvsig.exceptions.visitors.ProcessVisitorException;
 import com.iver.cit.gvsig.exceptions.visitors.ProcessWriterVisitorException;
 import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
 import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
 import com.iver.cit.gvsig.fmap.core.FShape;
 import com.iver.cit.gvsig.fmap.core.IFeature;
 import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
 import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
 import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
 import com.iver.cit.gvsig.fmap.drivers.IConnection;
 import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
 import com.iver.cit.gvsig.fmap.edition.IFieldManager;
 import com.iver.cit.gvsig.fmap.edition.IRowEdited;
 import com.iver.cit.gvsig.fmap.edition.ISpatialWriter;
 import com.iver.cit.gvsig.fmap.edition.fieldmanagers.JdbcFieldManager;
 import com.iver.cit.gvsig.fmap.edition.writers.AbstractWriter;
 
 public class SpatiaLiteWriter extends AbstractWriter implements ISpatialWriter,
 IFieldManager {
 
 	private DBLayerDefinition lyrDef;
 	private IConnection conex;
 	private Statement st;
 	private boolean bCreateTable;
 	private boolean bWriteAll;
 	private SpatiaLite spatiaLite = new SpatiaLite();
 	private JdbcFieldManager fieldManager;
 
 	/**
 	 * Useful to create a layer from scratch 
 	 * 
 	 * @param lyrDef
 	 * @throws InitializeWriterException
 	 * @throws IOException
 	 * @throws DriverException
 	 */
 	@Override
 	public void initialize(ITableDefinition lyrD)
 	throws InitializeWriterException {
 		super.initialize(lyrD);
 		this.lyrDef = (DBLayerDefinition) lyrD;
 		conex = lyrDef.getConnection();
 		try {
 			st = ((ConnectionJDBC) conex).getConnection().createStatement();
 			if (bCreateTable) {
 				dropTableIfExist();
 				String sqlCreate = spatiaLite.getSqlCreateSpatialTable(lyrDef,
 						true);
 				System.out.println("sqlCreate =" + sqlCreate);
 				st.execute(sqlCreate);
 				String sqlAlter = spatiaLite.getSqlAddGeometryColumn(lyrDef);
 				System.out.println("sqlAlter =" + sqlAlter);
 				st.execute(sqlAlter);
 				((ConnectionJDBC) conex).getConnection().commit();
 			}
 			((ConnectionJDBC) conex).getConnection().setAutoCommit(false);
 			String table_name = lyrDef.getTableName();
 			fieldManager = new JdbcFieldManager(((ConnectionJDBC)conex).getConnection(), table_name);
 		} catch (SQLException e) {
 			throw new InitializeWriterException(getName(), e);
 		}
 
 	}
 
 	@Override
 	public void preProcess() throws StartWriterVisitorException {
 		ResultSet rsAux;
 		try {
 			((ConnectionJDBC) conex).getConnection().rollback();
 		} catch (SQLException e) {
 			// There probably was no transaction in course...
 		}
 		try {
 			alterTable();
 			rsAux = st.executeQuery("PRAGMA encoding;");
 			rsAux.next();
 			String serverEncoding = rsAux.getString(1);
 			System.out.println("Server encoding = " + serverEncoding);
 			spatiaLite.setEncoding(serverEncoding);
 		} catch (SQLException e) {
 			throw new StartWriterVisitorException(getName(), e);
 		} catch (WriteDriverException e) {
 			throw new StartWriterVisitorException(getName(), e);
 		}
 
 	}
 
 	@Override
 	public void process(IRowEdited row) throws ProcessWriterVisitorException {
 		String sqlInsert;
 		try {
 			switch (row.getStatus()) {
 			case IRowEdited.STATUS_ADDED:
 				IFeature feat = (IFeature) row.getLinkedRow();
 				sqlInsert = spatiaLite.getSqlInsertFeature(lyrDef, feat);
 				System.out.println("sql = " + sqlInsert);
 				st.execute(sqlInsert);
 
 				break;
 			case IRowEdited.STATUS_MODIFIED:
 				IFeature featM = (IFeature) row.getLinkedRow();
 				if (bWriteAll) {
 					sqlInsert = spatiaLite.getSqlInsertFeature(lyrDef, featM);
 					System.out.println("sql = " + sqlInsert);
 					st.execute(sqlInsert);
 				} else {
 					String sqlModify = spatiaLite.getSqlModifyFeature(lyrDef,
 							featM);
 					System.out.println("sql = " + sqlModify);
 					st.execute(sqlModify);
 				}
 				break;
 			case IRowEdited.STATUS_ORIGINAL:
 				IFeature featO = (IFeature) row.getLinkedRow();
 				if (bWriteAll) {
 					sqlInsert = spatiaLite.getSqlInsertFeature(lyrDef, featO);
 					st.execute(sqlInsert);
 				}
 				break;
 			case IRowEdited.STATUS_DELETED:
				String sqlDelete = spatiaLite.getSqlDeleteFeature(lyrDef,
						row.getLinkedRow());
 				System.out.println("sql = " + sqlDelete);
 				st.execute(sqlDelete);
 				break;
 			}
 		} catch (ProcessVisitorException e) {
 			Logger.getLogger(this.getClass()).error(getName(), e);
 		} catch (SQLException e) {
 			throw new ProcessWriterVisitorException(this.getName(), e);
 		}
 
 	}
 
 	@Override
 	public void postProcess() throws StopWriterVisitorException {
 		try {
 			((ConnectionJDBC) conex).getConnection().setAutoCommit(true);
 		} catch (SQLException e) {
 			throw new StopWriterVisitorException(getName(), e);
 		}
 	}
 
 	@Override
 	public String getName() {
 		return "SpatiaLite Writer";
 	}
 
 	@Override
 	public boolean canWriteGeometry(int gvSIGgeometryType) {
 		switch (gvSIGgeometryType) {
 		case FShape.POINT:
 		case FShape.LINE:
 		case FShape.POLYGON:
 		case FShape.MULTIPOINT:
 		case FShape.MULTI:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	@Override
 	public boolean canWriteAttribute(int sqlType) {
 		return true;
 	}
 
 	/**
 	 * @return Returns the bCreateTable.
 	 */
 	public boolean isCreateTable() {
 		return bCreateTable;
 	}
 
 	/**
 	 * @param createTable
 	 *            The bCreateTable to set.
 	 */
 	public void setCreateTable(boolean createTable) {
 		bCreateTable = createTable;
 	}
 
 	@Override
 	public FieldDescription[] getOriginalFields() {
 		return lyrDef.getFieldsDesc();
 	}
 
 	@Override
 	public void addField(FieldDescription fieldDesc) {
 		fieldManager.addField(fieldDesc);
 
 	}
 
 	@Override
 	public FieldDescription removeField(String fieldName) {
 		return fieldManager.removeField(fieldName);
 
 	}
 
 	@Override
 	public void renameField(String antName, String newName) {
 		fieldManager.renameField(antName, newName);
 	}
 
 	@Override
 	public boolean alterTable() throws WriteDriverException {
 		return fieldManager.alterTable();
 	}
 
 	@Override
 	public FieldDescription[] getFields() {
 		return fieldManager.getFields();
 	}
 
 	@Override
 	public boolean canAlterTable() {
 		return canSaveEdits();
 	}
 
 	@Override
 	public boolean canSaveEdits() {
 		try {
 			String connUrl = lyrDef.getConnection().getURL();
 			File sqliteFile = new File(connUrl.replace(spatiaLite.getConnectionStringBeginning(), ""));
 			return sqliteFile.canWrite();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	private boolean dropTableIfExist() throws SQLException {
 		if (!this.existTable(lyrDef.getTableName())) {
 			return false;
 		}
 		st = ((ConnectionJDBC) conex).getConnection().createStatement();
 		st.execute("DROP TABLE " + lyrDef.getTableName() + ";");
 		st.close();
 		return true;
 	}
 
 	private boolean existTable(String tableName)
 	throws SQLException {
 		boolean exists = false;
 		
 		String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?;";
 
 		PreparedStatement st = ((ConnectionJDBC) conex).getConnection().prepareStatement(sql);
 		st.setString(1, tableName);
 		ResultSet rs = st.executeQuery();
 		if (rs.next()) {
 			exists = true;
 		}
 		rs.close();
 		st.close();
 
 		return exists;
 	}
 }
