 package org.hbird.core.spacesystemmodel.tmtcgroups;
 
 import java.math.BigDecimal;
 
 import org.hbird.core.commons.tmtc.Parameter;
 import org.hbird.core.commons.tmtc.TmTcGroup;
 import org.hbird.core.commons.tmtc.exceptions.UnknownParameterException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author kimmell
  *
  */
// TODO - 27.03.2012 kimmell - move it to core module?
 public class TmTcGroups {
 
 	private static final Logger LOG = LoggerFactory.getLogger(TmTcGroups.class);
	
 	// Cast suppress reasoning: Parameter names must be unique so if a Param is found in a specific type collection
 	// it is safe to cast.
 	@SuppressWarnings("unchecked")
 	public static void replaceParameterInGroup(final TmTcGroup group, final String qualifiedName, final Parameter<?> parameter) {
 		String pname = parameter.getQualifiedName();
 
 		if (group.getAllParameters().containsKey(pname)) {
 			group.getAllParameters().put(qualifiedName, parameter);
 		}
 
 		if (group.getIntegerParameters() != null && group.getIntegerParameters().containsKey(pname)) {
 			group.getIntegerParameters().put(qualifiedName, (Parameter<Integer>) parameter);
 		}
 		else if (group.getLongParameters() != null && group.getLongParameters().containsKey(pname)) {
 			group.getLongParameters().put(qualifiedName, (Parameter<Long>) parameter);
 		}
 		else if (group.getFloatParameters() != null && group.getFloatParameters().containsKey(pname)) {
 			group.getFloatParameters().put(qualifiedName, (Parameter<Float>) parameter);
 		}
 		else if (group.getDoubleParameters() != null && group.getDoubleParameters().containsKey(pname)) {
 			group.getDoubleParameters().put(qualifiedName, (Parameter<Double>) parameter);
 		}
 		else if (group.getBigDecimalParameters() != null && group.getBigDecimalParameters().containsKey(pname)) {
 			group.getBigDecimalParameters().put(qualifiedName, (Parameter<BigDecimal>) parameter);
 		}
 		else if (group.getStringParameters() != null && group.getStringParameters().containsKey(pname)) {
 			group.getStringParameters().put(qualifiedName, (Parameter<String>) parameter);
 		}
 		else if (group.getRawParameters() != null && group.getRawParameters().containsKey(pname)) {
 			group.getRawParameters().put(qualifiedName, (Parameter<Byte[]>) parameter);
 		}
 	}
	
 	public static TmTcGroup copyAllParameterValues(final TmTcGroup sourceGroup, final TmTcGroup targetGroup) {
 		try {
 			// Ints
 			if (targetGroup.getIntegerParameters() != null) {
 				for (String qualifiedName : targetGroup.getIntegerParameters().keySet()) {
 					targetGroup.getIntegerParameter(qualifiedName).setValue(sourceGroup.getIntegerParameter(qualifiedName).getValue());
 				}
 			}
 			if (targetGroup.getLongParameters() != null) {
 				for (String qualifiedName : targetGroup.getLongParameters().keySet()) {
 					targetGroup.getLongParameter(qualifiedName).setValue(sourceGroup.getLongParameter(qualifiedName).getValue());
 				}
 			}
 			if (targetGroup.getFloatParameters() != null) {
 				for (String qualifiedName : targetGroup.getFloatParameters().keySet()) {
 					targetGroup.getFloatParameter(qualifiedName).setValue(sourceGroup.getFloatParameter(qualifiedName).getValue());
 				}
 			}
 			if (targetGroup.getDoubleParameters() != null) {
 				for (String qualifiedName : targetGroup.getDoubleParameters().keySet()) {
 					targetGroup.getDoubleParameter(qualifiedName).setValue(sourceGroup.getDoubleParameter(qualifiedName).getValue());
 				}
 			}
 			if (targetGroup.getBigDecimalParameters() != null) {
 				for (String qualifiedName : targetGroup.getBigDecimalParameters().keySet()) {
 					targetGroup.getBigDecimalParameter(qualifiedName).setValue(sourceGroup.getBigDecimalParameter(qualifiedName).getValue());
 				}
 			}
 			if (targetGroup.getStringParameters() != null) {
 				for (String qualifiedName : targetGroup.getStringParameters().keySet()) {
 					targetGroup.getStringParameter(qualifiedName).setValue(sourceGroup.getStringParameter(qualifiedName).getValue());
 				}
 			}
 			if (targetGroup.getRawParameters() != null) {
 				for (String qualifiedName : targetGroup.getRawParameters().keySet()) {
 					targetGroup.getRawParameter(qualifiedName).setValue(sourceGroup.getRawParameter(qualifiedName).getValue());
 				}
 			}
 		}
 		catch (UnknownParameterException e) {
 			// TODO - 27.03.2012 kimmell - unit test
 			LOG.error("Unknown parameter when copying parameter values. This is is a serious internal error and must indicate a corruption "
 					+ "in memory, a system bug, or a seriosu misuse of the API (copying parameters to a different space system"
 					+ "model which has a different structure.  The system must shut down as integrity cannot be guaranteed.");
 			// TODO - 27.03.2012 kimmell - maybe there is better exception to throw at this point?
 			throw new RuntimeException("Unknown parameter when copying parameter values");
 		}
 
 		return targetGroup;
 	}
 }
