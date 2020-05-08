 package ch.sbs.plugin.preptools;
 
 import java.awt.event.ActionEvent;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.swing.AbstractAction;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 
 import ro.sync.exml.workspace.api.editor.WSEditor;
 import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
 import ch.sbs.utils.preptools.DocumentUtils;
 import ch.sbs.utils.preptools.FileUtils;
 import ch.sbs.utils.preptools.Match;
 import ch.sbs.utils.preptools.Match.PositionMatch;
 import ch.sbs.utils.preptools.MetaUtils;
 import ch.sbs.utils.preptools.RegionSkipper;
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
 			final DocumentMetaInfo dmi = prepToolsPluginExtension
 					.getDocumentMetaInfo();
 			final Document document = dmi.getDocument();
 
 			OxygenEditGrouper.perform(document, new OxygenEditGrouper.Edit() {
 				@Override
 				public void edit() {
 
 					try {
 						doSomething();
 					} catch (final RuntimeException e) {
 						prepToolsPluginExtension
 								.showDialog("RuntimeException occurred: " + e
 										+ " " + getStackTrace(e));
 					} catch (final BadLocationException e) {
 						prepToolsPluginExtension.showDialog(e.getMessage()
 								+ " " + getStackTrace(e));
 						throw new RuntimeException(e);
 					}
 
 				}
 			});
 			dmi.setCurrentState();
 			if (dmi.isDone()) {
 				wrapUp(document);
 				prepToolsPluginExtension.showDialog("You're done with "
 						+ getProcessName() + "!");
 
 				// TODO: once the schema has been upgraded, set this flag to
 				// true (or remove the if)
 				final boolean FEATURE_1205 = false;
 				if (FEATURE_1205) {
 					OxygenEditGrouper.perform(document,
 							new OxygenEditGrouper.Edit() {
 								@Override
 								public void edit() {
 
 									MetaUtils.insertPrepToolInfo(
 											prepToolsPluginExtension
 													.getDocumentMetaInfo()
 													.getDocument(),
 											getProcessName());
 
 								}
 							});
 				}
 			}
 		}
 	}
 
 	private void wrapUp(final Document document) {
 
 		OxygenEditGrouper.perform(document, new OxygenEditGrouper.Edit() {
 			@Override
 			public void edit() {
 
 				doWrapUp();
 
 			}
 
 		});
 
 	}
 
 	/**
 	 * Hook thet gets called just before meta-information is inserted into the
 	 * document.
 	 */
 	protected void doWrapUp() {
 
 	}
 
 	private String getStackTrace(final Exception e) {
 		final StringWriter sw = new StringWriter();
 		e.printStackTrace(new PrintWriter(sw));
 		return sw.toString();
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
 
 	protected int getStartIndex() {
 		final Document document = prepToolsPluginExtension
 				.getDocumentMetaInfo().getDocument();
 		try {
 			final String bookTag = "<book";
 			final int indexOf = document.getText(0, document.getLength())
 					.indexOf(bookTag);
 			if (indexOf < 0) {
 				final String message = "\"" + bookTag
 						+ "\" not found in document!";
 				prepToolsPluginExtension.showMessage(message);
 				throw new RuntimeException(message);
 			}
 			return indexOf;
 		} catch (final BadLocationException e) {
 			prepToolsPluginExtension.showMessage(e.getMessage());
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Subclasses call this method to notify completion of process.
 	 */
 	protected void done() {
 		prepToolsPluginExtension.getDocumentMetaInfo().setDone(true);
 	}
 
 	protected abstract String getProcessName();
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupAction extends AbstractPrepToolAction {
 
 	private final String MYTAG;
 
 	AbstractMarkupAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String tag) {
 		super(thePrepToolsPluginExtension);
 		MYTAG = tag;
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
 		// Possibly improve this: don't create a skipper for every searchOn
 		// call.
 		// Instead save the skipper in DocumentMetaInfo. However, make sure
 		// it is properly synced with changes occurring in the text, also
 		// via Undo!
 		final RegionSkipper skipper = prepToolsPluginExtension.makeSkipper();
 		final Match match = new MarkupUtil(skipper).find(newText, startAt,
 				getPattern());
 		if (match.equals(Match.NULL_MATCH)) {
 			match.startOffset = 0;
 			match.endOffset = 0;
 			done();
 		}
 		dmi.setCurrentState();
 		prepToolsPluginExtension.select(match.startOffset, match.endOffset);
 		dmi.setCurrentPositionMatch(new Match.PositionMatch(document, match));
 	}
 
 	abstract protected Pattern getPattern();
 
 	protected DocumentMetaInfo.MetaInfo getMetaInfo() {
 		return prepToolsPluginExtension.getDocumentMetaInfo()
 				.getCurrentToolSpecificMetaInfo();
 	}
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupStartAction extends AbstractMarkupAction {
 
 	AbstractMarkupStartAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String tag) {
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
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String tag) {
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
 				prepToolsPluginExtension.select(pm.startOffset.getOffset(),
 						pm.endOffset.getOffset());
 			}
 		}
 	}
 
 	protected int lastMatchStart;
 	protected int lastMatchEnd;
 
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupChangeVetoAction extends
 		AbstractMarkupProceedAction {
 
 	AbstractMarkupChangeVetoAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String tag) {
 		super(thePrepToolsPluginExtension, tag);
 	}
 
 	@Override
 	protected boolean veto(final String selText) {
 		return (selText == null || !MarkupUtil.matches(selText, getPattern()));
 	}
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupChangeAction extends
 		AbstractMarkupChangeVetoAction {
 
 	AbstractMarkupChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String tag) {
 		super(thePrepToolsPluginExtension, tag);
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.sbs.plugin.preptools.ProceedAction#handleText(javax.swing.text.Document, java.lang.String)
 	 */
 	@Override
 	protected int handleText(final Document document, final String selText)
 			throws BadLocationException {
 		final String FULL_OPENING_TAG = "<" + getTag() + ">";
 		final String FULL_CLOSING_TAG = "</"
 				+ MarkupUtil.getClosingTag(getTag()) + ">";
 		// starting with the end, so the start position doesn't shift
 		document.insertString(lastMatchEnd, FULL_CLOSING_TAG, null);
 		document.insertString(lastMatchStart, FULL_OPENING_TAG, null);
 
 		final int continueAt = lastMatchStart + FULL_OPENING_TAG.length()
 				+ FULL_CLOSING_TAG.length() + selText.length();
 		return continueAt;
 	}
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupFullRegexChangeAction extends
 		AbstractMarkupChangeVetoAction {
 
 	private final String replaceString;
 
 	AbstractMarkupFullRegexChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String tag, final String theReplaceString) {
 		super(thePrepToolsPluginExtension, tag);
 		replaceString = theReplaceString;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.sbs.plugin.preptools.ProceedAction#handleText(javax.swing.text.Document, java.lang.String)
 	 */
 	@Override
 	protected int handleText(final Document document, final String selText)
 			throws BadLocationException {
		if (selText == null) {
			return lastMatchStart;
		}
 		final String newText = getPattern().matcher(selText).replaceAll(
 				replaceString);
 		document.remove(lastMatchStart, selText.length());
 		document.insertString(lastMatchStart, newText, null);
 
 		return lastMatchStart + newText.length();
 	}
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupFindAction extends AbstractMarkupProceedAction {
 
 	AbstractMarkupFindAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String tag) {
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
 class RegexChangeAction extends AbstractMarkupChangeAction {
 	private final RegexHelper helper;
 
 	RegexChangeAction(
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
 class FullRegexChangeAction extends AbstractMarkupFullRegexChangeAction {
 	private final RegexHelper helper;
 
 	FullRegexChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String thePattern, final String theProcessName,
 			final String theTag, final String theReplaceString) {
 		super(thePrepToolsPluginExtension, theTag, theReplaceString);
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
 abstract class AccentChangeAction extends FullRegexChangeAction {
 
 	private static final String PLACE_HOLDER = "_";
 	private static final String REGEX_SPAN_TEMPLATE = "<span\\s+brl:accents\\s*=\\s*\""
 			+ PLACE_HOLDER + "\"\\s*>(.*?)</span\\s*>";
 	public static final String REGEX_SPAN_REDUCED = REGEX_SPAN_TEMPLATE
 			.replace(PLACE_HOLDER, "reduced");
 
 	public static final String REGEX_SPAN_DETAILED = REGEX_SPAN_TEMPLATE
 			.replace(PLACE_HOLDER, "detailed");
 
 	public static final String REPLACE = "$1";
 
 	AccentChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String thePattern, final String theProcessName,
 			final String theTag, final String theReplaceString) {
 		super(thePrepToolsPluginExtension, thePattern, theProcessName, theTag,
 				theReplaceString);
 	}
 
 	@Override
 	protected void doSomething() throws BadLocationException {
 		super.doSomething();
 		incrementCounter();
 	}
 
	// override veto behaviour from intermediate superclasses.
	@Override
	protected boolean veto(final String selText) {
		return false;
	}

 	protected abstract void incrementCounter();
 
 	@Override
 	protected void doWrapUp() {
 		final AccentPrepTool.MetaInfo metaInfo = getMetaInfo();
 		final int foreignCount = metaInfo.getForeignCount();
 		final int swissCount = metaInfo.getSwissCount();
 		if (foreignCount > swissCount) {
 			DocumentUtils.performReplacement(prepToolsPluginExtension
 					.getDocumentMetaInfo().getDocument(), REGEX_SPAN_REDUCED,
 					REPLACE);
 		}
 		else {
 			DocumentUtils.performReplacement(prepToolsPluginExtension
 					.getDocumentMetaInfo().getDocument(), REGEX_SPAN_DETAILED,
 					REPLACE);
 		}
 	}
 
 	/**
 	 * Utility (not hook!) method to get tool specific metainfo.
 	 * 
 	 * @return tool specific metainfo.
 	 */
 	@Override
 	protected final AccentPrepTool.MetaInfo getMetaInfo() {
 		final DocumentMetaInfo dmi = prepToolsPluginExtension
 				.getDocumentMetaInfo(prepToolsPluginExtension.getWsEditor()
 						.getEditorLocation());
 		return (AccentPrepTool.MetaInfo) dmi
 				.getToolSpecificMetaInfo(AccentPrepTool.LABEL);
 	}
 
 }
 
 @SuppressWarnings("serial")
 class AccentStartAction extends RegexStartAction {
 
 	AccentStartAction(PrepToolsPluginExtension thePrepToolsPluginExtension,
 			String thePattern, String theProcessName, String theTag) {
 		super(thePrepToolsPluginExtension, thePattern, theProcessName, theTag);
 	}
 
 	@Override
 	protected void doSomething() throws BadLocationException {
 		((AccentPrepTool.MetaInfo) getMetaInfo()).resetCounts();
 		super.doSomething();
 	}
 
 }
 
 @SuppressWarnings("serial")
 class SwissAccentChangeAction extends AccentChangeAction {
 
 	SwissAccentChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String thePattern, final String theProcessName,
 			final String theTag, final String theReplaceString) {
 		super(thePrepToolsPluginExtension, thePattern, theProcessName, theTag,
 				theReplaceString);
 	}
 
 	@Override
 	protected void incrementCounter() {
 		getMetaInfo().incrementSwissCount();
 	}
 }
 
 @SuppressWarnings("serial")
 class ForeignAccentChangeAction extends AccentChangeAction {
 
 	ForeignAccentChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String thePattern, final String theProcessName,
 			final String theTag, final String theReplaceString) {
 		super(thePrepToolsPluginExtension, thePattern, theProcessName, theTag,
 				theReplaceString);
 	}
 
 	@Override
 	protected void incrementCounter() {
 		getMetaInfo().incrementForeignCount();
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
 		super(thePrepToolsPluginExtension, VFormActionHelper.VFORM_TAG);
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
 class VFormChangeAction extends AbstractMarkupChangeAction {
 	private final VFormActionHelper helper;
 
 	VFormChangeAction(final PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		super(thePrepToolsPluginExtension, VFormActionHelper.VFORM_TAG);
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
 		super(thePrepToolsPluginExtension, VFormActionHelper.VFORM_TAG);
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
 
 		if (getMetaInfo().hasNext()) {
 			final PositionMatch match = getMetaInfo().next();
 			prepToolsPluginExtension.select(match.startOffset.getOffset(),
 					match.endOffset.getOffset());
 		}
 		else {
 			done();
 		}
 	}
 
 	protected void init() {
 
 	}
 
 	AbstractOrphanParenAction(
 			PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		super(thePrepToolsPluginExtension);
 	}
 
 	@Override
 	public String getProcessName() {
 		return ParensPrepTool.LABEL;
 	}
 }
 
 @SuppressWarnings("serial")
 class OrphanParenStartAction extends AbstractOrphanParenAction {
 
 	OrphanParenStartAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		super(thePrepToolsPluginExtension);
 	}
 
 	@Override
 	protected void init() {
 		final WSTextEditorPage aWSTextEditorPage = prepToolsPluginExtension
 				.getPage();
 		final Document document = aWSTextEditorPage.getDocument();
 		final List<Match> orphans;
 		try {
 			orphans = ParensUtil.findOrphans(
 					document.getText(0, document.getLength()), getStartIndex(),
 					prepToolsPluginExtension.makeSkipper());
 		} catch (BadLocationException e) {
 			prepToolsPluginExtension.showMessage(e.getMessage());
 			throw new RuntimeException(e);
 		}
 		final ParensPrepTool.MetaInfo parensMetaInfo = getMetaInfo();
 		parensMetaInfo.set(orphans);
 		parensMetaInfo.setHasStarted(true);
 		parensMetaInfo.setDone(false);
 	}
 }
 
 @SuppressWarnings("serial")
 class OrphanParenFindNextAction extends AbstractOrphanParenAction {
 
 	OrphanParenFindNextAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		super(thePrepToolsPluginExtension);
 	}
 }
