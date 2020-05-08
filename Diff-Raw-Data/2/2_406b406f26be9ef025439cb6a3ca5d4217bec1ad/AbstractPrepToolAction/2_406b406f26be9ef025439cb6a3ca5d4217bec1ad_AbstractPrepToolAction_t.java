 package ch.sbs.plugin.preptools;
 
 import java.awt.event.ActionEvent;
 import java.net.URL;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.swing.AbstractAction;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 
 import ro.sync.exml.workspace.api.editor.WSEditor;
 import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
 import ch.sbs.utils.preptools.FileUtils;
 import ch.sbs.utils.preptools.Match;
 import ch.sbs.utils.preptools.parens.ParensUtil;
 import ch.sbs.utils.preptools.vform.MarkupUtil;
 
 @SuppressWarnings("serial")
 abstract class AbstractPrepToolAction extends AbstractAction {
 	/**
 	 * 
 	 */
 	protected final PrepToolsPluginExtension prepToolsPluginExtension;
 
 	/**
 	 * @param thePrepToolsPluginExtension
 	 */
 	AbstractPrepToolAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		prepToolsPluginExtension = thePrepToolsPluginExtension;
 	}
 
 	@Override
 	public void actionPerformed(final ActionEvent arg0) {
 		if ((prepToolsPluginExtension.getPage()) != null) {
 			try {
 				doSomething();
 			} catch (final BadLocationException e) {
 				e.printStackTrace();
 			}
 			prepToolsPluginExtension.getDocumentMetaInfo().setCurrentState();
 		}
 	}
 
 	/**
 	 * Hook that gets called only when editor, page, document, text have
 	 * successfully been retrieved.
 	 * 
 	 * @throws BadLocationException
 	 */
 	protected abstract void doSomething() throws BadLocationException;
 
 	/**
 	 * Utility method that returns the end position of the current selection.
 	 * 
 	 * @return The end position of the current selection.
 	 */
 	protected final int getSelectionEnd() {
 		final WSEditor editorAccess = prepToolsPluginExtension.getWsEditor();
 		final WSTextEditorPage aWSTextEditorPage = PrepToolsPluginExtension
 				.getPage(editorAccess);
 		return aWSTextEditorPage.getSelectionEnd();
 	}
 
 	/**
 	 * 
 	 * Utility method to select text.
 	 * 
 	 * @param match
 	 */
 	protected final void select(final Match match) {
 		prepToolsPluginExtension.getPage().select(match.startOffset,
 				match.endOffset);
 	}
 
 	/**
 	 * 
 	 * Utility method to select text.
 	 * 
 	 * @param pm
 	 */
 	protected final void select(final Match.PositionMatch pm) {
 		prepToolsPluginExtension.getPage().select(pm.startOffset.getOffset(),
 				pm.endOffset.getOffset());
 	}
 
 	protected int getStartIndex() {
 		final Document document = prepToolsPluginExtension
 				.getDocumentMetaInfo().getDocument();
 		try {
 			return document.getText(0, document.getLength()).indexOf("<dtbook");
 		} catch (final BadLocationException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupAction extends AbstractPrepToolAction {
 
 	private final MarkupUtil markupUtil;
 
 	private final String MYTAG;
 
 	AbstractMarkupAction(PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String tag) {
 		super(thePrepToolsPluginExtension);
 		MYTAG = tag;
 		markupUtil = new MarkupUtil(tag);
 	}
 
 	protected String getTag() {
 		return MYTAG;
 	}
 
 	/**
 	 * Utility method to search on in document starting at startAt.
 	 * 
 	 * @param aWSTextEditorPage
 	 * @param editorAccess
 	 * @param startAt
 	 * 
 	 * @throws BadLocationException
 	 */
 	protected void searchOn(final WSTextEditorPage aWSTextEditorPage,
 			final WSEditor editorAccess, final int startAt)
 			throws BadLocationException {
 		final Document document = aWSTextEditorPage.getDocument();
 		final String newText = document.getText(0, document.getLength());
 		final DocumentMetaInfo dmi = prepToolsPluginExtension
 				.getDocumentMetaInfo(editorAccess.getEditorLocation());
 		final Match match = find(startAt, newText, getPattern());
 		if (match.equals(Match.NULL_MATCH)) {
 			prepToolsPluginExtension.showDialog("You're done with "
 					+ getProcessName() + "!");
 			dmi.getCurrentToolSpecificMetaInfo().done();
 			match.startOffset = 0;
 			match.endOffset = 0;
 		}
 		dmi.setCurrentState();
 		select(match);
 		dmi.setCurrentPositionMatch(new Match.PositionMatch(document, match));
 	}
 
 	private Match find(final int startAt, final String newText,
 			final Pattern pattern) {
 		return markupUtil.find(newText, startAt, pattern);
 	}
 
 	abstract protected Pattern getPattern();
 
 	abstract protected String getProcessName();
 
 	protected DocumentMetaInfo.MetaInfo getMetaInfo() {
 		return prepToolsPluginExtension.getDocumentMetaInfo()
 				.getCurrentToolSpecificMetaInfo();
 	}
 
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupStartAction extends AbstractMarkupAction {
 
 	AbstractMarkupStartAction(
 			PrepToolsPluginExtension thePrepToolsPluginExtension, String tag) {
 		super(thePrepToolsPluginExtension, tag);
 	}
 
 	@Override
 	protected void doSomething() throws BadLocationException {
 		final WSEditor editorAccess = prepToolsPluginExtension.getWsEditor();
 		final WSTextEditorPage aWSTextEditorPage = prepToolsPluginExtension
 				.getPage();
 		final URL editorLocation = editorAccess.getEditorLocation();
 		final DocumentMetaInfo.MetaInfo metaInfo = getMetaInfo();
 		if (metaInfo.isDone()) {
 			if (prepToolsPluginExtension.showConfirmDialog(getProcessName()
 					+ ": Start Over?",
 					"The document " + FileUtils.basename(editorLocation)
 							+ " has already been " + getProcessName()
 							+ "ed.\n Do you want to start over?")
 
 			) {
 				metaInfo.setDone(false);
 			}
 			else {
 				return;
 			}
 		}
 		else if (metaInfo.hasStarted()) {
 			if (prepToolsPluginExtension.showConfirmDialog(getProcessName()
 					+ ": Start Over?",
 					"The document " + FileUtils.basename(editorLocation)
 							+ " is currently being " + getProcessName()
 							+ "ed.\n Do you want to start over?")
 
 			) {
 				metaInfo.setDone(false);
 			}
 			else {
 				return;
 			}
 		}
 
 		metaInfo.setHasStarted(true);
 		metaInfo.setDone(false);
 		searchOn(aWSTextEditorPage, editorAccess, getStartIndex());
 	}
 
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupProceedAction extends AbstractMarkupAction {
 
 	AbstractMarkupProceedAction(
 			PrepToolsPluginExtension thePrepToolsPluginExtension, String tag) {
 		super(thePrepToolsPluginExtension, tag);
 	}
 
 	/**
 	 * 
 	 * Hook to be implemented by subclasses. Handles selected text and returns
 	 * position where to continue with search.
 	 * 
 	 * @param document
 	 *            The document.
 	 * @param selText
 	 *            The current selection.
 	 * @return The position where to continue with search.
 	 * @throws BadLocationException
 	 */
 	protected abstract int handleText(final Document document,
 			final String selText) throws BadLocationException;
 
 	/**
 	 * Hook to be implemented by subclasses. If true the process is aborted.
 	 * 
 	 * @param selText
 	 * @return True if the process is to be aborted.
 	 */
 	protected boolean veto(final String selText) {
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.sbs.plugin.preptools.AbstractVFormAction#doSomething(ro.sync.exml.workspace.api.editor.WSEditor, ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage, javax.swing.text.Document, ch.sbs.plugin.preptools.DocumentMetaInfo)
 	 */
 	@Override
 	protected void doSomething() throws BadLocationException {
 		final WSTextEditorPage aWSTextEditorPage = prepToolsPluginExtension
 				.getPage();
 
 		final String selText = aWSTextEditorPage.getSelectedText();
 
 		if (veto(selText))
 			return;
 
 		handleManualCursorMovement();
 
 		final int continueAt = handleText(aWSTextEditorPage.getDocument(),
 				selText);
 
 		searchOn(aWSTextEditorPage, prepToolsPluginExtension.getWsEditor(),
 				continueAt);
 	}
 
 	/**
 	 * Utility method to handle user's manual cursor movement.
 	 * 
 	 * @param aWSTextEditorPage
 	 * @param dmi
 	 */
 	private void handleManualCursorMovement() {
 		final DocumentMetaInfo dmi = prepToolsPluginExtension
 				.getDocumentMetaInfo();
 		final WSTextEditorPage aWSTextEditorPage = prepToolsPluginExtension
 				.getPage();
 		lastMatchStart = aWSTextEditorPage.getSelectionStart();
 		lastMatchEnd = aWSTextEditorPage.getSelectionEnd();
 		final Match.PositionMatch pm = dmi.getCurrentPositionMatch();
 		if (lastMatchStart != pm.startOffset.getOffset()
 				|| lastMatchEnd != pm.endOffset.getOffset()
 				|| dmi.manualEditOccurred()) {
 			if (prepToolsPluginExtension.showConfirmDialog(getProcessName()
 					+ ": Cursor", "Cursor position has changed!\n",
 					"Take up where we left off last time", "continue anyway")) {
 				select(pm);
 			}
 		}
 	}
 
 	protected int lastMatchStart;
 	protected int lastMatchEnd;
 
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupAcceptAction extends AbstractMarkupProceedAction {
 
 	AbstractMarkupAcceptAction(
 			PrepToolsPluginExtension thePrepToolsPluginExtension, String tag) {
 		super(thePrepToolsPluginExtension, tag);
 	}
 
 	@Override
 	protected boolean veto(final String selText) {
 		return (selText == null || !MarkupUtil.matches(selText, getPattern()));
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.sbs.plugin.preptools.ProceedAction#handleText(javax.swing.text.Document, java.lang.String)
 	 */
 	@Override
 	protected int handleText(final Document document, final String selText)
 			throws BadLocationException {
		final String ELEMENT_NAME = getTag();
 		// starting with the end, so the start position doesn't shift
 		document.insertString(lastMatchEnd, "</" + ELEMENT_NAME + ">", null);
 		document.insertString(lastMatchStart, "<" + ELEMENT_NAME + ">", null);
 
 		final int continueAt = lastMatchStart + ELEMENT_NAME.length() * 2
 				+ "<></>".length() + selText.length();
 		return continueAt;
 	}
 
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupFindAction extends AbstractMarkupProceedAction {
 
 	AbstractMarkupFindAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			String tag) {
 		super(thePrepToolsPluginExtension, tag);
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.sbs.plugin.preptools.ProceedAction#handleText(javax.swing.text.Document, java.lang.String)
 	 */
 	@Override
 	protected int handleText(final Document document, final String selText)
 			throws BadLocationException {
 		return getSelectionEnd();
 	}
 
 }
 
 /**
  * Helper class to factor out common code in VFormActions.
  */
 class VFormActionHelper {
 
 	private final PrepToolsPluginExtension prepToolsPluginExtension;
 
 	VFormActionHelper(final PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		prepToolsPluginExtension = thePrepToolsPluginExtension;
 	}
 
 	public Pattern getPattern() {
 		return getMetaInfo().getCurrentPattern();
 	}
 
 	public String getProcessName() {
 		return VFormPrepTool.LABEL;
 	}
 
 	/**
 	 * Utility method to get tool specific metainfo.
 	 * Covariant return type. (VFormPrepTool.MetaInfo is a subclass of
 	 * DocumentMetaInfo.MetaInfo)
 	 * 
 	 * @return tool specific metainfo.
 	 */
 	protected final VFormPrepTool.MetaInfo getMetaInfo() {
 		return (VFormPrepTool.MetaInfo) prepToolsPluginExtension
 				.getDocumentMetaInfo().getToolSpecificMetaInfo(
 						VFormPrepTool.LABEL);
 	}
 }
 
 class RegexHelper {
 
 	private final Pattern pattern;
 	private final String processName;
 	private final String tag;
 
 	RegexHelper(final String thePattern, final String theProcessName,
 			final String theTag) {
 		pattern = Pattern.compile(thePattern);
 		processName = theProcessName;
 		tag = theTag;
 	}
 
 	public Pattern getPattern() {
 		return pattern;
 	}
 
 	public String getProcessName() {
 		return processName;
 	}
 
 	public String getTag() {
 		return tag;
 	}
 
 }
 
 @SuppressWarnings("serial")
 class RegexStartAction extends AbstractMarkupStartAction {
 	private final RegexHelper helper;
 
 	RegexStartAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String thePattern, final String theProcessName,
 			final String theTag) {
 		super(thePrepToolsPluginExtension, theTag);
 		helper = new RegexHelper(thePattern, theProcessName, theTag);
 	}
 
 	@Override
 	protected Pattern getPattern() {
 		return helper.getPattern();
 	}
 
 	@Override
 	protected String getProcessName() {
 		return helper.getProcessName();
 	}
 }
 
 @SuppressWarnings("serial")
 class RegexAcceptAction extends AbstractMarkupAcceptAction {
 	private final RegexHelper helper;
 
 	RegexAcceptAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String thePattern, final String theProcessName,
 			final String theTag) {
 		super(thePrepToolsPluginExtension, theTag);
 		helper = new RegexHelper(thePattern, theProcessName, theTag);
 	}
 
 	@Override
 	protected Pattern getPattern() {
 		return helper.getPattern();
 	}
 
 	@Override
 	protected String getProcessName() {
 		return helper.getProcessName();
 	}
 }
 
 @SuppressWarnings("serial")
 class RegexFindAction extends AbstractMarkupFindAction {
 	private final RegexHelper helper;
 
 	RegexFindAction(final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String thePattern, final String theProcessName,
 			final String theTag) {
 		super(thePrepToolsPluginExtension, theTag);
 		helper = new RegexHelper(thePattern, theProcessName, theTag);
 	}
 
 	@Override
 	protected Pattern getPattern() {
 		return helper.getPattern();
 	}
 
 	@Override
 	protected String getProcessName() {
 		return helper.getProcessName();
 	}
 }
 
 @SuppressWarnings("serial")
 class VFormStartAction extends AbstractMarkupStartAction {
 	private final VFormActionHelper helper;
 
 	VFormStartAction(final PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		super(thePrepToolsPluginExtension, Constants.VFORM_TAG);
 		helper = new VFormActionHelper(thePrepToolsPluginExtension);
 	}
 
 	@Override
 	protected Pattern getPattern() {
 		return helper.getPattern();
 	}
 
 	@Override
 	protected String getProcessName() {
 		return helper.getProcessName();
 	}
 }
 
 @SuppressWarnings("serial")
 class VFormAcceptAction extends AbstractMarkupAcceptAction {
 	private final VFormActionHelper helper;
 
 	VFormAcceptAction(final PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		super(thePrepToolsPluginExtension, Constants.VFORM_TAG);
 		helper = new VFormActionHelper(thePrepToolsPluginExtension);
 	}
 
 	@Override
 	protected Pattern getPattern() {
 		return helper.getPattern();
 	}
 
 	@Override
 	protected String getProcessName() {
 		return helper.getProcessName();
 	}
 }
 
 @SuppressWarnings("serial")
 class VFormFindAction extends AbstractMarkupFindAction {
 	private final VFormActionHelper helper;
 
 	VFormFindAction(final PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		super(thePrepToolsPluginExtension, Constants.VFORM_TAG);
 		helper = new VFormActionHelper(thePrepToolsPluginExtension);
 	}
 
 	@Override
 	protected Pattern getPattern() {
 		return helper.getPattern();
 	}
 
 	@Override
 	protected String getProcessName() {
 		return helper.getProcessName();
 	}
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractOrphanParenAction extends AbstractPrepToolAction {
 
 	/**
 	 * Utility (not hook!) method to get tool specific metainfo.
 	 * 
 	 * @param dmi
 	 * @return tool specific metainfo.
 	 */
 	protected final ParensPrepTool.MetaInfo getMetaInfo(
 			final DocumentMetaInfo dmi) {
 		return (ParensPrepTool.MetaInfo) dmi
 				.getToolSpecificMetaInfo(ParensPrepTool.LABEL);
 	}
 
 	/**
 	 * Utility (not hook!) method to get tool specific metainfo.
 	 * 
 	 * @return tool specific metainfo.
 	 */
 	protected final ParensPrepTool.MetaInfo getMetaInfo() {
 		final DocumentMetaInfo dmi = prepToolsPluginExtension
 				.getDocumentMetaInfo(prepToolsPluginExtension.getWsEditor()
 						.getEditorLocation());
 		return (ParensPrepTool.MetaInfo) dmi
 				.getToolSpecificMetaInfo(ParensPrepTool.LABEL);
 	}
 
 	@Override
 	protected void doSomething() throws BadLocationException {
 		init();
 		final DocumentMetaInfo dmi = prepToolsPluginExtension
 				.getDocumentMetaInfo();
 
 		if (getMetaInfo().hasNext()) {
 			select(dmi);
 		}
 		else {
 			handleNoneFound();
 		}
 	}
 
 	protected void init() {
 
 	}
 
 	protected void handleNoneFound() {
 
 	}
 
 	AbstractOrphanParenAction(
 			PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		super(thePrepToolsPluginExtension);
 	}
 
 	protected void select(final DocumentMetaInfo dmi) {
 		select(getMetaInfo().next());
 	}
 
 }
 
 @SuppressWarnings("serial")
 class OrphanParenStartAction extends AbstractOrphanParenAction {
 
 	OrphanParenStartAction(PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		super(thePrepToolsPluginExtension);
 	}
 
 	@Override
 	protected void init() {
 		final WSTextEditorPage aWSTextEditorPage = prepToolsPluginExtension
 				.getPage();
 		final Document document = aWSTextEditorPage.getDocument();
 		List<Match> orphans = null;
 		try {
 			orphans = ParensUtil.findOrphans(
 					document.getText(0, document.getLength()), getStartIndex());
 		} catch (BadLocationException e) {
 			prepToolsPluginExtension.showMessage(e.getMessage());
 			e.printStackTrace();
 		}
 		final ParensPrepTool.MetaInfo parensMetaInfo = getMetaInfo();
 		parensMetaInfo.set(orphans);
 		parensMetaInfo.setHasStarted(true);
 		parensMetaInfo.setDone(false);
 	}
 
 	@Override
 	protected void handleNoneFound() {
 		prepToolsPluginExtension.showDialog("No orphaned parens found.");
 		getMetaInfo().setDone(true);
 	}
 
 }
 
 @SuppressWarnings("serial")
 class OrphanParenFindNextAction extends AbstractOrphanParenAction {
 
 	OrphanParenFindNextAction(
 			PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		super(thePrepToolsPluginExtension);
 	}
 
 	@Override
 	protected void handleNoneFound() {
 		prepToolsPluginExtension
 				.showDialog("You're done with orphaned parens.");
 		getMetaInfo().setDone(true);
 	}
 }
