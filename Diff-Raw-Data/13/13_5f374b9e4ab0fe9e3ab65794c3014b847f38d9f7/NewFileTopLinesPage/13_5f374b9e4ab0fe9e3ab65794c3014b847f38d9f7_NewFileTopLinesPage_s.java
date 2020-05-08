 package us.pwc.vista.eclipse.wizard.core;
 
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 import us.pwc.vista.eclipse.core.validator.DateInputValidator;
 import us.pwc.vista.eclipse.core.validator.NumberInputValidator;
 import us.pwc.vista.eclipse.core.validator.RequiredInputValidator;
 import us.pwc.vista.eclipse.core.validator.TrueInputValidator;
 
 public class NewFileTopLinesPage extends WizardPage {
 	private static class TextData {
 		private IInputValidator validator;
 		private String labelText;
 		
 		public TextData(IInputValidator validator, String labelText) {
 			this.validator = validator;
 			this.labelText = labelText;
 		}
 		
 		public String isValid(String newText) {
 			String validatorResult = this.validator.isValid(newText);
 			if (validatorResult == null) {
 				return null;
 			} else {
 				return this.labelText + " " + validatorResult;
 			}
 		}
 	}
 	
 	private Composite container;
 	
 	private Text site;
 	private Text developer;
 	private Text beriefDescription;
 	private Text date;
 
 	private Text majorVersionNumber;
 	private Text minorVersionNumber;
 	private Text packageName;
 	private Text patchNumbers;
 	private Text versionDate;
 	private Text buildNumber;
 	
 	private Label firstLine;
 	private Label secondLine;
 	
 	public NewFileTopLinesPage(String pageName) {
 		super(pageName);
         setTitle("Routine Description");
         setDescription("Create first two lines.");
 	}
 	
 	@Override
 	public void createControl(Composite parent) {
 		this.container = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		this.container.setLayout(layout);
 		layout.numColumns = 2;
 		
 		this.site = this.createPair(this.container, "Site:", new RequiredInputValidator());
 		this.developer = this.createPair(this.container, "Developer:", new RequiredInputValidator());
 		this.beriefDescription = this.createPair(this.container, "Brief description:", new RequiredInputValidator());
 		this.date = this.createPair(this.container, "Date:", new DateInputValidator(true));
 		
 		this.majorVersionNumber = this.createPair(this.container, "Major version number:", new NumberInputValidator(true));
 		this.minorVersionNumber = this.createPair(this.container, "Minor version number:", new NumberInputValidator(true));
 		this.packageName = this.createPair(this.container, "Package name:", new RequiredInputValidator());
 		this.patchNumbers = this.createPair(this.container, "Patch number:", new TrueInputValidator());
 		this.versionDate =  this.createPair(this.container, "Version date:", new DateInputValidator(false));
 		this.buildNumber = this.createPair(this.container, "Build number:", new NumberInputValidator(false));
 		
 		this.createEmptyLine(this.container);
 		
 		this.firstLine =  this.createLinePair(this.container, "First line:");	
 		this.secondLine = this.createLinePair(this.container, "Second line:");	
 		
 		this.setControl(container);
 		this.updateLines();
 	    this.setPageComplete(false);
 	}
 	
 	private void createEmptyLine(Composite parent) {
 		new Label(parent, SWT.NONE);
 		new Label(parent, SWT.NONE);
 		
 	}
 	
 	private Label createLinePair(Composite parent, String labelText) {
 		Label label = new Label(parent, SWT.NONE);
 		label.setText(labelText);
 		
 		Label lineTarget =  new Label(this.container, SWT.NONE);
 		this.setFillHorizontalLayout(lineTarget);
 		return lineTarget;		
 	}
 	
 	private Text createPair(Composite parent, String labelText, IInputValidator validator) {
 		Label label = new Label(parent, SWT.NONE);
 		label.setText(labelText);
 
 		final Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);
 		text.setText("");
 		TextData data = new TextData(validator, labelText);
 		text.setData(data);
 		text.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				NewFileTopLinesPage.this.updateLines();
 				TextData textData = (TextData) text.getData();				
 				String errorMessage = textData.isValid(text.getText());
 				if (errorMessage == null) {
 					NewFileTopLinesPage.this.validate();	
 				} else {
 					NewFileTopLinesPage.this.setErrorMessage(errorMessage);
 					NewFileTopLinesPage.this.setPageComplete(false);
 				}
 			}
 		});
 		this.setFillHorizontalLayout(text);
 		return text;
 	}
 	
 	private void setFillHorizontalLayout(Control c) {
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		c.setLayoutData(gd);	
 	}
 	
 	private void validate() {
 		Control[] children = this.container.getChildren();
 		for (Control child : children) {
 			if (child instanceof Text) {
 				Text twv = (Text) child;
 				TextData data = (TextData) child.getData();
 				String errorMessage = data.isValid(twv.getText());
 				if (errorMessage != null) {
 					this.setErrorMessage(errorMessage);
 					this.setPageComplete(false);
 					return;
 				}
 			}
 		}
 		this.setErrorMessage(null);
 		this.setPageComplete(true);
 	}
 	
 	private void updateLines() {
 		this.updateFirstLine();
 		this.updateSecondLine();
 	}
 	
 	private void updateFirstLine() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(';');
 		sb.append(this.site.getText());
 		sb.append('/');
 		sb.append(this.developer.getText());
 		sb.append(" - ");
 		sb.append(this.beriefDescription.getText());
 		sb.append(';');
 		sb.append(this.date.getText());
 		String result = sb.toString();
 		this.firstLine.setText(result);
 	}
 
 	private void updateSecondLine() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(';');
 		sb.append(';');		
 		sb.append(this.majorVersionNumber.getText());
 		sb.append(".");
 		sb.append(this.minorVersionNumber.getText());
 		sb.append(';');				
 		sb.append(this.packageName.getText());
 		String patchNumbers = this.patchNumbers.getText();
 		if (! patchNumbers.isEmpty()) {
			sb.append(';');				
 			sb.append('*'); sb.append('*');				
 			sb.append(patchNumbers);				
 			sb.append('*'); sb.append('*');			
 		}
 		String versionDate = this.versionDate.getText();
 		if (! versionDate.isEmpty()) {
 			sb.append(';');				
 			sb.append(versionDate);
 		}
 		String build = this.buildNumber.getText();
 		if (! build.isEmpty()) {
 			sb.append(';');				
 			sb.append("Build ");
 			sb.append(build);
 		}
 		String result = sb.toString();
 		this.secondLine.setText(result);
 	}
 	
 	public String getFirstLine() {
 		return this.firstLine.getText();
 	}
 
 	public String getSecondLine() {
 		return this.secondLine.getText();
 	}
 }
