 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.aksw.distributions;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.aksw.distributions.Fact.Entry;
 
 /**
  *
  * @author ngonga
  */
 public class ChiSquaredGlobalNormalization implements Normalization{
     public List<Fact> normalize(List<Fact> facts) {
         List<Fact> copy = new ArrayList<Fact>();
         for (int i = 0; i < facts.size(); i++) {
             copy.add(facts.get(i).copy());
         }
 
        
         //first get all dates
         Set<String> dates = new HashSet<String>();
         for (Fact f : copy) {
             dates.add(f.get(Entry.DATE));
         }
        
        Set<String> objects = new HashSet<String>();
        for (Fact f : copy) {
            objects.add(f.get(Entry.OBJECT));
        }
 
         //build a map for each date
         for (String date : dates) {
             double total = 0d;
             double counter = 0d;
             for (Fact f : copy) {
                 if (f.get(Entry.DATE).equals(date)) {
                     String score = f.get(Entry.SCORE);
                     total = total + Double.parseDouble(score);
                     counter ++;
                 }
             }
             
             //expected value 
            double expected = total/objects.size();
             
             for (Fact f : copy) {
                 if (f.get(Entry.DATE).equals(date)) {
                     String score = f.get(Entry.SCORE);
                     f.add(Entry.SCORE, chi(Double.parseDouble(score), expected) + "");
                 }
             }
         }
 
         return copy;
     }
     
     public double chi(double score, double expected)
     {
        return (score - expected)*(score - expected)/expected;
     }
 }
