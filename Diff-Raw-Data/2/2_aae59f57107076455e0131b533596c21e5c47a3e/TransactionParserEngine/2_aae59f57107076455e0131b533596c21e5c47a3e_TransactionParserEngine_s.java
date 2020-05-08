 package br.com.expense.parser;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import br.com.expense.config.Configuration;
 import br.com.expense.model.Transaction;
 import br.com.expense.parser.rules.CategoryRulesEngine;
 import br.com.expense.util.FileUtil;
 
 public class TransactionParserEngine {
 	
 	private String[] transactionFiles;
 	private List<TransactionParser> transactionParsers;
 	private CategoryRulesEngine rulesEngine;
 	private Configuration configuration;
 	
 	public TransactionParserEngine(Configuration configuration, List<TransactionParser> transactionParsers, CategoryRulesEngine categoryRulesEngine) {
 		this.transactionParsers = transactionParsers;
 		this.configuration = configuration;
 		this.rulesEngine = categoryRulesEngine;
 	}
 	
 	public List<Transaction> getTransactions(File basePath) {
 		List<Transaction> transactions = new ArrayList<Transaction>();
 		this. transactionFiles = filterFiles(basePath, configuration);
 		for (String fileName : transactionFiles) {
 			String fileContent = FileUtil.loadFile(basePath, fileName);
 			boolean contentAlreadyParsed = false;
 			for (TransactionParser parser : transactionParsers) {
 				if (parser.accept(fileContent)) {
					System.out.println(">> File " + fileName + " accepted by " + parser.getName());
 					if (contentAlreadyParsed) {
 						throw new IllegalArgumentException("More then 1 parser for content:\r\n" + fileContent);
 					}
 					contentAlreadyParsed = true;
 					transactions.addAll(getParsedTransactions(fileContent, parser));
 				}
 			}
 		}
 		Collections.sort(transactions);
 		return transactions;
 	}
 	
 	private List<Transaction> getParsedTransactions(String fileContent, TransactionParser parser) {
 		List<Transaction> parserTransactions = parser.parse(fileContent);
 		for (Transaction transaction : parserTransactions) {
 			transaction.setCategory(rulesEngine.getCategoryFor(transaction.getDescription()));
 		}
 		return parserTransactions;
 	}
 	
 	private static String[] filterFiles(File baseDir, Configuration config) {
 		return baseDir.list(new TransactionFileFilter(config.getTransactionExtension()));
 	}	
 	
 	
 	private static class TransactionFileFilter implements FilenameFilter {
 		
 		private String extension;
 		
 		public TransactionFileFilter(String extension) {
 			this.extension = extension;
 		}
 
 		@Override
 		public boolean accept(File dir, String name) {
 			boolean accept = name.endsWith(extension);
 			if (accept) {
 				System.out.println(">> Filtering " + name);
 			}
 			return accept;
 		}
 		
 	}	
 
 }
