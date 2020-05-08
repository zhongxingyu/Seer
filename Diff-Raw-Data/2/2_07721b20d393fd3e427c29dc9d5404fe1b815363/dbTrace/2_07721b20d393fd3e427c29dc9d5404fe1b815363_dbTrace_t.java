 package Escada.tpc.common.trace;
 
 import com.renesys.raceway.DML.*;
 
 import Escada.interfaces.*;
 import Escada.xest.*;
 import Escada.tpc.common.*;
 import Escada.ddb.kernel.*;
 import Escada.Util.*;
 
 
 import java.util.*;
 import java.io.*;
 
 /**
  * It defines a set of objects used to store information about a transaction. Specifically, it stores
  * the read and write sets and establishes a distinction between local and remote operations. It identifies the tables
  * used also establishing a distinction between local and remote operations, read and write access. Finally, 
  * it computes the size of the read and write operations according to the number of items accessed 
  * and the size of the tuples.
 **/
 class BagTransaction
 {
 	Transaction trans = null;
   	TreeSet masterWS = new TreeSet(); 
    	TreeSet masterRS = new TreeSet();
 
   	TreeSet slaveWS = new TreeSet(); 
    	TreeSet slaveRS = new TreeSet();
 
 	HashSet tableSlaveWS = new HashSet();
 	HashSet tableSlaveRS = new HashSet();
 	HashSet tableMasterWS = new HashSet();
 	HashSet tableMasterRS = new HashSet();
 
 	int masterws = 0;
 	int masterrs = 0;
  
 	int slavews = 0;
 	int slavers = 0;
 
 	int maxLength = 0;
 }
 
 /**
  * Basically, this class is responsible to capture the execution of transactions and translate it to 
  * read and written items. From the perspective of the data, it is the main point in order to integrate 
  * the benchmarks into a simulated environment.
  **/
 public class dbTrace {
   private static Hashtable outPutBag = new Hashtable();
   private static long tid = 0;
   private static DMLDBInfo dmlinfo = DMLDB.dmldb;
 
 
   /**
   * It registers for each operation the relations manipulated, the read and write sets.
   *
   * @param HashSet a set with the accessed items.
   * @param String the type of access which means read or write.
   * @param String the transaction identification, that is, the unique identifier.
   * @param String table 
   **/  
   public static void TransactionTrace(HashSet v, String type,
       String tid, String table, int index,String hid) {
 	try {
 		BagTransaction bagtrans = (BagTransaction) outPutBag.get(tid);
 		Transaction tran = bagtrans.trans;
 		String name = tran.header().name();
 
 		String tableId = getTableIdentification(table, index); 
 		long offset = dmlinfo.tablename_offset(tableId); 
 
 		if (tran != null) {
 
 			TreeSet items = null; 
       	
 			if(type.equalsIgnoreCase("r")) {
 				if (DistributedDb.isPossibleExecution(offset,hid))	{
 					items = bagtrans.masterRS;
 					bagtrans.masterrs = bagtrans.masterrs + (int)(v.size() * dmlinfo.tuplesize(offset));
 					bagtrans.tableMasterRS.add(new Long(offset));
 				}
 				else {
 					items = bagtrans.slaveRS;
 					bagtrans.slavers = bagtrans.slavers + (int)(v.size() * dmlinfo.tuplesize(offset));
 					bagtrans.tableSlaveRS.add(new Long(offset));
 				}
 			}		
 			else {
 				if (DistributedDb.isPossibleExecution(offset,hid))	{
 					items = bagtrans.masterWS;
 					bagtrans.masterws = bagtrans.masterws + (int)(v.size() * dmlinfo.tuplesize(offset));
 					bagtrans.tableMasterWS.add(new Long(offset));
 				}
 				else	{
 					items = bagtrans.slaveWS;
 					bagtrans.slavews = bagtrans.slavews + (int)(v.size() * dmlinfo.tuplesize(offset));
 					bagtrans.tableSlaveWS.add(new Long(offset));
 				}
 			}
 	      		Iterator it = v.iterator();
       			int i=0;
       			while(it.hasNext()) {
       				long l = Long.parseLong((String)it.next());
 	      			items.add(new Long(l + offset));
     				i++;
     			}
 			if (bagtrans.maxLength < items.size()) bagtrans.maxLength = items.size();
 			
 		}
 	}
     	catch (Exception ex) {
       		ex.printStackTrace(System.err);
     	}
   }
 
   /**
   * It initializes the structures that stores the transaction information. First of all, it creates
   * an internal object "transaction" which is used by simulator. This object is composed by
   * a Header and a resource usage that is populated during the transaction execution. It also associates
   * a unique identifier to the transaction.
   *
   * @param String the transaction name which is used to compose the header.
   * @param String the amount of time to wait before submitting this transaction.
   *
   * @return String the transaction unique indentifier.
   *
   * @see closeTransactionTrance, transactionTrace.
   **/
   public static String initTransactionTrace(String transaction, String thinkTime) {
 
 	// Configuration dml = Simulation.config(); // porra
 	
   	Header hd = new Header(transaction, (int)tid, "10"); // porra
   	ResourceUsage payload = new ResourceUsage(null, null, null, null);  	
     	Transaction tran = new Transaction(hd, payload,Long.parseLong(thinkTime));
 	BagTransaction bagtrans = new BagTransaction();
 	bagtrans.trans = tran;
   
 	synchronized  (outPutBag)
 	{
 		tid++;
 		tran.header().tid((int)tid);
 		outPutBag.put(Long.toString(tid),bagtrans);
 	}
 
 	return (Long.toString(tid));
   }
 
   /**
    * It saves the object transaction in a file which will be probably used off-line and also returns it.
    *
    * @param tid the transaction unique identifier.
    * @param file the file name.
    * @param hid the host id in which the transaction was processsed.
    *
    * @return Transaction the transaction stored in the file.
    **/
   public static Transaction closeTransactionTrace(String tid, String file,String hid) {
     Transaction closeTransactionTrace = null;
     
     try {
       ObjectOutputStream getFile = (ObjectOutputStream) getFile(file);
       closeTransactionTrace = closeTransactionTrace(tid,hid);
 
       if (closeTransactionTrace != null) {
         synchronized (outPutBag)
         {
            getFile.writeObject(closeTransactionTrace);
            getFile.flush();
            outPutBag.remove(tid);
         }
       }
     }
     catch (Exception ex) {
       ex.printStackTrace(System.err);
     }
     
     return (closeTransactionTrace);
     
   }
 
   /**
    * It returns the transaction that was captured during its execution and before returning it populates 
    * its resource usage defined during the creation with the information stored in the BagTransaction.
    * The resource usage extends a stack and for that reason the order in which the requests for resource
    * usage are inserted is extremelly important. For the current version of the simulation the order
    * must be as follows:
    *
    * 1 - push unlock request
    * 2 - push write request
    * 3 - push certification request
    * 4 - push cpu request
    * 5 - push read request
    * 6 - push thinktime request
    * 7 - push lock request
    * 8 - push distributed request
    *
    * @param tid the transaction unique identifier.
    * @param hid the host id in which the transaction was processsed.
    *
    * @return Transaction the transaction executed and translated according the simulator specifications.
    **/
   public static Transaction closeTransactionTrace(String tid,String hid) {
 	Transaction closeTransactionTrace = null;
 	try {
 		BagTransaction bagtrans = (BagTransaction) outPutBag.get(tid);
 		closeTransactionTrace = bagtrans.trans;
 		Iterator it = null;
 		int i = 0, j = 0; 
 		long lastTable = -1;
 
 		long[] masterWS = null;
 		long[] masterRS = null;
 		long[] slaveWS = null;
 		long[] slaveRS = null;
 		int[] tableMasterWS = null;
 		int[] tableMasterRS = null;
 		int[] tableSlaveWS = null;
 		int[] tableSlaveRS = null;
 
 		if (closeTransactionTrace != null) {
 			outPutBag.remove(tid);
 
 			if (bagtrans.masterWS.size() != 0)
 			{
 				masterWS = new long[bagtrans.masterWS.size()];
				tableMasterWS = new int[bagtrans.tableMasterWS.size()];		
 
 				it = bagtrans.masterWS.iterator();
 				i = 0; j = 0; lastTable = -1;
 				
 				while(it.hasNext()) {
 				      	masterWS[i] = ((Long)it.next()).longValue();
 
 					if (lastTable != dmlinfo.table_of(masterWS[i])) { 
 						tableMasterWS[j] = i;
 						lastTable = dmlinfo.table_of(masterWS[i]);
 						j++;
 					}
 					
                 		        i++;
 			
 				}
 			}
 			if (bagtrans.masterRS.size() != 0)
 			{
 				masterRS = new long[bagtrans.masterRS.size()];
 				tableMasterRS = new int[bagtrans.tableMasterRS.size()];
 	
 				it = bagtrans.masterRS.iterator();
 				i = 0; j = 0; lastTable = -1;
 
 				while(it.hasNext()) {
 				      	masterRS[i] = ((Long)it.next()).longValue();
 
                                         if (lastTable != dmlinfo.table_of(masterRS[i])) {
                                                 tableMasterRS[j] = i;
                                                 lastTable = dmlinfo.table_of(masterRS[i]);
                                                 j++;
                                         }
 
                 		        i++;
 				}
 			}
 			if (bagtrans.slaveRS.size() != 0)
 			{
 				slaveRS = new long[bagtrans.slaveRS.size()];
 				tableSlaveRS = new int[bagtrans.tableSlaveRS.size()];	
 	
 				it = bagtrans.slaveRS.iterator();
 				i = 0; j = 0; lastTable = -1;
 
 				while(it.hasNext()) {
 				      	slaveRS[i] = ((Long)it.next()).longValue();
 
                                         if (lastTable != dmlinfo.table_of(slaveRS[i])) {
                                                 tableSlaveRS[j] = i;
                                                 lastTable = dmlinfo.table_of(slaveRS[i]);
                                                 j++;
                                         }
 
                 		        i++;
 				}
 			}
 			if (bagtrans.slaveWS.size() != 0)
 			{
 				slaveWS = new long[bagtrans.slaveWS.size()];
 				tableSlaveWS = new int[bagtrans.tableSlaveWS.size()];
 
 				it = bagtrans.slaveWS.iterator();
 				i = 0; j = 0; lastTable = -1;
 
 				while(it.hasNext()) {
 				      	slaveWS[i] = ((Long)it.next()).longValue();
 
                                         if (lastTable != dmlinfo.table_of(slaveWS[i])) {
                                                 tableSlaveWS[j] = i;
                                                 lastTable = dmlinfo.table_of(slaveWS[i]);
                                                 j++;
                                         }
 
                 		        i++;
 				}
 			}
 
 			closeTransactionTrace.payload().WS(masterWS);
 			closeTransactionTrace.payload().RS(masterRS);
 			closeTransactionTrace.payload().indexOfWrittenTables(tableMasterWS);
 			closeTransactionTrace.payload().indexOfReadTables(tableMasterRS);
 
 			Simulation em = Simulation.self();
 			Tuple transModel = null;
 			Tuple []tmpModel = null;
 
 			if ((masterWS == null) && (slaveWS == null)) {
 				tmpModel = em.getTemplate(hid,true); 
 			}
 			else {
 				tmpModel = em.getTemplate(hid,false); 
 			}
 
 			long qttUsage = TransactionTimers.calculateQueryThinkTime(closeTransactionTrace.header().name()); 
 			long cpuUsage = TransactionTimers.calculateCPUTime(closeTransactionTrace.header().name()); 
 			Request req = null;
 			String info = null;
 
 			for (i = tmpModel.length - 1; i >= 0; i--) { 
                                 transModel = tmpModel[i];
 
 				if (((String)transModel.get(0)).equalsIgnoreCase("DBSMAdapter")) {
 					req = new NetRequest (Integer.valueOf(tid),(String)transModel.get(1),closeTransactionTrace);  // DBSM
 				
 				}
 				else if (((String)transModel.get(0)).equalsIgnoreCase("Storage")) {
 				 	info = (String)transModel.get(2);
 	
 					if (info.equalsIgnoreCase("R")) {
 						req = new StorageRequest(Integer.valueOf(tid),(String)transModel.get(1),bagtrans.masterrs,true); 
 					}
 					else {
 						if ((masterWS == null) && (slaveWS == null)) {
 							req = new StorageRequest(Integer.valueOf(tid),(String)transModel.get(1),bagtrans.masterws,false);
 						}
 					}
 				}
 				else if (((String)transModel.get(0)).equalsIgnoreCase("CPU")) {
 				
 					req = new ProcessRequest(Integer.valueOf(tid),(String)transModel.get(1),cpuUsage,false); // CPU
 				}
 				else if (((String)transModel.get(0)).equalsIgnoreCase("Thinker")) {
 					req = new ProcessRequest(Integer.valueOf(tid),(String)transModel.get(1),qttUsage,false); // QTT
 				
 				}
 				else if (((String)transModel.get(0)).equalsIgnoreCase("DDbProxyProcess")) {
 					req = new NetRequest (Integer.valueOf(tid),(String)transModel.get(1),closeTransactionTrace);  // DDB
 				
 				}
 				else if (((String)transModel.get(0)).equalsIgnoreCase("LockManager")) {
 				 	info = (String)transModel.get(2);
 					if (info.equalsIgnoreCase("L"))
 						req = new LockRequest(Integer.valueOf(tid),transModel.get(1),LockRequest.ORDER_LOCK,masterRS,masterWS,false); 
 					else
 						req = new LockRequest(Integer.valueOf(tid),transModel.get(1),LockRequest.ORDER_UNLOCK,masterRS,masterWS,false); 
 						
 				}
 				closeTransactionTrace.payload().push(req);
 			}
 		}
 	}
 	catch (Exception ex) {
 		ex.printStackTrace(System.err);
 	}
     
 	return (closeTransactionTrace);
 }
 
   public static Transaction closeErrorTransactionTrace(String tid) {
     Transaction closeTransactionTrace = null;
     try {
       BagTransaction bagtrans = (BagTransaction) outPutBag.get(tid);
       closeTransactionTrace = bagtrans.trans;
 
       if (closeTransactionTrace != null) {
         outPutBag.remove(tid);
       }
     }
     catch (Exception ex) {
       ex.printStackTrace(System.err);
     }
    
     if (closeTransactionTrace != null) closeTransactionTrace.setInducedAbort(); 
     return (closeTransactionTrace);
   }
 
   public static synchronized void generateOtherInformation(String transaction) {
     try {
       Runtime r = Runtime.getRuntime();
       Process proc = r.exec("./scriptlog.sh " + transaction);
 
       InputStreamReader reader = new InputStreamReader(proc.getInputStream());
 
       while (reader.read() != -1) {
       }
 
       proc.waitFor();
 
       proc.exitValue();
     }
     catch (Exception ex) {
       ex.printStackTrace(System.err);
     }
   }
 
 
   public static ObjectOutputStream getFile(String file) {
     ObjectOutputStream getFile = (ObjectOutputStream) outPutBag.get(file);
     try {
       if (getFile == null) {
         getFile = new ObjectOutputStream(new FileOutputStream(file));
         if (getFile == null) {
           throw new Exception("Problem opening archive.");
         }
       }
       outPutBag.put(file, getFile);
     }
     catch (Exception ex) {
       ex.printStackTrace(System.err);
     }
 
     return (getFile);
 
   }
 
   public static PrintStream getStringFile(String baseDirectory,
       String file) {
     PrintStream getStringFile = (PrintStream) outPutBag.get(file.toLowerCase());
     try {
       if (getStringFile == null) {
         getStringFile = new PrintStream(new FileOutputStream(baseDirectory +
             "/" + file));
         if (getStringFile == null) {
           throw new Exception("Problem opening archive.");
         }
       }
       outPutBag.put(file.toLowerCase(), getStringFile);
     }
     catch (Exception ex) {
       ex.printStackTrace(System.err);
     }
     
     return (getStringFile);
     
   }
 
   public static String getTableIdentification(String table, int index) {
     int frag = 1;
     String tablename = null;
     
     frag = index - 1;
         
     if ((dmlinfo.isFragmentedDatabase()) && (!dmlinfo.isGlobalTable(table))) {
         tablename = table + frag;
     }
     else
     {
     	tablename = table;
     }
 
     return (tablename); 
   }
 
   public static void compileTransactionTrace(String trans,
       String idStringTrace) {
   }
 
   public static void compileTransactionTrace(String trans,
       OutInfo obj) {
     String stm = null;
     String param = null;
     String value = null;
     int loop = 0;
     String stmbkp = null;
     String parambkp = null;
     int valuebkp = 0;
     boolean noOutput = false;
 
 
     PrintStream pts = getStringFile("trace", (String) obj.getInfo("file"));
     pts.println("Thinktime: " + (String) obj.getInfo("thinktime"));
     pts.println("Transaction: " + trans);
 
     try {
       BufferedReader dread
           = new BufferedReader(new InputStreamReader(new FileInputStream(
           "cache" + "/" + trans)));
 
       while ( (stm = dread.readLine()) != null) {
         stm = replaceString(stm, "%master%", "");
         stm = replaceString(stm, "%slave%", "");
         stm = replaceString(stm, "->", "");
 
         param = getStringParam(stm);
         if (param != null) {
           if (param.equalsIgnoreCase("repeat")) {
             stm = replaceStringParam(stm, param, "");
             param = getStringParam(stm);
             value = (String) obj.getInfo(param);
             stm = replaceStringParam(stm, param, "");
           }
           else {
             value = "1";
           }
 
           loop = 0;
           stmbkp = stm;
           parambkp = null;
           noOutput = false;
           valuebkp = Integer.parseInt(value);
           while (loop < valuebkp) {
             stm = stmbkp;
             while (stm.indexOf("%") != -1) {
               param = getStringParam(stm);
               if (param.indexOf("repeat") != -1) {
                 parambkp = param;
                 param = param.substring(0, param.indexOf("repeat"));
                 param = param.concat(Integer.toString(loop));
                 value = (String) obj.getInfo(param);
                 stm = replaceStringParam(stm, parambkp, value);
               }
               else if (param.indexOf("inc") != -1) {
                 value = Integer.toString(loop + 1);
                 stm = replaceStringParam(stm, param, value);
               }
               else if (param.equalsIgnoreCase("if")) {
                 stm = replaceOneStringParam(stm, param, "");
                 param = getStringParam(stm);
                 stm = replaceOneStringParam(stm, param, "");
                 String rstr = param.substring(0, param.indexOf("="));
                 String lstr = param.substring(param.indexOf("=") + 1);
                 value = (String) obj.getInfo(rstr);
                 if (value != null) {
                   if (!value.equalsIgnoreCase(lstr)) {
                     noOutput = true;
                     break;
                   }
                 }
                 else {
                   noOutput = true;
                   break;
                 }
               }
               else if (param.equalsIgnoreCase("like"))
               {
                 stm = replaceOneStringParam(stm, param, "");
                 String rstr = getStringParam(stm);
                 stm = replaceOneStringParam(stm, rstr, "");
                 String lstr = getStringParam(stm);
 
                 value = (String) obj.getInfo(lstr.substring(0,lstr.length() - 1));
                 String like = rstr + " like '" + value + "*'";
 
                 like = parseLike(like);
                 stm = replaceOneStringParam(stm,lstr,like);
               }
               else {
                 value = (String) obj.getInfo(param);
                 stm = replaceStringParam(stm, param, value);
               }
             }
             if (!noOutput) {
               stm = stm.replace('', '%');
               pts.println(stm);
             }
             loop++;
           }
         }
         else {
           if (stm.indexOf("committran") != -1) {
             value = (String) obj.getInfo("abort");
             if (value.equalsIgnoreCase("0")) {
               pts.println(stm);
             }
             else {
               pts.println("aborttran");
             }
           }
           else {
             pts.println(stm);
           }
         }
       }
     }
     catch (FileNotFoundException ex) {
 
     }
     catch (IOException ex) {
 
     }
     catch (Exception ex) {
       ex.printStackTrace(System.err);
       System.err.println("Statement " + stm + " param " + param + " value " +
                          value);
     }
 
     pts.flush();
 
     obj.resetInfo();
   }
 
   private static String PERCENT_ASTERISK = "*"; 
   private static String parseLike(String pLike){
    String lReturn = "";
 
    String splitLike[] = pLike.split("like");
    String lField = splitLike[0];
    String splitQuotations[] = pLike.split("'");
 
    if(!splitQuotations[1].endsWith(PERCENT_ASTERISK) && !splitQuotations[1].startsWith(PERCENT_ASTERISK)){
       return lField + " = '" + splitQuotations[1].substring(0,splitQuotations[1].length());
    }
 
    if(splitQuotations[1].startsWith(PERCENT_ASTERISK)){
        splitQuotations[1] = splitQuotations[1].substring(1,splitQuotations[1].length());
        lReturn = " %not implemented";
    }
 
    if(splitQuotations[1].endsWith(PERCENT_ASTERISK)){
       lReturn += lField + " >= '"
                + splitQuotations[1].substring(0,splitQuotations[1].length()-1)
                + "' and "
                + lField
                + " < '"
                + splitQuotations[1].substring(0,splitQuotations[1].length()-2)
                + (char)((int)splitQuotations[1].substring(splitQuotations[1].length()-2,splitQuotations[1].length()-1).charAt(0)+1)
                + "'";
    }
 
    return lReturn;
 }
 
 
   private static String getStringParam(String stm) {
     if (stm == null) {
       return (null);
     }
 
     int posini = stm.indexOf("%");
     int posend = stm.indexOf("%", posini + 1);
     if ( (posini == -1) || (posend == -1)) {
       return (null);
     }
     else {
       return (stm.substring(posini + 1, posend));
     }
   }
 
   private static String replaceOneStringParam(String stm, String param,
                                               String value) {
     int posini = -1, posend = -1;
 
     if ( (stm == null) || (param == null)) {
       return (stm);
     }
     else if (value == null) {
       value = "0";
     }
 
     param = "%" + param + "%";
     posini = stm.indexOf(param);
     posend = param.length() + posini;
     stm = stm.substring(0, posini) + value.replace('%', '') +
         stm.substring(posend);
 
     return (stm);
   }
 
   private static String replaceStringParam(String stm, String param,
                                            String value) {
     if ( (stm == null) || (param == null)) {
       return (stm);
     }
     else if (value == null) {
       value = "0";
     }
 
     return (replaceString(stm, "%" + param + "%", value));
   }
 
   private static String replaceString(String stm, String param,
                                                   String value) {
     int posini = -1, posend = -1;
 
     if (param == null || stm == null || value == null) {
       return (stm);
     }
 
     while (stm.indexOf(param) != -1) {
       posini = stm.indexOf(param);
       posend = param.length() + posini;
       stm = stm.substring(0, posini) + value.replace('%', '') +
           stm.substring(posend);
     }
     return (stm);
   }
 }
 // arch-tag: cd27b7fe-ae93-483e-af78-79491b558ac0
 // arch-tag: e932c514-e0b2-4b45-9a47-902984767993
