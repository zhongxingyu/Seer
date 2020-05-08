 package story.book.model;
 
 import android.content.Context;
 import android.view.View;
 
public class Annotation {
 	
 	private String author;
	private Illustration<?> illustration;
 	
	public Annotation(String author, Illustration<?> illustration) {
 		this.author = author;
 		this.illustration = illustration;
 	}
 	
 	public String getAuthor() {
 		return this.author;
 	}
 	
	public Illustration<?> getIllustration() {
 		return illustration;
 	}
 	
 	public View getView(String path, Boolean editMode, Context C) {
 		return this.illustration.getView(path, editMode, C);
 	}
 }
