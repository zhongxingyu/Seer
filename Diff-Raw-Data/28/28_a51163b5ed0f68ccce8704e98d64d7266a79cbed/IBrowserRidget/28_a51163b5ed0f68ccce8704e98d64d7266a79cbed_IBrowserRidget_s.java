 /*******************************************************************************
  * Copyright  2009, 2011 Florian Pirchner and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Florian Pirchner  initial API and implementation (based on other ridgets of 
  *                    compeople AG)
  * compeople AG     - adjustments for Riena v1.2 - 3.0
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets;
 
 import org.eclipse.riena.ui.ridgets.listener.ILocationListener;
 
 /**
  * The browser ridget displays a html page fetched from a given URL.
  * 
  * @since 1.2
  */
 public interface IBrowserRidget extends IValueRidget {
 
 	/**
 	 * Implementations represent Java-side "functions" that are invokable from JavaScript.
 	 * 
 	 * @since 6.0
 	 * @see IBrowserRidget#mapScriptFunction(String, IBrowserRidgetFunction)
 	 */
 	interface IBrowserRidgetFunction {
 
 		/**
 		 * Executes a function, which is triggered by JavaScript from within the browser.
 		 * 
 		 * @param jsParams
 		 *            the parameters, passed to the JavaScript function
 		 * @return the result to return the the JavaScript caller
 		 * 
 		 * @see IBrowserRidget#mapScriptFunction(String, IBrowserRidgetFunction)
 		 */
 		Object execute(Object[] jsParams);
 	}
 
 	/**
 	 * Property name of the url property ({@value} ).
 	 * 
 	 * @see #getUrl()
 	 * @see #setUrl(String)
 	 */
 	String PROPERTY_URL = "url"; //$NON-NLS-1$
 
 	/**
 	 * Add a {@link ILocationListener} that is notified of URL changes of this
 	 * ridget.
 	 * <p>
 	 * Adding the same listener instance several times has no effect.
 	 * <p>
 	 * Implementation note: you should be aware that these listeners are not
 	 * notified of URL changes in these cases:
 	 * <ul>
 	 * <li>user actions that change the current page content (scripts, dom
 	 * manipulation)</li>
 	 * <li>user actions that open a new browser in a separate window (hyperlink
 	 * with '{@code target="_blank"}')</li>
 	 * <li>per design - changing the URL via API calls, such as
 	 * {@code setUrl(...)} or {@code setText(...)}, does not notfiy listeners,
 	 * to prevent endless listener-event-listener loops</li>
 	 * <li>per design - rebinding this ridget to another browser widget does not
 	 * notify listeners and is not blockable</li>
 	 * </ul>
 	 * 
 	 * @param listener
 	 *            a non-null {@link ILocationListener}
 	 * @since 3.0
 	 */
 	void addLocationListener(ILocationListener listener);
 
 	/**
 	 * Return the text last set into the ridget or null.
 	 * <p>
 	 * The default value is null.
 	 * 
 	 * @since 2.0
 	 */
 	String getText();
 
 	/**
 	 * Return the url of this ridget or null.
 	 * <p>
 	 * The default value is null.
 	 * 
 	 * @return the url as a String. It is not guaranteed that the return value
 	 *         is a valid url. For example it may be null, empty or browser
 	 *         specific, such as 'about:blank'.
 	 */
 	String getUrl();
 
 	/**
 	 * Returns true, if an OutputMarker was added.
 	 * <p>
 	 * If an OutputMarker is added, the browser widget will not allow leaving
 	 * the page (i.e. it is not possible to folow a link).
 	 */
 	boolean isOutputOnly();
 
 	/**
 	 * Remove a {@link ILocationListener} from this ridget.
 	 * 
 	 * @param listener
 	 *            a non-null {@link ILocationListener}
 	 * @since 3.0
 	 */
 	void removeLocationListener(ILocationListener listener);
 
 	/**
 	 * Adds and removes the default OutputMarker.
 	 * <p>
 	 * If an OutputMarker is added, the browser widget will not allow leaving
 	 * the page (i.e. it is not possible to folow a link).
 	 * 
 	 * @param outputOnly
 	 *            <code>true</code> if the ridget should be 'output only'
 	 *            (=cannot be edited), <code>false</code> otherwise.
 	 */
 	void setOutputOnly(boolean outputOnly);
 
 	/**
 	 * A html String to show in the ridget.
 	 * <p>
 	 * Setting the text will also set the url value to null or 'about:blank'.
 	 * <p>
 	 * Note: currently there is no synchronisation. Invoking setText(...) while
 	 * a page is loaded asynchronously from setUrl(...) call may have undefined
 	 * results.
 	 * 
 	 * @param text
 	 *            a String of HTML content.
 	 * 
 	 * @since 2.0
 	 */
 	void setText(String text);
 
 	/**
 	 * Sets the url.
 	 * <p>
 	 * Setting the url will also set the text value to null.
 	 * 
 	 * @param newUrl
 	 *            a String that conforms to the constraints dictated by the
 	 *            underlying widget (for example an SWT Browser with IE or
 	 *            Mozilla underneath). May be null, empty, or browser specific,
 	 *            such as 'about:blank'.
 	 */
 	void setUrl(String newUrl);
 
 	/**
 	 * Executes the given script on the page in the browser.
 	 * 
 	 * @param script
 	 *            the script to execute
 	 * @return <code>true</code> if the script was successfully executed.
 	 * @since 6.0
 	 */
 	boolean execute(String script);
 
 	/**
 	 * Makes a JavaScript function available in the browser. This way it is possible to call a Java-side implementation for this function.
 	 * 
 	 * @param functionName
 	 *            the name under which the function can be called from within the browser.
 	 * @param controller
 	 *            the function implementation
 	 * @since 6.0
 	 * @see IBrowserRidgetFunction
 	 */
 	void mapScriptFunction(String functionName, IBrowserRidgetFunction controller);
 
 	/**
 	 * Removes the JavaScript function, so that is not callable from within the browser anymore. If there is no function with the given name, calling this
 	 * method has no effect.
 	 * 
 	 * @param functionName
 	 *            the name under which the function can be called from within the browser.
 	 * @since 6.0
 	 */
 	void unmapScriptFunction(String functionName);
 }
