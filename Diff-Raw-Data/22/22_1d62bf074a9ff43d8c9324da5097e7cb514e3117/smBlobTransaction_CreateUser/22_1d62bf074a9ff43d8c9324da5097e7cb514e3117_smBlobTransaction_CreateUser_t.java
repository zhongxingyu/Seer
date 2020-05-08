 package swarm.server.blobxn;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import swarm.server.account.smUserSession;
 
 import swarm.server.data.blob.smA_BlobTransaction;
 import swarm.server.data.blob.smBlobException;
 import swarm.server.data.blob.smBlobManagerFactory;
 import swarm.server.data.blob.smE_BlobCacheLevel;
 import swarm.server.data.blob.smI_Blob;
 import swarm.server.data.blob.smI_BlobKey;
 import swarm.server.data.blob.smI_BlobManager;
 
 import swarm.server.entities.smServerCell;
 import swarm.server.entities.smServerGrid;
 import swarm.server.entities.smServerUser;
 import swarm.server.structs.smServerCellAddress;
 import swarm.server.structs.smServerCellAddressMapping;
 import swarm.server.structs.smServerGridCoordinate;
 import swarm.shared.structs.smCellAddress;
 import swarm.shared.structs.smCellAddressMapping;
 
 public class smBlobTransaction_CreateUser extends smA_BlobTransaction
 {
 	private static final Logger s_logger = Logger.getLogger(smBlobTransaction_CreateUser.class.getName());
 	
 	private final smUserSession	m_session;
 	
 	private smServerUser m_user = null;
 	
 	private final smBlobTransaction_CreateCell m_createCellTransaction;
 	
 	public smBlobTransaction_CreateUser(smUserSession session, boolean createHomeCell, int gridExpansionDelta)
 	{
 		smServerCellAddress[] cellAddresses = {new smServerCellAddress(session.getUsername())};
 		m_createCellTransaction = createHomeCell ? new smBlobTransaction_CreateCell(cellAddresses, null, null, gridExpansionDelta) : null;
 		
 		m_session = session;
 	}
 	
 	@Override
 	protected void performOperations() throws smBlobException
 	{
 		m_user = null;
 		smBlobTransaction_CreateCell createCellTxn = m_createCellTransaction;
 		
 		if( createCellTxn != null )
 		{
 			try
 			{
				performNested(createCellTxn);
 			}
 			catch(smBlobException e )
 			{
 				s_logger.severe("Couldn't claim cell due to: " + e);
 			}
 			
 			createCellTxn = null;
 		}
 		
 		smI_BlobManager blobManager = m_blobMngrFactory.create(smE_BlobCacheLevel.PERSISTENT);
 		
 		//--- DRK > Add a user to the database.
 		m_user = new smServerUser();
 		
 		if( createCellTxn != null )
 		{
 			smServerCellAddressMapping mapping = createCellTxn.getMapping();
 			m_user.addOwnedCell(mapping);
 		}
 		
 		blobManager.putBlob(m_session, m_user);
 	}
 	
 	public smServerUser getUser()
 	{
 		return m_user;
 	}
 
 	@Override
 	protected void onSuccess()
 	{
 		if( m_createCellTransaction != null )
 		{
 			m_createCellTransaction.onSuccess();
 		}
 	}
 	
 	
 	public boolean didGridGrow()
 	{
 		return m_createCellTransaction != null && m_createCellTransaction.didGridGrow();
 	}
 	
 	public int getGridWidth()
 	{
 		return m_createCellTransaction.getGridWidth();
 	}
 	
 	public int getGridHeight()
 	{
 		return m_createCellTransaction.getGridHeight();
 	}
 	
 	public smServerGrid getGrid()
 	{
 		return m_createCellTransaction.getGrid();
 	}
 }
