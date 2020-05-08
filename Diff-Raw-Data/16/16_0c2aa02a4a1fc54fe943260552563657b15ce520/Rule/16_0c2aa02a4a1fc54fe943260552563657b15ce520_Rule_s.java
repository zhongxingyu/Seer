 package com.pace.base.rules;
 
 import java.util.Arrays;
 
 
 
 /**
  * Encapsulates a single Rule for the evaluation engine.
  * i.e. SLS_DLR = SLS_AUR * SLS_UNITS
  * It is meant to be combined with other rules in a rulegroup
  * and has specific attributes to affect processing.
  *
  * @version	x.xx
  * @author JWatkins
  *
  */
 public class Rule {
 
 		private Formula formula;
         private int key;
         private String[] triggerMeasures;
         private String baseAllocateMeasure;
         private boolean skipAllocation = false;
         private boolean lockAllocation = false;
         private boolean skipAggregation = false;
         private boolean lockSystemEvaluationResult = false;
         
         // this needs to be overridden in the initDefaults method below
         // due to the fact that PafXStream deserializes it as a null, not false.
         private Boolean lockUserEvaluationResult = false;
         
         private boolean evalLockedIntersections = false;
         private boolean lockAllPriorTime = false;
         private boolean calcAllPeriods = false;
         private boolean initialTBFirstAllocation = false;
 
 
         /** 
          *	read resolve called by PafXStream
          *
          * @return this
          */
         private Object readResolve() {
             initDefaults();
             
             if ( this.baseAllocateMeasure != null ) {
             	
             	this.baseAllocateMeasure = this.baseAllocateMeasure.trim();
             	
             }
             
             return this;
         }
         
         private void initDefaults() {
         	        	
              if (lockUserEvaluationResult == null) {
                  lockUserEvaluationResult = false;
              }
         }
 
 		public int getKey() {
             return key;
         }
 
         public void setKey(int key) {
             this.key = key;
         }
 
         public Rule() {}
         
         public Rule(String string) {
             String[] terms = string.split("=");
             formula = new Formula(terms[0], terms[1]);
         }
 
         public Formula getFormula() {
             return formula;
         }
         public void setFormula(Formula formula) {
             this.formula = formula;
         }
         public String toString() {
         	
         	if ( formula != null ) {
         		
         		return formula.toString();
         		
         	}
         	
             return "No formula specified.";
         }
 
         /**
          * @return Returns the triggerMeasures.
          */
         public String[] getTriggerMeasures() {
             return triggerMeasures;
         }
         
         /**
          * @return boolean convenience method to check for trigger measures
          */
         public boolean hasTriggerMeasures() {
         	return !(getTriggerMeasures() == null || getTriggerMeasures().length == 0);        	
         }
 
         /**
          * @param triggerMeasures The triggerMeasures to set.
          */
         public void setTriggerMeasures(String[] triggerMeasures) {
             this.triggerMeasures = triggerMeasures;
         }
 
 		public boolean isSkipAggregation() {
 			return skipAggregation;
 		}
 
 		public void setSkipAggregation(boolean skipAggregation) {
 			this.skipAggregation = skipAggregation;
 		}
 
 		public boolean isSkipAllocation() {
 			return skipAllocation;
 		}
 
 		public void setSkipAllocation(boolean skipAllocation) {
 			this.skipAllocation = skipAllocation;
 		}
 
 		public String getBaseAllocateMeasure() {
 			return baseAllocateMeasure;
 		}
 
 		public void setBaseAllocateMeasure(String baseAllocateMeasure) {
 			this.baseAllocateMeasure = baseAllocateMeasure;
 		}
 
 		public boolean isLockAllocation() {
 			return lockAllocation;
 		}
 
 		public void setLockAllocation(boolean lockAllocation) {
 			this.lockAllocation = lockAllocation;
 		}
 
         /**
          * @return Returns the evalLockedIntersections.
          */
         public boolean getEvalLockedIntersections() {
             return evalLockedIntersections;
         }
 
         /**
          * @param evalLockedIntersections The evalLockedIntersections to set.
          */
         public void setEvalLockedIntersections(boolean evalLockedIntersections) {
             this.evalLockedIntersections = evalLockedIntersections;
         }
         /**
 		 * @return Returns the calcAllPeriods.
 		 */
 		public boolean isCalcAllPeriods() {
 			return calcAllPeriods;
 		}
 
 		/**
 		 * @param calcAllPeriods The calcAllPeriods to set.
 		 */
 		public void setCalcAllPeriods(boolean calcAllPeriods) {
 			this.calcAllPeriods = calcAllPeriods;
 		}
 
         /**
          * @return Returns the lockSystemEvaluationResult.
          */
         public boolean isLockSystemEvaluationResult() {
             return lockSystemEvaluationResult;
         }
 
         /**
          * @param lockSystemEvaluationResult The lockSystemEvaluationResult to set.
          */
         public void setLockSystemEvaluationResult(boolean lockSystemEvaluationResult) {
             this.lockSystemEvaluationResult = lockSystemEvaluationResult;
         }
 
         /**
          * @return Returns the lockUserEvaluationResult.
          */
         public Boolean getLockUserEvaluationResult() {
             return lockUserEvaluationResult;
         }
 
         /**
          * @param lockUserEvaluationResult The lockUserEvaluationResult to set.
          */
         public void setLockUserEvaluationResult(Boolean lockUserEvaluationResult) {
             this.lockUserEvaluationResult = lockUserEvaluationResult;
         }
 
         /**
          * @return Returns the lockAllPriorTime.
          */
         public boolean isLockAllPriorTime() {
             return lockAllPriorTime;
         }
 
         /**
          * @param lockAllPriorTime The lockAllPriorTime to set.
          */
         public void setLockAllPriorTime(boolean lockAllPriorTime) {
             this.lockAllPriorTime = lockAllPriorTime;
         }
 
         /**
          * @return Returns the initialTBFirstAllocation.
          */
         public boolean isInitialTBFirstAllocation() {
             return initialTBFirstAllocation;
         }
 
         /**
          * @param initialTBFirstAllocation The initialTBFirstAllocation to set.
          */
         public void setInitialTBFirstAllocation(boolean initialTBFirstAllocation) {
             this.initialTBFirstAllocation = initialTBFirstAllocation;
         }
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#hashCode()
 		 */
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime
 					* result
 					+ ((baseAllocateMeasure == null) ? 0 : baseAllocateMeasure
 							.hashCode());
 			result = prime * result + (calcAllPeriods ? 1231 : 1237);
 			result = prime * result + (evalLockedIntersections ? 1231 : 1237);
 			result = prime * result
 					+ ((formula == null) ? 0 : formula.hashCode());
 			result = prime * result + (initialTBFirstAllocation ? 1231 : 1237);
 			result = prime * result + key;
 			result = prime * result + (lockAllPriorTime ? 1231 : 1237);
 			result = prime * result + (lockAllocation ? 1231 : 1237);
 			result = prime * result
 					+ (lockSystemEvaluationResult ? 1231 : 1237);
 			result = prime
 					* result
 					+ ((lockUserEvaluationResult == null) ? 0
 							: lockUserEvaluationResult.hashCode());
 			result = prime * result + (skipAggregation ? 1231 : 1237);
 			result = prime * result + (skipAllocation ? 1231 : 1237);
 			result = prime * result + Arrays.hashCode(triggerMeasures);
 			return result;
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			Rule other = (Rule) obj;
 			if (baseAllocateMeasure == null) {
 				if (other.baseAllocateMeasure != null)
 					return false;
 			} else if (!baseAllocateMeasure.equals(other.baseAllocateMeasure))
 				return false;
 			if (calcAllPeriods != other.calcAllPeriods)
 				return false;
 			if (evalLockedIntersections != other.evalLockedIntersections)
 				return false;
 			if (formula == null) {
 				if (other.formula != null)
 					return false;
 			} else if (!formula.equals(other.formula))
 				return false;
 			if (initialTBFirstAllocation != other.initialTBFirstAllocation)
 				return false;
 			if (key != other.key)
 				return false;
 			if (lockAllPriorTime != other.lockAllPriorTime)
 				return false;
 			if (lockAllocation != other.lockAllocation)
 				return false;
 			if (lockSystemEvaluationResult != other.lockSystemEvaluationResult)
 				return false;
 			if (lockUserEvaluationResult == null) {
 				if (other.lockUserEvaluationResult != null)
 					return false;
 			} else if (!lockUserEvaluationResult
 					.equals(other.lockUserEvaluationResult))
 				return false;
 			if (skipAggregation != other.skipAggregation)
 				return false;
 			if (skipAllocation != other.skipAllocation)
 				return false;
 			if (!Arrays.equals(triggerMeasures, other.triggerMeasures))
 				return false;
 			return true;
 		}
         
 }
