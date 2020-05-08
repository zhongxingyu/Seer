 package org.eclipse.iee.sample.formula.pad;
 
 import java.util.Collection;
 
 import org.eclipse.iee.editor.core.pad.Pad;
 import org.eclipse.iee.editor.core.utils.symbolic.SymbolicEngine;
 import org.eclipse.iee.sample.formula.FormulaPadManager;
 import org.eclipse.iee.sample.formula.pad.hover.HoverShell;
 import org.eclipse.iee.sample.formula.utils.FormulaRenderer;
 import org.eclipse.iee.sample.formula.utils.Function;
 import org.eclipse.jface.text.ITextListener;
 import org.eclipse.jface.text.TextEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Display;
 
 public class SymbolicPad extends FormulaPad {
 
 	public SymbolicPad() {
 		super();
 	}
 
 	@Override
 	public String getType() {
 		return "Symbolic";
 	}
 
 	public void validateInput() {
 		String text = fDocument.get();
 		fOriginalExpression = text;
 
 		if (SymbolicEngine.validate(text)) {
 			setInputIsValid();
 			fLastValidText = text;
 		} else {
 
 			setInputIsInvalid();
 		}
 	}
 
 	public void processInput() {
 		if (fIsInputValid) {
 			if (!fDocument.get().equals(fTranslatingExpression)) {
 				/* Remove result images from following pads */
 				Collection<Pad> following = FormulaPadManager
 						.getFollowingPads(this);
 
 				for (Pad pad : following) {
 					((SymbolicPad) pad).updateLastResult("");
 				}
 			}
 		}
 
 		fTranslatingExpression = fLastValidText;
 
 		/* Set formula image */
 		Image image = FormulaRenderer.getSymbolicImage(fTranslatingExpression);
 		fFormulaImageLabel.setImage(image);
 
 		/* Generate code */
 
 		String output = generateSymjaOutputCode(fTranslatingExpression);
 		updateLastResult(output);
 		getContainer().setValue(fOriginalExpression);
 
 	}
 
 	public String generateSymjaOutputCode(String expression) {
 		String variable = expression;
		char lastVariable = variable.charAt(fTranslatingExpression.trim()
 				.length() - 1);
 		if (lastVariable == '=')
			variable = expression.substring(0, expression.indexOf('='));
 		String output = "";
 
 		output = getContainer().getContainerManager().getSymbolicEngine()
 				.getOutput(variable);
 
 		if (lastVariable != '=')
 			output = "";
 
 		return output;
 	}
 
 	public void updateLastResult(String result) {
 		final Image image;
 		fResult = result;
 		if (result == "")
 			image = null;
 		else
 			image = FormulaRenderer.getSymbolicImage(result);
 
 		Function updateImage = new Function() {
 
 			@Override
 			public void f() {
 				if (!fLastResultImageLabel.isDisposed()) {
 					fLastResultImageLabel.setImage(image);
 					fParent.pack();
 				}
 			}
 		};
 
 		asyncUIUpdate(updateImage);
 
 	}
 
 	public void addTextListener() {
 
 		fViewer.addTextListener(new ITextListener() {
 
 			@Override
 			public void textChanged(TextEvent event) {
 
 				if (fDocument.get() != "") {
 
 					validateInput();
 
 					if (fHoverShell != null) {
 						fHoverShell.dispose();
 						fHoverShell = null;
 					}
 					// hack to paint hover image after widgets size
 					// recalculation.
 					Display.getCurrent().asyncExec(new Runnable() {
 						public void run() {
 							Image image = FormulaRenderer
 									.getSymbolicImage(fDocument.get());
 							if (image == null)
 								image = FormulaRenderer
 										.getSymbolicImage(fLastValidText);
 							fHoverShell = new HoverShell(fParent, image);
 						}
 					});
 					/* Resize fInputText */
 					Point size = fViewer.getControl().computeSize(SWT.DEFAULT,
 							SWT.DEFAULT, false);
 					fViewer.getControl().setSize(size);
 					fParent.pack();
 				}
 			}
 		});
 	}
 
 }
