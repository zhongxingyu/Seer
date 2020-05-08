 //----------------------------------------------------------------
 //
 //----------------------------------------------------------------
 class VoidType extends Type
 {
     //---------------------------------------------------------------------
    //      Constants
    //---------------------------------------------------------------------
    private static final String VOID_NAME = "void";
    private static final int VOID_SIZE    = 0;

    //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
    public
    VoidType()
    {
        this(VOID_NAME, VOID_SIZE);
    }

    public
    VoidType(String strName)
    {
        this(strName, VOID_SIZE);
    }

     public 
     VoidType (String strName, int size)
     {
         super(strName, size);     
     }
 
     //methods
     public boolean isVoid() { return true; }
 }
