 package org.telcomp.sbb;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtField;
 import javassist.CtMethod;
 
 import javax.management.ObjectName;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.slee.ActivityContextInterface;
 import javax.slee.RolledBackContext;
 import javax.slee.SbbContext;
 import javax.slee.ServiceID;
 import javax.slee.management.ServiceManagementMBean;
 import javax.slee.management.ServiceState;
 import javax.slee.serviceactivity.ServiceActivity;
 import javax.slee.serviceactivity.ServiceActivityFactory;
 import javax.slee.serviceactivity.ServiceStartedEvent;
 
 import org.mobicents.slee.container.SleeContainer;
 
 import servicecategory.Input;
 import servicecategory.Operation;
 
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.Morphia;
 import com.mongodb.Mongo;
 
 import datamodel.PetriNet;
 import datamodel.Place;
 import datamodel.Token;
 import de.schlichtherle.truezip.file.TFile;
 import de.schlichtherle.truezip.file.TFileInputStream;
 import de.schlichtherle.truezip.file.TFileOutputStream;
 import de.schlichtherle.truezip.file.TVFS;
 
 public abstract class CodeInjectorSbb implements javax.slee.Sbb {
 
 	public ServiceActivityFactory saf;
 	
 	private final String deployPath = "/usr/local/Mobicents-JSLEE/jboss-5.1.0.GA/server/default/deploy/";
 	private final String tempDirPath = "/usr/local/Mobicents-JSLEE/temp/";
 	private final String duJarCmpt = "-DU.jar";
 	private final String sbbJarCmpt = "CS-sbb.jar";
 	private final String sbbClassCmpt = "CSSbb";
 	private final String sbbPath = "org.telcomp.sbb.";
 	private final String JarsPath = "/usr/local/Mobicents-JSLEE/neededJars/";
 	private static String sbbClassName = "";
 	private final String listFileName = tempDirPath + "servicesList.txt";
 	private static List<String> branchFields = new ArrayList<String>();
 
 	private final String [] params = { "javax.slee.management.ServiceState" };
 	private final Object [] args = {ServiceState.ACTIVE};
 
 	
 	public void onServiceStartedEvent(ServiceStartedEvent event, ActivityContextInterface aci) {
 		aci.detach(this.sbbContext.getSbbLocalObject());
 		ServiceActivity sa = saf.getActivity();
 		ServiceID [] serviceList;
 		
 		try{
 			serviceList = (ServiceID[]) SleeContainer.lookupFromJndi().getMBeanServer().
 					invoke(new ObjectName(ServiceManagementMBean.OBJECT_NAME),"getServices", args ,params);
 			
 			if(sa.equals(aci.getActivity())){
 				File serviceListFile = createServiceList();
 				System.out.println("*********************Initial Services Base****************");
 				for (int i=0; i<serviceList.length; i++){
 					System.out.println("Service "+(i+1)+": "+serviceList[i].getName());
 					if(i==0){
 						writeServiceList(serviceListFile, serviceList[i].getName(), false);
 					} else{
 						writeServiceList(serviceListFile, serviceList[i].getName(), true);
 					}
 				}
 				System.out.println("*********************Initial Services Base****************");
 			} else{
 				System.out.println("A new service has been deployed");
 				if(event.getService().getName().indexOf("Convergent Service") >= 0 
 						&& !this.checkExistingCS(new File(listFileName), event.getService().getName())){
 					this.writeServiceList(new File(listFileName), event.getService().getName(), true);
 					System.out.println("It's a Covergent Service, proceeding to insert monitoring code...");
 					String tempServiceName = event.getService().getName().replace(" ", "");
 					sbbClassName = tempServiceName.replace("ConvergentService", ""); 
 					System.out.println("SBB Class Name: "+sbbClassName);
 					instrumentCode(sbbClassName);
 				} else{
 					System.out.println("The service deployed is not a convergent service or has been instrumented already");
 				}
 			}
 			
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	public void instrumentCode(String serviceName) {
 		try {
 			String newTempDir = this.getSbbJar(serviceName);
 			ClassPool cp = ClassPool.getDefault();
 			cp.insertClassPath(newTempDir + serviceName + sbbJarCmpt);
 			cp.insertClassPath(deployPath + "mobicents-slee/lib/jain-slee-1.1.jar");
 			cp.insertClassPath(JarsPath + "EndWSInvocator-event.jar");
 			cp.insertClassPath(JarsPath + "servlet-api-5.0.16.jar");
 			
 			CtClass ctclass = cp.get(sbbPath + serviceName + sbbClassCmpt);
 
 			// Se leen los campos declarados en la clase del servicio a monitorear
 			for (CtField ctf : ctclass.getDeclaredFields()) {
 				// Metodo para obtener los campos con el nombre branchControlFlow
 				if(ctf.getName().contains("branchControlFlow")){
 					branchFields.add(ctf.getName());
 				}
 			}
 			
 			// Campo con el tamaño igual al número de variables branchControlFlow y campos para el manejo de estos
 			CtField ctfAF = CtField.make("private javax.slee.facilities.AlarmFacility alarmFacility2;",ctclass);
 			CtField ctfbranchSize = CtField.make("public int branchSize2=" + branchFields.size()+";", ctclass);
 			CtField ctfvalorBranch = CtField.make("public String valorBranch2=\"\";", ctclass);
 			CtField ctfvalorMensaje = CtField.make("public String valorMensaje2=\"\";", ctclass);
 			CtField ctfFlag = CtField.make("public static boolean flag;", ctclass);
 			
 			ctclass.addField(ctfbranchSize);
 			ctclass.addField(ctfvalorBranch);
 			ctclass.addField(ctfvalorMensaje);
 			ctclass.addField(ctfAF);
 			ctclass.addField(ctfFlag);
 			
 			// Modificar metodo setSbbContext
 			CtMethod methodCtx = ctclass.getDeclaredMethod("setSbbContext");
 			methodCtx.insertAfter("try { this.alarmFacility2 = (javax.slee.facilities.AlarmFacility) new javax.naming.InitialContext()" +
 					".lookup(javax.slee.facilities.AlarmFacility.JNDI_NAME);} catch (javax.naming.NamingException e)" +
 					"{System.out.println(\"Problem on setSbbContext\");}");
 			
 			//Code to detect messaging WebServices and get its context information
 			Mongo mongo = new Mongo("localhost");
 			Datastore petriNets = new Morphia().createDatastore(mongo, "PetriNetsManager");
 			Datastore operationsRep = new Morphia().createDatastore(mongo, "OperationsManager");
 			PetriNet retrievedPN = petriNets.get(PetriNet.class, serviceName);
 			String contextInfo = "";
 			
 			for(Place p : retrievedPN.getPlaces()){
 				if(p.getIdentifier().indexOf("InputPlace") >= 0 && !(p.getName().indexOf("Telco") >= 0)){
 					String opName = p.getName().substring(0, p.getName().length()-1);
 					Operation op = operationsRep.find(Operation.class).field("operationName").equal(opName).get();
 					
 					if(op.getCategory().equals("messaging")){
 						Place dap = getDAPlace(op, p, retrievedPN.getPlaces());
 						String paramName = dap.getName() + "ip0";
 						contextInfo = contextInfo.concat(op.getOperationName()+"-\"+"+paramName+"+\";");
 					}
 				}
 			}
 			
 			mongo.close();
 			
 			// Modificar metodo para establecer una Alarma
 			// Contiene tambien procesamiento para agregar branchFields a mensaje de Alarma
 			CtMethod methodAlarm = ctclass.getDeclaredMethod("onEndWSInvocatorEvent");
			String formId = "\"hiddenDiv\"";
 			String formStatus = "\"visibility:hidden\"";
 			String href = this.getReloadLink(ctclass, serviceName);
 			String language = "\"Javascript\"";
 			methodAlarm.insertBefore("{System.out.println(\"Monitoring Service Inserted Code...\");" +
 					"if (!$1.isSuccess()){" +
 					"try{Class fieldClass = "+sbbPath+serviceName+sbbClassCmpt+".class;" +
 					"for (int i=1; i<=branchSize2; i++){" +
 					"java.lang.reflect.Field fd = fieldClass.getDeclaredField(\"branchControlFlow\"+i);" +
 					"valorMensaje2 = valorBranch2 + String.valueOf(fd.getInt(fieldClass))+\";\";" +
 					"valorBranch2 = valorMensaje2;}" +
 					"this.alarmFacility2.raiseAlarm(javax.slee.management.SbbNotification.ALARM_NOTIFICATION_TYPE," +
 					"\"01\",javax.slee.facilities.AlarmLevel.MAJOR," +
 					"\";"+serviceName+";\"+$1.getOperationName()+\";\"+mainControlFlow+\";\"+valorBranch2+\"userid-\"+startpv0+\";"+contextInfo+"\"); " +
 					"java.io.PrintWriter w = httpResponse.getWriter();" +
 					"w.print(\"<html><body><center><h2>"+serviceName+" execution failed due to a problem with \"+$1.getOperationName()+\"" +
					" operation, proceeding to reconfigure it...</h2><br><br><br><form id=\""+formId+"\" style=\""+formStatus+"\">" +
 					"<h3>Reconfiguration process finished try to execute the service again</h3><br><a href=\""+href+"\">Execute Again</a>" +
 					"</form><script language=\""+language+"\">" +
					"setTimeout(function(){document.getElementById('hiddenDiv').style.visibility = 'visible';}, 5000);" +
 					"</script></center></body></html>\");" +
 					"w.flush();httpResponse.flushBuffer();httpAci.detach(this.sbbContext.getSbbLocalObject());" +
					"this.getEventContext().resumeDelivery();" +
 					"for(int a=0; a<this.sbbContext.getActivities().length; a++){" +
 					"this.sbbContext.getActivities()[a].detach(this.sbbContext.getSbbLocalObject());}" +
 					"return;}" +
 					"catch(java.lang.Exception e){e.printStackTrace();};}}");
 
 			// Exportar clase modificada
 			ctclass.writeFile(newTempDir);
 			ctclass.defrost();
 			ctclass.detach();
 			
 			//Getting the path of the reconfigurated SBB Class file
 			String newClassFilePath = newTempDir + sbbPath.replace(".", "/") + serviceName + sbbClassCmpt + ".class";
 			Thread.sleep(5500);
 			//Copying the reconfigurated SBB Class file into its corresponding Deloyable Unit
 			this.updateDUJar(serviceName, newClassFilePath);
 			//Deleting all temporal files created during the reconfiguration
 			this.deleteTemporals(newTempDir);
 			System.out.println("Javasisst Process terminated succesfully!!!!!");
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 	
 	private Place getDAPlace(Operation op, Place p, ArrayList<Place> places){
 		Token itkn = null;
 		Place daop = null;
 		Place daip = null;
 		
 		main: for(Input i : op.getInputs()){
 			if(i.getType().equals("id")){
 				for(Token t : p.getTokens()){
 					if(t.getDestiny().equals(i.getInputName())){
 						itkn = t;
 						break main;
 					}
 				}
 			}
 		}
 		
 		main1: for(Place dp : places){
 			if(dp.getIdentifier().indexOf("OutputPlace") >= 0){
 				for(Token dt : dp.getTokens()){
 					if(itkn.getSource().equals(dt.getDestiny())){
 						daop = dp;
 						break main1;
 					}
 				}
 			}
 		}
 		
 		for(Place dp : places){
 			if(dp.getIdentifier().indexOf("InputPlace") >= 0 && dp.getName().equals(daop.getName())){
 				daip = dp;
 				break;
 			}
 		}
 		
 		return daip;
 	}
 	
 	private String getReloadLink(CtClass ctclass, String serviceName){
 		String queryParams = "";
 		try{
 			for(CtField ctf : ctclass.getDeclaredFields()){
 				if(ctf.getName().indexOf("startpn") >= 0){
 					queryParams = queryParams.concat("\"+"+ctf.getName() + "+\"=\"+" + 
 				ctclass.getField(ctf.getName().replaceAll("n", "v")).getName()+"+\"&\"");
 				}
 			}
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 		String link = "\"http://\"+System.getProperty(\"jboss.bind.address\")+\":8080/mobicents/"+serviceName+"?"+queryParams;
 		link = link.substring(0, link.length()-2);
 		link = link.concat("\"");
 		return link;
 	}
 
 	private String getSbbJar(String serviceName){
 		JarFile jar;
 		int files = new File(tempDirPath).list().length;
 		String newTempDir = null;
 		try {
 			newTempDir = this.createNewTempDir(files);
 			jar = new JarFile(deployPath + this.getDuName(serviceName) + duJarCmpt);
 			JarEntry entry = (JarEntry) jar.getEntry("jars/" + serviceName + sbbJarCmpt);
 			File f = new File(newTempDir + serviceName + sbbJarCmpt);
 			InputStream is = jar.getInputStream(entry);
 			FileOutputStream fos = new FileOutputStream(f);
 	        while (is.available() > 0) {
 	            fos.write(is.read());
 	        }
 	        fos.close();
 	        is.close();
 	        jar.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return newTempDir;
 	}
 	
 	private String createNewTempDir(int files){
 		try {
 			Runtime run = Runtime.getRuntime();
 			Process p = run.exec("mkdir "+tempDirPath+files);
 			p.waitFor();
 		    p.destroy();
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 		return tempDirPath + files + "/";
 	}
 
 	private String getDuName(String serviceName) {
 		String duTemp = serviceName.substring(1);
 		return serviceName.substring(0, 1).toLowerCase().concat(duTemp);
 	}
 	
 	private void updateDUJar(String serviceName, String newClassFile){
 		TFile tFileRead = new TFile(newClassFile);
 		TFile tFileWrite = new TFile(deployPath + this.getDuName(serviceName) + duJarCmpt + "/jars/" + 
 				serviceName + sbbJarCmpt + "/" + sbbPath.replace(".", "/") + serviceName + sbbClassCmpt + ".class");
 		try {
 			TFileInputStream tfIs = new TFileInputStream(tFileRead);
 			TFileOutputStream tfOs = new TFileOutputStream(tFileWrite);
 			while(tfIs.available() > 0){
 				tfOs.write(tfIs.read());
 			}
 			tfIs.close();
 			tfOs.close();
 			TVFS.umount();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 	}
 	
 	private void deleteTemporals(String directory){
 		String temporal = directory.substring(0, directory.length()-1);
 		TFile temp = new TFile(temporal);
 		try {
 			temp.rm_r();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private File createServiceList(){
 		File serviceList = null;
 		try{
 			serviceList = new File(tempDirPath+"servicesList.txt");
 			if(serviceList.exists()){
 				serviceList.delete();
 				serviceList = new File(tempDirPath+"servicesList.txt");
 			}
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 		return serviceList;
 	}
 	
 	private void writeServiceList(File file, String name, boolean flag){
 		try{
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
 			if(flag){
 				writer.newLine();
 			}
 			writer.append(name);
 			writer.close();
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	private boolean checkExistingCS(File file, String serviceName){
 		boolean exists = false;
 		
 		try{
 			 BufferedReader br = new BufferedReader(new FileReader(file));
 		     String line = br.readLine();
 		     
 		     while(line != null){
 		    	 if(line.equals(serviceName)){
 		    		 exists = true;
 		    		 break;
 		    	 }
 		    	 line = br.readLine();
 		     }
 		     br.close();
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		return exists;
 	}
 
 	// TODO: Perform further operations if required in these methods.
 	public void setSbbContext(SbbContext context) {
 		this.sbbContext = context;
 		try {
 			Context ctx = (Context) new InitialContext().lookup("java:comp/env");
 			saf = (ServiceActivityFactory) ctx.lookup("slee/serviceactivity/factory");
 		} catch (NamingException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void unsetSbbContext() {
 		this.sbbContext = null;
 	}
 
 	// TODO: Implement the lifecycle methods if required
 	public void sbbCreate() throws javax.slee.CreateException {
 	}
 
 	public void sbbPostCreate() throws javax.slee.CreateException {
 	}
 
 	public void sbbActivate() {
 	}
 
 	public void sbbPassivate() {
 	}
 
 	public void sbbRemove() {
 	}
 
 	public void sbbLoad() {
 	}
 
 	public void sbbStore() {
 	}
 
 	public void sbbExceptionThrown(Exception exception, Object event,
 			ActivityContextInterface activity) {
 	}
 
 	public void sbbRolledBack(RolledBackContext context) {
 	}
 
 	/**
 	 * Convenience method to retrieve the SbbContext object stored in
 	 * setSbbContext.
 	 * 
 	 * TODO: If your SBB doesn't require the SbbContext object you may remove
 	 * this method, the sbbContext variable and the variable assignment in
 	 * setSbbContext().
 	 * 
 	 * @return this SBB's SbbContext object
 	 */
 
 	protected SbbContext getSbbContext() {
 		return sbbContext;
 	}
 
 	private SbbContext sbbContext; // This SBB's SbbContext
 
 }
