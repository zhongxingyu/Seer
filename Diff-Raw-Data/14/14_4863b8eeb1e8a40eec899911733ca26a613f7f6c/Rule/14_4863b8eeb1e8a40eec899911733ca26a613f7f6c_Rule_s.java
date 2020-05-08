 package com.pshegger.test.science;
 
 import java.util.Random;
 
 
 public class Rule {
 	private int[] rule = {Constants.RULE_BARMI, Constants.RULE_BARMI, Constants.RULE_BARMI, Constants.RULE_BARMI};
 	public boolean main;
 	Random rand = new Random(System.currentTimeMillis());
 	
 	public Rule(boolean main) {
 		this.main = main;
 		
 		int ruleType = rand.nextInt(4);
 		int value = rand.nextInt(3);
 		
 		this.rule[ruleType] = value;
 		
 		if (!this.main) {
 			int ruleType2 = rand.nextInt(4);
 			while (ruleType2 == ruleType)
 				ruleType2 = rand.nextInt(4);
 			value = rand.nextInt(3);
 			this.rule[ruleType2] = value;
 		}
 	}
 	
 	public Rule(int szin, int szam, int forma, int kitoltes) {
 		this.rule[Constants.RULE_SZIN] = szin;
 		this.rule[Constants.RULE_SZAM] = szam;
 		this.rule[Constants.RULE_FORMA] = forma;
 		this.rule[Constants.RULE_KITOLTES] = kitoltes;
 	}
 	
 	public int[] getRule() {
 		return this.rule;
 	}
 	
 	public boolean check(Rule other) {
 		int bangCount = 0;
 		for (int i = 0; i < this.rule.length; i++) {
 			if (this.rule[i] == Constants.RULE_BARMI)
 				continue;
 			if (this.rule[i] == other.getRule()[i])
 				bangCount++;
 		}
 		
 		if (this.main)
 			return (bangCount==1)?true:false;
 		else
 			return (bangCount==2)?true:false;
 	}
 	
 	public boolean conflict(Rule other) {
 		boolean conflict = false;
 		int[] otherRule = other.getRule();
 		
 		if (other.main) {
 			for (int i = 0; i < this.rule.length; i++)
 				if (otherRule[i] == this.rule[i] && this.rule[i] != Constants.RULE_BARMI)
 					conflict = true;
 		}
 		
 		if (other.toString().equalsIgnoreCase(toString()))
 			conflict = true;
 
 		int[] same = {0, 0, 0, 0};
 		int[] count = {0, 0, 0, 0};
 		for (int i = 0; i < this.rule.length; i++) {
 			if (this.rule[i] == otherRule[i] && this.rule[i] != Constants.RULE_BARMI)
 				same[i]++;
 			if (this.rule[i] != Constants.RULE_BARMI && otherRule[i] != Constants.RULE_BARMI)
 				count[i]++;
 		}
 		boolean sameTwo = false;
 		int i = 0;
 		while (i < same.length || sameTwo) {
 			if (same[i] == 2) {
 				sameTwo = true;
 				break;
 			}
 			i++;
 		}
 		if (sameTwo) {
 			for (int j = 0; j < count.length; j++) {
 				if (count[j] == 2 && j != i)
 					conflict = true;
 			}
 		}
 		
 		return conflict;
 	}
 	
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append(this.rule[0]);
 		builder.append(";");
 		builder.append(this.rule[1]);
 		builder.append(";");
 		builder.append(this.rule[2]);
 		builder.append(";");
 		builder.append(this.rule[3]);
 		
 		return builder.toString();
 	}
 	
 	public String toHumanForm() {
 		StringBuilder builder = new StringBuilder();
 		
 		int szin = this.rule[Constants.RULE_SZIN];
 		int szam = this.rule[Constants.RULE_SZAM];
 		int forma = this.rule[Constants.RULE_FORMA];
 		int kitoltes = this.rule[Constants.RULE_KITOLTES];
 		
 		if (szam == Constants.SZAM_EGY)
 			builder.append("szőrös");
 		else if (szam == Constants.SZAM_KETTO)
 			builder.append("pikkelyes");
 		else if (szam == Constants.SZAM_HAROM)
 			builder.append("tollas");
 		builder.append(";");
 		
 		if (szin == Constants.SZIN_PIROS)
 			builder.append("növényevő");
 		else if (szin == Constants.SZIN_KEK)
			builder.append("ragadozó");
		else if (szin == Constants.SZIN_ZOLD)
 			builder.append("mindenevő");
 		builder.append(";");
 		
 		if (kitoltes == Constants.KITOLTES_CSIKOS)
			builder.append("cincog");
		else if (kitoltes == Constants.KITOLTES_POTTYOS)
 			builder.append("vonyít");
		else if (kitoltes == Constants.KITOLTES_URES)
 			builder.append("béget");
 		builder.append(";");
 		
 		if (forma == Constants.FORMA_HAROMSZOG)
 			builder.append("fut");
 		else if (forma == Constants.FORMA_KOR)
 			builder.append("gurul");
 		else if (forma == Constants.FORMA_NEGYZET)
 			builder.append("úszik");
 		
 		return builder.toString();
 	}
 }
