 /**
  *
  */
 package org.promasi.server.core;
 
 
 import java.beans.XMLDecoder;
 import java.net.ProtocolException;
 import org.apache.commons.lang.NullArgumentException;
 import org.promasi.protocol.request.LoginRequest;
 import org.xml.sax.InputSource;
 
 
 /**
  * @author m1cRo
  *
  */
 public class LoginProtocolState implements IProtocolState
 {
 	private ProMaSi _promasi;
 
 	public LoginProtocolState(ProMaSi promasi)
 	{
 		if(promasi==null)
 		{
 			throw new NullArgumentException("Wrong argument promasi");
 		}
 		_promasi=promasi;
 	}
 
 	/**
 	 *
 	 */
 	@Override
 	public void onReceive(ProMaSiClient client, String recData)throws ProtocolException
 	{
 		InputSource source=new InputSource(recData);
 		source.getByteStream();
 		XMLDecoder decoder=new XMLDecoder(source.getByteStream());
 		try
 		{
 			Object request=new LoginRequest("UserName","Password");
 			Object object=decoder.readObject();
			if(object instanceof LoginRequest)
 			{
 				LoginRequest loginRequest=(LoginRequest)object;
 				client.setClientId(loginRequest.getUserName());
 				_promasi.addUser(client);
 			}
 			else
 			{
 				throw new ProtocolException("Wrong protocol");
 			}
 			decoder.close();
 		}
 		catch(ArrayIndexOutOfBoundsException e)
 		{
 			throw new ProtocolException("Wrong protocol");
 		}
 		catch(NullArgumentException e)
 		{
 			throw new ProtocolException("Wrong protocol");
 		}
 		catch(IllegalArgumentException e)
 		{
 			throw new ProtocolException("Wrong protocol");
 		}
 	}
 }
