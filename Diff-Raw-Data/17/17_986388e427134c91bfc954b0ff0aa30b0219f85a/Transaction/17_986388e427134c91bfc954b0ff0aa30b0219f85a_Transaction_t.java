 package de.tomsplayground.peanuts.domain.process;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.google.common.collect.ImmutableList;
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 
 import de.tomsplayground.peanuts.domain.base.AccountManager;
 import de.tomsplayground.peanuts.domain.base.Category;
 import de.tomsplayground.peanuts.domain.beans.ObservableModelObject;
 import de.tomsplayground.util.Day;
 
 @XStreamAlias("transcation")
 public class Transaction extends ObservableModelObject implements ITransaction {
 
 	private Day day;
 	private BigDecimal amount;
 	// Optional
 	private Category category;
 	private String memo;
 	private List<Transaction> splits = new ArrayList<Transaction>();
 
 	protected transient PropertyChangeListener splitChangeListener;
 
 	public Transaction(Day day, BigDecimal amount) {
 		this(day, amount, null, null);
 	}
 
 	public Transaction(Day day, BigDecimal amount, Category category, String memo) {
 		if (day == null) {
 			throw new IllegalArgumentException("day");
 		}
 		if (amount == null) {
 			throw new IllegalArgumentException("amount");
 		}
 		this.day = day;
 		this.amount = amount;
 		this.category = category;
 		this.memo = memo;
 		initListener();
 	}
 
 	public Transaction(Transaction t) {
 		this(t.getDay(), t.getAmount(), t.getCategory(), t.getMemo());
 		for (Transaction s : t.splits) {
 			splits.add((Transaction) s.clone());
 		}
 	}
 
 	@Override
 	public Day getDay() {
 		return day;
 	}

 	public void setDay(Day day) {
 		if (day == null) {
 			throw new IllegalArgumentException("day");
 		}
 		Day oldValue = this.day;
 		this.day = day;
 		for (Transaction t : splits) {
 			t.setDay(day);
 		}
 		firePropertyChange("date", oldValue, day);
 	}
 
 	@Override
 	public BigDecimal getAmount() {
 		return amount;
 	}
 
 	public void setAmount(BigDecimal amount) {
 		if (amount == null) {
 			throw new IllegalArgumentException("amount");
 		}
 		if (! splits.isEmpty()) {
 			throw new IllegalStateException("This is a splitted transaction: setAmount() not allowed.");
 		}
 		setAmountInternal(amount);
 	}
 
 	private void setAmountInternal(BigDecimal amount) {
 		BigDecimal oldValue = this.amount;
 		this.amount = amount;
 		firePropertyChange("amount", oldValue, amount);
 	}
 
 	@Override
 	public Category getCategory() {
 		return category;
 	}
 
 	@Override
 	public void setCategory(Category category) {
 		Category oldValue = this.category;
 		this.category = category;
 		if (oldValue != null || category != null) {
 			firePropertyChange("category", oldValue, category);
 		}
 	}
 
 	@Override
 	public String getMemo() {
 		return memo;
 	}
 
 	public void setMemo(String memo) {
 		memo = StringUtils.trimToEmpty(memo);
 		String oldValue = this.memo;
 		this.memo = memo;
 		firePropertyChange("memo", oldValue, memo);
 	}
 
 	protected void setSplits(List<Transaction> splits) {
 		if (splits == null || splits.isEmpty()) {
 			this.splits.clear();
 			return;
 		}
 		this.splits.clear();
 		this.splits.addAll(splits);
 		for (Transaction split : splits) {
 			split.setDay(day);
 			split.addPropertyChangeListener("amount", splitChangeListener);
 		}
 		adjustAmount();
 	}
 
 	protected void adjustAmount() {
 		BigDecimal sum = BigDecimal.ZERO;
 		for (Transaction split : splits) {
 			sum = sum.add(split.getAmount());
 		}
 		setAmountInternal(sum);
 	}
 
 	public void addSplit(Transaction t) {
 		List<Transaction> newSplits = new ArrayList<Transaction>(splits);
 		newSplits.add(t);
 		setSplits(newSplits);
 		firePropertyChange("split", null, t);
 	}

 	public void removeSplit(Transaction t) {
 		List<Transaction> newSplits = new ArrayList<Transaction>(splits);
 		if (! newSplits.remove(t)) {
 			throw new IllegalArgumentException("Transaction " + t + " is not a split of " + this);
 		}
 		setSplits(newSplits);
 		firePropertyChange("split", t, null);
 	}
 
 	@Override
 	public ImmutableList<ITransaction> getSplits() {
 		return ImmutableList.<ITransaction>copyOf(splits);
 	}
 
 	@Override
 	public String toString() {
 		return "Transaction[" + day + ", " + amount + ", " + category + "]";
 	}
 
 	@Override
 	public Object clone() {
 		Transaction t = (Transaction) super.clone();
 		t.initListener();
 		t.splits = new ArrayList<Transaction>();
 		ArrayList<Transaction> splitClones = new ArrayList<Transaction>();
 		for (Transaction split : splits) {
 			splitClones.add((Transaction) split.clone());
 		}
 		t.setSplits(splitClones);
 		return t;
 	}
 
 	public void reconfigureAfterDeserialization(AccountManager accountManager) {
 		initListener();
 		if (category != null) {
 			this.category = accountManager.getCategoryByPath(category.getPath());
 		}
 		for (Transaction t : splits) {
 			if (t.getCategory() != null) {
 				t.setCategory(accountManager.getCategoryByPath(t.getCategory().getPath()));
 			}
			if (! t.getDay().equals(day)) {
				t.setDay(day);
			}
 			t.addPropertyChangeListener("amount", splitChangeListener);
 		}
 	}
 
 	private void initListener() {
 		splitChangeListener = new PropertyChangeListener() {
 			@Override
 			public void propertyChange(PropertyChangeEvent evt) {
 				adjustAmount();
 			}
 		};
 	}
 
 }
