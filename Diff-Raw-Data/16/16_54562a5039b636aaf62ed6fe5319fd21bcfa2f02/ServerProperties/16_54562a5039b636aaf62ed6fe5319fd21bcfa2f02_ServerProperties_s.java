 /*
  * Copyright (C) 2012 Helsingfors Segelklubb ry
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package fi.hoski.remote.ui;
 
 import com.google.appengine.api.datastore.Key;
 import fi.hoski.datastore.repository.DataObject;
 import fi.hoski.datastore.repository.DataObjectModel;
 import fi.hoski.datastore.repository.MapData;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 /**
  * @author Timo Vesalainen
  */
 public class ServerProperties extends DataObject
 {
     public static final String Kind = "ServerCredentials";
     
     public static final String Server = "remoteserver";
     public static final String Username = "remoteuser";
     public static final String Password = "remotepassword";
     public static final String SavePassword = "savepassword";
     public static final String Tables = "tables";
     public static final String SuperUser = "super-user";
     public static final String SupportsZonerSMS = "supports-zoner-sms";
     
     public static final DataObjectModel Model = new DataObjectModel(Kind);
 
     static
     {
         Model.property(Server);
         Model.property(Username);
         Model.property(Password);
         Model.setPassword(Password);
        Model.property(SavePassword, Boolean.class, false, false, false);
         Model.property(Tables);
        Model.property(SuperUser, Boolean.class, false, false, false);
        Model.property(SupportsZonerSMS, Boolean.class, false, false, false);
     }
     
     public ServerProperties(Properties properties)
     {
         super(new MapData(Model, properties));
     }
 
     public Properties getProperties()
     {
         Properties properties = new Properties();
         for (Entry<String,Object> entry : getAll().entrySet())
         {
             properties.setProperty(entry.getKey(), entry.getValue().toString());
         }
         return properties;
     }
     
     public String[] getTables()
     {
         String tables = (String) get(Tables);
         if (tables != null)
         {
             return tables.split(",");
         }
         else
         {
             return new String[]{};
         }
     }
     public String getPassword()
     {
         return (String) get(Password);
     }
 
     public void setPassword(String password)
     {
         set(Password, password);
     }
 
     public String getUsername()
     {
         return (String) get(Username);
     }
 
     public void setUsername(String username)
     {
         set(Username, username);
     }
 
     public String getServer()
     {
         return (String) get(Server);
     }
 
     public void setServer(String server)
     {
         set(Server, server);
     }
 
     public void setSavePassword(boolean save)
     {
         set(SavePassword, save);
     }
     public boolean isSavePassword()
     {
        return (Boolean) get(SavePassword);
     }
     public boolean isZonerSMSSupported()
     {
        return (Boolean) get(SupportsZonerSMS);
     }
     public boolean isSuperUser()
     {
        return (Boolean) get(SuperUser);
     }
     @Override
     public Key createKey()
     {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
 }
