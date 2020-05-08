 package com.diycomputerscience.slides.view.dto;
 
 import com.diycomputerscience.slides.model.Slide;
 
 
 public class SlideTO {
 
 	private long id;
 	private String title;
 	private String contents;
 	private int placement;
 	
 	public SlideTO() {
 		
 	}
 
 	public SlideTO(Slide slide) {
		this.id = slide.id;
		this.title = slide.title;
		this.contents = slide.contents;
		this.placement = slide.placement;
 	}
 	
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getContents() {
 		return contents;
 	}
 
 	public void setContents(String contents) {
 		this.contents = contents;
 	}
 
 	public int getPlacement() {
 		return placement;
 	}
 
 	public void setPlacement(int placement) {
 		this.placement = placement;
 	}
 	
 	
 }
