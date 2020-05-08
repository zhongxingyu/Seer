 package edu.ucsb.deepspace;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class ScriptLoader {
 	
 	private CommGalil protocol;
 	
 	private Script homeA;
 	private Script homeB;
 	private Script raster;
 	private Script fraster;
 	private Script azScan;
 	private Script elScan;
 	
 	
 	private Script readerInfo;
 	private Set<String> loadedScriptNames;
 	private Map<String, Script> scripts;
 	private int size = 0;
 	
 	public ScriptLoader() {
 		protocol = new CommGalil(23);
 		loadedScriptNames = new HashSet<String>();
 		scripts = new HashMap<String, Script>();
 		scripts.put("#HOMEAZ", homeA);
 		scripts.put("#HOMEB", homeB);
 		scripts.put("#READERI", readerInfo);
 		scripts.put("#RASTER", raster);
 		scripts.put("#FRASTER", fraster);
 		scripts.put("#AZSCAN", azScan);
 		scripts.put("#ELSCAN", elScan);
 		
 	}
 	
 	public Set<String> findExpected() {
 		System.out.println(scripts.keySet());
 		return scripts.keySet();
 	}
 	
 	public Set<String> findLoaded() {
 		protocol.read();
 		protocol.read();
 		protocol.read();
 		String labels = protocol.sendRead("LL");
 		System.out.println(labels);
 		String[] split = labels.split("\r\n");
 		for (String s : split) {
 			String name = s.split("=")[0];
 			loadedScriptNames.add(name);
 		}
 		System.out.println(loadedScriptNames);
 		return loadedScriptNames;
 	}
 	
 	public boolean readerReady() {
 		return loadedScriptNames.contains("#READERI");
 	}
 	
 //	public void check() {
 //		String labels = protocol.sendRead("LL");
 //		String[] split = labels.split("\r\n");
 //		for (String s : split) {
 //			String name = s.split("=")[0];
 //			loadedScriptNames.add(name);
 //		}
 //	}
 	
 	public void load() {
 		Set<String> scriptsToLoad = scripts.keySet();
 		
 		if (loadedScriptNames.containsAll(scriptsToLoad)) {
 			return;
 		}
 		
 		indexAz();
 		indexEl();
 		readerInfo();
 		raster();
 		fraster();
 		azScan();
 		elScan();
 		
 		protocol.sendRead(homeA.getScript());
 		pause();
 		protocol.sendRead(homeB.getScript());
 		pause();
 		protocol.sendRead(readerInfo.getScript());
 		pause();
 		protocol.sendRead(raster.getScript());
 		pause();
 		protocol.sendRead(fraster.getScript());
 		pause();
 		protocol.sendRead(azScan.getScript());
 		pause();
 		protocol.sendRead(elScan.getScript());
 		pause();
 	}
 	
 	public void close() {
 		protocol.close();
 	}
 	
 	private void indexAz() {
 		homeA = new Script("#HOMEAZ", size);
 		homeA.add("IF (_MOA)");
 		//"BG" commands fail if the motor is off. Therefore, check motor state
 		homeA.add("MG \"Motor is off. Cannot execute home operation\"");
 
 		homeA.add("ELSE");
 		//Save acceleration and jog speed values
 		homeA.add("T1 = _JGA");
 		homeA.add("T2 = _ACA");
 
 		//then overwrite them
 		homeA.add("MG \"Homing\", T1");
 		homeA.add("JGA=150000");
 		homeA.add("ACA=50000");
 
 		//"FE" - find the opto-edge
 		homeA.add("FE A");
 		homeA.add("BG A");
 		homeA.add("AM A");
 		homeA.add("MG \"Found Opto-Index\"; TP");
 
 		//Turn the jog speed WAAAY down when searching for the index
 		homeA.add("JGA=500");
 
 		//Do the index search ("FI")
 		homeA.add("FI A");
 		homeA.add("BG A");
 
 		homeA.add("AM A");
 		homeA.add("MG \"Motion Done\"; TP");
 
 		//Finally, restore accel and jog speeds from before routine was run
 		homeA.add("JGA=T1");
 		homeA.add("ACA=T2");
 
 		homeA.add("ENDIF");
 		homeA.add("EN");
 		size = homeA.size();
 	}
 	
 	private void indexEl() {
 		homeB = new Script("#HOMEB", size);
 		String axisAbbrev = Axis.EL.getAbbrev();
 		
 		homeB.add("T1 = _JG" + axisAbbrev);
 		homeB.add("T2 = _AC" + axisAbbrev);
 		
 		double jg = 1000d;
 		homeB.add("JG" + axisAbbrev + "=" + jg);
 		
 		// Do the index search ("FI")
 		homeB.add("FI" + axisAbbrev);
 		homeB.add("BG" + axisAbbrev);
 		homeB.add("AM" + axisAbbrev);
 		homeB.add("PRB=3900");
 		homeB.add("BG" + axisAbbrev);
 		jg = 50d;
 		homeB.add("AM" + axisAbbrev);
 		homeB.add("JG" + axisAbbrev + "=" + jg);
 		homeB.add("FI" + axisAbbrev);
 		homeB.add("BG" + axisAbbrev);
 		homeB.add("JG" + axisAbbrev + "=T1");
 		homeB.add("AC" + axisAbbrev + "=T2");
 		homeB.add("EN");
 		size += homeB.size();
 	}
 	
 	public void raster() {
 		
 		raster = new Script("#RASTER", size);
 		raster.add("dx = maxAz  - minAz");
 		raster.add("dy = (maxEl-minEl)/lineNum");
 
 
 		raster.add("AC 1000");
 
 		raster.add("dx = maxAz-minAz");
 		raster.add("vf = 2*dx/(time)*lineNum");
 
 		raster.add("SP vf");
 
 		raster.add("AC _AC");
 		raster.add("DC _AC");
 		raster.add("acTime = vf/_AC");
 		raster.add("acX = .5*_AC*acTime*acTime"); 
 
 		raster.add("n=0");
 		raster.add("#SNAKE");
 
 
 
 		raster.add("PR dx + acX");
 		raster.add("BG A");
 
 
 		raster.add("AD acX"); 
 		raster.add("OP 1");
 		raster.add("AM");
 
 
 
 		raster.add("PR -dx-acX");
 
 		raster.add("BG A");
 		raster.add("AD dx");
 		raster.add("OP 0");
 
 		raster.add("AM");
 
 
 		raster.add("PR 0,dy");
 		raster.add("BG");
 		raster.add("AM");
 
 		raster.add("n=n+1");
 		raster.add("JP #SNAKE, n<lineNum");
 
 		raster.add("EN");
 		size += raster.size();
 
 	
 	}
 	
 	public void fraster(){
 		fraster = new Script("#FRASTER", size);
 		fraster.add("lineNum=10");
 
 		fraster.add("dx = maxAz  - minAz");
 		fraster.add("dy = (maxEl-minEl)/lineNum");
 
 		fraster.add("AC 1000");
 
 		fraster.add("dx = maxAz-minAz");
 		fraster.add("vf = lineNum*(dx/(time))");
 
 		fraster.add("SP vf");
 
 		fraster.add("AC _AC");
 		fraster.add("DC _AC");
 		fraster.add("acTime = vf/_AC");
 		fraster.add("acX = .5*_AC*acTime*acTime ");
 
 		fraster.add("alt = 1");
 		fraster.add("n=0");
 		fraster.add("#ES");
 
 
 		fraster.add("IF(alt<0)");
 		fraster.add("PR dx + acX");
 		fraster.add("BG A");
 
 
 		fraster.add("AD acX ");
 		fraster.add("OP 1");
 		fraster.add("ENDIF");
 
 
 		fraster.add("IF(alt>0) ");
 		fraster.add("PR -dx-acX");
 
 		fraster.add("BG A");
 		fraster.add("AD dx");
 		fraster.add("OP 1");
 		fraster.add("ENDIF");
 		fraster.add("AM");
 
 		fraster.add("OP 0");
 
 
 		fraster.add("PR 0,dy");
 		fraster.add("BG");
 		fraster.add("AM");
 
 		fraster.add("alt=alt*-1");
 		fraster.add("n=n+1");
 		fraster.add("JP #ES, n<lineNum");
 
 
 		fraster.add("EN");
 		size += fraster.size();
 
 
 
 
 
 	}
 	private void azScan() {
 		azScan = new Script("#AZSCAN", size);	
 
 		azScan.add("scan = 0");
 
 
 
 		azScan.add("dx = maxAz-minAz");
 		azScan.add("vf = 2*dx/(time)");
 
 		azScan.add("SP vf");
 
 		azScan.add("AC _AC");
 		azScan.add("DC _AC");
 
 		azScan.add("acTime = vf/_AC");
 		azScan.add("acX = .5*_AC*acTime*acTime"); 
 
 
 		azScan.add("PR dx + acX,0");
 		azScan.add("BG ");
 
 
 		azScan.add("AD acX ");
		azScan.add("SB OP 1");
 		azScan.add("scan = 1");
 
 
 
 
 		azScan.add("AM");  
 		azScan.add("PR -dx-acX,0");
 
 		azScan.add("BG ");
 		azScan.add("AD dx");
 		azScan.add("OP 1");
 
 		azScan.add("AM");
 		azScan.add("OP 0");
 
 
 		azScan.add("EN");
 		size += azScan.size();
 
 
 
 	}
 	private void elScan() {
 		elScan = new Script("#ELSCAN", size);
 		elScan.add("scan = 0");
 
 
 
 		elScan.add("dx = maxEl-minEl");
 		elScan.add("vf = 2*dx/(time)");
 
 		elScan.add("SP ,vf");
 		elScan.add("DC _AC");
 		elScan.add("acTime = vf/_ACB");
 		elScan.add("acX = .5*_ACB*acTime*acTime ");
 
 
 		elScan.add("PR ,dx + acX");
 		elScan.add("BG B");
 
 
 
 		elScan.add("AD ,acX ");
 		elScan.add("OP 1");
 		elScan.add("scan = 1");
 
 
 
 
 		elScan.add("AM");
 		elScan.add("PR 0,-dx-acX");
 
 		elScan.add("BG B");
 		elScan.add("AD ,dx");
 		elScan.add("OP 1");
 
 		elScan.add("AM");
 		elScan.add("OP 0");
 
 
 
 
 
 
 
 		elScan.add("EN");
 		size += elScan.size();
 
 
 
 	}
 
 
 	private void readerInfo() {
 		readerInfo = new Script("#READERI", size);
 		//There is a maximum amount of data, that can be sent in one line, so if we want to add more we need another solution
 		String temp = "MG _TPA,_TVA,_JGA,_ACA,_TPB,_TVB,_JGB,_ACB,_MOA,_MOB,_BGA,_BGB,_HX0,_HX1,_HX2";
 		readerInfo.add(temp);
 		readerInfo.add("EN");
 		size += readerInfo.size();
 	}
 	
 	private void pause() {
 		try {
 			Thread.sleep(500);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 }
