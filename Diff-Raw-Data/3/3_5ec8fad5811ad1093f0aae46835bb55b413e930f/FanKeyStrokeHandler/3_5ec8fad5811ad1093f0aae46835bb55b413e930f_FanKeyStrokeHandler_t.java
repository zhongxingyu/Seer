 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.handlers;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Pattern;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Caret;
 import javax.swing.text.Document;
 import javax.swing.text.JTextComponent;
 import net.colar.netbeans.fan.FanLanguage;
 import net.colar.netbeans.fan.FanTokenID;
 import net.colar.netbeans.fan.parboiled.FanLexAstUtils;
 import net.colar.netbeans.fan.parboiled.FantomLexerTokens.TokenName;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.editor.BaseDocument;
 import org.netbeans.editor.Utilities;
 import org.netbeans.modules.csl.api.EditorOptions;
 import org.netbeans.modules.csl.api.KeystrokeHandler;
 import org.netbeans.modules.csl.api.OffsetRange;
 import org.netbeans.modules.csl.spi.GsfUtilities;
 import org.netbeans.modules.csl.spi.ParserResult;
 import org.netbeans.modules.editor.indent.api.IndentUtils;
 
 /**
  * Impl. of keystrokeHandler
  * Provides support for closing bracket/quote insertion and the likes
  * Also deals with trying to guess/insert proper indentation.
  * @author tcolar
  */
 public class FanKeyStrokeHandler implements KeystrokeHandler
 {
 	/* Array of special patterns that will cause increased intent on the next line
 	 * if/else/while/for/try/finaly/catch without blocks({})
 	 * Switch items (case / default)
 	 * Note .*?  = match anything NON-greedily
 	 */
 
 	public static Pattern[] INDENT_AFTER_PATTERNS =
 	{
 		// single line if
 		Pattern.compile("^\\s*if\\s*\\(.*?\\)[^{]*$"),
 		// single line else
 		Pattern.compile("^\\s*else\\s*(if\\s*\\(.*?\\))?[^{]*$"),
 		// switch: case / default
 		Pattern.compile("^\\s*case\\s*[^:]*:\\s*$"),
 		Pattern.compile("^\\s*default\\s*:\\s*$"),
 		// try catch finaly
 		Pattern.compile("^\\s*try\\s*$"),
 		Pattern.compile("^\\s*finally\\s*$"),
 		Pattern.compile("^\\s*catch(\\(.*?\\))?\\s*$"),
 		// while
 		Pattern.compile("^\\s*while\\s*\\(.*?\\)[^{]*$"),
 		// for
 		Pattern.compile("^\\s*for\\s*\\(.*?\\)[^{]*$"),
 	};
 	/**
 	 * Items that should indent one level less that line above
 	 * If not immeditaly following a bracket
 	 */
 	private static Pattern[] DEINDENT_PATTERNS =
 	{
 		Pattern.compile("^\\s*else"),
 		Pattern.compile("^\\s*catch"),
 		Pattern.compile("^\\s*finally"),
 		// those could follow a bracket? unlikely 
 		Pattern.compile("^\\s*case\\s*[^:]*:\\s*"),
 		Pattern.compile("^\\s*default\\s*:\\s*"),
 	};
 	// keep track of last auto-insertion (ie: closing brackets)
 	// so if user backspace right away we remove that as well.
 	private int lastInsertStart;
 	private int lastInsertSize;
 
 	@Override
 	public boolean beforeCharInserted(Document document, int caretOffset, JTextComponent target, char car) throws BadLocationException
 	{
 		lastInsertSize = 0; //reset at each keypressed
 		BaseDocument doc = (BaseDocument) document;
 		if (!isInsertMatchingEnabled(doc))
 		{
 			return false;
 		}
 
 		String toInsert = "";
 		char prev = ' ';
 		if (caretOffset > 0)
 		{
 			prev = doc.getText(caretOffset - 1, 1).charAt(0);
 		}
 		char next = ' ';
 		if (caretOffset < doc.getLength() - 1)
 		{
 			next = doc.getText(caretOffset, 1).charAt(0);
 		}
 		Token<? extends FanTokenID> token = FanLexAstUtils.getFanTokenAt(document, caretOffset);
 
 		// If User types "over" closing item we closed automaticaly, skip it
 		FanTokenID tokenId = token.id();
 		//String txt = token.text().toString().trim();
 		if (token != null)
 		{
 			//For str,uri : if backquoted -> don't skip
 			//System.out.println("prev: "+prev);
 			//System.out.println("next: "+next);
 			//System.out.println("car: "+car);
 			//System.out.println("tk: "+token.id().name());
 			if ((car == '"' && tokenId.matches(TokenName.STRS) && next == '"' && prev != '\\')
 					|| (car == '"' && tokenId.matches(TokenName.STRS) && next == '"' && prev != '\\')
 					|| (car == '`' && tokenId.matches(TokenName.URI) && next == '`' && prev != '\\')
 					|| //(car == '`' && ord == FanLexer.INC_URI && next=='`' && prev!='\\') ||
 					(car == '\'' && tokenId.matches(TokenName.CHAR_) && next == '\'')
 					|| (car == ']' && tokenId.matches(TokenName.SQ_BRACKET_R))
 					|| (car == ')' && tokenId.matches(TokenName.PAR_R))
 					|| (car == '}' && tokenId.matches(TokenName.BRACKET_R)))
 			{
 				// just skip the existing same characters
 				target.getCaret().setDot(caretOffset + 1);
 				return true;
 			}
 		}
 		// Same but dual characters
 		if (/*(car == '/' && prev=='*' && token.id().ordinal() == FanLexer.MULTI_COMMENT) ||*/(car == '>' && prev == '|' && tokenId.matches(TokenName.DSL)))
 		{
 			// remove previous char and then skip existing one
 			doc.remove(caretOffset - 1, 1);
 			target.getCaret().setDot(caretOffset + 1);
 			return true;
 		}
 
 		// If within those tokens, don't do anything special
 		if (tokenId.matches(TokenName.DSL)
 				|| tokenId.matches(TokenName.CHAR_)
 				|| tokenId.matches(TokenName.DOC)
 				|| tokenId.matches(TokenName.UNIXLINE)
 				|| tokenId.matches(TokenName.COMMENT)
 				|| //token.id().ordinal() == FanLexer.INC_COMMENT ||
 				//token.id().ordinal() == FanLexer.INC_DSL ||
 				//token.id().ordinal() == FanLexer.INC_STR ||
 				//token.id().ordinal() == FanLexer.INC_URI ||
 				//token.id().ordinal() == FanLexer.LINE_COMMENT ||
 				//token.id().ordinal() == FanLexer.MULTI_COMMENT ||
 				//token.id().ordinal() == FanLexer.QUOTSTR ||
 				tokenId.matches(TokenName.STRS)
 				|| tokenId.matches(TokenName.URI))
 		{
 			return false;
 		}
 
 		// Automatically add closing item/brace when opening one entered
 		switch (car)
 		{
 			case '{':
 				toInsert = "}";
 				break;
 			case '(':
 				toInsert = ")";
 				break;
 			case '[':
 				toInsert = "]";
 				break;
 			case '`':
 				toInsert = "`";
 				break;
 			case '\'':
 				toInsert = "'";
 				break;
 			case '"':
 				// If third quote in row(""") then don't close (quoted string)
 				if (caretOffset > 2)
 				{
 					String prev2 = doc.getText(caretOffset - 2, 2);
 					if (prev2.equals("\"\""))
 					{
 						break;
 					}
 				}
 				toInsert = "\"";
 				break;
 			// dual characters
 			case '|':
 				if (prev == '<')
 				{
 					toInsert = "|>";
 				}
 				break;
 			/*case '*':
 			if (prev=='/')
 			{
 			toInsert = "* /";
 			}
 			break;*/
 		}
 		// do the insertion job
 		if (toInsert.length() > 0)
 		{
 			doc.insertString(caretOffset, toInsert, null);
 			target.getCaret().setDot(caretOffset);
 			if (toInsert.length() > 0)
 			{
 				lastInsertStart = caretOffset;
 				lastInsertSize = toInsert.length();
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean afterCharInserted(Document document, int caretOffset, JTextComponent target, char car) throws BadLocationException
 	{
 		// deal with fixing identation for special cases (switch, if etc...)
 		BaseDocument doc = (BaseDocument) document;
 		int indentSize = IndentUtils.indentLevelSize(document);
 		int lineBegin = Utilities.getRowStart(doc, caretOffset);
 		int lineEnd = Utilities.getRowEnd(doc, caretOffset);
 		String line = doc.getText(lineBegin, lineEnd - lineBegin);
 		int above = -1;
 		String prevLine = "";
 		if (lineBegin > 1)
 		{
 			above = Utilities.getFirstNonEmptyRow(doc, lineBegin - 1, false);
 			int prevBegin = Utilities.getRowStart(doc, above);
 			int prevEnd = Utilities.getRowEnd(doc, above);
 			prevLine = doc.getText(prevBegin, prevEnd - prevBegin);
 		}
 		int indent = GsfUtilities.getLineIndent(doc, caretOffset);
 		int origIndent = indent;
 
 		// If user enters { after one of the identing patterns, we need to dedent
 		if (car == '{' && line.trim().startsWith("{") && above > -1)
 		{
 			for (int i = 0; i != INDENT_AFTER_PATTERNS.length; i++)
 			{
 				if (INDENT_AFTER_PATTERNS[i].matcher(prevLine).matches())
 				{
 					// fix indent to be same as prevline
 					indent = GsfUtilities.getLineIndent(doc, above);
 					break;
 				}
 			}
 		} else
 		{
 			//Check some special patterns, decrease indent of current line (ex - deafult:)
 			// except if immediatly following a closing bracket
 			if (!prevLine.trim().endsWith("}"))
 			{
 				for (int i = 0; i != DEINDENT_PATTERNS.length; i++)
 				{
 					if (DEINDENT_PATTERNS[i].matcher(line).matches())
 					{
 						if (above >= 0)
 						{
 							// start indent as same as previous non empty line
 							indent = GsfUtilities.getLineIndent(doc, above);
 						}
 						// and decrease indent (>=0)
 						indent = indent >= indentSize ? indent - indentSize : indent;
 						break;
 					}
 				}
 			}
 		}
 		// set the indentation
 		// this seem to be slow (calls lexer many times ?), so do only when needed
 		if(indent != origIndent)
 			GsfUtilities.setLineIndentation(doc, caretOffset, indent);
 		return false;
 	}
 
 	@Override
 	public boolean charBackspaced(Document document, int caretOffset, JTextComponent target, char car) throws BadLocationException
 	{
 		BaseDocument doc = (BaseDocument) document;
 		if (!isInsertMatchingEnabled(doc))
 		{
 			return false;
 		}
 
 		// If we just auto-added chars in beforeCharInserted() and the user
 		// press backspace right away(no moving), we remove them now.
 		if (lastInsertSize > 0 && caretOffset == lastInsertStart)
 		{
 			doc.remove(caretOffset, lastInsertSize);
 		}
 
 		return false;
 	}
 
 	@Override
 	public int beforeBreak(Document document, int caretOffset, JTextComponent target) throws BadLocationException
 	{
 		Token tkNext = FanLexAstUtils.getFanTokenAt(document, caretOffset);
 		int offset = caretOffset == 0 ? 0 : caretOffset - 1;
 		// Get token BEFORE line break
 		Token tk = FanLexAstUtils.getFanTokenAt(document, offset);
 		//If within DSL, STR, URI don't indent anything as they can be multiline
 		// unless they are complete (next token is !=)
 		if (tkNext != null && tk != null)
 		{
 			int ord = tk.id().ordinal();
 			int ord2 = tkNext.id().ordinal();
 			String nm = tk.id().name();
 			if ((nm.equals(TokenName.DSL.name()) && ord2 == ord)
 					| (nm.equals(TokenName.STRS.name()) && ord2 == ord)
 					| //(ord == FanLexer.QUOTSTR && ord2==ord) |
 					(nm.equals(TokenName.URI.name()) && ord2 == ord))
 			//ord == FanLexer.INC_DSL |
 			//ord == FanLexer.INC_STR |
 			//ord == FanLexer.INC_URI)
 			{
 				return -1;
 			}
 		}
 
 		// Deal with indentation
 		String NL =/*Character.LINE_SEPARATOR*/ "\n";
 		int indentSize = IndentUtils.indentLevelSize(document);
 		Caret caret = target.getCaret();
 		BaseDocument doc = (BaseDocument) document;
 
 		int lineBegin = Utilities.getRowStart(doc, caretOffset);
 		int lineEnd = Utilities.getRowEnd(doc, caretOffset);
 
 		int above = Utilities.getPositionAbove(target, caretOffset, 0);
 		int prevLineBegin = Utilities.getRowStart(doc, above);
 		int prevLineEnd = Utilities.getRowEnd(doc, above);
 
 		String line = null;
 		String prevLine = null;
 		if (lineBegin > -1 && lineEnd > lineBegin)
 		{
 			line = doc.getText(lineBegin, lineEnd - lineBegin);
 		}
 
 		if (prevLineBegin > -1 && prevLineEnd > prevLineBegin)
 		{
 			prevLine = doc.getText(prevLineBegin, prevLineEnd - prevLineBegin);
 		}
 
 		String lineHead = doc.getText(lineBegin, caretOffset - lineBegin);
 		String lineTail = doc.getText(caretOffset, lineEnd - caretOffset);
 
 		// standard indent (same as the line we pressed return on)
 		int indent = 0;
 		if (lineBegin > 0)
 		{
 			indent = IndentUtils.lineIndent(document, lineBegin);
 		}
 
 		int dotOffset = 0;
 		String insert = "";
 		if (line != null)
 		{
 			String trimmedLine = line.trim();
 			// If within doc -> insert the ** on next line
 			// insert only if the current doc line is not empty("**") - except for first one (empty ok)
 			boolean isFirstDocLine = prevLine == null || !prevLine.trim().startsWith("**");
 			if (trimmedLine.startsWith("**") && (isFirstDocLine || trimmedLine.length() > 3))
 			{
 				insert = "** ";
 			} else if (lineHead.trim().endsWith("{"))
 			{
 				if (lineTail.trim().startsWith("}"))
 				{
 					String extraIndent = IndentUtils.createIndentString(document, indent);
 					insert = NL + extraIndent;
 					// TODO: this probably not good if using spaces instead of tabs ??
 					dotOffset = -(1 + extraIndent.length());
 				}
 
 				indent += indentSize;
 			} else
 			{
 				// increase indent of next line for special patterns, see getSpecialIndentSize()
 				indent += indentSize * getSpecialIndentSize(lineHead);
 			}
 
 		}
 
 		// Do the insertion and the indent
 		String indentStr = IndentUtils.createIndentString(document, indent);
                //String str = doc.getText(0, doc.getText().length());
 		doc.insertString(caretOffset, indentStr + insert, null);
                //str = doc.getText(0, doc.getText().length());
 		caret.setDot(caretOffset);
 		return caretOffset + indentStr.length() + insert.length() + 1 + dotOffset;
 	}
 
 	/**
 	 * Helps finding mathcing opening/closing items (ex: {})
 	 * @param document
 	 * @param caretOffset
 	 * @return
 	 */
 	@Override
         @SuppressWarnings("unchecked")
 	public OffsetRange findMatching(
 			Document document, int caretOffset)
 	{
 		TokenSequence ts = FanLexAstUtils.getFanTokenSequence(document);
 		int searchOffset = 2; // start after rightToken
 
 		// Prefer matching the token to the right of caret
 		Token<? extends FanTokenID> token = FanLexAstUtils.getFanTokenAt(document, caretOffset + 1);
 		if (token == null)
 		{
 			// if rightToken is null, use left token
 			token = FanLexAstUtils.getFanTokenAt(document, caretOffset);
 			searchOffset =
 					1; // start after leftToken
 		} else
 		{
 			FanTokenID id = token.id();
 			// if rightToken is not 'matcheable', use left token
 			if (!id.matches(TokenName.PAR_L) && !id.matches(TokenName.PAR_R)
 					&& !id.matches(TokenName.SQ_BRACKET_L) && !id.matches(TokenName.SQ_BRACKET_R)
 					&& !id.matches(TokenName.BRACKET_L) && !id.matches(TokenName.BRACKET_R))
 			{
 				token = FanLexAstUtils.getFanTokenAt(document, caretOffset);
 				searchOffset =
 						1; // start after leftToken
 			}
 
 		}
 
 		if (token != null)
 		{
 			FanTokenID id = token.id();
 			//String txt = token.text().toString().trim();
 			//Ok, now try to find the matching token
 			if (id.matches(TokenName.PAR_L))
 			{
 				ts.move(caretOffset + searchOffset);// start after opening char
 				return FanLexAstUtils.findRangeFromOpening(document, ts, TokenName.PAR_L, TokenName.PAR_R);
 			}
 			else if (id.matches(TokenName.PAR_R))
 			{
 				ts.move(caretOffset + searchOffset - 1);// start before opening char (since going backward)
 				return FanLexAstUtils.findRangeFromClosing(document, ts, TokenName.PAR_L, TokenName.PAR_R);
 			}
 			else if (id.matches(TokenName.BRACKET_L))
 			{
 				ts.move(caretOffset + searchOffset);
 				return FanLexAstUtils.findRangeFromOpening(document, ts, TokenName.BRACKET_L, TokenName.BRACKET_R);
 			}
 			else if (id.matches(TokenName.BRACKET_R))
 			{
 				ts.move(caretOffset + searchOffset - 1);
 				return FanLexAstUtils.findRangeFromClosing(document, ts, TokenName.BRACKET_L, TokenName.BRACKET_R);
 			}
 			else if (id.matches(TokenName.SQ_BRACKET_L))
 			{
 				ts.move(caretOffset + searchOffset);
 				return FanLexAstUtils.findRangeFromOpening(document, ts, TokenName.SQ_BRACKET_L, TokenName.SQ_BRACKET_R);
 			}
 			else if (id.matches(TokenName.SQ_BRACKET_R))
 			{
 				ts.move(caretOffset + searchOffset - 1);
 				return FanLexAstUtils.findRangeFromClosing(document, ts, TokenName.SQ_BRACKET_L, TokenName.SQ_BRACKET_R);
 			}
 		}
 		//default - no match
 		return OffsetRange.NONE;
 	}
 
 	@Override
 	public List<OffsetRange> findLogicalRanges(ParserResult arg0, int arg1)
 	{
 		// not impl yet.
 		// what is this used for ?   - provide using FanStructureAnalyzer ??
 		return Collections.emptyList();
 	}
 
 	@Override
 	public int getNextWordOffset(Document arg0, int arg1, boolean arg2)
 	{
 		// not impl, default will be fine.
 		return -1;
 	}
 
 	/**
 	 * wether brcaket matching is turned on
 	 * @param doc
 	 * @return
 	 */
 	public boolean isInsertMatchingEnabled(BaseDocument doc)
 	{
 		// Default: true
 		EditorOptions options = EditorOptions.get(FanLanguage.FAN_MIME_TYPE);
 		if (options != null)
 		{
 			return options.getMatchBrackets();
 		}
 
 		return true;
 	}
 
 	/**
 	 * Return how much extra ident we will need for special patterns
 	 * such as for / if /case etc...
 	 * @param lineText
 	 * @return
 	 */
 	private int getSpecialIndentSize(String lineText)
 	{
 		for (int i = 0; i != INDENT_AFTER_PATTERNS.length; i++)
 		{
 			if (INDENT_AFTER_PATTERNS[i].matcher(lineText).matches())
 			{
 				return 1;
 			}
 
 		}
 		return 0;
 	}
 }
