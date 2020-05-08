 package test;
 
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 import model.metafile.FileType;
 import model.realtime.RealTimeReader;
 import model.util.google.GoogleFileSearch;
 
 
 public class TestRealTime {
 	public static void main(String[] args) {
 		
 		Path dd = Paths.get("/tmp/dump");		
		RealTimeReader rtr = new RealTimeReader(dd);
 		GoogleFileSearch gfs = new GoogleFileSearch();
		rtr.process(gfs.search("iyte", FileType.PDF, 5));
 				
 	}
 }
