 package entities;
 
 import java.util.NoSuchElementException;
 
 
 public enum TrainType {
     INTELLIGENCE, BODY, MODULE, CREW, TECH, COMMANDO, PAYMENT;
     
     
     public static TrainType parse(String s) {
         if (s.contains("Intelligenz")) {
             return INTELLIGENCE;
         } else if (s.contains("Kommandolimit")) {
             return COMMANDO;
         } else if (s.contains("Modullimit")) {
             return MODULE;
         } else if (s.contains("Krper")) {
             return BODY;
         } else if (s.contains("Crewlimit")) {
             return CREW;
         } else if (s.contains("Techlimit")) {
             return TECH;
         } else if (s.contains("Anzahlung")) {
             return PAYMENT;
         }
         throw new NoSuchElementException("unknown train type: " + s);
     }
     
     
     
     @Override
     public String toString() {
         switch (this) {
         default:
         case INTELLIGENCE: return "Intelligenz Training";
        case BODY: return "Krper Training";
         case COMMANDO: return "Kommandolimit Training";
         case MODULE: return "Modullimit Training";
         case CREW: return "Crewlimit Training";
         case TECH: return "Techlimit Training";
         case PAYMENT: return "Anzahlung";
         }
     }
 }
