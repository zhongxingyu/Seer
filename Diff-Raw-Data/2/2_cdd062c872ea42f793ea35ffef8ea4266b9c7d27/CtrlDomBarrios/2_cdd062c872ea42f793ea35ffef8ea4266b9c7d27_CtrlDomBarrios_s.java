 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controladors;
 
 
 import Auxiliares.Pair;
 import java.util.TreeMap;
 import restricciones.Restriccion;
 import elementos.*;
 import elementos.Cjt_Elementos;
 import barrio.Barrio;
 import mapa.Plano;
 
 /**
  *
  * @author ArclorenSarth
  */
 public class CtrlDomBarrios {
     
     private static CtrlDomBarrios INSTANCE;
     private TreeMap<String,Integer> TablaBarrios;
     private Barrio B;
     private Cjt_Elementos CjtElem;
     private TreeMap<String,Restriccion> CjtRest;
     private Plano Mapa;
     private CtrlDomRestricciones DOMRest;
     private CtrlDomElementos DOMElem;
     private stubbedElementosGDP GDPElem;
     private stubbedRestriccionesGDP GDPRest;
     private stubbedBarriosGDP GDPBarr;
     
    
     /**
      * 
      */
     private CtrlDomBarrios(){
         TablaBarrios = new TreeMap();
         DOMElem = CtrlDomElementos.getInstance();
         DOMRest = CtrlDomRestricciones.getInstance();
         GDPElem = stubbedElementosGDP.getInstance();
         GDPRest = stubbedRestriccionesGDP.getInstance();
         GDPBarr = stubbedBarriosGDP.getInstance();
     }
     
     private static void creaInstancia() {
         if (INSTANCE == null) {
             INSTANCE = new CtrlDomBarrios();
         }
     }
 
     public static CtrlDomBarrios getInstance() {
         if (INSTANCE == null) {
             creaInstancia();
         }
         return INSTANCE;
     }
     
     /**
      * Crea un barrio con el Nombre que le llega como parametro nombre y el tipo
      * de barrio tip, que le llega como parametro.
      * @param nombre Nombre que se quiere poner al nuevo barrio que se crea.
      * @param tip Tipo de Barrio que tendra el nuevo barrio que se crea.
      * @return Retorna si ha sido posible crear el barrio, cierto si no existe
      * un barrio con ese nombre todavia y falso si ese barrio existe.
      */
     public boolean crearBarrio(String nombre, int tip){
         if(!TablaBarrios.containsKey(nombre) && tip>0 && tip<=3){
             B = new Barrio(nombre,tip);
             CjtElem = new Cjt_Elementos();
             CjtRest = new TreeMap();
             return true;
         }
         else return false;
     }
     
     /**
      * Carga un barrio previamente creado con el Nombre que le llega como 
      * parametro nombre.
      * @param nombre Nombre del barrio que se quiere cargar.
      * @return Retorna si ha sido posible cargar el barrio, cierto si existe el 
      * barrio con ese nombre y falso si ese barrio no existe.
      */
     public boolean cargarBarrio(String nombre){
         if(TablaBarrios.containsKey(nombre)){
             GDPBarr.leerBarrio(nombre,B);
             GDPBarr.leerMapa(nombre,Mapa);
             GDPBarr.leerCjtRest(nombre,CjtRest);
             GDPBarr.leerCjtElem(nombre,CjtElem);
             return true;
         }
         else return false;
     }
     
     /**
      * Guarda el barrio sobre el cual se estaba trabajando, ya sea uno cargado
      * previamente o uno nuevo creado.
      */
     public void guardarBarrio(){
         String nombre = B.getNombreBarrio();
         GDPBarr.escribirBarrio(nombre,B);
         GDPBarr.escribirMapa(nombre,Mapa);
         GDPBarr.escribirCjtRest(nombre,CjtRest);
         GDPBarr.escribirCjtElem(nombre,CjtElem);
     }
     
     
     /**
      * Crea un Plano que se le asignara a un barrio acabado de crear, con las
      * dimensiones n y m que le llegan como parametros.
      * @param n Numero de filas que tendra el Plano.
      * @param m Numero de columnas que tendra el Plano
      * @return Retorna si ha sido posible crear el plano.
      */
    public boolean crearMapaBarrio(int n, int m){
         if(n>=1 && m>=1){
             Mapa = new Plano(n,m);
             return true;
         }
         else return false;
     }
     
     
     /**
      * Añande la Restriccion r, que le llega como parametro, al CjtRest del 
      * barrio sobre el que se trabaja.
      * @param Rest Nombre de la restriccion que se quiere añadir.
      * @param r Restriccion que se quiere añadir.
      */
     private void putRestriccion(String Rest, Restriccion r){
         CjtRest.put(Rest,r);
     }
     
     
     /**
      * Añade la Restriccion con el nombre Rest, que le llega como parametro, al
      * Conjunto de Restricciones del barrio sobre el que se trabaja.
      * @param Rest Nombre de la Restriccion que se quiere añadir.
      * @return Retorna si se ha podido añadir la Restriccion deseada, cierto si
      * esa Restriccion existe y falso si esa Restriccion no existe.
      */
     public boolean anadirRestBarrio(String Rest){
         Restriccion r;
         if((DOMRest.getRestriccion(Rest)!= null) = r){
             putRestriccion(Rest,r);
             return true;
         }
         else return false;
     }
     
     
     /**
      * Elimina la Restriccion con el nombre Rest del Conjunto de Restricciones
      * del barrio sobre el que se trabaja.
      * @param Rest Nombre de la restriccion que se quiere eliminar.
      * @return Retorna si se ha podido eliminar la Restriccion deseada, cierto
      * si la Restriccion existe en el Conjunto de Restricciones del Barrio o
      * falso si dicha Restriccion no existe en el Conjunto.
      */
     public boolean quitarRestBarrio(String Rest){
         if(CjtRest.containsKey(Rest)){
             CjtRest.remove(Rest);
             return true;
         }
         else return false;
     }
     
     
     /**
      * Añade el Elemento con el oid, que le llega como parametro, al 
      * Cjt_Elementos del barrio sobre el que se trabaja.
      * @param oid Identificador numerico del elemento que se quiere añadir.
      * @param val El Elemento junto con la cantidad de ese elemento que se 
      * quiere añadir
      */
     private void putElemento(int oid, Pair<Integer,Elemento> val){
         if(CjtElem.containsKey(oid)){
             int c = val.getFirst();
             CjtElem.anadir_cantidad_elementos(oid, c);
         }
         else{
             CjtElem.insertar_elementos(oid, val);           
         }        
     }
     
     
     /**
      * Guarda toda la informacion relacionada con el Elemento e y la cantidad 
      * cant que se quiere añadir, pasados como parametros, en el barrio sobre el
      * que se trabaja y lo añade a su Conjunto de Elementos. La informacion 
      * relacionada con el elemento es por ejemplo su precio y la capacidad de
      * habitantes si es una Vivienda, esta informacion sera pasada al barrio
      * para que la guarde.
      * @param e Elemento que se quiere añadir.
      * @param cant Cantidad del Elemento que se quiere añadir.
      */
     private void guardarElemento(Elemento e, int cant){
         int gasto;
         int oid = e.getId();
         Pair v = new Pair(cant,e);
         if(e instanceof Vivienda){
             Vivienda e2 = (Vivienda) e;
             gasto = cant * e2.getPrecio();
             B.anadirHabitantes(e2.Getcap_max());
         }
         else if(e instanceof Publico){
             Publico e2 = (Publico) e;
             gasto = cant * e2.getPrecio();
         }
         else {
             Comercio e2 = (Comercio) e;
             gasto = cant * e2.getPrecio();
         }
         B.anadirGasto(gasto);
         putElemento(oid,v);
     }
     
     
     /**
      * Añade cant Elementos con el nombre Elem al barrio sobre el que se
      * trabaja, ambos pasados como parametros a la funcion.
      * @param Elem Nombre del Elemento que se quiere añadir al barrio.
      * @param cant Cantidad del Elemento que se quiere añadir.
      * @return Retorna si ha sido posible añadir el Elemento deseado, cierto si
      * el Elemento existe y su tipo sea compatible con el Tipo de Barrio del 
      * Barrio sobre el que se trabaja o falso si el Elemento no existe o no es
      * compatible con el Tipo de Barrio del Barrio sobre el que se trabaja.
      */
     public boolean anadirElemBarrio(String Elem, int cant){
         int tipo = B.getTipoBarrio();
         boolean b = false;
         Elemento e;
         int tipoel;
         e = DOMElem.getElemento(Elem);
         if(e != null){
             tipoel = DOMElem.getTBElemento(e);
             switch(tipo){
                     case 0: b = true;
                     case 1: b = (tipoel==1 || tipoel==0);
                     case 2: b = (tipoel==2 || tipoel==0);
                     case 3: b = (tipoel==3 || tipoel==0);
             }
             if(b) guardarElemento(e,cant);
         }
         return b;
     }
     
     
     /**
      * Elimina cant Elementos con oid que se le pasa por parametro del 
      * Cjt_Elementos del barrio sobre el que se trabaja.
      * @param oid Identificador numerico del Elemento que se quiere eliminar.
      * @param catn Cantidad de Elementos con el oid deseado que se quieren 
      * eliminar 
      */
     private void removeElemento(int oid, int catn){
         if(CjtElem.containsKey(oid)){
             CjtElem.eliminar_cantidad_elementos(oid, catn);         
             
         }
     } 
     
     
     /**
      * Elimina cant elementos con el nombre de Elemento Elem, pasado por 
      * parametro, del Barrio sobre el que se trabaja. Tambien actualiza la 
      * informacion relacionada con ese Elemento dentro del Barrio sobre el que 
      * se trabaja.
      * @param Elem Nombre del Elemento que se quiere eliminar.
      * @param cant Cantidad del Elemento que se quiere eliminar.
      * @return Retorna si se ha podido eliminar el Elemento deseado del Barrio,
      * cierto si ese elemento existe en el Conjunto de Elementos del Barrio o 
      * falso si no existe.
      */
     public boolean quitarElemento(String Elem, int cant){
         Elemento e = DOMElem.getElemento(Elem);
         int oid = e.getId();
         if(CjtElem.containsKey(oid)){
             Pair valor = (Pair) CjtElem.get(oid);
             int cantreal = (int) valor.getFirst();
             if (cantreal<cant) cant = cantreal;
             int gasto;
             if(e instanceof Vivienda){
                 Vivienda e2 = (Vivienda) e;
                 gasto = cant * e2.getPrecio();
                 B.anadirHabitantes(-(e2.Getcap_max()));
             }
             else if(e instanceof Publico){
                 Publico e2 = (Publico) e;
                 gasto = cant * e2.getPrecio();
             }
             else {
                 Comercio e2 = (Comercio) e;
                 gasto = cant * e2.getPrecio();
             }
             B.anadirGasto(-gasto);
             removeElemento(oid,cant);
             
             return true;           
         }
         else return false;
     }
     
     
     
     
     
     
     
 
     public void modificarPlano(){
 //        esta esta por si un caso, no creo que haga falta,
     }
     
     
     private void backFUCKINGtrackingBITCH(){
 //        privada que hara el back-fucking-mother-fucking-tracking!!!
 //        Aqui tienes que ponerlo Dani, pero avisa antes, sino, nos joderemos
 //        faena!!!
     }
     
     public void generarBarrio(){
 //        Esta sera la que se encargara de preparar el backtracking y SUPONGO
 //        la que reviara condiciones previas a la generacion etc!
         
     }
     
     
     
 
     
     
 //   ESTAS 2 Estaban en el Ctrl de elementos, de momento los puse aqui para 
 //    reciclar codigo mas que nada
     
     /**
      * Crea un conjunto vacio y lo añade a las estructuras
      * @param Nombre Nombre del conjunto a crear
      * @return Devuelve true si todo se ha realizado correctamente
      */
     public boolean CrearConjunto(String Nombre){
 
         Cjt_Elementos cjt = new Cjt_Elementos();
         //mapCjtElem.put(Nombre, cjt);
         return true;
 
     }
     
     
     
     /**
      * Añade la cantidad de elemento especificada al conjunto especificado
      * @param Nombre
      * @param e
      * @param cantidad
      * @return Devuelve true en caso de que todo se reaize correctamente
      */
     public boolean Anadir_elemento_al_conjunto(String Nombre,Elemento e, Integer cantidad){
             
             if(true/*mapElem.containsKey(e.getNom()) && mapCjtElem.containsKey(Nombre)*/){
                 Pair p = new Pair(cantidad,e);
                 //mapCjtElem.get(Nombre).insertar_elementos(e.getId(), p);
                 return true;
             }
             return false;
         }
 }
