 package com.socialcomputing.wps.server.plandictionary.connectors.datastore.social.portablecontacts;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Set;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ArrayNode;
 import org.jdom.Element;
 
 import com.socialcomputing.wps.server.plandictionary.connectors.WPSConnectorException;
 import com.socialcomputing.wps.server.plandictionary.connectors.datastore.Attribute;
 import com.socialcomputing.wps.server.plandictionary.connectors.datastore.AttributePropertyDefinition;
 import com.socialcomputing.wps.server.plandictionary.connectors.datastore.Entity;
 import com.socialcomputing.wps.server.plandictionary.connectors.datastore.social.SocialEntityConnector;
 import com.socialcomputing.wps.server.plandictionary.connectors.utils.UrlHelper;
 import com.socialcomputing.wps.server.plandictionary.connectors.utils.UrlHelper.Type;
 
 public class FacebookEntityConnector extends SocialEntityConnector {
 
     private static final ObjectMapper mapper = new ObjectMapper();
 
     protected UrlHelper oAuth2Helper;
 
     static FacebookEntityConnector readObject(org.jdom.Element element) {
         FacebookEntityConnector connector = new FacebookEntityConnector( element.getAttributeValue("name"));
         connector._readObject( element);
         return connector;
     }
     
     public FacebookEntityConnector(String name) {
         super(name);
         oAuth2Helper = new UrlHelper();
     }
 
     @Override
     public void _readObject(Element element) {
         super._readObject(element);
         oAuth2Helper.readObject( element);
         for( Element property: (List<Element>)element.getChildren( "Facebook-property")) {
             attributeProperties.add( new AttributePropertyDefinition( property.getAttributeValue( "id"), property.getAttributeValue( "entity")));
         }
     }
 
 
     @Override
     public void openConnections(int planType, Hashtable<String, Object> wpsparams) throws WPSConnectorException {
         super.openConnections( planType, wpsparams);
         oAuth2Helper.openConnections( planType, wpsparams);
         String token = "";
         for( String p : oAuth2Helper.getResult().split("&")) {
             if( p.startsWith( "access_token=")) {
                 token = p.substring( p.indexOf( '=') + 1);
                 break;
             }
         }
         
         try {
             String kind = ( String)wpsparams.get("kind");
             if( kind == null || kind.equalsIgnoreCase( "friends")) {
                 // Liste amis
                 UrlHelper urlHelper = new UrlHelper();
                 urlHelper.setUrl( "https://graph.facebook.com/me/friends");
                 urlHelper.addParameter( "access_token", token);
                 urlHelper.openConnections( planType, wpsparams);
                 
                 JsonNode node = mapper.readTree(urlHelper.getStream());
                 List<String> friendslist = new ArrayList<String>();
                 ArrayNode friends = (ArrayNode)node.get( "data");
                 for( JsonNode friend : friends) {
                     addPerson(friend.get("id").getTextValue()).addProperty("name", friend.get("name").getTextValue());
                     friendslist.add(friend.get("id").getTextValue());
                 }
                 
                 // Mes infos
         //        String url = "https://graph.facebook.com/me";
         //        UrlHelper uh = new UrlHelper();
         //        uh.setUrl(url);
         //        uh.addParameter("access_token", oAuth2Helper.getToken());
         //        uh.openConnections( planType, wpsparams);
         //        JSONObject me =  (JSONObject)JSONValue.parse(new InputStreamReader(uh.getStream()));
         //        addPerson((String)me.get("id")).addProperty("name", me.get("name"));
                 
                // Les amis entre eux 
                 for (int i = 0 ; i < friendslist.size() -1 ; i++) {
                     StringBuilder sb1 = new StringBuilder();
                     StringBuilder sb2 = new StringBuilder();
                     for (int j = i + 1 ; j < friendslist.size() ; j++) {
                         sb1.append(friendslist.get(i)).append(",");
                         sb2.append(friendslist.get(j)).append(",");
                     }
                     String areFriends = "https://api.facebook.com/method/friends.areFriends";
                     UrlHelper uh1 = new UrlHelper();
                     uh1.setUrl(areFriends);
                     uh1.setType( Type.POST);
                     uh1.addParameter("uids1", sb1.toString());
                     uh1.addParameter("uids2", sb2.toString());
                     uh1.addParameter("access_token", token);
                     uh1.addParameter("format", "json");
                     uh1.openConnections( planType, wpsparams);
                     ArrayNode r = ( ArrayNode)mapper.readTree(uh1.getStream());
                     for (JsonNode rs : r) {
                         //System.out.println(rs.get("uid1") + "=>" + rs.get("uid2") + "=>" + rs.get("are_friends"));
                         if ( rs.get("are_friends").getBooleanValue())
                             setFriendShip(String.valueOf(rs.get("uid1").getLongValue()), String.valueOf(rs.get("uid2").getLongValue()).toString());
                     }
                 }
                 
                // Je suis ami avec tous mes amis
                 //setFriendShip((String)me.get("id"), friendslist);
                 
                 // AJout des propriétés d'entités sur les attributs
                 setEntityProperities();
             }
             else {
                 UrlHelper urlHelper = new UrlHelper();
                 urlHelper.setUrl( "https://graph.facebook.com/me/friends");
                 urlHelper.addParameter( "access_token", token);
                 urlHelper.openConnections( planType, wpsparams);
                 
                 JsonNode node = mapper.readTree(urlHelper.getStream());
                 ArrayNode friends = (ArrayNode)node.get( "data");
 
                 // My self
                 UrlHelper uh = new UrlHelper();
                 uh.setUrl( "https://graph.facebook.com/me");
                 uh.addParameter("access_token", token);
                 uh.openConnections( planType, wpsparams);
                 JsonNode me = mapper.readTree(uh.getStream());
                 friends.add( me);
                 
                 for( JsonNode friend : friends) {
                     Attribute attribute = addAttribute( friend.get("id").getTextValue());
                     attribute.addProperty( "name", friend.get("name").getTextValue());
 
                     UrlHelper urlHelper2 = new UrlHelper();
                     urlHelper2.setUrl( "https://graph.facebook.com/" + friend.get("id").getTextValue() + "/" + kind);
                     urlHelper2.addParameter( "access_token", token);
                     urlHelper2.openConnections( planType, wpsparams);
                     
                     JsonNode node2 = mapper.readTree(urlHelper2.getStream());
                     ArrayNode kinds = (ArrayNode)node2.get( "data");
                     for( JsonNode curkind : kinds) {
                         if (curkind.get("id") != null && curkind.get("name") != null) {
                             Entity entity = addEntity( curkind.get("id").getTextValue());
                             entity.addProperty( "name", curkind.get("name").getTextValue());
                             entity.addAttribute( attribute, 1);
                         }
                     }
                 }
                 // Delete entities with only one attribute 
                 Set<String> toRemove = new HashSet<String>();
                 for( Entity entity : m_Entities.values()) {
                     if( entity.getAttributes().size() == 1) {
                         toRemove.add( entity.getId());
                     }
                 }
                 for( String id : toRemove) {
                     removeEntity( id);
                 }
                 for( Attribute attribute : m_Attributes.values()) {
                     addEntityProperties( attribute);
                 }
                 
             }
         }
         catch (Exception e) {
             e.printStackTrace();
             throw new WPSConnectorException( "openConnections", e);
         }
     }
 
 
     @Override
     public void closeConnections() throws WPSConnectorException {
         super.closeConnections();
         oAuth2Helper.closeConnections();
     }
     
 }
