 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class VarSTO extends STO
 {
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public 
     VarSTO(String strName)
     {
        super (strName);
         // You may want to change the isModifiable and isAddressable 
         // fields as necessary
        setIsModifiable(true);
        setIsAddressable(true);
     }
 
     public 
     VarSTO(String strName, Type typ)
     {
        super (strName, typ);
         // You may want to change the isModifiable and isAddressable 
         // fields as necessary
        setIsModifiable(true);
        setIsAddressable(true);
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public boolean   
     isVar() 
     {
         return true;
     }
 }
