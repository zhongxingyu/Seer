 public class ERUtil {
 
 
  /**
 //fuente http://snipt.org/uffgc5
    Calcula el dígito de control
 
   @param entidad  Entidad bancaria.
   @param sucursal Código sucursal bancaria. 
   @param n_cuenta Número de cuenta bancaria.
   @return         Dígito de control.
   */
 
 public static String calculaDC(String entidad, String sucursal, String n_cuenta) {
 
         String dc = "";
         int calculo = 0;
         int calculo1 = 0;
         String dc1 = "";
         String dc2 = "";
         String enti = entidad;
         String sucur = sucursal;
         /*Primer dígito.*/
         for (int i = 0; i < 4; i++) {
             if (i==0){
                 calculo1 = Integer.parseInt(enti.substring(i, i + 1))*4;
             }else if (i==1){
                 calculo1 = Integer.parseInt(enti.substring(i, i + 1))*8;
             }else if (i==2){
                 calculo1 = Integer.parseInt(enti.substring(i, i + 1))*5;
             }else {
                 calculo1 = Integer.parseInt(enti.substring(i, i + 1))*10;
             }
             calculo = calculo + calculo1;
         }
         for (int j = 0; j < 4; j++) {
             if (j==0){
                 calculo1 = Integer.parseInt(sucur.substring(j, j + 1))*9;
             }else if (j==1){
                 calculo1 = Integer.parseInt(sucur.substring(j, j + 1))*7;
             }else if (j==2){
                 calculo1 = Integer.parseInt(sucur.substring(j, j + 1))*3;
             }else {
                 calculo1 = Integer.parseInt(sucur.substring(j, j + 1))*6;
             }
             calculo = calculo + calculo1;
         }
  
         calculo1 = 11 - calculo % 11;
         if (calculo1 == 10) {
             dc1 = String.valueOf(1);
         } else if (calculo1 == 11) {
             dc1 = String.valueOf(0);
         } else {
             dc1 = String.valueOf(calculo1);
         }
         calculo=0;
  
         /*Segundo dígito.*/
         for (int k=0; k<10; k++){
             if (k==0){
                 calculo1 = Integer.parseInt(n_cuenta.substring(k, k + 1))*1;
             }else if (k==1){
                 calculo1 = Integer.parseInt(n_cuenta.substring(k, k + 1))*2;
             }else if (k==2){
                 calculo1 = Integer.parseInt(n_cuenta.substring(k, k + 1))*4;
             }else if (k==3){
                 calculo1 = Integer.parseInt(n_cuenta.substring(k, k + 1))*8;
             }else if (k==4){
                 calculo1 = Integer.parseInt(n_cuenta.substring(k, k + 1))*5;
             }else if (k==5){
                 calculo1 = Integer.parseInt(n_cuenta.substring(k, k + 1))*10;
             }else if (k==6){
                 calculo1 = Integer.parseInt(n_cuenta.substring(k, k + 1))*9;
             }else if (k==7){
                 calculo1 = Integer.parseInt(n_cuenta.substring(k, k + 1))*7;
             }else if (k==8){
                 calculo1 = Integer.parseInt(n_cuenta.substring(k, k + 1))*3;
             }else {
                 calculo1 = Integer.parseInt(n_cuenta.substring(k, k + 1))*6;
             }
             calculo = calculo + calculo1;
         }
         calculo1 = 11 - calculo % 11;
         if (calculo1 == 10) {
             dc2 = String.valueOf(1);
         } else if (calculo1 == 11) {
             dc2 = String.valueOf(0);
         } else {
             dc2 = String.valueOf(calculo1);
         }
 	calculo=0;
        dc=String.valueOf(dc1)+String.valueOf(dc1);
         return dc;
     }
 
  /**
 //fuente http://felinfo.blogspot.com.es/2010/12/calcular-la-letra-del-dni-con-java.html
    Calcula la letra del DNI
 
   @param dni  Cadena numérica.
   @return     Letra.
   */
 public static char calculaLetraDNI(int dni)
     {
     String juegoCaracteres="TRWAGMYFPDXBNJZSQVHLCKET";
     int modulo= dni % 23;
     char letra = juegoCaracteres.charAt(modulo);
     return letra; 
     } 
 
 }
