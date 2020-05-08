 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common.resource;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
 
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.ISkeleton;
 import net.sf.okapi.common.MemMappedCharSequence;
 import net.sf.okapi.common.OkapiNotImplementedException;
 import net.sf.okapi.common.annotation.Annotations;
 import net.sf.okapi.common.annotation.IAnnotation;
 
 /**
  * File resource which holds a URI, InputStream or MemMappedCharSequence
  * referencing a file to be processed in the pipeline. File's can be decomposed
  * into events in the pipeline.
  */
 public class InputResource implements IResource {
 	private Annotations annotations;
 	private String id;
 	private String encoding;
 	private String language;
 	private BOMNewlineEncodingDetector.NewlineType originalNewlineType;
 	private InputStream inputStream;
 	private URI inputURI;
 	private CharSequence inputCharSequence;
 	private Reader reader;
 	
 	/* TODO: 
 	 * 
 	 * Some possible fields for FileResource
 	 
 	- its path/uri
     - what filter-options file to use (if any)
     - its default encoding (in case it cannot be detected)
     - the source language
     - possibly the main target language to work with (for multi-lingual documents)
     - possibly the name of the output file to generate at the end of the pipeline
     - possibly the encoding to use for the output
     */
 
 	public InputResource(URI inputURI, String encoding, String language) {
 		this.annotations = new Annotations();
 		reset(inputURI, encoding, language);
 	}
 
 	public InputResource(InputStream inputStream, String encoding, String language) {
 		this.annotations = new Annotations();
 		reset(inputStream, encoding, language);
 	}
 
 	public InputResource(CharSequence inputCharSequence, String language) {
 		this.annotations = new Annotations();
 		reset(inputCharSequence, language);
 	}
 	
 	public void reset(CharSequence inputCharSequence, String language) {
 		setInputCharSequence(inputCharSequence);
 		setEncoding("UTF-16BE");
 		setLanguage(language);
 		setOriginalNewlineType(BOMNewlineEncodingDetector.getNewlineType(getInputCharSequence()));		
 	}
 	
 	public void reset(URI inputURI, String encoding, String language) {
 		setInputURI(inputURI);
		setEncoding(encoding);
 		setLanguage(language);
 		try {
 			InputStream inputStream = inputURI.toURL().openStream();
 			setInputStream(inputStream);
 			setOriginalNewlineType(new BOMNewlineEncodingDetector(inputStream).getNewlineType());
 		} catch (MalformedURLException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}				
 	}
 	
 	public void reset(InputStream inputStream, String encoding, String language) {
 		setEncoding(encoding);		
 		setLanguage(language);
 		setInputStream(inputStream);
 		try {
 			setOriginalNewlineType(new BOMNewlineEncodingDetector(inputStream).getNewlineType());
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sf.okapi.common.resource.IResource#getAnnotation(java.lang.Class)
 	 */
 	@SuppressWarnings("unchecked")
 	public <A> A getAnnotation(Class<? extends IAnnotation> type) {
 		if (annotations == null)
 			return null;
 		else
 			return (A) annotations.get(type);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.resource.IResource#getId()
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.resource.IResource#getSkeleton()
 	 */
 	public ISkeleton getSkeleton() {
 		throw new OkapiNotImplementedException();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sf.okapi.common.resource.IResource#setAnnotation(net.sf.okapi.common
 	 * .annotation.IAnnotation)
 	 */
 	public void setAnnotation(IAnnotation annotation) {
 		if (annotations == null) {
 			annotations = new Annotations();
 		}
 		annotations.set(annotation);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.resource.IResource#setId(java.lang.String)
 	 */
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sf.okapi.common.resource.IResource#setSkeleton(net.sf.okapi.common
 	 * .filters.ISkeleton)
 	 */
 	public void setSkeleton(ISkeleton skeleton) {
 		// Not Implemented
 	}
 
 	public InputStream getInputStream() {
 		return inputStream;
 	}
 
 	public void setInputStream(InputStream inputStream) {
 		this.inputStream = inputStream;
 	}
 
 	public URI getInputURI() {
 		return inputURI;
 	}
 
 	public void setInputURI(URI inputURI) {
 		this.inputURI = inputURI;
 	}
 
 	/**
 	 * @param inputCharSequence
 	 *            the inputMemMappedCharSequence to set
 	 */
 	public void setInputCharSequence(CharSequence inputCharSequence) {
 		this.inputCharSequence = inputCharSequence;
 	}
 
 	/**
 	 * @return the inputMemMappedCharSequence
 	 */
 	public CharSequence getInputCharSequence() {
 		return inputCharSequence;
 	}
 
 	public String getEncoding() {
 		return encoding;
 	}
 
 	public void setEncoding(String encoding) {
 		this.encoding = encoding;
 	}
 	
 	public String getLanguage() {
 		return language;
 	}
 
 	public void setLanguage(String language) {
 		this.language = language;
 	}
 
 	/**
 	 * @param originalNewlineType the originalNewlineType to set
 	 */
 	public void setOriginalNewlineType(BOMNewlineEncodingDetector.NewlineType originalNewlineType) {
 		this.originalNewlineType = originalNewlineType;
 	}
 
 	/**
 	 * @return the originalNewlineType
 	 */
 	public BOMNewlineEncodingDetector.NewlineType getOriginalNewlineType() {
 		return originalNewlineType;
 	}
 
 	public Reader getReader() {
 		if (reader != null) {
 			return reader;
 		}
 		
 		if (getInputStream() != null)
 			try {
 				reader = new InputStreamReader(getInputStream(), getEncoding());
 			} catch (UnsupportedEncodingException e) {
 				throw new RuntimeException(e);
 			}
 		if (getInputCharSequence() != null)
 			reader = new StringReader(getInputCharSequence().toString()); 
 		
 		return reader;
 	}
 	
 	public void close() {
 		if (reader != null) {
 			try {
 				reader.close();
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		
 		if (getInputStream() != null)
 			try {
 				getInputStream().close();
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		if (getInputCharSequence() != null) {
 			CharSequence cs = getInputCharSequence(); 
 			// if this is a MemMappedCharSequence we need to close it
 			if (cs instanceof MemMappedCharSequence)
 				((MemMappedCharSequence)cs).close();
 		}					
 	}
 }
