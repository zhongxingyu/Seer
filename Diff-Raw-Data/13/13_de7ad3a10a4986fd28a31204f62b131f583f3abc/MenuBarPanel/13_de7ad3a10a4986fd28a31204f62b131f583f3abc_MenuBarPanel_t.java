 /*
  * This piece of code is dedicated to the wicket project (http://www.wicketframework.org).
  */
 package wicket.extensions.markup.html.menubar;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import wicket.AttributeModifier;
 import wicket.MarkupContainer;
 import wicket.ResourceReference;
 import wicket.behavior.HeaderContributor;
 import wicket.markup.html.WebMarkupContainer;
 import wicket.markup.html.basic.Label;
 import wicket.markup.html.list.Loop;
 import wicket.markup.html.panel.Panel;
 import wicket.model.Model;
 import wicket.util.resource.TextTemplateHeaderContributor;
 
 
 /**
  * A {@code MenuBarPanel} consits of several {@link Menu}s where each {@link Menu} consist of {@link MenuItem}s.
  * A {@code MenuBarPanel} can be constructed e.g. like this:
 <pre>
 	List&lt;Menu&gt; menus = new ArrayList&lt;Menu&gt;();
 
 	// The MenuItems for the first Menu
 	List&lt;MenuItem&gt; menu1Items = new ArrayList&lt;MenuItem&gt;();
 
 	menu1Items.add(new MenuItem(new Model&lt;String&gt;("Visible menu item 1.1"), new LinkToPage(Page1.class)));
 	menu1Items.add(new MenuItem(new Model&lt;String&gt;("Visible but disabled menu item 1.2"), new LinkToPage(Page2.class)).setEnabled(false));
 	menu1Items.add(new MenuItem(new Model&lt;String&gt;("Invisible menu item 1.3"), new LinkToPage(Page3.class)).setVisible(false));
 	menu1Items.add(new MenuItem(new Model&lt;String&gt;("visible menu item 1.4"), new LinkToPage(Page4.class)));
 
 	menus.add(new Menu(new Model&lt;String&gt;("First menu"), menu1Items));
 
 	// The MenuItems for the second Menu
 	List&lt;MenuItem&gt; menu2Items = new ArrayList&lt;MenuItem&gt;();
 
 	menu2Items.add(new MenuItem(new Model&lt;String&gt;("Visible menu item 2.1"), "http://www.wicketframework.org"));
 	menu2Items.add(new MenuItem(new Model&lt;String&gt;("Visible but disabled menu item 2.2"), new LinkToPage(Page1.class)).setEnabled(false));
 	menu2Items.add(new MenuItem(new Model&lt;String&gt;("Invisible menu item 2.3"), new LinkToPage(null, Page2.class)).setVisible(false));
 	menu2Items.add(new MenuItem(new Model&lt;String&gt;("visible menu item 2.4"), new LinkToPage(Page3.class)));
 
 	menus.add(new Menu(new Model&lt;String&gt;("Second menu"), menu2Items));
 
 	new MenuBarPanel(this, "menuBar", menus);
 
 	...
 
 	private class LinkToPage implements IPageLink {
 
 		private Class&lt;? extends Page&gt; pageClass;
 
 		public LinkToPage(Class&lt;? extends Page&gt; pageClass) {
 			this.pageClass = pageClass;
 		}
 
 		public Page getPage() {
 			return pageClass.newInstance();
 		}
 </pre>
  * The HTML markup for this example looks like this:
 <pre>
 	&lt;div wicket:id="menuBar"&gt;[[The menu bar goes here]]&lt;/div&gt;
 </pre>
  * You need the following css code to get this example to work with IE:
 <pre>
 	.menubar h1 {
 		text-align: center;
 		padding: 0 0 0.25em 0;
 		margin: 0;
 		z-index: 999;
 	}
 
 	.menubar ul {
 		list-style: none;
 		padding: 0;
 		margin: 0;
 		z-index: 999;
 	}
 
 	#nav a {
 		font-weight: bold;
 		color: rgb(15,15,15);
 		text-align: left;
 		text-decoration: none;
 		text-align: left;
 	}
 
 	#nav li li a {
 		font-weight: bold;
 		text-align: left;
 		display: block;
 		color: rgb(41,41,41);
 		padding : 0.2em 10px;
 		white-space : nowrap;
 		z-index: 999;
 	}
 
 	#nav li li.disabled {
 		font-weight: bold;
 		text-align: left;
 		display: block;
 		padding : 0.2em 10px;
 		white-space : nowrap;
 		z-index: 999;
 	}
 
 	#nav li li a:hover {
 		padding: 0.2em 5px;
 		border: 1px solid rgb(34,69,104);
 		border-width: 0 5px;
 		text-align: left;
 	}
 
 	.menubar li {
 		float: left;
 		position: relative;
 		text-align: center;
 		cursor: default;
 		background-color: rgb(152,186,225);
 		border: 1px solid rgb(34,69,104);
 		border-width: 1px 0;
 		text-align: left;
 		z-index: 999;
 	}
 
 	.menubar li#singleMenu {
 		padding-left: 1em;
 		padding-right: 1em;
 		border-left-width: 2px;
 		border-right-width: 2px;
 		margin-right: 2px;
 		white-space : nowrap;
 	}
 
 	.menubar li#firstMenu {
 		padding-left: 1em;
 		padding-right: 1em;
 		border-left-width: 2px;
 		border-right-width: 2px;
 		margin-right: 2px;
 		white-space : nowrap;
 	}
 
 	.menubar li#middleMenu {
 		padding-left: 1em;
 		padding-right: 1em;
 		border-left-width: 2px;
 		border-right-width: 2px;
 		margin-right: 2px;
 		white-space : nowrap;
 	}
 
 	.menubar li#lastMenu {
 		padding-left: 1em;
 		padding-right: 1em;
 		border-left-width: 2px;
 		border-right-width: 2px;
 		white-space : nowrap;
 	}
 
 	.menubar li ul {
 		display: none;
 		position: absolute;
 		top: 100%;
 		left: 0;
 		font-weight: normal;
 		background-color: rgb(152,186,225);
 		padding: 0.5em 0;
 		border-right: ridge 1px rgb(125,125,125);
 		border-left: ridge 1px rgb(125,125,125);
 		border-bottom: ridge 1px rgb(125,125,125);
 		text-align: left;
 		z-index:99999;
 	}
 
 	.menubar li>ul {
 		top: auto;
 		left: auto;
 	}
 
 	.menubar li li {
 		display: block;
 		float: none;
 		background-color: rgb(152,186,225);
 		border: 0;
 		color: rgb(41,41,41);
 	}
 
 	.menubar li li.disabled {
 		display: block;
 		float: none;
 		background-color: rgb(152,186,225);
 		border: 0;
 		color: rgb(123,123,123);
 	}
 
 	.menubar li:hover ul, li.over ul {
 		display: block;
 	}
 
 	.menubar hr {
 		display: none;
 	}
 
 	.menubar p {
 		clear: left;
 		background: url(images/remora.gif) center left no-repeat;
 		padding: 1em 1em 0 1em;
 		margin: 0;
 	}
 
 	.menubar p.image {
 		float: right;
 		font-size: 0.8em;
 		text-align: center;
 		color: #7d6340;
 		padding: 1.25em 1.25em 0.25em 0.25em;
 	}
 
 	.menubar  p.image img {
 		display: block;
 		border: 1px solid #7d6340;
 	}
 </pre>
  *
  * @author Stefan Lindner (lindner@visionet.de)
  */
 public class MenuBarPanel extends Panel {
 
 	private static final long serialVersionUID = 1L;
 
 	private final List<Menu> menus;
     private final String name;
     
     private static final ResourceReference CSS = new ResourceReference(
             MenuBarPanel.class, "res/wicket-extensions-menubar.css");
 
     
     /**
      * Constructs a {@code MenuBarPanel} with the default "nav" id.
      *
      * @param parent The parent of this component.
      * @param id See {@link wicket.Component}
      * @param menus A {@code List} of the {@link Menu}s that belong to this {@code MenuBarPanel}.
      */
 	public MenuBarPanel(MarkupContainer parent, String id, final List<Menu> menus)
     {
         this(parent, id, menus, "nav");
     }
 
     /**
 	 * Constructs a {@code MenuBarPanel} with a unique id "name".
 	 *
 	 * @param parent The parent of this component.
 	 * @param id See {@link wicket.Component}
 	 * @param menus A {@code List} of the {@link Menu}s that belong to this {@code MenuBarPanel}.
      * @param name the html dom id of the menubar in the markup
 	 */
 	public MenuBarPanel(MarkupContainer parent, final String id, final List<Menu> menus, final String name) {
 		super(parent, id);
         this.menus = menus;
         this.name = name;
 		init();
 
 		WebMarkupContainer menuBar = new WebMarkupContainer(this, "menubar");
        menuBar.add(new AttributeModifier("id", true, new Model<String>(name)));
 		// add the loop used to generate each single menu
 		new Loop(menuBar, "menus", menus.size()) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(LoopItem item) {
 				final int index = item.getIteration();
 				final Menu menu = MenuBarPanel.this.menus.get(index);
 
 				if (menu.isVisible()) {
 					new Label(item, "menuTitle", menu.getModel()).setRenderBodyOnly(true);
 
 					MenuPanel mp = new MenuPanel(item, "menu", menu.getMenuItems());
 					mp.setRenderBodyOnly(true);
 					if (index == 0)
 						if (MenuBarPanel.this.menus.size() == 1)
 							// Only one menu
 							item.add(new AttributeModifier("id", true, new Model<String>("singleMenu")));
 						else
 							// More than one menu, this is the first one
 							item.add(new AttributeModifier("id", true, new Model<String>("firstMenu")));
 					else if (index == MenuBarPanel.this.menus.size() - 1)
 						// The last menu
 						item.add(new AttributeModifier("id", true, new Model<String>("lastMenu")));
 					else
 						item.add(new AttributeModifier("id", true, new Model<String>("middleMenu")));
 				}
 				else
 					item.setVisible(false);
 			}
 		};
 	}
     
     /**
      * Returns the resource reference of default stylesheet.
      * 
      * @return The package resource reference
      */
     protected ResourceReference getCSS()
     {
         return CSS;
     }
     
     private void init()
     {
         ResourceReference css = getCSS();
         add(HeaderContributor.forCss(css.getScope(), css.getName()));
         add(TextTemplateHeaderContributor.forJavaScript(MenuBarPanel.class, "res/menubar.js", 
                 new Model<Map>(Collections.singletonMap("name", name))));
     }
 
 }
