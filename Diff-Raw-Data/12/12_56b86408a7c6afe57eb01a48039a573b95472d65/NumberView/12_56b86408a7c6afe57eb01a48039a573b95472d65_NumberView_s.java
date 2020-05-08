 package arithmea.client.view;
 
 import arithmea.client.presenter.NumberPresenter;
 import arithmea.client.widgets.ExtendedTextBox;
 import arithmea.shared.gematria.HebrewMethod;
 import arithmea.shared.gematria.LatinMethod;
 
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class NumberView extends Composite implements NumberPresenter.Display {	
 	private final Button cancelButton;
 	private final ExtendedTextBox numberBox;
 	private final ListBox methodBox;
 	private final Button showButton;
 	
 	private final FlowPanel similarWords = new FlowPanel();
 	private final FlexTable contentTable;
 
 	public NumberView() {
 		final String method = com.google.gwt.user.client.Window.Location.getParameter("method");
 		final String number = com.google.gwt.user.client.Window.Location.getParameter("number");
 		
 		final DecoratorPanel contentTableDecorator = new DecoratorPanel();
 		initWidget(contentTableDecorator);
 		contentTableDecorator.setWidth("100%");
 		contentTableDecorator.setWidth("800px");
 
 		contentTable = new FlexTable();
 		contentTable.setWidth("100%");
 		
 		final HorizontalPanel hPanel = new HorizontalPanel();
 		cancelButton = new Button("Cancel");
 		hPanel.add(cancelButton);
 		numberBox = new ExtendedTextBox();
 		numberBox.setText(number);
 		
 		hPanel.add(numberBox);
 		methodBox = new ListBox();
 		int index = 0;
 		for (LatinMethod lm : LatinMethod.values()) {
 			methodBox.addItem(lm.name());
 			if (lm.name().equalsIgnoreCase(method)) {
 				methodBox.setSelectedIndex(index);
 			}
 			index++;
 		}
 		for (HebrewMethod hm : HebrewMethod.values()) {
 			methodBox.addItem(hm.name());
 			if (hm.name().equalsIgnoreCase(method)) {
 				methodBox.setSelectedIndex(index);
 			}
 			index++;
 		}
 		hPanel.add(methodBox);
 		showButton = new Button("Show");
 		hPanel.add(showButton);
 
 		contentTable.getCellFormatter().addStyleName(0, 0, "menu-table");
 		contentTable.setWidget(0, 0, hPanel);
 		
		similarWords.setWidth("100%");
 		similarWords.setStyleName("flow-panel");
 		contentTable.setWidget(1, 0, similarWords);
 		
 		contentTableDecorator.add(contentTable);
 	}
 	
 	@Override
 	public ListBox getGematriaMethod() {
 		return methodBox;
 	}
 
 	@Override
 	public TextBox getNumber() {
 		return numberBox;
 	}
 
 	@Override
 	public HasClickHandlers getCancelButton() {
 		return cancelButton;
 	}
 
 	@Override
 	public HasClickHandlers getShowButton() {
 		return showButton;
 	}
 
 	@Override
 	public FlowPanel getSimilarWords() {
 		return similarWords;
 	}
 	
 	@Override
 	public Widget asWidget() {
 		return this;
 	}
 }
