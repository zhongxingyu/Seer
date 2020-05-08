 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.navigator;
 
 import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
 import org.eclipse.jface.viewers.ILabelDecorator;
 import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
 import org.eclipse.tcf.te.tcf.ui.internal.ImageConsts;
 import org.eclipse.tcf.te.tcf.ui.navigator.images.PeerImageDescriptor;
 import org.eclipse.tcf.te.tcf.ui.navigator.nodes.PeerRedirectorGroupNode;
 import org.eclipse.tcf.te.tcf.ui.nls.Messages;
 import org.eclipse.tcf.te.ui.jface.images.AbstractImageDescriptor;
 
 
 /**
  * Label provider delegate implementation.
  */
public class LabelProviderDelegate extends LabelProvider implements ILabelDecorator, IStyledLabelProvider {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
 	 */
 	@Override
 	public String getText(Object element) {
 		if (element instanceof IPeerModel) {
 			String label = null;
 
 			final IPeer peer = ((IPeerModel)element).getPeer();
 			final String[] peerName = new String[1];
 			if (Protocol.isDispatchThread()) {
 				peerName[0] = peer.getName();
 			} else {
 				Protocol.invokeAndWait(new Runnable() {
 					@Override
 					public void run() {
 						peerName[0] = peer.getName();
 					}
 				});
 			}
 			label = peerName[0];
 
 			if (label != null && !"".equals(label.trim())) { //$NON-NLS-1$
 				return label;
 			}
 		} else if (element instanceof PeerRedirectorGroupNode) {
 			return Messages.RemotePeerDiscoveryRootNode_label;
 		}
 
 		return ""; //$NON-NLS-1$
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
 	 */
 	@Override
 	public Image getImage(Object element) {
 		if (element instanceof IPeerModel) {
 			return UIPlugin.getImage(ImageConsts.PEER);
 		}
 		if (element instanceof PeerRedirectorGroupNode) {
 			return UIPlugin.getImage(ImageConsts.DISCOVERY_ROOT);
 		}
 
 		return super.getImage(element);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
 	 */
 	@Override
 	public Image decorateImage(Image image, Object element) {
 		Image decoratedImage = null;
 
 		if (image != null && element instanceof IPeerModel) {
 			AbstractImageDescriptor descriptor = new PeerImageDescriptor(UIPlugin.getDefault().getImageRegistry(),
 			                                                             image,
 			                                                             (IPeerModel)element);
 			decoratedImage = UIPlugin.getSharedImage(descriptor);
 		}
 
 		return decoratedImage;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
 	 */
 	@Override
 	public String decorateText(final String text, final Object element) {
 		if (element instanceof IPeerModel) {
 			String label = text;
 
 			final StringBuilder builder = new StringBuilder(label != null && !"".equals(label.trim()) ? label.trim() : "<noname>"); //$NON-NLS-1$ //$NON-NLS-2$
 
 			Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
 					IPeer peer = ((IPeerModel)element).getPeer();
 					String dnsName = ((IPeerModel)element).getStringProperty("dns.name.transient"); //$NON-NLS-1$
 
 					doDecorateText(builder, peer, dnsName);
 				}
 			};
 
 			if (Protocol.isDispatchThread()) runnable.run();
 			else Protocol.invokeAndWait(runnable);
 
 			label = builder.toString();
 
 			if (label != null && !"".equals(label.trim()) && !"<noname>".equals(label.trim())) { //$NON-NLS-1$ //$NON-NLS-2$
 				return label;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Decorate the text with some peer attributes.
 	 * <p>
 	 * <b>Note:</b> Must be called with the TCF event dispatch thread.
 	 *
 	 * @param builder The string builder to decorate. Must not be <code>null</code>.
 	 * @param peer The peer. Must not be <code>null</code>.
 	 * @param dnsName The peers DNS name or <code>null</code>.
 	 */
 	/* default */ void doDecorateText(StringBuilder builder, IPeer peer, String dnsName) {
 		Assert.isNotNull(builder);
 		Assert.isNotNull(peer);
 		Assert.isTrue(Protocol.isDispatchThread());
 
 		String ip = peer.getAttributes().get(IPeer.ATTR_IP_HOST);
 		String port = peer.getAttributes().get(IPeer.ATTR_IP_PORT);
 
 		if (ip != null && !"".equals(ip.trim())) { //$NON-NLS-1$
 			builder.append(" ["); //$NON-NLS-1$
 			builder.append(dnsName != null && !"".equals(dnsName.trim()) ? dnsName.trim() : ip.trim()); //$NON-NLS-1$
 
 			if (port != null && !"".equals(port.trim()) && !"1534".equals(port.trim())) { //$NON-NLS-1$ //$NON-NLS-2$
 				builder.append(":"); //$NON-NLS-1$
 				builder.append(port.trim());
 			}
 			builder.append("]"); //$NON-NLS-1$
 		}
 	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider#getStyledText(java.lang.Object)
	 */
	@Override
    public StyledString getStyledText(Object element) {
	    return new StyledString(getText(element));
    }
 }
