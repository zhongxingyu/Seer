 package pg13.presentation;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.SWT;
 import pg13.org.eclipse.wb.swt.SWTResourceManager;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.events.VerifyListener;
 
 import pg13.business.CryptogramManager;
 import pg13.models.Cryptogram;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.ModifyEvent;
 
 public class CryptogramLetterWidget extends Composite
 {
 	private Text txtPlaintextChar;
 	private char ciphertextChar;
 	private CryptogramManager cm;
 	private Composite parent;
 	private boolean updateOnTxtChange;
 
 	public CryptogramLetterWidget(Composite parent, int style,
 			Cryptogram parentCryptogram, char ciphertextChar)
 	{
 		super(parent, SWT.NONE);
 		setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		FormLayout formLayout = new FormLayout();
 		setLayout(formLayout);
 		this.ciphertextChar = ciphertextChar;
 		this.cm = new CryptogramManager(parentCryptogram);
 		this.parent = parent;
 		updateOnTxtChange = true;
 
 		// ciphertext character
 		Label lblCiphertextChar = new Label(this, SWT.NONE);
 		lblCiphertextChar.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		FormData fd_lblCiphertextChar = new FormData();
 		fd_lblCiphertextChar.bottom = new FormAttachment(100);
 		fd_lblCiphertextChar.right = new FormAttachment(100);
 		fd_lblCiphertextChar.top = new FormAttachment(50);
 		fd_lblCiphertextChar.left = new FormAttachment(0);
 		lblCiphertextChar.setLayoutData(fd_lblCiphertextChar);
 		lblCiphertextChar.setAlignment(SWT.CENTER);
 		lblCiphertextChar.setFont(SWTResourceManager.getFont("Segoe UI", 16,
 				SWT.NORMAL));
 		lblCiphertextChar.setText("" + ciphertextChar);
 
 		// area for the plaintext character entered by user
 		txtPlaintextChar = new Text(this, SWT.BORDER | SWT.CENTER);
 		txtPlaintextChar.addModifyListener(new ModifyListener()
 		{
 			public void modifyText(ModifyEvent event)
 			{
 				updateCryptogram();
 			}
 		});
 		txtPlaintextChar.setFont(SWTResourceManager.getFont("Segoe UI", 14,
 				SWT.NORMAL));
 		// txtPlaintextChar.set
 		FormData fd_txtPlaintextChar = new FormData();
 		fd_txtPlaintextChar.top = new FormAttachment(0);
 		fd_txtPlaintextChar.left = new FormAttachment(0);
 		fd_txtPlaintextChar.right = new FormAttachment(100);
 		fd_txtPlaintextChar.bottom = new FormAttachment(50);
 		txtPlaintextChar.setLayoutData(fd_txtPlaintextChar);
 
 		// if the ciphertext character is not a letter, then the plaintext
 		// character is given
 		if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(ciphertextChar) < 0)
 		{
 			txtPlaintextChar.setEditable(false);
 			txtPlaintextChar.setText("" + ciphertextChar);
 		} 
 		else
 		{
 			// limit characters that can be entered
 			txtPlaintextChar.setTextLimit(1);
 			txtPlaintextChar.addVerifyListener(new VerifyListener()
 			{
 				public void verifyText(VerifyEvent event)
 				{
 					try
 					{
 						event.text = event.text.toLowerCase();
 						cm.validateUserMapping(event.text);
 					}
 					catch(IllegalArgumentException e)
 					{
 						event.doit = false;
 					}
 				}
 			});
 		}
 
 	}
 
 	@Override
 	protected void checkSubclass()
 	{
 		// Disable the check that prevents subclassing of SWT components
 	}
 
 	public boolean isSpace()
 	{
 		return ciphertextChar == ' ';
 	}
 
 	private void updateCryptogram()
 	{
 		if (this.updateOnTxtChange == true
 				&& this.txtPlaintextChar.getEditable() == true)
 		{
 
 			String textBoxContents = this.txtPlaintextChar.getText();
 			char plaintextChar;
			if(textBoxContents.length() == 0)
 			{
 				plaintextChar = textBoxContents.charAt(0);
 			}
 			else
 			{
 				plaintextChar = '\0';
 			}
 
 			this.cm.setUserMapping(plaintextChar, this.ciphertextChar);
 
 			if (this.parent instanceof CryptogramSolveWidget)
 			{
 				((CryptogramSolveWidget) this.parent)
 						.updateLetterWidgetContents();
 			}
 		}
 	}
 
 	public void updateContents()
 	{
 		String plaintextChar;
 
 		if (txtPlaintextChar.getEditable() == true)
 		{
 			plaintextChar = this.cm
 					.getUserMapping(this.ciphertextChar);
 
 			this.updateOnTxtChange = false;
 			this.txtPlaintextChar.setText(plaintextChar);
 			this.updateOnTxtChange = true;
 
 		}
 	}
 }
