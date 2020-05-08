 package com.forum.client;
 
 import java.util.ArrayList;
 
 import com.forum.client.data.CategoryData;
 import com.forum.client.data.ForumService;
 import com.forum.client.data.ForumServiceAsync;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.smartgwt.client.types.Alignment;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 
 /**
  * A class that contains the categories of the forum it also extends Canvas to
  * have a GUI for the categories
  * 
  * @author jonas+henke
  * 
  */
 public class CategoryList extends Canvas {
 	// the service
 	private ForumServiceAsync forumSvc = GWT.create(ForumService.class);
 	// a list of the categories labels
 	private ArrayList<Label> labels = new ArrayList<Label>();
 	// a label that shows the title 'Forum Caytegries"
 	private final Label title = new Label(
 			"<div class='categoryHeader'> Forum Categories </div>");
 	// keeps track wither the component is hidden or not
 	private boolean hidden = false;
 	// a reference to the parent where the component is to be located
 	private Canvas parent;
 	// keeps track of the current category
 	private Category currentCategory = null;
 
 	// keeps track of the current height of internal components
 	private int currentHeight = 0;
 
 	/**
 	 * Constructor that only takes the parent as argument
 	 * 
 	 * @param parent
 	 *            Where the component will be
 	 */
 	public CategoryList(Canvas parent) {
 		super();
 		this.parent = parent;
 		setWidth(600);
 		setHeight(10);
 		setLeft(500);
 		title.setTop(currentHeight);
 		title.setHeight(30);
 		title.setWidth(200);
 		title.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				// TODO Auto-generated method stub
 				if (hidden) {
 					if (currentCategory != null) {
 						AddTopicPanel panel = currentCategory
 								.getAddTopicPanel();
 						if (panel != null) {
 							currentCategory.removeChild(panel);
 							currentCategory.destroyAddTopicPanel();
 						}
 					}
 					unhide();
 					killCategory();
 				}
 			}
 		});
 		addChild(title);
 		currentHeight += title.getHeight();
 
 		setCanDragReposition(false);
 		setCanDragResize(false);
 		setBorder("1px solid #000000");
 		setBackgroundColor("#b0f963");
 
 		getCategories();
 
 	}
 
 	/**
 	 * Adds an category to the forum
 	 * 
 	 * @param category
 	 *            the new category
 	 */
 	public void addCategory(final Category category) {
 		final Label label = new Label("<div class='category'>"
 				+ category.getName() + "</div>");
 		label.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				if (currentCategory != category) {
 					hide();
 					if (currentCategory != null) {
 						AddTopicPanel panel = currentCategory
 								.getAddTopicPanel();
 						if (panel != null) {
 							currentCategory.removeChild(panel);
 							currentCategory.destroyAddTopicPanel();
 
 						}
 						killCategory();
 					}
 					currentCategory = category;
 					parent.addChild(category);
 					category.unhide();
 				}
 			}
 		});
 		labels.add(label);
 		label.setHeight(30);
 		label.setWidth(getWidth());
 		label.setAlign(Alignment.CENTER);
 		setHeight(getHeight() + label.getHeight());
 		label.setTop(currentHeight);
 		currentHeight += label.getHeight();
 		addChild(label);
 	}
 
 	/**
 	 * Gets the current categories of the forum
 	 */
 	private void getCategories() {
 		AsyncCallback<CategoryData[]> callback = new AsyncCallback<CategoryData[]>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				System.err.println("dark_doom");
 
 			}
 
 			@Override
 			public void onSuccess(CategoryData[] result) {
 				for (int i = 0; i < result.length; i++) {
 					addCategory(new Category(result[i].getName(), result[i]
 							.getId(), parent));
 				}
 
 			}
 
 		};
 		forumSvc.getCategories(callback);
 	}
 
 	/**
 	 * Hides the categoylist to the far left of the parent. The movement is
 	 * animated = super cool
 	 */
 	@Override
 	public void hide() {
 
 		animateRect(0, 0, 200, getHeight(), null, 1000);
 		for (Label l : labels)
 			l.setWidth(200);
 		hidden = true;
 	}
 
 	/**
 	 * Removes the current category that is in foucus.
 	 */
 	public void killCategory() {
 		currentCategory.hide();
 		parent.removeChild(currentCategory);
 		currentCategory = null;
 	}
 
 	/**
 	 * Unhides the component and places it in focus.
 	 */
 	public void unhide() {
 		animateRect(500, 0, 600, getHeight(), null, 1000);
 		for (Label l : labels)
 			l.setWidth(600);
 		hidden = false;
 	}
 
 }
