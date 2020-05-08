 
 package org.paxle.desktop.impl.dialogues;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.InputEvent;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JViewport;
 import javax.swing.KeyStroke;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingUtilities;
 import javax.swing.JSpinner.DefaultEditor;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.osgi.framework.Bundle;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.cm.ConfigurationEvent;
 import org.osgi.service.cm.ConfigurationListener;
 import org.osgi.service.metatype.AttributeDefinition;
 import org.osgi.service.metatype.MetaTypeInformation;
 import org.osgi.service.metatype.MetaTypeService;
 import org.osgi.service.metatype.ObjectClassDefinition;
 
 import org.paxle.core.io.IOTools;
 import org.paxle.desktop.impl.Utilities;
 
 public class SettingsFrame extends JFrame implements ActionListener, ConfigurationListener {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private static class AttrConfig {
 		
 		private final AttributeDefinition ad;
 		private Object comp;
 		
 		public AttrConfig(final AttributeDefinition ad) {
 			this.ad = ad;
 		}
 		
 		public void addConfigLine(
 				final JPanel panel,
 				final Object currentValue,
 				final GridBagConstraints gbcLabel,
 				final GridBagConstraints gbcOptSingle,
 				final GridBagConstraints gbcOptMulti,
 				final GridBagConstraints gbcDesc) {
 			
 			final boolean isPassword = ad.getID().toLowerCase().contains("password");
 			
 			final String name = ad.getName();
 			panel.add(new JLabel(name), gbcLabel);
 			
 			String desc = ad.getDescription();
 			if (isPassword)
 				desc = desc + "\nBe aware that this password is being saved unencrypted.";
 			final JTextArea df = new JTextArea(desc);
 			Utilities.setTextLabelDefaults(df);
 			df.setLineWrap(true);
 			df.setWrapStyleWord(true);
 			panel.add(df, gbcDesc);
 			
 			final String[] labels = ad.getOptionLabels();
 			if (labels != null && labels.length > 0) {
 				final int[] selectedIndices;
 				final String[] values = ad.getOptionValues();
 				final HashMap<String,Integer> valMap = new HashMap<String,Integer>();
 				for (int i=0; i<values.length; i++)
 					valMap.put(values[i], Integer.valueOf(i));
 
 				Object value = (currentValue == null) ? ad.getDefaultValue() : currentValue;
 				
 				if (value == null) {
 					selectedIndices = new int[1];
 				} else if (value.getClass().isArray()) {
 					final Object[] curVals = (Object[])value;
 					// get selected indices
 					selectedIndices = new int[curVals.length];
 					int i = 0;
 					for (final Object val : curVals)
 						selectedIndices[i++] = valMap.get(val).intValue();
 				} else {
 					selectedIndices = new int[1];
 					selectedIndices[0] = valMap.get(value).intValue();
 				}
 				
 				final int cardinality = ad.getCardinality();
 				if (cardinality == 0) {
 					final JComboBox cbox = new JComboBox(labels);
 					cbox.setSelectedIndex(selectedIndices[0]);
 					panel.add(cbox, gbcOptSingle);
 					this.comp = cbox;
 				} else {
 					final JList list = new JList(labels);
 					list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 					list.setSelectedIndices(selectedIndices);
 					panel.add(list, gbcOptMulti);
 					this.comp = list;
 				}
 			} else {
 				Object value;
 				if (currentValue == null) {
 					final String[] defs = ad.getDefaultValue();
 					if (defs == null || defs.length == 0) {
 						value = null;
 					} else {
 						value = defs[0];
 					}
 				} else if (currentValue.getClass().isArray()) {
 					final Object[] cval = (Object[])currentValue;
 					value = (cval.length == 0) ? null : cval[0];
 				} else {
 					value = currentValue;
 				}
 				
 				double dmin = 0.0, dmax = 0.0;
 				long lmin = 0L, lmax = 0L;
 				switch (ad.getType()) {
 					case AttributeDefinition.BOOLEAN: {
 						if (value instanceof String)
 							value = Boolean.valueOf((String)value);
 						final boolean val = ((Boolean)value).booleanValue();
 						final JPanel op = new JPanel(new GridLayout(1, 3, 5, 5));
 						final JRadioButton rbTrue = new JRadioButton("True", val);
 						final JRadioButton rbFalse = new JRadioButton("False", !val);
 						final ButtonGroup bg = new ButtonGroup();
 						bg.add(rbTrue);
 						bg.add(rbFalse);
 						op.add(rbTrue);
 						op.add(rbFalse);
 						panel.add(op, gbcOptSingle);
 						this.comp = rbTrue;
 					} break;
 					
 					case AttributeDefinition.BYTE: {
 					} break;
 					case AttributeDefinition.CHARACTER: {
 					} break;
 					
 					case AttributeDefinition.SHORT:
 						lmin = Short.MIN_VALUE;
 						lmax = Short.MAX_VALUE;
 						// fall through
 					case AttributeDefinition.INTEGER:
 						if (lmin == 0L) lmin = Integer.MIN_VALUE;
 						if (lmax == 0L) lmax = Integer.MAX_VALUE;
 						// fall through
 					case AttributeDefinition.LONG: {
 						if (lmin == 0L) lmin = Long.MIN_VALUE;
 						if (lmax == 0L) lmax = Long.MAX_VALUE;
 						
 						if (value instanceof String)
 							value = Long.valueOf((String)value);
 						
 						final long val = ((Number)value).longValue();
 						final JSpinner spinner = new JSpinner();
 						if (ad.validate(Long.toString(val)) != null) {
 							final long[] minmax = getMinMax(lmin, lmax, val);
 							spinner.setModel(new SpinnerNumberModel(val, minmax[0], minmax[1], 1));
 						}
 						spinner.setValue(value);
 						panel.add(spinner, gbcOptSingle);
 						this.comp = spinner;
 					} break;
 					
 					case AttributeDefinition.FLOAT:
 						dmin = Float.MIN_VALUE;
 						dmax = Float.MAX_VALUE;
 						// fall-through
 					case AttributeDefinition.DOUBLE: {
 						if (dmin == 0.0) dmin = Double.MIN_VALUE;
 						if (dmax == 0.0) dmax = Double.MAX_VALUE;
 						
 						if (value instanceof String)
 							value = Double.valueOf((String)value);
 						
 						final double val = ((Number)value).doubleValue();
 						final JSpinner spinner = new JSpinner();
 						if (ad.validate(Double.toString(val)) != null) {
 							final double[] minmax = getMinMax(dmin, dmax, val);
 							spinner.setModel(new SpinnerNumberModel(val, minmax[0], minmax[1], 0.1));
 						}
 						
 						spinner.setValue(value);
 						panel.add(spinner, gbcOptSingle);
 						this.comp = spinner;
 					} break;
 					
 					case AttributeDefinition.STRING: {
 						final String val = (String)value;
 						final JTextField f = (isPassword) ? new JPasswordField() : new JTextField();
 						f.setText(val);
 						panel.add(f, gbcOptSingle);
 						this.comp = f;
 					} break;
 					default:
 						throw new RuntimeException("switch-statement doesn't cover attribute type " + ad.getType());
 				}
 			}
 		}
 		
 		private double[] getMinMax(final double min, final double max, final double mid) {
 			final boolean omitMin = (ad.validate(Double.toString(min)).length() == 0);
 			final boolean omitMax = (ad.validate(Double.toString(max)).length() == 0);
 			if (omitMin && omitMax)
 				return new double[] { min, max };
 			
 			double minlo = min;
 			double minhi = (omitMin) ? min : mid;
 			double maxlo = (omitMax) ? max : mid;
 			double maxhi = max;
 			
 			for (int i=0; i<MAX_DET_MINMAX_ITERATIONS; i++) {
 				if (!omitMin) {
 					final double nextMin = minhi - (minhi - minlo) / 2;
 					if (ad.validate(Double.toString(nextMin)).length() == 0) {
 						minhi = nextMin;
 					} else {
 						minlo = nextMin;
 					}
 				}
 				
 				if (!omitMax) {
 					final double nextMax = maxhi - (maxhi - maxlo) / 2;
 					if (ad.validate(Double.toString(nextMax)).length() == 0) {
 						maxlo = nextMax;
 					} else {
 						maxhi = nextMax;
 					}
 				}
 			}
 			
 			return new double[] { minhi, maxlo };
 		}
 		
 		private long[] getMinMax(final long min, final long max, final long mid) {
 			final boolean omitMin = (ad.validate(Long.toString(min)).length() == 0);
 			final boolean omitMax = (ad.validate(Long.toString(max)).length() == 0);
 			if (omitMin && omitMax)
 				return new long[] { min, max };
 			
 			long minlo = min;
 			long minhi = (omitMin) ? min : mid;
 			long maxlo = (omitMax) ? max : mid;
 			long maxhi = max;
 			
 			for (int i=0; i<MAX_DET_MINMAX_ITERATIONS; i++) {
 				if (!omitMin) {
 					final long nextMin = minhi - (minhi - minlo) / 2;
 					if (ad.validate(Long.toString(nextMin)).length() == 0) {
 						minhi = nextMin;
 					} else {
 						minlo = nextMin;
 					}
 				}
 				
 				if (!omitMax) {
 					final long nextMax = maxhi - (maxhi - maxlo) / 2;
 					if (ad.validate(Long.toString(nextMax)).length() == 0) {
 						maxlo = nextMax;
 					} else {
 						maxhi = nextMax;
 					}
 				}
 			}
 			
 			return new long[] { minhi, maxlo };
 		}
 		
 		public Object getValue() {
 			final String[] labels = ad.getOptionLabels();
 			if (labels != null && labels.length > 0) {
 				final int[] selectedIdces;
 				if (ad.getCardinality() == 0) {
 					final JComboBox cbox = (JComboBox)comp;
 					return ad.getOptionValues()[cbox.getSelectedIndex()];
 				} else {
 					final JList list = (JList)comp;
 					selectedIdces = list.getSelectedIndices();
 					
 					final String[] r = new String[selectedIdces.length];
 					final String[] vals = ad.getOptionValues();
 					for (int i=0; i<selectedIdces.length; i++)
 						r[i] = vals[selectedIdces[i]];
 					return r;
 				}
 			} else switch (ad.getType()) {
 				case AttributeDefinition.BOOLEAN: return Boolean.valueOf(((JRadioButton)comp).isSelected());
 				
 				case AttributeDefinition.BYTE: {
 				} break;
 				case AttributeDefinition.CHARACTER: {
 				} break;
 				
 				case AttributeDefinition.SHORT: return Short.valueOf(getSpinnerValue(true));
 				case AttributeDefinition.INTEGER: return Integer.valueOf(getSpinnerValue(true));
 				case AttributeDefinition.LONG: return Long.valueOf(getSpinnerValue(true));
 				
 				case AttributeDefinition.FLOAT: return Float.valueOf(getSpinnerValue(false));
 				case AttributeDefinition.DOUBLE: return Double.valueOf(getSpinnerValue(false));
 				
 				case AttributeDefinition.STRING: return ((JTextField)comp).getText();
 			}
 			return null;
 		}
 		
 		private String getSpinnerValue(final boolean dec) {
 			final JSpinner s = (JSpinner)comp;
 			try { s.commitEdit(); } catch (ParseException e) {
 				final JComponent editor = s.getEditor();
 				if (editor instanceof DefaultEditor)
 					((DefaultEditor)editor).getTextField().setText(s.getValue().toString());
 			}
 			final String val = s.getValue().toString();
 			final int idx = (dec) ? val.indexOf('.') : -1;
 			return (idx == -1) ? val : val.substring(0, idx);
 		}
 		
 		public void setValue(final Object value) {
 			// TODO set value
 			final String labels[] = ad.getOptionLabels();
 			if (labels != null && labels.length > 0) {
 			} else switch (ad.getType()) {
 			}
 		}
 		
 		public void save(final Hashtable<String,Object> props) {
 			props.put(ad.getID(), getValue());
 		}
 		
 		public void reset(final Hashtable<String,Object> props) {
 			setValue(ad.getDefaultValue());
 			save(props);
 		}
 	}
 	
 	private final class BundleConfig {
 		
 		private final Bundle bundle;
 		private final String pid;
 		private final int iconSize;
 		
 		private String locale = LOCALE;
 		
 		private ObjectClassDefinition ocd = null;
 		private JPanel panel = null;
 		private List<AttrConfig> aconfs = null;
 		private String name = null;
 		private ImageIcon icon = null;
 		
 		public BundleConfig(final int iconSize, final Bundle bundle, final String pid) {
 			this.iconSize = iconSize;
 			this.bundle = bundle;
 			this.pid = pid;
 		}
 		
 		public void save() throws IOException {
 			if (aconfs == null)
 				throw new RuntimeException("not initialized");
 			final Hashtable<String,Object> props = new Hashtable<String,Object>();
 			for (final AttrConfig aconf : aconfs)
 				aconf.save(props);
 			getConfiguration().update(props);
 		}
 		
 		public void reset() throws IOException {
 			if (!isInitialized())
 				initOptions();
 			final Hashtable<String,Object> props = new Hashtable<String,Object>();
 			for (final AttrConfig aconf : aconfs)
 				aconf.reset(props);
 			getConfiguration().update(props);
 		}
 		
 		public void deinit() {
 			ocd = null;
 			panel = null;
 			aconfs = null;
 			name = null;
 			icon = null;
 		}
 		
 		public String getName() {
 			if (name == null)
 				name = getOCD().getName();
 			return name;
 		}
 		
 		public boolean isChanged() {
 			return isInitialized();
 		}
 		
 		public boolean isInitialized() {
 			return aconfs != null;
 		}
 		
 		public ImageIcon getIcon() {
 			if (icon == null) try {
 				final InputStream is = getOCD().getIcon(iconSize);
 				if (is == null)
 					return null;
 				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
 				IOTools.copy(is, baos);
 				is.close();
 				icon = new ImageIcon(baos.toByteArray());
 			} catch (IOException e) { e.printStackTrace(); }
 			return icon;
 		}
 		
 		public JPanel getOptionsPanel() {
 			if (panel == null)
 				initOptions();
 			return panel;
 		}
 		
 		private ObjectClassDefinition getOCD() {
 			if (ocd == null) {
 				final MetaTypeInformation mtinfo = metatype.getMetaTypeInformation(bundle);
 				ocd = mtinfo.getObjectClassDefinition(pid, locale);
 			}
 			return ocd;
 		}
 		
 		private Configuration getConfiguration() {
 			try {
 				return cadmin.getConfiguration(pid, bundle.getLocation());
 			} catch (IOException e) { e.printStackTrace(); return null; }
 		}
 		
 		private GridBagConstraints createGbcLabel(final int y) {
 			final GridBagConstraints gbc = new GridBagConstraints();
 			gbc.gridx = 0; gbc.gridy = y;
 			gbc.anchor = GridBagConstraints.EAST;
 			gbc.insets = STD_INSETS;
 			return gbc;
 		}
 		
 		private GridBagConstraints createGbcOptSingle(final int y) {
 			final GridBagConstraints gbc = new GridBagConstraints();
 			gbc.gridx = 1; gbc.gridy = y;
 			gbc.anchor = GridBagConstraints.WEST;
 			gbc.weightx = .4;
 			gbc.insets = STD_INSETS;
 			gbc.fill = GridBagConstraints.HORIZONTAL;
 			return gbc;
 		}
 		
 		private GridBagConstraints createGbcOptMulti(final int y) {
 			return createGbcOptSingle(y);
 		}
 		
 		private GridBagConstraints createGbcDesc(final int y) {
 			final GridBagConstraints gbc = new GridBagConstraints();
 			gbc.gridx = 2; gbc.gridy = y;
 			gbc.anchor = GridBagConstraints.WEST;
 			gbc.weightx = .6;
 			gbc.fill = GridBagConstraints.BOTH;
 			gbc.insets = STD_INSETS;
 			return gbc;
 		}
 		
 		private GridBagConstraints createGbcHead() {
 			final GridBagConstraints gbc = new GridBagConstraints();
 			gbc.gridx = 0; gbc.gridy = 0;
 			gbc.gridwidth = 3;
 			gbc.anchor = GridBagConstraints.CENTER;
 			gbc.insets = STD_INSETS;
 			return gbc;
 		}
 		
 		private GridBagConstraints createGbcBottom(final int y) {
 			final GridBagConstraints gbc = new GridBagConstraints();
 			gbc.gridx = 0; gbc.gridy = y;
 			gbc.fill = GridBagConstraints.BOTH;
 			gbc.weightx = 1.0;
 			gbc.weighty = 1.0;
 			gbc.gridwidth = 3;
 			return gbc;
 		}
 		
 		private GridBagConstraints createGbcHeader(final int y) {
 			final GridBagConstraints gbc = createGbcDesc(y);
 			gbc.gridx = 1;
 			gbc.gridwidth = 2;
 			return gbc;
 		}
 		
 		private void initOptions() {
 			aconfs = new LinkedList<AttrConfig>();
 			final ObjectClassDefinition ocd = getOCD();
 			
 			panel = new JPanel(new GridBagLayout());
 			
 			final GridBagConstraints gbcHead = createGbcHead();
 			final JLabel heading = new JLabel(ocd.getName());
 			final Font hf = heading.getFont();
 			heading.setFont(new Font(hf.getName(), Font.BOLD, 16));
 			panel.add(heading, gbcHead);
 			incGridY(gbcHead);
 			
 			final int y = gbcHead.gridy;
 			final GridBagConstraints gbcHeader = createGbcHeader(y);
 			final GridBagConstraints gbcLabel = createGbcLabel(y);
 			final GridBagConstraints gbcOptSingle = createGbcOptSingle(y);
 			final GridBagConstraints gbcOptMulti = createGbcOptMulti(y);
 			final GridBagConstraints gbcDesc = createGbcDesc(y);
 			
 			panel.add(new JLabel("Description"), gbcLabel);
 			panel.add(new JLabel(ocd.getDescription()), gbcHeader);
 			incGridY(gbcHeader, gbcLabel, gbcOptSingle, gbcOptMulti, gbcDesc);
 			panel.add(new JLabel("Bundle"), gbcLabel);
 			panel.add(new JLabel(bundle.getSymbolicName()), gbcHeader);
 			incGridY(gbcHeader, gbcLabel, gbcOptSingle, gbcOptMulti, gbcDesc);
 			
 			final Dictionary<?,?> properties = getConfiguration().getProperties();
 			for (final AttributeDefinition ad : ocd.getAttributeDefinitions(ObjectClassDefinition.ALL)) {
 				final AttrConfig aconf = new AttrConfig(ad);
 				aconf.addConfigLine(panel, (properties == null) ? null : properties.get(ad.getID()), gbcLabel, gbcOptSingle, gbcOptMulti, gbcDesc);
 				aconfs.add(aconf);
 				incGridY(gbcLabel, gbcOptSingle, gbcOptMulti, gbcDesc);
 			}
 			
 			panel.add(new JPanel(), createGbcBottom(gbcLabel.gridy));		// clear bottom line
 		}
 		
 		private void incGridY(final GridBagConstraints... constraints) {
 			for (final GridBagConstraints gbc : constraints)
 				gbc.gridy++;
 		}
 	}
 	
 	private static final int MAX_DET_MINMAX_ITERATIONS = 63;			// max. iterations needed for type long
 	private static final Insets STD_INSETS = new Insets(5, 5, 5, 5);
 	private static final String LOCALE = "en";
 	private static final int ICON_SIZE = 16;
 	
 	private static final String KE_CLOSE = new String();
 	private final Action AC_CLOSE = new AbstractAction() {
 		private static final long serialVersionUID = 1L;
 		public void actionPerformed(ActionEvent e) {
 			SettingsFrame.this.dispose();
 		}
 	};
 	
 	private static enum Actions {
 		SAVE, RESET, REFRESH, LIST_SELECT
 	}
 	
 	private final ConfigurationAdmin cadmin;
 	private final MetaTypeService metatype;
 	private final Map<String,BundleConfig> confMap;
 	private final EnumMap<Actions,ActionRunnable> acMap = new EnumMap<Actions,ActionRunnable>(Actions.class);
 	
 	private JList list;
 	private JScrollPane optViewPanel;
 	
 	public SettingsFrame(final Bundle[] bundles, final ConfigurationAdmin cadmin, final MetaTypeService metatype) {
 		this.cadmin = cadmin;
 		this.metatype = metatype;
 		this.confMap = Collections.synchronizedMap(initConfMap(bundles));
 		init();
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		final Actions ac = Actions.valueOf(e.getActionCommand());
 		ActionRunnable ar = acMap.get(ac);
 		if (ar == null)
 			acMap.put(ac, ar = new ActionRunnable(ac));
 		SwingUtilities.invokeLater(ar);
 	}
 	
 	private final class ActionRunnable implements Runnable {
 		
 		private final Actions cmd;
 		private final BundleConfig bc;
 		
 		public ActionRunnable(final Actions cmd) {
 			this(cmd, null);
 		}
 		
 		public ActionRunnable(final Actions cmd, final BundleConfig bc) {
 			this.cmd = cmd;
 			this.bc = bc;
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
 					for (final BundleConfig bc : confMap.values())
 						if (bc.isChanged())
 							bc.save();
 					list.setSelectedValue(sel, true);
 				} catch (IOException e) { e.printStackTrace(); }
 				break;
 				
 				case RESET: try {
 					final BundleConfig sel = (BundleConfig)list.getSelectedValue();
 					for (final BundleConfig bc : confMap.values())
 						bc.reset();
 					list.setSelectedValue(sel, true);
 				} catch (IOException e) { e.printStackTrace(); }
 				break;
 				
 				default:
 					throw new RuntimeException("switch-statement does not cover action '" + cmd + "'");
 			}
 		}
 	}
 	
 	public void configurationEvent(ConfigurationEvent event) {
		if ((event.getType() & ConfigurationEvent.CM_UPDATED) == ConfigurationEvent.CM_UPDATED)
			SwingUtilities.invokeLater(new ActionRunnable(Actions.REFRESH, confMap.get(event.getPid())));
 	}
 	
 	private Map<String,BundleConfig> initConfMap(final Bundle[] bundles) {
 		final Map<String,BundleConfig> confs = new TreeMap<String,BundleConfig>();
 		for (final Bundle bundle : bundles) {
 			final MetaTypeInformation mtinfo = metatype.getMetaTypeInformation(bundle);
 			if (mtinfo == null)
 				continue;
 			if (bundle.getBundleId() != mtinfo.getBundle().getBundleId())
 				continue;
 			final String[] pids = mtinfo.getPids();
 			if (pids == null || pids.length == 0)
 				continue;
 			for (final String pid : pids)
 				confs.put(pid, new BundleConfig(ICON_SIZE, bundle, pid));
 		}
 		return confs;
 	}
 	
 	private static class OptListCellRenderer extends DefaultListCellRenderer implements ListCellRenderer {
 		
 		private static final long serialVersionUID = 1L;
 		
 		@Override
 		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 			super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
 			
 			final BundleConfig cfg = (BundleConfig)value;
 			super.setIcon(cfg.getIcon());
 			super.setText(cfg.getName());
 			
 			return this;
 		}
 	}
 	
 	private void init() {
 		// initialize right view
 		final JPanel optPanel = new JPanel(new BorderLayout());
 		optViewPanel = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		optViewPanel.addComponentListener(new ComponentListener() {
 			public void componentHidden(ComponentEvent e) {  }
 			public void componentMoved(ComponentEvent e) {  }
 			public void componentResized(ComponentEvent e) {
 				final JViewport vp = optViewPanel.getViewport();
 				final Dimension vdim = vp.getExtentSize();
 				final Component c = vp.getView();
 				final Dimension cdim = c.getSize();
 				// FIXME: not the current height of the component should be passed, but the new height computed relative to vdim.width
 				c.setPreferredSize(new Dimension(vdim.width, cdim.height));
 			}
 			public void componentShown(ComponentEvent e) {  }
 		});
 		optViewPanel.setViewportBorder(null);
 		optPanel.add(optViewPanel, BorderLayout.CENTER);
 		
 		final JButton save = new JButton("Save all changes");
 		save.setActionCommand(Actions.SAVE.name());
 		save.addActionListener(SettingsFrame.this);
 		final JButton reset = new JButton("Reset all settings");
 		reset.setActionCommand(Actions.RESET.name());
 		reset.addActionListener(SettingsFrame.this);
 		reset.setEnabled(false);
 		final JPanel bpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 		bpanel.add(save);
 		bpanel.add(reset);
 		optPanel.add(bpanel, BorderLayout.SOUTH);
 		
 		// initialize left view
 		list = new JList(confMap.values().toArray());
 		list.setCellRenderer(new OptListCellRenderer());
 		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		list.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(final ListSelectionEvent e) {
 				if (!e.getValueIsAdjusting())
 					SwingUtilities.invokeLater(new ActionRunnable(Actions.LIST_SELECT, (BundleConfig)list.getSelectedValue()));
 			}
 		});
 		list.setSelectedIndex(0);
 		
 		// initialize split view
 		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, list, optPanel);
 		split.setDividerLocation(.3);
 		split.setDividerSize(4);
 		
 		super.rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke('W', InputEvent.CTRL_DOWN_MASK), KE_CLOSE);
 		super.rootPane.getActionMap().put(KE_CLOSE, AC_CLOSE);
 		super.setTitle("Paxle Settings");
 		super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		super.setContentPane(split);
 		super.setResizable(true);
 		super.setLocationByPlatform(true);
 		super.pack();
 		super.setSize(1000, 600);
 	}
 }
