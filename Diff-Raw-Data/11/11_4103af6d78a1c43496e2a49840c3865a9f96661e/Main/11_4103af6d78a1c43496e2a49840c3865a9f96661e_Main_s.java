 package vdrm.disp.main;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import vdrm.base.data.IServer;
 import vdrm.base.data.ITask;
 import vdrm.disp.dispatcher.Dispatcher;
 
 public class Main {
 
	private static final String CONFIG_FILE_XML = "..\\Config\\ConfigVd\\config.xml";
 
 	public static void main(String[] args) {
 		
 		JaxbConfig configuration = null;
 		JaxbServers tempServers = null;
 		JaxbTasks tempTasks = null;
 		JaxbHistory tempHistory = null;
 		ArrayList<ITask> history = null;
 		ArrayList<IServer> servers = null;
 		ArrayList<ITask> tasks = null;
 		
 		Unmarshaller um;
 		JAXBContext context;
 		boolean ioException = false;
 		
 		Dispatcher dispatcher = null;
 		
 		//1. load configuration file
 		try {
 			context = JAXBContext.newInstance(JaxbConfig.class);
 			um = context.createUnmarshaller();
 			configuration = (JaxbConfig) um.unmarshal(new FileReader(CONFIG_FILE_XML));
 		} catch (IOException e1) {
 			ioException = true;
 		} catch (JAXBException ee2) {
 			
 		} 
 		
 		//2. parse configuration file and load servers, tasks and history
 
 		//2.1. first load the servers
 		try {
 			context = JAXBContext.newInstance(JaxbServers.class);
 			um = context.createUnmarshaller();
 			tempServers = (JaxbServers) um.unmarshal(new FileReader(configuration.getServersPath()));
 		} catch (IOException e1) {
 			ioException = true;
 		} catch (JAXBException ee2) {
 			
 		}
 		
 		//2.2. next load the tasks
 		try {
 			context = JAXBContext.newInstance(JaxbTasks.class);
 			um = context.createUnmarshaller();
 			tempTasks = (JaxbTasks) um.unmarshal(new FileReader(configuration.getTasksPath()));
 		} catch (IOException e1) {
 			ioException = true;
 		} catch (JAXBException ee2) {
 			
 		}
 		
 		//2.3. last but not least, load the history
 		try {
 			context = JAXBContext.newInstance(JaxbHistory.class);
 			um = context.createUnmarshaller();
 			tempHistory = (JaxbHistory) um.unmarshal(new FileReader(configuration.getHistoryPath()));
 		} catch (IOException e1) {
 			ioException = true;
 		} catch (JAXBException ee2) {
 			
 		}
 		
 		if (ioException == true) {
 			// TODO: We had an IOException (an XML was missing...)
 			
 			// Do something about it! (display a message...and exit(0) here!)
 		}
 		
 		//3. conversions
 		tasks = new ArrayList<ITask>();
 		tasks.addAll(tempTasks.getTaskList());
 		servers = new ArrayList<IServer>();
 		servers.addAll(tempServers.getServerList());
 		history = new ArrayList<ITask>();
 		for(Integer i : tempHistory.getTaskHistory()) {
 			history.add(tasks.get(i));
 		}
 
 		
 		//4. initialize Dispatcher and run it
 		dispatcher = Dispatcher.Instance();
 		dispatcher.worker.initialize(servers, tasks, history);
 		dispatcher.ParseWorkloadXML(configuration.getWorkloadPath());
 	}
 
 }
