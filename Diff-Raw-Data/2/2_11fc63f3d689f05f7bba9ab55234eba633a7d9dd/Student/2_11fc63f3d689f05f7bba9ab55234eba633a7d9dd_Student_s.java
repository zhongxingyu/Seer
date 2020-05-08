 package playground.presentation;
 
 import playground.business.*;
 
 import java.util.Iterator;
 import org.w3c.dom.html.*;
 import com.lutris.xml.xmlc.*;
 import com.lutris.appserver.server.httpPresentation.*;
 
 public class Student implements HttpPresentation {
 
     public void run(HttpPresentationComms comms) 
         throws HttpPresentationException {
 
 	String now = new java.util.Date().toString();
 	StudentHTML page = new StudentHTML();
 	
 	page.getElementName().setValue(now);
 	foo(page);
         comms.response.writeHTML(page.toDocument());
     }
 
     private void foo(StudentHTML page) {
 	Clipboard board = Clipboard.getDemoClipboard();
 	Iterator studis = board.getStudents();
 
 	while (studis.hasNext()) {
 	    StudentBO s = (StudentBO) studis.next();
 	    page.getElementStudis().appendChild(createStudiParagraph(s));
 	}
     }
 
 	private HTMLParagraphElement createStudiParagraph(StudentBO studi) {
 	    final boolean DEEPCLONE = false;
 	    HTMLParagraphElement temp = (HTMLParagraphElement) new StudentHTML().getElementStudiTemplate().cloneNode(DEEPCLONE);
	    temp.setNodeValue(studi.getName());
 	    return temp;
 	}
 
 }
