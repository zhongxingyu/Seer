 package org.eclipse.iee.sample.formula.pad;
 
 import java.util.Collection;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.iee.editor.core.pad.Pad;
 import org.eclipse.iee.editor.core.utils.console.ConsoleMessageEvent;
 import org.eclipse.iee.editor.core.utils.console.ConsoleMessager;
 import org.eclipse.iee.editor.core.utils.console.IConsoleMessageListener;
 import org.eclipse.iee.sample.formula.FormulaPadManager;
 import org.eclipse.iee.sample.formula.pad.hover.HoverShell;
 import org.eclipse.iee.sample.formula.storage.FileStorage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.custom.VerifyKeyListener;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 
 public class FormulaPad extends Pad {
 
 	private Composite fParent;
 
 	private Composite fInputView;
 	private Composite fResultView;
 
 	private Label fFormulaImageLabel;
 	private Label fLastResultImageLabel;
 
 	private StyledText fInputText;
 
 	private HoverShell fHoverShell;
 
 	private boolean fIsInputValid;
 
 	private String fOriginalExpression = "";
 	private String fTranslatingExpression = "";
 	private String fLastValidText = "";
 
 	public String getExpression() {
 		return fTranslatingExpression;
 	}
 
 	public void setExression(String expression) {
 		fTranslatingExpression = expression;
 	}
 
 	private final Color INPUT_VALID_COLOR = new Color(null, 255, 255, 255);
 	private final Color INPUT_INVALID_COLOR = new Color(null, 128, 255, 255);
 
 	private IConsoleMessageListener fConsoleMessageListener = new IConsoleMessageListener() {
 		@Override
 		public void messageReceived(ConsoleMessageEvent e) {
 			System.out.println("Message received:" + e.getMessage());
 			updateLastResult(e.getMessage());
 		}
 
 		@Override
 		public String getRequesterID() {
 			return getContainerID();
 		}
 	};
 
 	public FormulaPad() {
 	}
 
 	public FormulaPad(String containerID) {
 		super();
 	}
 
 	public void toggleInputText() {
 		// OFF
 		fResultView.setVisible(false);
 
 		// ON
 		fInputText.setText(fOriginalExpression);
 		fInputView.setVisible(true);
 
 		fParent.pack();
 
 		fInputText.forceFocus();
 	}
 
 	public void toggleFormulaImage() {
 		// OFF
 		fInputView.setVisible(false);
 
 		// ON
 		fResultView.setVisible(true);
 
 		fParent.pack();
 	}
 
 	public void setInputIsValid() {
 		fIsInputValid = true;
 		fInputText.setBackground(INPUT_VALID_COLOR);
 	}
 
 	public void setInputIsInvalid() {
 		fIsInputValid = false;
 		fInputText.setBackground(INPUT_INVALID_COLOR);
 	}
 
 	public void validateInput() {
 		String text = fInputText.getText();
 		fOriginalExpression = text;
 			
 		if (Translator.isTextValid(text) && FormulaRenderer.isTextValid(text)) {
 			setInputIsValid();
 			fLastValidText = text;
 		} else {
 			setInputIsInvalid();
 		}
 	}
 
 	public void processInput() {
 		if (fIsInputValid) {
 			if (!fInputText.getText().equals(fTranslatingExpression)) {
 				/* Remove result images from following pads */
 				Collection<Pad> following = FormulaPadManager
 						.getFollowingPads(this);
 
 				for (Pad pad : following) {
 					((FormulaPad) pad).updateLastResult("");
 				}
 			}
 			fTranslatingExpression = fInputText.getText();
 		}
 
 		/* Set formula image */
 		Image image = FormulaRenderer.getFormulaImage(fTranslatingExpression);
 		fFormulaImageLabel.setImage(image);
  
 		/* Generate code */
 		String generated = Translator.translateElement(fTranslatingExpression);
 
 		/* Add result output */
 		if (fTranslatingExpression.charAt(fTranslatingExpression.length() - 1) == '=')
 			generated += generateOutputCode(fTranslatingExpression);
 		getContainer().setTextContent(generated);
 	}
 
 	public String generateOutputCode(String expresion) {
 		Pattern p = Pattern.compile("\\s*\\w+\\s*=.+");
 		Matcher m = p.matcher(expresion);
 		if (m.matches()) {
 			String variable = expresion.substring(0, expresion.indexOf('='));
 			variable = variable.trim();
 			if (variable.charAt(0) != '[')
 			{
 				return "System.out.println(\"" + getContainerID() + "\" + "
 						+ variable + ");";
 			}
 			else
 			{
 				variable = variable.substring(1, variable.length() - 1);
 				return "System.out.print(\"" + getContainerID() + "\" + " + ");"+ variable + ".print(0,3);";
 			}
 		} else {
 			return "";
 		}
 	}
 
 	public void updateLastResult(String result) {
 		if (result == "") {
 			fLastResultImageLabel.setImage(null);
 			fParent.pack();
 			return;
 		}
 
		Image image = FormulaRenderer.getFormulaImage(result);
 		fLastResultImageLabel.setImage(image);
 		fParent.pack();
 	}
 
 	public void setListeners() {
 		ConsoleMessager.getInstance().addConsoleMessageListener(
 				fConsoleMessageListener);
 
 		fFormulaImageLabel.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				moveCaretToCurrentPad();
 				toggleInputText();
 			}
 
 			@Override
 			public void mouseDown(MouseEvent e) {
 				moveCaretToCurrentPad();
 			}
 
 			@Override
 			public void mouseUp(MouseEvent e) {
 			}
 		});
 
 		fInputText.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				if (fInputText.getText() != "")
 				{
 					validateInput();
 					Image image = FormulaRenderer.getFormulaImage(fInputText
 							.getText());
 					if (image == null)
 						image = FormulaRenderer.getFormulaImage(fLastValidText);
 					if (fHoverShell != null)
 						fHoverShell.dispose();
 					fHoverShell = new HoverShell(fParent, image);
 	
 					/* Resize fInputText */
 					Point size = fInputText.computeSize(SWT.DEFAULT, SWT.DEFAULT,
 							false);
 					fInputText.setSize(size);
 					fParent.pack();
 				}
 			}
 		});
 
 		fInputText.addVerifyKeyListener(new VerifyKeyListener() {
 			@Override
 			public void verifyKey(VerifyEvent e) {
 				switch (e.keyCode) {
 				case SWT.CR:
 					e.doit = false;
 					processInput();
 					moveCaretToCurrentPad();
 					if (fTranslatingExpression != "")
 						toggleFormulaImage();
 					fHoverShell.dispose();
 					break;
 
 				case SWT.ESC:
 					moveCaretToCurrentPad();
 					if (fTranslatingExpression != "")
 						toggleFormulaImage();
 					fHoverShell.dispose();
 					break;
 				}
 			}
 		});
 
 		fInputText.addFocusListener(new FocusListener() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				if (fTranslatingExpression != "")
 					toggleFormulaImage();
 				fHoverShell.dispose();
 			}
 
 			@Override
 			public void focusGained(FocusEvent e) {
 			}
 		});
 	}
 
 	@Override
 	public void createPartControl(final Composite parent) {
 		fParent = parent;
 
 		FillLayout layout = new FillLayout(SWT.HORIZONTAL);
 		parent.setLayout(layout);
 
 		SashForm sashForm = new SashForm(parent, SWT.FILL);
 		sashForm.setLayout(new FillLayout(SWT.HORIZONTAL));
 
 		/* Input View */
 
 		fInputView = new Composite(sashForm, SWT.NONE);
 		fInputView.setLayout(new GridLayout(1, true));
 
 		fInputText = new StyledText(fInputView, SWT.SINGLE | SWT.NONE);
 		GridData inputTextGridData = new GridData();
 		fInputText.setLayoutData(inputTextGridData);
 		fInputText.setSize(50, 100);
 
 		/* Result View */
 
 		fResultView = new Composite(sashForm, SWT.NONE);
 		fResultView.setLayout(new GridLayout(2, false));
 		fResultView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		fFormulaImageLabel = new Label(fResultView, SWT.NONE);
 		GridData formulaImageGridData = new GridData(SWT.FILL, SWT.FILL, true,
 				true);
 		fFormulaImageLabel.setLayoutData(formulaImageGridData);
 
 		fLastResultImageLabel = new Label(fResultView, SWT.NONE);
 		GridData lastResultImageGridData = new GridData(SWT.LEFT, SWT.FILL,
 				true, true);
 		fLastResultImageLabel.setLayoutData(lastResultImageGridData);
 
 		setListeners();
 
 		if (fTranslatingExpression != "" && fInputText.getText() != "") {
 			validateInput();
 			processInput();
 			toggleFormulaImage();
 		} else {
 			toggleInputText();
 		}
 
 	}
 
 	@Override
 	public void activate() {
 		toggleInputText();
 	}
 
 	@Override
 	public Pad copy() {
 		FormulaPad newPad = new FormulaPad();
 		newPad.fTranslatingExpression = this.fTranslatingExpression;
 		return newPad;
 	}
 
 	// Save&Load operations, use it for serialization
 
 	public void save() {
 		System.out.println("Saving...");
		//FileStorage.getInstance().saveToFile(this);
 	}
 
 	@Override
 	public void unsave() {
 		System.out.println("Unsaving...");
		//FileStorage.getInstance().removeFile(getContainerID());
 	}
 
 	@Override
 	public void onContainerAttached() {
 	}
 
 	@Override
 	public String getType() {
		return "Formula";
 	}
 }
