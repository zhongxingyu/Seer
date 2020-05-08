 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package restaurante;
 
 import restaurante.UI.FrmLogin;
 import restaurante.UI.FrmMain;
 
 /**
  *
  * @author aluno
  */
 public class Main {
     public final static String PROGRAM_NAME = "Restaurante Vila Nova";
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // TODO code application logic here
         //FrmLogin frmLogin = new FrmLogin();
        FrmMain frmLogin = new FrmMain(1);
         frmLogin.show();
     }
 }
