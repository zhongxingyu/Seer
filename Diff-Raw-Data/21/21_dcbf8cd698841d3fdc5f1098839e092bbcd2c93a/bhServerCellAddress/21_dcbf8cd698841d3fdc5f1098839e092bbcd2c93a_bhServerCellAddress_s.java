 package b33hive.server.structs;
 
 import java.io.Externalizable;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 
 import b33hive.client.app.bhE_Platform;
 import b33hive.server.data.blob.bhI_Blob;
 import b33hive.server.data.blob.bhI_BlobKey;
 import b33hive.server.data.blob.bhU_Blob;
 import b33hive.server.data.blob.bhU_Serialization;
 import b33hive.server.entities.bhS_BlobKeyPrefix;
 import b33hive.shared.json.bhI_JsonObject;
 import b33hive.shared.structs.bhCellAddress;
 import b33hive.shared.structs.bhCodePrivileges;
 import b33hive.shared.structs.bhE_CellAddressParseError;
 
 public class bhServerCellAddress extends bhCellAddress implements Externalizable, bhI_BlobKey
 {
 	private static final int EXTERNAL_VERSION = 1;
 	
 	public bhServerCellAddress(bhI_JsonObject json)
 	{
 		super(json);
 	}
 	
 	public bhServerCellAddress(String rawAddress)
 	{
 		super(rawAddress);
 	}
 
 	public bhServerCellAddress()
 	{
 		super();
 	}
 	
 	public boolean isValid()
 	{
 		return this.getParseError() == bhE_CellAddressParseError.NO_ERROR;
 	}
 	
 	public static bhServerCellAddress createInstance(String usernamePart, String cellPart)
 	{
 		String rawAddress = null;
 		if( cellPart == null )
 		{
 			rawAddress = usernamePart;
 		}
 		else
 		{
 			rawAddress = usernamePart + "/" + cellPart;
 		}
 		
 		return new bhServerCellAddress(rawAddress);
 	}
 
 	@Override
 	public String createBlobKey(bhI_Blob blob)
 	{
		return bhU_Blob.generateKey(blob, this.getCasedRawAddress());
 	}
 
 	@Override
 	public void writeExternal(ObjectOutput out) throws IOException
 	{
 		out.writeInt(EXTERNAL_VERSION);
 		
 		bhU_Serialization.writeNullableString(this.getCasedRawAddress(), out);
 	}
 
 	@Override
 	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
 	{
 		int externalVersion = in.readInt();
 		
 		String rawAddress = bhU_Serialization.readNullableString(in);
 		
 		this.init(rawAddress);
 	}
 }
