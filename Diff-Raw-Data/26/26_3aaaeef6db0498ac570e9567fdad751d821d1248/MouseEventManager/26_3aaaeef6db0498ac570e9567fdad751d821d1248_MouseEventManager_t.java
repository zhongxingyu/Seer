 package org.eclipse.iee.sample.image.pad;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.MouseTrackListener;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 
 public class MouseEventManager implements MouseListener, MouseMoveListener,
 		MouseTrackListener {
 
 	private Cursor fResizeCursorNS;
 	private Cursor fResizeCursorEW;
 	private Cursor fArrowCursor;
 	private Cursor fResizeCursorNWSE;
 	private Cursor fResizeCursorNESW;
 	private boolean fIsCursorCanBeChanged;
 	private Composite fComposite;
 	private Label fLabel;
 	private boolean fCanResize;
 	private boolean fIsResizing;
 	private boolean fChangeX;
 	private boolean fChangeY;
 
 	public MouseEventManager(final Composite composite, final Label label) {
 		fComposite = composite;
 		fLabel = label;
 		fResizeCursorNS = new Cursor(null, SWT.CURSOR_SIZENS);
 		fResizeCursorEW = new Cursor(null, SWT.CURSOR_SIZEWE);
 		fArrowCursor = new Cursor(null, SWT.CURSOR_ARROW);
 		fResizeCursorNWSE = new Cursor(null, SWT.CURSOR_SIZENWSE);
 		fResizeCursorNESW = new Cursor(null, SWT.CURSOR_SIZENESW);
 		fIsCursorCanBeChanged = true;
 		fCanResize = false;
 		fIsResizing = false;
 		fChangeX = false;
 		fChangeY = false;
 	}
 
 	@Override
 	public void mouseEnter(MouseEvent e) {
 		fComposite.forceFocus();
 	}
 
 	@Override
 	public void mouseExit(MouseEvent e) {
 		fComposite.setCursor(fArrowCursor);
 		fCanResize = false;
 		fChangeX = false;
 		fChangeY = false;
 	}
 
 	@Override
 	public void mouseHover(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseMove(MouseEvent e) {
 		int delta = 5;
 		Point border = fComposite.getSize();
 		for (int i = 0; i <= delta; i++) {
 			if (e.y == i && e.x == i) {
 				if (fIsCursorCanBeChanged) {
 					fComposite.setCursor(fResizeCursorNWSE);
 					fCanResize = true;
 					fChangeX = false;
 					fChangeY = false;
 				}
 			}
 
 			if (e.y == border.y - i && e.x == border.x - i) {
 				if (fIsCursorCanBeChanged) {
 					fComposite.setCursor(fResizeCursorNWSE);
 					fCanResize = true;
 					fChangeX = true;
 					fChangeY = true;
 				}
 			}
 
 			if (e.x == i && e.y == border.y - i) {
 				if (fIsCursorCanBeChanged) {
 					fComposite.setCursor(fResizeCursorNESW);
 					fCanResize = true;
 					fChangeX = false;
 					fChangeY = false;
 				}
 			}
 
 			if (e.y == i && e.x == border.x - i) {
 				if (fIsCursorCanBeChanged) {
 					fComposite.setCursor(fResizeCursorNESW);
 					fCanResize = true;
 					fChangeX = false;
 					fChangeY = false;
 				}
 			}
 
 			if (e.y == i && e.x > i && e.x < border.x - i) {
 				if (fIsCursorCanBeChanged) {
 					fComposite.setCursor(fResizeCursorNS);
 					fCanResize = true;
 					fChangeX = false;
 					fChangeY = false;
 				}
 			}
 
 			if (e.y == border.y - i && e.x > i && e.x < border.x - i) {
 				if (fIsCursorCanBeChanged) {
 					fComposite.setCursor(fResizeCursorNS);
 					fCanResize = true;
 					fChangeX = false;
 					fChangeY = true;
 				}
 			}
 
 			if (e.x == i && e.y > i && e.y < border.y - i) {
 				if (fIsCursorCanBeChanged) {
 					fComposite.setCursor(fResizeCursorEW);
 					fCanResize = true;
 					fChangeX = false;
 					fChangeY = false;
 				}
 			}
 
 			if (e.x == border.x - i && e.y > i && e.y < border.y - i) {
 				if (fIsCursorCanBeChanged) {
 					fComposite.setCursor(fResizeCursorEW);
 					fCanResize = true;
 					fChangeX = true;
 					fChangeY = false;
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public void mouseDoubleClick(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseDown(MouseEvent e) {
 		if (e.button == 1)
 			if (fCanResize) {
 				fIsResizing = true;
 				fIsCursorCanBeChanged = false;
 			}
 	}
 
 	@Override
 	public void mouseUp(MouseEvent e) {
 		if (e.button == 1)
 			if (fIsResizing) {
 				Point beforeResize = fComposite.getSize();
 				Rectangle beforeResizeBounds = fComposite.getBounds();
 				/*
 				 * TODO: Now pad can be resized only to right or to bottom.
 				 * Extend it.
 				 */
 				if (e.x > 0 && e.y > 0) {
 					Point afterResize = null;
 					Rectangle afterResizeBounds = null;
 					int newWidth = 0;
 					int newHeigth = 0;
					if (fChangeX && fChangeY) {
 						// To prevent appearance of uninformative pad
						if (e.x < 70)
 							newWidth = 70;
						else
 							newWidth = e.x;
						if (e.y < 70)
							newHeigth = 70;
						else
 							newHeigth = e.y;
 					}
 					if (fChangeX && !fChangeY) {
 						newHeigth = beforeResize.y;
 						if (e.x < 70)
 							newWidth = 70;
 						else
 							newWidth = e.x;
 					}
 					if (!fChangeX && fChangeY) {
 						newWidth = beforeResize.x;
 						if (e.y < 70)
 							newHeigth = 70;
 						else
 							newHeigth = e.y;
 					}
 					if (!fChangeX && !fChangeY) {
 						newWidth = beforeResize.x;
 						newHeigth = beforeResize.y;
 					}
 					afterResize = new Point(newWidth, newHeigth);
 					afterResizeBounds = new Rectangle(beforeResizeBounds.x,
 							beforeResizeBounds.y, newWidth, newHeigth);
 					fComposite.setSize(afterResize);
 					fComposite.setBounds(afterResizeBounds);
 					Image image = fLabel.getImage();

					final Image resizedImage = new Image(
							fComposite.getDisplay(), image.getImageData()
									.scaledTo(newWidth, newHeigth));
 					fLabel.setImage(resizedImage);
 					fComposite.redraw();
 				}
 				fIsCursorCanBeChanged = true;
 				fIsResizing = false;
 				fCanResize = false;
 			}
 	}
 
 }
