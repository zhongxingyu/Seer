 package br.com.expense.business;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import br.com.expense.config.Configuration;
 import br.com.expense.model.Transaction;
 import br.com.expense.parser.CartaoPersonnaliteParser;
 import br.com.expense.parser.Parser;
 import br.com.expense.parser.rules.CategoryRulesEngine;
 import br.com.expense.parser.rules.CategoryRulesParser;
 import br.com.expense.service.DateTimeServiceImpl;
 import br.com.expense.util.FileUtil;
 
 public class TransactionBusinessImpl implements TransactionBusiness {
 	
 	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
 	private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("###,###,##0.00");
 
 	private List<Parser> parsers = new ArrayList<Parser>();
 	private CategoryRulesParser rulesParser;
 	
 	public TransactionBusinessImpl(List<Parser> parsers, CategoryRulesParser rulesParser) {
 		this.parsers = parsers;
 		this.rulesParser = rulesParser;
 	}
 
 	public TransactionBusinessImpl() {
 		this.rulesParser = new CategoryRulesParser();
 		this.parsers = new ArrayList<Parser>();
 		parsers.add(new CartaoPersonnaliteParser(new DateTimeServiceImpl()));
 	}	
 
 	@Override
 	public void process(String path) {
 		File file = new File(path);
 		
 		if (!file.exists()) {
 			throw new IllegalArgumentException("Target directory does not exists");
 		}
 		
		if (!file.isDirectory()) {
			throw new IllegalArgumentException("Invalid transactions directory");
		}				
		
 		if (file.exists() && file.isDirectory()) {
 			
 			CategoryRulesEngine rulesEngine = new CategoryRulesEngine(Configuration.preset(), rulesParser);
 			
 			String[] transactionFiles = file.list(new TransactionFileFilter());
 			for (String fileName : transactionFiles) {
 				String fileContent = FileUtil.loadFile(file, fileName);
 				List<Transaction> transactions = new ArrayList<Transaction>();
 				for (Parser parser : parsers) {
 					if (parser.accept(fileContent)) {
 						List<Transaction> parserTransactions = parser.parse(fileContent);
 						for (Transaction transaction : parserTransactions) {
 							transaction.setCategory(rulesEngine.getCategoryFor(transaction.getDescription()));
 						}
 						if (!parserTransactions.isEmpty()) {
 							transactions.addAll(parserTransactions);
 						}
 					}
 				}
 				
 				Collections.sort(transactions);
 				Map<String, BigDecimal> costsByCategory = new HashMap<String, BigDecimal>();
 				
 				StringBuilder expensesOutput = new StringBuilder();
 				StringBuilder expensesByCategory = new StringBuilder();
 				if (!transactions.isEmpty()) {
 					for (Transaction transaction : transactions) {
 						// write file
 						String line = DATE_FORMAT.format(transaction.getDate()) + ";" + transaction.getDescription() + ";" + transaction.getType() + ";" + NUMBER_FORMAT.format(transaction.getCurrencyInfo().getTotalValue());
 						if (transaction.getCategory() != null) {
 							line += ";" + transaction.getCategory().getName();
 						}
 						expensesOutput.append(line + "\r\n");
 						
 						String category = transaction.getCategory() != null ? transaction.getCategory().getName() : "unspecified";
 						// just in case we have same category name for debit or credit
 						category += "^" + transaction.getType();
 						
 						BigDecimal categorySum = costsByCategory.get(category);
 						if (categorySum == null) {
 							categorySum = new BigDecimal(0);
 						}
 						costsByCategory.put(category, categorySum.add(transaction.getCurrencyInfo().getTotalValue()));
 						
 					}
 					
 					for (String category : costsByCategory.keySet()) {
 						expensesByCategory.append(category.split("\\^")[0] + ";" + NUMBER_FORMAT.format(costsByCategory.get(category)) + "\r\n");
 					}
 				}
 				
 				BufferedWriter bw = null;
 				try {
 					 bw = new BufferedWriter(new FileWriter("expenses-report.csv"));
 					 bw.write(expensesByCategory.toString() + "\r\n\r\n" + expensesOutput.toString()+"\r\n");
 					 bw.write("=soma(d1:d50)");
 					 bw.close();
 				} catch (IOException e) {
 					if (bw != null) {
 						try {
 							bw.close();
 						} catch (IOException e1) {
 							e1.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 		
 	}
 	
 	private static class TransactionFileFilter implements FilenameFilter {
 
 		@Override
 		public boolean accept(File dir, String name) {
 			boolean accept = name.endsWith(".txt");
 			if (accept) {
 				System.out.println(">> Accepting " + name);
 			}
 			return accept;
 		}
 		
 	}
 
 }
