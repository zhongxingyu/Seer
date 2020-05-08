 package story.book.model;
 
 import android.content.Context;
 import android.view.View;
 
public class Annotation<T extends Illustration> {
 	
 	private String author;
	private T illustration;
 	
	public Annotation(String author, T illustration) {
 		this.author = author;
 		this.illustration = illustration;
 	}
 	
 	public String getAuthor() {
 		return this.author;
 	}
 	
	public T getIllustration() {
 		return illustration;
 	}
 	
 	public View getView(String path, Boolean editMode, Context C) {
 		return this.illustration.getView(path, editMode, C);
 	}
 }
