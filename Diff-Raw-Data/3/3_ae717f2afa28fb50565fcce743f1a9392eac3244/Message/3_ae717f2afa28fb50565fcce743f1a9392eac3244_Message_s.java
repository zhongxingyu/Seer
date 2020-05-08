 package net.timendum.denis.message;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.timendum.denis.DomainNameCompressor;
 import net.timendum.denis.io.ArrayDataInputStream;
 import net.timendum.denis.io.ArrayDataOutputStream;
 import net.timendum.denis.message.resources.Resource;
 import net.timendum.denis.rfc.Flags;
 import net.timendum.denis.rfc.Opcode;
 
 public class Message implements Cloneable {
 
 	private Header header;
 
 	/**
 	 *Questions
 	 */
 	private List<Question> qd = new LinkedList<Question>();
 
 
 	/**
 	 * Answer
 	 */
 	private List<Resource> an = new LinkedList<Resource>();
 	/**
 	 * Authority records.
 	 */
 	private List<Resource> ns = new LinkedList<Resource>();
 	/**
 	 * Additional records.
 	 */
 	private List<Resource> ar = new LinkedList<Resource>();
 
 	public Message() {
 		header = Header.newHeader();
 	}
 
 	public Message(Header header) {
 		header = this.header;
 	}
 
 	public void addQuestion(String qName, int type, int dClass) {
 		addQuestion(Question.newQuestion(qName, type, dClass));
 	}
 
 	public void addQuestion(Question q) {
 		qd.add(q);
 		header.incrementQdCount();
 	}
 
 	public static Message newQuery(String qName, int type, int dClass) {
 		Message m = newQuery();
 		m.addQuestion(qName, type, dClass);
 		return m;
 	}
 
 	public static Message newQuery() {
 		Message m = new Message();
 		m.setQuery();
 		return m;
 	}
 
 	public void setQuery() {
 		header.setOpcode(Opcode.QUERY);
 		header.setFlag(Flags.RD);
 	}
 
 	public static Message read(ArrayDataInputStream dis, DomainNameCompressor dnc) throws IOException {
 		Message message = new Message();
 		message.header = Header.read(dis, dnc);
 		int count = message.header.getQdCount();
 		for (int i = 0; i < count; i++) {
 			message.qd.add(Question.read(dis, dnc));
 		}
 		count = message.header.getAnCount();
 		for (int i = 0; i < count; i++) {
 			message.an.add(Resource.read(dis, dnc));
 		}
 		count = message.header.getNsCount();
 		for (int i = 0; i < count; i++) {
 			message.ns.add(Resource.read(dis, dnc));
 		}
 		count = message.header.getArCount();
 		for (int i = 0; i < count; i++) {
 			message.ar.add(Resource.read(dis, dnc));
 		}
 		return message;
 	}
 
 	public void write(ArrayDataOutputStream dos) {
 		header.write(dos);
		for (Question q : qd) {
			q.write(dos);
		}
 		int count = header.getQdCount();
 		for (int i = 0; i < count; i++) {
 			qd.get(i).write(dos);
 		}
 		count = header.getAnCount();
 		for (int i = 0; i < count; i++) {
 			an.get(i).write(dos);
 		}
 		count = header.getNsCount();
 		for (int i = 0; i < count; i++) {
 			ns.get(i).write(dos);
 		}
 		count = header.getArCount();
 		for (int i = 0; i < count; i++) {
 			ar.get(i).write(dos);
 		}
 	}
 
 	public List<Question> getQd() {
 		return Collections.unmodifiableList(qd);
 	}
 
 	@Override
 	public Message clone() {
 		Message m = new Message();
 		m.header = header.clone();
 		for (Question q : qd) {
 			m.qd.add(q.clone());
 		}
 		for (Resource r : an) {
 			m.an.add(r.clone());
 		}
 		for (Resource r : ns) {
 			m.ns.add(r.clone());
 		}
 		for (Resource r : ar) {
 			m.ar.add(r.clone());
 		}
 		return m;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("Message [header=");
 		builder.append(header);
 		builder.append(", qd=");
 		builder.append(qd);
 		builder.append(", an=");
 		builder.append(an);
 		builder.append(", ns=");
 		builder.append(ns);
 		builder.append(", ar=");
 		builder.append(ar);
 		builder.append("]");
 		return builder.toString();
 	}
 }
