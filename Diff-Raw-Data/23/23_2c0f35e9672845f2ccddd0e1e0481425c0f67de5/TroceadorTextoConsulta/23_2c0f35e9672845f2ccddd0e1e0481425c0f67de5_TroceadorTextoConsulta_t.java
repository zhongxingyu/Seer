 package es.miguelgonzalezgomez.dataBaseFun.bd;
 
 import es.miguelgonzalezgomez.dataBaseFun.bd.estaticos.lenguajes.DatosBaseDatos;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Miguel González
  */
 public class TroceadorTextoConsulta {
 
     private DatosBaseDatos datosBaseDatos;
    public String consultaSQL;
     private String consultaSQLAnalizandose;
     
     private List<String> consultasSQL;
     
     public TroceadorTextoConsulta(DatosBaseDatos datosBaseDatos, String consultaSQL) {
         this.datosBaseDatos = datosBaseDatos;
         this.consultaSQL = consultaSQLAnalizandose = consultaSQL;
         
         consultasSQL = new ArrayList<>();
         
         parseConsultaSQL();
     }
     
     public String[] getConsultasTroceadas() {
         String[] arrayConsultasSQL = new String[consultasSQL.size()];
         int posConsulta = 0;
         for(String consultaActualSQL : consultasSQL) {
             arrayConsultasSQL[posConsulta++] = consultaActualSQL;
         }
         return arrayConsultasSQL;
     }
     
     private void parseConsultaSQL() {
         while(consultaSQLAnalizandose.length() > 0) {
             if(!analizarConsultaTrozoActual()) {
                 consultasSQL.add(consultaSQLAnalizandose);
                 consultaSQLAnalizandose = "";
             }
         }
     }
     
     private boolean analizarConsultaTrozoActual() {
         String [][]palabrasClaveComienzoFin = datosBaseDatos.
                 getPalabrasClaveDelimitadoresConsulta();
         
         boolean consultaAnalizada = false;
         for(String palabraClaveComienzoFin[] : palabrasClaveComienzoFin) {
             if(encontrarPosicionInicioConsulta(palabraClaveComienzoFin, 0) == 0) {
                 consultaAnalizada = true;
                 obtenerTrozoActual(palabraClaveComienzoFin);
             }
         }
         
         return consultaAnalizada;
     }
     
     private void obtenerTrozoActual(String[] palabraClaveComienzoFin) {
         int posFinal = encontrarPosicionFinConsulta(palabraClaveComienzoFin, 0);
         int posInicioSiguienteConsulta = encontrarPosicionInicioConsulta(
                 palabraClaveComienzoFin, palabraClaveComienzoFin[0].length());
         
         boolean buscandoTrozo = true;
         while(buscandoTrozo) {
             if(posFinal == -1) {
                 consultasSQL.add(consultaSQLAnalizandose);
                 consultaSQLAnalizandose = "";
                 buscandoTrozo = false;
            } else if(posInicioSiguienteConsulta == -1 || posFinal <= posInicioSiguienteConsulta) {
                 String trozoConsulta = consultaSQLAnalizandose.substring(0, posFinal - 1);
                 consultasSQL.add(trozoConsulta);
                consultaSQLAnalizandose = consultaSQLAnalizandose.substring(posFinal).trim();
                 
                 buscandoTrozo = false;
             } else if(posFinal > posInicioSiguienteConsulta) {    
                 posInicioSiguienteConsulta = encontrarPosicionInicioConsulta(
                     palabraClaveComienzoFin, posFinal + 1);
             } else {
                 posFinal = encontrarPosicionFinConsulta(palabraClaveComienzoFin,
                         posInicioSiguienteConsulta);
             }
         }
     }
     
     private int encontrarPosicionFinConsulta(String[] palabraClaveComienzoFin,
             int inicioBusqueda) {
         int posFinal = consultaSQLAnalizandose.toLowerCase().indexOf(
                 palabraClaveComienzoFin[1].toLowerCase(),
                 inicioBusqueda
         );
         
         if(posFinal != -1) {
             posFinal += palabraClaveComienzoFin[1].length();
         }
         
         while(posFinal != -1 && estaDentroComentario(posFinal)) {
             posFinal = consultaSQLAnalizandose.toLowerCase().indexOf(
                     palabraClaveComienzoFin[1].toLowerCase(),
                     posFinal
             );
             
             if(posFinal != -1) {
                 posFinal += palabraClaveComienzoFin[1].length();
             }
         }
         return posFinal;
     }
     
     private int encontrarPosicionInicioConsulta(String[] palabraClaveComienzoFin,
             int inicioBusqueda) {
         int posInicial = consultaSQLAnalizandose.toLowerCase().indexOf(
                 palabraClaveComienzoFin[0].toLowerCase(),
                 inicioBusqueda
         );
         
         while(posInicial != -1 && estaDentroComentario(posInicial)) {
             posInicial = consultaSQLAnalizandose.toLowerCase().indexOf(
                     palabraClaveComienzoFin[0].toLowerCase(),
                     posInicial + 1
             );
         }
         return posInicial;
     }
     
     private boolean estaDentroComentario(int posicionLetra) {
         boolean dentroComentarioSimple = false;
         boolean dentroComentarioDoble = false;
         
         for(int i=0; i<consultaSQLAnalizandose.length() && i < posicionLetra; i++) {
             if(!dentroComentarioDoble && consultaSQLAnalizandose.charAt(i) == '\'') {
                 dentroComentarioSimple = !dentroComentarioSimple;
             }
             if(!dentroComentarioSimple && consultaSQLAnalizandose.charAt(i) == '\"') {
                 dentroComentarioDoble = !dentroComentarioDoble;
             }
         }
         
         return dentroComentarioSimple || dentroComentarioDoble;
     }   
 }
