 package db;
 
 import db.registros.Usuario;
 import generic.GenericFrame;
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 /**
  *
  * @author fermani
  */
 public class Conexion {
 
     private static final Logger loggerSQL = Logger.getLogger(Conexion.class.getName());
     private GenericFrame padre;
     private final Connection conexion;
     private final Statement st;
     private Usuario usuario;
     
     private final String SEPARADOR = "\n-------------------------------------------";
 
     public Conexion(GenericFrame frame) throws Exception {
         this.padre = frame;
         File file = new File(getClass().getResource("/log/").getPath() + "logSQL.log");
         if (!file.exists()) {
             file.createNewFile();
         }
         FileHandler fh = new FileHandler(getClass().getResource("/log/logSQL.log").getFile(), true);
         loggerSQL.addHandler(fh);
         loggerSQL.setLevel(Level.ALL);
         //Para mostrar por consola el logger
         loggerSQL.setUseParentHandlers(true);
         SimpleFormatter formatter = new SimpleFormatter();
         fh.setFormatter(formatter);
         
         usuario = null;
         try {
             Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection("jdbc:mysql://localhost/JBox", "jbox", "jbox");
             st = conexion.createStatement();
         } catch (SQLException ex) {
             frame.logger(Level.SEVERE, ex.getSQLState());
             throw ex;
         } catch (ClassNotFoundException ex) {
             frame.logger(Level.SEVERE, ex.getCause().getMessage());
             throw ex;
         }
     }
 
     public ResultSet execSQLSelect(String sql) throws SQLException {
         ResultSet rs = st.executeQuery(sql);
         rs.last();
         String log = sql + "\nRtdo: " + rs.getRow() + SEPARADOR;
         loggerSQL.log(Level.INFO, log);
         rs.beforeFirst();
         return rs;
     }
 
     public int execUpdate(String sql) throws SQLException {
         int rtdo = st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
         String log = sql + "\nRtdo: " + rtdo + SEPARADOR;
         loggerSQL.log(Level.INFO, log);
         return rtdo;
     }
 
     public int getSerial() throws SQLException {
         ResultSet rs = st.getGeneratedKeys();
         int id = 0;
         while (rs.next()) {
             id = rs.getInt(1);
         }
         return id;
     }
 
     public Usuario getUsuario() {
         return usuario;
     }
 
     public void setUsuario(Usuario usuario) {
         this.usuario = usuario;
     }
 
     public void initTransaction() throws SQLException {
         conexion.setAutoCommit(true);
     }
 
     public void rollback() throws SQLException {
         conexion.rollback();
     }
 
     public void endTransaction() throws SQLException {
         conexion.setAutoCommit(false);
     }
     
     public void logger(Level level, String log) {
         loggerSQL.log(level, log);
     }
 }
