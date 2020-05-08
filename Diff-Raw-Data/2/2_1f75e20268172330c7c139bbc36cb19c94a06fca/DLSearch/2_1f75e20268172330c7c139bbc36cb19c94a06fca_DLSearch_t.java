 package cz.datalite.dao;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.*;
 
 import cz.datalite.helpers.ReflectionHelper;
 import cz.datalite.helpers.StringHelper;
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Projection;
 import org.hibernate.sql.JoinType;
 
 import javax.persistence.Embedded;
 
 /**
  * <p>Class for transfer filter parameters like criterion, sort, paging and projection.</p>
  * <p>This class is using Hibernate utils.</p>
  * @param <T> root class in definition hierarchy
  * @author Karel Cemus
  */
 public class DLSearch<T> {
     // when creating automatic alias for full path, use this suffix for alias naming (e.g. 'propertyAlias')
     private String ALIAS_SUFFIX = "Alias";
 
     /** Criterions in filter */
     private final List<Criterion> criterions = new LinkedList<Criterion>();
     /** Sorts - order by definition */
     private List<DLSort> sorts;
     /** Requested rowCount - if it is 0 then request all rows*/
     private int rowCount;
     /** First row in selection */
     private int firstRow;
     /** Return distinct values. */
     private boolean distinct;
     /** Aliases are used for hierarchy structure */
     private final Set<Alias> aliases = new HashSet<Alias>();
     /** Projection type like distinct or row count */
     private final List<Projection> projections = new LinkedList<Projection>();
     /** Defines constant for disabled paging */
     public static final int NOT_PAGING = -1;
 
     /** Class of the main entity. If persistence class is set, all properties are validated against this class.
      *  It is mandatory to set persistentClass if @Embedded annotation is used - we need to check if embeddable
      *  */
     private Class<T> persistentClass = null;
 
     /**
      * Create DLSearch
      * @param rowCount requested row count
      * @param firstRow index of 1st row (starts at 0)
      */
     public DLSearch( final int rowCount, final int firstRow ) {
         this( new LinkedList<DLSort>(), rowCount, firstRow );
     }
 
     /**
      * Create DLSearch
      */
     public DLSearch() {
         this( new LinkedList<DLSort>() );
     }
 
     /**
      * Create DLSearch
      * @param sorts order by definition
      */
     public DLSearch( final List<DLSort> sorts ) {
         this( sorts, 0, DLSearch.NOT_PAGING );
     }
 
     /**
      * Create DLSearch
      * @param sorts order by definition
      * @param rowCount requested row count
      * @param firstRow index of 1st row (starts at 0)
      */
     public DLSearch( final List<DLSort> sorts, final int rowCount, final int firstRow ) {
         this( sorts, rowCount, firstRow, null );
     }
 
     /**
      * Create DLSearch
      * @param sorts order by definition
      * @param rowCount requested row count
      * @param firstRow index of 1st row (starts at 0)
      * @param persistentClass if persistence class is set, all properties are validated against this class
      */
     public DLSearch( final List<DLSort> sorts, final int rowCount, final int firstRow, Class<T> persistentClass ) {
         this.sorts = sorts;
         this.rowCount = rowCount;
         this.firstRow = firstRow;
         this.persistentClass = persistentClass;
     }
 
     /**
      * Remove all filter criterions
      */
     public void clearFilterCriterions() {
         this.criterions.clear();
     }
 
     /**
      * Add all filter criterions
      * @param criterions filter criterions
      */
     public void addFilterCriterions( final List<Criterion> criterions ) {
         this.criterions.addAll( criterions );
     }
 
     /**
      * Add filter criterion
      * @param criterion filter criterion
      */
     public void addFilterCriterion( final Criterion criterion ) {
         this.criterions.add( criterion );
     }
 
     /**
      * Remove filter criterion
      * @param criterion filter criterion
      */
     public void removeFilterCriterions( final Criterion criterion ) {
         this.criterions.remove( criterion );
     }
 
     /**
      * Returns index of 1st row
      * @return index of 1st row, starts at 0
      */
     public int getFirstRow() {
         return firstRow;
     }
 
     /**
      * Set first row index - starts at 0
      * @param firstRow row index
      */
     public void setFirstRow( final int firstRow ) {
         this.firstRow = firstRow;
     }
 
     /**
      * Requested row count. If 0 then request all results.
      * @return row count - max result list length
      */
     public int getRowCount() {
         return rowCount;
     }
 
     /**
      * Set requested row count. If 0 then request all results.
      * @param rowCount requested row count
      */
     public void setRowCount( final int rowCount ) {
         this.rowCount = rowCount;
     }
 
     /**
      * Returns iterator for all sorts. It is ordered structure.
      * @return iterator on ordered structure
      */
     public Iterator<DLSort> getSorts() {
         return sorts.iterator();
     }
 
     /**
      * Set order by definition. It is ordered structure
      * @param sorts order by definitions
      */
     public void setSorts( final List<DLSort> sorts ) {
         this.sorts = sorts;
     }
 
     /**
      * Add order by definition. It is ordered structure so this metod append
      * on the tail.
      * @param sort order by definition
      */
     public void addSort( final DLSort sort ) {
         this.sorts.add( sort );
     }
 
     /**
      * Add order by definition by column ascending. It is ordered structure so this metod append
      * on the tail.
      * This method is shortcut for addSort(new DLSort(column, DLSortType.ASCENDING)).
      * @param column
      */
     public void addSort( final String column ) {
         addSort( new DLSort( column, DLSortType.ASCENDING ) );
     }
 
     /**
      * Odstraní všechny informace o sortování
      */
     public void clearSorts() {
         this.sorts.clear();
     }
 
     /**
      * Returns unordered criterions.
      * @return collection of criterions
      */
     public Collection<Criterion> getCriterions() {
         return criterions;
     }
 
     /**
      * Adds all aliases for full field path with INNER_JOIN type.
      * Use this method to obtain alias for Restrictions property names.
      *
      * <p>If alias already exists, it is modified to INNER_JOIN and it's name is returned. Otherwise new alias
      * is created with default name.</p>
      *
      * @param fullPath full field path (e.g. 'property', 'property.innerProperty', 'property.innerProperty.moreProperty')
      * @return existing or new alias name
      */
     public String addAliases( final String fullPath ) {
         return addAliases(fullPath, JoinType.INNER_JOIN);
     }
 
 
     /**
      * Adds all aliases for full field path with custom type. Typical usage is to get alias to create Restriction.
      *
      * @param fullPath full field path (e.g. 'property', 'property.innerProperty', 'property.innerProperty.moreProperty')
      * @param joinType {@link JoinType#FULL_JOIN}, {@link JoinType#LEFT_OUTER_JOIN} {@link JoinType#INNER_JOIN}
      * @return existing or new alias name
      */
     public String addAliases( final String fullPath, final JoinType joinType ) {
         // actual alias while traversing fullPath
         Alias parentAlias = null;
 
         String embeddablePrefix = "";
         Class lastEmbeddableClass = null;
         for (String property : parsePath( fullPath )) {
             // resolve current path (parentAlias + embeddablePrefix + property)
             String currentPath;
             if (parentAlias == null) {
                 if (!StringHelper.isNull(embeddablePrefix))
                     currentPath = embeddablePrefix.substring(1) + "." + property;
                 else
                     currentPath = property;
             } else {
                 currentPath = parentAlias.getAlias() + embeddablePrefix + "." + property                ;
             }
 
             Alias newAlias = getAliasForPath(currentPath);
 
             if (newAlias == null) {
 
                 Class clazz = lastEmbeddableClass != null ? lastEmbeddableClass :
                               parentAlias != null ? parentAlias.getPersistentClass() :
                                 persistentClass;
                 if (isEmbeddableField(clazz, property)) {
                     // skip embeddable field - it is similar to normal join (dot separated),
                     // but alias should be created only for a whole embeddable proeprty
                     // e.g. 'embeddable.property' should remain as single alias an will be named 'embeddable#propertyAlias'
                     embeddablePrefix = embeddablePrefix + "." + property;
                     lastEmbeddableClass = resolvePropertyClass(clazz, property);
                     continue;
                 }
 
                 // alias name is property name + alias suffix constant. If it contains dot, it is embedded property
                 // and we need to somehow escape, because it is not valid alias name - use # instead.
                 String aliasName = property.replace(".", "#") + ALIAS_SUFFIX;
 
                 // check duplicit alias name, prefix with full path. For root property s
                 // (e.g. 'prop.inner.prop' -> #prop#inner#propAlias)
                 if (getAlias(aliasName) != null) {
                     aliasName = "#" + fullPath.replace(".", "#") + ALIAS_SUFFIX;
                 }
 
                 newAlias = addAlias( currentPath, aliasName, joinType, resolvePropertyClass(clazz, property) );
             }
 
             parentAlias = newAlias;
             embeddablePrefix = "";
             lastEmbeddableClass = null;
         }
 
         return parentAlias != null ? parentAlias.getAlias() : null;
     }
 
     /**
      * Enforce eager fetching for full field path .
      *
      * <p>Note: this method is currently implemented as addAlias(fullPath, JoinType.LEFT_OUTER_JOIN).
      * However, this may change in the future and you should always create alias manually if you need
      * alias for sorting or querying.</p>
      *
      * @param  paths property address - contains max. one dot (e.g. 'property', 'myExistingAlias.property')
      * @return existing or new alias name
      */
     public void addFetch( final String ... paths) {
         for (String path : paths)
             addAlias(path, JoinType.LEFT_OUTER_JOIN);
     }
 
     /**
      * Enforce eager fetching for full field path .
      *
      * <p>Note: this method is currently implemented as addAlias(fullPath, JoinType.LEFT_OUTER_JOIN).
      * However, this may change in the future and you should always create alias manually if you need
      * alias for sorting or querying.</p>
      *
      * @param fullPaths full field path (e.g. 'property', 'property.innerProperty', 'property.innerProperty.moreProperty')
      * @return existing or new alias name
      */
     public void addFetches( final String ... fullPaths) {
         for (String fullPath : fullPaths)
             addAliases(fullPath, JoinType.LEFT_OUTER_JOIN);
     }
 
     /**
      * Add an alias with INNERT join type. {@see addAlias(String, String, JoinType)}
      *
      * @param path property address - contains max. one dot (e.g. 'property', 'myExistingAlias.property')
      * @throws java.lang.IllegalArgumentException if alias with different name for this path is already defined.
      * @return existing or new alias name
      */
     public String addAlias( final String path ) {
         return addAlias(path, JoinType.INNER_JOIN);
     }
 
     /**
      * Add an alias with custom join type. {@see addAlias(String, String, JoinType)}
      *
      * @param path table address - contains max. one dot (e.g. 'property', 'myExistingAlias.property')
      * @throws java.lang.IllegalArgumentException if alias with different name for this path is already defined.
      * @return existing or new alias name
      */
     public String addAlias( final String path, JoinType joinType ) {
         StringBuilder fullPath = new StringBuilder();
 
         // expand full Path
         for (String part : parsePath(path)) {
             if (fullPath.length() > 0)
                 fullPath.append(".");
           if (getAlias(part) != null)
               fullPath.append(getAlias(part).getFullPath());
           else
               fullPath.append(part);
         }
 
         // and call full path variant
         return addAliases( fullPath.toString(), joinType );
     }
 
      /**
      * Add an alias with INNER_JOIN and custom alias name. {@see addAlias(String, String, JoinType)}
       *
      * @param path table address - contains max. one dot (e.g. 'property', 'myExistingAlias.property')
      * @param alias alias for the path
      * @throws java.lang.IllegalArgumentException if alias with different name for this path is already defined.
      */
     public void addAlias( final String path, final String alias ) {
         addAlias( path, alias, JoinType.INNER_JOIN );
     }
 
 
     /**
      * Add an alias with custom join type and custom alias name.
      * <br/>
      * It the alias is already defined:<ul>
      *  <li>with different alias name, an exception is thrown</li>
      *  <li>with same alias name, but less restrictive JoinType (i.e. existing alias LEFT_OUTER_JOIN, new type is
      *      INNER_JOIN), current JoinType is changed</li>
      *  <li>same alias name, same or more restrictive JoinType - nothing is done</li>
      * </ul>
      *
      * <p>Notes: <br/>Alias must contain EXACTLY one dot.<br/>Alias name
      * mustn't be same like some field name.</p>
      *
      * <h2>Example:</h2>
      * <i>path:</i> human.hobby<br/>
      * <i>alias:</i> hobby<br/>
      * <h2>Example:</h2>
      * <i>path:</i> hobby.type<br/>
      * <i>alias:</i> typeAlias<br/>
      * @param path table address - contains exactly one dot
      * @param alias alias for the path
      * @param joinType {@link JoinType#FULL_JOIN}, {@link JoinType#LEFT_OUTER_JOIN} {@link JoinType#INNER_JOIN}
      *
      * @throws java.lang.IllegalArgumentException if alias with different name for this path is already defined.
      */
     public Alias addAlias( final String path, final String alias, final JoinType joinType ) {
         return addAlias(path, alias, joinType, null);
     }
 
     // same as previous method with prefilled propertyClass. If null, it is resolved from property
     protected Alias addAlias( final String path, final String alias, final JoinType joinType, Class propertyClass ) {
 
         // parse 'hobby.type' to type. It may contain multiple dots if embedded property.
         String pathWithoutProperty = null;
         String property = path;
         if (path.indexOf( '.' )  != -1) {
             pathWithoutProperty = path.substring( 0,  path.indexOf( '.' ) );
             property = path.substring(  path.indexOf( '.' ) + 1 );
         }
 
 
         Alias resolvedAlias = getAliasForPath(path);
         if (resolvedAlias != null) {
             // alias already defined, check it's setup
             if (!resolvedAlias.getAlias().equals(alias)) {
                 throw new IllegalArgumentException("Unable to create alias '" + alias +
                         "'Existing alias with different name '" + resolvedAlias.getAlias() +
                         "' defined for path '" + path + "'.");
             } else if (joinType.equals(JoinType.INNER_JOIN)) {
                 // ensure most strict join type
                 resolvedAlias.setJoinType(joinType);
             }
         } else {
             // create alias
             if (getAlias(alias) != null) {
                 throw new IllegalArgumentException("Unable to create alias '" + alias +
                         "'. Existing alias with different path '" + getAlias(alias).getPath() +
                         "' and full path '" + getAlias(alias).getFullPath() + "'.");
             }
 
             Alias parent = getAlias(pathWithoutProperty);
             if (parent != null) {
                 // new alias below parent
                 String fullPath = parent.getFullPath() + "." + property;
                 String aliasPath = parent.getAlias() + "." + property;
                 resolvedAlias = new Alias( fullPath, aliasPath, alias, joinType );
                 resolvedAlias.setPersistentClass(propertyClass != null ? propertyClass :
                         resolvePropertyClass(parent.getPersistentClass(), property));
             } else {
                 // new root alias
                 resolvedAlias = new Alias( path, path, alias, joinType );
                 resolvedAlias.setPersistentClass(propertyClass != null ? propertyClass :
                         resolvePropertyClass(persistentClass, property));
             }
 
             aliases.add( resolvedAlias );
         }
 
         return resolvedAlias;
     }
 
 
     /**
      * Returns alias for a path.
      *
      * @param alias alias name
      * @return existing alias or null if not exists.
      */
     public Alias getAlias(final String alias) {
         for ( Alias a : aliases ) {
             if ( a.getAlias().equals( alias ) ) {
                 return a;
             }
         }
         return null;
     }
 
     /**
      * Returns alias for the a path.
      *
      * @param path alias path (e.g. 'parentAlias.property')
      * @return alias for this path or null
      */
     public Alias getAliasForPath(final String path) {
         for ( Alias a : aliases ) {
             if ( a.getPath().equals( path ) ) {
                 return a;
             }
         }
         return null;
     }
 
     /**
      * Returns alias for the full path.
      *
      * @param fullPath full path (e.g. 'property' or 'property.innerProperty')
      * @return alias for this path or null
      */
     public String getAliasForFullPath(final String fullPath) {
         final String path = fullPath.lastIndexOf( '.' ) == -1 ? "" : fullPath.substring( 0, fullPath.lastIndexOf( '.' ) );
         for ( Alias a : aliases ) {
             if ( a.getFullPath().equals( path ) ) {
                 return a.getAlias() + '.' + fullPath.substring( fullPath.lastIndexOf( '.' ) + 1 );
             }
         }
         return fullPath;
     }
     /**
 
      * Returns aliases for path whole path to the field
      * @param fullPath  whole path with dots
      * @return required subpaths
      */
     public String[] parsePath( final String fullPath ) {
         // path: sth1.sth2.sth3.sth4.field
         return fullPath.split( "\\." );
     }
 
 
     /**
      * Returns all registered aliases.
      * @return collection with aliases
      */
     public Set<Alias> getAliases() {
         return aliases;
     }
 
 
     /**
      * Add projection like distinct or row count
      * @param projection hibernate projection
      */
     public void addProjection( final Projection projection ) {
         projections.add( projection );
     }
 
     /**
      * Return collection (unordered) with all projections
      * @return all defined projections
      */
     public Collection<Projection> getProjections() {
         return projections;
     }
 
 
     protected Class resolvePropertyClass(Class parent, String property) {
         if (parent == null)
             return null;
 
         try {
             Field field = ReflectionHelper.getDeclaredField(parent, property);
             return field.getType();
         } catch (NoSuchFieldException e) {
             Method method = ReflectionHelper.getFieldGetter(parent, property);
             if (method == null)
                 throw new IllegalArgumentException("Class " + parent + " does not contain property " + property);
 
             return method.getReturnType();
         }
     }
 
    public boolean isEmbeddableField(Class clazz, String property) {
        // if class is not known, there is no way how to check embeddable
        if (clazz == null)
            return false;
 
         // check annotation on the field
         Field field = null;
         try {
             field = ReflectionHelper.getDeclaredField(clazz, property);
         } catch (NoSuchFieldException e) { }
 
         if (field != null && ReflectionHelper.findAnnotation(field, Embedded.class) != null)
             return true;
 
         // if not found, check the method
         Method method = ReflectionHelper.getFieldGetter(clazz, property);
         if (method != null && ReflectionHelper.findAnnotation(method, Embedded.class) != null)
             return true;
 
         // neither field nor property - this property is not found at all!
         if (field == null && method == null)
             throw new IllegalArgumentException("Class " + clazz + " does not contain property " + property);
 
         return false;
     }
 
     /**
      * Add aliases for path with property (e.g. 'entity.inner.property')
      *
      * @param pathWithProperty full path with property
      * @return alias.property (e.g. 'innerAlias.property')
      */
     public String addAliasesForProperty(String pathWithProperty) {
         return addAliasesForProperty(pathWithProperty, JoinType.INNER_JOIN);
     }
 
     /**
      * Add aliases for path with property (e.g. 'entity.inner.property')
      *
      * @param pathWithProperty full path with property
      * @param joinType join type
      * @return alias.property (e.g. 'innerAlias.property')
      */
     public String addAliasesForProperty(String pathWithProperty, JoinType joinType) {
         String[] path = parsePath(pathWithProperty);
         if (path.length > 1) {
           return addAliases(pathWithProperty.substring(0, pathWithProperty.lastIndexOf(".")), joinType)
                    + "." + path[path.length-1];
         } else {
             return pathWithProperty;
         }
     }
 
 
     public static class Alias {
 
         protected String _fullPath;
         protected String _path;
         protected String _alias;
 
         protected JoinType _joinType;
 
 
         // associated entity to alias (heuristics only, may not be known)
         protected Class _persistentClass;
 
         /**
          * @deprecated as of Hibernate 4 user JoinType
          */
         @Deprecated
         public Alias( final String fullPath, final String path, final String alias, final int joinType ) {
             this._fullPath = fullPath;
             this._path = path;
             this._alias = alias;
             this._joinType = JoinType.parse(joinType);
         }
 
         public Alias( final String fullPath, final String path, final String alias, final JoinType joinType) {
             this._fullPath = fullPath;
             this._path = path;
             this._alias = alias;
             this._joinType = joinType;
         }
 
         public String getAlias() {
             return _alias;
         }
 
         public void setAlias( final String alias ) {
             this._alias = alias;
         }
 
         public Class getPersistentClass() {
             return _persistentClass;
         }
 
         public void setPersistentClass(Class persistentClass) {
             this._persistentClass = persistentClass;
         }
 
         /**
          * @deprecated as of Hibernate 4 user JoinType
          */
         @Deprecated
         public int getJoinTypeCriteria() {
             switch (_joinType) {
                 case INNER_JOIN: return Criteria.INNER_JOIN;
                 case LEFT_OUTER_JOIN: return Criteria.LEFT_JOIN;
                 case FULL_JOIN:return Criteria.FULL_JOIN;
 
                 default: throw new IllegalStateException("JoinType not supported: " + _joinType);
             }
 
         }
 
         /**
          * @deprecated as of Hibernate 4 user JoinType
          */
         @Deprecated
         public void setJoinTypeCriteria( final int joinType ) {
             this._joinType = JoinType.parse(joinType);
         }
 
         public JoinType getJoinType() {
             return _joinType;
         }
 
         public void setJoinType(JoinType joinTypeJpa) {
             this._joinType = joinTypeJpa;
         }
 
         public String getPath() {
             return _path;
         }
 
         public void setPath( final String path ) {
             this._path = path;
         }
 
         public String getFullPath() {
             return _fullPath;
         }
 
         public void setFullPath( final String _fullPath ) {
             this._fullPath = _fullPath;
         }
 
         @Override
         public boolean equals( final Object obj ) {
             if ( obj == null ) {
                 return false;
             }
             if ( getClass() != obj.getClass() ) {
                 return false;
             }
             final Alias other = ( Alias ) obj;
             if ( (this._path == null) ? (other._path != null) : !this._path.equals( other._path ) ) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int hash = 3;
             hash = 23 * hash + (this._path == null ? 0 : this._path.hashCode());
             return hash;
         }
     }
 
     public boolean isDistinct() {
         return distinct;
     }
 
     public void setDistinct( final boolean distinct ) {
         this.distinct = distinct;
     }
 
 
     /**
      * <p>Adds alias name for your property. If alias is already defined
      * nothing is done. Else your entered alias is added.</p>
      * <p>Notes: <br/>Alias must containt EXACTLY one dot.<br/>Alias name
      * mustn't be same like some field name.</p>
      * <h2>Example:</h2>
      * <i>path:</i> human.hobby<br/>
      * <i>alias:</i> hobby<br/>
      * <h2>Example:</h2>
      * <i>path:</i> hobby.type<br/>
      * <i>alias:</i> typeAlias<br/>
      * @param path table address - contains exactly one dot
      * @param alias pseudonym for the path
      * @param joinType {@link org.hibernate.criterion.CriteriaSpecification#FULL_JOIN},
      * {@link org.hibernate.criterion.CriteriaSpecification#INNER_JOIN},
      * {@link org.hibernate.criterion.CriteriaSpecification#LEFT_JOIN}
      * @deprecated as of Hibernate 4  use JoinType object
      */
     @Deprecated
     public void addAlias( final String path, final String alias, final int joinType ) {
         addAlias(path, alias, JoinType.parse(joinType));
     }
 
     /**
      * <p>Adds alias name for your property. If alias is already defined
      * nothing is done. Else your entered alias is added. Calls addAlias(path, path, joinType).</p>
      * @param path table address - contains exactly one dot
      * @param joinType {@link org.hibernate.criterion.CriteriaSpecification#FULL_JOIN},
      * {@link org.hibernate.criterion.CriteriaSpecification#INNER_JOIN},
      * {@link org.hibernate.criterion.CriteriaSpecification#LEFT_JOIN}
      * @deprecated as of Hibernate 4 use JPA JoinType
      */
     @Deprecated
     public void addAlias( final String path, final int joinType ) {
         addAlias( path, JoinType.parse(joinType) );
     }
 
 }
 
