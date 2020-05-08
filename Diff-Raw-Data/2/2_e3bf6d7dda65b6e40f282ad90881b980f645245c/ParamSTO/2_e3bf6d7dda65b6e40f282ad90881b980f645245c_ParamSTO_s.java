 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class ParamSTO extends STO
 {
     //---------------------------------------------------------------------
     //      Instance Variables
     //---------------------------------------------------------------------
     private boolean m_passByReference; 
 
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public 
     ParamSTO(String strName, Type type, boolean passByRef)
     {
         super(strName, type);

     }
     
     public
     ParamSTO(String strName)
     {
         super(strName);
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
 
     public boolean
     isPassByReference()
     {
         return m_passByReference;
     }
 
     private void
     setPassByReference(boolean passByRef)
     {
         m_passByReference = passByRef;
     }
 
     public boolean   
     isParam() 
     {
         return true;
     }
 }
