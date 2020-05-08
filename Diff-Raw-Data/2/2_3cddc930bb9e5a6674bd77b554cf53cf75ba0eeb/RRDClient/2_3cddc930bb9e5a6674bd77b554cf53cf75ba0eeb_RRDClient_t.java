 package info.fetter.rrdclient;
 
 /*
  * Copyright 2013 Didier Fetter
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Class containing main method to be called from the command line.
  * 
  * @author Didier Fetter
  *
  */
 public class RRDClient {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		List<String> remainingArgs = Arrays.asList(args);
 		String rrdServer[] = remainingArgs.remove(0).split(":");
 		String command = remainingArgs.remove(0);
 		RRDCommand rrdCommand;
 		
 		if(command.equals("graph")) {
 			String fileName = remainingArgs.remove(0);
			rrdCommand = new GraphCommand(remainingArgs.toArray(new String[0]));
 		} else if(command.equals("fetch")) {
 			String fileName = remainingArgs.remove(0);
 			String consolidationFunction = remainingArgs.remove(0);
 			rrdCommand = new FetchCommand(fileName, consolidationFunction, remainingArgs.toArray(new String[0]));
 		} else {
 			throw new IllegalArgumentException("RRD command unknown : " + command);
 		}
 		
 		rrdCommand.execute(System.out, rrdServer[0], Integer.parseInt(rrdServer[1]));
 	}
 
 	
 	
 }
