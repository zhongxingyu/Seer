 package br.com.expense.parser.itau.pj;
 
 import static br.com.expense.model.TransactionType.CREDIT;
 import static br.com.expense.model.TransactionType.DEBIT;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import br.com.expense.model.Currency;
 import br.com.expense.model.CurrencyInfo;
 import br.com.expense.model.Transaction;
 import br.com.expense.model.TransactionType;
 import br.com.expense.parser.TransactionParser;
 import br.com.expense.util.DateTimeUtil;
 
 public class ComprovantesItauPessoaJuridicaParser implements TransactionParser {
 	
	private static Pattern HEADER = Pattern.compile("Home\\s.+\\sContas\\s.+\\sComprovantes\\s.+\\sConsultar");
 	private static Pattern KEY_LINE = Pattern.compile("^Emiss.o.+comprovantes$", Pattern.MULTILINE);
 	private static Pattern FOOTER =  Pattern.compile("Ita.\\sUnibanco.+|.+mapa.+site");
 	
 	private static Pattern TRANSACTIONS_SNIPPET = Pattern.compile("(.+?e-mail)(.+)(Ita.+)", Pattern.DOTALL);
 	private static Pattern TRANSACTION_RECORD = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})\\t(.+?)\\t(.+?)\\t.+?((\\d{1,3}\\.?)+,(\\d{2}))\\s+?(.*?)$", Pattern.MULTILINE);
 	private static Pattern DISPOSABLE_TRANSACTION_TEXT = Pattern.compile("(\t+visualizar(\t+.*)?$)", Pattern.MULTILINE);
 	
 	private static final Set<Pattern> CREDIT_TRANSACTIONS_PATTERNS = new HashSet<Pattern>();
 	
 	static {
 		CREDIT_TRANSACTIONS_PATTERNS.add(Pattern.compile("Protocolo de dep.sito"));
 	}
 	
 	@Override
 	public List<Transaction> parse(String text) {
 		List<Transaction> transactions = new ArrayList<Transaction>();
 		text = getTransactionsSnippet(text);
 		Matcher transactionRecord = TRANSACTION_RECORD.matcher(text);
 		while (transactionRecord.find()) {
 			Transaction transaction = new Transaction();
 			transaction.setDate(DateTimeUtil.parse(transactionRecord.group(1)));
 			String description = transactionRecord.group(7);
 			if ("".equals(description.trim())) {
 				description = transactionRecord.group(2);
 			}
 			transaction.setDescription(description.replaceAll("\\t", ""));
 			transaction.setType(resolveType(transactionRecord.group(2)));
 			String value = transactionRecord.group(4).trim().replaceAll("\\.", "").replaceAll("\\,", ".");
 			if (DEBIT == transaction.getType()) {
 				value = "-" + value;
 			}
 			transaction.setCurrencyInfo(new CurrencyInfo(new BigDecimal(value), Currency.REAL, new BigDecimal("1")));
 			transactions.add(transaction);
 		}
 		return transactions;
 	}
 	
 	private TransactionType resolveType(String operationTypeDescription) {
 		TransactionType type = DEBIT;
 		for(Pattern pattern : CREDIT_TRANSACTIONS_PATTERNS) {
 			Matcher matcher = pattern.matcher(operationTypeDescription);
 			if (matcher.find()) {
 				type = CREDIT;
 				break;
 			}
 		}
 		return type;
 	}
 
 	private String getTransactionsSnippet(String text) {
 		String snippet = "";
 		Matcher matcher = TRANSACTIONS_SNIPPET.matcher(text);
 		if (matcher.matches()) {
 			snippet = matcher.group(2);
 		}
 		
 		snippet = removeLinks(snippet);
 		
 		return snippet;
 	}
 
 	private String removeLinks(String snippet) {
 		StringBuffer noLinkText = new StringBuffer("");
 		Matcher matcher = DISPOSABLE_TRANSACTION_TEXT.matcher(snippet);
 		while (matcher.find()) {
 			matcher.appendReplacement(noLinkText, "");
 		}
 		matcher.appendTail(noLinkText);
 		
 		return noLinkText.toString();
 	}
 
 	@Override
 	public boolean accept(String text) {
 		return hasHeader(text) && hasKeyLine(text) && hasFooter(text);
 	}
 	
 	private boolean hasHeader(String text) {
 		return HEADER.matcher(text).find();
 	}
 	
 	private boolean hasFooter(String text) {
 		return FOOTER.matcher(text).find();
 	}
 	
 	private boolean hasKeyLine(String text) {
 		return KEY_LINE.matcher(text).find();
 	}
 
 	@Override
 	public String getName() {
 		return "Itaú pessoa jurídica receipts parser";
 	}
 
 }
