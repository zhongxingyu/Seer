 /**
  *
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, NCSA.  All rights reserved.
  *
  * Developed by:
  * The Automated Learning Group
  * University of Illinois at Urbana-Champaign
  * http://www.seasr.org
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal with the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject
  * to the following conditions:
  *
  * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimers.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimers in
  * the documentation and/or other materials provided with the distribution.
  *
  * Neither the names of The Automated Learning Group, University of
  * Illinois at Urbana-Champaign, nor the names of its contributors may
  * be used to endorse or promote products derived from this Software
  * without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
  * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
  *
  */
 
 package org.seasr.meandre.components.tools.db.mongodb;
 
import java.util.logging.Level;
import java.util.logging.Logger;

 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 
 import com.mongodb.MongoClient;
 import com.mongodb.MongoClientOptions;
 import com.mongodb.MongoClientURI;
 import com.mongodb.ReadPreference;
 
 /**
  * @author Ian Wood
  */
 
 @Component(
         name = "Mongo DB Client",
         creator = "Ian Wood",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.all,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "#INPUT, tools, database, db, mongo, mongodb",
         description = "This component creates a mongodb client and it's associated DB collection pool. " +
         		"Currently, only read preference options are supported. In particular, authentication, " +
         		"read preference tags and 'slaveOk' are not implemented.",
         dependency = { "protobuf-java-2.2.0.jar", "mongo-2.10.1.jar" }
 )
 public class MongoDBClient extends AbstractExecutableComponent {
 	protected static final String MONGODB_CONN_CLIENT = "mongodb_conn_client";
 	public static final String BSON_STRING = "mongodb_bson_object";
 
     //------------------------------ INPUTS -----------------------------------------------------
 
 /*    @ComponentInput(
             name = Names.PORT_LOCATION,
             description = "The location URI of the mongodb server. eg: localhost:27017" +
                           "<br>TYPE: java.lang.String"
     )
     protected static final String IN_URL = Names.PORT_LOCATION;
 */
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = MONGODB_CONN_CLIENT,
             description = "The mongodb Java client" +
                 "<br>TYPE: com.mongodb.MongoClient"
     )
     protected static final String OUT_MONGODB_CONN_CLIENT = MONGODB_CONN_CLIENT;
 
     //----------------------------- PROPERTIES ---------------------------------------------------
 
     @ComponentProperty(
             name = "server_location_URI",
             description = "The location of the mongodb server. In it's simplest form, this is the " +
             		"location of the mongodb server. It can be any valid mongodb URI string. " +
             		"The syntax is as follows;" +
             		"<br>mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]" +
             		"<br>eg: mongodb://localhost:22017?replicaSet=rs0" +
             		"In this case, any settings here overridden by other property settings.",
             defaultValue = "localhost:27017"
     )
     protected static final String PROP_MONGODB_SERVER_LOCATION = "server_location_URI";
 
     @ComponentProperty(
             name = "read_preference",
             description = "The strategy to select a replica set server. Possible values" +
             		" are 'primary', primaryPreferred', 'secondary', 'secondaryPreferred', 'nearest'.",
             defaultValue = ""
     )
     protected static final String PROP_MONGODB_READ_PREFERENCE = "read_preference";
 
    @ComponentProperty(
            name = "restrict_logging",
            description = "When True, set the log level of ReplicaSetStatus to SEVERE. " +
            		"This is useful if you know that one member of a replica set is down.",
            defaultValue = "True"
    )
    protected static final String PROP_MONGODB_RESTRICT_LOGGING = "restrict_logging";

     //--------------------------------------------------------------------------------------------
 
 
     protected MongoClient mongoClient = null;
     protected MongoClientOptions mongoClientOptions = null;
     protected MongoClientURI mongoClientURI = null;
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
     	String location = getPropertyOrDieTrying(PROP_MONGODB_SERVER_LOCATION, ccp);
         String readPreference = getPropertyOrDieTrying(PROP_MONGODB_READ_PREFERENCE, ccp);
         
         console.fine(String.format("Got mongo server location %s", location));
         
         try {
         	mongoClientURI = new MongoClientURI(location); 
         	mongoClient = new MongoClient(mongoClientURI);
         } catch (IllegalArgumentException e) {
         	console.warning(String.format("Could not interpret %s as a mongo URI, interpreting as a server:port string.", location));
         	console.warning(String.format("Exception reason: %s", e.getMessage()));
         	mongoClient = new MongoClient(location);
         }
         if (readPreference != "") mongoClient.setReadPreference(ReadPreference.valueOf(readPreference));
        //mongoClient.logger.
        if (Boolean.parseBoolean(getPropertyOrDieTrying(PROP_MONGODB_RESTRICT_LOGGING, ccp))) {
        	Logger.getLogger( "com.mongodb.ReplicaSetStatus" ).setLevel(Level.SEVERE);
        }
        
         console.fine(String.format("Connected to mongodb with URI/location %s ", location));
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         cc.pushDataComponentToOutput(OUT_MONGODB_CONN_CLIENT, mongoClient);
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         mongoClient.close();
         mongoClient = null;
     }
 
     //--------------------------------------------------------------------------------------------
 
 }
