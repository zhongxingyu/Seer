 package com.mingebag.grover.falsebook.ic.datatype.string;
 
 import org.bukkit.block.Sign;
 import com.bukkit.gemo.FalseBook.IC.ICs.BaseChip;
 import com.bukkit.gemo.FalseBook.IC.ICs.ICGroup;
 import com.bukkit.gemo.FalseBook.IC.ICs.InputState;
 import com.grover.mingebag.ic.BaseDataChip;
 import com.grover.mingebag.ic.StringData;
 
 public class MC002S extends BaseDataChip {
 	public MC002S() {
 		this.ICName = "STRING COMBINE";
 		this.ICNumber = "[MC002S]";
 		setICGroup(ICGroup.CUSTOM_0);
 		this.chipState = new BaseChip(false, true, true, "String", "String", "String");
 		this.chipState.setOutputs("String", "", "");
 		this.chipState.setLines("", "");
 		this.ICDescription = "This combines strings from left to right.";
 	}
 
 
 	public void Execute(Sign signBlock, InputState currentInputs, InputState previousInputs) {
 		String input1 = null;
 		String input2 = null;
 		String out = null;
 		if(currentInputs.isInputTwoHigh() && previousInputs.isInputTwoLow())
 			input1 = ((StringData) getDataLeft(signBlock)).getString();
 		
		if(currentInputs.isInputThreeHigh() && previousInputs.isInputThreeLow())
 			input2 = ((StringData) getDataRight(signBlock)).getString();
 		
 		if(input1 != null && input2 != null) {
 			out = input1 + input2;
 		} else {
 			return;
 		}
 		
 		this.outputData(new StringData(out), signBlock, 2, 2);
 	}
 }
