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
		// gen.init(500, 5, new SecureRandom());
 		// params = gen.generateParameters();
 		params = new ElGamalParameters(
 				new BigInteger(
						"1481036519848617692612681570478610381651305627082483941468540276026284745564250426308497379"),
 				new BigInteger(
						"550431548021782717569146563950321500899081978637529330103028894231173207891450029000697135"));
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
