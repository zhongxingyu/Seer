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
 
 import java.net.URL;
 
 public class CodeBlockChunk extends BlockChunk {
 
 	private final String url;
 
 	public CodeBlockChunk(Slide slide, String url, int align) {
 		super(slide, align);
 		this.url = url;
 	}
 
 	@Override
 	public String getText() {
 		try {
			return (String) new URL(url).getContent();
 		} catch (Exception e) {
 			return e.getMessage();
 		}
 		//return "Bad Code URL";
 	}
 
 	@Override
 	public boolean hasText() {
 		return true;
 	}
 }
