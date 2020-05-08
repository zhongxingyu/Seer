 package com.bezzotech.oracleucm.arx;
 
 import intradoc.common.LocaleUtils;
 import intradoc.common.Report;
 import intradoc.common.ServiceException;
 import intradoc.data.DataBinder;
 import intradoc.data.DataException;
 import intradoc.data.DataResultSet;
 import intradoc.data.IdcCounterUtils;
 import intradoc.data.ResultSet;
 import intradoc.data.ResultSetUtils;
 import intradoc.data.Workspace;
 import intradoc.server.Service;
 import intradoc.server.ServiceHandler;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import com.bezzotech.oracleucm.arx.common.CMUtils;
 import com.bezzotech.oracleucm.arx.common.XMLUtils;
 import com.bezzotech.oracleucm.arx.service.FileStoreUtils;
 import com.bezzotech.oracleucm.arx.shared.SharedObjects;
 
 public class CoSignServiceHandler extends ServiceHandler {
 	protected static boolean m_undo;
 	protected static String m_cookie = "";
 	protected final String PLACEHOLDER_CONTENT = "__uploaded__File__Content__";
 
 	/** <span class="code">FileStoreUtils</span> object for this request. */
 	protected FileStoreUtils m_fsutil;
 
 	/** SharedObjects pointer to use for this request. */
 	public SharedObjects m_shared;
 
 	protected CMUtils m_cmutils;
 	protected XMLUtils m_xmlutils;
 	protected WSC m_WSC;
 	protected Workspace m_workspace;
 
 	/** Initialize the handler and set up the <span class="code">m_fsutil</span> object.
 	 * @param s the Service that we operate with.
 	 * @throws ServiceException if super.init() or the FileStoreUtils construction does.
 	 * @throws DataException if super.init() does.
 	 */
 	public void init( Service s ) throws ServiceException, DataException {
 		super.init( s );
 		m_undo = false;
 		m_fsutil = FileStoreUtils.getFileStoreUtils( s );
 		m_xmlutils = XMLUtils.getXMLUtils( s );
 		m_shared = SharedObjects.getSharedObjects( s );
 		m_WSC = WSC.getWSC( s );
 		m_cmutils = m_WSC.getCM();
 		m_workspace = s.getWorkspace();
 	}
 
 	/**
 	 *
 	 */
 	public void generateCoSignProfile() throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering generateCoSignProfile, passed in binder:", null );
 		String s = m_WSC.buildSigProfile( false );
 		String s1 = m_binder.getNextFileCounter() + ".xml";
 		String s2 = m_binder.getTemporaryDirectory() + s1;
 		OutputStreamWriter _out = null;
 		try {
 			_out = new OutputStreamWriter( new FileOutputStream( s2 ), "UTF8" );
 			_out.write( s );
 		}
 		catch( Exception exception ) {
 			m_service.createServiceException( null, exception.getMessage() );
 		}
 		finally {
 			try {
 				_out.close();
 			}
 			catch ( IOException e ) {
 				throwFullError( e );
 			}
 		}
 		m_binder.addTempFile( s2 );
 		m_binder.putLocal( "primaryFile", s1 );
 		m_binder.putLocal( "primaryFile:path", s2 );
 		String dRevLabel = m_binder.getLocal( "dRevLabel" );
 		if( dRevLabel == null || dRevLabel == "" )
 			dRevLabel = "0";
 		m_binder.putLocal( "dRevLabel", ( Integer.parseInt( dRevLabel ) + 1 ) + "" );
 		if( m_binder.getLocal( "IdcService" ).equals( "COSIGN_CHECKIN_NEW_PROFILE" ) )
 			m_cmutils.checkinNew();
 		else m_cmutils.checkinSel();
 
 		try {
 			Thread t = new Thread();
 			t.sleep( Long.parseLong( m_shared.getConfig( "CoSignProfileCheckinDelaySec") ) * 1000 );
 		}
 		catch ( Exception ignore ) {}
 	}
 
 	/**
 	 *
 		*/
 	public void validateUniqueProfile() throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering validateUniqueProfile, passed in binder:", null );
 		DataBinder binder = new DataBinder();
 		binder.putLocal( "xCoSignSignatureTag", m_binder.getLocal( "xCoSignSignatureTag" ) );
 		binder.putLocal( "xCoSignRequiredSignatures", m_binder.getLocal( "xCoSignRequiredSignatures" ) );
 		ResultSet rset = m_cmutils.createResultSet( "QcosignUniqueProfile", binder );
 		if( rset.isRowPresent() )
 			throw new ServiceException( "A profile with this criteria already exists in the system, " +
 					rset.getStringValueByName( "dDocTitle" ) + "(" + rset.getStringValueByName( "dDocName" ) + ")" );
 	}
 
 	/**
 	 *
 	 */
 	public void processSignRequest() throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering processSignRequest, passed in binder:", null );
 		m_binder.putLocal( "xSignatureStatus", "sent-to-cosign" );
 		m_cmutils.update();
 		m_cmutils.checkout();
 
 		m_binder.putLocal( "CoSign.Document.fields", "fileID;contentType;content" );
 		m_binder.putLocal( "CoSign.Document.fileID", m_binder.getLocal( "dDocName" ) );
 		m_binder.putLocal( "docId", m_binder.getLocal( "dID" ) );
 		ResultSet rset = m_cmutils.getDocInfo( m_binder.getLocal( "docId" ) );
 		m_binder.putLocal( "CoSign.Document.contentType", ResultSetUtils.getValue( rset, "dExtension" ) );
 		m_binder.putLocal( "CoSignProfile",
 				ResultSetUtils.getValue( rset, m_shared.getConfig( "coSignSignatureProfileMetaField" ) ) );
 		DataResultSet drset = new DataResultSet();
 		drset.copy( rset );
 		m_binder.addResultSet( "DOC_INFO", drset );
 		Report.debug( "bezzotechcosign", "Required metadata for Signing Ceremony have been gathered from" +
 				" content item, resulting binder: ", null );
 
 		m_binder.putLocal( "CoSign.Document.content", PLACEHOLDER_CONTENT );
 		String SignRequest = m_WSC.buildSignRequest();
 
 		String msg = "";
 		int response = 0;
 		try {
 			String file = m_cmutils.getFileAsString();
 			String input = SignRequest.replaceAll( PLACEHOLDER_CONTENT, file );
 			String output = URLEncoder.encode( input, "UTF-8" );
 			SignRequest = "inputXML=" + output;
 			m_binder.putLocal( "SignRequest", SignRequest );
 			response = m_WSC.processSignRequest();
 		}
 		catch ( UnsupportedEncodingException e ) {
 			e.printStackTrace();
 			msg += e.getMessage();
 			m_undo = true;
 		}
 		catch ( Exception e ) {
 			e.printStackTrace();
 			msg += e.getMessage();
 			m_undo = true;
 		}
 		finally {
 			if( m_binder.getLocal( "WSC_Session" ) == null ) {
 				msg += "\n\tCosign returned without a valid session found";
 				m_undo = true;
 			}
 		}
 
 		logHistory( msg );
 
 		if( m_undo ) {
 			m_cmutils.rollback( msg, false );
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void processSignedDocument() throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering processSignedDocument, passed in binder: ", null );
 		Report.debug( "bezzotechcosign", "HTTP_REFERER: " + this.m_binder.getEnvironmentValue("HTTP_REFERER"), null );
 		String msg = "";
 		int response = 0;
 		boolean canceled = false;
 		if( m_binder.getLocal( "errorMessage" ) != null ) {
 			response = Integer.parseInt( m_binder.getLocal( "returnCode" ) );
 			if( response == WSC.USER_OPER_CANCELLED ) {
 				String redirectURL = "<$ HttpCgiPath $>?IdcService=DOC_INFO_BY_NAME" +
 						"&RevisionSelectionMethod=LatestReleased&dDocName=" + m_binder.getLocal( "docId" );
 				m_binder.putLocal( "RedirectUrl", redirectURL );
 				canceled = true;
 			}
 			else {
 				msg = m_binder.getLocal( "errorMessage" );
 				m_undo = true;
 			}
 		}
 		m_binder.putLocal( "dDocName", m_binder.getLocal( "docId" ) );
 		m_binder.putLocal( "dID", "" );
 		if( m_cmutils.itemExistsInCoSignCacheTable( m_binder.getLocal( "docId" ) ) ) {
 			if( !( m_undo || canceled ) && m_binder.getLocal( "sessionId" ) == null ) {
 				msg = "csInvalidSessionId";
 				m_undo = true;
 			}
 			if( !( m_undo || canceled ) && m_binder.getLocal( "docId" ) == null ) {
 				msg = "csInvalidDocId";
 				m_undo = true;
 			}
 			try {
 				if( !( m_undo || canceled ) ) response = m_WSC.processDownloadRequest();
 			}
 			catch ( Exception e ) {
 				msg += e.getMessage();
 				m_undo = true;
 			}
 			finally {
 				if( response == WSC.USER_OPER_CANCELLED ) canceled = true;
 			}
 
 			logHistory( msg );
 
 			DataResultSet drset = new DataResultSet();
 			if( !( m_undo || canceled ) ) {
 				DataBinder qApproveBinder = new DataBinder();
 				qApproveBinder.putLocal( "dDocName", m_binder.getLocal( "dDocName" ) );
 				ResultSet rset = m_cmutils.createResultSet( "QwfDocInformation", qApproveBinder );
 				drset.copy( rset );
 				Report.debug( "bezzotechcosign", "Resulting Rset: " + drset.toString(), null );
 				String stepType = null;
 				if( !drset.isEmpty() ) {
 					stepType = drset.getStringValueByName( "dWfStepType" );
 					if( stepType.indexOf( ":CN:" ) >= 0 ) // allow New Revision
 						m_binder.putLocal( "dRevLabel",
 								( Integer.parseInt( m_binder.getLocal( "dRevLabel" ) ) + 1 ) + "" );
 					else if( stepType.indexOf( ":CE:" ) >= 0 ) {} // allow Edit Revision ZKG: Maybe build in Major/Minor revisioning
 				}
 				else
 					m_binder.putLocal( "dRevLabel",
 							( Integer.parseInt( m_binder.getLocal( "dRevLabel" ) ) + 1 ) + "" );
 				m_cmutils.checkinSel();
 				m_WSC.log();
 
 				if( !drset.isEmpty() )
 					m_cmutils.approve();
 				m_cmutils.removeItemFromCoSignCacheTable( m_binder.getLocal( "dDocName" ) );
 			}
 			else {
 				m_binder.putLocal( "hideErrorBackButton", "1" );
 				m_cmutils.rollback( msg, canceled );
 			}
 		}
 		else {
 			Report.trace( "bezzotechcosign", "Content item " + m_binder.getLocal( "docId" ) + " was already" +
 					" checked in/rolled back, this was likely the result of a 'Trays' orientation\n", null );
 			m_binder.putLocal( "hideErrorBackButton", "1" );
 			if( !canceled ) {
 				if( msg.equals( "" ) )
 					msg = "csCoSignSessionTimeOut";
 				msg = LocaleUtils.encodeMessage( msg, null );
 				throw new ServiceException( msg );
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void processReviewRequest() throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering processReviewRequest, passed in binder:", null );
 		ResultSet rset = m_cmutils.getSignatureReview( m_binder.getLocal( "dID" ) );
 		if( rset.isEmpty() ) {
 			ResultSet diRSet = m_cmutils.getDocInfo( m_binder.getLocal( "dID" ) );
 			m_binder.putLocal( "dDocName", ResultSetUtils.getValue( diRSet, "dDocName" ) );
 			DataResultSet drset = new DataResultSet();
 			drset.copy( diRSet );
 			m_binder.addResultSet( "DOC_INFO", drset );
 			m_binder.m_inStream = m_cmutils.getFileAsStream();
 
 			String msg = "";
 			boolean term = false;
 			int response = 0;
 			try {
 				response = m_WSC.processVerifyRequest();
 			}
 			catch( Exception e) {
 				Report.debug( "bezzotechcosign", "Returned from WSC on an error: " + e.getMessage(), null );
 				msg = e.getMessage();
 				term = true;
 			}
 			logHistory( msg );
 			if( term )
 				throw new ServiceException( msg );
 
 			rset = m_cmutils.getSignatureReview( m_binder.getLocal( "dID" ) );
 		}
 		DataResultSet drset = new DataResultSet();
 		drset.copy( rset );
 		m_binder.addResultSet( "SignatureReview", drset );
 	}
 
 	/**
 	 *
 	 */
 	public void readXMLToBinder() throws ServiceException {
 		try {
 			m_WSC.parseSigProfile();
 		}
 		catch ( Exception e ) {
 			e.printStackTrace();
 			throw new ServiceException( e.getMessage() );
 		}
 	}
 
 	/**
 	 *
 		*/
 	public void validateUserAccessToMenu() throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering validateUserAccessToMenu, passed in binder:" + m_binder.toString(), null );
 		DataResultSet drset = new DataResultSet();
 		String contentTag = m_binder.getLocal( m_shared.getConfig( "coSignSignatureProfileMetaField" ) );
		if( contentTag != null && !contentTag.equals( "" ) ) {
 			drset.copy( m_cmutils.getProfileWithMatchingTagAndUserRoles( contentTag ) );
 			if( drset.getNumRows() > 0 )
 				m_binder.putLocal( "userHasRequiredRole", "1" );
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected void logHistory( String msg ) throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering log, passed in binder:", null );
 		DataBinder binder = new DataBinder();
 		binder.putLocal( "User", m_binder.getLocal( "dUser" ) );
 		binder.putLocal( "Operation", m_binder.getLocal( "IdcService" ) );
 		if( msg == null ) msg = "";
 	 msg = LocaleUtils.encodeMessage( msg, null );
 		binder.putLocal( "Error", msg );
 		binder.putLocal( "dDocName", m_binder.getLocal( "dDocName" ) );
 		binder.putLocal( "dID", m_binder.getLocal( "dID" ) );
 		binder.putLocalDate( "date", new java.util.Date() );
 		try {
 			long seq = IdcCounterUtils.nextValue( m_workspace, "ESIG_SEQ" );
 			binder.putLocal( "ID", Long.toString( seq ) );
 			m_workspace.execute( "IcosignHistory", binder );
 		}
 		catch ( DataException e ) {
 			throwFullError( e );
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected void throwFullError( Exception e ) throws ServiceException {
 		StringBuilder sb = new StringBuilder();
 		for(StackTraceElement element : e.getStackTrace()) {
 			sb.append(element.toString());
 			sb.append("\n");
 		}
 		throw new ServiceException( e.getMessage() + "\n" + sb.toString() );
 	}
 }
