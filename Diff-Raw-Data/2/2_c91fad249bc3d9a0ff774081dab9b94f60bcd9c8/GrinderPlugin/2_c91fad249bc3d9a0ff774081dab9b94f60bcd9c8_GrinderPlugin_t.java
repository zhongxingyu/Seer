 // The Grinder
 // Copyright (C) 2001  Paco Gomez
 // Copyright (C) 2001  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.plugininterface;
 
 import java.util.Set;
 
 import net.grinder.util.GrinderProperties;
 
 
 /**
  * This interface defines the callbacks that an individual Grinder
  * thread can make on a plugin.
  *
  * @author Philip Aston
  * @version $Revision$
  */ 
 public interface GrinderPlugin
 {
     /**
      * This method is executed when the process starts. It is only
      * executed once.
      * @param processContext
      * @param testsFromPropertiesFile The tests defined in the
      * properties file. The plugin may or may not care about these.
      */
     public void initialize(PluginProcessContext processContext,
 			   Set testsFromPropertiesFile)
 	throws PluginException;
 
     /**
      * This method is called to create a handler for each thread.
      */
     public ThreadCallbacks createThreadCallbackHandler()
 	throws PluginException;
 
     /**
      * Returns a Set of Tests to use. The plugin may chose to simply
      * return the set passed to {@link #initialize}.
     * @see #initialize
      */
     public Set getTests() throws PluginException;
 }
