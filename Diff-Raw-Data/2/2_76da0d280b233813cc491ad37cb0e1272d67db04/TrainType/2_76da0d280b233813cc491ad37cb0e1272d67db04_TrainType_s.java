 package polly.rx.entities;
 
 import java.util.NoSuchElementException;
 
 import polly.rx.MSG;
 
 
 public enum TrainType {
     INTELLIGENCE, BODY, MODULE, CREW, TECH, COMMANDO, PAYMENT,
     EXTENDED_INTELLIGENCE, EXTENDED_BODY, EXTENDED_MODULE, EXTENDED_CREW, EXTENDED_TECH,
     EXTENDED_COMMANDO,
     INTESIVE_INTELLIGENCE, INTESIVE_BODY, INTESIVE_MODULE, INTESIVE_CREW, INTESIVE_TECH,
     INTESIVE_COMMANDO;
     
     
     public static TrainType parse(String s) {
         if (s.contains("intensives")) { //$NON-NLS-1$
             if (s.contains("Intelligenz")) { //$NON-NLS-1$
                 return INTESIVE_INTELLIGENCE;
             } else if (s.contains("Kommandolimit")) { //$NON-NLS-1$
                 return INTESIVE_COMMANDO;
             } else if (s.contains("Modullimit")) { //$NON-NLS-1$
                 return INTESIVE_MODULE;
            } else if (s.contains("K�rper")) { //$NON-NLS-1$
                 return INTESIVE_BODY;
             } else if (s.contains("Crewlimit")) { //$NON-NLS-1$
                 return INTESIVE_CREW;
             } else if (s.contains("Techlimit")) { //$NON-NLS-1$
                 return INTESIVE_TECH;
             }
         } else if (s.contains("erweitertes")) { //$NON-NLS-1$
             if (s.contains("Intelligenz")) { //$NON-NLS-1$
                 return EXTENDED_INTELLIGENCE;
             } else if (s.contains("Kommandolimit")) { //$NON-NLS-1$
                 return EXTENDED_COMMANDO;
             } else if (s.contains("Modullimit")) { //$NON-NLS-1$
                 return EXTENDED_MODULE;
             } else if (s.contains("Körper")) { //$NON-NLS-1$
                 return EXTENDED_BODY;
             } else if (s.contains("Crewlimit")) { //$NON-NLS-1$
                 return EXTENDED_CREW;
             } else if (s.contains("Techlimit")) { //$NON-NLS-1$
                 return EXTENDED_TECH;
             }
         } else if (s.contains("Anzahlung")) { //$NON-NLS-1$
             return PAYMENT;
         } else {
             if (s.contains("Intelligenz")) { //$NON-NLS-1$
                 return INTELLIGENCE;
             } else if (s.contains("Kommandolimit")) { //$NON-NLS-1$
                 return COMMANDO;
             } else if (s.contains("Modullimit")) { //$NON-NLS-1$
                 return MODULE;
             } else if (s.contains("Körper")) { //$NON-NLS-1$
                 return BODY;
             } else if (s.contains("Crewlimit")) { //$NON-NLS-1$
                 return CREW;
             } else if (s.contains("Techlimit")) { //$NON-NLS-1$
                 return TECH;
             }
         }
         throw new NoSuchElementException("unknown train type: " + s); //$NON-NLS-1$
     }
     
     
     
     @Override
     public String toString() {
         switch (this) {
         default:
         case INTELLIGENCE: return MSG.trainTypeIntelligence;
         case BODY: return MSG.trainTypeBody;
         case COMMANDO: return MSG.trainTypeCommando;
         case MODULE: return MSG.trainTypeModule;
         case CREW: return MSG.trainTypeCrew;
         case TECH: return MSG.trainTypeTech;
         case PAYMENT: return MSG.trainTypePayment;
         
         case EXTENDED_INTELLIGENCE: return MSG.trainTypeExtendedIntelligence;
         case EXTENDED_BODY: return MSG.trainTypeExtendedBody;
         case EXTENDED_COMMANDO: return MSG.trainTypeExtendedCommand;
         case EXTENDED_MODULE: return MSG.trainTypeExtendedModule;
         case EXTENDED_CREW: return MSG.trainTypeExtendedCrew;
         case EXTENDED_TECH: return MSG.trainTypeExtendedTech;
         
         case INTESIVE_INTELLIGENCE: return MSG.trainTypeIntensiveIntelligence;
         case INTESIVE_BODY: return MSG.trainTypeIntensiveBody;
         case INTESIVE_COMMANDO: return MSG.trainTypeIntensiveCommand;
         case INTESIVE_MODULE: return MSG.trainTypeIntensiveModule;
         case INTESIVE_CREW: return MSG.trainTypeIntensiveCrew;
         case INTESIVE_TECH: return MSG.trainTypeIntensiveTech;
         }
     }
 }
