 /**
  * Copyright (c) 2012 Michael Rolli - github.com/mrolli/emma
  * All rights reserved.
  * 
  * This work is licensed under the Creative Commons
  * Attribution-NonCommercial-ShareAlike 3.0 Switzerland
  * License. To view a copy of this license, visit
  * http://creativecommons.org/licenses/by-nc-sa/3.0/ch/
  * or send a letter to Creative Commons, 444 Castro Street,
  * Suite 900, Mountain View, California, 94041, USA.
  */
 package ch.rollis.emma.context;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import ch.rollis.emma.request.Request;
 
 /**
  * Manages all server contexts of the web server.
  * <p>
  * Central point of all server context. Based on a request a specific server
  * context is served.
  * 
  * @author mrolli
  * 
  */
 public class ServerContextManager {
     /**
      * Map consisting of server contexts indexed by theri server name.
      */
     private final HashMap<String, ServerContext> contexts;
 
     /**
      * Class constructor simply initializes an empty manager.
      */
     public ServerContextManager() {
         contexts = new HashMap<String, ServerContext>();
     }
 
     /**
      * Returns the server context for a given request.
      * <p>
      * The server context manager investigates the host request header of
      * HTTP/1.1 and returns the requested context if available. In the following
      * cases the default server context is returned instead:
      * <ul>
      * <li>Host specified in host header could not be found.</li>
      * <li>No host header found in HTTP/1.1 request.</li>
      * </ul>
      * 
      * @param request
      *            The request to resolve the context for
      * @return The server context that handles the given request
      * @throws ServerContextException
      *             In case a default context cannot be found
      */
     public ServerContext getContext(final Request request) throws ServerContextException {
         String host = request.getHeader("Host");
        if (contexts.containsKey(host)) {
            return contexts.get(host);
        } else {
            return getDefaultContext();
         }
     }
 
     /**
      * Adds a server context to the manager.
      * <p>
      * The given server context is stored by its server name and additionally by
      * each server alias defined for it. An exception is thrown if duplicate
      * server name/server aliases are encountered. One server context can be
      * setup to serve as the default server context. If such a default sever
      * context already exists and a second is stored as default context, an
      * exception will be thrown.
      * 
      * @param context
      *            The server context to store
      * @throws ServerContextException
      *             In case a context for server name already exists or default
      *             context already exists
      */
     public void addContext(final ServerContext context) throws ServerContextException {
         String name = context.getServerName();
         ArrayList<String> aliases = context.getServerAliases();
 
         if (contexts.containsKey(name)) {
             throw new ServerContextException(
                     "A context with the given server name already exists: " + name);
         }
         contexts.put(name, context);
 
         for (String alias : aliases) {
             if (contexts.containsKey(alias)) {
                 throw new ServerContextException(
                         "A context with the given server name already exists: " + name);
             }
             contexts.put(alias, context);
         }
 
         if (context.isDefault()) {
             if (hasDefaultContext()) {
                 throw new ServerContextException("Default context is already registered");
             }
             contexts.put("default", context);
         }
     }
 
     /**
      * Checks if a default server context is setup.
      * 
      * @return True if a default sever context is setup; false otherwise
      */
     public boolean hasDefaultContext() {
         return contexts.containsKey("default");
     }
 
     /**
      * Returns the default server context setup.
      * 
      * @return The default server context
      * @throws ServerContextException
      *             In case no default context is setup
      */
     public ServerContext getDefaultContext() throws ServerContextException {
         if (!hasDefaultContext()) {
             throw new ServerContextException("No default context is configured");
         }
         return contexts.get("default");
     }
 }
 
