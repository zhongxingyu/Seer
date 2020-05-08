 package sim.storage.cli;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import sim.Request;
 import sim.storage.util.RequestType;
 
 public class WorkloadReader {
 
 	private static final String NUM_FILES = "numfiles";
 	private static final String LOWER_BOUND = "lower";
 	private static final String UPPER_BOUND = "upper";
 
 	private BufferedReader reader;
 
 	public WorkloadReader(String filePath) {
 		init(filePath);
 	}
 
 	private void init(String filePath) {
 		try {
 			reader = new BufferedReader(new FileReader(filePath));
 		} catch (FileNotFoundException e) {
 			throw new WorkloadReadException(e.getMessage());
 		}
 	}
 
 	public InitialDataInfo getInitialDataInfo() {
 		int numOfFiles = 0;
 		int lowerBound = 0;
 		int upperBound = 0;
 		int index = 0;
 
 		try {
 			String line = "";
 			while((line = reader.readLine().trim()) != null) {
 				if (line.startsWith("#") || line.trim().length() == 0) continue;
 				boolean isHeaderExist = false;
 				if (line.matches("^\\s*\\[header\\]")) {
 					line = reader.readLine().trim().toLowerCase();
 					if(line.startsWith(NUM_FILES)) {
 						index = line.indexOf(NUM_FILES) + NUM_FILES.length();
 						numOfFiles = Integer.parseInt(line.substring(index).trim());
 					} else {
 						throwsException(NUM_FILES);
 					}
 
 					line = reader.readLine().trim().toLowerCase();
 					if(line.startsWith(LOWER_BOUND)) {
 						index = line.indexOf(LOWER_BOUND) + LOWER_BOUND.length();
 						lowerBound = Integer.parseInt(line.substring(index).trim());
 					} else {
 						throwsException(LOWER_BOUND);
 					}
 
 					line = reader.readLine().trim().toLowerCase();
 					if(line.startsWith(UPPER_BOUND)) {
 						index = line.indexOf(UPPER_BOUND) + UPPER_BOUND.length();
 						upperBound = Integer.parseInt(line.substring(index).trim());
 					} else {
 						throwsException(UPPER_BOUND);
 					}
 					isHeaderExist = true;
 					break;
 				}
 
 				if (!isHeaderExist)
 					throw new WorkloadReadException("No header exists.");
 			}
 		} catch (IOException e) {
 			throw new WorkloadReadException(e.getMessage());
 		}
 		return new InitialDataInfo(numOfFiles, lowerBound, upperBound);
 	}
 
 	public Request[] getRequests(int require) {
 		List<Request> requests = new ArrayList<Request>();
 		try {
 			String line = "";
			while ((line = this.reader.readLine()) != null || require > 0) {
 				if ((line = line.trim()).length() == 0) continue;
 				if (line.startsWith("#")) continue;
 
 				String[] tokens = line.split(",");
 				int key = Integer.parseInt(tokens[0]);
 				int size = Integer.parseInt(tokens[1]);
 				double arrival = Double.parseDouble(tokens[2]);
 				RequestType reqType;
 				String type = tokens[3];
 				if (RequestType.READ.toString().equalsIgnoreCase(type)) {
 					reqType = RequestType.READ;
 				} else if (RequestType.WRITE.toString().equalsIgnoreCase(type)) {
 					reqType = RequestType.WRITE;
 				} else {
 					throw new WorkloadReadException("request type is invalid.");
 				}
 				requests.add(new Request(key, size, arrival, reqType));
 				require--;
 			}
 		} catch (IOException e) {
 			throw new WorkloadReadException(e.getMessage());
 		}
 		return requests.toArray(new Request[0]);
 	}
 
 	public void close() {
 		try {
 			this.reader.close();
 		} catch (IOException e) {
 			throw new WorkloadReadException(e.getMessage());
 		}
 	}
 
 	private void throwsException(String item) {
 		throw new WorkloadReadException(String.format("%s is not found", item));
 	}
 
 }
