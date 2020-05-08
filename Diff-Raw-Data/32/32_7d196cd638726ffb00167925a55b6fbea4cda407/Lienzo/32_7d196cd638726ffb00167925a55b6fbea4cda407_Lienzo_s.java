 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Graficos;
 
 import UtilMath.Vector2D;
 import java.awt.Canvas;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferStrategy;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 /**
  *
  * @author Thanar
  */
 public class Lienzo extends Canvas {
     //adoptamos la estrategia del doble buffer para que no se vean los saltos
 
     public BufferStrategy strategy;
     public static JFrame frame; //ventana sobre la que pintaremos
     public JPanel panel; // subcontenido de la ventana
     public static Map<String, Image> imagenes;
 
     public Lienzo(int width, int height) {
         //le ponemos un titulo a la ventana y la creamos
         frame = new JFrame("Last Hope");
         //le creamos un icono
         frame.setIconImage(Lienzo.cargarImagen("imagenes/mounstrillo.png"));
 
         //inicializamos el panel
         panel = (JPanel) frame.getContentPane();
         //implementamos la función de cerrar ventana
         frame.addWindowListener(new WindowAdapter() {
 
             @Override
             public void windowClosing(WindowEvent we) {
                 System.exit(0);
             }
         });
         //le decimos a la ventana k no pueda ser modificada en tamaño por el usuario
         frame.setResizable(false);
         //le decimos al frame que no queremos darle una organizacion concreta
         frame.setLayout(null);
         // le decimos que el tamaño preferido es el width y height que pusimos en la clase Juego
         panel.setPreferredSize(new Dimension(width, height));
         //le decimos al panel que no queremos darle una organizacion concreta
         panel.setLayout(null);
 
         //movemos y situamos el componente
         setBounds(0, 0, width, height);
         //añadimos lienzo a panel
         panel.add(this);
         //ignoramos el repintado del sistema operativo
         setIgnoreRepaint(true);
         //hacemos que el frame se ajuste al tamaño preferido
         frame.pack();
         //le decimos que sea visible el frame
         frame.setVisible(true);
         //al buffer strategy le decimos que utilizaremos 2 buffer
         createBufferStrategy(2);
         //inicializamos la estrategia
         strategy = this.getBufferStrategy();
 
 
     }
 
     public JPanel getPanel() {
         return panel;
     }
     //metodo que utilizaremos para abstraernos de la carga de las imagenes
 
     public static Image cargarImagen(String url) {
         Image i = null;
         if (imagenes == null) {
             imagenes = new HashMap<String, Image>();
         }
         if (imagenes.containsKey(url)) {
             i = imagenes.get(url);
         } else {
             try {
                 i = ImageIO.read((new Vector2D(0, 0)).getClass().getClassLoader().getResource(url));
                 imagenes.put(url, i);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return i;
     }
 
     public static Image cargarImagen(URL url) {
         Image i = null;
         if (imagenes == null) {
             imagenes = new HashMap<String, Image>();
         }
 
         if (imagenes.containsKey(url.getFile())) {
             i = imagenes.get(url.getFile());
         } else {
             try {
                 i = ImageIO.read(url);
                 imagenes.put(url.getFile(), i);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return i;
     }
 
     public JFrame getFrame() {
         return frame;
     }
 
     @Override
     public void paint(Graphics g) {
     }
 }
