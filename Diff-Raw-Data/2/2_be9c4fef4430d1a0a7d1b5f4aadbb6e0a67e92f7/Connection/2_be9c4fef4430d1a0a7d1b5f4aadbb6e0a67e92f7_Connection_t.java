 package uk.me.grambo.syncro.comms;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.sqlite.SQLiteStatement;
 import android.media.MediaScannerConnection;
 import android.util.Base64;
 import android.util.Log;
 
 import uk.me.grambo.syncro.HashUtils;
 import uk.me.grambo.syncro.comms.pb.Binarydata;
 import uk.me.grambo.syncro.comms.pb.Binarydata.BinaryDataRequest.TransferDirection;
 import uk.me.grambo.syncro.comms.pb.Folders;
 import uk.me.grambo.syncro.comms.responsehandlers.FileHashResponseHandler;
 import uk.me.grambo.syncro.comms.responsehandlers.DownloadResponseHandler;
 import uk.me.grambo.syncro.comms.responsehandlers.FolderContentsResponseHandler;
 import uk.me.grambo.syncro.comms.responsehandlers.FolderListResponseHandler;
 import uk.me.grambo.syncro.comms.responsehandlers.HandshakeHandler;
 import uk.me.grambo.syncro.comms.responsehandlers.UploadResponseHandler;
 
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
 
 /**
  * @author Grambo <grambo@grambo.me.uk>
  * Top level class used for communication with syncro server.
  */
 public class Connection {
 
 	private PBSocketInterface 	m_pbInterface;
 	private Socket				m_sock;
 	private InputStream			m_inputStream;
 	private OutputStream		m_outputStream;
 	
 	//TODO: remove this if not needed
 	private Context				m_context;
 	
 	/**
 	 * Constructor for Connection object.
 	 */
 	public Connection(Context context) {
 		m_pbInterface = new PBSocketInterface();
 		m_context = context;
 	}
 	
 	public boolean Connect( ConnectionDetails details )
 	{
 		try 
 		{
 			m_sock = new Socket(
 					details.getHostname(),
 					details.getPort()
 					);
 			m_inputStream = m_sock.getInputStream();
 			m_outputStream = m_sock.getOutputStream();
 			//TODO: Start supporting credentials in here
 			DoHandshake();
 			return true;
 		}
 		catch( IOException e )
 		{
 			Log.e("Syncro","Couldn't connect to server");
 			//e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public void Disconnect()
 	{
 		try 
 		{
 			if( m_sock != null )
 			{
 				//	TODO: Notify the client we are disconnecting if possible
 				m_sock.close();
 			}
 		}
 		catch( IOException e )
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Performs a handshake with the connected server
 	 * @return True if the handshake was successful
 	 * @throws IOException
 	 */
 	protected boolean DoHandshake() throws IOException {
 		//TODO: maybe just extract all this code into the handshake handler?
 		HandshakeHandler oHandshaker = new HandshakeHandler();
 		oHandshaker.writeRequest( m_pbInterface, m_outputStream );
 		m_pbInterface.addResponseHandler( oHandshaker );
 		//TODO: add some sort of timeout to this stuff perhaps?
 		try {
 			m_pbInterface.HandleResponse( m_inputStream );
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if( oHandshaker.getHandshakeDone() )
 			return true;
 		//TODO: get the servers uuid from the response
 		return false;
 	}
 	
 	/**
 	 * Queries the server for a folder list
 	 * @param handler	The FolderListHandler to accept the data
 	 * @throws Exception
 	 */
 	public void GetFolderList( FolderListHandler handler ) throws Exception {
 		Folders.FolderListRequest request = Folders.FolderListRequest.newBuilder()
 			.setSearchString("")
 			.build();
 		
 		m_pbInterface.SendObject( 
 				m_outputStream, 
 				PBSocketInterface.RequestTypes.FOLDER_LIST_REQUEST, 
 				request
 				);
 		
 		FolderListResponseHandler responseHandler = 
 			new FolderListResponseHandler( handler );
 		
 		m_pbInterface.addResponseHandler( responseHandler );
 		m_pbInterface.HandleResponse( m_inputStream );
 	}
 	
 	/**
 	 * Gets a list of all downloadable files
 	 * @param folderId	The id of the folder to download from
 	 * @param handler	The handler to accept the list of downloadable files
 	 * @throws Exception 
 	 */
 	public void GetFolderContents( int folderId, FolderContentsHandler handler ) throws Exception
 	{
 		Folders.FolderContentsRequest request = 
 			Folders.FolderContentsRequest.newBuilder()
 				.setFolderId( folderId )
 				.build();
 		m_pbInterface.SendObject(
 				m_outputStream, 
 				PBSocketInterface.RequestTypes.FOLDER_CONTENTS_REQUEST, 
 				request
 				);
 		FolderContentsResponseHandler responseHandler = 
 			new FolderContentsResponseHandler( folderId, handler );
 		
 		m_pbInterface.addResponseHandler( responseHandler );
 		m_pbInterface.HandleResponse( m_inputStream );
 	}
 	
 	/**
 	 * Downloads/resumes a file transfer from the server
 	 * @param fileInfo			The file we want to download
 	 * @param destFilename		The destination filename
 	 * @param progressHandler	Progress handler to handle downloads
 	 * @return
 	 * @throws IOException
 	 */
 	public boolean GetFile(
 			RemoteFileData fileInfo, 
 			String destFilename,
 			ProgressHandler progressHandler
 			) throws IOException 
 	{
 		File destFile = new File( destFilename );
 		boolean canResume = false;
 		long nFileStartOffset = 0;
 		if( destFile.exists() )
 		{
 			try {
 				canResume = CheckResume( fileInfo, destFile );
 			}
 			catch( IOException e )
 			{
 				throw e;
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 			if( canResume )
 				nFileStartOffset = destFile.length();
 		}
 		//TODO: Handle initial progress handler stuff?
 		boolean fOK = false;
 		fOK = StartDownloadingFile( fileInfo, nFileStartOffset);
 		if( fOK ) {
 			FileOutputStream oFile = 
 				new FileOutputStream( destFilename, canResume );
 			try {
 				fOK = ReceiveFile( oFile, progressHandler );
 			}
 			catch( IOException e )
 			{
 				throw e;
 			}
 			catch (Exception e) 
 			{
 				e.printStackTrace();
 			}
 			oFile.close();
 			// TODO: Re-locate this code elsewhere (into progress handler presumably)
 			if( destFilename.endsWith(".mp3") )
 			{
 				//HACK: Media client any mp3s
 				//TODO: Fix this so it does things much better, rather than this crude filename hack.
 				MediaScannerConnection.scanFile(m_context, new String[]{ destFilename }, null, null);
 			}
 		}
 		return fOK;
 	}
 
 	/**
 	 * Sends the initial download file request
 	 * @param fileInfo			The file to request download of
 	 * @param innStartOffset 	The point to resume transferrign from
 	 * @return					True if there were no failures
 	 * @throws IOException
 	 */
 	private boolean StartDownloadingFile( 
 			RemoteFileData fileInfo,
 			long innStartOffset
 			) throws IOException 
 	{
 		Binarydata.BinaryDataRequest oRequest = Binarydata.BinaryDataRequest.newBuilder()
 			.setFileName( fileInfo.Filename )
 			.setFolderId( fileInfo.FolderId )
 			.setRecvBufferSize( m_sock.getReceiveBufferSize() )
 			.setDirection( TransferDirection.Download )
 			.setStartOffset( innStartOffset )
 			.build();
 		
 		m_pbInterface.SendObject(
 				m_outputStream, 
 				PBSocketInterface.RequestTypes.BINARY_REQUEST,
 				oRequest
 				);
 		
 		return true;
 	}
 	
 	/**
 	 * Performs the actual receiving of file data 
 	 * @param fileOutput		The output stream to write files to
 	 * @param progressHandler	Progress handler to handle progress updates
 	 * @return					True if there were no failures
 	 * @throws Exception
 	 */
 	private boolean ReceiveFile(
 			OutputStream fileOutput,
 			ProgressHandler progressHandler
 			) throws Exception 
 	{
 		DownloadResponseHandler responseHandler = 
 			new DownloadResponseHandler( fileOutput );
 		
 		m_pbInterface.addResponseHandler( responseHandler );
 		boolean fDone = false;
 		do {
 			m_pbInterface.HandleResponse( m_inputStream );
 			
 			if( progressHandler != null )
 				progressHandler.setCurrentProgress( 
 						responseHandler.getRecievedSize() 
 						);
 			
 			if( responseHandler.canRemove() ) {
 				if( responseHandler.getRejected() )
 				{
 					Log.w("Syncro", "Download Request Rejected.");
 				}
 				fDone = true;
 			} else {
 				m_pbInterface.SendMessage(
 						m_outputStream, 
 						PBSocketInterface.RequestTypes.BINARY_CONTINUE 
 						);
 			}
 		}while( !fDone );
 		return true;
 	}
 	
 	/**
 	 * Sends/resumes a file transfer to the server
 	 * @param folderId			The id of the folder to transfer to
 	 * @param localFilename		The name of the file to send
 	 * @param sendFilename		The name to send to the server
 	 * @param progressHandler	A handler for progress updates etc.
 	 * @return					True on success, false otherwise
 	 * @throws Exception
 	 */
 	public boolean SendFile( 
 			int folderId, 
 			String localFilename, 
 			String sendFilename,
 			ProgressHandler progressHandler
 			) throws Exception 
 	{
 		FileInputStream fileStream = 
 			new FileInputStream( localFilename );
 		
 		File localFile = new File( localFilename );
 		long totalFileSize = localFile.length();
		long modifiedTime = localFile.lastModified() / 1000;
 		if( totalFileSize > Integer.MAX_VALUE )
 		{
 			throw new Exception("File is too big to send");
 		}
 		
 		Log.i("Syncro","Sending file: " + localFilename);
 		Log.i("Syncro","File Size: " + Long.toString( totalFileSize ) );
 		
 		Binarydata.BinaryDataRequest oInitialRequest = Binarydata.BinaryDataRequest.newBuilder()
 			.setFileName( sendFilename )
 			.setFolderId( folderId )
 			.setRecvBufferSize( m_sock.getSendBufferSize() )
 			.setDirection( Binarydata.BinaryDataRequest.TransferDirection.Upload )
 			.setFileSize( (int)totalFileSize )
 			.setOneShot(false)
 			.setModifiedTime( modifiedTime )
 			.build();
 		
 		m_pbInterface.SendObject(
 				m_outputStream, 
 				PBSocketInterface.RequestTypes.BINARY_INCOMING_REQUEST, 
 				oInitialRequest);
 	
 		UploadResponseHandler responseHandler = new UploadResponseHandler();
 		byte[] sendBuffer = null;
 		
 		Binarydata.BinaryPacketHeader.SectionType sectionType = 
 			Binarydata.BinaryPacketHeader.SectionType.START;
 		long totalSizeRead = 0;
 		boolean finishedSending = false;
 		
 		boolean firstPass = true;
 		
 		if( progressHandler != null )
 			progressHandler.setTotalProgress( totalFileSize );
 		
 		m_pbInterface.addResponseHandler( responseHandler );
 		do {
 			m_pbInterface.HandleResponse( m_inputStream );
 			if( !responseHandler.canRemove() )
 			{
 				if( firstPass )
 				{
 					firstPass = false;
 					if( responseHandler.getSizeOnServer() > 0 )
 					{
 						Log.i("Syncro","File is already on server.  Checking Resume");
 						RemoteFileData fileData = new RemoteFileData();
 						fileData.Filename = sendFilename;
 						fileData.FolderId = folderId;
 						//Pretty sure size isn't used by CheckResume, but in case it starts to be in the future
 						fileData.Size = responseHandler.getSizeOnServer();
 						boolean resume = 
 							CheckResume( 
 									fileData, 
 									new File(localFilename), 
 									responseHandler.getSizeOnServer() 
 							);
 						//The server should automatically start us resuming when the hash above passes
 						if( resume )
 						{
 							totalSizeRead = responseHandler.getSizeOnServer();
 							Log.i(
 									"Syncro",
 									"Hash Check OK.  Starting send from " + 
 									Long.toString(totalSizeRead)
 									);
 							fileStream.skip( totalSizeRead );
 						}
 						else
 						{
 							Log.i( "Syncro", "Hash Check Failed.  Sending whole file" );
 						}
 					}
 				}
 				if( sendBuffer == null )
 				{
 					//TODO: The -128 bit might not be accurate/needed
 					sendBuffer = new byte[ responseHandler.getMaxPacketSize() - 128 ];
 				}
 				//Send some data
 				int nSizeRead = fileStream.read(sendBuffer);
 				if( nSizeRead != -1 )
 					totalSizeRead += nSizeRead;
 				else
 				{
 					finishedSending = true;
 				}
 				if( !finishedSending )
 				{
 					if( nSizeRead < sendBuffer.length && totalSizeRead == totalFileSize )
 					{
 						sectionType = 
 							Binarydata.BinaryPacketHeader.SectionType.END;
 					}
 					Binarydata.BinaryPacketHeader oRequest = Binarydata.BinaryPacketHeader.newBuilder()
 						.setBinaryPacketType( sectionType )
 				
 						.build();
 					m_pbInterface.SendObjectAndData(
 							m_outputStream, 
 							PBSocketInterface.RequestTypes.BINARY_INCOMING_DATA, 
 							oRequest, 
 							sendBuffer, 
 							nSizeRead
 							);
 					if( progressHandler != null )
 						progressHandler.setCurrentProgress( totalSizeRead );
 					
 					sectionType =
 						Binarydata.BinaryPacketHeader.SectionType.MIDDLE;
 				}
 			}
 			else
 			{
 				finishedSending = true;
 			}
 		} while( !finishedSending );
 		Log.i("Syncro", "Finished sending file (" + Long.toString(totalSizeRead) + "bytes)" );
 		//Need to manually remove the response handler here,
 		//as it doesn't know that we're done with the file
 		m_pbInterface.removeResponseHandler(responseHandler);
 		
 		//TODO: Check if the file transfer has succeeded or failed...
 		
 		return true;
 	}
 	
 	/**
 	 * Checks the hash of partial files, to see if it's possible to resume a download/upload
 	 * @param inoFileData	RemoteFileData with information on file on server
 	 * @param inoFile		The local file to check
 	 * @param fileLen		The size of the local/remote file we need to check
 	 * @return				True if the hash matches, false otherwise
 	 * @throws IOException
 	 * @throws Exception
 	 */
 	private boolean CheckResume( 
 			RemoteFileData inoFileData,
 			File inoFile, 
 			long fileLen 
 			) throws IOException,Exception
 	{
 		String hash; 
 		try 
 		{
 			hash = HashUtils.GetFileHash( inoFile, fileLen );
 		}
 		catch(HashUtils.HashException e)
 		{
 			Log.w(
 					"Syncro",
 					"Generating Hash Excepted in Connection::CheckResume"
 					);
 			return false;
 		}
 		
 		Binarydata.FileHashRequest request = Binarydata.FileHashRequest.newBuilder()
 			.setFileName( inoFileData.Filename )
 			.setFolderId( inoFileData.FolderId )
 			.setDataSize( fileLen )
 			.setHash( hash )
 			.build();
 		
 		m_pbInterface.SendObject( 
 				m_outputStream, 
 				PBSocketInterface.RequestTypes.FILE_HASH_REQUEST, 
 				request 
 				);
 		
 		FileHashResponseHandler response = new FileHashResponseHandler();
 		m_pbInterface.addResponseHandler(response);
 		m_pbInterface.HandleResponse( m_inputStream );
 		
 		return response.hashOk();
 	}
 	
 	/**
 	 * Checks hash of entire local file, to see if it's possible to resume a download/upload
 	 * @param inoFileData	RemoteFileData with information on file on server
 	 * @param inoFile		The local file to check
 	 * @return				True if the hash matches, false otherwise
 	 * @throws IOException
 	 * @throws Exception
 	 */
 	private boolean CheckResume( 
 			RemoteFileData inoFileData, 
 			File inoFile 
 			) throws IOException,Exception
 	{
 		return CheckResume( inoFileData, inoFile, inoFile.length() );
 	}
 
 }
