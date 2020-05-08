 package com.rau.evoting.ElGamal;
 
 import java.math.BigInteger;
 import java.security.SecureRandom;
 
 import org.bouncycastle.crypto.generators.ElGamalParametersGenerator;
 import org.bouncycastle.crypto.params.ElGamalParameters;
 
 public class GlobalParameters {
 
 	private static volatile GlobalParameters instance;
 	private static ElGamalParameters params = null;
 
 	private GlobalParameters() {
 		ElGamalParametersGenerator gen = new ElGamalParametersGenerator();
		// gen.init(200, 5, new SecureRandom());
 		// params = gen.generateParameters();
 		params = new ElGamalParameters(
 				new BigInteger(
						"1217632165455017325599107230499272850738519365493132423159259"),
 				new BigInteger(
						"895293172683390114107043256178557962436296753020590093614515"));
 		System.out.println("p:" + params.getP());
 		System.out.println("g:" + params.getG());
 
 	}
 
 	public static GlobalParameters getInstance() {
 		GlobalParameters localInstance = instance;
 		if (localInstance == null) {
 			synchronized (GlobalParameters.class) {
 				localInstance = instance;
 				if (localInstance == null) {
 					instance = localInstance = new GlobalParameters();
 				}
 			}
 		}
 		return localInstance;
 	}
 
 	public static ElGamalParameters getParams() {
 		if (params == null)
 			getInstance();
 
 		return params;
 	}
 
 }
