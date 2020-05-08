 package ldt.run;
 
 import ldt.threads.ThreadDumpReader;
 
 /**
  * TODO comment this class
  *
  * @author lrosenberg
  * @since 06.02.13 23:31
  */
 public class AnalyzeStackTrace {
 	public static void main(String a[]) throws Exception{
		//ThreadDumpReader.analyze("2013-02-07/jstack-2013-02-07-00-09.txt");
		//ThreadDumpReader.analyze("2013-02-05/5.txt");
		ThreadDumpReader.analyze("2013-02-07/jstack-2013-02-07-12-16.txt");

 	}
 }
