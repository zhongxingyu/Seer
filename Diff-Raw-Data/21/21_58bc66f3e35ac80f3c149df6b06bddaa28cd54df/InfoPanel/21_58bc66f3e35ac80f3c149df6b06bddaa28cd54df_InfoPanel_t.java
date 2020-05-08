 package thrust.display;
 
 /**
  * Information about the game.
  *
  * @author Joe Kiniry (kiniry@acm.org)
  * @version 11 April 2008
  * @revised by Daire O'Doherty 06535691 15/4/08
  */
public class InfoPanel extends AbstractInfoPanel {
   /**
    * @return Is the information panel currently displayed?
    */
   public /*@ pure @*/ boolean displayed() {
    return false;
   }
 
   /**
    * Display the information panel.
    */
   //@ ensures displayed();
  public void display() {
   }
 
   /**
    * Hide the information panel.
    */
   //@ ensures !displayed();
  public void hide() {
   }
 
   /**
    * Update the displayed information panel.
    */
  public void update() {

   }
 }

