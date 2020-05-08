 package simplehtmlconverter.element;
 
 import org.w3c.dom.Node;
 
 import simplehtmlconverter.writer.IDocumentContext;
 
 import com.lowagie.text.DocumentException;
 
 public class DefaultContainerElementBuilder implements ElementBuilder {
 
 	@Override
 	public void process(Node node, IDocumentContext context) throws DocumentException {
		if (isParagraph()) {
 			context.getDocumentWriter().addParagraphToDoc(node);
 			context.pushParagraphInfo();
 			modifyParagraph(node, context);
 			context.getDocumentWriter().setPharagraphSettings(node);
 		}
 
 		context.pushRangeInfo();
 		modifyPhrase(node, context);
 		context.getDocumentWriter().setPhraseSettings(node);
 	}
 
 	public void afterProcessChilds(Node node, IDocumentContext documentContext) {
 		documentContext.popRangeInfo();
 		if (isParagraph()) {
 			documentContext.popParagraphInfo();
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
