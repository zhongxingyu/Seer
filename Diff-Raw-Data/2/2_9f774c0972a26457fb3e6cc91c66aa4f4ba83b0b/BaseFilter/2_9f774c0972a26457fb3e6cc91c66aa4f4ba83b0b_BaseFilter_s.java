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
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 import java.util.logging.Logger;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
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
  * BaseFilter provides a simplified API for filter writers and hides the powerful,
  * but complex resource and skeleton API's (
  * {@link net.sf.okapi.common.resource} and {@link net.sf.okapi.common.skeleton}
  * ).
  * 
  * To create a new filter extend BaseFilter and call
  * {@link net.sf.okapi.common.filters#initialize()} and
  * {@link net.sf.okapi.common.filters#finalize()} methods at the beginning and
  * end of each filter run.
  */
 public abstract class BaseFilter implements IFilter {
 	private static final Logger logger = Logger.getLogger("net.sf.okapi.common.filters.BaseFilter");
 	
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
 	public BaseFilter() {
 		// reset is called in initialize method - no need to call it in the
 		// constructor
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
 	 * Return the {@link IFilterWriter} that should be used in conjunction with this filter ({@link IFilter}).
 	 */
 	public IFilterWriter createFilterWriter () {
 		return new GenericFilterWriter(createSkeletonWriter());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.filters.IFilter#setOptions(java.lang.String,
 	 * java.lang.String, boolean)
 	 */
 	public void setOptions(String language, String defaultEncoding, boolean generateSkeleton) {
 		setOptions(language, null, defaultEncoding, generateSkeleton);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.filters.IFilter#setOptions(java.lang.String,
 	 * java.lang.String, java.lang.String, boolean)
 	 */
 	public void setOptions(String sourceLanguage, String targetLanguage, String defaultEncoding,
 			boolean generateSkeleton) {
 		setEncoding(defaultEncoding);
 		setSrcLang(sourceLanguage);
 	}
 
 	/*
 	 * Create a formatted ID for named resource.
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
 	 * Return the current buffered Event and remove it from the buffer.
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
 	protected String getEncoding() {
 		return encoding;
 	}
 
 	/**
 	 * Sets the input document encoding.
 	 * 
 	 * @param encoding
 	 *            the new encoding
 	 */
 	protected void setEncoding(String encoding) {
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
 	 * Initialize the filter and send the {@link StartDocument} {@link Event}
 	 */
 	protected void initialize() {
 		reset();
 		startDocument();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#finalize()
 	 */
 	@Override
 	protected void finalize() {
 		if (hasUnfinishedSkeleton()) {
 			endDocumentPart();
 		} else if (!tempFilterEventStack.isEmpty()) {
 			// go through filtered object stack and close them one by one
 			while (!tempFilterEventStack.isEmpty()) {
 				Event fe = tempFilterEventStack.peek();
 				if (fe.getEventType() == EventType.START_GROUP) {
 					StartGroup sg = (StartGroup)fe.getResource();
 					endGroup((GenericSkeleton)sg.getSkeleton());
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
 	 * @return true if TextUnit, false ootherwise. 
 	 */
 	protected boolean isCurrentTextUnit() {
 		Event e = peekTempEvent();
 		if (e != null && e.getEventType() == EventType.TEXT_UNIT) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Check if the current buffered {@link Event} is a complex {@link TextUnit}. 
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
 	 * Checks if is current group.
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
 	 * Checks if is inside text run.
 	 * 
 	 * @return true, if is inside text run
 	 */
 	protected boolean isInsideTextRun() {
 		return isCurrentTextUnit();
 	}
 
 	/**
 	 * Can start new text unit.
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
 	 * Checks for queued events.
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
 	 * Peek most recent group.
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
 	 * Peek most recent text unit.
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
 	 * Checks for unfinished skeleton.
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
 	 * Checks for parent text unit.
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
 
 	/**
 	 * Reset parser for new input.
 	 */
 	protected void reset() {
 		startGroupId = 0;
 		endGroupId = 0;
 		textUnitId = 0;
 		documentPartId = 0;
 		subDocumentId = 0;
 
 		canceled = false;
 		done = false;
		preserveWhitespace = true;
 		
 		newlineType = "\n";
 
 		referencableFilterEvents = new LinkedList<Event>();
 		filterEvents = new LinkedList<Event>();
 
 		tempFilterEventStack = new Stack<Event>();
 
 		currentCode = null;
 		currentSkeleton = null;
 		currentDocumentPart = null;
 	}
 
 	/**
 	 * Checks if is canceled.
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
 	 * Start document.
 	 */
 	protected void startDocument() {
 		StartDocument startDocument = new StartDocument(createId(START_DOCUMENT, ++documentId));
 		startDocument.setEncoding(getEncoding(), hasUtf8Encoding() && hasUtf8Bom());
 		startDocument.setLanguage(getSrcLang());
 		startDocument.setMimeType(getMimeType());
 		startDocument.setLineBreak(getNewlineType());
 		startDocument.setFilterParameters(getParameters());
 		Event event = new Event(EventType.START_DOCUMENT, startDocument);
 		filterEvents.add(event);
 	}
 
 	/**
 	 * End document.
 	 */
 	protected void endDocument() {
 		Ending endDocument = new Ending(createId(END_DOCUMENT, ++documentId));
 		Event event = new Event(EventType.END_DOCUMENT, endDocument);
 		filterEvents.add(event);
 	}
 
 	/**
 	 * Start sub document.
 	 */
 	protected void startSubDocument() {
 		if (hasUnfinishedSkeleton()) {
 			endDocumentPart();
 		}
 
 		StartSubDocument startSubDocument = new StartSubDocument(createId(START_SUBDOCUMENT, ++subDocumentId));
 		Event event = new Event(EventType.START_SUBDOCUMENT, startSubDocument);
 		filterEvents.add(event);
 	}
 
 	/**
 	 * End sub document.
 	 */
 	protected void endSubDocument() {
 		Ending endDocument = new Ending(createId(END_SUBDOCUMENT, ++subDocumentId));
 		Event event = new Event(EventType.END_SUBDOCUMENT, endDocument);
 		filterEvents.add(event);
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
 		resource = setPropertyBasedOnLanguage(resource, language, new Property(propOrText.getName(), propOrText
 				.getValue(), false));
 		currentSkeleton.add(tag.substring(propOrText.getMainStartPos(), propOrText.getValueStartPos()));
 		currentSkeleton.addValuePlaceholder(resource, propOrText.getName(), language);
 		currentSkeleton.add(tag.substring(propOrText.getValueEndPos(), propOrText.getMainEndPos()));
 	}
 
 	private void embeddedReadonlyProp(INameable resource, PropertyTextUnitPlaceholder propOrText, String tag,
 			String language) {
 		setPropertyBasedOnLanguage(resource, language, new Property(propOrText.getName(), propOrText
 				.getValue(), true));
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
 				throw new BaseFilterException("Unkown Property or TextUnit type");
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
 				// all text - the parent TU hold the references instead of a DocumentPart
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
 	 * Start text unit.
 	 * 
 	 * @param text
 	 *            the text
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
 	 * Start text unit.
 	 * 
 	 * @param startMarker
 	 *            the start marker
 	 */
 	protected void startTextUnit(GenericSkeleton startMarker) {
 		startTextUnit(null, startMarker, null, null);
 	}
 
 	/**
 	 * Start text unit.
 	 * 
 	 * @param startMarker
 	 *            the start marker
 	 * @param propertyTextUnitPlaceholders
 	 *            the property text unit placeholders
 	 */
 	protected void startTextUnit(GenericSkeleton startMarker,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		startTextUnit(null, startMarker, null, propertyTextUnitPlaceholders);
 	}
 
 	/**
 	 * Start text unit.
 	 * 
 	 * @param startMarker
 	 *            the start marker
 	 * @param propertyTextUnitPlaceholders
 	 *            the property text unit placeholders
 	 * @param text
 	 *            the text
 	 */
 	protected void startTextUnit(String text, GenericSkeleton startMarker,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		startTextUnit(text, startMarker, null, propertyTextUnitPlaceholders);
 	}
 
 	/**
 	 * Start text unit.
 	 * 
 	 * @param text
 	 *            the text
 	 * @param startMarker
 	 *            the start marker
 	 * @param propertyTextUnitPlaceholders
 	 *            the property text unit placeholders
 	 * @param language
 	 *            the language
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
 	 * End text unit.
 	 */
 	protected void endTextUnit() {
 		endTextUnit(null, null, null);
 	}
 
 	/**
 	 * End text unit.
 	 * 
 	 * @param endMarker
 	 *            the end marker
 	 */
 	protected void endTextUnit(GenericSkeleton endMarker) {
 		endTextUnit(endMarker, null, null);
 	}
 
 	/**
 	 * End text unit.
 	 * 
 	 * @param endMarker
 	 *            the end marker
 	 * @param propertyTextUnitPlaceholders
 	 *            the property text unit placeholders
 	 * @param language
 	 *            the language
 	 */
 	protected void endTextUnit(GenericSkeleton endMarker, String language,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		Event tempTextUnit;
 
 		if (!isCurrentTextUnit()) {
 			throw new BaseFilterException("TextUnit not found. Cannot end TextUnit");
 		}
 
 		// The ability to add internal placeholders to end tags is not currently
 		// support
 		// if (endMarker != null && propertyTextUnitPlaceholders != null) {
 		// processAllEmbedded(endMarker.toString(), language,
 		// propertyTextUnitPlaceholders, false);
 		// }
 
 		tempTextUnit = popTempEvent();
 
 		if (endMarker != null) {
 			GenericSkeleton skel = (GenericSkeleton) tempTextUnit.getResource().getSkeleton();
 			skel.add((GenericSkeleton) endMarker);
 		}
 
 		filterEvents.add(tempTextUnit);
 	}
 
 	/**
 	 * Adds the to text unit.
 	 * 
 	 * @param text
 	 *            the text
 	 */
 	protected void addToTextUnit(String text) {
 		if (!isCurrentTextUnit()) {
 			throw new BaseFilterException("TextUnit not found. Cannot add text");
 		}
 
 		Event tempTextUnit = peekTempEvent();
 		TextUnit tu = (TextUnit) tempTextUnit.getResource();
 		tu.getSource().append(text);
 	}
 
 	/**
 	 * Nothing is actionable within the tag (i.e., no properties or text)
 	 * 
 	 * @param codeType
 	 *            the code type
 	 * @param literalCode
 	 *            the literal code
 	 * @param codeName
 	 *            the code name
 	 */
 	protected void addToTextUnit(TextFragment.TagType codeType, String literalCode, String codeName) {
 		if (!isCurrentTextUnit()) {
 			throw new BaseFilterException("TextUnit not found. Cannot add code");
 		}
 
 		Code code = new Code(codeType, codeName, literalCode);
 		startCode(code);
 		endCode();
 	}
 
 	/**
 	 * Adds the to text unit.
 	 * 
 	 * @param codeType
 	 *            the code type
 	 * @param literalCode
 	 *            the literal code
 	 * @param codeName
 	 *            the code name
 	 * @param propertyTextUnitPlaceholders
 	 *            the property text unit placeholders
 	 */
 	protected void addToTextUnit(TextFragment.TagType codeType, String literalCode, String codeName,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		addToTextUnit(codeType, literalCode, codeName, null, propertyTextUnitPlaceholders);
 	}
 
 	/**
 	 * Adds the to text unit.
 	 * 
 	 * @param codeType
 	 *            the code type
 	 * @param literalCode
 	 *            the literal code
 	 * @param codeName
 	 *            the code name
 	 * @param language
 	 *            the language
 	 * @param propertyTextUnitPlaceholders
 	 *            the property text unit placeholders
 	 */
 	protected void addToTextUnit(TextFragment.TagType codeType, String literalCode, String codeName, String language,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 
 		if (!isCurrentTextUnit()) {
 			throw new BaseFilterException("TextUnit not found. Cannot add codes");
 		}
 
 		currentSkeleton = new GenericSkeleton();
 		TextUnit tu = (TextUnit) peekMostRecentTextUnit().getResource();
 		startCode(new Code(codeType, codeName));
 		processAllEmbedded(literalCode, language, propertyTextUnitPlaceholders, true, tu);
 		endCode();		
 
 		currentSkeleton = null;
 	}
 
 	// ////////////////////////////////////////////////////////////////////////
 	// Group Methods
 	// ////////////////////////////////////////////////////////////////////////
 
 	/**
 	 * Start group.
 	 * 
 	 * @param startMarker
 	 *            the start marker
 	 */
 	protected void startGroup(GenericSkeleton startMarker) {
 		startGroup(startMarker, null, null);
 	}
 
 	/**
 	 * Start group.
 	 * 
 	 * @param startMarker
 	 *            the start marker
 	 * @param propertyTextUnitPlaceholders
 	 *            the property text unit placeholders
 	 * @param language
 	 *            the language
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
 	 * End group.
 	 * 
 	 * @param endMarker
 	 *            the end marker
 	 */
 	protected void endGroup(GenericSkeleton endMarker) {
 		endGroup(endMarker, null, null);
 	}
 
 	/**
 	 * End group.
 	 * 
 	 * @param endMarker
 	 *            the end marker
 	 * @param propertyTextUnitPlaceholders
 	 *            the property text unit placeholders
 	 * @param language
 	 *            the language
 	 */
 	protected void endGroup(GenericSkeleton endMarker, String language,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 
 		if (!isCurrentGroup()) {
 			throw new BaseFilterException("Start group not found. Cannot end group");
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
 
 	private void startCode(Code code) {
 		if (!isCurrentTextUnit()) {
 			throw new BaseFilterException("TextUnit not found. Cannot add a Code to a non-exisitant TextUnit.");
 		}
 		currentCode = code;
 		currentCode.setType(currentTagType);
 	}
 
 	private void endCode() {
 		if (currentCode == null) {
 			throw new BaseFilterException("Code not found. Cannot end a non-exisitant code.");
 		}
 
 		TextUnit tu = (TextUnit) peekMostRecentTextUnit().getResource();
 		tu.getSourceContent().append(currentCode);
 		currentCode = null;
 	}
 
 	// ////////////////////////////////////////////////////////////////////////
 	// DocumentPart Methods
 	// ////////////////////////////////////////////////////////////////////////
 
 	/**
 	 * Start document part.
 	 * 
 	 * @param part
 	 *            the part
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
 	 * Start document part.
 	 * 
 	 * @param part
 	 *            the part
 	 * @param name
 	 *            the name
 	 * @param propertyTextUnitPlaceholders
 	 *            the property text unit placeholders
 	 */
 	protected void startDocumentPart(String part, String name,
 			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
 		startDocumentPart(part, name, null, propertyTextUnitPlaceholders);
 	}
 
 	/**
 	 * Start document part.
 	 * 
 	 * @param part
 	 *            the part
 	 * @param name
 	 *            the name
 	 * @param language
 	 *            the language
 	 * @param propertyTextUnitPlaceholders
 	 *            the property text unit placeholders
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
 	 * End document part.
 	 * 
 	 * @param part
 	 *            the part
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
 	 * End document part.
 	 */
 	protected void endDocumentPart() {
 		endDocumentPart(null);
 	}
 
 	/**
 	 * Adds the to document part.
 	 * 
 	 * @param part
 	 *            the part
 	 */
 	protected void addToDocumentPart(String part) {
 		if (currentSkeleton == null) {
 			startDocumentPart(part);
 			return;
 		}
 		currentSkeleton.append(part);
 	}
 
 	/**
 	 * @param preserveWhitespace the preserveWhitespace to set
 	 */
 	protected void setPreserveWhitespace(boolean preserveWhitespace) {
 		this.preserveWhitespace = preserveWhitespace;
 	}
 
 	/**
 	 * @return the preserveWhitespace
 	 */
 	protected boolean isPreserveWhitespace() {
 		return preserveWhitespace;
 	}
 
 	/**
 	 * @param currentElementType the currentElementType to set
 	 */
 	public void setTagType(String tagType) {
 		this.currentTagType = tagType;
 	}
 	
 	abstract protected boolean hasUtf8Encoding();
 	
 	abstract protected boolean hasUtf8Bom();
 	
 	protected String getNewlineType() {
 		return newlineType;
 	}
 
 	protected void setNewlineType(String newlineType) {
 		this.newlineType = newlineType;
 	}	
 }
