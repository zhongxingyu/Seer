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
 package io.niowire.serversource;
 
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class acts as a server source by watching the configured directory and
  * parsing the files within it as JSON configuration files. On the first run it
  * will return all the files in this directory as being added. And from then on
  * it will return either added modified or deleted for each of the entries.
  *
  * @configparam directory {@link java.lang.String} - the directory that the
  * server configuration files are located
  *
  * @author Trent Houliston
  */
 public class DirectoryServerSource implements NioServerSource
 {
 
 	private static final Logger LOG = LoggerFactory.getLogger(DirectoryServerSource.class);
 	/**
 	 * Our Directory
 	 */
 	private File dir;
 	/**
	 * A GSON instance to parse the json files
 	 */
	private static Gson gson = new GsonBuilder().serializeNulls().create();
 	/**
 	 * Our current state
 	 */
 	private HashMap<File, Long> servers = new HashMap<File, Long>(1);
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void configure(Map<String, Object> configuration) throws Exception
 	{
 		//Get our directory from the configuration
 		String directory = (String) configuration.get("directory");
 
 		//Get the path
 		dir = new File(directory);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void close() throws IOException
 	{
 		servers = null;
 	}
 
 	/**
 	 * Gets the changes to the servers since this method was last run. It will
 	 * on first run get all the servers in the directory, and then on subsequent
 	 * runs it will get the changes (servers that have been added modified or
 	 * deleted).
 	 *
 	 * @return a map of NioServerConfiguration's to the event that occurred on
 	 *            them (added deleted or modified)
 	 *
 	 * @throws IOException
 	 */
 	@Override
 	public Map<NioServerDefinition, Event> getChanges() throws IOException
 	{
 		//Where we put the changes
 		HashMap<NioServerDefinition, Event> changes = new HashMap<NioServerDefinition, Event>(1);
 
 		//Get our files
 		for (File file : dir.listFiles())
 		{
 			//Check if we already had this server
 			if (!servers.containsKey(file))
 			{
 				//Try to parse the message into an object
 				FileReader fr = null;
 				try
 				{
 					//Read the file and make a server definition from it using the platforms default encoding
 					fr = new FileReader(file);
 					NioServerDefinition srv = gson.fromJson(fr, NioServerDefinition.class);
 					srv.setId(file.getName());
 
 					//Add in that this server has been added
 					changes.put(srv, Event.SERVER_ADD);
 
 					//Add it to the map
 					servers.put(file, file.lastModified());
 
 					//Log that it was added
 					LOG.info("Server definition {} was added", file.getName());
 				}
 				catch (JsonSyntaxException ex)
 				{
 					//Put it in the map so we don't keep looking at it
 					servers.put(file, file.lastModified());
 
 					//If there was an exception ignore it
 					LOG.info("Server definition {} was invalid, Ignoring", file.getName());
 				}
 				finally
 				{
 					fr.close();
 				}
 			}
 			//If we have the server but it's date modified has changed
 			else if (servers.get(file) != file.lastModified())
 			{
 				//Try to build a server defintion
 				FileReader fr = null;
 				try
 				{
 					//Read the file and make a server definition from it
 					fr = new FileReader(file);
 					NioServerDefinition srv = gson.fromJson(fr, NioServerDefinition.class);
 					srv.setId(file.getName());
 
 					//Put it in the servers list
 					servers.put(file, file.lastModified());
 
 					//Add that we are updating the server
 					changes.put(srv, Event.SERVER_UPDATE);
 
 					//Output that we are updating it
 					LOG.info("Server definition {} was updated", file.getName());
 				}
 				catch (JsonSyntaxException ex)
 				{
 					//Put it in the servers list so we don't look at it again
 					servers.put(file, file.lastModified());
 
 					//Output that we are still using the old definition
 					LOG.info("Server definition {} was invalid, Using Previous Definition", file.getName());
 				}
 				finally
 				{
 					fr.close();
 				}
 			}
 		}
 
 		//Get a list of all the servers we currently know about
 		HashSet<File> files = new HashSet<File>(servers.size());
 		files.addAll(servers.keySet());
 
 		//Remove the ones that still exist
 		files.removeAll(Arrays.asList(dir.listFiles()));
 
 		//What remains now is all the server which have been deleted
 		for (File f : files)
 		{
 			//Make a new server definition with the appropriate id (there is no more information)
 			NioServerDefinition srv = new NioServerDefinition();
 			srv.setId(f.getName());
 			changes.put(srv, Event.SERVER_REMOVE);
 
 			//Remove it from the servers list
 			servers.remove(f);
 
 			//Log that we removed it
 			LOG.info("Server definition {} was removed", f.getName());
 		}
 
 		//Return our result
 		return changes;
 	}
 }
