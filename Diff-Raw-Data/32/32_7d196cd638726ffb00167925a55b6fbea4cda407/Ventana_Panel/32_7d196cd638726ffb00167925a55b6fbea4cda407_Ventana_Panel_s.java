 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Panel;
 
 import Graficos.Boton;
 import Graficos.BotonGeneral;
 import Graficos.IVentana;
 import Graficos.Lienzo;
 import Observador.ObservadorIngame;
 import UtilMath.Vector2D;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author Thanar
  */
 public class Ventana_Panel implements IVentana {
 
     private static Ventana_Panel ventanaPanel; // instancia de la ventana panel (singleton)
     private int WIDTH; // ancho de la ventana
     private int HEIGHT; // alto de la ventana
     private int x; //posicion  x de la ventana
     private int y; // posicion Y de la ventana
     private Map<String, Contenido> contenidos; // contenidos de la ventana (subpartes)
     private Contenido fondoActual; // contenido actual
     private List<Boton> pestañas; // lista de pestañas (botones)
 
     private Ventana_Panel(int WIDTH, int HEIGHT, int x, int y) {
 
         //inicializamos los atributos
         this.WIDTH = WIDTH;
         this.HEIGHT = HEIGHT;
         this.x = x;
         this.y = y;
         this.pestañas = new ArrayList();
         contenidos = new HashMap<String, Contenido>();
         // cargamos la ventana
         this.cargar();
     }
 
     public static Ventana_Panel getVentanaPanel(int WIDTH, int HEIGHT, int x, int y){
         if(ventanaPanel==null){
             ventanaPanel= new Ventana_Panel(WIDTH, HEIGHT, x, y);
         }
         return ventanaPanel;
     }
     public static Ventana_Panel getVentanaPanel(){
         return ventanaPanel;
     }
 
     public void draw(Graphics2D g) {
         fondoActual.draw(g);
 
         for (Boton p : pestañas) {
             p.draw(g);
         }
     }
 
     public void update() {
         fondoActual.update();
 
         for (Boton p : pestañas) {
             p.update();
         }
     }
 
     public void cargar() {
         try {
             //cargamos las imagenes de las pestañas
             Image img = Lienzo.cargarImagen("imagenes/torres.png");
            Image img2 = Lienzo.cargarImagen("imagenes/torres2.png");
             Image img3 = Lienzo.cargarImagen("imagenes/editor.jpg");
             Image img4 = Lienzo.cargarImagen("imagenes/editor2.png");
             Image img5 = Lienzo.cargarImagen("imagenes/habilidades.png");
             Image img6 = Lienzo.cargarImagen("imagenes/habilidades2.png");
             //creamos y añadimos las pestañas a la lista
             Boton p = new BotonGeneral(img, img2, "torres", x, y, img.getWidth(null), img.getHeight(null));
             new ObservadorIngame(p);
             Boton p2 = new BotonGeneral(img3, img4, "editor", x + (WIDTH / 3), y, img3.getWidth(null), img3.getHeight(null));
             new ObservadorIngame(p2);
             Boton p3 = new BotonGeneral(img5, img6, "trap", x + ((WIDTH / 3) * 2), y, img5.getWidth(null), img5.getHeight(null));
             new ObservadorIngame(p3);
             pestañas.add(p);
             pestañas.add(p2);
             pestañas.add(p3);
         } catch (Exception e) {
             System.out.println(e.getStackTrace());
         }
 
 
         try {
             //cargamos los contenidos de las pestañas
             Contenido c = ContenidoTorres.getContenidoTorres("imagenes/fondoPanel.png", new Vector2D(this.x, this.y));
             Contenido c2 = ContenidoEditor.getContenidoEditor("imagenes/fondoPanel.png", new Vector2D(this.x, this.y));
             Contenido c3 = ContenidoHabilidades.getContenidoHabilidades("imagenes/fondoPanel.png", new Vector2D(this.x, this.y));
             //metemos los contenidos en la lista de fondos
             contenidos.put("fondoTorres", c);
             contenidos.put("fondoEditor", c2);
             contenidos.put("fondoTraps", c3);
             //establecemos el fondo actual
             fondoActual = contenidos.get("fondoTorres");
         } catch (Exception e) {
             System.out.println(e.getStackTrace());
         }
 
     }
 
     public void cambiaFondo(String nombre) {
         fondoActual = contenidos.get(nombre);
     }
 
     public Map<String, Contenido> getFondo() {
         return contenidos;
     }
 }
