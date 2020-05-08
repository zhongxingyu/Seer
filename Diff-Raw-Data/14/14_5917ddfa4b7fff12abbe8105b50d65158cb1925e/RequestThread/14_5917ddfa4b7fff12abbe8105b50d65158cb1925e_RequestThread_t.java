 /*
  Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/
 
  This file is part of Mini Wegb Server / SimpleWebServer.
 
  This software is dual-licensed, allowing you to choose between the GNU
  General Public License (GPL) and the www.jibble.org Commercial License.
  Since the GPL may be too restrictive for use in a proprietary application,
  a commercial license is also provided. Full license information can be
  found at http://www.jibble.org/licenses/
 
  $Author: pjm2 $
  $Id: ServerSideScriptEngine.java,v 1.4 2004/02/01 13:37:35 pjm2 Exp $
 
  */
 package me.StevenLawson.bukkithttpd;
 
 import java.io.*;
 import java.net.Socket;
 import java.net.URLDecoder;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.bukkit.Bukkit;
 import org.bukkit.event.server.RemoteServerCommandEvent;
 
 /**
  * Copyright Paul Mutton
  * http://www.jibble.org/
  *
  */
 public class RequestThread extends Thread
 {
     private File _rootDir;
     private Socket _socket;
     private BukkitHttpd _plugin;
     private CommandLogger _logger = null;
 
     public RequestThread(Socket socket, File rootDir, BukkitHttpd instance)
     {
         _socket = socket;
         _rootDir = rootDir;
         _plugin = instance;
     }
 
     private static void sendHeader(BufferedOutputStream out, int code, String contentType, long contentLength, long lastModified) throws IOException
     {
         StringBuilder output = new StringBuilder();
 
         output.append("HTTP/1.0 ").append(code).append(" OK\r\n");
         output.append("Date: ").append(new Date().toString()).append("\r\n");
         output.append("Server: BukkitHttpd/1.0\r\n");
         if (contentType != null)
         {
             output.append("Content-Type: ").append(contentType).append("\r\n");
         }
         if (contentLength != -1)
         {
             output.append("Content-Length: ").append(contentLength).append("\r\n");
         }
         output.append("Expires: Thu, 01 Dec 1994 16:00:00 GMT\r\n");
         output.append("Last-modified: ").append(new Date(lastModified).toString()).append("\r\n");
         output.append("Access-Control-Allow-Origin: *\r\n");
         output.append("\r\n");
 
         out.write(output.toString().getBytes());
     }
 
     private static void sendError(BufferedOutputStream out, int code, String message) throws IOException
     {
         message = message + "<hr>" + SimpleWebServer.VERSION;
         sendHeader(out, code, "text/html", message.length(), System.currentTimeMillis());
         out.write(message.getBytes());
         out.flush();
         out.close();
     }
 
     private static void sendPostResponse(BufferedOutputStream out, String message) throws IOException
     {
         sendHeader(out, 200, "text/html", message.length(), System.currentTimeMillis());
         out.write(message.getBytes());
         out.flush();
         out.close();
     }
 
     private boolean checkPassword(String in_password)
     {
         if (_plugin.password == null)
         {
             return true;
         }
         else if (_plugin.password.isEmpty())
         {
             return true;
         }
         else if (in_password == null)
         {
             return false;
         }
         else if (_plugin.password.equals(in_password))
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     @Override
     public void run()
     {
         InputStream reader = null;
         try
         {
             _socket.setSoTimeout(_plugin.timeout);
 
             final BufferedReader in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
             final BufferedOutputStream out = new BufferedOutputStream(_socket.getOutputStream());
 
             String method = null;
             String request_uri = null;
             String server_protocol = null;
 
             String req_line = in.readLine();
            if (req_line != null)
             {
                String[] line_parts = req_line.split(" ");
                if (line_parts.length == 3)
                {
                    method = line_parts[0];
                    request_uri = line_parts[1];
                    server_protocol = line_parts[2];
                }
             }
 
             if (method == null || request_uri == null || server_protocol == null)
             {
                 sendError(out, 400, request_uri);
                 return;
             }
 
             if (method.equalsIgnoreCase("post"))
             {
                 int content_length = 0;
 
                 req_line = in.readLine();
                 while (req_line != null)
                 {
                     if (req_line.isEmpty())
                     {
                         break;
                     }
 
                     Matcher cl_match = Pattern.compile("content-length: (.+)").matcher(req_line.toLowerCase());
                     if (cl_match.find())
                     {
                         content_length = Integer.parseInt(cl_match.group(1));
                     }
 
                     req_line = in.readLine();
                 }
 
                 StringBuilder post_body = new StringBuilder();
                 for (int i = 0; i < content_length; i++)
                 {
                     post_body.append((char) in.read());
                 }
 
                 Map<String, String> post_vars = new HashMap<String, String>();
                 for (String pair : post_body.toString().trim().split("&"))
                 {
                     String parts[] = pair.trim().split("=");
                     if (parts.length >= 2)
                     {
                         post_vars.put(URLDecoder.decode(parts[0], "UTF-8").toLowerCase().trim(), URLDecoder.decode(parts[1], "UTF-8").trim());
                     }
                 }
 
                 String password = post_vars.get("password");
                 String command = post_vars.get("command");
 
                 if (command != null)
                 {
                     if (checkPassword(password) && !command.isEmpty())
                     {
                         String username = post_vars.get("username");
                         if (username == null || username.length() <= 1)
                         {
                             username = "BukkitHttpd";
                         }
 
                         Logger.getLogger("Minecraft-Server").info("[" + _plugin.getDescription().getName() + "]: Recieved command \"" + command + "\" from " + _socket.getInetAddress().getHostAddress() + " with username \"" + username + "\".");
 
                         _logger = new CommandLogger(_plugin, username);
 
                         RemoteServerCommandEvent event = new RemoteServerCommandEvent(_logger, command);
 
                         Bukkit.getServer().getPluginManager().callEvent(event);
 
                         if (event.getCommand() != null)
                         {
                             if (event.getCommand().equals(""))
                             {
                                 Bukkit.dispatchCommand(_logger, command);
 
                                 String wait = post_vars.get("wait");
                                 boolean do_wait = true;
                                 if (wait != null)
                                 {
                                     do_wait = !wait.equalsIgnoreCase("false");
                                 }
 
                                 _plugin.getServer().getScheduler().runTaskLaterAsynchronously(_plugin, new Runnable()
                                 {
                                     @Override
                                     public void run()
                                     {
                                         try
                                         {
                                             sendPostResponse(out, _logger.getLog());
                                         }
                                         catch (IOException ex)
                                         {
                                             Logger.getLogger("Minecraft-Server").log(Level.SEVERE, null, ex);
                                         }
 
                                         _logger.close();
                                     }
                                 }, (do_wait ? 20L : 0L));
                             }
                         }
 
                         return;
                     }
                 }
 
                 sendError(out, 400, "Invalid POST Request");
                 return;
             }
             else if (!method.equalsIgnoreCase("get"))
             {
                 sendError(out, 405, "Method Not Allowed");
                 return;
             }
 
             String request_path = null;
             String query_string = "";
 
             String uri_parts[] = request_uri.split("\\?");
             if (uri_parts.length >= 1)
             {
                 request_path = uri_parts[0];
             }
             if (uri_parts.length >= 2)
             {
                 query_string = uri_parts[1];
             }
 
             Map<String, String> get_vars = new HashMap<String, String>();
             for (String pair : query_string.toString().trim().split("&"))
             {
                 String parts[] = pair.trim().split("=");
                 if (parts.length >= 2)
                 {
                     get_vars.put(URLDecoder.decode(parts[0], "UTF-8").toLowerCase().trim(), URLDecoder.decode(parts[1], "UTF-8").trim());
                 }
             }
 
             String password = get_vars.get("password");
             if (!checkPassword(password))
             {
                 sendError(out, 403, "Permission Denied.");
                 return;
             }
 
             if (request_path == null)
             {
                 sendError(out, 404, "File Not Found.");
                 return;
             }
 
             File file = new File(_rootDir, URLDecoder.decode(request_path, "UTF-8")).getCanonicalFile();
 
             if (file.isDirectory())
             {
                 // Check to see if there is an index file in the directory.
                 File indexFile = new File(file, "index.html");
                 if (indexFile.exists() && !indexFile.isDirectory())
                 {
                     file = indexFile;
                 }
             }
 
             if (!file.toString().startsWith(_rootDir.toString()))
             {
                 // Uh-oh, it looks like some lamer is trying to take a peek
                 // outside of our web root directory.
                 sendError(out, 403, "Permission Denied.");
             }
             else if (!file.exists())
             {
                 // The file was not found.
                 sendError(out, 404, "File Not Found.");
             }
             else if (file.isDirectory())
             {
                 // print directory listing
                 if (!request_path.endsWith("/"))
                 {
                     request_path = request_path + "/";
                 }
                 File[] files = file.listFiles();
                 sendHeader(out, 200, "text/html", -1, System.currentTimeMillis());
                 String title = "Index of " + request_path;
                 out.write(("<html><head><title>" + title + "</title></head><body><h3>Index of " + request_path + "</h3><p>\n").getBytes());
                 for (int i = 0; i < files.length; i++)
                 {
                     file = files[i];
                     String filename = file.getName();
                     String description = "";
                     if (file.isDirectory())
                     {
                         description = "&lt;DIR&gt;";
                     }
                     String pwline = _plugin.password != null ? "?password=" + _plugin.password : "";
                     out.write(("<a href=\"" + request_path + filename + pwline + "\">" + filename + "</a> " + description + "<br>\n").getBytes());
                 }
                 out.write(("</p><hr><p>" + SimpleWebServer.VERSION + "</p></body><html>").getBytes());
             }
             else
             {
                 reader = new BufferedInputStream(new FileInputStream(file));
 
                 long tail_offset = 0L;
                 if (get_vars.get("tail_offset") != null)
                 {
                     tail_offset = (long) Integer.parseInt(get_vars.get("tail_offset"));
                 }
 
                 long contentLength = file.length();
                 if (tail_offset > 0L && tail_offset < contentLength)
                 {
                     reader.skip(contentLength - tail_offset);
                     contentLength = tail_offset;
                 }
 
                 String contentType = SimpleWebServer.MIME_TYPES.get(SimpleWebServer.getExtension(file));
 
                 sendHeader(out, 200, contentType, contentLength, file.lastModified());
 
                 byte[] buffer = new byte[4096];
                 int bytesRead;
                 while ((bytesRead = reader.read(buffer)) != -1)
                 {
                     out.write(buffer, 0, bytesRead);
                 }
                 reader.close();
             }
             out.flush();
             out.close();
         }
         catch (IOException e)
         {
             if (reader != null)
             {
                 try
                 {
                     reader.close();
                 }
                 catch (Exception ioex)
                 {
                 }
             }
         }
     }
 }
