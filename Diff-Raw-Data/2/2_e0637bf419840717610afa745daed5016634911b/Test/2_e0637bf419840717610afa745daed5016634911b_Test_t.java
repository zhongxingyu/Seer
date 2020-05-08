 package test.backend;
 
import java.util.List;

 import com.google.gson.Gson;
 
 public abstract class Test {
 	protected final static Gson GSON = new Gson();
 	public abstract List<TestResult> launch(); 
 	
 	public class TestResult{
 		private Boolean result;
 		private String name;
 		
 		/**
 		 * @return the result
 		 */
 		public Boolean getResult() {
 			return result;
 		}
 		/**
 		 * @return the name
 		 */
 		public String getName() {
 			return name;
 		}
 		
 		/**
 		 * @param result
 		 * @param name
 		 */
 		public TestResult(Boolean result, String name) {
 			super();
 			this.result = result;
 			this.name = name;
 		}
 	}
 }
