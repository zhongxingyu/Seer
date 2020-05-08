 package ch20.ex06;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StreamTokenizer;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 public class StreamManager implements Attributed, Iterable<Attr>{
 	
 	enum State  {
 		NAME, OP, VALUE;
 	}
 	
 	private State state = State.NAME;	
 	protected Map<String, Attr> attrTable = new HashMap<String, Attr>();
 	
 	public Iterator<Attr> readStream(Reader source) throws IOException {
 		StreamTokenizer in = new StreamTokenizer(source);
 		Attr attr = null;
 		in.commentChar('#');
 		in.ordinaryChar('/');
 		while (in.nextToken() != StreamTokenizer.TT_EOF) {
 			if (in.ttype == StreamTokenizer.TT_WORD) {
 				state = getState(in.sval);
 				if (attr != null) {
 					attr.setValue(in.sval);
 					attr = null;
 				} else {
 					attr = new Attr(in.sval);
 					this.add(attr);
 				}
 			} else if (in.ttype == '=') {
 				if (attr == null)
 					throw new IOException("msplaced '='");
 			} else {
 				if (attr == null)
 					throw new IOException("bad Attr name");
 				switch (state) {
 				case NAME:
 					attr.setValue(in.sval);
 					break;
 				case OP:
 					attr.setValue(in.sval);
 					break;
 				case VALUE:
 					attr.setValue(new Double(in.sval));
 					break;
				default:
					throw new IOException("bad state");
 				}
 				attr = null;
 			}
 		}
 		return attrs();
 	}
 	
 	private State getState(String str) throws IOException {
 		if (str.equals("name")) {
 			return State.NAME;
 		} else if (str.equals("op")) {
 			return State.OP;
 		} else if (str.equals("value")) {
 			return State.VALUE;
 		} else {
 			throw new IOException("bad Attr name");
 		}
 	}
 
 	@Override
 	public void add(Attr newAttr) {
 		attrTable.put(newAttr.getName(), newAttr);
 	}
 	
 	public Iterator<Attr> attrs() {
 		return attrTable.values().iterator();
 	}
 
 	@Override
 	public Iterator<Attr> iterator() {
 		return attrs();
 	}
 	
 	public static void main(String[] args) throws IOException {
 		StreamManager manager = new StreamManager();
 		// The content of file for test is described below
 		// name="offset"
 		// value="1"
 		// op="-"
 
 		Reader source = new FileReader("/Users/knishide/Desktop/nameOPValue.txt");
 		Iterator<Attr> attrs = manager.readStream(source);
 		while (attrs.hasNext()) {
 			Attr attr = attrs.next();
 			System.out.println("Name : " + attr.getName() + ", Value : " + attr.getValue());
 		}
 	}
 }
