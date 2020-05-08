 package com.soueidan.extensions.zone.events;
 
 import java.sql.SQLException;
 
 import java.sql.*;
 import com.smartfoxserver.bitswarm.sessions.ISession;
 import com.smartfoxserver.v2.core.*;
 import com.smartfoxserver.v2.db.IDBManager;
 import com.smartfoxserver.v2.entities.data.*;
 import com.smartfoxserver.v2.exceptions.SFSException;
 import com.smartfoxserver.v2.exceptions.SFSLoginException;
 import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
 import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
 import com.soueidan.extensions.zone.core.ZoneExtension;
 
 public class UserLoginEventHandler extends BaseServerEventHandler {
 	
 	private SFSObject _data;
 	private ISession _session;
 	
 	@Override
 	public void handleServerEvent(ISFSEvent event) throws SFSException {
 		_session = (ISession) event.getParameter(SFSEventParam.SESSION);
 		
 		_data = (SFSObject) event.getParameter(SFSEventParam.LOGIN_IN_DATA);
 		
 		String session = _data.getUtfString(ZoneExtension.USER_SESSION);
 		if ( session.isEmpty() ) {
 			throw new SFSException("No session key specified");
 		}
 	
 		IDBManager dbManager = getParentExtension().getParentZone().getDBManager();
         
         try {
         	Connection connection = dbManager.getConnection();
         	
         	PreparedStatement stmt = connection.prepareStatement("SELECT id, nickname, password_digest FROM users where session=? limit 1");
     		stmt.setString(1, session);
     		
         	ResultSet res = stmt.executeQuery();
 
         	if (!res.first()) {
         		throw new SFSLoginException("Login failed for user");
 			}
         		
             String nickname = res.getString("nickname");
             String passwordDigest = res.getString("password_digest");
             Boolean isRegistered = true;
            if ( passwordDigest == null ) {
             	isRegistered = false;
             }
             
             ISFSObject outData = (ISFSObject) event.getParameter(SFSEventParam.LOGIN_OUT_DATA);
             outData.putUtfString(SFSConstants.NEW_LOGIN_NAME, nickname);
             
             _session.setProperty(ZoneExtension.USER_ID, res.getInt("id"));
             _session.setProperty(ZoneExtension.USER_REGISTERED, isRegistered);
             _session.setProperty(ZoneExtension.ROOM_NAME, _data.getUtfString(ZoneExtension.ROOM_NAME));
             
 			// Return connection to the DBManager connection pool
 			connection.close();
 			
         }
         catch (SQLException e)
         {
             trace(ExtensionLogLevel.WARN, "SQL Failed: " + e.toString());
         }
 	}
 }
