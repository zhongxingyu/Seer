 /*******************************************************************************
  * Copyright (c) 13 dec. 2013 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.screens.editing;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.eclipse.emf.cdo.CDOObject;
 import org.eclipse.emf.cdo.common.id.CDOID;
 import org.eclipse.emf.cdo.common.revision.delta.CDOAddFeatureDelta;
 import org.eclipse.emf.cdo.common.revision.delta.CDOFeatureDelta;
 import org.eclipse.emf.cdo.common.revision.delta.CDOListFeatureDelta;
 import org.eclipse.emf.cdo.common.revision.delta.CDORemoveFeatureDelta;
 import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
 import org.eclipse.emf.cdo.common.revision.delta.CDOSetFeatureDelta;
 import org.eclipse.emf.cdo.common.revision.delta.CDOUnsetFeatureDelta;
 import org.eclipse.emf.cdo.transaction.CDOTransaction;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Tree;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 
 public class DirtyStateMessageDialog extends MessageDialog {
 
 	private TreeViewer dirtyStateViewer;
 
 	private CDOTransaction transaction;
 
 	private ModelUtils modelUtils;
 
 	public DirtyStateMessageDialog(Shell parentShell, String dialogTitle,
 			Image dialogTitleImage, String dialogMessage, int dialogImageType,
 			String[] dialogButtonLabels, int defaultIndex,
 			CDOTransaction transaction, ModelUtils modelUtils) {
 		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
 				dialogImageType, dialogButtonLabels, defaultIndex);
 		super.setShellStyle(this.getShellStyle() | SWT.RESIZE);
 		this.transaction = transaction;
 		this.modelUtils = modelUtils;
 	}
 
 	public static int openAndReturn(int kind, Shell parent, String title,
 			String message, CDOTransaction transaction, ModelUtils modelUtils) {
 
 		DirtyStateMessageDialog dialog = new DirtyStateMessageDialog(parent,
 				title, null, message, kind, getButtonLabels(kind), 0,
 				transaction, modelUtils);
 
 		return dialog.open();
 	}
 
 	static String[] getButtonLabels(int kind) {
 		String[] dialogButtonLabels;
 		switch (kind) {
 		case ERROR:
 		case INFORMATION:
 		case WARNING: {
 			dialogButtonLabels = new String[] { IDialogConstants.OK_LABEL };
 			break;
 		}
 		case CONFIRM: {
 			dialogButtonLabels = new String[] { IDialogConstants.OK_LABEL,
 					IDialogConstants.CANCEL_LABEL };
 			break;
 		}
 		case QUESTION: {
 			dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL,
 					IDialogConstants.NO_LABEL };
 			break;
 		}
 		case QUESTION_WITH_CANCEL: {
 			dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL,
 					IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL };
 			break;
 		}
 		default: {
 			throw new IllegalArgumentException(
 					"Illegal value for kind in MessageDialog.open()"); //$NON-NLS-1$
 		}
 		}
 		return dialogButtonLabels;
 	}
 
 	@Override
 	protected Control createCustomArea(Composite parent) {
 
 		if (transaction != null) {
 			dirtyStateViewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI
 					| SWT.VIRTUAL | SWT.READ_ONLY);
 
 			Tree tree = dirtyStateViewer.getTree();
 			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 			gridData.heightHint = 200;
 			tree.setLayoutData(gridData);
 			initBinding();
 			dirtyStateViewer.expandAll();
 			return tree;
 		}
 		return super.createCustomArea(parent);
 	}
 
 	private void initBinding() {
 
 		CDOTransactionContentProvider transactionContentProvider = new CDOTransactionContentProvider();
 		dirtyStateViewer.setContentProvider(transactionContentProvider);
 		dirtyStateViewer.setLabelProvider(new LabelProvider() {
 
 			@Override
 			public String getText(Object element) {
 
 				if (element instanceof CDOFeatureDelta) {
 					return switchDelta((CDOFeatureDelta) element);
 				} else if (element instanceof EObject) {
 					return modelUtils.printModelObject((EObject) element);
 				}
 				return super.getText(element);
 			}
 
 			private String switchDelta(CDOFeatureDelta delta) {
 				StringBuffer sb = new StringBuffer();
 
 				switch (delta.getType()) {
 				case LIST: {
 					CDOListFeatureDelta castedFd = (CDOListFeatureDelta) delta;
 
					sb.append("LIST " + castedFd.getFeature().getName());
 				}
 					break;
 				case ADD: {
 					CDOAddFeatureDelta castedFd = (CDOAddFeatureDelta) delta;
 					sb.append("ADD index: " + castedFd.getIndex());
 					if (castedFd.getFeature().isMany()) {
 						CDOID objectFor = objectFor(castedFd);
 						CDOObject object = transaction.getObject(objectFor);
 						List<?> eGet = (List<?>) object.eGet(delta.getFeature());
 						sb.append(" , value:" + eGet.get(castedFd.getIndex()));
 
 					}
 				}
 					break;
 				case REMOVE: {
 					CDORemoveFeatureDelta castedFd = (CDORemoveFeatureDelta) delta;
 					sb.append("REMOVE index: " + castedFd.getIndex());
 					sb.append(" , value:" + castedFd.getValue());
 				}
 					break;
 
 				case SET: {
 					CDOSetFeatureDelta castedFd = (CDOSetFeatureDelta) delta;
 					sb.append("SET name: " + castedFd.getFeature().getName()
 							+ ", index: " + castedFd.getIndex());
 					sb.append(", : " + castedFd.getOldValue() + " new: "
 							+ castedFd.getValue());
 				}
 
 					break;
 				case UNSET: {
 					CDOUnsetFeatureDelta castedFd = (CDOUnsetFeatureDelta) delta;
 					sb.append("SET name: " + castedFd.getFeature().getName());
 				}
 					break;
 
 				default: {
 					sb.append(" TODO create an entry for  type "
 							+ delta.getType()
 							+ " entry for feature delta attributes of this type");
 				}
 				}
 				return sb.toString();
 
 			}
 
 		});
 		dirtyStateViewer.setInput(new RootWrapper(transaction));
 	}
 
 	class RootWrapper {
 
 		private CDOTransaction cdoTransaction;
 
 		public RootWrapper(CDOTransaction cdoTransaction) {
 			this.cdoTransaction = cdoTransaction;
 		}
 
 		public Object[] getChildren() {
 			return new Object[] { cdoTransaction };
 		}
 	}
 
 	public CDOID objectFor(final CDOFeatureDelta delta) {
 		CDOID result = null;
 		if (transaction.getRevisionDeltas() != null) {
 			Set<Entry<CDOID, CDORevisionDelta>> entrySet = transaction
 					.getRevisionDeltas().entrySet();
 
 			Entry<CDOID, CDORevisionDelta> find = Iterables.find(entrySet,
 					new Predicate<Entry<CDOID, CDORevisionDelta>>() {
 
 						public boolean apply(
 								Entry<CDOID, CDORevisionDelta> input) {
 
 							return input.getValue().getFeatureDelta(
 									delta.getFeature()) != null;
 						}
 
 					});
 			if (find != null) {
 				result = find.getKey();
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * A Content provider which takes a {@link CDOTransaction} as input.
 	 * 
 	 * @author Christophe
 	 */
 	class CDOTransactionContentProvider implements ITreeContentProvider {
 
 		// A wrapper to show the root of element in the viewer.
 		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=9262
 
 		private Map<CDOID, CDORevisionDelta> revisionDeltas;
 
 		public void dispose() {
 		}
 
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			// not supported.
 		}
 
 		public Object[] getElements(Object inputElement) {
 			if (inputElement instanceof RootWrapper) {
 				Object[] children = ((RootWrapper) inputElement).getChildren();
 				if (children.length == 1
 						&& children[0] instanceof CDOTransaction) {
 					CDOTransaction t = (CDOTransaction) children[0];
 					revisionDeltas = t.getRevisionDeltas();
 				}
 				return children;
 			} else {
 				throw new IllegalArgumentException(
 						"this content provider access object type CDOTransaction");
 			}
 		}
 
 		public Object[] getChildren(Object parentElement) {
 			if (parentElement instanceof CDOTransaction) {
 				Map<CDOID, CDOObject> dirtyObjects = ((CDOTransaction) parentElement)
 						.getDirtyObjects();
 				// Cache the revision deltas.
 				return dirtyObjects.values().toArray();
 			} else if (parentElement instanceof CDOObject) {
 				if (revisionDeltas != null) {
 					CDOObject cdoObject = (CDOObject) parentElement;
 					CDORevisionDelta cdoRevisionDelta = revisionDeltas
 							.get(cdoObject.cdoID());
 					return cdoRevisionDelta.getFeatureDeltas().toArray();
 				}
 			} else if (parentElement instanceof CDOListFeatureDelta) {
 				return ((CDOListFeatureDelta) parentElement).getListChanges()
 						.toArray();
 			}
 			return null;
 		}
 
 		public Object getParent(Object element) {
 
 			if (element instanceof CDOObject) {
 				// ((CDOObject) element).cdoView();
 			}
 			return null;
 		}
 
 		public boolean hasChildren(Object element) {
 			if (element instanceof CDOTransaction
 					&& ((CDOTransaction) element).isDirty()) {
 				return true;
 			} else if (element instanceof CDOObject) {
 				if (revisionDeltas != null
 						&& revisionDeltas.containsKey(((CDOObject) element)
 								.cdoID())) {
 					return true;
 				}
 				// We have children if we are a list delta.
 			} else if (element instanceof CDOListFeatureDelta) {
 				return true;
 			}
 
 			return false;
 		}
 	}
 
 }
