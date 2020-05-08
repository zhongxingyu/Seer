 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.lorent.common.util;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.lang.management.ManagementFactory;
 import java.lang.management.RuntimeMXBean;
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author Administrator
  */
 public class ProcessUtil {
     
     
     private static ProcessUtil instance = new ProcessUtil();
     private Logger log = Logger.getLogger(ProcessUtil.class);
     private ProcessUtil() {
 
     }
 
     public static ProcessUtil getInstance() {
         return instance;
     }
     
     
     /**
      * 终止processName名称的进程
      * @param processName  进程的名称
      * @throws Exception 
      */
     public void killProcessByName(String processName) throws Exception{
         Runtime.getRuntime().exec("cmd /c taskkill /F /im " + processName);
     }
     
     public Process startProcess(String Command)throws Exception{
         Process process = Runtime.getRuntime().exec(Command);
         return process;
     }
     
     public Process startProcess(String[] Command)throws Exception{
         Process process = Runtime.getRuntime().exec(Command);
         return process;
     }
     
     public boolean  processExists(String processName) throws Exception{
 //        ArrayList<String> namesList = new ArrayList<String>();
         Process process = Runtime.getRuntime().exec("cmd /c tasklist ");
         
         InputStreamReader ipsr = new InputStreamReader(process.getInputStream());//把得到的输入流转换为字节流
         BufferedReader br = new BufferedReader(ipsr);// 把字节流转换为字符流
         String result=null;
         boolean bFlag = false;
         while ((result = br.readLine()) != null) {
             int indexOf = result.toLowerCase().indexOf(processName.toLowerCase());
             if (indexOf != -1) {
 //                return true;
             	bFlag = true;
//            	break;
             }
         }
         process.waitFor();
         return  bFlag;
     }
 
     public String getTaskListLine(String processName) throws Exception{
         Process process = Runtime.getRuntime().exec("cmd /c tasklist ");
         InputStreamReader ipsr = new InputStreamReader(process.getInputStream());//把得到的输入流转换为字节流
         BufferedReader br = new BufferedReader(ipsr);// 把字节流转换为字符流
         String result="";
         String sTemp = "";
         while ((sTemp = br.readLine()) != null) {
             int indexOf = sTemp.toLowerCase().indexOf(processName.toLowerCase());
             if (indexOf != -1) {
                 result = sTemp.toLowerCase();
             }
         }
         process.waitFor();
         return  result;
     }
 
     
     public static void main(String args[]){
         /*
         try {
         //getInstance().startProcess("cmd /c C:\\Documents and Settings\\Administrator.NDJXPG-2372D204\\桌面\\debug\\Monitor_Lcc_Mfc.exe 5080 10.168.130.203 4192 123456 352 288 256 1 10001 10.168.130.102 1000");
         String[] command = {"F:\\debug\\Monitor_Lcc_Mfc.exe", "5080", "10.168.130.203", "4192", "123456", "352", "288", "256", "1", "10001", "10.168.130.102", "1000"};
         Process process = Runtime.getRuntime().exec(command);
         BufferedReader buf = new BufferedReader(new InputStreamReader(process.getErrorStream()));
         String str = "";
         while((str = buf.readLine())!= null)
         {
         System.out.println(str);
         }
         //process.destroy();
         //getInstance().restartApplication();
         } catch (Exception ex) {
         System.out.println("dfasdfdf");
         Logger.getLogger(ProcessUtil.class.getName()).log(Level.SEVERE, null, ex);
         }
          
         ProcessUtil processUtil = new ProcessUtil();
         try {
             processUtil.killProcessByName("Monitor_Lcc_Mfc.exe");
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         */
     	/*
     	ProcessUtil processUtil = new ProcessUtil();
     	try {
 			boolean processExists = processUtil.processExists("winvnc.exe");
 			System.out.println("processExists: "+processExists);
 			
 			String taskListLine = ProcessUtil.getInstance().getTaskListLine("winvnc.exe");
 			System.out.println(taskListLine);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		*/
     	try {
     		String liveTv = StringUtil.convertFilePath2DOSCommandStr("D:\\MyEclipseWorkspaces\\git\\lnvc2\\vovo\\client\\livetv\\tv.jar");
     		final String cmdStr = "cmd /c "+StringUtil.convertFilePath2DOSCommandStr("java -jar "+liveTv);
     		new Thread(){
 
     			@Override
     			public void run() {
     				try {
     					Process startProcess = ProcessUtil.getInstance().startProcess(cmdStr);
     					startProcess.waitFor();
     				} catch (Exception e) {
     				}
     			}
     		}.start();
 		} catch (Exception e) {
 			// TODO: handle exception
 			e.printStackTrace();
 		}
     }
     
     public int getOwnPid(){
         RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();   
         String name = runtime.getName(); // format: "pid@hostname"   
         try {   
             return Integer.parseInt(name.substring(0, name.indexOf('@')));   
         } catch (Exception e) {   
         	log.error("getOwnPid", e);
             return -1;   
         }   
     }
     
 }
