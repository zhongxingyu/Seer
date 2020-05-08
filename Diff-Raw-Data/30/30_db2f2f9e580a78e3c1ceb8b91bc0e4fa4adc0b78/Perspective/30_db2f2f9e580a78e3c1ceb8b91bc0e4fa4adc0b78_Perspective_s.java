 package org.holbreich.lfgm;
 
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
import org.holbreich.lfgm.views.MainGameView;
 
 /**
  * 
  * Default perspective.
  * Nothing is here, because we try to configure Layouts.
  *
  * @author Alexander Holbreich (http://alexander.holbreich.org) 
  * @version $Rev: 1 $, ${date}$
  */
 public class Perspective implements IPerspectiveFactory {
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createInitialLayout(IPageLayout layout) {
 		layout.setEditorAreaVisible(false); // Remove Editor Area
		layout.setFixed(true);
		layout.addView(MainGameView.ID, IPageLayout.TOP, IPageLayout.RATIO_MAX, IPageLayout.ID_EDITOR_AREA);
 	}
 }
