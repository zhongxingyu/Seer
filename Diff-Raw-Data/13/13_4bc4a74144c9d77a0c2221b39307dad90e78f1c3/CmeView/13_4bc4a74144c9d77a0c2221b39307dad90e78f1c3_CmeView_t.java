 package pkgCMEF;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.event.AncestorEvent;
 import javax.swing.event.AncestorListener;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.io.IOException;
 import java.util.Iterator;
 
 //====================================================================
 /**
  * CmeInstructions
  * <P>
  * Purpose: This panel displays a JTextArea object for display instructions.
  * 
  * @author Terry Meacham
  * @version 2.0 Date: May, 2011
  */
 // ===================================================================
 @SuppressWarnings("serial")
 public class CmeView extends JPanel {
 
 	/** Parent frame */
 	private CmeApp m_App;
 	/** Current state */
 	private CmeState m_CurState;
 	/** Html Pane for Instructions */
 	private CmeHtmlView m_HtmlView;
 	/** Scroll Pane for Instruction Scrolling */
 	private JScrollPane m_ScrollPane;
 	/** JButton for primary button */
 	private JButton m_bNext;
 	/** JButton for primary button */
 	private JButton m_bRefresh;
 	/** Reference to the image factory */
 	private CmePairFactory m_PairFactory;
 	/** Submit Action Response */
 	private ActionListener m_SubmitListener;
 	
 	private CmeClock m_cClock;
 	
 	private CmeEventResponse m_LimitResponse;
 	
 	
 	private String m_bString;
 	private String m_sContent;
 	private String m_sStudyContent;
 	private boolean m_bInStudyState;
 	private int m_iStudyCount;
 	
 	/**
 	 * Default constructor
 	 * 
 	 * @throws IOException
 	 */
 	public CmeView(CmeApp parent, int ops) throws Exception {
 		m_App = parent;
 		
 		final CmeView cInst = this;
 		
 		clearStudyState();
 
 		this.setVisible(false);
 		this.setSize(parent.getSize());
 		this.setBorder(null);
 
 		this.setLayout(null);
 
 		AncestorListener aListener = new AncestorListener() {
 
 			@Override
 			public void ancestorRemoved(AncestorEvent arg0) {
 			}
 
 			@Override
 			public void ancestorAdded(AncestorEvent arg0) {
 			}
 
 			@Override
 			public void ancestorMoved(AncestorEvent e) {
 				adjustLayout();
 			}
 		};
 		
 		HyperlinkListener hListener = new HyperlinkListener() {		
 			CmeView inst = cInst;
 			public void hyperlinkUpdate(HyperlinkEvent e) {
 				String[] desc = e.getDescription().toLowerCase().split(":");
 				if (desc.length != 2)
 					return;
 				if (desc[0].equals("study")) {
 					try {
 						inst.setStudyState(desc[1]);
 					} catch (Exception ex) {
 						//System.out.println("Failed to set study state!!\n" + desc[1]);
 					}
 				}
 			}
 		};
 
 		boolean textOnly = ((ops & CmeApp.CME_TEXT_ONLY) == CmeApp.CME_TEXT_ONLY);
 		
 		/** Configure HTML View Pane */
 		m_HtmlView = new CmeHtmlView(m_App, hListener, textOnly);
 		/*
 		 * m_HtmlView.setBorder(BorderFactory
 		 * .createBevelBorder(BevelBorder.LOWERED));
 		 */
 		
 		m_SubmitListener = new ActionListener() {
 			CmeView inst = cInst;
 			public void actionPerformed(ActionEvent e) {
 				inst.primaryClickEvent();
 			}
 		};
 		
 		ActionListener refreshListener = new ActionListener() {
 			CmeView inst = cInst;
 			public void actionPerformed(ActionEvent e) {
 				try {
 					inst.refreshView();
 				} catch (Exception ex) {
 					//System.out.println("Failed to catch refresh exception!");
 				}
 			}
 		};
 		
 		/** Configure Next JButton */
 		m_bNext = new JButton();
 		m_bNext.setText("Next");
 		m_bNext.setVisible(true);
 		m_bNext.addActionListener(m_SubmitListener);
 		this.add(m_bNext);
 		
 		if ((ops & CmeApp.CME_ENABLE_REFRESH) == CmeApp.CME_ENABLE_REFRESH) {
 			/** Configure Next JButton */
 			m_bRefresh = new JButton();
 			m_bRefresh.setText("Refresh");
 			m_bRefresh.setVisible(true);
 			m_bRefresh.addActionListener(refreshListener);
 			this.add(m_bRefresh);
 		}
 		
 		m_LimitResponse = new CmeEventResponse() {
 			CmeView inst = cInst;
 			public void Respond() {
 				inst.clockLimitHit();
 			}
 		};
 		
 		m_cClock = new CmeClock();
 		m_cClock.setEventResponse(m_LimitResponse);
 		m_cClock.setVisible(false);
 		this.add(m_cClock);
 
 		m_ScrollPane = new JScrollPane(m_HtmlView);
 		m_ScrollPane.setBackground(Color.white);
 		m_ScrollPane.addAncestorListener(aListener);
 		this.add(m_ScrollPane);
 
 	}
 
 	private void primaryClickEvent() {
 		if (m_bInStudyState) {
 			try {
 				clearStudyState();
 			} catch (Exception ex) {
 				//System.out.println("Failed to clear study state!");
 				System.exit(1);
 			}
 		} else if (m_CurState != null) {
 			m_CurState.TriggerEvent(CmeState.EVENT_CLICK_PRIMARY);
 		}
 	}
 
 	private void clockLimitHit() {
 		try {
 			clearStudyState();
 			m_cClock.stop();
 		} catch (Exception ex) {
 			//System.out.println(ex.getMessage() + ": Failed to propertly clear Study State.");
 			System.exit(1);
 		}
 	}
 
 	public void updateHtmlContent() throws Exception {
 		String content = m_CurState.translateString(m_sContent);
 		content = m_App.translateString(content);
 		m_HtmlView.setContent(content, null);
 	}
 
 	public void updateLinkColor(String element) {
 		
 		String postStudyColor = (String)m_CurState.getProperty("PostStudyColor");
 		
 		if (postStudyColor != null) {
 			//System.out.println("Pair" + element + "Color");
 			m_CurState.setProperty("Pair" + element + "Color", postStudyColor);
 		}
 	}
 
 	public boolean clearStudyState() throws Exception
 	{
 		boolean ret = false;
 		
 		if (m_bInStudyState) {
 			String pairNum = m_cClock.getElement();
 			int elapsed = 0;
 			
 			if (isContinuousTimer() && !m_cClock.isComplete())
 				elapsed = m_cClock.getElapsedTime();
 			else
 				elapsed = m_cClock.stop();
 			
 			double elapsedTime = ((double)elapsed)/1000.0;
 
 			/* Setup: trial variable. */
 			String trial = (String) m_CurState.getProperty("CurrentTrial");
 			if (trial == null)
 				trial = "";
 			else
 				trial = "_T" + trial;
 			
 			/* Setup: pair being studied. */
 			String pair = (String) m_CurState.getProperty("Pair" + pairNum);
 			
 			if (pair == null) 
 				throw new Exception("Failed to get the current pair!");
 			
 			updateLinkColor(pairNum);
 			updateHtmlContent();
 			updatePairSelection(trial, pair, elapsedTime);
 			updatePairTotals(trial, pair, elapsedTime);
 			updatePairCount(trial, pair);
 			
 			m_iStudyCount++;
 			ret = true;
 		}
 		
 		m_bInStudyState = false;
 		updateButtonText();
 		
 		return ret;
 	}
 	
 	private void updatePairSelection(String trial, String pair, double elapsedTime) throws Exception
 	{
 		String name = "Study" + trial + "_" + pair;
 		String value = Double.toString(elapsedTime);
 		String fbval = m_App.getFeedback(name);
 		
 		if (fbval == null)
 			fbval = "";
 		else
 			fbval += ",";
 		
 		fbval += Integer.toString(m_iStudyCount) + "," + value;
 		m_App.addFeedback(name, fbval);
 	}
 	
 	private void updatePairCount(String trial, String pair) throws Exception
 	{
 		String name = "StudyTotalCount" + trial + "_" + pair;
 		String value = m_App.getFeedback(name);
 
 		if (value == null) {
 			value ="1";
 		} else try {
 			int ival = Integer.valueOf(value);
 			ival++;
 			value = String.valueOf(ival);
 		} catch (Exception ex) {
 			System.err.println("Failed to properly set value!");
 			throw new Exception("Failed to properly set total count value!");
 		}
 
 		//System.out.println("Count: " + name + ":" + value);	
 		m_App.addFeedback(name, value);
 	}
 	
 	private void updatePairTotals(String trial, String pair, double elapsedTime) throws Exception {
 		String name = "StudyTotalTime" + trial + "_" + pair;
 		String value = m_App.getFeedback(name);
 	
 		if (value == null) {
 			value = Double.toString(elapsedTime);;
 		} else try {
 			double dval = Double.valueOf(value);
 			dval += elapsedTime;
 			value = String.valueOf(dval);
 		} catch (Exception ex) {
 			System.err.println("Failed to properly set value!");
 			throw new Exception("Failed to properly set total time value!");
 		}
 	
 		//System.out.println("Timer: " + name + ":" + value);
 		m_App.addFeedback(name, value);
 	}
 	
 	public void setStudyState(String set) throws Exception
 	{
 		if (!m_CurState.canStudy()) {
 			throw new  Exception("Not a STUDY MODE!");
 			//return;
 		}
 		
 		m_CurState.setProperty("CurrentPairA", m_CurState.getProperty("Pair" + set + "A"));
 		m_CurState.setProperty("CurrentPairB", m_CurState.getProperty("Pair" + set + "B"));
 		m_CurState.setProperty("CurrentPair", m_CurState.getProperty("Pair" + set));
 		
 		m_CurState.setProperty("CurrentPairAFile", m_CurState.getProperty("Pair" + set + "AFile"));
 		m_CurState.setProperty("CurrentPairBFile", m_CurState.getProperty("Pair" + set + "BFile"));
 		
 		m_CurState.setProperty("CurrentGroup", m_CurState.getProperty("Pair" + set + "Group"));	
 		m_CurState.setProperty("CurrentValue", m_CurState.getProperty("Pair" + set + "Value"));	
 		m_CurState.setProperty("CurrentOrder", m_CurState.getProperty("Pair" + set + "Order"));
 		
 		String translation = m_CurState.translateString(m_sStudyContent);
 		translation = m_App.translateString(translation);
 		
 		if (!m_cClock.start(set)) {
 			JOptionPane.showMessageDialog(this, "Please click '" + m_bString + 
 					"', your time has expired.", "Click " + m_bString + 
 					" to Proceed", JOptionPane.INFORMATION_MESSAGE);
 			return;
 		}
 
 		m_bNext.setText("Close");
 		
 		m_HtmlView.setContent(translation, null);
 		m_bInStudyState = true;
 	}
 	
 	/** Return the component string for the 'PageObjective' component! */
 	public String getObjective() {
 		CmeComponent comp = m_HtmlView.getComponentByName("PageObjective");
 		if (comp == null)
 			return null;
 		return comp.getValue();
 	}
 	
 	/** Set a reference to the image factory */
 	public void setPairFactory(CmePairFactory iFact) {
 		m_PairFactory = iFact;
 	}
 
 	/**
 	 * Adjust all component present for the current window size.
 	 * */
 	public void adjustLayout() {
 		adjustHtmlView();
 		adjustNextButton();
 		if (m_bRefresh != null)
 			adjustRefreshButton();
 		adjustClock();
 	}
 	
 	public void refreshView() throws Exception {
 		m_HtmlView.refreshView();
 	}
 
 	/**
 	 * Adjust the Next Button for the current window size.
 	 */
 	private void adjustNextButton() {
 		Dimension newSz = this.getSize();
 
 		Dimension border = new Dimension(10, 10);
 		Dimension dimensions = new Dimension(128, 24);
 		Point location = new Point(newSz.width, newSz.height);
 
 		location.x -= border.width + dimensions.width;
 		location.y -= border.height + dimensions.height;
 
 		m_bNext.setSize(dimensions);
 		m_bNext.setLocation(location);
 	}
 	
 	/**
 	 * Adjust the Refresh Button for the current window size.
 	 */
 	private void adjustRefreshButton() {
 		Dimension newSz = this.getSize();
 
 		Dimension border = new Dimension(10, 10);
 		Dimension dimensions = new Dimension(128, 24);
 		Point location = new Point(0, newSz.height);
 
 		location.x += border.width;
 		location.y -= border.height + dimensions.height;
 
 		m_bRefresh.setSize(dimensions);
 		m_bRefresh.setLocation(location);
 	}
 
 	/**
 	 * Adjust the HTML View for the current window size.
 	 */
 	private void adjustHtmlView() {
 		Dimension newSz = this.getSize();
 
 		Dimension border = new Dimension(10, 10);
 		Dimension dimensions = (Dimension) newSz.clone();
 		Point location = new Point(border.width, border.height);
 
 		int lower_offset = border.height + 24;
 
 		if (m_bNext.isVisible()) {
 			dimensions.height = newSz.height - (border.height * 2 + lower_offset);
 		} else {
 			dimensions.height = newSz.height - (border.height * 2);
 		}
 		dimensions.width -= border.width * 2;
 
 		m_HtmlView.setSize(dimensions);
 		m_ScrollPane.setSize(dimensions);
 		m_ScrollPane.setLocation(location);
 	}
 	
 	/**
 	 * Adjust the Next Button for the current window size.
 	 */
 	private void adjustClock() {
 		Dimension newSz = this.getSize();
 
 		Dimension border = new Dimension(10, 10);
 		Dimension dimensions = new Dimension(64, 35);
 		Point location = new Point(newSz.width/2-dimensions.width/2+border.width, newSz.height-1);
 
 		location.x -= border.width;
 		location.y -= border.height + dimensions.height;
 
 		m_cClock.setSize(dimensions);
 		m_cClock.setLocation(location);
 	}
 	
 	public boolean allowNextState() {
 		//System.out.println("Clock check...");
 		
 		if (m_cClock != null && !m_cClock.isComplete()) {
 			//System.out.println("Display Limit Prompt");
 			return (m_App.displayPrompt("LimitPromptText", "LimitPromptButtons"));
 		}
 		
 		return true;
 	}
 
 	/** 
 	 * 	Says that the clock timer should be reset after each Step in a 
 	 *  multi-step instruction.
 	 *  
 	 * @return true if the current state has the property.
 	 */
 	private boolean isPerStepTimer() {
 		String pst = m_CurState.getStringProperty("ResetOnNext");
 		
 		if (pst == null)
 			return false;
 		
 		if (pst.equals("yes") || pst.equals("true"))
 			return true;
 		
 		return false;
 	}
 
 	/** 
 	 * 	Says that the clock timer should be continuous regardless of
 	 *  study state.
 	 *  
 	 * @return true if the current state has the property.
 	 */
 	private boolean isContinuousTimer() {
 		String pst = m_CurState.getStringProperty("ContinuousTimer");
 		
 		if (pst == null)
 			return false;
 		
 		if (pst.equals("yes") || pst.equals("true"))
 			return true;
 		
 		/* Make sure that we consider the clock continuous
 		 * if ONLY ContinuousAfterFirstSelection is set.
 		 */
 		return isContinuousAfterFirstSelection();
 	}
 
 	/** 
 	 *  Says that the clock is continuous after the first study attempt.
 	 *  
 	 * @return true if the current state has the property.
 	 */
 	private boolean isContinuousAfterFirstSelection() {
 		String pst = m_CurState.getStringProperty("ContinuousAfterFirstSelection");
 		
 		if (pst == null)
 			return false;
 		
 		if (pst.equals("yes") || pst.equals("true"))
 			return true;
 		
 		return false;
 	}
 	
 	private void updateInstructionFile(boolean reconfigure) throws Exception {
 		// TODO: move stateful information into the STATE (is too much in this class?)
 		String instructionFile = (String) m_CurState.getSequenceFile();
 		
 		if (reconfigure) {
 			int icount = m_CurState.getIntProperty("Count");
 			int isets = m_CurState.getIntProperty("Sets");
 			
 			if (isets < 1)
 				isets = 1;
 			if (icount < 1)
 				icount = 1;
 			
 			m_CurState.setStep(1);
 			m_CurState.setStepMax(isets);
 			m_CurState.setPerStepCount(icount);
 			
 			setProperties();
 		}
 
 		updateClock();
 		updateButtonText();
 		
 		if (instructionFile != null) {
 			try {
 				this.showProcessedInstructions((String) instructionFile);
 			} catch (IOException ex) {
 				ex.printStackTrace();
 				throw new Exception("IO Error: " + ex.getMessage() + "\nFile: " + instructionFile);
 			}
 		} else {
 			throw new Exception("Failed to set instruction file! (InstructionFile == null)");
 		}
 		
 		m_HtmlView.requestFirstFocus();
 		
 		/* The clock is continuous and is NOT only after first Study */
 		if (isContinuousTimer() && !isContinuousAfterFirstSelection())
 			m_cClock.start("");
 	}
 	
 	
 	/**
 	 * Used to set the next rating environment.
 	 * 
 	 * @return true if the number of iterations has been met; false else.
 	 */
 	public boolean setNextInSequence() throws Exception {
 
 		if (!allowNextState()) {
 			return true;
 		}
  
 		int cstep = m_CurState.getStep();
 		if (cstep > 0) {
 			if (!isProvidedFeedbackValid()) {
 				return false;
 			}
 		
 			m_HtmlView.setVisible(false);
 			m_App.displayPrompt("PostPromptText");
 			
 			if (m_CurState.isDone()) {
 				m_HtmlView.clearContent();
 				if (m_cClock != null)
 					m_cClock.complete();
 				return false;
 			}
 		}
 		
		// If there is another instruction file to show, then show it!
		if (m_CurState.nextSequencePosition()) {
			updateInstructionFile(false);
			return true;
		}
		
 		m_iStudyCount = 1;
 
 		m_CurState.setStep(cstep + 1);
 		CmeIterator iterator = m_CurState.getIterator();
 
 		if (iterator == null) {
 			throw new Exception("Failed to retreive iterator from current state (setNextSequential)!");
 		}
 		
 		m_App.displayPrompt("PrePromptText");
 		
 		setProperties();
 		
 		if (isPerStepTimer()) {
 			m_cClock.reset();
 			this.updateUI();
 		}
 
 		m_CurState.resetSeqState();
 
 		updateInstructionFile(false);
 		return true;
 	}
 	
 	private double getScale() {
 		double scale = 1.0;
 		
 		String sscale = (String) m_CurState.getProperty("Scale");
 		if (sscale != null) {
 			try {	
 				scale = Double.parseDouble(sscale);
 			} catch (Exception ex) {
 				//System.out.println("Ooops, no scale defined! Using 1.0.");
 				scale = 1.0;
 				ex.printStackTrace();
 			}
 		}
 		
 		return scale;
 	}
 
 	/**
 	 * Set the display properties for a study state.
 	 * 
 	 * @throws Exception 
 	 */
 	private void setProperties() throws Exception {
 		if (m_CurState == null)
 			return;
 		
 		double scale = getScale();
 		CmeIterator iter = m_CurState.getIterator();		
 		int count = m_CurState.getPerStepCount();
 		final int currentStep = m_CurState.getStep();
 		String preStudyColor = m_CurState.getStringProperty("PreStudyColor");
 		
 		int step;
 		String vx;
 
 		m_CurState.setProperty("CurrentSet", Integer.toString(currentStep));
 
 		for (int x = 0; x < count; x++) {
 			step = iter.getNext();
 			vx = Integer.toString(x + 1);
 			//System.out.println("Generating Pairs for " + vx);
 
 			m_CurState.setProperty("Pair" + vx + "A", m_PairFactory.getFeedbackA(step, (int) (scale * 1000)));
 			m_CurState.setProperty("Pair" + vx + "B", m_PairFactory.getFeedbackB(step, (int) (scale * 1000)));
 			m_CurState.setProperty("Pair" + vx + "AFile", m_PairFactory.getFileA(step));
 			m_CurState.setProperty("Pair" + vx + "BFile", m_PairFactory.getFileB(step));
 			m_CurState.setProperty("Pair" + vx, Integer.toString(step));
 			
 			m_CurState.setProperty("Pair" + vx + "Sequence", Integer.toString(m_CurState.getStep()));
 			m_CurState.setProperty("Pair" + vx + "Group", m_PairFactory.getPairGroup(step));	
 			m_CurState.setProperty("Pair" + vx + "Order", Integer.toString(x));	
 			m_CurState.setProperty("Pair" + vx + "Value", m_PairFactory.getPairValue(step));
 			
 			if (m_CurState.canStudy() && preStudyColor != null) {
 				m_CurState.setProperty("Pair" + vx + "Color", preStudyColor);
 			}
 		}
 		
 		setSetPoolProperties();
 		setStatePoolProperties();
 		
 		//System.out.println("Done creating random pools.");
 	}
 	
 	private boolean setSetPoolProperties() {
 		final int count = m_CurState.getPerStepCount();
 		return m_App.handleCompoundProperty(m_CurState, "SetPool", new CmeStringHandler() {
 				public boolean handleString(String handle) {	
 					
 					/* Let the current state properties override the local properties. */
 					handle = m_CurState.translateString(handle);
 					handle = m_App.translateString(handle);
 					
 					CmeSelectiveIter sel = m_App.getPool(handle);
 					
 					if (sel == null)
 						return false;
 					
 					sel.reset();
 		
 					for (int x=0; x<count; x++) {
 						int next = sel.getNext();
 						handle = handle.trim();
 						String str = handle + String.valueOf(x+1);
 						//System.out.println("Adding Property: " + str + " as '" + String.valueOf(next) + "'");
 						m_CurState.setProperty(str, String.valueOf(next));
 					}
 			
 					return true;
 				}
 			});
 	}
 	
 	private boolean setStatePoolProperties() {
 		return m_App.handleCompoundProperty(m_CurState, "StatePool", new CmeStringHandler() {
 					public boolean handleString(String handle) {
 						
 						/* Let the current state properties override the local properties. */
 						handle = m_CurState.translateString(handle);
 						handle = m_App.translateString(handle);
 						
 						CmeSelectiveIter sel = m_App.getPool(handle);
 						
 						if (sel == null)
 							return false;
 						
 						if (m_CurState.getStep() == 0)
 							sel.reset();
 						
 						int next = sel.getNext();
 						String snext = String.valueOf(next);
 						
 						handle = handle.trim();
 						System.err.println("Adding Property: " + handle + " as '" + snext + "'");
 						m_CurState.setProperty(handle, snext);
 						
 						return true;
 					}
 			});
 	}
 
 	/**
 	 * Show the processed instructions with the current selected image.
 	 * 
 	 * @param fileName - name of the instruction file to show.
 	 * 
 	 * @throws IOException
 	 */
 	private void showProcessedInstructions(String fileName) throws Exception {
 		m_sContent = CmeApp.readFile(fileName);
 
 		this.adjustLayout();
 		
 		String content;
 		
 		/* App props may be constant, but the state namespace may chose to override them! */
 		content = m_CurState.translateString(m_sContent);
 		content = m_App.translateString(content);
 		
 		ActionListener listener = m_SubmitListener;
 		
 		m_App.dmsg(10, "Setting Contents");
 		if (m_CurState.getProperty("DisableSubmitHook") != null)
 			listener = null;
 		if (m_CurState.getProperty("DisableSubmitOnEnter") != null)
 			listener = null;
 		
 		m_HtmlView.setContent(content, listener);
 	}
 
 	private int displayTimer() {
 		String pst = m_CurState.getStringProperty("DisplayTimer");
 		
 		if (pst == null)
 			return 0;
 		
 		pst = pst.toLowerCase();
 		
 		if (pst.equals("no") || pst.equals("false"))
 			return 0;
 		
 		if (pst.equals("up"))
 			return +1;
 		
 		return -1;
 	}
 	
 	private void updateClock() {
 		int clockDirection = displayTimer();
 		if (clockDirection == 0)
 				m_cClock.setVisible(false);
 		else {
 			if (clockDirection < 0) 
 				m_cClock.setCountDown(true);
 			else
 				m_cClock.setCountDown(false);
 			m_cClock.setVisible(true);
 		}
 		
 		int timeLimit = m_CurState.getIntProperty("Limit");
 		int timeRes = m_CurState.getIntProperty("Resolution");
 		
 		if (timeLimit <= 0) {
 			m_cClock.reset();
 			m_cClock.setVisible(false);
 			return;
 		}
 
 		if (timeRes < 0)
 			timeRes = 1;
 		
 		m_cClock.setTimeLimit(timeLimit);
 		m_cClock.setResolution(timeRes);
 		m_cClock.reset();
 		
 		/* The clock is continuous and is NOT only after first Study */
 		if (isContinuousTimer() && !isContinuousAfterFirstSelection())
 			m_cClock.start("");
 	}
 
 	private void updateButtonText() {
 		if (m_bNext == null || m_CurState == null)
 			return;
 		
 		if (m_bRefresh != null)
 			m_bRefresh.setVisible(true);
 		
 		if (m_bInStudyState) {
 			m_bNext.setText("Close");
 			m_bNext.setVisible(true);
 		} else if (m_CurState.getEventResponseCount(CmeState.EVENT_CLICK_PRIMARY) == 0) {
 			m_bNext.setVisible(false);
 			if (m_bRefresh != null)
 				m_bRefresh.setVisible(false);
 		} else {
 			String bString = (String)m_CurState.getProperty("PrimaryButtonText");
 			if (bString != null) {
 				m_bString = bString;
 				m_bNext.setText(m_bString);
 			} else {
 				m_bString = "Next";
 			}
 			m_bNext.setVisible(true);
 		}
 	}
 	
 	private void updateStudyContent() throws IOException {
 		m_iStudyCount = 1;
 		Object pbStudyFile = m_CurState.getProperty("StudyFile");
 		
 		if (pbStudyFile != null) {
 			m_sStudyContent = CmeApp.readFile(pbStudyFile.toString());
 			//System.out.println("Study File: " + pbStudyFile.toString());
 		} else {
 			//System.out.println("No Study File Set!");
 		}
 	}
 	/** 
 	 * Blank the instruction screen.
 	 */
 	public void blankInstructions() {
 		m_HtmlView.setVisible(false);
 	}
 
 	/**
 	 * Show the input prompt.
 	 * 
 	 * @param msg
 	 *            - the message to display to the user
 	 * @return true if successful; false else
 	 * @throws Exception
 	 */
 	private boolean showInputPrompt(String msg) throws Exception {
 		String input;
 
 		if (msg == null) {
 			throw new Exception("Invalid prompt message specified!");
 		}
 
 		msg = m_CurState.translateString(msg);
 		msg = m_App.translateString(msg);
 
 		do {
 			input = JOptionPane.showInputDialog(msg);
 			if (input == null) {
 				return false;
 			}
 		} while (!m_CurState.validateInput(input));
 
 		String name = (String) m_CurState.getProperty("FeedbackName");
 		if (name == null) {
 			throw new Exception("Feedback name is null!");
 		}
 
 		m_App.addFeedback(name, input);
 
 		return true;
 	}
 
 	/**
 	 * Paint function for the Instruction
 	 * 
 	 * @param g - graphics context for the current JPane
 	 */
 	public void paint(Graphics g) {
 		super.paint(g);
 		// Paint the area
 	}
 
 	/**
 	 * Updates the PairFactory to the desired dataset settings.
 	 */
 	public void updateDataset() {
 		String group = m_CurState.getStringProperty("CurrentDataset");
 		if (group == null)
 			m_PairFactory.clearDataSet();
 		else
 			m_PairFactory.setDataSet(group);
 	}
 	
 	/**
 	 * Sets the current state and adjusts the layout appropriately
 	 * 
 	 * @param mCurState
 	 *            - the current state to be displayed
 	 * @throws Exception
 	 */
 	public void setState(CmeState mCurState) throws Exception {
 		m_CurState = mCurState;
 		
 		updateDataset();
 
 		updateStudyContent();
 		
 		// Begin!
 		try {
 			switch (m_CurState.getState()) {
 				case CmeState.STATE_INPUT:
 				case CmeState.STATE_INSTRUCTION:
 					updateInstructionFile(false);
 					break;
 
 				case CmeState.STATE_MULTIPLE:
 					updateInstructionFile(true);
 					break;
 
 				case CmeState.STATE_PROMPT:
 					this.setVisible(false);
 					showInputPrompt((String) m_CurState.getProperty("PromptText"));
 					if (isProvidedFeedbackValid())
 						m_App.setNextState();
 					return;
 
 				default:
 					throw new Exception("Default state handler hit!");
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			throw new Exception("Failed to set Instruction State: "
 					+ ex.getMessage());
 		}
 		
 		this.setVisible(true);
 		
 		m_HtmlView.requestFirstFocus();
 	}
 
 	public boolean isProvidedFeedbackValid() {
 		boolean valid = false;
 		
 		try {
 			Iterator<CmeResponse> iter = m_HtmlView.getResponseIterator();
 
 			valid = m_CurState.validateInput(iter);
 			if (!valid) {
 				//System.out.println("Invalid input!");
 				return false;
 			}
 			
 			valid = generateFeedback();
 		} catch (Exception ex) {
 			//System.out.println(
 			//		"Failed to generate output for feedback:\n" + ex.getMessage());
 			ex.printStackTrace();
 			System.exit(1);
 		}
 		return valid;
 	}
 	
 	/**
 	 * Generate all feedback from the current component set.
 	 * @return true if feedback was generate; false else
 	 * @throws Exception
 	 */
 	private boolean generateFeedback() throws Exception {
 		Iterator<CmeResponse> iter = m_HtmlView.getResponseIterator();
 		while (iter != null && iter.hasNext()) {
 			m_App.addFeedback(iter.next());
 		}
 		return true;
 	}
 }
