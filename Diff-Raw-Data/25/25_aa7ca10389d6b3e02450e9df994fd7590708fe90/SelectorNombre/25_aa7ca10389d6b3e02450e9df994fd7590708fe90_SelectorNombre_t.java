 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package battleships.menu;
 
 import battleships.ImageHelper;
 import battleships.effects.Slideable;
 import battleships.game.Resources;
 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.Image;
 import javax.microedition.lcdui.game.Sprite;
 
 /**
  *
  * @author edster
  */
 public class SelectorNombre 
     implements Slideable{
     public static final int POINTER_PRESSED = 0;
     public static final int POINTER_DRAGGED = 1;
     public static final int POINTER_RELEASED = 2;
     public static final int BUTTON_OK = 0;
     public static final int BUTTON_CANCEL = 1;
     public static final int VOLVER = -1;
     private final LetraItem[][] items;
     private LetraItem[] nombre;
     private MenuItem ok;
     private MenuItem cancel;
          
     private final Listener listener;
     public final int OUT_CX;
     public final int IN_CX;
     private final int middle;
     private final double scaling;
     private final int width;
     private final int height;
     private int x;
     private int y;
 
     /**
      * Constructor
      * @param capacity Amount of menu items
      * @param listener Listener for menu events
      */
     public SelectorNombre(int cornerX, int cornerY, int width, int height, Listener listener, double scaling,Resources r) {
         items = new LetraItem[3][9];
         nombre = new LetraItem[5];
         this.listener = listener;
         this.scaling = scaling;
         ok = new MenuItem(new StringMenuItem("Aceptar", r));
         cancel = new MenuItem(new StringMenuItem("Atrás", r));
         
         setNombreItem(0, new LetraItem("_", Letra.guion, r));
         setNombreItem(1, new LetraItem("_", Letra.guion, r));
         setNombreItem(2, new LetraItem("_", Letra.guion, r));
         setNombreItem(3, new LetraItem("_", Letra.guion, r));
         setNombreItem(4, new LetraItem("_", Letra.guion, r));
         nombre[0].setSelected(true);
         
         setItem(0,0, new LetraItem(Letra.obtenerLetra(Letra.A), Letra.A, r));
         setItem(0,1, new LetraItem(Letra.obtenerLetra(Letra.B), Letra.B, r));
         setItem(0,2, new LetraItem(Letra.obtenerLetra(Letra.C), Letra.C, r));
         setItem(0,3, new LetraItem(Letra.obtenerLetra(Letra.D), Letra.D, r));
         setItem(0,4, new LetraItem(Letra.obtenerLetra(Letra.E), Letra.E, r));
         setItem(0,5, new LetraItem(Letra.obtenerLetra(Letra.F), Letra.F, r));
         setItem(0,6, new LetraItem(Letra.obtenerLetra(Letra.G), Letra.G, r));
         setItem(0,7, new LetraItem(Letra.obtenerLetra(Letra.H), Letra.H, r));
         setItem(0,8, new LetraItem(Letra.obtenerLetra(Letra.I), Letra.I, r));
         setItem(1,0, new LetraItem(Letra.obtenerLetra(Letra.J), Letra.J, r));
         setItem(1,1, new LetraItem(Letra.obtenerLetra(Letra.K), Letra.K, r));
         setItem(1,2, new LetraItem(Letra.obtenerLetra(Letra.L), Letra.L, r));
         setItem(1,3, new LetraItem(Letra.obtenerLetra(Letra.M), Letra.M, r));
         setItem(1,4, new LetraItem(Letra.obtenerLetra(Letra.N), Letra.N, r));
         setItem(1,5, new LetraItem(Letra.obtenerLetra(Letra.Enie), Letra.Enie, r));
         setItem(1,6, new LetraItem(Letra.obtenerLetra(Letra.O), Letra.O, r));
         setItem(1,7, new LetraItem(Letra.obtenerLetra(Letra.P), Letra.P, r));
         setItem(1,8, new LetraItem(Letra.obtenerLetra(Letra.Q), Letra.Q, r));
         setItem(2,0, new LetraItem(Letra.obtenerLetra(Letra.R), Letra.R, r));
         setItem(2,1, new LetraItem(Letra.obtenerLetra(Letra.S), Letra.S, r));
         setItem(2,2, new LetraItem(Letra.obtenerLetra(Letra.T), Letra.T, r));
         setItem(2,3, new LetraItem(Letra.obtenerLetra(Letra.U), Letra.U, r));
         setItem(2,4, new LetraItem(Letra.obtenerLetra(Letra.V), Letra.V, r));
         setItem(2,5, new LetraItem(Letra.obtenerLetra(Letra.W), Letra.W, r));
         setItem(2,6, new LetraItem(Letra.obtenerLetra(Letra.X), Letra.X, r));
         setItem(2,7, new LetraItem(Letra.obtenerLetra(Letra.Y), Letra.U, r));
         setItem(2,8, new LetraItem(Letra.obtenerLetra(Letra.Z), Letra.Z, r));
         
       
         IN_CX = cornerX;
         OUT_CX = IN_CX - width;
         middle = calculateMiddle(width);
         this.height = cornerY + height;
         this.width = width;
         
 
         x = OUT_CX;
         y = cornerY + height / 2;
         positionItem();
         positionNombre();
         positionBotones();
     }
     
     private int calculateMiddle(int width){
         int num = width / 2;
         int largo = 0;
         for (int i = 0; i< nombre.length; i++){
             largo += nombre[i].getWidth() + 4;
         }
         largo = largo /2;
         return num - largo;
     }
     
    
     /**
      * Establece el objeto Grid en una posicion determinada
      * @param x posicion fila
      * @param y posicion columna
      * @param item objeto contenido en la posicion 
      */
     protected final void setItem(int x, int y, LetraItem item) {
         items[x][y] = item;
     }
     protected final void setNombreItem(int posicion, LetraItem item) {
         nombre[posicion] = item;
     }
     
     /**
      *Debuelve un Grid 
      * @param x posicion vertical del mapa
      * @param y posicion horizontal del mapa
      * @return un objeto Grid
      */
     protected final MenuItem getItem(int x, int y){
         return items[x][y];
     }
     
      /**
      * Render the menu items
      */
     public void paint(Graphics g) {
         // Posición vertical(x)
         for (int i = 0; i < items.length; i++){
             
             // Posición horizontal(y)
             for (int j = 0; j < items[i].length; j++){
                 items[i][j].paint(g);
             }
         }
         for (int i = 0; i < nombre.length; i++){
             nombre[i].paint(g);
         }
         ok.paint(g);
         cancel.paint(g);
     }
     
     public final void positionItem(){
         MenuItem item;
         int newx;
         int newy = y + getItem(0,0).getHeight() - 10;
         int value;
         int valuey = 0; 
         for (int i = 0; i < getHeight(); i++) {
             newx = x + 10;
             for(int j = 0; j < getWidth(); j++){                
                 item = getItem(i,j);
                 item.setPosition(newx, newy);
                 value = item.getWidth();
                 newx += 26;
                 valuey = item.getHeight();
             }            
             newy += valuey + 4;
         }
     }
     public final void positionNombre(){
         LetraItem item;
         int newx = x + middle;    
         for (int i = 0; i < nombre.length; i++) {
             item = nombre[i];
             item.setPosition(newx, y - 10);  
             newx += 17;
         }
     }
     public final void positionBotones(){
         
         ok.setPosition(x + 3, height - ok.getHeight() - 3);
         cancel.setPosition(x + width - cancel.getWidth() - 3, height - cancel.getHeight() - 3);
     }
     
     /**
      * Move view inwards
      */
     public boolean slideIn() {
         int distance = x - IN_CX;
         distance *= 0.8;
         x = IN_CX + distance;
         positionItem();
         positionNombre();
         positionBotones();
         return distance != 0;
     }
 
     /**
      * Move view outwards
      */
     public boolean slideOut() {
         int distance = x - OUT_CX;
         distance *= 0.8;
         x = OUT_CX + distance;
         positionItem();
         positionNombre();
         positionBotones();
         return distance != 0;
     }
     
     /**
      * establece el siguiente item que está seleccionado
      * @param x posición vertical
      * @param y posicion horizontal
      */
     public void selectItem(int x, int y){
         // Posición vertical(x)
         for (int i = 0; i < items.length; i++){
             
             // Posición horizontal(y)
             for (int j = 0; j < items[i].length; j++){
                 boolean selected = false;
                 if(x == i && y == j) {
                     selected  = true;
                 }
                 items[i][j].setSelected(selected);
             }
         }
     }
     
     /**
      *  devuelve el valor vertical de la selección
      * @return int
      */
     protected int getSelectedX() {
         
         // Posición vertical(x)
         for (int i = 0; i < items.length; i++){
             
             // Posición horizontal(y)
             for (int j = 0; j < items[i].length; j++){
                if(items[i][j].isSelected()){
                    return i;
                } 
             }
         }
         return -1;
     }
     
     /**
      * Devuelve el valor horizontal de la selección
      * @return int
      */
      protected int getSelectedY() {
         
         // Posición vertical(x)
         for (int i = 0; i < items.length; i++){
             
             // Posición horizontal(y)
             for (int j = 0; j < items[i].length; j++){
                if(items[i][j].isSelected()){
                    return j;
                } 
             }
         }
         return -1;
     }
      
      public void selectUp(){
          int sely = getSelectedY();
          if (sely == -1){
              sely = 0;
          }
          selectItem((Math.max(getSelectedX(), 0) - 1 + items.length) % items.length, sely);
      }
      
      public void selectDown(){
          int sely = getSelectedY();
          if (sely == -1){
              sely = 0;
          }
          selectItem((getSelectedX() + 1) % items.length, sely);
      }
      
      public void selectLeft(){
          int selx = getSelectedX();
          if (selx == -1){
              selx = 0;
          }
          selectItem(selx,(Math.max(getSelectedY(), 0) - 1 + items[selx].length)% items[selx].length);
      }
      
      public void selectRight(){
          int selx = getSelectedX();
          if (selx == -1){
              selx = 0;
          }
          selectItem( selx, (getSelectedY() + 1) % items[selx].length);
      }
      
      /**
      * Manually cause clicked event on the highlighted item
      */
      public void clickSelected(){
          listener.itemClicked(getSelectedX(),getSelectedY());
      }
      
      /**
      * Handle pointer events
      * @param type POINTER_PRESSED, POINTER_DRAGGED or POINTER_RELEASED
      * @param x
      * @param y
      */
     public void pointerEvent(int type, int x, int y) {
         for (int i = 0; i < items.length; i++) {
             for (int j = 0; j <items[i].length; j++)
             {
                 if (items[i][j].hits(x, y)) {
                     if (type == POINTER_RELEASED) {
                         listener.itemClicked(i,j);
                         items[i][j].setSelected(false);
                     }
                     else {
                         selectItem(i,j);
                     }
                     return;
                 }
             }
         }
         selectItem(0,0);
     }
     
     /**
      * Loads a sprite
      * @param fileName
      * @param lines Amount of vertical frames
      * @param scaling Scaling factor
      * @return Loaded sprite
      */
     protected Sprite loadSprite(String fileName, int lines, double scaling) {
         Image i = loadImage(fileName, lines, scaling);
         return new Sprite(i, i.getWidth(), i.getHeight() / lines);
     }
     
        protected Sprite loadSprite(Image file, int lines, double scaling) {
         return new Sprite(file, file.getWidth(), file.getHeight() / lines);
     }
 
     /**
      * Load and scale image
      * @param fileName
      * @param lines Amount of vertical frames
      * @param scaling Scaling factor
      * @return Loaded and scaled image
      */
     protected Image loadImage(String fileName, int lines, double scaling) {
         ImageHelper ih = ImageHelper.getInstance();
         Image i = ih.loadImage(fileName);
         return ImageHelper.scaleImage(i, (int) (scaling * i.getWidth() + 0.5),
                                       (int) (scaling * i.getHeight() / lines + 0.5) * lines);
     }        
    
     public int getWidth()
     {
         return items[0].length;
     }
     public int getHeight()
     {
         return items.length;
     }
      /**
      * Listener interface for menu events
      */
     public interface Listener {
 
         void itemClicked(int x, int y); 
         
         void changeState(int item);
     }
     
     public void AgregarLetra(int x, int y, Resources r){
         if (x > -1 && y >-1){
             int codigo = items[x][y].getCodigo();
             for(int i = 0; i < nombre.length; i++){
                 if (nombre[i].isSelected())
                 {
                     nombre[i] = new LetraItem(Letra.obtenerLetra(codigo),codigo, r);
                     if (i < nombre.length-1){
                         nombre[i+1].setSelected(true);
                     }
                     nombre[i].setSelected(false);
                     break;
                 }
             }
             positionNombre();
         }
     }
     
     public void borrarLetra(Resources r){
         int completed = 0;
         for(int i = 0; i < nombre.length; i++){
             if(nombre[i].getCodigo()!= Letra.guion){
                 completed++;
             }
         }
         switch (completed){
             case 5:
                 nombre[nombre.length-1] = new LetraItem(Letra.obtenerLetra(Letra.guion),Letra.guion, r);
                 nombre[nombre.length-1].setSelected(true);
                 break;
             case 0:
                 listener.changeState(VOLVER);
                 break;
            default:
                for(int i = nombre.length - 1; i > -1; i--){
                    if (nombre[i].isSelected())
                    {                
                        if(i > 0){
                            nombre[i-1] = new LetraItem(Letra.obtenerLetra(Letra.guion),Letra.guion, r);
                            nombre[i-1].setSelected(true);                
                            nombre[i].setSelected(false);
                        }
                        break;
                    }
                }
                break;
         }               
         positionNombre();
     }
     
     public int[] obtenerNombre(){
         int[] n = new int[5];
         for(int i = 0; i < nombre.length; i++){
             n[i] = nombre[i].getCodigo();
         }
         return n;
     }
     
     public boolean isNombreListo(){
         boolean done = true;
         if (nombre[0].getCodigo() == Letra.guion){
             done = false;
         }
         return done;
     }
     
 //    public void Confirmar
 }
