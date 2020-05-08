 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class DerefOp extends UnaryOp
 {
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public DerefOp(String strName)
     {
         super(strName);
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public STO checkOperand(STO operand)
     {
         STO resultSTO;
 
         // Check #15a - operand is pointer type
         // Check operand
         if(!operand.getType().isPtrGrp()) {
             return(new ErrorSTO(Formatter.toString(ErrorMsg.error15_Receiver, operand.getType().getName())));
         }
 
         /* Don't need to worry about constant folding because constant pointers WNBT */
 
        resultSTO = operand;
 
         return resultSTO;
     }
 
     public STO doOperation(ConstSTO operand, Type resultType)
     {
         Double value = 0.0;
         boolean b_value = true;
 
         b_value = !(operand.getBoolValue());
 
         if(b_value)
             value = new Double(1);
         else
             value = new Double(0);
 
         return new ConstSTO("DerefOp.doOperation Result", resultType, value);
     }
 }
