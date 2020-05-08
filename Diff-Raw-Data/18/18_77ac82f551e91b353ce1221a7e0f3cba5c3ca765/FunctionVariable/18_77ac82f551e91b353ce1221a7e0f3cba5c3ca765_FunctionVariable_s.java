 package base.hldd.structure.variables;
 
 import base.vhdl.structure.Operator;
 import base.hldd.structure.models.utils.PartedVariableHolder;
 import base.Indices;
 import base.Type;
 
 import java.util.*;
 
 /**
  * Class represents a FUNCTION VARIABLE as it is defined in AGM.<br>
  *
  * <p>User: Anton Chepurov
  * <br>Date: 26.02.2007
  * <br>Time: 9:23:37
  */
 public class FunctionVariable extends Variable {
     public static final String FAILED_ADD_OPERAND_TEXT = "Cannot add operand ";
     private static final int INITIAL_OPERAND_COUNT = 5;
 
     /*
     * TYPE of the function
     */
     private Operator operator;
     /*
     * Operands
     */
     protected List<PartedVariableHolder> operands;
 	/**
 	 * Index of function (denotes order)
 	 */
 	private int nameIdx;
 
     /**
      * Constructor to override in inherited classes
      * */
     protected FunctionVariable() {
         setFunction(true);
     }
 
     /**
      * General purpose constructor. Only receives <code>operator</code> and
      * <code>nameIdx</code> as parameters.
      * <p>
      * Operands are filled using {@link #addOperand(AbstractVariable, Indices)}
      * method. Each <code>operator</code> knows the number of supported
      * operands. So an Exception is thrown if this number gets exceeded when
      * calling {@link #addOperand(AbstractVariable, Indices)}.
      *
      * @param operator operator (from the set of supported ones)
      * @param nameIdx name index of the variable
      */
     public FunctionVariable(Operator operator, int nameIdx){
         this(nameIdx);
         this.operator = operator;
         operands = new ArrayList<PartedVariableHolder>(INITIAL_OPERAND_COUNT);
     }
 
     protected FunctionVariable(int nameIdx) {
         this();
         this.nameIdx = nameIdx;
     }
 
     public String toString() {
         return super.toString() +
                 "\nFUN#\t" + operatorToString() + "\t(" + operandsToString() + ")";
     }
 
 	@Override
 	public String getName() {
 		return operatorToString() + "____" + nameIdx;
 	}
 
 	protected String operatorToString() {
         return operator.toString();
     }
 
     private String operandsToString() {
         StringBuilder sb = new StringBuilder();
         int i = 1;
         String delim = operands.size() > 2 ? " " : "\t";
         for (PartedVariableHolder operand : operands) {
             sb.append("A").append(i++).append("<= ");
             String indicesAsString = operand.getPartedIndices() != null
                     ? operand.getPartedIndices().toStringAngular(false)
                     : operand.getVariable().lengthToString();
             sb.append(operand.getVariable().getIndex()).append(indicesAsString);
             sb.append(",").append(delim);
         }
         sb.delete(sb.lastIndexOf(","), sb.length());
         return sb.toString();
     }
 
     /**
      * In order to use this method, after creation of a new FunctionVariable always search for an existent identical FunctionVariable
      * @param comparedAbsVariable variable to be compared with
      * @return <code>true</code> if the fuctions are identical
      */
     // todo: USE this method WHEREVER POSSIBLE! Find such places.
     // todo: rewrite (simplify) the method when input1 and input2 get replaced by operands 
     public boolean isIdenticalTo(AbstractVariable comparedAbsVariable) /*throws Exception*/ {
         /* Compare links */
         if (this == comparedAbsVariable) return true;
         /* Compare classes */
         if (getClass() != comparedAbsVariable.getClass()) return false;
         FunctionVariable comparedVar = (FunctionVariable) comparedAbsVariable;
 
         /* Compare OPERATORS */
         if (operator != comparedVar.operator) return false;
 
         /* Compare OPERANDS */
         if (operands == null ^ comparedVar.operands == null) return false;
         if (operands == null) {
             /* Both are null */
             return true;
         } else {
             /* Both are not null */
             /* Compare NUMBER OF OPERANDS */
             if (operands.size() != comparedVar.operands.size()) return false;
             /* Compare EACH OPERAND */
             for (int i = 0; i < operands.size(); i++) {
                 if (!operands.get(i).isIdenticalTo(comparedVar.operands.get(i))) return false;
             }
         }
 
         /* All checks done */
         return true;
     }
 
     /**
      * @param operandVariable operand to add
      * @param partedIndices parted indices of the operand to add
      * @throws Exception if the operand being added exceeds the limit of the operator operands limit
      */
     public void addOperand(AbstractVariable operandVariable, Indices partedIndices) throws Exception {
         if (!isValidAddition()) throw new Exception(FAILED_ADD_OPERAND_TEXT + " to Function." +
                 "\nOperator " + operator + " supports only " + operator.getNumberOfOperands() + " operand(s).");
         operands.add(new PartedVariableHolder(operandVariable, partedIndices));
         /* Update highestSB */
         type = adjustType(type, operandVariable.getType(), partedIndices);
     }
 
     protected boolean isValidAddition() {
         return operands.size() < operator.getNumberOfOperands();
     }
 
     protected Type adjustType(Type currentType, Type addedType, Indices addedPartedIndices) {
         /* Update highestSB */
         Indices addedLength = addedType.getLength();
         if (currentType == null) {
             Indices adjustedLength = operator.adjustLength(null, addedLength, addedPartedIndices);
             currentType = new Type(adjustedLength);
         } else {
             Indices adjustedLength = operator.adjustLength(currentType.getLength(), addedLength, addedPartedIndices);
             currentType.setLength(adjustedLength);
         }
         return currentType;
     }
 
     /* Getters START */
 
     public Operator getOperator() {
         return operator;
     }
 
     public List<PartedVariableHolder> getOperands() {
         return operands;
     }
 
 	public int getNameIdx() {
 		return nameIdx;
 	}
 
 	/* Getters END */
 
     /* Setters START */
 
     public void setOperator(Operator operator) {
         this.operator = operator;
     }
 
     public void setOperand(int index, PartedVariableHolder operandHolder) {
         operands.set(index, operandHolder);
     }
 
 	public void setNameIdx(int nameIdx) {
 		this.nameIdx = nameIdx;
 	}
 
 	/* Setters END */
 
 	public static Comparator<FunctionVariable> getComparator() {
 		return new FunctionsComparator();
 	}
 
 	public static class FunctionsComparator implements Comparator<FunctionVariable> {
 
 		@Override
 		public int compare(FunctionVariable o1, FunctionVariable o2) {
 
			int compNames = o1.operator.name().compareTo(o2.operator.name());
 
 			if (compNames != 0) {
 				return compNames < 0 ? -1 : 1;
 			}
 
 			return new Integer(o1.nameIdx).compareTo(o2.nameIdx);
 		}
 	}
 }
