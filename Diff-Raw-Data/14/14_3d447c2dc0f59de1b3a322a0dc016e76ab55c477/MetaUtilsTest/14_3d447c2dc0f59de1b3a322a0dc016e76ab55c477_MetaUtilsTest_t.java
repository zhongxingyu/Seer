 package ch.sbs.utils.preptools;
 
 import static org.junit.Assert.assertEquals;
 
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.PlainDocument;
 
 import org.junit.Test;
 
 import ch.sbs.plugin.preptools.VFormActionHelper;
 
 public class MetaUtilsTest {
 
 	private final static String PLACE_HOLDER = "_____";
 
 	@Test
 	public void testMetaUtilsNoMeta() throws BadLocationException {
 		final StringBuilder sb = new StringBuilder();
 		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
 		sb.append("<dtbook xmlns:brl=\"http://www.daisy.org/z3986/2009/braille/\">\n");
 		sb.append("<head>");
 		sb.append(PLACE_HOLDER);
 		sb.append("</head>\n");
 		sb.append("<book>\n");
 		sb.append("</book></dtbook>\n");
 		final String TEMPLATE = sb.toString();
 		final Document document = makeDocument(TEMPLATE.replace(PLACE_HOLDER,
 				""));
 		MetaUtils.insertPrepToolInfo(document, VFormActionHelper.VFORM_TAG);
 		sb.setLength(0);
		sb.append("<meta name=\"prod:PrepTool:");
 		sb.append(VFormActionHelper.VFORM_TAG);
 		sb.append("\" content=\"done\"/>");
 		assertEquals(TEMPLATE.replace(PLACE_HOLDER, sb.toString()),
 				document.getText(0, document.getLength()));
 	}
 
 	@Test
 	public void testMetaUtilsWithMeta() throws BadLocationException {
 		final StringBuilder sb = new StringBuilder();
 		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
 		sb.append("<dtbook xmlns:brl=\"http://www.daisy.org/z3986/2009/braille/\">\n");
 		sb.append("<head>\n");
 		sb.append("<meta />\n");
 		sb.append(PLACE_HOLDER);
 		sb.append("</head>\n");
 		sb.append("<book>\n");
 		sb.append("</book></dtbook>\n");
 		final String TEMPLATE = sb.toString();
 		final Document document = makeDocument(TEMPLATE.replace(PLACE_HOLDER,
 				""));
 		MetaUtils.insertPrepToolInfo(document, VFormActionHelper.VFORM_TAG);
		assertEquals(
				TEMPLATE.replace(PLACE_HOLDER,
						"<meta name=\"prod:PrepTool:brl:v-form\" content=\"done\"/>\n"),
 				document.getText(0, document.getLength()));
 	}
 
 	@Test
 	public void testMetaUtilsWithMeta2() throws BadLocationException {
 		final StringBuilder sb = new StringBuilder();
 		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
 		sb.append("<dtbook xmlns:brl=\"http://www.daisy.org/z3986/2009/braille/\">\n");
 		sb.append("	<head>\n");
 		sb.append("		<meta name=\"dc:Title\" content=\"Hundenovelle\"/>\n");
 		sb.append(PLACE_HOLDER);
 		sb.append("	</head>\n");
 		sb.append("<book>\n");
 		sb.append("</book></dtbook>\n");
 		final String TEMPLATE = sb.toString();
 		final Document document = makeDocument(TEMPLATE.replace(PLACE_HOLDER,
 				""));
 		MetaUtils.insertPrepToolInfo(document, VFormActionHelper.VFORM_TAG);
		assertEquals(
				TEMPLATE.replace(PLACE_HOLDER,
						"		<meta name=\"prod:PrepTool:brl:v-form\" content=\"done\"/>\n"),
 				document.getText(0, document.getLength()));
 	}
 
 	private Document makeDocument(final String content)
 			throws BadLocationException {
 		final Document pd = new PlainDocument();
 		pd.insertString(0, content, null);
 		return pd;
 	}
 }
