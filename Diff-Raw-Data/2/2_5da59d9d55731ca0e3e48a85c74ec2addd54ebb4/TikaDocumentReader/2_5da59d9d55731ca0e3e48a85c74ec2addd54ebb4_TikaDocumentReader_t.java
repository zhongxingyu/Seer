 /*
  * Copyright (C) 2003-2010 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.services.document.impl.tika;
 
 import org.apache.tika.exception.TikaException;
 import org.apache.tika.metadata.DublinCore;
 import org.apache.tika.metadata.MSOffice;
 import org.apache.tika.metadata.Metadata;
 import org.apache.tika.parser.ParseContext;
 import org.apache.tika.parser.Parser;
 import org.apache.tika.parser.ParsingReader;
 import org.apache.tika.sax.BodyContentHandler;
 import org.apache.tika.sax.WriteOutContentHandler;
 import org.exoplatform.commons.utils.QName;
 import org.exoplatform.services.document.AdvancedDocumentReader;
 import org.exoplatform.services.document.DCMetaData;
 import org.exoplatform.services.document.DocumentReadException;
 import org.exoplatform.services.document.HandlerNotFoundException;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.security.AccessController;
 import java.security.PrivilegedActionException;
 import java.security.PrivilegedExceptionAction;
 import java.util.Properties;
 
 /**
  * Created by The eXo Platform SAS.
  * 
  * <br/>Date: 
  * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
  * @version $Id: TikaDocumentReader.java 111 2008-11-11 11:11:11Z serg $
  */
 public class TikaDocumentReader implements AdvancedDocumentReader
 {
    /**
     * Since Tika can not extract metadata without extracting document content,
     * so reed content is limited to 10 Kb.
     */
    private final int MAX_READED_SIZE = 10 * 1024;
 
    private final String mimeType;
 
    private final Parser parser;
 
    public TikaDocumentReader(Parser tikaParser, String mimeType) throws HandlerNotFoundException
    {
       this.parser = tikaParser;
       this.mimeType = mimeType;
    }
 
    public Reader getContentAsReader(final InputStream is, final String encoding) throws IOException,
       DocumentReadException
    {
       try
       {
          return (Reader)AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
          {
 
             public Object run() throws Exception
             {
                Metadata metadata = new Metadata();
                metadata.set(Metadata.CONTENT_TYPE, mimeType);
                metadata.set(Metadata.CONTENT_ENCODING, encoding);
                ParseContext context = new ParseContext();
                context.set(Parser.class, parser);
                return new ParsingReader(parser, is, metadata, context);
             }
          });
       }
       catch (PrivilegedActionException pae)
       {
          Throwable cause = pae.getCause();
          if (cause instanceof IOException)
          {
             throw (IOException)cause;
          }
          else if (cause instanceof RuntimeException)
          {
             throw (RuntimeException)cause;
          }
          else
          {
             throw new RuntimeException(cause);
          }
       }
    }
 
    public Reader getContentAsReader(final InputStream is) throws IOException, DocumentReadException
    {
       try
       {
          return (Reader)AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
          {
 
             public Object run() throws Exception
             {
                Metadata metadata = new Metadata();
                metadata.set(Metadata.CONTENT_TYPE, mimeType);
                ParseContext context = new ParseContext();
                context.set(Parser.class, parser);
                return new ParsingReader(parser, is, metadata, context);
             }
          });
       }
       catch (PrivilegedActionException pae)
       {
          Throwable cause = pae.getCause();
          if (cause instanceof IOException)
          {
             throw (IOException)cause;
          }
          else if (cause instanceof RuntimeException)
          {
             throw (RuntimeException)cause;
          }
          else
          {
             throw new RuntimeException(cause);
          }
       }
 
    }
 
    public String getContentAsText(final InputStream is) throws IOException, DocumentReadException
    {
       try
       {
          return (String)AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
          {
 
             public Object run() throws Exception
             {
                try
                {
                   Metadata metadata = new Metadata();
                   metadata.set(Metadata.CONTENT_TYPE, mimeType);
 
                   ContentHandler handler = new BodyContentHandler();
                   ParseContext context = new ParseContext();
                   context.set(Parser.class, parser);
                   try
                   {
                      parser.parse(is, handler, metadata, context);
                      return handler.toString();
                   }
                   catch (SAXException e)
                   {
                      throw new DocumentReadException(e.getMessage(), e);
                   }
                   catch (TikaException e)
                   {
                      throw new DocumentReadException(e.getMessage(), e);
                   }
                }
                finally
                {
                   try
                   {
                      is.close();
                   }
                   catch (Throwable e)
                   {
                   }
                }
             }
          });
       }
       catch (PrivilegedActionException pae)
       {
          Throwable cause = pae.getCause();
          if (cause instanceof IOException)
          {
             throw (IOException)cause;
          }
          else if (cause instanceof RuntimeException)
          {
             throw (RuntimeException)cause;
          }
          else
          {
             throw new RuntimeException(cause);
          }
       }
    }
 
    public String getContentAsText(final InputStream is, final String encoding) throws IOException,
       DocumentReadException
    {
       try
       {
          return (String)AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
          {
             public Object run() throws Exception
             {
                try
                {
                   Metadata metadata = new Metadata();
                   metadata.set(Metadata.CONTENT_TYPE, mimeType);
                   metadata.set(Metadata.CONTENT_ENCODING, encoding);
 
                   ContentHandler handler = new BodyContentHandler();
                   ParseContext context = new ParseContext();
                   context.set(Parser.class, parser);
                   try
                   {
                      parser.parse(is, handler, metadata, context);
                      return handler.toString();
                   }
                   catch (SAXException e)
                   {
                      throw new DocumentReadException(e.getMessage(), e);
                   }
                   catch (TikaException e)
                   {
                      throw new DocumentReadException(e.getMessage(), e);
                   }
                }
                finally
                {
                   try
                   {
                      is.close();
                   }
                   catch (Throwable e)
                   {
                   }
                }
             }
          });
       }
       catch (PrivilegedActionException pae)
       {
          Throwable cause = pae.getCause();
          if (cause instanceof IOException)
          {
             throw (IOException)cause;
          }
          else if (cause instanceof RuntimeException)
          {
             throw (RuntimeException)cause;
          }
          else
          {
             throw new RuntimeException(cause);
          }
       }
    }
 
    public String[] getMimeTypes()
    {
       return new String[]{mimeType};
    }
 
    public Properties getProperties(final InputStream is) throws IOException, DocumentReadException
    {
       try
       {
          return (Properties)AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
          {
 
             public Object run() throws Exception
             {
                try
                {
                   Metadata metadata = new Metadata();
                   metadata.set(Metadata.CONTENT_TYPE, mimeType);
 
                   ContentHandler handler = new WriteOutContentHandler(MAX_READED_SIZE);
                   ParseContext context = new ParseContext();
                   context.set(Parser.class, parser);
                   try
                   {
                      parser.parse(is, handler, metadata, context);
                   }
                   catch (SAXException e)
                   {
                      throw new DocumentReadException(e.getMessage(), e);
                   }
                   catch (TikaException e)
                   {
                      throw new DocumentReadException(e.getMessage(), e);
                   }
 
                   // construct Properties set
                   Properties props = new Properties();
                   convertProperty(metadata, props, DCMetaData.CONTRIBUTOR, new String[]{DublinCore.CONTRIBUTOR,
                      MSOffice.LAST_AUTHOR});
                   convertProperty(metadata, props, DCMetaData.COVERAGE, DublinCore.COVERAGE);
                   convertProperty(metadata, props, DCMetaData.CREATOR,
                      new String[]{MSOffice.AUTHOR, DublinCore.CREATOR});
                   //TODO different parsers return date in different formats, so keep it as String
                   convertProperty(metadata, props, DCMetaData.DATE, new String[]{DublinCore.DATE, MSOffice.LAST_SAVED,
                      MSOffice.CREATION_DATE});
                   convertProperty(metadata, props, DCMetaData.DESCRIPTION, new String[]{DublinCore.DESCRIPTION,
                      MSOffice.COMMENTS});
                   convertProperty(metadata, props, DCMetaData.FORMAT, DublinCore.FORMAT);
                   convertProperty(metadata, props, DCMetaData.IDENTIFIER, DublinCore.IDENTIFIER);
                   convertProperty(metadata, props, DCMetaData.LANGUAGE, DublinCore.LANGUAGE);
                   //convertProperty(metadata, props, DCMetaData.?, DublinCore.MODIFIED);
                   convertProperty(metadata, props, DCMetaData.PUBLISHER, DublinCore.PUBLISHER);
                   convertProperty(metadata, props, DCMetaData.RELATION, DublinCore.RELATION);
                   convertProperty(metadata, props, DCMetaData.RESOURCE, DublinCore.SOURCE);
                   convertProperty(metadata, props, DCMetaData.RIGHTS, DublinCore.RIGHTS);
                   convertProperty(metadata, props, DCMetaData.SUBJECT, new String[]{DublinCore.SUBJECT,
                      MSOffice.KEYWORDS});
                   convertProperty(metadata, props, DCMetaData.TITLE, DublinCore.TITLE);
                   convertProperty(metadata, props, DCMetaData.TYPE, DublinCore.TYPE);
 
                   return props;
                }
                finally
                {
                   try
                   {
                      is.close();
                   }
                   catch (Throwable e)
                   {
                   }
                }
             }
          });
       }
       catch (PrivilegedActionException pae)
       {
          Throwable cause = pae.getCause();
          if (cause instanceof IOException)
          {
             throw (IOException)cause;
          }
          else if (cause instanceof RuntimeException)
          {
             throw (RuntimeException)cause;
          }
          else
          {
            throw new DocumentReadException("Can not get properties: " + cause.getMessage(), cause);
          }
       }
    }
 
    private void convertProperty(Metadata metadata, Properties props, QName jcrDCProp, String tikaDCProp)
    {
       String value = (String)metadata.get(tikaDCProp);
       if (value != null)
       {
          props.put(jcrDCProp, value);
       }
    }
 
    /**
     * Test does Metadata contains property from tikaPropertyNames list. 
     * <p><b>Warning</b> - Order in tikaPropertyNames list is important. 
     * First property from list will be used as a result value.
     * 
     * @param metadata
     * @param props
     * @param jcrDCProp
     * @param tikaPropertyNames
     */
    private void convertProperty(Metadata metadata, Properties props, QName jcrDCProp, String[] tikaPropertyNames)
    {
       for (String propertyName : tikaPropertyNames)
       {
          String value = (String)metadata.get(propertyName);
          if (value != null)
          {
             props.put(jcrDCProp, value);
             return;
          }
       }
    }
 }
