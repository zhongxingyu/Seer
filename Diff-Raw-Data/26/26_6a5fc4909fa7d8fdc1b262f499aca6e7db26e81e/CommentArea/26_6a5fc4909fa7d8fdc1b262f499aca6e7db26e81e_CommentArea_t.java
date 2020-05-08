 package org.smartsnip.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.smartsnip.shared.ISnippet;
 import org.smartsnip.shared.XComment;
 import org.smartsnip.shared.XSnippet;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * 
  * 
  * @author Paul
  * 
  *         A composed Widget to create a possibility to comment
  * 
  */
 public class CommentArea extends Composite implements Updateable {
 
 	private final XSnippet snippet;
 
 	private VerticalPanel myPanel;
 	private Label lblComments;
 	private VerticalPanel vertComments;
 	private HorizontalPanel horPanel;
 	private TextArea myComment;
 	private Button btnSend;
 	private ScrollPanel scrPanel;
 
 	/**
 	 * Initializies the Widget
 	 * 
 	 * @param snip
 	 *            - a XSnippet
 	 */
 	public CommentArea(final XSnippet snip) {
 		myPanel = new VerticalPanel();
 		this.snippet = snip;
 		scrPanel = new ScrollPanel();
 		scrPanel.setStyleName("scrollComment");
 
 		myPanel.setWidth("300px");
 		myPanel.setHeight("500px");
 
 		vertComments = new VerticalPanel();
 		horPanel = new HorizontalPanel();
 
 		lblComments = new Label("");
 		lblComments.setStyleName("h4");
 		myPanel.add(lblComments);
 
 		myComment = new TextArea();
 		// myComment.setStyleName("commentTxt");
 		btnSend = new Button("Send");
 		btnSend.setStyleName("commentBtn");
 		btnSend.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 
 				if (!Control.getInstance().isLoggedIn()) {
 					lblComments.setText("Login first!");
 					return;
 				}
 				Control control = Control.getInstance();
 				String comment = myComment.getText();
 				if (comment.isEmpty())
 					return;
 
 				lblComments.setText("Commenting ... ");
 				control.writeComment(comment, snip.hash);
 			}
 		});
 
 		horPanel.add(myComment);
 		horPanel.add(btnSend);
 
 		scrPanel.add(vertComments);
 
 		myPanel.add(scrPanel);
 		myPanel.add(horPanel);
 
 		initWidget(myPanel);
 		// Give the overall composite a style name.
 		setStyleName("commentArea");
 
 		update();
 
 	}
 
 	/**
 	 * 
 	 * Updates the whole widget
 	 * 
 	 * 
 	 * 
 	 */
 	public void update() {
		myComment.setText("");
 		lblComments.setText("Refreshing comments ... ");
 
 		ISnippet.Util.getInstance().getComments(snippet.hash, 0, 50, new AsyncCallback<List<XComment>>() {
 
 			@Override
 			public void onSuccess(List<XComment> result) {
 				if (result == null)
 					result = new ArrayList<XComment>();
 
 				vertComments.clear();
 
 				for (XComment i : result) {
 					vertComments.add(new CommentField(CommentArea.this, i));
 				}
 				int count = result.size();
 				lblComments.setText(count + " Comment" + (count == 1 ? "" : "s"));
 
 				if (result.size() < 4) {
 					Label myLabel = new Label("Feel free to start commenting");
 					vertComments.add(myLabel);
 				}
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
				vertComments.add(new Label("Error fetching comment list: " + caught.getMessage()));
 				lblComments.setText("No comments");
 			}
 		});
 	}
 
 }
