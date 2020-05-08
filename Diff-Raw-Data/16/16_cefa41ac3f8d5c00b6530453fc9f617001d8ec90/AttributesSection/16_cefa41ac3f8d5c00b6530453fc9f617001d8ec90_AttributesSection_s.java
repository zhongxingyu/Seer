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
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.TypedEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
 import org.eclipse.tcf.te.tcf.core.peers.Peer;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
 import org.eclipse.tcf.te.tcf.locator.nodes.PeerRedirector;
 import org.eclipse.tcf.te.tcf.ui.controls.PeerAttributesTablePart;
 import org.eclipse.tcf.te.tcf.ui.nls.Messages;
 import org.eclipse.tcf.te.ui.forms.parts.AbstractSection;
 import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
 import org.eclipse.tcf.te.ui.views.editor.pages.AbstractEditorPage;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 
 /**
  * Peer custom attributes section implementation.
  */
 public class AttributesSection extends AbstractSection {
 	// The section sub controls
 	private PeerAttributesTablePart tablePart;
 
 	// Reference to the original data object
 	/* default */ IPeerModel od;
 	// Reference to a copy of the original data
 	/* default */ final IPropertiesContainer odc = new PropertiesContainer();
 	// Reference to the properties container representing the working copy for the section
 	/* default */ final IPropertiesContainer wc = new PropertiesContainer();
 
 	/*
 	 * The list of banned names for the table parts. Banned names are names the user is not allowed
 	 * to add to the table.
 	 */
 	private static final String[] BANNED_NAMES = new String[] {
 		IPeer.ATTR_ID, IPeer.ATTR_AGENT_ID, IPeer.ATTR_SERVICE_MANGER_ID, "ServerManagerID", //$NON-NLS-1$
 		IPeer.ATTR_NAME, IPeer.ATTR_TRANSPORT_NAME, IPeer.ATTR_IP_HOST,
 		IPeer.ATTR_IP_PORT, "PipeName", //$NON-NLS-1$
 		"redirect.proxy", //$NON-NLS-1$
 		IPeerModelProperties.PROP_VISIBLE, "ClientID" //$NON-NLS-1$
 	};
 
 	/*
 	 * The list of filtered attributes. This attributes are filtered from the data given to the
 	 * table part on initialization. The attributes listed here are handled by other controls of the
 	 * overview page.
 	 */
 	private static final String[] FILTERED_NAMES = new String [] {
 		IPeer.ATTR_ID, IPeer.ATTR_AGENT_ID, IPeer.ATTR_SERVICE_MANGER_ID, "ServerManagerID", //$NON-NLS-1$
 		IPeer.ATTR_NAME, IPeer.ATTR_TRANSPORT_NAME, IPeer.ATTR_IP_HOST,
 		IPeer.ATTR_IP_PORT, "PipeName", //$NON-NLS-1$
 		"redirect.proxy", //$NON-NLS-1$
 		IPeerModelProperties.PROP_VISIBLE, "ClientID" //$NON-NLS-1$
 	};
 
 	/**
 	 * Constructor.
 	 *
 	 * @param form The parent managed form. Must not be <code>null</code>.
 	 * @param parent The parent composite. Must not be <code>null</code>.
 	 */
 	public AttributesSection(IManagedForm form, Composite parent) {
 		super(form, parent, Section.DESCRIPTION);
 		createClient(getSection(), form.getToolkit());
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.AbstractFormPart#dispose()
 	 */
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (tablePart != null) { tablePart.dispose(); tablePart = null; }
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractSection#getAdapter(java.lang.Class)
 	 */
 	@Override
 	public Object getAdapter(Class adapter) {
 		if (PeerAttributesTablePart.class.equals(adapter)) {
 			return tablePart;
 		}
 		return super.getAdapter(adapter);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractSection#createClient(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit)
 	 */
 	@Override
 	protected void createClient(Section section, FormToolkit toolkit) {
 		Assert.isNotNull(section);
 		Assert.isNotNull(toolkit);
 
 		// Configure the section
 		section.setText(Messages.AttributesSection_title);
 		section.setDescription(Messages.AttributesSection_description);
 
 		section.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 
 		// Create the section client
 		Composite client = createClientContainer(section, 2, toolkit);
 		Assert.isNotNull(client);
 		section.setClient(client);
 
 		tablePart = new PeerAttributesTablePart() {
 			@Override
 			protected void onTableModified() {
 				dataChanged(null);
 			}
 		};
 		tablePart.setMinSize(SWTControlUtil.convertWidthInCharsToPixels(client, 20), SWTControlUtil.convertHeightInCharsToPixels(client, 6));
 		tablePart.setBannedNames(BANNED_NAMES);
 		tablePart.createControl(client, SWT.SINGLE | SWT.FULL_SELECTION, 2, toolkit);
 
 		// Adjust the control enablement
 		updateEnablement();
 
 		// Mark the control update as completed now
 		setIsUpdating(false);
 	}
 
 	/**
 	 * Indicates whether the sections parent page has become the active in the editor.
 	 *
 	 * @param active <code>True</code> if the parent page should be visible, <code>false</code> otherwise.
 	 */
 	public void setActive(boolean active) {
 		// If the parent page has become the active and it does not contain
 		// unsaved data, than fill in the data from the selected node
 		if (active) {
 			// Leave everything unchanged if the page is in dirty state
 			if (getManagedForm().getContainer() instanceof AbstractEditorPage
 							&& !((AbstractEditorPage)getManagedForm().getContainer()).isDirty()) {
 				Object node = ((AbstractEditorPage)getManagedForm().getContainer()).getEditorInputNode();
 				if (node instanceof IPeerModel) {
 					setupData((IPeerModel)node);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Initialize the page widgets based of the data from the given peer node.
 	 * <p>
 	 * This method may called multiple times during the lifetime of the page and
 	 * the given configuration node might be even <code>null</code>.
 	 *
 	 * @param node The peer node or <code>null</code>.
 	 */
 	public void setupData(final IPeerModel node) {
 		// Store a reference to the original data
 		od = node;
 		// Clean the original data copy
 		odc.clearProperties();
 		// Clean the working copy
 		wc.clearProperties();
 
 		// If no data is available, we are done
 		if (node == null) {
 			return;
 		}
 
 		// Thread access to the model is limited to the executors thread.
 		// Copy the data over to the working copy to ease the access.
 		Protocol.invokeAndWait(new Runnable() {
 			@Override
 			public void run() {
 				Map<String, String> properties = node.getPeer().getAttributes();
 				for (Entry<String, String> entry : properties.entrySet()) {
 					wc.setProperty(entry.getKey(), entry.getValue());
 					odc.setProperty(entry.getKey(), entry.getValue());
 				}
 			}
 		});
 
 		// From here on, work with the working copy only!
 
 		// Remove the filtered attributes from the original data and working copy in
 		// order to get an exact base to determine the dirty state of the section.
 		for (String filteredName : FILTERED_NAMES) {
 			wc.setProperty(filteredName, null);
 			odc.setProperty(filteredName, null);
 		}
 
 		// Remove all "*.transient" properties
 		for (String name : odc.getProperties().keySet()) {
 			if (name.endsWith(".silent") || name.endsWith(".transient")) { //$NON-NLS-1$ //$NON-NLS-2$
 				wc.setProperty(name, null);
 				odc.setProperty(name, null);
 			}
 		}
 
 		// Make a <String, String> map out of the remaining properties
 		Map<String, String> attributes = new HashMap<String, String>();
 		Map<String, Object> properties = wc.getProperties();
 		for (Entry<String, Object> entry : properties.entrySet()) {
 			attributes.put(entry.getKey(), entry.getValue().toString());
 		}
 
 		// Pass on to the table part
 		if (tablePart != null) {
 			tablePart.setAttributes(attributes);
 		}
 
 		// Adjust the control enablement
 		updateEnablement();
 	}
 
 	/**
 	 * Stores the page widgets current values to the given peer node.
 	 * <p>
 	 * This method may called multiple times during the lifetime of the page and
 	 * the given peer node might be even <code>null</code>.
 	 *
 	 * @param node The GDB Remote configuration node or <code>null</code>.
 	 */
 	public void extractData(final IPeerModel node) {
 		// If no data is available, we are done
 		if (node == null) {
 			return;
 		}
 
 		// Extract the table part attributes into the working copy
 		// Properties not longer available in the table part attributes
 		// are removed.
 		Map<String, String> attributes = tablePart.getAttributes();
 		Map<String, Object> properties = wc.getProperties();
 		final List<String> removed = new ArrayList<String>();
 		for (String key : properties.keySet()) {
 			if (attributes.containsKey(key)) {
 				wc.setProperty(key, attributes.get(key));
 				attributes.remove(key);
 			} else {
 				wc.setProperty(key, null);
 				removed.add(key);
 			}
 		}
 		// Add all remaining (new) attributes
 		if (!attributes.isEmpty()) {
 			for (String key : attributes.keySet()) {
 				wc.setProperty(key, attributes.get(key));
 			}
 		}
 
 		// Copy the working copy data back to the original properties container
 		Protocol.invokeAndWait(new Runnable() {
 			@Override
 			public void run() {
 				// To update the peer attributes, the peer needs to be recreated
 				IPeer oldPeer = node.getPeer();
 				// Create a write able copy of the peer attributes
 				Map<String, String> attributes = new HashMap<String, String>(oldPeer.getAttributes());
 				// Update with the current configured attributes
 				for (String key : wc.getProperties().keySet()) {
 					String value = wc.getStringProperty(key);
 					if (value != null) {
 						attributes.put(key, value);
 					} else {
 						attributes.remove(key);
 					}
 				}
 				// Remove the disappeared properties
 				for (String key : removed) {
 					attributes.remove(key);
 				}
 
 				// Create the new peer
 				IPeer newPeer = oldPeer instanceof PeerRedirector ? new PeerRedirector(((PeerRedirector)oldPeer).getParent(), attributes) : new Peer(attributes);
 				// Update the peer node instance (silently)
 				boolean changed = node.setChangeEventsEnabled(false);
 				node.setProperty(IPeerModelProperties.PROP_INSTANCE, newPeer);
 				if (changed) {
 					node.setChangeEventsEnabled(true);
 				}
 			}
 		});
 	}
 
 	/* (non-Javadoc)
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
 
 		if (tablePart != null) {
 			// Get the attributes from the table part
 			Map<String, String> attributes = tablePart.getAttributes();
 			Map<String, Object> properties = odc.getProperties();
 			if (attributes.size() != properties.size()) {
 				isDirty = true;
 			} else {
 				for (Entry<String, String> entry : attributes.entrySet()) {
 					if (!properties.containsKey(entry.getKey()) || !properties.get(entry.getKey()).equals(entry.getValue())) {
 						isDirty = true;
 						break;
 					}
 				}
 			}
 		}
 
 		// If dirty, mark the form part dirty.
 		// Otherwise call refresh() to reset the dirty (and stale) flag
 		markDirty(isDirty);
 	}
 
 	/**
 	 * Updates the control enablement.
 	 */
 	protected void updateEnablement() {
 		// Determine the input
 		final Object input = getManagedForm().getInput();
 
 		// Determine if the peer is a static peer
 		final AtomicBoolean isStatic = new AtomicBoolean();
 		final AtomicBoolean isRemote = new AtomicBoolean();
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				if (input instanceof IPeerModel) {
 					String value = ((IPeerModel)input).getPeer().getAttributes().get("static.transient"); //$NON-NLS-1$
 					isStatic.set(value != null && Boolean.parseBoolean(value.trim()));
 
 					value = ((IPeerModel)input).getPeer().getAttributes().get("remote.transient"); //$NON-NLS-1$
 					isRemote.set(value != null && Boolean.parseBoolean(value.trim()));
 				}
 			}
 		};
 		if (Protocol.isDispatchThread()) {
 			runnable.run();
 		}
 		else {
 			Protocol.invokeAndWait(runnable);
 		}
 
 		if (tablePart != null) {
 			tablePart.setReadOnly(!isStatic.get() || isRemote.get());
 		}
 	}
 }
