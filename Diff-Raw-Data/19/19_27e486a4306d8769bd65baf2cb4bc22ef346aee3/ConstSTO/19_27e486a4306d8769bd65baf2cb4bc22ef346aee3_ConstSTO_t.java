 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class ConstSTO extends STO
 {
 
     //---------------------------------------------------------------------
     //      Instance Variables
     //---------------------------------------------------------------------
     //----------------------------------------------------------------
     //    Constants have a value, so you should store them here.
     //    Note: We suggest using Java's Double class, which can hold
     //    floats and ints. You can then do .floatValue() or 
     //    .intValue() to get the corresponding value based on the
     //    type. Booleans/Ptrs can easily be handled by ints.
     //    Feel free to change this if you don't like it!
     //----------------------------------------------------------------
     private Double m_value;
 
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public 
     ConstSTO(String strName)
     {
         super(strName);
         m_value = null; // fix this
                 // You may want to change the isModifiable and isAddressable                      
                 // fields as necessary
     }
 
     public 
     ConstSTO(String strName, Type typ)
     {
         super(strName, typ);
         m_value = null; // fix this
                 // You may want to change the isModifiable and isAddressable                      
                 // fields as necessary
     }
 
     public
     ConstSTO(String strName, Type typ, Double val)
     {
         super(strName, typ);
         setValue(val);
     }
 
     public
     ConstSTO(String strName, Type typ, String val)
     {
         super(strName, typ);
        //System.out.println("strName: " + strName);
        //System.out.println("val: " + val);
         //System.out.println("Type: " + typ.getName());
 
         if(val.equals("true"))
         {
             setValue((double) 1);
         }
 
         else if(val.equals("false"))
         {
             setValue((double) 0);
         }
         else
         {
             int base;
             String str;
 
             // Check for hex
             if(val.toLowerCase().startsWith("0x"))
             {
                 str = val.replace("0x", "");
                 base = 16;
                 //System.out.println("\tIt's hex");
             }
             // Check for octal
             else if(val.startsWith("0") && val.length() > 1) 
             {
                 str = val.substring(1);
                 base = 8;
                 //System.out.println("\tIt's octal");
             }
 
             // It's decimal
             else
             {
                 str = val;
                 base = 10;
                 //System.out.println("\tIt's decimal");
             }
             
             Double value;
 
             if(typ.isInt())
             {
                 //System.out.println("\tIt's int");
 
                 Integer i = Integer.parseInt(str, base);
                 value = new Double(i);
             }
             else
             {
                 //System.out.println("\tIt's float");
                 Float f = Float.parseFloat(str);
                 value = f.doubleValue();
             }
 
             setValue(value);
 
             //System.out.println("\t\t\tFinal Value: " + value);
         }
 
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public boolean
     isConst() 
     {
         return true;
     }
 
     public void
     setValue(Double val) 
     {
         m_value = new Double(val);
     }
 
     public Double
     getValue() 
     {
         return m_value;
     }
 
     public int
     getIntValue() 
     {
         //if(m_value == null) //System.out.printf("m_value: null");
         //System.out.println("m_value:" + m_value.toString());
         return m_value.intValue();
     }
 
     public float
     getFloatValue() 
     {
         return m_value.floatValue();
     }
 
     public boolean
     getBoolValue() 
     {
         return(m_value.intValue() != 0);
     }
 
     public void
     setIntValue(int value)
     {
         m_value = new Double(value);
     }
 
     public void
     setFloatValue(float value)
     {
         m_value = new Double(value);
     }
 
     public void
     setBoolValue(boolean value)
     {
         if(value)
             m_value = new Double(1);
         else
             m_value = new Double(0);
     }
 
 }
