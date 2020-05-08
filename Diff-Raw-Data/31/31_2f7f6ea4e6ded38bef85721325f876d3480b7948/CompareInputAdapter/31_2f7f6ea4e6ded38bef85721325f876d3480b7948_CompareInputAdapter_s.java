 /*******************************************************************************
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer;
 
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.compare.structuremergeviewer.Differencer;
 import org.eclipse.compare.structuremergeviewer.ICompareInput;
 import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Conflict;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.MatchResource;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.accessor.AccessorAdapter;
 import org.eclipse.emf.compare.provider.ExtendedAdapterFactoryItemDelegator;
 import org.eclipse.emf.compare.rcp.ui.EMFCompareRCPUIPlugin;
 import org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.factory.IAccessorFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IDisposable;
 import org.eclipse.emf.edit.tree.TreeNode;
 import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
 import org.eclipse.jface.util.SafeRunnable;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public abstract class CompareInputAdapter extends AdapterImpl implements ICompareInput, IDisposable {
 
 	/**
 	 * Store the listeners for notifications.
 	 */
 	private final ListenerList fListener;
 
 	/**
 	 * The {@link AdapterFactory} used to implement {@link #getName()} and {@link #getImage()}.
 	 */
 	private final AdapterFactory fAdapterFactory;
 
 	/** The item delegator to use to retrieve item */
 	private final ExtendedAdapterFactoryItemDelegator itemDelegator;
 
 	/**
 	 * Simple constructor storing the given {@link AdapterFactory}.
 	 * 
 	 * @param adapterFactory
 	 *            the factory.
 	 */
 	public CompareInputAdapter(AdapterFactory adapterFactory) {
 		fAdapterFactory = adapterFactory;
 		itemDelegator = new ExtendedAdapterFactoryItemDelegator(getRootAdapterFactory());
 		fListener = new ListenerList();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.common.notify.impl.AdapterImpl#isAdapterForType(java.lang.Object)
 	 */
 	@Override
 	public boolean isAdapterForType(Object type) {
 		return type == fAdapterFactory;
 	}
 
 	/**
 	 * Final accessor to the {@link AdapterFactory} for sub classses.
 	 * 
 	 * @return the wrapped {@link AdapterFactory}.
 	 */
 	protected final AdapterFactory getAdapterFactory() {
 		return fAdapterFactory;
 	}
 
 	/**
 	 * Gets the root factory if this local adapter factory is composed, otherwise just the local one.
 	 */
 	protected final AdapterFactory getRootAdapterFactory() {
 		if (fAdapterFactory instanceof ComposeableAdapterFactory) {
 			return ((ComposeableAdapterFactory)fAdapterFactory).getRootAdapterFactory();
 		}
 
 		return fAdapterFactory;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#addCompareInputChangeListener(org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener)
 	 */
 	public void addCompareInputChangeListener(ICompareInputChangeListener listener) {
 		fListener.add(listener);
 	}
 
 	/**
 	 * {@inheritDoc}.
 	 * 
 	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#removeCompareInputChangeListener(org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener)
 	 */
 	public void removeCompareInputChangeListener(ICompareInputChangeListener listener) {
 		fListener.remove(listener);
 	}
 
 	/**
 	 * Sends out notification that a change has occurred on the <code>ICompareInput</code>.
 	 */
 	protected void fireChange() {
 		if (fListener != null) {
 			final Object[] listeners = fListener.getListeners();
 			for (int i = 0; i < listeners.length; i++) {
 				final ICompareInputChangeListener listener = (ICompareInputChangeListener)listeners[i];
 				SafeRunnable runnable = new SafeRunnable() {
 					public void run() throws Exception {
 						listener.compareInputChanged(CompareInputAdapter.this);
 					}
 				};
 				SafeRunner.run(runnable);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public EObject getComparisonObject() {
		return ((TreeNode)getTarget()).getData();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#copy(boolean)
 	 */
 	public void copy(boolean leftToRight) {
 
 	}
 
 	/**
 	 * Returns the appropriate {@link IAccessorFactory} from the accessor factory registry.
 	 * 
 	 * @return the appropriate {@link IAccessorFactory}.
 	 */
 	protected IAccessorFactory getAccessorFactoryForTarget() {
 		IAccessorFactory.Registry factoryRegistry = EMFCompareRCPUIPlugin.getDefault()
 				.getAccessorFactoryRegistry();
 		return factoryRegistry.getHighestRankingFactory(getComparisonObject());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.ITypedElement#getImage()
 	 */
 	public Image getImage() {
 		Object imageObject = itemDelegator.getImage(getComparisonObject());
 		return ExtendedImageRegistry.getInstance().getImage(imageObject);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#getKind()
 	 */
 	public int getKind() {
 		Notifier notifier = getComparisonObject();
 		boolean isThreeWay = false;
 		if (notifier instanceof Diff) {
 			isThreeWay = ((Diff)notifier).getMatch().getComparison().isThreeWay();
 		} else if (notifier instanceof Match) {
 			isThreeWay = ((Match)notifier).getComparison().isThreeWay();
 		} else if (notifier instanceof Conflict) {
 			isThreeWay = true;
 		} else if (notifier instanceof MatchResource) {
 			isThreeWay = ((MatchResource)notifier).getComparison().isThreeWay();
 		}
 		if (isThreeWay) {
 			return Differencer.CONFLICTING;
 		}
 		return Differencer.NO_CHANGE;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#getName()
 	 */
 	public String getName() {
 		return itemDelegator.getText(getComparisonObject());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#getAncestor()
 	 */
 	public ITypedElement getAncestor() {
 		final ITypedElement ret;
 		Notifier notifier = getComparisonObject();
		boolean isThreeWay = isThreeWay(notifier);
		if (isThreeWay) {
			IAccessorFactory accessorFactory = getAccessorFactoryForTarget();
			if (accessorFactory != null) {
				org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.ITypedElement typedElement = accessorFactory
						.createAncestor(getAdapterFactory(), getComparisonObject());
				if (typedElement != null) {
					ret = AccessorAdapter.adapt(typedElement);
 				} else {
 					ret = null;
 				}
 			} else {
 				ret = null;
 			}
		} else {
			ret = null;
		}
 		return ret;
 	}
 
 	protected boolean isThreeWay(Notifier notifier) {
 		final boolean isThreeWay;
 		if (notifier instanceof Diff) {
 			isThreeWay = ((Diff)notifier).getMatch().getComparison().isThreeWay();
 		} else if (notifier instanceof Match) {
 			isThreeWay = ((Match)notifier).getComparison().isThreeWay();
 		} else if (notifier instanceof Conflict) {
 			isThreeWay = true;
 		} else if (notifier instanceof MatchResource) {
 			isThreeWay = ((MatchResource)notifier).getComparison().isThreeWay();
 		} else if (notifier instanceof Comparison) {
 			isThreeWay = ((Comparison)notifier).isThreeWay();
 		} else {
 			isThreeWay = false;
 		}
 		return isThreeWay;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#getLeft()
 	 */
 	public ITypedElement getLeft() {
 		final ITypedElement ret;
 		IAccessorFactory accessorFactory = getAccessorFactoryForTarget();
 		if (accessorFactory != null) {
 			org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.ITypedElement typedElement = accessorFactory
 					.createLeft(getAdapterFactory(), getComparisonObject());
 			if (typedElement != null) {
 				ret = AccessorAdapter.adapt(typedElement);
 			} else {
 				ret = null;
 			}
 		} else {
 			ret = null;
 		}
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#getRight()
 	 */
 	public ITypedElement getRight() {
 		final ITypedElement ret;
 		IAccessorFactory accessorFactory = getAccessorFactoryForTarget();
 		if (accessorFactory != null) {
 			org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.ITypedElement typedElement = accessorFactory
 					.createRight(getAdapterFactory(), getComparisonObject());
 			if (typedElement != null) {
 				ret = AccessorAdapter.adapt(typedElement);
 			} else {
 				ret = null;
 			}
 		} else {
 			ret = null;
 		}
 		return ret;
 	}
 
 	/**
 	 * This will remove this adapter from all its the targets and dispose any remaining children wrappers in
 	 * the children store.
 	 */
 	public void dispose() {
 		Notifier oldTarget = target;
 		target = null;
 
 		if (oldTarget != null) {
 			oldTarget.eAdapters().remove(this);
 		}
 	}
 }
