 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 18th January 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.messenger.impl;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Map;
 
 import au.edu.uts.eng.remotelabs.schedserver.config.Config;
 import au.edu.uts.eng.remotelabs.schedserver.messenger.Message;
 import au.edu.uts.eng.remotelabs.schedserver.messenger.MessengerActivator;
 
 /**
  * Generates messages from templates.
  * <br />
  * The template format is:<br />
  * <pre>
  * &lt;Subject line&gt;
 
  * &lt;Body line 1&gt;
  * ...
  * &lt;Body line n&gt;
  * </pre>
  * Template lines can contain macros in the format <tt>${macro}</tt>. These are
  * replaced with a value from the supplied macro list or if the macro isn't in
  * the list, it is loaded from configuration.
  */
 public class Templator
 {
     /** URL to template. */
     private URL templateUrl;
     
     /** Macro list. */
     private Map<String, String> macros;
     
     /** Configuration. */
     private Config config;
     
     public Templator(URL template, Map<String, String> macros)
     {
         this.templateUrl = template;
         this.macros = macros;
         this.config = MessengerActivator.getConfiguration();
     }
     
     /**
      * Generate the templated message.
      * 
      * @return message
      * @throws Exception error generating message
      */
     public Message generate() throws Exception
     {
         Message message = new Message();
         
         BufferedReader reader = new BufferedReader(new InputStreamReader(this.templateUrl.openStream()));
         
         /* First line a subject line. */
         message.setSubject(this.replaceMacros(reader.readLine()));
         
         /* Next line is subject, body delimiter. */
         reader.readLine();
         
         /* The rest is body. */
         StringBuilder body = new StringBuilder();
         String line, prevLine = null;
         while ((line = reader.readLine()) != null)
         {
             line = this.replaceMacros(line);
             
             /* Prepend the previous lines content if they exist. */
             if (prevLine != null)
             {
                 line = prevLine + ' ' + line;
             }
             
             if (line.length() > 80)
             {
                body.append(line.substring(0, 80));
                prevLine = line.substring(81);
             }
             else
             {
                 body.append(line);
                 prevLine = null;
             }
             
             body.append('\n');
         }
         
         message.setBody(body.toString());
         return message;
     }
     
     /**
      * Replaces macros with values.
      * 
      * @param line line
      * @return line with macros replaced with values
      */
     private String replaceMacros(String line)
     {
         line = line.trim();
         
         /* No macros. */
         if (!line.contains("${")) return line;
         
         StringBuilder tokLine = new StringBuilder();
         
         for (String t : line.split("\\$\\{"))
         {
             if (t.contains("}"))
             {
                 int ei =  t.indexOf("}");
                 
                 String macro = t.substring(0, ei);
                 if (this.macros.containsKey(macro))  tokLine.append(this.macros.get(macro));
                 else tokLine.append(this.config.getProperty(macro, ""));
                 
                 if (ei < t.length() - 1) tokLine.append(t.substring(ei + 1));
             }
             else
             {
                 tokLine.append(t);
             }
         }
         
         return tokLine.toString();
     }
 }
