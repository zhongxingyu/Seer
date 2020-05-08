 package org.hexgame.ui;
 
 import org.hexgame.ui.HexGui;
 import org.hexgame.ui.Hexagon;
 import javax.swing.*;
 import java.awt.*;
 
 class Hexboard extends JPanel {
  public Hexagon [][] tab=new Hexagon [21][11];
   public HexGui gui;
   private int bx=142,by=0;
   private int size;
 
   public Hexboard(int size, HexGui gui) {
     this.gui=gui;
     this.size=size;
     setPreferredSize(new Dimension(310,630));
     setLayout(null);
     int m=0;
     for(int i=0;i<2*size-1;++i) {
       int bx1=bx;
       for(int j=0;j<=m;++j) {
        Hexagon tmp=new Hexagon(15,i+1,j+1, this);
         tmp.setBounds(bx1,by,26,30);
        tab[i][j]=tmp; add(tab[i][j]);
         bx1+=26;
       }
       if(i<size-1) {
         m++;
         bx-=13;
       }
       else {
         bx+=13;
         m--;
       }
       by+=24;
     }
   }
   
   public void paintHexagon(int x, int y) {
    tab[x-1][y-1].paintCurrentPlayer();
   }
 }
