 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 
 class PtrGrpType extends CompositeType
 {
     //---------------------------------------------------------------------
     //      Instance Variables
     //---------------------------------------------------------------------
     private Type m_pointsToType;
 
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public PtrGrpType(String strName, int size, Type pointsTo)
     {
         super(strName, size);
         m_pointsToType = pointsTo; 
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public Type getPointsToType()
     {
         return m_pointsToType;

     public void setPointsToType(Type pointsTo)
     {
         m_pointsToType = pointsTo;
     }
 
     public boolean isPtrGrp()
     {
         return true;
     }
 
 }
