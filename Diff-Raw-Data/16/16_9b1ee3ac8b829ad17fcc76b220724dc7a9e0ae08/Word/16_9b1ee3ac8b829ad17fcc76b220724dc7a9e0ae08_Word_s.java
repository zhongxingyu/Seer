 import java.awt.Polygon;
 
 
 
 public class Word implements Comparable<Word> {
     private int count; // Times this key words was found
     private int count2; // Times this key words was found in file 2
     private String str; // The key word
     private int size; // Font size
     private int x; // x coordinate
     private int y; // y coordinate
     private Polygon bounds;
     static int total1 = 0;;
     static int total2 = 0;
     
     public Word(String str, int i)
     {
         if (i == 1)
         {
         	count = 1;
         	total1++;
         }
         else
         {
         	count2++;
         	total2++;
         }
         this.str = str;
         this.size = 15;
         this.x = -1;
         this.y = -1;
     }
 
     public String getStr() {return str;}
     public int getCount() {return count;}
     public int getCount2() {return count2;}
     public int getSize() {return size;}
     public void hit() {count++; total1++;} // Found again
     public void hit2() {count2++; total2++;} // Found again in file 2
     public void setSize(int i) {size = i;}
     public void setPoint(int x, int y) {this.x = x;    this.y = y;}
     public void setBounds(Polygon bounds) {this.bounds = bounds;}
     public int X() {return x;}
     public int Y() {return y;}
     public Polygon getBounds() {return bounds;}
    public double P() { return ((double)count/total1) / (((double)count / total1) + ((double)count2 / total2)); }
     public double N() {return (0.5 - Math.abs(0.5 - P())) * (count + count2);}
    
     public void print()
     {
        System.out.println(str + " : " + count + " times");
     }
 
     public int compareTo(Word w) // Override function for Comparable
     {
        return w.count - count;
     }
 }
