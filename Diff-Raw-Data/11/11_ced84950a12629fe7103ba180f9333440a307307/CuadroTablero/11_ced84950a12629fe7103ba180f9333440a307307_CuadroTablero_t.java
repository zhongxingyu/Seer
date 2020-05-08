 
 /**
  * @author (Luis Ballinas, Gabriel Ramos Olvera, Fernando Gomez, Francisco
  * Barros)
  * @version (9/13/2013)
  */
 import javax.swing.*;
 import java.awt.*;
 import java.util.ArrayList;
 
 public abstract class CuadroTablero extends JPanel {
 
     private Posicion posicion;
     protected Color colorFondo;
     private Ficha ficha;
     private Tablero tablero;
     private ArrayList<Posicion> posiblesMovs;
 
     public CuadroTablero(Color color, Posicion posicion) {
         super();
         this.posicion = posicion;
         this.colorFondo = color;
         setBackground(color);
         setOpaque(true);
         setLayout(new BorderLayout(0, 0));
         addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 cuadroPresionado(evt);
             }
         });
     }
 
     public abstract void restablecerColorFondo();
 
     private void cuadroPresionado(java.awt.event.MouseEvent evt) {
         CuadroTablero[][] cuadros = this.tablero.getCuadros();
         // Si hay cuadros en verde de posibles movs regresar a su estado original sólo si ya tiró
         if (!Tablero.tirando) {
             this.tablero.restablecerCuadros();
         }
 
 //        System.out.println("# Se presionó el cuadro en:");
         // Determinar en qué posiciones se puede mover
 //        System.out.println(posicion);
         // Si no está tirando apenas va a seleccionar una ficha para moverla
         if (!Tablero.tirando) {
             if (this.ficha == null) {
                 System.out.println("NO HAY FICHA");
             } else {
                 boolean puedeMover = false;
                 System.out.println("HAY FICHA EN ESTE CUADRO");
                 // Sólo puede tirar si selecciona una ficha de su propio tipo
                 if (this.ficha instanceof FichaB && Tablero.turnoJugador1) {
                     // Puede mover esa ficha
                     puedeMover = true;
                 } else if (this.ficha instanceof FichaA && !Tablero.turnoJugador1) {
                     // Puede mover esta ficha
                     puedeMover = true;
                 } else {
                     puedeMover = false;
                     System.out.println("NO PUEDES MOVER ESTA FICHA");
                     JOptionPane.showMessageDialog(this, "NO PUEDES MOVER ESTA FICHA");
                 }
 
                 if (puedeMover) {
                     ficha.determinarPosiblesMovimientos();
                    // Sólo si tiene posibles movimientos puede tirar, sino necesita
                    // seleccionar otra ficha
                    if (ficha.getPosiblesMovs().size() > 0) {
                        Tablero.tirando = true;
                        Tablero.fichaAMover = ficha;
                    }
                 }
             }
         } else {
             // Si ya está tirando entonces seleccionó un cuadro donde quiere mover la ficha
             System.out.println("INTENTANDO TIRAR en:");
             System.out.println(posicion);
             // Si la ficha que se quiere mover contiene la posición en posible mov 
             // de este cuadro entonces se puede mover
             boolean movValido = false;
             for (Posicion p : Tablero.fichaAMover.getPosiblesMovs()) {
                 if (p.getX() == posicion.getX() && p.getY() == posicion.getY()) {
                     movValido = true;
                     break;
                 }
             }
             if (movValido) {
                 System.out.println("# MOVIMIENTO VÁLIDO. Tiraste en:");
                 System.out.println(posicion);
                 // Tirar: mover ficha al nuevo cuadro y remover la antigua
                 Tablero.fichaAMover.getCuadro().removeAll();
                 Tablero.fichaAMover.getCuadro().revalidate();
                 Tablero.fichaAMover.getCuadro().repaint();
                Tablero.fichaAMover.getCuadro().setFicha(null);
                 add(Tablero.fichaAMover);
                 setFicha(Tablero.fichaAMover);
                 Tablero.fichaAMover.setCuadro(this);
                 Tablero.fichaAMover.setPosicion(posicion);
                 
                 // Ya tiró, establecer como tirando false
                 Tablero.tirando = false;
                 // Restablecer cuadros (quitar color verde)
                 this.tablero.restablecerCuadros();
                 // Cambiar de turno
                 if (Tablero.turnoJugador1) {
                     Tablero.turnoJugador1 = false;
                     this.tablero.getVentanaJuego().cambiarLblTurno("TURNO DE JUGADOR 2");
                 } else {
                     Tablero.turnoJugador1 = true;
                     this.tablero.getVentanaJuego().cambiarLblTurno("TURNO DE JUGADOR 1");
                 }
             } else {
                 System.out.println("MOVIMIENTO INVÁLIDO");
                 JOptionPane.showMessageDialog(this, "MOVIMIENTO INVÁLIDO");
             }
         }
     }
 
     public void agregarFicha(Ficha ficha) {
         add(ficha);
         this.ficha = ficha;
         this.ficha.setCuadro(this);
         ficha.setPosicion(posicion);
     }
 
     public boolean hayFicha() {
         if (this.ficha == null) {
             return false;
         } else {
             return true;
         }
     }
 
     @Override
     public String toString() {
         if (this.ficha != null) {
             return "o";
         } else {
             return "x";
         }
     }
 
     public Posicion getPosicion() {
         return posicion;
     }
 
     public void setPosicion(Posicion posicion) {
         this.posicion = posicion;
     }
 
     public Ficha getFicha() {
         return ficha;
     }
 
     public void setFicha(Ficha ficha) {
         this.ficha = ficha;
     }
 
     public Tablero getTablero() {
         return tablero;
     }
 
     public void setTablero(Tablero tablero) {
         this.tablero = tablero;
     }
 }
