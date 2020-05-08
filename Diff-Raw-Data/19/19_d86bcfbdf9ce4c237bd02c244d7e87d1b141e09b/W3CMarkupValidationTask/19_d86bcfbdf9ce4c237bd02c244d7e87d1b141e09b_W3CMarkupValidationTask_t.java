 /*
  * #%L
  * Ant Validation Toolkit
  * %%
  * Copyright (C) 2013 Christoph Läubrich
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 package de.laeubisoft.tools.ant.validation;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethodBase;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import org.ccil.cowan.tagsoup.Parser;
 import org.w3.markup.validator.Culprit;
 import org.w3.markup.validator.Debug;
 import org.w3.markup.validator.MarkupValidationResponse;
 import org.w3.markup.validator.ObjectFactory;
 import org.w3.markup.validator.ValidationErrors;
 import org.w3.markup.validator.ValidationWarnings;
 import org.w3.markup.validator.Warning;
 import org.w3.soap.envelope.Envelope;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * Provides a task for automatic checking of HTML documents
  * 
  * @author Christoph Läubrich
  */
 public class W3CMarkupValidationTask extends Task {
 
     /**
      * The URL of the public online validator
      */
     private static final String W3_ORG_VALIDATOR        = "http://validator.w3.org/check";
 
     /**
      * We want soap !
      */
     private static final String VALIDATOR_FORMAT_OUTPUT = "soap12";
 
     /**
      * The URL of the document to validate,either this parameter, or
      * uploaded_file, or fragment must be given.
      */
     private URL                 uri;
     /**
      * The document to validate, either this parameter, or uri, or fragment must
      * be given.
      */
     private File                uploaded_file;
     /**
      * The source of the document to validate. Full documents only, either this
      * parameter, or uri, or uploaded_file must be given.
      */
     private String              fragment;
 
     /**
      * URL of the validator server to use
      */
     private String              validator               = W3_ORG_VALIDATOR;
 
     /**
      * Character encoding override: Specify the character encoding to use when
      * parsing the document. When used with the auxiliary parameter fbc set to
      * 1, the given encoding will only be used as a fallback value, in case the
      * charset is absent or unrecognized. Note that this parameter is ignored if
      * validating a fragment with the direct input interface, by default the
      * validator detects the charset of the document automatically.
      */
     private String              charset;
     /**
      * Document Type override: Specify the Document Type (DOCTYPE) to use when
      * parsing the document. When used with the auxiliary parameter fbd set to
      * 1, the given document type will only be used as a fallback value, in case
      * the document's DOCTYPE declaration is missing or unrecognized,by default
      * the validator detects the document type of the document automatically.
      */
     private String              doctype;
     /**
      * When set to 1, will output some extra debugging information on the
      * validated resource (such as HTTP headers) and validation process (such as
      * parser used, parse mode etc.). In the SOAP output, this information will
      * be given in <m:debug> elements.
      */
     private boolean             debug;
 
     /**
      * Try to recurse into links
      */
     private boolean             recurse;
     /**
      * Should the build fail on error
      */
     private boolean             fail                    = true;
 
     /**
      * The Pattern used to format error response, see
      * http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html#syntax
      * for syntax
      */
     private String              errorPattern            = "[ERROR] [%7$s] Line %1$s, Column %2$s: %3$s (ID %4$s) source = '%5$s', %6$s";
 
     /**
      * The Pattern used to format warning response, see
      * http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html#syntax
      * for syntax
      */
     private String              warningPattern          = "[WARNING] [%7$s] Line %1$s, Column %2$s: %3$s (ID %4$s) source = '%5$s', %6$s";
 
     /**
      * The Pattern used to format debug response, see
      * http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html#syntax
      * for syntax
      */
     private String              debugPattern            = "[DEBUG] [%1$s] %2$s: %3$s";
 
     /**
      * The List of pattern to ignore
      */
     private final List<Pattern> ignorePatternList       = new ArrayList<Pattern>();
 
     /**
      * Add a (configured) pattern to the ignore list
      * 
      * @param ignorePattern
      */
     public void addConfiguredIgnore(IgnorePattern ignorePattern) {
         ignorePatternList.add(ignorePattern.toPattern());
        log("Pattern added " + ignorePattern, Project.MSG_INFO);
     }
 
     /**
      * @param uri
      *            the new value for uri
      */
     public void setUri(URL uri) {
         this.uri = uri;
     }
 
     /**
      * @param warningPattern
      *            the new value for warningPattern
      */
     public void setWarningPattern(String warningPattern) {
         this.warningPattern = warningPattern;
     }
 
     /**
      * @param debugPattern
      *            the new value for debugPattern
      */
     public void setDebugPattern(String debugPattern) {
         this.debugPattern = debugPattern;
     }
 
     /**
      * @param validator
      *            the new value for validator
      */
     public void setValidator(String validator) {
         this.validator = validator;
     }
 
     /**
      * @param errorPattern
      *            the new value for errorPattern
      */
     public void setErrorPattern(String errorPattern) {
         this.errorPattern = errorPattern;
     }
 
     /**
      * Set this to <code>true</code> if you want to fail the build on validation
      * errors
      * 
      * @param fail
      *            the new value for fail
      */
     public void setFail(boolean fail) {
         this.fail = fail;
     }
 
     /**
      * @param file
      *            the new value for file
      */
     public void setFile(File file) {
         this.uploaded_file = file;
     }
 
     /**
      * @param recurse
      *            the new value for recurse
      */
     public void setRecurse(boolean recurse) {
         this.recurse = recurse;
     }
 
     /**
      * @param fragment
      *            the new value for fragment
      */
     public void setFragment(String fragment) {
         this.fragment = fragment;
     }
 
     /**
      * @param doctype
      *            the new value for doctype
      */
     public void setDoctype(String doctype) {
         this.doctype = doctype;
     }
 
     /**
      * @param charset
      *            the new value for charset
      */
     public void setCharset(String charset) {
         this.charset = charset;
     }
 
     /**
      * @param debug
      *            the new value for debug
      */
     public void setDebug(boolean debug) {
         this.debug = debug;
     }
 
     @Override
     public void execute() throws BuildException {
         validateParameter();
         List<URL> urlsToCheck = new ArrayList<URL>();
         urlsToCheck.add(uri);
         HashSet<String> checkedURIs = new HashSet<String>();
         while (!urlsToCheck.isEmpty()) {
             URL url = urlsToCheck.remove(0);
             if (url != null) {
                 String uriString = url.toString();
                 if (checkedURIs.contains(uriString)) {
                     continue;
                 }
                 checkedURIs.add(uriString);
             }
             //Check the URI (might be null if fragment or file was given...)
             if (checkURI(url)) {
                 //If we should recurse, parse the URL and determine all links
                 if (recurse) {
                     Set<URL> recurseInto = recurseInto(url);
                    urlloop: for (URL newUrl : recurseInto) {
                         String string = newUrl.toString();
                         if (checkedURIs.contains(string)) {
                             continue;
                         }
                         for (Pattern pattern : ignorePatternList) {
                             if (pattern.matcher(string).matches()) {
                                log("pattern " + pattern + " matches " + string + ", URL will be ignored", Project.MSG_INFO);
                                checkedURIs.add(string);
                                continue urlloop;
                             }
                         }
                         urlsToCheck.add(newUrl);
                     }
                 }
             }
         }
     }
 
     /**
      * Validates the parameter and throws exception if something is invalid
      * 
      * @throws BuildException
      */
     private void validateParameter() throws BuildException {
         int notNullSource = 0;
         if (uri != null) {
             notNullSource++;
         }
         if (fragment != null) {
             notNullSource++;
             if (recurse) {
                 throw new BuildException("the recurse option can only be used with uri attribute, but fragment was given");
             }
         }
         if (uploaded_file != null) {
             notNullSource++;
             if (recurse) {
                 throw new BuildException("the recurse option can only be used with uri attribute, but file was given");
             }
         }
         if (notNullSource == 0) {
             throw new BuildException("at least one of 'uri', 'fragment' or 'file' must be given!");
         }
         if (notNullSource > 1) {
             throw new BuildException("Only one of 'uri', 'fragment' or 'file' can be given!");
         }
     }
 
     /**
      * Send the given URL to the validator and check the result
      * 
      * @param uriToCheck
      *            the {@link URL} to check
      * @return <code>true</code> if URL was checked, <code>false</code> if this
      *         URL can't be checked because it is of wrong type
      * @throws BuildException
      */
     protected boolean checkURI(final URL uriToCheck) throws BuildException {
         try {
             InputStream connection = buildConnection(uriToCheck);
             Unmarshaller unmarshaller = JAXBContext.newInstance(Envelope.class, ObjectFactory.class).createUnmarshaller();
             Object object = Tools.getObject(unmarshaller.unmarshal(connection));
             if (W3_ORG_VALIDATOR.equals(validator)) {
                 //The W3C recommends to at least wait one second between automatic requests to their public service...
                 //So we sleep here for one second to comply with this
                 try {
                     TimeUnit.SECONDS.sleep(1);
                 } catch (InterruptedException e) {
                     //We don't care then...
                     return false;
                 }
             }
             if (object instanceof Envelope) {
                 Envelope envelope = (Envelope) object;
                 for (Object bodyObject : envelope.getBody().getAny()) {
                     bodyObject = Tools.getObject(bodyObject);
                     if (bodyObject instanceof MarkupValidationResponse) {
                         MarkupValidationResponse markupvalidationresponse = (MarkupValidationResponse) bodyObject;
                         handleResponse(markupvalidationresponse);
                         return true;
                     } else {
                         log("URL " + uriToCheck + " is ignored, it seem not to specify a valid document (e.g. link to binary file)", Project.MSG_DEBUG);
                         continue;
                     }
                 }
                 return false;
             }
             throw new BuildException("Invalid server response for URI: " + uriToCheck + " (was: " + object + ")");
         } catch (MalformedURLException e) {
             throw new BuildException("Bad URL for validation server", e);
         } catch (JAXBException e) {
             throw new BuildException("XML parser setup problem", e);
         } catch (IOException e) {
             throw new BuildException("Problem while communicating with server", e);
         }
     }
 
     /**
      * Creates the actual request to the validation server for a given
      * {@link URL} and returns an inputstream the result can be read from
      * 
      * @param uriToCheck
      *            the URL to check
      * @return the stream to read the response from
      * @throws IOException
      *             if unrecoverable communication error occurs
      * @throws BuildException
      *             if server returned unexspected results
      */
     private InputStream buildConnection(final URL uriToCheck) throws IOException, BuildException {
         List<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new NameValuePair("output", VALIDATOR_FORMAT_OUTPUT));
         if (uriToCheck != null) {
             params.add(new NameValuePair("uri", uriToCheck.toString()));
         } else {
             if (fragment != null) {
                 params.add(new NameValuePair("fragment", fragment));
             }
         }
         if (debug) {
             params.add(new NameValuePair("debug", "1"));
         }
         if (charset != null) {
             params.add(new NameValuePair("charset", charset));
         }
         if (doctype != null) {
             params.add(new NameValuePair("doctype", doctype));
         }
         HttpClient httpClient = new HttpClient();
         HttpMethodBase method;
         if (uriToCheck != null) {
             //URIs must be checked wia traditonal GET...
             GetMethod getMethod = new GetMethod(validator);
             getMethod.setQueryString(params.toArray(new NameValuePair[0]));
             method = getMethod;
         } else {
             PostMethod postMethod = new PostMethod(validator);
             if (fragment != null) {
                 //Fragment request can be checked via FORM Submission
                 postMethod.addParameters(params.toArray(new NameValuePair[0]));
             } else {
                 //Finally files must be checked with multipart-forms....
                 postMethod.setRequestEntity(Tools.createFileUpload(uploaded_file, "uploaded_file", charset, params, postMethod.getParams()));
             }
             method = postMethod;
         }
         int result = httpClient.executeMethod(method);
         if (result == HttpStatus.SC_OK) {
             return method.getResponseBodyAsStream();
         } else {
             throw new BuildException("Server returned " + result + " " + method.getStatusText());
         }
 
     }
 
     /**
      * Takes an {@link URL} and tries to find out all linked resources
      * 
      * @param uriToRecurse
      * @return a set of discovered urls
      */
     private Set<URL> recurseInto(final URL uriToRecurse) throws BuildException {
         final Set<URL> urlsFound = new HashSet<URL>();
         XMLReader reader = new Parser();
         reader.setContentHandler(new DefaultHandler() {
             @Override
             public void startElement(String nsuri, String localName, String qName, Attributes attributes) throws SAXException {
                 if ("a".equalsIgnoreCase(qName)) {
                     String value = attributes.getValue("href");
                     if (value != null) {
                         try {
                             URL url = new URL(uriToRecurse, value);
                             if (url.getHost().equalsIgnoreCase(uriToRecurse.getHost()) && url.getPort() == uriToRecurse.getPort()) {
                                 urlsFound.add(url);
                             }
                         } catch (MalformedURLException e) {
                             log("can't parse URL for href = " + value + ", it will be ignored!", Project.MSG_ERR);
                         }
                     }
                 }
             }
         });
         // Parsen wird gestartet
         try {
             reader.parse(new InputSource(uriToRecurse.openStream()));
             return urlsFound;
         } catch (IOException e) {
             throw new BuildException("error while accessing data at " + uriToRecurse, e);
         } catch (SAXException e) {
             throw new BuildException("error while parsing data at " + uriToRecurse, e);
         }
     }
 
     /**
      * Handle the response by printing out the relevant parts of the response to
      * the appropiate levels, and fails if {@link #fail} is set and validation
      * was not successfull
      * 
      * @param markupvalidationresponse
      */
     private void handleResponse(MarkupValidationResponse response) {
         log("URI:        " + response.getUri());
         log("Doctype:    " + response.getDoctype());
         log("Charset:    " + response.getCharset());
         log("is valid:   " + response.isValidity());
         List<Debug> debugList = response.getDebug();
         for (Debug debug : debugList) {
             log(String.format(debugPattern, response.getUri(), debug.getName(), debug.getValue()), Project.MSG_WARN);
         }
         ValidationErrors errors = response.getErrors();
         if (errors != null) {
             for (org.w3.markup.validator.Error error : errors.getErrorlist().getError()) {
                 logMessage(errorPattern, response, error, Project.MSG_ERR);
             }
         }
         ValidationWarnings warnings = response.getWarnings();
         if (warnings != null) {
             for (Warning warning : warnings.getWarninglist().getWarning()) {
                 logMessage(warningPattern, response, warning, Project.MSG_WARN);
             }
         }
         if (!response.isValidity() && fail) {
             throw new BuildException("Document at " + response.getUri() + " is invalid (" + response.getErrors().getErrorcount() + " errors)");
         }
     }
 
     private void logMessage(String errorPattern, MarkupValidationResponse response, Culprit culprit, int level) {
         log(String.format(errorPattern, culprit.getLine(), culprit.getCol(), culprit.getMessage(), culprit.getMessageid(), culprit.getSource(), culprit.getExplanation(), response.getUri()), level);
     }
 
 }
