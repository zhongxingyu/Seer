 //---------------------------------------------------------------------
 // 
 //---------------------------------------------------------------------
 
 
 class NullPtrType extends PointerType
 {
     //---------------------------------------------------------------------
     //      Constants
     //---------------------------------------------------------------------
     private static final String NULLPTR_NAME = "nullptr";
    private static final int NULLPTR_SIZE    = "4";
 
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public 
     NullPtrType ()
     {
         super(NULLPTR_NAME, NULLPTR_SIZE);
     }
 
     public 
     NullPtrType (String strName)
     {
         super(strName, NULLPTR_SIZE);
     }
 
     public 
     NullPtrType (String strName, int size)
     {
         super(strName, size);
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public boolean isNullPtr() { return true; }
 
 }
