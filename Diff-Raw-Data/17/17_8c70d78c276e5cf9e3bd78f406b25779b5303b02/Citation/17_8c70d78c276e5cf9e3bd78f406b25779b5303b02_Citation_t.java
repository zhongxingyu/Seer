 package edu.ycp.cs320.project;
 
 public class Citation {
 	private FormatType format;
 	/*
 	private Book book;
 	*/
 
 	private Source source;
 	//private String source2;
 	
 	/**
 	 * Constructor for a book citation.
 	 * 
 	 * @param book     the book
 	 * @param format   the format in which to generation the citation
 	 * @param source2  additional source associated with the book (for example, the book's website)
 	 */
 	public Citation(Source source,FormatType format){
 		
 	this.format=format;
 	//this.book=book;
 	this.source =source;
 	//this.source2=source2;
 	}
 
 
 
 	
 
 	
 
 	
 
 
 
 
 
 
 	public FormatType getformat() {
 		// TODO Auto-generated method stub
 		return format;
 	}
 	public String toString() {
         return source.toString();
 }
 	/*public String getsource(){
 		return source2;
 	}*/
 
 	public Book getbook(){
 		return (Book) source;
 	}
 	public Website getwebsite(){
 		return (Website) source;
 	}
 	public Journal getjournal(){
 		return (Journal) source;
 	}
 	public Magazine getmagazine(){
 		return (Magazine) source;
 	}
 	/*
 	public void setbook(Book book){
 		this.book=book;
 	}
 	*/
 
 	/*public String createdmla(){
 		return source2;
 	}*/
 
 	public String formatcit() {
 		String cit = null;
 		if(getformat().equals(FormatType.MLA)){
 			cit=printmla();
 		}
 		if(getformat().equals(FormatType.APA)){
 			cit=printapa();
 		}
 		if(getformat().equals(FormatType.CHICAGO)){
 			cit=printchicago();
 		}
 		return cit;
 		
 	}
 
 	private String printchicago() {
 		String cit = null;
 		// TODO Auto-generated method stub
 		if(source.getSourceType() == SourceType.BOOK){
 			cit=getbook().getlast()+","+getbook().getfirst()+"."+getbook().gettitle()+"."+getbook().getcity()+","+getbook().getpublisher()+","+getbook().getdate();
 				}
 		if(source.getSourceType() == SourceType.MAGAZINE){
 			cit=getmagazine().getlast()+","+getmagazine().getfirst()+"."+getmagazine().gettitle()+"."+getmagazine().getmagazine()+","+getmagazine().getdate()+","+getmagazine().getpagenumber();
 		}
 		if(source.getSourceType() == SourceType.WEBSITE){
 			cit=getwebsite().getlast()+","+getwebsite().getfirst()+"."+getwebsite().gettitle()+"."+getwebsite().getdate()+","+getwebsite().geturl();
 		}
 		if(source.getSourceType() == SourceType.JOURNAL){
 			cit=getjournal().getlast()+","+getjournal().getfirst()+"."+getjournal().gettitle()+"."+getjournal().getjournal()+","+getjournal().getvolume()+","+getjournal().getpagenumber();
 		}
 		return cit;
 	}
 
 	private String printapa() {
 		String cit=null;
 		if(source.getSourceType() == SourceType.BOOK){
			 cit=getbook().getlast()+","+getbook().getfirst()+".("+getbook().getdate()+")."+getbook().gettitle()+"."+getbook().getcity()+":"+getbook().getpublisher();
 				}
 		if(source.getSourceType() == SourceType.MAGAZINE){
			cit=getmagazine().getlast()+","+getmagazine().getfirst()+". ("+getmagazine().getdate()+")."+ getmagazine().gettitle()+","+getmagazine().getmagazine()+","+getmagazine().getpagenumber();
 		}
 		if(source.getSourceType() == SourceType.WEBSITE){
			cit=getwebsite().getlast()+","+getwebsite().getfirst()+". ("+getwebsite().getdate()+")"+getwebsite().gettitle()+"."+getwebsite().geturl();
 		}
 		if(source.getSourceType() == SourceType.JOURNAL){
			cit=getjournal().getlast()+","+getjournal().getfirst()+". ("+getjournal().getdate()+")."+getjournal().gettitle()+","+getjournal().getpublisher()+","+getjournal().getvolume()+","+getjournal().getpagenumber();
 		}
 		return cit;
 	}
 		
 	
 
 	private String printmla() {
 		// TODO:title in quote
 		String cit=null;
 		if(source.getSourceType() == SourceType.BOOK){
 			cit=getbook().getlast()+","+getbook().getfirst()+"."+getbook().gettitle()+"."+getbook().getcity()+":"+getbook().getpublisher()+","+getbook().getdate()+".";
 				}
 		if(source.getSourceType() == SourceType.MAGAZINE){
			cit=getmagazine().getlast()+","+getmagazine().getfirst()+"."+getmagazine().gettitle()+"."+getmagazine().getmagazine()+","+getmagazine().getdate()+":"+getmagazine().getpagenumber();
 		}
 		if(source.getSourceType() == SourceType.WEBSITE){
			cit=getwebsite().getlast()+","+getwebsite().getfirst()+"."+getwebsite().gettitle()+"."+getwebsite().getpublisher()+","+getwebsite().getdate()+"."+getwebsite().getaccess();
 		}
 		if(source.getSourceType() == SourceType.JOURNAL){
 			cit=getjournal().getlast()+","+getjournal().getfirst()+"."+getjournal().gettitle()+"."+getjournal().getpublisher()+" "+getjournal().getvolume()+" ("+getjournal().getdate()+"):"+getjournal().getpagenumber();
 		}
 		return cit;
 	}
 	}
 
 
 
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 	
 
 
