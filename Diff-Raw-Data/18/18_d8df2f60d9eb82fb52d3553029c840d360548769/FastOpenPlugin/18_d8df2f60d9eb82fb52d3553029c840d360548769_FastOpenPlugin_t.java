 package com.patelsoft.fastopen;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.Window;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import javax.swing.AbstractButton;
 import javax.swing.ButtonGroup;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.border.EtchedBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.gjt.sp.jedit.AbstractOptionPane;
 import org.gjt.sp.jedit.EditPlugin;
 import org.gjt.sp.jedit.View;
 import org.gjt.sp.jedit.jEdit;
 import org.gjt.sp.jedit.gui.ColorWellButton;
 import org.gjt.sp.util.Log;
 
 
 public class FastOpenPlugin extends EditPlugin
 {
 	public static final String NAME = "fastopen";
	static Hashtable<Window, FastOpen>  viewsToFastOpen = new Hashtable<Window, FastOpen>(5);
 
 	public static final String OPEN_FILES_FIRST = "OPEN_FILES_FIRST";
 	public static final String OPEN_FILES_LAST ="OPEN_FILES_LAST";
 	public static final String OPEN_FILES_NOPREF =null;
 	static WindowAdapter wa;
 
 	@Override
 	public void start()
 	{
 		wa = new FastOpenWindowAdapter();
 	}
 
 	@Override
 	public void stop()
 	{
 		Enumeration<Window> iter = viewsToFastOpen.keys();
 		while(iter.hasMoreElements())
 		{
 			View v = (View)iter.nextElement();
 			v.removeWindowListener(wa);
 		}
 		viewsToFastOpen.clear();
 	}
 
 	/**
 	 * @param view The View we activated FastOpen from.
 	 * @return A lightweight JPanel wrapper around the FastOpen instance for this View.
 	 */
 	public static FastOpen getFastOpenInstance(View view)
 	{
 		FastOpen fopen = (FastOpen)viewsToFastOpen.get(view);
 		if (fopen == null)
 		{
 			fopen = new FastOpen(view);
 			viewsToFastOpen.put(view,fopen);
 			view.addWindowListener(wa);
 		}
 		return fopen;
 	}
 
 	@SuppressWarnings("serial")
 	public static class FastOpenOptionPane extends AbstractOptionPane
 	{
 		private static final String FASTOPEN_INDEXING_FREQ = "fastopen.indexing.freq";
 		private static final String FASTOPEN_NON_PRJ_OPEN_FILES_FOREGROUND_COLOR = "fastopen.nonprjOpenFiles.foregroundcolor";
 		private static final String FASTOPEN_OPEN_FILES_FOREGROUND_COLOR = "fastopen.openFiles.foregroundcolor";
 		private static final String FASTOPEN_PATH_FG_COLOR = "fastopen.path.foregroundcolor";
 		private static final String FASTOPEN_FILES_ORDER = "fastopen.filesOrder";
 		private static final String FASTOPEN_SHOW_RECENT_FILES = "fastopen.showrecentfiles";
 		private static final String FASTOPEN_PATTERN_FROM_SELECTED_TEXT = "fastopen.patternFromSelectedText";
 		private static final String FASTOPEN_IGNORE_CASE = "fastopen.ignorecase";
 		private static final String FASTOPEN_HIDE_OPEN_FILES = "fastopen.hideOpenFiles";
 		private static final String FASTOPEN_SORT_FILES = "fastopen.sortFiles";
 		static final String FASTOPEN_SHOW_ALTERNATE_ROWS = "fastopen.showalternaterows";
 		static final String FASTOPEN_ALLOW_CAMELCASE_SEARCH = "fastopen.allowcamelcase.search";
 		JCheckBox chkSort,chkDontShowOpenFiles,chkIgnoreCase,chkPattFromSelectedText, chkShowRecentFiles, chkShowAlternateRows, chkAllowCamelCase;
 		JRadioButton radioOpenFilesFirst,radioOpenFilesLast,radioNoPref;
 		ButtonGroup bg = new ButtonGroup();
 		ColorWellButton btnOpenFilesColor, btnNonPrjFilesColor, btnPathFGColor;
 		JSlider indexingFreq;
 		JSpinner txtdelay;
 
 		public FastOpenOptionPane()
 		{
 			super(NAME);
 		}
 
 		@Override
 		protected void _init()
 		{
 			chkSort = new JCheckBox("Sort Files");
 			chkShowAlternateRows = new JCheckBox("Show Alternate Rows");
 			chkDontShowOpenFiles = new JCheckBox("Don't show Open files");
 			chkIgnoreCase = new JCheckBox("Ignore Case in Search",true);
 			chkPattFromSelectedText = new JCheckBox("Pattern from Selected text",true);
 			chkShowRecentFiles = new JCheckBox("Search Recent Files",true);
 			chkAllowCamelCase = new JCheckBox("Allow Camel Case Search",true);
 
 			radioOpenFilesFirst = new JRadioButton("Show open files First");
 			radioOpenFilesLast = new JRadioButton("Show open files Last");
 			radioNoPref = new JRadioButton("No Preference",true);
 
 			txtdelay = new JSpinner(new SpinnerNumberModel(jEdit.getDoubleProperty("fastopen.search.delay",2), 0.5,10,0.5));
 
 			indexingFreq = new JSlider(5,300);//Seconds. 5 sec. to 5 min. anything above this would be useless.
 			indexingFreq.setPaintLabels(true);
 			indexingFreq.setPaintLabels(true);
 			Dictionary<Integer, JLabel> tables = new Hashtable<Integer, JLabel>();
 			tables.put(5,new JLabel("5 sec."));
 			tables.put(60,new JLabel("1 min."));
 			tables.put(120,new JLabel("2 min."));
 			tables.put(180,new JLabel("3 min."));
 			tables.put(240,new JLabel("4 min."));
 			tables.put(300,new JLabel("5 min."));
 
 			indexingFreq.setLabelTable(tables);
 
 			final JLabel freqVal = new JLabel(String.valueOf(indexingFreq.getValue()));
 			// Register a change listener
 			indexingFreq.addChangeListener(new ChangeListener(){
 				// This method is called whenever the slider's value is changed
 				public void stateChanged(ChangeEvent evt)
 				{
 					JSlider slider = (JSlider)evt.getSource();
 					if (!slider.getValueIsAdjusting())
 					{
 						// Get new value
 						freqVal.setText(slider.getValue()+" sec.");
 					}
 				}
 			});
 
 			radioOpenFilesFirst.setActionCommand(FastOpenPlugin.OPEN_FILES_FIRST);
 			radioOpenFilesLast.setActionCommand(FastOpenPlugin.OPEN_FILES_LAST);
 			radioNoPref.setActionCommand(FastOpenPlugin.OPEN_FILES_NOPREF);
 
 
 			bg.add(radioOpenFilesFirst);
 			bg.add(radioOpenFilesLast);
 			bg.add(radioNoPref);
 
 			JPanel panel = new JPanel(new GridLayout(5,1));
 			JPanel panelChk = new JPanel(new GridLayout(0,3));
 			JPanel panelColors = new JPanel(new FlowLayout(FlowLayout.LEFT));
 			final JPanel pnlRadios = new JPanel(new GridLayout(0,3));
 			pnlRadios.setBorder(new EtchedBorder());
 			JPanel pnlslider = new JPanel(new BorderLayout());
 
 			JPanel pnldelay = new JPanel();
 			((FlowLayout)pnldelay.getLayout()).setAlignment(FlowLayout.LEFT);
 			JLabel lbldelay = new JLabel("Delay before searching (in seconds)");
 			pnldelay.add(lbldelay);
 			pnldelay.add(txtdelay);
 
 
 			panelChk.add(chkDontShowOpenFiles);
 			panelChk.add(chkIgnoreCase);
 			panelChk.add(chkPattFromSelectedText);
 			panelChk.add(chkShowRecentFiles);
 			panelChk.add(chkSort);
 			panelChk.add(chkShowAlternateRows);
 			panelChk.add(chkAllowCamelCase);
 			panel.add(panelChk);
 
 			chkSort.addItemListener(new ItemListener()
 				{
 					public void itemStateChanged(ItemEvent e)
 					{
 						if(e.getStateChange() == ItemEvent.SELECTED)
 						{
 							enableDisableChildren(pnlRadios, true);
 						}
 						else
 						{
 							enableDisableChildren(pnlRadios, false);
 						}
 					}
 				}
 			);
 			pnlRadios.add(radioOpenFilesFirst);
 			pnlRadios.add(radioOpenFilesLast);
 			pnlRadios.add(radioNoPref);
 
 			panel.add(pnlRadios);
 
 			//Setup Color Component
 
 			btnOpenFilesColor = new ColorWellButton(FastOpen.openFilesForeground);
 			panelColors.add(new JLabel("Open files Foreground :"));
 			panelColors.add(btnOpenFilesColor);
 			btnNonPrjFilesColor = new ColorWellButton(FastOpen.nonprjopenFilesForeground);
 			panelColors.add(new JLabel("Non-Project files Foreground :"));
 			panelColors.add(btnNonPrjFilesColor);

 			btnPathFGColor = new ColorWellButton(FastOpen.pathFGColor);
 			panelColors.add(new JLabel("Path Foreground :"));
 			panelColors.add(btnPathFGColor);
 
 
 			pnlslider.add(new JLabel("Indexing frequency "), BorderLayout.WEST);
 			pnlslider.add(indexingFreq, BorderLayout.CENTER);
 			pnlslider.add(freqVal, BorderLayout.EAST);
 
 			panel.add(panelColors);
 			panel.add(pnlslider);
 			panel.add(pnldelay);
 			addComponent(panel);
 
 			// Set initial component values
 			chkSort.setSelected(jEdit.getBooleanProperty(FASTOPEN_SORT_FILES));
 			chkShowAlternateRows.setSelected(jEdit.getBooleanProperty(FASTOPEN_SHOW_ALTERNATE_ROWS, true));

 			enableDisableChildren(radioOpenFilesFirst.getParent(), jEdit.getBooleanProperty(FASTOPEN_SORT_FILES));
 			chkDontShowOpenFiles.setSelected(jEdit.getBooleanProperty(FASTOPEN_HIDE_OPEN_FILES));
 			chkIgnoreCase.setSelected(jEdit.getBooleanProperty(FASTOPEN_IGNORE_CASE, true));
 			chkPattFromSelectedText.setSelected(jEdit.getBooleanProperty(FASTOPEN_PATTERN_FROM_SELECTED_TEXT));
 			chkShowRecentFiles.setSelected(jEdit.getBooleanProperty(FASTOPEN_SHOW_RECENT_FILES, true));
 			chkAllowCamelCase.setSelected(jEdit.getBooleanProperty(FASTOPEN_ALLOW_CAMELCASE_SEARCH, true));

 			Enumeration<AbstractButton> enumElements =bg.getElements();
 			while(enumElements.hasMoreElements())
 			{
 				AbstractButton btn  = (AbstractButton)enumElements.nextElement();
 				if (btn.getActionCommand().equals(jEdit.getProperty(FASTOPEN_FILES_ORDER)))
 					btn.setSelected(true);
 			}
 			indexingFreq.setValue(jEdit.getIntegerProperty(FASTOPEN_INDEXING_FREQ,10));//10 sec. default.
 			txtdelay.setValue(new Double(jEdit.getDoubleProperty("fastopen.search.delay",2)));//1 sec. default.
 		}
 
 		@Override
 		protected void _save()
 		{
 			jEdit.setBooleanProperty(FASTOPEN_SORT_FILES,chkSort.isSelected());
 			jEdit.setBooleanProperty(FASTOPEN_SHOW_ALTERNATE_ROWS,chkShowAlternateRows.isSelected());
 			jEdit.setBooleanProperty(FASTOPEN_HIDE_OPEN_FILES,chkDontShowOpenFiles.isSelected());
 			jEdit.setBooleanProperty(FASTOPEN_IGNORE_CASE,chkIgnoreCase.isSelected());
 			jEdit.setBooleanProperty(FASTOPEN_PATTERN_FROM_SELECTED_TEXT,chkPattFromSelectedText.isSelected());
 			jEdit.setBooleanProperty(FASTOPEN_SHOW_RECENT_FILES,chkShowRecentFiles.isSelected());
 			jEdit.setProperty(FASTOPEN_FILES_ORDER,bg.getSelection().getActionCommand());
 			jEdit.setColorProperty(FASTOPEN_OPEN_FILES_FOREGROUND_COLOR,btnOpenFilesColor.getSelectedColor());
 			jEdit.setColorProperty(FASTOPEN_NON_PRJ_OPEN_FILES_FOREGROUND_COLOR,btnNonPrjFilesColor.getSelectedColor());
 			jEdit.setColorProperty(FASTOPEN_PATH_FG_COLOR,btnPathFGColor.getSelectedColor());
 			jEdit.setIntegerProperty(FASTOPEN_INDEXING_FREQ,indexingFreq.getValue());
 			jEdit.setBooleanProperty(FASTOPEN_ALLOW_CAMELCASE_SEARCH,chkAllowCamelCase.isSelected());
 
 			double delay = 2.0;
 			if(txtdelay.getValue() instanceof Double)
 				delay = ((Double)txtdelay.getValue()).doubleValue();
 			else if(txtdelay.getValue() instanceof Integer)
 				delay = ((Integer)txtdelay.getValue()).doubleValue();
 			jEdit.setDoubleProperty("fastopen.search.delay",delay);
 
 			FastOpen.openFilesForeground = btnOpenFilesColor.getSelectedColor();
 			FastOpen.nonprjopenFilesForeground = btnNonPrjFilesColor.getSelectedColor();
 			FastOpen.pathFGColor = btnPathFGColor.getSelectedColor();
 		}//End of save
 
 
 		// enable (or disable) all children of a component
 		void enableDisableChildren(Container container, boolean isEnabled)
 		{
 			// get an array of all the components in this container
 			Component[] components = container.getComponents();
 			// for each element in the container enable/disable it
 			for (int i = 0; i < components.length; i++)
 				components[i].setEnabled(isEnabled);
 		}
 	} //End of FastOpenOptionPane
 
 	static class FastOpenWindowAdapter extends WindowAdapter
 	{
 		//We add view listener to prevent FO to extend from EBPlugin
 		@Override
 		public void windowClosed(WindowEvent evt)
 		{
 			//Log.log(Log.DEBUG,this.getClass(),"Removing FO instance on View close");
 			FastOpen hashFopen =(FastOpen)viewsToFastOpen.remove(evt.getWindow());
 			if(hashFopen != null)
 			{
 				hashFopen.killWindow();
 				hashFopen = null;
 				System.gc();
 			}
 			else
 			{
 				Log.log(Log.ERROR,this.getClass(),"Got no view for View close?? How come??");
 			}
 		}
 	}
 }//End of class FastOpenPlugin
