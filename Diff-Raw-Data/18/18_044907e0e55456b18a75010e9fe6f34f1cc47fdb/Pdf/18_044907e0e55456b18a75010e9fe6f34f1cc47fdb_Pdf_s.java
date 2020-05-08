 package walker;
 
 import java.io.*;
 
 /**
  *  Represent components of a probability distribution
  */
 public class Pdf implements Serializable, Comparable<Integer>
 {
     private long source;
     private int weight;
     private String category;
     private int summedWeight;
 
     public Pdf()
     {
     }
 
     public Pdf(long source, String category)
     {
         this.source = source;
         this.category = category;
     }
 
     public void setSource(long source)
     {
         this.source = source;
     }
 
     public long getSource()
     {
         return source;
     }
 
     public void setWeight(int weight)
     {
         this.weight = weight;
     }
 
     public int getWeight()
     {
         return weight;
     }
 
     public void setCategory(String category)
     {
         this.category = category;
     }
 
     public String getCategory()
     {
         return category;
     }
 
     public void setSummedWeight(int summedWeight)
     {
         this.summedWeight = summedWeight;
     }
 
     public int getSummedWeight()
     {
         return summedWeight;
     }
 
     public int compareTo(Integer integer)
     {
         int area = integer.intValue();
         if (summedWeight < area)
             return -1; 
         if (summedWeight > area)
             return 1; 
 
         return 0;
     }
 }
