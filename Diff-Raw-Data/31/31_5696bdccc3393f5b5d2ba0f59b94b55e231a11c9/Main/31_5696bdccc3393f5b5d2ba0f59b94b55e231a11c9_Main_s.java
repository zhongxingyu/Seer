 package htmlscanner;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 
 import com.google.common.collect.Lists;
 
 import scanner.ScanResult;
 
 public class Main {
 
 	@SuppressWarnings("rawtypes")
 	public static void printMap(Map map) {
 		for (Object entObject : map.entrySet()) {
 			Entry entry = (Entry) entObject;
 			// System.out.println(String.format(
 			// "key (%s): '%s' value (%s): '%s' ", entry.getKey()
 			// .getClass(), entry.getKey(), entry.getValue()
 			// .getClass(), entry.getValue()));
 		}
 	}
 
 	public static void main(String[] args) {
 		Automaton au = new Automaton();
 		au.init(args[0]);
 		/**/
 		String input;
 		try {
 			
 			input = FileUtils.readFileToString(new File(args[1]));
 			au.setInputToScan(input);
 			Token t = null;
 			List<Token> result = Lists.newArrayList();
 			StringBuilder str = new StringBuilder();
 			while((t=au.getNextToken())!=null){
				str.append(t.prettyPrint()).append("\n");
 			}
 			
 			FileUtils.writeStringToFile(new File("salida.tok"), str.toString());
 		} catch (IOException e) {
 			System.out.print(e.getMessage());
 			System.exit(1);
 		}
 		
 		/**/
 	}
 }
