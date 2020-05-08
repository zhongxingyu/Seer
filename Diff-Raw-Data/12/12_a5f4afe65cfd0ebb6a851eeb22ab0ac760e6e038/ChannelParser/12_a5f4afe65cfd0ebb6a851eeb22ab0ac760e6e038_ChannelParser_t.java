 /* $Id$
  * 
  * Copyright (c) 2005, Henrik Niehaus & Lazy Bones development team
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice, 
  *    this list of conditions and the following disclaimer in the documentation 
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project (Lazy Bones) nor the names of its 
  *    contributors may be used to endorse or promote products derived from this 
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package org.hampelratte.svdrp.util;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.hampelratte.svdrp.responses.highlevel.Channel;
 import org.hampelratte.svdrp.responses.highlevel.ChannelLineParser;
 import org.hampelratte.svdrp.responses.highlevel.ChannelLineParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 
 /**
  * Parses a list of channels received from VDR by the LSTC command
  * 
  * @author <a href="hampelratte@users.sf.net>hampelratte@users.sf.net</a>
  *
  */
 public class ChannelParser {
    private static transient Logger logger = LoggerFactory.getLogger(ChannelParser.class);
    
     /**
 	 * Parses a list of channels received from VDR by the LSTC command
 	 * 
 	 * @param channelData
 	 *            A list of channels received from VDR by LSTC command
 	 * @param ignoreErrors
 	 *            If set to true, all exceptions, which occure during parsing
 	 *            will be ignored. The channels.conf line, which threw the
 	 *            exception will be lost. If set to false, the parsing will 
 	 *            stop immediately, if an exception occurs.
 	 * @return A list of Channel objects
 	 * @throws ParseException
 	 */
     public static List<Channel> parse(String channelData, boolean ignoreErrors) throws ParseException {
         ArrayList<Channel> list = new ArrayList<Channel>();
         StringTokenizer st = new StringTokenizer(channelData, "\n");
         int lineNumber = 1;
         while (st.hasMoreTokens()) {
             String line = st.nextToken();
             
             ChannelLineParser parser;
 			try {
 				parser = ChannelLineParserFactory.createChannelParser(line);
 				Channel channel = parser.parse(line);
 	            list.add(channel);
 			} catch (Exception e) {
			    ParseException pe = new ParseException("Unknown channels.conf line format on line " + lineNumber + ": [" + line + "]", lineNumber); 
 				if(!ignoreErrors) {
					throw pe;
				} else {
				    logger.error("", pe);
 				}
 			}
 			lineNumber++;
         }
         return list;
     }
 }
