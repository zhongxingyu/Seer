 package uk.me.grambo.syncro;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
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
 import android.widget.RemoteViews;
 
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
 			    		RunSync( nID, sAddress, nPort );
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
 	
 	protected void RunSync(int innServerID,String insHost,int innPort) {
 		//TODO: probably want to move innServerID into a member variable or something
 		m_oProgressNotification = new ProgressNotification(this);
 		m_oProgressNotification.setShowRate(true);
 		m_oProgressNotification.update();
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Syncro");
 		 wl.acquire();
 		try {
 			//TODO: Implement wake lock sometime
 			
 			
 			Socket oSock = new Socket(insHost,innPort);
 			//TODO: if we can't connect, use the udp broadcast stuff to find the server again (if possible)
 			
 			if( DoHandshake( oSock ) ) {
 				DBHelper oHelper = new DBHelper( this );
 	        	SQLiteDatabase oDB = oHelper.getReadableDatabase();	
 	        	SQLiteStatement oInsertStatement = oDB.compileStatement("INSERT INTO folders(IDOnServer,ServerID,Name,ServerPath,LocalPath) VALUES(?," + innServerID + ",?,?,'/mnt/sdcard/Syncro/')");
 				GetFolderList(oSock,oInsertStatement);
 				GetFolderContents(oSock,innServerID,oDB);
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
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			
 			//TODO: update notification or something here?
 		} catch( Exception e ) {
 			e.printStackTrace();
 		}
 		wl.release();
 		m_oProgressNotification.stop();
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
 		//android.os.Debug.startMethodTracing("syncro");
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
 		boolean fOK = false;
 		fOK = StartDownloadingFile(inoSock,inoFileData.FolderId,inoFileData.Filename);
 		if( fOK ) {
 			FileOutputStream oFile = new FileOutputStream( insDestFilename );
 			try {
 				fOK = ReceiveFile(inoSock,oFile);
 			}catch (Exception e) {
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
 
 
 	private boolean StartDownloadingFile(Socket inoSock,int innFolderId, String insFilename) throws IOException {
 		Binarydata.BinaryDataRequest oRequest = Binarydata.BinaryDataRequest.newBuilder()
 			.setFileName(insFilename)
 			.setFolderId(innFolderId)
 			.setRecvBufferSize( inoSock.getReceiveBufferSize() )
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
 }
