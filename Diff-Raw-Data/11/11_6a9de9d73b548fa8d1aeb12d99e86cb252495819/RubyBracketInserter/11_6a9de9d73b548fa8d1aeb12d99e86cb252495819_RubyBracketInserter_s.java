 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.ui.editor;
 
 import org.eclipse.dltk.internal.ui.editor.BracketInserter;
 import org.eclipse.dltk.internal.ui.editor.ScriptEditor;
 import org.eclipse.dltk.internal.ui.editor.ScriptEditor.BracketLevel;
 import org.eclipse.dltk.ruby.internal.ui.text.IRubyPartitions;
 import org.eclipse.dltk.ruby.internal.ui.text.ISymbols;
 import org.eclipse.dltk.ruby.internal.ui.text.RubyHeuristicScanner;
 import org.eclipse.dltk.ruby.internal.ui.text.RubyPreferenceInterpreter;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.BadPositionCategoryException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITypedRegion;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.jface.text.link.LinkedModeModel;
 import org.eclipse.jface.text.link.LinkedModeUI;
 import org.eclipse.jface.text.link.LinkedPosition;
 import org.eclipse.jface.text.link.LinkedPositionGroup;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.ui.texteditor.ITextEditorExtension3;
 import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;
 
 public class RubyBracketInserter extends BracketInserter {
 
 	RubyBracketInserter(ScriptEditor scriptEditor) {
 		super(scriptEditor);
 	}
 
 	public void verifyKey(VerifyEvent event) {
 		// early pruning to slow down normal typing as little as possible
 		if (!event.doit
 				|| editor.getInsertMode() != ITextEditorExtension3.SMART_INSERT)
 			return;
 		switch (event.character) {
 		case '(':
 		case '<':
 		case '[':
 		case '\'':
 		case '\"':
 		case ' ':
 		case '\r':
 		case '\n':
 			break;
 		default:
 			return;
 		}
 
 		final ISourceViewer sourceViewer = this.editor.getScriptSourceViewer();
 		IDocument document = sourceViewer.getDocument();
 
 		final Point selection = sourceViewer.getSelectedRange();
 		final int offset = selection.x;
 		final int length = selection.y;
 
 		try {
 			IRegion startLine = document.getLineInformationOfOffset(offset);
 			IRegion endLine = document.getLineInformationOfOffset(offset
 					+ length);
 
 			RubyHeuristicScanner scanner = new RubyHeuristicScanner(document);
 			int nextToken = scanner.nextToken(offset + length, endLine
 					.getOffset()
 					+ endLine.getLength());
 			String next = nextToken == ISymbols.TokenEOF ? null : document.get(
 					offset, scanner.getPosition() - offset).trim();
 			int prevToken = scanner.previousToken(offset - 1, startLine
 					.getOffset());
 			int prevTokenOffset = scanner.getPosition();
 			if (prevTokenOffset < 0)
 				prevTokenOffset = 0;
 			String previous = offset > 1 && prevToken == ISymbols.TokenEOF ? null
 					: document.get(prevTokenOffset, offset - prevTokenOffset)
 							.trim();
 			boolean hasPrefixContent = false;
 			boolean hasSuffixContent = false;
 			if (previous != null) {
 				int hasOffset = startLine.getOffset();
 				int hasLength = (prevTokenOffset - startLine.getOffset());
 				hasPrefixContent = ((hasLength > 0) && (document.get(hasOffset,
 						hasLength).trim().length() > 0));
 
 				hasOffset = (prevTokenOffset + previous.length() + 1);
 				hasLength = (startLine.getLength() - (hasOffset - startLine
 						.getOffset()));
 				hasSuffixContent = ((hasLength > 0)
 						&& ((hasOffset + hasLength) <= document.getLength()) && (document
 						.get(hasOffset, hasLength).trim().length() > 0));
 			}
 
 			switch (event.character) {
 			case '(':
 				if (!fCloseBrackets || nextToken == ISymbols.TokenLPAREN
 						|| nextToken == ISymbols.TokenIDENTIFIER
 						|| next != null && next.length() > 1)
 					return;
 				break;
 
 			case '<':
 				if (!(fCloseAngularBrackets && fCloseBrackets)
 						|| nextToken == ISymbols.TokenLESSTHAN
 						|| prevToken != ISymbols.TokenLBRACE
 						&& prevToken != ISymbols.TokenRBRACE
 						&& prevToken != ISymbols.TokenSEMICOLON
 						&& (prevToken != ISymbols.TokenIDENTIFIER || !isAngularIntroducer(previous))
 						&& prevToken != ISymbols.TokenEOF)
 					return;
 				break;
 
 			case '[':
 				if (!fCloseBrackets || nextToken == ISymbols.TokenIDENTIFIER
 						|| next != null && next.length() > 1)
 					return;
 				break;
 
 			case '\'':
 			case '"':
 				if (!fCloseStrings
 						|| nextToken == ISymbols.TokenIDENTIFIER
 						/* || prevToken == Symbols.TokenIDENT */|| next != null
 						&& next.length() > 1
 						|| previous != null
 						&& (previous.length() > 1 && previous.charAt(0) == event.character))
 					return;
 				break;
 
 			case ' ':
 			case '\r':
 			case '\n':
 				if (!"case".equals(previous) && !"class".equals(previous) //$NON-NLS-1$ //$NON-NLS-2$
 						&& !"def".equals(previous) && !"do".equals(previous) //$NON-NLS-1$ //$NON-NLS-2$
 						&& !"if".equals(previous) && !"module".equals(previous) //$NON-NLS-1$ //$NON-NLS-2$
 						&& !"unless".equals(previous) //$NON-NLS-1$
 						&& !"while".equals(previous)) //$NON-NLS-1$
 					return;
 				if ((hasPrefixContent && !"do".equals(previous)) //$NON-NLS-1$
 						|| hasSuffixContent)
 					return;
 				if ((prevTokenOffset + previous.length()) < (offset - 1))
 					return;
 				break;
 
 			default:
 				return;
 			}
 
 			int correctedOffset = (document.getLength() > 0 && document
 					.getLength() == offset) ? offset - 1 : offset;
 			ITypedRegion partition = TextUtilities.getPartition(document,
 					IRubyPartitions.RUBY_PARTITIONING, correctedOffset, true);
 			if (!IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType()))
 				return;
 
 			if (!this.editor.validateEditorInputState())
 				return;
 
 			final char character = event.character;
 			final String prefixString;
 			final String suffixString;
 			if (character == ' ' || character == '\r' || character == '\n') {
 				final String lineDelimiter = TextUtilities
 						.getDefaultLineDelimiter(document);
 				final String prevIndent = getLineIndent(document, startLine);
 				final String nextIndent = selectIndent();
 				if ((character == '\r') || (character == '\n')) {
 					prefixString = lineDelimiter + prevIndent + nextIndent;
 					suffixString = prevIndent + "end"; //$NON-NLS-1$
 				} else {
 					prefixString = ""; //$NON-NLS-1$
 					// TODO check balance and insert only if needed -- Alex Panchenko
 					// final int balance = RubyAutoEditStrategy.getBlockBalance(
 					// document, offset);
 					// if (balance > 0) {
 					suffixString = lineDelimiter + prevIndent + nextIndent
 							+ lineDelimiter + prevIndent + "end"; //$NON-NLS-1$
 					// } else if (balance == 0) {
 					// suffixString = ""; //$NON-NLS-1$
 					// } else {
 					// suffixString = lineDelimiter + prevIndent + nextIndent;
 					// }
 				}
 			} else {
 				prefixString = ""; //$NON-NLS-1$
 				suffixString = String.valueOf(getPeerCharacter(character));
 			}
 			final char closingCharacter = suffixString.length() > 0 ? suffixString
 					.charAt(0)
 					: 0;
 
			document.replace(offset, length, prefixString + character
					+ suffixString);
 
 			BracketLevel level = new ScriptEditor.BracketLevel();
 			fBracketLevelStack.push(level);
 
 			LinkedPositionGroup group = new LinkedPositionGroup();
 			group.addPosition(new LinkedPosition(document, offset + 1, 0,
 					LinkedPositionGroup.NO_STOP));
 
 			LinkedModeModel model = new LinkedModeModel();
 			model.addLinkingListener(this);
 			model.addGroup(group);
 			model.forceInstall();
 
 			// set up position tracking for our magic peers
 			if (fBracketLevelStack.size() == 1) {
 				document.addPositionCategory(CATEGORY);
 				document.addPositionUpdater(fUpdater);
 			}
 			if (character == ' ' || character == '\r' || character == '\n') {
 				level.fOffset = (offset - previous.length());
 				level.fLength = (previous.length() + prefixString.length() + 1 + suffixString
 						.length());
 
 				level.fFirstPosition = new Position(
 						(offset - previous.length()),
 						(previous.length() + prefixString.length()));
 				level.fSecondPosition = new Position((offset + prefixString
 						.length()), (suffixString.length() + 1));
 			} else {
 				level.fOffset = offset;
 				level.fLength = 2;
 
 				level.fFirstPosition = new Position(offset, 1);
 				level.fSecondPosition = new Position(offset + 1, 1);
 			}
 			document.addPosition(CATEGORY, level.fFirstPosition);
 			document.addPosition(CATEGORY, level.fSecondPosition);
 
 			level.fUI = new EditorLinkedModeUI(model, sourceViewer);
 			level.fUI.setSimpleMode(true);
 			level.fUI.setExitPolicy(this.editor.new ExitPolicy(
 					closingCharacter, getEscapeCharacter(closingCharacter),
 					fBracketLevelStack));
 			level.fUI.setExitPosition(sourceViewer, offset + 2, 0,
 					Integer.MAX_VALUE);
 			level.fUI.setCyclingMode(LinkedModeUI.CYCLE_NEVER);
 			level.fUI.enter();
 
 			IRegion newSelection = level.fUI.getSelectedRegion();
 			if (character == ' ' || character == '\r' || character == '\n') {
 				newSelection = new Region(
 						newSelection.getOffset()
 								+ prefixString.length()
 								- (((character == '\r') || (character == '\n')) ? 1
 										: 0), newSelection.getLength());
 			}
 			sourceViewer.setSelectedRange(newSelection.getOffset(),
 					newSelection.getLength());
 
 			event.doit = false;
 
 		} catch (BadLocationException e) {
 			DLTKUIPlugin.log(e);
 		} catch (BadPositionCategoryException e) {
 			DLTKUIPlugin.log(e);
 		}
 	}
 
 	private String selectIndent() {
 		return RubyPreferenceInterpreter.getDefault().getIndent();
 	}
 
 	private String getLineIndent(IDocument document, IRegion startLine)
 			throws BadLocationException {
 		int end = document.getLength();
 		for (int cnt = startLine.getOffset(), max = document.getLength(); cnt <= max; cnt++) {
 			if (!Character.isWhitespace(document.getChar(cnt))) {
 				end = cnt;
 
 				break;
 			}
 		}
 		return document.get(startLine.getOffset(), end - startLine.getOffset());
 	}
 }
