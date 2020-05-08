 /*
  * jPOS Project [http://jpos.org]
  * Copyright (C) 2000-2010 Alejandro P. Revilla
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jpos.ee.pm.core.monitor;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /** A monitor source that takes information from a file
  * 
  * @author jpaoletti jeronimo.paoletti@gmail.com
  * @see http://github.com/jpaoletti/jPOS-Presentation-Manager
  * 
  * */
 public class FileMonitorSource extends MonitorSource {
 	private String filename;
 
 	public List<MonitorLine> getLinesFrom(Object actual) throws Exception {
 		//TODO Enhance line retrieve to get last lines directly
 		String line = null;
         Integer currentLineNo = 0;
         List<MonitorLine> result = new ArrayList<MonitorLine>(); 
 
         BufferedReader in = null;
         try {
                 in = new BufferedReader (new FileReader(getFilename()));
                 
                 Integer startLine = (actual==null)?0:(Integer) actual;
                System.out.println("Actual: "+startLine);
				//read to startLine
                 while(currentLineNo<startLine+1 ) {
                         if (in.readLine()==null) throw new IOException("File too small");
                         currentLineNo++;
                 }
                 
                 //read until endLine
                 line = in.readLine();
                 while(line != null ) {
                 	MonitorLine l = new MonitorLine();
                 	l.setId(currentLineNo);
                 	l.setValue(line);
                 	result.add(l);
                	//System.out.println("!---> "+line);
                    currentLineNo++;
                     line = in.readLine();
                 }
         } finally {
         	try { if (in!=null) in.close(); } catch(IOException ignore) {}
         }
         return result;
 	}
 	
 	public MonitorLine getLastLine() throws Exception {
 		String line = null;
         MonitorLine result = new MonitorLine(); 
 
         BufferedReader in = null;
         try {
         	in = new BufferedReader (new FileReader(getFilename()));
             int i=0;
         	line = in.readLine();
         	while(line != null ) {
         		result.setId(i);
         		result.setValue(line);
                 i++;
                 line = in.readLine();
         	}
        	System.out.println("!! "+result.getValue());
         } finally {
         	try { if (in!=null) in.close(); } catch(IOException ignore) {}
         }
         return result;
 	}
 
 
 	public void init() {
 		setFilename(getConfig("filename"));
 	}
 
 	public void setFilename(String filename) {
 		this.filename = filename;
 	}
 
 	public String getFilename() {
 		return filename;
 	}
 }
