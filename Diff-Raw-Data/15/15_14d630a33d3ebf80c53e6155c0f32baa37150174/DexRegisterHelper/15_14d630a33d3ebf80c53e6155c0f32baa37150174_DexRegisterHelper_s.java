 package com.rx201.dx.translator.util;
 
 import java.util.HashMap;
 
 import uk.ac.cam.db538.dexter.dex.code.DexParameterRegister;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 
 public class DexRegisterHelper {
 	
 	public static DexRegister next(DexRegister reg) {
 		assert !(reg instanceof DexParameterRegister);
 //		if (reg instanceof DexParameterRegister)
 //			return new DexParameterRegister(((DexParameterRegister)reg).getParameterIndex() + 1);
 //		else
 //			return new DexRegister(reg.getOriginalIndex() + 1);
 		return new DexRegister(reg.getOriginalIndex() + 1);
 	}
 	
 	private static long normalize0(DexRegister reg) {
 		if (reg instanceof DexParameterRegister)
 			return (1L << 32) | ((DexParameterRegister)reg).getParameterIndex();
 		else
 			return reg.getOriginalIndex();
 	}
 	
	private static HashMap<Integer, Integer> temporartRegMap = new HashMap<Integer, Integer>();
 	private static int temporaryRegStart;
 	public static void reset(int temporaryRegStart) {
	    temporartRegMap.clear();
	    DexRegisterHelper.temporaryRegStart = temporaryRegStart;
 	}
 	public static int normalize(DexRegister reg) {
 		// We were worried that DexParameterRegister may appear in the middle of the
 		// code, hence all the normalisation, but it turns out not to be the case.
 		// But just to be certain here.
 		assert  !(reg instanceof DexParameterRegister);
 		int regId = reg.getOriginalIndex(); 
 		// A negative regIndex represents a temporary register allocated 
 		// for in the middle taint tracking. We will keep track of them
 		// here and give out a real (positive) register index to make
 		// dx code happy.
 		// The downside is that before analyzing/translating a new piece of 
 		// code item, the reset(int) method needs to be called to let us know
 		// where the temporary register begins.
 		if (regId < 0) {
		    if (!temporartRegMap.containsKey(regId)) {
		        temporartRegMap.put(regId, temporaryRegStart++);
 		    }
		    return temporartRegMap.get(regId);
 		} else {
 		    return regId;
 		}
 	}
 	
 	public static boolean deepEqual(DexRegister r0, DexRegister r1) {
 		return normalize0(r0) == normalize0(r1);
 	}
 
 	public static boolean isPair(DexRegister r0, DexRegister r1) {
 		return normalize0(r0) + 1 == normalize0(r1);
 	}
 		
 }
