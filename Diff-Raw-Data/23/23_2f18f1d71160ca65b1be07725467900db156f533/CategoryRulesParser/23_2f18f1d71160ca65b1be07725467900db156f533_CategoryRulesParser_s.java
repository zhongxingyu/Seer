 package br.com.expense.parser.rules;
 
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 
 import br.com.expense.model.Category;
 
 public class CategoryRulesParser {
 	
 	public Map<String, Category> processRules(String rules) {
 		Map<String, Category> categories = new HashMap<String, Category>();
 		
 		Scanner scanner = new Scanner(new StringReader(rules));
 		while (scanner.hasNextLine()) {
 			String line = scanner.nextLine();
 			if (valid(line)) {
 				String[] categorizationInformation = line.split("=>");
				categories.put(categorizationInformation[0].trim(), new Category(categorizationInformation[1].trim()));			
 			}
 		}
 
 		return categories;
 	}
 	
 	private boolean blank(String line) {
 		return line == null || "".equals(line.trim());
 	}
 	
 	private boolean comment(String line) {
 		return line == null || line.startsWith("#");
 	}
 	
 	private boolean valid(String line) {
 		return !blank(line) && !comment(line);
 	}	
 
 }
