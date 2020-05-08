 package au.org.scoutmaster.views;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Join;
 import javax.persistence.criteria.JoinType;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import javax.persistence.criteria.Subquery;
 
 import org.apache.log4j.Logger;
 
 import au.com.vaadinutils.dao.QueryModifierAdaptor;
 import au.org.scoutmaster.domain.Contact;
 import au.org.scoutmaster.domain.Contact_;
 import au.org.scoutmaster.domain.SectionType;
 import au.org.scoutmaster.domain.SectionType_;
 import au.org.scoutmaster.domain.Tag;
 
public class ContactDefaultQueryModifierDelegate extends  QueryModifierAdaptor
 {
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(ContactDefaultQueryModifierDelegate.class);
 
 	private ArrayList<Tag> tags;
 	private String fullTextSearch;
 
 	public ContactDefaultQueryModifierDelegate(ArrayList<Tag> tags, String fullTextSearch)
 	{
 		this.tags = tags;
 		this.fullTextSearch = fullTextSearch;
 	}
 
 	@Override
 	public void filtersWillBeAdded(CriteriaBuilder builder, CriteriaQuery<?> query, List<Predicate> predicates)
 	{
 		@SuppressWarnings("unchecked")
 		Root<Contact> fromContact = (Root<Contact>) query.getRoots().iterator().next();
 
 		try
 		{
 			Subquery<Contact> subquery = null;
 			if (tags.size() > 0)
 			{
 				subquery = query.subquery(Contact.class);
 				Root<Contact> subqueryContact = subquery.from(Contact.class);
 				subqueryContact.alias("C");
 				Join<Contact, Tag> tagJoin = subqueryContact.join(Contact_.tags);
 
 				List<Predicate> tagPredicates = new ArrayList<>();
 				for (Tag tag : tags)
 				{
 
 					Predicate tagPredicate = builder.equal(tagJoin.get("id"), tag.getId());
 					tagPredicates.add(tagPredicate);
 				}
 				// Or all of the tag predicates together.
 				Predicate[] orPredicates = new Predicate[tagPredicates.size()];
 				Predicate or = builder.or((Predicate[]) tagPredicates.toArray(orPredicates));
 
 				// link the subquery to the outer contact
 				Predicate subqueryPredicate = builder.equal(subqueryContact.get("id"), fromContact.get("id"));
 				subquery.where(builder.and(subqueryPredicate, or));
 
 			}
 
 			Predicate fullTextSearchPredicate = null;
 			// Build main queries full text search
 			if (fullTextSearch != null && !fullTextSearch.isEmpty())
 			{
 				Join<Contact, SectionType> sectionJoin = fromContact.join(Contact_.section, JoinType.LEFT);
 
 				fullTextSearchPredicate = builder.like(builder.upper(fromContact.get(Contact_.firstname)), "%"
 						+ this.fullTextSearch.toUpperCase() + "%");
 				fullTextSearchPredicate = builder.or(
 						builder.like(builder.upper(fromContact.get(Contact_.lastname)),
 								"%" + this.fullTextSearch.toUpperCase() + "%"), fullTextSearchPredicate);
				
				fullTextSearchPredicate = builder.or(
						builder.like(builder.function("date_format", String.class, fromContact.get(Contact_.birthDate), builder.literal("%Y-%m-%d")),
								"%" + this.fullTextSearch.toUpperCase() + "%"), fullTextSearchPredicate);
				
				
				fullTextSearchPredicate = builder.or(
						builder.like(builder.upper(sectionJoin.get(SectionType_.name)), "%"
								+ this.fullTextSearch.toUpperCase() + "%"), fullTextSearchPredicate);
 
 
 				fullTextSearchPredicate = builder.or(
						builder.like(builder.upper(fromContact.get(Contact_.section).get(SectionType_.name)),
 								"%" + this.fullTextSearch.toUpperCase() + "%"), fullTextSearchPredicate);
				
 			}
 
 			if (subquery != null && fullTextSearchPredicate != null)
 				query.where(builder.and(fullTextSearchPredicate, builder.exists(subquery)));
 			else if (subquery != null)
 				query.where(builder.exists(subquery));
 			else if (fullTextSearchPredicate != null)
 				query.where(fullTextSearchPredicate);
 
 		}
 		catch (Throwable e)
 		{
 			logger.error(e, e);
 		}
 	}
 
 }
