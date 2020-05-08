 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.editor.sections;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.TypedEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
 import org.eclipse.tcf.te.tcf.core.Tcf;
 import org.eclipse.tcf.te.tcf.core.interfaces.ITransportTypes;
 import org.eclipse.tcf.te.tcf.core.peers.Peer;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
 import org.eclipse.tcf.te.tcf.locator.nodes.PeerRedirector;
 import org.eclipse.tcf.te.tcf.ui.controls.CustomTransportPanel;
 import org.eclipse.tcf.te.tcf.ui.controls.PipeTransportPanel;
 import org.eclipse.tcf.te.tcf.ui.controls.TcpTransportPanel;
 import org.eclipse.tcf.te.tcf.ui.editor.controls.TransportSectionTypeControl;
 import org.eclipse.tcf.te.tcf.ui.editor.controls.TransportSectionTypePanelControl;
 import org.eclipse.tcf.te.tcf.ui.nls.Messages;
 import org.eclipse.tcf.te.ui.controls.interfaces.IWizardConfigurationPanel;
 import org.eclipse.tcf.te.ui.forms.parts.AbstractSection;
 import org.eclipse.tcf.te.ui.interfaces.data.IDataExchangeNode;
 import org.eclipse.tcf.te.ui.interfaces.data.IDataExchangeNode3;
 import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
 import org.eclipse.tcf.te.ui.views.editor.pages.AbstractEditorPage;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 
 /**
  * Peer transport section implementation.
  */
 public class TransportSection extends AbstractSection {
 	// The section sub controls
 	private TransportSectionTypeControl transportTypeControl = null;
 	/* default */TransportSectionTypePanelControl transportTypePanelControl = null;
 
 	// Reference to the original data object
 	/* default */IPeerModel od;
 	// Reference to a copy of the original data
 	/* default */final IPropertiesContainer odc = new PropertiesContainer();
 	// Reference to the properties container representing the working copy for the section
 	/* default */final IPropertiesContainer wc = new PropertiesContainer();
 
 	/**
 	 * Constructor.
 	 *
 	 * @param form The parent managed form. Must not be <code>null</code>.
 	 * @param parent The parent composite. Must not be <code>null</code>.
 	 */
 	public TransportSection(IManagedForm form, Composite parent) {
 		super(form, parent, Section.DESCRIPTION);
 		createClient(getSection(), form.getToolkit());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.forms.AbstractFormPart#dispose()
 	 */
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (transportTypeControl != null) {
 			transportTypeControl.dispose();
 			transportTypeControl = null;
 		}
 		if (transportTypePanelControl != null) {
 			transportTypePanelControl.dispose();
 			transportTypePanelControl = null;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractSection#getAdapter(java.lang.Class)
 	 */
 	@Override
 	public Object getAdapter(Class adapter) {
 		if (TransportSectionTypeControl.class.equals(adapter)) {
 			return transportTypeControl;
 		}
 		if (TransportSectionTypePanelControl.class.equals(adapter)) {
 			return transportTypePanelControl;
 		}
 		return super.getAdapter(adapter);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see
 	 * org.eclipse.tcf.te.ui.forms.parts.AbstractSection#createClient(org.eclipse.ui.forms.widgets
 	 * .Section, org.eclipse.ui.forms.widgets.FormToolkit)
 	 */
 	@Override
 	protected void createClient(Section section, FormToolkit toolkit) {
 		Assert.isNotNull(section);
 		Assert.isNotNull(toolkit);
 
 		// Configure the section
 		section.setText(Messages.TransportSection_title);
 		section.setDescription(Messages.TransportSection_description);
 
 		if (section.getParent().getLayout() instanceof GridLayout) {
 			section.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		}
 
 		// Create the section client
 		Composite client = createClientContainer(section, 2, toolkit);
 		Assert.isNotNull(client);
 		section.setClient(client);
 
 		// Create the transport type control
 		transportTypeControl = new TransportSectionTypeControl(this);
 		transportTypeControl.setFormToolkit(toolkit);
 		transportTypeControl.setAdjustBackgroundColor(true);
 		transportTypeControl.setupPanel(client);
 
 		createEmptySpace(client, 2, toolkit);
 
 		// The transport type specific controls are placed into a stack
 		transportTypePanelControl = new TransportSectionTypePanelControl(this);
 
 		// Create and add the panels
 		if (isTransportTypeSupported(ITransportTypes.TRANSPORT_TYPE_TCP)) {
 			TcpTransportPanel tcpTransportPanel = new TcpTransportPanel(transportTypePanelControl) {
 				@Override
 				protected boolean isAdjustBackgroundColor() {
 					return true;
 				}
 			};
 			transportTypePanelControl.addConfigurationPanel(ITransportTypes.TRANSPORT_TYPE_TCP, tcpTransportPanel);
 		}
 		if (isTransportTypeSupported(ITransportTypes.TRANSPORT_TYPE_SSL)) {
 			TcpTransportPanel sslTransportPanel = new TcpTransportPanel(transportTypePanelControl) {
 				@Override
 				protected boolean isAdjustBackgroundColor() {
 					return true;
 				}
 			};
 			transportTypePanelControl.addConfigurationPanel(ITransportTypes.TRANSPORT_TYPE_SSL, sslTransportPanel);
 		}
 		if (isTransportTypeSupported(ITransportTypes.TRANSPORT_TYPE_PIPE)) {
 			PipeTransportPanel pipeTransportPanle = new PipeTransportPanel(transportTypePanelControl) {
 				@Override
 				protected boolean isAdjustBackgroundColor() {
 					return true;
 				}
 			};
 			transportTypePanelControl.addConfigurationPanel(ITransportTypes.TRANSPORT_TYPE_PIPE, pipeTransportPanle);
 		}
 		if (isTransportTypeSupported(ITransportTypes.TRANSPORT_TYPE_CUSTOM)) {
 			CustomTransportPanel customTransportPanel = new CustomTransportPanel(transportTypePanelControl) {
 				@Override
 				protected boolean isAdjustBackgroundColor() {
 					return true;
 				}
 			};
 			transportTypePanelControl.addConfigurationPanel(ITransportTypes.TRANSPORT_TYPE_CUSTOM, customTransportPanel);
 		}
 
 		// Setup the panel control
		transportTypePanelControl.setupPanel(client, transportTypePanelControl.getConfigurationPanelIds(), toolkit);
 		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		layoutData.horizontalSpan = 2;
 		transportTypePanelControl.getPanel().setLayoutData(layoutData);
 		toolkit.adapt(transportTypePanelControl.getPanel());
 
 		transportTypePanelControl.showConfigurationPanel(transportTypeControl
 		                .getSelectedTransportType());
 
 		// Adjust the control enablement
 		updateEnablement();
 
 		// Mark the control update as completed now
 		setIsUpdating(false);
 	}
 
 	/**
 	 * Override to control the availability of transport types.
 	 *
 	 * @param transportType
 	 * @return <code>true</code> if the given transport type should is available.
 	 */
 	public boolean isTransportTypeSupported(String transportType) {
 		return true;
 	}
 
 	/**
 	 * Indicates whether the sections parent page has become the active in the editor.
 	 *
 	 * @param active <code>True</code> if the parent page should be visible, <code>false</code>
 	 *            otherwise.
 	 */
 	public void setActive(boolean active) {
 		// If the parent page has become the active and it does not contain
 		// unsaved data, than fill in the data from the selected node
 		if (active) {
 			// Leave everything unchanged if the page is in dirty state
 			if (getManagedForm().getContainer() instanceof AbstractEditorPage && !((AbstractEditorPage) getManagedForm()
 			                .getContainer()).isDirty()) {
 				Object node = ((AbstractEditorPage) getManagedForm().getContainer())
 				                .getEditorInputNode();
 				if (node instanceof IPeerModel) {
 					setupData((IPeerModel) node);
 				}
 			}
 		}
 		else {
 			// Evaluate the dirty state even if going inactive
 			dataChanged(null);
 		}
 	}
 
 	/**
 	 * Initialize the page widgets based of the data from the given peer node.
 	 * <p>
 	 * This method may called multiple times during the lifetime of the page and the given
 	 * configuration node might be even <code>null</code>.
 	 *
 	 * @param node The peer node or <code>null</code>.
 	 */
 	public void setupData(final IPeerModel node) {
 		// If the section is dirty, nothing is changed
 		if (isDirty()) return;
 
 		boolean updateWidgets = true;
 
 		// If the passed in node is the same as the previous one,
 		// no need for updating the section widgets.
 		if ((node == null && od == null) || (node != null && node.equals(od))) {
 			updateWidgets = false;
 		}
 
 		// Besides the node itself, we need to look at the node data to determine
 		// if the widgets needs to be updated. For the comparisation, keep the
 		// current properties of the original data copy in a temporary container.
 		final IPropertiesContainer previousOdc = new PropertiesContainer();
 		previousOdc.setProperties(odc.getProperties());
 
 		// Store a reference to the original data
 		od = node;
 		// Clean the original data copy
 		odc.clearProperties();
 		// Clean the working copy
 		wc.clearProperties();
 
 		// If no data is available, we are done
 		if (node == null) return;
 
 		// Thread access to the model is limited to the executors thread.
 		// Copy the data over to the working copy to ease the access.
 		Protocol.invokeAndWait(new Runnable() {
 			@Override
 			public void run() {
 				// The section is handling the transport name and the
 				// transport type specific properties. Ignore other properties.
 				odc.setProperty(IPeer.ATTR_TRANSPORT_NAME, node.getPeer().getTransportName());
 				if (transportTypePanelControl != null) {
 					IPropertiesContainer src = new PropertiesContainer();
 					Map<String, String> properties = node.getPeer().getAttributes();
 					for (Entry<String, String> entry : properties.entrySet()) {
 						src.setProperty(entry.getKey(), entry.getValue());
 					}
 
 					for (String id : transportTypePanelControl.getConfigurationPanelIds()) {
 						IWizardConfigurationPanel panel = transportTypePanelControl
 						                .getConfigurationPanel(id);
 						if (panel instanceof IDataExchangeNode3) {
 							((IDataExchangeNode3) panel).copyData(src, odc);
 						}
 					}
 				}
 
 				// Initially, the working copy is a duplicate of the original data copy
 				wc.setProperties(odc.getProperties());
 			}
 		});
 
 		// From here on, work with the working copy only!
 
 		// If the original data copy does not match the previous original
 		// data copy, the widgets needs to be updated to present the correct data.
 		if (!previousOdc.getProperties().equals(odc.getProperties())) {
 			updateWidgets = true;
 		}
 
 		if (updateWidgets) {
 			// Mark the control update as in-progress now
 			setIsUpdating(true);
 
 			if (transportTypeControl != null) {
 				String transportType = wc.getStringProperty(IPeer.ATTR_TRANSPORT_NAME);
 				if (transportType != null && !"".equals(transportType)) { //$NON-NLS-1$
 					transportTypeControl.setSelectedTransportType(transportType);
 
 					if (transportTypePanelControl != null) {
 						transportTypePanelControl.showConfigurationPanel(transportType);
 						IWizardConfigurationPanel panel = transportTypePanelControl
 						                .getConfigurationPanel(transportType);
 						if (panel instanceof IDataExchangeNode) {
 							((IDataExchangeNode) panel).setupData(wc);
 						}
 					}
 				}
 			}
 
 			// Mark the control update as completed now
 			setIsUpdating(false);
 		}
 
 		// Re-evaluate the dirty state
 		dataChanged(null);
 		// Adjust the control enablement
 		updateEnablement();
 	}
 
 	/**
 	 * Stores the page widgets current values to the given peer node.
 	 * <p>
 	 * This method may called multiple times during the lifetime of the page and the given peer node
 	 * might be even <code>null</code>.
 	 *
 	 * @param node The GDB Remote configuration node or <code>null</code>.
 	 */
 	public void extractData(final IPeerModel node) {
 		// If no data is available, we are done
 		if (node == null) {
 			return;
 		}
 
 		// The list of removed attributes
 		final List<String> removed = new ArrayList<String>();
 		// Get the current key set from the working copy
 		Set<String> currentKeySet = wc.getProperties().keySet();
 
 		// Get the current transport type from the working copy
 		String oldTransportType = wc.getStringProperty(IPeer.ATTR_TRANSPORT_NAME);
 		if (oldTransportType != null) {
 			// Get the current transport type configuration panel
 			IWizardConfigurationPanel panel = transportTypePanelControl
 			                .getConfigurationPanel(oldTransportType);
 			// And clean out the current transport type specific attributes from the working copy
 			if (panel instanceof IDataExchangeNode3) {
 				((IDataExchangeNode3) panel).removeData(wc);
 			}
 		}
 
 		// Get the new transport type from the widget
 		String transportType = transportTypeControl.getSelectedTransportType();
 		// And write the new transport to the working copy
 		wc.setProperty(IPeer.ATTR_TRANSPORT_NAME, transportType);
 		// Get the new transport type configuration panel
 		IWizardConfigurationPanel panel = transportTypePanelControl
 		                .getConfigurationPanel(transportType);
 		// And extract the new attributes into the working copy
 		if (panel instanceof IDataExchangeNode) {
 			((IDataExchangeNode) panel).extractData(wc);
 		}
 
 		// If the data has not changed compared to the original data copy,
 		// we are done here and return immediately
 		if (odc.equals(wc)) {
 			return;
 		}
 
 		// Get the new key set from the working copy
 		Set<String> newKeySet = wc.getProperties().keySet();
 		// Everything from the old key set not found in the new key set is a removed attribute
 		for (String key : currentKeySet) {
 			if (!newKeySet.contains(key)) {
 				removed.add(key);
 			}
 		}
 
 		// Copy the working copy data back to the original properties container
 		Protocol.invokeAndWait(new Runnable() {
 			@Override
 			public void run() {
 				// To update the peer attributes, the peer needs to be recreated
 				IPeer oldPeer = node.getPeer();
 				// Create a write able copy of the peer attributes
 				Map<String, String> attributes = new HashMap<String, String>(oldPeer
 				                .getAttributes());
 				// Clean out the removed attributes
 				for (String key : removed) {
 					attributes.remove(key);
 				}
 				// Update with the current configured attributes
 				for (String key : wc.getProperties().keySet()) {
 					String value = wc.getStringProperty(key);
 					if (value != null) {
 						attributes.put(key, value);
 					}
 					else {
 						attributes.remove(key);
 					}
 				}
 
 				// If there is still a open channel to the old peer, close it by force
 				IChannel channel = Tcf.getChannelManager().getChannel(oldPeer);
 				if (channel != null) {
 					channel.close();
 				}
 
 				// Create the new peer
 				IPeer newPeer = oldPeer instanceof PeerRedirector ? new PeerRedirector(((PeerRedirector) oldPeer)
 				                .getParent(), attributes) : new Peer(attributes);
 				// Update the peer node instance (silently)
 				boolean changed = node.setChangeEventsEnabled(false);
 				node.setProperty(IPeerModelProperties.PROP_INSTANCE, newPeer);
 				// As the transport changed, we have to reset the state back to "unknown"
 				// and clear out the services and DNS markers
 				node.setProperty(IPeerModelProperties.PROP_STATE, IPeerModelProperties.STATE_UNKNOWN);
 				node.setProperty("dns.name.transient", null); //$NON-NLS-1$
 				node.setProperty("dns.lastIP.transient", null); //$NON-NLS-1$
 				node.setProperty("dns.skip.transient", null); //$NON-NLS-1$
 
 				ILocatorModelUpdateService service = node.getModel()
 				                .getService(ILocatorModelUpdateService.class);
 				service.updatePeerServices(node, null, null);
 
 				if (changed) {
 					node.setChangeEventsEnabled(true);
 				}
 			}
 		});
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractSection#isValid()
 	 */
 	@Override
 	public boolean isValid() {
 		// Validation is skipped while the controls are updated
 		if (isUpdating()) return true;
 
 		boolean valid = super.isValid();
 
 		if (transportTypeControl != null) {
 			valid &= transportTypeControl.isValid();
 			if (transportTypeControl.getMessageType() > getMessageType()) {
 				setMessage(transportTypeControl.getMessage(), transportTypeControl.getMessageType());
 			}
 		}
 
 		if (transportTypePanelControl != null) {
 			valid &= transportTypePanelControl.isValid();
 			if (transportTypePanelControl.getMessageType() > getMessageType()) {
 				setMessage(transportTypePanelControl.getMessage(), transportTypePanelControl.getMessageType());
 			}
 		}
 
 		return valid;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
 	 */
 	@Override
 	public void commit(boolean onSave) {
 		// Remember the current dirty state
 		boolean needsSaving = isDirty();
 		// Call the super implementation (resets the dirty state)
 		super.commit(onSave);
 
 		// Nothing to do if not on save or saving is not needed
 		if (!onSave || !needsSaving) {
 			return;
 		}
 		// Extract the data into the original data node
 		extractData(od);
 	}
 
 	/**
 	 * Called to signal that the data associated has been changed.
 	 *
 	 * @param e The event which triggered the invocation or <code>null</code>.
 	 */
 	public void dataChanged(TypedEvent e) {
 		// dataChanged is not evaluated while the controls are updated
 		if (isUpdating()) return;
 
 		boolean isDirty = false;
 
 		if (transportTypeControl != null) {
 			String transportType = transportTypeControl.getSelectedTransportType();
 			if ("".equals(transportType)) { //$NON-NLS-1$
 				String value = odc.getStringProperty(IPeer.ATTR_TRANSPORT_NAME);
 				isDirty |= value != null && !"".equals(value.trim()); //$NON-NLS-1$
 			}
 			else {
 				isDirty |= !odc.isProperty(IPeer.ATTR_TRANSPORT_NAME, transportType);
 			}
 
 			if (transportTypePanelControl != null) {
 				IWizardConfigurationPanel panel = transportTypePanelControl
 				                .getConfigurationPanel(transportType);
 				if (panel != null) {
 					isDirty |= panel.dataChanged(odc, e);
 				}
 			}
 		}
 
 		// If dirty, mark the form part dirty.
 		// Otherwise call refresh() to reset the dirty (and stale) flag
 		markDirty(isDirty);
 
 		// Adjust the control enablement
 		updateEnablement();
 	}
 
 	/**
 	 * Updates the given set of attributes with the current values of the page widgets.
 	 *
 	 * @param attributes The attributes to update. Must not be <code>null</code>:
 	 */
 	public void updateAttributes(IPropertiesContainer attributes) {
 		Assert.isNotNull(attributes);
 
 		if (transportTypePanelControl != null) {
 			String[] ids = transportTypePanelControl.getConfigurationPanelIds();
 			for (String id : ids) {
 				IWizardConfigurationPanel panel = transportTypePanelControl.getConfigurationPanel(id);
 				if (panel instanceof IDataExchangeNode) {
 					if (panel instanceof IDataExchangeNode3) {
 						((IDataExchangeNode3) panel).removeData(attributes);
 					}
 				}
 			}
 			IWizardConfigurationPanel panel = transportTypePanelControl.getActiveConfigurationPanel();
 			if (panel instanceof IDataExchangeNode) {
 				((IDataExchangeNode) panel).extractData(attributes);
 			}
 		}
 
 		if (transportTypeControl != null) {
 			attributes.setProperty(IPeer.ATTR_TRANSPORT_NAME, transportTypeControl
 			                .getSelectedTransportType());
 		}
 	}
 
 	/**
 	 * Updates the control enablement.
 	 */
 	protected void updateEnablement() {
 		// Determine the input
 		final Object input = od; // getManagedForm().getInput();
 
 		// Determine if the peer is a static peer
 		final AtomicBoolean isStatic = new AtomicBoolean();
 		final AtomicBoolean isRemote = new AtomicBoolean();
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				if (input instanceof IPeerModel) {
 					isStatic.set(((IPeerModel) input).isStatic());
 					isRemote.set(((IPeerModel) input).isRemote());
 				}
 			}
 		};
 
 		if (Protocol.isDispatchThread()) runnable.run();
 		else Protocol.invokeAndWait(runnable);
 
 		// The transport type control is enabled for static peers
 		if (transportTypeControl != null) {
 			boolean enabled = input == null || (isStatic.get() && !isRemote.get());
 			SWTControlUtil.setEnabled(transportTypeControl.getEditFieldControl(), enabled);
 			if (transportTypePanelControl != null) {
 				IWizardConfigurationPanel panel = transportTypePanelControl
 				                .getConfigurationPanel(transportTypeControl
 				                                .getSelectedTransportType());
 				if (panel != null) {
 					panel.setEnabled(enabled);
 				}
 			}
 		}
 	}
 }
