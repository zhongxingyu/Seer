 package base.hldd.structure.models.utils;
 
 import base.hldd.structure.variables.AbstractVariable;
 import base.hldd.structure.variables.ConstantVariable;
 import base.hldd.structure.variables.FunctionVariable;
 import base.hldd.structure.variables.UserDefinedFunctionVariable;
 import base.vhdl.structure.Operator;
 
 import java.util.TreeMap;
 import java.util.Collection;
 import java.util.TreeSet;
 
 /**
  * <br><br>User: Anton Chepurov
  * <br>Date: 29.11.2008
  * <br>Time: 14:48:58
  */
 public class VariableManager {
     private TreeMap<String, AbstractVariable> vars = new TreeMap<String, AbstractVariable>();
     private TreeMap<String, ConstantVariable> consts = new TreeMap<String, ConstantVariable>();
 
 
     /**
      *
      * @param newVariable variable to be added
      * @throws Exception if the collector has already got the variable with identical name
      */
     public void addVariable(AbstractVariable newVariable) throws Exception {
         addVariable(newVariable.getName(), newVariable);
     }
 
     /**
      *
      * @param varName name of the variable to use as key in maps
      * @param newVariable variable to be added
      */
     public void addVariable(String varName, AbstractVariable newVariable) {
         if (newVariable instanceof ConstantVariable) {
             if (!consts.containsKey(varName)) {
                 consts.put(varName, (ConstantVariable) newVariable);
 //                throw new Exception("Set of CONSTANTS does already have the following constant: " + newVariable);
             }
         } else {
             if (vars.put(varName, newVariable) != null) {
 //                throw new Exception("Set of VARIABLES does already have the following variable: " + newVariable);
             }
         }
     }
 
     public void removeVariable(AbstractVariable variableToRemove) {
         if (variableToRemove instanceof ConstantVariable) {
             consts.remove(variableToRemove.getName());
         } else {
             vars.remove(variableToRemove.getName());
         }
     }
 
     public Collection<AbstractVariable> getVariables() {
         return vars.values();
     }
 
 	public Collection<FunctionVariable> getFunctions(Operator operator) {
 		return getFunctions(vars.values(), operator);
 	}
 
 	public Collection<FunctionVariable> getUDFunctions(String userDefinedOperator) {
 		return getUDFunctions(vars.values(), userDefinedOperator);
 	}
 
 	public static Collection<FunctionVariable> getFunctions(Collection<AbstractVariable> vars, Operator operator) {
 		TreeSet<FunctionVariable> funcSet = new TreeSet<FunctionVariable>(FunctionVariable.getComparator());
 
 		if (operator == null) {
 			for (AbstractVariable variable : vars) {
				if (variable instanceof FunctionVariable) {
 					FunctionVariable functionVariable = (FunctionVariable) variable;
 
 					funcSet.add(functionVariable);
 				}
 			}
 		} else {
 			for (AbstractVariable variable : vars) {
				if (variable instanceof FunctionVariable) {
 					FunctionVariable functionVariable = (FunctionVariable) variable;
 
 					if (functionVariable.getOperator() == operator) {
 						funcSet.add(functionVariable);
 					}
 				}
 			}
 		}
 		return funcSet;
 	}
 
 	public static Collection<FunctionVariable> getUDFunctions(Collection<AbstractVariable> vars, String userDefinedOperator) {
 		TreeSet<FunctionVariable> funcSet = new TreeSet<FunctionVariable>(FunctionVariable.getComparator());
 
 		if (userDefinedOperator == null) {
 			for (AbstractVariable variable : vars) {
				if (variable instanceof UserDefinedFunctionVariable) {
 					UserDefinedFunctionVariable functionVariable = (UserDefinedFunctionVariable) variable;
 
 					funcSet.add(functionVariable);
 				}
 			}
 		} else {
 			for (AbstractVariable variable : vars) {
				if (variable instanceof UserDefinedFunctionVariable) {
 					UserDefinedFunctionVariable functionVariable = (UserDefinedFunctionVariable) variable;
 
 					if (functionVariable.getUserDefinedOperator().equals(userDefinedOperator)) {
 						funcSet.add(functionVariable);
 					}
 				}
 			}
 		}
 		return funcSet;
 	}
 
     public Collection<ConstantVariable> getConstants() {
         return consts.values();
     }
 
     public AbstractVariable[] getVariablesAsArray() {
         Collection<AbstractVariable> variablesCollection = getVariables();
         return variablesCollection.toArray(new AbstractVariable[variablesCollection.size()]);
     }
 
     public ConstantVariable[] getConstantsAsArray() {
         Collection<ConstantVariable> constantsCollection = getConstants();
         return constantsCollection.toArray(new ConstantVariable[constantsCollection.size()]);
     }
 
     public AbstractVariable getVariableByName(String variableName) {
         return vars.get(variableName);
     }
 
     public ConstantVariable getConstantByName(String constantName) {
         return consts.get(constantName);
     }
 
     public TreeMap<String, ConstantVariable> getConsts() {
         return consts;
     }
 }
