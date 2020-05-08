 package simplehtmlconverter.element;
 
 import org.w3c.dom.Node;
 
 import simplehtmlconverter.writer.IDocumentContext;
 
 import com.lowagie.text.DocumentException;
 
 public class DefaultContainerElementBuilder implements ElementBuilder {
 
 	@Override
 	public void process(Node node, IDocumentContext context) throws DocumentException {
		if (isParagraph()||!context.hasActiveParagraph()) {
 			context.getDocumentWriter().addParagraphToDoc(node);
 			context.pushParagraphInfo();
 			modifyParagraph(node, context);
 			context.getDocumentWriter().setPharagraphSettings(node);
			context.setHasActiveParagraph(true);
 		}
 
 		context.pushRangeInfo();
 		modifyPhrase(node, context);
 		context.getDocumentWriter().setPhraseSettings(node);
 	}
 
 	public void afterProcessChilds(Node node, IDocumentContext documentContext) {
 		documentContext.popRangeInfo();
 		if (isParagraph()) {
 			documentContext.popParagraphInfo();
			documentContext.setHasActiveParagraph(false);
 		}
 	}
 
 	protected void modifyParagraph(Node node, IDocumentContext documentContext) {
 	}
 
 	protected void modifyPhrase(Node node, IDocumentContext documentContext) {
 	}
 
 	protected boolean isParagraph() {
 		return false;
 	}
 
 }
