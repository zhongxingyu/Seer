 /**
  * Copyright (c) 2012, Nick Harvey
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * - Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution.
  * - Neither the name of the <ORGANIZATION> nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without specific prior written permission.
  *   
  *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  *   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  *   ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  *   LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  *   CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  *   SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  *   INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  *   CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  *   ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  *   POSSIBILITY OF SUCH DAMAGE.
  */
 package com.bluemini.websockets.server;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.util.ArrayList;
 
 
 public class WSRequest
 implements Runnable
 {
 	private final Server server;
 	private final Socket socket;
 	
 	private InputStream in;
 	private int FrameCount = 0;
 	
 	// Message parameters
 	private boolean FIN						= false;
 	private boolean RSV1					= false;
 	private boolean RSV2					= false;
 	private boolean RSV3					= false;
 	public boolean closing					= false;
 	private byte opcode						= 0;
 	private boolean controlFrame			= false;
 	private boolean masked					= false;
 	private int mask						= 0;
 	private ArrayList<Byte> payload 		= new ArrayList<Byte>();
 	private long payloadSize				= -1;
 	private PayloadType payloadType;
 	public WSResponse response				= null;
 	
 	public static byte OPCODE_CONTINUATION_FRAME	= 0;
 	public static byte OPCODE_TEXT_FRAME			= 1;
 	public static byte OPCODE_BINARY_FRAME			= 2;
 	public static byte OPCODE_CONNECTION_CLOSE		= 8;
 	public static byte OPCODE_PING					= 9;
 	public static byte OPCODE_PONG					= 10;
 	
 	public static final String WSGUID		= "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
 	
 	public static enum PayloadType			{TEXT, BINARY};
 	
 	/**
 	 * The constructor takes an input stream and parses out the meaning. You
 	 * do NOT construct the WSRequest to generate the appropriate headers
 	 * for a connection upgrade. These should all be called statically.
 	 * @param request
 	 */
 	public WSRequest(Server server, Socket socket)
 	{
 		this.server = server;
 		this.socket = socket;
 		// SocketAddress remote = socket.getRemoteSocketAddress();
 	}
 	
 	@Override
 	public void run()
 	{
 		resetFlags();
 		
 		try
 		{
 			this.in = this.socket.getInputStream();
 
 			// attempt to start a WebSocket session - failure caught in error handler
 			String responseString = startSession(in);
 			sendResponse(responseString, socket);
 
 			// the main connection loop takes the remaining message input
 			while(true)
 			{
 
 				parseFrame();
 
 				// close out of the loop if we are to close the WebSocket
 				if (closing)
 				{
 					System.out.println("Closing WebSocket");
 					break;
 				}
 				
 				// if this is the last frame of the message; return the response and then
 				// reset flags for new message
 				if (FIN) {
 					if (controlFrame)
 					{
 						WSControlHandler.respond(this);
 					}
 					else
 					{
 						respond();
 						System.out.println("Reset all flags as we've received the last frame of the current message.");
 						resetFlags();
 					}
 				}
 
 			}
 			
 		}
 		catch (SWSSUpgradeException swssue)
 		{
 			// the HTTP Upgrade failed
 			System.out.println("Upgrade Error: " + swssue.getMessage());
 		}
 		catch (IOException ioe)
 		{
 			// anything
 			System.out.println("ERROR: " + ioe.getMessage());
 		}
 		catch (Exception e)
 		{
 			System.out.println(e.getMessage());
 		}
 		
 	}
 	
 	/**
 	 * Reads in all parts of the frame and builds the message. This will
 	 * assemble multiple frames until the FIN is sent.
 	 * @throws IOException
 	 */
 	private void parseFrame()
 	throws IOException, SWSSUnsupportedPayloadType, SWSSIncorrectOpcode
 	{
 		byte[] buff = new byte[1024];
 		long bytesLeft = 0;
 
 		// get the first byte
 		setStatus(in.read());
 		
 		// figure out the payload length
 		setPayload(in);
 		
 		// read in the rest..if the payload is non-zero
 		if (payloadSize > 0)
 		{
 			// figure out the masking
 			if (masked)
 			{
 				mask = 0;
 				for (int i=4; i>0; i--)
 				{
 					mask += in.read() << ((i-1)*8);
 				}
 				System.out.println("Mask: "+mask);
 			}
 			
 			do
 			{
 				int bytesRead = in.read(buff);
 				
 				if (bytesRead >= 0)
 				{
 					bytesLeft = payloadSize-bytesRead;
 					System.out.println("read bytes: " + bytesRead + ", to read: " + bytesLeft);
 					
 					// unmask the data (if masked)
 					if (masked && bytesRead > 0)
 					{
 						buff = unmask(buff, bytesRead, mask);
 					}
 
 					// copy data to payload
 					for (int i=0; i<bytesRead; i++)
 					{
 						payload.add(buff[i]);
 					}
 				}
 				else
 				{
 					bytesLeft = 0;
 				}
 				
 			} while (bytesLeft > 0);
 		}
 		
 		if (!controlFrame)
 		{
 			// increment the frame count if its a genuine frame
 			FrameCount += 1;
 			
 			// if this is the first frame, establish the type (binary/text) and if not,
 			// ensure it's a continuation frame
 			if (FrameCount == 1)
 			{
 				if (opcode == WSRequest.OPCODE_BINARY_FRAME)
 				{
 					payloadType = PayloadType.BINARY;
 				}
 				else if (opcode == WSRequest.OPCODE_TEXT_FRAME)
 				{
 					payloadType = PayloadType.TEXT;
 				}
 				else
 				{
 					throw new SWSSUnsupportedPayloadType("Unknown opcode ("+opcode+") for non-control frame received.");
 				}
 			}
 			else
 			{
 				if (opcode != WSRequest.OPCODE_CONTINUATION_FRAME)
 				{
 					throw new SWSSIncorrectOpcode("The opcode for all frames other than the"+
 							" first, must be set to 0 (continuation)");
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Based on the opcode of the incoming frame, we prepare the response.
 	 */
 	public void respond()
 	throws IOException
 	{
 		response = server.handler.response(this);
 		System.out.println("Sending a response");
 
 		sendResponse(response.getResponse(), socket);
 		System.out.println(new String(response.getResponse()));
 	}
 	
 	/**
 	 * Reads the first byte of the incoming data stream and establishes the
 	 * type of the request and the sets the various flags.
 	 * @param status
 	 */
 	private void setStatus(int status)
 	{
 		if ((status & 128) == 128)
 			FIN = true;
 		if ((status & 64) == 64)
 			RSV1 = true;
 		if ((status & 32) == 32)
 			RSV2 = true;
 		if ((status & 16) == 16)
 			RSV3 = true;
 		
 		// if the opcode is 8, then we're closing
 		opcode = (byte) (status & (1+2+4+8));
 		if (opcode == WSRequest.OPCODE_CONNECTION_CLOSE)
 		{
 			System.out.println("Received request to close the connection. Returning closing frame");
 			closing = true;
 			response = new WSResponse(WSRequest.OPCODE_CONNECTION_CLOSE, "");
 		}
 
 		System.out.println("Opcode: "+opcode);
 	}
 	
 	/**
 	 * Parse the payload length data so that we know how much data to read in during
 	 * the following actions.
 	 * @param request
 	 * @throws IOException
 	 */
 	private void setPayload(InputStream request) throws IOException
 	{
 		byte payloadHeader = (byte) request.read();
 		
 		// first establish any masking..
 		if ((payloadHeader & 128) == 128)
 			masked = true;
 		
 		// then get the intial payload size value
 		payloadSize = payloadHeader & 127;
 		
 		// if the payload size is bigger than 125, we need to parse it in..
 		if (payloadSize == 126) // 126 means the length is contained in the next 2 bytes
 		{
 			payloadSize = request.read() << 8 + request.read();
 		}
 		else if (payloadSize == 127) // 127 means the length is in the next 8 bytes
 		{
 			payloadSize = 0;
 			for (int i=0; i<8; i++)
 			{
 				payloadSize += request.read() << i;
 			}
 		}
 		
 		// a little debug
 		System.out.println("Payload: " + payloadSize);
 	}
 	
 	private byte[] unmask(byte[] buffer, int length, int mask)
 	{
 		byte[] message = new byte[length];
 		byte[] newMask = intToByteArray(mask);
 		
 		for (int i=0; i<length; i++)
 		{
 			message[i] = (byte) (newMask[i%4] ^ buffer[i]);
 			// System.out.println(Byte.toString(buffer[i]) + ", " + Byte.toString(message[i]) + ", " + Byte.toString(newMask[i%4]));
 		}
 		
 		return message;
 	}
 	
 	/**
 	 * Starts a new WebSocket session by upgrading to the web socket
 	 */
 	private String startSession(InputStream in)
 	throws Exception
 	{
 		WSUpgradeHandler upgradeHandler = new WSUpgradeHandler(new BufferedReader(new InputStreamReader(in)));
 		if (upgradeHandler.isUpgradeRequest(this.server) ) {
 			// generate upgrade response
 			StringBuilder resp = new StringBuilder();
 			resp.append("HTTP/1.1 101 Switching Protocols\n");
 			resp.append("Upgrade: websocket\n");
 			resp.append("Connection: Upgrade\n");
 			resp.append("Sec-WebSocket-Accept: " + upgradeHandler.getAcceptKey() + "\n");
 			resp.append("\n");
 			System.out.println("Connection established, upgrading to WebSocket");
 			return resp.toString();
 		} else {
 			throw new SWSSUpgradeException("Connection upgrade disallowed. Reason: " + upgradeHandler.getFailure());
 		}
 	}
 	
 	/***
 	 * We take a responseBody (String or byte[]) and push it out through the provided socket
 	 * @param responseBody
 	 * @param socket
 	 * @throws IOException
 	 */
 	private void sendResponse(String responseBody, Socket socket) throws IOException {
 		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
 		bos.write(responseBody.getBytes());
 		bos.flush();
 	}
 	
 	private void sendResponse(byte[] responseBody, Socket socket) throws IOException {
 		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
 		bos.write(responseBody);
 		bos.flush();
 	}
 	
 	/**
 	 * resets all the flags. This should be called when the initial constructor
 	 * is run and also after a FIN frame is received.
 	 */
 	private void resetFlags()
 	{
 		FIN			= false;
 		RSV1		= false;
 		RSV2		= false;
 		RSV3		= false;
 		opcode		= 0;
 		masked		= false;
 		mask		= 0;
 		payloadSize	= -1;
 		payload.clear();
 		payloadType	= null;
 		FrameCount	= 0;
 	}
 	
 	/**
 	 * helper function to convert an int into a 4 byte array
 	 * @param a
 	 * @return
 	 */
 	public static byte[] intToByteArray(int a)
 	{
 	    return new byte[] {
 	        (byte) ((a >> 24) & 0xFF),
 	        (byte) ((a >> 16) & 0xFF),   
 	        (byte) ((a >> 8) & 0xFF),   
 	        (byte) (a & 0xFF)
 	    };
 	}
 	
 	
 	// ACCESSORS
 	public String getMessage()
 	{
 		int numBytes = payload.size();
 		byte[] message = new byte[numBytes];
 		for (int i=0; i<numBytes; i++)
 			message[i] = payload.get(i);
		return new String(message, "UTF-8");
 	}
 
 }
