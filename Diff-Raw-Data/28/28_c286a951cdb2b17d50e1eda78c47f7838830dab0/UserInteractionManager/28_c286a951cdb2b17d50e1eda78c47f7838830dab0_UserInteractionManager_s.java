 package org.eclipse.iee.editor.core.container;
 
 import org.eclipse.jface.text.Position;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CaretEvent;
 import org.eclipse.swt.custom.CaretListener;
 import org.eclipse.swt.custom.ST;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.custom.VerifyKeyListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.graphics.Point;
 
 public class UserInteractionManager {
 		
 	private final StyledText fStyledText;
 	private final ContainerManager fContainerManager;
 	
 	private Container fSelectedContainer;
 	
 	public UserInteractionManager(ContainerManager containerManager) {
 		fContainerManager = containerManager;
 		fStyledText = containerManager.getStyledText();
 		
 		fSelectedContainer = null;
 		
 		initListeners();
 	}
 	
 	public void moveCaretTo(int offset) {
 		fStyledText.setCaretOffset(offset);
 		fStyledText.forceFocus();
 	}
 	
 	public void updateCaretSelection() {
 		int caretOffset = fStyledText.getCaretOffset();
 		Container container = fContainerManager.getContainerHavingOffset(caretOffset);
 		
 		if (container != null && container.getPosition().getOffset() == caretOffset) {
 			/* Caret is at the left side of the pad */
 			if (container != fSelectedContainer) {
 				/* Select this container and unselect previous */
 				if (fSelectedContainer != null) {
 					fContainerManager.fireContainerLostSelection(fSelectedContainer);
 				}
 				fSelectedContainer = container;
 				fContainerManager.fireContainerSelected(container);
 			}
 		}
 		else if (fSelectedContainer != null) {
 			/* Only unselect previous container */
 			fContainerManager.fireContainerLostSelection(fSelectedContainer);
 			fSelectedContainer = null;
 		}
 	}
 		
 		
 	protected void initListeners() {
 		/* 1) Disallow modification within Container's text region */
 		fStyledText.addVerifyListener(new VerifyListener() {
 			@Override
 			public void verifyText(VerifyEvent e) {
 				Container atStart = fContainerManager.getContainerHavingOffset(e.start);
 				Container atEnd = fContainerManager.getContainerHavingOffset(e.end);
 
 				/* Text replaced */
 				if ((atStart != null && e.start != atStart.getPosition().getOffset()) ||
 						(atEnd != null && e.end != atEnd.getPosition().getOffset()))
 				{
 					e.doit = false;
 					return;
 				}
 
 				/* XXX Visibility */
 				//fContainerManager.updateContainerVisibility(false);
 			}
 		});
 
 		fStyledText.addVerifyKeyListener(new VerifyKeyListener() {
 			@Override
 			public void verifyKey(VerifyEvent event) {
 				int action = fStyledText.getKeyBinding(event.keyCode | event.stateMask);
 				if (action != SWT.NULL) {
				switch (action) {
 					case ST.COLUMN_PREVIOUS:
						caretPositionChange(fStyledText.getCaretOffset(), false);
 						break;
 					case ST.COLUMN_NEXT:
						caretPositionChange(fStyledText.getCaretOffset(), true);
 						break;
 					case ST.DELETE_NEXT:
						if (fSelectedContainer != null)
							fSelectedContainer.destroy();
 						break;
 					}
 				}
 			}
 		});
 		
 		fStyledText.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				/*
 				 * CTRL + ALT causes pad activation
 				 */
 				if ((e.stateMask & SWT.CTRL) != 0 && e.keyCode == SWT.ALT) {
 					int caretOffset = fStyledText.getCaretOffset();
 					Container container = fContainerManager.getContainerHavingOffset(caretOffset);					
 					if (container != null && container.getPosition().getOffset() == caretOffset) {
 						fContainerManager.fireContainerActivated(container);
 					}
 				}
 				
 				/*
 				 * PageUP or PageDown
 				 */
 				/*if (e.keyCode == SWT.PAGE_UP || e.keyCode == SWT.PAGE_DOWN) {
 					fContainerManager.getStyledTextManager().updatePresentations();
 				}*/
 			}
 			
 			@Override public void keyReleased(KeyEvent e) {}
 		});
 		
 		/*
 		 * If caret is inside Container's text region, moving it to the end of line
 		 */
 		fStyledText.addCaretListener(new CaretListener() {
 			@Override
 			public void caretMoved(CaretEvent e) {
 				Point selection = fStyledText.getSelection();
 				if (selection.y - selection.x == 0) {
 					Container container = fContainerManager.getContainerHavingOffset(e.caretOffset);
 					if (container != null) {
 						Position position = container.getPosition();
 						if (e.caretOffset != position.getOffset()) {
 								/* Move caret to the Pad's border */
 								fStyledText.setCaretOffset(position.getOffset() + position.getLength());
 						}
 					}
 					updateCaretSelection();
 				}
 			}
 		}); 
 		
 	}
 	
 	private void caretPositionChange(int x, boolean caretMovesForward) {
 		Point selection = fStyledText.getSelection();
 		if (selection.y - selection.x == 0) {
 			Container container = fContainerManager.getContainerHavingOffset(caretMovesForward ? x : x - 1);
 			if (container != null) {
 				Position position = container.getPosition();
 				if (caretMovesForward) {
 					fContainerManager.fireContainerActivated(container);					
 					fStyledText.setCaretOffset(position.getOffset() + position.getLength());
 				} else {
 					fContainerManager.fireContainerActivated(container);
 					fStyledText.setCaretOffset(position.getOffset());
 				}
 			}
 			
 			updateCaretSelection();
 		}
 	}
 }
