 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package Screens;
 
 import Graficos.Boton;
 import Graficos.BotonGeneral;
 import Graficos.Fondo;
 import Graficos.Lienzo;
 import Observador.ObservadorCredits;
 import Personajes.Actor;
 import Principal.Juego;
 import UtilMath.Vector2D;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Arcangel
  */
 public class CreditsScreen implements IScreen {
     
     Actor fondo; // el fondo del menu
     Actor creditos1;
     Actor creditos2;
     List<Boton> botones; // lista de botones del game over
 
     public CreditsScreen()
     {
     }
 
     public void cargarModelos()
     {
         botones= new ArrayList<Boton>();
         try {
             fondo = new Fondo("imagenes/FondoCreditos.png",Vector2D.zero);
             creditos1 = new Fondo("imagenes/Creditos1.png",new Vector2D(0, 600));
             creditos2 = new Fondo("imagenes/Creditos2.png",new Vector2D(0, 1200));
             cargarBotones();
         } catch (Exception ex) {
             Logger.getLogger(CreditsScreen.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
         private void cargarBotones() throws Exception{
         //cargamos las imagenes de los botones
         Image img = null;
         Image img2 = null;
         try {
                 img = Lienzo.cargarImagen("imagenes/skip.png");
                 //img2 = Lienzo.cargarImagen("imagenes/skipHover.png");
             } catch (Exception e) {
                 e.printStackTrace();
             }
         //añadimos los botones al menu
         Boton b =new BotonGeneral(img, img, "Menu", 900, 550, img.getWidth(null), img.getHeight(null));
         new ObservadorCredits(b);
         botones.add(b);
     }
 
     public void update() {
         creditos1.posicion.y=creditos1.posicion.y-2;
        if(creditos1.posicion.y<=-1200)
             creditos1.posicion.y=600;
         creditos2.posicion.y=creditos2.posicion.y-2;
        if(creditos2.posicion.y<=-600)
             creditos2.posicion.y=1200;
         for(Boton b:botones){
             b.update();
         }
     }
 
     public void draw(Graphics2D g) {
         fondo.draw(g);
         creditos1.draw(g);
         creditos2.draw(g);
         for(Boton b:botones){
             b.draw(g);
         }
     }
 
 }
