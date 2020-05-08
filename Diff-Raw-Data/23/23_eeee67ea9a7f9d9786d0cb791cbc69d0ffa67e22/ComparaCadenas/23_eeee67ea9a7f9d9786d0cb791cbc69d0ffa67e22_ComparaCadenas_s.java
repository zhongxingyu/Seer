 package ngmep.procesos;
 /*
 	ngmep2osm - importador de datos de ngmep a openstreetmap
 	
 	Copyright (C) 2011-2012 Alberto Fernández <infjaf@gmail.com>
 	
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 	
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 	
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 import java.util.Locale;
 
 import org.apache.commons.lang3.StringUtils;
 
 public final class ComparaCadenas {
     private static final Locale LOCALE_ES = new Locale("ES", "es");
     
     
     private ComparaCadenas() {
     	// No instances
     }
     
     /*
      *  Pasa a mayusculas, 
      *  cambia acentos
      *  elimina espacios en blanco
      *  elimina signos de puntuacion
      */
     public static String normaliza1 (final String cadena){
         String cadena2 = cadena.toUpperCase(LOCALE_ES);
         cadena2 =cadena2.replaceAll("Á", "A");
         cadena2 =cadena2.replaceAll("É", "E");
         cadena2 =cadena2.replaceAll("Í", "I");
         cadena2 =cadena2.replaceAll("Ó", "O");
         cadena2 =cadena2.replaceAll("Ú", "U");
         cadena2 =cadena2.replaceAll("Ü", "U");
         cadena2 =cadena2.replaceAll("Ñ", "N");
         cadena2 = cadena2.replaceAll(" ", "");
         cadena2 = cadena2.replaceAll(";", "");
         cadena2 = cadena2.replaceAll("-", "");
         return cadena2;
     }
     /**
      * La 1 + "faltas de ortografia"
      * @param cadena
      * @return
      */
     public static String normaliza2(final String cadena) {
         String cadena2 = normaliza1 (cadena);
         cadena2 = cadena2.replaceAll("H", "");
         cadena2 = cadena2.replaceAll("B", "V");
         cadena2 = cadena2.replaceAll("G", "J");
         return cadena2;
     }
     /**
      * Determina si cad2 es cad1 + apellidos.
      * @param cad1 La cadena simple
      * @param cad2 La cadena simple
      * @return true si cad1 tiene "apellidos" de cad2
      */
     public static boolean prefijoDe (final String cad1, final String cad2){
         return cad2.toUpperCase(LOCALE_ES).indexOf(cad1.toUpperCase(LOCALE_ES)+ " DE") == 0;
     }
     /**
      * Determina si cad2 es cad1 + apellidos.
      * @param cad1 La cadena simple
      * @param cad2 La cadena simple
      * @return true si cad1 tiene "apellidos" de cad2
      */
     public static boolean prefijoDeN2(final String cad1, final String cad2){
         final String normalizada1= normaliza2(cad1);
         final String  normalizada2 = normaliza2(cad2);
         return normalizada2.indexOf(normalizada1 + "DE") == 0;
     }
     public static String quitaArticulos(final String cadena){
         String cadena1 = cadena.toUpperCase(LOCALE_ES);
         cadena1 = cadena1.replaceAll("^URBANIZACIÓN "," ");
         cadena1 = cadena1.replaceAll(" DE ", " ");
         cadena1 = cadena1.replaceAll(" DEL ", " ");
         cadena1 = cadena1.replaceAll(" LA " , " ");
         cadena1 = cadena1.replaceAll(" LAS ", " ");
         cadena1 = cadena1.replaceAll(" LOS ", " ");
         cadena1 = cadena1.replaceAll(" EL ", " ");
         cadena1 = cadena1.replaceAll("^DE ", " ");
         cadena1 = cadena1.replaceAll("^LA " , " ");
         cadena1 = cadena1.replaceAll("^LAS ", " ");
         cadena1 = cadena1.replaceAll("^EL ", " ");
         
         return cadena1;
     }
     public static boolean articulos (final String cad1, final String cad2){
         final String cadena1 = quitaArticulos(cad1.toUpperCase(LOCALE_ES));
         final String cadena2 = quitaArticulos(cad2.toUpperCase(LOCALE_ES));
         return normaliza2(cadena1).equals(normaliza2(cadena2));
         
     }
     public static boolean distanciaUnaLetra (final String cad1, final String cad2){
         if (cad1.length() == cad2.length() ){
             final String cadena1 = cad1.toUpperCase(LOCALE_ES);
             final String cadena2 = cad2.toUpperCase(LOCALE_ES);
             for (int i = 0; i < cad1.length(); i++){
                 final StringBuilder test1 = new StringBuilder();
                 final StringBuilder test2 = new StringBuilder();
                 test1.append(cadena1);
                 test2.append(cadena2);
                 test1.setCharAt(i, 'X');
                 test2.setCharAt(i, 'X');
                 if (test1.toString().equals(test2.toString())){
                     return true;
                 }
             }
         }
         else if (Math.abs(cad1.length() - cad2.length()) == 1){
             String mayor = cad1.toUpperCase(LOCALE_ES);
             String menor = cad2.toUpperCase(LOCALE_ES);
             if (mayor.length() < menor.length()){
                 final String tmp = mayor;
                 mayor = menor;
                 menor = tmp;
             }
             for (int i = 0; i < mayor.length(); i++){
                 final StringBuilder test1 = new StringBuilder();
                 final StringBuilder test2 = new StringBuilder();
                 test1.append(mayor);
                 test2.append(menor);
                 test1.setCharAt(i, 'X');
                 test2.insert(i, 'X');
                 if (test1.toString().equals(test2.toString())){
                     return true;
                 }
             }
         }
         
         return false;
     }
     public static boolean iguales(final String ine, final String osm){
         if (StringUtils.isBlank(ine) || StringUtils.isBlank(osm)){
             return false;
         }
         if (ine.toUpperCase(LOCALE_ES).equals(osm.toUpperCase(LOCALE_ES))){
             return true;
         }
         if (normaliza1(ine).equals(normaliza1(osm))){
             return true;
         }
         if (normaliza2(ine).equals(normaliza2(osm))){
             return true;
         }
         if (prefijoDe (ine, osm) || prefijoDe (osm, ine)){
             return true;
         }
         if (prefijoDeN2 (ine, osm) || prefijoDeN2 (osm, ine)){
             return true;
         }
         if (articulos(ine, osm)){
             return true;
         }
         if (distanciaUnaLetra(ine, osm)){
             return true;
         }
         if (distanciaUnaLetra(normaliza2(ine), normaliza2(osm))){
             return true;
         }
         return false;
     }
     public static boolean igualesObjetivo3(final String ine, final String osm) {
         if (StringUtils.isBlank(ine) || StringUtils.isBlank(osm)){
             return false;
         }
         if (ine.toUpperCase(LOCALE_ES).equals(osm.toUpperCase(LOCALE_ES))){
             return true;
         }
         if (normaliza1(ine).equals(normaliza1(osm))){
             return true;
         }
         if (normaliza2(ine).equals(normaliza2(osm))){
             return true;
         }
         if (articulos(ine, osm)){
             return true;
         }
         if (distanciaUnaLetra(ine, osm)){
             return true;
         }
         if (distanciaUnaLetra(normaliza2(ine), normaliza2(osm))){
             return true;
         }
         return false;
    }
 }
