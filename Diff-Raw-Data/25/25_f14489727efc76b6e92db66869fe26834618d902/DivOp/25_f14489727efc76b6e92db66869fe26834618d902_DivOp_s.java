 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class DivOp extends ArithmeticOp
 {
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public 
     DivOp(String strName)
     {
         super(strName);
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
 
     public STO
     doOperation(ConstSTO operand1, ConstSTO operand2, Type resultType)
     {
         Double value = 0.0;
 
         {
         }
         if(resultType.isInt())
         {
             if(operand2.getType().isInt())
                 if(operand2.getIntValue() == 0)
                     return new ErrorSTO(ErrorMsg.error8_Arithmetic);
 
             value = new Double(operand1.getIntValue() / operand2.getIntValue());
         }
         else if(resultType.isFloat())
         {
             if(operand2.getType().isFloat())
                 if(operand2.getFloatValue() == 0.0)
                     return new ErrorSTO(ErrorMsg.error8_Arithmetic);
 
             value = new Double(operand1.getFloatValue() / operand2.getFloatValue());
         }
 
         return new ConstSTO("DivOp.doOperation Result", resultType, value);
     }
 
 
 }
