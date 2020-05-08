 import xtc.oop.helper.Bubble;
 
 class Pt{
 
     public Pt(){
     }
 }
 
 class ColorPt extends Pt{
     /*
     public static void main(String[] args){
         ColorPt c = new ColorPt("HAHAHAHA");
         System.out.println(c.getColor());
     }
     */
 
    final String color;
 
     public ColorPt(String color){
         this.color = color;
     }
     public String getColor(){
         return color;
     }
     public void setColor(String color){
         this.color = color;
     }
 
     public int addition(int a, int b){
         int c = a + b;
         return c;
         Integer theInt = new Integer(c);
         int g = theInt.hashCode();
         System.out.println(g);
     }
 }
