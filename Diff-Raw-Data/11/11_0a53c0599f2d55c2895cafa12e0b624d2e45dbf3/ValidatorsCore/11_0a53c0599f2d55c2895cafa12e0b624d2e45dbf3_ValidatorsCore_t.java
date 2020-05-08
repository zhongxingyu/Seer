 package org.eclipse.dltk.validators.internal.core;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
 import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
 import org.eclipse.dltk.validators.core.IValidator;
 import org.eclipse.dltk.validators.core.IValidatorChangedListener;
 import org.eclipse.dltk.validators.core.ValidatorRuntime;
 import org.osgi.framework.BundleContext;
 import org.w3c.dom.Document;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class ValidatorsCore extends Plugin implements IPropertyChangeListener {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.eclipse.dltk.validators.core";
 
 	// The shared instance
 	private static ValidatorsCore plugin;
 	
 	private boolean fIgnoreValidatorDefPropertyChangeEvents = false;
 
 	private boolean fBatchingChanges;
 	
 	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
 	
 	private String fOldInterpreterPrefString = EMPTY_STRING;
 
 	/**
 	 * The constructor
 	 */
 	public ValidatorsCore() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 		
 		getPluginPreferences().addPropertyChangeListener(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 * 
 	 * @return the shared instance
 	 */
 	public static ValidatorsCore getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns a Document that can be used to build a DOM tree
 	 * 
 	 * @return the Document
 	 * @throws ParserConfigurationException
 	 *             if an exception occurs creating the document builder
 	 */
 	public static Document getDocument() throws ParserConfigurationException {
 		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
 		Document doc = docBuilder.newDocument();
 		return doc;
 	}
 
 	public static String serializeDocument(Document doc) throws IOException,
 			TransformerException {
 		ByteArrayOutputStream s = new ByteArrayOutputStream();
 
 		TransformerFactory factory = TransformerFactory.newInstance();
 		Transformer transformer = factory.newTransformer();
 		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
 		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
 
 		DOMSource source = new DOMSource(doc);
 		StreamResult outputTarget = new StreamResult(s);
 		transformer.transform(source, outputTarget);
 
 		return s.toString("UTF8"); //$NON-NLS-1$			
 	}
 	public void setIgnoreValidatorDefPropertyChangeEvents(boolean ignore) {
 		fIgnoreValidatorDefPropertyChangeEvents = ignore;
 	}
 
 	public boolean isIgnoreValidatorDefPropertyChangeEvents() {
 		return fIgnoreValidatorDefPropertyChangeEvents;
 	}
 
 	public void propertyChange(PropertyChangeEvent event) {
 		String property = event.getProperty();
 		if (property.equals(ValidatorRuntime.PREF_VALIDATOR_XML)) {
 			if (!isIgnoreValidatorDefPropertyChangeEvents()) {
 				processValidatorPrefsChanged((String) event.getOldValue(),
 						(String) event.getNewValue());
 			}
 		}
 	}
 	private ValidatorDefinitionsContainer getValidatorDefinitions(String xml) {
 		if (xml.length() > 0) {
 			try {
 				ByteArrayInputStream stream = new ByteArrayInputStream(xml
 						.getBytes("UTF8")); //$NON-NLS-1$
 				return ValidatorDefinitionsContainer
 						.parseXMLIntoContainer(stream);
 			} catch (IOException e) {
				ValidatorsCore.getDefault().getLog().log(new Status( 0, ValidatorsCore.PLUGIN_ID, 0, "Exception", e ));
 			}
 		}
 		return new ValidatorDefinitionsContainer();
 	}
 	protected void processValidatorPrefsChanged(String oldValue,
 			String newValue) {
 
 		// batch changes
 		fBatchingChanges = true;
 		try {
 
 			String oldPrefString;
 			String newPrefString;
 
 			// If empty new value, save the old value and wait for 2nd
 			// propertyChange notification
 			if (newValue == null || newValue.equals(EMPTY_STRING)) {
 				fOldInterpreterPrefString = oldValue;
 				return;
 			}
 			// An empty old value signals the second notification in the import
 			// preferences
 			// sequence. Now that we have both old & new prefs, we can parse and
 			// compare them.
 			else if (oldValue == null || oldValue.equals(EMPTY_STRING)) {
 				oldPrefString = fOldInterpreterPrefString;
 				newPrefString = newValue;
 			}
 			// If both old & new values are present, this is a normal user
 			// change
 			else {
 				oldPrefString = oldValue;
 				newPrefString = newValue;
 			}
 
 			// Generate the previous Interpreters
 			ValidatorDefinitionsContainer oldResults = getValidatorDefinitions(oldPrefString);
 
 			// Generate the current
 			ValidatorDefinitionsContainer newResults = getValidatorDefinitions(newPrefString);
 
 			// Determine the deteled Interpreters
 			List deleted = oldResults.getValidatorList();
 			List current = newResults.getValidatorList();
 			deleted.removeAll(current);
 
 			// Dispose deleted Interpreters. The 'disposeInterpreterInstall'
 			// method fires notification of the
 			// deletion.
 			Iterator deletedIterator = deleted.iterator();
 			while (deletedIterator.hasNext()) {
 				IValidator deletedValidatorStandin = (IValidator) deletedIterator
 						.next();
 				deletedValidatorStandin.getValidatorType()
 						.disposeValidator(
 								deletedValidatorStandin.getID());
 			}
 		} finally {
 			// stop batch changes
 			fBatchingChanges = false;
 		}
 	}
 }
