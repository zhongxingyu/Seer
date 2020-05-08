 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 import java.util.Vector;
 
 class FuncSTO extends STO
 {
     //----------------------------------------------------------------
     //    Instance variables.
     //----------------------------------------------------------------
     private Type m_returnType;
     private int m_numOfParams;
     private Vector<ParamSTO> m_parameters;
     private boolean m_returnByReference;
 
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public 
     FuncSTO(String strName)
     {
         super(strName);
         setNumOfParams(0);
         setParameters(new Vector());        
         setReturnByRef(false);
         setReturnType (null);
     }
 
     public 
     FuncSTO(String strName, Vector<ParamSTO> params, boolean retByRef)
     {
         super(strName);
         setNumOfParams(params.size());
         setParameters(params);        
         setReturnByRef(retByRef);
         setReturnType (null);
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public boolean
     isFunc() 
     { 
         return true;
     }
 
     //----------------------------------------------------------------
     // This is the return type of the function. This is different from 
     // the function's type (for function pointers).
     //----------------------------------------------------------------
     public void
     setReturnType(Type typ)
     {
         m_returnType = typ;
     }
 
     public Type
     getReturnType()
     {
         return m_returnType;
     }
 
     private void
     setNumOfParams(int numParams)
     {
         m_numOfParams = numParams;
     }
 
     public int
     getNumOfParams()
     {
         return m_numOfParams;
     }
 
     public Vector<ParamSTO>
     getParameters()
     {
         return m_parameters;
     }
 
    public void
     setParameters(Vector<ParamSTO> params)
     {
         m_parameters = params;
     }
 
     private void
     setReturnByRef(boolean retByRef)
     {
         m_returnByReference = retByRef;
     }
 
     public boolean
     getReturnByRef()
     {
         return m_returnByReference;
     }
 }
