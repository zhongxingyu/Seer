 package org.opendarts.prototype.ui.dialog;
 
 import java.text.MessageFormat;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.FormDialog;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 import org.opendarts.prototype.internal.model.dart.ThreeDartThrow;
 import org.opendarts.prototype.model.dart.IDart;
 import org.opendarts.prototype.model.dart.IDartsThrow;
 import org.opendarts.prototype.model.game.IGame;
 import org.opendarts.prototype.model.game.IGameEntry;
 import org.opendarts.prototype.model.player.IPlayer;
 import org.opendarts.prototype.ui.utils.OpenDartsFormsToolkit;
 
 /**
  * The Class ThreeDartsComputerDialog.
  */
 public abstract class ThreeDartsComputerDialog extends FormDialog {
 	/** The darts. */
 	private final IDart[] darts;
 
 	/** The txt dart. */
 	private final Text[] txtDart;
 
 	/** The txt wished. */
 	private final Text[] txtWished;
 
 	/** The dart throw. */
 	private ThreeDartThrow dartThrow;
 
 	/** The toolkit. */
 	private final OpenDartsFormsToolkit toolkit;
 
 	/**
 	 * Instantiates a new computer throw.
 	 *
 	 * @param parentShell the parent shell
 	 * @param player the player
 	 * @param game the game
 	 * @param entry the entry
 	 */
 	public ThreeDartsComputerDialog(Shell parentShell, IPlayer player,
 			IGame game, IGameEntry entry) {
 		super(parentShell);
 		this.toolkit = OpenDartsFormsToolkit.getToolkit();
 
 		this.darts = new IDart[3];
 
 		this.txtDart = new Text[this.darts.length];
 		this.txtWished = new Text[this.darts.length];
 	}
 
 	/**
 	 * Gets the dart throw.
 	 *
 	 * @return the dart throw
 	 */
 	protected ThreeDartThrow getDartThrow() {
 		return this.dartThrow;
 	}
 
 	/**
 	 * Sets the dart throw.
 	 *
 	 * @param dartThrow the new dart throw
 	 */
 	protected void setDartThrow(ThreeDartThrow dartThrow) {
 		this.dartThrow = dartThrow;
 	}
 
 	/**
 	 * Gets the darts.
 	 *
 	 * @return the darts
 	 */
 	protected IDart[] getDarts() {
 		return this.darts;
 	}
 
 	/**
 	 * Gets the txt dart.
 	 *
 	 * @return the txt dart
 	 */
 	protected Text[] getTxtDart() {
 		return this.txtDart;
 	}
 
 	/**
 	 * Gets the txt wished.
 	 *
 	 * @return the txt wished
 	 */
 	protected Text[] getTxtWished() {
 		return this.txtWished;
 	}
 
 	/**
 	 * Gets the toolkit.
 	 *
 	 * @return the toolkit
 	 */
 	protected OpenDartsFormsToolkit getToolkit() {
 		return this.toolkit;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.FormDialog#createFormContent(org.eclipse.ui.forms.IManagedForm)
 	 */
 	@Override
 	protected void createFormContent(IManagedForm mform) {
 		String title = this.getFormTitle();
 		ScrolledForm form = mform.getForm();
 		form.setText(title);
 		toolkit.decorateFormHeading(form.getForm());
 
 		Composite body = form.getBody();
 		GridLayoutFactory.fillDefaults().margins(5, 5)
 				.numColumns(this.darts.length).equalWidth(true).applyTo(body);
 
 		GridDataFactory grabData = GridDataFactory.fillDefaults().grab(true,
 				true);
 		GridLayoutFactory clientLayout = GridLayoutFactory.fillDefaults()
 				.equalWidth(true);
 		// Create Darts section
 		Section section;
 		Composite client;
 		Composite cmpWish;
 		Composite cmpDart;
 		for (int i = 0; i < this.darts.length; i++) {
 			// Section
 			section = this.toolkit.createSection(body,
 					ExpandableComposite.TITLE_BAR);
 			grabData.copy().applyTo(section);
 			section.setText(MessageFormat.format("Dart #{0}", i + 1));
 
 			// Section body
 			client = this.toolkit.createComposite(section, SWT.WRAP);
 			clientLayout.copy().applyTo(client);
 
 			// Fill client
 			cmpWish = this.buildWishedComposite(client, i);
 			grabData.copy().applyTo(cmpWish);
 
 			cmpDart = this.buildDartComposite(client, i);
 			grabData.copy().applyTo(cmpDart);
 
 			// End section definition
 			this.toolkit.paintBordersFor(client);
 			section.setClient(client);
 		}
 	}
 
 	/**
 	 * Gets the form title.
 	 *
 	 * @return the form title
 	 */
 	protected abstract String getFormTitle();
 
 	/**
 	 * Builds the wished composite.
 	 *
 	 * @param parent the parent
 	 * @param i the i
 	 * @return the composite
 	 */
 	private Composite buildWishedComposite(Composite parent, int i) {
 		Composite result = this.toolkit.createComposite(parent);
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(result);
 
 		Label lbl = this.toolkit.createLabel(result, "Wished");
 		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER)
 				.grab(true, false).applyTo(lbl);
 
 		// Text
 		this.txtWished[i] = this.buildText(result);
 		this.txtWished[i].setEnabled(false);
 
 		return result;
 	}
 
 	/**
 	 * Builds the dart composite.
 	 *
 	 * @param parent the parent
 	 * @param i the i
 	 * @return the composite
 	 */
 	private Composite buildDartComposite(Composite parent, int i) {
 		Composite result = this.toolkit.createComposite(parent);
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(result);
 
 		Label lbl = this.toolkit.createLabel(result, "Done");
 		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER)
 				.grab(true, false).applyTo(lbl);
 
 		// Text
 		this.txtDart[i] = this.buildText(result);
 		this.txtDart[i].setBackground(this.toolkit.getColors().getColor(
 				OpenDartsFormsToolkit.COLOR_ACTIVE));
 
 		return result;
 	}
 
 	/**
 	 * Builds the text.
 	 *
 	 * @param parent the parent
 	 * @return the text
 	 */
 	private Text buildText(Composite parent) {
 		Text txt = this.toolkit.createText(parent, "", SWT.READ_ONLY
 				| SWT.CENTER);
		txt.setEnabled(false);
 		GridDataFactory.fillDefaults().grab(true, true)
 				.align(SWT.CENTER, SWT.CENTER).hint(150, SWT.DEFAULT)
 				.applyTo(txt);
 		txt.setFont(OpenDartsFormsToolkit
 				.getFont(OpenDartsFormsToolkit.FONT_SCORE_INPUT));
 		return txt;
 	}
 
 	/**
 	 * Display wished.
 	 *
 	 * @param wished the wished
 	 * @param index the index
 	 */
 	protected void displayWished(IDart wished, int index) {
 		Text text = this.txtWished[index];
 		if (!text.isDisposed()) {
 			text.setText(wished.toString());
 		}
 	}
 
 	/**
 	 * Display dart.
 	 *
 	 * @param dart the dart
 	 * @param scoreLeft the score left
 	 * @param index the index
 	 */
 	protected void displayDart(IDart dart, int scoreLeft, int index) {
 		Text text = this.txtDart[index];
 		if (!text.isDisposed()) {
 			text.setText(dart.toString());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.FormDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
 	 */
 	/**
 	 * Creates the button bar.
 	 *
 	 * @param parent the parent
 	 * @return the control
 	 */
 	@Override
 	protected Control createButtonBar(Composite parent) {
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		//Composite sep = new Composite(parent, SWT.NULL);
 		//sep.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
 		//gd.heightHint = 1;
 		Label sep = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
 		sep.setLayoutData(gd);
 		return sep;
 	}
 
 	/**
 	 * Gets the computer throw.
 	 *
 	 * @return the computer throw
 	 */
 	public IDartsThrow getComputerThrow() {
 		return this.dartThrow;
 	}
 
 }
