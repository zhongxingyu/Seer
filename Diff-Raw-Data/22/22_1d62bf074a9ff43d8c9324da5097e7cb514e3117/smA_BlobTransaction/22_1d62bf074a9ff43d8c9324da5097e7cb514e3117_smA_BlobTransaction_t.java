 package swarm.server.data.blob;
 
 import java.util.ConcurrentModificationException;
 
 
 import swarm.server.entities.smServerGrid;
 import swarm.shared.app.smS_App;
 import swarm.shared.transaction.smE_ResponseError;
 
 public abstract class smA_BlobTransaction
 {
 	protected smBlobManagerFactory m_blobMngrFactory;
 	
	
 	protected abstract void performOperations() throws smBlobException;
 	
 	protected abstract void onSuccess();
 	
	protected void performNested(smA_BlobTransaction blobTxn) throws smBlobException
	{
		blobTxn.m_blobMngrFactory = m_blobMngrFactory;
		blobTxn.performOperations();
	}
	
 	public void perform(smBlobManagerFactory blobMngrFactory, smE_BlobTransactionType transactionType, int maxTryCount) throws smBlobException
 	{
 		smBlobTransactionManager blobTransactionManager = blobMngrFactory.getBlobTxnMngr();
 		
 		m_blobMngrFactory = blobMngrFactory;
 		
 		int tryCount = 0;
 		while( tryCount < maxTryCount )
 		{
 			try
 			{
 				blobTransactionManager.beginTransaction(transactionType);
 
 				this.performOperations();
 				
 				blobTransactionManager.endTransaction();
 				
 				this.onSuccess();
 				
 				break;
 			}
 			catch(ConcurrentModificationException e)
 			{ 
 				tryCount++;
 				
 				if( tryCount >= maxTryCount )
 				{
 					throw new smBlobException("Met try count for transaction.", e);
 				}
 			}
 			finally
 			{
 				blobTransactionManager.rollbackTransaction();
 			}
 		}
 	}
 }
