 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controladors;
 
 
 import Auxiliares.Pair;
 import java.util.TreeMap;
 import restricciones.*;
 import elementos.*;
 import barrio.Barrio;
 import java.util.ArrayList;
 import java.util.HashMap;
 import mapa.Parcela;
 import mapa.Plano;
 
 /**
  *
  * @author ArclorenSarth
  */
 public class CtrlDomBarrios {
     
     private static CtrlDomBarrios INSTANCE=null;
     private TreeMap<String,Integer> TablaBarrios;
     private Barrio B;
     private Cjt_Edificios CjtElem;
     private TreeMap<String,Restriccion> CjtRest;
     private HashMap<Integer,ArrayList<Restriccion_ubicacion>> CjtRestUbic1;
     private HashMap<Integer,Restriccion_demografica> CjtRestDemog;
     private Restriccion_economica RestEcon;
     private Plano Mapa;
     private Plano copia;
     private CtrlDomRestricciones DOMRest;
     private CtrlDomElementos DOMElem;
     private stubbedElementosGDP GDPElem;
     private stubbedRestriccionesGDP GDPRest;
     private stubbedBarriosGDP GDPBarr;
     
     
     private int controlait;
    
     /////CREADORA/////
     
     /**
      * Creadora del Controlador de Dominio de Barrios.
      */
     private CtrlDomBarrios(){
         //TablaBarrios = new TreeMap();
         DOMElem = CtrlDomElementos.getInstance();
         DOMElem.inicializar();
         DOMRest = CtrlDomRestricciones.getInstance();
         DOMRest.inicializar();
         GDPElem = stubbedElementosGDP.getInstance();
         GDPRest = stubbedRestriccionesGDP.getInstance();
         GDPBarr = stubbedBarriosGDP.getInstance();
         TablaBarrios = GDPBarr.leerBarriosCreados();
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
     
     
     /////PUBLICAS/////
     
     /**
      * Crea un barrio con el Nombre que le llega como parametro nombre y el tipo
      * de barrio tip, que le llega como parametro.
      * @param nombre Nombre que se quiere poner al nuevo barrio que se crea.
      * @param tip Tipo de Barrio que tendra el nuevo barrio que se crea.
      * @return Retorna si ha sido posible crear el barrio, cierto si no existe
      * un barrio con ese nombre todavia y falso si ese barrio existe.
      */
     public boolean crearBarrio(String nombre, int tip, int modo) throws Exception{
         if(TablaBarrios.containsKey(nombre)){
             throw new Exception("\nYa existe un barrio con este nombre.\n");
         }
         if(tip<0 || tip>3){
             throw new Exception("\nTipo de barrio incorrecto.\n");
         }
         B = new Barrio();
         CjtElem = new Cjt_Edificios();
         CjtRest = new TreeMap();
         CjtRestUbic1 = new HashMap();
         CjtRestDemog = new HashMap();
         RestEcon = null;
         Mapa = new Plano();
         copia = new Plano();
         B.setNombreBarrio(nombre);
         B.setTipoBarrio(tip);
         B.setModo(modo);
         if(modo == 0){
         ArrayList<String> res = DOMRest.listarRestGenerales();
             for(int i=0; i<res.size(); ++i){
                 try{
                     anadirRestBarrio(res.get(i));
                 }catch(Exception e){
 
                 }
             }
         }
         return true;
     }
     
     
     /**
      * Carga un barrio previamente creado con el Nombre que le llega como 
      * parametro nombre.
      * @param nombre Nombre del barrio que se quiere cargar.
      * @return Retorna si ha sido posible cargar el barrio, cierto si existe el 
      * barrio con ese nombre y falso si ese barrio no existe.
      */
     public int cargarBarrio(String nombre)throws Exception{
         if(!TablaBarrios.containsKey(nombre)){
             throw new Exception("\nEl barrio que solicita no existe\n");
         }
         Barrio aux = GDPBarr.leerBarrio(nombre);
         int modo = aux.getModo();
         B = new Barrio();
         CjtElem = new Cjt_Edificios();
         CjtRest = new TreeMap();
         CjtRestUbic1 = new HashMap();
         CjtRestDemog = new HashMap();
         RestEcon = null;
         Mapa = new Plano();
         copia = new Plano();
         B = aux;
         Mapa = GDPBarr.leerMapa(nombre);
         copia = GDPBarr.leerCopiaMapa(nombre);
         if(modo == 0){
             CjtRest = GDPBarr.leerCjtRest(nombre);
             transRestBarrio();
         }
         CjtElem = GDPBarr.leerCjtElem(nombre);
         return modo;
         
     }
     
     
     /**
      * Guarda el barrio sobre el cual se estaba trabajando, ya sea uno cargado
      * previamente o uno nuevo creado.
      */
     public void guardarBarrio(){
         String nombre = B.getNombreBarrio();
         if(!TablaBarrios.containsKey(nombre))
                 TablaBarrios.put(nombre,(Integer) 1);
         GDPBarr.escribirBarrio(nombre,B);
         GDPBarr.escribirMapa(nombre,Mapa);
         GDPBarr.escribirCopiaMapa(nombre,copia);
         GDPBarr.escribirCjtRest(nombre,CjtRest);
         GDPBarr.escribirCjtElem(nombre,CjtElem);
     }
     
     
     public void eliminarBarrio(String nomBar) throws Exception{
         if(!GDPBarr.eliminarBarrio(nomBar)){
             throw new Exception("\nNo existe el barrio\n");
         }
         TablaBarrios.remove(nomBar);
     }
     
     
     /**
      * Funcion que devuelve un ArrayList con todos los nombres de los Barrios
      * ya creados.
      */
     public ArrayList<String> listarBarrios(){
         ArrayList<String> nom = new ArrayList();
         nom.addAll(TablaBarrios.keySet());
         return nom;
     }
     
     
     /**
      * 
      * Funcion encargada de generar el barrio actual con sus restricciones y elementos
      * @return True si el barrio se ha generado correctamente, False en caso contrario
      * @throws Exception 
      */
     public void generarBarrio(boolean comp)throws Exception{
         
         
         ArrayList<Integer> idElem = new ArrayList<>();
         idElem.addAll(CjtElem.keySet());
         for(int i=0;i<CjtElem.size();++i){
             int iterator = (int)CjtElem.consultar_elemento(idElem.get(i)).getFirst();
             for(int j=0;j<iterator-1;++j){
                 idElem.add(idElem.get(i));
             }
             
         }
         Pair[] lastVisited = new Pair[idElem.size()];
         int[] EstaVisitado = new int[idElem.size()];
         Pair[] coordenadas = new Pair[idElem.size()];
         for(int i=0;i<idElem.size();++i){
             Pair p = new Pair(0,0);
             lastVisited[i]=p;
             EstaVisitado[i]=0;
             Elemento e = (Elemento) CjtElem.consultar_elemento(idElem.get(i)).getSecond();
             int tipoel = TipoElemento(e);
             switch(tipoel){
                     case 1: 
                         Vivienda v = (Vivienda) e;
                         coordenadas[i] = new Pair(v.getTamanoX(),v.getTamanoY());
                         break;
                     case 2: 
                         Publico p2 = (Publico) e;
                         coordenadas[i] = new Pair(p2.getTamanoX(),p2.getTamanoY());
                         break;
                     case 3: 
                         Comercio c = (Comercio) e;
                         coordenadas[i] = new Pair(c.getTamanoX(),c.getTamanoY());
                         break;
             }
         }
         try{    
             comprobarRestPresServ(comp);
         }catch(Exception e){
             throw new Exception(e.getMessage());
         }
         Plano seguro = new Plano(Mapa);
         Mapa = new Plano(copia);
         controlait = 0;
         boolean back=false;
         try{
             back = backtracking(0,coordenadas,idElem,lastVisited,EstaVisitado,CjtRestUbic1,Mapa);
         }catch(Exception e){
             Mapa = seguro;
             throw new Exception(e.getMessage());
         }
         if(!back){
             Mapa = seguro;
             throw new Exception("\n No se ha podido generar el Barrio con este"
                     + "Conjunto de Edificios y Restricciones.\n");
         }
         
         
         
     }
     
     private void comprobarRestPresServ(boolean comp) throws Exception{
         int Poblac = B.getViviendo();
         if(B.getPoblacion()!= 0)
             Poblac = B.getPoblacion();
         
         //COMPROBACION DEL ESPACIO DEL BARRIO//
         if((Mapa.tama()*Mapa.tamb()) < areaCjtElem()){
             throw new Exception("\nEl area del Plano es demasiado pequena para "
                     + "el conjunto de elementos que quiere colocar.\n");
         }
         
         //COMPROBACION DEL PRESUPUESTO DEL BARRIO//
         if(B.getPresupuesto() != 0 && B.getPresupuesto() < B.getGastado()){
             throw new Exception("\nEl presupuesto del Barrio no se ajusta al "
                     + "presupuesto gastado actualmente.\n");
         }
         
         //COMPROBACION DE LA RESTRICCION ECONOMICA//
         if(RestEcon!=null){
             if(RestEcon.consultar_saldo()<B.getGastado()){
                 throw new Exception("\nEl presupuesto total de la Restriccion "
                         + "Economica del Barrio no se ajusta al presupuesto "
                         + "gastado actualmente.\n");
             }
             if(RestEcon.consultar_saldo_ind(1)<B.getGastadoViv()){
                 throw new Exception("\nEl presupuesto en Viviendas de la Restriccion "
                         + "Economica del Barrio no se ajusta al presupuesto "
                         + "gastado en Viviendas actualmente.\n");
             }
             if(RestEcon.consultar_saldo_ind(2)<B.getGastadoPub()){
                 throw new Exception("\nEl presupuesto en Servicios Publicos de la Restriccion "
                         + "Economica del Barrio no se ajusta al presupuesto "
                         + "gastado en Servicios Publicos actualmente.\n");
             }
             if(RestEcon.consultar_saldo_ind(0)<B.getGastadoCom()){
                 throw new Exception("\nEl presupuesto en Comercios de la Restriccion "
                         + "Economica del Barrio no se ajusta al presupuesto "
                         + "gastado en Comercios actualmente.\n");
             }
         }
         
         //COMPROBACION DE QUE HAYA SUFICIENTES VIVIENDAS//
         if(B.getPoblacion()!=0 && B.getPoblacion()>B.getViviendo()){
             throw new Exception("\nNo hay suficientes Viviendas para la poblacion "
                     + "actual del Barrio\n");
             
         }
         
         //COMPROBACION DE SERVICIOS PUBLICOS y COMERCIALES//
         if(comp){
             if(B.getCapSanidadCom()<Poblac){
                 throw new Exception("\nNo hay suficientes Servicios Sanitarios en el "
                         + "Barrio para la poblacion actual\n");
             }
             if(B.getCapEducacion()<Poblac){
                 throw new Exception("\nNo hay suficientes Servicios Educativos en el "
                         + "Barrio para la poblacion actual\n");
             }
             if(B.getCapSeguridad()<Poblac){
                 throw new Exception("\nNo hay suficientes Servicios de Seguridad en el "
                         + "Barrio para la poblacion actual\n");
             }
             if(B.getCapComunicacion()<Poblac){
                 throw new Exception("\nNo hay suficientes Servicios Comunicativos en el "
                         + "Barrio para la poblacion actual\n");
             }
             if(B.getCapOcio()<Poblac){
                 throw new Exception("\nNo hay suficientes Servicios de Ocio en el "
                         + "Barrio para la poblacion actual\n");
             }
             if(B.getCapacidad_comercio()<Poblac){
                 throw new Exception("\nNo hay suficientes Servicios Comerciales en el "
                         + "Barrio para la poblacion actual\n");
             }
         }
         
     }
     
     
     private int areaCjtElem(){
         ArrayList<Pair<Integer,Elemento>> aux = new ArrayList();
         aux.addAll(CjtElem.values());
         Elemento e;
         int suma = 0;
         for(int i=0; i<aux.size(); ++i){
             e = aux.get(i).getSecond();
             int x,y;
             if(e instanceof Vivienda){
                 Vivienda v = (Vivienda) e;
                 x = v.getTamanoX();
                 y = v.getTamanoY();
             }
             else if(e instanceof Publico){
                 Publico p = (Publico) e;
                 x = p.getTamanoX();
                 y = p.getTamanoY();
             }
             else{
                 Comercio c = (Comercio) e;
                 x = c.getTamanoX();
                 y = c.getTamanoY();
             }
             suma = suma + x*y*aux.get(i).getFirst();
         }
         return suma;
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
             copia = new Plano(n,m);
             return true;
         }
         else return false;
     }
     
     /**
      * Retorna la matriz del mapa para ser printada por pantalla
      * @return Mapa en formato int
      * @throws Exception 
      */
     public Integer[][] vistaMapa() {
         
         Integer [][] mapa = new Integer[Mapa.tama()][Mapa.tamb()];
         
         for(int i=0;i<Mapa.tama();++i){
             for(int j=0;j<Mapa.tamb();++j){
                 mapa[i][j] = Mapa.pos(i, j).getoid();
             }
         }
         
         return mapa;
         
     }
     
     
     /**
      * Añade la Restriccion con el nombre Rest, que le llega como parametro, al
      * Conjunto de Restricciones del barrio sobre el que se trabaja.
      * @param Rest Nombre de la Restriccion que se quiere añadir.
      * @return Retorna si se ha podido añadir la Restriccion deseada, cierto si
      * esa Restriccion existe y falso si esa Restriccion no existe.
      */
    public boolean anadirRestBarrio(String Rest) throws Exception{
         Restriccion r = DOMRest.getRestriccion(Rest);
         if(r == null){
             throw new Exception("\nLa restriccion no existe\n");
         }
         boolean b;
         b = putRestriccion(r);
         if(b) CjtRest.put(Rest,r);
         return b;
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
             Restriccion r = CjtRest.get(Rest);
             removeRestriccion(r);
             CjtRest.remove(Rest);
             return true;
         }
         else return false;
     }
     
     
     public void draguearElemBarrio(String Elem,int posX, int posY) throws Exception{
         if (posX < 0 || posX >= Mapa.tama() || posY < 0 || posY >= Mapa.tamb()){
             throw new Exception("\nLa posicion no es correcta.\n");
         } 
         Elemento e = DOMElem.getElemento(Elem);
         int tamX,tamY;
         if(e instanceof Vivienda){
             Vivienda v = (Vivienda) e;
             tamX = v.getTamanoX();
             tamY = v.getTamanoY();
         }
         else if(e instanceof Publico){
             Publico p = (Publico) e;
             tamX = p.getTamanoX();
             tamY = p.getTamanoY();
         }
         else{
             Comercio c = (Comercio) e;
             tamX = c.getTamanoX();
             tamY = c.getTamanoY();
         }
         for(int i=posX; i<posX+tamX; ++i){
             for(int j=posY; j<posY+tamY; ++j){
                 if (i < 0 || i >= Mapa.tama() || j < 0 || j >= Mapa.tamb()){
                     throw new Exception("\nEl elemento no cabe en esa posicion.\n");
                 }
                 Parcela aux = Mapa.pos(i, j);
                 if (aux.getoid() != 0){
                     throw new Exception("\nYa hay un Elemento o carretera en esa ubicacion.\n");
                 }
             }
         }
         Mapa.expande(posX,posY,tamX,tamY,(int)e.getId(), null, true);
         anadirElemBarrio(Elem, 1);
         Pair<Integer,Integer> pos = new Pair(posX,posY);
         ArrayList<Pair<Integer,Integer>> posiciones = B.getPosiciones();
         posiciones.add(pos);
         B.setPosiciones(posiciones);
     }
     
     
     public void takearElemBarrio(int posX, int posY) throws Exception{
         if (posX < 0 || posX >= Mapa.tama() || posY < 0 || posY >= Mapa.tamb()){
             throw new Exception("\nLa posicion no es correcta.\n");
         }
         boolean b=false;
         ArrayList<Pair<Integer,Integer>> posiciones = B.getPosiciones();
         for(int i=0; i<posiciones.size() && b==false; ++i){
             if(posX == posiciones.get(i).getFirst() &&
                posY == posiciones.get(i).getSecond()){
                b = true;
                posiciones.remove(i);
             }
         }
         if(!b){
             throw new Exception("\nEn esta posicion no hay ningun elemento.\n"
                     + "nota: Ha de seleccionar la esquina superior izquierda del elemento\n");
         }
         B.setPosiciones(posiciones);
        Parcela aux = Mapa.pos(posY, posY);
         Pair<Integer,Elemento> par = (Pair) CjtElem.get(aux.getoid());
         Elemento e = par.getSecond();
         String Elem = e.getNom();
         int tamX,tamY;
         if(e instanceof Vivienda){
             Vivienda v = (Vivienda) e;
             tamX = v.getTamanoX();
             tamY = v.getTamanoY();
         }
         else if(e instanceof Publico){
             Publico p = (Publico) e;
             tamX = p.getTamanoX();
             tamY = p.getTamanoY();
         }
         else{
             Comercio c = (Comercio) e;
             tamX = c.getTamanoX();
             tamY = c.getTamanoY();
         }
         quitarElemento(Elem, 1);
         Mapa.expande(posX,posY,tamX,tamY,0,null,false);
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
     public boolean anadirElemBarrio(String Elem, int cant) throws Exception{
         int tipo = B.getTipoBarrio();
         boolean b = false;
         Elemento e;
         int tipoel;
         e = DOMElem.getElemento(Elem);
         if(e == null){
             throw new Exception("\nEl solicitado edificio no existe\n");
         }
         tipoel = DOMElem.getTBElemento(e);
         switch(tipo){
                 case 0: 
                     b = true;
                     break;
                 case 1: 
                     b = (tipoel==1 || tipoel==0);
                     break;
                 case 2: 
                     b = (tipoel==2 || tipoel==0);
                     break;
                 case 3: 
                     b = (tipoel==3 || tipoel==0);
                     break;
         }
         if(b==false){
             throw new Exception("\nEste edificio no puede colocarse en este tipo"
                     + "de barrios\n");
         }
         if(CjtRestDemog.containsKey(e.getId())){
             Restriccion_demografica dem = CjtRestDemog.get(e.getId());
             if((B.getPoblacion()!= 0 && B.getPoblacion()<dem.consultar_habitantes())
                 || (B.getPoblacion()==0 && B.getViviendo()<dem.consultar_habitantes())){
                 throw new Exception("\nNo tiene el minimo de poblacion para colocar este"
                         + " edificio, consulte sus Restricciones Demograficas\n");
             }
         }
         guardarElemento(e,cant);
         return b;
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
         if (e == null) return false;
         int oid = e.getId();
         if(CjtElem.containsKey(oid)){
             Pair valor = (Pair) CjtElem.get(oid);
             int cantreal = (int) valor.getFirst();
             if (cantreal<cant) cant = cantreal;
             int gasto;
             if(e instanceof Vivienda){
                 Vivienda e2 = (Vivienda) e;
                 gasto = cant * e2.getPrecio();
                 B.anadirHabitantes(-(e2.Getcap_max()*cant));
                 B.anadirGastoViv((-gasto));
             }
             else if(e instanceof Publico){
                 Publico e2 = (Publico) e;
                 gasto = cant * e2.getPrecio();
                 B.anadirGastoPub((-gasto));
                 int cap = (e2.Getcapacidad_serv()*cant);
                 switch(e2.Gettipo()){
                 case 1: B.anadirSanidad((-cap));
                         break;
                 case 2: B.anadirEducacion((-cap));
                         break;
                 case 3: B.anadirSeguridad((-cap));
                         break;
                 case 4: B.anadirComunicacion((-cap));
                         break;
                 case 5: B.anadirOcio((-cap));
                         break;
                 }
             }
             else {
                 Comercio e2 = (Comercio) e;
                 gasto = cant * e2.getPrecio();
                 B.anadirGastoCom(-(gasto));
                 B.anadirComercio(-(e2.getCapacidad()*cant));
             }
             B.anadirGasto(-gasto);
             removeElemento(oid,cant);
             
             return true;           
         }
         else return false;
     }
     
     
     /**
      * Listar los Elementos del Conjunto de Elementos del Barrio. 
      * @return Retorna una matriz de Strings con los parametros de los 
      * Elementos del Conjunto.
      */
     public String[][] listarCjtElemBarrio(){
         ArrayList<Pair<Integer,Elemento>> elem = new ArrayList();
         elem.addAll(CjtElem.values());
         String[][] mat = null;
         if(!elem.isEmpty()) mat = new String[elem.size()][10];
         Pair<Integer,Elemento> val;
         int cant;
         Elemento e;
         Vivienda v;
         Publico p;
         Comercio c;
 
         for(int i=0; i<elem.size(); ++i){
             val = elem.get(i);
             cant = val.getFirst();
             e = val.getSecond();
             if(e instanceof Vivienda){    
                 v = (Vivienda) e;
                 mat[i][0] = "v";
                 mat[i][1] = String.valueOf(cant);
                 mat[i][2] = String.valueOf(v.getId());
                 mat[i][3] = v.getNom();
                 mat[i][4] = String.valueOf(v.getTBarrio());
                 mat[i][5] = String.valueOf(v.getPrecio());
                 mat[i][6] = String.valueOf(v.Getcap_max());
                 mat[i][7] = String.valueOf(v.getTamanoX());
                 mat[i][8] = String.valueOf(v.getTamanoY());
                 mat[i][9] = "-1";
             }
 
             else if(e instanceof Publico){
                 p = (Publico) e;
                 mat[i][0] = "p";
                 mat[i][1] = String.valueOf(cant);
                 mat[i][2] = String.valueOf(p.getId());
                 mat[i][3] = p.getNom();
                 mat[i][4] = String.valueOf(p.getTBarrio());
                 mat[i][5] = String.valueOf(p.getPrecio());
                 mat[i][6] = String.valueOf(p.Gettipo());
                 mat[i][7] = String.valueOf(p.Getcapacidad_serv());
                 mat[i][8] = String.valueOf(p.getTamanoX());
                 mat[i][9] = String.valueOf(p.getTamanoY());
             }
 
             else {
                 c = (Comercio) e;
                 mat[i][0] = "c";
                 mat[i][1] = String.valueOf(cant);
                 mat[i][2] = String.valueOf(c.getId());
                 mat[i][3] = c.getNom();
                 mat[i][4] = String.valueOf(c.getTBarrio());
                 mat[i][5] = String.valueOf(c.getPrecio());
                 mat[i][6] = String.valueOf(c.getCapacidad());
                 mat[i][7] = String.valueOf(c.getTamanoX());
                 mat[i][8] = String.valueOf(c.getTamanoY());
                 mat[i][9] = "-1";
             }
            
         }
         return mat;
     }
     
     
     /**
      * Listar Restricciones del Conjunto de Restricciones del Barrio.
      * @return Retorna una matriz de Strings con los parametros de las
      * Restricciones del Conjunto.
      */
     public String[][] listarCjtRestBarrio(){
         ArrayList<Restriccion> rest = new ArrayList();
         rest.addAll(CjtRest.values());
         String[][] mat = null;
         if(!rest.isEmpty()) mat = new String[rest.size()][6];
         Restriccion r;
         Restriccion_ubicacion u;
         Restriccion_economica e;
         Restriccion_demografica d;
          
         for(int i=0; i<rest.size(); ++i){
             r = rest.get(i);
             if(r instanceof Restriccion_ubicacion){
                 u = (Restriccion_ubicacion) r;
                 mat[i][0] = "u";
                 mat[i][1] = u.getId();
                 mat[i][2] = String.valueOf(DOMElem.NombreElemento(u.consultar_OID1()));
                 mat[i][3] = String.valueOf(DOMElem.NombreElemento(u.consultar_OID2()));
                 mat[i][4] = String.valueOf(u.consultar_distancia());
                 mat[i][5] = "-1";
             }
 
             else if(r instanceof Restriccion_economica){
                 e = (Restriccion_economica) r;
                 mat[i][0] = "e";
                 mat[i][1] = e.getId();
                 mat[i][2] = String.valueOf(e.consultar_saldo());
                 mat[i][3] = String.valueOf(e.consultar_saldo_ind(1));
                 mat[i][4] = String.valueOf(e.consultar_saldo_ind(2));
                 mat[i][5] = String.valueOf(e.consultar_saldo_ind(0));
             }
 
             else{
                 d = (Restriccion_demografica) r;
                 mat[i][0] = "d";
                 mat[i][1] = d.getId();
                 mat[i][2] = String.valueOf(DOMElem.NombreElemento(d.consultar_OID()));
                 mat[i][3] = String.valueOf(d.consultar_habitantes());
                 mat[i][4] = "-1";
                 mat[i][5] = "-1";
             }
             
         }
         return mat;
     }
     
     /**
      * Inserta una carretera en la posicion x,y del Plano del barrio 
      * @param x Fila del mapa donde se insertara la carretera
      * @param y Columna del mapa donde se nsertara la carretera
      */
     public boolean insertarCarretera(int x,int y) throws Exception {
         
         if (x < 0 || x >= Mapa.tama() || y < 0 || y >= Mapa.tamb()){
             throw new Exception("\nLa posicion no es correcta.\n");
         }
         if(Mapa.pos(x, y).getoid()!=0){
             throw new Exception("\nLa posicion esta ocupada.\n");
         }
         Mapa.pos(x, y).modificarPar(-1, 0);
         copia.pos(x, y).modificarPar(-1, 0);
         return true;
     }
     
     
     public void setPresupuestoBarrio(int pres) throws Exception{
         if(pres < 0){
             throw new Exception("\nEl presupuesto ha de ser mayor o igual a 0\n");
         }
         if(pres < B.getGastado() && pres!=0){
             throw new Exception("\nEl presupuesto ha de ser mayor o igual al"
                     + "gastado actualmente\n");
         }
         B.setPresupuesto(pres);
     }
     
     
     public void setPoblacionbarrio(int pob)throws Exception{
         if(pob < 0){
             throw new Exception("\nLa poblacion ha de ser mayor o igual a 0\n");
         }
         B.setPoblacion(pob);
     }
     
     
     /**
      * Consultora del Nombre del Barrio sobre el que se trabaja.
      * @return Retorna el Nombre del Barrio sobre el que trabaja.
      */
     public String getNombreBarrio(){
         return B.getNombreBarrio();
     }
     
     
     /**
      * Consultora del Tipo de Barrio del Barrio sobre el que se trabaja.
      * @return Retorna el Tipo de Barrio del Barrio sobre el que se trabaja.
      */
     public int getTipoBarrio(){
         return B.getTipoBarrio();
     }
     
     
     /**
      * Consultora del Presupuesto del Barrio sobre el que se trabaja.
      * @return Retorna el Presupuesto del Barrio sobre el que se trabaja.
      */
     public int getPresupuestoBarrio(){
         return B.getPresupuesto();
     }
     
     
     /**
      * Consultora de la Poblacion del Barrio sobre el que se trabaja.
      * @return Retorna la Poblacion del Barrio sobre el que se trabaja.
      */
     public int getPoblacionBarrio(){
         return B.getPoblacion();
     }
     
     
     /**
      * Consultora del dinero gastado del Barrio sobre el que se trabaja.
      * @return Retorna el dinero que se ha gastado del Barrio sobre el que se
      * trabaja
      */
     public int getGastadoBarrio(){
         return B.getGastado();
     }
     
     
     /**
      * Consultora de la poblacion que puede albergar el Barrio sobre el que se
      * trabaja.
      * @return Retorna el numero de habitantes que puede albergar el barrio 
      * sobre el que se trabaja.
      */
     public int getViviendoBarrio(){
         return B.getViviendo();
     }
     
     
     /**
      * Consultora de capacidad de comercio del Barrio sobre el que se trabaja.
      * @return Retorna la cantidad de habitantes que pueden atender el total de 
      * comercios disponibles en el Barrio.
      */
     public int getComercioBarrio(){
         return B.getCapacidad_comercio();
     }
     
     
     
     public String[] getInfoBarrio(){
         String[] info = new String[15];
         info[0] = B.getNombreBarrio();
         info[1] = String.valueOf(B.getTipoBarrio());
         info[2] = String.valueOf(B.getPresupuesto());
         info[3] = String.valueOf(B.getPoblacion());
         info[4] = String.valueOf(B.getGastado());
         info[5] = String.valueOf(B.getGastadoViv());
         info[6] = String.valueOf(B.getGastadoPub());
         info[7] = String.valueOf(B.getGastadoCom());
         info[8] = String.valueOf(B.getViviendo());
         info[9] = String.valueOf(B.getCapSanidadCom());
         info[10] = String.valueOf(B.getCapEducacion());
         info[11] = String.valueOf(B.getCapSeguridad());
         info[12] = String.valueOf(B.getCapComunicacion());
         info[13] = String.valueOf(B.getCapOcio());
         info[14] = String.valueOf(B.getCapacidad_comercio());
         return info;
     }    
     
     
     
     
     /////PRIVADAS/////
     
     
     /**
      * Funcion privada que reparte la informacion del CjtRest previamente
      * cargado del disco de un barrio ya creado, entre las estructuras de 
      * Restricciones utilizadas en el Controlador.
      */
     private void transRestBarrio()throws Exception{
         ArrayList<Restriccion> aux = new ArrayList();
         aux.addAll(CjtRest.values());
         Restriccion r;
         for (int i=0; i<aux.size(); ++i){
             r = aux.get(i);
             putRestriccion(r);
         }
     }
     
     
     /**
      * Añande la Restriccion r, que le llega como parametro, a las estructuras
      * de restricciones utilizadas por el Controlador.
      * @param r Restriccion que se quiere añadir a las estructuras.
      */
     private boolean putRestriccion(Restriccion r) throws Exception{
         boolean b = true;
         ArrayList<Restriccion_ubicacion> aux2;
         if(r instanceof Restriccion_ubicacion){
             Restriccion_ubicacion r2 = (Restriccion_ubicacion) r;
             int oid1 = r2.consultar_OID1();
             int oid2 = r2.consultar_OID2();
             Restriccion_ubicacion r3 = new Restriccion_ubicacion(r2.getId(),
                          r2.getTypeSU(),oid2,oid1,r2.consultar_distancia());
             
             if(CjtRestUbic1.containsKey(oid1))
                 aux2 = CjtRestUbic1.get(oid1);
             else
                 aux2 = new ArrayList();
             aux2.add(r2);
             CjtRestUbic1.put((Integer) oid1, aux2);
 
             if(CjtRestUbic1.containsKey(oid2))
                 aux2 = CjtRestUbic1.get(oid2);
             else
                 aux2 = new ArrayList();
             aux2.add(r3);
             CjtRestUbic1.put((Integer) oid2, aux2);
         }
         else if(r instanceof Restriccion_demografica){
             Restriccion_demografica r2 = (Restriccion_demografica) r;
             CjtRestDemog.put(r2.consultar_OID(), r2);
         }
         else if(r instanceof Restriccion_economica){
             Restriccion_economica aux = (Restriccion_economica) r;
             if(aux.consultar_saldo() <  B.getGastado()){
                 throw new Exception("\nEl presupuesto total de la restriccion"
                         + " economica ha de ser mayor o igual al presupuesto gastado "
                         + "actual\n");
             }
             if(aux.consultar_saldo_ind(1) <  B.getGastadoViv()){
                 throw new Exception("\nEl presupuesto en vivienda de la restriccion"
                         + " economica ha de ser mayor o igual al presupuesto gastado "
                         + "en viviendas actual\n");
             }
             if(aux.consultar_saldo_ind(2) <  B.getGastadoPub()){
                 throw new Exception("\nEl presupuesto en servicios publicos de la restriccion"
                         + " economica ha de ser mayor o igual al presupuesto gastado "
                         + "en servicios publicos actual\n");
             }
             if(aux.consultar_saldo_ind(0) <  B.getGastadoCom()){
                 throw new Exception("\nEl presupuesto en comercios de la restriccion"
                         + " economica ha de ser mayor o igual al presupuesto gastado "
                         + "en comercios actual\n");
             }
             
             RestEcon = (Restriccion_economica) r;
             
         }
         return b;
         
     }
     
     
     /**
      * Funcion privada que elimina una determianda Restriccion de las 
      * estructuras de Restricciones del Controlador.
      * @param r Restriccion que queremos eliminar.
      */
     private void removeRestriccion(Restriccion r){
         ArrayList<Restriccion_ubicacion> aux2;
         if(r instanceof Restriccion_ubicacion){
             Restriccion_ubicacion r2 = (Restriccion_ubicacion) r;
             int oid1 = r2.consultar_OID1().intValue();
             int oid2 = r2.consultar_OID2().intValue();
                       
             aux2 = CjtRestUbic1.get(oid1);
             aux2.remove(searchRestUbic(oid2,aux2));
             CjtRestUbic1.put((Integer) oid1, aux2);
             aux2 = CjtRestUbic1.get(oid2);
             aux2.remove(searchRestUbic(oid1,aux2));
             CjtRestUbic1.put((Integer) oid2, aux2);
         }
         else if(r instanceof Restriccion_demografica){
             Restriccion_demografica r2 = (Restriccion_demografica) r;
             CjtRestDemog.remove(r2.consultar_OID());
         }
         else if(r instanceof Restriccion_economica){
             RestEcon = null;
             
         }
     }
     
     
     /**
      * Funcion privada que busca en un ArrayList que indice tiene la restriccion
      * de ubicacion con OID2 igual al oid que le proporcionan.
      * @param oid OID2 de la restriccion que buscamos.
      * @param aux ArrayList donde buscamos la Restriccion.
      * @return 
      */
     private int searchRestUbic(int oid,ArrayList<Restriccion_ubicacion> aux){
         for(int i = 0; i<aux.size(); ++i){
             if(aux.get(i).consultar_OID2()==oid){
                 return i;
             }
         }
         return 0;
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
             B.anadirHabitantes(e2.Getcap_max()*cant);
             B.anadirGastoViv(gasto);
         }
         else if(e instanceof Publico){
             Publico e2 = (Publico) e;
             gasto = cant * e2.getPrecio();
             B.anadirGastoPub(gasto);
             int cap = (e2.Getcapacidad_serv()*cant);
             switch(e2.Gettipo()){
                 case 1: B.anadirSanidad(cap);
                         break;
                 case 2: B.anadirEducacion(cap);
                         break;
                 case 3: B.anadirSeguridad(cap);
                         break;
                 case 4: B.anadirComunicacion(cap);
                         break;
                 case 5: B.anadirOcio(cap);
                         break;
             }
         }
         else {
             Comercio e2 = (Comercio) e;
             gasto = cant * e2.getPrecio();
             B.anadirComercio(e2.getCapacidad()*cant);
             B.anadirGastoCom(gasto);
         }
         B.anadirGasto(gasto);
         putElemento(oid,v);
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
      * Ajusta el inicio de recorrido de la matriz en caso de llegar al final de la fila
      * 
      * @param p La ultima posicion visitada del elemento actualmente tratado
      * @param k Cantidad de columnas que tiene la matriz mapa
      */
     
     private void ajustaInicio(Pair p,int k, int vis){
         
         if(vis==1){
             if((int)p.getSecond()< k-1){
                 p.setSecond((int)p.getSecond()+1);
             }
             else{
                 p.setSecond(0);
                 p.setFirst((int)p.getFirst()+1);
             }
         }
         
     }
     
     /**
      * 
      * Funcion que comprueba si el elemento tratado puede introducirse en el mapa o no
      *
      * @param v Id del elemento que se esta tratando
      * @param p Plano del barrio actual 
      * @param eAct Posicion en la estructura de datos del elemento que se esta tratando
      * @param lastVisited Vector de las ultimas posiciones visitadas por los elementos
      * @param res Array de restricciones de elemento que se esta tratando actualmente
      * @return
      * @throws Exception 
      */
     private Pair cabeEnMapa(Integer v,Plano p,int tamx,int tamy,int eAct,Pair lastVisited[],int EstaVisitado[],ArrayList<Restriccion_ubicacion> res){
         //System.out.println("A ver si el elemento cabe");
         //System.out.println(""+tamx+" "+ tamy);
         if(EstaVisitado[eAct]==1){
             //p.pos((int)lastVisited[v].getFirst(), (int)lastVisited[v].getSecond()).modificarPar(0, 0);
             //System.out.println("Desexpando");
             p.expande((int)lastVisited[eAct].getFirst(),(int)lastVisited[eAct].getSecond(),tamx,tamy, 0, res, false);
         }
             ajustaInicio(lastVisited[eAct],p.tamb(),EstaVisitado[eAct]);
         
         boolean check;
         for(int i=(int)lastVisited[eAct].getFirst();i<p.tama();++i){
             for(int j=(int) lastVisited[eAct].getSecond();j<p.tamb();++j){
                 //System.out.println("posicion consultada: "+i+" "+j);
                 if(!p.consultaPar(v, i, j)){
                     //System.out.println("    posicion check");
                     check = true;
                   for(int k=i;k<i+tamx && k<=p.tama() && check;++k){
                       for(int q=j;q<j+tamy && q<=p.tamb() && check;++q){
                           //System.out.println("      "+k+" "+q);
                           if(k<p.tama() && q<p.tamb()){
                             check = !(p.consultaPar(v, k, q));
                           }
                           else check = false;
                       }
                   }
                   if(check){
                     Pair ret = new Pair<Integer,Integer>(i,j);
                     lastVisited[eAct].setFirst(i);
                     lastVisited[eAct].setSecond(j);
                     EstaVisitado[eAct]=1;
                     return ret;
                   } 
                 }
             }
         }
         Pair ret = new Pair<Integer,Integer>(-1,-1);
         
         return ret;
     }
     
     /**
      * 
      * Funcion que introduce los elementos en el plano , cumpliendo las restricciones
      * 
      * @param k Indice del elemento que se esta tratando actualemnte en las estructuras
      * @param cjt Conjunto de los elementos que deben ser introducidos al barrio
      * @param lastVisited Vector que guarda las ultimas posiciones visitadas 
      * @param res Restricciones de todos los elementos disponobles en el conjunto de elementos del barrio
      * @param p Plano del barrio
      * @return True si todos los elementos han podido introducirse en el barrio, False en caso contrario
      * @throws Exception 
      */
     
     private boolean backtracking(int k,Pair[] coordenadas,ArrayList<Integer> cjt,Pair lastVisited[],int EstaVisitado[],HashMap<Integer,ArrayList<Restriccion_ubicacion>> res,Plano p)throws Exception{
         
         ++controlait;
         if(controlait > 100000000) {
             throw new Exception("\nAlcanzado tiempo limite del Backtracking\n");
         }
         //System.out.println("Backtracking iteracion " + controlait);
         if(k==cjt.size()){
             return true;
         }
         
         else if(k==-1){
             return false;
         }
         
         else {
             Integer valor = cjt.get(k);
             Pair pos;
             if(!res.containsKey(valor)) pos = cabeEnMapa(valor,p,(int)coordenadas[k].getFirst(),(int)coordenadas[k].getSecond(),k,lastVisited,EstaVisitado,null); 
             else pos = cabeEnMapa(valor,p,(int) coordenadas[k].getFirst(),(int)coordenadas[k].getSecond(),k,lastVisited,EstaVisitado,res.get(valor));//Esta funcion debe desexpandirte en caso de que tus ultimos
                                                                         // valores visitados sean difentes a 0 (puesto previamente)
             //System.out.println(""+pos.getFirst()+" "+pos.getSecond());
             while(pos.getFirst()!=-1){
                 if(controlait > 100000000) {
                     return false;
                 }
                 
                 if(!res.containsKey(valor))p.expande((int)pos.getFirst(), (int)pos.getSecond(),(int)coordenadas[k].getFirst(),(int)coordenadas[k].getSecond(), valor, null, true);
                 else p.expande((int)pos.getFirst(), (int)pos.getSecond(),(int)coordenadas[k].getFirst(),(int)coordenadas[k].getSecond(), valor, res.get(valor), true);
                 
                 boolean back =  backtracking(k+1,coordenadas,cjt,lastVisited,EstaVisitado,res,p);
                 if(!back){
                     if(!res.containsKey(valor)) pos = cabeEnMapa(valor,p,(int)coordenadas[k].getFirst(),(int)coordenadas[k].getSecond(),k,lastVisited,EstaVisitado,null); 
                     else pos = cabeEnMapa(valor,p,(int)coordenadas[k].getFirst(),(int)coordenadas[k].getSecond(),k,lastVisited,EstaVisitado,res.get(valor));
                 }
                 else return true;
             }
             
             lastVisited[k].setFirst(0);
             lastVisited[k].setSecond(0);
             EstaVisitado[k] = 0;
             return false;
             
         }
     }
 
      /**
      * Funcion privada que nos indica que tipo de Elemento es el Elemento e.
      * @param e Elemento del cual queremos conocer el tipo.
      * @return Retorna el tipo de Elemento que es e: 1-Vivienda, 2-Publico,
      * 3-Comercio.
      */
     private int TipoElemento(Elemento e){
         if(e instanceof Vivienda)
             return 1;
         else if(e instanceof Publico)
             return 2;
         else 
             return 3;
     }
 }
 
