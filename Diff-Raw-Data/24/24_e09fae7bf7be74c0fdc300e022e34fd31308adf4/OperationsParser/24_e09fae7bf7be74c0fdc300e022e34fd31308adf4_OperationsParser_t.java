 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ui;
 
 import java.util.HashMap;
 import matrix.LU;
 import matrix.Matrix;
 
 /**
  *
  * @author Lasse
  */
 class OperationsParser {
 
     private HashMap<String, Variable> variables;
 
     OperationsParser() {
         variables = UserInterface.getInstance().getVariables();
     }
 
     OperationsParser(HashMap<String, Variable> variables) {
         this.variables = variables;
     }
 
     private boolean variablesFound(String[] vars) {
         for (String var : vars) {
             if (!isVariable(var)) {
                 System.err.println("Cannot find variable '" + var + "'!");
                 return false;
             }
         }
         return true;
     }
 
     private boolean variableFound(String var) {
         return variablesFound(new String[]{var});
     }
 
     private boolean allAreVariables(String[] vars) {
         for (String var : vars) {
             if (!isVariable(var)) {
                 return false;
             }
         }
         return true;
     }
 
     private boolean isVariable(String var) {
         return variables.containsKey(var);
     }
 
     Matrix scale(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 2) {
             ParseUtils.wrongNumberOfParametersInFunction("scale");
         }
 
         Matrix m;
         double k;
         if (ParseUtils.isDouble(split[0]) && isVariable(split[1])) {
             m = getMatrix(split[1]);
             k = ParseUtils.parseDouble(split[0]);
         } else if (ParseUtils.isDouble(split[1]) && isVariable(split[0])) {
             m = getMatrix(split[0]);
             k = ParseUtils.parseDouble(split[1]);
         } else {
             if (!ParseUtils.isDouble(split[0]) && !isVariable(split[0])) {
                 throw new IllegalArgumentException("Parameter 1 not a variable or number!");
             } else if (!ParseUtils.isDouble(split[1]) && !isVariable(split[1])) {
                 throw new IllegalArgumentException("Parameter 2 not a variable or number!");
             } else if (!ParseUtils.isDouble(split[0]) && !ParseUtils.isDouble(split[1])) {
                 throw new IllegalArgumentException("None of the parameters is a number!");
             } else if (!isVariable(split[0]) && !isVariable(split[1])) {
                 throw new IllegalArgumentException("None of the parameters is a variable!");
             }
             return null;
         }
         return m.scale(k);
     }
 
     Variable add(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 2) {
             ParseUtils.wrongNumberOfParametersInFunction("add");
         }
 
         Variable var1 = variables.get(split[0]);
         Variable var2 = variables.get(split[1]);
 
         if (var1 == null) {
             if (ParseUtils.isDouble(split[0])) {
                 var1 = new Variable(ParseUtils.parseDouble(split[0]));
             } else {
                 throw new IllegalArgumentException("Parameter 1 not a matrix or a number!");
             }
         }
         if (var2 == null) {
             if (ParseUtils.isDouble(split[1])) {
                 var2 = new Variable(ParseUtils.parseDouble(split[1]));
             } else {
                 throw new IllegalArgumentException("Parameter 2 not a matrix or a number!");
             }
         }
 
         if (var1.isMatrix() && var2.isMatrix()) {
             return new Variable(var1.getMatrix().add(var2.getMatrix()));
         }
 
         if (var1.isNumber() && var2.isNumber()) {
             return new Variable(var1.getNumber() + var2.getNumber());
         }
 
         throw new IllegalArgumentException("Not a compatible addition between "
                 + var1.getType() + " and " + var2.getType() + ".");
     }
 
     Variable sub(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 2) {
             ParseUtils.wrongNumberOfParametersInFunction("sub");
         }
 
         Variable var1 = variables.get(split[0]);
         Variable var2 = variables.get(split[1]);
 
         if (var1 == null) {
             if (ParseUtils.isDouble(split[0])) {
                 var1 = new Variable(ParseUtils.parseDouble(split[0]));
             } else {
                 throw new IllegalArgumentException("Parameter 1 not a matrix or a number!");
             }
         }
         if (var2 == null) {
             if (ParseUtils.isDouble(split[1])) {
                 var2 = new Variable(ParseUtils.parseDouble(split[1]));
             } else {
                 throw new IllegalArgumentException("Parameter 2 not a matrix or a number!");
             }
         }
 
         if (var1.isMatrix() && var2.isMatrix()) {
             return new Variable(var1.getMatrix().sub(var2.getMatrix()));
         }
 
         if (var1.isNumber() && var2.isNumber()) {
             return new Variable(var1.getNumber() - var2.getNumber());
         }
 
         throw new IllegalArgumentException("Not a compatible subtraction between "
                 + var1.getType() + " and " + var2.getType() + ".");
     }
 
     Variable mul(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 2) {
             ParseUtils.wrongNumberOfParametersInFunction("mul");
         }
 
         Variable var1 = variables.get(split[0]);
         Variable var2 = variables.get(split[1]);
 
         if (var1 == null) {
             if (ParseUtils.isDouble(split[0])) {
                 var1 = new Variable(ParseUtils.parseDouble(split[0]));
             } else {
                 throw new IllegalArgumentException("Parameter 1 not a matrix or a number!");
             }
         }
         if (var2 == null) {
             if (ParseUtils.isDouble(split[1])) {
                 var2 = new Variable(ParseUtils.parseDouble(split[1]));
             } else {
                 throw new IllegalArgumentException("Parameter 2 not a matrix or a number!");
             }
         }
 
         if (var1.getType() == Variable.VarType.MATRIX) {
             if (var2.getType() == Variable.VarType.MATRIX) {
                 return new Variable(((Matrix) var1.getObject()).mul((Matrix) var2.getObject()));
             }
             if (var2.getType() == Variable.VarType.NUMBER) {
                 return new Variable(((Matrix) var1.getObject()).scale((Double) var2.getObject()));
             }
         }
         if (var1.getType() == Variable.VarType.NUMBER) {
             if (var2.getType() == Variable.VarType.MATRIX) {
                 return new Variable(((Matrix) var2.getObject()).scale((Double) var1.getObject()));
             }
             if (var2.getType() == Variable.VarType.NUMBER) {
                 return new Variable(((Double) var1.getObject()) * ((Double) var2.getObject()));
             }
         }
 
         return null;
     }
 
     Matrix mulN(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 2) {
             ParseUtils.wrongNumberOfParametersInFunction("mulN");
         }
         if (!variablesFound(split)) {
             return null;
         }
         Matrix m1 = getMatrix(split[0]);
         Matrix m2 = getMatrix(split[1]);
 
         return m1.mulNaive(m2);
     }
 
     Matrix mulS(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 2) {
             ParseUtils.wrongNumberOfParametersInFunction("mulS");
         }
         if (!variablesFound(split)) {
             return null;
         }
         Matrix m1 = getMatrix(split[0]);
         Matrix m2 = getMatrix(split[1]);
 
         return m1.mulStrassen(m2);
     }
 
     Variable pow(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 2) {
             ParseUtils.wrongNumberOfParametersInFunction("pow");
         }
 
         Variable var1 = variables.get(split[0]);
         Variable var2 = variables.get(split[1]);
         if (var1 == null) {
             if (ParseUtils.isDouble(split[0])) {
                 var1 = new Variable(ParseUtils.parseDouble(split[0]));
             } else {
                 throw new IllegalArgumentException("Parameter 1 not a matrix or a number!");
             }
         }
         if (var2 == null) {
            if (ParseUtils.isDouble(split[1])) {
                 var2 = new Variable(ParseUtils.parseDouble(split[1]));
             } else {
                throw new IllegalArgumentException("Parameter 2 not a number!");
             }
         }
         if (!var2.isNumber()) {
             throw new IllegalArgumentException("Parameter 1 not a number!");
         }
        if (var1.isLU()) {
             throw new IllegalArgumentException("Cannot take power of a LU!");
         }

         if (var1.isNumber()) {
             return new Variable(Math.pow(var1.getNumber(), var2.getNumber()));
         }
         if (var1.isMatrix()) {
             double num = var2.getNumber();
            if (num != Math.floor(num) || num < -1) {
                 throw new IllegalArgumentException(num + " not an integer >= -1.");
             }
             int e = (int) num;
             return new Variable(var1.getMatrix().pow(e));
         }

         return null;
     }
 
     Matrix transpose(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 1) {
             ParseUtils.wrongNumberOfParametersInFunction("transpose");
         }
         if (!variableFound(split[0])) {
             return null;
         }
         return getMatrix(split[0]).transpose();
     }
 
     Double det(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 1) {
             ParseUtils.wrongNumberOfParametersInFunction("det");
         }
         if (!variableFound(split[0])) {
             return null;
         }
         return getMatrix(split[0]).det();
     }
 
     Matrix inv(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 1) {
             ParseUtils.wrongNumberOfParametersInFunction("inv");
         }
         if (!variableFound(split[0])) {
             return null;
         }
        Matrix m = getMatrix(split[0]).inv();
        if (m == null) {
            System.err.println("Matrix singular! Inverse does not exist.");
        }
        return m;
     }
 
     LU lu(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 1) {
             ParseUtils.wrongNumberOfParametersInFunction("lu");
         }
         if (!variableFound(split[0])) {
             return null;
         }
         return getMatrix(split[0]).lu();
     }
 
     Matrix getL(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 1) {
             ParseUtils.wrongNumberOfParametersInFunction("getL");
         }
         if (!variableFound(split[0])) {
             return null;
         }
         Variable var = variables.get(split[0]);
         if (var.getType() != Variable.VarType.LU) {
             throw new IllegalArgumentException("Parameter in function getL must be an LU-variable!");
         }
         return ((LU) var.getObject()).getPermutatedL();
     }
 
     Matrix getU(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 1) {
             ParseUtils.wrongNumberOfParametersInFunction("getU");
         }
         if (!variableFound(split[0])) {
             return null;
         }
         Variable var = variables.get(split[0]);
         if (var.getType() != Variable.VarType.LU) {
             throw new IllegalArgumentException("Parameter in function getU must be an LU-variable!");
         }
         return ((LU) var.getObject()).getU();
     }
 
     private Matrix getMatrix(String varName) {
         Variable var = variables.get(varName);
         if (var.getType() != Variable.VarType.MATRIX) {
             throw new IllegalArgumentException("Variable " + varName + " not a matrix!");
         }
         return (Matrix) var.getObject();
     }
 }
