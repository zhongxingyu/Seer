 /*
  * $Id: FXTooltipFeedbackPanel.java 560 2006-01-25 07:47:44 -0800 (Wed, 25 Jan
  * 2006) marcovandehaar $ $Revision$ $Date: 2006-01-25 07:47:44 -0800
  * (Wed, 25 Jan 2006) $
  * 
  * ==============================================================================
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package wicket.contrib.markup.html.form.validation;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import wicket.AttributeModifier;
 import wicket.MarkupContainer;
 import wicket.feedback.ComponentFeedbackMessageFilter;
 import wicket.feedback.FeedbackMessage;
 import wicket.feedback.FeedbackMessagesModel;
 import wicket.feedback.IFeedback;
 import wicket.feedback.IFeedbackMessageFilter;
 import wicket.markup.html.WebMarkupContainer;
 import wicket.markup.html.basic.Label;
 import wicket.markup.html.form.FormComponent;
 import wicket.markup.html.list.ListItem;
 import wicket.markup.html.list.ListView;
 import wicket.markup.html.panel.Panel;
 import wicket.model.IModel;
 import wicket.model.Model;
 
 /**
  * FeedbackPanel used in FXFeedbackTooltip Created as an external class in order
  * to let it define it's own UL and LI styles in FXTooltipFeedbackPanel.html.
  * 
  * @author Marco van de Haar
  * @author Ruud Booltink
  */
 public class FXTooltipFeedbackPanel extends Panel implements IFeedback
 {
 	/**
 	 * List for messages.
 	 */
 	private final class MessageListView extends ListView<FeedbackMessage>
 	{
 		private static final long serialVersionUID = 1L;
 
 		/**
 		 * @param parent
 		 * @param id
 		 * @see wicket.Component#Component(String)
 		 */
 		public MessageListView(MarkupContainer parent, final String id)
 		{
 			super(parent, id);
 			setModel(newFeedbackMessagesModel());
 		}
 
 		/**
 		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
 		 */
 		protected void populateItem(final ListItem<FeedbackMessage> listItem)
 		{
 			final FeedbackMessage message = listItem.getModelObject();
 			final IModel replacementModel = new Model()
 			{
 				private static final long serialVersionUID = 1L;
 
 				/**
 				 * Returns feedbackPanel + the message level, eg
 				 * 'feedbackPanelERROR'. This is used as the class of the li /
 				 * span elements.
 				 * 
 				 * @see wicket.model.IModel#getObject()
 				 */
 				public Object getObject()
 				{
 					return getCSSClass(message);
 				}
 			};
 
			final Label label = new Label(listItem, "message", message.getMessage().toString());
 			label.setEscapeModelStrings(getEscapeMessages());
 			final AttributeModifier levelModifier = new AttributeModifier("class", replacementModel);
 			label.add(levelModifier);
 			listItem.add(levelModifier);
 		}
 	}
 
 	private static final long serialVersionUID = 1L;
 
 	/** whether model messages should be HTML escaped. Default is true. */
 	private boolean escapeMessages = true;
 
 	private ComponentFeedbackMessageFilter filter;
 
 	/** Message view */
 	private final MessageListView messageListView;
 
 	/**
 	 * @param parent
 	 * @param id
 	 * @param c
 	 * @see wicket.Component#Component(String)
 	 */
 	public FXTooltipFeedbackPanel(MarkupContainer parent, final String id, FormComponent c)
 	{
 		super(parent, id);
 
 		WebMarkupContainer messagesContainer = new WebMarkupContainer(this, "feedbackul")
 		{
 			private static final long serialVersionUID = 1L;
 
 			public boolean isVisible()
 			{
 				return anyMessage();
 			}
 
 		};
 		filter = new ComponentFeedbackMessageFilter(c);
 		this.messageListView = new MessageListView(messagesContainer, "messages");
 		messageListView.setVersioned(false);
 	}
 
 
 	/**
 	 * Gets whether model messages should be HTML escaped. Default is true.
 	 * 
 	 * @return whether model messages should be HTML escaped
 	 */
 	public final boolean getEscapeMessages()
 	{
 		return escapeMessages;
 	}
 
 	/**
 	 * @see wicket.Component#isVersioned()
 	 */
 	public boolean isVersioned()
 	{
 		return false; // makes no sense to version the feedback panel
 	}
 
 	/**
 	 * Sets whether model messages should be HTML escaped. Default is true.
 	 * 
 	 * @param escapeMessages
 	 *            whether model messages should be HTML escaped
 	 */
 	public final void setEscapeMessages(boolean escapeMessages)
 	{
 		this.escapeMessages = escapeMessages;
 	}
 
 	/**
 	 * @param maxMessages
 	 *            The maximum number of feedback messages that this feedback
 	 *            panel should show at one time
 	 */
 	public final void setMaxMessages(int maxMessages)
 	{
 		this.messageListView.setViewSize(maxMessages);
 	}
 
 	/**
 	 * Sets the comparator used for sorting the messages.
 	 * 
 	 * @param sortingComparator
 	 *            comparator used for sorting the messages.
 	 */
 	public final void setSortingComparator(Comparator<FeedbackMessage> sortingComparator)
 	{
 		FeedbackMessagesModel feedbackMessagesModel = (FeedbackMessagesModel)messageListView
 				.getModel();
 		feedbackMessagesModel.setSortingComparator(sortingComparator);
 	}
 
 	/**
 	 * @see wicket.feedback.IFeedback#updateFeedback()
 	 */
 	public void updateFeedback()
 	{
 		// Force model to load
 		messageListView.getModelObject();
 	}
 
 	/**
 	 * Search messages that this panel will render, and see if there is any
 	 * message of level ERROR or up. This is a convenience method; same as
 	 * calling 'anyMessage(FeedbackMessage.ERROR)'.
 	 * 
 	 * @return whether there is any message for this panel of level ERROR or up
 	 */
 	protected final boolean anyErrorMessage()
 	{
 		return anyMessage(FeedbackMessage.ERROR);
 	}
 
 	/**
 	 * Search messages that this panel will render, and see if there is any
 	 * message.
 	 * 
 	 * @return whether there is any message for this panel
 	 */
 	protected final boolean anyMessage()
 	{
 		return anyMessage(FeedbackMessage.UNDEFINED);
 	}
 
 	/**
 	 * Search messages that this panel will render, and see if there is any
 	 * message of the given level.
 	 * 
 	 * @param level
 	 *            the level, see FeedbackMessage
 	 * @return whether there is any message for this panel of the given level
 	 */
 	protected final boolean anyMessage(int level)
 	{
 		List msgs = getCurrentMessages();
 
 		for (Iterator i = msgs.iterator(); i.hasNext();)
 		{
 			FeedbackMessage msg = (FeedbackMessage)i.next();
 			if (msg.isLevel(level))
 			{
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Gets the css class for the given message.
 	 * 
 	 * @param message
 	 *            the message
 	 * @return the css class; by default, this returns feedbackPanel + the
 	 *         message level, eg 'feedbackPanelERROR', but you can override this
 	 *         method to provide your own
 	 */
 	protected String getCSSClass(FeedbackMessage message)
 	{
 		return "feedbackTooltipERROR";
 	}
 
 	/**
 	 * Gets the currently collected messages for this panel.
 	 * 
 	 * @return the currently collected messages for this panel, possibly empty
 	 */
 	protected final List<FeedbackMessage> getCurrentMessages()
 	{
 		final List<FeedbackMessage> messages = messageListView.getModelObject();
 		return Collections.unmodifiableList(messages);
 	}
 
 	/**
 	 * @return Let subclass specify some other filter
 	 */
 	protected IFeedbackMessageFilter getFeedbackMessageFilter()
 	{
 		return filter;
 	}
 
 	/**
 	 * Gets a new instance of FeedbackMessagesModel to use.
 	 * 
 	 * @return instance of FeedbackMessagesModel to use
 	 */
 	protected FeedbackMessagesModel newFeedbackMessagesModel()
 	{
 		return new FeedbackMessagesModel(getPage(), getFeedbackMessageFilter());
 	}
 }
