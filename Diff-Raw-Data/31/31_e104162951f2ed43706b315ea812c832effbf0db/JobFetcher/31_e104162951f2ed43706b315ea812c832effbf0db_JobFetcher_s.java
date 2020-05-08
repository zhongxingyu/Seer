 /*
  * Copyright (c) 2011 Karsten Heiken <karsten@disposed.de>
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is 
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package org.scattport.client;
 
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * 
  * @author Karsten Heiken <karsten@disposed.de>
  */
 public class JobFetcher implements Runnable {
 
 	@Override
 	public void run() {
 		while (Client.running) {
 			try {
 				System.out.println("Checking for new jobs");
 
				HashMap result = Client.exec("get_job");
 
 				if (!result.get("success").equals("true")) {
 					System.out
 							.println("Server has the hick-ups. Try again later.");
 				}
 
 				if (result.get("new_job").equals("true")) {
 					System.out.println("New Job!");
 					System.out.println("ID: " + result.get("job_id"));
 
 					Job newJob = new Job(result.get("job_id").toString());
 					Client.addJob(newJob);
 				} else {
 				}
 
 				Thread.sleep(Client.JOBFETCHER_INTERVAL * 1000);
 			} catch (InterruptedException ex) {
 				Logger.getLogger(JobFetcher.class.getName()).log(Level.SEVERE,
 						null, ex);
 			}
 		}
 	}
 }
