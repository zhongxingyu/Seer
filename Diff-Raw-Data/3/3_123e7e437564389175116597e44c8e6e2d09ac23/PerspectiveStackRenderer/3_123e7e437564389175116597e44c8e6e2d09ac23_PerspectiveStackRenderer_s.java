 package org.eclipse.riena.e4.launcher.rendering;
 
 import org.eclipse.e4.ui.model.application.ui.MUIElement;
 import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.navigation.ui.swt.lnf.renderer.SubModuleViewRenderer;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 
 /**
  * {@link StackRenderer} with modified {@link CTabFolder}
  */
 public class PerspectiveStackRenderer extends StackRenderer {
 
 	@Override
 	public Object createWidget(final MUIElement element, final Object parent) {
 		final RienaTabFolder widget = new RienaTabFolder((Composite) parent, SWT.BORDER);// super.createWidget(element, parent);
 		bindWidget(element, widget);
 		ReflectionUtils.invokeHidden(this, "addTopRight", widget); //$NON-NLS-1$
 		final CTabFolder folder = widget;
 		folder.setTabHeight(0);
 		folder.setMaximizeVisible(false);
 		folder.setMinimizeVisible(false);
 		folder.setBorderVisible(false);
 		folder.addPaintListener(new BorderPaintListener());
 		return widget;
 	}
 
 	private class RienaTabFolder extends CTabFolder {
 
 		public RienaTabFolder(final Composite parent, final int style) {
 			super(parent, style);
 		}
 
 		@Override
 		public void setSelection(final int index) {
 			super.setSelection(index);
 			final Control control = getItem(index).getControl();
 			control.setVisible(true);
 			control.setBounds(getClientArea());
 			showItem(getItem(index));
 			redraw();
 		}
 
 	}
 
 	private static class BorderPaintListener implements PaintListener {
 		private SubModuleViewRenderer renderer;
 
 		public void paintControl(final PaintEvent e) {
 			final SubModuleViewRenderer viewRenderer = getRenderer();
 			if (viewRenderer != null) {
 				final Rectangle bounds = ((Control) e.widget).getParent().getClientArea();
 				viewRenderer.setBounds(bounds);
 				viewRenderer.paint(e.gc, null);
 			}
 		}
 
 		/**
 		 * Returns the renderer of the sub-module view.<br>
 		 * Renderer renders the border of the sub-module view and not the content of the view.
 		 * 
 		 * @return renderer of sub-module view
 		 */
 		private SubModuleViewRenderer getRenderer() {
 			if (renderer == null) {
 				renderer = (SubModuleViewRenderer) LnfManager.getLnf().getRenderer("SubModuleView.renderer"); //$NON-NLS-1$
 			}
 			return renderer;
 		}
 	}
 }
