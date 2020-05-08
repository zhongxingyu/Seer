 package org.cnio.appform.util.dump;
 
 import org.cnio.appform.entity.*;
 
 import org.cnio.appform.util.HibernateUtil;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 
 import java.util.Properties;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Calendar;
 
 import java.util.regex.*;
 
 import java.io.*;
 
 import org.cnio.appform.util.dump.*;
 
 public class MainDump {
 
 // arguments names for command line	
 	public final static String INTRV_ARG = "-i";
 	public final static String PRJ_ARG = "-p";
 	public final static String GRP_ARG = "-g";
 	public final static String SEC_ARG = "-s";
 	public final static String FILE_ARG = "-f";
 	public final static String ORDER_ARG = "-o";
 	public final static String VARS_ARG = "-h";
 	public final static String QUES_ARG = "-q";
 	public final static String QRY_ARG = "-qry";
 	public final static String BATCH_ARG = "-batch";
 	
 // prefix of arguments for the batch file
 	public final static String PRJ_PREFIX = "prj";
 	public final static String INTRV_PREFIX = "intrv";
 	public final static String SEC_PREFIX = "sec";
 	public final static String GRP_PREFIX = "grp";
 	public final static String QUES_PREFIX = "ques"; // for quetion codes
 	public final static String QTYPE_PREFIX = "qrytype";
 	public final static String FILE_PREFIX = "filename";
 	public final static String VARS_PREFIX = "vars";
 	
 	public final static String DEFAULT_FILEOUT_EXT = ".csv";
 	
 	private final static String PROPS_FILE = "bulks.props";
 	public final static int MAX_QUESTIONS = 64;
 	
 	private Session hibSess;
 	
 	private List<ParametersModel> bulksList;
 	
 	public MainDump () {
 		SessionFactory sf = HibernateUtil.getSessionFactory();
 		hibSess = sf.openSession();
 		
 		bulksList = new ArrayList<ParametersModel>();
 	}
 	
 	
 /**
  * Inner class to model the different parameters found in a properties file to
  * develop a batch of bulk downloads
  * 
  */	
 	public class ParametersModel {
 		
 // project, interview, groups, section
 		private String prj = "", intrv = "", grp = "", sec = "", order = "";
 		private int pos;
 		
 		private String qryType = DBDump.QRY_NORMAL, // query type
 				qCodes = "", // question codes if any 
 				filename = "", 
 				varsFile = "";
 
 // this is a value to index the array of question codes
 		private int qCodesSize = 0;
 		
 		public String getPrj() {
 			return prj;
 		}
 
 		public void setPrj(String prj) {
 			this.prj = prj;
 		}
 
 		public String getIntrv() {
 			return intrv;
 		}
 
 		public void setIntrv(String intrv) {
 			this.intrv = intrv;
 		}
 
 		public String getGrp() {
 			return grp;
 		}
 
 		public void setGrp(String grp) {
 			this.grp = grp;
 		}
 
 		public String getSec() {
 			return sec;
 		}
 
 		public void setSec(String sec) {
 			this.sec = sec;
 		}
 
 		ParametersModel(int i) {
 			this.pos = i;
 		}
 
 		public String getQType() {
 			return qryType;
 		}
 
 		public void setQType(String type) {
 			qryType = type;
 		}
 		
 		
 		public void insertVal (String key, String val) {
 // System.out.println ("ParametersModel.insertVal: key="+key+" :: val="+val);
 			if (key.equalsIgnoreCase(MainDump.PRJ_PREFIX))
 				prj = val;
 			
 			else if (key.equalsIgnoreCase(MainDump.INTRV_PREFIX))
 				intrv = val;
 			
 			else if (key.equalsIgnoreCase(MainDump.SEC_PREFIX))
 				sec = val;
 			
 			else if (key.equalsIgnoreCase(MainDump.GRP_PREFIX))
 				grp = val;
 			
 			else if (key.equalsIgnoreCase(MainDump.QUES_PREFIX)) {
 				qCodes = val;
 			}
 			else if (key.equalsIgnoreCase(MainDump.QTYPE_PREFIX))
 				qryType = val;
 			
 			else if (key.equalsIgnoreCase(MainDump.FILE_PREFIX))
 				filename = val;
 			
 			else if (key.equalsIgnoreCase(MainDump.VARS_PREFIX))
 				varsFile = val;
 		}
 		
 		
 		
 		public String toString () {
 			String str = "prj: "+prj+"; intrv: "+intrv+"; sec: "+sec;
 			str += "; grp: "+grp+"; qCodes: "+qCodes+"; qryType: "+qryType;
 			str += "; pos: "+pos+"; varsfile: "+varsFile+" & fileout: "+filename;
 			
 			return str;
 		}
 		
 		
 		
 		
 		public boolean equals (ParametersModel pm) {
 			return (pos == pm.getPos());
 				
 		}
 
 		public int getPos() {
 			return pos;
 		}
 
 		public void setPos(int pos) {
 			this.pos = pos;
 		}
 
 		public String getQCode() {
 			return qCodes;
 		}
 
 		public void setQCode(String code) {
 			qCodes = code;
 		}
 
 		public String getFilename() {
 			return filename;
 		}
 
 		public void setFilename(String filename) {
 			this.filename = filename;
 		}
 
 		public String getVarsFile() {
 			return varsFile;
 		}
 
 		public void setVarsFile(String varsFile) {
 			this.varsFile = varsFile;
 		}
 
 		public String getQryType() {
 			return qryType;
 		}
 
 		public void setQryType(String qryType) {
 			this.qryType = qryType;
 		}
 	}
 /****************************************************************************/
 	
 	
 	
 	public Session getHibSes () {
 		return hibSess;
 	}
 	
 	
 	public Session getCurrentHibSes () {
 		hibSess = HibernateUtil.getSessionFactory().getCurrentSession();
 		
 		return hibSess;
 	}
 	
 	
 	public Session getCurrentSession () {
 		return HibernateUtil.getSessionFactory().getCurrentSession();
 	}
 	
 	
 	public Session openSession () {
 		hibSess = HibernateUtil.getSessionFactory().openSession();
 		
 		return hibSess;
 	}
 	
 	
 	public void closeHibSes () {
 		hibSess.close();
 	}
 	
 	
 	
 /**
  * As, sometimes, the keys in the properties files have weird chars and the 
  * exports gets wrong, it is necessary to "clean" the keys to remove this chars.
  * The side effect is the parser would accept things like "prj2prj5", but only
  * prj2 will be taken into consideration
  * @param key, the key trimmed and as read from properties file
  * @return the key clean bad chars
  */	
 	private String cleanKey (String key) {
 		String regExp = "("+MainDump.FILE_PREFIX+"|"+MainDump.GRP_PREFIX+"|"+
 			MainDump.INTRV_PREFIX+"|"+MainDump.PRJ_PREFIX+"|"+MainDump.QTYPE_PREFIX+
 			"|"+MainDump.QUES_PREFIX+"|"+MainDump.SEC_PREFIX+"|"+MainDump.VARS_PREFIX+
 			")\\d{1,3}";
 		
 		Pattern pattern = Pattern.compile(regExp);
 		Matcher matcher = pattern.matcher(key);
 		boolean matchFound = matcher.find();
 
 		if (matchFound) {
 	    // Get all groups for this match
 			return matcher.group(0);
 /*
 	    for (int i=0; i<=matcher.groupCount(); i++) {
 	      String groupStr = matcher.group(i);
 System.out.println (i+" -> group: "+groupStr+" for key: "+key);	      
 	    }
 */
 		}
 		
 		return null;
 	}
 	
 	
 
 	
 /**
  * Read the properties file with all queries and put the parameters into a model.
  * The structure of the keys in the properties file is (i is the number of the 
  * bulk to number the group of parameters):
  * - prji, the name of the project
  * - grpi, the group (optional)
  * - intrvi, the name of the interview
  * - seci, the section to retrieve de data
  * - qrytypei, 
  * @param propsName, the name of the properties file with the batch actions coded
  */
	private void batch (String propsName) throws IOException {
 		Properties props = new Properties ();
 		props.load(new FileInputStream (propsName));
 		
 		Enumeration keys = props.keys();
 		while (keys.hasMoreElements()) {
 			String key = ((String)keys.nextElement()).trim();
 			String val = ((String)props.get(key)).trim();
 			
 			key = cleanKey (key);
 // System.out.println ("val ("+key+"): "+val);
 			int pos = -1;
 			
 // Getting the block number
 			int numBlock = -1;
 			Pattern pattern = Pattern.compile("\\d+");
 			Matcher matcher = pattern.matcher(key);
 			int indexNum = -1;
 			if (matcher.find()) { 
 				indexNum = matcher.start();
 				String aux = key.substring(indexNum, key.length());
 				numBlock = Integer.parseInt(aux);
 			}
 			ParametersModel pm;
 			int pmIndex = -1;
 /*			
 			if (Character.isDigit(numBlock))
 				pos = Character.digit(numBlock, 10);
 // System.out.println (key+"="+val+": pos: "+pos);			
 			if (pos == -1)
 				continue;
 */			
 			pos = numBlock;
 			for (ParametersModel pModel: bulksList) {
 				if (pModel.getPos() == pos)
 					pmIndex = bulksList.indexOf(pModel);
 			}
 			
 			pm = (pmIndex == -1? new ParametersModel (pos): bulksList.get(pmIndex));
 //			pm.insertVal (key.substring(0, key.length()-1), val);
 			pm.insertVal(key.substring(0, indexNum), val);
 			if (pmIndex == -1)
 				bulksList.add(pm);
		}
 		
 System.out.println("Perform batch job...");
 		DBDump dumper = new DBDump ();
 		for (ParametersModel pm: bulksList) {
 // System.out.println (pm.toString());
 			String filename = pm.getFilename();
 			String fileparts[];
 			fileparts = pm.getFilename().split("\\.");
 // System.out.println ("filesparts.length: "+fileparts.length);			
 			if (fileparts.length < 2) {
 				String nameFile = fileparts[0];
 				fileparts = new String [2];
 				fileparts[0] = nameFile;
 				fileparts[1] = MainDump.DEFAULT_FILEOUT_EXT;
 			}	
 
 //			String fileExt = fileparts[fileparts.length-1];
 // build date
 			Calendar now = Calendar.getInstance();
 			int day = now.get(Calendar.DAY_OF_MONTH);
 			int month = now.get(Calendar.MONTH)+1;
 			int year = now.get(Calendar.YEAR);
 			
 			String fileDate = (day < 10? "0"+day: day)+ "" +
 												(month < 10? "0"+month: month) + year;
 			pm.setFilename(fileparts[0]+"-"+fileDate+"."+fileparts[1]);
 			
 			
 			dumper.setFileName(pm.getFilename());
 			dumper.setGrpName(pm.getGrp());
 			dumper.setIntrvName(pm.getIntrv());
 			dumper.setPrjName(pm.getPrj());
 			dumper.setVarsMap(pm.getVarsFile());
 			dumper.setQryType(pm.getQryType());
 			dumper.setCodQues(pm.qCodes);
 			
 			if (pm.getSec().length() != 0)
 				dumper.setOrderSec(Integer.decode(pm.getSec()));
 			
 //			dumper.spitInfo();
 			dumper.dump();
 System.out.println ("=================================================");
 		}
 			
 	} // EO batch method
 	
 	
 	
 /**
  * This is the entry point to the dump program, which is addressed to download
  * bulk data from the database based on some parameters.
  * It inits the DBDump class (after retrieving the parameters) to start the 
  * dump process
  * @param args the command line arguments
  */
 	public static void main (String[] args) {
 	
 		String lastArg = "";
 		String prj = "", intrv = "", grp = "", file = "", sec = "", order="";
 		String fileVars = "";
 		String questionCods = "", qryType = "";
 		
 		MainDump rep = new MainDump ();
 		
 		if (args.length == 0 || args[0].equalsIgnoreCase("-h")) {
 			String helpMsg = "Usage is:\njava -jar customp-dump.jar -p <project_name> " +
 					"-i <questionnaire>";
 //			helpMsg += " [-g hostpial/group name] -s <section_order> [-o sort_order]";
 			helpMsg += " [-g hospital/group name] -s <section_order>";
 			helpMsg += " -f <filename> -h <filename>\n";
 			helpMsg += "\nDatabase dumper:\n\nOptions are (order is irrelevant):\n" +
 					"\t-p <project name>\n" +
 					"\t-i <questionnaire name>\n" +
 					"\t-g <hospital/group name>\n" +
 					"\t-s <section order> in questionnaire\n" +
 					"\t-q <qCode1[,qCode2 [,...]]> comma-separated list of quesetion codes\n" +
 					"\t-qry <qryType> the type of query: one of totals, questotals, normal\n" +
 //					"\t-o <the clasification order>: 0, section order first (default);" +
 //					" 1, number of the answer\n"+
 					"\t-h <vars filename>: the pathname (absolute or relative) of the file with "+
 					"the variable names mapping\n"+
 					"\t-f <result filename> (absolute or relative)\n" +
 					"or\n" +
 					"\t-batch, will read a properties file and will develop a batch job for" +
 					" each bulk request in that file\n"+
 					"\t-batch is exclusive from the other parameters.\n\n " +
 					"Spaces between switches and names are relevant.\n\n";
 			
 			System.out.println (helpMsg);
 			return;
 		}
 		
 		
 		for (String arg: args) {
 			
 			if (arg.startsWith("-")) 
 				lastArg = arg;
 
 			else {
 				if (lastArg.equalsIgnoreCase(MainDump.PRJ_ARG))
 					prj += prj.equals("")? arg: " "+arg;
 				
 				if (lastArg.equalsIgnoreCase(MainDump.INTRV_ARG))
 					intrv += intrv.equals("")? arg: " "+arg;
 				
 				if (lastArg.equalsIgnoreCase(MainDump.GRP_ARG))
 					grp += grp.equals("")? arg: " "+arg;
 				
 				if (lastArg.equalsIgnoreCase(MainDump.SEC_ARG))
 					sec += sec.equals("")? arg: " "+arg;
 				
 				if (lastArg.equalsIgnoreCase(MainDump.FILE_ARG))
 					file += file.equals("")? arg: " "+arg;
 				
 				if (lastArg.equalsIgnoreCase(MainDump.QUES_ARG))
 					questionCods += arg;
 				
 				if (lastArg.equalsIgnoreCase(MainDump.QTYPE_PREFIX))
 					qryType += arg;
 				
 				if (lastArg.equalsIgnoreCase(MainDump.ORDER_ARG))
 					order += order.equals("")? arg: " "+arg;
 				
 				if (lastArg.equalsIgnoreCase(MainDump.VARS_ARG))
 					fileVars += fileVars.equals("")? arg: " "+arg;
 				
 				if (lastArg.equalsIgnoreCase(MainDump.BATCH_ARG)) {
 					try {
 						String propsFile = arg;
 //						rep.batch (propsDir + "/" + MainDump.PROPS_FILE	);
 						rep.batch(propsFile);
 						System.out.println ("End batch process!!!");
 						return;
 					}
 					catch (IOException ioEx) {
						ioEx.printStackTrace();
 						return;
 					}
 				}
 				
 			}
 		} // EO for
 		
 		
 		if (prj.equals("")) {
 			System.out.println ("\nThe name of the project has to be provided with the form" +
 					" -p project_name");
 			return;
 		}
 		
 		if (intrv.equals("")) {
 			System.out.println ("\nThe name of the interview has to be provided with the form" +
 					" -i questionnaire_name");
 			return;
 		}
 		/*
 		if (grp.equals("")) {
 			System.out.println ("\nThe name of the hospital has to be provided with the form" +
 					" -g hospital_name");
 			return;
 		}
 		*/
 		if (sec.equals("")) {
 			System.out.println ("\nThe section order for the questionnaire has to be provided with the form" +
 					" -s section_order");
 			return;
 		}
 		
 		if (file.equals("")) {
 			System.out.println ("\nThe name of the output file has to be provided with the form" +
 					" -f filename. It can be absolute or relative.");
 			return;
 		}
 		
 		if (fileVars.equals("")) {
 			String msgVars = "\nNo input variable names mapping file was supplied.";
 			msgVars += " Output header labels will be question codes.";
 			
 			System.out.println (msgVars);
 		}
 		
 		order = order.equals("")? "0": order.trim();
 		
 		Integer orderSec = Integer.decode(sec);
 		Integer sortOrder = Integer.decode(order);
 		DBDump dumper = new DBDump (prj, intrv, grp, orderSec, sortOrder, 
 						questionCods, "", file, fileVars);
 		dumper.dump();
 /*		
 		NewRetriever nr = new NewRetriever ("");
 		nr.getDump ("157", 304, 50, 8);
 */		
 		
 		System.out.println ("End!!");
 	}
 	
 }
