 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 import java.util.Vector;
 
 class FuncPtrType extends PtrGrpType
 {
     //----------------------------------------------------------------
     //    Instance variables.
     //----------------------------------------------------------------
     private Type             m_returnType;
     private int              m_numOfParams;
     private Vector<ParamSTO> m_parameters;
     private boolean          m_returnByReference;
 
     //---------------------------------------------------------------------
     //      Constants
     //---------------------------------------------------------------------
     private static final String FUNCPTR_NAME = "funcptr";
     private static final int FUNCPTR_SIZE    = 8;
 
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public FuncPtrType(Type returnType, boolean returnByRef)
     {
         this(returnType, returnByRef, new Vector<ParamSTO>()); 
     }
 
     public FuncPtrType(Type returnType, boolean returnByRef, Vector<ParamSTO> paramList)
     {
         super(FUNCPTR_NAME, FUNCPTR_SIZE);
         setReturnType(returnType);
         setReturnByRef(returnByRef);
         setNumOfParams(paramList.size());
         setParameters(paramList);
         
         setFuncPtrName();
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public boolean isFuncPtr()
     {
         return true;
     }
 
     //////////////////////////////
     //      m_returnType        //
     //////////////////////////////
     public void setReturnType(Type typ)
     {
         m_returnType = typ;
     }
 
     public Type getReturnType()
     {
         return m_returnType;
     }
 
     //////////////////////////////
     //      m_numOfParams       //
     //////////////////////////////
     public void setNumOfParams(int numParams)
     {
         m_numOfParams = numParams;
     }
 
     public int getNumOfParams()
     {
         return m_numOfParams;
     }
 
     //////////////////////////////
     //      m_parameters        //
     //////////////////////////////
     public Vector<ParamSTO>
     getParameters()
     {
         return m_parameters;
     }
 
     public void setParameters(Vector<ParamSTO> params)
     {
         m_parameters = params;
         setNumOfParams(params.size());
         setFuncPtrName();
     }
 
     //////////////////////////////
     //      m_returnByReference //
     //////////////////////////////
     public void setReturnByRef(boolean retByRef)
     {
         m_returnByReference = retByRef;
     }
 
     public boolean getReturnByRef()
     {
         return m_returnByReference;
     }
 
     public boolean isEquivalent(Type type)
     {
 
         // Is type a function pointer
         if(!type.isFuncPtr())
             return false;
 
         // is return type same
         if(!getReturnType().isEquivalent(((FuncPtrType) type).getReturnType()))
             return false;
 
         // returnByReference the same
         if(getReturnByRef() != ((FuncPtrType) type).getReturnByRef())
             return false;
 
         // Parameter List - types only, not id names
         if(getNumOfParams() != ((FuncPtrType) type).getNumOfParams())
             return false;
 
         for(int i = 0; i < getNumOfParams(); i++) {
             ParamSTO thisParam1 = getParameters().elementAt(i);
             ParamSTO thisParam2 = ((FuncPtrType) type).getParameters().elementAt(i);
 
             if(!thisParam1.getType().isEquivalent(thisParam2.getType()))
                 return false;
 
            if(!thisParam1.isPassByReference() != thisParam2.isPassByReference())
                 return false;
         }
         
         return true;
     }
 
     public void setFuncPtrName()
     {
         String name = "funcptr : ";
         if(getReturnType() == null)
             name += "null";
         else
             name += getReturnType().getName();
 
         if(getReturnByRef())
             name += " & (";
         else
             name += " (";
         
         if(getNumOfParams() == 0)
             name += ")";
         else {
             Vector<String> paramNames = new Vector<String>();
 
             for(ParamSTO thisParam: getParameters()) {
                 String thisParamName = thisParam.getType().getName();
 
                 if(thisParam.isPassByReference())
                     thisParamName += " & " + thisParam.getName();
                 else
                     thisParamName += " " + thisParam.getName();
 
                 paramNames.addElement(thisParamName);
             }
         
             name += paramNames.elementAt(0);
 
             if(paramNames.size() == 1)
                 name += ")";
             else {
                 for(int i = 1; i < paramNames.size(); i++)
                     name += ", " + paramNames.elementAt(i);
 
                 name += ")";
             }
         }
         setName(name);
     }
 }
