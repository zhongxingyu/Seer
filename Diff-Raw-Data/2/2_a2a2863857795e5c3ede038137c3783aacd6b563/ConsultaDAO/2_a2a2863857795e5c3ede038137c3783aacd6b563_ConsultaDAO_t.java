 
 package Dao;
 
 import Javabeans.Area;
 import Utilitarios.Helpers;
 import Utilitarios.Query;
 import Utilitarios.ConexionBd;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import javax.swing.JLabel;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 
 public class ConsultaDAO {
     
     private Query qs;
     private Area objArea;
     private Helpers hp;
     private String filter[][];
     private String campos[];
     private int witdhcolum[];
     private String _table;
     private String _error;
     private ResultSet rs = null;
     private Statement s = null;
     private PreparedStatement  pt = null;
     private ConexionBd con;
     private Connection conexion;
 
     public ConsultaDAO(){
         _error = "Dao_ConsultaDAO_";
         filter = new String[0][0];
         campos = new String[0];
         witdhcolum = new int[1];
         witdhcolum[0]=50;
     }
     public void getTableAll(JTable tblDatos , JLabel lblcant){
         try{
             DefaultTableModel datos;
             qs= new Query();
             hp = new Helpers();
             if (filter.length <= 0){
                 filter = new String[0][0];
             }
             String Table = this._table;
             datos = qs.getAll(this.campos,Table,filter);
             tblDatos.setModel(datos);
             hp.setWidthJtable(tblDatos,witdhcolum);
             int num = tblDatos.getRowCount();
             lblcant.setText(String .valueOf(num));
         }
         catch(Exception e){
             System.out.println(_error + "getTableAll: "+e);
         }
     }
     public void findAsistencia(String args[], JTable tblDatos, JLabel lblcant) {
         qs= new Query();
         hp= new Helpers();
         Statement s = null;
         con = new ConexionBd();
         try {
             //Campos de la tabla report
             String lista[];
             campos = set_camp_registro();
             //Campos para la consulta
             qs.create_report(campos);
             String[] camp = new String[3];
             camp[0] = "idtip_reg%G_TIPOREG";
             camp[1] = "fecha";
             camp[2] = "hora";
             //Filtros
             filter = new String[2][2];
             filter[0][0] = "int_idemp";
             filter[0][1] = args[0];
             filter[1][0] = "bet_fecha_"+args[1];
             filter[1][1] = args[2];
             String Consulta = qs.getQueryList(camp,"registro/fecha",filter);
             helper_asistencia(Consulta, camp);
             qs.destroid_report();
         }
         catch(Exception e){
             System.out.println(_error + "findAsistencia : "+e);
         }
     }
     private void helper_asistencia(String Consulta, String camp[]) throws SQLException{
         Object[] fila;
         String[] temp;
         String tbl;
         System.out.println(Consulta);
         con.getConexion();
         conexion = con.getConetion();
         s = conexion.createStatement();
         rs = s.executeQuery(Consulta);
         ResultSetMetaData meta = rs.getMetaData();
         int nCols = meta.getColumnCount();
         fila = new Object[nCols];
         int count=0;
             //Recorro los dias del mes
             //Obtendo un dia y hago la consulta a la bd
             //cantidad de registros si es 2(entrada - salida) si es 4 (incluye refrigerios)
             //armo el arreglo deacuerdo a la cantidad
             //registro
             
          while(rs.next()){
             count++;
             for(int i=0; i<nCols; ++i){
                 fila[i] = rs.getObject(i+1);
                 temp = camp[i].split("%");
                 if(temp.length>1){
                     String[] campo;
                     tbl = temp[1];
                     campo =  hp.getConstantData(tbl);
                     fila[i] = campo[rs.getInt(i+1)];
                 }
                 System.out.println(fila[i]);
             }
         }
     }
             
     
     
     public void register_report(String[] args) {
         pt = null;
         try {
             String query = "insert into report values(";
             for(int i=0;i<args.length;i++){
                 query = query + args[i];
                 if(args.length!=i+1){ 
                     query = query + ",";
                 }
             }
             query = query + ")";
             pt  = conexion.prepareStatement(query);
             pt.executeUpdate();
             pt.close();   
         }
         catch(Exception e){
             System.out.println(_error + "register_findAsistencia : "+e);
         }
     }
     
     public String[] set_camp_registro(){
         String campos[] = new String[6];
         campos[0] = "fecha";
         campos[1] = "ingreso";
         campos[2] = "refrigerio_ing";
         campos[3] = "refrigerio_sal";
         campos[4] = "salida";
         campos[5] = "horas";
         return campos;
     }
     
    /**/
 }
