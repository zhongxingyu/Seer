 /*=============================================================================#
  # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
  # All rights reserved. This program and the accompanying materials
  # are made available under the terms of the Eclipse Public License v1.0
  # which accompanies this distribution, and is available at
  # http://www.eclipse.org/legal/epl-v10.html
  # 
  # Contributors:
  #     Stephan Wahlbrink - initial API and implementation
  #=============================================================================*/
 
 package de.walware.docmlet.tex.ui.sourceediting;
 
 import static de.walware.docmlet.tex.core.text.LtxHeuristicTokenScanner.CURLY_BRACKET_TYPE;
 import static de.walware.docmlet.tex.core.text.LtxHeuristicTokenScanner.PARATHESIS_TYPE;
 import static de.walware.docmlet.tex.core.text.LtxHeuristicTokenScanner.SQUARE_BRACKET_TYPE;
 import static de.walware.ecommons.text.ui.BracketLevel.AUTODELETE;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.text.AbstractDocument;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.BadPartitioningException;
 import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.DocumentCommand;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.text.ITypedRegion;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.jface.text.link.LinkedModeModel;
 import org.eclipse.jface.text.link.LinkedModeUI;
 import org.eclipse.jface.text.link.LinkedPosition;
 import org.eclipse.jface.text.link.LinkedPositionGroup;
 import org.eclipse.jface.text.source.SourceViewer;
 import org.eclipse.swt.custom.VerifyKeyListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.text.edits.TextEdit;
 import org.eclipse.ui.statushandlers.StatusManager;
 import org.eclipse.ui.texteditor.ITextEditorExtension3;
 
 import de.walware.ecommons.collections.ConstList;
 import de.walware.ecommons.ltk.LTK;
 import de.walware.ecommons.ltk.ui.sourceediting.ISmartInsertSettings;
 import de.walware.ecommons.ltk.ui.sourceediting.ISmartInsertSettings.TabAction;
 import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
 import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorAddon;
 import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
 import de.walware.ecommons.preferences.IPreferenceAccess;
 import de.walware.ecommons.text.IPartitionConstraint;
 import de.walware.ecommons.text.ITokenScanner;
 import de.walware.ecommons.text.IndentUtil;
 import de.walware.ecommons.text.PartitioningConfiguration;
 import de.walware.ecommons.text.StringParseInput;
 import de.walware.ecommons.text.TextUtil;
 import de.walware.ecommons.ui.ISettingsChangedHandler;
 import de.walware.ecommons.ui.util.UIAccess;
 
 import de.walware.docmlet.tex.core.ITexCoreAccess;
 import de.walware.docmlet.tex.core.TexCodeStyleSettings;
 import de.walware.docmlet.tex.core.ast.LtxParser;
 import de.walware.docmlet.tex.core.ast.SourceComponent;
 import de.walware.docmlet.tex.core.parser.NowebLtxLexer;
 import de.walware.docmlet.tex.core.sourcecode.LtxSourceIndenter;
 import de.walware.docmlet.tex.core.text.ITexDocumentConstants;
 import de.walware.docmlet.tex.core.text.LtxHeuristicTokenScanner;
 import de.walware.docmlet.tex.internal.ui.TexUIPlugin;
 import de.walware.docmlet.tex.internal.ui.editors.HardLineWrap;
 import de.walware.docmlet.tex.internal.ui.editors.TexBracketLevel;
 import de.walware.docmlet.tex.ui.editors.TexEditorOptions;
 
 
 /**
  * Auto edit strategy for TeX code
  */
 public class LtxAutoEditStrategy extends DefaultIndentLineAutoEditStrategy
 		implements ISourceEditorAddon {
 	
 	
 	protected static class SmartInsertSettings implements ISettingsChangedHandler {
 		
 		private final ITexCoreAccess fCoreAccess;
 		
 		private boolean fByDefaultEnabled;
 		private TabAction fTabAction;
 		private boolean fCloseBrackets;
 		private boolean fCloseParenthesis;
 		private boolean fCloseMathDollar;
 		
 		private boolean fWrapLines;
 		
 		
 		public SmartInsertSettings(final ITexCoreAccess coreAccess) {
 			fCoreAccess = coreAccess;
 		}
 		
 		
 		@Override
 		public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
 			if (groupIds.contains(TexEditorOptions.SMARTINSERT_GROUP_ID)) {
 				updateSettings();
 			}
 		}
 		
 		protected void updateSettings() {
 			final IPreferenceAccess prefs = fCoreAccess.getPrefs();
 			fByDefaultEnabled = prefs.getPreferenceValue(TexEditorOptions.SMARTINSERT_BYDEFAULT_ENABLED_PREF);
 			fTabAction = prefs.getPreferenceValue(TexEditorOptions.SMARTINSERT_TAB_ACTION_PREF);
 			fCloseBrackets = prefs.getPreferenceValue(TexEditorOptions.SMARTINSERT_CLOSEBRACKETS_ENABLED_PREF);
 			fCloseParenthesis = prefs.getPreferenceValue(TexEditorOptions.SMARTINSERT_CLOSEPARENTHESIS_ENABLED_PREF);
 			fCloseMathDollar = prefs.getPreferenceValue(TexEditorOptions.SMARTINSERT_CLOSEMATHDOLLAR_ENABLED_PREF);
 			fWrapLines = prefs.getPreferenceValue(TexEditorOptions.SMARTINSERT_HARDWRAP_TEXT_ENABLED_PREF);
 		}
 		
 	}
 	
 	
 	private class RealTypeListener implements VerifyKeyListener {
 		@Override
 		public void verifyKey(final VerifyEvent event) {
 			if (!event.doit) {
 				return;
 			}
 			switch (event.character) {
 			case '{':
 			case '(':
 			case '[':
 			case '$':
 				event.doit = !customizeKeyPressed(event.character);
 				return;
 			case '\t':
 				if (event.stateMask == 0) {
 					event.doit = !customizeKeyPressed(event.character);
 				}
 				return;
 			case 0x0A:
 			case 0x0D:
 				if (fEditor3 != null) {
 					event.doit = !customizeKeyPressed('\n');
 				}
 				return;
 			default:
 				return;
 			}
 		}
 	};
 	
 	
 	protected final ISourceEditor fEditor;
 	protected final ITextEditorExtension3 fEditor3;
 	protected final SourceViewer fViewer;
 	protected final RealTypeListener fMyListener;
 	
 	protected final ITexCoreAccess fTexCoreAccess;
 	protected final SmartInsertSettings fSettings;
 	
 	private AbstractDocument fDocument;
 	private IRegion fValidRange;
 	private LtxHeuristicTokenScanner fScanner;
 	private IPartitionConstraint fLtxOrMathConstraint;
 	private TexCodeStyleSettings fTexCodeStyle;
 	private LtxSourceIndenter fIndenter;
 	
 	private boolean fIgnoreCommands = false;
 	
 	
 	public LtxAutoEditStrategy(final ITexCoreAccess coreAccess, final ISourceEditor editor) {
 		assert (coreAccess != null);
 		assert (editor != null);
 		
 		fTexCoreAccess = coreAccess;
 		fEditor = editor;
 		fSettings = new SmartInsertSettings(coreAccess);
 //		assert (fOptions != null);
 		
 		fViewer = fEditor.getViewer();
 		fEditor3 = (editor instanceof SourceEditor1) ? (SourceEditor1) editor : null;
 		fMyListener = new RealTypeListener();
 		
 	}
 	
 	
 	@Override
 	public void install(final ISourceEditor editor) {
 		assert (editor.getViewer() == fViewer);
 		fViewer.prependVerifyKeyListener(fMyListener);
 		fSettings.updateSettings();
 	}
 	
 	@Override
 	public void uninstall() {
 		fViewer.removeVerifyKeyListener(fMyListener);
 	}
 	
 	
 	private final boolean initCustomization(final int offset, final int c) {
 		assert(fDocument != null);
 		if (fScanner == null) {
 			fScanner = createScanner();
 			fLtxOrMathConstraint = ITexDocumentConstants.LTX_DEFAULT_OR_MATH_CONSTRAINT;
 		}
 		fTexCodeStyle = fTexCoreAccess.getTexCodeStyle();
 		fValidRange = getValidRange(offset, c);
 		return (fValidRange != null);
 	}
 	
 	protected LtxHeuristicTokenScanner createScanner() {
 		return (LtxHeuristicTokenScanner) LTK.getModelAdapter(fEditor.getModelTypeId(),
 				LtxHeuristicTokenScanner.class );
 	}
 	
 	protected IRegion getValidRange(final int offset, final int c) {
 		return new Region(0, fDocument.getLength());
 	}
 	
 	protected final IDocument getDocument() {
 		return fDocument;
 	}
 	
 	private final void quitCustomization() {
 		fDocument = null;
 		fTexCodeStyle = null;
 	}
 	
 	
 	private final boolean isSmartInsertEnabled() {
 		return ((fEditor3 != null) ?
 				(fEditor3.getInsertMode() == ITextEditorExtension3.SMART_INSERT) :
 				fSettings.fByDefaultEnabled);
 	}
 	
 	private final boolean isClosedBracket(final int backwardOffset, final int forwardOffset,
 			final String currentPartition, final int searchType) {
 		int[] balance = new int[3];
 		balance[searchType]++;
 		fScanner.configure(fDocument, currentPartition);
 		balance = fScanner.computeBracketBalance(backwardOffset, forwardOffset, balance, searchType);
 		return (balance[searchType] <= 0);
 	}
 	
 	private boolean isCharAt(final int offset, final char c) throws BadLocationException {
 		return (offset >= fValidRange.getOffset() && offset < fValidRange.getOffset()+fValidRange.getLength()
 				&& fDocument.getChar(offset) == c);
 	}
 	
 	private boolean isValueChar(final int offset) throws BadLocationException {
 		if (offset >= fValidRange.getOffset() && offset < fValidRange.getOffset()+fValidRange.getLength()) {
 			final int c = fDocument.getChar(offset);
 			return (Character.isLetterOrDigit(c));
 		}
 		return false;
 	}
 	
 	private int countBackward(final char c, int offset) throws BadLocationException {
 		int count = 0;
 		while (--offset >= 0 && fDocument.getChar(offset) == c) {
 			count++;
 		}
 		return count;
 	}
 	
 	private int countForward(final char c, int offset) throws BadLocationException {
 		int count = 0;
 		final int length = fDocument.getLength();
 		while (offset < length && fDocument.getChar(offset++) == c) {
 			count++;
 		}
 		return count;
 	}
 	
 	
 	private final HardLineWrap fWrap = new HardLineWrap();
 	
 	@Override
 	public void customizeDocumentCommand(final IDocument d, final DocumentCommand c) {
 		if (fIgnoreCommands || c.doit == false || c.text == null) {
 			return;
 		}
		if (!isSmartInsertEnabled()) {
 			super.customizeDocumentCommand(d, c);
 			return;
 		}
 		fDocument = (AbstractDocument) d;
 		if (!initCustomization(c.offset, -1)) {
 			quitCustomization();
 			return;
 		}
 		try {
 			final PartitioningConfiguration partitioning = fScanner.getPartitioningConfig();
 			final ITypedRegion partition = fDocument.getPartition(partitioning.getPartitioning(), c.offset, true);
 			if (partitioning.getDefaultPartitionConstraint().matches(partition.getType())) {
 				if (c.length == 0 && TextUtilities.equals(d.getLegalLineDelimiters(), c.text) != -1) {
 					smartIndentOnNewLine(c);
 				}
 				else if (c.length == 0 && fSettings.fWrapLines) {
 					fWrap.doWrapB(fDocument, c, fTexCodeStyle.getLineWidth());
 				}
 			}
 		}
 		catch (final Exception e) {
 			StatusManager.getManager().handle(new Status(IStatus.ERROR, TexUIPlugin.PLUGIN_ID, -1,
 					"An error occurred when customizing action for document command in LaTeX auto edit strategy.", e )); //$NON-NLS-1$
 		}
 		finally {
 			quitCustomization();
 		}
 	}
 	
 	/**
 	 * Second main entry method for real single key presses.
 	 * 
 	 * @return <code>true</code>, if key was processed by method
 	 */
 	private boolean customizeKeyPressed(final char c) {
		if (!isSmartInsertEnabled() || !UIAccess.isOkToUse(fViewer)) {
 			return false;
 		}
 		fDocument = (AbstractDocument) fViewer.getDocument();
 		ITextSelection selection = (ITextSelection) fViewer.getSelection();
 		if (!initCustomization(selection.getOffset(), c)) {
 			quitCustomization();
 			return false;
 		}
 		fIgnoreCommands = true;
 		try {
 			final DocumentCommand command = new DocumentCommand() {};
 			command.offset = selection.getOffset();
 			command.length = selection.getLength();
 			command.doit = true;
 			command.shiftsCaret = true;
 			command.caretOffset = -1;
 			int linkedModeType = -1;
 			int linkedModeOffset = -1;
 			final int cEnd = command.offset+command.length;
 			
 			final ITypedRegion partition = fDocument.getPartition(
 					fEditor.getPartitioning().getPartitioning(), command.offset, true );
 			final String partitionType = partition.getType();
 			KEY: switch (c) {
 			case '\t':
 				if (fLtxOrMathConstraint.matches(partitionType)
 						|| partitionType == ITexDocumentConstants.LTX_COMMENT_CONTENT_TYPE
 						|| partitionType == ITexDocumentConstants.LTX_MATHCOMMENT_CONTENT_TYPE ) {
 					if (command.length == 0 || fDocument.getLineOfOffset(command.offset) == fDocument.getLineOfOffset(cEnd)) {
 						command.text = "\t"; //$NON-NLS-1$
 						switch (smartIndentOnTab(command)) {
 						case -1:
 							return false;
 						case 0:
 							break;
 						case 1:
 							break KEY;
 						}
 					}
 					if (fTexCodeStyle.getReplaceOtherTabsWithSpaces()) {
 						final IndentUtil indent = new IndentUtil(fDocument, fTexCodeStyle);
 						command.text = indent.createTabSpacesCompletionString(indent.getColumnAtOffset(command.offset));
 						break KEY;
 					}
 				}
 				return false;
 			case '{':
 				if (fLtxOrMathConstraint.matches(partitionType)
 						&& !LtxHeuristicTokenScanner.isEscaped(fDocument, command.offset) ) {
 					command.text = "{"; //$NON-NLS-1$
 					if (fSettings.fCloseBrackets && !isValueChar(cEnd)) {
 						if (!isClosedBracket(command.offset, cEnd, partitionType, CURLY_BRACKET_TYPE)) {
 							command.text = "{}"; //$NON-NLS-1$
 							linkedModeType = 2 | AUTODELETE;
 						}
 						else if (isCharAt(cEnd, '}')) {
 							linkedModeType = 2;
 						}
 					}
 					break KEY;
 				}
 				return false;
 			case '[':
 				if (fLtxOrMathConstraint.matches(partitionType)
 						&& !LtxHeuristicTokenScanner.isEscaped(fDocument, command.offset) ) {
 					command.text = "["; //$NON-NLS-1$
 					if (fSettings.fCloseBrackets && !isValueChar(cEnd)) {
 						if (!isClosedBracket(command.offset, cEnd, partitionType, SQUARE_BRACKET_TYPE)) {
 							command.text = "[]"; //$NON-NLS-1$
 							linkedModeType = 2 | AUTODELETE;
 						}
 						else if (isCharAt(cEnd, ']')) {
 							linkedModeType = 2;
 						}
 					}
 					break KEY;
 				}
 				return false;
 			case '(':
 				if (fLtxOrMathConstraint.matches(partitionType)
 						&& !LtxHeuristicTokenScanner.isEscaped(fDocument, command.offset) ) {
 					command.text = "("; //$NON-NLS-1$
 					if (fSettings.fCloseParenthesis && !isValueChar(cEnd)) {
 						if (!isClosedBracket(command.offset, cEnd, partitionType, PARATHESIS_TYPE)) {
 							command.text = "()"; //$NON-NLS-1$
 							linkedModeType = 2 | AUTODELETE;
 						}
 						else if (isCharAt(cEnd, ')')) {
 							linkedModeType = 2;
 						}
 					}
 					break KEY;
 				}
 				return false;
 			case '$':
 				if (partitionType == ITexDocumentConstants.LTX_MATH_CONTENT_TYPE
 						&& !LtxHeuristicTokenScanner.isEscaped(fDocument, command.offset) ) {
 					command.text = "$"; //$NON-NLS-1$
 					if (fSettings.fCloseMathDollar
 							&& partition.getOffset() == command.offset - 1
 							&& fDocument.getChar(command.offset - 1) == '$'
 							&& countForward('$', cEnd) == 1) {
 						command.text = "$$"; //$NON-NLS-1$
 						linkedModeType = 3 | AUTODELETE;
 						break KEY;
 					}
 				}
 				if ((fScanner.getPartitioningConfig().getDefaultPartitionConstraint().matches(partitionType))
 						&& !LtxHeuristicTokenScanner.isEscaped(fDocument, command.offset) ) {
 					command.text = "$"; //$NON-NLS-1$
 					if (fSettings.fCloseMathDollar && !isValueChar(cEnd)) {
 						command.text = "$$"; //$NON-NLS-1$
 						linkedModeType = 2 | AUTODELETE;
 					}
 					break KEY;
 				}
 				return false;
 			case '\n':
 				if (fLtxOrMathConstraint.matches(partitionType)
 						|| partitionType == ITexDocumentConstants.LTX_COMMENT_CONTENT_TYPE ) {
 					command.text = TextUtilities.getDefaultLineDelimiter(fDocument);
 					smartIndentOnNewLine(command);
 					break KEY;
 				}
 				return false;
 			default:
 				assert (false);
 				return false;
 			}
 			
 			if (command.text.length() > 0 && fEditor.isEditable(true)) {
 				fViewer.getTextWidget().setRedraw(false);
 				try {
 					fDocument.replace(command.offset, command.length, command.text);
 					final int cursor = (command.caretOffset >= 0) ? command.caretOffset :
 							command.offset+command.text.length();
 					selection = new TextSelection(fDocument, cursor, 0);
 					fViewer.setSelection(selection, true);
 					
 					if (linkedModeType >= 0) {
 						if (linkedModeOffset < 0) {
 							linkedModeOffset = command.offset;
 						}
 						createLinkedMode(linkedModeOffset, c, linkedModeType).enter();
 					}
 				}
 				finally {
 					fViewer.getTextWidget().setRedraw(true);
 				}
 			}
 			return true;
 		}
 		catch (final Exception e) {
 			StatusManager.getManager().handle(new Status(IStatus.ERROR, TexUIPlugin.PLUGIN_ID, -1,
 					"An error occurred when customizing action for pressed key in LaTeX auto edit strategy.", e )); //$NON-NLS-1$
 		}
 		finally {
 			fIgnoreCommands = false;
 			quitCustomization();
 		}
 		return false;
 	}
 	
 	
 	private int smartIndentOnTab(final DocumentCommand c) throws BadLocationException {
 		final IRegion line = fDocument.getLineInformation(fDocument.getLineOfOffset(c.offset));
 		int first;
 		fScanner.configure(fDocument);
 		first = fScanner.findAnyNonBlankBackward(c.offset, line.getOffset()-1, false);
 		if (first != ITokenScanner.NOT_FOUND) { // not first char
 			return 0;
 		}
 		final IndentUtil indentation = new IndentUtil(fDocument, fTexCodeStyle);
 		final int column = indentation.getColumnAtOffset(c.offset);
 		if (fSettings.fTabAction != ISmartInsertSettings.TabAction.INSERT_TAB_CHAR) {
 			c.text = indentation.createIndentCompletionString(column);
 		}
 		return 1;
 	}
 	
 	private void smartIndentOnNewLine(final DocumentCommand c) throws BadLocationException, BadPartitioningException, CoreException {
 		final IRegion line = fDocument.getLineInformationOfOffset(c.offset);
 		int backward = c.offset;
 		final ITypedRegion partition = fDocument.getPartition(fEditor.getPartitioning().getPartitioning(), backward, true);
 		if (partition.getType() == ITexDocumentConstants.LTX_COMMENT_CONTENT_TYPE
 				|| partition.getType() == ITexDocumentConstants.LTX_MATHCOMMENT_CONTENT_TYPE) {
 			backward = partition.getOffset();
 		}
 		int forward;
 		fScanner.configure(fDocument);
 		if (backward >= line.getOffset()
 				&& (forward = fScanner.findAnyNonBlankForward(c.offset + c.length, LtxHeuristicTokenScanner.UNBOUND, false)) != ITokenScanner.NOT_FOUND
 				&& forward + 6 < fDocument.getLength()
 				&& fDocument.get(forward, 4).equals("\\end")
 				&& (backward = fScanner.findAnyNonBlankBackward(backward, line.getOffset(), false)) != ITokenScanner.NOT_FOUND
 				&& fDocument.getChar(backward) == '}'
 				&& (backward = fScanner.scanBackward(backward, line.getOffset() - 1, '\\')) != ITokenScanner.NOT_FOUND
 				&& (backward == 0 || fDocument.getChar(backward - 1) != '\\')
 				&& fDocument.get(backward + 1, 5).equals("begin") ) {
 			c.text = c.text+c.text;
 		}
 		
 		smartIndentLine2(c, false, 1, null);
 	}
 	
 	private int searchParseStart(final int offset) throws BadLocationException, BadPartitioningException {
 		final ITypedRegion partition = fDocument.getPartition(fEditor.getPartitioning().getPartitioning(), offset, false);
 		if (partition.getType() == ITexDocumentConstants.LTX_DEFAULT_CONTENT_TYPE
 				|| partition.getType() == ITexDocumentConstants.LTX_DEFAULT_EXPL_CONTENT_TYPE) {
 			return offset;
 		}
 		if (partition.getType() == ITexDocumentConstants.LTX_MATH_CONTENT_TYPE
 				|| partition.getType() == ITexDocumentConstants.LTX_MATHCOMMENT_CONTENT_TYPE ) {
 			return LtxHeuristicTokenScanner.getSafeMathPartitionOffset(
 					fDocument.getDocumentPartitioner(fEditor.getPartitioning().getPartitioning()), offset );
 		}
 		if (partition.getType() == ITexDocumentConstants.LTX_COMMENT_CONTENT_TYPE
 				|| partition.getType() == ITexDocumentConstants.LTX_VERBATIM_CONTENT_TYPE ) {
 			return partition.getOffset() + partition.getLength();
 		}
 		return -1;
 	}
 	
 	private final boolean endsWithNewLine(final String text) {
 		for (int i = text.length()-1; i >= 0; i--) {
 			final char c = text.charAt(i);
 			if (c == '\r' || c == '\n') {
 				return true;
 			}
 			if (c != ' ' && c != '\t') {
 				return false;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Generic method to indent lines using the 
 	 * @param c handle to read and save the document informations
 	 * @param indentCurrentLine
 	 * @param setCaret positive values indicates the line to set the caret
 	 * @param traceCursor offset to update and return (offset at state after insertion of c.text)
 	 */
 	private Position[] smartIndentLine2(final DocumentCommand c, final boolean indentCurrentLine,
 			final int setCaret, final Position[] tracePos) throws BadLocationException, BadPartitioningException, CoreException {
 		if (fEditor3 == null) {
 			return tracePos;
 		}
 		final IRegion validRegion = fValidRange;
 		
 		// new algorithm using RSourceIndenter
 		final int cEnd = c.offset+c.length;
 		if (cEnd > validRegion.getOffset()+validRegion.getLength()) {
 			return tracePos;
 		}
 		fScanner.configure(fDocument);
 		final int smartEnd;
 		final String smartAppend;
 		if (endsWithNewLine(c.text)) {
 			final IRegion cEndLine = fDocument.getLineInformationOfOffset(cEnd);
 			final int validEnd = (cEndLine.getOffset()+cEndLine.getLength() <= validRegion.getOffset()+validRegion.getLength()) ?
 					cEndLine.getOffset()+cEndLine.getLength() : validRegion.getOffset()+validRegion.getLength();
 			final int next = fScanner.findAnyNonBlankForward(cEnd, validEnd, false);
 			smartEnd = (next >= 0) ? next : validEnd;
 			smartAppend = ""; //$NON-NLS-1$
 		}
 		else {
 			smartEnd = cEnd;
 			smartAppend = ""; //$NON-NLS-1$
 		}
 		
 		int shift = 0;
 		if (c.offset < validRegion.getOffset()
 				|| c.offset > validRegion.getOffset()+validRegion.getLength()) {
 			return tracePos;
 		}
 		if (c.offset > 2500) {
 			final int line = fDocument.getLineOfOffset(c.offset) - 40;
 			if (line >= 10) {
 				final int lineOffset = fDocument.getLineOffset(line);
 				shift = searchParseStart(lineOffset);
 			}
 		}
 		if (shift < validRegion.getOffset()) {
 			shift = validRegion.getOffset();
 		}
 		int dummyDocEnd = cEnd+1500;
 		if (dummyDocEnd > validRegion.getOffset()+validRegion.getLength()) {
 			dummyDocEnd = validRegion.getOffset()+validRegion.getLength();
 		}
 		final String text;
 		{	final StringBuilder s = new StringBuilder(
 					(c.offset-shift) +
 					c.text.length() +
 					(smartEnd-cEnd) +
 					smartAppend.length() +
 					(dummyDocEnd-smartEnd) );
 			s.append(fDocument.get(shift, c.offset-shift));
 			s.append(c.text);
 			if (smartEnd-cEnd > 0) {
 				s.append(fDocument.get(cEnd, smartEnd-cEnd));
 			}
 			s.append(smartAppend);
 			s.append(fDocument.get(smartEnd, dummyDocEnd-smartEnd));
 			text = s.toString();
 		}
 		
 		// Create temp doc to compute indent
 		int dummyCoffset = c.offset-shift;
 		int dummyCend = dummyCoffset+c.text.length();
 		final AbstractDocument dummyDoc = new Document(text);
 		final StringParseInput parseInput = new StringParseInput(text);
 		
 		// Lines to indent
 		int dummyFirstLine = dummyDoc.getLineOfOffset(dummyCoffset);
 		final int dummyLastLine = dummyDoc.getLineOfOffset(dummyCend);
 		if (!indentCurrentLine) {
 			dummyFirstLine++;
 		}
 		if (dummyFirstLine > dummyLastLine) {
 			return tracePos;
 		}
 		
 		// Compute indent
 		final LtxParser scanner = new LtxParser(new NowebLtxLexer());
 		final SourceComponent rootNode = scanner.parse(parseInput, fTexCoreAccess.getTexCommandSet());
 		if (fIndenter == null) {
 			fIndenter = new LtxSourceIndenter();
 		}
 		fIndenter.setup(fTexCoreAccess);
 		final TextEdit edit = fIndenter.getIndentEdits(dummyDoc, rootNode, 0, dummyFirstLine, dummyLastLine);
 		
 		// Apply indent to temp doc
 		final Position cPos = new Position(dummyCoffset, c.text.length());
 		dummyDoc.addPosition(cPos);
 		if (tracePos != null) {
 			for (int i = 0; i < tracePos.length; i++) {
 				tracePos[i].offset -= shift;
 				dummyDoc.addPosition(tracePos[i]);
 			}
 		}
 		
 		c.length = c.length+edit.getLength()
 				// add space between two replacement regions
 				// minus overlaps with c.text
 				-TextUtil.overlaps(edit.getOffset(), edit.getExclusiveEnd(), dummyCoffset, dummyCend);
 		if (edit.getOffset() < dummyCoffset) { // move offset, if edit begins before c
 			dummyCoffset = edit.getOffset();
 			c.offset = shift+dummyCoffset;
 		}
 		edit.apply(dummyDoc, TextEdit.NONE);
 		
 		// Read indent for real doc
 		int dummyChangeEnd = edit.getExclusiveEnd();
 		dummyCend = cPos.getOffset()+cPos.getLength();
 		if (!cPos.isDeleted && dummyCend > dummyChangeEnd) {
 			dummyChangeEnd = dummyCend;
 		}
 		c.text = dummyDoc.get(dummyCoffset, dummyChangeEnd-dummyCoffset);
 		if (setCaret != 0) {
 			c.caretOffset = shift+fIndenter.getNewIndentOffset(dummyFirstLine+setCaret-1);
 			c.shiftsCaret = false;
 		}
 		fIndenter.clear();
 		if (tracePos != null) {
 			for (int i = 0; i < tracePos.length; i++) {
 				tracePos[i].offset += shift;
 			}
 		}
 		return tracePos;
 	}
 	
 	private LinkedModeUI createLinkedMode(final int offset, final char type, final int mode)
 			throws BadLocationException {
 		final LinkedModeModel model = new LinkedModeModel();
 		int pos = 0;
 		
 		final LinkedPositionGroup group = new LinkedPositionGroup();
 		final LinkedPosition position = TexBracketLevel.createPosition(type, fDocument,
 				offset + 1, 0, pos++ );
 		group.addPosition(position);
 		model.addGroup(group);
 		
 		model.forceInstall();
 		
 		final TexBracketLevel level = new TexBracketLevel(fDocument,
 				fScanner.getPartitioningConfig().getPartitioning(),
 				new ConstList<LinkedPosition>(position), (mode & 0xffff0000) );
 		
 		/* create UI */
 		final LinkedModeUI ui = new LinkedModeUI(model, fViewer);
 		ui.setCyclingMode(LinkedModeUI.CYCLE_NEVER);
 		ui.setExitPosition(fViewer, offset + (mode & 0xff), 0, pos);
 		ui.setSimpleMode(true);
 		ui.setExitPolicy(level);
 		return ui;
 	}
 	
 }
