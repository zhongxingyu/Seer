 /*******************************************************************************
  * Copyright (c) 2012 Ericsson AB and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the controls that are shown in the R4E Annotation Hover
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.annotation.control;
 
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
 import org.eclipse.jface.text.IInformationControl;
 import org.eclipse.jface.text.IInformationControlCreator;
 import org.eclipse.jface.text.IInformationControlExtension4;
 import org.eclipse.jface.text.IWidgetTokenKeeper;
 import org.eclipse.jface.text.IWidgetTokenOwner;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeViewerListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeExpansionEvent;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.mylyn.reviews.frame.ui.annotation.IReviewAnnotation;
 import org.eclipse.mylyn.reviews.frame.ui.annotation.impl.ReviewAnnotationInformationControl;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.annotation.content.R4EAnnotation;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.annotation.content.R4EAnnotationText;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.annotation.content.R4ECommentAnnotation;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.commands.ModelContributionItems;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4ECompareEditorInput;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4EFileEditorInput;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4EFileRevisionEditorInput;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorLabelProvider;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.editors.text.EditorsUI;
 import org.eclipse.ui.internal.editors.text.EditorsPlugin;
 import org.eclipse.ui.texteditor.AnnotationPreference;
 import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 /**
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class R4EAnnotationInformationControl extends ReviewAnnotationInformationControl {
 
 	/**
 	 * Field fPresenterControlCreator.
 	 */
 	private IInformationControlCreator fPresenterControlCreator = null;
 
 	/**
 	 * Field fHoverControlCreator.
 	 */
 	private IInformationControlCreator fHoverControlCreator = null;
 
 	/**
 	 * Field fComposite.
 	 */
 	private Composite fComposite;
 
 	/**
 	 * Field fAnnotationTree.
 	 */
 	private TreeViewer fAnnotationTree = null;;
 
 	/**
 	 * Field fPreSelectionActivePart.
 	 */
 	private IWorkbenchPart fPreSelectionActivePart = null;
 
 	/**
 	 * Field fAutomaticSelection.
 	 */
 	private boolean fAutomaticSelection = false;
 
 	/**
 	 * Constructor for R4EAnnotationInformationControl.
 	 * 
 	 * @param aParentShell
 	 *            Shell
 	 * @param aStatusFieldText
 	 *            String
 	 */
 	public R4EAnnotationInformationControl(Shell aParentShell, String aStatusFieldText) {
 		super(aParentShell, aStatusFieldText);
 	}
 
 	/**
 	 * Constructor for R4EAnnotationInformationControl.
 	 * 
 	 * @param aParentShell
 	 *            Shell
 	 * @param aToolBarManager
 	 *            ToolBarManager
 	 */
 	public R4EAnnotationInformationControl(Shell aParentShell, ToolBarManager aToolBarManager) {
 		super(aParentShell, aToolBarManager);
 	}
 
 	/**
 	 * Method dispose.
 	 * 
 	 * @see org.eclipse.jface.text.IInformationControl#dispose()
 	 */
 	@Override
 	public void dispose() {
 		fAnnotationTree.getTree().dispose();
 		if (null != fComposite) {
 			fComposite.dispose();
 		}
 		super.dispose();
 	}
 
 	/**
 	 * Method disposeDeferredCreatedContent.
 	 * 
 	 * @see org.eclipse.mylyn.reviews.frame.ui.annotation.impl.ReviewAnnotationInformationControl#disposeDeferredCreatedContent()
 	 */
 	@Override
 	protected void disposeDeferredCreatedContent() {
 		if (null != fComposite) {
 			final Control[] children = fComposite.getChildren();
 			for (Control element : children) {
 				element.dispose();
 			}
 		}
 		final ToolBarManager toolBarManager = getToolBarManager();
 		if (toolBarManager != null) {
 			toolBarManager.removeAll();
 		}
 	}
 
 	/**
 	 * Create content of the hover. This is called after the input has been set.
 	 * 
 	 * @see org.eclipse.mylyn.reviews.frame.ui.annotation.impl.ReviewAnnotationInformationControl#deferredCreateContent()
 	 */
 	@Override
 	protected void deferredCreateContent() {
 		//Set Delta/Selection annotations first, then anomalies
 		addAnnotationsInformation();
 		setColorAndFont(fComposite, fComposite.getParent().getForeground(), fComposite.getParent().getBackground(),
 				JFaceResources.getDialogFont());
 		fComposite.layout(true);
 	}
 
 	/**
 	 * Method createContent.
 	 * 
 	 * @param aParent
 	 *            Composite
 	 * @see org.eclipse.jface.text.AbstractInformationControl#createContent(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	protected void createContent(Composite aParent) {
 		final GridLayout layoutParent = new GridLayout(1, false);
 		layoutParent.verticalSpacing = 0;
 		layoutParent.marginWidth = 0;
 		layoutParent.marginHeight = 0;
 		aParent.setLayout(layoutParent);
 
 		fComposite = new Composite(aParent, SWT.NONE);
 		final GridData data = new GridData(GridData.FILL, GridData.FILL, true, true);
 		fComposite.setLayoutData(data);
 
 		final GridLayout layoutComposite = new GridLayout(1, false);
 		layoutComposite.verticalSpacing = 0;
 		layoutComposite.marginWidth = 0;
 		layoutComposite.marginHeight = 0;
 		fComposite.setLayout(layoutComposite);
 	}
 
 	/**
 	 * Method computeSizeHint.
 	 * 
 	 * @return Point
 	 * @see org.eclipse.jface.text.IInformationControl#computeSizeHint()
 	 */
 	@Override
 	public Point computeSizeHint() {
		//Limit maximum size to avoid taking too much real estate
 		final Point newSize = super.computeSizeHint();
 		if (newSize.x > R4EUIConstants.ANNOTATION_CONTROL_MAX_WIDTH) {
 			newSize.x = R4EUIConstants.ANNOTATION_CONTROL_MAX_WIDTH;
 		}
 		if (newSize.y > R4EUIConstants.ANNOTATION_CONTROL_MAX_HEIGHT) {
 			newSize.y = R4EUIConstants.ANNOTATION_CONTROL_MAX_HEIGHT;
 		}
 		return newSize;
 	}
 
 	/**
 	 * Fills the toolbar actions, if a toolbar is available. This is called after the input has been set.
 	 * 
 	 * @param aSourceElement
 	 *            Object
 	 * @see org.eclipse.mylyn.reviews.frame.ui.annotation.impl.ReviewAnnotationInformationControl#updateToolbar()
 	 */
 	@Override
 	protected void updateToolbar(Object aSourceElement) {
 		final ToolBarManager toolBarManager = getToolBarManager();
 		if (null != toolBarManager) {
 			//Add commands for selected element in toolbar in refresh control
 			toolBarManager.removeAll();
 			addToolbarElementCommands(toolBarManager, aSourceElement);
 
 			//This is used to distinguish between the annotation toolbar and the Review Navigator view toolbar
 			toolBarManager.getControl().setData(R4EUIConstants.ANNOTATION_TOOLBAR, new Boolean(true));
 
 			toolBarManager.update(true);
 
 			//If the size is already bigger than the calculated one, do not change it
 			final Point size = computeSizeHint();
 			setSize(Math.max(size.x, getShell().getSize().x), Math.max(size.y, getShell().getSize().y));
 			getShell().redraw();
 		}
 	}
 
 	/**
 	 * Method addToolbarElementCommands.
 	 * 
 	 * @param aToolBarManager
 	 *            ToolBarManager
 	 * @param aSourceElement
 	 *            Object
 	 * @see org.eclipse.mylyn.reviews.frame.ui.annotation.impl.ReviewAnnotationInformationControl#addToolbarElementCommands(ToolBarManager,
 	 *      Object)
 	 */
 	@Override
 	protected void addToolbarElementCommands(ToolBarManager aToolBarManager, Object aSourceElement) {
 		final ModelContributionItems r4eItemsManager = new ModelContributionItems();
 		final IContributionItem[] items = r4eItemsManager.getContributionItems((IR4EUIModelElement) aSourceElement);
 		for (IContributionItem item : items) {
 			aToolBarManager.add(item);
 		}
 	}
 
 	/**
 	 * Method addAnnotationsInformation.
 	 * 
 	 * @see org.eclipse.mylyn.reviews.frame.ui.annotation.impl.ReviewAnnotationInformationControl#addAnnotationsInformation()
 	 */
 	@Override
 	protected void addAnnotationsInformation() {
 		fAnnotationTree = new TreeViewer(fComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.READ_ONLY | SWT.H_SCROLL
 				| SWT.V_SCROLL);
 		fAnnotationTree.setContentProvider(new R4EAnnotationContentProvider());
 		fAnnotationTree.setSorter(new R4EAnnotationTypeSorter());
 		fAnnotationTree.setLabelProvider(new ReviewNavigatorLabelProvider() {
 
 			@Override
 			public void update(ViewerCell aCell) {
 
 				final Object cellContents = aCell.getElement();
 				if (cellContents instanceof IReviewAnnotation) {
 					aCell.setText(((IReviewAnnotation) cellContents).getText());
 					final Image image = getImage((Annotation) cellContents);
 					if (null != image) {
 						aCell.setImage(image);
 					}
 				} else if (cellContents instanceof R4EAnnotationText) {
 					aCell.setText(((R4EAnnotationText) cellContents).getText());
 				}
 			}
 
 			private Image getImage(Annotation aAnnotation) {
 				if (aAnnotation instanceof R4ECommentAnnotation) {
 					return UIUtils.loadIcon(((R4ECommentAnnotation) aAnnotation).getSourceElement().getImageLocation());
 				}
 				final AnnotationPreferenceLookup lookup = EditorsPlugin.getDefault().getAnnotationPreferenceLookup();
 				if (lookup != null) {
 					final AnnotationPreference preference = lookup.getAnnotationPreference(aAnnotation);
 					if (preference != null) {
 						final ImageRegistry registry = EditorsPlugin.getDefault().getImageRegistry();
 						final Image image = registry.get(aAnnotation.getType());
 						if (image != null) {
 							return image;
 						}
 					}
 				}
 				return null;
 			}
 		});
 		fAnnotationTree.setInput(fInput);
 
 		//Adjust toolbar commands based on selection
 		fAnnotationTree.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent aEvent) {
 				//Check and set files based on selected cloneable anomaly
 				if (aEvent.getSelection() instanceof IStructuredSelection) {
 					if (null != ((IStructuredSelection) aEvent.getSelection()).getFirstElement()) {
 						Object selectedObject = ((IStructuredSelection) aEvent.getSelection()).getFirstElement();
 						while ((null != selectedObject) && !(selectedObject instanceof R4EAnnotation)) {
 							selectedObject = ((R4EAnnotationText) selectedObject).getParent();
 						}
 						if (null != selectedObject) {
 							selectElementInNavigator((R4EAnnotation) selectedObject);
 						}
 					}
 				}
 			}
 
 			private void selectElementInNavigator(R4EAnnotation aSelectedAnnotation) {
 				final IR4EUIModelElement element = aSelectedAnnotation.getSourceElement();
 				if (null != element) {
 					updateToolbar(element);
 					//NOTE: For now, we changed to code so that we do not show the properties here
 					//      (we only select the element in the Review Navigator if possible).  To show the properties,
 					//      the user can use the Show Properties command available in the annotation popup window.
 					//		the only drawback is that the element needs to be visible in the REview Navigator for 
 					//		the properties to be displayed.  This is a limitation for now.
 					if (null != R4EUIModelController.getNavigatorView()) {
 						final IWorkbenchPage page = PlatformUI.getWorkbench()
 								.getActiveWorkbenchWindow()
 								.getActivePage();
 						fPreSelectionActivePart = page.getActivePart();
 						R4EUIModelController.getNavigatorView().updateView(element, 0, false);
 					}
 
 					//Highlight selected annotation source in editor
 					if (!fAutomaticSelection) {
 						setHighlightText(aSelectedAnnotation);
 					}
 					fAutomaticSelection = false;
 				}
 			}
 
 			/**
 			 * Method setHighlightText.
 			 * 
 			 * @param aAnnotation
 			 *            R4EAnnotation
 			 */
 			private void setHighlightText(R4EAnnotation aAnnotation) {
 
 				//Set the highlight on the editor previously active
 				if ((null != fPreSelectionActivePart) && fPreSelectionActivePart instanceof IEditorPart) {
 					final IEditorInput input = ((IEditorPart) fPreSelectionActivePart).getEditorInput();
 					if (null != input) {
 						if (input instanceof R4ECompareEditorInput) {
 							UIUtils.selectElementInEditor((R4ECompareEditorInput) input);
 						} else if (input instanceof R4EFileEditorInput || input instanceof R4EFileRevisionEditorInput) {
 							final Position position = aAnnotation.getPosition();
 							if (null != position) {
 								final int offset = position.getOffset();
 								final int length = position.getLength();
 								if (fPreSelectionActivePart instanceof ITextEditor) {
 									((ITextEditor) fPreSelectionActivePart).selectAndReveal(offset, length);
 									final TextSelection selectedText = new TextSelection(offset, length);
 									((ITextEditor) fPreSelectionActivePart).getSelectionProvider().setSelection(
 											selectedText);
 								}
 							}
 						}
 					}
 				}
 			}
 		});
 
 		fAnnotationTree.addTreeListener(new ITreeViewerListener() {
 			public void treeExpanded(TreeExpansionEvent aEvent) {
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 						final Point size = computeSizeHint();
 						setSize(size.x, size.y);
 					}
 				});
 			}
 
 			public void treeCollapsed(TreeExpansionEvent aEvent) {
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 						final Point size = computeSizeHint();
 						setSize(size.x, size.y);
 					}
 				});
 			}
 		});
 
 		final GridData data = new GridData(GridData.FILL, GridData.FILL, true, true);
 		fAnnotationTree.getTree().setLayoutData(data);
 
 		//If there is only 1 element in this annotation, select it automatically
 		if ((null != fInput) && (fInput.getAnnotations().size() == 1)) {
 			fAutomaticSelection = true;
 			final ISelection selection = new StructuredSelection(fInput.getAnnotations().get(0));
 			fAnnotationTree.setSelection(selection, true);
 			fAnnotationTree.expandToLevel(2);
 		}
 	}
 
 	/**
 	 * Method getInformationPresenterControlCreator.
 	 * 
 	 * @return IInformationControlCreator
 	 * @see org.eclipse.jface.text.IInformationControlExtension5#getInformationPresenterControlCreator()
 	 */
 	@Override
 	public IInformationControlCreator getInformationPresenterControlCreator() {
 		if (fPresenterControlCreator == null) {
 			fPresenterControlCreator = new PresenterControlCreator();
 		}
 		return fPresenterControlCreator;
 	}
 
 	/**
 	 * Method getHoverControlCreator.
 	 * 
 	 * @return IInformationControlCreator
 	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
 	 */
 	@Override
 	public IInformationControlCreator getHoverControlCreator() {
 		if (fHoverControlCreator == null) {
 			fHoverControlCreator = new HoverControlCreator(getInformationPresenterControlCreator());
 		}
 		return fHoverControlCreator;
 	}
 
 	// ------------------------------------------------------------------------
 	// Inner Classes
 	// ------------------------------------------------------------------------ 
 
 	/**
 	 * Hover control creator.
 	 * 
 	 * @author lmcdubo
 	 */
 	private static final class HoverControlCreator extends AbstractReusableInformationControlCreator {
 
 		/**
 		 * Field fPresenterControlCreator.
 		 */
 		private final IInformationControlCreator fPresenterControlCreator;
 
 		/**
 		 * Constructor for HoverControlCreator.
 		 * 
 		 * @param aPresenterControlCreator
 		 *            IInformationControlCreator
 		 */
 		public HoverControlCreator(IInformationControlCreator aPresenterControlCreator) {
 			fPresenterControlCreator = aPresenterControlCreator;
 		}
 
 		/**
 		 * Method doCreateInformationControl.
 		 * 
 		 * @param aParent
 		 *            Shell
 		 * @return IInformationControl
 		 */
 		@Override
 		public IInformationControl doCreateInformationControl(Shell aParent) {
 			return new R4EAnnotationInformationControl(aParent, EditorsUI.getTooltipAffordanceString()) {
 
 				/**
 				 * Method getInformationPresenterControlCreator.
 				 * 
 				 * @return IInformationControlCreator
 				 * @see org.eclipse.jface.text.IInformationControlExtension5#getInformationPresenterControlCreator()
 				 */
 				@Override
 				public IInformationControlCreator getInformationPresenterControlCreator() {
 					return fPresenterControlCreator;
 				}
 			};
 		}
 
 		/**
 		 * Method canReuse.
 		 * 
 		 * @param aControl
 		 *            IInformationControl
 		 * @return boolean
 		 * @see org.eclipse.jface.text.IInformationControlCreatorExtension#canReuse(IInformationControl)
 		 */
 		@Override
 		public boolean canReuse(IInformationControl aControl) {
 			if (!super.canReuse(aControl)) {
 				return false;
 			}
 
 			if (aControl instanceof IInformationControlExtension4) {
 				((IInformationControlExtension4) aControl).setStatusText(EditorsUI.getTooltipAffordanceString());
 			}
 
 			return true;
 		}
 	}
 
 	/**
 	 * Presenter control creator.
 	 * 
 	 * @author lmcdubo
 	 */
 	private static final class PresenterControlCreator extends AbstractReusableInformationControlCreator implements
 			IWidgetTokenKeeper {
 
 		/**
 		 * Method doCreateInformationControl.
 		 * 
 		 * @param aParent
 		 *            Shell
 		 * @return IInformationControl
 		 * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#doCreateInformationControl(org.eclipse.swt.widgets.Shell)
 		 */
 		@Override
 		public IInformationControl doCreateInformationControl(Shell aParent) {
 			return new R4EAnnotationInformationControl(aParent, new ToolBarManager(SWT.FLAT));
 		}
 
 		/**
 		 * Method requestWidgetToken.
 		 * 
 		 * @param aOwner
 		 *            IWidgetTokenOwner
 		 * @return boolean
 		 * @see org.eclipse.jface.text.IWidgetTokenKeeper#requestWidgetToken(IWidgetTokenOwner)
 		 */
 		public boolean requestWidgetToken(IWidgetTokenOwner aOwner) {
 			return false;
 		}
 	}
 }
