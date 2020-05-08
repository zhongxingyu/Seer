 package denoflionsx.DenPipes.AddOns.AutomaticWoodenPipe.Pipe;
 
 import buildcraft.api.core.IIconProvider;
 import buildcraft.api.core.Position;
 import buildcraft.api.gates.IAction;
 import buildcraft.core.utils.Utils;
 import buildcraft.transport.IPipeTransportItemsHook;
 import buildcraft.transport.TravelingItem;
 import buildcraft.transport.pipes.PipeItemsWood;
 import denoflionsx.DenPipes.API.Annotations.PipeName;
import denoflionsx.DenPipes.API.DenPipesAPI;
 import denoflionsx.DenPipes.AddOns.AutomaticWoodenPipe.Action.Extract;
 import denoflionsx.DenPipes.AddOns.AutomaticWoodenPipe.Action.Extract32;
 import denoflionsx.DenPipes.AddOns.AutomaticWoodenPipe.Action.Extract64;
 import denoflionsx.DenPipes.AddOns.AutomaticWoodenPipe.AutoWoodenAddon;
 import denoflionsx.DenPipes.AddOns.AutomaticWoodenPipe.Client.AutoWoodenPipeIconProvider;
 import java.util.LinkedList;
 import java.util.Map;
 import net.minecraftforge.common.ForgeDirection;
 
 @PipeName(name = "Automatic Wooden Pipe")
 public class AutomaticWoodenPipe extends PipeItemsWood implements IPipeTransportItemsHook {
 
     public AutomaticWoodenPipe(int itemID) {
         super(itemID);
     }
 
     @Override
     public LinkedList<IAction> getActions() {
         LinkedList<IAction> a = super.getActions();
         a.add(AutoWoodenAddon.extract);
         a.add(AutoWoodenAddon.extractx32);
         a.add(AutoWoodenAddon.extractx64);
         return a;
     }
 
     @Override
     protected void actionsActivated(Map<IAction, Boolean> actions) {
         super.actionsActivated(actions);
         for (IAction a : actions.keySet()) {
             if (a instanceof Extract) {
                 // Why the fuck is powerHandler protected in here but private in PipeFluidsWood?!
                 if (actions.get(AutoWoodenAddon.extract)) {
                     this.powerHandler.setEnergy(1.5f);
                 }
             } else if (a instanceof Extract32) {
                 if (actions.get(AutoWoodenAddon.extractx32)) {
                     this.powerHandler.setEnergy(32.5f);
                 }
             } else if (a instanceof Extract64) {
                 if (actions.get(AutoWoodenAddon.extractx64)) {
                     this.powerHandler.setEnergy(64.5f);
                 }
             }
         }
     }
 
     @Override
     public LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos, TravelingItem item) {
         return possibleOrientations;
     }
 
     @Override
     public void entityEntered(TravelingItem item, ForgeDirection orientation) {
     }
 
     @Override
     public void readjustSpeed(TravelingItem item) {
        item.setSpeed(DenPipesAPI.normalPipeSpeed * 20);
     }
 
     @Override
     public IIconProvider getIconProvider() {
         return AutoWoodenAddon.pipes;
     }
 
     @Override
     public int getIconIndex(ForgeDirection direction) {
         if (direction == ForgeDirection.UNKNOWN) {
             return AutoWoodenPipeIconProvider.icon_AutomaticWoodenPipe;
         } else {
             int metadata = this.getWorld().getBlockMetadata(this.getContainer().xCoord, this.getContainer().yCoord, this.getContainer().zCoord);
 
             if (metadata == direction.ordinal()) {
                 return AutoWoodenPipeIconProvider.icon_AutomaticWoodenPipe_Overlay;
             } else {
                 return AutoWoodenPipeIconProvider.icon_AutomaticWoodenPipe;
             }
         }
     }
 }
