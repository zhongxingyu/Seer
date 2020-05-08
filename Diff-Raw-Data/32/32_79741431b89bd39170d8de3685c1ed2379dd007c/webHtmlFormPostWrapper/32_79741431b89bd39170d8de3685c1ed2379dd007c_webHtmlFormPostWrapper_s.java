 /*
  *	Copyright 2006-2012, Rathravane LLC
  *
  *	Licensed under the Apache License, Version 2.0 (the "License");
  *	you may not use this file except in compliance with the License.
  *	You may obtain a copy of the License at
  *	
  *	http://www.apache.org/licenses/LICENSE-2.0
  *	
  *	Unless required by applicable law or agreed to in writing, software
  *	distributed under the License is distributed on an "AS IS" BASIS,
  *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *	See the License for the specific language governing permissions and
  *	limitations under the License.
  */
 package com.rathravane.drumlin.app.htmlForms;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import com.rathravane.drumlin.service.framework.context.request;
 import com.rathravane.till.collections.rrMultiMap;
 import com.rathravane.till.data.rrConvertor;
 import com.rathravane.till.logging.rrLogSetup;
 
 public class webHtmlFormPostWrapper
 {
 	public webHtmlFormPostWrapper ( request req )
 	{
 		this ( req, null );
 	}
 
 	public webHtmlFormPostWrapper ( request req, mimePartFactory mos )
 	{
 		fRequest = req;
 		final String ct = req.getContentType ();
 
 		fIsMultipartFormData = ct != null && ct.startsWith ( "multipart/form-data" );
 		fPartFactory = mos == null ? new simpleStorage () : mos;
 		fParsedValues = new HashMap<String,mimePart> ();
 		fParseComplete = false;
 	}
 
 	/**
 	 * this must be called to cleanup mime part resources (e.g. tmp files)
 	 */
 	public void close ()
 	{
 		for ( mimePart vi : fParsedValues.values () )
 		{
 			vi.discard ();
 		}
 	}
 	
 	@Override
 	public String toString ()
 	{
 		final StringBuffer sb = new StringBuffer ();
 
 		sb.append ( fRequest.getMethod ().toUpperCase () ).append ( " {" );
 		if ( fIsMultipartFormData )
 		{
 			if ( fParseComplete )
 			{
 				for ( Entry<String, mimePart> e : fParsedValues.entrySet () )
 				{
 					sb.append ( e.getKey () + ":" );
 					final mimePart mp = e.getValue ();
 					if ( mp.getAsString () != null )
 					{
 						 sb.append ( "'" + mp.getAsString () + "' " );
 					}
 					else
 					{
 						sb.append ( "(data) " );
 					}
 				}
 			}
 			else
 			{
 				sb.append ( "not parsed yet" );
 			}
 		}
 		else
 		{
 			for ( Entry<?, ?> e : fRequest.getParameterMap().entrySet () )
 			{
 				sb.append ( e.getKey ().toString () + ":'" + e.getValue ().toString () + "' " );
 			}
 		}
 		sb.append ( " }" );
 
 		return sb.toString ();
 	}
 	
 	public boolean isPost ()
 	{
 		return fRequest.getMethod ().toLowerCase ().equals ( "post" );
 	}
 
 	public boolean hasParameter ( String name )
 	{
 		parseIfNeeded ();
 
 		return fIsMultipartFormData ?
 			fParsedValues.containsKey ( name ) :
 			fRequest.getParameterMap ().containsKey ( name );
 	}
 
 	public Map<String,String> getValues ()
 	{
 		final HashMap<String,String> map = new HashMap<String,String> ();
 
 		parseIfNeeded ();
 
 		if ( fIsMultipartFormData )
 		{
 			for ( Map.Entry<String,mimePart> e : fParsedValues.entrySet () )
 			{
 				final String val = e.getValue ().getAsString ();
 				if ( val != null )
 				{
 					map.put ( e.getKey(), val );
 				}
 			}
 		}
 		else
 		{
 			for ( Map.Entry<?,?> e : fRequest.getParameterMap ().entrySet() )
 			{
 				final String key = e.getKey ().toString ();
 				final String[] vals = (String[]) e.getValue ();
 				String valToUse = "";
 				if ( vals.length > 0 )
 				{
 					valToUse = vals[0];
 				}
 				map.put ( key, valToUse );
 			}
 		}
 		return map;
 	}
 
 	public boolean isFormField ( String name )
 	{
 		boolean result = false;
 		if ( hasParameter ( name ) )
 		{
 			if ( fIsMultipartFormData )
 			{
 				final mimePart val = fParsedValues.get ( name );
 				result = ( val != null && val.getAsString () != null );
 			}
 			else
 			{
 				result = true;
 			}
 		}
 		return result;
 	}
 
 	public boolean isStreamParam ( String name )
 	{
 		return !isFormField ( name );
 	}
 
 	public String getValue ( String name )
 	{
 		parseIfNeeded ();
 
 		String result = null;
 		if ( fIsMultipartFormData )
 		{
 			final mimePart val = fParsedValues.get ( name );
 			
 			result = null;
 			if ( val != null && val.getAsString () != null )
 			{
 				result = val.getAsString ().trim ();
 			}
 		}
 		else
 		{
 			result = fRequest.getParameter ( name );
 			if ( result != null )
 			{
 				result = result.trim ();
 			}
 		}
 		return result;
 	}
 
 	public boolean getValueBoolean ( String name, boolean valIfMissing )
 	{
 		boolean result = valIfMissing;
 		final String val = getValue ( name );
 		if ( val != null )
 		{
 			result = rrConvertor.convertToBooleanBroad ( val );
 		}
 		return result;
 	}
 	
 	public void changeValue ( String fieldName, String newVal )
 	{
 		parseIfNeeded ();
 
 		if ( fIsMultipartFormData )
 		{
 			if ( fParsedValues.containsKey ( fieldName ) )
 			{
 				fParsedValues.get ( fieldName ).discard ();
 			}
 			
 			final inMemoryFormDataPart part = new inMemoryFormDataPart ( "", "form-data; name=\"" + fieldName + "\"" );
 			final byte[] array = newVal.getBytes ();
 			part.write ( array, 0, array.length );
 			part.close ();
 			fParsedValues.put ( fieldName, part );
 		}
 		else
 		{
 			fRequest.changeParameter ( fieldName, newVal );
 		}
 	}
 
 	public mimePart getStream ( String name )
 	{
 		parseIfNeeded ();
 
 		mimePart result = null;
 		if ( fIsMultipartFormData )
 		{
 			final mimePart val = fParsedValues.get ( name );
 			if ( val != null && val.getAsString () == null )
 			{
 				return val;
 			}
 		}
 		return result;
 	}
 	
 	private final request fRequest;
 	private final boolean fIsMultipartFormData;
 	private boolean fParseComplete;
 	private final HashMap<String,mimePart> fParsedValues;
 	private final mimePartFactory fPartFactory;
 
 	private void parseIfNeeded ()
 	{
 		if ( fIsMultipartFormData && !fParseComplete )
 		{
 			try
 			{
 				final String ct = fRequest.getContentType ();
 				int boundaryStartIndex = ct.indexOf ( kBoundaryTag );
 				if ( boundaryStartIndex != -1 )
 				{
 					boundaryStartIndex = boundaryStartIndex + kBoundaryTag.length ();
 					final int semi = ct.indexOf ( ";", boundaryStartIndex );
 					int boundaryEndIndex = semi == -1 ? ct.length () : semi;
 
 					final String boundary = ct.substring ( boundaryStartIndex, boundaryEndIndex ).trim ();
 					final multipartMimeReader mmr = new multipartMimeReader ( boundary, fPartFactory );
 					final InputStream is = fRequest.getBodyStream ();
 					mmr.read ( is );
 					is.close ();
 
 					for ( mimePart mp : mmr.getParts () )
 					{
 						fParsedValues.put ( mp.getName(), mp );
 					}
 				}
 			}
 			catch ( IOException e )
 			{
 				log.warning ( "There was a problem reading a multipart/form-data POST: " + e.getMessage () );
 			}
 			fParseComplete = true;
 		}
 	}
 
 	private static final String kBoundaryTag = "boundary=";
 
 	static final Logger log = rrLogSetup.getLog ( webHtmlFormPostWrapper.class );
 
 	public static abstract class basePart implements mimePart
 	{
 		public basePart ( String contentType, String contentDisp )
 		{
 			fType = contentType;
 			fDisp = contentDisp;
 
 			final int nameSpot = fDisp.indexOf ( "name=\"" );
 			String namePart = fDisp.substring ( nameSpot + "name=\"".length () );
 			final int closeQuote = namePart.indexOf ( "\"" );
 			namePart = namePart.substring ( 0, closeQuote );
 			fName = namePart;
 		}
 
 		@Override
 		public String getContentType ()
 		{
 			return fType;
 		}
 
 		@Override
 		public String getContentDisposition ()
 		{
 			return fDisp;
 		}
 
 		@Override
 		public String getName ()
 		{
 			return fName;
 		}
 
 		@Override
 		public void discard ()
 		{
 		}
 
 		private final String fType;
 		private final String fDisp;
 		private final String fName;
 	}
 	
 	public static class inMemoryFormDataPart extends basePart
 	{
 		public inMemoryFormDataPart ( String ct, String cd )
 		{
 			super ( ct, cd );
 			fValue = "";
 		}
 		
 		@Override
 		public void write ( byte[] line, int offset, int length )
 		{
 			fValue = new String ( line, offset, length );
 		}
 
 		@Override
 		public void close ()
 		{
 		}
 
 		@Override
 		public InputStream openStream () throws IOException
 		{
 			throw new IOException ( "Opening stream on in-memory form data." );
 		}
 
 		@Override
 		public String getAsString ()
 		{
 			return fValue;
 		}
 
 		private String fValue;
 	}
 
 	private static class tmpFilePart extends basePart
 	{
 		public tmpFilePart ( String ct, String cd ) throws IOException
 		{
 			super ( ct, cd );
 
 			fFile = File.createTempFile ( "drumlin.", ".part" );
 			fStream = new FileOutputStream ( fFile );
 		}
 
 		@Override
 		public void write ( byte[] line, int offset, int length ) throws IOException
 		{
 			if ( fStream != null )
 			{
 				fStream.write ( line, offset, length );
 			}
 		}
 
 		@Override
 		public void close () throws IOException
 		{
 			if ( fStream != null )
 			{
 				fStream.close ();
 				fStream = null;
 			}
 		}
 
 		@Override
 		public InputStream openStream () throws IOException
 		{
 			if ( fStream != null )
 			{
 				log.warning ( "Opening input stream on tmp file before it's fully written." );
 			}
 			return new FileInputStream ( fFile );
 		}
 
 		@Override
 		public String getAsString ()
 		{
 			return null;
 		}
 
 		@Override
 		public void discard ()
 		{
 			fFile.delete ();
 			fFile = null;
 			fStream = null;
 		}
 
 		private File fFile;
 		private FileOutputStream fStream;
 	}
 
 	private static class simpleStorage implements mimePartFactory
 	{
 		@Override
 		public mimePart createPart ( rrMultiMap<String, String> partHeaders ) throws IOException
 		{
 			final String contentDisp = partHeaders.getFirst ( "content-disposition" );
 			if ( contentDisp != null && contentDisp.contains ( "filename=\"" ) )
 			{
 				return new tmpFilePart ( partHeaders.getFirst ( "content-type" ), contentDisp );
 			}
 			else
 			{
 				return new inMemoryFormDataPart ( partHeaders.getFirst ( "content-type" ), contentDisp );
 			}
 		}
 	}
 }
