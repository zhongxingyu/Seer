 package com.rau.evoting.ElGamal;
 
 import java.security.SecureRandom;
 
 import org.bouncycastle.crypto.generators.ElGamalParametersGenerator;
 import org.bouncycastle.crypto.params.ElGamalParameters;
 
 public class GlobalParameters {
 
 	private static volatile GlobalParameters instance;
 	private static ElGamalParameters params = null;
 
 	private GlobalParameters() {
 		ElGamalParametersGenerator gen = new ElGamalParametersGenerator();
		gen.init(100, 5, new SecureRandom());
		params = gen.generateParameters();
 
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
