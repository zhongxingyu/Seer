 package data.dbutil;
 
 import java.sql.SQLException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author 8i
  */
 public abstract class SqlFunctions{
     /**
      * Executa un insert pe o tabela in baza de date
      *
      * @param tableName este numele tabelei pe care se face insert
      * @param data este lista de {numeColoana,denumire coloana}
      * @see DbObject#DbObject(java.lang.String, java.lang.String)
      * @return true daca s-a efectuat operatia in baza de date si fals in caz
      * contrar
      * @throws SQLException
      */
    public static boolean insert(String tableName, List<DbObject> data){
         try {
             String f = "";
             String v = "";
             String ff = "";
             String vv = "";
             for (DbObject dbo : data) {
                 if (!ff.equals("")) {
                     ff += ",";
                 }
                 ff += dbo.getField();
                 if (!vv.equals("")) {
                     vv += ",";
                 }
                 vv += "'" + dbo.getValue() + "'";
             }
             f += ff;
             v += vv;
            String s = "insert into x " + tableName + "(" + f + ") values(" + v + ")";
             DbUtil dbu = new DbUtil();
            System.out.println(s);
             if (dbu.makeUpdate(s) > 0) {
                 return true;
             }
             
         } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException ex) {
            
        }catch(SQLException ex){
            System.out.println(ex.getMessage());
         }
         return false;
     }
 
 
     /**
      * Executa un update in baza de date
      *
      * @param tableName este numele tabelei pe care se face update
      * @param data o lista de {numeColoana,denumire coloana}
      * @param where este conditia dupa care se face update
      * @see DbObject#DbObject(java.lang.String, java.lang.String)
      * @return true daca s-a efectuat operatia in baza de date si fals in caz
      * contrar
      * @throws SQLException
      */
    public static boolean update(String tableName, List<DbObject> data, String where) {
        try {
             String f = "";
             String ff = "";
             for (DbObject dbo : data) {
                 if (!ff.equals("")) {
                     ff += ", ";
                 }
             }
             String s = "update " + tableName + f + "where" + where;
             DbUtil dbu = new DbUtil();
             //if(dbu.makeUpdate(s) > 0)
             //  return true;
            
        } catch (SQLException ex) {
            if(ex instanceof com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException)
                ;
        }
         return false;
     }
 
     /**
      * Executa un delete din baza de date
      *
      * @param tableName este numele tabelei din care se doreste stergerea
      * @param where este conditia dupa care se face stergerea
      * @return true daca s-a efectuat operatia in baza de date si fals in caz
      * contrar
      */
     public static boolean delete(String tableName, String where)
                                 throws SQLException{
         DbUtil dbu = new DbUtil();
         if(dbu.makeUpdate("delete from "+tableName+" where "+where) > 0)
             return true;
         return false;
     }
 }  
