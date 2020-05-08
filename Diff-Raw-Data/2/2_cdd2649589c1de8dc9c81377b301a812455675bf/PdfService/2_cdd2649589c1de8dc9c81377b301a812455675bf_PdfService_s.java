 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.pdf.concatenate;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.StreamingOutput;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.pdf.PdfCopy;
 import com.itextpdf.text.pdf.PdfReader;
 import com.meltmedia.cadmium.core.CadmiumApiEndpoint;
 
 @CadmiumApiEndpoint
 @Path("/concatenate")
 public class PdfService {
 	private final Logger log = LoggerFactory.getLogger(getClass());
 	private Pattern ownerPasswordPattern;
 	
 	@GET
 	@Path("{fileName}")
 	public Response pdfService(@PathParam("fileName") String fileName, @Context HttpServletRequest req) {
 		String[] srcUrls = req.getParameterValues("srcUrl");
 	  log.info("Building pdf {} from {}", fileName, srcUrls);
 		StreamingOutput response = null;
 		
 		if (srcUrls != null && srcUrls.length > 0) {
 
       List<PdfFile> fileList = new LinkedList<PdfFile>();
       try {
         for (String url : srcUrls) {
 
           String password = null;
           Matcher m = getOwnerPasswordPattern().matcher(url);
           if (m.find()) {
             password = m.group(3);
 
             // this will fix file URLs like file:///password@host/...
             url = url.replaceAll("///.*@", "///");
 
             // this will fix the remaining URLs
             url = url.replaceAll("//.*@", "//");
 
             fileList.add(new PdfFile(new URL(url), password));
           } else {
             fileList.add(new PdfFile(new URL(url), null));
           }
         } 
         log.debug("Creating return object for src {}", fileList);
         response = new PdfStreamingOutput(fileList);
       }catch(Exception e) {
       	return Response.status(Response.Status.BAD_REQUEST).build();
       }
       
 		}
		return Response.ok(response).header("Content-Disposition", "attachment; filename=" + fileName).build();
 	}
 
 	private Pattern getOwnerPasswordPattern() {
     if (ownerPasswordPattern == null) {
       ownerPasswordPattern = Pattern.compile("^(https?|ftp|file):///?((.*)@)");
     }
     return ownerPasswordPattern;
   }
 	
 	private class PdfStreamingOutput implements StreamingOutput {
 		
 		private List<PdfFile> pdfFiles;
 		
 		public PdfStreamingOutput(List<PdfFile> pdfFiles) {
 			this.pdfFiles = pdfFiles;
 		}
 		
 		public void concatenate(OutputStream os) throws IOException, DocumentException {
 	    log.debug("Concatenating {}", pdfFiles);
 	    Document document = new Document();
 	
 	    PdfCopy copy = new PdfCopy(document, os);
 	
 	    document.open();
 	
 	    PdfReader reader = null;
 	    int pageCount;
 	    String path = null;
 	
 	    try {
 	      for (PdfFile f : pdfFiles) {
 	        path = f.getUrl().toString();
 	
 	        if (f.getPassword() != null) {
 	          reader = new PdfReader(f.getUrl(), f.getPassword().getBytes());
 	        } else {
 	          reader = new PdfReader(f.getUrl());
 	        }
 	
 	        pageCount = reader.getNumberOfPages();
 	        for (int page = 1; page <= pageCount; page++) {
 	          copy.addPage(copy.getImportedPage(reader, page));
 	        }
 	        reader.close();
 	        reader = null;
 	      }
 	      document.close();
 	      document = null;
 	    } catch (IOException ioe) {
 	      log.error("Error reading pdf: " + path, ioe);
 	      throw ioe;
 	    } catch (DocumentException de) {
 	    	log.error("Error processing pdf: " + path, de);
 	      throw de;
 	    } catch (RuntimeException re) {
 	    	log.error("Runtime Error processing pdf: " + path, re);
 	      throw re;
 	    } finally {
 	      if (reader != null) reader.close();
 	      if (document != null) document.close();
 	    }
 	  }
 
 		@Override
 		public void write(OutputStream os) throws IOException,
 				WebApplicationException {
 			try {
 				concatenate(os);
 			} catch(DocumentException e) {
 				log.warn("Failed to build document.", e);
 				throw new WebApplicationException(e);
 			}
 		}
 	}
 
 }
