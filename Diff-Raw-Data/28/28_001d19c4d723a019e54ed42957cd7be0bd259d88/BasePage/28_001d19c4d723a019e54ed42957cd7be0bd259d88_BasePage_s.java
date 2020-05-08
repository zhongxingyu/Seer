 package org.apache.wicket.examples.yatzy.frontend.pages;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.examples.yatzy.frontend.behaviours.ajax.timer.CompoundAjaxTimerBehavior;
 import org.apache.wicket.examples.yatzy.frontend.panels.BookmarkableMenuItem;
 import org.apache.wicket.examples.yatzy.frontend.panels.IMenuItem;
 import org.apache.wicket.examples.yatzy.frontend.panels.MenuPanel.MenuBlock;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.model.StringResourceModel;
 import org.apache.wicket.util.time.Duration;
 
 public abstract class BasePage<T> extends WebPage<T> {
 
 	public static interface ILeftMenuBlock extends Serializable {
 		Component<?> createMenuBlock(String wicketId);
 	}
 
 	private CompoundAjaxTimerBehavior timerBehavior;
 
 	public BasePage() {
 		this(null);
 	}
 
 	public BasePage(IModel<T> model) {
 		super(model);
 
 		timerBehavior = new CompoundAjaxTimerBehavior(Duration.ONE_SECOND);
 		add(timerBehavior);
 
		add(new ListView<ILeftMenuBlock>("blocks", new PropertyModel<List<ILeftMenuBlock>>(this,
				"leftMenuBlocks")) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(ListItem<ILeftMenuBlock> item) {
 				item.setRenderBodyOnly(true);
 
 				ILeftMenuBlock block = item.getModelObject();
 				item.add(block.createMenuBlock("block"));
 			}
		});
 
 		add(new Label<String>("pageTitle", getPageTitleModel()));
 	}
 
 	protected abstract IModel<String> getPageTitleModel();
 
 	public List<ILeftMenuBlock> getLeftMenuBlocks() {
 		List<ILeftMenuBlock> blocks = new ArrayList<ILeftMenuBlock>();
 		blocks.add(new MenuBlock(new PropertyModel<List<IMenuItem>>(this, "menuItems")));
 		return blocks;
 	}
 
 	public List<IMenuItem> getMenuItems() {
 		List<IMenuItem> menuItems = new ArrayList<IMenuItem>();
 
 		addMenuItems(menuItems);
 
 		menuItems.add(new BookmarkableMenuItem(new StringResourceModel("highscore", this, null),
 				HighscorePage.class));
 
 		return menuItems;
 	}
 
 	protected void addMenuItems(List<IMenuItem> menuItems) {
 	}
 
 	protected CompoundAjaxTimerBehavior getTimerBehavior() {
 		return timerBehavior;
 	}
 
 }
