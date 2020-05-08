 package com.eekboom.copy_as_html;
 
 import com.intellij.lang.Language;
 import com.intellij.lexer.Lexer;
 import com.intellij.openapi.actionSystem.*;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.editor.Document;
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.editor.HighlighterColors;
 import com.intellij.openapi.editor.colors.EditorColors;
 import com.intellij.openapi.editor.colors.EditorColorsScheme;
 import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
 import com.intellij.openapi.editor.markup.*;
 import com.intellij.openapi.fileTypes.SyntaxHighlighter;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.TextRange;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.tree.IElementType;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.datatransfer.Clipboard;
 import java.awt.event.KeyEvent;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.text.Format;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Pattern;
 
 public class CopyAsHtmlAction extends AnAction {
     private static final Logger LOGGER = Logger.getInstance(CopyAsHtmlAction.class.getName());
     private static final Format _colorFormat = new ColorFormat();
     private int _lineNumberCharCount;
     private CodeStyle _lineNoCodeStyle;
     private boolean _isStartOfLine;
     private int _lineNo;
     private boolean _showLineNos;
     private boolean _unindent;
     private int _highlightStartIndex;
     private Integer _fontSize;
     private String _tabText;
 
     public CopyAsHtmlAction() {
         super("Copy as HTML");
         KeyStroke ctrlShiftAKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, false);
         CustomShortcutSet shortcutSet = new CustomShortcutSet(ctrlShiftAKeyStroke);
         setShortcutSet(shortcutSet);
     }
 
     public void update(AnActionEvent e) {
         DataContext dataContext = e.getDataContext();
         Editor editor = (Editor) dataContext.getData(DataConstants.EDITOR);
         Presentation presentation = e.getPresentation();
         presentation.setEnabled(editor != null);
     }
 
     public void actionPerformed(AnActionEvent event) {
         // Look at EditorColors, HighlighterColors and CodeInsightColors for color definitions
         DataContext dataContext = event.getDataContext();
         Project project = (Project) dataContext.getData(DataConstants.PROJECT);
         Editor editor = (Editor) dataContext.getData(DataConstants.EDITOR);
         if (editor == null) {
             return;
         }
         TextRange textRange = Utils.getSelectedTextRange(editor);
 
         PsiFile psiFile = (PsiFile) dataContext.getData(DataConstants.PSI_FILE);
         EditorColorsScheme colorsScheme = editor.getColorsScheme();
 
         CodeStyle defaultCodeStyle = getDefaultCodeStyle(editor);
 
         Document document = editor.getDocument();
 
         Configuration configuration = Configuration.getInstance();
 
         int startLineNo = document.getLineNumber(textRange.getStartOffset()) + 1;
         int endLineNo = document.getLineNumber(textRange.getEndOffset() - 1) + 1;
         int maxLineNo = configuration.getLineNosStartAt1() ? (endLineNo - startLineNo + 1) : endLineNo;
         _lineNumberCharCount = (int) Math.ceil(Math.log(maxLineNo + 1) / Math.log(10));
         Color lineNosColor = colorsScheme.getColor(EditorColors.LINE_NUMBERS_COLOR);
         Color lineNosBackgroundColor = colorsScheme.getColor(EditorColors.LEFT_GUTTER_BACKGROUND);
         _lineNoCodeStyle = new CodeStyle(lineNosColor, lineNosBackgroundColor, false, false, null, null, null);
         _lineNo = configuration.getLineNosStartAt1() ? 0 : startLineNo;
         _isStartOfLine = true;
         _highlightStartIndex = 0;
         String lineNoType = configuration.getLineNoType();
         if (Configuration.LINE_NO_FOLLOW.equals(lineNoType)) {
             _showLineNos = editor.getSettings().isLineNumbersShown();
         }
         else {
             _showLineNos = Configuration.LINE_NO_ALWAYS.equals(lineNoType);
         }
 
         _unindent = configuration.isUnindent();
 
         String fontSizeType = configuration.getFontSizeType();
         _fontSize = null;
         if (Configuration.FONT_SIZE_FOLLOW.equals(fontSizeType)) {
             _fontSize = Integer.valueOf(colorsScheme.getEditorFontSize());
         }
        else if (Configuration.FONT_SIZE_FIXED.equals(fontSizeType)) {
             _fontSize = Integer.valueOf(configuration.getFontSize());
         }
         _tabText = null;
         if (configuration.isTabsToSpaces()) {
             _tabText = Utils.repeat(' ', configuration.getTabSize());
         }
 
         final String html = copyAsHtml(project, defaultCodeStyle, colorsScheme, document, psiFile, textRange);
         LOGGER.info(html);
         Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         int mimeTypes = configuration.getMimeTypes();
         ClipboardHelper.publish(systemClipboard, mimeTypes, html);
     }
 
     private CodeStyle getDefaultCodeStyle(Editor editor) {
         EditorColorsScheme colorsScheme = editor.getColorsScheme();
         Color backgroundColor = getBackgroundColor(editor);
         Color foregroundColor = getForegroundColor(editor);
 
         TextAttributes textAttributes = colorsScheme.getAttributes(HighlighterColors.TEXT);
         int fontType = textAttributes.getFontType();
 
         boolean isBold = (fontType & Font.BOLD) != 0;
         boolean isItalic = (fontType & Font.ITALIC) != 0;
         EffectType effectType = textAttributes.getEffectType();
         Color underlineColor =
                 EffectType.LINE_UNDERSCORE == effectType || EffectType.WAVE_UNDERSCORE == effectType ? textAttributes.getEffectColor() :
                 null;
         Color strikeThroughColor = EffectType.STRIKEOUT == effectType ? textAttributes.getEffectColor() : null;
         Color boxColor = EffectType.BOXED == effectType ? textAttributes.getEffectColor() : null;
         CodeStyle defaultCodeStyle = new CodeStyle(foregroundColor, backgroundColor, isBold, isItalic, underlineColor, strikeThroughColor,
                                                    boxColor);
         return defaultCodeStyle;
     }
 
     private Color getForegroundColor(Editor editor) {
         EditorColorsScheme colorsScheme = editor.getColorsScheme();
         TextAttributes textAttributes = colorsScheme.getAttributes(HighlighterColors.TEXT);
         Color foregroundColor = textAttributes.getForegroundColor();
         return foregroundColor;
     }
 
     private Color getBackgroundColor(Editor editor) {
         Document document = editor.getDocument();
         EditorColorsScheme colorsScheme = editor.getColorsScheme();
         TextAttributes textAttributes = colorsScheme.getAttributes(HighlighterColors.TEXT);
         boolean isWritable = document.isWritable();
         Color backgroundColor;
         if (isWritable) {
             backgroundColor = textAttributes.getBackgroundColor();
         }
         else {
             backgroundColor = colorsScheme.getColor(EditorColors.READONLY_BACKGROUND_COLOR);
         }
         if (backgroundColor == null) {
             backgroundColor = Color.WHITE;
         }
         return backgroundColor;
     }
 
     /**
      * The API to get a SyntaxHighlighter for a PsiFile changed form Irida (Idea 5.x) to Demetra (Idea 6.x) and then again to Diana (Idea
      * 8.x). Handle all of the ways via reflection.
      */
     private static SyntaxHighlighter getSyntaxHighlighter(Language language, Project project, VirtualFile virtualFile) {
         SyntaxHighlighter syntaxHighlighter;
         try {
             Class shf = Class.forName("com.intellij.openapi.fileTypes.SyntaxHighlighterFactory", true,
                                       CopyAsHtmlAction.class.getClassLoader());
             syntaxHighlighter = (SyntaxHighlighter) reflect("Can't get SyntaxHighlighter", shf, "getSyntaxHighlighter", null,
                                                             new Class[]{Language.class, Project.class, VirtualFile.class},
                                                             new Object[]{language, project, virtualFile});
         }
         catch (Exception noIdea8) {
             try {
                 syntaxHighlighter = (SyntaxHighlighter) reflect("Can't get SyntaxHighlighter", Language.class, "getSyntaxHighlighter",
                                                                 language,
                                                                 new Class[]{Project.class, VirtualFile.class},
                                                                 new Object[]{project, virtualFile});
             }
             catch (RuntimeException ignore) {
                 syntaxHighlighter = (SyntaxHighlighter) reflect("Can't get SyntaxHighlighter", Language.class, "getSyntaxHighlighter",
                                                                 language,
                                                                 new Class[]{Project.class}, new Object[]{project});
             }
         }
 
         return syntaxHighlighter;
     }
 
     private static Object reflect(String errorMessage, Class clazz, String methodName, Object object, Class[] parameterClasses,
                                   Object[] args)
     {
         try {
             Method method = clazz.getMethod(methodName, parameterClasses);
             Object result = method.invoke(object, args);
             return result;
         }
         catch (NoSuchMethodException e) {
             throw new RuntimeException(errorMessage, e);
         }
         catch (InvocationTargetException e) {
             throw new RuntimeException(errorMessage, e);
         }
         catch (IllegalAccessException e) {
             throw new RuntimeException(errorMessage, e);
         }
     }
 
     private String copyAsHtml(Project project, CodeStyle defaultCodeStyle, EditorColorsScheme colorsScheme,
                               Document document, PsiFile psiFile, TextRange textRange)
     {
         Language language = psiFile.getLanguage();
         VirtualFile virtualFile = psiFile.getVirtualFile();
 
         SyntaxHighlighter syntaxHighlighter = getSyntaxHighlighter(language, project, virtualFile);
         Lexer lexer = syntaxHighlighter.getHighlightingLexer();
         String text = psiFile.getText();
         int startOffset = textRange.getStartOffset();
         int endOffset = textRange.getEndOffset();
         if (endOffset > 0 && text.charAt(endOffset - 1) == '\n') {
             --endOffset;
         }
         StringBuffer buffer = new StringBuffer();
         CodeStyle currentCodeStyle = null;
 
         int commonWhiteSpacePrefixCount = _unindent ? getCommonWhiteSpacePrefixCount(text, startOffset, endOffset) : 0;
 
         List rangeHighlighters = getRangeHighlighters(project, document);
 
         Configuration configuration = Configuration.getInstance();
         buffer.append("<pre style=\"line-height: 100%;font-family:monospace;background-color:");
         buffer.append(_colorFormat.format(defaultCodeStyle.getBackgroundColor()));
         if (configuration.getAddBorder()) {
             buffer.append("; border-width:0.01mm; border-color:#000000; border-style:solid;");
         }
         if (configuration.isIncludePadding()) {
             buffer.append("padding:").append(configuration.getPadding()).append("px;");
         }
         if (_fontSize != null) {
             buffer.append("font-size:").append(_fontSize).append("pt;");
         }
         buffer.append("\">");
 
         lexer.start(text);
 
         IElementType tokenType;
         while ((tokenType = lexer.getTokenType()) != null) {
             int tokenStart = lexer.getTokenStart();
             int tokenEnd = lexer.getTokenEnd();
             if (tokenStart < endOffset && tokenEnd >= startOffset) {
                 tokenStart = Math.max(tokenStart, startOffset);
                 tokenEnd = Math.min(tokenEnd, endOffset);

                 StringBuffer tokenText = new StringBuffer();
                 tokenText.append(text, tokenStart, tokenEnd);
                 Utils.quoteForXml(tokenText);
 
                 CodeStyle codeStyle = getCodeStyle(defaultCodeStyle, syntaxHighlighter, colorsScheme, tokenType, tokenStart,
                                                    rangeHighlighters);
                 currentCodeStyle = appendToBuffer(buffer, currentCodeStyle, codeStyle, tokenText, commonWhiteSpacePrefixCount);
             }
             lexer.advance();
         }
         if (currentCodeStyle != null) {
             buffer.append(currentCodeStyle.endHtml());
         }
         buffer.append("</pre>");
 
         return new String(buffer);
     }
 
     private int getCommonWhiteSpacePrefixCount(String chars, int startOffset, int endOffset) {
         String text = chars.substring(startOffset, endOffset);
         String[] lines = LINE_PATTERN.split(text, -1);
 
         String whiteSpace = null;
 
         for (int i = 0; i < lines.length; i++) {
             String line = lines[i];
             if (isWhiteSpace(line)) {
                 continue;
             }
             String testWhiteSpace = getWhiteSpacePrefix(line);
             if (testWhiteSpace.length() == 0) {
                 return 0;
             }
             if (whiteSpace == null) {
                 whiteSpace = testWhiteSpace;
             }
             else if (testWhiteSpace.length() < whiteSpace.length()) {
                 if (whiteSpace.startsWith(testWhiteSpace)) {
                     whiteSpace = testWhiteSpace;
                 }
                 else {
                     return 0;
                 }
             }
             else if (!testWhiteSpace.startsWith(whiteSpace)) {
                 return 0;
             }
         }
 
         return whiteSpace == null ? 0 : whiteSpace.length();
     }
 
     private static boolean isWhiteSpace(String text) {
         for (int i = 0, length = text.length(); i < length; ++i) {
             if (!Character.isWhitespace(text.charAt(i))) {
                 return false;
             }
         }
         return true;
     }
 
     private String getWhiteSpacePrefix(String line) {
         int charIndex = 0;
         int length = line.length();
         while (length > charIndex && Character.isWhitespace(line.charAt(charIndex))) {
             ++charIndex;
         }
         return line.substring(0, charIndex);
     }
 
     private List getRangeHighlighters(Project project, Document document) {
        MarkupModel documentMarkupModel = DocumentMarkupModel.forDocument(document, project, false);
         RangeHighlighter[] highlighters = documentMarkupModel.getAllHighlighters();
         List rangeHighlighters = new ArrayList(highlighters.length);
         Configuration configuration = Configuration.getInstance();
         boolean includeWarningHighlights = configuration.getIncludeWarningHighlights();
         for (int i = 0; i < highlighters.length; i++) {
             RangeHighlighter highlighter = highlighters[i];
             int layer = highlighter.getLayer();
             if (layer == HighlighterLayer.SYNTAX || layer == HighlighterLayer.ADDITIONAL_SYNTAX
                 || includeWarningHighlights && (layer == HighlighterLayer.WARNING || layer == HighlighterLayer.ERROR))
             {
                 int highlighterStartOffset = highlighter.getStartOffset();
                 int highlighterEndOffset = highlighter.getEndOffset();
                 TextAttributes textAttributes = highlighter.getTextAttributes();
                 // Don't know why textAttributes can be null, but happen e.g. in class java.lang.String
                 if (textAttributes != null) {
                     RangeHighlight rangeHighlight = new RangeHighlight(highlighterStartOffset, highlighterEndOffset, textAttributes);
                     rangeHighlighters.add(rangeHighlight);
                 }
             }
         }
         Collections.sort(rangeHighlighters);
         return rangeHighlighters;
     }
 
     private static final Pattern LINE_PATTERN = Pattern.compile("\\n|\\r\\n|\\r");
 
     private CodeStyle appendToBuffer(StringBuffer buffer, CodeStyle oldCodeStyle, CodeStyle newCodeStyle, StringBuffer text,
                                      int whiteSpacePrefixCount)
     {
         String[] lines = LINE_PATTERN.split(text, -1);
         int lineCount = lines.length;
 
         for (int i = 0; i < lineCount; i++) {
             String line = lines[i];
             if (_isStartOfLine) {
                 if (_showLineNos) {
                     ++_lineNo;
                     oldCodeStyle = appendChunkToBuffer(buffer, oldCodeStyle, _lineNoCodeStyle, Utils.formatInt(_lineNo,
                                                                                                                _lineNumberCharCount) + " ");
                 }
                 if (line.length() >= whiteSpacePrefixCount) {
                     line = line.substring(whiteSpacePrefixCount);
                 }
                 _isStartOfLine = false;
             }
             if (_tabText != null) {
                 line = Utils.replaceAll(line, '\t', _tabText);
             }
 
             if (line.length() > 0) {
                 oldCodeStyle = appendChunkToBuffer(buffer, oldCodeStyle, newCodeStyle, line);
             }
             if (i != lineCount - 1) {
                 CodeStyle newLineCodeStyle = oldCodeStyle == null ? null :
                                              new CodeStyle(oldCodeStyle.getForegroundColor(), oldCodeStyle.getBackgroundColor(),
                                                            oldCodeStyle.isBold(),
                                                            oldCodeStyle.isItalic(), oldCodeStyle.getUnderlineColor(),
                                                            oldCodeStyle.getStrikeThroughColor(),
                                                            null);
                 oldCodeStyle = appendChunkToBuffer(buffer, oldCodeStyle, newLineCodeStyle, "\n");
                 _isStartOfLine = true;
             }
         }
         return oldCodeStyle;
     }
 
     private CodeStyle appendChunkToBuffer(StringBuffer buffer, CodeStyle oldCodeStyle, CodeStyle newCodeStyle, String line) {
         if (!Utils.equals(oldCodeStyle, newCodeStyle)) {
             if (oldCodeStyle != null) {
                 String endHtmlString = oldCodeStyle.endHtml();
                 buffer.append(endHtmlString);
             }
             if (newCodeStyle != null) {
                 String startHtml = newCodeStyle.startHtml();
                 buffer.append(startHtml);
             }
         }
         buffer.append(line);
         return newCodeStyle;
     }
 
     private CodeStyle getCodeStyle(CodeStyle defaultCodeStyle, SyntaxHighlighter syntaxHighlighter, EditorColorsScheme colorsScheme,
                                    IElementType tokenType, int tokenStart,
                                    List rangeHighlighters)
     {
         TextAttributesKey[] syntaxTextAttributeKeys = syntaxHighlighter.getTokenHighlights(tokenType);
         Color foregroundColor = defaultCodeStyle.getForegroundColor();
         Color backgroundColor = defaultCodeStyle.getBackgroundColor();
         boolean isBold = defaultCodeStyle.isBold();
         boolean isItalic = defaultCodeStyle.isItalic();
         Color underlineColor = defaultCodeStyle.getUnderlineColor();
         Color strikeThroughColor = defaultCodeStyle.getStrikeThroughColor();
         Color boxColor = defaultCodeStyle.getBoxColor();
         TextAttributes[] highlightTextAttributes = getTextAttributes(rangeHighlighters, tokenStart);
         ArrayList textAttributes = new ArrayList(syntaxTextAttributeKeys.length + highlightTextAttributes.length);
         for (int i = 0; i < syntaxTextAttributeKeys.length; i++) {
             TextAttributesKey tokenHighlight = syntaxTextAttributeKeys[i];
             TextAttributes attributes = colorsScheme.getAttributes(tokenHighlight);
             textAttributes.add(attributes);
         }
         textAttributes.addAll(Arrays.asList(highlightTextAttributes));
 
         for (int i = 0; i < textAttributes.size(); i++) {
             TextAttributes attributes = (TextAttributes) textAttributes.get(i);
             Color highlightForegroundColor = attributes.getForegroundColor();
             if (highlightForegroundColor != null) {
                 foregroundColor = highlightForegroundColor;
             }
             Color highlightBackgroundColor = attributes.getBackgroundColor();
             if (highlightBackgroundColor != null) {
                 backgroundColor = highlightBackgroundColor;
             }
             EffectType effectType = attributes.getEffectType();
             Color attributeUnderlineColor =
                     EffectType.LINE_UNDERSCORE == effectType || EffectType.WAVE_UNDERSCORE == effectType ? attributes.getEffectColor() :
                     null;
             if (attributeUnderlineColor != null) {
                 underlineColor = attributeUnderlineColor;
             }
             Color attributeStrikeThroughColor = EffectType.STRIKEOUT == effectType ? attributes.getEffectColor() : null;
             if (attributeStrikeThroughColor != null) {
                 strikeThroughColor = attributeStrikeThroughColor;
             }
             Color attributeBoxColor = EffectType.BOXED == effectType ? attributes.getEffectColor() : null;
             if (attributeBoxColor != null) {
                 boxColor = attributeBoxColor;
             }
 
             int fontType = attributes.getFontType();
             isBold |= (fontType & Font.BOLD) != 0;
             isItalic |= (fontType & Font.ITALIC) != 0;
         }
 
         return new CodeStyle(foregroundColor, backgroundColor, isBold, isItalic, underlineColor, strikeThroughColor, boxColor);
     }
 
     private TextAttributes[] getTextAttributes(List rangeHighlighters, int offset) {
         List textAttributes = new ArrayList();
         for (int i = _highlightStartIndex; i < rangeHighlighters.size(); i++) {
             RangeHighlight rangeHighlight = (RangeHighlight) rangeHighlighters.get(i);
             int startOffset = rangeHighlight.getStartOffset();
             int endOffset = rangeHighlight.getEndOffset();
             if (endOffset < offset) {
                 ++_highlightStartIndex;
             }
             if (startOffset <= offset && offset < endOffset) {
                 textAttributes.add(rangeHighlight.getTextAttributes());
             }
             if (startOffset > offset) {
                 break;
             }
         }
 
         return (TextAttributes[]) textAttributes.toArray(new TextAttributes[textAttributes.size()]);
     }
 }
