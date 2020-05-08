 /*******************************************************************************
  * Copyright (c) 2013 Pronoia Health LLC.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Pronoia Health LLC - initial API and implementation
  *******************************************************************************/
 package com.pronoiahealth.olhie.client.shared.events.book;
 
 import java.util.List;
 
 import org.jboss.errai.common.client.api.annotations.Portable;
 import org.jboss.errai.enterprise.client.cdi.api.Conversational;
 
 import com.pronoiahealth.olhie.client.shared.vo.BookDisplay;
 
 /**
  * BookSearchResponseEvent.java<br/>
  * Responsibilities:<br/>
  * 1. Returned with a book<br/>
  * 
  * <p>
  * Fired By: BookSearchService <br/>
  * Observed By: NewBookPage class <br/>
  * </p>
  * 
  * @author Alex Roman
  * @version 1.0
  * @since Jun 25, 2013
  * 
  */
 @Portable
 @Conversational
 public class BookSearchResponseEvent {
 	private int totalInResultSet;
 	private List<BookDisplay> bookDisplayList;
 
 	/**
 	 * Constructor
 	 * 
 	 */
 	public BookSearchResponseEvent() {
 	}
 
 	/**
 	 * @param bookList
 	 */
 	public BookSearchResponseEvent(List<BookDisplay> bookDisplayList,
 			int totalInResultSet) {
 		super();
 		this.bookDisplayList = bookDisplayList;
		this.totalInResultSet = totalInResultSet;
 	}
 
 	public List<BookDisplay> getBookDisplayList() {
 		return bookDisplayList;
 	}
 
 	public void setBookDisplayList(List<BookDisplay> bookDisplayList) {
 		this.bookDisplayList = bookDisplayList;
 	}
 
 	public int getTotalInResultSet() {
 		return totalInResultSet;
 	}
 
 	public void setTotalInResultSet(int totalInResultSet) {
 		this.totalInResultSet = totalInResultSet;
 	}
 
 }
