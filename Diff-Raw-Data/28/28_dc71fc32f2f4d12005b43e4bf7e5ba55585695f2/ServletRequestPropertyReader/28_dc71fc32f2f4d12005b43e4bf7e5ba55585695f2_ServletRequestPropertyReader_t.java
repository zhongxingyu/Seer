 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.servlet;
 
 import java.util.HashMap;
 import java.util.StringTokenizer;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.AbstractPropertyReader;
 import org.xins.common.text.URLEncoding;
 
 /**
  * Implementation of a <code>PropertyReader</code> that returns the
 * request parameters from a <code>ServletRequest</code> object.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
 * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class ServletRequestPropertyReader
 extends AbstractPropertyReader {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
    * Constructs a new <code>ServletRequestPropertyReader</code> for a
	 * <code>ServletRequest</code>.
     *
     * @param request
     *    the {@link ServletRequest} object, cannot be <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>request == null</code>.
     */
    public ServletRequestPropertyReader(ServletRequest request)
    throws NullPointerException {
       super(request.getParameterMap());
    }
 
    /**
    * Constructs a new <code>ServletRequestPropertyReader</code> for an
	 * <code>HttpServletRequest</code>.
	 *
     * @param request
     *    the {@link HttpServletRequest} object, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null</code>.
	 *
	 * @since XINS 1.4.0
     */
    public ServletRequestPropertyReader(HttpServletRequest request)
    throws IllegalArgumentException {

		// Initially allocate a complete HashMap already
       super(new HashMap(20));
       
       // Check preconditions
       MandatoryArgumentChecker.check("request", request);
 
		// Get the HTTP query string
       String query = request.getQueryString();

      // Parse the parameters in the HTTP query string
       StringTokenizer stParameters = new StringTokenizer(query, "&");
       while (stParameters.hasMoreTokens()) {
          String nextParameter = stParameters.nextToken();
          int equalsPos = nextParameter.indexOf('=');
          if (equalsPos != -1 && equalsPos != nextParameter.length() -1) {
             String parameterKey = nextParameter.substring(0, equalsPos);
             String parameterValue = URLEncoding.decode(nextParameter.substring(equalsPos + 1));
             getPropertiesMap().put(parameterKey, parameterValue);
          }
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
