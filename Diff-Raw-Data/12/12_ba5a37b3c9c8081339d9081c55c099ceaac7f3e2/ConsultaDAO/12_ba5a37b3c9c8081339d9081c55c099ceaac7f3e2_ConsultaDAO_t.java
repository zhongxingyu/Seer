 
 package Dao;
 
 import Appi.TimeOPeration;
 import Javabeans.Area;
 import Utilitarios.Helpers;
 import Utilitarios.Query;
 import Utilitarios.ConexionBd;
 import Utilitarios.Data;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Time;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import javax.swing.JLabel;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 
 public class ConsultaDAO {
     
     private Query qs;
     private Area objArea;
     private Helpers hp;
     private Data dt;
     private String filter[][];
     private String campos[];
     private int witdhcolum[];
     private String _table;
     private String _error;
     private ResultSet rs = null;
     private ResultSet rs_extra = null;
     private Statement s = null;
     private Statement s_extra = null;
     private PreparedStatement  pt = null;
     private ConexionBd con;
     private Connection conexion;
     private TimeOPeration tm;
     private String DateEmp[];
     private String _Table;
     private String R_Justifica[];
 
     public ConsultaDAO(){
         _error = "Dao_ConsultaDAO_";
         filter = new String[0][0];
         campos = new String[0];
         witdhcolum = new int[6];
         witdhcolum[0]=180;
         witdhcolum[1]=80;
         witdhcolum[2]=75;
         witdhcolum[3]=75;
         witdhcolum[4]=75;
         witdhcolum[5]=60;
         _table = "report";
     }
     public void setTable(String table) {
         this._Table = table;
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
     public void findAsistencia(String args[]) {
         qs = new Query();
         Statement s = null;
         con = new ConexionBd();
         try {
             //Campos de la tabla report
             String lista[];
             campos = set_camp_registro();
             //Campos para la consulta
             qs.create_report(campos);
             String[] camp = new String[2];
             camp[0] = "fecha";
             //camp[1] = "idtip_reg%G_TIPOREG";
             camp[1] = "hora";
             DateEmp = qs.getRecords("empleado",Integer.parseInt(args[0]));
             //Filtros
             filter = new String[2][2];
             filter[0][0] = "int_idemp";
             filter[0][1] = args[0];
             filter[1][0] = "bet_fecha_" + args[1];
             filter[1][1] = args[2];
             helper_asistencia(filter, camp, args[1], args[2]);
             filter = new String[0][0];
         }
         catch(Exception e){
             System.out.println(_error + "findAsistencia : "+e);
         }
     }
     public void destroid_report() {
         qs.destroid_report();
     }
     public void gettabel(JTable tblDatos, JLabel lblcant) {
         getTableAll(tblDatos,lblcant);
     }
     /*
      * Reporte de asistencia
      */
     private void helper_asistencia(String filter[][], String camp[],
             String fechInicio, String fechFinal) throws SQLException, ParseException{
         qs= new Query();
         hp= new Helpers();
         dt = new Data();
         //Declaracion
         SimpleDateFormat fhora = new SimpleDateFormat("HH:mm:ss");
         String Consulta = "";
         Object[] fila;
         String[] temp;
         String tbl;
         int count = 0;
         int ind = 0;
         boolean tardanza=false;
         boolean extra=false;
         con.getConexion();
         conexion = con.getConetion();
         s = conexion.createStatement();
         //Titulos
         Consulta = qs.getQueryList(camp, this._Table + "/fecha", filter);
         //System.out.println("consulta: "+Consulta);
         rs = s.executeQuery(Consulta);
         ResultSetMetaData meta = rs.getMetaData();
         int nCols = meta.getColumnCount();
         fila = new Object[nCols];
         //Logica
         fechFinal = qs.getDay(fechFinal, "+");
         String fechActual = fechInicio;
         int dia = 0;
         String Total="00:00";
         String tmp_table = "";
         do {
             tardanza =  false;
             extra = false;
             ind = 0;
             //Ejecucion de consulta
             filter[1][0] = "bet_fecha_" + fechActual;filter[1][1] = fechActual;
             //Consulta a justifiaciones si tiene una justificaciond e horas extras o tardanza en ese dia
             int tar = Integer.parseInt(qs.Execute("select count(*) from justificaciones where fecha = '"+fechActual+"' and idtip_jus = 1  and empleado_idemp="+filter[0][1]));
             int ext = Integer.parseInt(qs.Execute("select count(*) from justificaciones where fecha = '"+fechActual+"' and idtip_jus = 4  and empleado_idemp="+filter[0][1]));
             if((tar+ext)==0){
                 Consulta = qs.getQueryList(camp, this._Table + "/fecha", filter);
                 tmp_table = "";
             } else {
                 tmp_table = "registro_backlog";
                 Consulta = qs.getQueryList(camp, tmp_table + "/fecha", filter);
                 if(tar>0){
                     tardanza = true;
                 }
                 if(ext>0){
                     extra = true;
                 }
             }
             //System.out.println(Consulta);
             rs = s.executeQuery(Consulta);
             boolean countReg = false;
             //Registro del reporte
             String[] campReg;
             campReg = new String[6];
             dia = Integer.parseInt(qs.getDayOfTheWeek(fechActual));
             while(rs.next()){
                 countReg = true;
                 count++;
                 for(int i=0; i<nCols; ++i){
                     fila[i] = rs.getObject(i+1);
                     //Temporal
                     if(ind >= 2 && i == 1 ){
                         campReg[ind]=String.valueOf(fila[i]);
                         ind++;
                     }
                     if(ind < 2){
                         if(ind == 0) {
                             fila[i] = fila[i] + " (" + dt.G_DIAS[dia] + ")";
                         }
                         campReg[ind]=String.valueOf(fila[i]);
                         ind++;
                     }
                 }
             }
             //Validacion tardanza justificada
             if(tardanza){
                campReg[1] = qs.Execute("select fin from justificaciones where fecha = '"+fechActual+"' and idtip_jus = 1  and empleado_idemp="+filter[0][1]);
             }
             
             //Suma
             if(countReg == true) {
                 if(ind == 3){
                     campReg[4] = campReg[2];campReg[2] = "";campReg[3] = "";
                     campReg[5] = calculoHoras(campReg, 2);
                 } else if(ind == 5){
                     campReg[5] = calculoHoras(campReg, 4);
                 }
                 Total = tm.SumaHoras(String.valueOf(Total), String.valueOf(campReg[5]));
             }
             //No existen registros del dia
             if(countReg == false) { 
                 campReg = new String[6];
                 campReg[0]=String.valueOf(fechActual + " (" + dt.G_DIAS[dia] + ")");
                 //Dia no labora deacuerdo a su horario
                 if(getDiasTrabajo(DateEmp[1], fechActual, dia) == false) {
                     campReg[1] = "";
                     if(extra){
                         System.out.println();
                         campReg[1] = qs.Execute("select inicio from justificaciones where fecha = '"+fechActual+"' and idtip_jus = 4  and empleado_idemp="+filter[0][1]);
                         campReg[2] = "";
                         campReg[3] = "";
                         campReg[4] = qs.Execute("select fin from justificaciones where fecha = '"+fechActual+"' and idtip_jus = 4  and empleado_idemp="+filter[0][1]);
                         campReg[5] = qs.Execute("select horas from justificaciones where fecha = '"+fechActual+"' and idtip_jus = 4  and empleado_idemp="+filter[0][1]);
                         //campReg[1]=String.valueOf("HORA: "+campReg[4]);
                         Total = tm.SumaHoras(String.valueOf(Total), String.valueOf(campReg[5]));
                     }
                 }
                 else {
                     int cVal=0;
                     String[] args =  new String[5];
                     args[0]="nolaborables";
                     args[1]="idempr";
                     args[2]=DateEmp[14];
                     args[3]="fecha";
                     args[4]=fechActual;
                     cVal = qs.getCountRegister(args);
                     if(cVal > 0) {
                        campReg[1]=String.valueOf("Feriado");
                     } 
                     if(cVal == 0){
                         cVal = qs.getcount("vacaciones where idemp='" + DateEmp[1] +"' and '"+fechActual+"'>=f_ini and '"+fechActual+"'<=f_final");
                         if(cVal > 0) {
                             campReg[1]=String.valueOf("Vacaciones");
                         } 
                     }
                     if(cVal == 0){
                         args =  new String[7];
                         args[0]="justificaciones";
                         args[1]="empleado_idemp";
                         args[2]=DateEmp[1];
                         args[3]="fecha";
                         args[4]=fechActual;
                         args[5]="idtip_jus";
                         args[6]="2";
                         cVal = qs.getCountRegister(args);
                         if(cVal > 0) {
                             campReg[1]=String.valueOf("Justificado");
                         }
                     }
                     if(cVal == 0)
                         campReg[1]=String.valueOf("Falto");
                     }
                if(!extra){
                    for(int i=2;i<campReg.length;i++){
                         campReg[i]=String.valueOf("");
                     }
                }
             }
             register_report(campReg);
         fechActual = qs.getDay(fechActual, "+");
         } while(!fechActual.equals(fechFinal));
          String[] campReg = new String[6];
          campReg[0] = "";
          campReg[1] = "";
          campReg[2] = "";
          campReg[3] = "";
          campReg[4] = "Total";
          campReg[5] = Total;
         register_report(campReg);
     }
         
     public String calculoHoras(String[] args, int op){
         String suma="";
         try {
             tm = new TimeOPeration();
             DateFormat sdf = new SimpleDateFormat("HH:mm:ss");
             if (op == 2) {
                 String inicio = args[1];
                 String termino = args[4];
                 Date date_ini = sdf.parse(inicio);
                 Date date_fin = sdf.parse(termino);
                 Time hrs = tm.restarTime(Time.valueOf(sdf.format(date_fin)),
                         Time.valueOf(sdf.format(date_ini)));
                 suma = String.valueOf(hrs);
             }
             if(op == 4) {
                 String inicio = args[1];
                 String termino = args[4];
                 String ref_inicio = args[2];
                 String ref_termino = args[3];
                 Date date_ini = sdf.parse(inicio);
                 Date date_fin = sdf.parse(termino);
                 Date date_ref_ini = sdf.parse(ref_inicio);
                 //Date ref = sdf.parse("12:00:00");
                 Date date_ref_fin = sdf.parse(ref_termino);
                 Time hrs_trabajo = tm.restarTime(Time.valueOf(sdf.format(date_ref_ini)),
                         Time.valueOf(sdf.format(date_ini)));
                 Time hrs_refrigerio = tm.restarTime(Time.valueOf(sdf.format(date_fin)),
                         Time.valueOf(sdf.format(date_ref_fin)));
                 //Time hrs = tm.sumarTime(hrs_trabajo, hrs_refrigerio);
                 //hrs = tm.restarTime(hrs, Time.valueOf(sdf.format(ref)));
                 //suma = String.valueOf(hrs);
                 suma = tm.SumaHoras(String.valueOf(hrs_trabajo), String.valueOf(hrs_refrigerio));
             } 
         } catch(Exception e) {
             System.out.println(_error + "calculoHoras: "+e);
         }
         return suma;
     }
     /*
      * Registro en report
      */
     public void register_report(String[] args) {
         pt = null;
         try {
             System.out.println("insertar");
             String query = "insert into report values(";
             for(int i=0;i<args.length;i++){
                 query = query + "'" + args[i] + "'";
                 if(args.length!=i+1) { 
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
     /*
      * Asignacion decampos
      */
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
     public String[] set_camp_justificaciones(){
         String campos[] = new String[6];
         campos[0] = "tipo";
         campos[1] = "recibo";
         campos[2] = "descripcion";
         campos[3] = "hora_ini";
         campos[4] = "hora_final";
         campos[5] = "salida";
         return campos;
     }
     public String[] set_camp_resumen(){
         String campos[] = new String[11];
         campos[0] = "idemp";
         campos[1] = "horas_trabajadas";
         campos[2] = "c_extra";
         campos[3] = "h_extra";
         campos[4] = "c_tardanza";
         campos[5] = "h_tardanza";
         campos[6] = "c_falta";
         campos[7] = "j_falta";
         campos[8] = "total";
         campos[9] = "descuento";
         campos[10] = "adicional";
         return campos;
     }
 /*
 * Reporte de justificaciones
 */
    public void create_report_justificaciones(String args[]){
        qs= new Query();
        con = new ConexionBd();
        tm = new TimeOPeration();
        try {
             Statement s = null;
             Statement s_extra = null;
             campos = set_camp_justificaciones();
             qs.create_report(campos);
             con.getConexion();
             conexion = con.getConetion();
             s = conexion.createStatement();
             s_extra = conexion.createStatement();
             String[] campReg = new String[6];
             String Consulta;
             String ConsultaTipo = "select * from tipo_justificaciones";
             //System.out.println(Consulta);
             
             rs = s.executeQuery(ConsultaTipo);
             int i;
             while(rs.next()){
                 String suma="00:00";
                 i = 0;
                 Consulta = "select * from justificaciones "
                         + "where idtip_jus = " +rs.getInt(1)
                         + " and empleado_idemp="+args[0]
                         + " and fecha between '"+args[1]+"' and '"+args[2]+"'";
                 rs_extra = s_extra.executeQuery(Consulta);
                 while(rs_extra.next()){
                     if(i==0){
                         campReg[0] = rs.getString(1);
                         campReg[1] = rs.getString(2);
                         campReg[2] = "";
                         campReg[3] = "";
                         campReg[4] = "";
                         campReg[5] = "";
                         register_report(campReg);i++;
                     }
                     campReg[0] = rs.getString(1);
                     campReg[1] = rs_extra.getString(7);
                     campReg[2] = rs_extra.getString(4);
                     if (!"2".equals(rs.getString(1))){
                         campReg[3] = rs_extra.getString(8);
                         campReg[4] = rs_extra.getString(9);
                         campReg[5] = rs_extra.getString(6);
                     }
                     register_report(campReg);
                     suma = tm.SumaHoras(suma, rs_extra.getString(6));
                 }
                 if(i>0){
                     if (!"2".equals(rs.getString(1))){
                         campReg[0] = rs.getString(1);
                         campReg[1] = "";
                         campReg[2] = "";
                         campReg[3] = "";
                         campReg[4] = "Total";
                         campReg[5] = suma;
                         register_report(campReg);
                     }
                 }
             }
             con.closeConexion();
        } catch(Exception e){
            System.out.println(_error + "create_report_justificaciones : "+e);
        }
     }
     public boolean getDiasTrabajo(String emp, String fecha, int dia) throws SQLException{
            boolean val = false;
            con = new ConexionBd();
            con.getConexion();
            s = conexion.createStatement();
            conexion = con.getConetion();
             String Consulta = "select dia "
                     + "from empleado_has_horarios  c, detailhorario d "
                     + "where idemp= "+emp+" "
                     + "and '"+fecha+"' >= inicio  "
                     + "and '"+fecha+"' <= fin "
                     + "and c.idhor = d.horarios_idhor";
             rs = s.executeQuery(Consulta);
             while(rs.next()){
                 //Si trabaja ese dia = true
                 if(rs.getInt(1) == dia) {
                     return true;
                 }
             }
         
         return val;
        }
    /*
    * Reporte de justificaciones
    */
    public void create_report_resumen(String args[]){
        qs= new Query();
        con = new ConexionBd();
        try {
             con.getConexion();
             conexion = con.getConetion();
             //Horas trabajadas (report registro)
                 setTable("registro");
                 findAsistencia(args);
                 String hTrabajada = qs.Execute("select horas from report where salida='Total'");
                 destroid_report();
                 //Consulta el total
             //JUstifiaciones
                 create_report_justificaciones(args);
                 String hExtras = "00:00";
                 String tipo = qs.Execute("select idtip_jus from tipo_justificaciones where nombre = 'PERMISO HORAS EXTRAS'");
                 String cExtras = qs.Execute("select count(*) from report where tipo='"+tipo+"'");
                 if(!"0".equals(cExtras)){
                     hExtras = qs.Execute("select salida from report where tipo='"+tipo+"' and hora_final='Total'");
                 }
                 tipo = qs.Execute("select idtip_jus from tipo_justificaciones where nombre = 'TARDANZA JUSTIFICADA'");
                 String hTardanza = "00:00";
                 String cTardanza = qs.Execute("select count(*) from report where tipo='"+tipo+"'");
                 if(!"0".equals(cTardanza)){
                     hTardanza = qs.Execute("select salida from report where tipo='"+tipo+"' and hora_final='Total'");
                 }
                 tipo = qs.Execute("select idtip_jus from tipo_justificaciones where nombre = 'FALTA JUSTIFICADA'");
                 String cFaltas = qs.Execute("select count(*) from report where tipo='"+tipo+"'");
                 String Total = "00:00";
                 destroid_report();
             campos = set_camp_resumen();
             qs.create_report(campos);
             qs.Execute("ALTER TABLE report ALTER COLUMN idemp type integer USING (idemp::integer)");
             String[] campReg = new String[11];
             campReg[0] = args[0];
             campReg[1] = hTrabajada;
             campReg[2] = cExtras;
             campReg[3] = hExtras;
             campReg[4] = cTardanza;
             campReg[5] = hTardanza;
             campReg[6] = "0";
             campReg[7] = cFaltas;
             campReg[8] = Total;
             campReg[9] = "";
             campReg[10] = "";
             con.getConexion();
             conexion = con.getConetion();
             register_report(campReg);
             con.closeConexion();   
        } catch(Exception e) {
            System.out.println(_error + "create_report_resumen : "+e);
        }
    }
    public String[][]  report_injustificaciones(String idemp, String Fini, String Ffin){
        qs = new Query();
        con = new ConexionBd();
        String result[][];
        result = new String[0][0];
        try {
             con.getConexion();
             conexion = con.getConetion();
             s = conexion.createStatement();
             s_extra = conexion.createStatement();
             String args[];
             args = new String[3];
             args[0] =  idemp;
             args[1] =  Fini;
             args[2] =  Ffin;
 
             ConsultaDAO consul = new ConsultaDAO();
             consul.setTable("registro_backlog");
             consul.findAsistencia(args);
             
             String query = "select * from report"; 
             rs = s.executeQuery(query);
             String fecha="",
                    hini="",
                    hfin="",
                    sum="",
                    ingreso="",
                    salida="";
             
             while(rs.next()){
                 
                 fecha = rs.getString(1).substring(0, 10);
                 hini = rs.getString(2);
                 hfin = rs.getString(5);
                 
                 String Consulta = "select ingreso, salida "
                     + "from empleado_has_horarios  c, detailhorario d "
                     + "where idemp= "+idemp+" "
                     + "and '"+fecha+"' >= inicio  "
                     + "and '"+fecha+"' <= fin "
                     + "and c.idhor = d.horarios_idhor "
                     + "and tip_reg = 1";
                     rs_extra= s_extra.executeQuery(Consulta);
                     ingreso = "";salida = "";
                     while(rs_extra.next()){
                         ingreso = rs.getString(1);
                         salida = rs.getString(2);
                     }
                     if(!"".equals(ingreso) && salida != "" ){
                         //hini - ingreso
                         //hfin - salida
                    //comparo entrada backlog no puede ser mayor a su horario de entrada (tardanza)
                         //verifico q no tenga justificacion de ese dia de tipo tardanza
                    //comparo salida backlog no puede ser mayor a su horario de salida (extra)
                         //verifico q no tenga justificacion de ese dia de tipo hora extra
                     } else {
                         //No trabaja ese dia
                     }
             }
             //registro en un arreglo [justi][fecha];[justi][entrada horario];[justi][entrada backlog](resta)
             consul.destroid_report();
             con.closeConexion();
        } catch(Exception e ){
            System.out.println(_error + "report_injustificaciones : "+e);
        }
        return result;
        
    }
    
    }
