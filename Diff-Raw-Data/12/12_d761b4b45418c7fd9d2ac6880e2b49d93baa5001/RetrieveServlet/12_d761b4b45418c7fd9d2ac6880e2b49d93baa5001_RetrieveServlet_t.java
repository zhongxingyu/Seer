 /*
  * RetrieveServlet.java
  *
  * Version: $Revision$
  *
  * Date: $Date$
  *
  * Copyright (c) 2002-2005, Hewlett-Packard Company and Massachusetts
  * Institute of Technology.  All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * - Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  * - Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * - Neither the name of the Hewlett-Packard Company nor the name of the
  * Massachusetts Institute of Technology nor the names of their
  * contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
  * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
  * DAMAGE.
  */
 package org.dspace.app.webui.servlet;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.SQLException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.dspace.app.webui.util.JSPManager;
 import org.dspace.app.webui.util.UIUtil;
 import org.dspace.authorize.AuthorizeException;
 import org.dspace.authorize.AuthorizeManager;
 import org.dspace.content.Bitstream;
 import org.dspace.content.Bundle;
 import org.dspace.core.ConfigurationManager;
 import org.dspace.core.Constants;
 import org.dspace.core.Context;
 import org.dspace.core.LogManager;
 import org.dspace.core.Utils;
 import org.dspace.usage.UsageEvent;
 import org.dspace.utils.DSpace;
 
 /**
  * Servlet for retrieving bitstreams. The bits are simply piped to the user.
  * <P>
  * <code>/retrieve/bitstream-id</code>
  * 
  * @author Robert Tansley
  * @version $Revision$
  */
 public class RetrieveServlet extends DSpaceServlet
 {
     /** log4j category */
     private static Logger log = Logger.getLogger(RetrieveServlet.class);
 
     /**
      * Threshold on Bitstream size before content-disposition will be set.
      */
     private int threshold;
     
     @Override
 	public void init(ServletConfig arg0) throws ServletException {
 
 		super.init(arg0);
 		threshold = ConfigurationManager
 				.getIntProperty("webui.content_disposition_threshold");
 	}
     
     protected void doDSGet(Context context, HttpServletRequest request,
             HttpServletResponse response) throws ServletException, IOException,
             SQLException, AuthorizeException
     {
         Bitstream bitstream = null;
         boolean displayLicense = ConfigurationManager.getBooleanProperty("webui.licence_bundle.show", false);
         boolean isLicense = false;
         
 
         // Get the ID from the URL
         String idString = request.getPathInfo();
 
         if (idString != null)
         {
             // Remove leading slash
             if (idString.startsWith("/"))
             {
                 idString = idString.substring(1);
             }
 
             // If there's a second slash, remove it and anything after it,
             // it might be a filename
             int slashIndex = idString.indexOf('/');
 
             if (slashIndex != -1)
             {
                 idString = idString.substring(0, slashIndex);
             }
 
             // Find the corresponding bitstream
             try
             {
                 int id = Integer.parseInt(idString);
                 bitstream = Bitstream.find(context, id);
             }
             catch (NumberFormatException nfe)
             {
                 // Invalid ID - this will be dealt with below
             }
         }
 
         // Did we get a bitstream?
         if (bitstream != null)
         {

             // Check whether we got a License and if it should be displayed
            // (Note: list of bundles may be empty array, if a bitstream is a Community/Collection logo)
            Bundle bundle = bitstream.getBundles().length>0 ? bitstream.getBundles()[0] : null;
             
            if (bundle!=null && 
                bundle.getName().equals(Constants.LICENSE_BUNDLE_NAME) &&
                 bitstream.getName().equals(Constants.LICENSE_BITSTREAM_NAME))
             {
                     isLicense = true;
             }
             
             if (isLicense && !displayLicense && !AuthorizeManager.isAdmin(context))
             {
                 throw new AuthorizeException();
             }
             log.info(LogManager.getHeader(context, "view_bitstream",
                     "bitstream_id=" + bitstream.getID()));
 
             new DSpace().getEventService().fireEvent(
             		new UsageEvent(
             				UsageEvent.Action.VIEW,
             				request, 
             				context, 
             				bitstream));
             
             //UsageEvent ue = new UsageEvent();
            // ue.fire(request, context, AbstractUsageEvent.VIEW,
 		   //Constants.BITSTREAM, bitstream.getID());
 
             // Pipe the bits
             InputStream is = bitstream.retrieve();
 
             // Set the response MIME type
             response.setContentType(bitstream.getFormat().getMIMEType());
 
             // Response length
             response.setHeader("Content-Length", String.valueOf(bitstream
                     .getSize()));
             
     		if(threshold != -1 && bitstream.getSize() >= threshold)
     		{
     			UIUtil.setBitstreamDisposition(bitstream.getName(), request, response);
     		}
 
             Utils.bufferedCopy(is, response.getOutputStream());
             is.close();
             response.getOutputStream().flush();
         }
         else
         {
             // No bitstream - we got an invalid ID
             log.info(LogManager.getHeader(context, "view_bitstream",
                     "invalid_bitstream_id=" + idString));
 
             JSPManager.showInvalidIDError(request, response, idString,
                     Constants.BITSTREAM);
         }
     }
 }
