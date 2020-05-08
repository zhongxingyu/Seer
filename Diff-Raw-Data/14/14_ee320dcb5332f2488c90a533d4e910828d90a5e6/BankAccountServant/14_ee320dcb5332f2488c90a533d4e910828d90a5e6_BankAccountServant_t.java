 package server;
 
 import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
 
 import common.BankAccount;
 import common.ExcessiveAmountException;
 import common.Money;
 import common.NegativeAmountException;
 
 
 /**
  * Class to represent a simple bank account. The state of a BankAccount
  * comprises a balance, account number, name of account holder, and a maximum
  * amount that can be withdrawn in one transaction. A BankAccount's balance is
  * not constrained - it can be in credit or debit for an arbitrary amount.
  */
public class BankAccountServant extends UnicastRemoteObject implements BankAccount {
 
 	private Money fBalance;
 
 	private String fName;
 
 	private String fNumber;
 
 	private Money fMaxWithdrawal;
 
 	/**
 	 * Creates a BankAccount instance.
 	 * 
 	 * @param initialBalance
 	 *            the starting balance for the BankAccount instance.
 	 * @param name
 	 *            the name of the BankAccount's owner.
 	 * @param number
 	 *            the unique number for the account.
 	 * @param maxWithdrawal
 	 *            the maximum amount that can be withdrawn in one operation.
 	 */
 	public BankAccountServant(Money initialBalance, String name, String number,
			Money maxWithdrawal) throws RemoteException {
 		this.fBalance = new Money(initialBalance);
 		this.fName = name;
 		this.fNumber = number;
 		this.fMaxWithdrawal = new Money(maxWithdrawal);
 	}
 
 	/**
 	 * Returns the balance of this BankAccount object.
 	 */
 	public Money getBalance() throws RemoteException {
 		/*
 		 * Return a separate copy of balance. 
 		 */
 		return new Money(fBalance);
 	}
 
 	/**
 	 * Returns the name of the account holder for this BankAccount instance.
 	 */
 	public String getName() throws RemoteException {
 		return fName;
 	}
 
 	/**
 	 * Returns the unique number of this BankAccount object.
 	 */
 	public String getNumber() throws RemoteException {
 		return fNumber;
 	}
 
 	/**
 	 * Attempts to deposit a sum of money into this BankAccount object.
 	 * 
 	 * @param amount
 	 *            the sum of money to deposit.
 	 * @throws NegativeAmountException
 	 *             if the value of the amount argument is negative. The state of
 	 *             the BankAccount object is unchanged.
 	 */
 	public void deposit(Money amount) throws NegativeAmountException {
 		/* Check that the amount to deposit is non-negative. */
 		if (amount.isNegative()) {
 			throw new NegativeAmountException();
 		}
 		fBalance.add(amount);
 	}
 
 	/**
 	 * Attempts to withdraw a sum of money from this BankAccount object.
 	 * 
 	 * @param amount
 	 *            the sum of money to withdraw.
 	 * @throws NegativeAmountException
 	 *             if the value of the amount argument is negative. The state of
 	 *             the BankAccount object is unchanged.
 	 * @throws ExcessiveAmountException
 	 *             if the amount specified exceeds the account's withdrawal
 	 *             limit. In this case, the BankAccount object's state is
 	 *             unchanged.
 	 */
 	public void withdraw(Money amount) throws 
 	NegativeAmountException, ExcessiveAmountException {
 		/* Check that the amount to withdraw is non-negative. */
 		if (amount.isNegative()) {
 			throw new NegativeAmountException();
 		}
 
 		/* Check that the amount does not exceed the maximum permitted. */
 		if (amount.compareTo(fMaxWithdrawal) > 0) {
 			throw new ExcessiveAmountException();
 		}
 
 		fBalance.subtract(amount);
 	}
 
 	/**
 	 * Returns a string representation of a BankAccount object.
 	 */
 	public String toString() {
 		StringBuffer description = new StringBuffer();
 
 		description.append("[number=");
 		description.append(fNumber);
 		description.append(';');
 		description.append("name=");
 		description.append(fName);
 		description.append(';');
 		description.append("balance=");
 		description.append(fBalance);
 		description.append(";]");
 
 		return description.toString();
 	}
 }
