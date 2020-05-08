 package util;
 
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 
 import org.apache.commons.io.output.ByteArrayOutputStream;
 import org.eclipse.mylyn.internal.wikitext.textile.core.block.CodeBlock;
 import org.eclipse.mylyn.internal.wikitext.textile.core.block.FootnoteBlock;
 import org.eclipse.mylyn.internal.wikitext.textile.core.block.PreformattedBlock;
 import org.eclipse.mylyn.internal.wikitext.textile.core.block.QuoteBlock;
 import org.eclipse.mylyn.internal.wikitext.textile.core.phrase.EscapeTextilePhraseModifier;
 import org.eclipse.mylyn.internal.wikitext.textile.core.phrase.HyperlinkPhraseModifier;
 import org.eclipse.mylyn.internal.wikitext.textile.core.phrase.ImageTextilePhraseModifier;
 import org.eclipse.mylyn.internal.wikitext.textile.core.phrase.SimpleTextilePhraseModifier;
 import org.eclipse.mylyn.internal.wikitext.textile.core.phrase.SimpleTextilePhraseModifier.Mode;
 import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
 import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
 import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
 import org.eclipse.mylyn.wikitext.core.parser.markup.Block;
 import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
 import org.eclipse.mylyn.wikitext.core.parser.markup.phrase.HtmlEndTagPhraseModifier;
 import org.eclipse.mylyn.wikitext.core.parser.markup.phrase.HtmlStartTagPhraseModifier;
 import org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;
 
 public class WikiUtil {
 	public static String parse(String text) throws UnsupportedEncodingException {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
		HtmlDocumentBuilder builder = new HtmlDocumentBuilder(new OutputStreamWriter(out));
 		builder.setEmitAsDocument(false);
 		MarkupParser parser = new MarkupParser();
 		parser.setMarkupLanguage(textileLanguage);
 		parser.setBuilder(builder);
 		parser.parse(text);
		return out.toString("GBK");
 	}
 	
 	private static MarkupLanguage textileLanguage = new TextileLanguage() {
 		@Override
 		protected void addStandardPhraseModifiers(PatternBasedSyntax phraseModifierSyntax) {
 			boolean escapingHtml = configuration == null ? false : configuration.isEscapingHtmlAndXml();
 
 			phraseModifierSyntax.add(new HtmlEndTagPhraseModifier(escapingHtml));
 			phraseModifierSyntax.add(new HtmlStartTagPhraseModifier(escapingHtml));
 			phraseModifierSyntax.beginGroup("(?:(?<=[\\s\\.,\\\"'?!;:\\)\\(\\{\\}\\[\\]])|^)(?:", 0); //$NON-NLS-1$
 			phraseModifierSyntax.add(new EscapeTextilePhraseModifier());
 			phraseModifierSyntax.add(new SimpleTextilePhraseModifier("**", SpanType.BOLD, Mode.NESTING)); //$NON-NLS-1$
 //			phraseModifierSyntax.add(new SimpleTextilePhraseModifier("??", SpanType.CITATION, Mode.NESTING)); //$NON-NLS-1$
 			phraseModifierSyntax.add(new SimpleTextilePhraseModifier("__", SpanType.ITALIC, Mode.NESTING)); //$NON-NLS-1$
 //			phraseModifierSyntax.add(new SimpleTextilePhraseModifier("_", SpanType.EMPHASIS, Mode.NESTING)); //$NON-NLS-1$
 			phraseModifierSyntax.add(new SimpleTextilePhraseModifier("*", SpanType.STRONG, Mode.NESTING)); //$NON-NLS-1$
 //			phraseModifierSyntax.add(new SimpleTextilePhraseModifier("+", SpanType.INSERTED, Mode.NESTING)); //$NON-NLS-1$
 			phraseModifierSyntax.add(new SimpleTextilePhraseModifier("~", SpanType.SUBSCRIPT, Mode.NORMAL)); //$NON-NLS-1$
 			phraseModifierSyntax.add(new SimpleTextilePhraseModifier("^", SpanType.SUPERSCRIPT, Mode.NORMAL)); //$NON-NLS-1$
 //			phraseModifierSyntax.add(new SimpleTextilePhraseModifier("@", SpanType.CODE, Mode.SPECIAL)); //$NON-NLS-1$
 //			phraseModifierSyntax.add(new SimpleTextilePhraseModifier("%", SpanType.SPAN, Mode.NESTING)); //$NON-NLS-1$
 			phraseModifierSyntax.add(new SimpleTextilePhraseModifier("-", SpanType.DELETED, Mode.NESTING)); //$NON-NLS-1$
 			phraseModifierSyntax.add(new LinkTextilePhraseModifier("/view/%s")); //$NON-NLS-1$
 //			phraseModifierSyntax.add(new ImageTextilePhraseModifier());
 			phraseModifierSyntax.add(new HyperlinkPhraseModifier()); // hyperlinks are actually a phrase modifier see bug 283093
 			phraseModifierSyntax.endGroup(")(?=\\W|$)", 0); //$NON-NLS-1$
 		}
 		@Override
 		protected void addStandardBlocks(List<Block> blocks, List<Block> paragraphBreakingBlocks) {
 			blocks.add(new PreformattedBlock());
 //			blocks.add(new QuoteBlock());
 //			blocks.add(new CodeBlock());
 //			blocks.add(new FootnoteBlock());
 		}
 	};
 	
 	
 }
