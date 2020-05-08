 package com.osc.web.action.journal;
 
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.SimpleError;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 import net.sourceforge.stripes.validation.ValidationErrors;
 import net.sourceforge.stripes.validation.ValidationMethod;
 import net.sourceforge.stripes.validation.ValidationState;
 
 import org.springframework.dao.DataAccessException;
 
 import com.osc.biz.bean.journal.TransactionBean;
 import com.osc.biz.bean.ledger.EntryBean;
 import com.osc.biz.enums.EntryType;
 import com.osc.biz.enums.TransactionType;
 import com.osc.biz.svc.journal.JournalService;
 import com.osc.web.action.AccountAwareActionBean;
 
 
 public class TransactionActionBean extends AccountAwareActionBean {
 
 	@Validate(required=true, minvalue=1, maxvalue=100, on={"create", "update"})
 	private int percentage;
 	
 	@Validate(required=true, on={"create", "update"})
 	private Date date;
 	
 	@Validate(required=true, minlength=5, maxlength=128, on={"create", "update"})
 	private String description;
 	
 	@Validate(required=true, on={"create", "update"})
 	private BigDecimal beforeTax;
 	
 	private BigDecimal tax;
 	private BigDecimal finalBeforeTax;
 	private BigDecimal finalTax;
 	private BigDecimal finalAmount;
 	
 	private int transactionId;
 	private String confAction;
 	
 	@ValidateNestedProperties ({
         @Validate(field="name", required=true, on={"create", "update"})
     })
 	private List<EntryBean> entries;
 
 	
 	private JournalService journalService;
 	
 	@SpringBean("journalService")
 	public void injectJournalService(JournalService svc) {
 		journalService = svc;
 	}
 
 
 	@DefaultHandler
 	public Resolution start() {
 		percentage = 100;
 		return new ForwardResolution("/journal/transaction.jsp");
 	}
 	
 	
 	@ValidationMethod(on={"create", "update"}, when=ValidationState.NO_ERRORS) 
 	public void validateTransactionFields(ValidationErrors errors) {
 		if(entries == null || entries.size() < 2) {
 			errors.add("system", new SimpleError("You must provide at least two entries"));
 			return;
 		}
 		
 		BigDecimal totalDebit = BigDecimal.ZERO, totalCredit = BigDecimal.ZERO;
 		for(EntryBean entry : entries) {
 			if(isAccountNameValid(entry.getName())) {
 				String[] items = entry.getName().split("\\s-\\s");	
 				entry.setAccountId(Integer.parseInt(items[0]));
 			} else {
 				errors.add("system", new SimpleError("Invalid account selected: '"+entry.getName()+"'"));
 			}
 			if(entry.getDebit() != null) {
 				if(entry.getCredit() != null) {
 					errors.add("system", new SimpleError("You cannot have both debit and credit values in the same entry"));
 					break;
 				} else {
 					entry.setAmount(entry.getDebit());
 					entry.setEntryType(EntryType.DEBIT);
 					totalDebit = totalDebit.add(entry.getDebit());
 				}	
 			} else {
 				if(entry.getCredit() != null) {
 					entry.setAmount(entry.getCredit());
 					entry.setEntryType(EntryType.CREDIT);
 					totalCredit = totalCredit.add(entry.getCredit());
 				} else {
 					// entry will be ignored
 				}
 			}
 		}
 		if(totalCredit.equals(totalDebit)) {
 			if(!totalCredit.equals(finalAmount)) 
 				errors.add("system", new SimpleError("Total credits/debits must equal the total effective amount of the transaction"));
 		} else {
 			errors.add("system", new SimpleError("Total credits must equal total debits"));
 		}
 	}
 
 
 	public Resolution create() {
 		TransactionBean transaction = new TransactionBean();
 		transaction.setTransactionDate(date);
 		transaction.setPercentage(percentage);
 		transaction.setDescription(description);
 		transaction.setAmount(beforeTax);
 		transaction.setTax(tax);
 		transaction.setFinalAmount(finalBeforeTax);
 		transaction.setFinalTax(finalTax);
 		transaction.setFinalTotalAmount(finalAmount);	
 		transaction.setReadonly(false);
 		transaction.setEntries(entries);
 		transaction.setType(TransactionType.REGULAR);
 		
 		try {
 			journalService.createNewTransaction(transaction);
 			transactionId = transaction.getTransactionId();
 			confAction = "created";
 			return new ForwardResolution("/journal/transaction-conf.jsp");
 			
 	    } catch (DataAccessException e) {
 	    	return handleException(e);
 	    }
 	}
 
 
 	public Resolution edit() {
 		try {
 			TransactionBean transaction = journalService.getTransaction(transactionId);
 			
 			transactionId = transaction.getTransactionId();
 			date = transaction.getTransactionDate();
 			percentage = transaction.getPercentage();
 			description = transaction.getDescription();
 			beforeTax = transaction.getAmount();
 			tax = transaction.getTax();
 			finalBeforeTax = transaction.getFinalAmount();
 			finalTax = transaction.getFinalTax();
 			finalAmount = transaction.getFinalTotalAmount();		
 			entries = transaction.getEntries();
 			return new ForwardResolution("/journal/transaction.jsp");
 			
 	    } catch (DataAccessException e) {
 	    	return handleException(e);
 	    }
 	}
 
 
 	public Resolution delete() {
 		try {
			TransactionBean transaction = journalService.getTransaction(transactionId);
			entries = transaction.getEntries(); // retrieved for account link display in conf. screen
			
 			journalService.deleteTransaction(transactionId);
 			confAction = "deleted";
 			return new ForwardResolution("/journal/transaction-conf.jsp");
 			
 		} catch (DataAccessException e) {
 			return handleException(e);
 	    }
 	}
 
 	
 	public Resolution duplicate() {
 		transactionId = 0;
 		return new ForwardResolution("/journal/transaction.jsp");
 	}
 
 
 	public Resolution update() {
 		TransactionBean transaction = new TransactionBean();
 		transaction.setTransactionId(transactionId);
 		transaction.setTransactionDate(date);
 		transaction.setPercentage(percentage);
 		transaction.setDescription(description);
 		transaction.setAmount(beforeTax);
 		transaction.setTax(tax);
 		transaction.setFinalAmount(finalBeforeTax);
 		transaction.setFinalTax(finalTax);
 		transaction.setFinalTotalAmount(finalAmount);		
 		transaction.setEntries(entries);
 		
 		// Note: readonly and type fields are not updated.
 		
 		try {
 			journalService.updateTransaction(transaction);
 			confAction = "updated";
 			return new ForwardResolution("/journal/transaction-conf.jsp");
 			
 	    } catch (DataAccessException e) {
 	    	return handleException(e);
 	    }
 	}
 
 	
 	public int getPercentage() {
 		return percentage;
 	}
 
 
 	public void setPercentage(int percentage) {
 		this.percentage = percentage;
 	}
 
 
 	public List<EntryBean> getEntries() {
 		return entries;
 	}
 
 
 	public void setEntries(List<EntryBean> entries) {
 		this.entries = entries;
 	}
 
 
 	public Date getDate() {
 		return date;
 	}
 
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 
 	public String getDescription() {
 		return description;
 	}
 
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 
 	public BigDecimal getBeforeTax() {
 		return beforeTax;
 	}
 
 
 	public void setBeforeTax(BigDecimal beforeTax) {
 		this.beforeTax = beforeTax;
 	}
 
 
 	public BigDecimal getTax() {
 		return tax;
 	}
 
 
 	public void setTax(BigDecimal tax) {
 		this.tax = tax;
 	}
 
 
 	public BigDecimal getFinalBeforeTax() {
 		return finalBeforeTax;
 	}
 
 
 	public void setFinalBeforeTax(BigDecimal finalBeforeTax) {
 		this.finalBeforeTax = finalBeforeTax;
 	}
 
 
 	public BigDecimal getFinalTax() {
 		return finalTax;
 	}
 
 
 	public void setFinalTax(BigDecimal finalTax) {
 		this.finalTax = finalTax;
 	}
 
 
 	public BigDecimal getFinalAmount() {
 		return finalAmount;
 	}
 
 
 	public void setFinalAmount(BigDecimal finalAmount) {
 		this.finalAmount = finalAmount;
 	}
 
 
 	public int getTransactionId() {
 		return transactionId;
 	}
 
 
 	public void setTransactionId(int transactionId) {
 		this.transactionId = transactionId;
 	}
 
 
 	public String getConfAction() {
 		return confAction;
 	}
 	
 }
