 package com.weieditor.mds.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.weieditor.mds.visitor.DocumentVisitor;
 
 public class Paragraph {
 
 	private Document doc;
 	private int paragraphId;
 	private String content = "";
 	private List<Sentence> sentences = new ArrayList<Sentence>();
 
 	public Paragraph(Document doc, int paragraphId) {
 		this.doc = doc;
		this.paragraphId = paragraphId;
 	}
 
 	public Document getDoc() {
 		return doc;
 	}
 
 	public void setDoc(Document doc) {
 		this.doc = doc;
 	}
 
 	public int getParagraphId() {
 		return paragraphId;
 	}
 
 	public void setParagraphId(int id) {
 		this.paragraphId = id;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 	public List<Sentence> getSentences() {
 		return sentences;
 	}
 
 	public void setSentences(List<Sentence> sentences) {
 		this.sentences = sentences;
 	}
 
 	public Sentence get(int id) {
 		return sentences.get(id);
 	}
 
 	public void add(Sentence sentence) {
 		sentences.add(sentence);
 	}
 
 	public void accept(DocumentVisitor docVisitor) {
 		docVisitor.visit(this);
 		for (Sentence sentence : sentences) {
 			sentence.accept(docVisitor);
 		}
 
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((doc == null) ? 0 : doc.hashCode());
 		result = prime * result + paragraphId;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Paragraph other = (Paragraph) obj;
 		if (doc == null) {
 			if (other.doc != null)
 				return false;
 		} else if (!doc.equals(other.doc))
 			return false;
 		if (paragraphId != other.paragraphId)
 			return false;
 		return true;
 	}
 
 }
