 package com.reimDev.reimWeb.client.ui.pages;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.ParagraphElement;
 import com.google.gwt.dom.client.SpanElement;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.reimDev.reimWeb.client.entity.BlogEntry;
 import com.reimDev.reimWeb.client.service.BlogServiceAsync;
 import com.reimDev.reimWeb.client.service.util.ServiceHandler;
 import com.reimDev.reimWeb.client.ui.pages.util.PageBuilder;
 import com.reimDev.reimWeb.client.ui.pages.util.PageContainer;
 
 public class BlogPage extends AbstractPage {
 	private Logger logger = Logger.getLogger("Blog"); 
 	
	//TODO by start scan for historyToken
 	public BlogPage() {
 		final VerticalPanel pagePanel = new VerticalPanel();
 		initWidget(pagePanel);
 		
 		if(History.getToken().contains("entry")) {
 			String tokenId = History.getToken().substring(5);
			PageBuilder.setPage(new BlogPage(Integer.parseInt(tokenId)));
 		} else {			
 			pagePanel.setWidth("100%");
 			
 			BlogServiceAsync blogService = ServiceHandler.getInstance().getBlogService();
 			
 			blogService.getLatestEntries(5, new AsyncCallback<List<BlogEntry>>() {
 				public void onSuccess(List<BlogEntry> result) {				
 					for(BlogEntry entry : result)
 						pagePanel.add(new EntryUiBinder(entry));	
 				}
 				public void onFailure(Throwable caught) {
 					pagePanel.add(PageContainer.getInstance().error500Page(caught.getMessage()));
 				}
 			});
 		}
 		
 	}
 	
 	public BlogPage(final int entryId) {
 		final VerticalPanel pagePanel = new VerticalPanel();
 		initWidget(pagePanel);
 		
 		pagePanel.setWidth("100%");
 		
 		ServiceHandler.getInstance().getBlogService().getByEntryId(entryId, new AsyncCallback<BlogEntry>() {
 			public void onFailure(Throwable caught) {
 				pagePanel.add(PageContainer.getInstance().error500Page(caught.getMessage()));
 			}
 			public void onSuccess(BlogEntry result) {
 				if(result == null)
 					pagePanel.add(PageContainer.getInstance().error500Page("Blog Entry "+entryId+" does not exist"));
 				else
 					pagePanel.add(new EntryUiBinder(result));
 			}
 		});
 		
 	}
 	
 }
 
 /**
  * Contains the UI processed entry
  * @author reim
  *
  */
 class EntryUiBinder extends Composite {
 	private Logger logger = Logger.getLogger("Blog"); 
 		
 	interface BlogEntryPanel extends UiBinder<Widget, EntryUiBinder> {}
 	private static BlogEntryPanel uiBinder = GWT.create(BlogEntryPanel.class);
 	
 	@UiField 
 	Anchor title;
 	
 	@UiField
 	Hyperlink author;
 	
 	@UiField
 	ParagraphElement content;
 	
 	@UiField
 	SpanElement date;
 	
 	@UiField
 	Hyperlink commentLink;
 	
 	@UiField
 	Hyperlink readMoreLink;
 	
 	public EntryUiBinder(final BlogEntry entry) {	
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		title.setText(entry.getTitle());
 		title.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				History.newItem("entry"+entry.getEntryId());
 			}
 		});
 		title.addMouseOverHandler(new MouseOverHandler() {
 			public void onMouseOver(MouseOverEvent event) {
 				title.addStyleName("anchor-over");
 			}
 		});
 		title.addMouseOutHandler(new MouseOutHandler() {
 			public void onMouseOut(MouseOutEvent event) {
 				title.removeStyleName("anchor-over");
 			}
 		});
 				
 		author.setText(entry.getAuthorId() == null ? "admin" : "THIS WILL BE THE AUTHOR ID");
 		
 		content.setInnerText(entry.getContent());
 		
 		DateTimeFormat sdf = DateTimeFormat.getFormat("MMM dd, yyyy");
 		
 		date.setInnerText(sdf.format(entry.getCreated()));
 		
 		readMoreLink.addStyleName("readmore");
 		
 		BlogServiceAsync blogService = ServiceHandler.getInstance().getBlogService();
 		blogService.getNumberOfComments(entry.getId(), new AsyncCallback<Integer>() {
 			public void onFailure(Throwable caught) {
 				makeCommentLink(-1);
 			}
 			public void onSuccess(Integer result) {
 				makeCommentLink(result.intValue());
 			}
 			private void makeCommentLink(int i) {
 				commentLink.setText("Comments ("+(i < 0 ? "NAN" : i)+")");
 				commentLink.addStyleName("comments");
 			}
 		});
 		
 		//This is what will be done after clicking a link
 		History.addValueChangeHandler(new ValueChangeHandler<String>() {			
 			public void onValueChange(ValueChangeEvent<String> event) {
 				String historyToken = event.getValue();
 				logger.log(Level.FINE, "called blog entry '"+historyToken+"'");
 				if(historyToken.contains("entry")) {
 					int entryId = Integer.parseInt(historyToken.substring(5));
 					PageBuilder.setPage(new BlogPage(entryId));
 				} else {
 					PageBuilder.setPage(PageContainer.getInstance().getPage("blog"));
 				}
 			}
 		});
 	}	
 }
