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
 import swarm.server.structs.smServerCodePrivileges;
 import swarm.server.structs.smServerGridCoordinate;
 import swarm.shared.structs.smCellAddress;
 import swarm.shared.structs.smCellAddressMapping;
 import swarm.shared.structs.smCodePrivileges;
 import swarm.shared.structs.smE_CellAddressParseError;
 import swarm.shared.structs.smGridCoordinate;
 
 public class smBlobTransaction_AddCellToUser extends smA_BlobTransaction
 {
 private static final Logger s_logger = Logger.getLogger(smBlobTransaction_AddCellToUser.class.getName());
 	
 	private final smUserSession	m_session;
 	
 	private smServerUser m_user = null;
 	
 	private final smBlobTransaction_CreateCell m_createCellTransaction;
 	
 	public smBlobTransaction_AddCellToUser(smUserSession session, smServerCellAddress[] cellAddresses, smGridCoordinate preference, smServerCodePrivileges privileges, int gridExpansionDelta)
 	{
 		m_createCellTransaction = new smBlobTransaction_CreateCell(cellAddresses, preference, privileges, gridExpansionDelta);
 		
 		m_session = session;
 	}
 	
 	public smBlobTransaction_AddCellToUser(smUserSession session, smServerCellAddress[] cellAddresses, smGridCoordinate preference, int gridExpansionDelta)
 	{
 		m_createCellTransaction = new smBlobTransaction_CreateCell(cellAddresses, preference, null, gridExpansionDelta);
 		
 		m_session = session;
 	}
 
 	@Override
 	protected void performOperations() throws smBlobException
 	{
 		m_user = null;
 		
 		//--- DRK > Sanity check.
 		smServerCellAddress address = m_createCellTransaction.getAddresses()[0];
 		String usernamePart = address.getPart(smCellAddress.E_Part.USERNAME);
 		if( usernamePart == null || !m_session.getUsername().equals(usernamePart))
 		{
 			throw new smBlobException("Username doesn't match username part of cell address..." + m_session.getUsername() + ", " + usernamePart);
 		}
 		
 		//--- DRK > Another sanity check.
 		// NOTE: Allowing user part only for now.
 		/*String cellPart = address.getPart(smCellAddress.E_Part.CELL);
 		if( cellPart == null )
 		{
 			throw new smBlobException("Address doesn't have a cell part.");
 		}*/
 		
 		if( address.getParseError() != smE_CellAddressParseError.NO_ERROR )
 		{
 			throw new smBlobException("Bad address: " + address.getRawAddressLeadSlash());
 		}
 		
 		//--- DRK > Do a get that we'll use to perform some sanity checks.
 		smI_BlobManager blobManager = m_blobMngrFactory.create(smE_BlobCacheLevel.PERSISTENT);
 		HashMap<smI_BlobKey, Class<? extends smI_Blob>> existanceQuery = new HashMap<smI_BlobKey, Class<? extends smI_Blob>>();
 		existanceQuery.put(m_session, smServerUser.class);
 		existanceQuery.put(address, smServerCellAddressMapping.class);
 		Map<smI_BlobKey, smI_Blob> result = blobManager.getBlobs(existanceQuery);
 		
 		//--- DRK > Make sure user exists.
 		if( result == null )
 		{
 			throw new smBlobException("User came up null.");
 		}
 		m_user = (smServerUser) result.get(m_session);
 		if( m_user == null )
 		{
 			throw new smBlobException("User came up null.");
 		}
 		
 		//--- DRK > Make sure address isn't already registered.
 		smServerCellAddressMapping mappingResult = (smServerCellAddressMapping) result.get(address);
 		if( mappingResult != null )
 		{
 			throw new smBlobException("Address already taken.");
 		}
 		
		m_createCellTransaction.performOperations();
 		
 		//--- DRK > Add coordinate and pop user back into database.
 		smServerCellAddressMapping mapping = m_createCellTransaction.getMapping();
 		m_user.addOwnedCell(mapping);
 		blobManager.putBlob(m_session, m_user);
 	}
 	
 	public smServerUser getUser()
 	{
 		return m_user;
 	}
 
 	@Override
 	protected void onSuccess()
 	{
 		m_createCellTransaction.onSuccess();
 	}
 }
