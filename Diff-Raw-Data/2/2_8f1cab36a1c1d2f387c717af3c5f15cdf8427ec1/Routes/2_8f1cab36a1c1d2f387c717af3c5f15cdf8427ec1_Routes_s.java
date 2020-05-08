 package earth.xor.rest.routes;
 
 public abstract class Routes {
     public static final String BASE = "/bookmarks";
 
     public static final String POST_BOOKMARK = BASE;
     public static final String GET_ALL_BOOKMARKS = BASE;
     public static final String GET_BOOKMARKS_BY_ID = BASE + "/:id";
     public static final String OPTIONS_BOOKMARKS = BASE;
    public static final String DELETE_BOOKMARK = BASE;
 }
