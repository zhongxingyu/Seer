 package ecologylab.oodss.distributed.server.clientsessionmanager;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.nio.channels.SelectionKey;
 
 import ecologylab.collections.Scope;
 import ecologylab.net.ParsedURL;
 import ecologylab.oodss.distributed.impl.NIOServerIOThread;
 import ecologylab.oodss.distributed.server.NIOServerProcessor;
 import ecologylab.oodss.messages.HttpRequest;
 import ecologylab.oodss.messages.RequestMessage;
 import ecologylab.oodss.messages.ResponseMessage;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.formatenums.StringFormat;
 
 public class HTTPPostClientSessionManager extends HTTPClientSessionManager
 {	
 	static final String	HTTP_RESPONSE_HEADERS			= HTTP_VERSION + " 200" + "\r\n";
 
 	static final String	HTTP_PREPEND				= "POST /";
 
 	static final int		HTTP_PREPEND_LENGTH	= HTTP_PREPEND.length();
 
 	/**
 	 * @param token
 	 * @param server
 	 * @param socketKey
 	 * @param translationScope
 	 * @param registry
 	 */
 	public HTTPPostClientSessionManager(String token, int maxPacketSize, NIOServerIOThread server,
 			NIOServerProcessor frontend, SelectionKey socketKey, SimplTypesScope translationScope,
 			Scope<?> registry)
 	{
 		super(token, maxPacketSize, server, frontend, socketKey, translationScope, registry);
 		this.initialized = true;
 	}
 
 	/**
 	 * This method only handles HttpPostRequest messages; it will report an error for any
 	 * non-HttpPostRequest. Otherwise, it will not add anything to the msgBufOutgoing
 	 * 
 	 * @see ecologylab.oodss.distributed.server.clientsessionmanager.ClientSessionManager#translateResponseMessageToString(ecologylab.oodss.messages.RequestMessage,
 	 *      ecologylab.oodss.messages.ResponseMessage)
 	 */
 	@Override
 	protected void translateResponseMessageToStringBufferContents(RequestMessage requestMessage,
 			ResponseMessage responseMessage, StringBuilder outgoingMessageBuf)
 			throws SIMPLTranslationException
 	{
 		if (!(requestMessage instanceof HttpRequest))
 		{
 			debug("ERROR! HTTPPostContextManager only handles HttpRequests!");
 		}
 		else
 		{
 			try {
 				SimplTypesScope.serialize(responseMessage, outgoingMessageBuf, StringFormat.JSON);
 			} catch (SIMPLTranslationException e) {
 				e.printStackTrace();
 			}
 		}		
 	}
 
 	/**
 	 * @throws UnsupportedEncodingException
 	 * @see ecologylab.oodss.distributed.server.clientsessionmanager.ClientSessionManager#translateStringToRequestMessage(java.lang.String)
 	 */
 	@Override
 	protected RequestMessage translateStringToRequestMessage(CharSequence incomingMessage)
 			throws SIMPLTranslationException, UnsupportedEncodingException
 	{
 		String messageString = incomingMessage.toString();
 		messageString = URLDecoder.decode(messageString, "UTF-8");
 
 		return super.translateStringToRequestMessage(messageString);
 	}
 	
 	/**
 	 * @see ecologylab.oodss.distributed.server.clientsessionmanager.ClientSessionManager#createHeader(java.lang.StringBuilder,
 	 *      java.lang.StringBuilder, RequestMessage, ResponseMessage)
 	 */
 	@Override
 	protected void createHeader(int messageSize, StringBuilder outgoingMessageHeaderBuf,
 			RequestMessage incomingRequest, ResponseMessage outgoingResponse, long uid)
 	{
 		boolean isOK = outgoingResponse.isOK();
 		if(isOK)
 		{			
 			outgoingMessageHeaderBuf.append(HTTP_RESPONSE_HEADERS
 					+ HTTP_CONTENT_TYPE
 					+ "Content-Length: " + messageSize);
 			
 			debugA("Server sending response!!!\n" + outgoingMessageHeaderBuf.toString());
 		}
 	}
 	
 	/**
 	 * Translates an incoming character sequence identified to be an OODSS request message (not a GET
 	 * or POST request).
 	 * 
 	 * @param messageCharSequence
 	 * @param startLineString
 	 *          TODO
 	 * @return The request message contained in the message.
 	 * @throws SIMPLTranslationException
 	 */
 	protected RequestMessage translateOODSSRequest(CharSequence messageCharSequence,
 			String startLineString) throws SIMPLTranslationException
 	{
 		return (RequestMessage) translationScope.deserialize(messageCharSequence, StringFormat.JSON);
 	}
	
	/**
	 * Translates an incoming character sequence identified to be a POST request.
	 * 
	 * This implementation expects the POST request to contain a nested OODSS request.
	 * 
	 * @param messageCharSequence
	 * @param startLineString
	 *          TODO
	 * @return
	 * @throws SIMPLTranslationException
	 */
	protected RequestMessage translatePostRequest(CharSequence messageCharSequence,
			String startLineString) throws SIMPLTranslationException
	{
		String messageString = messageCharSequence.toString();

		return this.translateOODSSRequest(messageString, startLineString);
	}
 
 }
