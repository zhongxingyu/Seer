 package model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import util.InfoPacket;
 import util.MaxInputComparator;
 import util.Pair;
 import util.Pair.Label;
 
 /**
  * A general component that has water flowing through, all components within the power plant that
  * have water flowing through them are children of this class.
  * Contains all common attributes and methods of components that relate to the flow of water.
  * 
  * @author Harrison
  */
 public abstract class WaterComponent extends Component {
 	private Double temperature = 50.0;
 	private Double amount = 500.0;
 	private Double volume = 500.0;
 	private ArrayList<WaterComponent> outputsWaterTo = new ArrayList<WaterComponent>();
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	public WaterComponent(String name) {
 		super(name);	
 	}
 
 	/** 
 	 * {@inheritDoc}
 	 * sets attributes: temperature, volume and amount.
 	 */
 	public WaterComponent(String name, InfoPacket info) {
 		super(name, info);
 		Pair<?> currentpair = null;
 		Iterator<Pair<?>> pi = info.namedValues.iterator();
 		Label currentlabel = null;
 		while(pi.hasNext()){
 			currentpair = pi.next();
 			currentlabel = currentpair.getLabel();
 			switch (currentlabel){
 			case temp:
 				temperature = (Double) currentpair.second();
 				break;
 			case Vlme:
 				volume = (Double) currentpair.second();
 				break;
 			case Amnt:
 				amount = (Double) currentpair.second();
 				break;
 			default:
 				break;
 			}
 		}
 	}
 	/** 
 	 * {@inheritDoc}
 	 * returns volume, amount and temperature
 	 */
 	@Override
 	public InfoPacket getInfo() {
 		InfoPacket wcompinfo = super.getInfo();
 		wcompinfo.namedValues.add(new Pair<Double>(Label.OPFL, getOutputFlowRate()));
 		wcompinfo.namedValues.add(new Pair<Double>(Label.Vlme, getVolume()));
 		wcompinfo.namedValues.add(new Pair<Double>(Label.Amnt, getAmount()));
 		wcompinfo.namedValues.add(new Pair<Double>(Label.temp, getTemperature()));
 		return wcompinfo;
 	}
 	
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public abstract void calculate();
 	
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected abstract boolean checkFailed();
 
 
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void takeInfo(InfoPacket info) throws Exception {
 		super.takeInfo(info);
 		resetConections();
 		Iterator<Pair<?>> i = info.namedValues.iterator();
 		Pair<?> pair = null;
 		Label label = null;
 		while(i.hasNext()){
 			pair = i.next();
 			label = pair.getLabel();
 			switch (label){
 			case Amnt:
 				setAmount((Double) pair.second());
 				break;
 			case Vlme:
 				setVolume((Double) pair.second());
 				break;
 			case temp:
 				setTemperature((Double) pair.second());
 				break;	
 			default:
 				break;
 			}
 		}
 
 	}
 
 	/**
 	 * @param amnt Change amount of steam/water in component to this.
 	 */
 	public void setAmount(double amnt){
 		amount = amnt;
 	}
 	
 	/** 
 	 * {@inheritDoc}
 	 * also adds component to the list of components that this object outputs water to
 	 */
 	public void connectToOutput(Component component){
 		super.connectToOutput(component);
 		if (component instanceof WaterComponent){
 			outputsWaterTo.add((WaterComponent)component);
 		}	
 	}
 
 	/**
 	 * @return The amount of steam/water of the component
 	 */
 	public double getAmount(){ return amount;}
 
 	/**
 	 * @param vlme Change volume of component to this.
 	 */
 	public void setVolume(double vlme){
 		volume = vlme;
 	}
 	/**
 	 * @return The volume of the component
 	 */
 	public double getVolume(){ return volume;}
 
 	/**
 	 * @param temp Change temperature of component to this.
 	 */
 	public void setTemperature(double temp){
		if (temp <0){
			temperature = 0.0;
		}else{
			temperature = temp;
		}
 	}
 	/**
 	 * @return The temperature of the component
 	 */
 	public double getTemperature(){ return temperature;}
 
 	/**
 	 * @return The pressure of the component, no actual variable as pressure is just a function of temp Amnt and Vlme
 	 */
 	public double getPressure() {
 		return (getAmount()*getTemperature())/getVolume();
 	}
 	
 	/**
 	 * Recieves an amount/temp waterpacket
 	 * @param info: InfoPacket containing a Temp and an Amnt
 	 */
 	public void recieveWater(InfoPacket info) {
 		double packetTemp = 0.0;
 		double packetAmount = 0.0;
 		Pair<?> currentpair = null;
 		Iterator<Pair<?>> pi = info.namedValues.iterator();
 		Label currentlabel = null;
 		while(pi.hasNext()){
 			currentpair = pi.next();
 			currentlabel = currentpair.getLabel();
 			switch (currentlabel){
 			case temp:
 				packetTemp = (Double) currentpair.second();
 				break;
 			case Amnt:
 				packetAmount = (Double) currentpair.second();
 				break;
 			default:
 				break;
 			}
 		}	
 		double totalPacketHeat = packetTemp * packetAmount;
 		double totalInternalHeat = getTemperature() * getAmount();
 		double totalWaterAmount = getAmount() + packetAmount;
 		if (totalWaterAmount != 0){
 			setTemperature((totalPacketHeat+ totalInternalHeat)/totalWaterAmount);			
 		}
 		setAmount(totalWaterAmount);
 	}	
 	
 	/** 
 	 * sorts the list of components that this outputs water to by the maximum input they accept
 	 */
 	public void sortOutputs(){
 		Collections.sort( (List<WaterComponent>) outputsWaterTo, new MaxInputComparator() );
 	}
 
 	/** 
 	 * method to work out how much water should go to each component that this component outputs to
 	 */
 	public void transmitOutputWater(){
 		InfoPacket info = outputWater();
 		Double watertemperature = 0.0;
 		Double totalOutput = 0.0;
 		Pair<?> currentPair;
 		Label currentLabel;
 		Iterator<Pair<?>> pairIt = info.namedValues.iterator();
 		while (pairIt.hasNext()) {
 			currentPair = pairIt.next();
 			currentLabel = currentPair.getLabel();
 			switch (currentLabel) {
 			case Amnt:
 				totalOutput = (Double) currentPair.second();
 				break;
 			case temp:
 				watertemperature = (Double) currentPair.second();
 				break;
 			default:
 				break;
 			}
 		}
 		double leftToOutput = totalOutput;
 		int count = outputsWaterTo.size();
 		double outputPerComponent;
 		WaterComponent comp;
 		sortOutputs();
 		Iterator<WaterComponent> cIt = outputsWaterTo.iterator();
 		while(cIt.hasNext()){
 			comp = cIt.next();
 			outputPerComponent = (leftToOutput / count);
 			if (outputPerComponent > comp.maxInput()){
 				leftToOutput = leftToOutput - comp.maxInput();// this is the bit where water is transferred
 				InfoPacket waterpack = new InfoPacket();
 				waterpack.namedValues.add(new Pair<Double>(Pair.Label.Amnt, comp.maxInput()));
 				waterpack.namedValues.add(new Pair<Double>(Pair.Label.temp, watertemperature));
 				comp.recieveWater(waterpack);
 			}else{
 				leftToOutput = leftToOutput - outputPerComponent;// this is the bit where water is transferred
 				InfoPacket waterpack = new InfoPacket();
 				waterpack.namedValues.add(new Pair<Double>(Pair.Label.Amnt, outputPerComponent));
 				waterpack.namedValues.add(new Pair<Double>(Pair.Label.temp, watertemperature));
 				comp.recieveWater(waterpack);
 			}
 			count = count - 1;
 		}
 		setOuputFlowRate(totalOutput - leftToOutput);
 		InfoPacket waterpack = new InfoPacket();
 		waterpack.namedValues.add(new Pair<Double>(Pair.Label.Amnt, leftToOutput));
 		waterpack.namedValues.add(new Pair<Double>(Pair.Label.temp, watertemperature));
 		this.recieveWater(waterpack);
 	}
 
 	/** 
 	 * @return The maximum amount of water this component can accept
 	 */
 	public abstract double maxInput();
 
 	/** 
 	 *  @return the amount of water the component is trying to get rid of this gametick
 	 */
 	public abstract InfoPacket outputWater();
 }
