 package org.gdc.gdcalaga;
 import java.util.ArrayList;
 
 
 public class Collision {
     
     public static void checkCollisions(ArrayList<Entity> ents)
     {
         
         //Very inefficient, but should work fine for now
         
         for(int entA = 0; entA < ents.size(); entA++)
         {
             
             Entity A = ents.get(entA);
             
             if(A.shape != null && A.shape.type != Shape.ShapeType.Null)
             {
                 
                 for(int entB = entA + 1; entB < ents.size(); entB++)
                 {
                     
                     Entity B = ents.get(entB);
                     
                     if(B.shape != null && B.shape.type != Shape.ShapeType.Null)
                     {
                         
                        if(A.shape.Intersects(B.shape))
                         {
                             
                             A.Collide(B);
                             B.Collide(A);
                             
                         }
                         
                     }
                     
                 }
                 
             }
             
         }
         
     }
     
 }
