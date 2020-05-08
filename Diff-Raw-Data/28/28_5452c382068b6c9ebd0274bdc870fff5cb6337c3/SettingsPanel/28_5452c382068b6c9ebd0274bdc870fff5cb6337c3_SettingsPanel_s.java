 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.desktop.impl.dialogues.settings;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.cm.ConfigurationEvent;
 import org.osgi.service.cm.ConfigurationListener;
 import org.osgi.service.metatype.AttributeDefinition;
 import org.osgi.service.metatype.MetaTypeInformation;
 import org.osgi.service.metatype.MetaTypeProvider;
 import org.osgi.service.metatype.MetaTypeService;
 import org.osgi.service.metatype.ObjectClassDefinition;
 
 import org.paxle.core.io.IOTools;
 import org.paxle.core.metadata.Attribute;
 import org.paxle.core.metadata.Metadata;
 import org.paxle.desktop.Utilities;
 import org.paxle.desktop.impl.DesktopServices;
 import org.paxle.desktop.impl.Messages;
 import org.paxle.desktop.impl.ServiceManager;
 import org.paxle.desktop.impl.dialogues.DIServicePanel;
 import org.paxle.desktop.impl.dialogues.ScrollablePanel;
 import org.paxle.desktop.impl.event.MultipleChangesListener;
 
 public class SettingsPanel extends DIServicePanel implements ConfigurationListener {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private static GridBagConstraints createGbcLabel(final int y) {
 		final GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = 0; gbc.gridy = y;
 		gbc.anchor = GridBagConstraints.EAST;
 		gbc.insets = Utilities.INSETS_DEFAULT;
 		return gbc;
 	}
 	
 	private static GridBagConstraints createGbcOptSingle(final int y) {
 		final GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = 1; gbc.gridy = y;
 		gbc.anchor = GridBagConstraints.WEST;
 		gbc.weightx = .4;
 		gbc.insets = Utilities.INSETS_DEFAULT;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		return gbc;
 	}
 	
 	private static GridBagConstraints createGbcOptMulti(final int y) {
 		return createGbcOptSingle(y);
 	}
 	
 	private static GridBagConstraints createGbcDesc(final int y) {
 		final GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = 2; gbc.gridy = y;
 		gbc.anchor = GridBagConstraints.WEST;
 		gbc.weightx = .6;
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.insets = Utilities.INSETS_DEFAULT;
 		return gbc;
 	}
 	
 	private static GridBagConstraints createGbcHead() {
 		final GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = 0; gbc.gridy = 0;
 		gbc.gridwidth = 3;
 		gbc.anchor = GridBagConstraints.CENTER;
 		gbc.insets = Utilities.INSETS_DEFAULT;
 		return gbc;
 	}
 	
 	private static GridBagConstraints createGbcBottom(final int y) {
 		final GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = 0; gbc.gridy = y;
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.weightx = 1.0;
 		gbc.weighty = 1.0;
 		gbc.gridwidth = 3;
 		return gbc;
 	}
 	
 	private static GridBagConstraints createGbcHeader(final int y) {
 		final GridBagConstraints gbc = createGbcDesc(y);
 		gbc.gridx = 1;
 		gbc.gridwidth = 2;
 		gbc.anchor = GridBagConstraints.NORTHWEST;
 		return gbc;
 	}
 	
 	private final class BundleConfig implements ActionListener {
 		
 		boolean changed = false;
 		
 		private final MetaTypeProvider metaTypeProvider;
 		private final Bundle bundle;
 		private final String pid;
 		private final int iconSize;
 		
 		private ObjectClassDefinition ocd = null;
 		private MultipleChangesListener optionsComp = null;
 		private JPanel optionsPanel = null;
 		private List<AbstractAttrConfig<?>> aconfs = null;
 		private String name = null;
 		private ImageIcon icon = null;
 		
 		public BundleConfig(final int iconSize, final Bundle bundle, final String pid) {
 			this(iconSize, bundle, pid, null);
 		}
 		
 		public BundleConfig(final int iconSize, final Bundle bundle, final String pid, final MetaTypeProvider metaProvider) {
 			this.iconSize = iconSize;
 			this.bundle = bundle;
 			this.pid = pid;
 			this.metaTypeProvider = metaProvider;
 		}
 		
 		public void save() throws IOException {
 			if (aconfs == null)
 				throw new RuntimeException("not initialized"); //$NON-NLS-1$
 			final Hashtable<String,Object> props = new Hashtable<String,Object>();
 			for (final AbstractAttrConfig<?> aconf : aconfs)
 				aconf.save(props);
 			getConfiguration().update(props);
 			optionsComp.resetDefaults();
 			changed = false;
 		}
 		
 		public void reset() throws IOException {
 			if (!isInitialized())
 				initOptions();
 			final Hashtable<String,Object> props = new Hashtable<String,Object>();
 			for (final AbstractAttrConfig<?> aconf : aconfs)
 				aconf.reset(props);
 			getConfiguration().update(props);
 			changed = false;
 		}
 		
 		public void deinit() {
 			ocd = null;
 			if (optionsComp != null) {
 				mcl.removeComp2Monitor(optionsComp);
 				optionsComp = null;
 			}
 			aconfs = null;
 			name = null;
 			icon = null;
 		}
 		
 		@Override
 		protected void finalize() throws Throwable {
 			if (optionsComp != null)
 				mcl.removeComp2Monitor(optionsComp);
 		}
 		
 		public String getName() {
 			if (name == null)
 				name = getOCD().getName();
 			return name;
 		}
 		
 		public boolean isChanged() {
 			return isInitialized() && changed;
 		}
 		
 		public boolean isInitialized() {
 			return aconfs != null;
 		}
 		
 		public ImageIcon getIcon() {
 			if (icon == null) try {
 				InputStream is = getOCD().getIcon(iconSize);
 				if (is == null) {
 					// fallback to default icon
 					is = this.getClass().getResourceAsStream("/OSGI-INF/images/cog.png"); //$NON-NLS-1$
 				}					
 				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
 				IOTools.copy(is, baos);
 				is.close();
 				icon = new ImageIcon(baos.toByteArray());
 			} catch (IOException e) { e.printStackTrace(); }
 			return icon;
 		}
 		
 		public JPanel getOptionsPanel() {
 			if (optionsComp == null)
 				initOptions();
 			return optionsPanel;
 		}
 		
 		public MultipleChangesListener getChangesListener() {
 			if (optionsComp == null)
 				initOptions();
 			return optionsComp;
 		}
 		
 		private ObjectClassDefinition getOCD() {
 			if (ocd == null) {
 				if (metaTypeProvider != null) {
 					ocd = metaTypeProvider.getObjectClassDefinition(pid, LOCALE);
 				} else {
 					final MetaTypeInformation mtinfo = metatypeService.getMetaTypeInformation(bundle);
 					ocd = mtinfo.getObjectClassDefinition(pid, LOCALE);
 				}
 			}
 			return ocd;
 		}
 		
 		private Configuration getConfiguration() {
 			try {
 				return cadmin.getConfiguration(pid, bundle.getLocation());
 			} catch (IOException e) { e.printStackTrace(); return null; }
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			final String ac = e.getActionCommand();
 			try {
 				if (ac == Actions.SAVE.name()) {
 					save();
 				} else if (ac == Actions.RESET.name()) {
 					reset();
 				}
 			} catch (IOException ee) { ee.printStackTrace(); }
 		}
 		
 		private void initOptions() {
 			aconfs = new LinkedList<AbstractAttrConfig<?>>();
 			final ObjectClassDefinition ocd = getOCD();
 			
 			final JPanel panel = new JPanel(new GridBagLayout());
 			optionsComp = new MultipleChangesListener(this, Actions.SAVE.name(), Actions.RESET.name());
 			mcl.addComp2Monitor(optionsComp);
 			optionsComp.getSaveButton().setText(Messages.getString("settingsPanel.save")); //$NON-NLS-1$
 			optionsComp.getResetButton().setText(Messages.getString("settingsPanel.reset")); //$NON-NLS-1$
 			optionsPanel = new ScrollablePanel(new BorderLayout(), ScrollablePanel.ALWAYS_TRACK, ScrollablePanel.MAXIMIZE);
 			optionsPanel.add(optionsComp.layoutButtonsDefault(true), BorderLayout.SOUTH);
 			optionsPanel.add(panel);
 			
 			final GridBagConstraints gbcHead = createGbcHead();
 			final JLabel heading = new JLabel(ocd.getName());
 			heading.setFont(heading.getFont().deriveFont(Font.BOLD, 20f));
 			panel.add(heading, gbcHead);
 			incGridY(gbcHead);
 			
 			final int y = gbcHead.gridy;
 			final GridBagConstraints gbcHeader = createGbcHeader(y);
 			final GridBagConstraints gbcLabel = createGbcLabel(y);
 			final GridBagConstraints gbcOptSingle = createGbcOptSingle(y);
 			final GridBagConstraints gbcOptMulti = createGbcOptMulti(y);
 			final GridBagConstraints gbcDesc = createGbcDesc(y);
 			
 			panel.add(new JLabel(Messages.getString("settingsPanel.description")), gbcLabel); //$NON-NLS-1$
 			panel.add(Utilities.setTextLabelDefaults(new JTextArea(ocd.getDescription())), gbcHeader);
 			incGridY(gbcHeader, gbcLabel, gbcOptSingle, gbcOptMulti, gbcDesc);
 			panel.add(new JLabel(Messages.getString("settingsPanel.bundle")), gbcLabel); //$NON-NLS-1$
 			panel.add(new JLabel(bundle.getSymbolicName()), gbcHeader);
 			incGridY(gbcHeader, gbcLabel, gbcOptSingle, gbcOptMulti, gbcDesc);
 			
 			final HashMap<String,Attribute> attrMetadata;
 			final Metadata metadata = ocd.getClass().getAnnotation(Metadata.class);
 			if (metadata == null) {
 				attrMetadata = null;
 			} else {
 				attrMetadata = new HashMap<String,Attribute>();
 				for (final Attribute attr : metadata.value())
 					attrMetadata.put(attr.id(), attr);
 			}
 			
 			final Dictionary<?,?> properties = getConfiguration().getProperties();
 			for (final AttributeDefinition ad : ocd.getAttributeDefinitions(ObjectClassDefinition.ALL)) {
 				final String id = ad.getID();
 				final AbstractAttrConfig<?> aconf = AbstractAttrConfig.createAttrConfig(ad,
 						(attrMetadata == null) ? null : attrMetadata.get(id));
 				aconf.addConfigLine(
						optionsComp, panel, (properties == null) ? null : properties.get(id),
 						gbcLabel, gbcOptSingle, gbcOptMulti, gbcDesc);
 				aconfs.add(aconf);
 				incGridY(gbcLabel, gbcOptSingle, gbcOptMulti, gbcDesc);
 			}
 			
 			panel.add(new JPanel(), createGbcBottom(gbcLabel.gridy));		// clear bottom line
 			optionsComp.resetDefaults();
 		}
 		
 		private void incGridY(final GridBagConstraints... constraints) {
 			for (final GridBagConstraints gbc : constraints)
 				gbc.gridy++;
 		}
 	}
 	
 	private static final String LOCALE = Locale.getDefault().getLanguage(); //$NON-NLS-1$
 	private static final int ICON_SIZE = 16;
 	
 	private static final Dimension DIM_SETTINGS = new Dimension(1000, 600);
 	
 	private static enum Actions {
 		SAVE, RESET, REFRESH, LIST_SELECT
 	}
 	
 	private final ServiceManager manager; 
 	private final ConfigurationAdmin cadmin;
 	private final MetaTypeService metatypeService;
 	private final Map<String,BundleConfig> confMap;
 	private final ActionRunnable saveRunnable = new ActionRunnable(Actions.SAVE);
 	private final ActionRunnable resetRunnable = new ActionRunnable(Actions.RESET);
 	
 	private final MultipleChangesListener mcl = new MultipleChangesListener(saveRunnable, Actions.SAVE.name(), Actions.RESET.name());
 	
 	private JList list;
 	private JScrollPane optViewPanel;
 	
 	public SettingsPanel(final DesktopServices services) {
 		super(services, DIM_SETTINGS);
 		this.manager = services.getServiceManager();
 		this.cadmin = manager.getService(ConfigurationAdmin.class);
 		this.metatypeService = manager.getService(MetaTypeService.class);
 		this.confMap = Collections.synchronizedMap(initConfMap(manager.getBundles()));
 		init();
 	}
 	
 	@Override
 	public String getTitle() {
 		return Messages.getString("settingsPanel.title"); //$NON-NLS-1$
 	}
 	
 	private final class ActionRunnable extends DefaultListCellRenderer implements ListCellRenderer, ListSelectionListener, Runnable, ActionListener {
 		
 		private static final long serialVersionUID = 1L;
 		
 		private final Actions cmd;
 		private final BundleConfig bc;
 		
 		public ActionRunnable(final Actions cmd) {
 			this(cmd, null);
 		}
 		
 		public ActionRunnable(final Actions cmd, final BundleConfig bc) {
 			this.cmd = cmd;
 			this.bc = bc;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			final String ac = e.getActionCommand();
 			final Runnable r;
 			if (ac == Actions.SAVE.name()) {
 				r = saveRunnable;
 			} else if (ac == Actions.RESET.name()) {
 				r = resetRunnable;
 			} else {
 				return;
 			}
 			SwingUtilities.invokeLater(r);
 		}
 		
 		public void run() {
 			switch (cmd) {
 				case REFRESH: {
 					bc.deinit();
 					list.revalidate();
 					list.repaint();
 				} // fall through
 				
 				case LIST_SELECT: {
 					optViewPanel.setViewportView(bc.getOptionsPanel());
 					optViewPanel.revalidate();
 					optViewPanel.repaint();
 				} break;
 				
 				case SAVE: try {
 					final BundleConfig sel = (BundleConfig)list.getSelectedValue();
 					list.setValueIsAdjusting(true);
 					for (final BundleConfig bc : confMap.values())
 						if (bc.isChanged())
 							bc.save();
 					list.setValueIsAdjusting(false);
 					list.setSelectedValue(sel, true);
 				} catch (IOException e) { e.printStackTrace(); }
 				break;
 				
 				case RESET: try {
 					final BundleConfig sel = (BundleConfig)list.getSelectedValue();
 					list.setValueIsAdjusting(true);
 					for (final BundleConfig bc : confMap.values())
 						bc.reset();
 					list.setValueIsAdjusting(false);
 					list.setSelectedValue(sel, true);
 				} catch (IOException e) { e.printStackTrace(); }
 				break;
 				
 				default:
 					throw new RuntimeException("switch-statement does not cover action '" + cmd + "'"); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 		
 		@Override
 		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 			super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
 			
 			final BundleConfig cfg = (BundleConfig)value;
 			super.setIcon(cfg.getIcon());
 			super.setText(cfg.getName());
 			
 			return this;
 		}
 		
 		public void valueChanged(ListSelectionEvent e) {
 			if (!e.getValueIsAdjusting())
 				SwingUtilities.invokeLater(new ActionRunnable(Actions.LIST_SELECT, (BundleConfig)list.getSelectedValue()));
 		}
 	}
 	
 	public void configurationEvent(ConfigurationEvent event) {
 		if ((event.getType() & ConfigurationEvent.CM_UPDATED) == ConfigurationEvent.CM_UPDATED) {
 			final BundleConfig bc = confMap.get(event.getPid());
 			if (bc == list.getSelectedValue() && !list.getValueIsAdjusting())
 				SwingUtilities.invokeLater(new ActionRunnable(Actions.REFRESH, bc));
 		}
 	}
 	
 	private Map<String,BundleConfig> initConfMap(final Bundle[] bundles) {
 		final Map<String,BundleConfig> confs = new TreeMap<String,BundleConfig>();
 		for (final Bundle bundle : bundles) {
 			
 			// lookup metatypes by metatype-servcie
 			final MetaTypeInformation mtinfo = this.metatypeService.getMetaTypeInformation(bundle);			
 			if (mtinfo != null && bundle.getBundleId() == mtinfo.getBundle().getBundleId()) {
 				final String[] pids = mtinfo.getPids();
 				for (final String pid : pids) {
 					confs.put(pid, new BundleConfig(ICON_SIZE, bundle, pid));
 				}
 			}
 			
 			// loopup metatypes by metatype-providers
 			ServiceReference[] serviceRefs = bundle.getRegisteredServices();
 			if (serviceRefs != null) {
 				for (ServiceReference ref : serviceRefs) {
 					// a managed-service always has a PID
 					String pid = (String) ref.getProperty(Constants.SERVICE_PID);
 					if (pid!= null) {			
 						HashSet<String> objectClassSet = new HashSet<String>(Arrays.asList((String[])ref.getProperty(Constants.OBJECTCLASS)));
 						if (objectClassSet.contains(MetaTypeProvider.class.getName()) && !confs.containsKey(pid)) {
 							confs.put(pid, new BundleConfig(ICON_SIZE, bundle, pid, manager.getService(ref, MetaTypeProvider.class)));
 						}
 					}
 				}
 			}
 		}
 		return confs;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void init() {
 		// initialize right view
 		final JPanel optPanel = new JPanel(new BorderLayout());
 		optViewPanel = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		optViewPanel.setViewportBorder(null);
 		optPanel.add(optViewPanel, BorderLayout.CENTER);
 		
 		final JButton save = mcl.getSaveButton();
 		save.setText(Messages.getString("settingsPanel.saveAll")); //$NON-NLS-1$
 		final JButton reset = mcl.getResetButton();
 		reset.setText(Messages.getString("settingsPanel.resetAll")); //$NON-NLS-1$
 		final JPanel bpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 		bpanel.add(save);
 		bpanel.add(reset);
 		optPanel.add(bpanel, BorderLayout.SOUTH);
 		
 		// initialize left view
 		list = new JList(confMap.values().toArray());
 		list.setCellRenderer(saveRunnable);
 		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		list.addListSelectionListener(saveRunnable);
 		list.setSelectedIndex(0);
 		
 		// initialize split view
 		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, list, optPanel);
 		split.setDividerLocation(.3);
 		split.setDividerSize(4);
 		
 		super.setLayout(new BorderLayout());
 		super.add(split);
 		mcl.resetDefaults();
 		final Hashtable<String,Object> props = new Hashtable<String,Object>();
 		super.registerService(this, this, props, ConfigurationListener.class);
 	}
 }
