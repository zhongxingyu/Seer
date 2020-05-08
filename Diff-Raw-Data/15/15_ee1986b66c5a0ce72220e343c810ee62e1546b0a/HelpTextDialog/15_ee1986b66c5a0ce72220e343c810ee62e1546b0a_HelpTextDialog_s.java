 package com.scholastic.sbam.client.uiobjects.uitop;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.data.BasePagingLoader;
 import com.extjs.gxt.ui.client.data.BeanModelReader;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.PagingLoadConfig;
 import com.extjs.gxt.ui.client.data.PagingLoadResult;
 import com.extjs.gxt.ui.client.data.PagingLoader;
 import com.extjs.gxt.ui.client.data.RpcProxy;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.CardPanel;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.SplitButton;
 import com.extjs.gxt.ui.client.widget.button.ToolButton;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
 import com.extjs.gxt.ui.client.widget.grid.LiveGridView;
 import com.extjs.gxt.ui.client.widget.layout.CardLayout;
 import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
 import com.extjs.gxt.ui.client.widget.layout.MarginData;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.extjs.gxt.ui.client.widget.menu.MenuItem;
 import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.LiveToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.scholastic.sbam.client.services.HelpTextSearchService;
 import com.scholastic.sbam.client.services.HelpTextSearchServiceAsync;
 import com.scholastic.sbam.client.services.HelpTextService;
 import com.scholastic.sbam.client.services.HelpTextServiceAsync;
 import com.scholastic.sbam.client.services.HelpTextWordService;
 import com.scholastic.sbam.client.services.HelpTextWordServiceAsync;
 import com.scholastic.sbam.client.uiobjects.foundation.EffectsDialog;
 import com.scholastic.sbam.client.util.IconSupplier;
 import com.scholastic.sbam.shared.exceptions.ServiceNotReadyException;
 import com.scholastic.sbam.shared.objects.FilterWordInstance;
 import com.scholastic.sbam.shared.objects.HelpTextInstance;
 import com.scholastic.sbam.shared.objects.SearchResultInstance;
 
 /**
  * A dialog window to display and navigate help text.
  * 
  * @author Bob Lacatena
  *
  */
 public class HelpTextDialog extends EffectsDialog implements HelpIndexTreeActor {
 	private		  boolean	EFFECT_ON			= true;
 	private 	  boolean	DYNAMIC_NAV_ICONS	= false;
 	private final int		DEFAULT_WIDTH		= 650;
 	private final int		DEFAULT_HEIGHT		= 500;
 	
 	/**
 	 * A button designed specifically to jump to another page.
 	 * @author Bob Lacatena
 	 *
 	 */
 	protected class JumpButton extends Button {
 		private String jumpId;
 		private String jumpTitle;
 		private String jumpIconName;
 		private String defaultTitle;
 		private String defaultIconName;
 		
 		public JumpButton(String defaultTitle, String defaultIconName) {
 			super(defaultTitle);
 			this.defaultTitle = defaultTitle;
 			this.defaultIconName = defaultIconName;
 			if (defaultIconName != null && defaultIconName.length() > 0)
 				IconSupplier.setIcon(this, defaultIconName);
 			this.addListener(Events.Select, getListener());
 			this.setJumpId(null);
 		}
 		
 		public SelectionListener<ButtonEvent> getListener() {
 			SelectionListener<ButtonEvent> listener = new SelectionListener<ButtonEvent>() {
 				@Override
 				public void componentSelected(ButtonEvent ce) {
 					jumpTo(jumpId);
 				}
 			};
 			return listener;
 		}
 		
 		public void setJump(String jumpId, String jumpTitle) {
 			setJumpId(jumpId);
 			setJumpTitle(jumpTitle);
 		}
 		
 		public void setJump(String jumpId, String jumpTitle, String jumpIconName) {
 			setJumpId(jumpId);
 			setJumpTitle(jumpTitle);
 			setJumpIconName(jumpIconName);
 		}
 
 		public String getJumpId() {
 			return jumpId;
 		}
 		public void setJumpId(String jumpId) {
 			this.jumpId = jumpId;
 			if (jumpId == null || jumpId.length() == 0)
 				disable();
 			else
 				enable();
 		}
 		public String getJumpTitle() {
 			return jumpTitle;
 		}
 		public void setJumpTitle(String jumpTitle) {
 			this.jumpTitle = jumpTitle;
 			if (jumpTitle != null && jumpTitle.length() > 0)
 				setText(jumpTitle);
 			else
 				setText(defaultTitle);
 		}
 		public String getJumpIconName() {
 			return jumpIconName;
 		}
 		public void setJumpIconName(String jumpIconName) {
 			this.jumpIconName = jumpIconName;
 			if (DYNAMIC_NAV_ICONS && jumpIconName != null && jumpIconName.length() > 0) {
 				IconSupplier.forceIcon(this, jumpIconName);
 			} else if (defaultIconName != null && defaultIconName.length() > 0) {
 				IconSupplier.forceIcon(this, defaultIconName);
 			} else {
 				setIcon(null);
 			}
 		}
 		
 	}
 	
 	/**
 	 * This class encapsulates a MenuItem that will perform jumps to spots in history.
 	 * @author Bob Lacatena
 	 *
 	 */
 	protected class JumpMenuItem extends MenuItem {
 		private String jumpId;
 		private int    histIndex;
 		
 		public JumpMenuItem(String setJumpId, String title, String iconName, int setHistIndex) {
 			super(title);
 			this.jumpId = setJumpId;
 			this.histIndex = setHistIndex;
 			IconSupplier.setIcon(this, iconName);
 			this.addListener(Events.Select, new Listener<BaseEvent>() {
 					public void handleEvent(BaseEvent me) {
 						jumpTo(jumpId, histIndex);
 					}
 				});
 		}
 		
 		public JumpMenuItem(String setJumpId, String title, String iconName) {
 			this(setJumpId, title, iconName, -1);
 		}
 		
 		public JumpMenuItem(int histIndex) {
 			this(historyIds.get(histIndex), historyTitles.get(histIndex), historyIconNames.get(histIndex), histIndex);
 		}
 	}
 	
 	public static class SearchGridSelectionModel extends GridSelectionModel<ModelData> {
 		HelpIndexTreeActor actor;
 		
 		SearchGridSelectionModel(HelpIndexTreeActor actor) {
 			super();
 			this.actor = actor;
 		}
 		
 		@Override
 		public void onSelectChange(ModelData model, boolean select) {
 			super.onSelectChange(model, select);
 			if (select) {
 				actor.jumpTo(model.get("id").toString());
 			}
 			this.deselectAll();
 		}
 	}
 	
 	private final HelpTextServiceAsync			helpTextService			= GWT.create(HelpTextService.class);
 	private final HelpTextWordServiceAsync		helpTextWordService		= GWT.create(HelpTextWordService.class);
 	private final HelpTextSearchServiceAsync	helpTextSearchService	= GWT.create(HelpTextSearchService.class);
 	
 	
 	private	String 				helpTextId;
 	private	HelpTextInstance	helpText;
 	
 	protected Html					text = new Html();
 	protected LayoutContainer		content;
 	protected CardLayout			cards;
 	protected CardPanel				textCard;
 	protected CardPanel				indexCard;
 	protected CardPanel				searchCard;
 	protected Button				done;
 	protected LayoutContainer		textContainer;
 	// Navigation toolbars
 	protected ToolBar				topToolBar;
 	protected ToolBar				bottomToolBar;
 	// Content navigation buttons
 	protected JumpButton			prevPageButton;
 	protected JumpButton			nextPageButton;
 	protected Button				indexButton;
 	protected Button				relatedButton;
 	protected Menu					relatedMenu;
 	protected Button				childrenButton;
 	protected Menu					childrenMenu;
 	protected JumpButton			parentButton;
 	protected ComboBox<ModelData>	searchBox;
 	// History navigation buttons
 	protected Button				histFirstButton;
 	protected Button				histLastButton;
 	protected SplitButton			histForwButton;
 	protected SplitButton			histBackButton;
 	protected Menu					histBackMenu;
 	protected Menu					histForwMenu;
 	
 	protected List<String>			historyIds = new ArrayList<String>();
 	protected List<String>			historyTitles = new ArrayList<String>();
 	protected List<String>			historyIconNames = new ArrayList<String>();
 	protected int					histIndex;
 	
 	protected TreePanel<ModelData>	indexTree = null;
 	
 	protected Timer					filterListenTimer;
 	protected String				filter = "";
 
 	private ContentPanel			searchGridContainer;
 	private ToolBar					searchToolBar;
 	private Grid<ModelData>			searchResultsGrid;
 	private LiveGridView			liveView;
 	ListStore<ModelData>			searchStore;
 	private PagingLoader<PagingLoadResult<SearchResultInstance>> searchLoader;
 	
 
 	public HelpTextDialog(String helpTextId) {
 		this.helpTextId = helpTextId;
 		init();
 	}
 
 	public HelpTextDialog() {
 		init();
 	}
 
 	public void init() {
 		FlowLayout layout = new FlowLayout();
 		setLayout(layout);
 	
 //		setAnimCollapse(true);
 		setButtonAlign(HorizontalAlignment.LEFT);
 		setButtons("");
 		IconSupplier.setIcon(this, IconSupplier.getHelpIconName());
 		setHeading("SBAM Help");
 		setModal(true);
 		setBodyBorder(true);
 //		setBodyStyle("padding: 8px;background: none");
 		setLayoutData(new MarginData(8));
 		setWidth(DEFAULT_WIDTH);
 		setHeight(DEFAULT_HEIGHT);
 		setResizable(true);
 		setClosable(true);
 		setShadow(true);
 		setShadowOffset(getShadowOffset() + (getShadowOffset() / 2));
 //		setAutoHide(true);
 		this.removeFromParentOnHide = true;
 		
 		addPanelButtons();
 		
 		topToolBar = getNavigationToolBar();
 		bottomToolBar = getHistoryToolBar();
 		
 		content = new LayoutContainer();
 
 		cards = new CardLayout();
 		content.setLayout(cards);
 //		content.setLayoutData(new FitData(0));
 		
 		textCard = new CardPanel();
 //		textCard.setLayout(new FlowLayout());
 		textContainer = new LayoutContainer(new FlowLayout(20));
 		textContainer.setScrollMode(Scroll.AUTO);
 		textContainer.setStyleAttribute("padding", "10px");
 		textContainer.addStyleName("sbam-help");
 		textContainer.add(text);
 		text.setAutoHeight(true);
 		textCard.add(textContainer);
 		content.add(textCard);
 //		content.add(text);
 		
 		setTopComponent(topToolBar);
 		
 		add(content);
 		
 		setBottomComponent(bottomToolBar);
 		
 		if (helpTextId != null && helpTextId.length() > 0)
 			jumpTo(helpTextId);
 		else
 			formatBlankPage();
 
 	}
 
 	private void addPanelButtons() {
 
 		
 		ToolButton printBtn = new ToolButton("x-tool-print");
 //		if (GXT.isAriaEnabled()) {
 //			helpBtn.setTitle(GXT.MESSAGES.pagingToolBar_beforePageText());
 //		}
 		printBtn.addListener(Events.Select, new Listener<ComponentEvent>() {
 			public void handleEvent(ComponentEvent ce) {
 				MessageBox.alert("Print", "Not yet implemented.", null);
 			}
 		});
 		head.addTool(printBtn);
 	}
 	
 	@Override
 	public void onResize(int width, int height) {
 		super.onResize(width, height);
 		// We have to do this so that the content gets sized properly, for scrollbars
 		content.setSize(this.getInnerWidth(), this.getInnerHeight());
 		// We have to do this so that the text content gets scroll bars
 		setTextContainerSize();
 		// We have to do this so that the search grid gets sized properly, for scrollbars
 		setSearchGridSize(width);
 	}
 	
 	private void setTextContainerSize() {
 		if (textContainer != null)
 			textContainer.setSize(this.getInnerWidth() - 32, this.getInnerHeight()); // Subtract 32 for scroll bar
 	}
 	
 	private void setSearchGridSize(int width) {
 		if (searchResultsGrid != null) {
 			searchResultsGrid.setSize(width, this.getInnerHeight() - searchToolBar.getHeight());
 		}
 	}
 	
 	/**
 	 * Create the navigation toolbar.
 	 * @return
 	 */
 	protected ToolBar getNavigationToolBar() {
 		ToolBar toolBar = new ToolBar();
 		toolBar.setAlignment(HorizontalAlignment.LEFT);
 		toolBar.setSpacing(5);
 		
 		prevPageButton = new JumpButton("Previous", IconSupplier.getHelpPrevIconName());
 		
 		nextPageButton = new JumpButton("Next", IconSupplier.getHelpNextIconName());
 		
 		relatedButton = new Button("Related");
 		relatedButton.disable();
 		IconSupplier.forceIcon(relatedButton, IconSupplier.getHelpRelatedIconName());
 		
 		relatedMenu = new Menu();
 		relatedMenu.setMaxHeight(200);
 		relatedButton.setMenu(relatedMenu);
 		
 		indexButton = new Button("Index");
 		IconSupplier.forceIcon(indexButton, IconSupplier.getHelpIndexIconName());
 		indexButton.addListener(Events.Select,  new SelectionListener<ButtonEvent>() {
 				@Override
 				public void componentSelected(ButtonEvent ce) {
 					formatIndex();
 				}
 			});
 
 		childrenButton = new Button("More");
 		childrenButton.disable();
 		IconSupplier.forceIcon(childrenButton, IconSupplier.getHelpMoreIconName());
 		
 		childrenMenu = new Menu();
 		childrenMenu.setMaxHeight(200);
 		childrenButton.setMenu(childrenMenu);
 		
 		parentButton = new JumpButton("Up", IconSupplier.getHelpUpIconName());
 		
 //		searchStore = getSearchStore();
 		
 		searchBox = getFilterBox();
 //		searchBox = new ComboBox<ModelData>();  
 //		searchBox.setFieldLabel("Search");  
 //		searchBox.setDisplayField("title");  
 //		searchBox.setName("id");  
 //		searchBox.setValueField("id");  
 //		searchBox.setForceSelection(true);  
 //		searchBox.setStore(searchStore);  
 //		searchBox.setTriggerAction(TriggerAction.ALL);
 	
 		toolBar.add(prevPageButton);
 		toolBar.add(nextPageButton); 
 		
 		toolBar.add(new SeparatorToolItem());
 		
 		toolBar.add(parentButton);
 		toolBar.add(childrenButton);
 		toolBar.add(relatedButton);
 
 		toolBar.add(new FillToolItem());
 		
 		toolBar.add(indexButton);
 		
 		toolBar.add(new SeparatorToolItem());
 		
 		toolBar.add(searchBox);
 		
 		return toolBar;
 	}
 	
 	protected ComboBox<ModelData> getFilterBox() {
 
 		PagingLoader<PagingLoadResult<FilterWordInstance>> loader = getWordLoader(); 
 		
 		ListStore<ModelData> wordStore = new ListStore<ModelData>(loader);  
 		
 		ComboBox<ModelData> combo = new ComboBox<ModelData>();  
 		combo.setWidth(250); 
 		combo.setDisplayField("word");  
 		combo.setEmptyText("Enter search criteria here...");
 		combo.setStore(wordStore);
 		combo.setMinChars(1);
 		combo.setHideTrigger(true);  
 		combo.setPageSize(10);
 		combo.setAllowBlank(true);
 		combo.setEditable(true);
 //		combo.setTypeAhead(true);
 		
 //		addComboListeners();			// This method sends messages by listening for keypresses
 		
 		setFilterListenTimer(combo);	// This method sends messages using a timer... it is less responsive, but so bothers the server less, and is a little more reliable
 		
 		return combo;
 	}
 	
 	protected void setFilterListenTimer(final ComboBox<ModelData> combo) {
 		filterListenTimer = new Timer() {
 			  @Override
 			  public void run() {
 				  String value = (combo.getRawValue() == null)?"":combo.getRawValue().trim();
 				  if (value.length() == 0) {
 					  // If no search terms, flip back to the content card
 					  if (cards.getActiveItem() == searchCard) {
 						  cards.setActiveItem(textCard);
 					  }
 				  } else if (!value.equals(filter)) {
 					  if (!value.equals(filter.trim()))
 						  doSearch(combo.getRawValue());
 					  // else do nothing, the filter hasn't changed
 				  }
 			  }
 			};
 
 			filterListenTimer.scheduleRepeating(200);
 	}
 	
 	protected void doSearch(String filter) {
 		this.filter = filter;
 		if (this.filter.length() > 2) {
 			addSearchResultsGrid();
 			searchLoader.load(0, 200);
 			cards.setActiveItem(searchCard);
 		}
 	}
 	
 //	/**
 //	 * 
 //	 * @return
 //	 */
 //	protected ListStore<ModelData> getSearchStore() {
 //	
 //		List<ModelData> list = new ArrayList<ModelData>();
 //		ModelData model = new BaseModelData(); model.set("id", "X"); model.set("title", "Mr. X"); list.add(model);
 //		model = new BaseModelData(); model.set("id", "Y"); model.set("title", "Mr. Y"); list.add(model);
 //		model = new BaseModelData(); model.set("id", "Z"); model.set("title", "Mr. Z"); list.add(model);
 //		ListStore<ModelData> searchStore = new ListStore<ModelData>();  
 //		searchStore.add(list);
 //		
 //		return searchStore;
 //	}
 	
 	/**
 	 * Create the history navigation toolbar.
 	 * @return
 	 */
 	protected ToolBar getHistoryToolBar() {
 		ToolBar toolBar = new ToolBar();
 		toolBar.setAlignment(HorizontalAlignment.CENTER);
 		toolBar.setSpacing(15);
 		
 		histFirstButton = new Button("First", new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				jumpTo(0);
 			}
 		});
 		histFirstButton.disable();
 		IconSupplier.forceIcon(histFirstButton, IconSupplier.getFirstIconName());
 		
 		histBackButton = new SplitButton("Back", new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				jumpTo(histIndex - 1);
 			}
 		});
 		histBackButton.disable();
 		IconSupplier.forceIcon(histBackButton, IconSupplier.getBackwardIconName());
 		histBackMenu = new Menu();
 		histBackMenu.setMaxHeight(200);
 		histBackButton.setMenu(histBackMenu);
 		
 		histForwButton = new SplitButton("Forward", new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				jumpTo(histIndex + 1);
 			}
 		});
 		histForwButton.disable();
 		IconSupplier.forceIcon(histForwButton, IconSupplier.getForwardIconName());
 		histForwMenu = new Menu();
 		histForwMenu.setMaxHeight(200);
 		histForwButton.setMenu(histForwMenu);
 		
 		histLastButton = new Button("Last", new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				jumpTo(historyIds.size() - 1);
 			}
 		});
 		histLastButton.disable();
 		IconSupplier.forceIcon(histLastButton, IconSupplier.getLastIconName());
 		
 		toolBar.add(histFirstButton);
 		toolBar.add(histBackButton);
 		toolBar.add(histForwButton);
 		toolBar.add(histLastButton);
 		
 		return toolBar;
 	}
 	
 	/**
 	 * Show the dialog.
 	 */
 	public void show(boolean animate) {
 		super.show(animate && EFFECT_ON);
 	}
 	
 	/**
 	 * Load a particular page by ID, possibly already at a particular location in history
 	 * @param id
 	 * @param histIndex
 	 */
 	protected void jumpTo(String id, int histIndex) {
 		if (id != null && id.length() > 0) {
 			loadHelpText(id, histIndex);
 		}
 	}
 	
 	/**
 	 * Jump to a point in history, by index
 	 * @param histIndex
 	 */
 	protected void jumpTo(int histIndex) {
 		// If this index is displayed, re-show the text
 		if (cards.getActiveItem() == indexCard) {
 			cards.setActiveItem(textCard);
 			//	If this index was displayed, and we're not going back to the beginning of a history chain (i.e. a "first") then just stay where we are
 			//		(which is the same as "going back from the index")
 			if (histIndex > 0 || historyIds.size() <= 1) {
 				adjustHistoryNavigation();
 				return;
 			}
 		}
 		if (histIndex >= 0 && histIndex < historyIds.size())
 			jumpTo(historyIds.get(histIndex), histIndex);
 	}
 	
 	/**
 	 * Jump to a new page, by ID
 	 */
 	public void jumpTo(String id) {
 		jumpTo(id, -1);
 	}
 	
 	/**
 	 * This is a new page load, to be added to history.
 	 * @param helpText
 	 */
 	protected void addToHistory(HelpTextInstance helpText) {
 		//	First, erase history beyond the current pointer
 		for (int removeIndex = historyIds.size() - 1; removeIndex > histIndex; removeIndex--) {
 			historyIds.remove(removeIndex);
 			historyTitles.remove(removeIndex);
 			historyIconNames.remove(removeIndex);
 		}
 		//	Then add this to the top of history, and set the pointer there
 		historyIds.add(helpText.getId());
 		historyTitles.add(helpText.getTitle());
 		historyIconNames.add(helpText.getIconName());
 		histIndex = historyIds.size() - 1;
 		//	Now adjust the navigation state
 		adjustHistoryNavigation();
 	}
 	
 	/**
 	 * This is a history page load, so just set the index and adjust the controls.
 	 * @param histIndex
 	 */
 	protected void setHistIndex(int histIndex) {
 		this.histIndex = histIndex;
 		adjustHistoryNavigation();
 	}
 	
 	/**
 	 * Adjust the history navigation buttons to match the state of the history and the current position.
 	 */
 	protected void adjustHistoryNavigation() {
 		if (histIndex + 1 < historyIds.size()) {
 			histForwButton.enable();
 			IconSupplier.forceIcon(histForwButton, IconSupplier.getForwardIconName());
 			histLastButton.enable();
 			IconSupplier.forceIcon(histLastButton, IconSupplier.getLastIconName());
 			histForwMenu.removeAll();
 			for (int i = histIndex + 1; i < historyIds.size(); i++) {
 				MenuItem item = new JumpMenuItem(i);
 				histForwMenu.add(item);
 			}
 		} else {
 			histForwButton.disable();
 			IconSupplier.forceIcon(histForwButton, IconSupplier.getForwardIconName());
 			histLastButton.disable();
 			IconSupplier.forceIcon(histLastButton, IconSupplier.getLastIconName());
 			histForwMenu.removeAll();
 		}
 
 		if (histIndex > 0 && historyIds.size() > 0) {
 			histBackButton.enable();
 			IconSupplier.forceIcon(histBackButton, IconSupplier.getBackwardIconName());
 			histFirstButton.enable();
 			IconSupplier.forceIcon(histFirstButton, IconSupplier.getFirstIconName());
 			histBackMenu.removeAll();
 			for (int i = histIndex - 1; i >= 0; i--) {
 				MenuItem item = new JumpMenuItem(i);
 				histBackMenu.add(item);
 			}
 		} else {
 			histBackButton.disable();
 			IconSupplier.forceIcon(histBackButton, IconSupplier.getBackwardIconName());
 			histFirstButton.disable();
 			IconSupplier.forceIcon(histFirstButton, IconSupplier.getFirstIconName());
 			histBackMenu.removeAll();
 		}
 	}
 
 	protected void loadHelpText(final String id, final int histIndex) {
 	//	System.out.println("Before update: " + targetBeanModel.getProperties());
 		helpTextService.getHelpText(id,
 				new AsyncCallback<HelpTextInstance>() {
 					public void onFailure(Throwable caught) {
 						setHelpText(null);
 						// Show the RPC error message to the user
 						if (caught instanceof ServiceNotReadyException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Help text access failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 					}
 
 					public void onSuccess(HelpTextInstance helpText) {
 						setHelpText(helpText);
 						if (histIndex < 0)
 							addToHistory(helpText);
 						else
 							setHistIndex(histIndex);
 					}
 			});
 	}
 	
 	/**
 	 * Format a page of Help Text
 	 */
 	private void formatPage() {
 		if (helpText == null) {
 			formatBlankPage();
 			return;
 		}
 		
 		// Set the heading
 		this.setHeading(helpText.getTitle());
 		if (helpText.getIconName() != null && helpText.getIconName().length() > 0)
 			IconSupplier.setIcon(this, helpText.getIconName());
 		else
 			IconSupplier.setIcon(this, IconSupplier.getHelpIconName());
 		
 		// Set the text
 		StringBuffer sb = new StringBuffer();
 		sb.append(helpText.getText());
 		text.setHtml(sb.toString());
 		
 		//	Set navigation buttons
 		prevPageButton.setJump(helpText.getPrevSiblingId(), helpText.getPrevSiblingTitle(),		helpText.getPrevSiblingIconName());
 		nextPageButton.setJump(helpText.getNextSiblingId(), helpText.getNextSiblingTitle(),		helpText.getNextSiblingIconName());
 		parentButton.setJump(helpText.getParentId(),      	helpText.getParentTitle(),			helpText.getParentIconName());
 		
 		//	Set children menu
 		if (helpText.getChildIdCount() == 0) {
 			childrenButton.disable();
 			IconSupplier.forceIcon(childrenButton, IconSupplier.getHelpMoreIconName());	//	To switch icon for enabled/disabled
 			childrenMenu.removeAll();
 		} else {
 			childrenButton.enable();
 			IconSupplier.forceIcon(childrenButton, IconSupplier.getHelpMoreIconName());	//	To switch icon for enabled/disabled
 			childrenMenu.removeAll();
 			for (int i = 0; i < helpText.getChildIdCount(); i++) {
 				MenuItem item = new JumpMenuItem(helpText.extractChildId(i), helpText.extractChildTitle(i), helpText.extractChildIconName(i));
 				childrenMenu.add(item);
 			}
 		}
 		
 		//	Set related menu
 		if (helpText.getRelatedIdCount() == 0) {
 			relatedButton.disable();
 			IconSupplier.forceIcon(relatedButton, IconSupplier.getHelpRelatedIconName());	//	To switch icon for enabled/disabled
 			relatedMenu.removeAll();
 		} else {
 			relatedButton.enable();
 			IconSupplier.forceIcon(relatedButton, IconSupplier.getHelpRelatedIconName());	//	To switch icon for enabled/disabled
 			relatedMenu.removeAll();
 			for (int i = 0; i < helpText.getRelatedIdCount(); i++) {
 				final MenuItem item = new JumpMenuItem(helpText.extractRelatedId(i), helpText.extractRelatedTitle(i), helpText.extractRelatedIconName(i));
 				relatedMenu.add(item);
 			}
 		}
 		
 		//	Make sure the text is displayed (in case another card is active)
 		if (cards.getActiveItem() != textCard) {
 			cards.setActiveItem(textCard);
 		}
 	}
 	
 	/**
 	 * Format a blank page... used when no Help Text is unexpectedly returned.
 	 */
 	protected void formatBlankPage() {
 		text.setHtml("<em>This page intentionally left blank.</em>");
 		
 		if (cards.getActiveItem() != textCard) {
 			cards.setActiveItem(textCard);
 		}
 	}
 	
 	/**
 	 * Show the index.
 	 */
 	protected void formatIndex() {
 		//	If the index hasn't been shown yet, create it
 		if (indexCard == null) {
 			indexTree = HelpIndexTree.getTreePanel(this);
 			indexCard = new CardPanel();
 			indexCard.setLayout(new FlowLayout(20));
 			indexCard.setStyleAttribute("overflow", "auto");
 			indexCard.add(indexTree);
 			content.add(indexCard);
 		}
 		//	Make the index active
 		cards.setActiveItem(indexCard);
 		content.layout(true);
 		//	Set the nav buttons for going back to the last page (or first page)
 		histFirstButton.enable();
 		IconSupplier.forceIcon(histFirstButton,IconSupplier.getFirstIconName());
 		histBackButton.enable();
 		IconSupplier.forceIcon(histBackButton, IconSupplier.getBackwardIconName());
 	}
 	
 	/**
 	 * Construct and return a loader to return a list of words.
 	 * 
 	 * @return
 	 */
 	protected PagingLoader<PagingLoadResult<FilterWordInstance>> getWordLoader() {
 		// proxy and reader  
 		RpcProxy<PagingLoadResult<FilterWordInstance>> proxy = new RpcProxy<PagingLoadResult<FilterWordInstance>>() {  
 			@Override  
 			public void load(Object loadConfig, final AsyncCallback<PagingLoadResult<FilterWordInstance>> callback) {
 		    	
 				// This could be as simple as calling userListService.getUsers and passing the callback
 				// Instead, here the callback is overridden so that it can catch errors and alert the users.  Then the original callback is told of the failure.
 				// On success, the original callback is just passed the onSuccess message, and the response (the list).
 				
 				AsyncCallback<PagingLoadResult<FilterWordInstance>> myCallback = new AsyncCallback<PagingLoadResult<FilterWordInstance>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else if (caught instanceof ServiceNotReadyException)
 								MessageBox.alert("Alert", "The " + caught.getMessage() + " is not available at this time.  Please try again in a few minutes.", null);
 						else {
 							MessageBox.alert("Alert", "Word load failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 						callback.onFailure(caught);
 					}
 
 					public void onSuccess(PagingLoadResult<FilterWordInstance> result) {
 						callback.onSuccess(result);
 					}
 				};
 
 				helpTextWordService.getHelpTextWords((PagingLoadConfig) loadConfig, myCallback);
 				
 		    }  
 		};
 		BeanModelReader reader = new BeanModelReader();
 		
 		// loader and store  
 		PagingLoader<PagingLoadResult<FilterWordInstance>> loader = new BasePagingLoader<PagingLoadResult<FilterWordInstance>>(proxy, reader);
 		return loader;
 	}
 	
 	/**
 	 * Set the help text and format the page to contain it.
 	 * @param helpText
 	 */
 	public void setHelpText(HelpTextInstance helpText) {
 		if(helpText != null)
 			this.helpTextId = helpText.getId();	//	Can't use the setter here of all places, because it will trigger an infinite loop of reloads
 		this.helpText = helpText;
 		formatPage();
 	}
 
 	public String getHelpTextId() {
 		return helpTextId;
 	}
 
 	/**
 	 * Set the help text to a particular ID (loading and displaying the page for that ID).
 	 * @param helpTextId
 	 */
 	public void setHelpTextId(String helpTextId) {
 		this.helpTextId = helpTextId;
 		loadHelpText(helpTextId, -1);
 	}
 
 	public HelpTextInstance getHelpText() {
 		return helpText;
 	}
 
 	
 	/**
 	 * Construct and return a loader to handle returning a list of institutions.
 	 * @return
 	 */
 	protected PagingLoader<PagingLoadResult<SearchResultInstance>> getSearchLoader() {
 		// proxy and reader  
 		RpcProxy<PagingLoadResult<SearchResultInstance>> proxy = new RpcProxy<PagingLoadResult<SearchResultInstance>>() {  
 			@Override  
 			public void load(Object loadConfig, final AsyncCallback<PagingLoadResult<SearchResultInstance>> callback) {
 		    	
 				// This could be as simple as calling userListService.getUsers and passing the callback
 				// Instead, here the callback is overridden so that it can catch errors and alert the users.  Then the original callback is told of the failure.
 				// On success, the original callback is just passed the onSuccess message, and the response (the list).
 				
 				AsyncCallback<PagingLoadResult<SearchResultInstance>> myCallback = new AsyncCallback<PagingLoadResult<SearchResultInstance>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else if (caught instanceof ServiceNotReadyException)
 								MessageBox.alert("Alert", "The " + caught.getMessage() + " is not available at this time.  Please try again in a few minutes.", null);
 						else {
 							MessageBox.alert("Alert", "Help text search failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 						callback.onFailure(caught);
 					}
 
 					public void onSuccess(PagingLoadResult<SearchResultInstance> result) {
 						if ( result == null || result.getData() == null || result.getData().size() == 0 ) {
 							liveView.setEmptyText("No pages were found referencing your criteria.");
 						}
 						callback.onSuccess(result);
 					}
 				};
 
 				helpTextSearchService.searchHelpText((PagingLoadConfig) loadConfig, filter, myCallback);
 				
 		    }  
 		};
 		BeanModelReader reader = new BeanModelReader();
 		
 		// loader and store  
 		PagingLoader<PagingLoadResult<SearchResultInstance>> loader = new BasePagingLoader<PagingLoadResult<SearchResultInstance>>(proxy, reader);
 		return loader;
 	}
 	
 	protected void createSearchLoader() {
 		if (searchLoader != null)
 			return;
 		
 		searchLoader = getSearchLoader();
 		searchLoader.setSortDir(SortDir.DESC);  
 		searchLoader.setSortField("score");  
 		searchLoader.setRemoteSort(true);
 
 		searchStore = new ListStore<ModelData>(searchLoader);  
 	}
 	
 	protected void addSearchResultsGrid() {
 		if (searchResultsGrid != null)
 			return;
 		
 		createSearchLoader();
  
 		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();  
 
 		ColumnConfig id	  = new ColumnConfig("id", "ID", 10);
 		id.setHidden(true);
 		columns.add(id);
 		
 		ColumnConfig title = new ColumnConfig("title", "Title", 150);  
 		title.setRenderer(new GridCellRenderer<ModelData>() {
 
 		  public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,  
 		      ListStore<ModelData> store, Grid<ModelData> grid) {  
 		    return "<b>"  
 		        + model.get("title")  
 		        + "</b>";  
 		  }  
 
 		});  
 		columns.add(title);  
 		columns.add(new ColumnConfig("text",	"Text", 350));  
 //		ColumnConfig score = new ColumnConfig("score100",	"Score", 50);
 //		score.setNumberFormat(NumberFormat.getFormat("####"));
 //		columns.add(score); 
 
 		ColumnModel cm = new ColumnModel(columns);
 
 		searchResultsGrid = new Grid<ModelData>(searchStore, cm);  
 		searchResultsGrid.setBorders(true);
 		searchResultsGrid.setAutoWidth(true);
 		searchResultsGrid.setAutoExpandColumn("text");  
 		searchResultsGrid.setLoadMask(true);  
 		searchResultsGrid.setStripeRows(true);
 		searchResultsGrid.setColumnReordering(false);
 		searchResultsGrid.setColumnLines(false);
 		searchResultsGrid.setHideHeaders(true);
 		searchResultsGrid.setSelectionModel(new SearchGridSelectionModel(this));
 
 		liveView = new LiveGridView();  
 		liveView.setEmptyText("Enter filter criteria to search for help entries.");
 		searchResultsGrid.setView(liveView);
 		searchResultsGrid.getAriaSupport().setLabelledBy(this.getHeader().getId() + "-label");
 		
 		// This is necessary because the grid doesn't size itself properly to start (in order to get scroll bars)
 		final ContentPanel container = this;
 		searchToolBar = new ToolBar() {
 			@Override
 			public void onRender(Element el, int index) {
 				super.onRender(el, index);
 				setSearchGridSize(container.getInnerWidth());
 			}
 		};
 		searchToolBar.setAlignment(HorizontalAlignment.LEFT);
 		searchToolBar.getAriaSupport().setLabel("Search Results");
 
 		LiveToolItem item = new LiveToolItem();  
 		item.bindGrid(searchResultsGrid);
 		searchToolBar.add(item);
 		
 		searchCard = new CardPanel();
 		searchGridContainer = new ContentPanel();
 		searchGridContainer.setHeaderVisible(false);
 		searchGridContainer.add(searchResultsGrid);
 		searchGridContainer.setTopComponent(searchToolBar);
 		searchCard.add(searchGridContainer);
 		
 		content.add(searchCard);
 	}
 	public int getHideAnimateTime() {
 		return 500;
 	}
 	
 	public int getShowAnimateTime() {
 		return 500;
 	}
 }
