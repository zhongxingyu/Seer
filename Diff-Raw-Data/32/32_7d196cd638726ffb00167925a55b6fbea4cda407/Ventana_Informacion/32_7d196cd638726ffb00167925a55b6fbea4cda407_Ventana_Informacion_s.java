 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Informacion;
 
 import Graficos.Boton;
 import Graficos.BotonGeneral;
 import Graficos.IVentana;
 import Graficos.Lienzo;
 import Mapa.Ventana_Mapa;
 import Personajes.Actor;
 import Personajes.CentralEnergia;
 import Personajes.Enemy;
 import Personajes.Habilidad;
 import Personajes.Tower;
 import Principal.Juego;
 import Principal.Jugador;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Image;
 
 /**
  *
  * @author Thanar
  */
 public class Ventana_Informacion implements IVentana {
 
     private int WIDTH;
     private int HEIGHT;
     private int x;
     private int y;
     private Image iUranio;
     private Image iGrafeno;
     private Image infIzq;
     private Image infCtr;
     private Image infDr;
     private Image health;
     private Image atack;
     private Image armor;
     private Image alcan;
     private Image reload;
     private Image dolar;
     private Image dolar2;
     private Image iRodio;
     private Image iRadio;
     private Image iCromo;
     private Image energy;
     Boton vender;
     Boton observa;
     public static Image brillo;
     //private Actor ac;
     //creado de prueba
     public static Actor ac = null;
 
     public Ventana_Informacion(int WIDTH, int HEIGHT, int x, int y) {
         //los parametros magicos
         this.WIDTH = WIDTH;
         this.HEIGHT = HEIGHT;
         this.x = x;
         this.y = y;
         this.cargar();
         try {
             vender = new BotonGeneral(dolar, dolar2, "sell", x + 1000, y + 83, dolar.getWidth(null), dolar.getHeight(null));
             new ObservadorVentana_Informacion(vender);
             //observa = new BotonGeneral(cOjo, cOjo, "look", x+100, y+50, cOjo.getWidth(null), cOjo.getHeight(null));
             //new ObservadorVentana_Informacion(observa);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public void cargar() {
         infIzq = Lienzo.cargarImagen("imagenes/informacionIzq.png");
         infCtr = Lienzo.cargarImagen("imagenes/informacionCtr.png");
         infDr = Lienzo.cargarImagen("imagenes/informacionDr.png");
         health = Lienzo.cargarImagen("imagenes/corazon.png");
         atack = Lienzo.cargarImagen("imagenes/ataque.png");
         energy = Lienzo.cargarImagen("imagenes/energia.png");
         alcan=  Lienzo.cargarImagen("imagenes/alcance.png");
         armor = Lienzo.cargarImagen("imagenes/armadura.png");
         reload =Lienzo.cargarImagen("imagenes/recarga.png");
         brillo = Lienzo.cargarImagen("imagenes/brillo.png");
         dolar = Lienzo.cargarImagen("imagenes/Sell.png");
         dolar2 = Lienzo.cargarImagen("imagenes/Sell2.png");
         iUranio=Lienzo.cargarImagen("imagenes/Uranio.png");
         iGrafeno=Lienzo.cargarImagen("imagenes/Grafeno.png");
         iRodio = Lienzo.cargarImagen("imagenes/rodio.png");
         iRadio = Lienzo.cargarImagen("imagenes/radio.png");
        iCromo = Lienzo.cargarImagen("imagenes/Cromo.png");
 
     }
 
     public void update() {
 
         vender.update();
     }
 
     public void obserbaActor(Actor ac) {
         this.ac = ac;
     }
 
     public void draw(Graphics2D g) {
 
         //Cambiamos el tipo de letra
         g.setFont(new Font("SansSerif", Font.BOLD, 12));
         //Barra informacion imagenes de fondo
         g.drawImage(infIzq, x, y, null);
         g.drawImage(infCtr, x + 100, y, null);
         g.drawImage(infDr, WIDTH - 175, y, null);
         //Barra informacion Izquierda
         g.setColor(Color.WHITE);
         g.drawString("Puntuacion", x+25, y+17);
         g.drawString("Vida", x+25, y+57);
         g.setColor(Color.BLACK);
         g.drawString(""+Juego.getJuego().jugador.getPuntuacion(), x+35, y+37);
         g.drawString(""+Juego.getJuego().jugador.getVida()+"/"+Juego.getJuego().jugador.getVidaMax(), x+31, y+75);
         //Barra informacion Centro (Atributos)
         
         g.drawImage(iUranio, x+100, y+30, null);
         g.drawImage(iRodio, x+200, y+30, null);
         g.drawImage(iGrafeno, x+300, y+30, null);
         g.drawImage(iRadio, x+400, y+30, null);
         g.drawImage(iCromo, x+500, y+30, null);
         g.drawImage(energy, x+600, y+30, null);
         Tower torre = Ventana_Mapa.torre;
         Habilidad hab = Ventana_Mapa.habilidad;
         if (torre != null || hab != null) {
             //g.drawImage(iUranio, x+100, y+30, null);
             g.drawString("Uranio", x + 150, y + 30);
             g.drawString("" + Juego.getJuego().jugador.getRecursos().get("uranio"), x + 150, y + 50);
             
             //g.drawImage(iRodio, x+200, y+30, null);
             g.drawString("Rodio", x + 250, y + 30);
             g.drawString("" + Juego.getJuego().jugador.getRecursos().get("rodio"), x + 250, y + 50);
 
             //g.drawImage(iGrafeno, x+300, y+30, null);
             g.drawString("Grafeno", x + 350, y + 30);
             g.drawString("" + Juego.getJuego().jugador.getRecursos().get("grafeno"), x + 350, y + 50);
             
             //g.drawImage(iRadio, x+400, y+30, null);
             g.drawString("Radio", x + 450, y + 30);
             g.drawString("" + Juego.getJuego().jugador.getRecursos().get("radio"), x + 450, y + 50);
             
             //g.drawImage(iCromo, x+500, y+30, null);
             g.drawString("Cromo", x + 550, y + 30);
             g.drawString("" + Juego.getJuego().jugador.getRecursos().get("cromo"), x + 550, y + 50);
 
             //g.drawImage(energy, x+600, y+30, null);
             g.drawString("Energia", x + 650, y + 30);
             g.drawString("" + Juego.getJuego().jugador.getRecursos().get("energia") + "/" + Juego.getJuego().jugador.getEnergiaMax(), x + 650, y + 50);
             g.setColor(Color.red);
             if (torre != null) {
                 g.drawString("-" + torre.coste.get("uranio"), x + 150, y + 70);
                 g.drawString("-" + torre.coste.get("rodio"), x + 250, y + 70);
                 g.drawString("-" + torre.coste.get("grafeno"), x + 350, y + 70);
                 g.drawString("-" + torre.coste.get("radio"), x + 450, y + 70);
                 g.drawString("-" + torre.coste.get("cromo"), x + 550, y + 70);
                 g.drawString("-" + torre.coste.get("energia") + "/" + Juego.getJuego().jugador.getEnergiaMax(), x + 650, y + 70);
             } else {
                 g.drawString("-" + hab.getCoste().get("uranio"), x + 150, y + 70);
                 g.drawString("-" + hab.getCoste().get("rodio"), x + 250, y + 70);
                 g.drawString("-" + hab.getCoste().get("grafeno"), x + 350, y + 70);
                 g.drawString("-" + hab.getCoste().get("radio"), x + 450, y + 70);
                 g.drawString("-" + hab.getCoste().get("cromo"), x + 550, y + 70);
                 if (hab instanceof CentralEnergia) {
                     g.setColor(Color.GREEN);
                     g.drawString("+" + 50 + "/" + (Juego.getJuego().jugador.getEnergiaMax() + 50), x + 650, y + 70);
                 } else {
                     g.drawString("-" + hab.getCoste().get("energia") + "/" + Juego.getJuego().jugador.getEnergiaMax(), x + 650, y + 70);
                 }
             }
             g.setColor(Color.BLACK);
         } else {
             //g.drawImage(iUranio, x+100, y+40, null);
             g.drawString("Uranio", x + 150, y + 40);
             g.drawString("" + Juego.getJuego().jugador.getRecursos().get("uranio"), x + 150, y + 70);
             g.drawString("Rodio", x + 250, y + 40);
             g.drawString("" + Juego.getJuego().jugador.getRecursos().get("rodio"), x + 250, y + 70);
             //g.drawImage(iGrafeno, x+300, y+40, null);
             g.drawString("Grafeno", x + 350, y + 40);
             g.drawString("" + Juego.getJuego().jugador.getRecursos().get("grafeno"), x + 350, y + 70);
             g.drawString("Radio", x + 450, y + 40);
             g.drawString("" + Juego.getJuego().jugador.getRecursos().get("radio"), x + 450, y + 70);
             g.drawString("Cromo", x + 550, y + 40);
             g.drawString("" + Juego.getJuego().jugador.getRecursos().get("cromo"), x + 550, y + 70);
             g.drawString("Energia", x + 650, y + 40);
             g.drawString("" + Juego.getJuego().jugador.getRecursos().get("energia") + "/" + Juego.getJuego().jugador.getEnergiaMax(), x + 650, y + 70);
         }
         g.drawString("Oleada", x + 750, y + 40);
         g.drawString("" + Ventana_Mapa.nivel + "/10", x + 750, y + 70);
 
         //Barra informacion derecha (Informacion)
 
         if (ac == null) {
             //TODO: esto hay que cambiarlo por una informacion vacia por defecto!!
             g.drawImage(health, x + 940, y + 28, 17, 15, null);
             g.drawImage(armor, x + 940, y + 48, 17, 15, null);
             g.drawImage(atack, x + 940, y + 68, 17, 15, null);
             g.drawString("Vida: ---", x + 860, y + 40);
             g.drawString("Armadura: ---", x + 860, y + 60);
             g.drawString("Daño: ---", x + 860, y + 80);
             g.drawString("?", x + 985, y + 55);
             //ac=new EBasico(1, new Vector2D(-100,-100));
         } else {
             g.drawImage(ac.getImagen(), x + 965, y + 30, null);
         }
         if (ac instanceof Enemy) {
             Enemy e = (Enemy) ac;
             g.drawImage(health, x + 940, y + 28, 17, 15, null);
             g.drawImage(armor, x + 940, y + 48, 17, 15, null);
             g.drawImage(atack, x + 940, y + 68, 17, 15, null);
             g.drawString("Vida: " + (int) e.getVida(), x + 860, y + 40);
             g.drawString("Armadura: " + e.getArmadura(), x + 860, y + 60);
             g.drawString("Daño: " + e.getDano(), x + 860, y + 80);
 
             //brillo al bicho
             //TODO: remarcar el bicho elegido
             g.drawImage(brillo, (int) ac.posicion.x - 5, (int) ac.posicion.y - 5, ac.width + 10, ac.getImagen().getHeight(null) + 10, null);
             if (e.getVida() < 0) {
                 ac = null;
 
             }
         } else if (ac instanceof Tower) {
             Tower t = (Tower) ac;
             g.drawImage(atack, x + 940, y + 28, 17, 15, null);
             g.drawImage(alcan, x + 940, y + 48, 17, 15, null);
             g.drawImage(reload, x + 940, y + 68, 17, 15, null);
             g.drawString("Ataque: " + (int) t.getAtaque(), x + 860, y + 40);
             g.drawString("Alcance: " + (int) t.getRango(), x + 860, y + 60);
             g.drawString("Recarga: " + (int) t.getRecarga()/1000+"s", x + 860, y + 80);
             vender.draw(g);
 
             //brillo a la torre
             //TODO: remarcar la torre y su alcance
             g.drawImage(brillo, (int) (ac.posicion.x + (Ventana_Mapa.casillaWidth / 2) - t.getRango()), (int) (ac.posicion.y + (Ventana_Mapa.casillaHeight / 2) - t.getRango()), (int) (t.getRango() * 2), (int) (t.getRango() * 2), null);
 
         }
 
     }
 }
