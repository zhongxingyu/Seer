 package com.mangofactory.moolah.processing;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Lists;
 import com.mangofactory.moolah.FinancialTransaction;
 import com.mangofactory.moolah.LedgerPost;
 import com.mangofactory.moolah.Transactable;
 import com.mangofactory.moolah.TransactionBuilder;
 import com.mangofactory.moolah.TransactionStatus;
 
 public class FinancialTransactionController {
 
 	private static final Logger log = LoggerFactory.getLogger(FinancialTransactionController.class);
 	/**
 	 * Commits a transaction, using two-phases - HOLD and COMMIT.
 	 * 
 	 * If the transaction has already been previously held, 
 	 * (by calling hold()) then
 	 * only a commit is performed.
 	 * 
 	 * If the transaction fails during processing, it is rolled back.
 	 * The transaction's status will reflect the reason for the failure.
 	 * 
 	 * @param transaction
 	 */
 	public TransactionStatus commit(FinancialTransaction transaction)
 	{
 		synchronized (transaction) {
 			if (transaction.getStatus().isErrorState())
 				return transaction.getStatus();
 			
 			if (transaction.getStatus().equals(TransactionStatus.NOT_STARTED))
 				hold(transaction);
 			
 			if (transaction.getStatus().isErrorState())
 				return transaction.getStatus();
 			
 			internalCommit(transaction);	
 		}
 		return transaction.getStatus();
 	}
 	public TransactionSet commit(Collection<? extends Transactable> transactables)
 	{
 		TransactionSet transactionSet = new TransactionSet();
 		for (Transactable transactable : transactables) {
 			FinancialTransaction transaction = transactable.getTransaction();
 			transactionSet.add(transaction);
 			commit(transaction);
 		}
 		return transactionSet;
 	}
 	public TransactionStatus commit(Transactable transactable)
 	{
 		return commit(transactable.getTransaction());
 	}
 	
 	public FinancialTransaction commit(TransactionBuilder builder)
 	{
 		FinancialTransaction transaction = builder.build();
 		commit(transaction);
 		return transaction;
 	}
 	public TransactionSet hold(Collection<? extends Transactable> transactables)
 	{
 		TransactionSet processed = new TransactionSet();
 		for (Transactable transactable : transactables)
 		{
 			FinancialTransaction transaction = transactable.getTransaction();
 			hold(transaction);
 			processed.add(transaction);
 			if (transaction.getStatus().isErrorState())
 			{
 				rollbackAll(processed);
 				break;
 			}
 		}
 		return processed;
 	}
 	
 	private void rollbackAll(Collection<FinancialTransaction> transactions) {
 		for (FinancialTransaction financialTransaction : transactions) {
 			rollback(financialTransaction);
 		}
 	}
 	/**
 	 * Applies the transaction to the two ledgers,
 	 * placing the funds into a 'held' state.
 	 * 
 	 * The transaction has not been committed at this stage.
 	 * 
 	 * If the transaction fails on either side (debit or credit)
 	 * it is rolled back before this method returns, and the failing
 	 * status is returned.
 	 * 
 	 */
 	public void hold(FinancialTransaction transaction) {
 		TransactionStatus transactionStatus = transaction.getStatus();
 		Exception transactionError = null;
 		for (LedgerPost posting : transaction.getLedgerPosts())
 		{
 			try
 			{
 				transactionStatus = posting.hold();
 			} catch (Exception e)
 			{
 				log.error("Error thrown when processing hold",e);
 				transactionError = e;
 				transactionStatus = TransactionStatus.INTERNAL_ERROR;
 			}
 			if (transactionStatus.isErrorState())
 			{
 				transaction.setStatus(transactionStatus);
 				if (transactionError != null)
 				{
 					transaction.setErrorMessage(transactionError.getMessage());
 				}
 				rollback(transaction);
 				break;
 			}
 		}
 		transaction.setStatus(transactionStatus);
 	}
 
 	public FinancialTransaction hold(TransactionBuilder builder) {
 		FinancialTransaction transaction = builder.build();
 		hold(transaction);
 		return transaction;
 	}
 	
 	/**
 	 * Commits a held transaction to the two ledgers.
 	 * 
 	 * If the transaction fails on either side (debit or credit)
 	 * it is rolled back before this method returns, and the failing
 	 * status is returned.
 	 * 
 	 * If the transaction has not been held before calling commit,
 	 * then an exception is thrown.
 	 * 
 	 * @param transaction
 	 */
 	private void internalCommit(FinancialTransaction transaction) {
 		if (!transaction.getStatus().equals(TransactionStatus.HELD))
 			throw new IllegalStateException("Transaction must be held before committing");
 		TransactionStatus status = transaction.getStatus();
 		for (LedgerPost posting : transaction.getLedgerPosts())
 		{
 			try
 			{
 				status = posting.commit();
 			} catch (Exception e)
 			{
 				log.error("Error thrown when processing debit commit",e);
 				status = TransactionStatus.INTERNAL_ERROR;
 			}
 			if (status.isErrorState())
 			{
 				transaction.setStatus(status);
 				rollback(transaction);
 				break;
 			}
 		}
 		transaction.setStatus(status);
 	}
 
 
 	public void rollback(FinancialTransaction transaction)
 	{
 		if (transaction.getStatus().equals(TransactionStatus.COMPLETED))
 			throw new IllegalStateException("Cannot rollback a completed transaction");
 		if (transaction.getStatus().equals(TransactionStatus.NOT_STARTED))
 			return;		
 		for (LedgerPost posting : transaction.getLedgerPosts())
 		{
 			posting.rollback();
 		}
 	}
 
 	private enum TransactionSide
 	{
 		DEBIT_ONLY,CREDIT_ONLY,BOTH;
 	}
 }
