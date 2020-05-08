 package org.polyforms.repository.jpa.query;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.WeakHashMap;
 import java.util.regex.Pattern;
 
 import org.polyforms.repository.ExecutorPrefixHolder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.Assert;
 import org.springframework.util.ReflectionUtils;
 import org.springframework.util.StringUtils;
 
 /**
  * Helper to create JPQL.
  * 
  * @author Kuisong Tong
  * @since 1.0
  */
 abstract class JpqlQueryStringBuilder {
     private static final Logger LOGGER = LoggerFactory.getLogger(JpqlQueryStringBuilder.class);
     protected static final Pattern PATTERN;
     private static final int NUMBER_OF_PARTS = 3;
     private static final String EMPTY_STRING = "";
     private static final String ORDER_BY = "OrderBy";
     private static final String BY = "By";
     private final Map<Class<?>, Map<String, String>> queryStringCache = new HashMap<Class<?>, Map<String, String>>();
     private final ExecutorPrefixHolder executorPrefix;
 
     static {
         final StringBuffer keyWords = new StringBuffer();
         for (final KeyWord keyWord : KeyWord.values()) {
             if (keyWords.length() > 0) {
                 keyWords.append("|");
             }
             keyWords.append(keyWord.name());
         }
         PATTERN = Pattern.compile(String.format("(?<=[a-z])((?<=%1$s)|(?=%1$s))", keyWords.toString()));
         LOGGER.debug("The PATTERN is {}", PATTERN.pattern());
     }
 
     protected JpqlQueryStringBuilder(final ExecutorPrefixHolder executorPrefix) {
         this.executorPrefix = executorPrefix;
     }
 
     protected String getQuery(final Class<?> entityClass, final String queryString) {
         if (!queryStringCache.containsKey(entityClass)) {
             queryStringCache.put(entityClass, new WeakHashMap<String, String>());
         }
 
        String query = queryStringCache.get(entityClass).get(queryString);
		if (query == null) {
             LOGGER.trace("Cache miss for query {}.", queryString);
             final String[] parts = split(nomalizeQueryString(queryString));
             LOGGER.debug("The parts of {} are {}.", queryString, Arrays.toString(parts));
 
             final JpqlStringBuffer jpql = new JpqlStringBuffer(entityClass);
             appendSelectClause(jpql, parts[0]);
             if (StringUtils.hasText(parts[1])) {
                 appendWhereClause(jpql, parts[1]);
             }
             if (StringUtils.hasText(parts[2])) {
                 appendOrderClause(jpql, parts[2]);
             }
            query = jpql.getJpql();
            queryStringCache.get(entityClass).put(queryString, query);
         }
 
        return query;
     }
 
     private String nomalizeQueryString(final String queryString) {
         return StringUtils.capitalize(executorPrefix.removePrefixIfAvailable(queryString));
     }
 
     protected abstract void appendSelectClause(final JpqlStringBuffer jpql, final String selectClause);
 
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
 
     private void appendWhereClause(final JpqlStringBuffer jpql, final String whereClause) {
         jpql.appendToken("WHERE");
         boolean not = false;
         for (final String token : PATTERN.split(whereClause)) {
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
         for (final String token : PATTERN.split(orderClause)) {
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
     private static final String DEFAULT_ALIAS = "e";
     private final IndexHolder indexHolder = new IndexHolder();
     private final StringBuffer jpql = new StringBuffer();
     private final Class<?> entityClass;
     private final String alias;
     private boolean isNewProperty;
     private String lastProperty;
 
     protected JpqlStringBuffer(final Class<?> entityClass) {
         this(entityClass, DEFAULT_ALIAS);
     }
 
     protected JpqlStringBuffer(final Class<?> entityClass, final String alias) {
         this.entityClass = entityClass;
         this.alias = alias;
     }
 
     protected void appendKeyWord(final KeyWord keyWord, final boolean not) {
         appendToken(keyWord.getToken(not, indexHolder));
         isNewProperty = false;
     }
 
     protected void appendEntity() {
         appendToken(entityClass.getSimpleName());
     }
 
     protected void appendAlias() {
         appendToken(alias);
     }
 
     protected void appendEqualsIfNecessary() {
         if (isNewProperty) {
             appendProperty();
             appendKeyWord(KeyWord.Equal, false);
         }
     }
 
     protected void newProperty(final String property) {
         lastProperty = property;
         isNewProperty = true;
     }
 
     protected void appendProperty() {
         jpql.append(alias);
         for (final String property : splitProperty(lastProperty)) {
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
         Assert.notNull(properties, "Could not parse properties from " + propertyString);
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
             final String token = tokens[--index];
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
 
     protected void appendToken(final String token) {
         jpql.append(token);
         jpql.append(" ");
     }
 
     protected String getJpql() {
         return jpql.toString();
     }
 }
 
 class IndexHolder {
     private int index = 1;
 
     protected int get() {
         return index++;
     }
 }
 
 enum KeyWord {
     Distinct, And, Or, Not, Between("BETWEEN {} AND {}"), Is, Null("NULL"), Empty("EMPTY"), Member("MEMBER"), Of, LessThan(
             "< {}", ">= {}"), GreatThan("> {}", "<= {}"), Equal("= {}", "<> {}"), Like("LIKE {}"), In("IN {}"), Asc, Desc;
 
     protected static final List<KeyWord> LOGICAL_OPERATORS = new ArrayList<KeyWord>();
     protected static final List<KeyWord> OPERATORS = new ArrayList<KeyWord>();
     protected static final List<KeyWord> NOT_CONSUMERS = new ArrayList<KeyWord>();
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
 
     protected String getToken(final boolean not, final IndexHolder indexHoder) {
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
