 package com.mangofactory.moolah;
 
 import java.util.Set;
 
 import javax.persistence.Access;
 import javax.persistence.AccessType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.OneToMany;
 import javax.persistence.PostLoad;
 import javax.persistence.Transient;
 
 import lombok.EqualsAndHashCode;
 import lombok.Getter;
 import lombok.Setter;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.hibernate.annotations.Formula;
 import org.joda.money.CurrencyUnit;
 import org.joda.money.Money;
 
 import com.mangofactory.moolah.exception.IncorrectAccountException;
 import com.mangofactory.moolah.exception.IncorrectCurrencyException;
 import com.mangofactory.moolah.util.MoneyUtils;
 
 @MappedSuperclass
 @EqualsAndHashCode(of={"account","balance"})
 public class BaseLedger implements Ledger {
 
 	@Access(AccessType.FIELD)
 	@Getter
 	private Money balance;
 	private Money heldBalance;
 
 	private CurrencyUnit currency;
 	private PostingSet postings;
 	private Account account;
 	
 	@Access(AccessType.FIELD)
 	@Formula("(select sum(post.value) from LedgerPost post where post.ledger_id = id and post.transactionStatus = 'COMPLETED')")
 	private Money calculatedBalance;
 
 	@Access(AccessType.FIELD)
 	@Formula("(select sum(post.value) from LedgerPost post where post.ledger_id = id and post.transactionStatus = 'HELD')")
 	private Money calculatedHeldBalance;
 
 	
 
 	public BaseLedger(CurrencyUnit currency,Account account) {
 		this.currency = currency;
 		this.account = account;
 		if (currency != null)
 		{
 			this.postings = new PostingSet(currency);
 			this.heldBalance = Money.zero(currency);
 			this.balance = Money.zero(currency);	
 		}
 	}
 	
 	@PostLoad
 	private void assertIsValid()
 	{
 		if (!MoneyUtils.areSame(calculatedBalance,getBalance()))
 		{
 			throw new IllegalStateException("The persisted balance of " + ObjectUtils.toString(getBalance(),"null") + " does not match the true, calculated balance of "+ ObjectUtils.toString(calculatedBalance,"null") + " for " + getAccountDescription());
 		}
 		if (!MoneyUtils.areSame(calculatedHeldBalance,getHeldBalance()))
 		{
 			throw new IllegalStateException("The persisted held balance of " + ObjectUtils.toString(getHeldBalance(),"null") + " does not match the true, calculated held balance of "+ ObjectUtils.toString(calculatedHeldBalance,"null") + " for "  + getAccountDescription());
 		}
 	}
	
 	private String getAccountDescription() {
 		return "account " + getAccount().getClass().getSimpleName() + " " + getAccount().getId(); 
 	}
 
 	public Money getHeldBalance() {
 		return heldBalance;
 	}
 	@SuppressWarnings("unused") // for JPA
 	private void setHeldBalance(Money value) {
 		this.heldBalance = value;
 	}
 	
 	@Override
 	@Transient
 	/**
 	 * Returns the available balance.
 	 * 
 	 * An available balance is calculated as the current balance,
 	 * minus any held (uncommitted) debits.
 	 * Uncommitted credits are ignored.
 	 */
 	public Money getAvailableBalance()
 	{
 		// uncommitedPostings are a negative value, so add them.
 		return getBalance().minus(getHeldBalance().abs()).plus(account.getCreditLimit());
 	}
 	@Override
 	public TransactionStatus hold(LedgerPost posting)
 	{
 		synchronized (this) {
 			assertCorrectCurrency(posting);
 			assertStatus(posting, TransactionStatus.NOT_STARTED, "Transaction has already been held, but not committed");
 			assertIsForThisLedger(posting);
 			return processHold(posting);
 		}
 	}
 	private void assertIsForThisLedger(LedgerPost posting) {
 		if (!posting.getLedger().equals(this))
 		{
 			throw new IncorrectAccountException();
 		}
 		
 	}
 
 	private void assertStatus(LedgerPost posting, TransactionStatus status, String message) {
 		if (posting.getTransaction().getStatus() != status)
 			throw new IllegalStateException(message);
 	}
 
 
 	@Override
 	public TransactionStatus commit(LedgerPost posting)
 	{
 		if (posting.getTransactionStatus().isErrorState())
 			throw new IllegalStateException("The transaction contains an error");
 		assertStatus(posting,TransactionStatus.HELD,"Transaction must be held before it is committed");
 		doInternalPost(posting);
 		assertIsForThisLedger(posting);
 		if (!postings.contains(posting))
 		{
 			throw new IllegalStateException("The transaction has already been held, but is missing from the internal posting set");
 		}
 		return TransactionStatus.COMPLETED;
 
 	}
 	private void doInternalPost(LedgerPost posting)
 	{
 		this.balance = balance.plus(posting.getValue());
 		unhold(posting);
 	}
 
 
 	private TransactionStatus processHold(LedgerPost posting) {
 		if (!hasSufficientFunds(posting))
 		{
 			return TransactionStatus.REJECTED_INSUFFICIENT_FUNDS;
 		}
 		// Don't hold credits
 		if (posting.isDebit())
 		{
 			heldBalance = heldBalance.plus(posting.getValue());
 		}
 		postings.add(posting);
 		return TransactionStatus.HELD; 
 	}
 
 
 	private void assertCorrectCurrency(LedgerPost posting) {
 		if (!posting.getCurrencyUnit().equals(getCurrency()))
 			throw new IncorrectCurrencyException(getCurrency(),posting.getCurrencyUnit());
 	}
 
 	@Override
 	public boolean hasSufficientFunds(LedgerPost posting) {
 		if (posting.isCredit())
 			return true;
 		Money value = posting.getNegatedDebitValue();
 		return getAvailableBalance().isGreaterThan(value) || getAvailableBalance().isEqual(value);
 	}
 
 
 	@Override
 	public void rollback(LedgerPost posting) {
 		if (posting.getTransactionStatus() == TransactionStatus.HELD)
 		{
 			unhold(posting);
 		} // should we do stuff with unheld?
 	}
 
 	private void unhold(LedgerPost posting) {
 		if (posting.isDebit())
 		{
 			heldBalance = heldBalance.minus(posting.getValue());
 		}
 	} 
 
 	@Transient
 	public CurrencyUnit getCurrency() {
 		return currency;
 	}
 
 	@Transient
 	@Override
 	public Account getAccount() {
 		return account;
 	}
 	protected void setAccount(Account value)
 	{
 		if (this.account != null && !this.account.equals(value))
 		{
 			throw new IllegalStateException("Cannot change account once set");
 		}
 		this.account = value;
 	}
 	protected void setCurrency(CurrencyUnit value) {
 		if (this.currency != null && !this.currency.equals(value))
 		{
 			throw new IllegalStateException("Cannot change currency once set");
 		}
 		this.currency = value;
 	}
 	
 	/**
 	 * TODO : Currently eagerly fetching transactions.
 	 * This is a bit mixed -- we need to know the transactions for doing
 	 * things like summing available balance, etc.
 	 * However, loading thousands of transactions is dumb.
 	 * Need to introduce something like a balance point, and we only
 	 * keep persistent transactions that have occurred after the last balance point.
 	 * @return
 	 */
 	@OneToMany(mappedBy="ledger")
 	protected Set<LedgerPost> getPersistentTransactions()
 	{
 		return postings.asSet();
 	}
 	protected void setPersistentTransactions(Set<LedgerPost> value)
 	{
 		postings = new PostingSet(currency,value);
 	}
 	@Transient
 	public PostingSet getPostings()
 	{
 		return postings;
 	}
 	@Transient
 	/**
 	 * {@inheritDoc}
 	 */
 	public PostingSet getPostings(TransactionStatus status)
 	{
 		return postings.inStatus(status);
 	}
 }
