 package arithmea.client.presenter;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import arithmea.client.event.CancelledEvent;
 import arithmea.client.service.ArithmeaServiceAsync;
 import arithmea.shared.data.Term;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class NumberPresenter implements Presenter {
 
 	public interface Display {
 		HasClickHandlers getCancelButton();
 		TextBox getNumber();
 		ListBox getGematriaMethod();
 		FlowPanel getSimilarWords();
 		HasClickHandlers getShowButton();
 		Widget asWidget();
 	}
 
 	private final ArithmeaServiceAsync rpcService;
 	private final HandlerManager eventBus;
 	private final Display display;
 
 	public NumberPresenter(final ArithmeaServiceAsync rpcService,
 			final HandlerManager eventBus, final Display view) {
 		this.rpcService = rpcService;
 		this.eventBus = eventBus;
 		this.display = view;
 	}
 
 	public void bind() {
 		this.display.getCancelButton().addClickHandler(new ClickHandler() {
 			public void onClick(final ClickEvent event) {
 				eventBus.fireEvent(new CancelledEvent());
 			}
 		});
 		
 		this.display.getNumber().addValueChangeHandler(new ValueChangeHandler<String>() {
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				tryShow();
 			}
 		});
 		
 		this.display.getNumber().addKeyUpHandler(new KeyUpHandler() {
 			@Override
 			public void onKeyUp(KeyUpEvent event) {
 				tryShow();
 			}
 		});
 		
 		this.display.getGematriaMethod().addChangeHandler(new ChangeHandler() {
 			public void onChange(ChangeEvent event) {
 				tryShow();
 			}
 		});
 
 		this.display.getShowButton().addClickHandler(new ClickHandler() {
 			public void onClick(final ClickEvent event) {
 				tryShow();
 			}
 		});
 	}
 	
 	private void tryShow() {		
 		Integer number = null;
 		try {
 			String input = this.display.getNumber().getValue();
 			if (!input.equals("")) {
 				number = Integer.valueOf(input);
 				doShow(number);				
 			} else {
 				display.getSimilarWords().clear();
 				display.getSimilarWords().add(new Label("Please enter a number and select a method."));
 			}
 		} catch(NumberFormatException nfe) {
 			display.getSimilarWords().clear();
 			display.getSimilarWords().add(new Label("Only numbers allowed."));
 			String assumedOldText = this.display.getNumber().getValue().
 				substring(0, this.display.getNumber().getValue().length() - 1);
 			this.display.getNumber().setText(assumedOldText);
 			this.display.getNumber().setFocus(true);
 		}
 	}
 	
 	private void doShow(int number) {
 		display.getSimilarWords().clear();
 		display.getSimilarWords().add(new Label("Loading.."));
 		
 		int selectedIndex = this.display.getGematriaMethod().getSelectedIndex();
 		String methodName = this.display.getGematriaMethod().getValue(selectedIndex);
 		
 		rpcService.getTermsFor(methodName, number, new AsyncCallback<ArrayList<Term>>() {
 				@Override 
 				public void onSuccess(ArrayList<Term> result) {
 					Iterator<Term> it = result.iterator();
 					display.getSimilarWords().clear();
 					if (!it.hasNext()) {
 						display.getSimilarWords().add(new Label("No matching words found."));
 					} else {
 						display.getSimilarWords().add(new Label("Listing " + result.size() + " matches: "));
 						while (it.hasNext()) {
 							Term term = it.next();
							Anchor anchor = new Anchor(term.getLatinString() + " ");
 							anchor.setHref("?word=" + term.getLatinString() + "#add");
 							anchor.setStyleName("padding-right");
							display.getSimilarWords().add(anchor);
 						}
 					}
 				}
 				public void onFailure(Throwable caught) {
 					Window.alert("Fail loading terms.");
 				}
 			}
 		);
 	}
 	
 
 	public void go(final HasWidgets container) {
 		bind();
 		container.clear();
 		container.add(display.asWidget());
 		tryShow();
 		display.getNumber().setFocus(true);
 	}
 }
