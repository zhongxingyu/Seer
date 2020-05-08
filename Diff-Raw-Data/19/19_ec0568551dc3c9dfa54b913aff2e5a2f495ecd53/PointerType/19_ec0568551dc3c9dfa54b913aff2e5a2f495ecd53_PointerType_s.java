 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class PointerType extends PtrGrpType
 {
     //---------------------------------------------------------------------
     //      Constants
     //---------------------------------------------------------------------
     private static final String POINTER_NAME = "pointer";
     private static final int POINTER_SIZE    = 4;
 
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public PointerType()
     {
         super("null*", POINTER_SIZE, null);
     }
 
     public PointerType(Type pointsTo)
     {
        super(pointsTo.getName() + "*", POINTER_SIZE, pointsTo);
     }
 
     public PointerType(String name, int size, Type pointsTo)
     {
         super(pointsTo.getName() + "*", POINTER_SIZE, pointsTo);
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public boolean isPointer()
     {
         return true;
     }
 
 }
