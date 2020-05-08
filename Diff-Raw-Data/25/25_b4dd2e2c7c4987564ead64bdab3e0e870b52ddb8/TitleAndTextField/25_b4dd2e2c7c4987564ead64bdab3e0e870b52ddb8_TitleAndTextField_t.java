 package org.softwareFm.swtBasics.text;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.softwareFm.swtBasics.SwtBasicConstants;
 import org.softwareFm.swtBasics.Swts;
 import org.softwareFm.swtBasics.images.IImageButtonListener;
 import org.softwareFm.swtBasics.images.ImageButton;
 import org.softwareFm.swtBasics.images.ImageButtons;
 import org.softwareFm.utilities.functions.IFunction1;
 import org.softwareFm.utilities.strings.Strings;
 
 public class TitleAndTextField extends AbstractTitleAnd {
 	private final Text txtText;
 	private boolean globalEditable;
 
 	public TitleAndTextField(ConfigForTitleAnd config, Composite parent, String titleKey) {
 		this(config, parent, titleKey, true);
 	}
 
 	public TitleAndTextField(ConfigForTitleAnd config, Composite parent, String titleOrTitleKey, boolean titleIsKey) {
 		super(config, parent, titleOrTitleKey, titleIsKey);
 
 		txtText = new Text(this, SWT.BORDER);
 		txtText.setText("");
 		txtText.setEditable(false);
 
 		updateBackground();
 		int titleAndButtonsWidth = config.titleHeight + config.buttonsWidth;
 		RowData rowData = new RowData(titleAndButtonsWidth * 2, config.titleHeight);
 		txtText.setLayoutData(rowData);
 		txtText.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				txtText.setToolTipText(txtText.getText());
 			}
 		});
 	}
 
 	public void addEditButton() {
 		addCrListener(new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				txtText.setEditable(globalEditable);
 				updateBackground();
 			}
 		});
 		ImageButtons.addEditButton(this, new IImageButtonListener() {
 			@Override
 			public void buttonPressed(ImageButton button) {
 				txtText.setEditable(globalEditable || !txtText.getEditable());
 				updateBackground();
 			}
 		});
 	}
 
 	protected void updateBackground() {
 		if (txtText.getEditable())
			txtText.setBackground(config.editingBackground);
 		else
			txtText.setBackground(config.normalBackground);
 
 	}
 
 	public void setText(String rawText) {
 		String text = Strings.nullSafeToString(rawText);
 		txtText.setToolTipText(text);
 		txtText.setText(text);
 	}
 
 	public String getText() {
 		return txtText.getText();
 	}
 
 	public int getAsInteger() {
 		return Integer.parseInt(getText());
 	}
 
 	public void addModifyListener(ModifyListener listener) {
 		txtText.addModifyListener(listener);
 	}
 
 	@Override
 	public String toString() {
 		return "TitleAndTextField [title=" + getTitle() + ", text =" + txtText.getText() + "]";
 	}
 
 	public void addCrListener(Listener listener) {
 		txtText.addListener(SWT.DefaultSelection, listener);
 	}
 
 	public void setEditable(boolean globalEditable) {
 		this.globalEditable = globalEditable;
 		txtText.setEditable(globalEditable);
 		updateBackground();
 	}
 
 	public static void main(String[] args) {
 		Swts.display("TitleAndTextField", new IFunction1<Composite, Composite>() {
 			@Override
 			public Composite apply(Composite from) throws Exception {
 				TitleAndTextField titleAndTextField = new TitleAndTextField(ConfigForTitleAnd.createForBasics(from.getDisplay()), from, "Title");
 				titleAndTextField.addEditButton();
 				ImageButtons.addHelpButton(titleAndTextField, SwtBasicConstants.helpKey);
 				return titleAndTextField;
 			}
 		});
 	}
 }
