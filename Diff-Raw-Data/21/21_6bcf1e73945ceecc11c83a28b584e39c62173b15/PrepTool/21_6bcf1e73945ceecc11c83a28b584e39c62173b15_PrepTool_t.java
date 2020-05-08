 package ch.sbs.plugin.preptools;
 
 import java.awt.Container;
 import java.awt.event.InputEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.ComponentInputMap;
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.plaf.ActionMapUIResource;
 import javax.swing.text.Document;
 
 import ro.sync.exml.editor.EditorPageConstants;
 import ch.sbs.utils.preptools.Match;
 import ch.sbs.utils.preptools.vform.VFormUtil;
 
 /**
  * PrepTool provides PrepTool-Specific:
  * - menuItemNr
  * - DocumentMetaInfo.MetaInfo (this is also document-specific)
  * - toolbar
  * - actions
  * 
  */
 abstract class PrepTool {
 
 	protected final PrepToolsPluginExtension prepToolsPluginExtension;
 	private final int menuItemNr;
 
 	/**
 	 * @param thePrepToolsPluginExtension
 	 * @param theMenuItemNr
 	 */
 	PrepTool(final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			int theMenuItemNr) {
 		prepToolsPluginExtension = thePrepToolsPluginExtension;
 		menuItemNr = theMenuItemNr;
 	}
 
 	/**
 	 * 
 	 * Optional hook to make document-specific, tool-specific
 	 * DocumentMetaInfo.MetaInfo
 	 * 
 	 * @param document
 	 * @return DocumentMetaInfo.MetaInfo
 	 */
 	public DocumentMetaInfo.MetaInfo makeMetaInfo(final Document document) {
 		return new DocumentMetaInfo.MetaInfo();
 	}
 
 	public void activate() {
 		final DocumentMetaInfo documentMetaInfo = prepToolsPluginExtension
 				.getDocumentMetaInfo();
 		if (documentMetaInfo != null) {
 			documentMetaInfo.setCurrentPrepTool(this);
 		}
 		prepToolsPluginExtension.selectPrepToolItem(menuItemNr);
 		makeToolbar();
 	}
 
 	private void makeToolbar() {
 		final JComponent[] components = getComponents();
 		prepToolsPluginExtension.toolbarPanel.removeAll();
 		for (final JComponent component : components) {
 			prepToolsPluginExtension.toolbarPanel.add(component);
 		}
 		prepToolsPluginExtension.toolbarPanel.add(new JLabel(" " + getLabel()));
 		setCurrentState(prepToolsPluginExtension.getDocumentMetaInfo());
 		relayout();
 	}
 
 	private void relayout() {
 		Container parent = prepToolsPluginExtension.toolbarPanel;
 		while (parent.getParent() != null) {
 			parent = parent.getParent();
 			parent.invalidate();
 			parent.validate();
 		}
 	}
 
 	/**
 	 * Utility method. Makes button.
 	 * 
 	 * @param theAction
 	 *            The action associated with the button
 	 * @param theLabel
 	 *            The label for the button.
 	 * @return the newly created button.
 	 */
 	protected JButton makeButton(final Action theAction, final String theLabel,
 			int theKeyEvent) {
 		// assign accelerator key to JButton
 		// http://www.stratulat.com/assign_accelerator_key_to_a_JButton.html
 		final JButton jButton = new JButton(theAction);
 		jButton.setText(theLabel);
 		assignAcceleratorKey(theAction, theLabel, theKeyEvent, jButton);
 		return jButton;
 	}
 
 	private static void assignAcceleratorKey(final Action theAction,
 			final String theLabel, int theKeyEvent, final JComponent jComponent) {
 		final InputMap keyMap = new ComponentInputMap(jComponent);
 		keyMap.put(
 				KeyStroke.getKeyStroke(theKeyEvent, InputEvent.CTRL_DOWN_MASK
 						| InputEvent.ALT_DOWN_MASK), theLabel);
 		final ActionMap actionMap = new ActionMapUIResource();
 		actionMap.put(theLabel, theAction);
 		SwingUtilities.replaceUIActionMap(jComponent, actionMap);
 		SwingUtilities.replaceUIInputMap(jComponent,
 				JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap);
 	}
 
 	public void setAllActionsEnabled(boolean enabled) {
 		final Action[] allActions = getAllActions();
 		for (final Action action : allActions) {
 			action.setEnabled(enabled);
 		}
 		final JComponent[] additionalComponents = getAdditionalComponents();
 		for (final JComponent component : additionalComponents) {
 			component.setEnabled(enabled);
 		}
 		if (enabled) {
 			enableStuff();
 		}
 		else {
 			disableStuff();
 		}
 	}
 
 	/**
 	 * optional hookto enable gui stuff
 	 */
 	protected void enableStuff() {
 
 	}
 
 	/**
 	 * optional hook to disable gui stuff
 	 */
 	protected void disableStuff() {
 
 	}
 
 	/**
 	 * hook to provide label
 	 */
 	protected abstract String getLabel();
 
 	/**
 	 * hook to provide components
 	 */
 	protected abstract JComponent[] getComponents();
 
 	/**
 	 * hook to provide all actions
 	 */
 	protected abstract Action[] getAllActions();
 
 	/**
 	 * optional hook to provide all additional components that aren't covered
 	 * with hook getAllActions().
 	 */
 	protected JComponent[] getAdditionalComponents() {
 		return new JComponent[0];
 	}
 
 	/**
 	 * hook to provide mnemonic for this tool
 	 */
 	public abstract int getMnemonic();
 
 	public void setCurrentState(final DocumentMetaInfo theDocumentMetaInfo) {
 		if (theDocumentMetaInfo != null && theDocumentMetaInfo.isDtBook()) {
 			prepToolsPluginExtension.enableMenuPrepTools();
 			doSetCurrentState(theDocumentMetaInfo);
 			if (theDocumentMetaInfo.isDone()) {
 				prepToolsPluginExtension.setPrepToolItemDone(menuItemNr);
 			}
 		}
 		else {
 			prepToolsPluginExtension.disableMenuPrepTools();
 			setAllActionsEnabled(false);
 		}
 	}
 
 	/**
 	 * optional hook to updated current state of this tool
 	 */
 	protected void doSetCurrentState(final DocumentMetaInfo theDocumentMetaInfo) {
 
 	}
 
 	protected boolean isTextPage(final DocumentMetaInfo theDocumentMetaInfo) {
 		return theDocumentMetaInfo.getCurrentEditorPage().equals(
 				EditorPageConstants.PAGE_TEXT);
 	}

	/**
	 * Optional hook for PrepTools to provide the tag they insert.
	 * 
	 * @return Tag
	 */
	public String getTag() {
		return null;
	}
 }
 
 /**
  * Common superclass for RegexPrepTool and VFormPrepTool
  */
 abstract class MarkupPrepTool extends PrepTool {
 
 	private Action startAction;
 
 	private Action findAction;
 
 	private Action acceptAction;
 
 	protected abstract Action makeStartAction();
 
 	protected abstract Action makeFindAction();
 
 	protected abstract Action makeAcceptAction();
 
 	private TrafficLight trafficLight;
 
 	@Override
 	protected Action[] getAllActions() {
 		if (startAction == null) {
 			startAction = makeStartAction();
 			findAction = makeFindAction();
 			acceptAction = makeAcceptAction();
 		}
 		return new Action[] { startAction, acceptAction, findAction };
 	}
 
 	@Override
 	protected JComponent[] getComponents() {
 		getAllActions();
 		return new JComponent[] {
 				makeButton(startAction, "Start", KeyEvent.VK_7),
 				makeButton(findAction, "Find", KeyEvent.VK_8),
 				makeButton(acceptAction, "Accept", KeyEvent.VK_9),
 				trafficLight = new TrafficLight(26) };
 	}
 
 	MarkupPrepTool(final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			int theMenuItemNr) {
 		super(thePrepToolsPluginExtension, theMenuItemNr);
 	}
 
 	@Override
 	protected void disableStuff() {
 		trafficLight.off();
 	}
 
 	@Override
 	public void doSetCurrentState(final DocumentMetaInfo theDocumentMetaInfo) {
 
 		final boolean isTextPage = isTextPage(theDocumentMetaInfo);
 
 		/*
 		 TODO: make this table driven or something
 			 We have:
 			               possible states
 			 - hasStarted: 0 1 1 0 1 1
 			 - isDone:     0 0 1 0 0 1
 			 - isTextPage: 0 0 0 1 1 1
 			 ---------------------------
 			 - traffic:    0 0 3 1 2 3
 			 - start:      0 0 0 1 1 1 = isTextPage
 			 - find:       0 0 0 0 1 0 = isTextPage && hasStarted && !isDone
 			 - accept:     0 0 0 0 1 0 = isTextPage && hasStarted && !isDone
 			 - allforms:   0 0 0 1 1 1 = isTextPage
 			 UI elements
 			 - traffic:    stop:0/go:1/inProgress:2/done:3
 			 - start:      disabled:0/enabled:1
 			 - find:       disabled:0/enabled:1
 			 - accept:     disabled:0/enabled:1
 			 - allforms:   disabled:0/enabled:1
 		 */
 		startAction.setEnabled(isTextPage);
 		if (!theDocumentMetaInfo.hasStarted()) { // not started
 			findAction.setEnabled(false);
 			acceptAction.setEnabled(false);
 			if (isTextPage) {
 				trafficLight.go();
 			}
 			else {
 				trafficLight.stop();
 			}
 		}
 		else if (theDocumentMetaInfo.isDone()) { // done
 			trafficLight.done();
 			findAction.setEnabled(false);
 			acceptAction.setEnabled(false);
 		}
 		else { // inProgress
 			setAllActionsEnabled(isTextPage);
 			if (isTextPage) {
 				trafficLight.inProgress();
 			}
 			else {
 				trafficLight.stop();
 			}
 		}
 	}
 }
 
 /**
  * 
  * RegexPrepTool is not supposed to be subclassed.
  * It should be seen as a blackbox class that can be configured with
  * a regex, a mnemonic, a pattern, and a label.
  */
 class RegexPrepTool extends MarkupPrepTool {
 
 	final String LABEL;
 	final String TAG;
 	final int MNEMOMIC;
 	final String PATTERN;
 
 	RegexPrepTool(final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			int theMenuItemNr, int theMnemonic, final String theLabel,
 			final String thePattern, final String theTag) {
 		super(thePrepToolsPluginExtension, theMenuItemNr);
 		MNEMOMIC = theMnemonic;
 		LABEL = theLabel;
 		PATTERN = thePattern;
 		TAG = theTag;
 	}
 
 	@Override
	public String getTag() {
		return TAG;
	}

	@Override
 	protected String getLabel() {
 		return LABEL;
 	}
 
 	@Override
 	public int getMnemonic() {
 		return MNEMOMIC;
 	}
 
 	@Override
 	protected Action makeStartAction() {
 		return new RegexStartAction(prepToolsPluginExtension, PATTERN, LABEL,
 				TAG);
 	}
 
 	@Override
 	protected Action makeFindAction() {
 		return new RegexFindAction(prepToolsPluginExtension, PATTERN, LABEL,
 				TAG);
 	}
 
 	@Override
 	protected Action makeAcceptAction() {
 		return new RegexAcceptAction(prepToolsPluginExtension, PATTERN, LABEL,
 				TAG);
 	}
 
 }
 
 class FullRegexPrepTool extends RegexPrepTool {
 
 	private final String replaceString;
 
 	FullRegexPrepTool(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			int theMenuItemNr, int theMnemonic, final String theLabel,
 			final String theRegex, final String theTag,
 			final String theReplaceString) {
 		super(thePrepToolsPluginExtension, theMenuItemNr, theMnemonic,
 				theLabel, theRegex, theTag);
 		replaceString = theReplaceString;
 	}
 
 	@Override
 	protected Action makeAcceptAction() {
 		return new FullRegexAcceptAction(prepToolsPluginExtension, PATTERN,
 				LABEL, TAG, replaceString);
 	}
 
 }
 
 class VFormPrepTool extends MarkupPrepTool {
 
 	@Override
 	public DocumentMetaInfo.MetaInfo makeMetaInfo(final Document document) {
 		return new VFormPrepTool.MetaInfo();
 	}
 
 	static class MetaInfo extends DocumentMetaInfo.MetaInfo {
 		private Pattern currentPattern;
 
 		public MetaInfo() {
 			setPatternTo3rdPP();
 		}
 
 		/**
 		 * 
 		 * @return true if vform pattern is set to all.
 		 */
 		public boolean patternIsAll() {
 			return currentPattern == VFormUtil.getAllPattern();
 		}
 
 		/**
 		 * Sets vform pattern to all.
 		 */
 		public void setPatternToAll() {
 			currentPattern = VFormUtil.getAllPattern();
 		}
 
 		/**
 		 * Sets vform pattern to 3rdPersonPlural.
 		 */
 		public void setPatternTo3rdPP() {
 			currentPattern = VFormUtil.get3rdPPPattern();
 		}
 
 		/**
 		 * 
 		 * @return current vform-pattern.
 		 */
 		public Pattern getCurrentPattern() {
 			return currentPattern;
 		}
 
 	}
 
 	static final String LABEL = "VForms";
 
 	VFormPrepTool(final PrepToolsPluginExtension prepToolsPluginExtension,
 			int theMenuItemNr) {
 		super(prepToolsPluginExtension, theMenuItemNr);
 	}
 
 	private JCheckBox allForms;
 
 	@Override
 	protected String getLabel() {
 		return LABEL;
 	}
 
 	@Override
	public String getTag() {
		return Constants.VFORM_TAG;
	}

	@Override
 	protected JComponent[] getComponents() {
 		final JComponent[] comps = super.getComponents();
 		// intermediary list required, because the list
 		// created by Arrays.asList does not support the
 		// operation add(index, element) (throws RuntimeException)
 		final List<JComponent> list = new ArrayList<JComponent>(
 				Arrays.asList(comps));
 		list.add(list.size() - 1, allForms = makeCheckbox());
 		return list.toArray(new JComponent[0]);
 	}
 
 	@Override
 	public int getMnemonic() {
 		return KeyEvent.VK_V;
 	}
 
 	@Override
 	public void doSetCurrentState(final DocumentMetaInfo theDocumentMetaInfo) {
 		super.doSetCurrentState(theDocumentMetaInfo);
 		allForms.setSelected(getMetaInfo(theDocumentMetaInfo).patternIsAll());
 		allForms.setEnabled(isTextPage(theDocumentMetaInfo));
 	}
 
 	private MetaInfo getMetaInfo(final DocumentMetaInfo theDocumentMetaInfo) {
 		return (MetaInfo) theDocumentMetaInfo.getToolSpecificMetaInfo(LABEL);
 	}
 
 	private MetaInfo getMetaInfo() {
 		return getMetaInfo(prepToolsPluginExtension.getDocumentMetaInfo());
 	}
 
 	private JCheckBox makeCheckbox() {
 		final JCheckBox checkBox = new JCheckBox("All");
 		checkBox.setMnemonic('A');
 		checkBox.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					getMetaInfo().setPatternToAll();
 				}
 				else {
 					getMetaInfo().setPatternTo3rdPP();
 				}
 			}
 		});
 		return checkBox;
 	}
 
 	@Override
 	protected JComponent[] getAdditionalComponents() {
 		return new JComponent[] { allForms };
 	}
 
 	@Override
 	protected Action makeStartAction() {
 		return new VFormStartAction(prepToolsPluginExtension);
 	}
 
 	@Override
 	protected Action makeFindAction() {
 		return new VFormFindAction(prepToolsPluginExtension);
 	}
 
 	@Override
 	protected Action makeAcceptAction() {
 		return new VFormAcceptAction(prepToolsPluginExtension);
 	}
 }
 
 class ParensPrepTool extends PrepTool {
 
 	@Override
 	public DocumentMetaInfo.MetaInfo makeMetaInfo(final Document document) {
 		return new ParensPrepTool.MetaInfo(document);
 	}
 
 	static class MetaInfo extends DocumentMetaInfo.MetaInfo {
 		private Iterator<Match.PositionMatch> orphanedParensIterator;
 		private final Document document;
 
 		MetaInfo(final Document theDocument) {
 			document = theDocument;
 		}
 
 		/**
 		 * Sets orphaned parens.
 		 * 
 		 * @param theOrphanedParens
 		 */
 		public void set(final List<Match> theOrphanedParens) {
 			final List<Match.PositionMatch> pml = new ArrayList<Match.PositionMatch>();
 			for (final Match match : theOrphanedParens) {
 				Match.PositionMatch mp = new Match.PositionMatch(document,
 						match);
 				pml.add(mp);
 			}
 			orphanedParensIterator = pml.iterator();
 		}
 
 		/**
 		 * Iterator-function.
 		 * 
 		 * @return true if iterator has more orphaned parens.
 		 */
 		public boolean hasNext() {
 			return orphanedParensIterator.hasNext();
 		}
 
 		/**
 		 * Iterator-function.
 		 * 
 		 * @return next orphaned paren of this iterator.
 		 */
 		public Match.PositionMatch next() {
 			return orphanedParensIterator.next();
 		}
 
 	}
 
 	static final String LABEL = "Parens";
 
 	ParensPrepTool(final PrepToolsPluginExtension prepToolsPluginExtension,
 			int theMenuItemNr) {
 		super(prepToolsPluginExtension, theMenuItemNr);
 	}
 
 	private final Action startAction = new OrphanParenStartAction(
 			prepToolsPluginExtension);
 	private final Action findNextAction = new OrphanParenFindNextAction(
 			prepToolsPluginExtension);
 
 	@Override
 	protected String getLabel() {
 		return LABEL;
 	}
 
 	@Override
 	protected JComponent[] getComponents() {
 		return new JComponent[] {
 				makeButton(startAction, "Start", KeyEvent.VK_7),
 				makeButton(findNextAction, "Find", KeyEvent.VK_8) };
 	}
 
 	@Override
 	public int getMnemonic() {
 		return KeyEvent.VK_P;
 	}
 
 	@Override
 	public void doSetCurrentState(final DocumentMetaInfo theDocumentMetaInfo) {
 		startAction.setEnabled(true);
 		findNextAction.setEnabled(theDocumentMetaInfo.isProcessing());
 	}
 
 	@Override
 	protected Action[] getAllActions() {
 		return new Action[] { startAction, findNextAction };
 	}
 }
