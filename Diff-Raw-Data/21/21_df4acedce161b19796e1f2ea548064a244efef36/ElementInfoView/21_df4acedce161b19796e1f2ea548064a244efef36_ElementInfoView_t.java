 package org.fritzing.fritzing.diagram.views;
 
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gmf.runtime.notation.impl.DiagramImpl;
 import org.eclipse.gmf.runtime.notation.impl.EdgeImpl;
 import org.eclipse.gmf.runtime.notation.impl.NodeImpl;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.PaintObjectEvent;
 import org.eclipse.swt.custom.PaintObjectListener;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.GlyphMetrics;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.views.contentoutline.ContentOutline;
 import org.fritzing.fritzing.IElement;
 import org.fritzing.fritzing.ILegConnection;
 import org.fritzing.fritzing.IWireConnection;
 import org.fritzing.fritzing.Part;
 import org.fritzing.fritzing.Terminal;
 import org.fritzing.fritzing.diagram.edit.PartDefinition;
 import org.fritzing.fritzing.diagram.edit.parts.LegEditPart;
 import org.fritzing.fritzing.diagram.edit.parts.PartEditPart;
 import org.fritzing.fritzing.diagram.edit.parts.SketchEditPart;
 import org.fritzing.fritzing.diagram.edit.parts.Terminal2EditPart;
 import org.fritzing.fritzing.diagram.edit.parts.WireEditPart;
 import org.fritzing.fritzing.diagram.part.FritzingDiagramEditor;
 import org.fritzing.fritzing.impl.LegImpl;
 import org.fritzing.fritzing.impl.PartImpl;
 import org.fritzing.fritzing.impl.SketchImpl;
 import org.fritzing.fritzing.impl.TerminalImpl;
 import org.fritzing.fritzing.impl.WireImpl;
 
 public class ElementInfoView extends ViewPart implements ISelectionListener {
 	protected SimpleLinkTextViewer text;
 	protected ArrayList<EditPart> parts;
 	protected StyleRange titleStyle;
 	protected StyleRange imageStyle;
 	protected Image image;
 
 	public ElementInfoView() {
 		parts = new ArrayList<EditPart>();
 	}
 
 	public void createPartControl(Composite parent) {
 		text = new SimpleLinkTextViewer(parent, SWT.READ_ONLY);// | SWT.BORDER | SWT.WRAP);
 		text.getTextWidget().setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
 		
 		text.getTextWidget().addPaintObjectListener(new PaintObjectListener() {
 			public void paintObject(PaintObjectEvent event) {
 				GC gc = event.gc;
 				StyleRange style = event.style;
 				int x = event.x;
 				int y = event.y + event.ascent - style.metrics.ascent;						
 				gc.drawImage(image, x, y);
 			}
 		});
 		
 //		text.addMouseListener(new MouseListener() {
 //
 //			public void mouseDoubleClick(MouseEvent e) {
 //				Program p = Program.findProgram (".html");
 //				if (p != null) p.execute ("http://www.fritzing.org");
 //			}
 //
 //			public void mouseDown(MouseEvent e) {
 //			}
 //
 //			public void mouseUp(MouseEvent e) {
 //			}
 //		});
 		
 		imageStyle = new StyleRange (0, 0, null, null, SWT.NORMAL);
 		
 		titleStyle = new StyleRange(0, 0, null, null, SWT.BOLD);
 		FontData data = text.getTextWidget().getFont().getFontData()[0];
 		Font titleFont = new Font(Display.getCurrent(), data.getName(), data.getHeight() + 2, data.getStyle() | SWT.BOLD);
 		titleStyle.font = titleFont;
 				
 		getViewSite().getPage().addSelectionListener(this);
 	}
 
 	public void refresh() {
 		text.getTextWidget().setRedraw(false);
 		 // clear styles
 		StyleRange[] noStyles = {};
 		text.getTextWidget().setStyleRanges(noStyles);
		text.setText("");
 		
 		int len = parts.size();
 		if (len == 0) {
 			text.setText("");
 		} else {
 			if (len == 1) {
 				EditPart part = parts.get(0);
 				if (part instanceof PartEditPart) {
 					showPartInfo((PartEditPart)part);
 				}
 				if (part instanceof Terminal2EditPart) {
 					showTerminalInfo((Terminal2EditPart)part);
 				}
 				else if (part instanceof LegEditPart) {
 					showLegInfo((LegEditPart)part);							
 				}
 				else if (part instanceof WireEditPart) {
 					showWireInfo((WireEditPart)part);
 				}
 				else if (part instanceof SketchEditPart) {
 					showSketchInfo((SketchEditPart)part);
 				}
 			}
 			else {
				showMultipleInfo(parts);				
 			}
 		}
 		// apply styles
 		// replace is better, but we have to do it one by one because of the link styles
 //		StyleRange[] styles = {imageStyle, titleStyle};
 //		text.getTextWidget().replaceStyleRanges(0, text.getTextWidget().getCharCount(), styles);
 		if (image != null) {
 			Rectangle rect = image.getBounds();
 			imageStyle.metrics = new GlyphMetrics(rect.height, 0, rect.width);
 		}
 		text.getTextWidget().setStyleRange(imageStyle);
 		text.getTextWidget().setStyleRange(titleStyle);
 		text.getTextWidget().setRedraw(true);
 	}
 	
 	protected void showPartInfo(PartEditPart epPart) {
 		Part part = (Part) ((NodeImpl) ((PartEditPart) epPart).getModel()).getElement();
 		final PartDefinition pd = ((PartEditPart)epPart).getPartDefinition();
 		StringBuffer sb = new StringBuffer();
 		// TODO: cache icons
 		ImageDescriptor id = ImageDescriptor.createFromFile(null,
 				pd.getContentsPath() + pd.getIconSmallFilename());
 		image = id.createImage();
 		imageStyle.length = 1;
 		sb.append("  ");
 		titleStyle.start = sb.length();
 		sb.append(pd.getTitle().trim());
 		titleStyle.length = sb.length() - titleStyle.start;
 		sb.append(" " + part.getName() + "\n");
 		sb.append(pd.getDescription().trim() + "\n");
 		sb.append(part.getGenus() + "." + part.getSpecies() + "\n");
 		text.setText(sb.toString());
 		text.append("Version " + part.getVersion());		
 		Iterator<PartDefinition.Author> iAuthors = pd.getAuthors();
 		if (iAuthors.hasNext()) {
			text.append("\nAuthor");
			if (pd.getAuthorsNum() > 1) {
				text.append("s");
			}
			text.append(": ");
 			while (iAuthors.hasNext()) {
 				final PartDefinition.Author a = iAuthors.next();
 				if (a.url != null) {
 					text.appendLink(a.name, new Runnable() {
 						public void run() {
 							Program p = Program.findProgram (".html");
 							if (p != null) p.execute (a.url.toString());
 						}
 					});
 				} else {
 					text.append(a.name);
 				}
 				if (iAuthors.hasNext())
 					text.append(", ");
 			}
 		}
 		text.append("\n");
 		text.appendLink("Reference", new Runnable() {
 			public void run() {
 				Program p = Program.findProgram (".html");
 				if (p != null) p.execute (pd.getReference().toString());
 			}
 		});
 	}
 
 	protected void showTerminalInfo(Terminal2EditPart epTerminal) {
 		TerminalImpl terminal = (TerminalImpl) ((NodeImpl) ((EditPart) epTerminal).getModel()).getElement();		
 		Part parent = terminal.getParent();
 		StringBuffer sb = new StringBuffer();
 		imageStyle.length = 0;
 		titleStyle.start = 0;
 		titleStyle.length = appendName(sb, terminal);
 		sb.append("\n of ");
 		appendName(sb, parent);
 		text.setText(sb.toString());
 	}
 
 	protected void showLegInfo(LegEditPart epLeg) {
 		LegImpl leg = (LegImpl) ((EdgeImpl) ((EditPart) epLeg).getModel()).getElement();		
 		Terminal source = (Terminal)(leg.getSource());
 		ILegConnection target = leg.getTarget();
 		StringBuffer sb = new StringBuffer();
 		imageStyle.length = 0;
 		titleStyle.start = 0;
 		titleStyle.length = appendName(sb, leg);
 		sb.append("\n of ");
 		appendName(sb, source.getParent());
 		sb.append("\n to ");
 		appendName(sb, target);
 		text.setText(sb.toString());
 	}
 
 	protected void showWireInfo(WireEditPart epWire) {
 		WireImpl wire = (WireImpl) ((EdgeImpl) ((EditPart) epWire).getModel()).getElement();
 		IWireConnection source = wire.getSource();
 		IWireConnection target = wire.getTarget();
 		StringBuffer sb = new StringBuffer();
 		imageStyle.length = 0;
 		titleStyle.start = 0;
 		titleStyle.length = appendName(sb, wire);
 		sb.append("\n from ");
 		appendName(sb, source);
 		sb.append("\n to ");
 		appendName(sb, target);
 		text.setText(sb.toString());
 	}
 
 	protected void showSketchInfo(SketchEditPart epSketch) {
 		SketchImpl sketch = (SketchImpl) ((DiagramImpl) ((EditPart) epSketch).getModel()).getElement();
 		StringBuffer sb = new StringBuffer();
 		imageStyle.length = 0;
 		titleStyle.start = 0;
 		titleStyle.length = appendName(sb, sketch);
 		sb.append("\n" + sketch.getParts().size() + " parts");
 		sb.append("\n" + sketch.getWires().size() + " wires");
 		text.setText(sb.toString());
 	}

	protected void showMultipleInfo(ArrayList<EditPart> parts) {
		// multiple parts selected
		imageStyle.start = 0;
		imageStyle.length = 0;
		titleStyle.start = 0;
		titleStyle.length = 0;
		text.setText("");
	}
 	
 	
 	protected int appendName(StringBuffer sb, IElement il) {
 		if (il instanceof SketchImpl) {	
 			sb.append("Sketch ");
 			String s = il.getName();
 			if (s != null) {
 				sb.append(s);
 			}
 			return 6;
 		} 
 		else if (il instanceof PartImpl) {
 			String type = ((PartImpl)il).getSpecies();
 			sb.append(type + " ");
 			String s = il.getName();
 			if (s != null) {
 				sb.append(s);
 			}
 			return type.length();		
 		}
 		else if (il instanceof LegImpl) {
 			sb.append("Leg ");
 			String s = ((LegImpl)il).getSource().getName();
 			if (s != null) {
 				sb.append(s);
 			}
 			return 3;
 		}
 		else if (il instanceof TerminalImpl) {
 			sb.append("Terminal ");
 			String s = il.getName();
 			if (s != null) {
 				sb.append(s);
 			}
 			return 8;
 		}
 		else if (il instanceof WireImpl) {
 			sb.append("Wire ");
 			String s = il.getName();
 			if (s != null) {
 				sb.append(s);
 			}
 			return 4;
 		}
 		return 0;
 	}
 	
 
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 		if (!(selection instanceof StructuredSelection)) return;
 		boolean passThru = false;
 		if (part instanceof FritzingDiagramEditor) {
 			passThru = true;
 		}
 		else if (part instanceof ContentOutline) {
 			if (selection.isEmpty()) return;	
 			passThru = true;
 		}			
 		if (passThru) {
 			inputChanged(null, selection);
 		}		
 	}
 	
 	public void inputChanged(Object oldInput, Object newInput) {
 		if (!(newInput instanceof StructuredSelection)) return;
 		
 		parts.clear();
 		for (Iterator it = ((StructuredSelection) newInput).iterator(); it.hasNext(); ) {
 			Object editPart = it.next();
 			if (editPart instanceof EditPart) {
 				parts.add((EditPart) editPart);					
 			}
 		}
 		refresh();
 	}
 	
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		text.getTextWidget().setFocus();
 	}
 
 	public void dispose() {
 		super.dispose();
 	}
 }
