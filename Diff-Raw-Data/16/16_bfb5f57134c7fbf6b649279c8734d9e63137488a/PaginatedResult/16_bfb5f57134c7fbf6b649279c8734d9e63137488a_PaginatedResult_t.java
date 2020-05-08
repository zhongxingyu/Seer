 /**
  * PaginatedResult Class
  *
  * @copyright 2011 Monits
  * @license   Copyright (C) 2010. All rights reserved
  * @version   Release: 1.0.0
  * @link      http://www.monits.com/
  * @since     1.0.0
  */
 package com.monits.commons;
 
 import java.util.List;
 
 /**
  * PaginatedResult Class
  *
  * @author    José Manuel Rolón<jmrolon@monits.com>
  * @copyright 2011 Monits
  * @license   Copyright (C) 2011. All rights reserved
  * @version   Release: 1.0.0
  * @link      http://www.monits.com/
  * @since     1.0.0
  */
 public class PaginatedResult<T> {
 
	private int actualPage;
 
 	private int totalPage;
 
 	private List<T> elements;
 
 	/**
 	 * Basic constructor with all fields
 	 *
	 * @param actualPage The Selected page
 	 * @param totalPage The total amount of pages
 	 * @param elements The items in the selected page
 	 */
	public PaginatedResult(int actualPage, int totalPage, List<T> elements) {
 		super();
		this.actualPage = actualPage;
 		this.totalPage = totalPage;
 		this.elements = elements;
 	}
 
 	/**
 	 * @return the acutalPage
 	 */
	public int getActualPage() {
		return actualPage;
 	}
 
 	/**
 	 * @return the totalPage
 	 */
 	public int getTotalPage() {
 		return totalPage;
 	}
 
 	/**
 	 * @return the elements
 	 */
 	public List<T> getElements() {
 		return elements;
 	}
 
 }
