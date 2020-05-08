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
         if(!operand.getType().isPointer()) {
             return(new ErrorSTO(Formatter.toString(ErrorMsg.error15_Receiver, operand.getType().getName())));
         }
 
         /* TODO: Find out if we need this
         if(operand.isConst()) {
             resultSTO = new ConstSTO("DerefOp.checkOperands() Result", new BoolType());
         }
         else {
             resultSTO = new ExprSTO("DerefOp.checkOperands() Result", new BoolType());
         }
         */
 
        resultSTO = new VarSTO("DerefOp.checkOperands() Result", ((PtrGrpType)operand.getType()).getPointsToType());
 
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
