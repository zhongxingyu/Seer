 package de.fkoeberle.autocommit.message.ui;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.editor.FormEditor;
 import org.eclipse.ui.forms.editor.FormPage;
 import org.eclipse.ui.forms.events.HyperlinkAdapter;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Hyperlink;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 
 import de.fkoeberle.autocommit.message.ProfileIdResourceAndName;
 import de.fkoeberle.autocommit.message.ui.Model.ICurrentProfileListener;
 
 public class OverviewPage extends FormPage {
 	private static final String ID = FormPage.class.getCanonicalName();
 	private static final String TITLE = "Overview";
 	private final Model model;
 
 	public OverviewPage(FormEditor editor, Model model) {
 		super(editor, ID, TITLE);
 		this.model = model;
 	}
 
 	/**
 	 * Create contents of the form.
 	 * 
 	 * @param managedForm
 	 */
 	@Override
 	protected void createFormContent(IManagedForm managedForm) {
 		FormToolkit toolkit = managedForm.getToolkit();
 		ScrolledForm form = managedForm.getForm();
 		form.setText("Overview");
 		Composite body = form.getBody();
 		toolkit.decorateFormHeading(form.getForm());
 		toolkit.paintBordersFor(body);
 		managedForm.getForm().getBody().setLayout(new GridLayout(1, false));
 
 		Section sctnSelectHowCommit = managedForm.getToolkit().createSection(
 				managedForm.getForm().getBody(),
 				Section.TWISTIE | Section.TITLE_BAR);
 		managedForm.getToolkit().paintBordersFor(sctnSelectHowCommit);
 		sctnSelectHowCommit
 				.setText("Select which commit messages should be generated:");
 
 		Composite profileRadioButtonsComposite = managedForm.getToolkit()
 				.createComposite(sctnSelectHowCommit, SWT.NONE);
 		managedForm.getToolkit().paintBordersFor(profileRadioButtonsComposite);
 		sctnSelectHowCommit.setClient(profileRadioButtonsComposite);
 		profileRadioButtonsComposite.setLayout(new GridLayout(1, false));
 		for (Object object : model.getProfiles()) {
 			final ProfileIdResourceAndName profile = (ProfileIdResourceAndName) object;
 			Composite rowComposite = toolkit
 					.createComposite(profileRadioButtonsComposite);
 			GridLayout rowLayout = new GridLayout(2, false);
 			rowLayout.marginHeight = 0;
 			rowComposite.setLayout(rowLayout);
 
 			final Button radioButton = toolkit.createButton(rowComposite,
 					profile.getName(), SWT.RADIO);
 			managedForm.getToolkit().adapt(radioButton, true, true);
 			ICurrentProfileListener listener = new RadioButtonCurrentProfileListener(
 					model, radioButton, profile);
 			model.addCurrentProfileListener(listener);
 			radioButton.addSelectionListener(new RadioButtonSelectionListener(
 					model, radioButton, profile));
 			if (profile != Model.CUSTOM_PROFILE) {
 				Hyperlink hyperlink = toolkit.createHyperlink(rowComposite,
 						"(Customize...)", SWT.NONE);
 				hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
 					@Override
 					public void linkActivated(HyperlinkEvent event) {
 						getEditor().setActivePage(AdvancedPage.ID);
 						try {
 							model.switchToProfile(profile);
 						} catch (ExecutionException e) {
 							AdvancedPage.reportError(
 									radioButton.getShell(),
 									"Failed to switch profile for customization",
 									e);
 						}
 					}
 				});
 			}
 			listener.currentProfileChanged();
 		}
		/*
		 * setExpanded needs to be called after creating the content. Since
		 * eclipse 3.7 seems to not create the section content properly
		 * otherwise.
		 */
		sctnSelectHowCommit.setExpanded(true);

 	}
 
 	private final class RadioButtonSelectionListener implements
 			SelectionListener {
 		private final Model model;
 		private final Button radioButton;
 		private final ProfileIdResourceAndName profile;
 
 		private RadioButtonSelectionListener(Model model, Button radioButton,
 				ProfileIdResourceAndName profile) {
 			this.model = model;
 			this.radioButton = radioButton;
 			this.profile = profile;
 		}
 
 		@Override
 		public void widgetSelected(SelectionEvent event) {
 			try {
 				model.switchToProfile(profile);
 			} catch (ExecutionException e) {
 				AdvancedPage.reportError(radioButton.getShell(),
 						"Failed to switch profile", e);
 			}
 		}
 
 		@Override
 		public void widgetDefaultSelected(SelectionEvent event) {
 			try {
 				model.switchToProfile(profile);
 			} catch (ExecutionException e) {
 				AdvancedPage.reportError(radioButton.getShell(),
 						"Failed to switch profile", e);
 			}
 		}
 	}
 
 	private final class RadioButtonCurrentProfileListener implements
 			ICurrentProfileListener {
 		private final Model model;
 		private final Button radioButton;
 		private final ProfileIdResourceAndName profile;
 
 		private RadioButtonCurrentProfileListener(Model model,
 				Button radioButton, ProfileIdResourceAndName profile) {
 			this.model = model;
 			this.radioButton = radioButton;
 			this.profile = profile;
 		}
 
 		@Override
 		public void currentProfileChanged() {
 			boolean selected = (model.getCurrentProfile() == profile);
 			if (selected != radioButton.getSelection()) {
 				radioButton.setSelection(selected);
 			}
 			if (profile == Model.CUSTOM_PROFILE) {
 				radioButton.setEnabled(selected);
 			}
 		}
 	}
 }
