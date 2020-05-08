 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edacc.model;
 
 import edacc.manageDB.Util;
 import edacc.manageDB.NoSolverBinarySpecifiedException;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author dgall
  */
 public class SolverBinariesDAO {
 
     private static final String TABLE = "SolverBinaries";
     private static final String INSERT_QUERY = "INSERT INTO " + TABLE + " (idSolver, binaryName, binaryArchive, md5, version, runCommand, runPath) VALUES (?, ?, ?, ?, ?, ?, ?)";
     private static final String UPDATE_QUERY = "UPDATE " + TABLE + " SET binaryName = ?, version = ?, runCommand = ?, runPath = ? WHERE idSolverBinary = ?";
     private static final String DELETE_QUERY = "DELETE FROM " + TABLE + " WHERE idSolverBinary=?";
     private static ObjectCache<SolverBinaries> cache = new ObjectCache<SolverBinaries>();
 
     public static void removeBinariesOfSolver(Solver solver) throws SQLException, NoSolverBinarySpecifiedException, FileNotFoundException, IOException {
         for (SolverBinaries b : solver.getSolverBinaries()) {
             b.setDeleted();
             try {
                 save(b);
             } catch (NoSuchAlgorithmException ex) {
                 throw new IOException("Error while calculating md5 sum: " + ex.getMessage());
             }
         }
 
     }
 
    public static void clearCache() {
        cache.clear();
    }

     private SolverBinariesDAO() {
         
     }
 
     public static void save(SolverBinaries s) throws SQLException, NoSolverBinarySpecifiedException, FileNotFoundException, IOException, NoSuchAlgorithmException {
         if (s.isSaved()) {
             return;
         }
 
         PreparedStatement ps = null;
 
         if (s.isNew()) {
             ps = DatabaseConnector.getInstance().getConn().prepareStatement(INSERT_QUERY, PreparedStatement.RETURN_GENERATED_KEYS);
             ps.setInt(1, s.getIdSolver());
             ps.setString(2, s.getBinaryName());
             if (s.getBinaryFiles() != null && s.getBinaryFiles().length > 0) {
                 ByteArrayOutputStream zipped = Util.zipFileArrayToByteStream(s.getBinaryFiles(), new File(s.getRootDir()));
                 s.setMd5(Util.calculateMD5(new ByteArrayInputStream(zipped.toByteArray())));
                 ps.setBinaryStream(3, new ByteArrayInputStream(zipped.toByteArray()));
             } else {
                 throw new NoSolverBinarySpecifiedException();
             }
             ps.setString(4, s.getMd5());
             ps.setString(5, s.getVersion());
             ps.setString(6, s.getRunCommand());
             ps.setString(7, s.getRunPath());
         } else if (s.isModified()) {
             ps = DatabaseConnector.getInstance().getConn().prepareStatement(UPDATE_QUERY);
             ps.setString(1, s.getBinaryName());
             ps.setString(2, s.getVersion());
             ps.setString(3, s.getRunCommand());
             ps.setString(4, s.getRunPath());
             ps.setInt(5, s.getIdSolverBinary());
         } else if (s.isDeleted()) {
             ps = DatabaseConnector.getInstance().getConn().prepareStatement(DELETE_QUERY);
             ps.setInt(1, s.getIdSolverBinary());
         }
         ps.executeUpdate();
         if (s.isNew()) {
             // set id
             ResultSet rs = ps.getGeneratedKeys();
             if (rs.next()) {
                 s.setIdSolverBinary(rs.getInt(1));
             }
         }
         if (s.isDeleted()) {
             cache.remove(s);
             // remove SolverBinary from Vector in corresponding solver object
             SolverDAO.getById(s.getIdSolver()).removeSolverBinary(s);
             s.setNew();
         } else {
             cache.cache(s);
             s.setSaved();
         }
     }
 
     private static SolverBinaries getSolverBinaryFromResultSet(ResultSet rs) throws SQLException {
         SolverBinaries b = new SolverBinaries(rs.getInt("idSolver"));
         b.setIdSolverBinary(rs.getInt("idSolverBinary"));
         b.setBinaryName(rs.getString("binaryName"));
         b.setMd5(rs.getString("md5"));
         b.setVersion(rs.getString("version"));
         b.setRunCommand(rs.getString("runCommand"));
         b.setRunPath(rs.getString("runPath"));
 
         return b;
     }
 
     public static Vector<SolverBinaries> getBinariesOfSolver(Solver solver) throws SQLException {
         final String query = "SELECT idSolverBinary, idSolver, binaryName, md5, version, runCommand, runPath FROM " + TABLE + " WHERE idSolver=?";
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement(query);
         ps.setInt(1, solver.getId());
         ResultSet rs = ps.executeQuery();
         Vector<SolverBinaries> res = new Vector<SolverBinaries>();
         while (rs.next()) {
             SolverBinaries c = cache.getCached(rs.getInt("idSolverBinary"));
             if (c != null)
                 res.add(c);
             else {
                 SolverBinaries b = getSolverBinaryFromResultSet(rs);
                 cache.cache(b);
                 res.add(b);
                 b.setSaved();
             }
         }
         rs.close();
         return res;
     }
 
     public static Vector<SolverBinaries> getAll() throws SQLException {
         final String query = "SELECT idSolverBinary, idSolver, binaryName, md5, version, runCommand, runPath FROM " + TABLE;
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement(query);
         ResultSet rs = ps.executeQuery();
         Vector<SolverBinaries> res = new Vector<SolverBinaries>();
         while (rs.next()) {
             SolverBinaries c = cache.getCached(rs.getInt("idSolverBinary"));
             if (c != null)
                 res.add(c);
             else {
                 SolverBinaries b = getSolverBinaryFromResultSet(rs);
                 cache.cache(b);
                 res.add(b);
                 b.setSaved();
             }
         }
         rs.close();
         return res;
     }
 
     public static SolverBinaries getById(int id) throws SQLException {
         final String query = "SELECT idSolverBinary, idSolver, binaryName, md5, version, runCommand, runPath FROM " + TABLE + " WHERE idSolverBinary=?";
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement(query);
         ps.setInt(1, id);
         ResultSet rs = ps.executeQuery();
         SolverBinaries res = null;
         if (rs.next()) {
             SolverBinaries c = cache.getCached(rs.getInt("idSolverBinary"));
             if (c != null)
                 res = c;
             else {
                 SolverBinaries b = getSolverBinaryFromResultSet(rs);
                 cache.cache(b);
                 res = b;
                 b.setSaved();
             }
         }
         rs.close();
         return res;
     }
 }
