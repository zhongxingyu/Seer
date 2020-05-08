 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
  * 
  * Portions completed before September 1, 2008
  * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
  *
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
  *
  **********************************************************************************/
 
 package org.etudes.ambrosia.impl;
 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.etudes.ambrosia.api.Component;
 import org.etudes.ambrosia.api.Container;
 import org.etudes.ambrosia.api.Context;
 import org.etudes.ambrosia.api.Decision;
 import org.etudes.ambrosia.api.Destination;
 import org.etudes.ambrosia.api.Message;
 import org.etudes.ambrosia.api.PropertyReference;
 import org.etudes.ambrosia.api.RenderListener;
 import org.etudes.ambrosia.api.Selection;
 import org.sakaiproject.util.StringUtil;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * UiSelection presents a selection for the user to choose or not.<br />
  * The text can be either a property reference or a message.
  */
 public class UiSelection extends UiComponent implements Selection
 {
 	protected class ContainerRef
 	{
 		Container container;
 
 		boolean reversed = false;
 
 		boolean separate = false;
 
 		ContainerRef(Container c, boolean separate, boolean reversed)
 		{
 			this.container = c;
 			this.separate = separate;
 			this.reversed = reversed;
 		}
 	}
 
 	/** Decision for including the correct markers. */
 	protected Decision correctDecision = null;
 
 	/** Icon to use to show correct. */
 	protected String correctIcon = "!/ambrosia_library/icons/correct.png";
 
 	/** The correct message. */
 	protected Message correctMessage = new UiMessage().setMessage("correct");
 
 	/** A model reference to a value that is considered "correct" for correct/incorrect marking. */
 	protected PropertyReference correctReference = null;
 
 	/** Dropdown # lines to display. */
 	protected int height = 1;
 
 	/** Icon to use to show incorrect. */
 	protected String incorrectIcon = "!/ambrosia_library/icons/incorrect.png";
 
 	/** The incorrect message. */
 	protected Message incorrectMessage = new UiMessage().setMessage("incorrect");
 
 	/** The context name for the current iteration object when using selectionReference. */
 	protected String iteratorName = null;
 
 	/** The value we use if the user does not selecet the selection. */
 	protected String notSelectedValue = "false";
 
 	/** The decision to control the onEmptyAlert. */
 	protected Decision onEmptyAlertDecision = null;
 
 	/** The message for the onEmptyAlert. */
 	protected Message onEmptyAlertMsg = null;
 
 	/** The orientation for multiple selection choices. */
 	protected Orientation orientation = Orientation.vertical;
 
 	/**
 	 * The PropertyReference for encoding and decoding this selection - this is what will be updated with the end-user's selection choice, and what value seeds the display.
 	 */
 	protected PropertyReference propertyReference = null;
 
 	/** The read only decision. */
 	protected Decision readOnly = null;
 
 	/** The read only and show only the selected item (collapsed) decision. */
 	protected Decision readOnlyCollapsed = null;
 
 	/** If we should include select-all or not. */
 	protected boolean selectAll = true;
 
 	/** The value we find if the user selects the selection. */
 	protected String selectedValue = "true";
 
 	/** Containers holding dependent components to a selection. */
 	protected List<ContainerRef> selectionContainers = new ArrayList<ContainerRef>();
 
 	/** The message that pulls out the display text for each selection. */
 	protected Message selectionDisplayMessage = null;
 
 	/** The set of messages for multiple selection choices. */
 	protected List<Message> selectionMessages = new ArrayList<Message>();
 
 	/** The ref to a Collection or [] in the model that will populate the selection. */
 	protected PropertyReference selectionReference = null;
 
 	/** The message that pulls out the value for each selection. */
 	protected Message selectionValueMessage = null;
 
 	/** The set of values for multiple selection choices. */
 	protected List<Message> selectionValues = new ArrayList<Message>();
 
 	/** If set, use this instead of sigleSelect to see if we are going to be single or multiple select. */
 	protected Decision singleSelectDecision = null;
 
 	/** The destination to submit if we are submitting on change. */
 	protected Destination submitDestination = null;
 
 	/** if set, submit on change and use the value selected as the destination. */
 	protected boolean submitValue = false;
 
 	/** The message that will provide title text. */
 	protected Message titleMessage = null;
 
 	/**
 	 * No-arg constructor.
 	 */
 	public UiSelection()
 	{
 	}
 
 	/**
 	 * Construct from a dom element.
 	 * 
 	 * @param service
 	 *        the UiService.
 	 * @param xml
 	 *        The dom element.
 	 */
 	protected UiSelection(UiServiceImpl service, Element xml)
 	{
 		// component stuff
 		super(service, xml);
 
 		// short form for title - attribute "title" as the selector
 		String title = StringUtil.trimToNull(xml.getAttribute("title"));
 		if (title != null)
 		{
 			setTitle(title);
 		}
 
 		// short for model
 		String model = StringUtil.trimToNull(xml.getAttribute("model"));
 		if (model != null)
 		{
 			PropertyReference pRef = service.newPropertyReference().setReference(model);
 			setProperty(pRef);
 		}
 
 		// short for correct
 		String correct = StringUtil.trimToNull(xml.getAttribute("correct"));
 		if (model != null)
 		{
 			PropertyReference pRef = service.newPropertyReference().setReference(correct);
 			setCorrect(pRef);
 		}
 
 		// short form for destination - attribute "destination" as the destination
 		String destination = StringUtil.trimToNull(xml.getAttribute("destination"));
 		if (destination != null)
 		{
 			setDestination(service.newDestination().setDestination(destination));
 		}
 
 		// selected value
 		String value = StringUtil.trimToNull(xml.getAttribute("value"));
 		if (value != null) this.selectedValue = value;
 
 		// select all
 		String selectAll = StringUtil.trimToNull(xml.getAttribute("selectAll"));
 		if ((selectAll != null) && ("FALSE".equals(selectAll))) setSelectAll(false);
 
 		// orientation
 		String orientation = StringUtil.trimToNull(xml.getAttribute("orientation"));
 		if (orientation != null)
 		{
 			if (orientation.equals("HORIZONTAL"))
 			{
 				setOrientation(Orientation.horizontal);
 			}
 			else if (orientation.equals("DROPDOWN"))
 			{
 				setOrientation(Orientation.dropdown);
 			}
 		}
 
 		// title
 		Element settingsXml = XmlHelper.getChildElementNamed(xml, "title");
 		if (settingsXml != null)
 		{
 			// let Message parse this
 			this.titleMessage = new UiMessage(service, settingsXml);
 		}
 
 		// model
 		settingsXml = XmlHelper.getChildElementNamed(xml, "model");
 		if (settingsXml != null)
 		{
 			PropertyReference pRef = service.parsePropertyReference(settingsXml);
 			if (pRef != null) setProperty(pRef);
 		}
 
 		// correct
 		settingsXml = XmlHelper.getChildElementNamed(xml, "correct");
 		if (settingsXml != null)
 		{
 			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
 			if (innerXml != null)
 			{
 				PropertyReference pRef = service.parsePropertyReference(innerXml);
 				if (pRef != null) setCorrect(pRef);
 			}
 		}
 
 		// correct decision
 		settingsXml = XmlHelper.getChildElementNamed(xml, "correctDecision");
 		if (settingsXml != null)
 		{
 			setCorrectDecision(service.parseDecisions(settingsXml));
 		}
 
 		// selection choices
 		settingsXml = XmlHelper.getChildElementNamed(xml, "selectionChoices");
 		if (settingsXml != null)
 		{
 			NodeList contained = settingsXml.getChildNodes();
 			for (int i = 0; i < contained.getLength(); i++)
 			{
 				Node node = contained.item(i);
 				if (node.getNodeType() == Node.ELEMENT_NODE)
 				{
 					Element innerXml = (Element) node;
 					if ("selectionChoice".equals(innerXml.getTagName()))
 					{
 						Message displayMsg = null;
 						Message valueMsg = null;
 						Element wayInnerXml = XmlHelper.getChildElementNamed(innerXml, "displayMessage");
 						if (wayInnerXml != null)
 						{
 							displayMsg = new UiMessage(service, wayInnerXml);
 						}
 						wayInnerXml = XmlHelper.getChildElementNamed(innerXml, "valueMessage");
 						if (wayInnerXml != null)
 						{
 							valueMsg = new UiMessage(service, wayInnerXml);
 						}
 						this.selectionMessages.add(displayMsg);
 						this.selectionValues.add(valueMsg);
 
 						// is there a container?
 						Container container = null;
 						boolean separate = false;
 						boolean reversed = false;
 						Element containerXml = XmlHelper.getChildElementNamed(innerXml, "container");
 						if (containerXml != null)
 						{
 							String separateCode = StringUtil.trimToNull(containerXml.getAttribute("separate"));
 							separate = "TRUE".equals(separateCode);
 
 							String reversedCode = StringUtil.trimToNull(containerXml.getAttribute("reversed"));
 							reversed = "TRUE".equals(reversedCode);
 
 							container = new UiContainer(service, innerXml);
 						}
 						this.selectionContainers.add(new ContainerRef(container, separate, reversed));
 					}
 				}
 			}
 		}
 
 		// selection choices from model
 		settingsXml = XmlHelper.getChildElementNamed(xml, "selectionModel");
 		if (settingsXml != null)
 		{
 			String name = StringUtil.trimToNull(settingsXml.getAttribute("name"));
 			if (name != null) this.iteratorName = name;
 
 			// short for model
 			model = StringUtil.trimToNull(settingsXml.getAttribute("model"));
 			if (model != null)
 			{
 				this.selectionReference = service.newPropertyReference().setReference(model);
 			}
 
 			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
 			if (innerXml != null)
 			{
 				this.selectionReference = service.parsePropertyReference(innerXml);
 			}
 
 			// value message
 			innerXml = XmlHelper.getChildElementNamed(settingsXml, "valueMessage");
 			if (innerXml != null)
 			{
 				this.selectionValueMessage = new UiMessage(service, innerXml);
 			}
 
 			// display model
 			innerXml = XmlHelper.getChildElementNamed(settingsXml, "displayMessage");
 			if (innerXml != null)
 			{
 				this.selectionDisplayMessage = new UiMessage(service, innerXml);
 			}
 		}
 
 		// read only shortcut
 		String readOnly = StringUtil.trimToNull(xml.getAttribute("readOnly"));
 		if ((readOnly != null) && ("TRUE".equals(readOnly)))
 		{
 			this.readOnly = new UiDecision().setProperty(new UiConstantPropertyReference().setValue("true"));
 		}
 
 		// read only collapsed shortcut
 		String readOnlyCollapsed = StringUtil.trimToNull(xml.getAttribute("readOnlyCollapsed"));
 		if ((readOnlyCollapsed != null) && ("TRUE".equals(readOnlyCollapsed)))
 		{
 			this.readOnlyCollapsed = new UiDecision().setProperty(new UiConstantPropertyReference().setValue("true"));
 		}
 
 		// read only
 		settingsXml = XmlHelper.getChildElementNamed(xml, "readOnly");
 		if (settingsXml != null)
 		{
 			this.readOnly = service.parseDecisions(settingsXml);
 		}
 
 		// read only collapsed
 		settingsXml = XmlHelper.getChildElementNamed(xml, "readOnlyCollapsed");
 		if (settingsXml != null)
 		{
 			this.readOnlyCollapsed = service.parseDecisions(settingsXml);
 		}
 
 		// single select
 		settingsXml = XmlHelper.getChildElementNamed(xml, "singleSelect");
 		if (settingsXml != null)
 		{
 			this.singleSelectDecision = service.parseDecisions(settingsXml);
 		}
 
 		// short for height
 		String height = StringUtil.trimToNull(xml.getAttribute("height"));
 		if (height != null)
 		{
 			this.setHeight(Integer.parseInt(height));
 		}
 
 		// submit value
 		String submitValue = StringUtil.trimToNull(xml.getAttribute("submitValue"));
 		if ((submitValue != null) && ("TRUE".equals(submitValue)))
 		{
 			this.submitValue = true;
 		}
 
 		// submitDestination
 		settingsXml = XmlHelper.getChildElementNamed(xml, "destination");
 		if (settingsXml != null)
 		{
 			// let Destination parse this
 			this.submitDestination = new UiDestination(service, settingsXml);
 		}
 
 		// onEmptyAlert
 		settingsXml = XmlHelper.getChildElementNamed(xml, "onEmptyAlert");
 		if (settingsXml != null)
 		{
 			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
 			if (innerXml != null)
 			{
 				this.onEmptyAlertMsg = new UiMessage(service, innerXml);
 			}
 
 			this.onEmptyAlertDecision = service.parseDecisions(settingsXml);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection addComponentToSelection(Component component, boolean separate)
 	{
 		this.selectionContainers.get(this.selectionContainers.size() - 1).container.add(component);
 		// TODO: separate
 
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection addSelection(Message selector, Message value)
 	{
 		this.selectionValues.add(value);
 		this.selectionMessages.add(selector);
 		this.selectionContainers.add(new ContainerRef(new UiContainer(), false, false));
 
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean render(Context context, Object focus)
 	{
 		// included?
 		if (!isIncluded(context, focus)) return false;
 
 		// read only?
 		boolean readOnly = false;
 		if (this.readOnly != null)
 		{
 			readOnly = this.readOnly.decide(context, focus);
 		}
		
 		// read only collapsed?
 		boolean readOnlyCollapsed = false;
 		if (this.readOnlyCollapsed != null)
 		{
 			readOnlyCollapsed = this.readOnlyCollapsed.decide(context, focus);
 			if (readOnlyCollapsed) readOnly = true;
 		}
 
 		// single select?
 		boolean single = true;
 		if (this.singleSelectDecision != null)
 		{
 			single = this.singleSelectDecision.decide(context, focus);
 		}
 
 		// alert if empty at submit?
 		boolean onEmptyAlert = false;
 		if (this.onEmptyAlertMsg != null)
 		{
 			onEmptyAlert = true;
 			if (this.onEmptyAlertDecision != null)
 			{
 				onEmptyAlert = this.onEmptyAlertDecision.decide(context, focus);
 			}
 		}
 
 		// find values and display text
 		List<String> values = new ArrayList<String>();
 		if (!this.selectionValues.isEmpty())
 		{
 			for (Message msg : this.selectionValues)
 			{
 				if (msg != null)
 				{
 					values.add(msg.getMessage(context, focus));
 				}
 				else
 				{
 					values.add("");
 				}
 			}
 		}
 
 		List<String> display = new ArrayList<String>();
 		if (!this.selectionMessages.isEmpty())
 		{
 			for (Message msg : this.selectionMessages)
 			{
 				if (msg != null)
 				{
 					display.add(msg.getMessage(context, focus));
 				}
 				else
 				{
 					display.add("");
 				}
 			}
 		}
 
 		// add in any from the model
 		boolean fromModel = false;
 		if ((this.selectionValueMessage != null) && (this.selectionDisplayMessage != null) && (this.selectionReference != null))
 		{
 			// get the main collection
 			Collection collection = null;
 			Object obj = this.selectionReference.readObject(context, focus);
 			if (obj != null)
 			{
 				if (obj instanceof Collection)
 				{
 					collection = (Collection) obj;
 				}
 
 				else if (obj.getClass().isArray())
 				{
 					Object[] array = (Object[]) obj;
 					collection = new ArrayList(array.length);
 					for (Object o : array)
 					{
 						collection.add(o);
 					}
 				}
 			}
 
 			// if we got something
 			if (collection != null)
 			{
 				// like iteration, make each object available then get the value and display
 				int index = -1;
 				for (Object o : collection)
 				{
 					index++;
 
 					// place the item
 					if (this.iteratorName != null)
 					{
 						context.put(this.iteratorName, o, this.selectionReference.getEncoding(context, o, index));
 					}
 
 					values.add(this.selectionValueMessage.getMessage(context, o));
 					display.add(this.selectionDisplayMessage.getMessage(context, o));
 					fromModel = true;
 
 					// remove item
 					if (this.iteratorName != null)
 					{
 						context.remove(this.iteratorName);
 					}
 				}
 			}
 		}
 
 		// read the current value(s)
 		Set<String> value = new HashSet<String>();
 		if (this.propertyReference != null)
 		{
 			Object obj = this.propertyReference.readObject(context, focus);
 			if (obj != null)
 			{
 				// any sort of collection
 				if (obj instanceof Collection)
 				{
 					for (Object o : ((Collection) obj))
 					{
 						value.add(o.toString());
 					}
 				}
 
 				// any sort of array
 				if (obj.getClass().isArray())
 				{
 					for (Object o : ((Object[]) obj))
 					{
 						value.add(o.toString());
 					}
 				}
 
 				// otherwise take it as a string
 				else
 				{
 					value.add(obj.toString());
 				}
 			}
 		}
 
 		if (this.orientation == Orientation.dropdown)
 		{
 			renderDropdown(context, focus, readOnly, single, values, display, value);
 			return true;
 		}
 
 		// generate some ids
 		int idRoot = context.getUniqueId();
 		String id = getId();
 		if (id == null) id = this.getClass().getSimpleName() + "_" + idRoot;
 		String decodeId = "decode_" + idRoot;
 		String dependencyId = id + "_dependencies";
 
 		PrintWriter response = context.getResponseWriter();
 
 		// read the "correct" value
 		String correctValue = null;
 		boolean includeCorrectMarkers = false;
 		if ((this.correctReference != null) && ((this.correctDecision == null) || (this.correctDecision.decide(context, focus))))
 		{
 			correctValue = this.correctReference.read(context, focus);
 			if (correctValue != null)
 			{
 				includeCorrectMarkers = true;
 			}
 		}
 
 		if (onEmptyAlert)
 		{
 			// this will become visible if a submit happens and the validation fails
 			response.println("<div class=\"ambrosiaAlert\" style=\"display:none\" id=\"alert_" + id + "\">"
 					+ this.onEmptyAlertMsg.getMessage(context, focus) + "</div>");
 		}
 
 		boolean includeSelectAll = this.selectAll && (!single);
 
 		// title if we are doing select all
 		if (includeSelectAll)
 		{
 			response.print("<input type=\"checkbox\" id=\"all_" + id + "\"" + " onclick=\"ambrosiaSelectGroup(this, ids_" + id + ");\"" + " />");
 			if (this.titleMessage != null)
 			{
 				response.print("<label for=\"all_" + id + "\">");
 				response.print(this.titleMessage.getMessage(context, focus));
 				response.println("</label><br /><br />");
 			}
 		}
 
 		// title otherwise
 		else if (this.titleMessage != null)
 		{
 			response.print("<div class=\"ambrosiaComponentTitle\">");
 			response.print(this.titleMessage.getMessage(context, focus));
 			response.println("</div>");
 		}
 
 		// for a single option (unless we are using values from the model, even just one)
 		if ((values.size() == 1) && !fromModel)
 		{
 			String onclick = "";
 
 			if (this.submitDestination != null)
 			{
 				String destination = this.submitDestination.getDestination(context, focus);
 				onclick = "onclick=\"ambrosiaSubmit('" + destination + "')\" ";
 			}
 			else if (this.submitValue)
 			{
 				onclick = "onclick=\"ambrosiaSubmit(this.value)\" ";
 			}
 
 			// convert to boolean
 			boolean checked = value.contains("true");
 
 			// if we are doing correct marking
 			if (includeCorrectMarkers)
 			{
 				// if checked, mark as correct or not
 				if (checked)
 				{
 					// is the value correct?
 					boolean correct = (correctValue == null) ? false : Boolean.parseBoolean(correctValue) == checked;
 
 					if (correct)
 					{
 						response.print("<img src=\"" + context.getUrl(this.correctIcon) + "\" alt=\""
 								+ this.correctMessage.getMessage(context, focus) + "\" title=\"" + this.correctMessage.getMessage(context, focus)
 								+ "\"/>");
 					}
 					else
 					{
 						response.print("<img src=\"" + context.getUrl(this.incorrectIcon) + "\" alt=\""
 								+ this.incorrectMessage.getMessage(context, focus) + "\" title=\"" + this.incorrectMessage.getMessage(context, focus)
 								+ "\"/>");
 					}
 				}
 
 				// else leave a placeholder
 				else
 				{
 					response.println("<div style=\"float:left;width:16px\">&nbsp;</div>");
 				}
 			}
 
 			// the check box
 			response.println("<input " + onclick + "type=\"checkbox\" name=\"" + id + "\" id=\"" + id + "\" value=\"" + values.get(0) + "\" "
 					+ (checked ? "CHECKED" : "") + (readOnly ? " disabled=\"disabled\"" : "") + " />");
 
 			context.editComponentRendered(id);
 
 			// the decode directive
 			if ((this.propertyReference != null) && (!readOnly))
 			{
 				response.println("<input type=\"hidden\" name=\"" + decodeId + "\" value =\"" + id + "\" />" + "<input type=\"hidden\" name=\""
 						+ "prop_" + decodeId + "\" value=\"" + this.propertyReference.getFullReference(context) + "\" />"
 						+ "<input type=\"hidden\" name=\"" + "null_" + decodeId + "\" value=\"" + this.notSelectedValue + "\" />");
 			}
 
 			// title after (right) for the single check box version
 			if ((display.size() > 0) && (display.get(0) != null))
 			{
 				response.print("<label for=\"" + id + "\">");
 				response.print(display.get(0));
 				response.println("</label>");
 			}
 		}
 
 		else
 		{
 			final StringBuffer dependency = new StringBuffer();
 			dependency.append("var " + dependencyId + "=[");
 			String startingValue = null;
 			boolean needDependencies = false;
 			String onclick = "";
 
 			boolean hasContained = false;
 			for (ContainerRef cr : this.selectionContainers)
 			{
 				if ((cr.container != null) && (!cr.container.getContained().isEmpty()))
 				{
 					hasContained = true;
 					break;
 				}
 			}
 			if (hasContained)
 			{
 				onclick = "onclick=\"ambrosiaSelectDependencies(this.value, " + dependencyId + ")\" ";
 			}
 			else if (this.submitDestination != null)
 			{
 				String destination = this.submitDestination.getDestination(context, focus);
 				onclick = "onclick=\"ambrosiaSubmit('" + destination + "')\" ";
 			}
 			else if (this.submitValue)
 			{
 				onclick = "onclick=\"ambrosiaSubmit(this.value)\" ";
 			}
 			else if (includeSelectAll)
 			{
 				onclick = "onclick=\"ambrosiaSelectChange(this, ids_" + id + ", 'all_" + id + "');\" ";
 			}
 
 			// collect the rendered ids
 			List<String> ids = new ArrayList<String>();
 
 			// collected dependent components marked separate
 			String fragment = "";
 
 			for (int i = 0; i < values.size(); i++)
 			{
 				String val = values.get(i);
 				String message = "";
 				if (i < display.size())
 				{
 					message = display.get(i);
 				}
 				ContainerRef containerRef = null;
 				if (i < this.selectionContainers.size())
 				{
 					containerRef = this.selectionContainers.get(i);
 				}
 
 				boolean selected = value.contains(val);
				
 				if ((!selected) && readOnlyCollapsed) continue;
 
 				if (selected)
 				{
 					startingValue = val;
 				}
 
 				// if we are doing correct marking
 				if (includeCorrectMarkers)
 				{
 					// if checked, mark as correct or not
 					if (selected)
 					{
 						// is this one the correct one?
 						boolean correct = (correctValue == null) ? false : correctValue.equals(val);
 
 						if (correct)
 						{
 							response.print("<img src=\"" + context.getUrl(this.correctIcon) + "\" alt=\""
 									+ this.correctMessage.getMessage(context, focus) + "\" title=\"" + this.correctMessage.getMessage(context, focus)
 									+ "\"/>");
 						}
 						else
 						{
 							response.print("<img src=\"" + context.getUrl(this.incorrectIcon) + "\" alt=\""
 									+ this.incorrectMessage.getMessage(context, focus) + "\" title=\""
 									+ this.incorrectMessage.getMessage(context, focus) + "\"/>");
 						}
 					}
 
 					// else leave a placeholder
 					else
 					{
 						response.println("<div style=\"float:left;width:16px\">&nbsp;</div>");
 					}
 				}
 
 				// use a radio for single select
 				String thisId = id + "_" + i;
 				if (single)
 				{
 					// the radio button
 					response.println("<input " + onclick + "type=\"radio\" name=\"" + id + "\" id=\"" + thisId + "\" value=\"" + val + "\" "
 							+ (selected ? "CHECKED" : "") + (readOnly ? " disabled=\"disabled\"" : "") + " />");
 				}
 
 				// for multiple selection, use a checkbox set
 				else
 				{
 					response.println("<input " + onclick + "type=\"checkbox\" name=\"" + id + "\" id=\"" + thisId + "\" value=\"" + val + "\" "
 							+ (selected ? "CHECKED" : "") + (readOnly ? " disabled=\"disabled\"" : "") + " />");
 				}
 				ids.add(thisId);
 
 				// message
 				response.print("<label for=\"" + thisId + "\">");
 				response.print(message);
 				response.println("</label>");
 
 				// container of dependent components
 				if ((containerRef != null) && (containerRef.container != null) && (!containerRef.container.getContained().isEmpty()))
 				{
 					needDependencies = true;
 
 					dependency.append("[\"" + val + "\",");
 					dependency.append(Boolean.toString(containerRef.reversed) + ",");
 					RenderListener listener = new RenderListener()
 					{
 						public void componentRendered(String id)
 						{
 							dependency.append("\"" + id + "\",");
 						}
 					};
 
 					// listen for any dependent edit components being rendered
 					context.addEditComponentRenderListener(listener);
 
 					if (containerRef.separate)
 					{
 						context.setCollecting();
 						response = context.getResponseWriter();
 					}
 
 					// render the dependent components
 					for (Component c : containerRef.container.getContained())
 					{
 						if (containerRef.separate) response.println("<div class=\"ambrosiaContainerComponent\">");
 						c.render(context, focus);
 						if (containerRef.separate) response.println("</div>");
 					}
 
 					if (containerRef.separate)
 					{
 						fragment += context.getCollected();
 						response = context.getResponseWriter();
 					}
 
 					// stop listening
 					context.removeEditComponentRenderListener(listener);
 
 					dependency.setLength(dependency.length() - 1);
 					dependency.append("],");
 				}
 
 				if (this.orientation == Orientation.vertical)
 				{
 					response.println("<br />");
 				}
 			}
 
 			// register the ids in reverse order
 			// Note: reverse order so that the first choice gets processed last, as in when this is a dependent component to another selection
 			Collections.reverse(ids);
 			for (String thisId : ids)
 			{
 				context.editComponentRendered(thisId);
 			}
 
 			// record the group of checkbox element ids
 			if (includeSelectAll)
 			{
 				StringBuilder buf = new StringBuilder();
 				buf.append("var ids_" + id + "=[");
 				for (String oneId : ids)
 				{
 					buf.append("\"" + oneId + "\",");
 				}
 				buf.setLength(buf.length() - 1);
 				buf.append("];\n");
 				context.addScript(buf.toString());
 			}
 
 			if (needDependencies)
 			{
 				dependency.setLength(dependency.length() - 1);
 				dependency.append("];\n");
 				context.addScript(dependency.toString());
				context.addScript("ambrosiaSelectDependencies(\"" + startingValue + "\", " + dependencyId + ");\n");
 			}
 
 			// the decode directive
 			if ((this.propertyReference != null) && (!readOnly))
 			{
 				response.println("<input type=\"hidden\" name=\"" + decodeId + "\" value =\"" + id + "\" />" + "<input type=\"hidden\" name=\""
 						+ "prop_" + decodeId + "\" value=\"" + this.propertyReference.getFullReference(context) + "\" />");
 			}
 
 			// for onEmptyAlert, add some client-side validation
 			if ((onEmptyAlert) && (!readOnly) && single)
 			{
 				StringBuffer buf = new StringBuffer();
 				for (int i = 0; i < values.size(); i++)
 				{
 					buf.append("!document.getElementById('" + id + "_" + Integer.toString(i) + "').checked &&");
 				}
 				buf.setLength(buf.length() - 3);
 
 				context.addValidation("	if (" + buf.toString() + ")\n" + "	{\n" + "		if (document.getElementById('alert_" + id
 						+ "').style.display == \"none\")\n" + "		{\n" + "			document.getElementById('alert_" + id + "').style.display = \"\";\n"
 						+ "			rv=false;\n" + "		}\n" + "	}\n");
 			}
 
 			if (fragment.length() > 0)
 			{
 				response.print(fragment);
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection setCorrect(PropertyReference correctReference)
 	{
 		this.correctReference = correctReference;
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection setCorrectDecision(Decision decision)
 	{
 		this.correctDecision = decision;
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection setDestination(Destination destination)
 	{
 		this.submitDestination = destination;
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection setHeight(int height)
 	{
 		this.height = height;
 
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection setOnEmptyAlert(Decision decision, String selector, PropertyReference... references)
 	{
 		this.onEmptyAlertDecision = decision;
 		this.onEmptyAlertMsg = new UiMessage().setMessage(selector, references);
 
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection setOrientation(Orientation orientation)
 	{
 		this.orientation = orientation;
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection setProperty(PropertyReference propertyReference)
 	{
 		this.propertyReference = propertyReference;
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection setReadOnly(Decision decision)
 	{
 		this.readOnly = decision;
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection setReadOnlyCollapsed(Decision decision)
 	{
 		this.readOnlyCollapsed = decision;
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection setSelectAll(boolean setting)
 	{
 		this.selectAll = setting;
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection setSelectedValue(String value)
 	{
 		this.selectedValue = value;
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection setSelectionModel(PropertyReference modelRef, String iteratorName, Message valueRef, Message displayRef)
 	{
 		this.selectionReference = modelRef;
 		this.iteratorName = iteratorName;
 		this.selectionValueMessage = valueRef;
 		this.selectionDisplayMessage = displayRef;
 
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection setSingleSelectDecision(Decision decision)
 	{
 		this.singleSelectDecision = decision;
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection setSubmitValue()
 	{
 		this.submitValue = true;
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Selection setTitle(String selector, PropertyReference... references)
 	{
 		this.titleMessage = new UiMessage().setMessage(selector, references);
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected void renderDropdown(Context context, Object focus, boolean readOnly, boolean single, List<String> values, List<String> display,
 			Set<String> value)
 	{
 		// generate some ids
 		int idRoot = context.getUniqueId();
 		String id = this.getClass().getSimpleName() + "_" + idRoot;
 		String decodeId = "decode_" + idRoot;
 
 		PrintWriter response = context.getResponseWriter();
 
 		// Note: correct / incorrect markings not supported for dropdown
 
 		// title
 		if (this.titleMessage != null)
 		{
 			response.print("<label class=\"ambrosiaComponentTitle\" for=\"" + id + "\">");
 			response.print(this.titleMessage.getMessage(context, focus));
 			response.println("</label>");
 		}
 
 		String onchange = "";
 		if (this.submitDestination != null)
 		{
 			String destination = this.submitDestination.getDestination(context, focus);
 			onchange = " onchange=\"ambrosiaSubmit('" + destination + "');\" ";
 		}
 		else if (this.submitValue)
 		{
 			onchange = " onchange=\"ambrosiaSubmit(this.value);\" ";
 		}
 
 		response.println("<select size=\"" + Integer.toString(this.height) + "\" " + (single ? "" : "multiple ") + "name=\"" + id + "\" id=\"" + id
 				+ "\"" + (readOnly ? " disabled=\"disabled\"" : "") + onchange + ">");
 
 		// TODO: must have selection values
 
 		// TODO: selectionContainers not supported
 
 		for (int i = 0; i < values.size(); i++)
 		{
 			String val = values.get(i);
 			String message = "";
 			if (i < display.size())
 			{
 				message = display.get(i);
 			}
 
 			boolean selected = value.contains(val);
 
 			// the option
 			response.println("<option " + "value=\"" + val + "\" " + (selected ? "selected=\"selected\"" : "") + ">" + message + "</option>");
 		}
 
 		response.println("</select>");
 
 		// the decode directive
 		if ((this.propertyReference != null) && (!readOnly))
 		{
 			response.println("<input type=\"hidden\" name=\"" + decodeId + "\" value =\"" + id + "\" />" + "<input type=\"hidden\" name=\"" + "prop_"
 					+ decodeId + "\" value=\"" + this.propertyReference.getFullReference(context) + "\" />");
 		}
 	}
 }
