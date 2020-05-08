 package ru.hh.spellcorrector.dto;
 
 
 import javax.xml.bind.annotation.XmlElement;
 
 public class WordDto {
 
   @XmlElement
   public String text;
 
   @XmlElement
   public Correction correction;
 
   @XmlElement
   public String source;
 
   public static WordDto text(String text) {
     WordDto word = new WordDto();
     word.text = text;
     return word;
   }
 
   public static WordDto correction(String text, String correction, String source) {
     WordDto word = new WordDto();
    if (!text.equals("")) {
      word.text = text;
    }
 
     word.correction = new Correction();
     word.correction.variant = correction;
 
     word.source = source;
     return word;
   }
 
   public static class Correction {
     @XmlElement
     public String variant;
   }
 }
