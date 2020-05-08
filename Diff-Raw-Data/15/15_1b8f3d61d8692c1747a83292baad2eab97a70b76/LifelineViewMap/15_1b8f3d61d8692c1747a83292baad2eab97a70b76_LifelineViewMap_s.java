 package org.eclipse.uml2.diagram.sequence.figures;
 
 import org.eclipse.draw2d.AbstractLayout;
 import org.eclipse.draw2d.Border;
 import org.eclipse.draw2d.BorderLayout;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.FlowLayout;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.MarginBorder;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.draw2d.RectangleFigure;
 import org.eclipse.draw2d.TreeSearch;
 import org.eclipse.draw2d.XYLayout;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Insets;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.uml2.diagram.common.draw2d.PileLayout;
 import org.eclipse.uml2.diagram.sequence.draw2d.shadow.ShadowStealth;
 import org.eclipse.uml2.diagram.sequence.internal.layout.GeometryConstants;
 import org.eclipse.uml2.diagram.sequence.internal.layout.model.LMLifeLine;
 import org.eclipse.uml2.diagram.sequence.part.UMLDiagramEditorPlugin;
 
 
 public class LifelineViewMap {
 
 	public static class LifelineShape extends /*RectangleFigure*/InvisibleRectangle {//transparent for shdow master
 
 		private final LifelineTailShape myTail;
 
 		private final LifelineHeadShape myHead;
 
 		private final LifelineLayout myLifelineLayout;
 
 		public LifelineShape() {
 			setOutline(false);
 			setFill(false);
 
 			myLifelineLayout = new LifelineLayout();
 			setLayoutManager(myLifelineLayout);
 
 			myHead = new LifelineHeadShape(this);
 			add(myHead);
 
 			myTail = new LifelineTailShape(this);
 			add(myTail);
 			
 			myLifelineLayout.setHorizontalPositions(30, 30);
 		}
 
 		public LMLifeLine.DetailsLayouter getLifelineLayout() {
 			return myLifelineLayout;
 		}
 
 		public LifelineHeadShape getHead() {
 			return myHead;
 		}
 
 		public LifelineTailShape getTail() {
 			return myTail;
 		}
 
 		public IFigure findFigureAt(int x, int y, TreeSearch search) {
 			IFigure result = super.findFigureAt(x, y, search);
 			return (result == this) ? null : result;
 		}
 
 		public Label getLifelineStereotypesLabel() {
 			return getHead().getLifelineStereotypesLabel();
 		}
 
 		public Label getLifelineIdentLabel() {
 			return getHead().getLifelineIdentLabel();
 		}
 
 		public Label getLifelineDecomposedAsLabel() {
 			return getHead().getLifelineDecomposedAsLabel();
 		}
 
 		public Label getLifelineRefLabel() {
 			return getHead().getLifelineRefLabel();
 		}
 
 		public void setDecomposedAsBlockVisible(boolean visible) {
 			getHead().getLifelineDecomposedAsPanel().setVisible(visible);
 		}
 	}
 
 	public static class LifelineHeadShape extends RectangleFigure {
 
 		private static final Border HEAD_TEXT_MARGIN = new MarginBorder(5, 5, 0, 5);
 
 		private Label myStereotypesLabel;
 
 		private Label myLifelineIdentLabel;
 
 		private Label myDecomposedAsLabel;
 
 		private Label myRefLabel;
 
 		private IFigure myDecomposedAsPanel;
 
 		public LifelineHeadShape(IFigure lifeLineShape) {
 			setLineWidth(1);
 			setLifelineHeadText(this);
 			assert getLayoutManager() instanceof PileLayout;
 			if (getLayoutManager() == null) {
 				setLayoutManager(new PileLayout());
 			}
 
 			setBorder(new MarginBorder(0, 0, GeometryConstants.Lifeline.UNDER_HEAD_SPACE, 0));
 			setMinimumSize(new Dimension(GeometryConstants.Lifeline.MINIMUM_HEAD_WIDTH, GeometryConstants.Lifeline.HEIGHT + GeometryConstants.Lifeline.UNDER_HEAD_SPACE));
 		}
 
 		//Preferred size must be more than minimum size
 		public Dimension getPreferredSize(int wHint, int hHint) {
 			Dimension d = super.getPreferredSize(wHint, hHint);
 			return d.union(minSize);
 		}
 
 		public Label getLifelineStereotypesLabel() {
 			return myStereotypesLabel;
 		}
 
 		public Label getLifelineIdentLabel() {
 			return myLifelineIdentLabel;
 		}
 
 		public Label getLifelineDecomposedAsLabel() {
 			return myDecomposedAsLabel;
 		}
 
 		public Label getLifelineRefLabel() {
 			return myRefLabel;
 		}
 
 		public IFigure getLifelineDecomposedAsPanel() {
 			return myDecomposedAsPanel;
 		}
 
 		private void setLifelineHeadText(IFigure headFigure) {
 			headFigure.setLayoutManager(new PileLayout());
 
 			myStereotypesLabel = new Label();
 			add(myStereotypesLabel);
 
 			myLifelineIdentLabel = new Label();
 			myLifelineIdentLabel.setLabelAlignment(PositionConstants.CENTER);
 			myLifelineIdentLabel.setForegroundColor(ColorConstants.black);
 			myLifelineIdentLabel.setFont(UMLDiagramEditorPlugin.getInstance().getDefaultFont());
 			myLifelineIdentLabel.setBorder(HEAD_TEXT_MARGIN);
 
 			IFigure centeredLabel = new Figure();
 			centeredLabel.setLayoutManager(new BorderLayout());
 			centeredLabel.add(myLifelineIdentLabel, BorderLayout.CENTER);
 
 			headFigure.add(centeredLabel, PileLayout.ALIGN_CENTER);
 
 			myDecomposedAsPanel = new InvisibleRectangle();
 			myDecomposedAsPanel.setLayoutManager(new FlowLayout(true));
 
 			myRefLabel = new Label("ref  "); //$NON-NLS-1$
 			myRefLabel.setFont(UMLDiagramEditorPlugin.getInstance().getDefaultBoldFont());
 			myDecomposedAsPanel.add(myRefLabel);
 
 			myDecomposedAsLabel = new Label();
 			myDecomposedAsPanel.add(myDecomposedAsLabel);
 
 			//[#18037]container.setForegroundColor(LabelUtil.getForegroundColor(nodeEditPart, null));
 			myDecomposedAsPanel.setBorder(HEAD_TEXT_MARGIN);
 			//myDecomposedAsPanel.setHorizontalAligment(PositionConstants.CENTER);
 			//registerInplace(decomposeLabel, nodeEditPart, SD20_LifeLineImpl.STORED_DECOMPOSITION, "decomposedAs");
 			IFigure centeredContainer = new Figure();
 			centeredContainer.setLayoutManager(new BorderLayout());
 			centeredContainer.add(myDecomposedAsPanel, BorderLayout.CENTER);
 
 			headFigure.add(centeredContainer, PileLayout.ALIGN_CENTER);
 		}
 	}
 
 	public static class LifelineTailShape extends RectangleFigure implements ShadowStealth {
 
		public LifelineTailShape(LifelineShape lifelineShape) {
 			setOpaque(false);
 			setFill(false);
 			setLineStyle(Graphics.LINE_DASH);
 			setMinimumSize(new Dimension());//to avoid head shrinking
 			
			setLayoutManager(new XYLayout());
 		}
 
 		public IFigure findFigureAt(int x, int y, TreeSearch search) {
 			IFigure result = super.findFigureAt(x, y, search);
 			if (result == this) {
 				int distToLine = Math.abs(getLineAbsoluteX() - x);
 				if (distToLine >= getLineWidth() + SELECTABLE_WIDTH) {
 					result = null;
 				}
 			}
 			return result;
 		}
 
 		//CR#  20187  
 		//        public boolean containsPoint(int x, int y) {
 		//            int distToLine = Math.abs(myX - x);
 		//            if (distToLine >= getLineWidth() + SELECTABLE_WIDTH) {
 		//                return false;
 		//            }
 		//            return super.containsPoint(x, y);
 		//        }
 
 		protected void outlineShape(Graphics graphics) {
 			Rectangle bounds = getBounds();
 			graphics.drawLine(getLineAbsoluteX(), bounds.y, getLineAbsoluteX(), bounds.y + bounds.height);
 		}
 
 		private int getLineAbsoluteX() {
 			return bounds.x + myLineRelativeX;//don't assume that line is always centered, SD layout should set this position 
 		}
 
 		void setLineRelativeX(int x) {
 			myLineRelativeX = x;
 		}
 
 		private int myLineRelativeX;
 
 		/**
 		 * don't move children
 		 */
 		//      CR# 21137        
 		//        protected void primTranslate(int dx, int dy) {
 		//            bounds.x += dx;
 		//            bounds.y += dy;
 		//        }
 		private static final int SELECTABLE_WIDTH = 10;
 	}
 
 	private static class LifelineLayout extends AbstractLayout implements LMLifeLine.DetailsLayouter {
 
 		// LMLifeLine.DetailsLayouter methods:
 		public int getHeadPreferredWidth() {
 			return myHead.getPreferredSize().width;
 		}
 
 		public int getHeadPreferredHeight() {
 			return myHead.getPreferredSize().height;
 		}
 
 		public int getHeadTopPos() {
 			return myHead.getBounds().y;
 		}
 
 		public void setHorizontalPositions(int centerRelativePos, int headHalfWidth) {
 			myCenterRelativePos = centerRelativePos;
 			myHeadHalfWidth = headHalfWidth;
 			myHead.revalidate();
 
 			myTail.setLineRelativeX(centerRelativePos);
 			myTail.revalidate();
 		}
 
 		public int getCenterRelativePos() {
 			return myCenterRelativePos;
 		}
 
 		public int getHeadHalfWidth() {
 			return myHeadHalfWidth;
 		}
 
 		//------------------------------------------------------
 
 		protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
 			int minWHint = 0, minHHint = 0;
 			if (wHint < 0)
 				minWHint = -1;
 
 			if (hHint < 0)
 				minHHint = -1;
 
 			Insets border = container.getInsets();
 			wHint = Math.max(minWHint, wHint - border.getWidth());
 			hHint = Math.max(minHHint, hHint - border.getHeight());
 			Dimension prefSize = new Dimension();
 
 			Dimension headSize = myHead.getPreferredSize(wHint, hHint);
 			hHint = Math.max(minHHint, hHint - headSize.height);
 			prefSize.setSize(headSize);
 
 			Dimension tailSize = myTail.getPreferredSize(wHint, hHint);
 			prefSize.height += tailSize.height;
 			prefSize.width = Math.max(prefSize.width, tailSize.width);
 
 			return prefSize.expand(border.getWidth(), border.getHeight());
 		}
 
 		public void layout(IFigure container) {
 			Rectangle area = container.getClientArea();
 
 			Rectangle headBounds = new Rectangle(area);
 			headBounds.translate(myCenterRelativePos - myHeadHalfWidth, 0);
 			headBounds.width = 2 * myHeadHalfWidth;
 			Dimension headPreferredSize = myHead.getPreferredSize(headBounds.width, -1);
 			headBounds.height = headPreferredSize.height;
 			myHead.setBounds(headBounds);
 
 			area.y += headBounds.height;
 			area.height -= headBounds.height;
 			myTail.setBounds(area);
 		}
 
 		public void setConstraint(IFigure child, Object constraint) {
 			if (child instanceof LifelineHeadShape) {
 				myHead = child;
 			} else if (child instanceof LifelineTailShape) {
 				myTail = (LifelineTailShape) child;
 			} else {
 				throw new IllegalArgumentException();
 			}
 			super.setConstraint(child, constraint);
 		}
 
 		private IFigure myHead;
 
 		private LifelineTailShape myTail;
 
 		private int myCenterRelativePos;
 
 		private int myHeadHalfWidth;
 	}
 
 }
