 package uk.ac.ox.oucs.oxam.pages;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.Page;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.feedback.FeedbackMessage;
 import org.apache.wicket.feedback.IFeedbackMessageFilter;
 import org.apache.wicket.markup.html.IHeaderContributor;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.link.PageLink;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.repeater.RepeatingView;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.ResourceModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import uk.ac.ox.oucs.oxam.logic.SakaiProxy;
 
 /**
  * This is our base page for our Sakai app. It sets up the containing markup and
  * top navigation. All top level pages should extend from this page so as to
  * keep the same navigation. The content for those pages will be rendered in the
  * main area below the top nav.
  * 
  * <p>
  * It also allows us to setup the API injection and any other common methods,
  * which are then made available in the other pages.
  * 
  * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
  * 
  */
 public class SakaiPage extends WebPage implements IHeaderContributor {
 
 	private static final Logger log = Logger.getLogger(SakaiPage.class);
 
 	@SpringBean(name = "sakaiProxy")
 	protected SakaiProxy sakaiProxy;
 
 	FeedbackPanel feedbackPanel;
 
 	// Any links to be added to the page.
 	private RepeatingView links;
 
 	public SakaiPage() {
 
		log.debug("SakaiPage()");
 
 		links = new RepeatingView("link");
 		add(links);
 
 		// Add a FeedbackPanel for displaying our messages
 		feedbackPanel = new FeedbackPanel("feedback") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected Component newMessageDisplayComponent(final String id,
 					final FeedbackMessage message) {
 				final Component newMessageDisplayComponent = super
 						.newMessageDisplayComponent(id, message);
 
 				if (message.getLevel() == FeedbackMessage.ERROR
 						|| message.getLevel() == FeedbackMessage.DEBUG
 						|| message.getLevel() == FeedbackMessage.FATAL
 						|| message.getLevel() == FeedbackMessage.WARNING) {
 					add(new SimpleAttributeModifier("class", "alertMessage"));
 				} else if (message.getLevel() == FeedbackMessage.INFO) {
 					add(new SimpleAttributeModifier("class", "success"));
 				}
 
 				return newMessageDisplayComponent;
 			}
 			
 			// If we don't link up visibility to having messages, then when changing the filter after displaying
 			// the message, the message disappears but they surrounding box remains.
 			public boolean isVisible() {
 				return anyMessage();
 			}
 		};
 		feedbackPanel.setFilter(new IFeedbackMessageFilter() {
 
 			private static final long serialVersionUID = 1L;
 
 			public boolean accept(FeedbackMessage message) {
 				return !message.isRendered();
 			}
 		});
 		add(feedbackPanel);
 
 	}
 
 	/**
 	 * Helper to clear the feedbackpanel display.
 	 * 
 	 * @param f
 	 *            FeedBackPanel
 	 */
 	public void clearFeedback(FeedbackPanel f) {
 		if (!f.hasFeedbackMessage()) {
 			f.add(new SimpleAttributeModifier("class", ""));
 		}
 	}
 
 	/**
 	 * This block adds the required wrapper markup to style it like a Sakai
 	 * tool. Add to this any additional CSS or JS references that you need.
 	 * 
 	 */
 	public void renderHead(IHeaderResponse response) {
 		// get Sakai skin
 		String skinRepo = sakaiProxy.getSkinRepoProperty();
 		String toolCSS = sakaiProxy.getToolSkinCSS(skinRepo);
 		String toolBaseCSS = skinRepo + "/tool_base.css";
 
 		// Sakai additions
 		response.renderJavascriptReference("/library/js/headscripts.js");
 		response.renderCSSReference(toolBaseCSS);
 		response.renderCSSReference(toolCSS);
 		response.renderOnLoadJavascript("\nif (typeof setMainFrameHeight !== 'undefined'){\nsetMainFrameHeight( window.name );\n}");
 
 		// Tool additions (at end so we can override if required)
 		response.renderString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
 		response.renderCSSReference(new ResourceReference(getClass(),"style.css"));
 		
 		// Need a resource reference so that we don't have to worry about path components.
 		response.renderJavascriptReference(new ResourceReference(getClass(), "jquery-1.7.1.min.js"));
 		response.renderJavascriptReference(new ResourceReference(getClass(), "script.js"));
 	}
 
 	/**
 	 * Helper to disable a link. Add the Sakai class 'current'.
 	 */
 	protected void disableLink(Link<Void> l) {
 		l.add(new AttributeAppender("class", new Model<String>("current"), " "));
 		l.setRenderBodyOnly(true);
 		l.setEnabled(false);
 	}
 
 	protected void addLink(Class<? extends Page> clazz, String title,
 			String tooltip) {
 		Link<Page> link = new BookmarkablePageLink<Page>("anchor", clazz);
 		link.setEnabled(!getClass().equals(clazz));
 		addLink(link, title, tooltip);
 	}
 	
 	protected void addLink(final Link<Page> link, String title,
 			String tooltip) {
 		WebMarkupContainer parent = new WebMarkupContainer(links.newChildId());
 		links.add(parent); 
 		link.add(new Label("label", new ResourceModel(title))
 				.setRenderBodyOnly(true));
 		if (tooltip != null) {
 			link.add(new AttributeModifier("title", true, new ResourceModel(
 					tooltip)));
 		};
 		parent.add(link);
 	}
 }
