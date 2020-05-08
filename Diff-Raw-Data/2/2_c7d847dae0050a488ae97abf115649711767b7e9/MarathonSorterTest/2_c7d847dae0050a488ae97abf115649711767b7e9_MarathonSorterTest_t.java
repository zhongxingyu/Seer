 package unittest;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import enduro.MarathonSorter;
 
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 public class MarathonSorterTest {
 	private MarathonSorter sorter;
 	
 	@Before 	
 	public void setUp() {
 		sorter = new MarathonSorter();
 
 	}
 	
 	@Test public void testReadStartTimeFile() {
 		try {
 			sorter.readStartFile("fakeStart.txt");
 		} catch (Exception e) {
 			System.err.println(e);
 		}
 		
 		
 	}
 	
 	@Test public void testReadFinishTimeFile() {
 		try {
 			sorter.readFinishFile("fakeFinish.txt");
 		} catch (Exception e) {
 			System.err.println(e);
 		}
 	}
 
 	
 	@Test public void testReadingFiles() {
 		
 		try{
 			sorter.readStartFile("fakeStart.txt");
 			sorter.readFinishFile("fakeFinish.txt");
 			sorter.readNameFile("fakeName.txt");
 			sorter.createResultFile("fakesortertestresult.txt");
 			
 			ArrayList<String[]> list = new ArrayList<String[]>();
 			BufferedReader in = new BufferedReader(new FileReader("fakesortertestresult.txt"));
 			while(in.ready()){
 				list.add(in.readLine().split("; "));
 			}
 			assertEquals("1; 12.00.00", list.get(1)[0] + "; " + list.get(1)[3]);
 			assertEquals("1; 12.30.00", list.get(1)[0] + "; " + list.get(1)[4]);
 			assertEquals("1; Anders Asson", list.get(1)[0] + "; " + list.get(1)[1]);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test public void testCreateResultFile() {
 		
 		try {
 			sorter.readStartFile("fakeStart.txt");
 			sorter.readFinishFile("fakeFinish.txt");
 			sorter.readNameFile("fakeName.txt");
 			sorter.createResultFile("fakesortertestresult.txt");
 		} catch (Exception e) {
 			System.err.println(e);
 		}
 		
 		try {
 			BufferedReader in = new BufferedReader(new FileReader("fakesortertestresult.txt"));//
			assertEquals("StartNr; Namn; TotalTid; StartTider; Mltider", in.readLine());
 			assertEquals("1; Anders Asson; 00.30.00; 12.00.00; 12.30.00", in.readLine());
 			in.close();
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 }
