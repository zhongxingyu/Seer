 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common.filters;
 
 import java.security.acl.Group;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Stack;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.BOMNewlineEncodingDetector.NewlineType;
 import net.sf.okapi.common.exceptions.OkapiIllegalFilterOperationException;
 import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder.PlaceholderType;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.INameable;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.StartSubDocument;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 /**
  * BaseFilter provides a simplified API for filter writers and hides the low
  * level resource and skeleton API's ({@link net.sf.okapi.common.resource} and
  * {@link net.sf.okapi.common.skeleton}).
  * <p>
  * The BaseFilter allows filter writers to think in terms of simple start and
  * end calls. For example, to produce a non-translatable {@link Event} you would
  * use startDocumentPart() and endDocumentPart(). For a text-based {@link Event}
  * you would use startTextUnit() and endTextUnit().
  * <p>
  * More complex cases such as tags with embedded translatable text can also be
  * handled. See the BaseMarkupFilter, HtmlFilter and OpenXmlFilter for examples
  * of using the AbstractBaseFilter.
  * <p>
  * To create a new filter extend BaseFilter and call startFilter() and
  * endFilter() methods at the beginning and end of each filter run.
  */
 public abstract class AbstractBaseFilter implements IFilter {
 	private static final Logger LOGGER = Logger.getLogger(AbstractBaseFilter.class.getName());
 
 	private static final String START_GROUP = "sg"; //$NON-NLS-1$
 	private static final String END_GROUP = "eg"; //$NON-NLS-1$
 	private static final String TEXT_UNIT = "tu"; //$NON-NLS-1$
 	private static final String DOCUMENT_PART = "dp"; //$NON-NLS-1$
 	private static final String START_DOCUMENT = "sd"; //$NON-NLS-1$
 	private static final String END_DOCUMENT = "ed"; //$NON-NLS-1$
 	private static final String START_SUBDOCUMENT = "ssd"; //$NON-NLS-1$
 	private static final String END_SUBDOCUMENT = "esd"; //$NON-NLS-1$
 
 	private String encoding;
 	private String srcLang;
 	private String mimeType;
 	private String newlineType;
 	private String documentName;
 
 	private String currentTagType;
 
 	private int startGroupId = 0;
 	private int endGroupId = 0;
 	private int textUnitId = 0;
 	private int subDocumentId = 0;
 	private int documentId = 0;
 	private int documentPartId = 0;
 
 	private Stack<Event> tempFilterEventStack;
 
 	private List<Event> filterEvents;
 	private List<Event> referencableFilterEvents;
 
 	private boolean canceled = false;
 	private boolean done = false;
 	private boolean preserveWhitespace;
 
 	private GenericSkeleton currentSkeleton;
 	private Code currentCode;
 	private DocumentPart currentDocumentPart;
 
 	/**
 	 * Instantiates a new base filter.
 	 */
 	public AbstractBaseFilter() {
 		// newlineType should only be initialized once rather than on each reset
 		// call.
 		newlineType = "\n";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.filters.IFilter#hasNext()
 	 */
 	public boolean hasNext() {
 		return !done;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.filters.IFilter#next()
 	 */
 	public Event next() {
 		Event event;
 
 		if (hasNext()) {
 			if (!referencableFilterEvents.isEmpty()) {
 				return referencableFilterEvents.remove(0);
 			} else if (!filterEvents.isEmpty()) {
 				event = filterEvents.remove(0);
 				if (event.getEventType() == EventType.END_DOCUMENT)
 					done = true;
 				return event;
 			}
 		}
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.filters.IFilter#cancel()
 	 */
 	public void cancel() {
 		canceled = true;
 		// flush out all pending events
 		filterEvents.clear();
 		referencableFilterEvents.clear();
 
 		Event event = new Event(EventType.CANCELED);
 		filterEvents.add(event);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.filters.IFilter#createSkeletonWriter()
 	 */
 	public ISkeletonWriter createSkeletonWriter() {
 		return new GenericSkeletonWriter();
 	}
 
 	/**
 	 * Return the {@link IFilterWriter} that should be used in conjunction with
 	 * this filter ({@link IFilter}).
 	 */
 	public IFilterWriter createFilterWriter() {
 		return new GenericFilterWriter(createSkeletonWriter());
 	}
 
 	/**
 	 * Each {@link IFilter} has a small set of options beyond normal
 	 * configuration that gives the {@link IFilter} the needed information to
 	 * properly parse the content.
 	 * 
 	 * @param sourceLanguage
 	 *            - source language of the input document
 	 * @param targetLanguage
 	 *            - target language if the input document is multilingual.
 	 * @param defaultEncoding
 	 *            - assumed encoding of the input document. May be overriden if
 	 *            a different encoding is detected.
 	 * @param generateSkeleton
 	 *            - store skeleton (non-translatable parts of the document)
 	 *            along with the extracted text.
 	 */
 	protected void setOptions(String sourceLanguage, String targetLanguage, String defaultEncoding,
 			boolean generateSkeleton) {
 		setEncoding(defaultEncoding);
 		setSrcLang(sourceLanguage);
 	}
 
 	/*
 	 * Create a formatted ID for named resources.
 	 */
 	private String createId(String name, int number) {
 		return String.format("%s%d", name, number); //$NON-NLS-1$
 	}
 
 	/*
 	 * Return the current buffered Event without removing it.
 	 */
 	private Event peekTempEvent() {
 		if (tempFilterEventStack.isEmpty()) {
 			return null;
 		}
 		return tempFilterEventStack.peek();
 	}
 
 	/*
 	 * Return the current buffered Event and removes it from the buffer.
 	 */
 	private Event popTempEvent() {
 		if (tempFilterEventStack.isEmpty()) {
 			return null;
 		}
 		return tempFilterEventStack.pop();
 	}
 
 	/**
 	 * Gets the input document encoding.
 	 * 
 	 * @return the encoding
 	 */
 	public String getEncoding() {
 		return encoding;
 	}
 
 	/**
 	 * Sets the input document encoding.
 	 * 
 	 * @param encoding
 	 *            the new encoding
 	 */
 	public void setEncoding(String encoding) {
 		this.encoding = encoding;
 	}
 
 	/**
 	 * Gets the input document source language.
 	 * 
 	 * @return the src lang
 	 */
 	protected String getSrcLang() {
 		return srcLang;
 	}
 
 	/**
 	 * Sets the input document source language.
 	 * 
 	 * @param srcLang
 	 *            the new src lang
 	 */
 	protected void setSrcLang(String srcLang) {
 		this.srcLang = srcLang;
 	}
 
 	/**
 	 * Gets the input document mime type.
 	 * 
 	 * @return the mime type
 	 */
 	public String getMimeType() {
 		return mimeType;
 	}
 
 	/**
 	 * Sets the input document mime type.
 	 * 
 	 * @param mimeType
 	 *            the new mime type
 	 */
 	protected void setMimeType(String mimeType) {
 		this.mimeType = mimeType;
 	}
 
 	/**
 	 * Initialize the filter for every input and send the {@link StartDocument}
 	 * {@link Event}
 	 */
 	protected void startFilter() {
 		reset();
 		startDocument();
 	}
 
 	/**
 	 * End the current filter processing and send the {@link Ending}
 	 * {@link Event}
 	 */
 	protected void endFilter() {
 		if (hasUnfinishedSkeleton()) {
 			endDocumentPart();
 		} else if (!tempFilterEventStack.isEmpty()) {
 			// go through filtered object stack and close them one by one
 			while (!tempFilterEventStack.isEmpty()) {
 				Event fe = tempFilterEventStack.peek();
 				if (fe.getEventType() == EventType.START_GROUP) {
 					StartGroup sg = (StartGroup) fe.getResource();
 					endGroup((GenericSkeleton) sg.getSkeleton());
 				} else if (fe.getEventType() == EventType.TEXT_UNIT) {
 					endTextUnit();
 				}
 			}
 		}
 
 		endDocument();
 	}
 
 	/**
 	 * Check if the current buffered {@link Event} is a {@link TextUnit}.
 	 * 
 	 * @return true if TextUnit, false otherwise.
 	 */
 	protected boolean isCurrentTextUnit() {
 		Event e = peekTempEvent();
 		if (e != null && e.getEventType() == EventType.TEXT_UNIT) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Check if the current buffered {@link Event} is a complex {@link TextUnit}
 	 * A complex TextUnit is one which carries along with it it's surrounding
 	 * context such &lt;p> text &lt;/p> or &lt;title> text &lt;/title>
 	 * 
 	 * 
 	 * @return true, if complex text unit, false otherwise.
 	 */
 	protected boolean isCurrentComplexTextUnit() {
 		Event e = peekTempEvent();
 		if (e != null && e.getEventType() == EventType.TEXT_UNIT && e.getResource().getSkeleton() != null) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Check if the current buffered {@link Event} is a {@link Group}
 	 * 
 	 * @return true, if is current group
 	 */
 	protected boolean isCurrentGroup() {
 		Event e = peekTempEvent();
 		if (e != null && e.getEventType() == EventType.START_GROUP) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if the filter is inside text run.
 	 * 
 	 * @return true, if is inside text run
 	 */
 	protected boolean isInsideTextRun() {
 		return isCurrentTextUnit();
 	}
 
 	/**
 	 * Can we start new {@link TextUnit}? A new {@link TextUnit} can only be
 	 * started if the current one has been ended with endTextUnit. Or no
 	 * {@link TextUnit} has been created yet.
 	 * 
 	 * @return true, if successful
 	 */
 	protected boolean canStartNewTextUnit() {
 		if (isCurrentTextUnit()) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Checks for queued events. We queue events in the correct order as
 	 * expected by the Okapi Writers (IWriter).
 	 * 
 	 * @return true, if successful
 	 */
 	protected boolean hasQueuedEvents() {
 		if (filterEvents.isEmpty()) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Peek at the most recently created {@link Group}.
 	 * 
 	 * @return the filter event
 	 */
 	protected Event peekMostRecentGroup() {
 		if (tempFilterEventStack.isEmpty()) {
 			return null;
 		}
 		for (Event fe : tempFilterEventStack) {
 			if (fe.getEventType() == EventType.START_GROUP) {
 				return fe;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Peek At the most recently created {@link TextUnit}.
 	 * 
 	 * @return the filter event
 	 */
 	protected Event peekMostRecentTextUnit() {
 		if (tempFilterEventStack.isEmpty()) {
 			return null;
 		}
 		for (Event fe : tempFilterEventStack) {
 			if (fe.getEventType() == EventType.TEXT_UNIT) {
 				return fe;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Checks for unfinished skeleton aka {@link DocumentPart}
 	 * 
 	 * @return true, if successful
 	 */
 	protected boolean hasUnfinishedSkeleton() {
 		if (currentSkeleton == null) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Checks if the current {@link TextUnit} has a parent.
 	 * 
 	 * @return true, if successful
 	 */
 	protected boolean hasParentTextUnit() {
 		if (tempFilterEventStack.isEmpty()) {
 			return false;
 		}
 		boolean first = true;
 		// skip current TextUnit - the one we are currently processing
 		for (Event fe : tempFilterEventStack) {
 			if (fe.getEventType() == EventType.TEXT_UNIT && !first) {
 				return true;
 			}
 			first = false;
 		}
 		return false;
 	}
 
 	/*
 	 * Reset {@link IFilter} for new input.
 	 */
 	private void reset() {
 		startGroupId = 0;
 		endGroupId = 0;
 		textUnitId = 0;
 		documentPartId = 0;
 		subDocumentId = 0;		
 
 		canceled = false;
 		done = false;
 		preserveWhitespace = true;
 
 		referencableFilterEvents = new LinkedList<Event>();
 		filterEvents = new LinkedList<Event>();
 
 		tempFilterEventStack = new Stack<Event>();
 
 		currentCode = null;
 		currentSkeleton = null;
 		currentDocumentPart = null;
 	}
 
 	/**
 	 * Checks if the {@link IFilter} has been canceled.
 	 * 
 	 * @return true, if is canceled
 	 */
 	protected boolean isCanceled() {
 		return canceled;
 	}
 
 	// ////////////////////////////////////////////////////////////////////////
 	// Start and Finish Methods
 	// ////////////////////////////////////////////////////////////////////////
 
 	/**
 	 * Send the {@link EventType} START_DOCUMENT {@link Event}
 	 */
 	protected void startDocument() {
 		StartDocument startDocument = new StartDocument(createId(START_DOCUMENT, ++documentId));
 		startDocument.setEncoding(getEncoding(), hasUtf8Encoding() && hasUtf8Bom());
 		startDocument.setLanguage(getSrcLang());
 		startDocument.setMimeType(getMimeType());
 		startDocument.setLineBreak(getNewlineType());
 		startDocument.setFilterParameters(getParameters());
 		startDocument.setName(getDocumentName());
 		Event event = new Event(EventType.START_DOCUMENT, startDocument);
 		filterEvents.add(event);
 		LOGGER.log(Level.FINE, "Start Document for " + startDocument.getId());
 	}
 
 	/**
 	 * Send the {@link EventType} END_DOCUMENT {@link Event}
 	 */
 	protected void endDocument() {
 		Ending endDocument = new Ending(createId(END_DOCUMENT, ++documentId));
 		Event event = new Event(EventType.END_DOCUMENT, endDocument);
 		filterEvents.add(event);
 		LOGGER.log(Level.FINE, "End Document for " + endDocument.getId());
 	}
 
 	/**
 	 * Send the {@link EventType} START_SUBDOCUMENT {@link Event}
 	 */
 	protected void startSubDocument() {
 		if (hasUnfinishedSkeleton()) {
 			endDocumentPart();
 		}
 
 		StartSubDocument startSubDocument = new StartSubDocument(createId(START_SUBDOCUMENT, ++subDocumentId));
 		Event event = new Event(EventType.START_SUBDOCUMENT, startSubDocument);
 		filterEvents.add(event);
 		LOGGER.log(Level.FINE, "Start Sub-Document for " + startSubDocument.getId());
 	}
 
 	/**
 	 * Send the {@link EventType} END_SUBDOCUMENT {@link Event}
 	 */
 	protected void endSubDocument() {
 		Ending endDocument = new Ending(createId(END_SUBDOCUMENT, ++subDocumentId));
 		Event event = new Event(EventType.END_SUBDOCUMENT, endDocument);
 		filterEvents.add(event);
 		LOGGER.log(Level.FINE, "End Sub-Document for " + endDocument.getId());
 	}
 
 	// ////////////////////////////////////////////////////////////////////////
 	// Private methods used for processing properties and text embedded within
 	// tags
 	// ////////////////////////////////////////////////////////////////////////
 
 	private TextUnit embeddedTextUnit(PropertyTextUnitPlaceholder propOrText, String tag) {
 		TextUnit tu = new TextUnit(createId(TEXT_UNIT, ++textUnitId), propOrText.getValue());
 		tu.setPreserveWhitespaces(isPreserveWhitespace());
 
 		tu.setMimeType(propOrText.getMimeType());
 		tu.setIsReferent(true);
 
 		GenericSkeleton skel = new GenericSkeleton();
 
 		skel.add(tag.substring(propOrText.getMainStartPos(), propOrText.getValueStartPos()));
 		skel.addContentPlaceholder(tu);
 		skel.add(tag.substring(propOrText.getValueEndPos(), propOrText.getMainEndPos()));
 		tu.setSkeleton(skel);
 
 		return tu;
 	}
 
 	private void embeddedWritableProp(INameable resource, PropertyTextUnitPlaceholder propOrText, String tag,
 			String language) {
 		setPropertyBasedOnLanguage(resource, language, new Property(propOrText.getName(), propOrText.getValue(), false));
 		currentSkeleton.add(tag.substring(propOrText.getMainStartPos(), propOrText.getValueStartPos()));
 		currentSkeleton.addValuePlaceholder(resource, propOrText.getName(), language);
 		currentSkeleton.add(tag.substring(propOrText.getValueEndPos(), propOrText.getMainEndPos()));
 	}
 
 	private void embeddedReadonlyProp(INameable resource, PropertyTextUnitPlaceholder propOrText, String tag,
 			String language) {
 		setPropertyBasedOnLanguage(resource, language, new Property(propOrText.getName(), propOrText.getValue(), true));
 		currentSkeleton.add(tag.substring(propOrText.getMainStartPos(), propOrText.getMainEndPos()));
 	}
 
 	private INameable setPropertyBasedOnLanguage(INameable resource, String language, Property property) {
 		if (language == null) {
 			resource.setSourceProperty(property);
 		} else if (language.equals("")) {
 			resource.setProperty(property);
 		} else {
 			resource.setTargetProperty(language, property);
 		}
 
 		return resource;
 	}
 
 	private boolean processAllEmbedded(String tag, String language,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders, boolean inlineCode) {
 		return processAllEmbedded(tag, language, propertyTextUnitPlaceholders, inlineCode, null);
 	}
 
 	private boolean isTextPlaceHoldersOnly(List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		boolean text = false;
 		boolean nontext = false;
 		for (PropertyTextUnitPlaceholder propOrText : propertyTextUnitPlaceholders) {
 			if (propOrText.getType() == PlaceholderType.TRANSLATABLE) {
 				text = true;
 			} else {
 				nontext = true;
 			}
 		}
 
 		return (text && !nontext);
 
 	}
 
 	private boolean processAllEmbedded(String tag, String language,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders, boolean inlineCode, TextUnit parentTu) {
 
 		int propOrTextId = -1;
 		boolean textPlaceholdersOnly = isTextPlaceHoldersOnly(propertyTextUnitPlaceholders);
 		INameable resource = null;
 
 		// set the resource that will hold all the references
 		if (inlineCode) {
 			if (textPlaceholdersOnly) {
 				resource = parentTu;
 			} else {
 				resource = new DocumentPart(createId(DOCUMENT_PART, ++documentPartId), inlineCode);
 			}
 		} else {
 			if (parentTu != null) {
 				resource = parentTu;
 			} else {
 				resource = currentDocumentPart;
 			}
 		}
 
 		// sort to make sure we do the Properties or Text in order
 		Collections.sort(propertyTextUnitPlaceholders);
 
 		// add the part up to the first prop or text
 		PropertyTextUnitPlaceholder pt = propertyTextUnitPlaceholders.get(0);
 		currentSkeleton.add(tag.substring(0, pt.getMainStartPos()));
 
 		for (PropertyTextUnitPlaceholder propOrText : propertyTextUnitPlaceholders) {
 			propOrTextId++;
 
 			// add the markup between the props or text
 			if (propOrTextId >= 1 && propOrTextId < propertyTextUnitPlaceholders.size()) {
 				PropertyTextUnitPlaceholder pt1 = propertyTextUnitPlaceholders.get(propOrTextId - 1);
 				PropertyTextUnitPlaceholder pt2 = propertyTextUnitPlaceholders.get(propOrTextId);
 				currentSkeleton.add(tag.substring(pt1.getMainEndPos(), pt2.getMainStartPos()));
 			}
 
 			if (propOrText.getType() == PlaceholderType.TRANSLATABLE) {
 				TextUnit tu = embeddedTextUnit(propOrText, tag);
 				currentSkeleton.addReference(tu);
 				referencableFilterEvents.add(new Event(EventType.TEXT_UNIT, tu));
 			} else if (propOrText.getType() == PlaceholderType.WRITABLE_PROPERTY) {
 				embeddedWritableProp(resource, propOrText, tag, language);
 			} else if (propOrText.getType() == PlaceholderType.READ_ONLY_PROPERTY) {
 				embeddedReadonlyProp(resource, propOrText, tag, language);
 			} else {
 				throw new OkapiIllegalFilterOperationException("Unkown Property or TextUnit type");
 			}
 		}
 
 		// add the remaining markup after the last prop or text
 		pt = propertyTextUnitPlaceholders.get(propertyTextUnitPlaceholders.size() - 1);
 		currentSkeleton.add(tag.substring(pt.getMainEndPos()));
 
 		// setup references based on type
 		if (inlineCode) {
 			if (!textPlaceholdersOnly) {
 				currentCode.appendReference(resource.getId());
 				resource.setSkeleton(currentSkeleton);
 				// we needed to create a document part to hold the
 				// writable/localizables
 				referencableFilterEvents.add(new Event(EventType.DOCUMENT_PART, resource));
 			} else {
 				// all text - the parent TU hold the references instead of a
 				// DocumentPart
 				currentCode.append(currentSkeleton.toString());
 				currentCode.setReferenceFlag(true);
 			}
 		}
 
 		return textPlaceholdersOnly;
 	}
 
 	// ////////////////////////////////////////////////////////////////////////
 	// TextUnit Methods
 	// ////////////////////////////////////////////////////////////////////////
 
 	/**
 	 * Start a {@link TextUnit}. Also create a TextUnit {@link Event} and queue
 	 * it.
 	 * 
 	 * @param text
 	 *            the text used to prime the {@link TextUnit}
 	 */
 	protected void startTextUnit(String text) {
 		startTextUnit(text, null, null, null);
 	}
 
 	/**
 	 * Start text unit.
 	 */
 	protected void startTextUnit() {
 		startTextUnit(null, null, null, null);
 	}
 
 	/**
 	 * Start a complex {@link TextUnit}. Also create a TextUnit {@link Event}
 	 * and queue it.
 	 * 
 	 * @param startMarker
 	 *            the tag that begins the complex {@link TextUnit}
 	 */
 	protected void startTextUnit(GenericSkeleton startMarker) {
 		startTextUnit(null, startMarker, null, null);
 	}
 
 	/**
 	 * Start a complex {@link TextUnit} with actionable (translatable, writable
 	 * or read-only) attributes. Also create a TextUnit {@link Event} and queue
 	 * it.
 	 * 
 	 * @param startMarker
 	 *            the tag that begins the complex {@link TextUnit}
 	 * @param propertyTextUnitPlaceholders
 	 *            the list of actionable {@link TextUnit} or {@link Properties}
 	 *            with offset information into the tag.
 	 */
 	protected void startTextUnit(GenericSkeleton startMarker,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		startTextUnit(null, startMarker, null, propertyTextUnitPlaceholders);
 	}
 
 	/**
 	 * Start a complex {@link TextUnit} with actionable (translatable, writable
 	 * or read-only) attributes. Also create a TextUnit {@link Event} and queue
 	 * it.
 	 * 
 	 * @param startMarker
 	 *            the tag that begins the complex {@link TextUnit}
 	 * @param propertyTextUnitPlaceholders
 	 *            the list of actionable {@link TextUnit} or {@link Properties}
 	 *            with offset information into the tag.
 	 * @param text
 	 *            the text used to prime the {@link TextUnit}
 	 */
 	protected void startTextUnit(String text, GenericSkeleton startMarker,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		startTextUnit(text, startMarker, null, propertyTextUnitPlaceholders);
 	}
 
 	/**
 	 * Start a complex {@link TextUnit} with actionable (translatable, writable
 	 * or read-only) attributes. Also create a TextUnit {@link Event} and queue
 	 * it.
 	 * 
 	 * @param startMarker
 	 *            the tag that begins the complex {@link TextUnit}
 	 * @param propertyTextUnitPlaceholders
 	 *            the list of actionable {@link TextUnit} or {@link Properties}
 	 *            with offset information into the tag.
 	 * @param text
 	 *            the text used to prime the {@link TextUnit}
 	 * @param language
 	 *            the language of the text
 	 */
 	protected void startTextUnit(String text, GenericSkeleton startMarker, String language,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 
 		if (hasUnfinishedSkeleton()) {
 			endDocumentPart();
 		}
 
 		TextUnit tu = new TextUnit(createId(TEXT_UNIT, ++textUnitId), text);
 		tu.setMimeType(getMimeType());
 		tu.setPreserveWhitespaces(isPreserveWhitespace());
 
 		if (startMarker != null && propertyTextUnitPlaceholders != null) {
 			currentSkeleton = new GenericSkeleton();
 			processAllEmbedded(startMarker.toString(), language, propertyTextUnitPlaceholders, false, tu);
 			tu.setSkeleton(currentSkeleton);
 			currentSkeleton.addContentPlaceholder(tu);
 			tempFilterEventStack.push(new Event(EventType.TEXT_UNIT, tu, currentSkeleton));
 			currentSkeleton = null;
 			return;
 		} else if (startMarker != null) {
 			GenericSkeleton skel = new GenericSkeleton((GenericSkeleton) startMarker);
 			skel.addContentPlaceholder(tu);
 			tempFilterEventStack.push(new Event(EventType.TEXT_UNIT, tu, skel));
 			return;
 		} else {
 			tempFilterEventStack.push(new Event(EventType.TEXT_UNIT, tu));
 		}
 	}
 
 	/**
 	 * End the current {@link TextUnit} and place the {@link Event} on the
 	 * finished queue.
 	 */
 	protected void endTextUnit() {
 		endTextUnit(null, null, null);
 	}
 
 	/**
 	 * End the current {@link TextUnit} and place the {@link Event} on the
 	 * finished queue.
 	 * 
 	 * @param endMarker
 	 *            the tag that ends the complex {@link TextUnit}
 	 */
 	protected void endTextUnit(GenericSkeleton endMarker) {
 		endTextUnit(endMarker, null, null);
 	}
 
 	/**
 	 * End the current {@link TextUnit} and place the {@link Event} on the
 	 * finished queue.
 	 * 
 	 * @param endMarker
 	 *            the tag that ends the complex {@link TextUnit}
 	 * @param propertyTextUnitPlaceholders
 	 *            the list of actionable {@link TextUnit} or {@link Properties}
 	 *            with offset information into the tag.
 	 * @param language
 	 *            the language of the text
 	 * 
 	 * @throws OkapiIllegalFilterOperationException
 	 */
 	protected void endTextUnit(GenericSkeleton endMarker, String language,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		Event tempTextUnit;
 		// String sourceString; // for testing to see if there is embedded text
 
 		if (!isCurrentTextUnit()) {
 			OkapiIllegalFilterOperationException e = new OkapiIllegalFilterOperationException(
 					"Cannot end the TextUnit. TextUnit not found.");
 			LOGGER.log(Level.SEVERE, "Trying to end a TextUnit that does not exist.", e);
 			throw e;
 		}
 
 		/*
 		 * The ability to add internal placeholders to end tags is not currently
 		 * support if (endMarker != null && propertyTextUnitPlaceholders !=
 		 * null) { processAllEmbedded(endMarker.toString(), language,
 		 * propertyTextUnitPlaceholders, false); }
 		 */
 		tempTextUnit = popTempEvent();
 
 		/*
 		 * // Test if we actually have text of some type, if not convert this
 		 * Event // into a DocumrntPart TextUnit tu = (TextUnit)
 		 * tempTextUnit.getResource(); sourceString = tu.getSource().toString();
 		 * // for testing of embedded text if (!tu.getSource().hasText() &&
 		 * sourceString!=null && sourceString.indexOf("[#$")==-1) { // only do
 		 * this if there isn't embedded text String prefix =
 		 * tu.getSkeleton().toString(); // include anything in skeleton of TU
 		 * before self int ego = prefix.indexOf("[#$$self$]"); // ego points to
 		 * the self if (ego>0) { prefix = prefix.substring(0,ego); // delete
 		 * your "self", i.e. inside structure of text unit is no longer
 		 * necessary startDocumentPart(prefix); // create a document part with
 		 * just what was in the TU before self }
 		 * 
 		 * startDocumentPart(sourceString); // parameter was
 		 * tu.getSource().toString() if (endMarker != null) { endDocumentPart();
 		 * // end the document part corresponding to the internals of the text
 		 * unit startDocumentPart(endMarker.toString()); // create a document
 		 * part consisting only of the end marker } endDocumentPart(); return; }
 		 */
 
 		if (endMarker != null) {
 			GenericSkeleton skel = (GenericSkeleton) tempTextUnit.getResource().getSkeleton();
 			skel.add((GenericSkeleton) endMarker);
 		}
 
 		tempTextUnit = postProcessText(tempTextUnit);
 
 		filterEvents.add(tempTextUnit);
 	}
 
 	/**
 	 * Do any required post-processing on the TextUnit before the {@link Event}
 	 * leaves the {@link IFilter}. Default implementation leaves Event
 	 * unchanged. Override this method if you need to do format specific handing
 	 * such as collapsing whitespace.
 	 */
 	protected Event postProcessText(Event tempTextUnit) {
 		return tempTextUnit;
 	}
 
 	/**
 	 * Adds text to the current {@link TextUnit}
 	 * 
 	 * @param text
 	 *            the text
 	 * 
 	 * @throws OkapiIllegalFilterOperationException
 	 */
 	protected void addToTextUnit(String text) {
 		if (!isCurrentTextUnit()) {
 			OkapiIllegalFilterOperationException e = new OkapiIllegalFilterOperationException(
 					"Cannot add to the TextUnit. TextUnit not found.");
 			LOGGER.log(Level.SEVERE, "Trying to add text to a TextUnit that does not exist.", e);
 			throw e;
 		}
 
 		Event tempTextUnit = peekTempEvent();
 		TextUnit tu = (TextUnit) tempTextUnit.getResource();
 		tu.getSource().append(text);
 	}
 
 	/**
 	 * Add a {@link Code} to the current {@link TextUnit}. Nothing is actionable
 	 * within the tag (i.e., no properties or text)
 	 * 
 	 * @param codeType
 	 *            the code type
 	 * @param literalCode
 	 *            the literal code as it appears in the input.
 	 * @param codeName
 	 *            the code name
 	 * 
 	 * @throws OkapiIllegalFilterOperationException
 	 */
 	protected void addToTextUnit(TextFragment.TagType codeType, String literalCode, String codeName) {
 		if (!isCurrentTextUnit()) {
 			OkapiIllegalFilterOperationException e = new OkapiIllegalFilterOperationException(
 					"Cannot add a Code to the TextUnit. TextUnit not found.");
 			LOGGER.log(Level.SEVERE, "Trying to add a Code to a TextUnit that does not exist.", e);
 			throw e;
 		}
 
 		Code code = new Code(codeType, codeName, literalCode);
 		startCode(code);
 		endCode();
 	}
 
 	/**
 	 * Add a {@link Code} to the current {@link TextUnit}. The Code contains
 	 * actionable attributes.
 	 * 
 	 * @param codeType
 	 *            the code type
 	 * @param literalCode
 	 *            the literal code as it appears in the input.
 	 * @param codeName
 	 *            the code name
 	 * @param propertyTextUnitPlaceholders
 	 *            the list of actionable {@link TextUnit} or {@link Properties}
 	 *            with offset information into the tag.
 	 */
 	protected void addToTextUnit(TextFragment.TagType codeType, String literalCode, String codeName,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		addToTextUnit(codeType, literalCode, codeName, null, propertyTextUnitPlaceholders);
 	}
 
 	/**
 	 * Add a {@link Code} to the current {@link TextUnit}. The Code contains
 	 * actionable attributes.
 	 * 
 	 * @param codeType
 	 *            the code type
 	 * @param literalCode
 	 *            the literal code as it appears in the input.
 	 * @param codeName
 	 *            the code name
 	 * @param propertyTextUnitPlaceholders
 	 *            the list of actionable {@link TextUnit} or {@link Properties}
 	 *            with offset information into the tag.
 	 * @param language
 	 *            the language of the text
 	 * @throws OkapiIllegalFilterOperationException
 	 */
 	protected void addToTextUnit(TextFragment.TagType codeType, String literalCode, String codeName, String language,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 
 		if (!isCurrentTextUnit()) {
 			OkapiIllegalFilterOperationException e = new OkapiIllegalFilterOperationException(
 					"Cannot add Codes to the TextUnit. TextUnit not found.");
 			LOGGER.log(Level.SEVERE, "Trying to add Codes to a TextUnit that does not exist.", e);
 			throw e;
 		}
 
 		currentSkeleton = new GenericSkeleton();
 		TextUnit tu = (TextUnit) peekMostRecentTextUnit().getResource();
 		startCode(new Code(codeType, codeName));
 		processAllEmbedded(literalCode, language, propertyTextUnitPlaceholders, true, tu);
 		endCode();
 
 		currentSkeleton = null;
 	}
 
 	/**
 	 * Appends text to the first data part of the skeleton {@link TextUnit}
 	 * 
 	 * @param text
 	 *            the text
 	 * 
 	 * @throws OkapiIllegalFilterOperationException
 	 */
 	protected void appendToFirstSkeletonPart(String text) { // DWH 5-2-09
 		Event tempTextUnit = peekTempEvent();
 		GenericSkeleton skel = (GenericSkeleton) tempTextUnit.getResource().getSkeleton();
 		skel.appendToFirstPart(text);
 	}
 
 	// ////////////////////////////////////////////////////////////////////////
 	// Group Methods
 	// ////////////////////////////////////////////////////////////////////////
 
 	/**
 	 * Create and send a {@link StartGroup} {@link Event}
 	 * 
 	 * @param startMarker
 	 *            the tag which starts the {@link Group}
 	 */
 	protected void startGroup(GenericSkeleton startMarker) {
 		startGroup(startMarker, null, null);
 	}
 
 	/**
 	 * Create and send a {@link StartGroup} {@link Event}
 	 * 
 	 * @param startMarker
 	 *            the tag which starts the {@link Group}
 	 * @param propertyTextUnitPlaceholders
 	 *            the list of actionable {@link TextUnit} or {@link Properties}
 	 *            with offset information into the tag.
 	 * @param language
 	 *            the language of any actionable items
 	 */
 	protected void startGroup(GenericSkeleton startMarker, String language,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		if (hasUnfinishedSkeleton()) {
 			endDocumentPart();
 		}
 
 		if (startMarker != null && propertyTextUnitPlaceholders != null) {
 			processAllEmbedded(startMarker.toString(), language, propertyTextUnitPlaceholders, false);
 		}
 
 		String parentId = createId(START_SUBDOCUMENT, subDocumentId);
 		Event parentGroup = peekMostRecentGroup();
 		if (parentGroup != null) {
 			parentId = parentGroup.getResource().getId();
 		}
 
 		String gid = createId(START_GROUP, ++startGroupId);
 		StartGroup g = new StartGroup(parentId, gid);
 
 		GenericSkeleton skel = new GenericSkeleton((GenericSkeleton) startMarker);
 
 		Event fe = new Event(EventType.START_GROUP, g, skel);
 
 		if (isCurrentComplexTextUnit()) {
 			// add this group as a code of the complex TextUnit
 			g.setIsReferent(true);
 			Code c = new Code(TagType.PLACEHOLDER, startMarker.toString(), TextFragment.makeRefMarker(gid));
 			c.setReferenceFlag(true);
 			startCode(c);
 			endCode();
 			referencableFilterEvents.add(fe);
 		} else {
 			filterEvents.add(fe);
 		}
 
 		tempFilterEventStack.push(fe);
 	}
 
 	/**
 	 * Send an {@link Ending} {@link Event} of type END_GROUP
 	 * 
 	 * @param endMarker
 	 *            the tags that ends the {@link Group}
 	 */
 	protected void endGroup(GenericSkeleton endMarker) {
 		endGroup(endMarker, null, null);
 	}
 
 	/**
 	 * Send an {@link Ending} {@link Event} of type END_GROUP
 	 * 
 	 * @param endMarker
 	 *            the tags that ends the {@link Group}
 	 * @param propertyTextUnitPlaceholders
 	 *            the list of actionable {@link TextUnit} or {@link Properties}
 	 *            with offset information into the tag.
 	 * @param language
 	 *            the language of any actionable items
 	 * 
 	 * @throws OkapiIllegalFilterOperationException
 	 */
 	protected void endGroup(GenericSkeleton endMarker, String language,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 
 		if (!isCurrentGroup()) {
 			OkapiIllegalFilterOperationException e = new OkapiIllegalFilterOperationException(
 					"Cannot end current Group. StartGroup not found.");
 			LOGGER.log(Level.SEVERE, "Trying end a Group that does not exist. Can be cuased by unbalanced Group tags.",
 					e);
 			throw e;
 		}
 
 		GenericSkeleton skel = new GenericSkeleton((GenericSkeleton) endMarker);
 
 		if (endMarker != null && propertyTextUnitPlaceholders != null) {
 			processAllEmbedded(endMarker.toString(), language, propertyTextUnitPlaceholders, false);
 		}
 
 		popTempEvent();
 
 		Ending eg = new Ending(createId(END_GROUP, ++endGroupId));
 
 		filterEvents.add(new Event(EventType.END_GROUP, eg, skel));
 	}
 
 	// ////////////////////////////////////////////////////////////////////////
 	// Code Methods
 	// ////////////////////////////////////////////////////////////////////////
 
 	/*
 	 * Create a Code and store it for later processing.
 	 */
 	private void startCode(Code code) {
 		if (!isCurrentTextUnit()) {
 			OkapiIllegalFilterOperationException e = new OkapiIllegalFilterOperationException(
 					"Cannot add a Code to the TextUnit. TextUnit not found.");
 			LOGGER.log(Level.SEVERE, "Trying to add a Code to a TextUnit that does not exist.", e);
 			throw e;
 		}
 		currentCode = code;
 		currentCode.setType(currentTagType);
 	}
 
 	/*
 	 * End the COde and add it to the TextUnit.
 	 */
 	private void endCode() {
 		if (currentCode == null) {
 			OkapiIllegalFilterOperationException e = new OkapiIllegalFilterOperationException(
 					"Cannot end the current Code. Code not found.");
 			LOGGER.log(Level.SEVERE, "Trying to end a Code that does not exist. Did you call startCode?", e);
 			throw e;
 		}
 
 		TextUnit tu = (TextUnit) peekMostRecentTextUnit().getResource();
 		tu.getSourceContent().append(currentCode);
 		currentCode = null;
 	}
 
 	// ////////////////////////////////////////////////////////////////////////
 	// DocumentPart Methods
 	// ////////////////////////////////////////////////////////////////////////
 
 	/**
 	 * Start a {@link DocumentPart} and create an {@link Event}. Store the
 	 * {@link Event} for later processing.
 	 * 
 	 * @param part
 	 *            the {@link DocumentPart} (aka skeleton)
 	 */
 	protected void startDocumentPart(String part) {
 
 		if (hasUnfinishedSkeleton()) {
 			endDocumentPart();
 		}
 		currentSkeleton = new GenericSkeleton(part);
 		currentDocumentPart = new DocumentPart(createId(DOCUMENT_PART, ++documentPartId), false);
 		currentDocumentPart.setSkeleton(currentSkeleton);
 	}
 
 	/**
 	 * Start a {@link DocumentPart} and create an {@link Event}. Store the
 	 * {@link Event} for later processing.
 	 * 
 	 * @param part
 	 *            the {@link DocumentPart} (aka skeleton)
 	 * @param name
 	 *            the name
 	 * @param propertyTextUnitPlaceholders
 	 *            the list of actionable {@link TextUnit} or {@link Properties}
 	 *            with offset information into the tag.
 	 */
 	protected void startDocumentPart(String part, String name,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		startDocumentPart(part, name, null, propertyTextUnitPlaceholders);
 	}
 
 	/**
 	 * Start a {@link DocumentPart} and create an {@link Event}. Store the
 	 * {@link Event} for later processing.
 	 * 
 	 * @param part
 	 *            the {@link DocumentPart} (aka skeleton)
 	 * @param name
 	 *            the name
 	 * @param propertyTextUnitPlaceholders
 	 *            the list of actionable {@link TextUnit} or {@link Properties}
 	 *            with offset information into the tag.
 	 * @param language
 	 *            the language of any actionable items
 	 */
 	protected void startDocumentPart(String part, String name, String language,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 
 		if (hasUnfinishedSkeleton()) {
 			endDocumentPart();
 		}
 
 		currentSkeleton = new GenericSkeleton();
 		currentDocumentPart = new DocumentPart(createId(DOCUMENT_PART, ++documentPartId), false);
 		currentDocumentPart.setSkeleton(currentSkeleton);
 
 		processAllEmbedded(part, language, propertyTextUnitPlaceholders, false);
 	}
 
 	/**
 	 * End the {@link DocumentPart} and finalize the {@link Event}. Place the
 	 * {@link Event} on the finished queue.
 	 * 
 	 * @param part
 	 *            the {@link DocumentPart} (aka skeleton)
 	 */
 	protected void endDocumentPart(String part) {
 		if (part != null) {
 			currentSkeleton.append(part);
 		}
 		filterEvents.add(new Event(EventType.DOCUMENT_PART, currentDocumentPart));
 		currentSkeleton = null;
 		currentDocumentPart = null;
 	}
 
 	/**
 	 * End the {@link DocumentPart} and finalize the {@link Event}. Place the
 	 * {@link Event} on the finished queue.
 	 */
 	protected void endDocumentPart() {
 		endDocumentPart(null);
 	}
 
 	/**
 	 * Add to the current {@link DocumentPart}.
 	 * 
 	 * @param part
 	 *            the {@link DocumentPart} as a String.
 	 */
 	protected void addToDocumentPart(String part) {
 		if (currentSkeleton == null) {
 			startDocumentPart(part);
 			return;
 		}
 		currentSkeleton.append(part);
 	}
 
 	/**
 	 * Tell the {@link IFilter} what to do with whitespace.
 	 * 
 	 * @param preserveWhitespace
 	 *            the preserveWhitespace as boolean.
 	 */
 	protected void setPreserveWhitespace(boolean preserveWhitespace) {
 		this.preserveWhitespace = preserveWhitespace;
 	}
 
 	/**
 	 * 
 	 * @return the preserveWhitespace boolean.
 	 */
 	protected boolean isPreserveWhitespace() {
 		return preserveWhitespace;
 	}
 
 	/**
 	 * Set the tag type of the current tag (BOLD, UNDERLINED etc..).
 	 * 
 	 * @param tagType
 	 *            the tagType of the current tag.
 	 */
 	public void setTagType(String tagType) {
 		this.currentTagType = tagType;
 	}
 
 	/**
 	 * Set the tag type of the current tag (BOLD, UNDERLINED etc..).
 	 * 
 	 * @return the tagType of the current tag.
 	 */
 	public String getTagType() {
 		return currentTagType;
 	}
 
 	/**
 	 * Is the input encoded as UTF-8?
 	 * 
 	 * @return true if input is UTF-8, false otherwise.
 	 */
 	abstract protected boolean hasUtf8Encoding();
 
 	/**
 	 * Does the input have a UTF-8 Byte Order Mark?
 	 * 
 	 * @return true if thre is a UTF-8 BOM, false otherwise.
 	 */
 	abstract protected boolean hasUtf8Bom();
 
 	/**
 	 * Get the newline type used in the input.
 	 * 
 	 * @return the {@link NewlineType} one of '\n', '\r' or '\r\n'
 	 */
 	protected String getNewlineType() {
 		return newlineType;
 	}
 
 	/**
 	 * Set the newline type.
 	 * 
 	 * @param newlineType
 	 *            one of '\n', '\r' or '\r\n'.
 	 */
 	protected void setNewlineType(String newlineType) {
 		this.newlineType = newlineType;
 	}
 
 	/**
 	 * Allows implementers to set the START_DOCUMENT name for the current input.
 	 * 
	 * @param name
	 *            - set the input document name or path
 	 */
 	protected void setDocumentName(String documentName) {
 		this.documentName = documentName;
 	}
 
 	/**
	 * Get the START_DOCUMENT name for the current input.
	 * 
	 * @param name
	 *            - set the input document name or path
 	 */
 	protected String getDocumentName() {
 		return documentName;
 	}
 }
