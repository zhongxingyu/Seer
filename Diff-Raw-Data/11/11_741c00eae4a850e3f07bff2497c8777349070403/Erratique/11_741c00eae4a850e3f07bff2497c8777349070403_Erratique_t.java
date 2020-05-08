 package tpneuneu;
 
 import graphisme.IDrawable;
 import graphisme.JCanvas;
 import graphisme.ObjetGraphique;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 public class Erratique extends Neuneu {
 
     /**
      * 
      * @param a position in X
      * @param b position in Y
      */
     	
 
     protected JCanvas jcanvas;
     
   public Erratique(int a, int b, Point pos, Dimension dim, JCanvas jc){
       super(Color.RED,pos,dim,jc);
       
       this.nameId++;
       this.name="Erratique"+this.nameId;
       this.posX=a;
       this.posY=b;
       this.niveau=100;
       this.sexe=(int)Math.random()*2;
       jc.addDrawable(this);
   }
   @Override //TODO il va surement falloir ajouter un cas se dans seDeplacer qui se définira 
   public void seDeplacer() {
      this.posX+=(int)(Math.random()*6)-3;
       /**
        * Prevents out of bound for x
        */
      if (this.posX>=20) 
           this.posX=19;
       if (this.posX<0)
           this.posX=0;
      this.posY+=(int)(Math.random()*6)-3;
       /**
        * Prevents out of bound for y
        */
      if (this.posY>=20)
           this.posY=19;
       if (this.posY<0)
           this.posY=0;
       this.niveau-=10;
       /**
        * on va appeler la fonction majpresence en dehors de cette fonction
        */
   }
   
   	public void draw(Graphics g) {
 
 		Color c = g.getColor();
 		g.setColor(color);
 		g.drawRect((this.posX)*20+5,(this.posY)*20+5,5,5);
                   g.setColor(c);
 	}    
 }
