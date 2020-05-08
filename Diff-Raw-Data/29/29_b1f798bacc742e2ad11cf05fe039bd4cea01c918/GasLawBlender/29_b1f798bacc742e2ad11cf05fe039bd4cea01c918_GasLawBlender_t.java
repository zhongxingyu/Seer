 /**
  * 
  */
 package uk.co.craigwarren.gascalc.blender;
 
 import java.math.BigDecimal;
 import java.util.Map;
 
 import com.google.common.collect.Maps;
 
 import uk.co.craigwarren.gascalc.exception.ImpossibleMixException;
 import uk.co.craigwarren.gascalc.gaslaw.GasLaw;
 import uk.co.craigwarren.gascalc.model.Cylinder;
 import uk.co.craigwarren.gascalc.model.Element;
 import uk.co.craigwarren.gascalc.model.FillRequirement;
 import uk.co.craigwarren.gascalc.model.Gas;
 import uk.co.craigwarren.gascalc.model.filling.FillAction;
 import uk.co.craigwarren.gascalc.model.filling.FillPlan;
 import uk.co.craigwarren.gascalc.model.filling.FillStep;
 
 /**
  * A Gas Blender which uses a gas law to calculate Fill Plans
  * 
  * @author craig
  *
  */
 public class GasLawBlender implements GasBlender {
 
     private GasLaw gasLaw;
     
     public GasLawBlender(GasLaw law) {
         if(null == law) {
             throw new IllegalArgumentException("GasLaw law cannot be null");
         }
         gasLaw = law;
     }
 
     public FillPlan blendGas(FillRequirement requirement) throws ImpossibleMixException {
         Map<Element,Double> targetMolByElement = getTargetMolsByElement(requirement);
         Map<Element,Double> availableMolsByElement = getAvailableMolsByElement(requirement.getStartingCylinder());
         Map<Element,Double> requiredMolsByElement = 
                 subtractMolsByElement(targetMolByElement, availableMolsByElement);
         FillPlan plan = new FillPlan();
         FillStep drain = getDrainAction(requirement, requiredMolsByElement, availableMolsByElement, targetMolByElement);
         if(null != drain) {
             //OK so we need to drain the cylinder first and then generate a plan based on the new starting pressure
             requirement.getStartingCylinder().setPressure(drain.getPressure());
             targetMolByElement = getTargetMolsByElement(requirement);
             availableMolsByElement = getAvailableMolsByElement(requirement.getStartingCylinder());
             requiredMolsByElement = subtractMolsByElement(targetMolByElement, availableMolsByElement);
             plan.addFirst(drain);
         }
         
         requiredMolsByElement = removeOxygenFromAir(requiredMolsByElement, requirement.getTargetGas());
 
         Cylinder cylinder = requirement.getStartingCylinder();
         
         if(requiredMolsByElement.containsKey(Element.HELIUM) && requiredMolsByElement.get(Element.HELIUM) > 0.0){
             plan.add(addGasToCylinder(cylinder, Element.HELIUM, requiredMolsByElement.get(Element.HELIUM)));
         }
         if(requiredMolsByElement.containsKey(Element.OXYGEN) && requiredMolsByElement.get(Element.OXYGEN) > 0.0) {
             plan.add(addGasToCylinder(cylinder, Element.OXYGEN, requiredMolsByElement.get(Element.OXYGEN)));
         }
         plan.add(new FillStep(FillAction.FILL, requirement.getTargetPressure(), Gas.air()));
         
         return plan;
     }
     
     /**
      * Adds the required element to the cylinder and returns a fill step indicating the action to take
      * 
      * @param cylinder
      * @param element
      * @param mols
      * @return
      */
     private FillStep addGasToCylinder(Cylinder cylinder, Element element, double molsToAdd) {
         Map<Element, Double> molsByElement = getAvailableMolsByElement(cylinder);
         double currentMols = 0.0;
         if(molsByElement.containsKey(element)) {
             currentMols = molsByElement.get(element);
         }
         currentMols = currentMols + molsToAdd;
         molsByElement.put(element, currentMols);
         Gas newGas = new Gas(molsByElement);
         gasLaw.setGas(newGas);
         double newPressure = gasLaw.getPressureBar(cylinder.getVolume(), getTotalAmount(molsByElement), cylinder.getAmbientTemperatureKelvin());
         BigDecimal decimal = new BigDecimal(newPressure).setScale(1, BigDecimal.ROUND_DOWN);
         FillStep step = new FillStep(FillAction.FILL, decimal.doubleValue(), new Gas(element));
         cylinder.setGas(newGas);
         cylinder.setPressure(newPressure);
         
         return step;
     }
     
     /**
      * @param molsByElement the collection of mols separated by element
      * @return the sum of all mols
      */
     private double getTotalAmount(Map<Element,Double> molsByElement) {
         double sum = 0.0;
         for (Double amount : molsByElement.values()) {
             sum = sum + amount;
         }
         return sum;
     }
     
     private Map<Element,Double> removeOxygenFromAir(Map<Element,Double> molsByElement, Gas targetGas) throws ImpossibleMixException {
         if(molsByElement.containsKey(Element.NITROGEN)) {
             double freeOxygen = getMolsOfOxygenFromAir(molsByElement.get(Element.NITROGEN));
             if(molsByElement.containsKey(Element.OXYGEN)) {
                 double oxygenMols = molsByElement.get(Element.OXYGEN);
                 oxygenMols = oxygenMols - freeOxygen;
                if(oxygenMols < -0.0000001) {
                     throw new ImpossibleMixException(targetGas);
                 }
                 molsByElement.put(Element.OXYGEN, oxygenMols);
             } else {
                 throw new ImpossibleMixException(targetGas);
             }
         }
         return molsByElement;
     }
     
     private FillStep getDrainAction(FillRequirement requirement, Map<Element,Double> requiredMolsByElement, Map<Element,Double> availableMolsByElement, Map<Element,Double> targetMolsByElement) {
         
     	double startAirTopupMols = getMolsBeforeAirTopup(requirement.getStartingCylinder().getGas(), requirement.getTargetGas(), getTargetMols(requirement));
         double availableMols = getAvailableMols(requirement.getStartingCylinder());
     	
         double requiredHelium = 0.0;
         double availableHelium = 0.0;
         if(availableMolsByElement.containsKey(Element.HELIUM)) {
         	availableHelium = availableMolsByElement.get(Element.HELIUM);
         	if(requiredMolsByElement.containsKey(Element.HELIUM)){
         		requiredHelium = requiredMolsByElement.get(Element.HELIUM);
         	}
         }
        double heliumDiff = availableHelium + requiredHelium;
         
         if(heliumDiff >= 0.0 && startAirTopupMols > (availableMols+heliumDiff)) {
         	//No need to drain as we have
             return null;
         }
         
         double heliumMolDrain = 0.0;
         if(heliumDiff<0.0) {
         	heliumMolDrain = Math.abs(heliumDiff)/requirement.getStartingCylinder().getGas().getDecimalRatio(Element.HELIUM);
         }
         double molsAfterHeliumDrain = availableMols - heliumMolDrain;
         
         double molsDrainTo = Math.min(molsAfterHeliumDrain, startAirTopupMols);
         gasLaw.setGas(requirement.getStartingCylinder().getGas());
         double barDrainTo = gasLaw.getPressureBar(
         		requirement.getStartingCylinder().getVolume(),
         		molsDrainTo, requirement.getStartingCylinder().getAmbientTemperatureKelvin());
         BigDecimal decimal = new BigDecimal(barDrainTo).setScale(1, BigDecimal.ROUND_DOWN);
         
         return new FillStep(FillAction.DRAIN, decimal.doubleValue(), requirement.getStartingCylinder().getGas());
         
     }
     
     private double getMolsOfOxygenFromAir(Double molsOfNitrogen) {
         return (molsOfNitrogen/Gas.air().getDecimalRatio(Element.NITROGEN))*Gas.air().getDecimalRatio(Element.OXYGEN);
     }
     
     /**
      * Performs subtraction of doubles between two maps, but only subtract doubles where they have matching key elements
      * *Note: elements in the subtrahend but not the minuend will result in a negative number
      * 
      * @param minuend the Map of doubles to subtract from
      * @param subtrahend the map of doubles to subtract
      * @return a map containing the minuend doubles minus the subrahend doubles
      */
     private Map<Element, Double> subtractMolsByElement(Map<Element,Double> minuend, Map<Element,Double> subtrahend) {
         Map<Element,Double> result = Maps.newEnumMap(Element.class);
         for (Element minuendElement : minuend.keySet()) {
             double minuendMols = minuend.get(minuendElement);
             double resultMols = minuendMols;
             if(subtrahend.containsKey(minuendElement)) {
                 resultMols = minuendMols - subtrahend.get(minuendElement);
             }
             result.put(minuendElement, resultMols);
         }
         for (Element subtrahendElement : subtrahend.keySet() ) {
             if(!result.containsKey(subtrahendElement)) {
                 result.put(subtrahendElement, 0.0 - subtrahend.get(subtrahendElement));
             }
         }
         return result;
     }
     
     private Map<Element, Double> getTargetMolsByElement(FillRequirement requirement) throws ImpossibleMixException {
         Map<Element, Double> result = getMolsByElement(requirement.getTargetGas(), getTargetMols(requirement));
         checkForImpossibleMix(requirement.getTargetGas(),result);
         return result;
     }
     
     private  Map<Element, Double> getAvailableMolsByElement(Cylinder cylinder) {
         return getMolsByElement(cylinder.getGas(), getAvailableMols(cylinder));
     }
     
     private double getAvailableMols(Cylinder cylinder) {
         gasLaw.setGas(cylinder.getGas());
         return gasLaw.getMolsOfGas(cylinder.getPressure(), cylinder.getVolume(), cylinder.getAmbientTemperatureKelvin());
     }
     
     private double getTargetMols(FillRequirement requirement) {
         gasLaw.setGas(requirement.getTargetGas());
         return gasLaw.getMolsOfGas(requirement.getTargetPressure(), requirement.getStartingCylinder().getVolume(), requirement.getStartingCylinder().getAmbientTemperatureKelvin());
     }
 
     
     private Map<Element, Double> getMolsByElement(Gas gas, double mols) {
         Map<Element,Double> result = Maps.newEnumMap(Element.class);
         for (Element element : gas.getComponents()) {
             result.put(element, gas.getDecimalRatio(element)*mols);
         }
         return result;
     }
     
     private void checkForImpossibleMix(Gas gas, Map<Element, Double> targetMolsByElement) throws ImpossibleMixException {
         if( targetMolsByElement.containsKey(Element.NITROGEN)) {
             double nitrogenMolsRequired = targetMolsByElement.get(Element.NITROGEN);
             double oxygenMolsFree = getMolsOfOxygenFromAir(nitrogenMolsRequired);
             double oxygenMolsRequired = 0.0;
             
             if(targetMolsByElement.containsKey(Element.OXYGEN)) {
                 oxygenMolsRequired = targetMolsByElement.get(Element.OXYGEN);
             }
             
            if((oxygenMolsFree - oxygenMolsRequired ) >= 0.0000001){
                 throw new ImpossibleMixException(gas);
             }
             
         }
     }
     
     /**
      * 
      * Work out the mols of a starting gas which can be mixed with Air  to
 	 * result in a specific number of mols of a target gas.
 	 * 
 	 * Ao(Tm - X) + (Go*X) = To * Tm
 	 * 
 	 * where
 	 * Ao = the decimal value of O2 in air
 	 * Go = the decimal value of O2 in the initial gas
 	 * Tm = the target mols of the target gas
 	 * To = the decimal value of O2 in the target gas
 	 * 
 	 * X = the number of mols of the initial gas that when mixed will result the correct
 	 * mix of the target gas
 	 */
     private double getMolsBeforeAirTopup(Gas start, Gas target, double targetMols) {
     	Element oxygen = Element.OXYGEN;
     	double part1 = target.getDecimalRatio(oxygen)*targetMols;
 		double part2 = Gas.air().getDecimalRatio(oxygen)* targetMols;
 		double part3 = start.getDecimalRatio(oxygen) - Gas.air().getDecimalRatio(oxygen);
 		
 		double answer = (part1 - part2) / part3;
 		return answer;
     }
 }
