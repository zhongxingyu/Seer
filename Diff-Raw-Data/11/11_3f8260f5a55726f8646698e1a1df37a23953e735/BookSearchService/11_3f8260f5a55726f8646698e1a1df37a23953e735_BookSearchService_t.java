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
 package com.pronoiahealth.olhie.server.services;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.event.Event;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 
 import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
 import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
 import com.pronoiahealth.olhie.client.shared.constants.SecurityRoleEnum;
 import com.pronoiahealth.olhie.client.shared.events.BookSearchEvent;
 import com.pronoiahealth.olhie.client.shared.events.BookSearchResponseEvent;
 import com.pronoiahealth.olhie.client.shared.events.ServiceErrorEvent;
 import com.pronoiahealth.olhie.client.shared.vo.Book;
 import com.pronoiahealth.olhie.client.shared.vo.BookCategory;
 import com.pronoiahealth.olhie.client.shared.vo.BookCover;
 import com.pronoiahealth.olhie.client.shared.vo.BookForDisplay;
 import com.pronoiahealth.olhie.client.shared.vo.BookState;
 import com.pronoiahealth.olhie.server.dataaccess.orient.OODbTx;
 import com.pronoiahealth.olhie.server.security.SecureAccess;
 
 /**
  * BookSearchService.java<br/>
  * Responsibilities:<br/>
  * 1. Use to return a book based on a search query<br/>
  * 
  * @author Alex Roman
  * @version 1.0
  * @since Jun 25, 2013
  * 
  */
 @RequestScoped
 public class BookSearchService {
 	@Inject
 	private Logger log;
 
 	@Inject
 	private Event<BookSearchResponseEvent> bookSearchResponseEvent;
 
 	@Inject
 	private Event<ServiceErrorEvent> serviceErrorEvent;
 
 	@Inject
 	private TempCoverBinderHolder holder;
 
 	@Inject
 	@OODbTx
 	private OObjectDatabaseTx ooDbTx;
 
 	/**
 	 * Constructor
 	 * 
 	 */
 	public BookSearchService() {
 	}
 
 	/**
 	 * Finds a book and then fires a response with the found book.
 	 * 
 	 * @param bookFindByIdEvent
 	 */
 	@SecureAccess({ SecurityRoleEnum.ADMIN, SecurityRoleEnum.AUTHOR })
 	protected void observesBookSearchEvent(
 			@Observes BookSearchEvent bookSearchEvent) {
 		try {
 			String searchText = bookSearchEvent.getSearchText();
 			
 			log.log(Level.SEVERE, "observesBookSearchEvent: " + searchText);
 			
 			List<BookForDisplay> bookForDisplayList = new ArrayList<BookForDisplay>();
 			
 			//TODO: removed when query is fixed
			/*
 			bookForDisplayList.add(
 					new BookForDisplay("id", "Title 1", "John D", 4, "Test introduction",
 							"TOC", "06/26/1958", "400", "Book 1 Book2",
 							BookState.BOOK_STATE_INVISIBLE, new BookCategory("yellow", "Legal"),
 							new BookCover("Olhie/images/p1.png", "Paper")));
							*/

 			// Find Book
 			//TODO: fix the query
 			OSQLSynchQuery<Book> bQuery = new OSQLSynchQuery<Book>(
					"select from Book where bookTitle like :title");
 			HashMap<String, String> bparams = new HashMap<String, String>();
 			bparams.put("title", "%" + searchText + "%");
 			List<Book> bResult = ooDbTx.command(bQuery).execute(bparams);
 
 			for (Book book : bResult) {
 				log.log(Level.SEVERE, book.getBookTitle());
 
 				BookForDisplay bookForDisplay = new BookForDisplay(
 						book.getId(),
 						book.getBookTitle(),
 						book.getAuthorId(),
 						4,
 						book.getIntroduction(),
 						"TOC", 
 						"06/26/1958", 
 						"400", 
 						"Book 1 Book2",
 						BookState.BOOK_STATE_INVISIBLE, 
 						new BookCategory("yellow", "Legal"),
 						new BookCover("Olhie/images/p1.png", "Paper"));
 				bookForDisplayList.add(bookForDisplay);
 			}

 			// Fire the event
 			bookSearchResponseEvent.fire(new BookSearchResponseEvent(bookForDisplayList));
 		} catch (Exception e) {
 			String errMsg = e.getMessage();
 			log.log(Level.SEVERE, errMsg, e);
 			serviceErrorEvent.fire(new ServiceErrorEvent(errMsg));
 		}
 
 	}
 
 }
