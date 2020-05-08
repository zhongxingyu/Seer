 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lectorbmp;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 
 /**
  *
  * @author jmanuel
  */
 public class test_BmpTransform extends javax.swing.JFrame{
     
     public test_BmpTransform() {
         initComponents();
     }
     
     private void initComponents() {
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 400, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 300, Short.MAX_VALUE)
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 new test_BmpTransform().setVisible(true);
             }
         });
     }
     
     @Override
     public void paint(Graphics g) {
         super.paint(g);
         Graphics2D g2d = (Graphics2D) g;
         BmpTransform bmp = new BmpTransform();
         bmp.readBMP("tux.bmp");
         TratamientoImagen timg = new TratamientoImagen(bmp.getMatriz());
        timg.contraste();
        timg.nitidez();
        timg.suavizado();
        timg.contraste();
        timg.contraste();
        timg.nitidez();
         timg.nitidez();
         int m[][][] = timg.getImgbytes();
         for (int fil = m.length-1; fil > 0; fil--)
             for (int col = 0; col < m[fil].length; col++) {
                 g2d.setPaint(new Color(m[fil][col][0],m[fil][col][1],m[fil][col][2]));
                 g2d.drawRect(col, fil, 1, 1);
             }
     }
     
 }
