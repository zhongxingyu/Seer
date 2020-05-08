 package ch.hslu.appe.fs1303.gui.views;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 
 import ch.hslu.appe.fs1301.business.shared.UserRole;
 import ch.hslu.appe.fs1301.business.shared.dto.DTOBestellung;
 import ch.hslu.appe.fs1303.gui.controls.APPECheckBoxField;
 import ch.hslu.appe.fs1303.gui.controls.APPEComboBoxField;
 import ch.hslu.appe.fs1303.gui.controls.APPEDateField;
 import ch.hslu.appe.fs1303.gui.controls.APPEIntField;
 import ch.hslu.appe.fs1303.gui.controls.APPEStringField;
 import ch.hslu.appe.fs1303.gui.controls.APPETableField;
 import ch.hslu.appe.fs1303.gui.datasource.ComboDataSource;
 import ch.hslu.appe.fs1303.gui.datasource.OrderTableDescriptor;
 import ch.hslu.appe.fs1303.gui.labelprovider.PersonLabelProvider;
 import ch.hslu.appe.fs1303.gui.models.PersonEditorModel;
 import ch.hslu.appe.fs1303.gui.presenter.PersonPresenter.iPersonView;
 import ch.hslu.appe.fs1303.gui.presenter.PersonPresenter.iPersonViewListener;
 
 public class PersonView extends AbstractBaseView<PersonEditorModel, iPersonViewListener> implements iPersonView {
 	
 	private APPEIntField fUserId;
 	private APPEStringField fLastName;
 	private APPEStringField fFirstName;
 	private APPEStringField fStreet ;
 	private APPEIntField fPLZ;
 	private APPEDateField fBirthday;
 	private APPEStringField fCity;
 	private APPEStringField fEmail;
 	private APPEStringField fUserName;
 	private APPEStringField fPassword;
 	private APPETableField<DTOBestellung> fOrderTable;
 	private Button fNewOrderButton;
 	private APPECheckBoxField fActive;
 	private APPEComboBoxField fRole;	
 
 	@Override
 	public void createPageContent(Composite parent, FormToolkit toolkit) {
 		Section section = toolkit.createSection(fForm.getBody(), Section.DESCRIPTION
 		            | Section.TITLE_BAR);
 	    section.setText("Allgemeine Informationen");
 	    Composite client = toolkit.createComposite(section, SWT.FILL);
 	    
 	    GridLayout gl_container = new GridLayout(2, false);
 	    gl_container.marginRight = 5;
 	    gl_container.marginLeft = 5;
 	    client.setLayout(gl_container);
 	    
 	    fUserId = new APPEIntField(client, toolkit, "ID: ", SWT.READ_ONLY);
 	    register(fUserId);
	   	   	    
	    fFirstName = new APPEStringField(client, toolkit, "Vorname: ", SWT.None);
	    fFirstName.addValidationMessage(fForm.getMessageManager(), "Geben Sie einen Vornamen an.");
	    register(fFirstName);
 	    
 	    fLastName = new APPEStringField(client, toolkit, "Name: ", SWT.None);
 	    fLastName.addValidationMessage(fForm.getMessageManager(), "Geben Sie einen Namen an");
 	    register(fLastName);
 	    
 	    fStreet = new APPEStringField(client, toolkit, "Strasse: ", SWT.None);
 	    fStreet.addValidationMessage(fForm.getMessageManager(), "Geben Sie eine Strasse an.");
 	    register(fStreet);
 	    
 	    fPLZ = new APPEIntField(client, toolkit, "PLZ: ", SWT.None);
 	    fPLZ.addValidationMessage(fForm.getMessageManager(), "Geben Sie eine gueltige Postleitzahl an.");
 	    register(fPLZ);
 	    
 	    fCity = new APPEStringField(client, toolkit, "Ort: ", SWT.None);
 	    fCity.addValidationMessage(fForm.getMessageManager(), "Geben Sie einen Ort an.");
 	    register(fCity);
 	    
 	    fBirthday = new APPEDateField(client, toolkit, "Geburtstag: ", SWT.None);
 	    fBirthday.addValidationMessage(fForm.getMessageManager(), "Geben Sie ein gueltiges Geburtsdatum an.");
 	    register(fBirthday);
 	    
 	    fEmail = new APPEStringField(client, toolkit, "Email: ", SWT.None);
 	    fEmail.addValidationMessage(fForm.getMessageManager(), "Geben sie eine Email an.");
 	    register(fEmail);
 	    
 	    fUserName = new APPEStringField(client, toolkit, "Benutzername: ", SWT.None);
 	    fUserName.setNullable(true);
 	    register(fUserName);
 	    
 	    fPassword = new APPEStringField(client, toolkit, "Passwort: ", SWT.PASSWORD);
 	    fPassword.setNullable(true);
 	    register(fPassword);
 	    
 	    fRole = new APPEComboBoxField(client, toolkit, "Rolle: ", SWT.None);
 	    fRole.setDataSource(ComboDataSource.getDataSourceForClass(UserRole.class));
 	    register(fRole);
 	    
 	    fActive = new APPECheckBoxField(client, toolkit, "Aktive: ", SWT.None);
 	    register(fActive);
 	    
 	    section.setClient(client);
 	    
 	    Section orderSection = toolkit.createSection(fForm.getBody(), Section.DESCRIPTION
 	            | Section.TITLE_BAR);
 	    orderSection.setText("Bestellungen");
 	    orderSection.marginHeight = 10;
 
 	    Composite orderComposite = toolkit.createComposite(orderSection, SWT.FILL);
 	    orderComposite.setLayout(gl_container);
 	    
 	    fOrderTable = new APPETableField<DTOBestellung>(orderComposite, toolkit);
 	    fOrderTable.setTableDescriptor(new OrderTableDescriptor());
 	    fOrderTable.addMouseListener(new MouseListener() {
 			
 			@Override
 			public void mouseUp(MouseEvent e) {	}
 			
 			@Override
 			public void mouseDown(MouseEvent e) { }
 			
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				DTOBestellung selectedItem = fOrderTable.getSelectedItem();
 				if (selectedItem != null) {
 					fListener.openOrder(selectedItem.getId());
 				}
 			}
 		});
 	    register(fOrderTable);
 	    
 	    fNewOrderButton = toolkit.createButton(orderComposite, "Bestellung erfassen", 0);
 	    fNewOrderButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
 	    fNewOrderButton.addListener(SWT.Selection, new Listener() {
 			
 			@Override
 			public void handleEvent(Event arg0) {				
 				fListener.onNewOrderButtonClick();
 			}
 		});
 	    
 	    orderSection.setClient(orderComposite);
 	}
 	
 	@Override
 	public void bindModel(PersonEditorModel model) {
 		fModel = model;
 		
 		fForm.setText(new PersonLabelProvider().getText(model.getPerson()));
 		
 		fUserId.bindModel(model.getPerson(), "fId");
 		fLastName.bindModel(model.getPerson(), "fName");
 		fFirstName.bindModel(model.getPerson(), "fVorname");
 		fStreet.bindModel(model.getPerson(), "fStrasse");
 		fPLZ.bindModel(model.getPerson(), "fPlz");
 		fBirthday.bindModel(model.getPerson(), "fGeburtstag");
 		fCity.bindModel(model.getPerson(), "fOrt");
 		fEmail.bindModel(model.getPerson(), "fEMail");
 		fUserName.bindModel(model.getPerson(), "fBenutzername");
 		fPassword.bindModel(model.getPerson(), "fPasswort");
 		fRole.bindModel(model.getPerson(), "fRolle");
 		fActive.bindModel(model.getPerson(), "fAktiv");
 		
 		fOrderTable.bindModel(model, "fOrders");
 	}
 }
