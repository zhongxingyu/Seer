 package com.mangofactory.moolah;
 
import java.util.Comparator;
 import java.util.UUID;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 
 import lombok.EqualsAndHashCode;
 import lombok.Getter;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.hibernate.annotations.Immutable;
 import org.joda.money.CurrencyUnit;
 import org.joda.money.Money;
 import org.joda.time.DateTime;
 
 import com.mangofactory.json.converters.JodaMoneySerializer;
import com.mangofactory.json.converters.JodaTimeSerializer;
 import com.mangofactory.moolah.persistence.AbstractPersistentLedger;
 
 @Entity(name="LedgerPost")
@EqualsAndHashCode(of={"ledger","postId","transaction"})
 public class LedgerPost implements Comparable<LedgerPost>{
 
 	@Id @GeneratedValue(strategy=GenerationType.AUTO)
 	@Getter
 	private Long id;
 	
 	@Getter
 	private String postUid = UUID.randomUUID().toString();
 	
 	private Money value;
 	
 	@ManyToOne(targetEntity=AbstractPersistentLedger.class, cascade=CascadeType.ALL)
 	private Ledger ledger;
 
 	@ManyToOne @Immutable
 	private FinancialTransaction transaction;
 
	
 	/**
 	 * Creates a Debit posting.
 	 * Postings are considered to be debit if the value is less than 0.
 	 * 
 	 * If value has a monetary value > 0, it is negated.
 	 * Therefore, it is safe to call debitOf() and creditOf() with the same value. 
 	 */
 	public static LedgerPost debitOf(Money value,Ledger ledger)
 	{
 		if (value.isPositive())
 			value = value.negated();
 		return new LedgerPost(value, ledger);
 	}
 	public static LedgerPost creditOf(Money value,Ledger ledger)
 	{
 		return new LedgerPost(value, ledger);
 	}
 	private LedgerPost(Money value, Ledger ledger)
 	{
 		this.value = value;
 		this.ledger = ledger;
 	}
 	@SuppressWarnings("unused")
 	protected LedgerPost() {} 
 
 	@JsonIgnore
 	public Account getAccount()
 	{
 		return ledger.getAccount();
 	}
 	public boolean isDebit()
 	{
 		return value.isNegative();
 	}
 	public boolean isCredit()
 	{
 		return !isDebit();
 	}
 	public String getDescription()
 	{
 		return transaction.getDescription();
 	}
	@JsonSerialize(using=JodaTimeSerializer.class)
	public DateTime getTransactionDate()
	{
		return transaction.getTransactionDate();
	}
 	public Long getTransactionId()
 	{
 		return transaction.getTransactionId();
 	}
 	
 	
 	/**
 	 * Debits are traditionally negative in value.
 	 * This method returns the non-negative eqivalent
 	 * @return
 	 */
 	@JsonIgnore
 	public Money getNegatedDebitValue()
 	{
 		return value.negated();
 	}
 	@JsonIgnore
 	public FinancialTransaction getTransaction() {
 		return transaction;
 	}
 
 	public void setTransaction(FinancialTransaction transaction) {
 		if (this.transaction != null)
 			throw new IllegalStateException("Transaction cannot be changed once set");
 		
 		this.transaction = transaction;
 	}
 	@JsonSerialize(using=JodaMoneySerializer.class)
 	public Money getValue() {
 		return value;
 	}
 	@JsonIgnore
 	public Ledger getLedger() {
 		return ledger;
 	}
 
 	public TransactionStatus getTransactionStatus() {
 		return transaction.getStatus();
 	}
 	@JsonIgnore
 	public CurrencyUnit getCurrencyUnit() {
 		return value.getCurrencyUnit();
 	}
 	
 	public TransactionStatus hold()
 	{
 		return ledger.hold(this);
 	}
 	public void rollback()
 	{
 		ledger.rollback(this);
 	}
 	public TransactionStatus commit()
 	{
 		return ledger.commit(this);
 	}
 	public void rollbackIfPossible()
 	{
 		if (ledger.canRollback(this))
 		{
 			ledger.rollback(this);
 		}
 	}
 	@Override
 	public int compareTo(LedgerPost arg0) {
 		return getTransactionDate().compareTo(arg0.getTransactionDate()) * -1;
 	}
 }
