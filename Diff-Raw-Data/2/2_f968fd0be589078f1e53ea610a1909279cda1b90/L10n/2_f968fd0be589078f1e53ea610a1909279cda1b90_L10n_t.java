 /**
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * © Copyright 2013, Gardet Julien, Droy Yann, Araujo Auxence.
  * 
  * The logo in edu.cubesta.ressources.favicon.png is a derivate work from
  * <http://commons.wikimedia.org/w/index.php?title=File:Rubik%27s_cube.svg&oldid=70000649>.
  * 
  * Other legal notices on <http://cubesta-project.github.io/CubeSTA/legals.html>.
  */
 /* Project : CubeSTA
  * Location : edu.cubesta.ressources
  * Class : L10n.java
  */
 
 package edu.cubesta.ressources;
 
 /**
  * Permet de gérer l'internationalisation du programme
  * @author julien.gardet yann.droy auxence.araujo
  */
 public class L10n {
     
     /**
      * Tips and tricks : 
      * Utiliser fileformat.info pour retrouver le code unicode des caractères
      * http://www.fileformat.info/info/unicode/char/search.htm?q=&preview=entity
      * Character : U+0041  ->  Java : \u0041
      */
     
     /**
      * Variables globales
      */
     
     private static String language;
     private static String[] i18n = new String[18];
     private static String[] i18nHelp = new String[23];
     
     /**
      * Définit la langue d'affichage par défaut
      */
 
     public L10n() {
         setEN();
         language = System.getProperty("user.language");
         setLanguage(language);
     }
     
     /**
      * Modifie la langue d'affichage
      * @param language 
      * la langue à définir
      */
 
     public static void setLanguage(String language) {
         switch(language){
             case "fr" : setFR(); break;
             case "en" : setEN(); break;
             case "pl" : setPL(); break;
             case "de" : setDE(); break;
             case "it" : setIT(); break;
             default : setEN(); break;
         }
     }
     
     /**
      * Retourne la liste des langues disponible
      * @return 
      * la liste des langues
      */
     
     public static String[] listLanguage(){
         String[] s = {"en", "de", "fr", "pl", "it"};
         return s;
     }
     
     /**
      * Retourne l'internationalisation d'un message
      * @param opt
      * Le numéro du message
      * @return 
      * le message traduit
      */
     
     public static String getLanguage(int opt){
         return i18n[opt];
     }
     
     /**
      * Retourne l'internationalisation d'un message d'aide
      * @param opt
      * Le numéro du message
      * @return 
      * le message traduit
      */
     
     public static String getLanguageHelp(int opt){
         return i18nHelp[opt];
     }
     
     /**
      * Retourne l'internationalisation des adjectifs ordinaux
      * @param number
      * Le numéro
      * @return 
      * l'abreviation
      */
     
     public static String getOrdinal(int number){
         String retour;
         if("fr".equals(language)){//French
             switch(number){
                 case 1 : retour = "er"; break;
                 case 2 : retour = "nd"; break;
                 default : retour = "\u00e8me"; break;
             }
         }else if("de".equals(language) || "pl".equals(language)){//Deutsch & Polski
             retour = ".";
        }else if("it".equals(language)){//Deutsch & Polski
             retour = "°";
         }else{//English and Default
             if(number <= 20){
                switch(number){
                     case 1 : retour = "st"; break;
                     case 2 : retour = "nd"; break;
                     case 3 : retour = "rd"; break;
                     default : retour = "th"; break;
                 } 
             }else{
                switch(number % 10){
                     case 1 : retour = "st"; break;
                     case 2 : retour = "nd"; break;
                     case 3 : retour = "rd"; break;
                     default : retour = "th"; break;
                 } 
             }
         }
         return retour;
     }
     
     /**
      * The english translation
      */
 
     private static void setEN() {
         language = "en";
         //MAIN I18N
         i18n[0] = "Round";
         i18n[1] = "Average";
         i18n[2] = "Best";
         i18n[3] = "Scramble";
         i18n[4] = "Timer";
         i18n[5] = "Option";
         i18n[6] = "Warning";
         i18n[7] = "Enter the number of movements for the scramble !";
         i18n[8] = "Enter the number of time for the average !";
         i18n[9] = "The number is too small (It should be greater than 3)";
         i18n[10] = "Choose your language !";
         i18n[11] = "seconds";
         i18n[12] = "This is not a number";
         i18n[13] = "Error";
         i18n[14] = "Inspection";
         i18n[15] = "Keyboard Help";
         i18n[16] = "Scramble Help";
         i18n[17] = "Enter the inspection time (in second) !";
         //HELP I18N
         i18nHelp[0] = "Escape : Quit the program";
         i18nHelp[1] = "Espace : Start the timer";
         i18nHelp[2] = "Shift : Start the inspection timer";
         i18nHelp[3] = "Enter : Add a penality of 2 seconds";
         i18nHelp[4] = "D : Set the previous time as DNF";
         i18nHelp[5] = "S : Set the next time as DNS";
         i18nHelp[6] = "R : Reset all times";
         i18nHelp[7] = "F1 : Display the keyboard help";
         i18nHelp[8] = "F2 : Display the syntax movement help";
         i18nHelp[9] = "F3 : Set the language of the program";
         i18nHelp[10] = "F9 : Set the inspection time";
         i18nHelp[11] = "F10 : Set the number of movements for the scramble";
         i18nHelp[12] = "F11 : Set the number of times to calculate the average";
         i18nHelp[13] = "F12 : Regenerate a new scramble";
         i18nHelp[14] = "F : Front side";
         i18nHelp[15] = "B : Back side";
         i18nHelp[16] = "U : Up side";
         i18nHelp[17] = "D : Down side";
         i18nHelp[18] = "R : Right side";
         i18nHelp[19] = "L : Left side";
         i18nHelp[20] = "  : Clockwise";
         i18nHelp[21] = "' : Counterclockwise";
         i18nHelp[22] = "2 : Twice";
     }
     
     /**
      * La traduction française
      */
     
     private static void setFR() {
         language = "fr";
         //MAIN I18N
         i18n[0] = "Tour";
         i18n[1] = "Moyenne";
         i18n[2] = "Meilleur";
         i18n[3] = "M\u00e9lange";
         i18n[4] = "Chronom\u00e8tre";
         i18n[5] = "Option";
         i18n[6] = "Attention";
         i18n[7] = "Entrer le nombre de mouvements pour le m\u00e9lange !";
         i18n[8] = "Entrer le nombre de temps pour la moyenne !";
         i18n[9] = "Le nombre est trop petit (Il doit \u00eatre sup\u00e9rieur \u00e0 3)";
         i18n[10] = "Choisissez votre langue !";
         i18n[11] = "secondes";
         i18n[12] = "Ce n'est pas un nombre";
         i18n[13] = "Erreur";
         i18n[14] = "Inspection";
         i18n[15] = "Aide Clavier";
         i18n[16] = "Aide M\u00e9lange";
         i18n[17] = "Entrer le temps d'inspection (en secondes) !";
         //HELP I18N
         i18nHelp[0] = "Echap : Quiter le programme";
         i18nHelp[1] = "Espace : D\u00e9marre le chronom\u00e8tre";
         i18nHelp[2] = "Majuscule : D\u00e9marre le temps d'inspection";
         i18nHelp[3] = "Entrer : Ajoute une p\u00e9naliter de 2 secondes";
         i18nHelp[4] = "D : D\u00e9finit le temps pr\u00e9c\u00e9dent comme DNF";
         i18nHelp[5] = "S : D\u00e9finit le temps suivant comme DNS";
         i18nHelp[6] = "R : Remet à z\u00e9ro tous les temps";
         i18nHelp[7] = "F1 : Afficher l'aide clavier";
         i18nHelp[8] = "F2 : Afficher l'aide de la syntaxe des mouvements";
         i18nHelp[9] = "F3 : D\u00e9finit la langue du programme";
         i18nHelp[10] = "F9 : D\u00e9finit le temps d'inspection";
         i18nHelp[11] = "F10 : D\u00e9finit le nombre de mouvements du m\u00e9lange";
         i18nHelp[12] = "F11 : D\u00e9finit le nombre de temps pour le calcul de la moyenne";
         i18nHelp[13] = "F12 : Permet de r\u00e9g\u00e9n\u00e9rer un m\u00e9lange";
         i18nHelp[14] = "F : Face avant";
         i18nHelp[15] = "B : Face arri\u00e8re";
         i18nHelp[16] = "U : Face du haut";
         i18nHelp[17] = "D : Face du bas";
         i18nHelp[18] = "R : Face de droite";
         i18nHelp[19] = "L : Face de gauche";
         i18nHelp[20] = "  : Dans le sens horaire";
         i18nHelp[21] = "' : Dans le sens antihoraire";
         i18nHelp[22] = "2 : Deux fois";
     }
     
     /**
      * Die deutsche Übersetzung
      */
     
     private static void setDE() {
         language = "de";
         //Main Help
         i18n[0] = "Runde";
         i18n[1] = "Mittelwert";
         i18n[2] = "Beste";
         i18n[3] = "Scramble";
         i18n[4] = "Stoppuhr";
         i18n[5] = "Wahl";
         i18n[6] = "Achtung";
         i18n[7] = "Geben Sie die Nummer der Bewegung f\u00fcr die Scramble !";
         i18n[8] = "Geben Sie die Nummer der Zeit f\u00fcr der Mittelwert !";
         i18n[9] = "Die Zahl ist zu klein (muss gr\u00f6\u00dfer als 3)";
         i18n[10] = "W\u00e4hlen Sie Ihre Sprache";
         i18n[11] = "Strafsekunden";
         i18n[12] = "Das ist nicht eine Nummer";
         i18n[13] = "Fehler";
         i18n[14] = "Pr\u00fcfung";
         i18n[15] = "Tastatur-Hilfe";
         i18n[16] = "Scramble-Hilfe";
         i18n[17] = "Geben Sie die Pr\u00fcfung Zeit (in Sekunden) !";
         //HELP I18N
          i18nHelp [0] = "Escape-Taste: Beenden das Programm";
          i18nHelp [1] = "Leertaste: Starten der Stoppuhr";
          i18nHelp [2] = "Umschalttaste: Starten der Pr\u00fcfung Stoppuhr";
          i18nHelp [3] = "Eingabetaste: F\u00fcgt 2 Strafsekunden";
          i18nHelp [4] = "D: Definiert der vorherige Zeit als DNF";
          i18nHelp [5] = "S: Definiert der n\u00e4chste Zeit als DNS";
          i18nHelp [6] = "R: Zur\u00fccksetzen aller Zeiten";
          i18nHelp [7] = "F1: Zeigt die Tastatur-Hilfe";
          i18nHelp [8] = "F2: Zeigt die Scramble-Hilfe";
          i18nHelp [9] = "F3: Definiert die Sprache des Programms";
          i18nHelp [10] = "F9: Definiert der Pr\u00fcfung Stoppuhr";
          i18nHelp [11] = "F10: Definiert die Anzahl der Bewegung f\u00fcr die Scramble";
          i18nHelp [12] = "F11: Definiert die Anzahl der f\u00fcr die Berechnung des Mittelwertes";
          i18nHelp [13] = "F12: Regeneriert eine neue Scramble";
          i18nHelp [14] = "F: Vorderseite";
          i18nHelp [15] = "B: R\u00fcckseite";
          i18nHelp [16] = "U: Obenseite";
          i18nHelp [17] = "D: Unterseite";
          i18nHelp [18] = "R: Richtigseite";
          i18nHelp [19] = "L: Linkenseite";
          i18nHelp [20] = " : Im Uhrzeigersinn";
          i18nHelp [21] = "': Gegen den Uhrzeigersinn";
          i18nHelp [22] = "2: Zweimal";
     }
     
     /**
      * Tłumaczenie na Polski
      */
     
     private static void setPL() {
         language = "pl";
         //MAIN I18N
         i18n[0] = "Runda";
         i18n[1] = "\u015arednia";
         i18n[2] = "Najlepszy";
         i18n[3] = "Mieszanie";
         i18n[4] = "Zegar";
         i18n[5] = "Ustawienia";
         i18n[6] = "Uwaga";
         i18n[7] = "Wpisa\u0107 liczb\u0119 ruch\u00f3w do pomieszania kostki !";
         i18n[8] = "Wpisa\u0107 liczb\u0119 czas\u00f3w na \u015bredni\u0105 ";
         i18n[9] = "Liczba jest za ma\u0142a (powy\u017cej 3)";
         i18n[10] = "Wybraj j\u0119zyk !";
         i18n[11] = "sekund";
         i18n[12] = "To nie jest numer";
         i18n[13] = "B\u0142\u0105d";
         i18n[14] = "Czas podgl\u0105dania";
         i18n[15] = "Pomoc klawiatury";
         i18n[16] = "Pomoc mieszania";
         i18n[17] = "Wpisa\u0107 czas podgl\u0105dania (w sekundach)!";
         //HELP I18N
         i18nHelp[0] = "Echap : Zamkn\u0105\u0107 program";
         i18nHelp[1] = "Spacja : Startowa\u0107 zegar";
         i18nHelp[2] = "Shift : Startowa\u0107 czas podgl\u0105dania";
         i18nHelp[3] = "Enter : Doda\u0107 kar\u0119 2 sekund";
         i18nHelp[4] = "D : Ustawi\u0107 poprzedni czas jako DNF";
         i18nHelp[5] = "S : Ustawi\u0107 przysz\u0142y czas jako DNS";
         i18nHelp[6] = "R : Zresetowa\u0107 zegar";
         i18nHelp[7] = "F1 : Wy\u015Bwietli\u0107 pomoc klawiatury";
         i18nHelp[8] = "F2 : Wy\u015Bwietli\u0107 pomoc ruch\u00f3w kostki";
         i18nHelp[9] = "F3 : Ustawi\u0107 j\u0119zyk";
         i18nHelp[10] = "F9 : Ustawi\u0107 czas podgl\u0105dania";
         i18nHelp[11] = "F10 : Ustawi\u0107 liczb\u0119 ruch\u00f3w do pomieszania kostki";
         i18nHelp[12] = "F11 : Ustawi\u0107 liczb\u0119 czas\u00f3w do liczenia \u015Bredniej";
         i18nHelp[13] = "F12 : Utworzy\u0107 nowe mieszanie ";
         i18nHelp[14] = "F : Przednia \u015Bciana";
         i18nHelp[15] = "B : Tylnia \u015Bciana";
         i18nHelp[16] = "U : G\u00f3rna \u015Bciana";
         i18nHelp[17] = "D : Dolna \u015Bciana";
         i18nHelp[18] = "R : Prawa \u015Bciana";
         i18nHelp[19] = "L : Lewa \u015Bciana";
         i18nHelp[20] = "  : Kierunkiem ruchu wed\u0142ug wskaz\u00f3wek zegara";
         i18nHelp[21] = "' : Kierunkiem przeciwnym wed\u0142ug ruchu wskaz\u00f3wek zegara";
         i18nHelp[22] = "2 : Dwa razy";
     }
     
     /**
      * Traduzione italiana
      */
 
     private static void setIT() {
         language = "it";
         //MAIN I18N
         i18n[0] = "Turno";
         i18n[1] = "Media";
         i18n[2] = "Migliore";
         i18n[3] = "Miscela";
         i18n[4] = "Cronometro";
         i18n[5] = "Opzione";
         i18n[6] = "Attenzione";
         i18n[7] = "Scrivere il numero di movimenti per la miscela !";
         i18n[8] = "Scrivere il numero di tempi per la media !";
         i18n[9] = "Il numlero \u00e8 troppo piccolo (Deve essere più grande di 3)";
         i18n[10] = "Scegliete la vostra lingua !";
         i18n[11] = "secondi";
         i18n[12] = "Non \u00e8 un numero";
         i18n[13] = "Errore";
         i18n[14] = "Inspezione";
         i18n[15] = "Aiuta di Testiera";
         i18n[16] = "Aiuta di Miscela";
         i18n[17] = "Scrivere il tempo di osservazione (secondi) !";
         //HELP I18N
         i18nHelp[0] = "Escape : Chiudere il programma";
         i18nHelp[1] = "Spazio : Avviare il cronometro";
         i18nHelp[2] = "Maiuscolo : Avviare il tempo di osservazione";
         i18nHelp[3] = "Invio : Aggiungere una penalità di 2 secondi";
         i18nHelp[4] = "D : Assegnare il tempo precedente come DNF";
         i18nHelp[5] = "S : Assegnare il tempo successivo come DNS";
         i18nHelp[6] = "R : Azzezare il cronometro";
         i18nHelp[7] = "F1 : Mostrare l'aiuta di testiera";
         i18nHelp[8] = "F2 : Mostrare l'aiuta di movimenti di miscela";
         i18nHelp[9] = "F3 : Cambiare la lingua del programma";
         i18nHelp[10] = "F9 : Cambiare il tempo di osservazione";
         i18nHelp[11] = "F10 : Cambiare il numero di movimenti della miscela";
         i18nHelp[12] = "F11 : Cambiare il numero di tempi della media";
         i18nHelp[13] = "F12 : Generare una nuova miscela";
         i18nHelp[14] = "F : Faccia anteriore";
         i18nHelp[15] = "B : Faccia posteriore";
         i18nHelp[16] = "U : Faccia superiore";
         i18nHelp[17] = "D : Faccia inferiore";
         i18nHelp[18] = "R : Faccia destra";
         i18nHelp[19] = "L : Faccia sinistra";
         i18nHelp[20] = "  : Direzione orario";
         i18nHelp[21] = "' : Direzione antiorario";
         i18nHelp[22] = "2 : Due volte";
     }
 }
