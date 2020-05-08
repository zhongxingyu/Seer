 package org.bh.data;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 import org.bh.calculation.IShareholderValueCalculator;
 import org.bh.calculation.IStochasticProcess;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.DoubleValue;
 import org.bh.platform.Services;
 
 /**
  * Scenario DTO
  * 
  * <p>
  * This DTO contains scenariodata, acts as a root-element for periods
  * and as a child for project DTO
  * 
  * @author Michael Löckelt
  * @version 0.3, 17.12.2009
  * 
  */
 
 public class DTOScenario extends DTO<DTOPeriod> {
 	private static final Logger log = Logger.getLogger(DTOScenario.class);
 	
 	/**
 	 * Used to difference between values in the past and values in the future
 	 * if futureValues is true, new childs are appended at the end of the childlist,
 	 * if not, new childs are appended at the beginning
 	 */
 	protected boolean futureValues;
 	
 	protected IShareholderValueCalculator dcfMethod;
 	
 	public enum Key {
 		/** 
 		 * equity yield 
 		 */
 		REK,
 		
 		/** 
 		 * liability yield 
 		 */
 		RFK,
 		
 		/**
 		 * 	corporate income tax 
 		 */
 		CTAX,
 		
 		/**
 		 * business tax
 		 */
 		BTAX,
 		
 		/**
 		 * summed tax
 		 */
 		@Method
 		TAX,
 		
 		/**
 		 * name
 		 */
 		NAME,
 		
 		/**
 		 * comment
 		 */
 		COMMENT,
 		
 		/**
 		 * DCF method
 		 */
 		DCF_METHOD,
 		
 		/**
 		 * Stochastic process
 		 */
 		STOCHASTIC_PROCESS,
 	}
 	
     /**
      * initialize key and method list
      */
 	public DTOScenario(boolean futureValues) {
 		super(Key.values());
 		this.futureValues = futureValues;
 		log.debug("Object created");
 	}
 
 	@Override
 	public boolean validate() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("This method has not been implemented");
 	}
 	
 	@Override
 	public DTOPeriod addChild(DTOPeriod child) throws DTOAccessException {
 		DTOPeriod result = super.addChild(child,this.futureValues);
 		child.scenario = this;
 		refreshPeriodReferences();
 		return result;
 	}
 	
 	@Override
 	public DTOPeriod removeChild(int index) throws DTOAccessException {
 		DTOPeriod removedPeriod = super.removeChild(index);
 		removedPeriod.next = removedPeriod.previous = null;
 		removedPeriod.scenario = null;
 		refreshPeriodReferences();
 		return removedPeriod;
 	}
 
 	private void refreshPeriodReferences() {
 		DTOPeriod previous = null;
 		for (DTOPeriod child : children) {
 			child.previous = previous;
 			if (previous != null)
 				previous.next = child;
 			previous = child;
 		}
 		if (previous != null)
 			previous.next = null;
 		
 		log.debug("PeriodReferences refreshed!");
 	}
 	
 	/**
 	 * Gets tax for scenario.
 	 * @return Tax for scenario.
 	 */
 	public Calculable getTax() {
 		
 		DoubleValue ctax = (DoubleValue) this.get(DTOScenario.Key.CTAX);
 		DoubleValue btax = (DoubleValue) this.get(DTOScenario.Key.BTAX);
 		
 		DoubleValue myTax = (DoubleValue) btax.mul(new DoubleValue(0.5)).mul(ctax.mul(new DoubleValue(-1)).add(new DoubleValue(1)));
 		myTax = (DoubleValue) myTax.add(ctax);
 		
 		return myTax;
 	}
 
 	/**
 	 * Returns a reference to the DCF method.
 	 * @return Reference to the DCF method.
 	 */
 	public IShareholderValueCalculator getDCFMethod() {
 		return Services.getDCFMethod(get(Key.DCF_METHOD).toString());
 	}
 	
 	/**
 	 * Returns a reference to the stochastic process.
 	 * @return Reference to the stochastic process.
 	 */
 	public IStochasticProcess getStochasticProcess() {
 		return Services.getStochasticProcess(get(Key.STOCHASTIC_PROCESS).toString());
 	}
 	
 	/**
 	 * Returns all keys whose values have to be determined stochastically.
 	 * @return List of keys.
 	 */
 	public List<DTOKeyPair> getPeriodStochasticKeys() {
 		DTOPeriod lastPeriod = children.getLast();
 		ArrayList<DTOKeyPair> list = new ArrayList<DTOKeyPair>();
 		for (IPeriodicalValuesDTO dto : lastPeriod.getChildren()) {
 			@SuppressWarnings("unchecked")
 			List<String> stochasticKeys = dto.getStochasticKeys();
 			for (String key : stochasticKeys) {
 				list.add(new DTOKeyPair(dto.getUniqueId(), key));
 			}
 		}
 		return list;
 	}
 	
 	/**
 	 * This method returns the keys whose values are determined stochastically
 	 * together with their values in the past periods.
 	 * @see #getPeriodStochasticKeys()
 	 * @return Keys and past values.
 	 */
 	public TreeMap<DTOKeyPair, List<Calculable>> getPeriodStochasticKeysAndValues() {
 		List<DTOKeyPair> keys = getPeriodStochasticKeys();
 		TreeMap<DTOKeyPair, List<Calculable>> map = new TreeMap<DTOKeyPair, List<Calculable>>();
 		DTOPeriod lastPeriod = children.getLast();
 		for (DTOKeyPair key : keys) {
 			List<Calculable> pastValues = new ArrayList<Calculable>();
 			for (DTOPeriod period : children) {
 				if (period == lastPeriod)
 					break;
 				IPeriodicalValuesDTO periodValuesDto = period.getPeriodicalValuesDTO(key.getDtoId());
 				pastValues.add(periodValuesDto.getCalculable(key.getKey()));
 			}
 			map.put(key, pastValues);
 		}
 		return map;
 	}
 	
 	
 	public static class DTOKeyPair implements Comparable<DTOKeyPair> {
 		private final String dtoId;
 		private final String key;
 		
 		public DTOKeyPair(String dtoId, String key) {
 			this.dtoId = dtoId;
 			this.key = key;
 		}
 
 		public String getDtoId() {
 			return dtoId;
 		}
 
 		public String getKey() {
 			return key;
 		}
 
 		@Override
 		public int compareTo(DTOKeyPair o) {
 			int num = dtoId.compareTo(o.dtoId);
 			if (num != 0)
 				return num;
 			
 			return key.compareTo(o.key);
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((dtoId == null) ? 0 : dtoId.hashCode());
 			result = prime * result + ((key == null) ? 0 : key.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null || !(obj instanceof DTOKeyPair))
 				return false;
 			DTOKeyPair other = (DTOKeyPair) obj;
 			return dtoId.equals(other.dtoId) && key.equals(other.key);
 		}
 	}
 }
