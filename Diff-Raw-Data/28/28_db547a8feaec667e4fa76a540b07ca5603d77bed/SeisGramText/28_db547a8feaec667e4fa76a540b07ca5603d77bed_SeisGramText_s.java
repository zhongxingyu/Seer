 
 // added by HPC to put in a package
 //package net.alomax.freq;
 // change package
 package freq;
 
 /*
  * This file is part of the Anthony Lomax Java Library.
  *
  * Copyright (C) 1999 Anthony Lomax <lomax@faille.unice.fr>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 
 import java.util.Locale;
 
 
 /** A class for multi-language text */
 
 public class SeisGramText {
 
     private static final String LOCALE_NAME_DEFAULT = "en_US";
     private static final String LANGUAGE_DEFAULT = "en";
     private static final String COUNTRY_DEFAULT = "US";
     private static final String VARIANT_DEFAULT = " ";
 
     private String localeName = null;
     private Locale locale = null;
 
 
     // default en_US text
 
     String HELP = "Help";
     String PRINT = "Print";
     String EXIT = "Exit";
     String OPEN = "Open";
     String CLOSE = "Close";
     String OK = "OK";
     String CANCEL = "Cancel";
     String FILE = "File";
     String APPLY = "Apply";
     String CLEAR = "Clear";
     String SAVE = "Save";
     String OPTIONS = "Options";
     String INIT = "Initial";
     String PREV = "Previous";
     String OVERLAY = "Overlap";
     String SEPARATE = "Separate";
     String ROTATE = "Rotate";
     String PICK = "Pick";
     String PROCESS = "Process";
     String REMOVE_MEAN = "RemoveMean";
     String INTEGRATE = "Integrate";
     String DIFFERENTIATE = "Differentiate";
     String DEL_ALL = "Del All";
     String ANGLE = "Angle";
     String AMP = "Amp";
     String FILTER = "Filter";
     String BUTTERWORTH_FILTER = "Butterworth";
     String LOW_FREQ = "Low Freq";
     String HIGH_FREQ = "High Freq";
     String NUM_POLES = "Num Poles";
     String GAUSSIAN_FILTER = "Gaussian";
     String CENT_FREQ = "Cent Freq";
     String ALPHA = "Alpha";
     String FREQ = "Freq";
     String HILBERT = "Hilbert";
     String ENVELOPE = "Envelope";
     String ZNE = "Z->N,E; 0->1,2";
     String FORMAT = "Format";
     String BINARY_TYPE = "Binary Type";
     String URLDIALOG_TITLE = OPEN + " Channels";
     String TRAVEL_TIME = "Phases";
     String DISTANCE = "D";
     String OTIME = "To";
     String DEPTH = "h";
     String TRAVEL_TIME_DIALOG_TITLE = "Phase Options";
     String DISTANCE_UNITS = "Distance Units";
     String DEG = "deg";
     String KM = "km";
     String FIX = "Fix";
     String FIRST_ARRIVAL = "First Arrival";
     String PHASE_LIST = "Phase List";
     String VIEW = "View";
 
     String ALLIGN_TO = "Allign to";
     String ALL = "All";
     String ACTIVE = "Active";
 
     String PARTICLE_MOTION = "Particle Motion";
     String VIEW_FROM = "View From";
     String SAMPLE = "Sample";
     String ANIMATE = "Animation";
 
     String UTILITIES = "Utilities";
     String LANGUAGE = "Language";
     String en_US = "English";
     String fr_FR = "French";
     String it_IT = "Italian";
 
     String SEIS_PRINT_DIALOG_TITLE = "Print Selection";
     String SEIS_PRINT_DIALOG_PROMPT = "Print";
     String SEISMOGRAMS = "Seismograms";
 
     // error messages
     String ERROR_MULTIPLE_PHASES_FOUND =
         "WARNING: multiple P, S or CODA picks found.";
 
     // Butterworth filter exceptions
     String invalid_low_frequency_corner =
         "invalid low frequency corner.";
     String invalid_high_frequency_corner =
         "invalid high frequency corner.";
     String invalid_number_of_poles =
         "invalid number of poles.";
     String low_corner_greater_than_high_corner =
         "low frequency corner greater than high frequency corner.";
     // Gaussian filter exceptions
     String invalid_center_frequency =
         "invalid center frequency.";
     String invalid_alpha_value =
         "invalid alpha value.";
 
     // help text
     String HELP_TITLE = "Help - SeisGram2K";
     String HELP_TEXT = "help 1\nhelp 2\nhelp 3\nhelp 4\nhelp 5\nhelp 6\n";
 
     // about text
     String ABOUT = "About SeisGram2K";
     String ABOUT_TITLE = ABOUT;
 
     /** constructor - sets text */
 
     public SeisGramText(){
         this(LOCALE_NAME_DEFAULT);
     }
 
     /** constructor - sets text */
 
     public SeisGramText(String locName){
 
         localeName = locName;
 
         if (localeName == null)         // default en_US
             localeName = LOCALE_NAME_DEFAULT;
 
         // French, France
         if (localeName.equalsIgnoreCase("fr_FR")) {
 
             // labels
             HELP = "Aide";
             PRINT = "Imprimer";
             EXIT = "Sortir";
             OPEN = "Ouvrir";
             CLOSE = "Fermer";
             CANCEL = "Annuler";
             FILE = "Fichier";
             APPLY = "Appliquer";
            CLEAR = "Dgager";
             SAVE = "Enregistrer";
             OPTIONS = "Options";
             INIT = "Initiale";
            PREV = "Prcdente";
             OVERLAY = "Chevaucher";
            SEPARATE = "Sparer";
             ROTATE = "Tourner";
             PICK = "Pointer";
             PROCESS = "Traiter";
             REMOVE_MEAN = "EnleverMoyenne";
            INTEGRATE = "Intgrer";
            DIFFERENTIATE = "Diffrentier";
             DEL_ALL = "Supp Tous";
             ANGLE = "Angle";
             AMP = "Amp";
             FILTER = "Filtrer";
             BUTTERWORTH_FILTER = "Butterworth";
             LOW_FREQ = "Freq Bas";
             HIGH_FREQ = "Freq Haut";
             NUM_POLES = "Nom Poles";
             GAUSSIAN_FILTER = "Gaussian";
             CENT_FREQ = "Freq Cent";
             ALPHA = "Alpha";
             FREQ = "Freq";
             HILBERT = "Hilbert";
             ENVELOPE = "Envelope";
             ZNE = "Z->N,E; 0->1,2";
             FORMAT = "Format";
             BINARY_TYPE = "Sort Binaire";
             URLDIALOG_TITLE = OPEN + " Channels";
             TRAVEL_TIME = "Phases";
             DISTANCE = "D";
             OTIME = "To";
             DEPTH = "h";
             TRAVEL_TIME_DIALOG_TITLE = "Options Phases";
             DISTANCE_UNITS = "Unites Distance";
             DEG = "deg";
             KM = "km";
             FIX = "Fixer";
            FIRST_ARRIVAL = "Arrive Premiere";
             PHASE_LIST = "Liste des phases";
             VIEW = "Affichage";
 
             ALLIGN_TO = "Alligner a";
             ALL = "Tous";
             ACTIVE = "Actif";
 
             PARTICLE_MOTION = "Motion de particle";
             VIEW_FROM = "Vue de";
             SAMPLE = "Sample";
             ANIMATE = "Animation";
 
             UTILITIES = "Outils";
             LANGUAGE = "Langue";
             en_US = "Anglais";
             fr_FR = "Francais";
             it_IT = "Italian";
 
             SEIS_PRINT_DIALOG_TITLE = "Imprimer Selection";
             SEIS_PRINT_DIALOG_PROMPT = "Imprimer";
             SEISMOGRAMS = "Seismograms";
 
             // error messages
             ERROR_MULTIPLE_PHASES_FOUND =
                "ATTENTION: multiple P, S ou CODA pointes trouve.";
 
             // Butterworth filter exceptions
             invalid_low_frequency_corner =
                 "invalid low frequency corner.";
             invalid_high_frequency_corner =
                 "invalid high frequency corner.";
             invalid_number_of_poles =
                 "invalid number of poles.";
             low_corner_greater_than_high_corner =
                 "low frequency corner greater than high frequency corner.";
             // Gaussian filter exceptions
             invalid_center_frequency =
                 "invalid center frequency.";
             invalid_alpha_value =
                 "invalid alpha value.";
 
             // help text
             HELP_TITLE = "Aide - SeisGram2K";
             HELP_TEXT = "help 1\nhelp 2\nhelp 3\nhelp 4\nhelp 5\nhelp 6\n";
 
             // about text
             ABOUT = "About SeisGram2K";
             ABOUT_TITLE = ABOUT;
 
         }
 
             // Italian, Italy
         else if (localeName.equalsIgnoreCase("it_IT")) {
 
             HELP = "Help";
             PRINT = "Stampa";
             EXIT = "Esci";
             OPEN = "Apri";
             CLOSE = "Chiudi";
             OK = "OK";
             CANCEL = "Cancella";
             FILE = "File";
             APPLY = "Applica";
             CLEAR = "Cancella";
             SAVE = "Salva";
             OPTIONS = "Opzioni";
             INIT = "Initial";
             PREV = "Precedente";
             OVERLAY = "Sovrapponi";
             SEPARATE = "Separa";
             ROTATE = "Ruota";
             PICK = "Pick";
             PROCESS = "Processa";
             REMOVE_MEAN = "RemoveMean";
             //INTEGRATE = "Traccia Integrata";
             INTEGRATE = "Integrata";
             //DIFFERENTIATE = "Traccia Differenziata";
             DIFFERENTIATE = "Differenziata";
             //DEL_ALL = "Cancella tutto";
             DEL_ALL = "Canc Tutto";
             ANGLE = "Angolo";
             AMP = "Ampiezza";
             FILTER = "Filtri";
             BUTTERWORTH_FILTER = "Filtro Butterworth";
             LOW_FREQ = "Low Freq";
             HIGH_FREQ = "High Freq";
             NUM_POLES = "Num. Poles";
             GAUSSIAN_FILTER = "Filtro Gaussian";
             CENT_FREQ = "Cent Freq";
             ALPHA = "Alpha";
             FREQ = "Frequenza";
             HILBERT = "Hilbert";
             ENVELOPE = "Envelope";
             ZNE = "Z->N,E; 0->1,2";
             FORMAT = "Formato";
             BINARY_TYPE = "Binary Type";
             URLDIALOG_TITLE = OPEN + " Canali";
             TRAVEL_TIME = "Fasi";
             DISTANCE = "Distanza";
             OTIME = "To";
            DEPTH = "Profondit";
             TRAVEL_TIME_DIALOG_TITLE = "Opzioni fasi";
            DISTANCE_UNITS = "Unit di distanza";
             DEG = "deg";
             KM = "km";
             FIX = "Fix";
             FIRST_ARRIVAL = "First Arrival";
             PHASE_LIST = "Elenco fasi";
             VIEW = "Visione";
 
             ALLIGN_TO = "Allinea a";
             ALL = "Tutti";
             ACTIVE = "Attiva";
 
             PARTICLE_MOTION = "Particle Motion";
             VIEW_FROM = "View From";
             SAMPLE = "Esempio";
             ANIMATE = "Animazione";
 
            UTILITIES = "Utilit";
             LANGUAGE = "Lingua";
             en_US = "Inglese";
             fr_FR = "Francese";
             it_IT = "Italiano";
 
             SEIS_PRINT_DIALOG_TITLE = "Opzioni di stampa";
             SEIS_PRINT_DIALOG_PROMPT = "Stampa";
             SEISMOGRAMS = "Sismogrammi";
 
             // error messages
             ERROR_MULTIPLE_PHASES_FOUND =
                 "ATTENZIONE: multiple P, S or CODA picks non trovati.";
 
             // Butterworth filter exceptions
             invalid_low_frequency_corner =
                 "invalid low frequency corner.";
             invalid_high_frequency_corner =
                 "invalid high frequency corner.";
             invalid_number_of_poles =
                 "invalid number of poles.";
             low_corner_greater_than_high_corner =
                 "low frequency corner greater than high frequency corner.";
             // Gaussian filter exceptions
             invalid_center_frequency =
                 "invalid center frequency.";
             invalid_alpha_value =
                 "invalid alpha value.";
 
             // help text
             HELP_TITLE = "Help - SeisGram2K";
             HELP_TEXT = "help 1\nhelp 2\nhelp 3\nhelp 4\nhelp5\nhelp6\n";
 
             // about text
             ABOUT = "About SeisGram2K";
             ABOUT_TITLE = ABOUT;
 
 
         }
 
             // English, USA
         else {      // default en_US
 
         }
 
         // create Locale object
         locale = toLocale();
 
     }
 
 
     /** get Locale object */
 
     public Locale getLocale() {
         return(locale);
     }
 
 
     /** convert locale to Locale object */
 
     public Locale toLocale() {
 
         String language = LANGUAGE_DEFAULT;
         String country = COUNTRY_DEFAULT;
         String variant = VARIANT_DEFAULT;
 
         String locStr = localeName;
 
         // language
         int ndx = locStr.indexOf("_");
         if (ndx > 0) {
             language = locStr.substring(0, ndx);
             locStr = locStr.substring(ndx + 1);
             //System.out.println("1 - language: <" + language + ">  country: <" + country + ">  variant: <" + variant + ">");
             // country
             ndx = locStr.indexOf("_");
             if (ndx > 0) {
                 country = locStr.substring(0, ndx);
                 locStr = locStr.substring(ndx + 1);
                 // variant
                 variant = locStr;
                 //System.out.println("2 - language: <" + language + ">  country: <" + country + ">  variant: <" + variant + ">");
             } else {
                 country = locStr;
                 //System.out.println("3 - language: <" + language + ">  country: <" + country + ">  variant: <" + variant + ">");
             }
         } else {
             language = locStr;
         }
         //System.out.println("4 - language: <" + language + ">  country: <" + country + ">  variant: <" + variant + ">");
 
         // construct Locale object
         Locale loc = new Locale(language, country, variant);
         try {
             loc.setDefault(
                 new Locale(LANGUAGE_DEFAULT, COUNTRY_DEFAULT));
         } catch (Exception e) {
         }
 
         return(loc);
 
     }
 
 
 }   // end class SeisGramText
 
