 package servicios;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import objetos.Film;
 
 public class ServicioJDBCFilms extends ServicioJDBC {
 
     public ServicioJDBCFilms() {
         super();
     }
     
     public boolean agregarNuevoFilm(int idFilm,String nombreFilm,String directorFilm){
         boolean result = false;
         try {
             String sql = String.format("INSERT INTO APP.FILMS VALUES(%d,'%s','%s')", 
                                        idFilm,nombreFilm,directorFilm);
             Statement pstAgregaFilm =  super.getConn().createStatement();
            result = pstAgregaFilm.execute(sql);          
             pstAgregaFilm.close();
             return result; 
         } catch (SQLException ex) {
             Logger.getLogger(ServicioJDBCFilms.class.getName()).log(Level.SEVERE, null, ex);
             return result;
         }
     }
     
     public Film getFilmByID(int idFilm){
         Film oFilm=new Film();
         try {
             String sqlSeleccionaFilmPorID = 
                    String.format("SELECT IDFILM,NOMBREFILM,DIRECTORFILM FROM APP.FILMS "
                                + " WHERE IDFILM = %d ", 
                                        idFilm);
             Statement stSeleccionaFilmPorID = super.getConn().createStatement();
             ResultSet rsFilm = stSeleccionaFilmPorID.executeQuery(sqlSeleccionaFilmPorID);
             if(rsFilm.next()){   
                 oFilm.setId(rsFilm.getLong("IDFILM"));
                 oFilm.setName(rsFilm.getString("NOMBREFILM"));
                 oFilm.setAutor(rsFilm.getString("DIRECTORFILM"));
             }
             rsFilm.close();
             stSeleccionaFilmPorID.close();
         } catch (SQLException ex) {
             Logger.getLogger(ServicioJDBCFilms.class.getName()).log(Level.SEVERE, null, ex);
         }
         return oFilm;
     }
     
 }
