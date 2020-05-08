 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 /**
 * User interface for decision support application.
 * 
 * @author Josh Gillham
 * @version 10-29-12
 */
 public abstract class UserInterfaceBase implements UserInterface {
     /** Carries the answer to a yes or no question. */
     enum YNAnswer {
         Yes, No
     }
     
     /**
      * Creates a yes or no dialog displaying the message.
      * 
      * @param message is the text to display.
      * 
      * @return is an enum showing the result OR null if the user clicks the X
      */
     public abstract YNAnswer inputYNQuestion( String message );
     
     /**
      * Creates an input dialog displaying the message.
      * 
      * @param message is the text to display.
      * 
      * @return the user input OR null if the user clicked cancel or the X.
      */
     public abstract String inputQuestion( String message );
     
     /**
      * Displays a dialog with the message.
      * 
      * @param message is the text to display.
      */
     public abstract void showMessage( String message );
     
     /**
      * Accesses the choices.
      * 
      * @return the list of choices.
      */
     public List<Choice> getChoices() {
          List<Choice> choices = new ArrayList<Choice>();
          String chr = null;
          do {
              chr = inputQuestion( "Enter a canidate:" );
              if( chr != null )
                 choices.add( new Choice( chr ) );
          } while( chr != null );
          return choices;
     }
     
     /**
      * Accesses the list of characteristics.
      * 
      * @return the list of characteristics.
      */
     public List<Characteristic> getCharacteristics() {
          List<Characteristic> characteristics = 
             new ArrayList<Characteristic>();
          String chr = null;
          do {
              chr = inputQuestion( "Enter a characteristic:" );
              if( chr != null )
                 characteristics.add( new Characteristic( chr ) );
          } while( chr != null );
          return characteristics;
     }
     
     /**
      * Accesses the list of characteristics rankings.
      * 
      * @param characteristics the list of characteristics.
      * @param defaultValue hte default value for a ranking.
      */
     public void getCharacteristicRankings(List<Characteristic> characteristics,
       int defaultValue) {
          String chr = null;
          Iterator< Characteristic > i = characteristics.iterator();
          while( i.hasNext() ) {
              Characteristic aChar = i.next();
              chr = inputQuestion( "Enter the ranking for" + aChar.getName() +":" );
              if( chr != null ){
                 aChar.setRank( Integer.parseInt( chr ) );
             }
             else break;
          } 
     }
     
     /**
      * Accesses a table of the cross rankings.
      * 
      * @param choices a list of choices.
      * @param characteristics a list of characteristics.
      * @param defaultValue is the default ranking.
      * 
      * @return a table of the cross rankings.
      */
     public double[][] getCrossRankings(List<Choice> choices,
             List<Characteristic> characteristics,
             int defaultValue) {
          double[][] crossRankings =
             new double[choices.size()][characteristics.size()];
          String chr;
         for( int r = 0; r < choices.size(); ++r ) {
             for( int c = 0; c < characteristics.size(); ++c ) {
                 chr = inputQuestion( "Rank " + choices.get( r ) +
                    " in terms of " + characteristics.get( c ) + "." );
                  if( chr != null ) {
                      double rank = Double.parseDouble( chr );
                     crossRankings[r][c] = rank;
                  }
                  else break;
              }
          }
         double[] charTotals = new double[ characteristics.size() ];
         for( int c = 0; c < characteristics.size(); ++c ) {
             charTotals[c] = 0;
             for( int r = 0; r < choices.size(); ++r ) {             
                 charTotals[c] += crossRankings[r][c];
             }
         }
         for( int r = 0; r < choices.size(); ++r ) {
            for( int c = 0; c < characteristics.size(); ++c ) {
                 crossRankings[r][c] /= charTotals[c];
             }
         }

          return crossRankings;
     }
     
     /**
      * Shows the results.
      * 
      * @param choices the list of choices.
      */
     public void showResults(List<Choice> choices) {
         for( Choice choice: choices ) {
             showMessage( "The final score of " + choice.getName() + 
                 " is " + choice.getFinalScore() + "." );
         }
     }
 }
