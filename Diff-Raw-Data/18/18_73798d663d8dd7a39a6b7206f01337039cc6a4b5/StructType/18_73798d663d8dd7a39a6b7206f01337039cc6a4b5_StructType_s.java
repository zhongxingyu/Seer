 import java.util.Vector;
 
 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class StructType extends CompositeType
 {
     private Vector<STO> m_fieldList;
 
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
    public StructType(String strName, int size)
     {
         super(strName, size);
     }
 
     public StructType(String strName)
     {
         super(strName, 4);
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public boolean isStruct()
     {
         return true;
     }
 
     Vector<STO> getFields()
     {
         return m_fieldList;
     }
 
     void setFields(Vector<STO> fields)
     {
         m_fieldList = fields;
     }
 }
