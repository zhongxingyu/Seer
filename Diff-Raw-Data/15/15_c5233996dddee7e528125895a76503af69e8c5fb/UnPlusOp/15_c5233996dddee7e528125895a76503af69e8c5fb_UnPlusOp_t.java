 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class UnPlusOp extends UnaryOp
 {
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public 
     UnPlusOp(String strName)
     {
         super(strName);
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public STO
     checkOperand(STO operand)
     {
         STO resultSTO;
 
         // Check #1 - UnPlus - operand numeric
         // Check operand
         if(!operand.getType().isNumeric())
         {
             return(new ErrorSTO(Formatter.toString("Incompatible type %T to unary operator %O, numeric expected.", operand.getType().getName(), this.getName())));
         }
 
         if(operand.isConst())
         {
            resultSTO = new ConstSTO("UnMinus.checkOperand() Result", new IntType());
         }
         else
         {
            resultSTO = new ExprSTO("UnMinus.checkOperand() Result", new IntType());
         }
 
         return resultSTO;
 }
 
     public STO
     doOperation(ConstSTO operand, Type resultType)
     {
         Double value = 0.0;
 
         if(resultType.isInt())
         {
             value = new Double(+operand.getIntValue());
         }
         else if(resultType.isFloat())
         {
             value = new Double(+operand.getFloatValue());
         }
 
         return new ConstSTO("UnPlusOp.doOperation Result", resultType, value);
     }
 
 }
