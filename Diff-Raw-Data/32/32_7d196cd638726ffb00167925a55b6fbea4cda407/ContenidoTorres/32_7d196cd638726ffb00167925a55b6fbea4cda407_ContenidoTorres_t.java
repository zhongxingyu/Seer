 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Panel;
 
 import Graficos.Boton;
 import Graficos.BotonGeneral;
 import Graficos.Lienzo;
 import Observador.ObservadorPanelTorre;
 import Observador.Observador_CreadorTorre;
 import Personajes.Tower;
 import UtilMath.Vector2D;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Jose
  */
 public class ContenidoTorres extends Contenido {
 
     private static ContenidoTorres contenidoTorres; // instancia del contenido (singleton)
 
     private ContenidoTorres(String url, Vector2D posicion) {
         super(url, posicion);
         try {
 
             cargar();
         } catch (Exception ex) {
             Logger.getLogger(ContenidoTorres.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public static ContenidoTorres getContenidoTorres(String url, Vector2D posicion) {
         if (contenidoTorres == null) {
             contenidoTorres = new ContenidoTorres(url, posicion);
         }
         return contenidoTorres;
     }
 
     public void cargar() throws Exception {
         //cargamos el boton que lleva al menu
         Image img1 = Lienzo.cargarImagen("imagenes/torrePanel.png");
         Image img2 = Lienzo.cargarImagen("imagenes/torrePanel3.png");
         Image img3 = Lienzo.cargarImagen("imagenes/torrePanel4.png");
         Image img6 = Lienzo.cargarImagen("imagenes/exitGame.jpg");
         Image img7 = Lienzo.cargarImagen("imagenes/exitHoverGame.png");
         Image img8 = Lienzo.cargarImagen("imagenes/pausa.png");
         Image img9 = Lienzo.cargarImagen("imagenes/pausapulsada.png");
         Image img10 = Lienzo.cargarImagen("imagenes/play.png");
        Image img11 = Lienzo.cargarImagen("imagenes/playpulsado.png");
         addBoton(img6, img7, "Menu", getImagen().getWidth(null) - img6.getWidth(null) - 10, getImagen().getHeight(null) - img6.getHeight(null) - 20);
         addBoton(img8, img9, "Pausa", 70, 440);
         addBoton(img10, img11, "Play", 30, 440);
         Map<String, Integer> coste = new LinkedHashMap<String, Integer>();
         coste.put("uranio", 0);
         coste.put("rodio", 30);
         coste.put("grafeno", 50);
         coste.put("radio", 0);
         coste.put("cromo", 20);
         coste.put("energia", 20);
         BotonCreadorTorre b1 = new BotonCreadorTorre(img1, img1, "Torreta", this.calculaX(), this.calculaY(), img1.getWidth(null), img1.getHeight(null), new Tower("Torreta",20, 0, 100, 0, 0, 0, 7, 0, coste, Vector2D.fuera, img1));
         new Observador_CreadorTorre(b1);
         addBotonPorDefecto(b1);
         BotonCreadorTorre b2 = new BotonCreadorTorre(img2, img2, "Teminator", this.calculaX(), this.calculaY(), img1.getWidth(null), img1.getHeight(null), new Tower("Teminator",30, 10, 120, 5, 3, 2, 5, 0, coste, Vector2D.fuera, img2));
         new Observador_CreadorTorre(b2);
         addBotonPorDefecto(b2);
         BotonCreadorTorre b3 = new BotonCreadorTorre(img3, img3, "Frio y Veneno", this.calculaX(), this.calculaY(), img1.getWidth(null), img1.getHeight(null), new Tower("Frio y Veneno", 2, 15, 150, 50, 0, 0, 20, 30, coste, Vector2D.fuera, img3));
         new Observador_CreadorTorre(b3);
         addBotonPorDefecto(b3);
     }
 
     @Override
     //sobreescribimos el metodo para asegurarnos de que solo se creen 12 botones como máximo
     public void addBotonPorDefecto(Boton b) throws Exception {
         if (this.getBotonesPorDefecto().size() < 12) {
 
             this.getBotonesPorDefecto().add(b);
         }
     }
 
     @Override
     public void draw(Graphics2D g) {
         g.setColor(Color.black);
         //dibujamos el fondo
         g.drawImage(this.getImagen(), (int) posicion.x, (int) posicion.y, null);
         // dibujamos los botones por defecto (los que crean torres)
         for (Boton b : this.getBotonesPorDefecto()) {
             b.draw(g);
             g.drawString(b.getNombre(), b.getX(), b.getY() + b.getHeight() + 12);
         }
         //dibujamos el resto de botones
         for (Boton b : this.getBotones()) {
                 b.draw(g);
         }
     }
 
     @Override
     //metodo que calcula la posicion X del proximo boton por defecto
     public int calculaX() {
         int pos = 0;
         int modulo = this.getBotonesPorDefecto().size() % 3;
         if (this.getBotonesPorDefecto().size() == 0) {
             pos = (int) posicion.x + 31;
         } else {
             pos = (int) posicion.x + (modulo * 81) + 31;
         }
         return pos;
     }
 
     @Override
     //metodo que calcula la posicion Y del proximo boton por defecto
     public int calculaY() {
         int pos = 0;
         int cociente = this.getBotonesPorDefecto().size() / 3;
         if (this.getBotonesPorDefecto().size() == 0) {
             pos = (int) posicion.y + 66;
         } else {
             pos = (int) posicion.y + (cociente * 81) + 66;
         }
         return pos;
     }
     //addBoton añadirá un boton en la posicion relativa pasada por argumento
 
     @Override
     public void addBoton(Image up, Image down, String nombre, int x, int y) throws Exception {
         Boton b = new BotonGeneral(up, down, nombre, (int) posicion.x + x, (int) posicion.y + y, up.getWidth(null), up.getHeight(null));
         new ObservadorPanelTorre(b);
         this.getBotones().add(b);
     }
 }
