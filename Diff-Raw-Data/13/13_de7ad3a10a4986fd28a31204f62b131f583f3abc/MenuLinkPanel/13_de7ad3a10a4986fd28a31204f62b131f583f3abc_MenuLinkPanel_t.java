 /*
  * This piece of code is dedicated to the wicket project (http://www.wicketframework.org).
  */
 package wicket.extensions.markup.html.menubar;
 
 
 import wicket.MarkupContainer;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.ajax.markup.html.AjaxFallbackLink;
 import wicket.markup.html.basic.Label;
 import wicket.markup.html.link.ExternalLink;
 import wicket.markup.html.link.Link;
 import wicket.markup.html.link.PageLink;
 import wicket.markup.html.link.PopupSettings;
 import wicket.markup.html.panel.Panel;
 
 
 /**
  * This is a internal class. It is responsible for rendering a visible {@link MenuItem}, it's link and the associated label.
  *
  * @author Stefan Lindner (lindner@visionet.de)
  */
 class MenuLinkPanel extends Panel<String> {
 
 	private static final long serialVersionUID = 1L;
 
 	protected MenuLinkPanel(MarkupContainer parent, final String id, final MenuItem menuItem) {
 		super(parent, id);
 		if (menuItem == null) {
 			throw new IllegalArgumentException("argument [menuItem] cannot be null");
 		}
 
 		switch (menuItem.getMenuItemType()) {
 			case PAGE_LINK:
 				PageLink pageLink = new PageLink(this, "link", menuItem.getPageLink());
 				new Label(pageLink, "linkLabel", menuItem.getModel().getObject()).setRenderBodyOnly(true);
 				break;
 			case LINK_LISTENER:
 				Link<String> linkListener = new Link<String>(this, "link", menuItem.getModel()) {
 					private static final long serialVersionUID = 1L;
 					public void onClick() {
 						menuItem.getLinkListener().onLinkClicked();
 					}
 				};
 				new Label(linkListener, "linkLabel", menuItem.getModel().getObject()).setRenderBodyOnly(true);
 				break;
 			case EXTERNAL_URL:
 				PopupSettings popupSettings = new PopupSettings(PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | PopupSettings.MENU_BAR | PopupSettings.STATUS_BAR);
 				ExternalLink externalLink = new ExternalLink(this, "link", menuItem.getExternalUrl()).setPopupSettings(popupSettings);
 				new Label(externalLink, "linkLabel", menuItem.getModel().getObject()).setRenderBodyOnly(true);
 				break;
              case IMENULINKCALLBACK:
                     AjaxFallbackLink<String> menuCallBack = new AjaxFallbackLink<String>(this, "link", menuItem.getModel()) {
 
						private static final long serialVersionUID = 1L;

						@Override
                         public void onClick(AjaxRequestTarget target)
                         {
                             menuItem.getMenuLinkCallback().onClick(target);                        
                         }
                         
                     };
                     new Label(menuCallBack, "linkLabel", menuItem.getModel().getObject()).setRenderBodyOnly(true);
                     break;
 		}
 		this.setRenderBodyOnly(true);
 	}
 
 }
