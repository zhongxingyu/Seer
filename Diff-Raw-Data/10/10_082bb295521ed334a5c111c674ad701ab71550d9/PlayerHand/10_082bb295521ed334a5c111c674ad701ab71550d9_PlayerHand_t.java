 package com.mingebag.grover.falsebook.ic.datatype.player;
 
 import org.bukkit.block.Sign;
 import com.bukkit.gemo.FalseBook.IC.ICs.BaseChip;
 import com.bukkit.gemo.FalseBook.IC.ICs.ICGroup;
 import com.bukkit.gemo.FalseBook.IC.ICs.InputState;
 import com.grover.mingebag.ic.BaseData;
 import com.grover.mingebag.ic.BaseDataChip;
 import com.grover.mingebag.ic.DataTypes;
 import com.grover.mingebag.ic.ItemData;
 import com.grover.mingebag.ic.PlayerData;
 
 public class PlayerHand extends BaseDataChip {
 
     public PlayerHand() {
         this.ICName = "PLAYER ITEM IN HAND";
         this.ICNumber = "ic.player.hand";
         setICGroup(ICGroup.CUSTOM_0);
         this.chipState = new BaseChip(true, false, false, "Player", "", "");
         this.chipState.setOutputs("Item", "", "");
         this.chipState.setLines("", "");
         this.ICDescription = "This pulses the item the player has in his hand";
     }
 
     public void Execute(Sign signBlock, InputState currentInputs, InputState previousInputs) {
         if (currentInputs.isInputOneHigh() && previousInputs.isInputOneLow()) {
             BaseData data = getData(signBlock);
             if (data.getType() == DataTypes.PLAYER) {
                 PlayerData player = (PlayerData) data;
                 this.outputData(new ItemData(player.getPlayer().getItemInHand()), signBlock, 2);
             }
         }
     }
 }
