 package ru.spbau.bioinf.tagfinder;
 
 
 public class AllDataGenerator {
     public static void main(String[] args) throws Exception {
        ValidTags.main(args);
         TexTableGenerator.main(args);
         CalculateRelation.main(args);
         IntencityTableGenerator.main(args);
     }
 }
