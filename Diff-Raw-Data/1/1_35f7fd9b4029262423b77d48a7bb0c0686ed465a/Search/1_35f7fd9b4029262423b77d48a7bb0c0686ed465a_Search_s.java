 package edu.unlp.cyrus.core.article.search;
 
 import java.text.MessageFormat;
 import java.util.Collection;
 import java.util.List;
 
 import edu.unlp.cyrus.core.article.field.Field;
 import edu.unlp.cyrus.core.article.field.Type;
 import edu.unlp.cyrus.core.article.field.TypeBoolean;
 import edu.unlp.cyrus.core.article.field.TypeEnumerative;
 import edu.unlp.cyrus.core.article.field.TypeNumber;
 import edu.unlp.cyrus.core.article.field.TypePicture;
 import edu.unlp.cyrus.core.article.field.TypeString;
 import edu.unlp.cyrus.core.article.type.ArticleType;
 import edu.unlp.cyrus.core.article.value.BooleanValue;
 import edu.unlp.cyrus.core.article.value.BooleanValueFalse;
 import edu.unlp.cyrus.core.article.value.BooleanValueTrue;
 import edu.unlp.cyrus.core.article.value.StringValue;
 import edu.unlp.cyrus.core.article.value.Value;
 import edu.unlp.cyrus.util.Function;
 import edu.unlp.cyrus.util.Just;
 import edu.unlp.cyrus.util.Maybe;
 import edu.unlp.cyrus.util.Nothing;
 import edu.unlp.cyrus.util.Utils;
 
 public final class Search {
 	private class CannotBuildConditionException extends Exception {
 		private static final long serialVersionUID = 1L;
 	}
 
 	public static final Search Empty = new Search("");
 	
 	private final String key;
 
 	public Search(String _key) {
 		key = _key;
 	}
 	
 	private static final String WHERE_STRUCTURE = "WHERE {0}";
 	public String getWhere(ArticleType type, List<Value> parameters) {
 		if (this.isEmpty())
 			return "";
 		
 		try {
 			return MessageFormat.format(WHERE_STRUCTURE, this.getConditions(type, parameters));
 		} catch (CannotBuildConditionException e) {
 			return "";
 		}
 	}
 	
 	private boolean isEmpty() {
 		return key.equals("");
 	}
 	
 	private static final String EQUAL_STRING_STRUCTURE = "{0} LIKE ?";
 	private static final String EQUAL_BOOLEAN_STRUCTURE = "{0} = ?";
 	private String getConditions(ArticleType type, final List<Value> parameters) throws CannotBuildConditionException {
 		final Collection<Field> fields = type.getFields();
 		
 		if (fields.isEmpty())
 			throw new CannotBuildConditionException();
 		
 		return
 				Utils.join(
 					Utils.map(fields, new Function<Field, String>() {
 						public String apply(final Field field) {
 							return field.getType().visit(new Type.Visitor0<String>() {
 								public String visit(TypeBoolean typeBoolean) {
 									Maybe<BooleanValue> bool = Search.this.getKeyAsBoolean();
 									if (bool.exists()) {
 										parameters.add(bool.getValue());
 										return MessageFormat.format(EQUAL_BOOLEAN_STRUCTURE, field.getID());
 									} else
 										return "FALSE";
 								}
 								
 								public String visit(TypeString typeString) {
 									return this.visitAsString();
 								}
 
 								public String visit(TypePicture typePicture) {
 									return this.visitAsString();
 								}
 
 								public String visit(TypeNumber typeNumber) {
 									return this.visitAsString();
 								}
 
 								public String visit(TypeEnumerative typeEnumerative){
 									return this.visitAsString();
 								}
 								
 								private String visitAsString() {
 									parameters.add(Search.this.getKeyAsString());
 									return MessageFormat.format(EQUAL_STRING_STRUCTURE, field.getID());									
 								}
 							});
 						}
 					}),
 					" OR "
 				);
 	}
 	
 	private Maybe<BooleanValue> getKeyAsBoolean() {
 		switch (key.toUpperCase()) {
 		case "YES":
 		case "SI":
 		case "TRUE":
 			return new Just<BooleanValue>(BooleanValueTrue.Instance);
 		case "FALSE":
 			return new Just<BooleanValue>(BooleanValueFalse.Instance);
 		default:
 			return new Nothing<BooleanValue>();
 		}
 	}
 	
 	private static final String STRING_SEARCH_PATTERN = "%{0}%";
 	private StringValue getKeyAsString() {
 		return new StringValue(MessageFormat.format(STRING_SEARCH_PATTERN, key));
 	}
 }
