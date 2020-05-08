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
		ThreadDumpReader.analyze("2013-02-06/jstack-2013-02-06-14-07.txt");
 	}
 }
