 package sfs.async.handler.http.reader;
 
 import java.util.Arrays;
 
 import sfs.header.http.HeaderEntry;
 import sfs.header.http.ending.Ending;
 import sfs.header.http.separator.Colon;
 import sfs.header.http.separator.SemiColon;
 import sfs.mime.Mime;
 import sfs.request.http.RequestMessage;
 import sfs.stat.message.ContentDisposition;
 import sfs.stat.message.DataType;
 import sfs.stat.message.MessageStat;
 import sfs.util.string.StringUtil;
 
 public class HTTPMessageReader extends AbstractHTTPReader {
 	
 	private final RequestMessage requestMessage;
 	private final String CHUNKED_KEY = "chunked";
 	private final String CHUNKED_END_KEY = Ending.CRLF + "0" + Ending.CRLF + Ending.CRLF;
 	private final String TRANSFER_ENCODING_HEADER_KEY = HeaderEntry.TRANSFER_ENCODING.toString() + ": " + CHUNKED_KEY
 			+ Ending.CRLF;
 	private final String CONTENT_LENGTH_HEADER_KEY = HeaderEntry.CONTENT_LENGTH.toString() + ": ";
 	private final String BOUNDARY_KEY = "boundary=";
 	private final String CONTENT_DISPOSITION_KEY = "Content-Disposition: ";
 	private final String NAME_KEY = "name=";
 	private final String FILENAME_KEY = "filename=";
 	private final String FORM_DATA_KEY = "form-data;";
 	private final String ATTACHMENT_KEY = "attachment;";
 
 	public HTTPMessageReader() {
 		super();
 		requestMessage = new RequestMessage();
 	}
 
 	public HTTPMessageReader(int bufferCapacity) {
 		super( bufferCapacity );
 		requestMessage = new RequestMessage();
 	}
 
 	@Override
 	public boolean findEndOfMessage(String message, MessageStat messageStat) {
 
 		if ( messageStat.isHeaderHasBeenSet() ) {
 
 			// sets request header to messageStat.
 			messageStat.checkAndSetHeaderAndBoundary( BOUNDARY_KEY, requestMessage );
 
 			if ( messageStat.getMessageBodyType().equals( HeaderEntry.CONTENT_LENGTH.toString() ) ) {
 				messageStat.setMessage( messageStat.getMessage() + message );
 				messageStat.setLength( messageStat.getLength() + message.length() );
 				messageStat.setMessageBodyLength( messageStat.getMessageBodyLength() - message.length() );
 
 				checkAndSetContentDisposition( messageStat );
 				if ( !messageStat.isContentDispositionSet() && messageStat.getMessageBodyLength() == 0 ) {
 					messageStat.setMessageBodyLength( messageStat.getLength() - messageStat.getMessageBodyStartIndex() );
 					messageStat.checkAndSetHeader( requestMessage );
 					messageStat.setEndOfMessage( true );
 
 					return true;
 				}
 
 				return false;
 			}
 			else if ( messageStat.getMessageBodyType().equals( "chunked" ) ) {
 				// TODO chunked type to read message body here.
 				
 				checkAndSetContentDisposition( messageStat );
 			}
 		}
 
 		messageStat.setMessage( messageStat.getMessage() + message );
 		messageStat.setLength( messageStat.getMessage().length() );
 
 		int separatorIndex = StringUtil.searchLastIndexOfByMB( messageStat.getMessage(), Ending.CRLF.toString()
 				+ Ending.CRLF.toString() );
 
 		if ( separatorIndex == -1 ) {
 			return false;
 		}
 
 		int contentHeaderIndex = StringUtil.searchLastIndexOfByMB( messageStat.getMessage(), CONTENT_LENGTH_HEADER_KEY );
 		int chunkedHeaderIndex = StringUtil.searchLastIndexOfByMB( messageStat.getMessage(),
 				TRANSFER_ENCODING_HEADER_KEY );
 
 		if ( ( contentHeaderIndex == -1 ) && ( chunkedHeaderIndex == -1 ) ) {
 			// no message body attached
 			messageStat.checkAndSetHeader( requestMessage );
 			messageStat.setMessageBodyLength( 0 );
 			messageStat.setMessageBodyContained( false );
 			messageStat.setEndOfMessage( true );
 
 			return true;
 		}
 
 		messageStat.setMessageBodyContained( true );
 		messageStat.setMessageBodyStartIndex( separatorIndex + 1 );
 		messageStat.setHeaderHasBeenSet( true );
 
 		if ( contentHeaderIndex != -1 ) {
 			messageStat.setMessageBodyType( HeaderEntry.CONTENT_LENGTH.toString() );
 			int contentLengthIndex = StringUtil.searchFirstIndexOfByMB( messageStat.getMessage(),
 					Ending.CRLF.toString(), contentHeaderIndex );
 
 			messageStat.setMessageBodyLength( Integer.parseInt( String.valueOf( Arrays.copyOfRange( messageStat
 					.getMessage().toCharArray(), contentHeaderIndex + 1, contentLengthIndex ) ) )
 					- ( messageStat.getLength() - messageStat.getMessageBodyStartIndex() ) );
 
 			if ( messageStat.getMessageBodyLength() == 0 ) {
 				messageStat.setMessageBodyLength( messageStat.getLength() - messageStat.getMessageBodyStartIndex() );
 				checkAndSetContentDisposition( messageStat );
 				messageStat.setEndOfMessage( true );
 
 				return true;
 			}
 		}
 
 		if ( chunkedHeaderIndex != -1 ) {
 			messageStat.setMessageBodyType( CHUNKED_KEY );
 
 			// TODO can increase the starting point from messageStat.getMessageBodyStartIndex().
 			if ( StringUtil.searchFirstIndexOfByMB( messageStat.getMessage(), CHUNKED_END_KEY,
 					messageStat.getMessageBodyStartIndex() ) != -1 ) {
 
 				// found the end of chunked, \CR\LF0\CR\LF\CR\LF
 				checkAndSetContentDisposition( messageStat );
 				messageStat.setEndOfMessage( true );
 
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Checks and sets ContentDisposition to the specified MessageStat.
 	 * 
 	 * @param messageStat
 	 *            Used to check and set ContentDiposition.
 	 */
 	private void checkAndSetContentDisposition(MessageStat messageStat) {
 
 		// sets request header to messageStat.
 		messageStat.checkAndSetHeaderAndBoundary( BOUNDARY_KEY, requestMessage );
 
 		if ( !messageStat.isFileUploadable() ) {
 			return;
 		}
 
 		// checks content-disposition key
 		if ( !messageStat.isContentDispositionHasBeenSet() ) {
 			messageStat.setCurrentContentDispositionIndex( StringUtil.searchLastIndexOfByMB( messageStat.getMessage(),
 					messageStat.getBoundary() + Ending.CRLF + CONTENT_DISPOSITION_KEY,
 					messageStat.getMessageBodyStartIndex() ) );
 		}
 		else {
 			messageStat.setCurrentContentDispositionIndex( StringUtil.searchLastIndexOfByMB( messageStat.getMessage(),
 					messageStat.getBoundary() + Ending.CRLF + CONTENT_DISPOSITION_KEY,
 					messageStat.getCurrentContentDispositionIndex() ) );
 		}
 
 		// found content-disposition key
 		if ( messageStat.getCurrentContentDispositionIndex() != -1 && !messageStat.isContentDispositionSet() ) {
 
 			// set the index for content-disposition key content
 			messageStat.setCurrentContentStartIndex( StringUtil.searchFirstIndexOfByMB( messageStat.getMessage(),
 					Ending.CRLF.toString(), messageStat.getCurrentContentDispositionIndex() + 1 ) + 2 );
 
 			if ( messageStat.getCurrentContentStartIndex() != -1 ) {
 
 				ContentDisposition added = getContentDispositionKey(
 						messageStat,
 						messageStat.getMessage().substring( messageStat.getCurrentContentDispositionIndex() + 1,
 								messageStat.getCurrentContentStartIndex() - 2 ) );
 
 				// set the index for content-disposition content
 				messageStat.setCurrentContentDispositionIndex( StringUtil.searchFirstIndexOfByMB(
 						messageStat.getMessage(), Ending.CRLF.toString(), messageStat.getCurrentContentStartIndex() ) );
 
 				// set the content from form value.
 				if ( messageStat.getCurrentContentDispositionIndex() != -1 ) {
 					added.setFieldValue( messageStat.getMessage().substring( messageStat.getCurrentContentStartIndex(),
 							messageStat.getCurrentContentDispositionIndex() ) );
 					messageStat.addContentDisposition( added );
 
 					// reached the end of message
 					if ( messageStat.isEndOfMessage( messageStat.getCurrentContentDispositionIndex(), 2 ) ) {
 						messageStat.setContentDispositionSet( true );
 					}
 					else {
 						checkAndSetContentDisposition( messageStat );
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gets ContentDisposition object out of the parameter, str.
 	 * 
 	 * @param messageStat
 	 *            Used to store extracted ContentDisposition.
 	 * @param str
 	 *            Extraction of ContetnDisposition is taken place.
 	 * @return Newly created ContentDisposition object.
 	 */
 	private ContentDisposition getContentDispositionKey(MessageStat messageStat, String str) {
 
 		ContentDisposition contentDisposition = new ContentDisposition();
 		String[] each = str.split( " " );
 		int index = -1;
 		for ( int i = 0; i < each.length; i++ ) {
 			if ( i == 0 ) {
 				if ( StringUtil.startsWith( each[0], FORM_DATA_KEY ) > 0 ) {
 					contentDisposition.setDataType( DataType.FORM_DATA );
 				}
 				else if ( StringUtil.startsWith( each[0], ATTACHMENT_KEY ) > 0 ) {
 					contentDisposition.setDataType( DataType.ATTACHMENT );
 				}
 			}
 
 			if ( ( index = StringUtil.startsWith( each[i], NAME_KEY ) ) > 0 ) {
 				if ( each[i].toCharArray()[each[i].length() - 1] == SemiColon.SEMI_COLON ) {
 					contentDisposition.setFieldName( each[i].substring( index + 1, each[i].length() - 1 ) );
 				}
 				else {
 					contentDisposition.setFieldName( each[i].substring( index + 1 ) );
 				}
 			}
 			else if ( ( index = StringUtil.startsWith( each[i], FILENAME_KEY ) ) > 0 ) {
 				contentDisposition.setFileName( each[i].substring( index + 1 ) );
 			}
 			else if ( ( index = StringUtil.startsWith( each[i], HeaderEntry.CONTENT_TYPE.toString() + Colon.COLON ) ) > 0 ) {
 				contentDisposition.setContentType( (Mime) Mime.MIMES.get( each[i + 1] ) );
 				break;
 			}
 		}
 
 		return contentDisposition;
 	}
 }
