 //---------------------------------------------------------------------
 // 
 //---------------------------------------------------------------------
 
 class ArrayType extends CompositeType
 {
 	Type elementType;
 	STO dimensionSize;
 	
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public 
     ArrayType(Type elementType, STO dimenionSize)
     {
         super(elementType.getName()+"["+((ConstSTO) dimenionSize).getIntValue()+"]", ((ConstSTO) dimenionSize).getIntValue());
         setElementType(elementType);
         setDimensionSize(dimenionSize);
 
     }
     
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public boolean isArray() { return true; }
     
     public Type getElementType() {
     	return elementType;
     }
     
     public void setElementType(Type type) {
     	elementType = type;
     }
     
     public STO getDimensionSize() {
     	return dimensionSize;
     }
     
     public void setDimensionSize(STO size) {
     	dimensionSize = size;
     }
 
     public STO checkArray() {
    	STO result = dimensionSize;
         // Check #10
        if(!dimensionSize.getType().isEquivalent(new IntType()))
         {
        	result = (new ErrorSTO(Formatter.toString(ErrorMsg.error10i_Array, dimensionSize.getType().getName())));    
         } 
         else if(!dimensionSize.isConst())
         {
         	result = (new ErrorSTO(ErrorMsg.error10c_Array));    
         }
         else if(((ConstSTO) dimensionSize).getIntValue() <= 0)
         {
         	result = (new ErrorSTO(Formatter.toString(ErrorMsg.error10z_Array,((ConstSTO)dimensionSize).getIntValue())));
         }
         return result;
     }
 }
