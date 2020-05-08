 package org.polyforms.repository.jpa.support;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.springframework.util.ReflectionUtils;
 import org.springframework.util.StringUtils;
 
 class JpqlQueryStringBuilder implements QueryResolver {
     private static final int NUMBER_OF_PARTS = 3;
     private static final String EMPTY_STRING = "";
     private static final String ENTITY_CLASS_PLACE_HOLDER = "{ENTITY_CLASS_HOLDER}";
     private static final String ORDER_BY = "OrderBy";
     private static final String BY = "By";
     private final Map<String, String> queryStringCache = new HashMap<String, String>();
     private final Pattern pattern;
 
     public JpqlQueryStringBuilder() {
         final StringBuffer keyWords = new StringBuffer();
         for (final KeyWord keyWord : KeyWord.values()) {
             if (keyWords.length() > 0) {
                 keyWords.append("|");
             }
             keyWords.append(keyWord.name());
         }
         pattern = Pattern.compile(String.format("(?<=[a-z])((?<=%1$s)|(?=%1$s))", keyWords.toString()));
     }
 
     public String getQuery(final Class<?> entityClass, final Method method) {
         final String methodName = StringUtils.capitalize(method.getName());
         if (!queryStringCache.containsKey(methodName)) {
             final String[] parts = split(methodName);
 
             final JpqlStringBuffer jpql = new JpqlStringBuffer(entityClass);
             appendSelectClause(jpql, parts[0]);
             if (StringUtils.hasText(parts[1])) {
                 appendWhereClause(jpql, parts[1]);
             }
             if (StringUtils.hasText(parts[2])) {
                 appendOrderClause(jpql, parts[2]);
             }
             queryStringCache.put(methodName, jpql.getJpql());
         }
 
         return queryStringCache.get(methodName).replace(ENTITY_CLASS_PLACE_HOLDER, entityClass.getSimpleName());
     }
 
     private String[] split(final String methodName) {
         int orderByPosition = methodName.indexOf(ORDER_BY);
         if (orderByPosition == -1) {
             orderByPosition = methodName.length();
         }
         int byPosition = methodName.substring(0, orderByPosition).indexOf(BY);
         if (byPosition == -1) {
             byPosition = orderByPosition;
         }
 
         final String[] parts = new String[NUMBER_OF_PARTS];
         parts[0] = byPosition == 0 ? EMPTY_STRING : methodName.substring(0, byPosition);
         parts[1] = byPosition == orderByPosition ? EMPTY_STRING : methodName.substring(byPosition + BY.length(),
                 orderByPosition);
         parts[2] = orderByPosition == methodName.length() ? EMPTY_STRING : methodName.substring(orderByPosition
                 + ORDER_BY.length());
         return parts;
     }
 
     private void appendSelectClause(final JpqlStringBuffer jpql, final String selectClause) {
         jpql.appendToken("SELECT");
         if (selectClause.contains(KeyWord.Distinct.name())) {
             jpql.appendKeyWord(KeyWord.Distinct, false);
         }
         jpql.appendToken("e FROM");
         jpql.appendToken(ENTITY_CLASS_PLACE_HOLDER);
         jpql.appendToken("e");
     }
 
     private void appendWhereClause(final JpqlStringBuffer jpql, final String whereClause) {
         jpql.appendToken("WHERE");
         boolean not = false;
         for (final String token : pattern.split(whereClause)) {
             try {
                 final KeyWord keyWord = KeyWord.valueOf(token);
                 if (KeyWord.Not == keyWord) {
                     not = true;
                     continue;
                 }
                 if (KeyWord.LOGICAL_OPERATORS.contains(keyWord)) {
                     jpql.appendEqualsIfNecessary();
                 }
                 if (KeyWord.OPERATORS.contains(keyWord)) {
                     jpql.appendProperty();
                 }
                 jpql.appendKeyWord(keyWord, not);
                 if (KeyWord.NOT_CONSUMERS.contains(keyWord)) {
                     not = false;
                 }
             } catch (final IllegalArgumentException e) {
                 jpql.newProperty(token);
             }
         }
         jpql.appendEqualsIfNecessary();
     }
 
     private void appendOrderClause(final JpqlStringBuffer jpql, final String orderClause) {
         jpql.appendToken("ORDER BY");
         for (final String token : pattern.split(orderClause)) {
             try {
                 jpql.appendKeyWord(KeyWord.valueOf(token), false);
             } catch (final IllegalArgumentException e) {
                 jpql.newProperty(token);
                 jpql.appendProperty();
             }
         }
     }
 
 }
 
 class JpqlStringBuffer {
     private static final Pattern CAMEL_CASE = Pattern
             .compile("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[A-Za-z])(?=[^A-Za-z])");
     private final IndexHolder indexHolder = new IndexHolder();
     private final StringBuffer jpql = new StringBuffer();
     private final Class<?> entityClass;
     private boolean newProperty;
     private String lastProperty;
 
     public JpqlStringBuffer(final Class<?> entityClass) {
         this.entityClass = entityClass;
     }
 
     public void appendKeyWord(final KeyWord keyWord, final boolean not) {
         appendToken(keyWord.getToken(not, indexHolder));
         newProperty = false;
     }
 
     public void appendEqualsIfNecessary() {
         if (newProperty) {
             appendProperty();
             appendKeyWord(KeyWord.Equal, false);
         }
     }
 
     public void newProperty(final String property) {
         lastProperty = property;
         newProperty = true;
     }
 
     public void appendProperty() {
         jpql.append("e");
        for (String property : splitProperty(lastProperty)) {
             jpql.append(".");
             jpql.append(StringUtils.uncapitalize(property));
         }
         jpql.append(" ");
     }
 
     private String[] splitProperty(final String propertyString) {
         if (propertyString.contains("_")) {
             return propertyString.split("_");
         }
 
         final List<String> properties = findProperties(entityClass, propertyString);
         if (properties == null) {
             throw new IllegalArgumentException("Could not parse properties from " + propertyString);
         }
         return properties.toArray(new String[0]);
     }
 
     private List<String> findProperties(final Class<?> clazz, final String propertyString) {
         String property = StringUtils.uncapitalize(propertyString);
 
         Field field = ReflectionUtils.findField(clazz, property);
         if (field != null) {
             return Collections.singletonList(property);
         }
 
         final String[] tokens = CAMEL_CASE.split(propertyString);
         int index = tokens.length;
         do {
            String token = tokens[--index];
             property = property.substring(0, propertyString.lastIndexOf(token));
 
             field = ReflectionUtils.findField(clazz, property);
             if (field != null) {
                 final List<String> subProperties = findProperties(field.getType(),
                         propertyString.substring(field.getName().length()));
                 if (subProperties != null) {
                     final List<String> properties = new ArrayList<String>();
                     properties.add(field.getName());
                     properties.addAll(subProperties);
                     return properties;
                 }
             }
         } while (index > 0);
 
         return null;
     }
 
     public void appendToken(final String token) {
         jpql.append(token);
         jpql.append(" ");
     }
 
     public String getJpql() {
         return jpql.toString();
     }
 }
 
 class IndexHolder {
     private int index = 1;
 
     public int get() {
         return index++;
     }
 }
 
 enum KeyWord {
     Distinct, And, Or, Not, Between("BETWEEN {} AND {}"), Is, Null("NULL"), Empty("EMPTY"), Member("MEMBER"), Of, LessThan(
             "< {}", ">= {}"), GreatThan("> {}", "<= {}"), Equal("= {}", "<> {}"), Like("LIKE {}"), In("IN {}"), Asc, Desc;
 
     public static final List<KeyWord> LOGICAL_OPERATORS = new ArrayList<KeyWord>();
     public static final List<KeyWord> OPERATORS = new ArrayList<KeyWord>();
     public static final List<KeyWord> NOT_CONSUMERS = new ArrayList<KeyWord>();
     private static final String POSITIONAL_PARAMETER_PLACE_HOLDER = "{}";
     private String token;
     private String notToken;
 
     static {
         LOGICAL_OPERATORS.add(KeyWord.And);
         LOGICAL_OPERATORS.add(KeyWord.Or);
 
         OPERATORS.add(KeyWord.Equal);
         OPERATORS.add(KeyWord.LessThan);
         OPERATORS.add(KeyWord.GreatThan);
         OPERATORS.add(KeyWord.Is);
         OPERATORS.add(KeyWord.Like);
         OPERATORS.add(KeyWord.In);
         OPERATORS.add(KeyWord.Between);
         OPERATORS.add(KeyWord.Member);
 
         NOT_CONSUMERS.add(KeyWord.Equal);
         NOT_CONSUMERS.add(KeyWord.LessThan);
         NOT_CONSUMERS.add(KeyWord.GreatThan);
         NOT_CONSUMERS.add(KeyWord.Null);
         NOT_CONSUMERS.add(KeyWord.Empty);
         NOT_CONSUMERS.add(KeyWord.Like);
         NOT_CONSUMERS.add(KeyWord.In);
         NOT_CONSUMERS.add(KeyWord.Between);
         NOT_CONSUMERS.add(KeyWord.Member);
     }
 
     private KeyWord(final String token, final String notToken) {
         this.token = token;
         this.notToken = notToken;
     }
 
     private KeyWord(final String token) {
         this(token, "NOT " + token);
     }
 
     private KeyWord() {
         token = name().toUpperCase(Locale.getDefault());
         notToken = token;
     }
 
     public String getToken(final boolean not, final IndexHolder indexHoder) {
         final String result = not ? getNotToken() : getToken();
         return replacePositionalParameters(result, indexHoder);
     }
 
     private String replacePositionalParameters(final String result, final IndexHolder indexHoder) {
         final StringBuffer buffer = new StringBuffer();
         int i = 0;
         while (true) {
             final int j = result.indexOf(POSITIONAL_PARAMETER_PLACE_HOLDER, i);
             if (j == -1) {
                 break;
             }
 
             buffer.append(result.substring(i, j));
             buffer.append("?");
             buffer.append(indexHoder.get());
             i = j + POSITIONAL_PARAMETER_PLACE_HOLDER.length();
         }
         buffer.append(result.substring(i));
         return buffer.toString();
     }
 
     private String getToken() {
         return token;
     }
 
     private String getNotToken() {
         return notToken;
     }
 }
