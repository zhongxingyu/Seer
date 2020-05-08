 package sirp.Entidades;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import sirp.*;
 
 public class ListaAlumnos {
     int id_cur;
     List<Alumno> lista;
     
     public ListaAlumnos(int id_cur){
         this.id_cur = id_cur;
         lista = new ArrayList();
         List n = new ArrayList();
         ResultSet rs = SIRP.con.listaResultados("SELECT alumno.id_alu FROM registro.inscripcion INNER JOIN registro.alumno ON inscripcion.id_alu = alumno.id_alu AND id_cur = '"+id_cur+"';");
         try {
             while(rs.next()){ 
                 n.add(rs.getInt("id_alu"));
             }
         } catch (SQLException ex) {
             Logger.getLogger(ListaAlumnos.class.getName()).log(Level.SEVERE, null, ex);
         }
         for(int i = 0;i<n.size();i++){
             Alumno a = new Alumno((int)n.get(i));
             lista.add(a);
         }
     }
 
    public List getAl(int i){
         return lista;
     }
     
     
     @Override public String toString(){
         String s = "";
         for(int i = 0; i<lista.size();i++){
             s+= lista.get(i)+"\n";
         }
         return s;
     }
 }
