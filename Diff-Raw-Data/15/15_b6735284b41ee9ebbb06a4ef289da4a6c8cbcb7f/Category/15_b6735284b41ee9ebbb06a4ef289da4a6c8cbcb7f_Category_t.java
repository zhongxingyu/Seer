 package com.forum.client;
 
 import java.util.ArrayList;
 
 import com.forum.client.data.ForumService;
 import com.forum.client.data.ForumServiceAsync;
 import com.forum.client.data.SessionHandler;
 import com.forum.client.data.TopicData;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Cursor;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.IButton;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 
 /**
  * A graphical representation of the list of topics in the forum
  * 
  * @author jonas
  * 
  */
 public class Category extends Canvas {
 	// service
 	private ForumServiceAsync forumSvc = GWT.create(ForumService.class);
 	// is the component hidden?
 	private boolean hidden = true;
 	// the name of the category
 	private String name = "";
 	// the label containing the title
 	private Label title;
 	// the parent that holds the component
 	private Canvas parent;
 	// labels of the topics
 	private ArrayList<Label> labels = new ArrayList<Label>();
 	// the IDs' of the topice
 	private ArrayList<ForumTopic> topics = new ArrayList<ForumTopic>();
 	// list of ids
 	private ArrayList<Integer> topicIds = new ArrayList<Integer>();
 	// a button to add a thread
 	private IButton addThreadButton = new IButton("Add Topic");
 	// the ID of nthe categort
 	private int catid;
 	// keeps track of where to draw
 	private int currentHeight = 0;
 	// keeps track of the current showing thread
 	private ForumTopic currentThread = null;
 	// Panel for adding topics
 	private AddTopicPanel addTopicPanel = null;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param name
 	 *            Name of the category
 	 * @param id
 	 *            ID of the category
 	 * @param parent
 	 *            the canvas where the component shall be put
 	 */
 	public Category(String name, int id, final Canvas parent) {
 		super();
 
 		setCanDragReposition(false);
 		setCanDragResize(false);
 		setWidth(600);
 		setHeight(10);
 		setBackgroundColor("#b0f963");
 		setBorder("1px solid #000000");
 		this.name = name;
 		this.parent = parent;
 		this.catid = id;
 		// title label
 		title = new Label("<div class='categoryTitle'>" + name
 				+ " Topics</div>");
 		title.setTop(currentHeight);
 		title.setHeight(25);
 		title.setWidth(200);
		title.setCursor(Cursor.POINTER);
 		title.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				if (hidden) {
 					if (addTopicPanel != null) {
 						removeChild(addTopicPanel);
 						addTopicPanel.destroy();
 					}
 					unhide();
 					killTopic();
 				}
 			}
 		});
 		addChild(title);
 		currentHeight += title.getHeight();
 		// button
 		addThreadButton.setTop(currentHeight);
 		currentHeight += addThreadButton.getHeight();
 		addThreadButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				if (!hidden) {
 					hide();
 				} else {
 					killTopic();
 				}
 				addTopicPanel = new AddTopicPanel(catid, parent, getThis());
 			}
 		});
 		forumSvc.isLoggedIn(SessionHandler.getSessionId(),
 				new AsyncCallback<Boolean>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 
 					}
 
 					@Override
 					public void onSuccess(Boolean result) {
 						if (result) {
 							addChild(addThreadButton);
 						}
 					}
 				});
 
 		getTopics();
 	}
 
 	public void destroyAddTopicPanel() {
 		if (addTopicPanel != null) {
 			addTopicPanel.destroy();
 		}
 	}
 
 	/**
 	 * @return the addTopicPanel
 	 */
 	public AddTopicPanel getAddTopicPanel() {
 		return addTopicPanel;
 	}
 
 	/**
 	 * @param addTopicPanel
 	 *            the addTopicPanel to set
 	 */
 	public void setAddTopicPanel(AddTopicPanel addTopicPanel) {
 		this.addTopicPanel = addTopicPanel;
 	}
 
 	/**
 	 * Adds a topic to the category
 	 * 
 	 * @param topic
 	 *            The topic to be added
 	 */
 	public void addThread(final ForumTopic topic) {
 		final Label label = new Label("<div class='category'>"
 				+ topic.getName() + "</div>");
 		topics.add(topic);
 		label.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				if (currentThread != topic) {
 					if (addTopicPanel != null) {
 						removeChild(addTopicPanel);
 						addTopicPanel.destroy();
 					}
 					topic.draw();
 					hide();
 					currentThread = topic;
 				}
 			}
 		});
 
 		labels.add(label);
 		label.setHeight(30);
 		label.setWidth(getWidth());
		label.setCursor(Cursor.POINTER);
 		setHeight(getHeight() + label.getHeight());
 		label.setTop(currentHeight);
 		currentHeight += label.getHeight();
 		addChild(label);
 	}
 
 	/**
 	 * Gets the name of the categoy
 	 * 
 	 * @return the name of the category
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Gets a reference to the categort a help for nested calsses
 	 * 
 	 * @return the category
 	 */
 	private Category getThis() {
 		return this;
 	}
 
 	/**
 	 * Gets the topics from the database that belongs to the category
 	 */
 	private void getTopics() {
 		AsyncCallback<TopicData[]> callback = new AsyncCallback<TopicData[]>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				System.err.println("failed getting topics");
 
 			}
 
 			@Override
 			public void onSuccess(TopicData[] result) {
 				for (int i = 0; i < result.length; i++) {
 
 					if (!topicIds.contains(result[i].getId())) {
 						addThread(new ForumTopic(result[i].getId(), result[i]
 								.getCategoryID(), result[i].getAuthorID(),
 								result[i].getName(), result[i].getDate(),
 								parent));
 						topicIds.add(result[i].getId());
 					}
 				}
 
 			}
 		};
 		forumSvc.getThreads(catid, callback);
 	}
 
 	/**
 	 * Hides the component
 	 */
 	@Override
 	public void hide() {
 		animateRect(250, 0, 200, getHeight(), null, 1000);
 		for (Label l : labels)
 			l.setWidth(200);
 		if (currentThread != null)
 			killTopic();
 		hidden = true;
 	}
 
 	/**
 	 * Takes away the current topic
 	 */
 	private void killTopic() {
 		currentThread.kill();
 		currentThread = null;
 	}
 
 	/**
 	 * Sets the current topic
 	 * 
 	 * @param topic
 	 *            topic to be the current topic
 	 */
 	public void setTopic(final int topicID) {
 
 		Timer timer = new Timer() {
 
 			@Override
 			public void run() {
 
 				getTopics();
 
 				Timer timer2 = new Timer() {
 
 					@Override
 					public void run() {
 						ForumTopic topic = null;
 						for (ForumTopic t : topics) {
 							if (t.getID() == topicID) {
 								topic = t;
 								break;
 							}
 						}
 						if (topic != null) {
 							topic.draw();
 							currentThread = topic;
 						}
 					}
 
 				};
 				timer2.schedule(500);
 			}
 
 		};
 
 		timer.schedule(500);
 
 	}
 
 	/**
 	 * Unhides the component
 	 */
 	public void unhide() {
 		animateRect(500, 0, 600, getHeight(), null, 1000);
 		getTopics();
 		for (Label l : labels)
 			l.setWidth(600);
 		setVisible(true);
 		hidden = false;
 	}
 
 }
