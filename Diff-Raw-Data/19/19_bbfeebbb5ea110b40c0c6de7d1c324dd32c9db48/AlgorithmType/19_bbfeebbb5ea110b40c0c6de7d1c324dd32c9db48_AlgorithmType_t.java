 package edu.mayo.phenoportal.shared;
 
import com.google.gwt.user.client.rpc.IsSerializable;

public enum AlgorithmType implements IsSerializable {
 	UNKNOWN {public String toString(){return "Unknown";}},
 	NQF2013 {public String toString(){return "NQF 2013";}},
 	NQF2014 {public String toString(){return "NQF 2014";}}
 }
