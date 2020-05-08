 package dbmigrate.app;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import dbmigrate.executor.AddColumnExecutor;
 import dbmigrate.executor.ChangeColumnExecutor;
 import dbmigrate.executor.CreateTableExecutor;
 import dbmigrate.executor.DropColumnExecutor;
 import dbmigrate.executor.DropTableExecutor;
 import dbmigrate.executor.ExecutorEngine;
 import dbmigrate.executor.MergeColumnExecutor;
 import dbmigrate.executor.ModifyColumnExecutor;
 import dbmigrate.executor.RenameColumnExecutor;
 import dbmigrate.executor.SplitColumnExecutor;
 import dbmigrate.gui.ApplicationFrame;
import dbmigrate.logging.HistoryElement;
 import dbmigrate.logging.HistoryStorage;
 import dbmigrate.logging.LoggerFactory;
 import dbmigrate.model.db.DbConnector;
 import dbmigrate.model.operation.AddColumnOperationDescriptor;
 import dbmigrate.model.operation.ChangeColumnOperationDescriptor;
 import dbmigrate.model.operation.CreateTableOperationDescriptor;
 import dbmigrate.model.operation.DropColumnOperationDescriptor;
 import dbmigrate.model.operation.DropTableOperationDescriptor;
 import dbmigrate.model.operation.MergeColumnOperationDescriptor;
 import dbmigrate.model.operation.MigrationConfiguration;
 import dbmigrate.model.operation.ModifyColumnOperationDescriptor;
 import dbmigrate.model.operation.RenameColumnOperationDescriptor;
 import dbmigrate.model.operation.SplitColumnOperationDescriptor;
 import dbmigrate.parser.Loader;
 
 public class Application {
 	private final static int ARGS_LENGTH = 5;
 	private static ApplicationFrame app;
 
 	public static void configureExecutorEngine(ExecutorEngine executorEngine) {
 		executorEngine.registerExecutor(AddColumnOperationDescriptor.class,
 				AddColumnExecutor.class);
 		executorEngine.registerExecutor(DropTableOperationDescriptor.class,
 				DropTableExecutor.class);
 		executorEngine.registerExecutor(DropColumnOperationDescriptor.class,
 				DropColumnExecutor.class);
 		executorEngine.registerExecutor(CreateTableOperationDescriptor.class,
 				CreateTableExecutor.class);
 		executorEngine.registerExecutor(RenameColumnOperationDescriptor.class,
 				RenameColumnExecutor.class);
 		executorEngine.registerExecutor(ModifyColumnOperationDescriptor.class,
 				ModifyColumnExecutor.class);
 		executorEngine.registerExecutor(ChangeColumnOperationDescriptor.class,
 				ChangeColumnExecutor.class);
 		executorEngine.registerExecutor(SplitColumnOperationDescriptor.class,
 				SplitColumnExecutor.class);
 		executorEngine.registerExecutor(MergeColumnOperationDescriptor.class,
 				MergeColumnExecutor.class);
 	}
 
 	public static void main(String[] args) {
 		if (args.length < Application.ARGS_LENGTH) {
 			java.awt.EventQueue.invokeLater(new Runnable() {
 
 				public void run() {
 					app = new ApplicationFrame();
 					app.setVisible(true);
 
 				}
 			});
 			return;
 		}
 		boolean performValidation = false;
 		if (args.length > Application.ARGS_LENGTH) {
 			for (String opt : args) {
 				if ("--validate".equals(opt)) {
 					performValidation = true;
 				}
 			}
 		}
 
 		try {
 			DbConnector dbConnector = new DbConnector();
 
 			Connection connection = dbConnector.getConnection(
 					DbConnector.DB_TYPE, args[1], args[2], args[3], args[4]);
 
 			app.setConnection(connection);
 			HistoryStorage historyStorage = new HistoryStorage(connection);
 			app.setHistoryStorage(historyStorage);
 			
 			historyStorage.store("1.2.3.4", "mig_01_12_2011", "2011-12-01", 1,
 					"added table blabla", true);
 

 			MigrationConfiguration migrationConfiguration = Loader.load(
 					new File("migrations/" + args[0]), performValidation);
 
 			ExecutorEngine executorEngine = new ExecutorEngine(connection,
 					migrationConfiguration, true);
 
 			Application.configureExecutorEngine(executorEngine);
 
 			executorEngine.setLogger(LoggerFactory.getLogger());
 			executorEngine.executeMigration();
 
 			try {
 				connection.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 }
