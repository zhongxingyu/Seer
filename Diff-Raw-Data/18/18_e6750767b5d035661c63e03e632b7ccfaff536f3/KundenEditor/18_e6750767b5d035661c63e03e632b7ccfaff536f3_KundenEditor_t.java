 /*******************************************************************************
  * Copyright (c) 2010 C1WPS GmbH. All rights reserved.
  *******************************************************************************/
 package de.c1wps.winterschool.ui.kundeneditor;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.bindings.keys.KeyStroke;
 import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.fieldassist.ContentProposalAdapter;
 import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
 import org.eclipse.jface.fieldassist.TextContentAdapter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 import org.osgi.framework.ServiceException;
 
 import de.c1wps.winterschool.domain.common.IGenericServiceListener;
 import de.c1wps.winterschool.domain.kunde.fachwerte.Kundennummer;
 import de.c1wps.winterschool.domain.kunde.listener.IKundeChangedListener;
 import de.c1wps.winterschool.domain.kunde.material.Kunde;
 import de.c1wps.winterschool.domain.kunde.service.IKundenService;
 import de.c1wps.winterschool.ui.kundeneditor.internal.KundenEditorActivator;
 
 public class KundenEditor extends EditorPart {
 
 	public static final String ID = "de.c1wps.winterschool.ui.KundenEditor";
 
 	private Kunde originalKunde;
 	private Kunde currentKunde;
 	private Text vornameTextField;
 	private Text nachnameTextField;
 	private IKundenService kundenService;
 	private Label kundenNummerLabel;
 	private Text strasseTextField;
 	private Text hausnummerTextField;
 	private Text plzTextField;
 	private Text stadtTextField;
 
 	@Override
 	public boolean isDirty() {
 		return !(originalKunde.equals(currentKunde));
 	}
 
 	@Override
 	public void setFocus() {
 		vornameTextField.setFocus();
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		if (kundenService != null) {
 			try {
 				kundenService.aktualisiereKunde(currentKunde);
 				originalKunde = currentKunde.copy();
 				firePropertyChange(EditorPart.PROP_DIRTY);
 			} catch (ServiceException e) {
				switch (e.getType()) {
				case ServiceException.REMOTE:
					MessageDialog.openError(Display.getCurrent()
							.getActiveShell(),
							"Network Error (you data is probably lost)", e
									.getMessage());
					// TODO make this bundle "dynamic aware" so that it recovers
					// if the kundenservice is pulled
					// includes keeping/preserving the state!
					break;
				default:
					// TODO handle local messages too!
					e.printStackTrace();
					break;
				}
 			}
 
 		}
 	}
 
 	@Override
 	public void doSaveAs() {
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
 		setSite(site);
 		setInput(input);
 		if (input instanceof KundenEditorInput) {
 			KundenEditorInput editorInput = (KundenEditorInput) input;
 			originalKunde = editorInput.getKunde();
 			currentKunde = originalKunde.copy();
 		}
 
 		KundenEditorActivator.getDefault().registerKundenServiceListener(
 				createKundenServiceListener());
 		KundenEditorActivator.getDefault().registerKundeChangedListener(
 				createKundeChangedListener());
 	}
 
 	private IGenericServiceListener<IKundenService> createKundenServiceListener() {
 		return new IGenericServiceListener<IKundenService>() {
 
 			public void bindService(IKundenService service) {
 				kundenService = service;
 			}
 
 			public void unbindService(IKundenService service) {
 				kundenService = null;
 
 			}
 		};
 	}
 
 	private IKundeChangedListener createKundeChangedListener() {
 
 		IKundeChangedListener kundeChangedListener = new IKundeChangedListener() {
 
 			public void kundeGeandert(final Kundennummer kundenNummer) {
 				if (currentKunde.getKundennummer().equals(kundenNummer)) {
 					Display.getDefault().asyncExec(new Runnable() {
 
 						public void run() {
 							updateEditor(kundenNummer);
 						}
 					});
 				}
 			}
 		};
 
 		return kundeChangedListener;
 	}
 
 	private void updateEditor(final Kundennummer kundenNummer) {
 		try {
 			originalKunde = kundenService.getKunde(kundenNummer);
 			currentKunde = originalKunde.copy();
 
 			vornameTextField.setText(currentKunde.getVorname());
 			nachnameTextField.setText(currentKunde.getNachname());
 			kundenNummerLabel
 					.setText(currentKunde.getKundennummer().toString());
 			strasseTextField.setText(currentKunde.getAdresse().getStrasse());
 			hausnummerTextField.setText(currentKunde.getAdresse()
 					.getHausnummer());
 			plzTextField.setText(Integer.toString(currentKunde.getAdresse()
 					.getPlz()));
 			stadtTextField.setText(currentKunde.getAdresse().getOrt());
 		} catch (ServiceException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		System.out.println("KundenEditor.createPartControl()");
 		Composite main = new Composite(parent, SWT.NONE);
 		main.setLayout(new GridLayout(2, false));
 
 		Label kundenNummerTitle = new Label(main, SWT.RIGHT);
 		kundenNummerTitle.setText("Kundennummer:");
 		kundenNummerTitle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
 				false, false));
 		kundenNummerLabel = new Label(main, SWT.NONE);
 		kundenNummerLabel.setLayoutData(new GridData(300, SWT.DEFAULT));
 
 		vornameTextField = createTextEntry(main, "Vorname: ");
 		vornameTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				currentKunde.setVorname(vornameTextField.getText());
 				firePropertyChange(EditorPart.PROP_DIRTY);
 			}
 		});
 
 		nachnameTextField = createTextEntry(main, "Nachname: ");
 		nachnameTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				currentKunde.setNachname(nachnameTextField.getText());
 				firePropertyChange(EditorPart.PROP_DIRTY);
 			}
 		});
 
 		strasseTextField = createTextEntry(main, "Strasse: ");
 		strasseTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				currentKunde.getAdresse()
 						.setStrasse(strasseTextField.getText());
 				firePropertyChange(EditorPart.PROP_DIRTY);
 			}
 		});
 
 		hausnummerTextField = createTextEntry(main, "Hausnummer: ");
 		hausnummerTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				currentKunde.getAdresse().setHausnummer(
 						hausnummerTextField.getText());
 				firePropertyChange(EditorPart.PROP_DIRTY);
 			}
 		});
 
 		plzTextField = createTextEntry(main, "PLZ: ");
 		plzTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				try {
 					Integer valueOf = Integer.valueOf(plzTextField.getText());
 					currentKunde.getAdresse().setPlz(valueOf);
 					firePropertyChange(EditorPart.PROP_DIRTY);
 				} catch (Exception ex) {
 					plzTextField.setText(String.valueOf(currentKunde
 							.getAdresse().getPlz()));
 				}
 			}
 		});
 
 		stadtTextField = createTextEntry(main, "Stadt: ");
 		stadtTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				currentKunde.getAdresse().setOrt(stadtTextField.getText());
 				firePropertyChange(EditorPart.PROP_DIRTY);
 			}
 		});
 
 		try {
 			char[] autoCharacters = new char[] { '#' };
 			KeyStroke keyStroke = KeyStroke.getInstance("Ctrl+Space");
 			String[] proposals = new String[] { "Hamburg", "Berlin", "Muenchen" };
 			new ContentProposalAdapter(stadtTextField,
 					new TextContentAdapter(),
 					new SimpleContentProposalProvider(proposals), keyStroke,
 					autoCharacters);
 		} catch (ParseException e1) {
 			e1.printStackTrace();
 		}
 
 		kundenNummerLabel
 				.setText(String.valueOf(currentKunde.getKundennummer()));
 
 		vornameTextField.setText(currentKunde.getVorname());
 		nachnameTextField.setText(currentKunde.getNachname());
 		// weitere Text-Felder für die restlichen Daten aus dem Kunden
 
 		strasseTextField.setText(currentKunde.getAdresse().getStrasse());
 		String valueOf = String.valueOf(currentKunde.getAdresse().getPlz());
 		plzTextField.setText(valueOf);
 		hausnummerTextField.setText(currentKunde.getAdresse().getHausnummer());
 		stadtTextField.setText(currentKunde.getAdresse().getOrt());
 
 	}
 
 	private Text createTextEntry(Composite parent, String title) {
 		Label nachname = new Label(parent, SWT.RIGHT);
 		nachname.setText(title);
 		nachname.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 		Text result = new Text(parent, SWT.BORDER);
 		result.setLayoutData(new GridData(300, SWT.DEFAULT));
 		return result;
 	}
 
 }
