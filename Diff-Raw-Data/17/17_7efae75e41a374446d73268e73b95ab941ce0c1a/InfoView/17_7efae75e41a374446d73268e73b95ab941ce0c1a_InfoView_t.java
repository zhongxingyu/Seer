 package arithmea.client.view;
 
 import arithmea.client.presenter.InfoPresenter;
 
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class InfoView extends Composite implements InfoPresenter.Display {	
 	private final Button cancelButton;
 	private final FlexTable contentTable;
 
 	public InfoView() {
 		final DecoratorPanel contentTableDecorator = new DecoratorPanel();
 		initWidget(contentTableDecorator);
 		contentTableDecorator.setWidth("100%");
 		contentTableDecorator.setWidth("800px");
 
 		contentTable = new FlexTable();
 		contentTable.setWidth("100%");
 		
 		final HorizontalPanel menuPanel = new HorizontalPanel();
 		cancelButton = new Button("Cancel");
 		menuPanel.add(cancelButton);
 
 		contentTable.getCellFormatter().addStyleName(0, 0, "menu-table");
 		contentTable.setWidget(0, 0, menuPanel);
 		
 		FlexTable infoTable = new FlexTable();
 		int row = 0;
 		infoTable.setText(row++, 0, "Chaldean:");
 		infoTable.setText(row++, 0, "Shows the value of the word in chaldean numerology. The result is not reduced in order to preserve the full information.");
		infoTable.setWidget(row++, 0, new Anchor("http://en.wikipedia.org/wiki/Arithmancy#The_Chaldean_Method", "http://en.wikipedia.org/wiki/Arithmancy#The_Chaldean_Method"));
 		infoTable.setWidget(row++, 0, new HTML("&nbsp;"));
 		
 		infoTable.setText(row++, 0, "Pythagorean:");
 		infoTable.setText(row++, 0, "Shows the value of the word in pythagorean (or agrippan) numerology. The result is not reduced in order to preserve the full information.");
		infoTable.setWidget(row++, 0, new Anchor("http://en.wikipedia.org/wiki/Arithmancy#The_Agrippan_Method", "http://en.wikipedia.org/wiki/Arithmancy#The_Agrippan_Method"));
 		infoTable.setWidget(row++, 0, new HTML("&nbsp;"));
 		
 		infoTable.setText(row++, 0, "IA:");
 		infoTable.setText(row++, 0, "1=A, 2=B, 3=C .. 26=Z. Also known as Simple English Gematria.");
		infoTable.setWidget(row++, 0, new Anchor("http://wmjas.wikidot.com/simple-english-gematria", "http://wmjas.wikidot.com/simple-english-gematria"));
 		infoTable.setWidget(row++, 0, new HTML("&nbsp;"));
 		
 		infoTable.setText(row++, 0, "NAEQ:");
 		infoTable.setText(row++, 0, "New Aeon English Qabalah");
		infoTable.setWidget(row++, 0, new Anchor("http://en.wikipedia.org/wiki/English_Qabalah#ALW_Cipher", "http://en.wikipedia.org/wiki/English_Qabalah#ALW_Cipher"));
 		infoTable.setWidget(row++, 0, new HTML("&nbsp;"));
 		
 		infoTable.setText(row++, 0, "TQ:");
 		infoTable.setText(row++, 0, "Trigrammaton Qabalah");
		infoTable.setWidget(row++, 0, new Anchor("http://en.wikipedia.org/wiki/English_Qabalah#Trigrammaton_Qabalah_.28TQ.29", "http://en.wikipedia.org/wiki/English_Qabalah#Trigrammaton_Qabalah_.28TQ.29"));
 		infoTable.setWidget(row++, 0, new HTML("&nbsp;"));
 		
 		infoTable.setText(row++, 0, "German:");
 		infoTable.setText(row++, 0, "A cipher specific to the german language, that was discovered by Rolf Keppler.");
		infoTable.setWidget(row++, 0, new Anchor("http://www.rolf-keppler.de/schluessel.htm", "http://www.rolf-keppler.de/schluessel.htm"));
 		infoTable.setWidget(row++, 0, new HTML("&nbsp;"));
 		
 		infoTable.setText(row++, 0, "EQ:");
 		infoTable.setText(row++, 0, "English Qabalah method that uses the same sequence as NAEQ, but without reducing the Values (thanks to Alien696).");
 		infoTable.setWidget(row++, 0, new HTML("&nbsp;"));		
 
 		contentTable.setWidget(1, 0, infoTable);
 		contentTable.getCellFormatter().setAlignment(1, 0,
 				HasHorizontalAlignment.ALIGN_LEFT,
 				HasVerticalAlignment.ALIGN_TOP);
 		
 		contentTable.getCellFormatter().setHeight(1, 0, "635px");
 		
 		contentTableDecorator.add(contentTable);
 	}
 
 	@Override
 	public HasClickHandlers getCancelButton() {
 		return cancelButton;
 	}
 		
 	@Override
 	public Widget asWidget() {
 		return this;
 	}
 
 }
