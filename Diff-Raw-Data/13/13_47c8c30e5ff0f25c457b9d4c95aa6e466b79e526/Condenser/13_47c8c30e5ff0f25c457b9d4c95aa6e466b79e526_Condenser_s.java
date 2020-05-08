 package model;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import util.InfoPacket;
 import util.Pair;
 import util.Pair.Label;
 
 
 /**
  * This class is the representation of the condenser within the power plant - This component receives steam from another component,
  * then depending on what the state the power plant condensed it back to water to use elsewhere in the power plant.
  * 
  * @author Harrison
  */
 public class Condenser extends WaterComponent {
 	private double waterLevel = 0.0;//---DEPRECATED---
 	private double coolantpumpRPM = 10.0;
 
 	/**
 	 * @see model.Component#Component(String)
 	 */
 	public Condenser(String name) {
 		super(name);
 	}
 	/**
 	 * @see model.Component#Component(String, InfoPacket)
 	 */
 	public Condenser(String name, InfoPacket info){
 		super(name, info);
 		Pair<?> currentpair = null;
 		Iterator<Pair<?>> pi = info.namedValues.iterator();
 		Label currentlabel = null;
 		while(pi.hasNext()){
 			currentpair = pi.next();
 			currentlabel = currentpair.getLabel();
 			switch (currentlabel){
 			case wLvl:
 				waterLevel = (Double) currentpair.second();
 				break;
 			case RPMs:
 				coolantpumpRPM = (Double) currentpair.second(); 
 			default:
 				break;
 			}
 		}
 	}
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public InfoPacket getInfo() {
 		InfoPacket info = super.getInfo();
 		info.namedValues.add(new Pair<Double>(Label.pres, getPressure()));
 		info.namedValues.add(new Pair<Double>(Label.wLvl, getAmount()));
 		info.namedValues.add(new Pair<Double>(Label.RPMs, getCoolantpumpRPM()));
 		return info;
 	}
 	
 	public double getCoolantpumpRPM() {
 		return coolantpumpRPM;
 	}
 	
 	public void setCoolantpumpRPM(double coolantpumpRPM) {
 		this.coolantpumpRPM = coolantpumpRPM;
 	}
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void calculate() {
 		
 		transmitOutputWater();
 		calculateTemperature();
 		checkFailed();
 //		super.setFailed(calculateFailed());
 //		if(!super.isFailed()){
 			// double oldPressure = pressure;
 			//pressure = calculatePressure();
 			//setTemperature(calculateTemp(oldPressure));
 //			waterLevel = calculateWaterLevel();
 //			super.setOuputFlowRate(calculateOutputFlowRate());
 //		}
 	}
 
 	private void calculateTemperature() {
		double heatRemoved = 1000*(1 + getCoolantpumpRPM());
 		double tempDecrease = heatRemoved/getAmount();
 		setTemperature(getTemperature() - tempDecrease);
 		
 	}
 	/**
 	 * {@inheritDoc}
 	 * Condenser only fails when it reaches it's fail time
 	 */
 	@Override
 	protected boolean checkFailed(){
 		setFailureTime(getFailureTime()-1);
 		if(super.getFailureTime() < 0){
 			setFailed(true);
 		}else if(getPressure() > 1000){
 			setFailed(true);
 		}
 		return getFailed();
 	}
 
 	/**
 	 * Calculate the temperature of the condenser, 
 	 * Temperature = old temp * ratio that pressure increased or decreased by - the coolent flow rate
 	 * @param oldPressure pressure before the last calculation of pressure.
 	 * @return The new temperature.
 	 */
 	//	protected double calculateTemp(double oldPressure){
 	//		//Temperature = old temp * pressure increase/decrease raito - coolent flow rate
 	//
 	//		ArrayList<Component> inputs = super.getRecievesInputFrom();
 	//		Iterator<Component> it = inputs.iterator();
 	//		Component c = null;
 	//
 	//		double totalCoolantFlowRate = 0;
 	//
 	//		while(it.hasNext()){
 	//			c = it.next();
 	//			if(c.getName().contains("Coolant")){
 	//				totalCoolantFlowRate += c.getOutputFlowRate();
 	//			}
 	//		}
 	//		//double ratio = pressure/oldPressure;
 	//		//return (getTemperature() * ratio - totalCoolantFlowRate);
 	//	}
 
 	/**
 	 * Calculates the pressure within the condenser
 	 * Pressure  = current pressure + input flow rate of steam - output flow rate of water.
 	 * @return The new pressure
 	 */
 	//	protected double calculatePressure(){
 	//		//The pressure of the condenser is the current pressure + input flow of steam - output flow of water.
 	//		ArrayList<Component> inputs = super.getRecievesInputFrom();
 	//		Iterator<Component> it = inputs.iterator();
 	//		Component c = null;
 	//		double totalInputFlowRate = 0;
 	//		while(it.hasNext()){
 	//			c = it.next();
 	//			if(!(c.getName().contains("Coolant"))){
 	//				totalInputFlowRate += c.getOutputFlowRate();
 	//			}
 	//		}
 	//		if(getTemperature() > 100){
 	//			return getPressure() + totalInputFlowRate - super.getOutputFlowRate();
 	//		}else{
 	//			return (pressure-pressure/getTemperature()) + totalInputFlowRate - super.getOutputFlowRate();
 	//		}
 	//	}
 	/**
 	 * Calculate the water level within the condenser
 	 * water level = steam condensed + current water level - water flow rate out.
 	 * @return The new water level.
 	 * ---DEPRECATED---
 	 */
 	protected double calculateWaterLevel(){
 		//Water level = steam condensed + water level - water out
 		double wLevel;
 		if(getTemperature() > 100){
 			wLevel = waterLevel - super.getOutputFlowRate();
 		}else{
 			wLevel = (waterLevel - super.getOutputFlowRate()) + getPressure() / 10;
 		}
 		return wLevel;
 	}
 
 
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void takeInfo(InfoPacket info) throws Exception {
 		super.takeSuperInfo(info);
 		Iterator<Pair<?>> it = info.namedValues.iterator();
 		Pair<?> pair = null;
 		Label label = null;
 		while(it.hasNext()){
 			pair = it.next();
 			label = pair.getLabel();
 			switch(label){
 			case wLvl:
 				waterLevel = (Double) pair.second();
 				break;
 			case RPMs:
 				setCoolantpumpRPM((Double) pair.second());
 				break;
 			default:
 				break;
 			}
 		}
 
 
 	}
 	@Override	
 	/**
 	 * {@inheritDoc}
 	 */
 	public InfoPacket outputWater() {
 		InfoPacket waterpack = new InfoPacket();
 		if (getTemperature() < 100){
 			double packAmount = getPressure()/10;
 			waterpack.namedValues.add(new Pair<Double>(Pair.Label.Amnt, packAmount));
 			setAmount(getAmount() - packAmount);
 			waterpack.namedValues.add(new Pair<Double>(Pair.Label.temp, getTemperature()));
 		}else{
 			double packAmount = getPressure()/10;
 			waterpack.namedValues.add(new Pair<Double>(Pair.Label.Amnt, packAmount));
 			waterpack.namedValues.add(new Pair<Double>(Pair.Label.temp, 100.0));
 			double totalHeat = getTemperature()*getAmount();
 			double remainingHeat = totalHeat - (packAmount*100.0);
 			setAmount(getAmount() - packAmount);
 			setTemperature(remainingHeat/getAmount());			
 		}
 		return waterpack;
 	}
 	@Override
 	public double maxInput() {
 		return 1987654321;
 	}
 
 }
