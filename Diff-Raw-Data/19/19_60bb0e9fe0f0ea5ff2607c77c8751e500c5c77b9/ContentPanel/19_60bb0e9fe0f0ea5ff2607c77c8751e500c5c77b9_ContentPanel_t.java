 package org.gwtapp.ccalc.client.ui;
 
 import org.gwtapp.ccalc.client.CCalc;
 import org.gwtapp.ccalc.client.ui.book.BookPanel;
 import org.gwtapp.ccalc.rpc.data.book.Book;
 import org.gwtapp.core.client.SimpleAsyncCallback;
 import org.gwtapp.template.client.UiHandler;
 import org.gwtapp.template.client.handler.WidgetHandler;
 import org.gwtapp.template.client.ui.TemplatePanel;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 
 public class ContentPanel extends TemplatePanel<Book> {
 
 	private final WidgetHandler submit = new WidgetHandler();
 	private final WidgetHandler backup = new WidgetHandler();
 	private final UiHandler<BookPanel> bookPanel = //
 	new UiHandler<BookPanel>(new BookPanel());
 
 	public ContentPanel() {
 		super(CCalc.templateService.load("ContentPanel.jsp"));
 		add("backupButton", backup);
 		add("submitButton", submit);
 		add("bookPanel", bookPanel);
 		setVisible(false);
 	}
 
 	@Override
 	public void onAddWidgets() {
 		submit.getWidget().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				Book value = bookPanel.getWidget().getValue();
 				value.setCurrentVersion(Book.CURRENT_VERSION.def());
 				CCalc.backup(value);
 				CCalc.downloader.download(value,
 						create(new SimpleAsyncCallback<Void>()));
 			}
 		});
 		backup.getWidget().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
				CCalc.backup(bookPanel.getWidget().getValue());
 			}
 		});
 	}
 
 	@Override
 	public Book getValue() {
 		return bookPanel.getWidget().getValue();
 	}
 
 	@Override
 	public void setValue(Book value, boolean fireEvents) {
 		setVisible(true);
 		bookPanel.getWidget().setValue(value, fireEvents);
 	}
 }
