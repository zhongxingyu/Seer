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
     public StructType(String strName, int size, Vector<STO> fieldList)
     {
         super(strName, size);
         setFields(fieldList);
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
 
     public void setFields(Vector<STO> fields)
     {
         m_fieldList = fields;
     }
     
     public boolean isEquivalent(Type type)
     {
        return (m_typeName == type.getName());
     }
 }
