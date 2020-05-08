 package org.h2.command.dml;
 
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.sql.SQLException;
 import org.h2.constant.ErrorCode;
 import org.h2.engine.Database;
 import org.h2.engine.Session;
 import org.h2.h2o.comms.QueryProxy;
 import org.h2.h2o.comms.remote.TableManagerRemote;
 import org.h2.h2o.manager.TableManager;
 import org.h2.h2o.manager.ISystemTableReference;
 import org.h2.h2o.manager.MigrationException;
 import org.h2.h2o.manager.MovedException;
 import org.h2.h2o.util.LockType;
 import org.h2.h2o.util.TableInfo;
 import org.h2.message.Message;
 import org.h2.schema.Schema;
 
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 
 /**
  * @author Angus Macdonald (angus@cs.st-andrews.ac.uk)
  */
 public class MigrateTableManager extends org.h2.command.ddl.SchemaCommand {
 
 	private String tableName;
 
 
 	/**
 	 * @param session
 	 * @param schema
 	 */
 	public MigrateTableManager(Session session, Schema schema, String tableName) {
 		super(session, schema);
 
 
 		this.tableName = tableName;
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see org.h2.command.Prepared#isTransactional()
 	 */
 	@Override
 	public boolean isTransactional() {
 		return false;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.h2.command.Prepared#update()
 	 */
 	@Override
 	public int update() throws SQLException, RemoteException {
 		int result = -1;
 
 		try {
 			Database db = this.session.getDatabase();
 			ISystemTableReference sm = db.getSystemTableReference();
 			String schemaName = "";
 			if (getSchema() != null){
 				schemaName = getSchema().getName();
 			} else {
 				schemaName = "PUBLIC";
 			}
 			TableManagerRemote tableManager = sm.lookup(new TableInfo(tableName, schemaName), true);
 
 			if (tableManager == null){
 				Message.getSQLException(ErrorCode.TABLE_OR_VIEW_NOT_FOUND_1, getSchema().getName() + tableName);
 			}
 
 			QueryProxy qp = null;
 			try {
 				qp = tableManager.getQueryProxy(LockType.WRITE, db.getLocalDatabaseInstanceInWrapper());
 			} catch (MovedException e){
 				tableManager = sm.lookup(new TableInfo(tableName, schemaName), false);
 				qp = tableManager.getQueryProxy(LockType.WRITE, db.getLocalDatabaseInstanceInWrapper());
 			}
 
 			if (!qp.getLockGranted().equals(LockType.NONE)){
 				result = migrateTableManagerToLocalInstance(tableManager, schemaName, db);
 			} else {
 				throw Message.getSQLException(ErrorCode.LOCK_TIMEOUT_1, getSchema().getName() + tableName);
 			}
 		} catch (MovedException e) {
 			throw Message.getSQLException(ErrorCode.TABLE_OR_VIEW_NOT_FOUND_1, getSchema().getName() + tableName);
 		} catch (SQLException e) {
 			throw e;
 		}catch (Exception e) {
 			throw new SQLException("Failed to migrate table manager for " + getSchema().getName() + tableName + ".");
 		}
 
 
 		return result;
 	}
 
 	public int migrateTableManagerToLocalInstance(TableManagerRemote oldTableManager, String schemaName, Database db) throws SQLException{
 		Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Preparing to migrate Table Manager for [" + schemaName + "." + tableName);
 
 		/*
 		 * Create a new System Table instance locally.
 		 */
 		TableManagerRemote newTableManager = null;
 
 		TableInfo ti = new TableInfo(tableName, schemaName, 0l, 0, "TABLE", db.getURL());
 
 		try {
 			newTableManager = new TableManager(ti, db);
 			newTableManager.persistToCompleteStartup(ti);
 		} catch (Exception e) {
 			ErrorHandling.exceptionError(e, "Failed to create new Table Manager [" + schemaName + "." + tableName + "].");
 		}
 
 		/*
 		 * Stop the old, remote, manager from accepting any more requests.
 		 */
 		try {
 			oldTableManager.prepareForMigration(db.getURL().getURLwithRMIPort());
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} catch (MigrationException e) {
 			throw new SQLException("This Table Manager [" + schemaName + "." + tableName + "] is already being migrated to another instance.");
 		} catch (MovedException e) {
 			throw new SQLException("This Table Manager [" + schemaName + "." + tableName + "] is already being migrated to another instance.");
 		}
 
 		/*
 		 * Build the System Table's state from that of the existing manager.
 		 */
 		try {
 			newTableManager.buildTableManagerState(oldTableManager);
 		} catch (RemoteException e) {
 			throw new SQLException("Failed to migrate Table Manager [" + schemaName + "." + tableName + "] to new machine.");
 		} catch (MovedException e) {
 			throw new SQLException("This shouldn't be possible here. The Table Manager [" + schemaName + "." + tableName + "] has moved, but this instance should have had exclusive rights to it.");		
 		}
 
 		/*
 		 * Shut down the old, remote, System Table. Redirect requests to new manager.
 		 */
 		try {
 			oldTableManager.completeMigration();
 		} catch (RemoteException e) {
 			throw new SQLException("Failed to complete migration [" + schemaName + "." + tableName + "].");
 
 		} catch (MovedException e) {
 			throw new SQLException("This shouldn't be possible here. The Table Manager has moved, but this instance should have had exclusive rights to it.");
 
 		} catch (MigrationException e) {
 			throw new SQLException("Migration process timed out [" + schemaName + "." + tableName + "]. It took too long.");
 		}
 		Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Table Manager [" + schemaName + "." + tableName + "] officially migrated.");
 
 		/*
 		 * Confirm the new System Tables location by updating all local state.
 		 */
 		oldTableManager = newTableManager;
 
 
 		try {
 
 			TableManagerRemote stub = (TableManagerRemote) UnicastRemoteObject.exportObject(newTableManager, 0);
 
 			db.getSystemTableReference().getSystemTable().changeTableManagerLocation(stub, ti);
 			db.getSystemTableReference().addProxy(ti, newTableManager);
 		} catch (Exception e) {
 			ErrorHandling.exceptionError(e, "Table Manager migration failed [" + schemaName + "." + tableName + "].");
 		}
 
 		return 1;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.h2.command.Prepared#update(java.lang.String)
 	 */
 	@Override
 	public int update(String transactionName) throws SQLException,
 	RemoteException {
 		return update();
 	}
 
 
 }
