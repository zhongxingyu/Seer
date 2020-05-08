 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 
 
 public class SlideItem extends Composite {
 
 	private Slide mySlide;
 
 	public SlideItem(Composite parent, int style, Slide slide) {
		super(parent, style);
 		mySlide = slide;
 
		Composite slideComposite = new Composite(parent, SWT.BORDER);
		slideComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
 
		final Canvas slideCanvas = new Canvas(slideComposite, SWT.BORDER);
 		slideCanvas.setSize(100, 100);
 		slideCanvas.addPaintListener(new PaintListener() {
 			
 			@Override
 			public void paintControl(PaintEvent e) {
 				GC gc = e.gc;
 				Image image = mySlide.getContent();
 				gc.drawImage(image, 0, 0, image.getBounds().width,
 						image.getBounds().height, 0, 0,
 						slideCanvas.getBounds().width,
 						slideCanvas.getBounds().height);
 				gc.dispose();
 			}
 		});
 
		Composite notesComposite = new Composite(slideComposite, SWT.NONE);
 		notesComposite.setLayout(new RowLayout(SWT.VERTICAL));
 
 		Button addTextButton = new Button(notesComposite, SWT.PUSH);
 		addTextButton.setEnabled(false);
 		addTextButton.setText("add Text Note");
 
 		Button addImageButton = new Button(notesComposite, SWT.PUSH);
 		addImageButton.setEnabled(false);
 		addImageButton.setText("add Image Note");
 
 		Button addPdfButton = new Button(notesComposite, SWT.PUSH);
 		addPdfButton.setEnabled(false);
 		addPdfButton.setText("add Pdf Note");
 	}
 }
