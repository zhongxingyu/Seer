 package org.ebag.net.obj;
 
 import java.net.InetAddress;
 
 /**
  * Ϣ
  * @author sdujq
  *
  */
 public final class I {
 //	/**0-99*/
 //	public static final class request{
 //		public static final int no_ruquest=0;
 //		
 //		public static final int get_exams=1;
 //		public static final int get_answerState=2;
 //		public static final int get_examActivityState=3;
 //		
 //		public static final int up_exampaper=4;//ʦϴwordԾ
 //		public static final int up_studentAnswers=5;
 //		public static final int up_teacherMarks=6;
 //		
 //		public static final int start_exam=7;
 //	}
 	/**100-999*/
 	public static final class  signal{
 		public static final int sys_error=100;
 		public static final int sys_busy=101;
 		
 		public static final int login_true=102;
 		public static final int login_false=103;
 	}
 	/**1000-9999*/
 	public static final class choice{
 		public static final int answerState_waitAnser=1001;
 		public static final int answerState_waitMark=1002;
 		public static final int answerState_waitComment=1003;
 		public static final int answerState_finish=1004;
 		
 		public static final int userType_teacher=1005;
 		public static final int userType_student=1006;
 		
 		public static final int examType_homework=1007;
 		public static final int examType_exam=1008;
 		
 		public static final int problemType_xz=1009;
 		public static final int problemType_jd=1010;
 	}
 	
 	public static final class url{
 		public static final String problem="1";
 		public static final String hint="2";
 		public static final String ans="3";
 		public static final String analysis="4";
 		public static final String difficulty="5";
 		public static final String aspect="6";
 		public static final String request="7";
 	}
 	public static final class tupload{
		public static final String mina_server_site="127.0.01";
 		public static final int mina_server_port=731;
		public static final String ftp_site="127.0.0.1";
		public static int ftp_port=21;
 		public static String ftp_pwd="@!!*&@@&%^";
 		
 	}
 }
