 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 
 class NullPtrType extends PointerType
 {
     //---------------------------------------------------------------------
     //      Constants
     //---------------------------------------------------------------------
     private static final String NULLPTR_NAME = "nullptr";
     private static final int NULLPTR_SIZE    = 4;
 
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public NullPtrType()
     {
         super(NULLPTR_NAME, NULLPTR_SIZE, null);
     }
 
     public NullPtrType(String strName)
     {
         super(strName, NULLPTR_SIZE, null);
     }
 
     public NullPtrType(String strName, int size)
     {
         super(strName, size, null);
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public boolean isNullPtr()
     {
         return true;
     }
 
     public String getName()
     {
         return NULLPTR_NAME;
     }
 
     // meaning: param = this
     public boolean isAssignable(Type type)
     {
         // If trying to assign to pointer, check if pointer type is this array's element type
        if(type.isPointer())
             return true;    
         else
             return false;
     }
 }
