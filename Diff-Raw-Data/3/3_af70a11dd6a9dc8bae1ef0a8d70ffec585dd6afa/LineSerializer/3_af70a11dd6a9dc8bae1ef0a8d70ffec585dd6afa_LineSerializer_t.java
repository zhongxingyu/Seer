 /**
  * This file is part of Niowire.
  *
  * Niowire is free software: you can redistribute it and/or modify it under the
  * terms of the Lesser GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  *
  * Niowire is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more
  * details.
  *
  * You should have received a copy of the Lesser GNU General Public License
  * along with Niowire. If not, see <http://www.gnu.org/licenses/>.
  */
 package io.niowire.serializer;
 
 import io.niowire.data.NioPacket;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.*;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This class serializes new line delimited binary blobs into strings, It then
  * calls deserializeString on the string so that this class can be overloaded by
  * other classes which use line based object serialization. It will also call
  * serializeString which can be used to convert data into a string form to be
  * sent to the client.
  *
  * @author Trent Houliston
  */
 public class LineSerializer extends DelimitedSerializer
 {
 
 	/**
 	 * Our charset that we are going to use
 	 */
 	private Charset CHARSET;
 	/**
 	 * Our encoder (note not thread safe)
 	 */
 	private CharsetDecoder DECODER;
 	/**
 	 * Our decoder (not not thread safe)
 	 */
 	private CharsetEncoder ENCODER;
 
 	/**
 	 * This method overrides from the Delimited serializer and deserializes the
 	 * passed blob into a string. It then passes this to the deserializeString
 	 * method to be further parsed.
 	 *
 	 * @param blob the byte buffer blob which has a window where our line is
 	 *
 	 * @return A NioPacket containing our string
 	 *
 	 * @throws CharacterCodingException if the encoding we are using does not
 	 *                                     exist
 	 */
 	@Override
 	protected List<NioPacket> deserializeBlob(ByteBuffer blob) throws CharacterCodingException
 	{
 		//Decode the string
 		String str = DECODER.decode(blob).toString();
 
		//Remove any carriage returns
		str = str.replaceAll("\r", "");

 		//Deserialize this
 		Object data = deserializeString(str);
 
 		//Return a new one element list
 		return Collections.singletonList(new NioPacket(context.getUid(), data));
 	}
 
 	/**
 	 * This method is used to serialize a string into a ByteBuffer by using our
 	 * charset to convert it into its binary form
 	 *
 	 * @param objects the objects to be serialized
 	 *
 	 * @return a byte buffer containing our serialized objects
 	 */
 	@Override
 	protected ByteBuffer serializeBlob(NioPacket packet) throws CharacterCodingException
 	{
 		StringBuffer buffer = new StringBuffer();
 
 		return ENCODER.encode(CharBuffer.wrap(String.format("%s\n", serializeString(packet))));
 	}
 
 	/**
 	 * This loads and configures this Line serializer with its character
 	 * encoding.
 	 *
 	 * @param configuration the configuration to load
 	 *
 	 * @throws Exception there is an exception while setting up the encoders
 	 */
 	@Override
 	public void configure(Map<String, Object> configuration) throws Exception
 	{
 		//Get our charset property
 		String charset = (String) configuration.get("charset");
 
 		//Build our charset, encoder and decoders
 		CHARSET = Charset.forName(charset);
 		DECODER = CHARSET.newDecoder().onMalformedInput(CodingErrorAction.IGNORE).onUnmappableCharacter(CodingErrorAction.IGNORE);
 		ENCODER = CHARSET.newEncoder().onMalformedInput(CodingErrorAction.IGNORE).onUnmappableCharacter(CodingErrorAction.IGNORE);
 	}
 
 	/**
 	 * Gets the delimiter for the line serializer, in this case we are using \n
 	 * rather then \r\n or String.format("%n") as we are not sure which they
 	 * will be using. Therefor if we use \n we will get all windows and linux
 	 * encoded data so long as we strip out all \r from the output.
 	 *
 	 * @return the bytes for a new line character in our charset
 	 */
 	@Override
 	protected byte[] getDelimiter()
 	{
 		return "\n".getBytes(CHARSET);
 	}
 
 	/**
 	 * This method is meant to be overridden by subclasses which use a line
 	 * based deserializeation method. The default implementation just returns
 	 * the string as the object.
 	 *
 	 * @param str the string to deserialize
 	 *
 	 * @return the string passed in
 	 */
 	protected Object deserializeString(String str)
 	{
 		return str;
 	}
 
 	/**
 	 * This method is meant to be overridden by subclasses which use a line
 	 * based serialization method. The default implementation just returns
 	 * obj.toString()
 	 *
 	 * @param obj the object to serialize
 	 *
 	 * @return a string representation of the object
 	 */
 	protected String serializeString(NioPacket obj)
 	{
 		return obj.getData().toString();
 	}
 }
