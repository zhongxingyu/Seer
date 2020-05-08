 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Vistas.Tablero;
 
 import LogicGame.Equipo;
 import java.awt.Color;
 import java.util.ArrayList;
 import javax.swing.ImageIcon;
 
 /**
  *
  * @author OMAR
  */
 public class TableroGUIHMF extends javax.swing.JPanel {
     public  int seleccionar=1;
     
     private ImageIcon cocodriloA,gozillaA,hombreA,polloA;
     private ImageIcon cocodriloB,gozillaB,hombreB,polloB;
     private ImageIcon agua, tocado, seleccion;
     private boolean tipoTablero;
     private CasillasGUIHMF [][] casillasHM ;
     
     private int filas,columnas;
     private final int anchoCasilla=70,alturaCasilla=70;
     Equipo equipoHumano,equipoMaquina;
     //Por mientras
     int Equipo=0;//Equipo A:0, Equipo B:1 Empiezan las amarillas
     boolean restriccionA=true;
     boolean restriccionB=false;
     int turno_actual=100;//turnoA=100 ,turnoB=-100
     int fila_actual,columna_actual;
     
     
     private int[][] tabla;
     public int [][] tablaPosibilidades;
     
     public int getTurno_actual() {
         return turno_actual;
     }
 
     public void setTurno_actual(int turno_actual) {
         this.turno_actual = turno_actual;
     }
     
 
     public int getFila_actual() {
         return fila_actual;
     }
 
     public void setFila_actual(int fila_actual) {
         this.fila_actual = fila_actual;
     }
 
     public int getColumna_actual() {
         return columna_actual;
     }
 
     public void setColumna_actual(int columna_actual) {
         this.columna_actual = columna_actual;
     }
     
 
     public int[][] getTabla() {
         return tabla;
     }
     
     public TableroGUIHMF() {
         initComponents();
     }
 
     public TableroGUIHMF(int filas,int columnas, boolean tipo) {
         initComponents();
         tabla=new int[5][8];//El tablero actual!!
         tablaPosibilidades=new int[5][8];//indica las posibilidaddes de movimiento
         inicializarTabla();
         actualizaMat(true);
         setLayout(new java.awt.GridLayout(filas, columnas));
         this.tipoTablero = tipo;
         cargarImagenes();
         casillasHM = new CasillasGUIHMF[filas][columnas];//Tablero de jpanels
         this.filas=filas;
         this.columnas=columnas;
         //Cargar equipos!!!!
         inicializarCampo();//inicializar las casillasHM con  imagenes
         inicializarEquipoHumano();
         inicializarEquipoMaquina();
         pintar_tablero(true);
         this.setBackground(new Color(0, 255, 153));
     }
     
     
     
     void  inicializarTabla(){
         for(int i =0;i <5;i++){
             for(int j =0;j <8;j++){
                 tabla[i][j]=0;
             }
         }
         //Equipo A:
         tabla[0][0]=2;tabla[0][1]=1;
         tabla[1][0]=3;tabla[1][1]=1;
         tabla[2][0]=4;tabla[2][1]=1;
         tabla[3][0]=3;tabla[3][1]=1;
         tabla[4][0]=2;tabla[4][1]=1;
         
         //Equipo B:
         tabla[0][6]=-1;tabla[0][7]=-2;
         tabla[1][6]=-1;tabla[1][7]=-3;
         tabla[2][6]=-1;tabla[2][7]=-4;
         tabla[3][6]=-1;tabla[3][7]=-3;
         tabla[4][6]=-1;tabla[4][7]=-2;
     }
    @Deprecated
     void pintar_tablero(boolean original){
         for(int i =0;i <5;i++){
             for(int j =0;j <8;j++){
                 if(original){
                     System.out.print(" "+tabla[i][j]+" ");
                 }else{
                     System.out.print(" "+tablaPosibilidades[i][j]+" ");
                 }
             }
             
             System.out.println(" ");
         }
     }
     
     public void inicializarCampo(){
         int x,y,valor=0;
         
         for (int i = 0; i < filas; i++){
             for (int j = 0; j < columnas; j++){
                 casillasHM[i][j] = new CasillasGUIHMF(this); 
                 casillasHM[i][j].setFondo(agua);
                 valor=tabla[i][j];
                 switch( valor){
                     case -1: casillasHM[i][j].setFondo(polloB); break;
                     case -2: casillasHM[i][j].setFondo(cocodriloB);break;
                     case -3: casillasHM[i][j].setFondo(hombreB);break;
                     case -4: casillasHM[i][j].setFondo(gozillaB);break;
                     case 1: casillasHM[i][j].setFondo(polloA);break; 
                     case 2: casillasHM[i][j].setFondo(cocodriloA);break;
                     case 3: casillasHM[i][j].setFondo(hombreA);break;
                     case 4: casillasHM[i][j].setFondo(gozillaA);break;
                     default : casillasHM[i][j].setFondo(agua);  
                     
                 }
                 y = (i * (anchoCasilla+1))+1;
                 x = (j * (alturaCasilla+1))+1;
                 casillasHM[i][j].setBounds(x, y, anchoCasilla, alturaCasilla);
                 this.add(casillasHM[i][j]);
             }
         }
     }
     public void actualizaMat(boolean copiar){
         for(int i=0;i<5;i++){
             for(int j=0;j<8;j++){
                 if (copiar==true)
                     tablaPosibilidades[i][j]=tabla[i][j];
                 else
                     tabla[i][j]=tablaPosibilidades[i][j];
             } 
         } 
     }	
     
     boolean validarTuno(){
         int fila=this.fila_actual;
         int columna=this.columna_actual;
         boolean suTurno=false;
         if((tabla[fila][columna]>0 && turno_actual==100)||(tabla[fila][columna]<0 && turno_actual==-100)){
             suTurno=true;
 
         }
         return suTurno;
     }
     public int devolverValorFichaSeleccionada(){
         System.out.println("Ficha seleccionada"+tabla[this.fila_actual][this.columna_actual]);
         return tabla[this.fila_actual][this.columna_actual];
         
     }
     /*public int devolverValorFichaSeleccionada(int f , int c){
         return tabla[f][c];
     }*/
     
     void marcarPosibilidades(int  tipoFicha,int fila,int colu){
             switch(tipoFicha){
             case  1:  posibilidad_Pollo(fila,colu,0);break;
             case  2:  posibilidad_cocodrilo(fila,colu,0);break;
             case  3:  posibilidad_humano(fila,colu,0);break;
             case  4:  posibilidad_godzilla(fila,colu,0);break;   
             case  -1: posibilidad_Pollo(fila,colu,1);break;
             case  -2: posibilidad_cocodrilo(fila,colu,1);break;
             case  -3: posibilidad_humano(fila,colu,1);break;
             case  -4: posibilidad_godzilla(fila,colu,1);break;     
         }
     }
     void  posibilidad_Pollo(int x, int y, int Equipo){
             for(int a=0;a<5;a++){
             for(int b=0;b<8;b++){
                 if(Equipo==0){
                 if((b-y==0 && Math.abs(a-x)==1)||(a-x==0 && b-y==1)){
                 if(tablaPosibilidades[a][b]<=0){
                 tablaPosibilidades[a][b]=10;
                 }
            }
         }
        if(Equipo==1){
        if((b-y==0 && Math.abs(a-x)==1)||(a-x==0 && b-y==-1)){
             if(tablaPosibilidades[a][b]>=0){
             tablaPosibilidades[a][b]=20;
                 }       
        }
        }
             
         }
        
     }
     }
     
     void  posibilidad_cocodrilo(int x,int y,int Equipo){//se  mueve  1  o dos casillasHM
         
         for(int a=0;a<5;a++){
             for(int b=0;b<8;b++){
                 double  distancia=Math.hypot(Math.abs(a-x), Math.abs(b-y));
                 System.out.println("distancia: "+distancia);
                 if(distancia==1){
                     System.out.println("(a,b)="+a+" ,"+b);
                 }
                 if((Math.abs(b-y)==0 || Math.abs(a-x)==0) && (Math.hypot(Math.abs(a-x),Math.abs(b-y))<=2)){
                     if(tablaPosibilidades[a][b]<=0 && Equipo==0){tablaPosibilidades[a][b]=10;}
                     if(tablaPosibilidades[a][b]>=0 && Equipo==1){tablaPosibilidades[a][b]=20;}
                 }
             }
         }
     }
     
     void  posibilidad_humano(int x,int y,int Equipo){
         for(int a=0;a<5;a++){
             for(int b=0;b<8;b++){
                 if((a-x)!=0){
                     if(( Math.abs((b-y)/(a-x))==1) && (Math.hypot(Math.abs(a-x),Math.abs(b-y))<=2*Math.sqrt(2))){
                         if(tablaPosibilidades[a][b]<=0 && Equipo==0){tablaPosibilidades[a][b]=10;}
                         if(tablaPosibilidades[a][b]>=0 && Equipo==1){tablaPosibilidades[a][b]=20;}
                     
                     }
                 }
             }
         }
     }
     
     void posibilidad_godzilla(int x,int y,int Equipo){
         for(int a=0;a<5;a++){
             for(int b=0;b<8;b++){
                 double  distancia=Math.hypot(Math.abs(a-x), Math.abs(b-y));
                 System.out.println("distancia: "+distancia);
                 if(distancia==1){
                     System.out.println("(a,b)="+a+" ,"+b);
                 }
                 if((Math.abs(b-y)==0 || Math.abs(a-x)==0) && (Math.hypot(Math.abs(a-x),Math.abs(b-y))<=2)){
                     if(tablaPosibilidades[a][b]<=0 && Equipo==0){
                         tablaPosibilidades[a][b]=10;
                     }
                     if(tablaPosibilidades[a][b]>=0 && Equipo==1){
                         tablaPosibilidades[a][b]=20;
                     }                    
                 }
             }
         }
         for(int a=0;a<5;a++){
             for(int b=0;b<8;b++){
                 if((a-x)!=0){
                     if(( Math.abs((b-y)/(a-x))==1) && (Math.hypot(Math.abs(a-x),Math.abs(b-y))<=2*Math.sqrt(2))){
                         if(tablaPosibilidades[a][b]<=0 && Equipo==0){tablaPosibilidades[a][b]=10;}
                         if(tablaPosibilidades[a][b]>=0 && Equipo==1){tablaPosibilidades[a][b]=20;}
                     }
                 }
                 
             }
         }
         
         
         
     }
     void dibujarPosibilidades(){
         int x,y,valor=0;
         for(int i=0;i<5;i++){
             for(int j=0;j<8;j++){
                 if (tablaPosibilidades[i][j]==10 || tablaPosibilidades[i][j] ==20){//Uso innecesario.
                     System.out.println("se tiene que  pintar");    
                     //casillasHM[i][j] = new CasillasGUIHMF(this); 
                     casillasHM[i][j].setFondo(seleccion);
                     valor=tablaPosibilidades[i][j];
                         switch( valor){
                         case -1: casillasHM[i][j].setFondo(polloB); break;
                         case -2: casillasHM[i][j].setFondo(cocodriloB);break;
                         case -3: casillasHM[i][j].setFondo(hombreB);break;
                         case -4: casillasHM[i][j].setFondo(gozillaB);break;
                         case 1: casillasHM[i][j].setFondo(polloA);break; 
                         case 2: casillasHM[i][j].setFondo(cocodriloA);break;
                         case 3: casillasHM[i][j].setFondo(hombreA);break;
                         case 4: casillasHM[i][j].setFondo(gozillaA);break;
                         case 10: casillasHM[i][j].setFondo(seleccion);break;
                         case 20:casillasHM[i][j].setFondo(seleccion);break;
                         default : casillasHM[i][j].setFondo(tocado);break;
                     
                     }
                     /*y = (i * (anchoCasilla+1))+1;
                     x = (j * (alturaCasilla+1))+1;
                     casillasHM[i][j].setBounds(x, y, anchoCasilla, alturaCasilla);
                     this.add(casillasHM[i][j]);*/
                 }
                 
                 } 
             }
         } 
         
         public boolean evaluarMovimiento(int f,int c,int Equipo){
             boolean correcto=false;
             for(int i=0;i<5;i++){
                 for(int j=0;j<8;j++){
                     if ( i==f && j==c){
                         if(tablaPosibilidades[i][j]==10 && Equipo==0){
                         System.out.println("Movimiento Correcto A");
                         correcto=true;
                         System.out.println("i:_"+i+"j_"+j+"f_"+f+"c_"+c);   
                         }
                        if(tablaPosibilidades[i][j]==20 && Equipo==1){
                         System.out.println("Movimiento Correcto B");
                         System.out.println("i:_"+i+"j_"+j+"f_"+f+"c_"+c);
                         correcto=true;
                        }
                     }
                                           
                 }
             }
             return correcto;
         }
         
         void  establecer_nueva_posicion(int x,int y , int a , int  b){//a y b fila  y columna actuales
             this.tabla[a][b]=this.tabla[x][y];
             this.tabla[x][y]=0;
             
         }
         void redibujarTablero(){
             int x,y,valor=0;
             System.out.println("se tiene que redibujar");   
             for(int i=0;i<5;i++){
                 for(int j=0;j<8;j++){
                         //casillasHM[i][j] = new CasillasGUIHMF(this);  
                         valor=tabla[i][j];
                             switch( valor){
                             case -1: casillasHM[i][j].setFondo(polloB); break;
                             case -2: casillasHM[i][j].setFondo(cocodriloB);break;
                             case -3: casillasHM[i][j].setFondo(hombreB);break;
                             case -4: casillasHM[i][j].setFondo(gozillaB);break;
                             case 1: casillasHM[i][j].setFondo(polloA);break; 
                             case 2: casillasHM[i][j].setFondo(cocodriloA);break;
                             case 3: casillasHM[i][j].setFondo(hombreA);break;
                             case 4: casillasHM[i][j].setFondo(gozillaA);break;
                             case 10: casillasHM[i][j].setFondo(seleccion);break;
                             case 20:casillasHM[i][j].setFondo(seleccion);break;
                             default : casillasHM[i][j].setFondo(agua);  
                     
                         }                            
                 } 
             }
         }
     
     
     public int   hayGanador(){
         int  ganador = 0;
         int jugadorA=  0;
         int jugadorB=  0;        
         int  valor=0;
         for(int i=0;i<5;i++){
                 for(int j=0;j<8;j++){                     
                         valor=tabla[i][j];
                         if(valor==-4 ){
                             jugadorB=valor;//gana
                         }   
                         if(valor==4){
                             jugadorA=valor;
                         }   
                 } 
          }
         ganador=jugadorA +jugadorB;
         return  ganador;
     }
     
     public void inicializarEquipoHumano(){
         //casillasHM[2][0]=new CasillasGUIHMF(this);
         //casillasHM[2][0].setFondo(equipoHumano.godzilla.getImagen());
         
     
     }
     public void inicializarEquipoMaquina(){
         
     }
     public boolean getTipoTablero(){
         return this.isTipoTablero();
     }
     
     public void pintar(int x, int y){
         this.casillasHM[x][y].setFondo(tocado);
         this.repaint();
     }
     
     private void cargarImagenes() {
         
         this.agua = this.cargarFondo("../../imagenes/fondo.jpg");
         this.tocado = this.cargarFondo("../../imagenes/tocado.jpg");
         
         this.gozillaA=this.cargarFondo("../../imagenes_jugadores/godzillaA.jpg");
         this.hombreA=this.cargarFondo("../../imagenes_jugadores/hombreA.jpg");
         this.cocodriloA=this.cargarFondo("../../imagenes_jugadores/cocodriloA.jpg");
         this.polloA=this.cargarFondo("../../imagenes_jugadores/polloA.jpg");
         
         this.gozillaB=this.cargarFondo("../../imagenes_jugadores/godzillaB.jpg");
         this.hombreB=this.cargarFondo("../../imagenes_jugadores/hombreB.jpg");
         this.cocodriloB=this.cargarFondo("../../imagenes_jugadores/cocodriloB.jpg");
         this.polloB=this.cargarFondo("../../imagenes_jugadores/polloB.jpg");
         this.seleccion=this.cargarFondo("../../imagenes_jugadores/seleccion.jpg");
         
     }
     
     protected static ImageIcon cargarFondo(String ruta) {
         java.net.URL localizacion = TableroGUIHMF.class.getResource(ruta);
         if (localizacion != null) {
             return new ImageIcon(localizacion);
         } else {
             System.out.println("No se ha encontrado el archivo: " + ruta);
             return null;
         }
     }
     
     public int[] getCoordenadas(CasillasGUIHMF casilla) {
         int [] coordenadas = new int[2];
         for (int i=0; i < filas; i++) {
             for (int j=0; j < columnas; j++) {
                 if (this.casillasHM[i][j] == casilla) {
                     coordenadas[0] = i;
                     coordenadas[1] = j;
                     System.out.println("coordenada (i: "+i+" , "+ j+ ")");
                     this.columna_actual=j;//por las puras?
                     this.fila_actual=i;
                     
                 }
             }
         }
         return coordenadas;
     }
     
     public CasillasGUIHMF[][] getCasillas() {
         return casillasHM;
     }
     
     public void setCasillas(CasillasGUIHMF[][] casillasHM) {
         this.casillasHM = casillasHM;
     }
     
     public boolean isTipoTablero() {
         return tipoTablero;
     }    
     public void setTipoTablero(boolean tipoTablero) {
         this.tipoTablero = tipoTablero;
     }
                               
     private void initComponents() {
 
         setLayout(null);
 
         setBackground(new java.awt.Color(0, 0, 0));
         setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
 
     }                      
 
     public ArrayList<Casillero> obtenerPosibilidadesPollo(int x, int y, int equipo) {
         ArrayList <Casillero> posibilidades=new ArrayList();
         Casillero posible;
         for (int a = 0; a < 5; a++) {
             for (int b = 0; b < 8; b++) {
                 if (equipo == 1) {
                     if ((b - y == 0 && Math.abs(a - x) == 1) || (a - x == 0 && b - y == -1)) {
                         if (tablaPosibilidades[a][b] >= 0&&!(a==x&&b==y)) {
                             posible=new Casillero(a,b);
                             posibilidades.add(posible);
                         }
                     }
                 }
             }
         }
         
         return posibilidades;
     }
 
     public ArrayList<Casillero> obtenerPosibilidadesCocodrilo(int x, int y, int equipo) {
         ArrayList<Casillero> posibilidades = new ArrayList();
         Casillero posible;
         if(equipo==1){
         for (int a = 0; a < 5; a++) {
             for (int b = 0; b < 8; b++) {
                 double distancia = Math.hypot(Math.abs(a - x), Math.abs(b - y));
                 if ((Math.abs(b - y) == 0 || Math.abs(a - x) == 0) && (Math.hypot(Math.abs(a - x), Math.abs(b - y)) <= 2)) {
                     if (tablaPosibilidades[a][b] >= 0&&!(a==x&&b==y)) {
                         posible=new Casillero(a,b);
                         posibilidades.add(posible);
                     }
                 }
             }
         }
         }
         return posibilidades;
     }
 
     public ArrayList obtenerPosibilidadesHumano(int x, int y, int equipo) {
         ArrayList<Casillero> posibilidades = new ArrayList();
         Casillero posible;
         if (equipo == 1) {
             for (int a = 0; a < 5; a++) {
                 for (int b = 0; b < 8; b++) {
                     if ((a - x) != 0) {
                         if ((Math.abs((b - y) / (a - x)) == 1) && (Math.hypot(Math.abs(a - x), Math.abs(b - y)) <= 2 * Math.sqrt(2))) {
                             if (tablaPosibilidades[a][b] >= 0 &&!(a==x&&b==y)) {
                                 posible = new Casillero(a, b);
                                 posibilidades.add(posible);
                             }
                         }
                     }
                 }
             }
         }
         return posibilidades;
     }
 
     public ArrayList obtenerPosibilidadesGodzilla(int x, int y, int equipo) {
             ArrayList<Casillero> posibilidades = new ArrayList();
             Casillero posible;
         if(equipo==1){
             for(int a=0;a<5;a++){
             for(int b=0;b<8;b++){
                 double  distancia=Math.hypot(Math.abs(a-x), Math.abs(b-y));
                 
                 if((Math.abs(b-y)==0 || Math.abs(a-x)==0) && (Math.hypot(Math.abs(a-x),Math.abs(b-y))<=2)){
 
                     if(tablaPosibilidades[a][b]>=0 &&!(a==x&&b==y)){
                         posible=new Casillero(a,b);
                         posibilidades.add(posible);
                     }                    
                 }
             }
         }
         for(int a=0;a<5;a++){
             for(int b=0;b<8;b++){
                 if((a-x)!=0){
                     if(( Math.abs((b-y)/(a-x))==1) && (Math.hypot(Math.abs(a-x),Math.abs(b-y))<=2*Math.sqrt(2))){
                         if(tablaPosibilidades[a][b]>=0&&!(a==x&&b==y)){
                            posible=new Casillero(a,b);
                             posibilidades.add(posible);
                         }
                     }
                 }
                 
             }
         }
         }
         return posibilidades;
     }
                      
 }
