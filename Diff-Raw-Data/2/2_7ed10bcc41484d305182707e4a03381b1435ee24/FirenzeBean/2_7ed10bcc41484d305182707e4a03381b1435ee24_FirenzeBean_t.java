 package capasdecontrol;
 
 import algoritmosdeinferencia.AlgoritmoDeInferencia;
 import algoritmosdeinferencia.Deduccion;
 import algoritmosdeinferencia.Regla;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Charlie Corner
  */
 public class FirenzeBean {
 
     private AlgoritmoDeInferencia algoritmo;
     private List<Regla> listaReglas;
     private List<String> comboDeObjetivosASeleccionar;
     private List<String> listaHechosDeInicioASeleccionar;
     private String objetivo;
     private List<String> listaHechosDeInicioSeleccionados;
     private File archivoDeRegla;
     private String resultado;
     private boolean activarBoton;
 
     public FirenzeBean() {
         algoritmo = null;
         activarBoton = false;
     }
 
     public void correrAlgoritmoDeduccion() {
        this.algoritmo = new Deduccion(listaReglas, listaHechosDeInicioSeleccionados);
         this.algoritmo.correrAlgoritmo();
         this.resultado = algoritmo.getResultado();
     }
 
     public void correrAlgoritmoInduccion() {
     }
 
     public boolean isActivarBoton() {
         return activarBoton;
     }
 
     public void setActivarBoton(boolean activarBoton) {
         this.activarBoton = activarBoton;
     }
 
     public File getArchivoDeRegla() {
         return archivoDeRegla;
     }
 
     public void setArchivoDeRegla(File archivoDeRegla) {
         this.archivoDeRegla = archivoDeRegla;
         if (null != this.archivoDeRegla) {
             this.listaReglas = FirenzeUtil.listaFromLineasSinParsear(leerArchivo());
             this.listaHechosDeInicioASeleccionar = parsearListaDeHechosDeInicio(this.listaReglas);
             this.comboDeObjetivosASeleccionar = parsearListaDeObjetivos(this.listaReglas);
             activarBoton = true;
         }
     }
 
     public AlgoritmoDeInferencia getAlgoritmo() {
         return algoritmo;
     }
 
     public String getResultado() {
         return resultado;
     }
 
     @SuppressWarnings("CallToThreadDumpStack")
     private List<String> leerArchivo() {
         List<String> listaSinParsear = new ArrayList<String>();
         FileReader fr = null;
         BufferedReader br = null;
         try {
             fr = new FileReader(this.archivoDeRegla);
             br = new BufferedReader(fr);
 
             String linea;
             while ((linea = br.readLine()) != null) {
                 listaSinParsear.add(linea);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             try {
                 if (null != fr) {
                     fr.close();
                 }
             } catch (Exception e2) {
                 e2.printStackTrace();
             }
         }
         return listaSinParsear;
     }
 
     public List<String> getComboDeObjetivosASeleccionar() {
         return comboDeObjetivosASeleccionar;
     }
 
     public void setComboDeObjetivosASeleccionar(List<String> comboDeObjetivosASeleccionar) {
         this.comboDeObjetivosASeleccionar = comboDeObjetivosASeleccionar;
     }
 
     public List<String> getListaHechosDeInicioSeleccionados() {
         return listaHechosDeInicioSeleccionados;
     }
 
     public void setListaHechosDeInicioSeleccionados(List<String> listaHechosDeInicioSeleccionados) {
         this.listaHechosDeInicioSeleccionados = listaHechosDeInicioSeleccionados;
     }
 
     public String getObjetivo() {
         return objetivo;
     }
 
     public void setObjetivo(String objetivo) {
         this.objetivo = objetivo;
     }
 
     public List<String> getListaHechosDeInicioASeleccionar() {
         return listaHechosDeInicioASeleccionar;
     }
 
     public void setListaHechosDeInicioASeleccionar(List<String> listaHechosDeInicioASeleccionar) {
         this.listaHechosDeInicioASeleccionar = listaHechosDeInicioASeleccionar;
     }
 
     private List<String> parsearListaDeHechosDeInicio(List<Regla> listaReglas) {
         List<String> hechosDeInicio = new ArrayList<String>();
 
         for (Regla r : listaReglas) {
             List<String> causantes = r.getCausantes();
             
             for(String s: causantes){
                 
                 if (!isCadenaEnLista(s, hechosDeInicio)) {
                     hechosDeInicio.add(s);
                 }
             }
         }
         return hechosDeInicio;
     }
 
     private List<String> parsearListaDeObjetivos(List<Regla> listaReglas) {
         List<String> objetivos = new ArrayList<String>();
 
         for (Regla r : listaReglas) {
             String producto = r.getProducto();
 
             if (!isCadenaEnLista(producto, objetivos)) {
                 objetivos.add(producto);
             }
         }
         return objetivos;
     }
 
     private boolean isCadenaEnLista(String cadena, List<String> lista) {
         
         for (String s : lista) {
             if (cadena.equals(s)) {
                 return true;
             }
         }
         return false;
     }
 }
