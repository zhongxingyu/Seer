 package com.boomui.ddcharactersheet;
 
 import java.util.*;
 
 public class DiceRollSequence {
 	public class DiceRoll {
 		int multiplier;
 		int type;
 		int value;
 		Boolean isConstant;
 
 		public DiceRoll() {
 			type = 0;
 			value = 0;
 			multiplier = 0;
 			isConstant = false;
 		}
 
 		public void computeValue() {
 			if (isConstant) {
 				value = multiplier * type;
 			} else {
				value = multiplier * (int) (Math.random() * type) + 1;
 			}
 		}
 
 		public String toString() {
 			String ret = "";
 			if (isConstant) {
 				ret += multiplier + "*" + type;
 			} else {
 				if (multiplier == 1) {
 					ret += "d" + type;
 				} else {
 					ret += multiplier + "d" + type;
 				}
 			}
 			return ret;
 		}
 	}
 
 	List<DiceRoll> rollsList;
 	List<Integer> currentMultipliers;
 
 	public DiceRollSequence() {
 		rollsList = new LinkedList<DiceRoll>();
 		currentMultipliers = new LinkedList<Integer>();
 	}
 
 	public void addMultiplier(int mult) {
 		currentMultipliers.add(mult);
 	}
 
 	private void addDiceRoll(int diceType, Boolean isConstant) {
 		DiceRoll newDiceRoll = new DiceRoll();
 		if (isConstant) {
 			newDiceRoll.isConstant = true;
 		}
 		newDiceRoll.type = diceType;
 		if (currentMultipliers.size() == 0) {
 			newDiceRoll.multiplier = 1;
 		} else {
 			int multiplier = 0;
 			int size = currentMultipliers.size();
 			for (int i = size - 1; i >= 0; i--) {
 				multiplier = multiplier + (int) Math.pow(10, i)
 						* currentMultipliers.get(size - i - 1);
 			}
 			newDiceRoll.multiplier = multiplier;
 		}
 		newDiceRoll.computeValue();
 		rollsList.add(newDiceRoll);
 		currentMultipliers.clear();
 	}
 
 	public void addConstant(int constantValue) {
 		addDiceRoll(constantValue, true);
 	}
 
 	public void addDiceRoll(int diceType) {
 		addDiceRoll(diceType, false);
 	}
 
 	public String diceToString() {
 		String ret = "";
 		DiceRoll cur;
 		if (rollsList.isEmpty()) {
 			if(!currentMultipliers.isEmpty()){
 				for(int i = 0; i < currentMultipliers.size(); i++){
 					ret += currentMultipliers.get(i);
 				}
 			}
 			return ret;
 		}
 		for (int i = 0; i < rollsList.size() - 1; i++) {
 			cur = rollsList.get(i);
 			ret += cur.toString() + " + ";
 		}
 		cur = rollsList.get(rollsList.size() - 1);
 		ret += cur.toString();
 		if(!currentMultipliers.isEmpty()){
 			ret += " + ";
 			for(int i = 0; i < currentMultipliers.size(); i++){
 				ret += currentMultipliers.get(i);
 			}
 		}
 		return ret;
 	}
 
 	public String valuesToString() {
 		String ret = "";
 		DiceRoll cur;
 		if (rollsList.isEmpty()) {
 			return ret;
 		}
 		for (int i = 0; i < rollsList.size() - 1; i++) {
 			cur = rollsList.get(i);
 			ret += cur.value + " + ";
 		}
 		cur = rollsList.get(rollsList.size() - 1);
 		ret += cur.value;
 		return ret;
 	}
 
 	public int totalValueOfSequence() {
 		int totalValue = 0;
 		DiceRoll cur;
 		for (int i = 0; i < rollsList.size(); i++) {
 			cur = rollsList.get(i);
 			totalValue += cur.value;
 		}
 		return totalValue;
 	}
 
 	public String finalToString() {
 		return diceToString() + " = " + valuesToString() + " = "
 				+ totalValueOfSequence();
 	}
 	
 	public void clearSequence(){
 		rollsList.clear();
 		currentMultipliers.clear();
 	}
 	
 	public String sequenceDone(){
 		//TODO save it out or return string or something
 		String ret = finalToString();
 		clearSequence();
 		return ret;
 	}
 
 }
