 package ru.spbau.bioinf.tagfinder;
 
 public class Protein {
     private int proteinId;
    private String acids;
     private String simplifiedAcids = null;
     private String name;
 
    public Protein(int proteinId, String acids, String name) {
         this.proteinId = proteinId;
        this.acids = acids;
         this.name = name;
     }
 
     public int getProteinId() {
         return proteinId;
     }
 
    public String getAcids() {
        return acids;
     }
 
     public String getName() {
         return name;
     }
 
     public String getSimplifiedAcids() {
         if (simplifiedAcids == null) {
            simplifiedAcids = acids.replaceAll("L", "I").replaceAll("Z", "Q").replaceAll("B", "E").replaceAll("X", "I");
         }
         return simplifiedAcids;
     }
 }
