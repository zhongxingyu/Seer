 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class NotOp extends UnaryOp
 {
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public 
     NotOp(String strName)
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
 
         // Check #1 - Not - operand bool
         // Check operand
         if(!operand.getType().isBool())
         {
             return (new ErrorSTO(Formatter.toString(ErrorMsg.error1u_Expr, operand.getType().getName(), this.getName(), "bool")));
         }
 
        resultSTO = new ExprSTO("NotOp.checkOperands() Result", new BoolType());
 
         return resultSTO;
     }
 }
