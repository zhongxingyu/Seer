 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.figgo.modules.bank.data;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.jdo.annotations.NotPersistent;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import br.octahedron.figgo.modules.bank.TransactionInfoService;
 import br.octahedron.util.Log;
 
 /**
  * @author Danilo Queiroz
  */
 @PersistenceCapable
 public class BankAccount implements Serializable {
 	
 	private static final Log logger = new Log(BankAccount.class);
 
 	private static final long serialVersionUID = 7892638707825018254L;
 
 	@PrimaryKey
 	@Persistent
 	private String ownerId;
 	@Persistent
 	private boolean enabled;
 	@Persistent
 	private BigDecimal value;
 	@Persistent
 	private Long lastTimestamp;
 	@Persistent
 	private Collection<Long> lastTransactionId = new ArrayList<Long>();
 	@NotPersistent
 	private transient TransactionInfoService transactionInfoService;
 
 	public BankAccount(String ownerId) {
 		this.ownerId = ownerId;
 		this.value = new BigDecimal(0);
 		this.lastTimestamp = null;
 		this.enabled = true;
 	}
 
 	public void setTransactionInfoService(TransactionInfoService tInfoService) {
 		this.transactionInfoService = tInfoService;
 	}
 
 	/**
 	 * @return the enabled
 	 */
 	public boolean isEnabled() {
 		return this.enabled;
 	}
 
 	/**
 	 * @param enabled
 	 *            the enabled to set
 	 */
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 	}
 
 	/**
 	 * @return the ownerId
 	 */
 	public String getOwnerId() {
 		return this.ownerId;
 	}
 
 	/**
 	 * @return the balance's value
 	 */
 	public BigDecimal getBalance() {
 		if (this.transactionInfoService == null) {
 			throw new IllegalStateException("TransactionInfoService cannot be null. Must be set before balance operations");
 		}
 
 		Collection<BankTransaction> transactions = this.transactionInfoService.getLastTransactions(this.ownerId, this.lastTimestamp);
 
 		if (!transactions.isEmpty()) {
 			BigDecimal transactionsBalance = new BigDecimal(0);
 
 			for (BankTransaction bankTransaction : transactions) {
 				if (!this.lastTransactionId.contains(bankTransaction.getId())) {
 					// Transaction ID not used yet.
 					if (bankTransaction.isOrigin(this.ownerId)) {
 						transactionsBalance = transactionsBalance.subtract(bankTransaction.getAmount());
 					} else {
 						transactionsBalance = transactionsBalance.add(bankTransaction.getAmount());
 					}
 					this.updateLastTransactionData(bankTransaction);
 				}
 			}
 
 			this.value = this.value.add(transactionsBalance);
 			if (value.compareTo(BigDecimal.ZERO) < 0) {
 				logger.error("Something goes wrong - The account %s has a negative balance: %s", this.ownerId, this.value);
 			}
 		}
 
 		return this.value;
 	}
 
 	/**
 	 * @param bankTransaction
 	 */
 	private void updateLastTransactionData(BankTransaction bankTransaction) {
 		long currentTransactionId = bankTransaction.getId();
 		Long currentTransactionTimestamp = bankTransaction.getTimestamp();
 		if (currentTransactionTimestamp.equals(this.lastTimestamp)) {
 			this.lastTransactionId.add(currentTransactionId);
		} if( this.lastTimestamp == null || currentTransactionTimestamp.compareTo(this.lastTimestamp) > 0 ) {
 			this.lastTransactionId.clear();
 			this.lastTransactionId.add(currentTransactionId);
 			this.lastTimestamp = currentTransactionTimestamp;
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		return this.ownerId.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof BankAccount) {
 			BankAccount other = (BankAccount) obj;
 			return this.ownerId.equals(other.ownerId);
 		} else {
 			return false;
 		}
 	}
 }
