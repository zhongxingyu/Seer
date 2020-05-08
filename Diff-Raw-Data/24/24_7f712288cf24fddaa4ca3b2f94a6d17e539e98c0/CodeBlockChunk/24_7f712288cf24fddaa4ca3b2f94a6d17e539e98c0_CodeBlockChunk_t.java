 /*******************************************************************************
  * Copyright (c) 2009 The Eclipse Foundation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.examples.slideshow.core;
 
import java.io.IOException;
import java.io.InputStream;
 import java.net.URL;
import java.net.URLConnection;
 
 public class CodeBlockChunk extends BlockChunk {
 
 	private final String url;
 
 	public CodeBlockChunk(Slide slide, String url, int align) {
 		super(slide, align);
 		this.url = url;
 	}
 
 	@Override
 	public String getText() {
		InputStream stream = null;
 		try {
			URLConnection connection = new URL(url).openConnection();
			stream = connection.getInputStream();
			StringBuilder builder = new StringBuilder();
			byte[] buffer = new byte[1024];
			int count = 0;
			while ((count = stream.read(buffer)) > 0) {
				builder.append(new String(buffer, 0, count));
			}
			return builder.toString();
 		} catch (Exception e) {
 			return e.getMessage();
		} finally {
			try {
				if (stream != null) stream.close();
			} catch (IOException e) {
				// TODO Log this?
			}
 		}
 		//return "Bad Code URL";
 	}
 
 	@Override
 	public boolean hasText() {
 		return true;
 	}
 }
