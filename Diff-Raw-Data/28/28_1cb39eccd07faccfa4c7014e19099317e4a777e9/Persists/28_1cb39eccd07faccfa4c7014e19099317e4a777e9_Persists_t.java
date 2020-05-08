 /* (c) Copyright by Man YUAN */
 package net.epsilony.mf.util.persistence;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map;
 import net.epsilony.mf.process.IntegrateResult;
 import net.epsilony.mf.process.MFLinearMechanicalProcessor;
 import net.epsilony.mf.process.solver.MFSolver;
 import net.epsilony.mf.project.MFMechanicalProject;
 import net.epsilony.mf.project.SimpMFMechanicalProject;
 import net.epsilony.mf.util.MFConstants;
 import no.uib.cipr.matrix.DenseVector;
 import no.uib.cipr.matrix.Matrix;
 import no.uib.cipr.matrix.MatrixEntry;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class Persists {
 
     public static int getMaxDbId(Statement stat, String tableName) throws SQLException {
         ResultSet result = stat.executeQuery(String.format(MFConstants.SQL_GET_MAX_DB_ID, tableName));
         if (!result.isBeforeFirst()) {
             return 0;
         } else {
             return result.getInt(1);
         }
     }
 
     public static void main(String[] args) throws ClassNotFoundException, SQLException {
         Class.forName("org.sqlite.JDBC");
         MFMechanicalProject project = SimpMFMechanicalProject.genTimoshenkoProjectFactory().produce();
         MFLinearMechanicalProcessor processor = new MFLinearMechanicalProcessor();
         processor.setProject(project);
         processor.preprocess();
         IntegrateResult integrateResult = processor.getIntegrateResult();
 
         Connection connection = DriverManager.getConnection("jdbc:sqlite:/home/epsilon/Desktop/temp_test.db");
 
         Map<String, Object[]> stored = new HashMap<>();
 
         MatrixPersist mp = new MatrixPersist();
         mp.setConnection(connection);
         mp.createTables();
         int tid = mp.store(MFMatries.wrap(integrateResult.getMainMatrix()));
         stored.put("main matrix", new Object[]{tid, integrateResult.getMainMatrix()});
 
         tid = mp.store(MFMatries.wrap(integrateResult.getMainVector()));
         stored.put("main vector", new Object[]{tid, integrateResult.getMainVector()});
         processor.solve();
 
         MFSolver mfSolver = project.getMFSolver();
         DenseVector result = mfSolver.getResult();
 
         tid = mp.store(MFMatries.wrap(result));
         stored.put("result", new Object[]{tid, result});
 
         MatrixInfo matrixInfo = mp.retrieveInfo((int) stored.get("main matrix")[0]);
         MFMatrix retrieved = mp.retrieve(MFMatries.allocateMTJ(matrixInfo), (int) stored.get("main matrix")[0]);
         Matrix storedMat = (Matrix) stored.get("main matrix")[1];
         for (MatrixEntry me : retrieved) {
             if (storedMat.get(me.row(), me.column()) != me.get()) {
                 System.out.println("bad me = " + me);
             }
         }
     }

    public static void commit(Connection connection) throws SQLException {
        if (!connection.getAutoCommit()) {
            connection.commit();
        }
    }
 }
