 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package blokd;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Stack;
 
 /**
  *
  * @author arjandoets
  */
 public class Vakje {
     Vakje left;
     boolean muurleft = true;
     Vakje up;
     boolean muurup = true;
     Vakje right;
     boolean muurright = true;
     Vakje down;
     boolean muurdown = true;
     boolean Done = false;
     
     //tmp
     int id;
     boolean path;
     int minstepstart = 0;
     
     Spelonderdeel bevat;
     
     public Vakje(int id) {
         this.id = id;   
     }
 
     public void setSpeler(Spelonderdeel speler) {
         this.bevat = speler;
         this.bevat.huidigvakje = this;
     }
     
     public void setVriend(Spelonderdeel vriend){
         this.bevat = vriend;
         this.bevat.huidigvakje = this;
     }
     
     public void setHelper(Spelonderdeel helper){
         this.bevat = helper;
         this.bevat.huidigvakje = this;
     }
     
     public void setDown(Vakje down) 
     {
         this.down = down;
         down.up = this;
     }
 
     public void setLeft(Vakje left) 
     {
         this.left = left;
         left.right = this;
     }
 
     public void setRight(Vakje right) 
     {
         this.right = right;
         right.left = this;
     }
 
     public void setUp(Vakje up) 
     {
         this.up = up;
         up.down = this;
     }
     
     //de done variabele reseten bij alle vakjes
     public void Done()
     {
         Done = false;
         if(left != null){
             if(left.Done){
                 left.Done();
             }
         }
         if(up != null){
             if(up.Done){
                 up.Done();
             }
         }
         if(right != null){
             if(right.Done){
                 right.Done();
             }
         }
         if(down != null){
             if(down.Done){
                 down.Done();
             }
         }
         minstepstart = 0;
         path = false;
     }
     
     public void findroute(PathFinder find){        
         if((right == null) && (down == null)){
             find.current.push(this);
             find.foundroute();
         } else if ((minstepstart == 0) || (minstepstart >= find.current.size())){
             ArrayList<Direction> pdir = new ArrayList<>();
             
             find.current.push(this);
             
             if((left != null) && (!muurleft)){
              
                     if (!find.current.contains(left)){
                         pdir.add(Direction.LEFT);
                     }
             }
             
             if((up != null) && (!muurup)){
              
                     if (!find.current.contains(up)){
                         pdir.add(Direction.UP);
                     }
             }
             if((right != null) && (!muurright)){
              
                     if (!find.current.contains(right)){
                         pdir.add(Direction.RIGHT);
                     }
             }
             if((down != null) && (!muurdown)){
                 
                     if (!find.current.contains(down)){
                         pdir.add(Direction.DOWN);
                     }
             }
                 
                 minstepstart = find.current.size();
                 
                 if(pdir.contains(Direction.LEFT)){
                     left.findroute(find);
                 }
                 if(pdir.contains(Direction.UP)){
                     up.findroute(find);
                 }
                 if(pdir.contains(Direction.RIGHT)){
                     right.findroute(find);
                 }
                 if(pdir.contains(Direction.DOWN)){
                     down.findroute(find);
                 }
            
             
         }
         
         find.current.remove(this);      
     }
     
     
     public void generatelevel()
     {
         Done = true;
         
         ArrayList<Direction> pdir = new ArrayList<>();
         ArrayList<Direction> draw = new ArrayList<>();
         
         if(left != null){
             if(!left.Done){
                 pdir.add(Direction.LEFT);
             }
         }
         if(up != null){
             if(!up.Done){
                 pdir.add(Direction.UP);
             }
         }
         if(right != null){
             if(!right.Done){
                 pdir.add(Direction.RIGHT);
             }
         }
         if(down != null){
             if(!down.Done){
                 pdir.add(Direction.DOWN);
             }
         }
             
         
         draw.add(Direction.LEFT);
         draw.add(Direction.UP);
         draw.add(Direction.RIGHT);
         draw.add(Direction.DOWN);
         
         for( int i = pdir.size(); i != 0;  i --) {
         
             Random random = new Random();
             int dir = random.nextInt(i);
             int deletewall = random.nextInt(20);
             //int deletewall = 2;
             
             Direction sdir = pdir.get(dir);
 
             switch (sdir) {
                 case LEFT:
                     if (!left.Done){
                         left.generatelevel();
                         draw.remove(Direction.LEFT);
                         pdir.remove(Direction.LEFT);
                     } else if (deletewall < 1) {
                         draw.remove(Direction.LEFT);
                         pdir.remove(Direction.LEFT);
                     }
                     
                     
                     break;
                 case UP:
                     if (!up.Done){
                         up.generatelevel();
                         draw.remove(Direction.UP);
                         pdir.remove(Direction.UP);
                     } else if (deletewall < 1) {
                         draw.remove(Direction.UP);
                         pdir.remove(Direction.UP);
                     }
                     
                     break;
                 case RIGHT:
                     if (!right.Done){
                         right.generatelevel();
                         draw.remove(Direction.RIGHT);
                         pdir.remove(Direction.RIGHT);
                     } else if (deletewall < 1) {
                         draw.remove(Direction.RIGHT);
                         pdir.remove(Direction.RIGHT);
                     }
                     break;
                 case DOWN:
                     if (!down.Done){
                         down.generatelevel();
                         draw.remove(Direction.DOWN);
                         pdir.remove(Direction.DOWN);
                     } else if (deletewall < 1) {
                         draw.remove(Direction.DOWN);
                         pdir.remove(Direction.DOWN);
                     }
                     break;
             }
 
         }
 
             
         if(!draw.contains(Direction.LEFT)){
             muurleft = false;
             if(left != null){
                 left.muurright = false;
             }
         }
         if(!draw.contains(Direction.UP)){
             muurup = false;
             if(up != null){
                 up.muurdown = false;
             }
         }
         if(!draw.contains(Direction.RIGHT)){
             muurright = false;
             if(right != null){
                 right.muurleft = false;
             }
         }
         if(!draw.contains(Direction.DOWN)){
             muurdown = false;
             if(down != null){
                 down.muurup = false;
             }
         }
         
         
     }
     
     // tekenen van het huidige vakje en het aanroepen van het tekenen van de omliggende vakjes
     public void draw(Graphics2D g, int x, int y)
     {       
         g.setColor(Color.red);
         if (path) {
             g.fillRect(x*Speelveld.vakjessize + Speelveld.vakjessize / 4, y*Speelveld.vakjessize + Speelveld.vakjessize / 4, Speelveld.vakjessize /2, Speelveld.vakjessize / 2);
         }
         
         g.setColor(Color.green);
         if(bevat != null){
             bevat.draw(g, x, y);
         }
         
         g.setColor(Color.blue);
         if(bevat != null){
             bevat.draw(g, x, y);
         }
         
         g.setColor(Color.black);
         if(bevat != null){
             bevat.draw(g, x, y);
         }
         
        g.setColor(Color.black);
         Done = true;
         
         if(muurleft == true)
         {
             g.drawLine(x * Speelveld.vakjessize, y * Speelveld.vakjessize, x * Speelveld.vakjessize, y * Speelveld.vakjessize + Speelveld.vakjessize);
         }  
         
         if(muurup == true)
         {
             g.drawLine(x * Speelveld.vakjessize, y * Speelveld.vakjessize, x * Speelveld.vakjessize + Speelveld.vakjessize, y * Speelveld.vakjessize);
         }
         
         if(muurright == true)
         {
             g.drawLine(x * Speelveld.vakjessize + Speelveld.vakjessize, y * Speelveld.vakjessize, x * Speelveld.vakjessize + Speelveld.vakjessize, y * Speelveld.vakjessize + Speelveld.vakjessize);
         }
         
         if(muurdown == true)
         {
             g.drawLine(x * Speelveld.vakjessize, y * Speelveld.vakjessize + Speelveld.vakjessize, x * Speelveld.vakjessize + Speelveld.vakjessize, y * Speelveld.vakjessize + Speelveld.vakjessize);
         }
         
         if(left != null){
             if(!left.Done){
                 left.draw(g, x - 1, y);
             }
         }
         if(up != null){
             if(!up.Done){
                 up.draw(g, x , y - 1);
             }
         }
         if(right != null){
             if(!right.Done){
                 right.draw(g, x + 1, y);
             }
         }
         if(down != null){
             if(!down.Done){
                 down.draw(g, x, y + 1);
             }
         }
         //g.drawString("" + id, x * 50 + 10, y * 50 + 20);
         
     }
     
     
     
 }
