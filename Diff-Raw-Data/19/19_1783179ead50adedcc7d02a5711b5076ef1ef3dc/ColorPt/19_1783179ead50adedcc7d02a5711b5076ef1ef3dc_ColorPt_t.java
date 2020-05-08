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
 
     String color;
 
     public ColorPt(String color){
         this.color = color;
     }
     public String getColor(){
         return color;
     }
     public void setColor(String color){
         this.color = color;
     }
 }
