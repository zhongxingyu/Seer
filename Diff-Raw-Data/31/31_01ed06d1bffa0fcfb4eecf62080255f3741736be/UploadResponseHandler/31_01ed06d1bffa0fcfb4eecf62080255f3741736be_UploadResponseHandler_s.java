 package uk.me.grambo.syncro;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 
 import uk.me.grambo.syncro.pb.Binarydata;
 
 public class UploadResponseHandler implements PBResponseHandler {
 	
 	private int m_lastType;
 	private byte[] m_buffer; 
 	private int m_maxPacketSize;
 	private boolean m_finished;
 	
 	public UploadResponseHandler()
 	{
 		m_finished = false;
 	}
 
 	@Override
 	public boolean canHandleResponse(int innType) {
 		if( 
 				(innType == PBSocketInterface.ResponseTypes.BINARY_INCOMING_RESPONSE)
 				|| ( innType == PBSocketInterface.ResponseTypes.BINARY_INCOMING_DATA_ACK )
 				)
 		{
 			m_lastType = innType;
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean handleResponse(int[] nSubpacketSizes, InputStream inoStream)
 			throws Exception, IOException {
 		switch( m_lastType )
 		{
 		case PBSocketInterface.ResponseTypes.BINARY_INCOMING_RESPONSE:
 			return handleIncomingResponse(nSubpacketSizes,inoStream);
 		case PBSocketInterface.ResponseTypes.BINARY_INCOMING_DATA_ACK:
 			return handleIncomingDataAck(nSubpacketSizes,inoStream);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean canRemove() {
 		// TODO Auto-generated method stub
 		return m_finished;
 	}
 	
 	public void checkBuffer(int nSubpacketSize)
 	{
 		if( (m_buffer == null) || m_buffer.length < nSubpacketSize )
 		{
 			m_buffer = new byte[nSubpacketSize];
 		}
 	}
 
 	public boolean handleIncomingResponse(int[] nSubpacketSizes, InputStream inoStream) throws Exception
 	{
 		DataInputStream oInput = new DataInputStream(inoStream);
 		if( nSubpacketSizes.length != 1 )
 		{
 			throw new Exception("Invalid number of subpackets sent in binary incoming response packet");
 		}
 		checkBuffer( nSubpacketSizes[0] );
 		oInput.readFully( m_buffer, 0, nSubpacketSizes[0] );
 		
 		Binarydata.BinaryIncomingResponse.Builder oPacketInfo = Binarydata.BinaryIncomingResponse.newBuilder();
 		oPacketInfo.mergeFrom( m_buffer, 0, nSubpacketSizes[0] );
 		if( oPacketInfo.getAccepted() )
 		{
 			m_maxPacketSize = oPacketInfo.getMaxPacketSize();
 			return true;
 		}
 		m_finished = true;
 		return false;
 	}
 
 	public boolean handleIncomingDataAck(int[] nSubpacketSizes, InputStream inoStream) throws Exception
 	{
 		DataInputStream oInput = new DataInputStream(inoStream);
 		if( nSubpacketSizes.length != 1 )
 		{
 			throw new Exception("Invalid number of subpackets sent in binary incoming response packet");
 		}
 		checkBuffer( nSubpacketSizes[0] );
 		oInput.readFully( m_buffer, 0, nSubpacketSizes[0] );
 		
 		Binarydata.BinaryIncomingAck.Builder oPacketInfo = 
 			Binarydata.BinaryIncomingAck.newBuilder(); 
 		oPacketInfo.mergeFrom( m_buffer, 0, nSubpacketSizes[0] );
 		if( oPacketInfo.getOk() )
 		{
 			return true;
 		}
 		m_finished = true;
 		return false;
 	}
 
 	public int getMaxPacketSize() {
 		return m_maxPacketSize;
 	}
 }
