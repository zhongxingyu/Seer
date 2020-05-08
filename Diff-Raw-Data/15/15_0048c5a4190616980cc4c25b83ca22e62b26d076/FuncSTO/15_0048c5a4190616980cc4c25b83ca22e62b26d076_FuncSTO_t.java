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
     private boolean m_hasReturnStatement;
     private int m_level;
 
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public 
     FuncSTO(String strName)
     {
         this(strName, new Vector(), false);
     }
 
     public 
     FuncSTO(String strName, Vector<ParamSTO> params, boolean retByRef)
     {
         super(strName, new FuncPtrType());
         setNumOfParams(params.size());
         setParameters(params);        
         setReturnByRef(retByRef);
         setReturnType(null);
         setHasReturnStatement(false);
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
     // the function's type(for function pointers).
     //----------------------------------------------------------------
 
     //////////////////////////////
     //      m_returnType        //
     //////////////////////////////
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
 
     //////////////////////////////
     //      m_numOfParams       //
     //////////////////////////////
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
 
     //////////////////////////////
     //      m_parameters        //
     //////////////////////////////
     public Vector<ParamSTO>
     getParameters()
     {
         return m_parameters;
     }
 
     public void
     setParameters(Vector<ParamSTO> params)
     {
         m_parameters = params;
        setNumOfParams(params.size());
     }
 
     //////////////////////////////
     //      m_returnByReference //
     //////////////////////////////
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
 
     //////////////////////////////
     //     m_hasReturnStatement //
     //////////////////////////////
     // m
     public void
     setHasReturnStatement(boolean hasRtnStmt)
     {
         m_hasReturnStatement = hasRtnStmt;
     }
 
     public boolean
     getHasReturnStatement()
     {
         return m_hasReturnStatement;
     }
 
     //////////////////////////////
     //      m_level             //
     //////////////////////////////
     public void
     setLevel(int level)
     {
         m_level = level;
     }
 
     public int
     getLevel()
     {
         return m_level;
     }
 }
