 package com.merap.promoren.component.officemenu;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.vaadin.server.Resource;
 import com.vaadin.server.ThemeResource;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.CssLayout;
 import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ChameleonTheme;
 
 public class OfficeMenu extends CssLayout {
 
 	private static final long serialVersionUID = -3411790058279262819L;
 
 	private static final String STYLE_SELECTED = "selected";
 
 	static final String HEIGHT_CLOSED = "20px";
 	static final String HEIGHT_SECTIONS = "100px";
 	static final String HEIGHT_OPEN = "120px";
 
 	private final Resource ARROW_UP = new ThemeResource("images/arrow-up.png");
 	private final Resource ARROW_DOWN = new ThemeResource(
 			"images/arrow-down.png");
 
 	private final CssLayout captions = new CssLayout();
 	private final Panel ribbonPanel = new Panel();
 
 	private final Map<String, MenuTab> tabs = new HashMap<String, MenuTab>();
 	private final Map<String, Button> tabCaptions = new HashMap<String, Button>();
 
 	private final Button closeButton;
 	private boolean open = false;
 
 	public OfficeMenu() {
 		super();
 
 		setWidth("100%");
 		addStyleName("officemenu");
 		captions.addStyleName("tabcaptions");
 
 		captions.setWidth("100%");
 		ribbonPanel.setWidth("100%");
 
 		addComponent(captions);
 		addComponent(ribbonPanel);
 
 		closeButton = new Button();
 		closeButton.addStyleName("headerclose");
 		closeButton.addStyleName(ChameleonTheme.BUTTON_BORDERLESS);
 		closeButton.addClickListener(closeButtonListener);
 		close();

 		captions.addComponent(closeButton);
		// captions.setExpandRatio(closeButton, 1);
		// captions.setComponentAlignment(closeButton, Alignment.MIDDLE_RIGHT);
 	}
 
 	/**
 	 * Sets the ribbon visibility
 	 */
 	protected void showTabs(boolean show) {
 		ribbonPanel.setVisible(show);
 	}
 
 	/**
 	 * Opens the ribbons and changes close button icon
 	 */
 	protected void open() {
 		showTabs(true);
 		open = true;
 		closeButton.setIcon(ARROW_DOWN);
 	}
 
 	/**
 	 * Closes the ribbons and changes close button icon
 	 */
 	protected void close() {
 		showTabs(false);
 		open = false;
 		closeButton.setIcon(ARROW_UP);
 	}
 
 	/**
 	 * Adds a new {@link MenuSection} to the selected ribbon. If the ribbon
 	 * doesn't exist, it is created.
 	 * 
 	 * @param tabCaption
 	 * @param desc
 	 * @return
 	 */
 	public MenuSection addSection(String tabCaption, String desc) {
 
 		MenuTab tab = tabs.get(tabCaption);
 		if (tab == null) {
 			tab = addTab(tabCaption);
 		}
 
 		MenuSection section = tab.addSection(desc);
 
 		return section;
 	}
 
 	/**
 	 * Adds a menu item that has only a {@link MenuCommand} instead of sub menu
 	 * items. Clicking on this will not change the selected ribbon.
 	 * 
 	 * @param tabCaption
 	 * @param icon
 	 * @param command
 	 */
 	public Button addEmptySection(String tabCaption, Resource icon,
 			final MenuCommand command) {
 		Button caption = new Button(tabCaption);
 		caption.addStyleName("tabcaption");
 		caption.addStyleName("empty");
 		tabCaptions.put(tabCaption, caption);
 
 		captions.addComponent(caption, captions.getComponentCount() - 1);
 
 		caption.addClickListener(new ClickListener() {
 
 			private static final long serialVersionUID = -615975673819020510L;
 
 			@Override
 			public void buttonClick(ClickEvent event) {
 				if (command != null) {
 					command.menuItemClicked(null);
 				}
 			}
 		});
 
 		return caption;
 	}
 
 	/**
 	 * Adds a new menu ribbon that can hold {@link MenuSection}s.
 	 * 
 	 * @param tabCaption
 	 * @param icon
 	 * @param select
 	 * @return
 	 */
 	public MenuTab addTab(String tabCaption, Resource icon, boolean select) {
 		MenuTab tab = new MenuTab();
 		tabs.put(tabCaption, tab);
 
 		Button caption = new Button(tabCaption);
 		caption.addStyleName("tabcaption");
 		tabCaptions.put(tabCaption, caption);
 
 		captions.addComponent(caption, captions.getComponentCount() - 1);
 
 		caption.addClickListener(tabChangeListener);
 
 		if (select) {
 			caption.click();
 		}
 
 		return tab;
 	}
 
 	/**
 	 * @see OfficeMenu#addTab(String, Resource, boolean)
 	 */
 	public MenuTab addTab(String tabCaption, Resource icon) {
 		return addTab(tabCaption, icon, false);
 	}
 
 	/**
 	 * @see OfficeMenu#addTab(String, Resource, boolean)
 	 */
 	public MenuTab addTab(String tabCaption) {
 		return addTab(tabCaption, null);
 	}
 
 	/**
 	 * Listener for upper row of buttons, to change ribbons
 	 */
 	private final ClickListener tabChangeListener = new ClickListener() {
 
 		private static final long serialVersionUID = 4397027389196697692L;
 
 		@Override
 		public void buttonClick(ClickEvent event) {
 			String tabCaption = event.getButton().getCaption();
 
 			ribbonPanel.setContent(tabs.get(tabCaption));
 
 			// show ribbon but don't change close button icon
 			showTabs(true);

			for (Button tab : tabCaptions.values()) {
				tab.removeStyleName(STYLE_SELECTED);
			}
			event.getButton().addStyleName(STYLE_SELECTED);
 		}
 	};
 
 	/**
 	 * Listener for the close button on the upper right
 	 */
 	private final ClickListener closeButtonListener = new ClickListener() {
 
 		private static final long serialVersionUID = -933901482894951128L;
 
 		@Override
 		public void buttonClick(ClickEvent event) {
 
 			if (open) {
 				close();
 			} else {
 				open();
 			}
 		}
 	};
 
 }
