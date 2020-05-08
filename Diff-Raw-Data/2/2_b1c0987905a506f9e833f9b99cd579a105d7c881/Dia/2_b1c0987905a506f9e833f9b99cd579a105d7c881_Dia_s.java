 package sirp.Entidades;
 
 import java.util.Date;
 import sirp.*;
 
 public class Dia {
     private int id_dia;
     private Date dia;
 
     public Dia(Date dia) {
         this.dia = dia;
         java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd");
        if(SIRP.con.nSeleccionados("SELECT id_dia FROM registro.dia WHERE dia ='"+format.format(dia)+"'")==0)
             id_dia = SIRP.con.ultimo("registro.dia", "id_dia")+1;
         else
             id_dia = Integer.parseInt(SIRP.con.ver("SELECT id_dia FROM registro.dia WHERE dia ='"+format.format(dia)+"'", "id_dia"));
     }
 
     public int getId_dia() {
         return id_dia;
     }
     
     public Dia(){
         dia = new Date();
         id_dia = SIRP.con.ultimo("registro.dia", "id_dia")+1;
     }
     
     public String getDia(){
         java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
         return sdf.format(dia);
     }   
     
     public void insert(){        
         java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd");
         if(SIRP.con.nSeleccionados("SELECT id_dia FROM registro.dia WHERE dia ='"+format.format(dia)+"'")==0){
             SIRP.con.query("INSERT INTO registro.dia(id_dia,dia)VALUES('"+
                     id_dia+"','"+
                     format.format(dia)+"');");
         }
     }    
     @Override public String toString(){
         String s="";
         s+=id_dia+" "+dia.toString();
         return s;
     }
 }
