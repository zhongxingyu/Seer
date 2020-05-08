 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller.terminal.controller;
 
 /**
  *
  * @author Valentin SEITZ
  */
 public class PayAmount {
 
 	private int duration;
 	private String durationUnit;
 	private float durationPricePerUnit;
 	private int bikeQuantity;
 	private float guaranteeAmount;
 
 	public void setDuration(int duration) {
 		this.duration = duration;
 	}
 
 	public void setDurationUnit(String durationUnit) {
 		this.durationUnit = durationUnit;
 	}
 
 	public void setDurationPricePerUnit(float durationPricePerUnit) {
 		this.durationPricePerUnit = durationPricePerUnit;
 	}
 
 	public void setBikeQuantity(int bikeQuantity) {
 		this.bikeQuantity = bikeQuantity;
 	}
 
 	public void setGuaranteeAmount(float guaranteeAmount) {
 		this.guaranteeAmount = guaranteeAmount;
 	}
 
 	public int getDuration() {
 		return duration;
 	}
 
 	public String getDurationUnit() {
 		return durationUnit;
 	}
 
 	public float getDurationPricePerUnit() {
 		return durationPricePerUnit;
 	}
 
 	public int getBikeQuantity() {
 		return bikeQuantity;
 	}
 
 	public float getRentAmount() {
		return getDurationPricePerUnit() * getBikeQuantity();
 	}
 
 	public float getGuaranteeAmount() {
 		return guaranteeAmount;
 	}
 
 	public float getTotalAmount() {
 		return getRentAmount() + getGuaranteeAmount();
 	}
 }
