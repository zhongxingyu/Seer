 package pcc.test.integration;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import pcc.vercon.ProjectVersion;
 import pcc.vercon.SourceFileRecord;
 
 public class StubVersion1 extends ProjectVersion {
 	private static final long serialVersionUID = 4623446490667911804L;
 	
 	StubFile1 stubFileA = new StubFile1();
 	StubFile2 stubFileB = new StubFile2();
 
 	public StubVersion1() throws IOException {
		super("stub", "", "", "", new ArrayList<String>());
 	}
 	
 	@Override
 	public ArrayList<SourceFileRecord> getFiles(){
 		ArrayList<SourceFileRecord> files = new ArrayList<SourceFileRecord>();
 		files.add(stubFileA);
 		files.add(stubFileB);
 		return files;
 	}
 	
 	@Override
 	public SourceFileRecord getFile(String name){
 		if(name.equals("StubFileA.txt"))
 			return stubFileA;
 		else if(name.equals("StubFileB.txt"))
 			return stubFileB;
 		else
 			return null;
 	}
 	
 	@Override
 	public String getMetaData(){
 		return "<Meta Data Version 1>";
 	}
 	
 	@Override
 	public String getNumber(){
 		return "1";
 	}
 }
