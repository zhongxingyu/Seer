 package playacem.allrondism.tileentity;
 
 /**
  * Allrondism
  * 
  * IExtensionSlot
  * 
  * provides functions for the TileEntites, which unlock certain slots
  * 
  * @author Playacem
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
public interface IExtensionSlot extends ICoreExtension{
 
     /** return the type of slot here */
     public EnumSlotType getSlotType();
     /** how many slots shall be added by this specific TE?*/
     public int getAmountAdditionalSlots();
 }
