 /**
  * Wikidot API Open Source Project
  * Java API library
  * 
  * Lead developer: Shane Smith (leiger)
  * Developer Website: www.shane-smith.com
  * Project Website: api-java.wikidot.com
  * 
  * Licensed under GNU GPL v3
  **/
 
 package com.shanesmith.wikidot;
 
 import org.apache.ws.commons.util.NamespaceContextImpl;
 import org.apache.xmlrpc.common.TypeFactoryImpl;
 import org.apache.xmlrpc.common.XmlRpcController;
 import org.apache.xmlrpc.common.XmlRpcStreamConfig;
 import org.apache.xmlrpc.parser.NullParser;
 import org.apache.xmlrpc.parser.TypeParser;
 import org.apache.xmlrpc.serializer.NullSerializer;
 
 /**
 * This class extends TypeFactoryImpl and caters for the Wikidot API's use of "NIL" as a data type.
  * @author Shane Smith (leiger)
  */
 public class WikidotTypeFactory extends TypeFactoryImpl
 {
 	/**
 	 * TODO: Documentation
 	 * @param pController
 	 */
 	public WikidotTypeFactory(XmlRpcController pController)
 	{
 		super(pController);
 	}
 	
 	/**
 	 * TODO: Documentation
 	 */
 	@Override
 	public TypeParser getParser(XmlRpcStreamConfig pConfig, NamespaceContextImpl pContext, String pURI, String pLocalName)
 	{
 		if ("".equals(pURI) && NullSerializer.NIL_TAG.equals(pLocalName))
 		{
 			return new NullParser();
 		}
 		else
 		{
 			return super.getParser(pConfig, pContext, pURI, pLocalName);
 		}
 	}
 }
