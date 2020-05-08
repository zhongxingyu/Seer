 package Panel;
 
 import Graficos.Boton;
 import Graficos.BotonGeneral;
 import Graficos.Lienzo;
 import Observador.ObservadorPanelEditor;
 import Personajes.Tower;
 import UtilMath.Vector2D;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Jose
  */
 public class ContenidoEditor extends Contenido {
     private static ContenidoEditor contenidoEditor; //instancia del editor (singleton)
     Image img1; //opc1 de imagen al crear el diseño de una torre
     Image img2; //opc2 de imagen al crear el diseño de una torre
     Image img3; //opc3 de imagen al crear el diseño de una torre
     private Image fondoAtributos; // imagen de los botones de los atributos de una torre
     private Image imagenTorre; // imagen que usaremos para crear la torre
     private Map<String, String> atributos; // valor de los atributos de la torre diseñada
 
     private ContenidoEditor(String url, Vector2D posicion) {
         super(url, posicion);
         try {
             this.cargar();
         } catch (Exception ex) {
             Logger.getLogger(ContenidoEditor.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     public static ContenidoEditor getContenidoEditor(String url, Vector2D posicion){
         if(contenidoEditor==null){
             contenidoEditor=new ContenidoEditor(url, posicion);
         }
         return contenidoEditor;
     }
     public static ContenidoEditor getContenidoEditor(){
         return contenidoEditor;
     }
 
     public void cargar() throws Exception {
         //cargamos la imagen de los botones de los atributos
         fondoAtributos = Lienzo.cargarImagen("imagenes/atributos.png");
         //inicializamos el map de atributos con un linkedHashMap para que guarde el orden en el que
         //introducimos los atributos
         atributos = new LinkedHashMap();
         //cargamos las imagenes
         img1 = Lienzo.cargarImagen("imagenes/torrePanel.png");
         img2 = Lienzo.cargarImagen("imagenes/torrePanel2.png");
         img3 = Lienzo.cargarImagen("imagenes/torrePanel3.png");
        Image img4 = Lienzo.cargarImagen("imagenes/crear.png");
        Image img5 = Lienzo.cargarImagen("imagenes/crearPulsado.png");
         Image img6 = Lienzo.cargarImagen("imagenes/insertador.png");
         Image img7 = Lienzo.cargarImagen("imagenes/insertador2.png");
         //inicializamos la imagen de la torre
         imagenTorre = Lienzo.cargarImagen("imagenes/torrePanel.png");
         // añadimos los botones de los atributos
         addBotonPorDefecto(fondoAtributos, "Nombre");
         addBotonPorDefecto(fondoAtributos, "Daño");
         addBotonPorDefecto(fondoAtributos, "Rango");
         addBotonPorDefecto(fondoAtributos, "Área de daño");
         addBotonPorDefecto(fondoAtributos, "Congelación");
         addBotonPorDefecto(fondoAtributos, "Fuego");
         addBotonPorDefecto(fondoAtributos, "Veneno");
         addBotonPorDefecto(fondoAtributos, "Recarga");
         addBotonPorDefecto(fondoAtributos, "Penetración");
         // añadimos los botones que seleccionan la imagen para la torre
         addBoton(img1, img1, "cambiaImagen", 20, 320);
         addBoton(img2, img2, "cambiaImagen2", 120, 320);
         addBoton(img3, img3, "cambiaImagen3", 220, 320);
         addBoton(img6, img7, "cambiaImagenPorTeclado", 20, 390);
 
         //añadimos el boton que crea botones en el contenidoTorres para que creen las torres
         Boton b = new BotonGeneral(img4, img4, "creaBotonCreador", (int) posicion.x + getImagen().getWidth(null) - img4.getWidth(null)-12, (int) posicion.y + getImagen().getHeight(null) - img4.getHeight(null)-20, img4.getWidth(null), img4.getHeight(null));
         new ObservadorPanelEditor(b);
         addBoton(b);
         //inicializamos los atributos
         atributos.put("Nombre", "-Nombre aqui-");
         atributos.put("Daño", "0");
         atributos.put("Rango", "0");
         atributos.put("Área de daño", "0");
         atributos.put("Congelación", "0");
         atributos.put("Fuego", "0");
         atributos.put("Veneno", "0");
         atributos.put("Recarga", "0");
         atributos.put("Penetración", "0");
     }
 
     @Override
     public void draw(Graphics2D g) {
         g.setColor(Color.black);
         //dibujamos el fondo
         g.drawImage(this.getImagen(), (int) posicion.x, (int) posicion.y, null);
         // dibujamos los botones por defecto
         for (Boton b : this.getBotonesPorDefecto()) {
             b.draw(g);
             g.drawString(b.getNombre(), b.getX()+10, b.getY() + 15);
         }
         //dibujamos el resto de botones
         for (Boton b : getBotones()) {
             b.draw(g);
         }
         // dibujamos los valores actuales de los atributos
         int x = 900, y = 60;
         for (String element : atributos.values()) {
             g.drawString(element, x, y);
             y += 30;
         }
     }
 
     @Override
     //calcula la posicion X para un boton por defecto
     public int calculaX() {
         int pos = (int) posicion.x + 10;
         return pos;
 
     }
 
     @Override
     //calcula la posicion Y para un boton por defecto
     public int calculaY() {
         int pos = (int) posicion.y + (this.getBotonesPorDefecto().size() * 31) + 45;
         return pos;
     }
     // crea un boton en Contenido Torres para que ese boton pueda crear torres
 
     public void creaBotonCreador() {
         try {
             
             //creamos el boton en cointenido torres para que pueda crear una torre
             Ventana_Panel.getVentanaPanel().getFondo().get("fondoTorres").addBotonPorDefecto(new BotonCreadorTorre(imagenTorre, imagenTorre, getAtributos().get("Nombre"), Ventana_Panel.getVentanaPanel().getFondo().get("fondoTorres").calculaX(), Ventana_Panel.getVentanaPanel().getFondo().get("fondoTorres").calculaY(), imagenTorre.getWidth(null), imagenTorre.getHeight(null),
                     //le pasamos la torre segun los atributos que hemos recogido en este contenido
                     new Tower(getAtributos().get("Nombre"),Float.parseFloat(getAtributos().get("Daño")),
                     Float.parseFloat(getAtributos().get("Área de daño")),
                     Integer.parseInt(getAtributos().get("Rango")),
                     Float.parseFloat(getAtributos().get("Congelación")),
                     Float.parseFloat(getAtributos().get("Penetración")),
                     Float.parseFloat(getAtributos().get("Fuego")),
                     Long.parseLong(getAtributos().get("Recarga")),
                     Float.parseFloat(getAtributos().get("Veneno")),
                     calculaCosteProduccion(),
                     Vector2D.fuera,
                     this.imagenTorre)));
         } catch (Exception ex) {
             Logger.getLogger(ContenidoEditor.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     // metodo que calcula el coste que va a suponer crear esta torre en el mapa
 
     public Map<String, Integer> calculaCosteProduccion() {
         Map<String, Integer> costeProduccion = new HashMap<String, Integer>();
 
 
         int c;
         c = Integer.parseInt(atributos.get("Daño")) * 2 + Integer.parseInt(atributos.get("Fuego")) * 2 + Integer.parseInt(atributos.get("Rango")) * 2;
         costeProduccion.put("uranio", c);
         c = Integer.parseInt(atributos.get("Daño")) * 2 + Integer.parseInt(atributos.get("Área de daño")) * 2 + Integer.parseInt(atributos.get("Rango")) * 2;
         costeProduccion.put("rodio", c);
 
         c = Integer.parseInt(atributos.get("Recarga")) * 2 + Integer.parseInt(atributos.get("Rango")) * 2;
         costeProduccion.put("grafeno", c);
 
         c = Integer.parseInt(atributos.get("Daño")) * 2 + Integer.parseInt(atributos.get("Congelación")) * 2;
         costeProduccion.put("radio", c);
 
         c = Integer.parseInt(atributos.get("Rango")) * 2 + Integer.parseInt(atributos.get("Veneno")) * 2 + Integer.parseInt(atributos.get("Penetración")) * 2;
         costeProduccion.put("cromo", c);
 
         c = Integer.parseInt(atributos.get("Daño")) * 2 + Integer.parseInt(atributos.get("Fuego")) * 2;
         costeProduccion.put("energia", c);
 
 
         return costeProduccion;
 
 
     }
 
     public void inicializaAtributo(String atributo, String nivel) {
         atributos.put(atributo, nivel);
     }
 
     public Map<String, String> getAtributos() {
         return atributos;
 
 
     }
 
     public void cambiaImagen(String cad) {
         //cargamos la imagen que tenemos en imagen torre
         imagenTorre = Lienzo.cargarImagen(cad);
     }
 
     public void cambiaImagen(Image imagen) {
         imagenTorre=imagen;
     }
 
     //addBotonPorDefecto añadirá un botón según el orden en que tienen que estar los botones por defecto
     //dependiendo del contenido en el que estemos
     @Override
     public void addBotonPorDefecto(Image up, String nombre) {
         Boton b = new Boton(up, nombre, this.calculaX(), this.calculaY(), up.getWidth(null), up.getHeight(null));
         new ObservadorPanelEditor(b);
         this.getBotonesPorDefecto().add(b);
     }
 
     //addBoton añadirá un boton en la posicion relativa pasada por argumento
     @Override
     public void addBoton(Image up, Image down, String nombre, int x, int y) throws Exception {
         Boton b = new BotonGeneral(up, down, nombre, (int) posicion.x + x, (int) posicion.y + y, up.getWidth(null), up.getHeight(null));
         new ObservadorPanelEditor(b);
         this.getBotones().add(b);
     }
 }
