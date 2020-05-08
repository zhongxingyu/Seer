 package net.mariusgundersen.bookmarks.domain;
 
 import java.util.List;
 
 import qvc.handlers.CommandHandler;
 import qvc.handlers.QueryHandler;
 
 public class BookmarkHandler implements CommandHandler, QueryHandler {
 
 	private BookmarkCollection bookmarks;
 	
 	
 	
 	public BookmarkHandler(BookmarkCollection bookmarks) {
 		this.bookmarks = bookmarks;
 	}
 
 
 
 	public void handle(AddBookmark bookmark){
		bookmarks.addBookmark(new Bookmark(bookmark.link, bookmark.name));
 	}
 	
 	public List<Bookmark> handle(GetAllBookmarks query){
 		return bookmarks.getAllBookmarks();
 	}
 }
