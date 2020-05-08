 package ro.alexbolboaca.FindDuplicateFiles;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.*;
 import org.junit.rules.*;
 
 
 public class FindDuplicateFilesTests {
 
 	@Rule
 	public ExpectedException expectedEx = ExpectedException.none();
 
	List<String> nonExistentPaths = new ArrayList<String>();
 
 	@Before
 	public void setup(){
		nonExistentPaths.add("nonExistingPath");
		nonExistentPaths.add("anotherNonExistingPath");
 	}
 
 	
 	@Test
 	public void emptyPathHasNoDuplicates(){
 
		DuplicateSeeker seeker = new DuplicateSeeker(new Path(nonExistentPaths,"doesn't matter"));
 		
 		boolean foundDuplicates = seeker.seek();
 		
 		assertFalse(foundDuplicates);
 	}
 	
 	@Test
 	public void throwsExceptionIfPathDoesntExist(){
 		expectedEx.expect(RuntimeException.class);
 		expectedEx.expectMessage("Path nonExistingPath doesn't exist");
 
		DuplicateSeeker seeker = new DuplicateSeeker(new Path(nonExistentPaths,"nonExistingPath"));
 		
 		seeker.seek();
 	}
 	
 	@Test
 	public void throwsExceptionIfAnotherPathDoesntExist(){
 		expectedEx.expect(RuntimeException.class);
 		expectedEx.expectMessage("Path anotherNonExistingPath doesn't exist");
 		
		DuplicateSeeker seeker = new DuplicateSeeker(new Path(nonExistentPaths,"anotherNonExistingPath"));
 		
 		seeker.seek();
 	}
 	
 	class Path{
		List<String> nonExistentPaths;
 		private String path;
 		
		public Path(List<String> nonExistentPaths,String path){
			this.nonExistentPaths = nonExistentPaths;
 			this.path = path;
 		}
 		
 		public void checkExists(){
			if(nonExistentPaths.contains(path)){
 				throw new RuntimeException("Path " + path + " doesn't exist.");			
 			}
 		}
 	}
 
 }
