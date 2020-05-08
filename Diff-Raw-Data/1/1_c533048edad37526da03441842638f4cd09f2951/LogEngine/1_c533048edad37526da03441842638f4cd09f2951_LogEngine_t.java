 package org.publicmain.common;
 import java.sql.Time;
 import java.util.Arrays;
 
 import org.publicmain.nodeengine.ConnectionHandler;
 
 
 /**GGF mal ber java.util.logging nachdenken
  * @author tkessels
  *
  */
 public class LogEngine {
 	private static int verbosity=4;
 	public static final int TRACE=4;
 	public static final int INFO=3;
 	public static final int WARNING=2;
 	public static final int ERROR=1;
 	public static final int NONE=0;
 	public static MSGCode[] filter_code= {MSGCode.ECHO_REQUEST,MSGCode.ECHO_RESPONSE};
 	public static NachrichtenTyp[] filter_typ= {};
 	public static String[] filter_source={};
 	
 	/**Gibt eine Exception auf dem Programm Fehlerstrom aus
 	 * @param e Die zu dokumentierende Exception
 	 */
 	public static void log(Exception e) {
 		if(verbosity>0){
 			log(e.getMessage(),ERROR);
 			//e.printStackTrace();
 		}
 	}
 	
 	public static void log(Object source, Exception e) {
 		String sourceString=(source instanceof String)?(String)source:source.getClass().getSimpleName() ;
 		if(verbosity>=ERROR){
 			log(sourceString + ":"+e.getMessage(),ERROR);
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/** Setzt die Meldeschwelle ab welchem schweregrad eine Ausgabe erfolgen soll.
 	 * @param x Das Verbosittslevel ?!?!
 	 */
 	public static void setVerbosity(int x){
 		verbosity=x;
 	}
 	
 	/**Gibt eine Fehlermeldung auf dem Fehlerstrom aus 
 	 * @param source Die Quelle des Fehlers
 	 * @param meldung Der Text der Fehlermeldung
 	 * @param errorLevel Das Niveau des Fehlers (INFO, WARNING oder ERROR)
 	 */
 	public static void log(final Object source, final String meldung,final int errorLevel){
 			new Thread(new Runnable() {
 				public void run() {
 					String sourceString = (source instanceof String) ? (String) source : source.getClass().getSimpleName();
 					if(!Arrays.asList(filter_source).contains(sourceString)&&errorLevel<=verbosity)log(sourceString + " : " + meldung, errorLevel);
 					else if ((errorLevel==ERROR)&&(verbosity>=ERROR))log(sourceString + " : " + meldung, errorLevel);
 				}
 			}).start();
 	}
 	
 	private static String msg2String(MSG x){
		if(x==null) return "null";
 		return "MSG{"+x.getTyp()+"("+((x.getCode()!=null)?x.getCode():"")+((x.getGroup()!=null)?x.getGroup():"")+")"+ "\t:"+Math.abs(x.getSender()%10000)+"("+x.getId()+")"+">"+Math.abs(x.getEmpfnger()%10000)+"["+x.getData()+"]}";
 	}
 	
 	
 	
 	public static void log(final Object source,final String action,final MSG x){
 		if (verbosity == 4) {
 			new Thread(new Runnable() {
 				public void run() {
 					if (!filtered(x)) {
 						String sourceString = (source instanceof String) ? (String) source: source.getClass().getSimpleName();
 						log(sourceString + " : " + action + " : " + msg2String(x), TRACE);
 					}
 				}
 			}).start();
 		}
 	}
 	
 	protected static boolean filtered(MSG x) {
 		if(x!=null)
 		{
 		for (NachrichtenTyp tmp : filter_typ) 	if(x.getTyp()==tmp)return true;
 		if(x.getTyp()==NachrichtenTyp.SYSTEM) 	for (MSGCode tmp : filter_code)	if(x.getCode()==tmp) return true;
 		}
 		return false;
 	}
 
 	public static void log(ConnectionHandler quelle,String meldung){
 		log(quelle.toString()+":"+meldung,INFO);
 	}
 	
 	public static void log(String meldung,int errorLevel){
 		if(errorLevel<=verbosity){
 			System.err.println(new Time(System.currentTimeMillis()).toString()+" : "+meldung);
 		}
 	}
 
 	public static void log(final ConnectionHandler quelle, final String action,final MSG paket) {
 				log(quelle.toString(), action, paket);
 	}
 	
 	
 	
 
 }
