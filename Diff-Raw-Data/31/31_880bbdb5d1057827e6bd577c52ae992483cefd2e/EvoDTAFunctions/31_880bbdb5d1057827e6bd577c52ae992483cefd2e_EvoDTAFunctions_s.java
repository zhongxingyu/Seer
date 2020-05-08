 package com.aarribas1.evodta.ecj;
 
 import static com.google.common.collect.Lists.newArrayList;
 
 import java.util.Collection;
 
 import rinde.ecj.GPFunc;
 import rinde.ecj.GPFuncSet;
 import rinde.ecj.GenericFunctions.Add;
 import rinde.ecj.GenericFunctions.Constant;
 import rinde.ecj.GenericFunctions.Div;
//import rinde.ecj.GenericFunctions.If4;
 import rinde.ecj.GenericFunctions.Mul;
 import rinde.ecj.GenericFunctions.Pow;
 import rinde.ecj.GenericFunctions.Sub;
 import com.aarribas1.evodta.ecj.EvoDTAEvaluator.EvoDTAContext;
 
 public class EvoDTAFunctions extends GPFuncSet<EvoDTAContext> {
 
 	private static final long serialVersionUID = -887686900221451629L;
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Collection<GPFunc<EvoDTAContext>> create() {
 		return newArrayList(
 		/* GENERIC FUNCTIONS */
	
 				new Add<EvoDTAContext>(), /* */
 				new Sub<EvoDTAContext>(), /* */
 				new Div<EvoDTAContext>(), /* */
 				new Mul<EvoDTAContext>(), /* */
 				new Pow<EvoDTAContext>(), /* */
 	
 				/* CONSTANTS */
 				new Constant<EvoDTAContext>(1), /* */
 				new Constant<EvoDTAContext>(0), /* */
 				/* PROBLEM SPECIFIC VARIABLES */
 				new CDEMAND(), new ODEMAND(),
 				new INVITERATION(), new NORMCOSTDIFF(),
 				new CUMUDELTA());
 		
 	}
 
 	public static class CDEMAND extends GPFunc<EvoDTAContext> {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 7472539582231934098L;
 
 		@Override
 		public double execute(double[] input, EvoDTAContext context) {
 			return context.cDemand;
 		}
 	}
 
 	public static class ODEMAND extends GPFunc<EvoDTAContext> {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -6464361580236464741L;
 
 		@Override
 		public double execute(double[] input, EvoDTAContext context) {
 			return context.oDemand;
 		}
 	}
 	public static class INVITERATION extends GPFunc<EvoDTAContext> {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 9133189122070147488L;
 
 		@Override
 		public double execute(double[] input, EvoDTAContext context) {
 			return context.invIteration;
 		}
 	}
 	public static class NORMCOSTDIFF extends GPFunc<EvoDTAContext> {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 173210137270918269L;
 
 		@Override
 		public double execute(double[] input, EvoDTAContext context) {
 			return context.normCostDiff;
 		}
 	}
 	public static class CUMUDELTA extends GPFunc<EvoDTAContext> {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1002355814725210658L;
 
 		@Override
 		public double execute(double[] input, EvoDTAContext context) {
 			return context.cumuDelta;
 		}
 	}	
 
 }
