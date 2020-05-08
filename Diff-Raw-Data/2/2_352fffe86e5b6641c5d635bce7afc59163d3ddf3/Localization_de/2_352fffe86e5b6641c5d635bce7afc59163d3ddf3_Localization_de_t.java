 package piano;
 
 /**
  * German localization.
  *
  * @author Wincent Balin
  */
 public class Localization_de implements LocalizationInterface
 {
     /**
      * Implementation of the LocalizationInterface.
      */
     public String getResource(int id)
     {
         switch(id)
         {
             case ID_COMMAND_EXIT: return "Beenden";
             case ID_COMMAND_BACK: return "Zurück";
             case ID_COMMAND_OK: return "OK";
             case ID_COMMAND_CANCEL: return "Abbrechen";
             case ID_HELP: return "Hilfe";
             case ID_VOLUME: return "Lautstärke";
             case ID_TIMBRES: return "Klangfarben";
             case ID_HELP_SECTION_INTRODUCTION_TITLE: return "";
             case ID_HELP_SECTION_INTRODUCTION_TEXT:
                return "Dies ist ein simples Musikinstrument.";
             case ID_HELP_SECTION_PLAYING_TITLE: return "Bedienung";
             case ID_HELP_SECTION_PLAYING_TEXT:
                 return "Benutzen Sie die Telefontastatur, " +
                        "um auf der dargestellten Tastatur zu spielen. " +
                        "Die Hilfeleiste unter der abgebildeten Tastatur " +
                        "zeigt den Zusammenhang zwischen den Tasten " +
                        "der beiden Tastaturen.\n" +
                        "Es ist nicht ausgeschlossen, dass Sie wegen " +
                        "der Einschränkungen in der Telefontastatur " +
                        "nicht mehr als eine oder zwei Tasten gleichzeitig " +
                        "spielen können.\n";
             case ID_HELP_SECTION_OCTAVE_TITLE: return "Oktavenwechsel";
             case ID_HELP_SECTION_OCTAVE_TEXT:
                 return "Benutzen Sie die Pfeiltasten, " +
                        "um die Oktave zu wechseln.";
             case ID_HELP_SECTION_COPYRIGHT_TITLE: return "Copyright";
             case ID_HELP_SECTION_COPYRIGHT_TEXT:
                 return "Copyright (C) 2010 Wincent Balin";
             case ID_HELP_SECTION_THANKS_TITLE: return "Danksagungen";
             case ID_HELP_SECTION_THANKS_TEXT:
                 return "Ein großer Dank geht an meine Betatester " +
                        "Birgit und Marina.";
             case ID_OCTAVE: return "Oktave";
             case ID_ERROR: return "Fehler";
             case ID_ERROR_NO_TONE_GENERATOR:
                 return "Kein Tongenerator vorhanden!";
             default: return "";
         }
     }
 }
