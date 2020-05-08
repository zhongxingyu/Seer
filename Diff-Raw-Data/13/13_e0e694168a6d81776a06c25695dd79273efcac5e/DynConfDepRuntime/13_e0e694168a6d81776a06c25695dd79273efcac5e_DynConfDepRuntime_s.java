 package chord.analyses.confdep;
 
 import chord.analyses.logging.RelLogStmts;
 import chord.util.WeakIdentityHashMap;
 import java.io.*;
 import java.util.*;
 
 public class DynConfDepRuntime {
   //extends DynamicAnalysis
 
   static PrintStream out;
   protected static WeakIdentityHashMap labels;
   static {
     try {
 
       out = new PrintStream(new FileOutputStream(DynConfDep.results));
       labels = new WeakIdentityHashMap();
     } catch(IOException e) {
       e.printStackTrace();
     }
     out.println("runtime event handler alive "  + new Date());
     out.flush();
   }
   
   public static String reformatArray(Object s) {
   	Class aType = s.getClass().getComponentType();
   	if(!aType.isPrimitive()) {
   		Object[] arr = (Object[]) s;
   		StringBuilder sb = new StringBuilder();
   		for(Object a : arr) {
   			sb.append(a.toString());
   			sb.append(",");
   		}
   		if(sb.length() > 0)
   			sb.deleteCharAt(sb.length() -1);
   		return sb.toString();
   	} else {
   		return "PRIM-array";
   	}
   }
   
   public synchronized static void methodCallAftEventSuper(int iIdx, String cname, String mname, Object ret,
       Object tref, Object[] args) {
 
     //int cOpt = ConfDefines.confOptionPos(cname, mname);
     boolean isConf = ConfDefines.isConf(cname, mname);
     int cOpt = 0;
     for(; cOpt < args.length; ++cOpt)
       if(args[cOpt] instanceof String)
         break;
     
     if(isConf && cOpt < args.length) {
 //      if(tref != null)
 //        cOpt --;
       /*
       if(args.length <= cOpt) {
         out.println("ERR: expected at least " + (cOpt+1) +  " options for call to " + cname + " " + mname);
         return;
       } */
       String confOpt = ConfDefines.optionPrefixByName(cname, mname) + (String) args[cOpt] +"-" +iIdx;
       if(ret != null) {
       	if(ret.getClass().isArray())
      		ret = reformatArray(ret);
        out.println(iIdx +" calling " + cname + " " + mname + " returns option " + confOpt + " value=" + ret);
       }
       else
         out.println(iIdx +" calling " + cname + " " + mname + " returns option " + confOpt + " value=null");
       
      addLabel(ret,  confOpt);
     } else {
       boolean taintedCall = false;
       HashSet<String> rtaints = new HashSet<String>();
       rtaints.addAll(taintlist(tref)); // start by adding all taints from this
 
       if(RelLogStmts.isLogStmt(cname, mname)) {
         for(int i= 0; i < args.length; ++i)
           if(args[i] != null && args[i] instanceof String)
             out.println("LOG " + mname +" " + args[i]);
       }
       
       String thisT = taintStr(tref);
       if(thisT.length() > 0) {
         out.println(iIdx + " invoking " + cname + " " + mname + " 0=" + thisT);
         taintedCall = true;
       }
       
       for(int i= 0; i < args.length; ++i) {  //then all taints from other args
         rtaints.addAll(taintlist(args[i]));
         String tStr = taintStr(args[i]);
         if(tStr.length() > 0) {
           if(tref == null)
             out.println(iIdx + " invoking " + cname + " " + mname + " " + (i) + "=" + tStr);
           else
             out.println(iIdx + " invoking " + cname + " " + mname + " " + (i+1) + "=" + tStr);
           taintedCall = true;
         }
       }
       
       if(!taintedCall) {
         out.println("call to "+ cname + " " + mname  + " without taint at " + iIdx);
       }
       
       ArrayList<String> newTList = new ArrayList<String>();
       newTList.addAll(rtaints);
       if(RelAPIMethod.isAPI(cname, mname)) {
         if(ret == null) {
           out.println(iIdx + " returns null");
         } else
           setTaints(ret, newTList); //mark return value
         setTaints(tref, newTList);   //and add taints to this
       } 
       
       /*
       if(newTList.size() > 0) {
         out.println("call " + iIdx+ " to " + cname + "  " + mname);
         for(int i= 0; i < args.length; ++i) {
           out.println("\targ"+i + " taintlist is "+ taintStr(args[i]));
         }
       }*/
     }
     out.flush();
   }
   
 
   public synchronized static void astoreReferenceEvent(int eId,Object array, int iId, Object parm) {
     //this message is just for debugging
 //    out.println("store to array with taints: " + taintStr(parm));
     List<String> taintlist = taintlist(parm);
     for(String t: taintlist) {
       addLabel(array, t);
     }
   }
   
   public synchronized static void aloadReferenceEvent(int eId,Object array, int iId, Object result) {
     //message is just for debugging
 //    out.println("load from array with taints: " + taintStr(array));
     List<String> taintlist = taintlist(array);
     for(String t: taintlist) {
       addLabel(result, t);
     }
   }
      
   private static void setTaints(Object o, ArrayList<String> newTList) {
     if(o != null)
       labels.put(o, newTList);    
   }
 
   public static List<String> taintlist(Object o) {
     if(o == null)
       return Collections.EMPTY_LIST;
     Object l = labels.get(o);
     if(l != null) {
       List<String> l2 = (List<String>) l;
       return l2;
     } else {
       return Collections.EMPTY_LIST;
     }
   }
 
 
   public static String taintStr(Object o) {
     if(o == null)
       return "";
     Object l = labels.get(o);
     if(l != null) {
       List<String> l2 = (List<String>) l;
       StringBuilder sb = new StringBuilder();
       for(String s: l2) {
         sb.append(s);
         sb.append("|");
       }
       if(sb.length() > 0)
         sb.deleteCharAt(sb.length() -1);
       return sb.toString();
     } else {
       return "";
     }
   }
   
   public static void addLabel(Object o, String label) {
     if(o == null)
       return;
     
     Object l = labels.get(o);
     if(l != null) {
       ((List<String>)l).add(label);
     } else {
       ArrayList<String> l2 = new ArrayList<String>();
       l2.add(label);
       labels.put(o, l2);
     }
   }
   
   public static void returnReferenceEvent(int pId, Object o) {
     out.println("returning " + o + " at " + pId);
   }
 
 }
