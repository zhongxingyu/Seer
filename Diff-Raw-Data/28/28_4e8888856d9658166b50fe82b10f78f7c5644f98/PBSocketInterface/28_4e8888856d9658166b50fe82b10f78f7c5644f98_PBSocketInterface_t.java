 /*  This file is part of Syncro. 
 	Copyright (c) Graeme Coupar <grambo@grambo.me.uk>
 
 	Syncro is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	Syncro is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with Syncro.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package uk.me.grambo.syncro;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 
 import android.util.Log;
 
 import com.google.protobuf.CodedInputStream;
 import com.google.protobuf.ExtensionRegistryLite;
 import com.google.protobuf.GeneratedMessageLite;
 import uk.me.grambo.syncro.pb.Header;
 
 public class PBSocketInterface {
 	private static final byte PB_REQUEST_FIRST_BYTE = 105;
 	private static final byte PB_RESPONSE_FIRST_BYTE = 106;
 	
 	private ArrayList<PBResponseHandler> m_aResponseHandlers;
 	
 	public static class RequestTypes {
 		public static final int BINARY_REQUEST = 1;
 		public static final int BINARY_CONTINUE = 2;
 		public static final int HANDSHAKE_REQUEST = 4;
 		public static final int BINARY_INCOMING_REQUEST = 6;
 		public static final int BINARY_INCOMING_DATA = 8;
 		public static final int FILE_HASH_REQUEST = 16;
 		public static String Str(int Type)
 		{
 			switch(Type)
 			{
 			case BINARY_REQUEST:
 				return "Binary Request";
 			case BINARY_CONTINUE:
 				return "Binary Continue";
 			case HANDSHAKE_REQUEST:
 				return "Handshake Request";
 			case BINARY_INCOMING_REQUEST:
 				return "Binary Incoming Request";
 			case BINARY_INCOMING_DATA:
 				return "Binary Incoming Data";
 			case FILE_HASH_REQUEST:
 				return "File Hash Request";
 			}
 			return "Unknown";
 		}
 	};
 	
 	public static class ResponseTypes {
 		public static final int BINARY_RESPONSE = 3;
 		public static final int HANDSHAKE_RESPONSE = 5;
 		public static final int BINARY_INCOMING_RESPONSE = 7;
 		public static final int BINARY_INCOMING_DATA_ACK = 9;
 		public static final int FILE_HASH_RESPONSE = 17;
 		public static String Str(int Type)
 		{
 			switch(Type)
 			{
 			case BINARY_RESPONSE:
 				return "Binary Resposne";
 			case HANDSHAKE_RESPONSE:
 				return "Handshake Response";
 			case BINARY_INCOMING_RESPONSE:
 				return "Binary Incoming Response";
 			case BINARY_INCOMING_DATA_ACK:
 				return "Binary Incoming Data Ack";
 			case FILE_HASH_RESPONSE:
 				return "File Hash Response";
 			}
 			return "Unknown";
 		}
 	};
 
 	public PBSocketInterface() {
 		m_aResponseHandlers = new ArrayList<PBResponseHandler>();
 	}
 	
 	public void WriteInitialHeader(OutputStream inoStream,int innHeaderSize) throws IOException {
 		DataOutputStream oOutput = new DataOutputStream(inoStream);
 		oOutput.writeByte(PB_REQUEST_FIRST_BYTE);
 		oOutput.writeInt(innHeaderSize);
 		oOutput.flush();
 	}
 	
 	public void SendMessage(OutputStream inoStream,int inType) throws IOException {
 		Header.PacketHeader oHeader = Header.PacketHeader.newBuilder()
 				.setPacketType(inType)
 				.build();
 		//Log.d("Syncro","Sending Message.  Header:\n" + oHeader.toString() );
 		//Log.d("Syncro", "Sending message: " + RequestTypes.Str(inType));
 		WriteInitialHeader(inoStream,oHeader.getSerializedSize());
 		oHeader.writeTo(inoStream);
 		inoStream.flush();
 		//Log.d("Syncro","Finished sending");
 	}
 	
 	public void SendObject(OutputStream inoStream,int inType,GeneratedMessageLite inoMessage) throws IOException {
 		//Wraps the object in a header and sends it
 		Header.PacketHeader oHeader = Header.PacketHeader.newBuilder()
 				.setPacketType(inType)
 				.addSubpacketSizes( inoMessage.getSerializedSize() )
 				.build();
 		//Log.d("Syncro","Sending Object.  Type: " + inType);
 		//Log.d("Syncro","Header:\n" + oHeader.toString() );
 		//Log.d("Syncro","Object:\n" + inoMessage.toString() );
 		//Log.d("Syncro", "Sending message: " + RequestTypes.Str(inType));
 		WriteInitialHeader(inoStream,oHeader.getSerializedSize());
 		oHeader.writeTo(inoStream);
 		inoMessage.writeTo(inoStream);
 		inoStream.flush();
		//Log.d("Syncro","Finished sending");
 	}
 	
 	public void SendObjectAndData( 
 			OutputStream inoStream, 
 			int inType, 
 			GeneratedMessageLite inoMessage, 
 			byte[] inoData, 
 			int inDataSize
 			) throws IOException
 	{
 		Header.PacketHeader oHeader = Header.PacketHeader.newBuilder()
 			.setPacketType(inType)
 			.addSubpacketSizes( inoMessage.getSerializedSize() )
 			.addSubpacketSizes( inDataSize )
 			.build();
 		
 		/*Log.d("Syncro", 
 				"Sending message: " + 
 				RequestTypes.Str(inType) + 
 				" (+" +
 				Integer.valueOf(inDataSize).toString() + 
 				" bytes)"
 				); */
 		WriteInitialHeader( inoStream, oHeader.getSerializedSize() );
 		oHeader.writeTo( inoStream );
 		inoMessage.writeTo( inoStream );
 		inoStream.write( inoData, 0, inDataSize );
 		inoStream.flush();
		//Log.d("Syncro","Finished sending");
 	}
 	
 	public void HandleResponse(InputStream inoStream) throws Exception,IOException {
 		DataInputStream oInput = new DataInputStream(inoStream);
 		byte nFirstByte = oInput.readByte();
 		if( nFirstByte != PB_RESPONSE_FIRST_BYTE )
 			throw new Exception( "Invalid first byte in PBSocketInterface::GetResponse - " + nFirstByte);
 		int nHeaderSize = oInput.readInt();
 		//Log.d("Syncro","Reading Header Size: " + nHeaderSize+"\n");
 		byte aBuffer[] = new byte[nHeaderSize];
 		oInput.read(aBuffer);
 		/*CodedInputStream oMessageInputStream = CodedInputStream.newInstance(inoStream);
 		//oMessageInputStream.setSizeLimit(nHeaderSize); */
 		Header.PacketHeader.Builder oHeaderBuilder = Header.PacketHeader.newBuilder();
 		//oHeaderBuilder.mergeFrom(oMessageInputStream);
 		//TODO: Figure out how to use CodedInputStream
 		oHeaderBuilder.mergeFrom(aBuffer);
 		//Log.d("Syncro","Received " + ResponseTypes.Str( oHeaderBuilder.getPacketType() ) );
 		//Log.d("Syncro","Recieved Header:\n" + oHeaderBuilder.toString() );
 		PBResponseHandler oSelectedHandler = null;
 		for( PBResponseHandler oHandler : m_aResponseHandlers ) {
 			if( oHandler.canHandleResponse( oHeaderBuilder.getPacketType() ) ) {
 				int nSubpacketSizes[] = new int[oHeaderBuilder.getSubpacketSizesCount()];
 				for(int nSubpacket=0; nSubpacket < oHeaderBuilder.getSubpacketSizesCount(); nSubpacket++ ) {
 					nSubpacketSizes[nSubpacket] = oHeaderBuilder.getSubpacketSizes(nSubpacket);
 				}
 				oHandler.handleResponse(nSubpacketSizes, inoStream);
 				oSelectedHandler = oHandler;
 				break;
 			}
 		}
 		if( oSelectedHandler != null ) {
 			if( oSelectedHandler.canRemove() )
 				m_aResponseHandlers.remove(oSelectedHandler);
 		} else {
 			throw new Exception("Unknown packet type sent to PBSocketInterface::GetResponse");
 		}
 	}
 	
 	public void addResponseHandler(PBResponseHandler inoHandler) {
 		m_aResponseHandlers.add(inoHandler);
 	}
 	
 	public void removeResponseHandler(PBResponseHandler inoHandler) {
 		m_aResponseHandlers.remove(inoHandler);
 	}
 	
 	public void clearAllResponseHandlers() {
 		m_aResponseHandlers.clear();
 	}
 }
