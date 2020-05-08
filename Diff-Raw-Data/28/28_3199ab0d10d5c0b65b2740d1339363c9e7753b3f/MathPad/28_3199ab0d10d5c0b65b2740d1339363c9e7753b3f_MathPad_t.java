 package org.eclipse.iee.sample.math.pad;
 
 import java.io.*;
 
 import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import java.awt.Color;
 import javax.swing.text.BadLocationException;
 
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.iee.editor.core.pad.Pad;
 import org.matheclipse.core.eval.EvalEngine;
 import org.matheclipse.core.eval.EvalUtilities;
 import org.matheclipse.core.eval.TeXUtilities;
 import org.matheclipse.core.expression.F;
 import org.matheclipse.core.form.output.OutputFormFactory;
 import org.matheclipse.core.form.output.StringBufferWriter;
 import org.matheclipse.core.interfaces.IExpr;
 import org.scilab.forge.jlatexmath.TeXConstants;
 import org.scilab.forge.jlatexmath.TeXFormula;
 
 public class MathPad extends Pad {
 
 	private String fImagePath;
 	private String fExpression;
 
 	private boolean fIsTextVisible;
 	private Image fImage;
 
 	public MathPad() {
 		super();
 		fImagePath = this.getContainerID() + ".jpg";
 		fExpression = "";
 		fIsTextVisible = true;
 		fImage = null;
 	}
 
 	@Override
 	public void createPartControl(final Composite parent) {
		parent.setLayout(new FillLayout());
 		FormLayout layout = new FormLayout();
 		layout.marginBottom = 3;
 		layout.marginRight = 3;
 		layout.marginLeft = 3;
 		layout.marginTop = 3;
 		Group mathPad = new Group(parent, SWT.NONE);
 		mathPad.setText("Sample math Pad");
 		mathPad.setLayout(layout);
 		mathPad.setToolTipText("Press DoubleClick on expression for calculation");
 
 		final Text text = new Text(mathPad, SWT.BORDER | SWT.H_SCROLL);
 		text.setText(fExpression);
 		FormData textFormData = new FormData();
 		textFormData.top = new FormAttachment(20);
		//textFormData.left = new FormAttachment(50, -50);
		//textFormData.right = new FormAttachment(50, 50);
 		text.setLayoutData(textFormData);
 		text.setVisible(fIsTextVisible);
 
		final Label label = new Label(mathPad, SWT.NONE|SWT.RESIZE);
		label.setText(fExpression);
 		FormData labelFormData = new FormData();
 		labelFormData.top = new FormAttachment(20);
		//labelFormData.left = new FormAttachment(50, -50);
		//labelFormData.right = new FormAttachment(50, 50);
 		label.setLayoutData(labelFormData);
 
 		if (!fImagePath.isEmpty()) {
 			File imageFile = new File(fImagePath);
 
 			if (imageFile.exists()) {
 				try {
 					fImage = new Image(parent.getDisplay(), fImagePath);
 				} catch (Exception exception) {
 					exception.printStackTrace();
 				}
 				label.setImage(fImage);
 				parent.pack();
 			}
 		}
 
 		label.setVisible(!fIsTextVisible);
 
 		parent.pack();
 
 		// Listeners
 
 		text.addModifyListener(new ModifyListener() {
 
 			@Override
 			public void modifyText(ModifyEvent e) {
 				fExpression = text.getText();
 			}
 		});
 
 		text.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseUp(MouseEvent e) {
 
 			}
 
 			@Override
 			public void mouseDown(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				if (fIsTextVisible) {
 					fIsTextVisible = false;
 					text.setVisible(fIsTextVisible);
 					fExpression = text.getText();
 					String output = "";
 					try {
 						F.initSymbols(null);
 						EvalUtilities util = new EvalUtilities();
 						IExpr result = null;
 						StringBufferWriter buf = new StringBufferWriter();
 
 						if (fExpression.isEmpty())
 							result = util.evaluate("Empty string");
 						else
 							result = util.evaluate(fExpression);
 						OutputFormFactory.get().convert(buf, result);
 
 						output = buf.toString();
 
 						/* Convert to TeX and image */
 						EvalEngine engine = new EvalEngine();
 						TeXUtilities texUtil = new TeXUtilities(engine);
 
 						StringWriter stw = new StringWriter();
 						texUtil.toTeX(result, stw);
 						output = stw.toString();
 						fExpression = output;
 
 						TeXFormula formula = new TeXFormula(stw.toString());
 						/* */
 						formula.createJPEG(TeXConstants.STYLE_DISPLAY, 20,
 								fImagePath, Color.white, Color.black);
 
 					} catch (BadLocationException e1) {
 						e1.printStackTrace();
 					} catch (IOException e2) {
 						e2.printStackTrace();
 					} catch (Exception e3) {
 						e3.printStackTrace();
 					}
 					if (fImage != null)
 						if (!fImage.isDisposed()) {
 							fImage.dispose();
 							fImage = null;
 						}
 					try {
 						fImage = new Image(parent.getDisplay(), fImagePath);
 					} catch (Exception exception) {
 
 						exception.printStackTrace();
 
 					}
 					label.setImage(fImage);
					System.out.println("width:" + fImage.getBounds().width);
					System.out.println("heigth:" + fImage.getBounds().height);
					label.setSize(fImage.getBounds().width, fImage.getBounds().height);
 					label.setVisible(!fIsTextVisible);
 					
 					parent.pack();
 				}
 			}
 		});
		
 		label.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseUp(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseDown(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				if (!fIsTextVisible) {
 					fIsTextVisible = true;
 					text.setText(fExpression);
 					text.setVisible(fIsTextVisible);
 					label.setVisible(!fIsTextVisible);
 					parent.pack();
 				}
 			}
 		});
 
 	}
 
 	protected MathPad(String containerID) {
 		super(containerID);
 		fImagePath = containerID + ".jpg";
 		fExpression = "";
 		fIsTextVisible = true;
 		fImage = null;
 	}
 
 	@Override
 	public Pad copy() {
 		MathPad newPad = new MathPad();
 		newPad.fExpression = this.fExpression;
 		newPad.fIsTextVisible = this.fIsTextVisible;
 		File inputFile = new File(this.fImagePath);
 		File outputFile = new File(newPad.fImagePath);
 
 		try {
 			BufferedInputStream bis = new BufferedInputStream(
 					new FileInputStream(inputFile), 4096);
 			BufferedOutputStream bos = new BufferedOutputStream(
 					new FileOutputStream(outputFile), 4096);
 			int buffer;
 			while ((buffer = bis.read()) != -1) {
 				bos.write(buffer);
 			}
 			bos.close();
 			bis.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return newPad;
 	}
 
 	@Override
 	public String getType() {
 		return "Math";
 	}
 
 }
