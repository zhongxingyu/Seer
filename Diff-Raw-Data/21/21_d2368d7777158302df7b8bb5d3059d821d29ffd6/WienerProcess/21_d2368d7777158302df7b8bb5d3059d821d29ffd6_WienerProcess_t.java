 package org.bh.plugin.wienerProcess;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 
 import javax.swing.JPanel;
 
 import org.apache.log4j.Logger;
 import org.bh.calculation.IShareholderValueCalculator;
 import org.bh.calculation.IStochasticProcess;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOScenario;
 import org.bh.data.IPeriodicalValuesDTO;
 import org.bh.data.DTOScenario.DTOKeyPair;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.DistributionMap;
 import org.bh.data.types.DoubleValue;
 import org.bh.data.types.IValue;
 import org.bh.data.types.IntegerValue;
 import org.bh.data.types.StochasticValue;
 import org.bh.data.types.StringValue;
 import org.bh.gui.swing.BHLabel;
 import org.bh.gui.swing.BHTextField;
 import org.bh.platform.Services;
 import org.bh.platform.i18n.ITranslator;
 import org.bh.plugin.directinput.DTODirectInput;
 /**
  * This class provides the functionality to process the Wiener Process on every value which
  * should be determined stochastically.
  * 
  * @author Sebastian
  * @version 0.1, 04.01.2010
  *
  */
 public class WienerProcess implements IStochasticProcess {
 	private static final String SLOPE = "slope";
 	private static final String STANDARD_DEVIATION = "standardDeviation";
 	private static final String REPETITIONS = "repetitions";
 	private static final String STEPS_PER_PERIOD = "stepsPerPeriod";
 	private static final String AMOUNT_OF_PERIODS = "amountOfPeriods";
 	
 	
 	@SuppressWarnings("unused")
 	private static final Logger log = Logger.getLogger(WienerProcess.class);
 
 	private static final String UNIQUE_ID = "wienerProcess";
 	private static final String GUI_KEY = "wienerProcess";
 
 	private DTOScenario scenario;
 	private JPanel panel;
 	private HashMap<String, IValue> internalMap;
 	private HashMap<String, Integer> map;
 
 	@Override
 	public DistributionMap calculate() {
 		
 		DistributionMap result = new DistributionMap(1);
 		DTOPeriod last = scenario.getLastChildren();
 		List<DTOKeyPair> stochasticKeys = scenario.getPeriodStochasticKeys();
 		// List<String> stochasticKeys = last.getStochasticKeys();
 
 		for (int j = 0; j < map.get(REPETITIONS); j++) {
 			DTOScenario temp = new DTOScenario(true);
 			temp.put(DTOScenario.Key.REK, scenario.get(DTOScenario.Key.REK));
 			temp.put(DTOScenario.Key.RFK, scenario.get(DTOScenario.Key.RFK));
 			temp.put(DTOScenario.Key.BTAX, scenario.get(DTOScenario.Key.BTAX));
 			temp.put(DTOScenario.Key.CTAX, scenario.get(DTOScenario.Key.CTAX));
 
 			DTOPeriod clone = last.clone();
 			DTOPeriod previous = last.getPrevious();
 
 			for (DTOKeyPair key : stochasticKeys) {
 				IPeriodicalValuesDTO pvdto = clone.getPeriodicalValuesDTO(key
 						.getDtoId());
 				pvdto.put(key.getKey(), this.doOneWienerProcess(previous
 						.getCalculable(key.getKey()), map.get(STEPS_PER_PERIOD),
						(Calculable) this.internalMap.get(key.getKey() + STANDARD_DEVIATION),
						(Calculable) this.internalMap.get(key.getKey() + SLOPE)));
 			}
 			temp.addChild(clone);
 
 			for (int i = 0; i < map.get(AMOUNT_OF_PERIODS) - 1; i++) {
 				previous = clone;
 				clone = last.clone();
 				for (DTOKeyPair key : stochasticKeys) {
 					IPeriodicalValuesDTO pvdto = clone
 							.getPeriodicalValuesDTO(key.getDtoId());
 					pvdto.put(key.getKey(), this.doOneWienerProcess(previous
 							.getCalculable(key.getKey()), map.get(STEPS_PER_PERIOD),
 							(Calculable) this.internalMap.get(key.getKey()
									+ STANDARD_DEVIATION), (Calculable) this.internalMap
									.get(key.getKey() + SLOPE)));
 				}
 				temp.addChild(clone);
 			}
 
 			result
 					.put(((DoubleValue) Services.getDCFMethod("apv").calculate(
 							temp).get(
 							IShareholderValueCalculator.SHAREHOLDER_VALUE)[0])
 							.getValue());
 		}
 		return result;
 	}
 
 	@Override
 	public JPanel calculateParameters() {
 		ITranslator translator = Services.getTranslator();
 		internalMap = new HashMap<String, IValue>();
		map = new HashMap<String, Integer>();
 		TreeMap<DTOKeyPair, List<Calculable>> toBeDetermined = scenario
 				.getPeriodStochasticKeysAndValues();
 
 		if (toBeDetermined.isEmpty())
 			return null;
 		else {
 			JPanel result = new JPanel();
 			result.setLayout(new BorderLayout());
 
 			JPanel north = new JPanel();
 			north.setLayout(new FlowLayout());
 			north.add(new BHLabel(translator.translate(AMOUNT_OF_PERIODS)));
 			BHTextField tf = new BHTextField(AMOUNT_OF_PERIODS);
 			int[] rule = { ValidationWienerProcess.isMandatory,
 					ValidationWienerProcess.isInteger,
 					ValidationWienerProcess.isPositive };
 			tf.setValidateRules(rule);
 			north.add(tf);
 			map.put(AMOUNT_OF_PERIODS, new Integer(5));
 
 			north.add(new BHLabel(translator.translate(STEPS_PER_PERIOD)));
 			BHTextField tf1 = new BHTextField(STEPS_PER_PERIOD);
 			int[] rule1 = { ValidationWienerProcess.isMandatory,
 					ValidationWienerProcess.isInteger,
 					ValidationWienerProcess.isPositive };
 			tf1.setValidateRules(rule1);
 			north.add(tf1);
			map.put(STEPS_PER_PERIOD, new Integer(1));
 
 			north.add(new BHLabel(translator.translate(REPETITIONS)));
 			BHTextField tf2 = new BHTextField(REPETITIONS);
 			int[] rule2 = { ValidationWienerProcess.isMandatory,
 					ValidationWienerProcess.isInteger,
 					ValidationWienerProcess.isPositive };
 			tf2.setValidateRules(rule2);
 			north.add(tf2);
 			map.put(REPETITIONS, new Integer(100000));
 
 			result.add(north, BorderLayout.NORTH);
 
 			JPanel center = new JPanel();
 			center.setLayout(new GridLayout(0, 5));
 
 			for (Entry<DTOKeyPair, List<Calculable>> e : toBeDetermined
 					.entrySet()) {
 				String key = e.getKey().getKey();
 				Calculable standardDeviation = calcStandardDeviation(e
 						.getValue());
 				Calculable slope = calcSlope(e.getValue());
 
 				center.add(new BHLabel(translator.translate(key)));
 				center.add(new BHLabel(translator.translate(SLOPE)));
 				center.add(new BHTextField(key + SLOPE, "" + slope, ""));
 				center.add(new BHLabel(translator.translate(STANDARD_DEVIATION)));
 				center
 						.add(new BHTextField(key + STANDARD_DEVIATION, "" + standardDeviation,
 								""));
 
 				internalMap.put(key + SLOPE, slope);
 				internalMap.put(key + STANDARD_DEVIATION, standardDeviation);
 			}
 			result.add(center, BorderLayout.CENTER);
 			this.panel = result;
 			return result;
 		}
 	}
 
 	@Override
 	public Map<String, IValue> getParametersForAnalysis() {
 		return internalMap;
 	}
 
 	@Override
 	public void setScenario(DTOScenario scenario) {
 		this.scenario = scenario;
 	}
 
 	@Override
 	public void updateParameters() {
 		Component[] components = panel.getComponents();
 		for (int i = 0; i < components.length; i++) {
 			if (components[i] instanceof BHTextField) {
 				BHTextField c = (BHTextField) components[i];
 				String key = c.getKey();
 				String text = c.getText();
				if (internalMap.containsKey(key))
					internalMap.put(key, Calculable.parseCalculable(text));
				else
					map.put(key, Integer.parseInt(text));
 			}
 		}
 	}
 
 	private Calculable calcSlope(List<Calculable> inputValues) {
 		// d = 1/n(X0 - X-n)
 		Calculable result = (inputValues.get(inputValues.size() - 1)
 				.sub(inputValues.get(0))).div(new IntegerValue(inputValues
 				.size()));
 		return result;
 	}
 
 	private Calculable calcStandardDeviation(List<Calculable> inputValues) {
 		Calculable d = calcSlope(inputValues);
 		Calculable sum = new DoubleValue(0);
 		for (int i = 0; i < inputValues.size() - 1; i++) {
 			sum = sum
 					.add(inputValues.get(i + 1).sub(inputValues.get(i)).sub(d));
 		}
 		Calculable result = (sum.div(new IntegerValue(inputValues.size())))
 				.sqrt();
 		return result;
 	}
 
 	private Calculable doOneWienerProcess(Calculable lastValue,
 			int amountOfSteps, Calculable standardDeviation, Calculable slope) {
 		Random r = new Random();
 		Calculable deltaT = new DoubleValue(1 / amountOfSteps);
 		Calculable value = lastValue;
 		for(int i = 0; i < amountOfSteps; i++){
 			value = value.add(slope.mul(deltaT))
 					.add(standardDeviation.mul(deltaT.sqrt()).mul(
 							new DoubleValue(r.nextGaussian())));
 		}
 		return value;
 	}
 
 	@Override
 	public IStochasticProcess createNewInstance() {
 		return new WienerProcess();
 	}
 
 	@Override
 	public String getUniqueId() {
 		return UNIQUE_ID;
 	}
 
 	@Override
 	public String getGuiKey() {
 		return GUI_KEY;
 	}
 
 	public static void main(String[] args) {
 		DTOScenario scenario = new DTOScenario(false);
 		scenario.put(DTOScenario.Key.REK, new DoubleValue(0.11));
 		scenario.put(DTOScenario.Key.RFK, new DoubleValue(0.1));
 		scenario.put(DTOScenario.Key.BTAX, new DoubleValue(0.1694));
 		scenario.put(DTOScenario.Key.CTAX, new DoubleValue(0.26375));
 		scenario.put(DTOScenario.Key.DCF_METHOD, new StringValue("apv"));
 
 		DTODirectInput di0 = new DTODirectInput();
 		di0.put(DTODirectInput.Key.FCF, StochasticValue.INSTANCE);
 		di0.put(DTODirectInput.Key.LIABILITIES, StochasticValue.INSTANCE);
 		DTOPeriod period0 = new DTOPeriod();
 		period0.addChild(di0);
 		scenario.addChild(period0);
 
 		DTODirectInput di = new DTODirectInput();
 		di.put(DTODirectInput.Key.FCF, new DoubleValue(130));
 		di.put(DTODirectInput.Key.LIABILITIES, new DoubleValue(1200));
 		DTOPeriod period1 = new DTOPeriod();
 		period1.addChild(di);
 		scenario.addChild(period1);
 
 		DTODirectInput di2 = new DTODirectInput();
 		di2.put(DTODirectInput.Key.FCF, new DoubleValue(105));
 		di2.put(DTODirectInput.Key.LIABILITIES, new DoubleValue(1050));
 		DTOPeriod period2 = new DTOPeriod();
 		period2.addChild(di2);
 		scenario.addChild(period2);
 
 		DTODirectInput di3 = new DTODirectInput();
 		di3.put(DTODirectInput.Key.FCF, new DoubleValue(90));
 		di3.put(DTODirectInput.Key.LIABILITIES, new DoubleValue(1100));
 		DTOPeriod period3 = new DTOPeriod();
 		period3.addChild(di3);
 		scenario.addChild(period3);
 
 		DTODirectInput di4 = new DTODirectInput();
 		di4.put(DTODirectInput.Key.FCF, new DoubleValue(100));
 		di4.put(DTODirectInput.Key.LIABILITIES, new DoubleValue(1000));
 		DTOPeriod period4 = new DTOPeriod();
 		period4.addChild(di4);
 		scenario.addChild(period4);
 		scenario.getPeriodStochasticKeysAndValues();
 
 		WienerProcess wp = new WienerProcess();
 		wp.setScenario(scenario);
 		wp.calculateParameters();
 		DistributionMap dm = wp.calculate();
 
 		Iterator<Entry<java.lang.Double, Integer>> iter = dm.iterator();
 		System.out.println("Key 0");
 		while (iter.hasNext()) {
 			Map.Entry<java.lang.Double, java.lang.Integer> entry = (Map.Entry<java.lang.Double, java.lang.Integer>) iter
 					.next();
 			System.out.println(entry.getKey());
 		}
 		iter = dm.iterator();
 		System.out.println("Value 0");
 		while (iter.hasNext()) {
 			Map.Entry<java.lang.Double, java.lang.Integer> entry = (Map.Entry<java.lang.Double, java.lang.Integer>) iter
 					.next();
 			System.out.println(entry.getValue());
 		}
 	}
 
 }
