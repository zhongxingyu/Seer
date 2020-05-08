 package at.ac.tuwien.lsdc.generator;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import at.ac.tuwien.lsdc.mape.Monitor;
 
 public class RequestGenerator {
 	static volatile RequestGenerator instance;
 	
 	// RequestQueue
 	private List<Request> requests = new LinkedList<Request>();
 		
 	public static RequestGenerator getInstance() {
 		if (RequestGenerator.instance == null) {
 			synchronized(RequestGenerator.class) {
 				if (RequestGenerator.instance == null) {
 					RequestGenerator.instance = new RequestGenerator();
 				}
 			}
 		}
 		
 		return RequestGenerator.instance;
 	}
 	
 	/**
 	 * Retrieve requests that are not yet handled by the system
 	 * 
 	 * @return List of requests that should be converted to apps
 	 */
 	public List<Request> getPendingRequests() {
 		int globalTick = Monitor.getInstance().getGlobalTicks();
 		
 		List<Request> pendingRequests = new LinkedList<Request>();
 		for (Request request : this.requests) {
			if (request.getStart() >= globalTick) {
 				pendingRequests.add(request);
 			}
 		}
 		
 		return pendingRequests;
 	}
 	
 	/**
 	 * Removes a request from the request-queue
 	 * 
 	 * @param request Request that should be removed
 	 */
 	public void removeRequestFromQueue(Request request) {
 		this.requests.remove(request);
 	}
 
 	public List<Request> getRequests() {
 		return requests;
 	}
 
 	public void setRequests(List<Request> requests) {
 		this.requests = requests;
 	}
 	
 	public void addRequest(Request request) {
 		this.requests.add(request);
 	}
 	
 	
 	public List<Request> generateRequests() throws NumberFormatException, IOException {
 		
 		List<Request> requests = new ArrayList<Request>();
 		
 		
 		
 		FileReader input = null;
 		try {
 			input = new FileReader("input.csv");
 		} catch (FileNotFoundException e) {
 			System.out.println("FileNotFoundException");
 			e.printStackTrace();
 		}
 		BufferedReader bufRead = new BufferedReader(input);
 		String line = null;
 		while((line= bufRead.readLine()) != null){
 		Request request = new Request();
 		String[] values = line.split(";");
 		System.out.println("Size of the values " + values.length);
 		
 		List<Integer> slas = new LinkedList<Integer>();
 		int i;
 		for (i = 1; i < 4;i++)
 			slas.add(Integer.parseInt(values[i]));
 		
 		request.setCpu(slas.get(0));
 		request.setMemory(slas.get(1));
 		request.setStorage(slas.get(2));
 		System.out.println("Size of the slas " + slas.size());
 		
 		LinkedList<Integer> cpu = new LinkedList<Integer>();
 		for (; i < Integer.parseInt(values[0])+4;i++)
 			cpu.add(Integer.parseInt(values[i]));
 		request.setCpuUsage(cpu);
 		System.out.println("Size of the cpus " + cpu.size());
 		
 		LinkedList<Integer> memory = new LinkedList<Integer>();
 		for (; i < 2*Integer.parseInt(values[0])+4;i++)
 			memory.add(Integer.parseInt(values[i]));
 		request.setMemoryUsage(memory); 
 		
 		System.out.println("Size of the memory " + memory.size());
 		
 		LinkedList<Integer> storage = new LinkedList<Integer>();
 		for (; i < 3*Integer.parseInt(values[0])+4;i++)
 			storage.add(Integer.parseInt(values[i]));
 		request.setStorageUsage(storage);
 		System.out.println("Size of the storage " + storage.size());
 		
 		
 		request.setStart(Integer.parseInt(values[values.length-1]));
 		requests.add(request);
 		
 		
 		}
 		return requests;
 		
 	}
 
 }
