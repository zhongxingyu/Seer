 package awesome.app;
 
 import java.util.ArrayList;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class StudentHandler extends DefaultHandler {
 
 	private StudentData student;
 	private ArrayList<StudentData> resultList;
 	private String currentValue;
 
 	@Override
 	public void startDocument() throws SAXException {
 		resultList = new ArrayList<StudentData>();
 	}
 
 	@Override
 	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
 		if (localName.equals("Student")) {
 			student = new StudentData();
 		}
 	}
 
 	@Override
 	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
 		if (localName.equals("Student")) {
 			resultList.add(student);
 		} else if (localName.equals("username")) {
 			student.setUsername(currentValue);
 		} else if (localName.equals("status")) {
 			student.setStatus(currentValue);
 		} else if (localName.equals("name")) {
 			student.setName(currentValue);
 		} else if (localName.equals("cm")) {
 			student.setCm(currentValue);
 		} else if (localName.equals("dept")) {
 			student.setDepartment(currentValue);
 		} else if (localName.equals("phone")) {
 			student.setPhone(currentValue);
 		} else if (localName.equals("room")) {
 			student.setRoom(currentValue);
 		}
 	}
 
 	@Override
 	public void characters(char[] ch, int start, int length) throws SAXException {
 		currentValue = new String(ch, start, length);
 	}
 
 	public ArrayList<StudentData> getStudentList() {
		ArrayList<StudentData> toReturn = resultList;
		resultList = new ArrayList<StudentData>();
		return toReturn;
 	}
 }
