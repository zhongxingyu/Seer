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
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.net.SocketException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.helpers.DefaultHandler;
 
 import uk.me.grambo.syncro.pb.Binarydata;
 
 import android.app.AlarmManager;
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteStatement;
 import android.media.MediaScannerConnection;
 import android.net.Uri;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.PowerManager;
 import android.util.Log;
 import android.util.Base64;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 public class SyncroService extends IntentService implements RemoteFileHandler{
 	
 	private static final int XML_REQUEST_FIRST_BYTE = 5;
 	private static final int XML_RESPONSE_FIRST_BYTE = 6;
 	
 	private Vector<RemoteFileHandler.RemoteFileData> m_aFilesToDownload;
 	private PBSocketInterface m_oPBInterface;
 	
 	private FilterFactory m_oFilterFactory;
 	private ArrayList<IncludeFilter> m_aIncludeFilters;
 	private ArrayList<FilenameFilter> m_aFilenameFilters;
 	
 	private ProgressNotification m_oProgressNotification;
 	
 	String m_sCurrentLocalPath;
 	
 	private String m_sServerUUID;
 
 	public SyncroService() {
 		super("SyncroService");
 		// TODO Auto-generated constructor stub
 		m_aFilesToDownload = new Vector<RemoteFileHandler.RemoteFileData>();
 		m_oPBInterface = new PBSocketInterface();
 		m_aIncludeFilters = new ArrayList<IncludeFilter>();
 		m_aFilenameFilters = new ArrayList<FilenameFilter>();
 		m_oFilterFactory = new FilterFactory(this);
 	}
 
 	@Override
 	protected void onHandleIntent(Intent arg0) {
 		if( arg0.getAction().equals("uk.me.grambo.syncro.SYNCRO_SYNC") ) {
 			
 			//Temporary hack - only run on wifi.
 			//TODO: Make the logic of this better - check user preferences etc.
 			WifiManager wifiMan = (WifiManager)getSystemService(Context.WIFI_SERVICE);
 			if( !wifiMan.isWifiEnabled() )
 			{
 				return;
 			}
 			WifiInfo wifiInfo = wifiMan.getConnectionInfo();
 			if( wifiInfo == null )
 				return;
 			String ssid = wifiInfo.getSSID(); 
 			if( ssid == null )
 				return;
 			//Otherwise, attempt to sync.
 			
 			Uri oURI = arg0.getData();
 			if( oURI != null ) {
 				String oScheme = oURI.getScheme();
 				if( oScheme.equals("syncro") ) { 
 					String sHost = oURI.getHost();
 					int nPort = oURI.getPort();
 					//RunSync(0,sHost,nPort);
 				} else if( oScheme.equals("syncroid") ) {
 			    	DBHelper oHelper = new DBHelper( this );
 			    	SQLiteDatabase oDB = oHelper.getReadableDatabase();
 			    	String aSQLArgs[] = new String[1];
 			    	aSQLArgs[0] = oURI.getHost();
 			    	Cursor oResults = oDB.rawQuery("SELECT ID,IP,Port FROM servers WHERE ID=?", aSQLArgs);
 			    	if( oResults.moveToFirst() ) {
 			    		int nID,nPort;
 			    		String sAddress;
 			    		nID = oResults.getInt(0);
 			    		sAddress = oResults.getString(1);
 			    		nPort = oResults.getInt(2);
 			    		oResults.close();
 			    		oDB.close();
 			    		boolean retry;
 			    		int retryCount = 5;
 			    		do {
 			    			retry = !RunSync( nID, sAddress, nPort );
 			    			retryCount--;
 			    			if( retry && ( retryCount > 0 ) )
 			    			{
 			    				Log.w("Syncro", "Syncro failed.  Retrying in 10 seconds");
 				    			try {
 									Thread.sleep( 10000 );
 								} catch (InterruptedException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 									retry = false;
 								}
 								Log.i("Syncro", "Retrying now...");
 			    			}else if( retry )
 			    			{
 			    				Log.e("Syncro", "Syncro failed, but ran out of retries");
 			    			}
 			    		}while( retry && ( retryCount > 0 ) );
 			    	} else {
 			    		oDB.close();
 			    	}
 				}
 			}
 		}
 	}
 	
 	protected void ProcessXML(InputStream inoData,DefaultHandler inoHandler) {
 		SAXParserFactory oFactory = SAXParserFactory.newInstance();
 		try {
 			SAXParser oParser = oFactory.newSAXParser();
 			oParser.parse(inoData,inoHandler);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	//Run sync should return false if we need to retry.
 	protected boolean RunSync(int innServerID,String insHost,int innPort) {
 		boolean retry = false;
 		boolean gotInitialConnection = false;
 		
 		//TODO: probably want to move innServerID into a member variable or something
 		m_oProgressNotification = new ProgressNotification(this);
 		m_oProgressNotification.setShowRate(true);
 
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Syncro");
 		 wl.acquire();
 		try {
 			//TODO: Implement wake lock sometime
 			
 			
 			Socket oSock = new Socket(insHost,innPort);
 			gotInitialConnection = true;
 			
 			//Start the progress notification
 			m_oProgressNotification.update();
 			if( DoHandshake( oSock ) ) {
 				DBHelper oHelper = new DBHelper( this );
 	        	SQLiteDatabase oDB = oHelper.getReadableDatabase();	
 	        	SQLiteStatement oInsertStatement = oDB.compileStatement("INSERT INTO folders(IDOnServer,ServerID,Name,ServerPath,LocalPath) VALUES(?," + innServerID + ",?,?,'/mnt/sdcard/Syncro/')");
 				GetFolderList(oSock,oInsertStatement);
 				GetFolderContents(oSock,innServerID,oDB);
 				SendFiles(oSock,innServerID,oDB);
 				oDB.close();
 				oDB = null;
 			}
 			oSock.close();
 			//
 			// TEMPORARY HACK: Start a timer to make us sync again in a bit
 			//					( but only if we've not crashed or anything )
 			//
 			//TODO: Replace this shit with some user preference controlled thing
 /*			AlarmManager alarmMan = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
 			Intent i = new Intent( this, SyncroService.class );
 			i.setAction("uk.me.grambo.syncro.SYNCRO_SYNC");
 			i.setData( Uri.parse( "syncroid://" + innServerID ) );
 			PendingIntent pendingIntent = PendingIntent.getService(this, 0, i, 0);
 			alarmMan.setInexactRepeating(
 					AlarmManager.ELAPSED_REALTIME, 
 					System.currentTimeMillis() + 60000, 
 					AlarmManager.INTERVAL_HOUR, 
 					pendingIntent
 					);*/
 		} 
 		catch ( SocketException e )
 		{
 			if( gotInitialConnection )
 				retry = true;
 			else {
 				Log.e("Syncro","Couldn't connect to server");
 				e.printStackTrace();
 			}
 			//TODO: Need to work out the difference between can't connect at all,
 			//		and have been disconnected
 		}
 		catch( EOFException e )
 		{
 			//TODO: Need to determine between network EOF and file EOF
 			if( gotInitialConnection )
 				retry = true;
 			else {
 				Log.e("Syncro","Couldn't connect to server");
 				e.printStackTrace();
 			}
 		}
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			
 			//TODO: update notification or something here?
 		} catch( Exception e ) {
 			e.printStackTrace();
 		}
 		wl.release();
 		m_oProgressNotification.stop();
 		return !retry;
 	}
 	
 	protected boolean DoHandshake(Socket inoSock) throws IOException {
 		InputStream oInput = inoSock.getInputStream();
 		OutputStream oOutput = inoSock.getOutputStream();
 		//TODO: maybe just extract all this code into the handshake handler?
 		HandshakeHandler oHandshaker = new HandshakeHandler();
 		oHandshaker.writeRequest(m_oPBInterface, oOutput);
 		m_oPBInterface.addResponseHandler(oHandshaker);
 		//TODO: add some sort of timeout to this stuff perhaps?
 		try {
 			m_oPBInterface.HandleResponse(oInput);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if( oHandshaker.getHandshakeDone() )
 			return true;
 		//TODO: get the servers uuid from the response
 		return false;
 	}
 	
 	protected boolean GetFolderList(Socket inoSock,SQLiteStatement inoInsertStatement) throws IOException {
 		DataInputStream oInput = new DataInputStream( inoSock.getInputStream() );
 		DataOutputStream oOutput = new DataOutputStream( inoSock.getOutputStream() );
 		OutputStreamWriter oWriter = new OutputStreamWriter( inoSock.getOutputStream() );
 		
 		String sRequest = "GET_FOLDER_LIST";
 		oOutput.write( XML_REQUEST_FIRST_BYTE );
 		int nSendSize = sRequest.length() + 1 + 4; 
 		oOutput.writeInt( nSendSize );
 		oOutput.flush();
 		oWriter.write( sRequest );
 		oWriter.flush();
 		int nSize;
 		if( oInput.read() != XML_RESPONSE_FIRST_BYTE ) {
 			return false;
 		}
 		nSize = oInput.readInt();
 		if( nSize > 5 ) {
 			byte aData[] = new byte[nSize - 5];
 			/*if( oInput.read(aData) != (nSize-5) ) {
 				return false;
 			}*/
 			oInput.readFully(aData);
 			Log.i("Syncro",new String(aData) );
 			ProcessXML(new ByteArrayInputStream(aData), new FolderListXMLHandler(inoInsertStatement));
 		}
 		return true;
 	}
 	
 	protected boolean GetFolderContents(Socket inoSock,int innServerID,SQLiteDatabase inoDB) throws IOException,Exception {
 		String[] aArgs = new String[1];
 		aArgs[0] = String.valueOf(innServerID);
 		Cursor oFolders = inoDB.rawQuery("SELECT IDOnServer,LocalPath FROM folders WHERE ServerID=? AND SyncToPhone=1", aArgs);
 		oFolders.moveToFirst();
 		while (oFolders.isAfterLast() == false) {
             int nFolderID = (int)oFolders.getLong(0);
             m_sCurrentLocalPath = oFolders.getString(1);
             
             m_aIncludeFilters.clear();
             m_oFilterFactory.getIncludeFilters( inoDB, nFolderID, m_aIncludeFilters);
             
             GetFolderContents(inoSock,nFolderID);
             
             if( m_aFilesToDownload.size() > 0 ) {
             	m_aFilenameFilters.clear();
             	m_oFilterFactory.getFilenameFilters(inoDB, nFolderID, m_aFilenameFilters);
             	GetFiles(inoSock);
             }
 			
 			//TODO: Tidy up excess clears sometime?
 			m_aFilesToDownload.clear();
 			m_aIncludeFilters.clear();
 			m_aFilenameFilters.clear();
 			
             oFolders.moveToNext();
         }
 		oFolders.close();
 		return true;
 	}
 	
 	protected boolean GetFolderContents(Socket inoSock,int innFolderID) throws IOException {
 		DataInputStream oInput = new DataInputStream( inoSock.getInputStream() );
 		DataOutputStream oOutput = new DataOutputStream( inoSock.getOutputStream() );
 		OutputStreamWriter oWriter = new OutputStreamWriter( inoSock.getOutputStream() );
 		
 		String sRequest = "GET_FOLDER_CONTENTS:";
 		sRequest += (new Integer(innFolderID)).toString();
 		oOutput.write( XML_REQUEST_FIRST_BYTE );
 		//TODO: find out if the numbers added in the legnth below are correct
 		int nSendSize = sRequest.length() + 1 + 4; 
 		oOutput.writeInt( nSendSize );
 		oOutput.flush();
 		oWriter.write( sRequest );
 		oWriter.flush();
 		if( oInput.read() != XML_RESPONSE_FIRST_BYTE ) {
 			return false;
 		}
 		int nSize = oInput.readInt();
 		if( nSize > 5 ) {
 			byte aData[] = new byte[nSize - 5];
 			oInput.readFully(aData); 
 			/*if( nSizeRead != (nSize-5) ) {
 				return false;
 			}*/
 			Log.i("Syncro",new String(aData) );
 			FolderContentsXMLHandler oHandler = new FolderContentsXMLHandler();
 			oHandler.AddFileHandler(this);
 			oHandler.SetFolderId( innFolderID );
 			ProcessXML(new ByteArrayInputStream(aData), oHandler );
 			Log.i("Syncro","Files To Download:");
 			for(int n=0;n < m_aFilesToDownload.size();n++) {
 				Log.i("Syncro",m_aFilesToDownload.elementAt(n).Filename );
 			}
 		}
 		return true;
 	}
 	
 	@Override
 	public void HandleRemoteFile(RemoteFileHandler.RemoteFileData inoFile) {
 		if( CheckIncludeFilters(inoFile) )
 			m_aFilesToDownload.add(inoFile);
 	}
 	
 	protected boolean CheckIncludeFilters(RemoteFileHandler.RemoteFileData inoFile) {
 		for( int n=0; n < m_aIncludeFilters.size(); n++ ) {
 			IncludeFilter oFilter = m_aIncludeFilters.get( n );
 			if( !oFilter.needsFilename() ) {
 				if( oFilter.shouldInclude(inoFile) ) {
 					return true;
 				} 
 				//TODO: probably a better way to handle this should end list stuff...
 				if( oFilter.shouldEndList() )
 					return false;
 			}
 		}
 		return false;
 	}
 	
 	protected boolean CheckIncludeFilters(RemoteFileHandler.RemoteFileData inoFile,String insDestFilename) {
 		for( int n=0; n < m_aIncludeFilters.size(); n++ ) {
 			IncludeFilter oFilter = m_aIncludeFilters.get( n );
 			if( oFilter.needsFilename() ) {
 				if( oFilter.shouldInclude(inoFile,insDestFilename) ) {
 					return true;
 				} 
 				//TODO: probably a better way to handle this should end list stuff...
 				if( oFilter.shouldEndList() )
 					return false;
 			}
 		}
 		return false;
 	}
 	
 	protected boolean GetFiles(Socket inoSock) throws Exception,IOException {
 		//android.os.Debug.startMethodTracing("syncro-download");
 		boolean fOK = false;
 		int nPrevFolderId = -1;
 		m_oProgressNotification.setTotalNumFiles( m_aFilesToDownload.size() );
 		for(int nFile = 0;nFile < m_aFilesToDownload.size();nFile++) {
 			RemoteFileHandler.RemoteFileData oFile = m_aFilesToDownload.elementAt(nFile);
 
 			if( (nPrevFolderId != -1) && (nPrevFolderId != oFile.FolderId) ) {
 				throw new Exception("GetFiles called with contents of different) folders");
 			}
 			
 			String destFilename;
 			try {
 				destFilename = GetDestinationFilename(oFile);
 			}catch(Exception e) {
 				e.printStackTrace();
 				continue;
 			}
 			
 			if( CheckIncludeFilters(oFile,destFilename) ) {
 				//TODO: To get accurate total file numbers, need to run include filters etc. before this loop
 				//			but never mind for now...
 				m_oProgressNotification.setCurrentFileDetails( oFile, nFile );
 				m_oProgressNotification.setProgress( 0 );
 				fOK = GetFile( inoSock, oFile, destFilename );
 				if( !fOK )
 					return false;
 			}
 			
 			nPrevFolderId = oFile.FolderId;
 		}
 		//android.os.Debug.stopMethodTracing();
 		return fOK;
 	}
 	
 	protected String GetDestinationFilename(RemoteFileHandler.RemoteFileData inoFile) throws Exception {
 		for( int n=0; n < m_aFilenameFilters.size(); n++ ) {
 			FilenameFilter oFilter = m_aFilenameFilters.get(n);
 			if( oFilter.canHandle(inoFile) ) {
 				return oFilter.getDestinationFilename(inoFile);
 			}
 		}
 		throw new Exception("Could not find suitable Filename filter for " + inoFile.Filename + " (FolderID: " + inoFile.FolderId + ")");
 	}
 	
 	protected boolean GetFile(Socket inoSock,RemoteFileHandler.RemoteFileData inoFileData, String insDestFilename) throws IOException {
 		File destFile = new File( insDestFilename );
 		boolean canResume = false;
 		long nFileStartOffset = 0;
 		if( destFile.exists() )
 		{
 			try {
 				canResume = CheckResume( inoSock, inoFileData, destFile );
 			}
 			catch( IOException e )
 			{
 				throw e;
 			}
 			catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			if( canResume )
 				nFileStartOffset = destFile.length();
 		}
 		
 		boolean fOK = false;
 		fOK = StartDownloadingFile(inoSock,inoFileData.FolderId,inoFileData.Filename,nFileStartOffset);
 		if( fOK ) {
 			FileOutputStream oFile = new FileOutputStream( insDestFilename, canResume );
 			try {
 				fOK = ReceiveFile(inoSock,oFile);
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
 			if( insDestFilename.endsWith(".mp3") )
 			{
 				//HACK: Media client any mp3s
 				//TODO: Fix this so it does things much better, rather than this crude filename hack.
 				MediaScannerConnection.scanFile(this, new String[]{ insDestFilename }, null, null);
 			}
 		}
 		return fOK;
 	}
 
 	private boolean StartDownloadingFile(Socket inoSock,int innFolderId, String insFilename,long innStartOffset) throws IOException {
 		Binarydata.BinaryDataRequest oRequest = Binarydata.BinaryDataRequest.newBuilder()
 			.setFileName( insFilename )
 			.setFolderId( innFolderId )
 			.setRecvBufferSize( inoSock.getReceiveBufferSize() )
 			.setStartOffset( innStartOffset )
 			.build();
 		m_oPBInterface.SendObject(inoSock.getOutputStream(), PBSocketInterface.RequestTypes.BINARY_REQUEST ,oRequest);
 		return true;
 	}
 	
 	private boolean ReceiveFile(Socket inoSock, OutputStream inoFile) throws Exception {
 		FileResponseHandler oResponseHandler = new FileResponseHandler(inoFile);
 		InputStream oInputStream = inoSock.getInputStream();
 		OutputStream oOutputStream = inoSock.getOutputStream();
 		m_oPBInterface.addResponseHandler(oResponseHandler);
 		boolean fDone = false;
 		do {
 			m_oPBInterface.HandleResponse(oInputStream);
 			m_oProgressNotification.setProgress( oResponseHandler.getRecievedSize() );
 			if( oResponseHandler.canRemove() ) {
 				fDone = true;
 			} else {
 				m_oPBInterface.SendMessage(oOutputStream, PBSocketInterface.RequestTypes.BINARY_CONTINUE );
 			}
 		}while( !fDone );
 		return true;
 	}
 	
 	private boolean CheckResume( Socket inoSock, RemoteFileHandler.RemoteFileData inoFileData, File inoFile, long fileLen ) throws IOException,Exception
 	{
 		String hash = GetFileHash( inoFile, fileLen );
 		Binarydata.FileHashRequest oRequest = Binarydata.FileHashRequest.newBuilder()
 			.setFileName( inoFileData.Filename )
 			.setFolderId( inoFileData.FolderId )
 			.setDataSize( fileLen )
 			.setHash( hash )
 			.build();
 		m_oPBInterface.SendObject( inoSock.getOutputStream(), PBSocketInterface.RequestTypes.FILE_HASH_REQUEST, oRequest );
 		
 		FileHashResponseHandler response = new FileHashResponseHandler();
 		m_oPBInterface.addResponseHandler(response);
 		m_oPBInterface.HandleResponse( inoSock.getInputStream() );
 		return response.hashOk();
 	}
 	
 	private boolean CheckResume( Socket inoSock, RemoteFileHandler.RemoteFileData inoFileData, File inoFile ) throws IOException,Exception
 	{
 		return CheckResume( inoSock, inoFileData, inoFile, inoFile.length() );
 	}
 	
 	private String GetFileHash( File inoFile, long size ) throws Exception
 	{
 		try {
 			if( size > inoFile.length() )
 			{
 				throw new Exception( "Invalid size passed to GetFileHash" );
 			}
 			MessageDigest digester;
 			try {
 				digester = MessageDigest.getInstance("SHA-1");
 			} catch (NoSuchAlgorithmException e) {
 				e.printStackTrace();
 				return "";
 			}
 			byte[] buffer = new byte[ 1024 * 8 ];
 			int read;
 			FileInputStream hashInputStream = new FileInputStream( inoFile );
 			int nextRead = buffer.length;
 			if( nextRead > size )
 				nextRead = (int)size;
 			while( ( read = hashInputStream.read( buffer, 0, nextRead ) ) > 0 )
 			{
 				size -= read;
 				digester.update(buffer, 0, read);
 				if( nextRead > size )
 					nextRead = (int)size;
 				if( size == 0 )
 					break;
 			}
 			hashInputStream.close();
 			byte[] hash = digester.digest();
 			return Base64.encodeToString(hash, Base64.DEFAULT );
 		}
 		catch(IOException e )
 		{
 			return "";
 		}
 	}
 	
 	private String GetFileHash( File inoFile ) throws Exception
 	{
 		return GetFileHash( inoFile, inoFile.length() );
 	}
 
 	
 	protected boolean SendFiles(Socket inoSock,int innServerID,SQLiteDatabase inoDB) throws IOException,Exception {
 		//android.os.Debug.startMethodTracing("syncro-upload");
 		String args[] = { Integer.valueOf(innServerID).toString() };
 		Cursor results = inoDB.rawQuery(
 				"SELECT ID,IDOnServer,LocalPath " +
 				"FROM folders WHERE SyncFromPhone=1 AND ServerID=?", 
 				args);
 		m_oProgressNotification.setTotalNumFiles(1);
 		while( results.moveToNext() )
 		{
 			String folderPath = results.getString(2);
 			File folder = new File(folderPath);
 			if( !folder.isDirectory() || !folder.canRead() )
 			{
 				continue;
 			}
 			m_sCurrentLocalPath = folder.getAbsolutePath();
 			if( !m_sCurrentLocalPath.endsWith( File.separator ) )
 				m_sCurrentLocalPath = 
 					m_sCurrentLocalPath.concat( File.separator );
 			SendFolder( inoSock, results.getInt(1), folder );
 		}
 		//android.os.Debug.stopMethodTracing();
 		return true;
 	}
 	
 	protected void SendFolder(Socket inoSock,int inFolderId,File folder) throws Exception
 	{
 		File[] files = folder.listFiles();
 		
 		for( int nFile=0; nFile < files.length; nFile++ )
 		{
 			if( files[nFile].isDirectory() )
 			{
 				SendFolder(inoSock,inFolderId,files[nFile]);
 			}
 			else
 			{
 				String sendPath = files[nFile].getAbsolutePath().substring( m_sCurrentLocalPath.length() );
 				SendFile( inoSock, files[nFile].getAbsolutePath(), sendPath, inFolderId );
 			}
 		}
 	}
 	
 	protected void SendFile(Socket inoSock, String filename, String sendFilename,int inFolderId ) throws Exception {
 		InputStream oInputStream = inoSock.getInputStream();
 		OutputStream oOutputStream = inoSock.getOutputStream();
 		
 		FileInputStream fileStream = 
 			new FileInputStream( filename );
 		
 		long totalFileSize = new File(filename).length();
 		if( totalFileSize > Integer.MAX_VALUE )
 		{
 			throw new Exception("File is too big to send");
 		}
 		
		Log.i("Syncro","Sending file: " + filename);
		Log.i("Syncro","File Size: " + Long.toString( totalFileSize ) );
		
 		Binarydata.BinaryDataRequest oInitialRequest = Binarydata.BinaryDataRequest.newBuilder()
 			.setFileName( sendFilename )
 			.setFolderId( inFolderId )
 			.setRecvBufferSize( inoSock.getSendBufferSize() )
 			.setDirection( Binarydata.BinaryDataRequest.TransferDirection.Upload )
 			.setFileSize( (int)totalFileSize )
 			.setOneShot(false)
 			
 			.build();
 		
 		m_oPBInterface.SendObject(
 				oOutputStream, 
 				PBSocketInterface.RequestTypes.BINARY_INCOMING_REQUEST, 
 				oInitialRequest);
 	
 		UploadResponseHandler responseHandler = new UploadResponseHandler();
 		byte[] sendBuffer = null;
 		
 		Binarydata.BinaryPacketHeader.SectionType sectionType = 
 			Binarydata.BinaryPacketHeader.SectionType.START;
 		long totalSizeRead = 0;
 		boolean finishedSending = false;
 		
 		boolean firstPass = true;
 		
 		//TODO: Could pass an actual file number (and total number of files)
 		//		but can't be arsed;
 		m_oProgressNotification.setCurrentFileDetails(
 				sendFilename, 
 				(int)totalFileSize, 
 				1
 				);
 		m_oPBInterface.addResponseHandler( responseHandler );
 		do {
 			m_oPBInterface.HandleResponse(oInputStream);
 			//TODO: Progress notification
 			if( !responseHandler.canRemove() )
 			{
 				if( firstPass )
 				{
 					firstPass = false;
 					if( responseHandler.getSizeOnServer() > 0 )
 					{
						Log.i("Syncro","File is already on server.  Checking Resume");
 						RemoteFileHandler.RemoteFileData fileData = new RemoteFileHandler.RemoteFileData();
 						fileData.Filename = sendFilename;
 						fileData.FolderId = inFolderId;
 						//Pretty sure size isn't used by CheckResume, but in case it starts to be in the future
 						fileData.Size = responseHandler.getSizeOnServer();
 						boolean resume = 
 							CheckResume(
 									inoSock, 
 									fileData, 
 									new File(filename), 
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
 					m_oPBInterface.SendObjectAndData(
 							oOutputStream, 
 							PBSocketInterface.RequestTypes.BINARY_INCOMING_DATA, 
 							oRequest, 
 							sendBuffer, 
 							nSizeRead
 							);
 					m_oProgressNotification.setProgress( (int)totalSizeRead );
 					
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
 		m_oPBInterface.removeResponseHandler(responseHandler);
 	}
 }
