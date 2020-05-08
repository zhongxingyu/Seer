 package org.springsource.vfabric.tools;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
 
 public class AppDirector {
 	
 	/**
 	 * @param args
 	 * Deploys App, waits until deployed and retrieves the IP addresses of node 'nodename'
	 * @throws UnknownHostException 
 	 */
	public static void main(String[] args) throws UnknownHostException {
 		// java -cp target/classes org.springsource.vfabric.tools.AppDirector 'admin' 'Vmware1!' '172.16.227.192' '5'  'iiCaptainLite' "{\"node\":[ { \"name\":\"iiCaptainLite\", \"nodeComponent\":[ { \"name\":\"iicaptain\", \"property\":[ { \"key\":\"war_file\", \"value\":\"http://172.16.227.189/iicaptain-0.8.50-SNAPSHOT.war\"} ] } ] } ] }"
 	
 		
 		String user= args[0];
 		String passwd= args[1];
 		String appdip= args[2];
 		String depprofile= args[3];
 		String nodename= args[4];
 		String data1= args[5];
 		
		InetAddress address = InetAddress.getByName(appdip);
		
		boolean reachable= true;
		try {
			 reachable= address.isReachable(10000);
		} catch (IOException e1) {
			reachable= false;
		}
		if(!reachable)
			return;
		
 		String execargsDeploy[]= {"curl", "--user",user+":"+passwd,"-X","POST","-H","Content-Type: application/json","-d"," "+data1+" ","--basic","-k", "https://"+appdip+":8443/darwin/api/1.0/deployment-profile/"+depprofile+"/action/deploy"};
 		
 		String taskId= exec(execargsDeploy).replaceAll(".*<result>", "");
 		taskId= taskId.replaceAll("</result>.*", "");
 		
 		String execargsDeployments[]= {"curl",  "--user",user+":"+passwd,  "--basic", "-k", "https://"+appdip+":8443/darwin/api/1.0/deployments/page/0/page-size/65536"};
 		
 		
 		String depid= exec(execargsDeployments).replaceAll(" ", "");
 		depid= depid.replaceAll("\t", "");
 		depid= depid.replaceAll("\n", "");
 		depid= depid.replaceAll("<latest-task><id>"+taskId+"</id>.*", "");
 		depid= depid.replaceAll(".*<id>", "");
 		depid= depid.replaceAll("</id>.*", "");
 			
 		String execargsDeployment[]= {"curl",  "--user" ,user+":"+passwd, "--basic", "-k" ,"https://"+appdip+":8443/darwin/api/1.0/deployment/"+depid};
 		
 		boolean deployed= false;
 		do {
 			try {
 				Thread.currentThread().sleep(30000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			String status= exec(execargsDeployment);
 			if(status.contains("<end-time"))
 				deployed= true;
 			
 		} while(!deployed);
 		
 		
 		String execargsNodeInfo[]= {"curl",  "--user",user+":"+passwd,"--basic", "-k", "https://"+appdip+":8443/darwin/api/1.0/deployment/"+depid+"/node-info" };
 		String nodeinfo= exec(execargsNodeInfo);
 		nodeinfo= nodeinfo.replaceAll(".*<name>"+nodename+"</name>", "");
 		nodeinfo= nodeinfo.replaceAll("</instances>.*", "");
 		nodeinfo= nodeinfo.replaceAll(".*<ip-addresses>", "");
 		nodeinfo= nodeinfo.replaceAll("</ip-addresses>.*", "");
 		
 		System.out.println(nodeinfo);
 		
 	}
 	
 	private static String exec(String args[]) {
 		ProcessBuilder pb = new ProcessBuilder( args);
 		Process process= null;
 		try {
 			process = pb.start();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 		InputStream is = process.getInputStream();
 		InputStreamReader isr = new InputStreamReader(is);
 		BufferedReader br = new BufferedReader(isr);
 		StringBuffer out= new StringBuffer();
 		String line;
 		
 		try {
 			while ((line = br.readLine()) != null) {
 			    out.append(line);      
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return out.toString();
 	}
 	
 	
 }
