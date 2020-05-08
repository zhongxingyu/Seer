 package org.jboss.tools.fuse.reddeer.editor;
 
 import java.util.List;
 
 import org.eclipse.draw2d.Clickable;
 import org.eclipse.draw2d.FigureCanvas;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.ui.palette.PaletteViewer;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Control;
 import org.hamcrest.Matcher;
 import org.hamcrest.core.IsInstanceOf;
 import org.jboss.reddeer.swt.condition.JobIsRunning;
 import org.jboss.reddeer.swt.condition.ShellWithTextIsActive;
 import org.jboss.reddeer.swt.impl.button.PushButton;
 import org.jboss.reddeer.swt.util.Display;
 import org.jboss.reddeer.swt.util.ResultRunnable;
 import org.jboss.reddeer.swt.wait.WaitUntil;
 import org.jboss.reddeer.swt.wait.WaitWhile;
 import org.jboss.reddeer.workbench.editor.DefaultEditor;
import org.jboss.reddeer.workbench.view.impl.WorkbenchView;
 import org.jboss.tools.fuse.reddeer.editor.finder.EditPartFinder;
 import org.jboss.tools.fuse.reddeer.editor.finder.FigureFinder;
 import org.jboss.tools.fuse.reddeer.editor.matcher.All;
 import org.jboss.tools.fuse.reddeer.editor.matcher.WithLabel;
 import org.jboss.tools.fuse.reddeer.editor.matcher.WithTooltip;
 import org.jboss.tools.fuse.reddeer.utils.BoundsCalculation;
 import org.jboss.tools.fuse.reddeer.utils.MouseUtils;
 
 /**
  * 
  * @author apodhrad
  * 
  */
 public class GefEditor extends DefaultEditor {
 
 	protected GraphicalViewer viewer;
 
 	public GefEditor() {
 		super();
 		init();
 	}
 
 	public GefEditor(String title) {
 		super(title);
 		init();
 	}
 
 	private void init() {
 		viewer = Display.syncExec(new ResultRunnable<GraphicalViewer>() {
 			@Override
 			public GraphicalViewer run() {
 				return (GraphicalViewer) getEditorPart().getAdapter(GraphicalViewer.class);
 			}
 
 		});
 		if (viewer == null) {
 			throw new RuntimeException("Cannot find graphical viewer which is needed for GEF");
 		}
 	}
 
 	public FigureCanvas getFigureCanvas() {
 		return (FigureCanvas) viewer.getControl();
 	}
 
 	public void printAllFigures() {
 		IFigure parent = getFigureCanvas().getContents();
 		List<IFigure> list = new FigureFinder().find(parent, new All());
 		for (IFigure figure : list) {
 			System.out.println(figure.getClass());
 			if (figure instanceof Label) {
 				System.out.println("> Label: " + ((Label) figure).getText());
 			}
 			IFigure tooltip = figure.getToolTip();
 			if (tooltip instanceof Label) {
 				System.out.println("> Tooltip: " + ((Label) tooltip).getText());
 			}
 		}
 	}
 
 	protected List<EditPart> getEditParts(final Matcher<?> matcher) {
 		return Display.syncExec(new ResultRunnable<List<EditPart>>() {
 			@Override
 			public List<EditPart> run() {
 				EditPart root = viewer.getContents();
 				return new EditPartFinder().find(root, matcher);
 			}
 
 		});
 	}
 
 	protected EditPart getEditPartWithLabel(String label) {
 		return getEditPartWithLabel(label, 0);
 	}
 
 	protected EditPart getEditPartWithLabel(String label, int index) {
 		List<EditPart> list = getEditParts(new WithLabel(label));
 		if (list.isEmpty()) {
 			throw new RuntimeException("Cannot find edit part with label '" + label + "'");
 		}
 		return list.get(index);
 	}
 
 	protected void select(final EditPart editPart) {
 		Display.syncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				viewer.select(editPart);
 			}
 		});
 	}
 
 	protected void hover(final EditPart editPart) {
 		hover(getFigure(editPart));
 	}
 
 	protected void hover(final IFigure figure) {
 		hover(figure, 1);
 	}
 
 	protected void hover(final IFigure figure, int count) {
 		if (count > 10) {
 			throw new RuntimeException("Hover doesn't show any context button!");
 		}
 		Rectangle rec = BoundsCalculation.getAbsoluteBounds(getFigureCanvas(), figure);
 		final Point centralPoint = BoundsCalculation.getCentralPoint(rec);
 		MouseUtils.click(centralPoint.x, centralPoint.y);
 		MouseUtils.mouseMove(centralPoint.x, centralPoint.y);
 
 		IFigure parent = getFigureCanvas().getContents();
 		List<IFigure> list = new FigureFinder().find(parent, new IsInstanceOf(Clickable.class));
 		if (list.isEmpty()) {
 			hover(figure, count + 1);
 		}
 	}
 
 	private IFigure getFigure(EditPart editPart) {
 		return ((GraphicalEditPart) editPart).getFigure();
 	}
 
 	public void selectEditPartWithLabel(String label) {
 		EditPart editPart = getEditPartWithLabel(label);
 		select(editPart);
 	}
 
 	public void selectEditPartWithTooltip(String tooltip) {
 		List<EditPart> list = getEditParts(new WithTooltip(tooltip));
 		if (list.isEmpty()) {
 			throw new RuntimeException("Cannot find edit part with tooltip '" + tooltip + "'");
 		}
 		select(list.get(0));
 	}
 
 	public void hoverEditPartWithLabel(String label) {
 		EditPart editPart = getEditPartWithLabel(label);
 		hover(editPart);
 	}
 
 	public void hoverEditPartWithTooltip(String tooltip) {
 		List<EditPart> list = getEditParts(new WithTooltip(tooltip));
 		if (list.isEmpty()) {
 			throw new RuntimeException("Cannot find edit part with tooltip '" + tooltip + "'");
 		}
 		hover(list.get(0));
 	}
 
 	public void deleteEditPartWithLabel(String label) {
 		hoverEditPartWithLabel(label);
 		new ContextButton("Delete").click();
 		String deleteShellText = "Confirm Delete";
 		new WaitUntil(new ShellWithTextIsActive(deleteShellText));
 		new PushButton("Yes").click();
 		new WaitWhile(new ShellWithTextIsActive(deleteShellText));
 		new WaitWhile(new JobIsRunning());
 	}
 
 	public void deleteEditPartWithTooltip(String tooltip) {
 		hoverEditPartWithTooltip(tooltip);
 		new ContextButton("Delete").click();
 		String deleteShellText = "Confirm Delete";
 		new WaitUntil(new ShellWithTextIsActive(deleteShellText));
 		new PushButton("Yes").click();
 		new WaitWhile(new ShellWithTextIsActive(deleteShellText));
 		new WaitWhile(new JobIsRunning());
 	}
 
 	public Palette getPalette() {
		new WorkbenchView("General", "Palette").open();
 		PaletteViewer paletteViewer = viewer.getEditDomain().getPaletteViewer();
 		return new Palette(paletteViewer);
 	}
 
 	public void click(int x, int y) {
 		Control control = viewer.getControl();
 		Rectangle rec = BoundsCalculation.getAbsoluteBounds(control);
 		MouseUtils.click(rec.x + x, rec.y + y);
 	}
 
 }
