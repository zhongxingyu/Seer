 package com.bezzotech.oracleucm.arx;
 
 import intradoc.common.CommonDataConversion;
 import intradoc.common.Errors;
 import intradoc.common.ExecutionContext;
 import intradoc.common.LocaleUtils;
 import intradoc.common.Report;
 import intradoc.common.ServiceException;
 import intradoc.common.StringUtils;
 import intradoc.common.SystemUtils;
 import intradoc.data.DataBinder;
 import intradoc.data.DataException;
 import intradoc.data.DataResultSet;
 import intradoc.data.IdcCounterUtils;
 import intradoc.data.ResultSet;
 import intradoc.data.ResultSetUtils;
 import intradoc.data.Workspace;
 import intradoc.server.Service;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.FileOutputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.util.Vector;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.bezzotech.oracleucm.arx.common.CMUtils;
 import com.bezzotech.oracleucm.arx.common.XMLUtils;
 import com.bezzotech.oracleucm.arx.service.FileStoreUtils;
 import com.bezzotech.oracleucm.arx.shared.SharedObjects;
 
 public class WSC {
 	protected final static int SUCCESS = 0;
 	protected final static int USER_OPER_CANCELLED = -2;
 
 	/** SharedObjects pointer to use for this request. */
 	public SharedObjects m_shared;
 
 	/** <span class="code">FileStoreUtils</span> object for this request. */
 	protected FileStoreUtils m_fsutil;
 
 	final String m_appName = "CoSign";
 	protected Document m_doc;
 	protected Service m_service;
 	protected Workspace m_workspace;
 	protected DataBinder m_binder;
 	protected Element m_doc_root;
 	protected CMUtils m_cmutil;
 	protected XMLUtils m_xmlutil;
 
 	protected WSC( ExecutionContext context ) throws ServiceException {
 		m_shared = SharedObjects.getSharedObjects( context );
 		m_fsutil = FileStoreUtils.getFileStoreUtils( context );
 		m_cmutil = CMUtils.getCMUtils( context );
 		m_xmlutil = XMLUtils.getXMLUtils( context );
 		if( context instanceof Service ) {
 			Service m_service = ( Service )context;
 			m_workspace = m_service.getWorkspace();
 			m_binder = m_service.getBinder();
 		}
 	}
 
 	static WSC getWSC( ExecutionContext context ) throws ServiceException {
 		return new WSC( context );
 	}
 
 	public CMUtils getCM() {
 		return m_cmutil;
 	}
 
 	/**
 	 *
 	 */
 	public String buildSignRequest() throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering buildSignRequest", null );
 		parseSigProfile();
 		return buildSigProfile( true );
 	}
 
 	/**
 	 *
 	 */
 	public String buildSigProfile( boolean isSignRequest ) throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering buildSigProfile, passed in parameters:" +
 				"\n\tisSignRequest: " + isSignRequest + "\n\tbinder: ", null );
 		m_doc = m_xmlutil.getNewDocument();
 		m_doc_root = m_doc.createElement( "request" );
 		m_doc.appendChild( m_doc_root );
 		if( m_binder.getLocal( "dWorkflowState" ) != "" ) {
 			m_binder.putLocal( m_appName + ".SigField.enforceFieldToSign", "true" );
 			m_binder.putLocal( m_appName + ".RejectReasons.fields", "rejectReason" );
 			m_binder.putLocal( m_appName + ".RejectReasons.rejectReason", "None" );
 			m_doc_root.appendChild( m_xmlutil.appendChildrenFromLocal( m_appName, m_doc, "RejectReasons" ) );
 		}
 		if( !Boolean.parseBoolean( m_binder.getLocal( m_appName + ".Logic.allowAdHoc" ) ) ) {
 			m_doc_root.appendChild( m_xmlutil.appendChildrenFromLocal( m_appName, m_doc, "SigField" ) );
 //			prepareAdHocSigProfileValues();
 		} else {
 			m_doc_root.appendChild( buildSigProfilesElement() );
 		}
 		if( isSignRequest )
 			m_doc_root.appendChild( m_xmlutil.appendChildrenFromLocal( m_appName, m_doc, "Document" ) );
 		m_doc_root.appendChild( m_xmlutil.appendChildrenFromLocal( m_appName, m_doc, "SignReasons" ) );
 		m_doc_root.appendChild( m_xmlutil.appendChildrenFromLocal( m_appName, m_doc, "Logic" ) );
 		if( !isSignRequest )
 			m_doc_root.appendChild(
 					m_xmlutil.appendChildrenFromEnvironmental( m_appName, m_doc, "Logic", true ) );
 		m_doc_root.appendChild(
 				m_xmlutil.appendChildrenFromEnvironmental( m_appName, m_doc, "Url", false ) );
 		m_doc_root.appendChild( buildAuthElement() );
 		return m_xmlutil.getStringFromDocument( m_doc );
 	}
 
 	/**
 	 *
 	 */
 	public void parseVerifyResponse( String response ) throws ServiceException {
 		m_doc = m_xmlutil.getNewDocument( response );
 		m_doc_root = m_doc.getDocumentElement();
 		parseVerify();
 	}
 
 	/**
 	 *
 	 */
 	public void parseSigProfile() throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering parseSigProfile", null );
 		m_doc = m_xmlutil.getExistingDocument( m_cmutil.retrieveSigProfilesFilePath() );
 		m_doc_root = m_doc.getDocumentElement();
 		parseSigProfileEx();
 	}
 
 	/**
 	 *
 	 */
 	public void parseSigProfileEx() {
 		Report.trace( "bezzotechcosign", "Entering parseSigProfileEx", null );
 		m_xmlutil.parseChildrenToLocal( m_appName, m_doc_root, "SignReasons", 0 );
 		m_xmlutil.parseChildrenToLocal( m_appName, m_doc_root, "Logic", 0 );
 		if( !Boolean.parseBoolean( m_binder.getLocal( m_appName + ".Logic.allowAdHoc" ) ) )
 			m_xmlutil.parseChildrenToLocal( m_appName, m_doc_root, "SigField", 0 );
 		else
 			parseSigProfiles();
 	}
 
 	/**
 	 *
 	 */
 	protected void parseWSCResponse( String response, boolean isFullResponse ) throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering parseWSCResponse, passed in parameters:\n\tresponse: " +
 				response, null );
 		m_doc = m_xmlutil.getNewDocument( response );
 		m_doc_root = m_doc.getDocumentElement();
 		m_xmlutil.parseChildrenToLocal( m_appName, m_doc_root, "Error", 0 );
 		if( Integer.parseInt( m_binder.getLocal( "CoSign.Error.returnCode" ) ) == SUCCESS )
 			m_xmlutil.parseChildrenToLocal( m_appName, m_doc_root, "Session", 0 );
 		if( isFullResponse ) {
 			parseDocument();
 			m_xmlutil.parseChildrenToLocal( m_appName, m_doc_root, "Result", 0 );
 			m_xmlutil.parseChildrenToLocal( m_appName, m_doc_root, "SigDetails", 0 );
 			parseVerify();
 		}
 	}
 
 	/**
 	 *
 	 */
 	public int processSignRequest() throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering processSignRequest", null );
 		String message = postRequestToWSC( "UploadDoc.aspx?docId=" +
 				m_binder.getLocal( "CoSign.Document.fileID" ), m_binder.getLocal( "SignRequest" ),
 				"application/x-www-form-urlencoded" );
 		Report.trace( "bezzotechcosign", "WSC response: " + message, null );
 		parseWSCResponse( message, false );
 		if( Integer.parseInt( m_binder.getLocal( "CoSign.Error.returnCode" ) ) == SUCCESS ) {
 			m_binder.putLocal( "WSC_Session", m_binder.getLocal( "CoSign.Session.sessionId" ) );
 		} else if( Integer.parseInt( m_binder.getLocal( "CoSign.Error.returnCode" ) ) == USER_OPER_CANCELLED ) {
 			return USER_OPER_CANCELLED;
 		} else {
 			throw new ServiceException( m_binder.getLocal( "CoSign.Error.errorMessage" ) );
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 *  Note: Concern over possible syncing issues
 	 */
 	public int processDownloadRequest() throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering processDownloadRequest", null );
 		String message = postRequestToWSC( "pullSignedDoc.ashx?sessionID=" +
 				m_binder.getLocal( "sessionId" ), null, null );
 		Report.trace( "bezzotechcosign", "WSC response: " + message, null );
 		parseWSCResponse( message, true );
 		Report.debug( "bezzotechcosign", "Resulting binder: ", null );
 		if( Integer.parseInt( m_binder.getLocal( "CoSign.Error.returnCode" ) ) == Errors.SUCCESS ) {
 			byte[] buffer =
 					CommonDataConversion.uudecode( m_binder.getLocal( "CoSign.Document.content" ), null );
 			int bytesRead = buffer.length;
 			String file =
 					m_fsutil.getTemporaryFileName( "." + m_binder.getLocal( "CoSign.Document.contentType" ), 0x0 );
 			BufferedOutputStream _bos = null;
 			try {
 				_bos = new BufferedOutputStream( new FileOutputStream( file ) );
 				_bos.write( buffer, 0, bytesRead );
 			} catch ( FileNotFoundException e ) {
 				throwFullError( e );
 			} catch ( IOException e ) {
 				throwFullError( e );
 			} finally {
 				try {
 					_bos.close();
 				} catch ( IOException e ) {
 					throwFullError( e );
 				}
 			}
 			m_binder.putLocal( "dDocName", m_binder.getLocal( "CoSign.Session.docId" ) );
 			m_binder.mergeResultSetRowIntoLocalData(
 					m_cmutil.getDocInfoByName( m_binder.getLocal( "CoSign.Session.docId" ) ) );
 			m_binder.putLocalDate( "dInDate", new java.util.Date() );
 			m_binder.putLocal( "primaryFile:path", file );
 			m_binder.putLocal( "dExtension", m_binder.getLocal( "CoSign.Document.contentType" ) );
 			m_binder.putLocal( "dWebExtension", m_binder.getLocal( "CoSign.Document.contentType" ) );
 			int statusCode = Integer.parseInt( m_binder.getLocal( "CoSign.Status.validationStatus" ) );
 			String status;
 			if( statusCode == 0 ) status = "Valid";
 			else if(statusCode == 1 ) status = "Invalid";
 			else if(statusCode == 2 ) status = "Error";
 			else if(statusCode == 3 ) status = "Incomplete";
 			else if(statusCode == 4 ) status = "Unknown";
 			else status = "Empty";
 			m_binder.putLocal( "xSignatureStatus", status );
 			ResultSet rset = m_binder.getResultSet( "Fields" );
 			DataResultSet drset = new DataResultSet();
 			drset.copy( rset );
 			drset.first();
 			if( drset.getStringValueByName( "status" ).equals( "0" ) ) {
 				m_binder.putLocal( "xSigner", drset.getStringValueByName( "signerName" ) );
 				try {
 					SimpleDateFormat sdf = new SimpleDateFormat( m_shared.getConfig( "CoSignDateFormat" ) );
 					Date date = sdf.parse( drset.getStringValueByName( "signingTime" ) );
 					m_binder.putLocalDate( "xSignTime", date );
 				} catch ( ParseException e ) {
 					throwFullError( e );
 			 }
 			}
 			try {
 				m_binder.putLocal( "xSignatureCount", "" + ResultSetUtils
 						.createFilteredStringArrayForColumn( drset, "fieldName", "status", "0", false, false).length );
 			} catch ( DataException e ) {
 				throwFullError( e );
 			}
 		} else if( Integer.parseInt( m_binder.getLocal( "CoSign.Error.returnCode" ) ) == USER_OPER_CANCELLED ) {
 			return USER_OPER_CANCELLED;
 		} else {
 			throw new ServiceException( m_binder.getLocal( "CoSign.Error.errorMessage" ) );
 		}
 		return Errors.SUCCESS;
 	}
 
 	/**
 	 *
 	 */
 	public int processVerifyRequest() throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering processVerifyRequest", null );
 		String message = postStreamToWSC( "VerifyService.aspx", m_binder,
 				m_binder.getResultSetValue( m_binder.getResultSet( "DOC_INFO" ), "dFormat" ) );
 		Report.trace( "bezzotechcosign", "WSC response: " + message, null );
 		parseVerifyResponse( message );
 //		String returnCode = m_binder.getLocal( "CoSign.Error.returnCode" );
 //		if( returnCode == null || Integer.parseInt( returnCode ) == SUCCESS ) {
 		ResultSet rset = m_binder.getResultSet( "Fields" );
 		if( rset == null || rset.isEmpty() )	return SUCCESS;//throw new ServiceException( "Unable to retrieve valid signatures." );
 		DataResultSet drset = new DataResultSet();
 		drset.copy( rset );
 		drset.first();
 		m_binder.mergeResultSetRowIntoLocalData( m_binder.getResultSet( "DOC_INFO" ) );
 //		int statusCode = Integer.parseInt( drset.getStringValueByName( "validationStatus" ) );
 		int statusCode = Integer.parseInt( m_binder.getLocal( "CoSign.Status.validationStatus" ) );
 		String status;
 		if( statusCode == 0 ) status = "Valid";
 		else if(statusCode == 1 ) status = "Invalid";
 		else if(statusCode == 2 ) status = "Error";
 		else if(statusCode == 3 ) status = "Incomplete";
 		else if(statusCode == 4 ) status = "Unknown";
 		else status = "Empty";
 		m_binder.putLocal( "xSignatureStatus", status );
 		if( drset.getStringValueByName( "status" ).equals( "0" ) ) {
 			m_binder.putLocal( "xSigner", drset.getStringValueByName( "signerName" ) );
 			try {
 				SimpleDateFormat sdf = new SimpleDateFormat( m_shared.getConfig( "CoSignDateFormat" ) );
 				Date date = sdf.parse( drset.getStringValueByName( "signingTime" ) );
 				m_binder.putLocalDate( "xSignTime", date );
 			} catch ( ParseException e ) {
 				throwFullError( e );
 			}
 		}
 		try {
 			m_binder.putLocal( "xSignatureCount", "" + ResultSetUtils
 					.createFilteredStringArrayForColumn( drset, "fieldName", "status", "0", false, false).length );
 		} catch ( DataException e ) {
 			throwFullError( e );
 		}
 		m_cmutil.update();
 		log();
 //		} else if( Integer.parseInt( m_binder.getLocal( "CoSign.Error.returnCode" ) ) == USER_OPER_CANCELLED ) {
 //			return USER_OPER_CANCELLED;
 //		} else {
 //			throw new ServiceException( m_binder.getLocal( "CoSign.Error.errorMessage" ) );
 //		}
 		return SUCCESS;
 	}
 
 	/**
 	 *
 	 */
 	protected String postStreamToWSC( String urlQueryString, DataBinder content, String contentType )
 			throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering postStreamToWSC, passed in parameters:" +
 				"\n\turlQueryString: " + urlQueryString + "\n\tcontent:\n\tcontentType: " + contentType, null );
 		String WSC_URL = m_shared.getConfig( "wscServerAddress" );
 		String WSC_PORT = m_shared.getConfig( "wscServerPort" );
 		String WSC_ROOT = m_shared.getConfig( "CoSignVerifyServerPath" );
 		if( WSC_PORT == null || WSC_PORT.equals( "" ) ) WSC_PORT = "80";
 		String response = postRequestBinder( WSC_URL + ":" + WSC_PORT + WSC_ROOT + urlQueryString, content,
 				contentType );
 		return response;
 	}
 
 	/**
 	 *
 	 */
 	protected String postRequestToWSC( String urlQueryString, String content, String contentType )
 			throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering postRequestToWSC, passed in parameters:" +
 				"\n\turlQueryString: " + urlQueryString + "\n\tcontent: " + content + "\n\tcontentType: " +
 				contentType, null );
 		String WSC_URL = m_shared.getConfig( "wscServerAddress" );
 		String WSC_PORT = m_shared.getConfig( "wscServerPort" );
 		String WSC_ROOT = m_shared.getConfig( "CoSignServerPath" );
 		if( WSC_PORT == null || WSC_PORT.equals( "" ) ) WSC_PORT = "80";
 		String response = postRequestString( WSC_URL + ":" + WSC_PORT + WSC_ROOT + urlQueryString, content,
 				contentType );
 		return response;
 	}
 
 	/**
 	 *  Note: CoSign running over SSL
 	 *  Note: Handling read in case of Socket lag
 	 */
 	protected String postRequestBinder( String iUrl, DataBinder content, String iContentType )
 			throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering postRequestBinder, passed in parameters:" +
 				"\n\tiUrl: " + iUrl + "\n\tcontent:\n\tiContentType: " +	iContentType, null );
 		StringBuffer response = new StringBuffer();
 		BufferedOutputStream _out = null;
 		BufferedReader _read = null;
 		InputStream _is = null;
 		HttpURLConnection httpCon = null;
 		try {
 			URL url = new URL( iUrl );
 			httpCon = ( HttpURLConnection ) url.openConnection();
 			httpCon.setRequestProperty( "Content-Type", iContentType );
 			httpCon.setDoOutput( true );
 			httpCon.setRequestMethod( "POST" );
 
 			Report.trace( "bezzotechcosign", "Avialable file size: " + content.m_inStream.available(), null );
 			_out = new BufferedOutputStream( httpCon.getOutputStream() );
 			byte[] baBuffer = new byte[64 * 1024];
 			int len = 0;
 			while ( ( len = content.m_inStream.read( baBuffer, 0, baBuffer.length ) ) > 0 ) {
 				_out.write( baBuffer, 0, len );
 			}
 			_out.flush();
 			if( httpCon.getResponseCode() <= 400 ) {
 				_is = httpCon.getInputStream();
 			} else {
 				/* error from server */
 				_is = httpCon.getErrorStream();
 			}
 			_read = new BufferedReader( new InputStreamReader( _is ) );
 			String strBuffer = null;
 			while ( ( strBuffer = _read.readLine() ) != null ) {
 				response.append( strBuffer );
 			}
 		} catch ( MalformedURLException e ) {
 			throwFullError( e );
 		} catch ( ProtocolException e ) {
 			throwFullError( e );
 		} catch ( IOException e ) {
 			throwFullError( e );
 		} finally {
 			try {
 				content.m_inStream.close();
 				_out.close();
 				_is.close();
 				_read.close();
 				httpCon.disconnect();
 			} catch ( IOException e ) {
 				throwFullError( e );
 			}
 		}
 		return response.toString();
 	}
 
 	/**
 	 *
 	 */
 	protected String postRequestString( String iUrl, String iContent, String iContentType )
 			throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering postRequestString, passed in parameters:" +
 				"\n\tiUrl: " + iUrl + "\n\tiContent: " + iContent + "\n\tiContentType: " +	iContentType, null );
 		StringBuffer response = new StringBuffer();
 		BufferedOutputStream _out = null;
 		BufferedInputStream _in = null;
 		BufferedReader _read = null;
 		InputStream _is = null;
 		HttpURLConnection httpCon = null;
 		try {
 			URL url = new URL( iUrl );
 			httpCon = ( HttpURLConnection ) url.openConnection();
 			if( iContentType != null ) httpCon.setRequestProperty( "Content-Type", iContentType );
 
 			httpCon.setDoOutput( true );
 			if( iContent != null && iContentType != null ) {
 				httpCon.setDoInput( true );
 				httpCon.setRequestMethod( "POST" );
 				httpCon.setChunkedStreamingMode(1);
 			}
 
 			if( iContent != null ) {
 				_out = new BufferedOutputStream( httpCon.getOutputStream() );
 				_in = new BufferedInputStream( new ByteArrayInputStream( iContent.getBytes( "UTF-8" ) ) );
 				byte[] buffer = new byte[8 * 1024];
 				int len = 0;
 				while ( ( len = _in.read( buffer, 0, buffer.length ) ) > 0 ) {
 					_out.write( buffer, 0, len );
 				}
 				_out.flush();
 			}
 
 			if( httpCon.getResponseCode() <= 400 ) {
 				_is = httpCon.getInputStream();
 			} else {
 				/* error from server */
 				_is = httpCon.getErrorStream();
 			}
 			_read = new BufferedReader( new InputStreamReader( _is ) );
 			String strBuffer = null;
 			while( ( strBuffer = _read.readLine() ) != null ) {
 				response.append( strBuffer );
 			}
 		} catch ( MalformedURLException e ) {
 			throwFullError( e );
 		} catch ( ProtocolException e ) {
 			throwFullError( e );
 		} catch ( UnsupportedEncodingException e ) {
 			throwFullError( e );
 		} catch ( IOException e ) {
 			throwFullError( e );
 		} finally {
 			try {
 				if( _in != null )	_in.close();
 				if( _out != null )	_out.close();
 				_is.close();
 				_read.close();
 				httpCon.disconnect();
 			} catch ( IOException e ) {
 				throwFullError( e );
 			}
 		}
 		return response.toString();
 	}
 
 	protected void prepareAdHocSigProfileValues() {
 		Report.trace( "bezzotechcosign", "Entering prepareAdHocSigProfileValues", null );
 		String [] alter = { "fieldName", ".SigField.fieldNameToSign" };
 		String [] exclude = { "fieldName", "x", "y", "width", "height", "pageNumber", "title",
 				"dateFormat", "timeFormat" };
 		String fields = m_binder.getLocal( m_appName + ".SigProfile.fields" );
 		String [] fieldArray = fields.split( ";" );
 		for( int i = 0; i < fieldArray.length; i++ ) {
 			if( !Arrays.asList( exclude ).contains( fieldArray[ i ] ) )
 				m_binder.putLocal( m_appName + ".SigProfile." + fieldArray[ i ], "" );
 			else
 				if( Arrays.asList( alter ).contains( fieldArray[ i ] ) ) {
 					Report.debug( "bezzotechcosign", "Altering: " + fieldArray[ i ] + " at index: " +
 							Arrays.asList( alter ).indexOf( fieldArray[ i ] ), null );
 					int altInd = Arrays.asList( alter ).indexOf( fieldArray[ i ] ) + 1;
 					m_binder.putLocal( m_appName + ".SigProfile." + fieldArray[ i ],
 							m_binder.getLocal( m_appName + Arrays.asList( alter ).get( altInd ) ) );
 				}
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected Element buildSigProfilesElement() throws ServiceException {
 		Element root = m_doc.createElement( "SigProfiles" );
 		Element row = m_xmlutil.appendChildrenFromLocal( m_appName, m_doc, "SigProfile" );
 		root.appendChild( row );
 		return root;
 	}
 
 	/** Generates XML Element Auth from binder local properties
 	 *  <Auth>...</Auth>
 	 *
 	 *  If environmental variable useWCCUserName is set to TRUE, we inject dUser value into username
 	 *  text node
 	 *
 	 *  Throws ServiceException - dUser value is missing from session
 	 *  Throws ServiceException - Auth Element was not built properly for injection
 	 */
 	protected Element buildAuthElement() throws ServiceException {
 		Element ele = m_xmlutil.appendChildrenFromEnvironmental( m_appName, m_doc, "Auth", false );
 		if( StringUtils.convertToBool( m_shared.getConfig( "useWCCUserName" ), false ) ) {
 			String username = m_binder.getLocal( "dUser" );
 			if( username == null || username.equals( "" ) ) {
 				String msg = LocaleUtils.encodeMessage( "csIISorCSWADown", null );
 				SystemUtils.error( null, msg );
 			}
 			m_xmlutil.appendTextNodeToChild( m_doc, ele, "username", username );
 		}
 		return ele;
 	}
 
 	/** Parse SigProfile XML response to binder local properties
 	 *  <SigProfiles><SigProfil>...</SigProfile></SigProfiles>
 	 */
 	protected void parseSigProfiles() {
 		Element root = ( Element )m_doc_root.getElementsByTagName( "SigProfiles" ).item( 0 );
 		m_xmlutil.parseChildrenToLocal( m_appName, root, "SigProfile", 0 );
 	}
 
 	/** Parse Document XML response to binder local properties
 	 *  <Document>...</Document>
 	 */
 	protected void parseDocument() {
 		m_xmlutil.parseChildrenToLocal( m_appName, m_doc_root, "Document", 0 );
 	}
 
 	/** Parse Verify XML response to binder local properties
 	 *  <Verify><Status>...</Status><Fields><Field>...</Field>...</Fields></Verify>
 	 */
 	protected void parseVerify() throws ServiceException {
 		Element root = ( Element )m_doc_root.getElementsByTagName( "Verify" ).item( 0 );
 		m_xmlutil.parseChildrenToLocal( m_appName, root, "Status", 0 );
 		try {
 			m_xmlutil.parseChildrenToResultSet( m_appName, root, "Fields", "signingTime" );
 		} catch ( Exception e ) {
 			throwFullError( e );
 			// Swallow error, if no Fields is returned likely there is an error with WSC
 		}
 	}
 
 	/** Prepares binder for inserting Verify response.  We get much more data on pull than we do on
 	 *  Verify request
 	 */
 	protected void prepareLog() {
 		Report.trace( "bezzotechcosign", "Entering prepareLog, passed in binder:", null );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.x" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.x", "0" );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.y" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.y", "0" );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.width" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.width", "0" );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.height" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.height", "0" );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.pageNumber" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.pageNumber", "0" );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.dateFormat" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.dateFormat", "null" );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.timeformat" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.timeformat", "null" );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.graphicalImage" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.graphicalImage", "null" );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.signer" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.signer", "null" );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.date" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.date", "null" );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.initials" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.initials", "null" );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.logo" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.logo", "null" );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.showTitle" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.showTitle", "null" );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.showReason" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.showReason", "null" );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.title" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.title", "null" );
 		if( m_binder.getAllowMissing( "CoSign.SigDetails.reason" ) == null )
 			m_binder.putLocal( "CoSign.SigDetails.reason", "null" );
 	}
 
 	/** Inserts Verify response into DB
 	 *
 	 *  Throws ServiceException - error occurred during query action
 	 */
 	protected void log() throws ServiceException {
 		Report.trace( "bezzotechcosign", "Entering log, passed in binder:", null );
 		prepareLog();
 		DataBinder queryBinder = m_binder.createShallowCopyWithClones( 1 );
 		ResultSet rset = m_binder.getResultSet( "Fields" );
 		DataResultSet drset = new DataResultSet();
 		drset.copy( rset );
 		drset.first();
 		try {
 			do {
 				queryBinder.mergeResultSetRowIntoLocalData( drset );
 				try {
 					String dateString = queryBinder.getLocal( "signingTime" );
 					if( dateString != null && !dateString.trim().equals( "" ) ) {
 						SimpleDateFormat sdf = new SimpleDateFormat( m_shared.getConfig( "CoSignDateFormat" ) );
 						Date date = sdf.parse( dateString );
 						queryBinder.putLocalDate( "signingTime", date );
 					}
 				} catch ( ParseException e ) {
 					throwFullError( e );
 				}
     long seq = IdcCounterUtils.nextValue( m_workspace, "ESIG_SEQ" );
 				queryBinder.putLocal( "sID", Long.toString( seq ) );
 				m_workspace.execute( "IcosignSignatureDetails", queryBinder );
 			}	while( drset.next() );
 		} catch ( DataException e ) {
 		 throwFullError( e );
 		}
 	}
 
 	/** Prints out error message and stack trace from caught exceptions and throws them as message in
 	 *  ServiceException
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
