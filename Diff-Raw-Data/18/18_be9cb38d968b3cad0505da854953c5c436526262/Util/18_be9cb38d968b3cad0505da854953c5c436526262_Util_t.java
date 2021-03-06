 //
 // THIS FILE IS AUTOMATICALLY GENERATED!!
 //
 // Generated at 2009-02-18 by the VDM++ to JAVA Code Generator
 // (v8.2b - Fri 13-Feb-2009 09:10:36)
 //
 // Supported compilers: jdk 1.4/1.5/1.6
 //
 
 // ***** VDMTOOLS START Name=HeaderComment KEEP=NO
 // ***** VDMTOOLS END Name=HeaderComment
 
 // ***** VDMTOOLS START Name=package KEEP=NO
 package org.overturetool.traces;
 
 // ***** VDMTOOLS END Name=package
 
 // ***** VDMTOOLS START Name=imports KEEP=YES
 
 import jp.co.csk.vdm.toolbox.VDM.*;
 import java.util.*;
 import org.overturetool.ast.itf.*;
 // ***** VDMTOOLS END Name=imports
 
 
 
 public class Util extends StdLib {
 
 // ***** VDMTOOLS START Name=vdmComp KEEP=NO
   static UTIL.VDMCompare vdmComp = new UTIL.VDMCompare();
 // ***** VDMTOOLS END Name=vdmComp
 
 // ***** VDMTOOLS START Name=writeType KEEP=NO
  private static Object writeType = new jp.co.csk.vdm.toolbox.VDM.quotes.start();
 // ***** VDMTOOLS END Name=writeType
 
 // ***** VDMTOOLS START Name=buf KEEP=NO
   private static String buf = new String("");
 // ***** VDMTOOLS END Name=buf
 
 // ***** VDMTOOLS START Name=outputFileName KEEP=NO
   public static String outputFileName = new String("tmp.xmi");
 // ***** VDMTOOLS END Name=outputFileName
 
 
 // ***** VDMTOOLS START Name=vdm_init_Util KEEP=NO
   private void vdm_init_Util () throws CGException {}
 // ***** VDMTOOLS END Name=vdm_init_Util
 
 
 // ***** VDMTOOLS START Name=Util KEEP=NO
   public Util () throws CGException {
     vdm_init_Util();
   }
 // ***** VDMTOOLS END Name=Util
 
 
 // ***** VDMTOOLS START Name=Put#1|String KEEP=NO
   static public void Put (final String pVal) throws CGException {
 
     String rhs_2 = null;
     rhs_2 = buf.concat(pVal);
     buf = UTIL.ConvertToString(UTIL.clone(rhs_2));
   }
 // ***** VDMTOOLS END Name=Put#1|String
 
 
 // ***** VDMTOOLS START Name=ViewBuf KEEP=NO
   static public void ViewBuf () throws CGException {
     Print(buf);
   }
 // ***** VDMTOOLS END Name=ViewBuf
 
 
 // ***** VDMTOOLS START Name=SaveBuf#1|String KEEP=NO
   static public void SaveBuf (final String fileName) throws CGException {
 
     SetFileName(fileName);
     PrintL(buf);
   }
 // ***** VDMTOOLS END Name=SaveBuf#1|String
 
 
 // ***** VDMTOOLS START Name=Clear KEEP=NO
   static public void Clear () throws CGException {
     buf = UTIL.ConvertToString(UTIL.clone(new String("")));
   }
 // ***** VDMTOOLS END Name=Clear
 
 
 // ***** VDMTOOLS START Name=Print#1|String KEEP=NO
   static public void Print (final String debugString) throws CGException {
 
     IOProxy file = (IOProxy) new IOProxy();
     file.print(debugString);
   }
 // ***** VDMTOOLS END Name=Print#1|String
 
 
 // ***** VDMTOOLS START Name=SaveCharSeqMapSeqSeq#2|String|HashMap KEEP=NO
   static public void SaveCharSeqMapSeqSeq (final String filename, final HashMap val) throws CGException {
 
     Boolean tmpVal_4 = null;
     IOProxy obj_5 = null;
     obj_5 = (IOProxy) new IOProxy();
    tmpVal_4 = (Boolean) obj_5.fwriteval(filename, val, new jp.co.csk.vdm.toolbox.VDM.quotes.start());
   }
 // ***** VDMTOOLS END Name=SaveCharSeqMapSeqSeq#2|String|HashMap
 
 
 // ***** VDMTOOLS START Name=PrintL#1|String KEEP=NO
   static public void PrintL (final String line) throws CGException {
 
     IOProxy file = (IOProxy) new IOProxy();
     file.overwrite(outputFileName, line);
   }
 // ***** VDMTOOLS END Name=PrintL#1|String
 
 
 // ***** VDMTOOLS START Name=SetFileName#1|String KEEP=NO
   static public void SetFileName (final String name) throws CGException {
 
     outputFileName = UTIL.ConvertToString(UTIL.clone(name));
    writeType = UTIL.clone(new jp.co.csk.vdm.toolbox.VDM.quotes.start());
   }
 // ***** VDMTOOLS END Name=SetFileName#1|String
 
 
 // ***** VDMTOOLS START Name=ExpandSpecTracesToString#1|HashMap KEEP=NO
   static public HashMap ExpandSpecTracesToString (final HashMap tc_um) throws CGException {
 
     HashMap rexpr_2 = new HashMap();
     HashMap res_m_3 = new HashMap();
     {
 
       HashSet e_set_38 = new HashSet();
       e_set_38.clear();
       e_set_38.addAll(tc_um.keySet());
       String clnm = null;
       {
         for (Iterator enm_41 = e_set_38.iterator(); enm_41.hasNext(); ) {
 
           String elem_40 = UTIL.ConvertToString(enm_41.next());
           clnm = elem_40;
           HashMap mr_5 = new HashMap();
           HashMap res_m_6 = new HashMap();
           {
 
             HashSet e_set_31 = new HashSet();
             e_set_31.clear();
             e_set_31.addAll(((HashMap) tc_um.get(clnm)).keySet());
             String tdnm = null;
             {
               for (Iterator enm_36 = e_set_31.iterator(); enm_36.hasNext(); ) {
 
                 String elem_35 = UTIL.ConvertToString(enm_36.next());
                 tdnm = elem_35;
                 HashMap mr_8 = new HashMap();
                 {
 
                   Vector tc_ul = (Vector) UTIL.ConvertToList(SetToSeq((HashSet) ((HashMap) tc_um.get(clnm)).get(tdnm)));
                   HashMap res_m_16 = new HashMap();
                   {
 
                     HashSet e_set_23 = new HashSet();
                     HashSet riseq_25 = new HashSet();
                     int max_26 = tc_ul.size();
                     for (int i_27 = 1; i_27 <= max_26; i_27++) 
                       riseq_25.add(new Long(i_27));
                     e_set_23 = riseq_25;
                     Long i = null;
                     {
                       for (Iterator enm_29 = e_set_23.iterator(); enm_29.hasNext(); ) {
 
                         Long elem_28 = UTIL.NumberToLong(enm_29.next());
                         i = elem_28;
                         Vector mr_18 = null;
                         Vector par_19 = null;
                         if ((1 <= i.intValue()) && (i.intValue() <= tc_ul.size())) 
                           par_19 = (Vector) UTIL.ConvertToList(tc_ul.get(i.intValue() - 1));
                         else 
                           UTIL.RunTime("Run-Time Error:Illegal index");
                         mr_18 = ExprToString(par_19);
                         res_m_16.put(i, mr_18);
                       }
                     }
                   }
                   mr_8 = res_m_16;
                 }
                 res_m_6.put(tdnm, mr_8);
               }
             }
           }
           mr_5 = res_m_6;
           res_m_3.put(clnm, mr_5);
         }
       }
     }
     rexpr_2 = res_m_3;
     return rexpr_2;
   }
 // ***** VDMTOOLS END Name=ExpandSpecTracesToString#1|HashMap
 
 
 // ***** VDMTOOLS START Name=ExprToString#1|Vector KEEP=NO
   static public Vector ExprToString (final Vector e_ul) throws CGException {
 
     Vector argexpr_ul = new Vector();
     {
 
       IOmlExpression e = null;
       for (Iterator enm_14 = e_ul.iterator(); enm_14.hasNext(); ) {
 
         IOmlExpression elem_3 = (IOmlExpression) enm_14.next();
         e = (IOmlExpression) elem_3;
         {
 
           Oml2VppVisitor ppvisitor = (Oml2VppVisitor) new Oml2VppVisitor();
           {
 
             ppvisitor.visitExpression((IOmlExpression) e);
             String e_11 = null;
             e_11 = ppvisitor.result;
             argexpr_ul.add(e_11);
           }
         }
       }
     }
     return argexpr_ul;
   }
 // ***** VDMTOOLS END Name=ExprToString#1|Vector
 
 
 // ***** VDMTOOLS START Name=GetTraceDefinitionClasses#1|IOmlSpecifications KEEP=NO
   static public HashSet GetTraceDefinitionClasses (final IOmlSpecifications spec) throws CGException {
 
     HashSet classes = new HashSet();
     HashSet res_s_4 = new HashSet();
     {
 
       Vector e_set_19 = null;
       e_set_19 = spec.getClassList();
       IOmlClass cl = null;
       {
         for (Iterator enm_21 = e_set_19.iterator(); enm_21.hasNext(); ) {
 
           IOmlClass elem_20 = (IOmlClass) enm_21.next();
           cl = (IOmlClass) elem_20;
           Boolean pred_6 = null;
           Long var1_7 = null;
           HashSet unArg_8 = new HashSet();
           HashSet res_s_9 = new HashSet();
           {
 
             Vector e_set_14 = null;
             e_set_14 = cl.getClassBody();
             IOmlDefinitionBlock dfs = null;
             {
               for (Iterator enm_16 = e_set_14.iterator(); enm_16.hasNext(); ) {
 
                 IOmlDefinitionBlock elem_15 = (IOmlDefinitionBlock) enm_16.next();
                 dfs = (IOmlDefinitionBlock) elem_15;
                 if (new Boolean(dfs instanceof IOmlTraceDefinitions).booleanValue()) {
                   res_s_9.add(dfs);
                 }
               }
             }
           }
           unArg_8 = res_s_9;
           var1_7 = new Long(unArg_8.size());
           pred_6 = new Boolean((var1_7.intValue()) > (new Long(0).intValue()));
           if (pred_6.booleanValue()) {
 
             String res_s_5 = null;
             res_s_5 = cl.getIdentifier();
             res_s_4.add(res_s_5);
           }
         }
       }
     }
     classes = res_s_4;
     return classes;
   }
 // ***** VDMTOOLS END Name=GetTraceDefinitionClasses#1|IOmlSpecifications
 
 }
 ;
