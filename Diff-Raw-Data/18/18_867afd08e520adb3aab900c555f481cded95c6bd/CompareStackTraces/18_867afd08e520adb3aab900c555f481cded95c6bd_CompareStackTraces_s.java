 package ldt.run;
 
 import ldt.threads.ThreadDumpReader;
 
 /**
  * TODO comment this class
  *
  * @author lrosenberg
  * @since 06.02.13 23:29
  */
 public class CompareStackTraces {
 	public static void main(String a[]) throws Exception{
 
		ThreadDumpReader.compare("2013-02-06/jstack-2013-02-06-11-45.txt","2013-02-06/jstack-2013-02-06-10-44.txt");
 
 		ThreadDumpReader.compare("2013-02-06/jstack-2013-02-06-11-55.txt","2013-02-06/jstack-2013-02-06-11-45.txt");
 
 		ThreadDumpReader.compare("2013-02-06/jstack-2013-02-06-11-57-syncstarted.txt", "2013-02-06/jstack-2013-02-06-11-55.txt");
 
 		ThreadDumpReader.compare("2013-02-06/jstack-2013-02-06-12-01-updatefinished.txt", "2013-02-06/jstack-2013-02-06-11-57-syncstarted.txt");
 
 		ThreadDumpReader.compare("2013-02-06/jstack-2013-02-06-14-07.txt", "2013-02-06/jstack-2013-02-06-10-44.txt");

 	}
 }
