 package org.eclipse.viatra2.emf.incquery.triggerengine.notification;
 
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.transaction.TransactionalEditingDomain.Lifecycle;
 import org.eclipse.emf.transaction.TransactionalEditingDomainEvent;
 import org.eclipse.emf.transaction.TransactionalEditingDomainListener;
 import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.emf.workspace.impl.EMFOperationTransaction;
 import org.eclipse.viatra2.emf.incquery.runtime.api.IPatternMatch;
 
 public class TransactionBasedNotificationProvider<T extends IPatternMatch> extends NotificationProvider<T> {
 
 	private TransactionListener transactionListener;
 	private TransactionalEditingDomain editingDomain;
 	private Lifecycle lifecycle;
 	
 	public TransactionBasedNotificationProvider(TransactionalEditingDomain editingDomain) {
 		super();
 		this.transactionListener = new TransactionListener();
 		this.editingDomain = editingDomain;
 		this.lifecycle = TransactionUtil.getAdapter(this.editingDomain, Lifecycle.class);
 		this.lifecycle.addTransactionalEditingDomainListener(transactionListener);
 	}
 	
 	private class TransactionListener implements TransactionalEditingDomainListener {
 
 		@Override
 		public void transactionStarting(TransactionalEditingDomainEvent event) {}
 
 		@Override
 		public void transactionInterrupted(TransactionalEditingDomainEvent event) {}
 
 		@Override
 		public void transactionStarted(TransactionalEditingDomainEvent event) {}
 
 		@Override
 		public void transactionClosing(TransactionalEditingDomainEvent event) {}
 
 		@Override
 		public void transactionClosed(TransactionalEditingDomainEvent event) {
			boolean needsNotification = true;
			
			/*Omit notifications about the executions of the assigned jobs in the RecordingActivation
			Applying a rule in the Rule Engine will result the job to be executed under an 
			EMFOperationTransaction transaction*/
			if (event.getTransaction() instanceof EMFOperationTransaction) {
				EMFOperationTransaction transaction = (EMFOperationTransaction) event.getTransaction();
				if (transaction.getCommand().getLabel().equals("RecordingActivation")) {
					needsNotification = false;
				}
			}
			
			if (needsNotification) {
				for (NotificationProviderListener<T> listener : listeners) {
					listener.notificationCallback();
				}
 			}
 		}
 
 		@Override
 		public void editingDomainDisposing(TransactionalEditingDomainEvent event) {}
 	}
 
 	@Override
 	public void dispose() {
 		this.lifecycle.removeTransactionalEditingDomainListener(transactionListener);
 		this.listeners.clear();
 	}
 }
