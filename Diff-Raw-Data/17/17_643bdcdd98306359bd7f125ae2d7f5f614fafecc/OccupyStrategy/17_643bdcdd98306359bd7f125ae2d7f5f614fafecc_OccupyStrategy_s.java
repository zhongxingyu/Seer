 package vooga.rts.gamedesign.strategy.occupystrategy;
 
 import vooga.rts.gamedesign.sprite.Building;
 import vooga.rts.gamedesign.sprite.Unit;
 import vooga.rts.gamedesign.sprite.rtsprite.interactive.IOccupiable;
import vooga.rts.gamedesign.sprite.rtsprite.interactive.units.Units;
 
 /**
  * This interface is implemented by the classes CanOccupy and CannotOccupy that
  * are then used as instance variables in the classes that could possibly
  * occupy IOccupiables.  If the unit currently can occupy, it will have an 
  * instance of CanOccupy, otherwise it will have an instance of CannotOccupy.  
  * Using the strategy pattern like this, interactives ability to occupy can be
  * dynamically changed.  For example, a unit may be implemented such that 
  * it can not occupy an IOccupiable until an upgrade is bought.  The unit will 
  * initially have an instance of CannotOccupy but once the upgrade is bought 
  * it will switch to have an instance of CanOccupy.
  * 
  * @author Ryan Fishel
  * @author Kevin Oh
  * @author Francesco Agosti
  * @author Wenshun Liu 
  *
  */
 public interface OccupyStrategy {
 
   public void occupy(IOccupiable o);
 
   public void occupy(Unit unit);
 
 }
