 package pt.utl.ist.fenix.tools.file;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import pt.utl.ist.fenix.tools.file.FilesetMetadataQuery.ConjunctionType;
 import pt.utl.ist.fenix.tools.file.FilesetMetadataQuery.MetadataQuery;
 
 public class FileSearchCriteria {
 		
 	FilesetMetadataQuery query; 
 	
 	public FileSearchCriteria() {
 		this(0,15);
 	}
 	
 	public FileSearchCriteria(int start, int pageSize) {
 		query = new FilesetMetadataQuery();
 		query.setStart(start);
 		query.setPageSize(pageSize);
 	}
 	
 	public FilesetMetadataQuery getQuery() {
 		return query;
 	}
 	
 	public static FilesetMetadataQuery searchByAuthor(String name) {
 		FileSearchCriteria searchObject = new FileSearchCriteria();
 		searchObject.addAndCriteria(SearchField.AUTHOR, name);
 		return searchObject.getQuery();
 	}
 	
 	public static FilesetMetadataQuery searchByTitle(String title) {
 		FileSearchCriteria searchObject = new FileSearchCriteria();
 		searchObject.addOrCriteria(SearchField.TITLE, title);
 		return searchObject.getQuery();
 	}
 	
 	public void addCriteria(SearchField criteria, String value) {
 		addAndCriteria(criteria, value);
 	}
 
 	public void addAndCriteria(SearchField criteria, String value) {
		if(value!=null && value.length()>0) { 
			query.addNextQuery(criteria.fieldName(), "\"" + value + "\"", ConjunctionType.AND);
 		}
 	}
 	
 	public void addOrCriteria(SearchField criteria, String value) {
		if(value!=null && value.length()>0) {
			query.addToPreviousQuery(ConjunctionType.OR,criteria.fieldName(), "\"" + value + "\"");
 		}
 	}
 	
 	public static enum SearchField {
 		AUTHOR ("author"),
 		COURSE("author"), // on executionCourses the author of the file is the course
 		TITLE ("title"),
 		KEYWORD("keyword"),		
 		DATE ("date"),
 		PUBLISHER("publisher"),
 		DESCRIPTION("description"),
 		TYPE("type"),
 		ANY(MetadataQuery.ANY_FIELD);
 		
 		
 		private String field;
 		
 		private SearchField(String name) {
 			field = name;
 		}
 		
 		public String fieldName() {
 			return field;
 		}
 		
 		public static List<SearchField> getSearchFieldsInResearchPublications() {
 			List<SearchField> fields = new ArrayList<SearchField>();
 			fields.add(SearchField.AUTHOR);
 			fields.add(SearchField.TITLE);
 			fields.add(SearchField.DATE);
 			fields.add(SearchField.PUBLISHER);
 			fields.add(SearchField.DESCRIPTION);
 			fields.add(SearchField.ANY);
 			return fields;
 		}
 		
 	}
 	
 }
 
