 //---------------------------------------------------------------------------
 // Copyright 2012 Ray Group International
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //---------------------------------------------------------------------------
 
 package com.raygroupintl.vista.tools;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.raygroupintl.m.struct.LineLocation;
 
 public class ErrorExemptions {
 	private Map<String, Set<LineLocation>> lines;
 	private Set<String> routines;
 	
 	public void addLine(String routineName, LineLocation lineLocation) {
 		if (this.lines == null) {
 			this.lines =  new HashMap<String, Set<LineLocation>>();
 		}
 		Set<LineLocation> locations = this.lines.get(routineName);
 		if (locations == null) {
 			locations = new HashSet<>();
 			this.lines.put(routineName, locations);
 		}		
 		locations.add(lineLocation);
 	}
 	
 	public void addLine(String routineName, String tag, int offset) {
 		this.addLine(routineName, new LineLocation(tag, offset));
 	}
 	
 	public void addRoutine(String routineName) {
 		if (this.routines == null) {
 			this.routines =  new HashSet<String>();
 		}
 		this.routines.add(routineName);
 	}
 	
 	public boolean containsLine(String routineName, LineLocation lineLocation) {
 		if (this.lines != null) {
 			Set<LineLocation> locations = this.lines.get(routineName);
 			if (locations != null) {
 				return locations.contains(lineLocation);
 			}
 		}
 		return false;	
 	}
 
 	public boolean containsLine(String routineName, String tag, int offset) {
 		return containsLine(routineName, new LineLocation(tag, offset));	
 	}
 	
 	public boolean containsRoutine(String routineName) {
 		return (this.routines != null) && this.routines.contains(routineName);	
 	}
 	
 	public Set<LineLocation> getLines(String routine) {
 		if (this.lines == null) {
 			return Collections.emptySet();
 		} else {
 			return this.lines.get(routine);
 		}
 	}
 	
 	public static ErrorExemptions getVistAFOIAInstance() {
 		ErrorExemptions r = new ErrorExemptions();
 		r.addLine("ANRVRRL", "BEGIN", 3);
 		r.addLine("ANRVRRL", "A1R", 2);
 		r.addLine("DINVMSM", "T0", 2);
 		r.addLine("DINVMSM", "T1", 2);
 		r.addLine("ZOSVMSM", "T0", 2);
 		r.addLine("ZOSVMSM", "T1", 2);
 		r.addLine("MUSMCR3", "BEG", 2);
 		r.addLine("MUSMCR3", "BEG", 6);
 		r.addLine("MUSMCR1", "EN11", 3);
 		r.addLine("DENTA14", "P1", 0);
 		r.addLine("HLOTCP", "RETRY", 8);
 		r.addLine("HLOTCP", "RETRY", 9);
 		r.addLine("HLOTCP", "RETRY", 14);
 		r.addLine("RGUTRRC", "JOBIT", 2);
 		r.addLine("ZISHMSU", "OPEN", 9);
 		r.addLine("ZISG3", "SUBITEM", 4);
 		r.addLine("PRCBR1", "LOCK", 1);
 		r.addLine("ZISTCP", "CVXD", 2);
 		r.addLine("ZOSVKRV", "JT", 11);
 		r.addLine("ZTMB", "RESTART", 21);
 		r.addLine("LEXAR7", "MAILQ", 1);
 		r.addLine("XMRTCP", "SOC25", 3);
 		r.addLine("ORRDI2", "TCOLD", 6);
 		r.addLine("PSSFDBRT", "POST", 21);
 		r.addLine("PSSHTTP", "PEPSPOST", 26);
 		r.addLine("PSSHTTP", "PEPSPOST", 34);
 		r.addRoutine("PSORELD1");
 		r.addRoutine("HLUCM001");// Do block
 		r.addRoutine("ZISG3");
 		r.addRoutine("ZOSVKSOE");
 		r.addRoutine("DGRUGMFU");
 		r.addRoutine("SPNRPC4");
 		r.addRoutine("MUSMCR1");
 		r.addRoutine("SDCNP");
 		//r.addRoutine("GMRAPT");  // Do block
 		//r.addRoutine("OOPSGUI3");
 		//r.addRoutine("IBDF10A");
 		return r;
 	}
 }
