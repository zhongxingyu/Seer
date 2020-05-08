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
  * Location : edu.cubesta.ressource
  * Class : Dialog.java
  */
 
 package edu.cubesta.ressources;
 
 import edu.cubesta.timer.Average;
 import javax.swing.JOptionPane;
 
 /**
  * Permet de gérer le contenu de la boîte de dialogue d'aide des mouvements
  * @author julien.gardet
  */
 
 public class Dialog {
     
     /**
      * Variables super globales
      */
     
     private static int numberOfScramble;
     private static int numberOfTime;
     private static int inspectionTime;
     
     /**
      * Définit les variable par défaut
      */
 
     public Dialog() {
         numberOfScramble = 25;
         numberOfTime = 5;
         inspectionTime = 15000;
     }
     
     /**
      * Affiche une boîte de dialogue pour changer le nombre de mouvements pour le mélange
      * @return 
      * le nombre entré
      */
     
     public static int setNOS(){
         int retour = numberOfScramble;
         try{
             retour = Integer.valueOf(showInputDialog(L10n.getLanguage(5) + " - CubeSTA", L10n.getLanguage(7))).intValue();
         } catch (NumberFormatException ex) {
             showErrorDialog(L10n.getLanguage(13) + " - CubeSTA", L10n.getLanguage(12));
         }
         if(retour < 3){
             showAlertDialog(L10n.getLanguage(6) + " - CubeSTA", L10n.getLanguage(9));
         }else{
            Average.changeAverageSize(retour);
             setNumberOfScramble(retour);
         }
         return retour;
     }
     
     /**
      * Affiche un boîte de dialogue pour changer le nombre de temps pour le calcul de l'average
      * @return 
      * la valeur entrée
      */
     
     public static int setNOT(){
         int retour = numberOfTime;
         try{
             retour = Integer.valueOf(showInputDialog(L10n.getLanguage(5) + " - CubeSTA", L10n.getLanguage(8))).intValue();
         } catch (NumberFormatException ex) {
             showErrorDialog(L10n.getLanguage(13) + " - CubeSTA", L10n.getLanguage(12));
         }
         if(retour < 3){
             showAlertDialog(L10n.getLanguage(6) + " - CubeSTA", L10n.getLanguage(9));
         }else{
             Average.changeAverageSize(retour);
             setNumberOfTime(retour);
         }
         return retour;
     }
     
     /**
      * Permet de modifier la langue
      */
     
     public static void setLAN(){
         try{
             L10n.setLanguage((String)JOptionPane.showInputDialog(null,L10n.getLanguage(10),L10n.getLanguage(5) + " - CubeSTA", JOptionPane.QUESTION_MESSAGE, null, L10n.listLanguage(), L10n.listLanguage()[0]));
         } catch (NullPointerException ex) {
         }
     }
     
     /**
      * Permet de modifier le temps d'inspection
      * @return 
      * la valeur entrée
      */
     
     public static int setINS(){
         int retour = inspectionTime;
         try{
             retour = Integer.valueOf(showInputDialog(L10n.getLanguage(5) + " - CubeSTA", L10n.getLanguage(17))).intValue()*1000;
             setInspectionTime(retour);
         } catch (NumberFormatException ex) {
             showErrorDialog(L10n.getLanguage(13) + " - CubeSTA", L10n.getLanguage(12));
         }
         return retour;
     }
     
     /**
      * Permet de générer une boîte de dialogue avec une entrée
      * @param title
      * Titre de la fenêtre
      * @param question
      * Question à poser
      * @return 
      * la valeur entrée
      */
     
     private static String showInputDialog(String title, String question){
         String answer = JOptionPane.showInputDialog(null, question, title, JOptionPane.QUESTION_MESSAGE);
         return answer;
     }
     
     /**
      * Affiche une boîte de dialogue d'alerte
      * @param title
      * Titre de la fenêtre
      * @param question 
      * Question à poser
      */
     
     private static void showAlertDialog(String title, String question){
         JOptionPane.showMessageDialog(null, question, title, JOptionPane.WARNING_MESSAGE);
     }
     
     /**
      * Affiche une boîte de dialogue d'erreur
      * @param title
      * Titre de la fenêtre
      * @param question 
      * Question à poser
      */
     
     private static void showErrorDialog(String title, String question){
         JOptionPane.showMessageDialog(null, question, title, JOptionPane.ERROR_MESSAGE);
     }
     
     /**
      * Permet d'obtenir la variable du nombre de mouvements pour les mélanges
      * @return 
      * la valeur de la variable
      */
 
     public static int getNumberOfScramble() {
         return numberOfScramble;
     }
     
     /**
      * Permet de définir la variable du nombre de mouvements du mélange
      * @param numberOfScramble 
      * Valeur à définir pour la variable
      */
 
     private static void setNumberOfScramble(int numberOfScramble) {
         Dialog.numberOfScramble = numberOfScramble;
     }
     
     /**
      * Permet d'obtenir la variable du nombre de temps pour l'average
      * @return 
      * la valeur de la variable
      */
 
     public static int getNumberOfTime() {
         return numberOfTime;
     }
     
     /**
      * Permet de définir la variable du nombre de temps pour l'average
      * @param numberOfTime 
      * Valeur à définir pour la variable
      */
 
     private static void setNumberOfTime(int numberOfTime) {
         Dialog.numberOfTime = numberOfTime;
     }
     
     /**
      * Permet d'obtenir la valeur de la variable du temps d'inspection
      * @return 
      * sa valeur
      */
 
     public static int getInspectionTime() {
         return inspectionTime;
     }
     
     /**
      * Permet de définir la variable de temps d'inspection
      * @param inspectionTime 
      * la nouvelle valeur à définir
      */
 
     private static void setInspectionTime(int inspectionTime) {
         Dialog.inspectionTime = inspectionTime;
     }
 }
