 /*
  * Copyright (C) 2011 asksven
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.asksven.commandcenter.valueobjects;
 
 /**
  * @author sven
  *
  */
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.asksven.commandcenter.exec.DataStorage;
 import com.google.gson.Gson;
 
 /**
  * Singleton management class for all accesses to command collections
  * @author sven
  *
  */
 public class CollectionManager
 {
     
     private static final String TAG = "CollectionManager";
     private static CollectionManager m_instance;
     private static Context m_context;
     
     private HashMap<String, CommandCollection> m_collections = null;
      
     public static final String USER_COLLECTION_NAME = "User";
     
     public static CollectionManager getInstance(Context ctx)
     {
     	if (m_instance == null)
     	{
     		m_instance = new CollectionManager();
     		m_instance.init(ctx);
     	}
     	
     	return m_instance;
     }
     
     /**
      * Returns a CommandCollection for a given name
      * @param strName the name of a collection
      * @return a command collection or null
      */
     public CommandCollection getCollectionByName(String strName, boolean updateCache)
     {
     	if (updateCache)
     	{
     		m_collections.get(strName).updateCache();
     	}
     	return m_collections.get(strName);
     }
     
     /**
      * Returns a Set of the names of all existing collections
      * @return
      */
     public Set<String> getCollectionNames()
     {
     	return m_collections.keySet();
     }
     
     /**
      * Returns the list of available commands to the Locale plugin (if plugin enabled)
      * @return a list of allowed commands to be called by the plugin
      */
     public ArrayList<String> getAvailableCommandsForPlugin()
     {
     	ArrayList<String> myRet = new ArrayList<String>();
     	
     	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(m_context);
     	boolean bAllowPlugin = preferences.getBoolean("allowPlugin", false);
 
     	if (!bAllowPlugin)
     	{
     		Log.i(TAG, "Plugin accesd is disabled: Returning message");
     		myRet.add("Plugin access is disabled");
     		return myRet;
     	}
     	
     	Set<String> collections = this.getCollectionNames();
     	
     	Iterator<String> it = collections.iterator();
     	while (it.hasNext())
     	{
     	    // Get element
     	    String collectionName = it.next();
     	    CommandCollection collection = this.getCollectionByName(collectionName, false);
     	    for (int i=0; i<collection.getEntries().size(); i++)
     	    {
     	    	Command command = collection.getEntries().get(i);
     	    	if (command.getCommandValues().equals(""))
     	    	{
     	    		myRet.add(command.getCommand());
     	    	}
     	    }
     	}
 
     	Log.i(TAG, "Returning " + myRet.size() + " commands to plugin");
     	return myRet;
     }
     
     /**
      * Returns the command object given a command (if Plugin enabled) 
      * @param strCommand the command
      * @return a Command object is any was found
      */
     public Command getCommandByString(String strCommand)
     {
     	Log.i(TAG, "Plugin trying to retrieve command: " + strCommand);
     	Command myRet = null;
     	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(m_context);
     	boolean bAllowPlugin = preferences.getBoolean("allowPlugin", false);
 
     	if (!bAllowPlugin)
     	{
     		Log.i(TAG, "Plugin accesd is disabled: Returning null");
     		return myRet;
     	}
 
     	Set<String> collections = this.getCollectionNames();
     	
     	Iterator<String> it = collections.iterator();
     	while (it.hasNext())
     	{
     	    // Get element
     	    String collectionName = it.next();
     	    CommandCollection collection = this.getCollectionByName(collectionName, false);
     	    for (int i=0; i<collection.getEntries().size(); i++)
     	    {
     	    	Command command = collection.getEntries().get(i);
     	    	if (command.getCommand().equals(strCommand))
     	    	{
     	    		// return only commands for batch.... they can not contain values
     	    		if (command.getCommandValues().equals(""))
     	    		{
 	    	    		myRet = command;
 	    	    		Log.i(TAG, "Command returned: " + command.toString());
 	    	    		break;
     	    		}
     	    	}
     	    }
     	}
 
     	return myRet;
     }
 
     /**
      * Initialize: read all CommandCollections if some exist, if not initialize the
      * storage with the samples from the project assets
      */
     private void init(Context ctx)
     {
     	m_context = ctx;
    	// check if the private storage exists. If not create it
     	if (!CommandsIO.getInstance(ctx).externalStorageEnvironmentReady())
     	{
     		CommandsIO.getInstance(ctx).createExternalStorageEnvironment();
     	}
     	
    	// always populate with samples
   		CommandsIO.getInstance(ctx).CopyAssets();
     	
     	ArrayList<String> collections = CommandsIO.getInstance(ctx).getCollectionFilenames();
     	m_collections = new HashMap<String, CommandCollection>();
     	
     	for (int i = 0; i < collections.size(); i++)
     	{
     		String strFileName = collections.get(i);
     		
     		CommandCollection commands = CommandReaderWriter.readFile(ctx, strFileName);
     		// never import a collection with the name "User"
     		if (!commands.getTitle().equals(CollectionManager.USER_COLLECTION_NAME))
     		{
     			m_collections.put(commands.getTitle(), commands);
     		}
     		else
     		{
     			Log.e(TAG, "Collection with name " + commands.getTitle()
     					+ " from file " + strFileName
     					+ " was not imported");
     		}
     	}
     	
     	// add user commands from database
     	CommandDBHelper myDB = new CommandDBHelper(ctx);
     	CommandCollection commands = myDB.getCommandCollection();
     	commands.setEditable(true);
     	m_collections.put(commands.getTitle(), commands);
     }
         
 
 }
