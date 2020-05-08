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
 package br.octahedron.straight.bank.manager;
 
 import java.math.BigDecimal;
 
 import br.octahedron.straight.bank.data.BankAccount;
 import br.octahedron.straight.bank.data.BankAccountDAO;
 import br.octahedron.straight.bank.data.BankTransaction;
 import br.octahedron.straight.bank.data.BankTransactionDAO;
 import br.octahedron.straight.bank.data.BankTransaction.TransactionType;
 
 /**
  * @author Erick Moreno
  *
  */
 public class AccountManager {
 	
 	private BankAccountDAO accountDAO = new BankAccountDAO();
 	private BankTransactionDAO transactionDAO = new BankTransactionDAO();
 	
 	/**
 	 * 
 	 * @param ownerId
 	 * @return
 	 */
 	public BankAccount createAccount(String ownerId){
 		BankAccount account = new BankAccount(ownerId);
 		accountDAO.save(account);
 		return account;
 	}
 	
 	/**
 	 * @param accountOrig
 	 * @param accountDest
 	 * @param value
 	 * @param comment
 	 * @param type
 	 */
 	public void transact(String accountOrig, String accountDest, BigDecimal value, String comment, TransactionType type) {
 		BankTransaction transaction = createTransaction(accountOrig, accountDest, value, comment, type);
 		BankAccount accountOrigin = accountDAO.get(accountOrig);
 		if (accountOrigin.getBalance().compareTo(value) >= 0) {
 			transactionDAO.save(transaction);
 		}
 	}
 	
 	private BankTransaction createTransaction(String accountOrig, String accountDest, BigDecimal value, String comment, TransactionType type) {
 		return new BankTransaction(accountOrig, accountDest, value, type, comment);
 	}
 
 	/*
 	 * Just for tests.
 	 */
	protected void setTransactionDAO(BankTransactionDAO transactionDAO)
	{
		
 	}
 	
 	/*
 	 * Just for tests.
 	 */
	protected void setAccountDAO(BankAccountDAO accountDAO)
	{
		
 	}
 
 	
 }
