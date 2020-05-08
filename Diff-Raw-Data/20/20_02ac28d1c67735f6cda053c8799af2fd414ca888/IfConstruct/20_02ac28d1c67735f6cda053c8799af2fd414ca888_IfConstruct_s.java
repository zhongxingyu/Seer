 /**
  * 
  */
 package ee.olegus.fortran.ast;
 
 import java.util.List;
 
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 
 import ee.olegus.fortran.ASTVisitor;
 import ee.olegus.fortran.XMLGenerator;
 
 /**
  * @author olegus
  *
  */
 public class IfConstruct extends Statement implements XMLGenerator {
 	
 	private List<IfStatement> ifStatements;
 
 	/* (non-Javadoc)
 	 * @see ee.olegus.fortran.ASTVisitable#astWalk(ee.olegus.fortran.ASTVisitor)
 	 */
 	public Object astWalk(ASTVisitor visitor) {
 		for (IfStatement stmt: ifStatements) {
 			stmt.astWalk(visitor);
 		}
 		visitor.visit(this);
 		return this;
 	}
 
 	@Override
 	public void generateXML(ContentHandler handler) throws SAXException {
 		handler.startElement("", "", "ifconstruct", null);		
 		handler.startElement("", "", "block", null);
 		super.generateXML(handler);
 
 		for (IfStatement stmt: ifStatements) {
 			stmt.generateXML(handler);
 		}
 		handler.endElement("", "", "block");
 		handler.endElement("", "", "ifconstruct");
 	}
 
 	public List<IfStatement> getIfStatements() {
 		return ifStatements;
 	}
 
 	public void setIfStatements(List<IfStatement> ifStatements) {
 		this.ifStatements = ifStatements;
 	}
 
 }
