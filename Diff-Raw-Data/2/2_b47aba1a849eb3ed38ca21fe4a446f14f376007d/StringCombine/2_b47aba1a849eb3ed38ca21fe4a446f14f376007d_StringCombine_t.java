 package com.mingebag.grover.falsebook.ic.datatype.string;
 
 import org.bukkit.block.Sign;
 import com.bukkit.gemo.FalseBook.IC.ICs.BaseChip;
 import com.bukkit.gemo.FalseBook.IC.ICs.ICGroup;
 import com.bukkit.gemo.FalseBook.IC.ICs.InputState;
 import com.grover.mingebag.ic.BaseData;
 import com.grover.mingebag.ic.BaseDataChip;
 import com.grover.mingebag.ic.DataTypes;
 import com.grover.mingebag.ic.StringData;
 
 public class StringCombine extends BaseDataChip {
 
     public StringCombine() {
         this.ICName = "String Combine";
         this.ICNumber = "ic.string.comb";
         setICGroup(ICGroup.CUSTOM_0);
         this.chipState = new BaseChip(false, true, true, "String", "String", "String");
         this.chipState.setOutputs("String", "", "");
         this.chipState.setLines("", "");
         this.ICDescription = "This combines strings from left to right or combines the input string with the string provided on line 4.";
     }
 
     public void Execute(Sign signBlock, InputState currentInputs, InputState previousInputs) {
         String inputLeft = null;
         String inputRight = null;
         String out = null;
        String def = signBlock.getLine(2);
 
         if (currentInputs.isInputTwoHigh() && previousInputs.isInputTwoLow()) {
             BaseData dataLeft = getDataLeft(signBlock);
             if (dataLeft != null) {
                 if (dataLeft.getType() == DataTypes.STRING) {
                     inputLeft = ((StringData) dataLeft).getString();
                 }
             }
         }
 
         if (currentInputs.isInputThreeHigh() && previousInputs.isInputThreeLow()) {
             BaseData dataRight = getDataRight(signBlock);
             if (dataRight != null) {
                 if (dataRight.getType() == DataTypes.STRING) {
                     inputRight = ((StringData) dataRight).getString();
                 }
             }
         }
 
         if (inputLeft != null && inputRight != null) {
             out = inputLeft + inputRight;
         }
 
         if (def.length() > 0 && (inputLeft != null || inputRight != null)) {
             if (inputLeft != null) {
                 out = inputLeft + def;
             }
             if (inputRight != null) {
                 out = def + inputRight;
             }
         }
         
         if (out != null)
         	this.outputData(new StringData(out), signBlock, 2);
     }
 }
