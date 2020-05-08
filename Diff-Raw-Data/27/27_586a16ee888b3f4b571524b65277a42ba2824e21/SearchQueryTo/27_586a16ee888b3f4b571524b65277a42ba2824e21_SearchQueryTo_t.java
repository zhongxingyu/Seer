 /**
  * Copyright (C) [2013] [The FURTHeR Project]
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.utah.further.core.query.domain;
 
 import static edu.utah.further.core.api.collections.CollectionUtil.newList;
 import static edu.utah.further.core.api.text.ToStringCustomStyles.SHORT_WITH_SPACES_STYLE;
 import static org.slf4j.LoggerFactory.getLogger;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlElements;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 import javax.xml.bind.annotation.adapters.XmlAdapter;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.slf4j.Logger;
 
 import edu.utah.further.core.api.collections.CollectionUtil;
 import edu.utah.further.core.api.context.Implementation;
 import edu.utah.further.core.api.data.PersistentEntity;
 import edu.utah.further.core.api.lang.CopyableFrom;
 import edu.utah.further.core.api.xml.XmlNamespace;
 import edu.utah.further.core.query.util.SearchQueryUtil;
 
 /**
  * An search query transfer object.
  * <p>
  * This class must be public because it is used in TOs outside this package, e.g.
  * <code>QueryContextTo</code>. It would have been better to make it package-private if
  * Java had a "friend class" feature. In that case, <code>QueryContextTo</code> would be a
  * friend of this class.
  * -----------------------------------------------------------------------------------<br>
  * (c) 2008-2013 FURTHeR Project, Health Sciences IT, University of Utah<br>
  * Contact: {@code <further@utah.edu>}<br>
  * Biomedical Informatics, 26 South 2000 East<br>
  * Room 5775 HSEB, Salt Lake City, UT 84112<br>
  * Day Phone: 1-801-581-4080<br>
  * -----------------------------------------------------------------------------------
  * 
  * @author Oren E. Livne {@code <oren.livne@utah.edu>}
  * @version Apr 2, 2009
  */
 @Implementation
 // ============================
 // JAXB annotations
 // ============================
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlType(name = "", propOrder =
 { "id", "rootCriterion", "sortCriteria", "aliases", "firstResult", "maxResults" })
 @XmlRootElement(namespace = XmlNamespace.CORE_QUERY, name = SearchQueryTo.ENTITY_NAME)
 public final class SearchQueryTo implements SearchQuery,
 		CopyableFrom<SearchQuery, SearchQueryTo>
 {
 	// ========================= CONSTANTS =================================
 
 	/**
 	 * @serial Serializable version identifier.
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * A logger that helps identify this class' printouts.
 	 */
 	@SuppressWarnings("unused")
 	private static final Logger log = getLogger(SearchQueryTo.class);
 
 	/**
 	 * XML name of this entity.
 	 */
 	static final String ENTITY_NAME = "query";
 
 	// ========================= FIELDS ====================================
 
 	/**
 	 * An optional ID to help map {@link Map}s with {@link SearchQueryTo} values.
 	 * <p>
 	 * A general, less intrusive approach (but far more complex and therefore not used
 	 * here) is to create dynamic proxies using bytecode generation (e.g. Jen) to create
 	 * an object decorator of {@link SearchQueryTo} into a map entry object that adds the
 	 * ID field, retains all other JAXB annotations of this class, and can be used by an
 	 * {@link XmlAdapter} of a {@link Map} <code><Long,SearchQueryTo></code>.
 	 */
 	@XmlAttribute(name = "qid", required = false)
 	private Long id;
 
 	/**
 	 * The root entity/object which this query is based off of, not to be confused with
 	 * the root criterion. This is the root object, not criterion.
 	 */
 	@XmlAttribute(required = true)
 	private String rootObject;
 
 	/**
 	 * A criterion to be decorated by this object.
 	 */
 	@XmlElement(name = "rootCriterion", required = false)
 	private SearchCriterionTo rootCriterion;
 
 	/**
 	 * List of sort criteria.
 	 */
 	@XmlElementWrapper(name = "sortCriteria", required = false)
 	@XmlElements(
 	{ @XmlElement(name = SortCriterionTo.ENTITY_NAME, type = SortCriterionTo.class) })
 	private List<SortCriterionTo> sortCriteria;
 
 	/**
 	 * A map of alias definitions.
 	 */
	@XmlElementWrapper(name = "aliases")
 	@XmlElement(name = "alias", required = false)
 	private List<SearchQueryAliasTo> aliases;
 
 	/**
 	 * The first result to be retrieved.
 	 */
 	@XmlElement(name = "firstResult", required = false)
 	private Integer firstResult;
 
 	/**
 	 * A limit upon the number of objects to be retrieved.
 	 */
 	@XmlElement(name = "maxResults", required = false)
 	private Integer maxResults;
 
 	// ========================= CONSTRUCTORS ==============================
 
 	/**
 	 * A no-argument constructor. Initializes no fields.
 	 */
 	private SearchQueryTo()
 	{
 		super();
 	}
 
 	/**
 	 * A no-argument constructor. Initializes no fields.
 	 */
 	public static SearchQueryTo newInstance()
 	{
 		return new SearchQueryTo();
 	}
 
 	/**
 	 * A copy-constructor.
 	 * 
 	 * @param other
 	 *            other object to deep-copy fields from
 	 */
 	public static SearchQueryTo newCopy(final SearchQuery other)
 	{
 		return new SearchQueryTo().copyFrom(other);
 	}
 
 	/**
 	 * Create a search query object.
 	 * 
 	 * @param builder
 	 *            query builder to read query fields from into this object
 	 */
 	public static SearchQueryTo newInstance(final SearchQueryBuilderImpl builder)
 	{
 		final SearchQueryTo instance = newInstance();
 		instance.setRootCriterion(builder.rootCriterion)
 				.setSortCriteria(builder.sortCriteria)
 				.setAliases(builder.aliases)
 				.setFirstResult(builder.firstResult);
 		instance.setMaxResults(builder.maxResults);
 		instance.setId(builder.id);
 		instance.setRootObjectName(builder.rootObject);
 		return instance;
 	}
 
 	// ========================= IMPLEMENTATION: Object ====================
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString()
 	{
 		return new ToStringBuilder(this, SHORT_WITH_SPACES_STYLE)
 				.append("qid", id)
 				.append("rootObject", rootObject)
 				.append("rootCriterion", rootCriterion)
 				.append("sortCriteria", sortCriteria)
 				.append("aliases", aliases)
 				.append("firstResult", firstResult)
 				.append("maxResults", maxResults)
 				.toString();
 	}
 
 	/**
 	 * @param obj
 	 * @return
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(final Object obj)
 	{
 		if (this == obj)
 		{
 			return true;
 		}
 		if (obj == null)
 		{
 			return false;
 		}
 		if (getClass() != obj.getClass())
 		{
 			return false;
 		}
 		final SearchQueryTo that = (SearchQueryTo) obj;
 		return new EqualsBuilder()
 				.append(this.id, that.id)
 				.append(this.rootObject, that.rootObject)
 				.append(this.rootCriterion, that.rootCriterion)
 				.append(this.sortCriteria, that.sortCriteria)
 				.append(this.aliases, that.aliases)
 				.append(this.firstResult, that.firstResult)
 				.append(this.maxResults, that.maxResults)
 				.isEquals();
 	}
 
 	/**
 	 * Two {@link SearchQueryTo} objects must be equal <i>if and only if</i> their hash
 	 * codes are equal. This is important because a dependent object (e.g. a query
 	 * context) may decide whether to deep-copy its search query field based on hash code
 	 * equality.
 	 * 
 	 * @return hash code
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode()
 	{
 		return new HashCodeBuilder()
 				.append(this.id)
 				.append(this.rootObject)
 				.append(this.rootCriterion)
 				.append(this.sortCriteria)
 				.append(this.aliases)
 				.append(this.firstResult)
 				.append(this.maxResults)
 				.toHashCode();
 	}
 
 	// ========================= IMPLEMENTATION: CopyableFrom ==============
 
 	/**
 	 * Returns a deep-copy of the argument into this object. This object is usually
 	 * constructed with a no-argument constructor first, and then this method is called to
 	 * copy fields into it.
 	 * 
 	 * @param other
 	 *            object to copy
 	 * @return this object, for chaining
 	 * @see edu.utah.further.core.misc.CopyableFrom#copyFrom(java.lang.Object)
 	 */
 	@Override
 	public SearchQueryTo copyFrom(final SearchQuery other)
 	{
 		if (other == null)
 		{
 			return this;
 		}
 
 		// Deep-copy fields
 		this.setRootObjectName(other.getRootObjectName());
 		this.setFirstResult(other.getFirstResult());
 		this.setMaxResults(other.getMaxResults());
 		this.setId(other.getId());
 		this.setRootCriterion(SearchCriterionTo.newCopy(other.getRootCriterion()));
 
 		// Deep-copy collection fields
		this.setAliases(CollectionUtil.copyListOrNull(other.getAliases()));
 
 		this.sortCriteria = null;
 		final List<SortCriterion> otherCriteria = other.getSortCriteria();
 		if ((otherCriteria != null) && !otherCriteria.isEmpty())
 		{
 			this.sortCriteria = newList();
 			for (final SortCriterion criterion : otherCriteria)
 			{
 				sortCriteria.add(SortCriterionTo.newCopy(criterion));
 			}
 		}
 
 		return this;
 	}
 
 	// ========================= IMPLEMENTATION: HasIdentifier =============
 
 	/**
 	 * @return
 	 * @see edu.utah.further.core.api.discrete.HasIdentifier#getId()
 	 */
 	@Override
 	public Long getId()
 	{
 		return id;
 	}
 
 	// ========================= IMPLEMENTATION: HasSettableIdentifier =====
 
 	/**
 	 * @param id
 	 * @see edu.utah.further.core.api.discrete.HasSettableIdentifier#setId(java.lang.Comparable)
 	 */
 	@Override
 	public void setId(final Long id)
 	{
 		this.id = id;
 	}
 
 	// ========================= IMPLEMENTATION: SearchQuery ===============
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.utah.further.core.query.domain.SearchQuery#getRootObject(java.lang.String)
 	 */
 	@Override
 	public Class<? extends PersistentEntity<?>> getRootObject(
 			final String domainClassPackage)
 	{
 		return SearchQueryUtil.getDomainClass(domainClassPackage, rootObject);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.utah.further.core.query.domain.SearchQuery#getRootObjectName()
 	 */
 	@Override
 	public String getRootObjectName()
 	{
 		return rootObject;
 	}
 
 	/**
 	 * Set the root objectname
 	 * 
 	 * @param rootObjectName
 	 */
 	private void setRootObjectName(final String rootObjectName)
 	{
 		this.rootObject = rootObjectName;
 	}
 
 	/**
 	 * Return the first result number in the result set. Returns <code>null</code> if the
 	 * result set is not set a lower bound.
 	 * 
 	 * @return the first result number in the result set, if set
 	 */
 	@Override
 	public Integer getFirstResult()
 	{
 		return firstResult;
 	}
 
 	/**
 	 * Return the limit upon the number of objects to be retrieved. Returns
 	 * <code>null</code> if the result set is not set a size limit.
 	 * 
 	 * @return the limit upon the number of objects to be retrieved
 	 */
 	@Override
 	public Integer getMaxResults()
 	{
 		return maxResults;
 	}
 
 	/**
 	 * Set a new value for the maxResults property.
 	 * 
 	 * @param maxResults
 	 *            the maxResults to set
 	 * @see edu.utah.further.core.query.domain.SearchQuery#setMaxResults(java.lang.Integer)
 	 */
 	@Override
 	public void setMaxResults(final Integer maxResults)
 	{
 		this.maxResults = maxResults;
 	}
 
 	/**
 	 * Return the root criterion of this criteria tree.
 	 * 
 	 * @return the root criterion of this criteria tree
 	 * @see edu.utah.further.core.query.domain.search.SearchCriterionTo#getCriteria()
 	 */
 	@Override
 	public SearchCriterionTo getRootCriterion()
 	{
 		return rootCriterion;
 	}
 
 	/**
 	 * Return the list of specified sort criteria. Defensively copied.
 	 * 
 	 * @return the list of criteria.
 	 * @see edu.utah.further.core.query.domain.SearchQuery#getSortCriteria()
 	 */
 	@Override
 	public List<SortCriterion> getSortCriteria()
 	{
 		return (sortCriteria != null) ? CollectionUtil
 				.<SortCriterion> newList(sortCriteria) : CollectionUtil
 				.<SortCriterion> newList();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.utah.further.core.query.domain.SearchQuery#getAliases()
 	 */
 	@Override
 	public List<SearchQueryAlias> getAliases()
 	{
 		return (aliases != null) ? CollectionUtil.<SearchQueryAlias> newList(aliases)
 				: CollectionUtil.<SearchQueryAlias> newList();
 	}
 
 	/**
 	 * Set a new value for the rootCriterion property.
 	 * 
 	 * @param rootCriterion
 	 *            the rootCriterion to set
 	 */
 	private SearchQueryTo setRootCriterion(final SearchCriterion rootCriterion)
 	{
 		this.rootCriterion = SearchCriterionTo.newCopy(rootCriterion);
 		return this;
 	}
 
 	/**
 	 * Set a new value for the sortCriteria property.
 	 * 
 	 * @param sortCriteria
 	 *            the sortCriteria to set
 	 */
 	private SearchQueryTo setSortCriteria(final List<? extends SortCriterion> sortCriteria)
 	{
		if (sortCriteria != null)
 		{
			this.sortCriteria = CollectionUtil.newList();
			for (final SortCriterion sortCriterion : sortCriteria)
			{
				this.sortCriteria.add(SortCriterionTo.newCopy(sortCriterion));
			}
 		}
 		return this;
 	}
 
 	/**
 	 * Set a new value for the aliases property.
 	 * 
 	 * @param aliases
 	 *            the aliases to set
 	 */
 	private SearchQueryTo setAliases(final List<SearchQueryAlias> aliases)
 	{
		if (aliases != null)
 		{
			this.aliases = CollectionUtil.newList();
			for (final SearchQueryAlias alias : aliases)
			{
				this.aliases.add(SearchQueryAliasTo.newCopy(alias));
			}
 		}
 		return this;
 	}
 
 	/**
 	 * Set a new value for the firstResult property.
 	 * 
 	 * @param firstResult
 	 *            the firstResult to set
 	 */
 	private SearchQuery setFirstResult(final Integer firstResult)
 	{
 		this.firstResult = firstResult;
 		return this;
 	}
 }
