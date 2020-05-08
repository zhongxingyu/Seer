 /*
 *
 *  JMoney - A Personal Finance Manager
 *  Copyright (c) 2004 Nigel Westbury <westbury@users.sourceforge.net>
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
 package net.sf.jmoney.serializeddatastore.formats;
 
 import java.beans.XMLDecoder;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.sax.SAXTransformerFactory;
 import javax.xml.transform.sax.TransformerHandler;
 import javax.xml.transform.stream.StreamResult;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.BankAccount;
 import net.sf.jmoney.model2.BankAccountInfo;
 import net.sf.jmoney.model2.CapitalAccount;
 import net.sf.jmoney.model2.Commodity;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.ExtendablePropertySet;
 import net.sf.jmoney.model2.ExtensionPropertySet;
 import net.sf.jmoney.model2.IListManager;
 import net.sf.jmoney.model2.IObjectKey;
 import net.sf.jmoney.model2.IValues;
 import net.sf.jmoney.model2.IncomeExpenseAccount;
 import net.sf.jmoney.model2.IncomeExpenseAccountInfo;
 import net.sf.jmoney.model2.ListKey;
 import net.sf.jmoney.model2.ListPropertyAccessor;
 import net.sf.jmoney.model2.PropertyAccessor;
 import net.sf.jmoney.model2.PropertyNotFoundException;
 import net.sf.jmoney.model2.PropertySet;
 import net.sf.jmoney.model2.PropertySetNotFoundException;
 import net.sf.jmoney.model2.ReferencePropertyAccessor;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.SessionInfo;
 import net.sf.jmoney.model2.Transaction;
 import net.sf.jmoney.serializeddatastore.IFileDatastore;
 import net.sf.jmoney.serializeddatastore.SerializedDatastorePlugin;
 import net.sf.jmoney.serializeddatastore.SessionManager;
 import net.sf.jmoney.serializeddatastore.SimpleListManager;
 import net.sf.jmoney.serializeddatastore.SimpleObjectKey;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * Implementation of the IFileDatastore extension listener
  * for the JMoney JMX format.
  * 
  * @author Nigel Westbury
  */
 public class JMoneyXmlFormat implements IFileDatastore {
 	public static String ID_FILE_FORMAT = "net.sf.jmoney.serializeddatastore.jmxFormat"; 
 	
 	/**
 	 * Date format used for dates in this file format:
 	 * yyyy.MM.dd
 	 */
 	static SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance();
 	static {
 		dateFormat.applyPattern("yyyy.MM.dd");
 	}
 	
 	/**
 	 * Read session from file.  The session is set as the open session
 	 * in the given session manager.
 	 * <P>
 	 * The opened session is set as the current open JMoney session. 
 	 * If no session can be opened then an appropriate message is
 	 * displayed to the user and the previous session, if any, is
 	 * left open.
 	 * <P>
 	 * If this method returns false then any previous session will be
 	 * left open.  The caller will not display any error message.
 	 * This method must display an appropriate error message if the
 	 * file cannot be read.
 	 * 
 	 * @return true if the file was successfully read and the session
 	 * 			was set in the given session manager, false if the user
 	 * 			cancelled the operation or if a failure occurred
 	 */
 	public boolean readSession(final File sessionFile, final SessionManager sessionManager, final IWorkbenchWindow window) {
 		try {
 			if (sessionFile.length() < 500000) {
 				// If the file is smaller than 500K then it is
 				// not worthwhile using a progress monitor.
 				// The monitor would flash up so quickly that the
 				// user could not read it.
 				readSessionQuietly(sessionFile, sessionManager, null);
 			} else {
 				IRunnableWithProgress readSessionRunnable = new IRunnableWithProgress() {
 					
 					public void run(IProgressMonitor monitor) throws InvocationTargetException {
 						// Set the number of work units in the monitor where
 						// one work unit is reading 100 Kbytes.
 						int workUnits = (int)(sessionFile.length()/100000);
 						
 						monitor.beginTask(
 								SerializedDatastorePlugin.getResourceString("MainFrame.OpeningFile") + " " + sessionFile, 
 								workUnits);   
 						
 						try {
 							readSessionQuietly(sessionFile, sessionManager, monitor);
 						} catch (Exception ex) {
 							throw new InvocationTargetException(ex);
 						} finally {
 							monitor.done();
 						}
 					}
 					
 				};
 				
 				ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(window.getShell());
 				
 				try {
 					progressDialog.run(true, false, readSessionRunnable);
 				} catch (InvocationTargetException e) {
 					throw e.getCause();
 				}
 			}
 		} catch (InterruptedException e) {
 			// If the user inturrupted the read then no error message is displayed.
 			// Currently this cannot happen because the cancel button is not
 			// enabled in the progress dialog, but if the cancel button is enabled
 			// then we do nothing here, leaving the previous session, if any, open.
 			return false;
 		} catch (Throwable ex) {
 			JMoneyPlugin.log(ex);
 			fileReadError(sessionFile, window);
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * This class extends FileInputStream and overrides the
 	 * various read methods, counting the total number of
 	 * bytes read and updating the progress monitor.
 	 * <P>
 	 * This stream is used as input to BufferedInputStream,
 	 * either directly or through GZIPInputStream.  Of all
 	 * the read methods, only read(byte b[], int off, int len)
 	 * is used by BufferedInputStream, and read() is used
 	 * occassionally by GZIPInputStream.  However, for completeness,
 	 * all the read methods have been overridden to update
 	 * the byte count.
 	 * Other methods that may affect the progress, such as
 	 * skip(n), do not appear to be called by the above
 	 * consumers of the stream.
 	 */
 	private class FileInputStreamWithMonitor extends FileInputStream {
 
 		private IProgressMonitor monitor;
 		private long totalBytes = 0;
 		private int previousTotalWork = 0;
 
 		/**
 		 * @param monitor The monitor to be updated.  This
 		 * 			parameter must be non-null.  The monitor
 		 * 			must have been initialized for an expected
 		 * 			amount of total work units where one work unit
 		 * 			is reading 100 KBytes of the input stream.
 		 */
 		FileInputStreamWithMonitor(File sessionFile, IProgressMonitor monitor) 
 			throws FileNotFoundException {
 			super(sessionFile);
 			this.monitor = monitor;
 		}
 		
 		/* This method reads a single byte at a time.
 		 * GZIPInputStream uses this method occassionally, so we increment
 		 * the count of bytes read just to stop errors creeping
 		 * in.  However, we don't bother to update the monitor.
 		 */
 		@Override
 		public int read() throws IOException {
 			totalBytes++;
 			return super.read();
 		}
 
 		@Override
 	    public int read(byte b[]) throws IOException {
 	    	int bytesRead = super.read(b);
 			updateProgress(bytesRead);
 			return bytesRead;
 	    }
 
 		@Override
 	    public int read(byte b[], int off, int len) throws IOException {
 		int bytesRead = super.read(b, off, len);
 		updateProgress(bytesRead);
 		return bytesRead;
 	    }
 
 	    /**
 	     * Update the progress monitor.  The number of bytes read from
 	     * the input stream is passed to this method and used to measure
 	     * the progress.
 	     *
 	     * @param bytesRead the number of bytes read from the input stream.
 	     */
 	    private void updateProgress(int bytesRead) {
 	    	if (bytesRead > 0) {
 	    		totalBytes += bytesRead;
 	    		int newTotalWork = (int)(totalBytes/100000);
 	    		if (newTotalWork > previousTotalWork) {
 	    			monitor.worked(newTotalWork - previousTotalWork);
 	    			previousTotalWork = newTotalWork;
 	    		}
 	    	}
 	    }
 	}
 	
 	/**
 	 * Read a session from file, creating a session manager and a
 	 * session.
 	 *
 	 * @param monitor Monitor into which this method will call
 	 * 			the beginTask method and update the progress.
 	 * 			This parameter may be null in which this method
 	 * 			will read the session without feedback on the progress.
 	 */
 	public void readSessionQuietly(File sessionFile, SessionManager sessionManager, IProgressMonitor monitor) throws FileNotFoundException, IOException, CoreException {
 		InputStream fin;
 		if (monitor == null) {
 			fin = new FileInputStream(sessionFile);
 		} else {
 			fin = new FileInputStreamWithMonitor(sessionFile, monitor);
 		}
 		
 		// If the extension is 'xml' then no compression is used.
 		// If the extension is 'jmx' then compression is used.
 		GZIPInputStream gin = null;
 		BufferedInputStream bin;
 		if (sessionFile.getName().endsWith(".xml")) {
 			bin = new BufferedInputStream(fin);
 		} else {
 			gin = new GZIPInputStream(fin);
 			bin = new BufferedInputStream(gin);
 		}
 
 		// First attempt to read the XML as though it is in the
 		// current format.
 		
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		try {
             try {
                 idToCommodityMap = new HashMap<String, Object>();
                 idToAccountMap = new HashMap<String, Object>();
                 currentSAXEventProcessor = null;
 
                 factory.setValidating(false);
                 factory.setNamespaceAware(true);
                 SAXParser saxParser = factory.newSAXParser();
                 HandlerForObject handler = new HandlerForObject(sessionManager);
                 saxParser.parse(bin, handler);
                 Session newSession = handler.getSession();
 
                 sessionManager.setSession(newSession);
             } catch (ParserConfigurationException e) {
                 throw new RuntimeException("Serious XML parser configuration error");
             } catch (SAXException e) {
                 // Workaround: OldFormatJMoneyFileException seems to be thrown in
                 // two different ways: Either embedded in a SAXException or
                 // directly as an OldFormatJMoneyFileException.
                 if (e.getException() instanceof OldFormatJMoneyFileException) {
                     throw (OldFormatJMoneyFileException) e.getException();
                 } else {
                     throw new RuntimeException("Fatal SAX parser error");
                 }
             }
         }
 		catch (OldFormatJMoneyFileException se) {
 			// This exception will be throw if the file is old format (0.4.5 or prior).
 			// Try reading as an old format file.
 
 			// First close and re-open the file.
 			bin.close();
 			if (gin != null) gin.close();
 			fin.close();
 			
 			if (monitor == null) {
 				fin = new FileInputStream(sessionFile);
 			} else {
 				fin = new FileInputStreamWithMonitor(sessionFile, monitor);
 			}
 			
 			// If the extension is 'xml' then no compression is used.
 			// If the extension is 'jmx' then compression is used.
 			if (sessionFile.getName().endsWith(".xml")) {
 				bin = new BufferedInputStream(fin);
 			} else {
 				gin = new GZIPInputStream(fin);
 				bin = new BufferedInputStream(gin);
 			}
 
 			// The XMLDecoder must use the same classpath that was used to load this class.
 			// The classpath set in this thread is the system class path, and if that
 			// is used then XMLDecoder will not be able to find the classes specified
 			// in the XML.  We must therefore temporarily replace the classpath.
 			// ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
 			// Thread.currentThread().setContextClassLoader(SerializedDatastorePlugin.getDefault().getDescriptor().getPluginClassLoader());
 			XMLDecoder dec = new XMLDecoder(bin);
 			Object newSession = dec.readObject();
 			dec.close();
 			// Thread.currentThread().setContextClassLoader(originalClassLoader);            
 
 			if  (!(newSession instanceof net.sf.jmoney.model.Session)) {
 				throw new CoreException(
 						new Status(Status.ERROR, "net.sf.jmoney.serializeddatastore", Status.OK,
 								"session object deserialized, but the object was not a session!",
 								null));	
 			}
 			
 			SimpleObjectKey key = new SimpleObjectKey(sessionManager); 
 			Session newSessionNewFormat = new Session(key, null);
 			key.setObject(newSessionNewFormat);
 			sessionManager.setSession(newSessionNewFormat);
 			
 			convertModelOneFormat((net.sf.jmoney.model.Session)newSession, newSessionNewFormat);
 		}
 		catch (IOException ioe) { 
 			throw new RuntimeException("IO internal exception error");
 		} finally {
 			bin.close();
 			if (gin != null) gin.close();
 			fin.close();
 		}
 	}
 	
 	private class HandlerForObject extends DefaultHandler {
 		
 		protected SessionManager sessionManager;
 		/**
 		 * The top level session object.
 		 */
 		private Session session;
 		
 		HandlerForObject(SessionManager sessionManager) {
 			this.sessionManager = sessionManager;
 		}
 		
 		Session getSession() {
 			return session;
 		}
 		
 		/**
 		 * Receive notification of the start of an element.
 		 *
 		 * <p>See if there is a setter for this element name.  If there is
 		 * then set the setter.  Otherwise set the setter to null to indicate
 		 * that any character data should be ignored.
 		 * </p>
 		 * @param name The element type name.
 		 * @param attributes The specified or defaulted attributes.
 		 * @exception org.xml.sax.SAXException Any SAX exception, possibly
 		 *            wrapping another exception.
 		 * @see org.xml.sax.ContentHandler#startElement
 		 */
 		@Override
 		public void startElement(String uri, String localName,
 				String qName, Attributes attributes)
 		throws SAXException {
 			if (currentSAXEventProcessor == null) {
 				if (!localName.equals("session")) {
 					if (localName.equals("java")) {
 						throw new OldFormatJMoneyFileException();
 					} else {
 						throw new SAXException("Unexpected top level element '" + localName + "' found.  The top level element must be either 'session' (new format file) or 'java' (old format file).");
 					}
 				}
 				
				currentSAXEventProcessor = new ObjectProcessor(sessionManager, null, SessionInfo.getPropertySet());
 			} else {
 				currentSAXEventProcessor.startElement(uri, localName, attributes);
 			}
 		}
 		
 		
 		/**
 		 * Receive notification of the end of an element.
 		 *
 		 * <p>Set the property accessor back to null.
 		 * </p>
 		 * @param name The element type name.
 		 * @param attributes The specified or defaulted attributes.
 		 * @exception org.xml.sax.SAXException Any SAX exception, possibly
 		 *            wrapping another exception.
 		 * @see org.xml.sax.ContentHandler#endElement
 		 */
 		@Override
 		public void endElement(String uri, String localName, String qName) {
 			SAXEventProcessor parent = currentSAXEventProcessor.endElement();
 			
 			if (parent == null) {
 				// We are back at the top level.
 				// Save this object because it is the session object.
 				session = (Session)currentSAXEventProcessor.getValue();
 			}
 			
 			currentSAXEventProcessor = parent;
 		}
 		
 		
 		/**
 		 * Receive notification of character data inside an element.
 		 *
 		 * <p>If a setter method is set then the character data is passed
 		 * to the setter.  Otherwise the character data is dropped.
 		 * </p>
 		 * @param ch The characters.
 		 * @param start The start position in the character array.
 		 * @param length The number of characters to use from the
 		 *               character array.
 		 * @exception org.xml.sax.SAXException Any SAX exception, possibly
 		 *            wrapping another exception.
 		 * @see org.xml.sax.ContentHandler#characters
 		 */
 		@Override
 		public void characters(char ch[], int start, int length)
 		throws SAXException {
 			if (currentSAXEventProcessor == null) {
 				throw new SAXException("data outside top element is illegal");
 			}
 			
 			currentSAXEventProcessor.characters(ch, start, length);
 		}
 	}
 	
 	abstract private class SAXEventProcessor {
 		protected SAXEventProcessor parent;
 		protected SessionManager sessionManager;
 		
 		/**
 		 * Creates a new SAXEventProcessor object.
 		 *
 		 * @param parent The event processor that was in effect.  This newly
 		 *        created event processor will take over and will process the
 		 *        contents of an element.  When the end tag for the element is
 		 *        found then this original event processor must be restored as
 		 *        the active event processor.
 		 */
 		SAXEventProcessor(SessionManager sessionManager, SAXEventProcessor parent) {
 			this.sessionManager = sessionManager;
 			this.parent = parent;
 		}
 		
 		/**
 		 * DOCUMENT ME!
 		 *
 		 * @param name DOCUMENT ME!
 		 * @param atts DOCUMENT ME!
 		 *
 		 * @throws SAXException DOCUMENT ME!
 		 */
 		public abstract void startElement(String uri, String localName, Attributes atts)	throws SAXException;
 		
 		/**
 		 * DOCUMENT ME!
 		 *
 		 * @param ch DOCUMENT ME!
 		 * @param start DOCUMENT ME!
 		 * @param length DOCUMENT ME!
 		 *
 		 * @throws SAXException DOCUMENT ME!
 		 */
 		public void characters(char ch[], int start, int length) {
 			/* ignore data by default */
 		}
 		
 		/**
 		 * DOCUMENT ME!
 		 *
 		 * @param name DOCUMENT ME!
 		 *
 		 * @return DOCUMENT ME!
 		 *
 		 * @throws SAXException DOCUMENT ME!
 		 */
 		public SAXEventProcessor endElement() {
 			return parent;
 		}
 
 		/**
 		 * This method is called each time the next processor down
 		 * the stack processed an 'end element' and returned control
 		 * to this processor.
 		 * <P>
 		 * The processor below will pass the object that it had
 		 * read in.  This object might be a scalar property value
 		 * or an object that itself has properties. 
 		 */
 		public void elementCompleted(Object value) {
 			// The default processing assumes that no inner
 			// element processors will ever pass values back.
 			// If inner elements may pass up values then this
 			// method must be overridden.
 			throw new RuntimeException("Value passed back from inner element but no value expected.");
 		}
 
 		/**
 		 * @return
 		 */
 		// TODO: This method can be called instead of using elementCompleted.
 		// elementCompleted can then be removed.
 		public abstract Object getValue();
 	}
 
 	/**
 	 * An event processor that takes over processing
 	 * while we are inside an object.  The processor looks for
 	 * elements representing the properties of the object.
 	 */
 	private class ObjectProcessor extends SAXEventProcessor {
 		
 		private ExtendablePropertySet<?> propertySet;
 		
 		/**
 		 * If we have processed the start of an element representing
 		 * a property but have not yet processed the end of the element
 		 * then this field is the property accessor.  Otherwise this
 		 * field is null.  This property may be a scalar property
 		 * (containing an extendable object) or a list property.
 		 */
 		private PropertyAccessor propertyAccessor = null;
 		
 		/**
 		 * Key to the object being parsed by this ObjectProcessor.
 		 */
 		SimpleObjectKey objectKey;
 		
 		/**
 		 * Key to the list that contains the object being parsed.  This is saved
 		 * because we will need it when the object is constructed.
 		 */
 		ListKey listKey;
 		
 		/**
 		 * The list of parameters to be passed to the constructor
 		 * of this object.
 		 */
 		Map<PropertyAccessor, Object> propertyValueMap = new HashMap<PropertyAccessor, Object>();
 		
 		Set<ExtensionPropertySet<?>> nonDefaultExtensions = new HashSet<ExtensionPropertySet<?>>();
 
 		/**
 		 * Saved id of objects that are Currency or Account objects.
 		 * This id is saved so that when the object is later created, it can
 		 * be added to a map.
 		 */
 		Map<String, Object> map;
 		String id;
 
 		Object value;
 		
 		/**
 		 *
 		 * @param parent The event processor that was in effect.  This newly
 		 *        created event processor will take over and will process the
 		 *        contents of an element.  When the end tag for the element is
 		 *        found then this original event processor must be restored as
 		 *        the active event processor.
 		 */
		ObjectProcessor(SessionManager sessionManager, ObjectProcessor parent, ExtendablePropertySet<?> propertySet) {
 			super(sessionManager, parent);
 			this.propertySet = propertySet;
 			
 			objectKey = new SimpleObjectKey(sessionManager);
 			
 			for (ListPropertyAccessor propertyAccessor: propertySet.getListProperties3()) {
 				propertyValueMap.put(propertyAccessor, new SimpleListManager(sessionManager, new ListKey(objectKey, propertyAccessor)));
 			}
 		}
 		
 		/**
 		 * DOCUMENT ME!
 		 *
 		 * @param name DOCUMENT ME!
 		 * @param atts DOCUMENT ME!
 		 *
 		 * @throws SAXException DOCUMENT ME!
 		 */
 		@Override
 		public void startElement(String uri, String localName, Attributes atts) {
 			// Kludge: "memo" is now used for both accounts and categories.
 			// This can be removed once all old xml format files have been updated.
 			if (localName.equals("description")) {
 				localName = "memo";
 			}
 			
 			// We set propertyAccessor to be the property accessor
 			// for the property whose value is contained in this
 			// element.  This property may be a scalar or a list
 			// property.
 			
 			// If the property is not found then we currently drop
 			// the value.
 			// TODO: Keep values even for unknown properties in
 			// case plug-ins are installed later that can use the data.
 		
 			// All elements are expected to be in a namespace beginning
 			// "http://jmoney.sf.net".  If the element is a property in an
 			// extension property set then the id of the extension property
 			// set will be appended.
 			String namespace;
 			if (uri.length() == 20) {
 				namespace = null;
 			} else {
 				namespace = uri.substring(21);
 			}
 			
 			try {
 				// Find the property accessor for this property.
 				// The property may be in the property set for the
 				// object or in the property set for any base objects,
 				// or may be in extensions of this or any base object.
 				// If no namespace is specified then we search only
 				// this and the base property sets.
 				
 				if (namespace == null) {
 					// Search this property set and base property sets,
 					// but exclude extensions.  
 					propertyAccessor = propertySet.getPropertyAccessorGivenLocalNameAndExcludingExtensions(localName);
 				} else {
 					ExtensionPropertySet<?> propertySet = PropertySet.getExtensionPropertySet(namespace);
 					propertyAccessor = propertySet.getProperty(localName);
 				}
 			} catch (PropertySetNotFoundException e) {
 				// The property no longer exists.
 				// TODO: Log this.  When changing the properties,
 				// one is supposed to provide upgrader properties
 				// for all obsoleted properties.
 				// We drop the value.
 				// Ignore content
 				currentSAXEventProcessor = new IgnoreElementProcessor(sessionManager, this, null);
 				return;
 			} catch (PropertyNotFoundException e) {
 					// The property no longer exists.
 					// TODO: Log this.  When changing the properties,
 					// one is supposed to provide upgrader properties
 					// for all obsoleted properties.
 					// We drop the value.
 					// Ignore content
 					currentSAXEventProcessor = new IgnoreElementProcessor(sessionManager, this, null);
 					return;
 			}
 			
 			map = null;
 			id = null;
 			
 			if (propertyAccessor.isScalar()) {
 				Class propertyClass = ((ScalarPropertyAccessor<?>)propertyAccessor).getClassOfValueObject();
 
 				// See if the 'idref' attribute is specified.
 				String idref = atts.getValue("idref");
 				if (idref != null) {
 					Object value;
 					
 					if (propertyClass == Currency.class) {
 						value = idToCommodityMap.get(idref);
 					} else if (propertyClass == Account.class) {
 						value = idToAccountMap.get(idref);
 					} else {
 						throw new RuntimeException("bad XML file");
 					}
 
 					// Process this element.
 					// Although we already have all the data we need
 					// from the start element, we still need a processor
 					// to process it.
 					// Ideally we should create another processor which
 					// gives errors if there is any additional data.
 
 					// We pass the value to the processor so that it can pass the value
 					// back to us!  (That is the design - it is up to the inner processor
 					// to supply the value.  It just so happens in the case of an idref
 					// that we know the value before we even create the inner processor).
 					
 					currentSAXEventProcessor = new IgnoreElementProcessor(sessionManager, this, value);
 				} else {
 					Assert.isTrue(!ExtendableObject.class.isAssignableFrom(propertyClass));
 
 					// Property class is primitive or primitive class
 					currentSAXEventProcessor = new PropertyProcessor(sessionManager, this, propertyClass);
 				}
 			} else {
 				ListPropertyAccessor<?> listProperty = (ListPropertyAccessor<?>)propertyAccessor;
 				ExtendablePropertySet<?> typedPropertySet = listProperty.getElementPropertySet();
 				Class propertyClass = typedPropertySet.getImplementationClass();
 
 				ExtendablePropertySet<?> actualPropertySet;
 				
 				if (typedPropertySet.isDerivable()) {
 					String propertySetId = atts.getValue("propertySet");
 					if (propertySetId == null) {
 						throw new RuntimeException("No 'propertySet' attribute specified when required.");
 					}
 					try {
 						actualPropertySet = PropertySet.getExtendablePropertySet(propertySetId);
 					} catch (PropertySetNotFoundException e) {
 						throw new RuntimeException("Invalid 'propertySet' attribute specified.");
 					}
 				} else {
 					actualPropertySet = typedPropertySet;
 				}
 				
 				// Save the id and appropriate map for this object so that the 
 				// object can be added to the map later when the object is created.
 				if (Commodity.class.isAssignableFrom(propertyClass)) {
 					map = idToCommodityMap;
 					id = atts.getValue("id");
 				} else if (Account.class.isAssignableFrom(propertyClass)) {
 					map = idToAccountMap;
 					id = atts.getValue("id");
 				}
 				
				currentSAXEventProcessor = new ObjectProcessor(sessionManager, this, actualPropertySet);
 			}
 		}
 			
 		/**
 		 * DOCUMENT ME!
 		 *
 		 * @param ch DOCUMENT ME!
 		 * @param start DOCUMENT ME!
 		 * @param length DOCUMENT ME!
 		 *
 		 * @throws SAXException DOCUMENT ME!
 		 */
 		@Override
 		public void characters(char ch[], int start, int length) {
 			for (int i = start; i < start + length; i++ ) {
 				if (ch[i] != ' ' && ch[i] != '\n' && ch[i] != '\t') {
 					throw new RuntimeException("unexpected character data found.");
 				}
 			}
 		}
 
 		@Override
 		public SAXEventProcessor endElement() {
 
 			IValues values = new IValues() {
 
 				public <V> V getScalarValue(ScalarPropertyAccessor<V> propertyAccessor) {
 					if (propertyValueMap.containsKey(propertyAccessor)) {
 						return propertyAccessor.getClassOfValueObject().cast(propertyValueMap.get(propertyAccessor));
 					} else {
 						return propertyAccessor.getDefaultValue();
 					}
 				}
 
 				public IObjectKey getReferencedObjectKey(ReferencePropertyAccessor<?> propertyAccessor) {
 					if (propertyValueMap.containsKey(propertyAccessor)) {
 						return ((ExtendableObject)propertyValueMap.get(propertyAccessor)).getObjectKey();
 					} else {
 						return null;
 					}
 				}
 
 				public <E extends ExtendableObject> IListManager<E> getListManager(IObjectKey listOwnerKey, ListPropertyAccessor<E> listAccessor) {
 					return (IListManager<E>)propertyValueMap.get(listAccessor);
 				}
 
 				public Collection<ExtensionPropertySet<?>> getNonDefaultExtensions() {
 					return nonDefaultExtensions;
 				}
 			};
 			
 			// We can now create the object.
 			ExtendableObject extendableObject = propertySet.constructImplementationObject(objectKey, listKey, values);
 			
 			objectKey.setObject(extendableObject);
 			
 			// TODO: Move this out of format specific code
 			if (extendableObject instanceof Account) {
 				Account account = (Account)extendableObject;
 				sessionManager.addAccountList(account);
 			}
 			if (extendableObject instanceof Entry) {
 				Entry entry = (Entry)extendableObject;
 				if (entry.getAccount() != null) {
 					sessionManager.addEntryToList(entry.getAccount(), entry);
 				}
 			}
 			
 			// Pass the value back up to the outer element processor.
 			if (parent != null) {
 				parent.elementCompleted(extendableObject);
 			}
 			
 			// Save the value so that getValue can return it.
 			// TODO: Change this method so it returns the value,
 			// and replace the getValue method with a getParent method.
 			// That would be a little cleaner.
 			value = extendableObject;
 			
 			return parent;
 		}
 
 		/**
 		 * The inner element processor has returned a value to us.
		 * We now set the value into the apppropriate property or
 		 * add it to the appropriate list.
 		 */
 		@Override
 		public void elementCompleted(Object value) {
 			/*
 			 * Now we have the value of this property. If it is null, something
 			 * is wrong, because null values are not written out. An empty
 			 * element may exist, but only if the element content is of a type
 			 * that can construct a valid non-null value from an empty string,
 			 * so no null values should be here.
 			 */
 			if (value == null) {
 				throw new RuntimeException("null value");
 			}
 			
 			if (propertyAccessor == null) {
 				throw new RuntimeException("internal error");
 			}
 			
 			// Set the value in our object.  If the property
 			// is a list property then the object is added to
 			// the list.
 			if (propertyAccessor.isScalar()) {
 				propertyValueMap.put(propertyAccessor, value);
 			} else {
 				// Must be an element in an array.
 				SimpleListManager list = (SimpleListManager)propertyValueMap.get(propertyAccessor);
 				list.add(value);
 				
 				// For Currency and Account objects, we also add to a map so that
 				// references can be resolved.
 				if (map != null) {
 					map.put(id, value);
 				}
 			}
 			
 			/*
 			 * Update set of all extensions for which a property value has
 			 * been set.
 			 */
 			if (propertyAccessor.getPropertySet() instanceof ExtensionPropertySet) {
 				nonDefaultExtensions.add((ExtensionPropertySet)propertyAccessor.getPropertySet());
 			}
 		}
 
 		/* (non-Javadoc)
 		 * @see net.sf.jmoney.serializeddatastore.SerializedDatastorePlugin.SAXEventProcessor#getValue()
 		 */
 		@Override
 		public Object getValue() {
 			return value;
 		}
 	}
 
 	/**
 	 * An event processor that takes over processing while we are inside a
 	 * scalar property. The processor looks for the character content of the
 	 * element (which gives the value of the property).
 	 */
 	private class PropertyProcessor extends SAXEventProcessor {
 		/**
 		 * Class of the property we are expecting.
 		 * This may be a primative or Date.
 		 */
 		Class propertyClass;
 		
 		/**
 		 * Value of the property to be returned to the outer
 		 * processor.
 		 */
 		Object value = null;
 		
 		String s = "";
 		
 		/**
 		 *
 		 * @param parent The event processor that was in effect.  This newly
 		 *        created event processor will take over and will process the
 		 *        contents of an element.  When the end tag for the element is
 		 *        found then this original event processor must be restored as
 		 *        the active event processor.
 		 */
 		PropertyProcessor(SessionManager sessionManager, SAXEventProcessor parent, Class<?> propertyClass) {
 			super(sessionManager, parent);
 			this.propertyClass = propertyClass;
 		}
 		
 		/**
 		 * DOCUMENT ME!
 		 *
 		 * @param name DOCUMENT ME!
 		 * @param atts DOCUMENT ME!
 		 *
 		 * @throws SAXException DOCUMENT ME!
 		 */
 		@Override
 		public void startElement(String uri, String localName, Attributes atts)
 		throws SAXException {
 			throw new SAXException("element not expected inside scalar property");
 		}
 			
 		/**
 		 * DOCUMENT ME!
 		 *
 		 * @param ch DOCUMENT ME!
 		 * @param start DOCUMENT ME!
 		 * @param length DOCUMENT ME!
 		 *
 		 * @throws SAXException DOCUMENT ME!
 		 */
 		@Override
 		public void characters(char ch[], int start, int length) {
 			s += new String(ch, start, length);
 		}
 
 		@Override
 		public SAXEventProcessor endElement() {
 			// TODO: change this.  Find a constructor from string.
 			if (propertyClass.equals(Integer.class)) {
 				value = new Integer(s);
 			} else if (propertyClass.equals(Long.class)) {
 				value = new Long(s);
 			} else if (propertyClass.equals(String.class)) {
 				value = s;
 			} else if (propertyClass.equals(Character.class)) {
 				value = new Character(s.charAt(0));
 			} else if (propertyClass.equals(Boolean.class)) {
 				value = new Boolean(s);
 			} else if (propertyClass.equals(Date.class)) {
 				try {
 					value = dateFormat.parse(s);
 				} catch (ParseException e) {
 					// If the date does not parse then the file is not
 					// valid, so throw an exception to cause a file read
 					// failure.
 					throw new RuntimeException("file contains invalid date");
 				}
 			} else {
 				// The property value is an class that is in none of the above
 				// categories.  We therefore use the string constructor to construct
 				// the object.
 				try {
 					value = propertyClass
 					.getConstructor( new Class [] { String.class } )
 					.newInstance(new Object [] { s });
 				} catch (Exception e) {
 					// The classes used in the data model are checked when the
 					// PropertySet and PropertyAccessor static fields are
 					// initialized.  Therefore other plug-ins should not be
 					// able to cause an error here.
 					// TODO: put the above mentioned check into the
 					// initialization code.
 					e.printStackTrace();
 					throw new RuntimeException("internal error");
 				}
 			}
 			
 			// Pass the value back up to the outer element processor.
 			parent.elementCompleted(value);
 			
 			return parent;
 		}
 
 		/* (non-Javadoc)
 		 * @see net.sf.jmoney.serializeddatastore.SerializedDatastorePlugin.SAXEventProcessor#getValue()
 		 */
 		@Override
 		public Object getValue() {
 			return value;
 		}
 	}
 
 
 	/**
 	 * Process events that occur within any element for which we are not
 	 * interested in the contents.
 	 * <P>
 	 * This class does double duty.  It processes both elements with an idref
 	 * and elements for which we know nothing about the object.  In the former
 	 * case, a non-null value is passed to the constructor which is passed back
 	 * as the value of this element.  In the latter case a null value is passed.
 	 */
 	private class IgnoreElementProcessor extends SAXEventProcessor {
 		private Object value;
 		
 		/**
 		 * Creates a new IgnoreElementProcessor object.
 		 *
 		 * @param parent DOCUMENT ME!
 		 * @param elementName DOCUMENT ME!
 		 */
 		IgnoreElementProcessor(SessionManager sessionManager, SAXEventProcessor parent, Object value) {
 			super(sessionManager, parent);
 			this.value = value;
 		}
 		
 		/**
 		 * Process elements that occur within an element for which we are
 		 * ignoring content.
 		 *
 		 * @param name The name of the element found inside the element.
 		 * @param atts A map object that contains the names and values of all
 		 *        the attributes for the element.
 		 *
 		 * @throws SAXException DOCUMENT ME!
 		 */
 		@Override
 		public void startElement(String uri, String localName, Attributes atts) {
 			// If we are ignoring an element, also ignore elements inside it.
 			if (value != null) {
 				throw new RuntimeException("Cannot have content inside an element with an idref");
 			}
 			currentSAXEventProcessor = new IgnoreElementProcessor(sessionManager, this, null);
 		}
 		
 		/**
 		 * DOCUMENT ME!
 		 *
 		 * @param name DOCUMENT ME!
 		 *
 		 * @return DOCUMENT ME!
 		 *
 		 * @throws SAXException DOCUMENT ME!
 		 */
 		@Override
 		public SAXEventProcessor endElement() {
 			if (value != null) {
 				parent.elementCompleted(value);
 			}
 			
 			return parent;
 		}
 
 		/* (non-Javadoc)
 		 * @see net.sf.jmoney.serializeddatastore.SerializedDatastorePlugin.SAXEventProcessor#getValue()
 		 */
 		@Override
 		public Object getValue() {
 			return value;
 		}
 	}
 	
 
 
 	// Used for writing
 
 	/**
 	 * PropertySet to String (namespace prefix)
 	 */
 	Map<PropertySet, String> namespaceMap;
 	int accountId;
 	Map<Account, String> accountIdMap;
 	
 	// Used for reading
 	Map<String, Object> idToCommodityMap;
 	Map<String, Object> idToAccountMap;
 	
 	/**
 	 * Current event processor.  A stack of event processors
 	 * is maintained as the XML is parsed.  Each event
 	 * processor has a reference to the previous (next
 	 * outer) event processor.
 	 */
 	public SAXEventProcessor currentSAXEventProcessor;
 
 
 	/**
 	 * Write session to file.
 	 */
 	public void writeSession(final SessionManager sessionManager, final File sessionFile, IWorkbenchWindow window)  {
 		// If there is any modified data in the controls in any of the
 		// views, then commit these to the database now.
 		// TODO: How do we do this?  Should framework call first
 		// commitRemainingUserChanges();
 		
 		try {
 			if (/*session.getTransactionCount() < 1000*/ false) {
 				// If the session has less than 1000 transactions then it is
 				// not worthwhile using a progress monitor.
 				// The monitor would flash up so quickly that the
 				// user could not read it.
 				writeSessionQuietly(sessionManager, sessionFile, null);
 			} else {
 				IRunnableWithProgress writeSessionRunnable = new IRunnableWithProgress() {
 					
 					public void run(IProgressMonitor monitor) throws InvocationTargetException {
 						// Set the number of work units in the monitor where
 						// one work unit is writing 500 transactions
 						//int workUnits = (int)(session.getTransactionCount()/500);
 						int workUnits =  IProgressMonitor.UNKNOWN;
 						
 						monitor.beginTask(
 								SerializedDatastorePlugin.getResourceString("MainFrame.SavingFile") + " " + sessionFile, 
 								workUnits);   
 						
 						try {
 							writeSessionQuietly(sessionManager, sessionFile, monitor);
 						} catch (Exception ex) {
 							throw new InvocationTargetException(ex);
 						} finally {
 							monitor.done();
 						}
 					}
 					
 				};
 				
 				ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(window.getShell());
 				
 				try {
 					progressDialog.run(true, false, writeSessionRunnable);
 				} catch (InvocationTargetException e) {
 					throw e.getCause();
 				}
 			}
 		} catch (InterruptedException e) {
 			// If the user inturrupted the write then we do nothing.
 			// Currently this cannot happen because the cancel button is not
 			// enabled in the progress dialog, but if the cancel button is enabled
 			// then a message should perhaps be displayed here indicating that the
 			// file is unusable.
 		} catch (Throwable ex) {
 			JMoneyPlugin.log(ex);
 			fileWriteError(sessionFile, window);
 		}
 	}
 	
 
 	/**
 	 * Write session to file.
 	 *
 	 * @param monitor Monitor into which this method will call
 	 * 			the beginTask method and update the progress.
 	 * 			This parameter may be null in which this method
 	 * 			will write the session without feedback on the progress.
 	 */
 	// TODO: update the monitor, perhaps by counting the transactions. 
 	public void writeSessionQuietly(SessionManager sessionManager, File sessionFile, IProgressMonitor monitor)  
 	throws IOException, SAXException, TransformerConfigurationException {
 
 		FileOutputStream fout = new FileOutputStream(sessionFile);
 		
 		// If the extension is 'xml' then no compression is used.
 		// If the extension is 'jmx' then compression is used.
 		BufferedOutputStream bout;
 		if (sessionFile.getName().endsWith(".xml")) {
 			bout = new BufferedOutputStream(fout);
 		} else {
 			GZIPOutputStream gout = new GZIPOutputStream(fout);
 			bout = new BufferedOutputStream(gout);
 		}
 		
 		namespaceMap = new HashMap<PropertySet, String>();
 		accountId = 1;
 		accountIdMap = new HashMap<Account, String>();
 		
 		StreamResult streamResult = new StreamResult(bout);
 		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
 		// SAX2.0 ContentHandler.
 		TransformerHandler hd = tf.newTransformerHandler();
 		Transformer serializer = hd.getTransformer();
 		serializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
 		serializer.setOutputProperty(OutputKeys.INDENT,"no");
 		hd.setResult(streamResult);
 		hd.startDocument();
 		writeObject(hd, sessionManager.getSession(), "session", Session.class);
 		hd.endDocument();
 		
 		bout.close();
 		fout.close();
 	}
 	
 
 	/**
 	 * 
 	 * @param hd
 	 * @param object
 	 * @param elementName
 	 * @param propertyType The typed class of the property.  The property may be an object
 	 * 			of a class that is derived from this typed class.  If the property is a
 	 * 			scalar property then the property type is determined by inspecting the
 	 * 			getter and setter methods.  If the property is a list property then the
 	 * 			type is determined by inspecting the adder and remover methods. 
 	 * @throws SAXException
 	 */
 	void writeObject(TransformerHandler hd, ExtendableObject object, String elementName, Class propertyType) throws SAXException {
 		// Find the property set information for this object.
 		ExtendablePropertySet<?> propertySet = PropertySet.getPropertySet(object.getClass());
 
 		AttributesImpl atts = new AttributesImpl();
 		
 		// Generate and declare the namespace prefixes.
 		// All extension property sets have namespace prefixes.
 		// Properties in base and derived property sets must be
 		// unique within each object, so are all put in the
 		// default namespace.
 		atts.clear();
 		if (propertyType == Session.class) {
 			atts.addAttribute("", "", "xmlns", "CDATA", "http://jmoney.sf.net");
 
 			int suffix = 1;
 			for (ExtensionPropertySet<?> extensionPropertySet: PropertySet.getAllExtensionPropertySets()) {
 				// Put into our map.
 				String namespacePrefix = "ns" + new Integer(suffix++).toString();
 				namespaceMap.put(extensionPropertySet, namespacePrefix);
 
 				atts.addAttribute("", "", "xmlns:" + namespacePrefix, "CDATA", "http://jmoney.sf.net/" + extensionPropertySet.getId());
 			}
 		}
 		
 		String id = null;
 		if (object instanceof Currency) {
 			id = ((Currency)object).getCode();
 		} else if (object instanceof Account) {
 			id = "account" + new Integer(accountId++).toString();
 			accountIdMap.put((Account)object, id);
 		}
 
 		if (id != null) {
 			atts.addAttribute("", "", "id", "CDATA", id);
 		}
 
 		if (propertySet.getImplementationClass() != propertyType) {
 			atts.addAttribute("", "", "propertySet", "CDATA", propertySet.getId());
 		}
 
 		hd.startElement("", "", elementName, atts);
 		
 		/*
 		 * Write all properties for this object, including properties from base
 		 * objects and properties from extensions.
 		 * 
 		 * For derived property sets, information must be in the XML that allows
 		 * the derived property set to be determined. This is done by outputting
 		 * the actual final property set id. The property set id is specified as
 		 * an attribute.
 		 * 
 		 * When an object is not owned, an id is specified. These are specified
 		 * as 'id' and 'idref' attributes in the normal way.
 		 * 
 		 * Write the list properties. This is done before the properties because
 		 * then, as it happens, we get no problems due to the single pass.
 		 * 
 		 * TODO: we cannot rely on this mechanism to ensure all idref's are
 		 * written before they are used.
 		 */
 		for (ListPropertyAccessor<?> listAccessor: propertySet.getListProperties3()) {
 			PropertySet<?> propertySet2 = listAccessor.getPropertySet(); 
 			if (!propertySet2.isExtension()
 					|| object.getExtension((ExtensionPropertySet<?>)propertySet2, false) != null) {
 				for (ExtendableObject listElement: object.getListPropertyValue(listAccessor)) {
 					writeObject(hd, listElement, listAccessor.getLocalName(), listAccessor.getElementPropertySet().getImplementationClass());
 				}
 			}
 		}
 		
 		for (ScalarPropertyAccessor<?> propertyAccessor: propertySet.getScalarProperties3()) {
 			PropertySet<?> propertySet2 = propertyAccessor.getPropertySet(); 
 			if (!propertySet2.isExtension()
 					|| object.getExtension((ExtensionPropertySet<?>)propertySet2, false) != null) {
 				String name = propertyAccessor.getLocalName();
 				Object value = object.getPropertyValue(propertyAccessor);
 
 				/*
 				 * If no element for a property exists in the file then the
 				 * property value is treated as null. Therefore, if the
 				 * property value is null, we do not write out an element.
 				 * 
 				 * Strings are a special case because JMoney treats null
 				 * strings and empty strings the same. If a string is empty,
 				 * we treat the string as null and do not write out the
 				 * value.
 				 */ 
 
 				if (value instanceof String 
 						&& ((String)value).length() == 0) {
 					value = null;
 				}
 
 				if (value != null) {
 					atts.clear();
 
 					String idref = null;
 					if (propertyAccessor.getClassOfValueObject() == Currency.class) {
 						idref = ((Currency)value).getCode();
 					} else if (value instanceof Account) {
 						idref = accountIdMap.get(value);
 					}
 					if (idref != null) {
 						atts.addAttribute("", "", "idref", "CDATA", idref);
 					}
 
 					String qName;
 					if (propertySet2.isExtension()) {
 						String namespacePrefix = namespaceMap.get(propertySet2); 
 						qName = namespacePrefix + ":" + name;
 					} else {
 						qName = name;
 					}
 					hd.startElement("", "", qName, atts);
 
 					if (idref == null) {
 						String text;
 						if (value instanceof Date) {
 							Date date = (Date)value;
 							text = dateFormat.format(date);
 						} else {
 							text = value.toString();
 						}
 						hd.characters(text.toCharArray(), 0, text.length());
 					}
 
 					hd.endElement("", "", qName);
 				}
 			}
 		}
 		
 		hd.endElement("", "", elementName);
 	}
 	
 	/**
 	 * This method is used when reading a session.
 	 */
 	public void fileReadError(File file, IWorkbenchWindow window) {
 		String message = SerializedDatastorePlugin.getResourceString("MainFrame.CouldNotReadFile")
 		+ " "
 		+ file.getPath();
 		String title = SerializedDatastorePlugin.getResourceString("MainFrame.FileError");
 		
 		MessageDialog.openError(
 				window.getShell(),
 				title,
 				message);
 	}
 	
 	/**
 	 * This method is used when writing a session.
 	 */
 	public void fileWriteError(File file, IWorkbenchWindow window) {
 		String message = SerializedDatastorePlugin.getResourceString("MainFrame.CouldNotWriteFile")
 		+ " "
 		+ file.getPath();
 		String title = SerializedDatastorePlugin.getResourceString("MainFrame.FileError");
 		
 		MessageDialog.openError(
 				window.getShell(),
 				title,
 				message);
 	}
 	
 	/**
 	 * Converts an old format session (net.sf.jmoney.model.Session) to
 	 * the latest format session (net.sf.jmoney.model2.Session).
 	 * The current model is implemented in the net.sf.jmoney.model2
 	 * package.   The net.sf.jmoney.model package implements an older
 	 * model that is now obsolete.  This method allows persistent
 	 * serializations of the old model to be converted to the new model
 	 * to ensure backwards compatibility.
 	 */
 	private void convertModelOneFormat(net.sf.jmoney.model.Session oldFormatSession, Session newSession) {
 		Map<Object, Account> accountMap = new Hashtable<Object, Account>();
 		
 		// Add the currencies
 		JMoneyPlugin.initSystemCurrency(newSession);
 		
 		// Add the income and expense accounts
 		net.sf.jmoney.model.CategoryNode root = oldFormatSession.getCategories().getRootNode();
 		for (Enumeration e = root.children(); e.hasMoreElements();) {
 			net.sf.jmoney.model.CategoryNode node = (net.sf.jmoney.model.CategoryNode) e.nextElement();
 			Object obj = node.getUserObject();
 			if (obj instanceof net.sf.jmoney.model.SimpleCategory) {
 				net.sf.jmoney.model.SimpleCategory oldCategory = (net.sf.jmoney.model.SimpleCategory) obj;
 				IncomeExpenseAccount newCategory = newSession.createAccount(IncomeExpenseAccountInfo.getPropertySet());
 				copyCategoryProperties(oldCategory, newCategory, accountMap);
 			}
 		}
 		
 		// Add the capital accounts
 		Vector oldAccounts = oldFormatSession.getAccounts();
 		for (Iterator iter = oldAccounts.iterator(); iter.hasNext(); ) {
 			net.sf.jmoney.model.Account oldAccount = (net.sf.jmoney.model.Account)iter.next();
 			
             BankAccount newAccount = newSession.createAccount(BankAccountInfo.getPropertySet());
 			newAccount.setName(oldAccount.getName());
 			newAccount.setAbbreviation(oldAccount.getAbbrevation());
 			newAccount.setAccountNumber(oldAccount.getAccountNumber());
 			newAccount.setBank(oldAccount.getBank());
 			newAccount.setComment(oldAccount.getComment());
 			newAccount.setCurrency(JMoneyPlugin.getIsoCurrency(newSession, oldAccount.getCurrencyCode()));
 			newAccount.setMinBalance(oldAccount.getMinBalance());
 			newAccount.setStartBalance(oldAccount.getStartBalance());
 
 			accountMap.put(oldAccount, newAccount);
 		}
 		
 		// Add the transactions and entries
 		
 		// We must be very careful here.  Consider a split entry that
 		// contain a double entry within it.  The other account in the
 		// double entry does not see the split entry.  There is simply no
 		// way of getting to the split entry from the other account.
 		// If we create a new format transaction for the old format
 		// double entry then we are in trouble because we would need to
 		// find and amend the transaction later when we find the split
 		// entry.
 		
 		// When we find a split entry, we create the entire transaction
 		// at that time.  We know that the other half of any double entries
 		// in the split entry cannot also be in a split entry, because
 		// this could not have happened under the old model.
 		// When we find a double entry (that is not part of a split entry)
 		// we do not create the transaction because we do not know if the
 		// other half of the entry is in a split entry.  We add the
 		// double entry to the set of double entries previously found.
 		// However, if the other half of this entry is in the set then
 		// we know neither half of the double entry is in a split entry,
 		// so we create the transaction at that time.
 		
 		// Here is the set of double entries that have been found but
 		// not yet processed.
 		Set<net.sf.jmoney.model.DoubleEntry> doubleEntriesPreviouslyFound = new HashSet<net.sf.jmoney.model.DoubleEntry>();
 		
 		// See if the plug-in for the reconciliation state is present.
 		// If it is then we can copy the reconciliation state into
 		// the extension for this plug-in.
 		// This is an example of a plug-in that does not depend on another
 		// plug-in but will use it if it is there.
 		ScalarPropertyAccessor<?> statusProperty;
 		try {
 			ExtensionPropertySet<?> reconciliationProperties = PropertySet.getExtensionPropertySet("net.sf.jmoney.reconciliation.entryProperties");
 			statusProperty = (ScalarPropertyAccessor<?>)reconciliationProperties.getProperty("status");
 		} catch (PropertySetNotFoundException e) {
 			// If the property set is not found then this means
 			// the reconciliation plug-in is not installed.
 			// We simply drop the reconciliation field in such
 			// circumstances.
 			// TODO It would be better if we saved the data
 			// in case the user installs the plug-in later.
 			// To do this, we must create a general purpose
 			// property class that is able to store any property
 			// given to it.
 			// Alternatively, we could not do the above but
 			// recommend creating an extension property and then
 			// create a propagator to get the value into the
 			// reconciliation plug-in.  This would be better
 			// if this process could be made more efficient.
 			statusProperty = null;
 		} catch (PropertyNotFoundException e) {
 			// If the property is not found then this means
 			// the reconciliation plug-in has been updated to
 			// a later version and the 'status' property is not
 			// longer supported in the later version.
 			// The reconciliation plug-in should provide a 'status'
 			// property with a setter only so that upgrades are
 			// possible.  However, plug-ins do not have to support
 			// unlimited past versions (or should they)?
 			// We simply drop the reconciliation field in such
 			// circumstances.
 			statusProperty = null;
 		}
 		
 		for (Iterator iter = oldAccounts.iterator(); iter.hasNext(); ) {
 			net.sf.jmoney.model.Account oldAccount = (net.sf.jmoney.model.Account)iter.next();
 			CapitalAccount newAccount = (CapitalAccount)accountMap.get(oldAccount);
 
 			// As all accounts in the old format are bank accounts, we can get
 			// the currency of the account simply by casting to BankAccount.
 			Currency currencyForCategories =  
 				((BankAccount)newAccount).getCurrency();
 				
 			for (Iterator entryIter = oldAccount.getEntries().iterator(); entryIter.hasNext(); ) {
 				net.sf.jmoney.model.Entry oldEntry = (net.sf.jmoney.model.Entry)entryIter.next();
 				
 				if (oldEntry instanceof net.sf.jmoney.model.DoubleEntry) {
 					net.sf.jmoney.model.DoubleEntry de = (net.sf.jmoney.model.DoubleEntry)oldEntry;
 					// Only add this transaction if we have already come across the
 					// other half of this entry and so we know the other half is not
 					// part of a split entry.
 					if (doubleEntriesPreviouslyFound.contains(de.getOther())) {
 						Transaction trans = newSession.createTransaction();
 						trans.setDate(de.getDate());
 						Entry entry1 = trans.createEntry();
 						Entry entry2 = trans.createEntry();
 						entry1.setAmount(de.getAmount());
 						entry2.setAmount(-de.getAmount());
 						entry1.setAccount(accountMap.get(de.getOther().getCategory()));
 						entry2.setAccount(accountMap.get(de.getCategory()));
 						
 						copyEntryProperties(de, entry1, statusProperty);
 						copyEntryProperties(de.getOther(), entry2, statusProperty);
 					} else {
 						doubleEntriesPreviouslyFound.add(de);
 					}
 				} else if (oldEntry instanceof net.sf.jmoney.model.SplittedEntry) {
 					net.sf.jmoney.model.SplittedEntry se = (net.sf.jmoney.model.SplittedEntry)oldEntry;
 					
 					Transaction trans = newSession.createTransaction();
 					trans.setDate(oldEntry.getDate());
 					
 					// Add the entry for the account that was holding the split entry.
 					Entry subEntry = trans.createEntry();
 					subEntry.setAmount(oldEntry.getAmount());
 					subEntry.setAccount(newAccount);
 					
 					copyEntryProperties(oldEntry, subEntry, statusProperty);
 					
 					// Add an entry for each old entry in the split.
 					for (Iterator subEntryIter = se.getEntries().iterator(); subEntryIter.hasNext(); ) {
 						net.sf.jmoney.model.Entry oldSubEntry = (net.sf.jmoney.model.Entry)subEntryIter.next();
 						
 						subEntry = trans.createEntry();
 						subEntry.setAmount(oldSubEntry.getAmount());
 						subEntry.setAccount(accountMap.get(oldSubEntry.getCategory()));
 						copyEntryProperties(oldSubEntry, subEntry, statusProperty);
 						
 						
 						// Under the old model, all categories are multi-currency categories
 						// and the currency of the category matches the currency of the account
 						// entry.  We must set the currency.
 						if (subEntry.getAccount() instanceof IncomeExpenseAccount) {
 							subEntry.setIncomeExpenseCurrency(currencyForCategories);
 						}
 					}
 				} else {
 					Transaction trans = newSession.createTransaction();
 					trans.setDate(oldEntry.getDate());
 					Entry entry1 = trans.createEntry();
 					Entry entry2 = trans.createEntry();
 					entry1.setAmount(oldEntry.getAmount());
 					entry2.setAmount(-oldEntry.getAmount());
 					entry1.setAccount(newAccount);
 					if (oldEntry.getCategory() != null) {
 					    entry2.setAccount(accountMap.get(oldEntry.getCategory()));
 					}
 					
 					// Put the check, memo, valuta, and status into the account entry only.
 					// Assume the creation and description apply to both account and
 					// category.
 					copyEntryProperties(oldEntry, entry1, statusProperty);
 					
 					entry2.setCreation(oldEntry.getCreation());
 					entry2.setMemo(oldEntry.getDescription());
 					
 					// Under the old model, all categories are multi-currency categories
 					// and the currency of the category matches the currency of the account
 					// entry.  We must set the currency.
 					entry2.setIncomeExpenseCurrency(currencyForCategories);
 				}
 			}
 		}
 	}
 
     /**
 	 * Copies category properties across from old to new.  Sub-categories are also
 	 * copied across.
 	 * 
 	 * @param accountMap this and all sub-categories are added to this map, mapping
 	 * 			old categories to the new categories
 	 */
 	private void copyCategoryProperties(net.sf.jmoney.model.SimpleCategory oldCategory, IncomeExpenseAccount newCategory, Map<Object, Account> accountMap) {
 		accountMap.put(oldCategory, newCategory);
 		
 		newCategory.setName(oldCategory.getCategoryName());
 		
 		for (Enumeration e2 = oldCategory.getCategoryNode().children(); e2.hasMoreElements();) {
 			net.sf.jmoney.model.CategoryNode subNode = (net.sf.jmoney.model.CategoryNode) e2.nextElement();
 			Object obj2 = subNode.getUserObject();
 			if (obj2 instanceof net.sf.jmoney.model.SimpleCategory) {
 				net.sf.jmoney.model.SimpleCategory oldSubCategory = (net.sf.jmoney.model.SimpleCategory) obj2;
 				IncomeExpenseAccount newSubCategory = newCategory.createSubAccount();
 				copyCategoryProperties(oldSubCategory, newSubCategory, accountMap);
 
 				accountMap.put(oldSubCategory, newSubCategory);
 			}
 		}
 	}
 	
 	private void copyEntryProperties(net.sf.jmoney.model.Entry oldEntry, Entry entry, ScalarPropertyAccessor<?> statusProperty) {
 		entry.setCheck(oldEntry.getCheck());
 		entry.setCreation(oldEntry.getCreation());
 		if (oldEntry.getCategory() instanceof net.sf.jmoney.model.Account) {
 			entry.setMemo(oldEntry.getMemo());
 		} else {
 			entry.setMemo(oldEntry.getDescription());
 		}
 		entry.setValuta(oldEntry.getValuta());
 		if (statusProperty != null && oldEntry.getStatus() != 0) {
 			entry.setPropertyValue((ScalarPropertyAccessor<Integer>)statusProperty, oldEntry.getStatus());
 		}
 	}
 
 }
