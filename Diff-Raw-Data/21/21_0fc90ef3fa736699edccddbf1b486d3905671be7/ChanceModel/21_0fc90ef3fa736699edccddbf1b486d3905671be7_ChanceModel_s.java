 package com.example.pokerchance.model;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 
 import com.example.pokerchance.Util;
 import com.example.pokerchance.data.Chance;
 
 public class ChanceModel {
 	
 	private Chance chance = new Chance();
 	
 	public interface OutsChangeListener{
 		void onOutsChange(ChanceModel chanceModel);
 	}
 	
 	private OutsChangeListener outsChangeListener;
 	
 	public void setChance(Chance chance){
 		this.chance = chance;
 	}
 
 	public void setOutsChangeListener(OutsChangeListener listener){
 		outsChangeListener = listener;
 	}
 	
 	private void notifyListener(){
 		if(outsChangeListener != null){
 			outsChangeListener.onOutsChange(this);
 		}
 	}
 	
 	public void setOuts(float outs){
 		this.chance.outs = outs;
 		this.chance.ternChance = calculate_tern_chance(outs);
 		this.chance.riverChance =  calculate_river_chance(outs);	
 		this.chance.ternAndRiverChance = calculate_tr_chance(outs);
 
 		this.chance.ternPercent = calculate_tern_percent(outs);
 		this.chance.riverPercent = calculate_river_percent(outs);
 		this.chance.ternRiverPercent = calculate_tr_percent(outs);
 		
		
 		notifyListener();
 	}
 	
 	public String getOuts(){
 		return  Util.formatUnit(this.chance.outs);
 	}
 
 	public float calculate_tern_chance(float outs){
 		return  (47 - outs) / outs;	
 	}
 	
 	public float calculate_river_chance(float outs){
 		return  (46 - outs) / outs;	
 	}
 	
 	public float calculate_tr_chance(float outs){
 		return (100 / ((1 - ((47 - outs) / 47) * ((46 - outs) / 46)) * 100)) - 1;
 	}
 	
 	public float calculate_tern_percent(float outs){
 		return  outs / 47 *100	;	
 	}
 	
 	public float calculate_river_percent(float outs){
 		return   (outs / 46) * 100	;
 	}
 	
 	public float calculate_tr_percent(float outs){
 		return  (1 - ((47 - outs) / 47) * ((46 - outs) / 46)) * 1000;
 	}
 	
 	
 	
 	public String getTernChance(){
 		return Util.formatChance(this.chance.ternChance);
 	}
 	
 	public String getRiverChance(){
 		return Util.formatChance(this.chance.riverChance);
 	}
 	
 	public String getTRChance(){
 		return Util.formatChance(this.chance.ternAndRiverChance);
 	}
 	
 	public String getTernPercent(){
 		return Util.formatPercent(this.chance.ternPercent);
 	}
 	
 	public String getRiverPercent(){
 		return Util.formatPercent(this.chance.riverPercent);
 	}
 	
 	public String getTRPercent(){
 		return Util.formatPercent(this.chance.ternPercent);
 	}
 	
 	
 	
 }
