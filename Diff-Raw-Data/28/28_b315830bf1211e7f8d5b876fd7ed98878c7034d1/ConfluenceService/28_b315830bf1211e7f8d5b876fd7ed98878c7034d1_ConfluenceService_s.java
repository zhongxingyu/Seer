 package com.enonic.cms.plugin.confluence.service;
 
 import redstone.xmlrpc.XmlRpcArray;
 import redstone.xmlrpc.XmlRpcClient;
 import redstone.xmlrpc.XmlRpcFault;
 import redstone.xmlrpc.XmlRpcStruct;
 
 import java.net.URL;
 import java.util.Hashtable;
 
 public final class ConfluenceService
 {
     private final XmlRpcClient client;
 
     public ConfluenceService( URL url )
     {
         this.client = new XmlRpcClient( url, true );
     }
 
     /**
      * Do invovation.
      */
     private Object invoke( String method, Object... params )
         throws XmlRpcFault
     {
         return this.client.invoke( "confluence1." + method, params );
     }
 
     /**
      * Do the login.
      */
     public String login( String user, String password )
         throws XmlRpcFault
     {
         return (String) invoke( "login", user, password );
     }
 
     /**
      * Do the logout.
      */
     public boolean logout( String ticket )
         throws XmlRpcFault
     {
         return (Boolean) invoke( "logout", ticket );
     }
 
     /**
      * Return the space.
      */
     public XmlRpcStruct getSpace( String ticket, String spaceKey )
         throws XmlRpcFault
     {
         return (XmlRpcStruct) invoke( "getSpace", ticket, spaceKey );
     }
 
     /**
      * Return the child pages.
      */
     public XmlRpcArray getChildren( String ticket, String pageId )
         throws XmlRpcFault
     {
         return (XmlRpcArray) invoke( "getChildren", ticket, pageId );
     }
 
     /**
      * Return the page.
      */
     public XmlRpcStruct getPage( String ticket, String pageId )
         throws XmlRpcFault
     {
         return (XmlRpcStruct) invoke( "getPage", ticket, pageId );
     }
 
     /**
      * Return the page.
      */
     public String renderContent( String ticket, String spaceKey, String pageId )
         throws XmlRpcFault
     {
         Hashtable<String, String> params = new Hashtable<String, String>();
         params.put( "style", "clean" );
         return (String) invoke( "renderContent", ticket, spaceKey, pageId, "", params );
     }
 
     public XmlRpcStruct getAttachment( String ticket, String pageId, String fileName )
         throws XmlRpcFault
     {
         return (XmlRpcStruct) invoke( "getAttachment", ticket, pageId, fileName, "0" );
     }
 
     public byte[] getAttachmentData( String ticket, String pageId, String fileName )
         throws XmlRpcFault
     {
         return (byte[]) invoke( "getAttachmentData", ticket, pageId, fileName, "0" );
     }
 }
