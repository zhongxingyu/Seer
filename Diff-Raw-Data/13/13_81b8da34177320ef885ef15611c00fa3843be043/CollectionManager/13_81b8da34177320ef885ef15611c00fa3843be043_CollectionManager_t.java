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
 package com.asksven.controlcenter.valueobjects;
 
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
 
 import com.asksven.controlcenter.exec.DataStorage;
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
     private HashMap<String, CommandCollection> m_collections = null;
     
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
     public CommandCollection getCollectionByName(String strName)
     {
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
      * Initialize: read all CommandCollections if some exist, if not initialize the
      * storage with the samples from the project assets
      */
     private void init(Context ctx)
     {
     	// check if the private storage exists. If not create it
     	if (!CommandsIO.getInstance(ctx).externalStorageEnvironmentReady())
     	{
     		CommandsIO.getInstance(ctx).createExternalStorageEnvironment();
    	}
    	
    	// if there are no files populate with samples
    	if (CommandsIO.getInstance(ctx).getCollectionFilenames().size() == 0)
    	{
     		CommandsIO.getInstance(ctx).CopyAssets();
     	}
     	
     	ArrayList<String> collections = CommandsIO.getInstance(ctx).getCollectionFilenames();
     	m_collections = new HashMap<String, CommandCollection>();
     	
     	for (int i = 0; i < collections.size(); i++)
     	{
     		String strFileName = collections.get(i);
     		CommandCollection commands = CommandReaderWriter.readFile(ctx, strFileName);
     		m_collections.put(commands.getTitle(), commands);
     	}
     }
         
 
 }
