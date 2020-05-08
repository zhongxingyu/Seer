 package org.hbird.transport.xtce.utils;
 
 import org.hbird.transport.generatedcode.xtce.IntegerParameterType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
public class XtceToJavaMapping {
 	private static final Logger LOG = LoggerFactory.getLogger(XtceToJavaMapping.class);
 
 	private XtceToJavaMapping() {
 		// Utility class
 	}
 
 	public final static boolean doesIntRequireJavaLong(final IntegerParameterType type) {
 		boolean longRequired = false;
 		// If signed
 		if (type.getSigned()) {
 			if (type.getSizeInBits() > 32) {
 				longRequired = true;
 			}
 		}
 		// else if unsigned
 		else {
 			if (type.getSizeInBits() > 31) {
 				longRequired = true;
 			}
 		}
 
 		if(LOG.isTraceEnabled()) {
 			LOG.trace("Type " + type.getName() + " returns " + longRequired + " for long requried");
 		}
 		return longRequired;
 	}
 }
