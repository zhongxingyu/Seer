 package org.arc4eclipse.swtBasics.text;
 
 import org.arc4eclipse.swtBasics.Swts;
 import org.arc4eclipse.swtBasics.images.IImageFactory;
 import org.arc4eclipse.utilities.functions.IFunction1;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.widgets.Composite;
 
 public class TitleAndStyledTextField extends AbstractTitleAnd {
 
 	private final StyledText txtText;
 
 	public TitleAndStyledTextField(Composite arg0, int arg1, IImageFactory imageFactory, String title) {
 		super(arg0, arg1, imageFactory, title);
 		txtText = new StyledText(this, SWT.BORDER);
 		txtText.setLayoutData(new RowData(400, 400));
		setLayout(new FormLayout());

		FormData lblLayoutData = new FormData();
		lblLayoutData.top = new FormAttachment(0, 0);
		lblLayoutData.left = new FormAttachment(0, 0);
		compTitleAndButtons.setLayoutData(lblLayoutData);

		FormData txtLayoutData = new FormData();
		txtLayoutData.top = new FormAttachment(0, 31);
		txtLayoutData.left = new FormAttachment(0, 0);
		txtLayoutData.bottom = new FormAttachment(100, 0);
		txtLayoutData.right = new FormAttachment(100, 0);
		txtText.setLayoutData(txtLayoutData);
 		// Swts.addGrabHorizontalAndFillGridDataToAllChildren(this);
 	}
 
 	public void setText(final String text) {
 		getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				txtText.setText(text);
 			}
 		});
 	}
 
 	public void appendText(final String text) {
 		getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				txtText.append(text);
 			}
 		});
 	}
 
 	public String getText() {
 		return txtText.getText();
 	}
 
 	public static void main(String[] args) {
 		Swts.display("TitleAndStyledText", new IFunction1<Composite, Composite>() {
 
 			@Override
 			public Composite apply(Composite from) throws Exception {
 				return new TitleAndStyledTextField(from, SWT.NULL, IImageFactory.Utils.imageFactory(), "Title");
 			}
 		});
 	}
 }
