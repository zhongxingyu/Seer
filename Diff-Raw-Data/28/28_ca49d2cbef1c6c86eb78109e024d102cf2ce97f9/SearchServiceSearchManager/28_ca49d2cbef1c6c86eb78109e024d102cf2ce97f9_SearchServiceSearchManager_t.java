 /**
  * Copyright 2011 Henric Persson (henric.persson@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package burrito.services;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import siena.Model;
 import burrito.annotations.BBCode;
 import burrito.annotations.Displayable;
 import burrito.annotations.RichText;
 import burrito.annotations.SearchableField;
 import burrito.annotations.SearchableMethod;
 import burrito.client.util.StringCutter;
 import burrito.client.widgets.panels.table.ItemCollection;
 import burrito.client.widgets.panels.table.PageMetaData;
 import burrito.util.EntityUtil;
 import burrito.util.StringUtils;
 
 import com.google.appengine.api.search.Consistency;
 import com.google.appengine.api.search.Document;
 import com.google.appengine.api.search.Index;
 import com.google.appengine.api.search.IndexSpec;
 import com.google.appengine.api.search.ListRequest;
 import com.google.appengine.api.search.ListResponse;
 import com.google.appengine.api.search.Query;
 import com.google.appengine.api.search.QueryOptions;
 import com.google.appengine.api.search.Results;
 import com.google.appengine.api.search.ScoredDocument;
 import com.google.appengine.api.search.SearchException;
 import com.google.appengine.api.search.SearchServiceFactory;
 import com.google.appengine.api.search.SortExpression;
 import com.google.appengine.api.search.SortExpression.Builder;
 import com.google.appengine.api.search.SortOptions;
 
 
 /**
  * Idéerna till den här klassen kommer främst från
  * <nobr>http://googleappengine.blogspot
  * .com/2010/04/making-your-app-searchable-using-self.html</nobr>
  * 
  * Teknikerna för säkning går ut på att splitta texten i tokens som sen sparas i
  * en entitet i databasen. App engines datastore sköter indexeringen och man kan
  * ställa enkla queries.
  * 
  * @author henper
  * 
  */
 public class SearchServiceSearchManager implements SearchManager {
 
 	private static final String CONTENT_FIELD_NAME = "content_"; //ending with an underscore to prevent clashes with true field names.
 	private static final String TITLE_FIELD_NAME = "title_"; //ending with an underscore to prevent clashes with true field names.
 	
 	private static double NUMBER_MAX_VAL = 2.14748364E9d;
 	
 	protected SearchServiceSearchManager() {
 		//private
 	}
 	
 	private Index getIndex() {
 	    IndexSpec indexSpec = IndexSpec.newBuilder()
 	        .setName("global")
 	        .setConsistency(Consistency.PER_DOCUMENT)
 	        .build();
 	    return SearchServiceFactory.getSearchService().getIndex(indexSpec);
 	}
 	
 	@Override
 	public void insertOrUpdateSearchEntry(Model entity, Long entityId) {
 		Class<? extends Model> ownerType = entity.getClass();
 		
 		Document document = entityToDocument(ownerType, entity, entityId);
 		if (document != null) {
 			getIndex().add(document);
 		}
 		//If the document is null, there is nothing to index
 		
 	}
 
 	@Override
 	public void deleteSearchEntry(Class<? extends Model> ownerType, Long entityId) {
 		getIndex().remove(ownerType.getName() + ":" + entityId);
 	}
 
 	private Document entityToDocument(Class<? extends Model> ownerType, Model entity, Long entityId) {
 		Document.Builder builder = Document.newBuilder()
 			.setId(ownerType.getName() + ":" + entityId)
 			.addField(com.google.appengine.api.search.Field.newBuilder().setName("ownerType").setText(ownerType.getName()));
 		
 		String title = entity.toString();
 		if (title == null) {
 			title = "(untitled)";
 		}
 		builder.addField(com.google.appengine.api.search.Field.newBuilder().setName(TITLE_FIELD_NAME).setText(title));
 			
 		
 		for (Field field : EntityUtil.getFields(ownerType)) {
 			if (field.isAnnotationPresent(Displayable.class)) {
 				try {
 					field.setAccessible(true);
 					Object obj = field.get(entity);
 					
 					com.google.appengine.api.search.Field.Builder fieldBuilder = com.google.appengine.api.search.Field.newBuilder().setName(ownerType.getSimpleName() + "_" + field.getName());
 					if (Date.class.isAssignableFrom(field.getType())) {
 						Date d = (Date) obj;
 						long seconds = 0;
 						if (d != null) {
 							seconds = d.getTime() / 1000;
 						}
 						fieldBuilder.setNumber(seconds);
 					} else if (Number.class.isAssignableFrom(field.getType())) {
 						double d = 0d;
 						if (obj != null) {
 							d = ((Number)obj).doubleValue();
 						}
 						if (d > NUMBER_MAX_VAL) {
 							d = NUMBER_MAX_VAL; //We don't really deal with large numbers. But this will prevent it from breaking if a very large value is entered.
 						}
 						fieldBuilder.setNumber(d);
 					} else {
 						fieldBuilder.setText(obj == null ? "" : obj.toString());
 					}
 					builder.addField(fieldBuilder);
 				} catch (Exception e) {
 					throw new RuntimeException("Failed to get displayable texts from entity", e);
 				}
 			}
 		}
 		
 		Set<String> searchables = getSearchableTextsFromEntity(ownerType,
 				entity);  
 		if (searchables.isEmpty()) {
 			//Nothing to index
 			return null;
 		}
 		String text = concatenate(searchables);
 		builder.addField(com.google.appengine.api.search.Field.newBuilder().setName(CONTENT_FIELD_NAME).setText(text));
 		return builder.build();
 	}
 
 	protected static Set<String> getSearchableTextsFromEntity(
 			Class<? extends Model> ownerType, Model entity) {
 		Set<String> searchables = new HashSet<String>();
 		for (Field field : EntityUtil.getFields(ownerType)) {
 			if (field.isAnnotationPresent(SearchableField.class) || field.isAnnotationPresent(Displayable.class)) {
 				try {
 					field.setAccessible(true);
 					Object obj = field.get(entity);
 					String val = null;
 					if (obj != null) {
 						val = obj.toString();
 						if (field.isAnnotationPresent(RichText.class)) {
 							val = StringUtils.stripHTML(val);
 						}
 						if (field.isAnnotationPresent(BBCode.class)) {
 							val = StringUtils.stripBBCode(val);
 						}
 						searchables.add(val);
 					}
 				} catch (Exception e) {
 					throw new RuntimeException("Failed to get searchable texts from entity", e);
 				}
 			}
 		}
 		
 		for (Method method : EntityUtil.getMethods(ownerType)) {
 			if (method.isAnnotationPresent(SearchableMethod.class) || method.isAnnotationPresent(Displayable.class)) {
 				try {
 					method.setAccessible(true);
 					Object obj = method.invoke(entity);
 					if (obj != null) {
 						String val = obj.toString();
 						if (val != null) {
 							searchables.add(val);
 						}
 					}
 				} catch (Exception e) {
 					throw new RuntimeException("Failed to get searchable texts from entity", e);
 				}
 			}
 		}
 		return searchables;
 	}
 
 
 	private String concatenate(Set<String> texts) {
 		StringBuilder sb = new StringBuilder();
 		for (String text : texts) {
 			if (sb.length() > 0) {
 				sb.append(". ");
 			}
 			sb.append(text);
 		}
 		return sb.toString();
 	}
 
 	@Override
 	public ItemCollection<SearchHit> search(Class<? extends Model> clazz, String query) {
 		return search(clazz, query, new PageMetaData<String>(100, 0, null, true));
 	}
 	
 
 	/**
 	 * Makes a global search, i.e. not filtering on a specific entity kind.
 	 * 
 	 * @param query
 	 */
 	@Override
 	public ItemCollection<SearchHit> search(String query) {
 		return search(query, new PageMetaData<String>(100, 0, null, true));
 	}
 	
 	/**
 	 * Makes a global search, i.e. not filtering on a specific entity kind.
 	 * 
 	 * @param query the search query
 	 * @param pageMetaData The current page
 	 * @return
 	 */
 	@Override
 	public ItemCollection<SearchHit> search(String query, PageMetaData<String> pageMetaData) {
 		return search(null, query, pageMetaData);
 	}
 
 
 	/**
 	 * Searches for entries matching a query.
 	 * 
 	 * @param clazz
 	 * @param query
 	 * @param p
 	 * @return
 	 */
 	@Override
 	public ItemCollection<SearchHit> search(Class<? extends Model> clazz, String query, PageMetaData<String> page) {
 		
 		query = escape(query);
 		try {
 			QueryOptions.Builder options = QueryOptions.newBuilder()
 				.setFieldsToSnippet(CONTENT_FIELD_NAME)
 				.setFieldsToReturn(CONTENT_FIELD_NAME, TITLE_FIELD_NAME)
 				.setLimit(page.getItemsPerPage())
 				.setOffset((int) page.getRangeStart());
 			
 			
 			if (page.getSortKey() != null) {
 				Builder sortExpression = SortExpression.newBuilder();
 				sortExpression.setExpression(clazz.getSimpleName() + "_" + page.getSortKey());
 				sortExpression.setDirection((page.isAscending()) ? SortExpression.SortDirection.ASCENDING : SortExpression.SortDirection.DESCENDING);
 				
 				if (isFieldNumber(clazz, page.getSortKey())) {
 					sortExpression.setDefaultValueNumeric(0d);
 				} else {
 					sortExpression.setDefaultValue("");
 				}
 				
 				
 				SortOptions.Builder sortOptions = SortOptions.newBuilder()
 					.setLimit(1000)
 					.addSortExpression(sortExpression);
 				options.setSortOptions(sortOptions);
 			}
 			String ownerTypeFilter = "";
 			if (clazz != null) {
 				//Filter on entity kind
 				ownerTypeFilter = "ownerType:\""+clazz.getName()+"\" ";
 			}
 			Query q = Query.newBuilder().setOptions(options).build(ownerTypeFilter + CONTENT_FIELD_NAME+":\"" + query + "\"");
 
 		    Results<ScoredDocument> results = getIndex().search(q);
 		    List<SearchHit> entries = new ArrayList<SearchHit>();
 		    for (ScoredDocument document : results) {
 		    	SearchHit entry = documentToSearchEntry(document, query);
 		        entries.add(entry);
 		    }
 		    ItemCollection<SearchHit> result = new ItemCollection<SearchHit>();
 		    result.setHasNextPage(false);
 		    result.setItems(entries);
 		    result.setHasNextPage(entries.size() == page.getItemsPerPage());
 		    result.setItemsPerPage(page.getItemsPerPage());
 		    return result;
 		    
 		} catch (SearchException e) {
 			throw new RuntimeException("Failed search", e);
 		}
 	}
 
 	private boolean isFieldNumber(Class<? extends Model> clazz, String sortKey) {
 		Field f = EntityUtil.getField(clazz, sortKey);
 		if (Date.class.isAssignableFrom(f.getType())) {
 			return true;
 		}
 		if (Number.class.isAssignableFrom(f.getType())) {
 			return true;
 		}
 		return false;
 	}
 
 	private String escape(String query) {
 		if (query == null) {
 			return null;
 		}
 		return query.replace("\"", "");
 	}
 
 
 	private SearchHit documentToSearchEntry(Document document, String query) {
 		String docId = document.getId();
 		String[] splitId = docId.split(":");
 		
 		
 		SearchHit entry = new SearchHit();
 		entry.setOwnerClass(splitId[0]);
 		entry.setOwnerId(Long.valueOf(splitId[1]));
 		
 		com.google.appengine.api.search.Field content = document.getOnlyField(CONTENT_FIELD_NAME);
 		String snippet = content.getHTML();
 		if (snippet == null && query != null) {
 			//In devmode, snippets are not created. Therefor, we add our own. 
 			//See http://code.google.com/p/googleappengine/issues/detail?id=7473
 			snippet = createSnippet(content.getText(), query);
 		}
 		
 		entry.setSnippetHtml(snippet);
 		entry.setTitle(document.getOnlyField(TITLE_FIELD_NAME).getText());
 		return entry;
 	}
 	
 	
 	private String createSnippet(String test, String query) {
 		int nameIndex = test.toLowerCase().indexOf(query);
 		if (nameIndex != -1) {
 			String highlight = test.substring(nameIndex, nameIndex
 					+ query.length());
 			StringBuffer sb = new StringBuffer(new StringCutter(100).cut(test, true,
 					nameIndex));
 			// update name index from the cut string
 			nameIndex = sb.toString().toLowerCase().indexOf(query);
 			if (nameIndex == -1) {
 				//something wrong
 				return sb.toString();
 			}
 			sb.replace(nameIndex, nameIndex + query.length(),
 					"<span class=\"snippet-highlight\">" + highlight
 							+ "</span>");
 			return sb.toString();
 		}
 		return null;
 	}
 	
 
 
 	@Override
 	public List<SearchHit> getAllEntries() {
 		List<SearchHit> result = new ArrayList<SearchHit>();
 		ListResponse<Document> response = getIndex().listDocuments(ListRequest.newBuilder().build());
 		for (Document doc : response) {
             result.add(documentToSearchEntry(doc, null));
         }
 		return result;
 	}
 
 
 	@Override
 	public void clearIndexForEntity(Class<? extends Model> clazz) {
 		while (true) {
 			List<String> docIds = new ArrayList<String>();
 			// Return a set of document IDs.
 			QueryOptions.Builder options = QueryOptions.newBuilder()
 					.setLimit(100).setReturningIdsOnly(true);
 
 			Query q = Query.newBuilder().setOptions(options)
 					.build("ownerType:" + clazz.getName());
 
 			Results<ScoredDocument> results = getIndex().search(q);
 			if (results.getNumberFound() == 0) {
 				break;
 			}
 			for (ScoredDocument document : results) {
 				docIds.add(document.getId());
 			}
 			getIndex().remove(docIds);
 	    }
 	}
 
 	@Override
 	public Date getLastModified(Class<? extends Model> clazz) {
		String fieldName = clazz.getSimpleName() + "_lastModified";
 		QueryOptions.Builder options = QueryOptions.newBuilder()
			.setFieldsToReturn(fieldName);
 
 		Builder sortExpression = SortExpression.newBuilder();
		sortExpression.setExpression(fieldName);
 		sortExpression.setDefaultValueNumeric(0.0d);
 		sortExpression.setDirection(SortExpression.SortDirection.DESCENDING);
 		SortOptions.Builder sortOptions = SortOptions.newBuilder()
 			.setLimit(1)
 			.addSortExpression(sortExpression);
 		options.setSortOptions(sortOptions);
 		
 		Query q = Query.newBuilder().setOptions(options)
 				.build("ownerType:\"" + clazz.getName() + "\" ");
 
 		Results<ScoredDocument> results = getIndex().search(q);
 		for (ScoredDocument document : results) {
			com.google.appengine.api.search.Field f = document.getOnlyField(fieldName);
 			return new Date((long) (f.getNumber() * 1000d));
 		}
 		return null;
 	}
 	
 }
