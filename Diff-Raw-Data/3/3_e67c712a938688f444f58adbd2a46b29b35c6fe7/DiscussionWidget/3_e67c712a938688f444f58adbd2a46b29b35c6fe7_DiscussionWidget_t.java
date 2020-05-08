 package com.wtf.client;
 
 import java.util.Date;
 import java.util.List;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.ToggleButton;
 
 public class DiscussionWidget extends Composite{
 
 	private Discussion _discussion;
 	private FlexTable _thread = new FlexTable();
 	final FlexTable _form = new FlexTable();
 	ToggleButton _show_button;
 	private Command _on_close;
 
 	public DiscussionWidget(Discussion discusion, Command on_close) {
 		_discussion = discusion;
 		_on_close = on_close;
 		
 		DockPanel dock = new DockPanel();
 		initWidget(dock);
 		getElement().setClassName("wtf_ignore");
 		dock.setStyleName("wtf_discussion");
 		dock.setSpacing(0);
 
 		HorizontalPanel bar = new HorizontalPanel();
 		bar.setStyleName("wtf_discussion_bar");
 		bar.setSpacing(0);
 		addPollTo(bar);
 		addCloseTo(bar);
 
 		dock.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		dock.add(bar, DockPanel.NORTH);
 
 		dock.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_DEFAULT);
 		ScrollPanel scroller = new ScrollPanel();
 		scroller.setStyleName("wtf_discussion_thread");
 		scroller.setSize("450px", "200px");
 		scroller.add(_thread);
 		_thread.setCellSpacing(0);
 		_thread.setCellPadding(0);
 		_thread.setWidth("100%");
 		DOM.setStyleAttribute(_thread.getElement(), "margin", "0px");
 
 		dock.add(scroller, DockPanel.CENTER);
 
 		_form.setVisible(false);
 
 		_show_button = new ToggleButton("Add post", "Hide");
 		_show_button.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				_form.setVisible(!_form.isVisible());
 			}
 		});	
 		dock.add(_show_button, DockPanel.SOUTH);
 		_show_button.setWidth("440px");
 		DOM.setStyleAttribute(_show_button.getElement(), "margin", "0px");
 		DOM.setStyleAttribute(_show_button.getElement(), "padding", "4px");
 
 		fillForm(_form);
 		dock.add(_form, DockPanel.SOUTH);
 
 		DOM.setStyleAttribute(getElement(), "position", "absolute");
 		//call also position(topElement) to set coordinates
 		
 		fillThread();
 	}
 	
 	public void setFormVisibility(boolean b) {
 		_form.setVisible(b);
 		_show_button.setDown(b);
 	}
 
 	public void position(Element elem) {
 		int top = elem.getAbsoluteTop() - this.getOffsetHeight();
 		int left = elem.getAbsoluteLeft();
 		left = Math.max(left, 0);
 		top = Math.max(top, 0);
 		
 		if(left + this.getOffsetWidth() > Window.getClientWidth())
 			left = Window.getClientWidth() - this.getOffsetWidth();
 	
 		DOM.setStyleAttribute(getElement(), "top", top + "px");
 		DOM.setStyleAttribute(getElement(), "left", left + "px");
 	}
 	
 	private void addPollTo(Panel parent) {
 		Poll poll = _discussion.getPoll();
 		for(final Poll.Answer a : poll.getAnswers()) {
 			InlineLabel al = new InlineLabel(a.getLabel() + "(" + a.getVotes() + ")");
 			al.setStyleName(a.getClassAttr());
 			DOM.setStyleAttribute(al.getElement(), "padding", "2px");
 			DOM.setStyleAttribute(al.getElement(), "cursor", "hand");
 			DOM.setStyleAttribute(al.getElement(), "cursor", "pointer");
 			parent.add(al);
 			al.addClickHandler(new ClickHandler() { 
 				public void onClick(ClickEvent event) {
 					a.clicked();
 				}
 			});	
 		}
 	}
 	
 	public void setOnClose(Command on_close) {
 		_on_close = on_close;
 	}
 
 	private void addCloseTo(Panel parent) {
 		InlineLabel close = new InlineLabel("X");
 		close.setStyleName("wtf_discussion_close");
 		DOM.setStyleAttribute(close.getElement(), "padding", "2px");
 		DOM.setStyleAttribute(close.getElement(), "cursor", "hand");
 		DOM.setStyleAttribute(close.getElement(), "cursor", "pointer");
 		parent.add(close);
 		close.addClickHandler(new ClickHandler() { 
 			public void onClick(ClickEvent event) {
 				_on_close.execute();
 			}
 		});	
 
 	}
 
 	private void fillThread() {
 		List<Post> thread = _discussion.getThread();
 		for(Post p : thread) {
 			addPost(p);
 		}
 	}
 
 	private void addPost(Post p) {
 		int numRows = _thread.getRowCount();
 		Label cl = new Label(p.getContent());
 
 		Label al = new Label(p.getAuthor());
 		al.setStyleName("wtf_discussion_author");
 		al.setWidth("90px");
 		DOM.setStyleAttribute(al.getElement(), "overflow", "hidden");
 
 		DateTimeFormat fmt = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");
 		Label date = new Label(fmt.format(p.getDate()));
 		date.setStyleName("wtf_discussion_date");
 
 		al.getElement().appendChild(date.getElement());
 		_thread.setWidget(numRows, 0, al);
 		_thread.setWidget(numRows, 1, cl);
 		if(numRows % 2 == 0) {
 			cl.getElement().getParentElement().setClassName("wtf_discussion_post_even");
 			al.getElement().getParentElement().setClassName("wtf_discussion_post_even");
			al.getElement().getParentElement().setAttribute("width", "90px");
 		} else {
 			cl.getElement().getParentElement().setClassName("wtf_discussion_post_odd");
 			al.getElement().getParentElement().setClassName("wtf_discussion_post_odd");
			al.getElement().getParentElement().setAttribute("width", "90px");
 		}
 		cl.getElement().getParentElement().scrollIntoView();
 	}
 
 	private void fillForm(FlexTable form) {
 		form.setCellSpacing(5);
 		form.setStyleName("wtf_discussion_form");
 		DOM.setStyleAttribute(form.getElement(), "margin", "0px");
 
 		final TextBox author = new TextBox();
 		Label author_l = new Label("Author: ");
 		form.setWidget(0, 0, author_l);
 		form.setWidget(0, 1, author);
 
 		final TextArea content = new TextArea();
 		Label content_l = new Label("Content: ");
 		content.setSize("350px", "100px");
 		form.setWidget(1, 0, content_l);
 		form.setWidget(1, 1, content);
 
 		final ToggleButton submit = new ToggleButton("Add");
 		submit.setWidth("50px");
 		submit.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				if(submit.isDown()) {
 					DeferredCommand.addCommand(new Command() {	//simulate server request
 						public void execute() {
 							Post p = new Post(author.getText(), content.getText(), new Date());
 							_discussion.addPost(p);
 							addPost(p);
 							submit.setDown(false);
 						}
 					});
 				}
 			}
 		});
 
 		form.setWidget(2, 1, submit);
 	}
 }
 
 
