 package commons.io;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 import commons.cloud.Request;
 import commons.config.Configuration;
 
public abstract class AbstractWorkloadParser implements WorkloadParser<Request> {
 	
 	private BufferedReader reader;
 	private int currentDay = 0;
 	
 	protected int periodsAlreadyRead = 0;
 	private Request next;
 
 	public AbstractWorkloadParser(String workload, int saasclientID) {
 		try {
 			this.reader = new BufferedReader(new FileReader(readFileToUse(workload)));//Using normal load file
 			this.next = readNext();
 		} catch (FileNotFoundException e) {
			throw new RuntimeException("Problem reading workload file.", e);
 		}
 	}
 	
 	private String readFileToUse(String workload) {
 		this.currentDay = Configuration.getInstance().getSimulationInfo().getSimulatedDays();
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(workload));
 			String file = reader.readLine();
 			int currentLine = 0;
 			while(currentLine < this.currentDay){
 				currentLine++;
 				file = reader.readLine();
 			}
 			return file;
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException("Problem reading workload file.", e);
 		} catch (IOException e) {
 			throw new RuntimeException("Problem reading workload file.", e);
 		}
 	}
 
 	@Override
 	public void setDaysAlreadyRead(int simulatedDays){
 		throw new RuntimeException("not yet implemented");
 	}
 	
 	@Override
 	public void clear() {
 		throw new RuntimeException("not yet implemented");
 	}
 
 	@Override
 	public Request next() {
 		Request toReturn = this.next;
 		this.next = readNext();
 		return toReturn;
 	}
 
 	@Override
 	public boolean hasNext() {
 		return next != null;
 	}
 	
 	/**
 	 * @return
 	 */
 	private Request readNext() {
 		String line;
 		try {
 			line = reader.readLine();
 			if(line == null){
 				return null;
 			}
 			return parseRequest(line);
 		} catch (IOException e) {
 			throw new RuntimeException("Problem reading workload file.", e);
 		}
 	}
 
 	/**
 	 * @param line
 	 * @return
 	 */
 	protected abstract Request parseRequest(String line);
 
 	@Override
 	public void close(){
 		try {
 			this.reader.close();
 		} catch (IOException e) {
 			throw new RuntimeException("Problem closing workload file.");
 		}
 	}
 }
