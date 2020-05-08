 package com.dottydingo.hyperion.service.persistence;
 
 import com.dottydingo.hyperion.api.ApiObject;
 import com.dottydingo.hyperion.exception.BadRequestException;
 import com.dottydingo.hyperion.exception.NotFoundException;
 import com.dottydingo.hyperion.exception.ValidationException;
 import com.dottydingo.hyperion.service.configuration.ApiVersionPlugin;
 import com.dottydingo.hyperion.service.configuration.EntityPlugin;
 import com.dottydingo.hyperion.service.context.RequestContext;
 import com.dottydingo.hyperion.service.model.PersistentObject;
 import com.dottydingo.hyperion.service.query.Mapper;
 import com.dottydingo.hyperion.service.query.PredicateBuilder;
 import com.dottydingo.hyperion.service.query.RsqlPredicateBuilder;
 import com.dottydingo.hyperion.service.sort.SortBuilder;
 import com.dottydingo.hyperion.service.translation.Translator;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.jpa.domain.Specification;
 import org.springframework.data.jpa.domain.Specifications;
 import org.springframework.data.repository.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import static org.springframework.core.GenericTypeResolver.resolveTypeArguments;
 
 /**
  */
 public class SpringJpaPersistenceOperations<C extends ApiObject, P extends PersistentObject<ID>, ID extends Serializable>
         implements PersistenceOperations<C,ID>
 {
     private Sort defaultSort = new Sort("id");
     private HyperionJpaRepository<P,ID> jpaRepository;
     private RsqlPredicateBuilder predicateBuilder;
     private PersistenceFilter<P> persistenceFilter = new EmptyPersistenceFilter<P>();
 
 
     public void setJpaRepository(HyperionJpaRepository<P, ID> jpaRepository)
     {
         this.jpaRepository = jpaRepository;
     }
 
     public void setPredicateBuilder(RsqlPredicateBuilder predicateBuilder)
     {
         this.predicateBuilder = predicateBuilder;
     }
 
     public void setPersistenceFilter(PersistenceFilter<P> persistenceFilter)
     {
         this.persistenceFilter = persistenceFilter;
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<C> findByIds(List<ID> ids, RequestContext context)
     {
 
         ApiVersionPlugin<C,P> apiVersionPlugin = context.getApiVersionPlugin();
 
 
         Iterable<P> iterable = jpaRepository.findAll(ids);
 
         List<P> result = new ArrayList<P>();
         for (P p : iterable)
         {
             if(persistenceFilter.isVisible(p,context))
                 result.add(p);
         }
 
         return apiVersionPlugin.getTranslator().convertPersistent(result,context);
     }
 
     @Override
     @Transactional(readOnly = true)
     public QueryResult<C> query(String query, Integer start, Integer limit, String sort, RequestContext context)
     {
         ApiVersionPlugin<C,P> apiVersionPlugin = context.getApiVersionPlugin();
 
         int size = limit == null ? 500 : limit;
         int pageStart = start == null ? 1 : start;
 
         Pageable pageable = new RangePageAdapter(pageStart,size,getSort(context.getEntityPlugin(), sort));
 
         List<Specification<P>> specificationList = new ArrayList<Specification<P>>();
         if(query != null && query.length() > 0)
         {
             specificationList.add(new QuerySpecification(predicateBuilder, query, getDomainType()));
         }
 
         Specification<P> filter = persistenceFilter.getFilterSpecification(context);
         if(filter != null)
             specificationList.add(filter);
 
         Specifications<P> specification = null;
         for (Specification<P> spec : specificationList)
         {
             if(specification == null)
                 specification = Specifications.where(spec);
             else
                 specification = specification.and(spec);
         }
 
         Page<P> all = jpaRepository.findAll(specification,pageable);
 
         List<P> list = all.getContent();
 
         List<C> converted = apiVersionPlugin.getTranslator().convertPersistent(list,context);
 
         QueryResult<C> queryResult= new QueryResult<C>();
         queryResult.setItems(converted);
         queryResult.setResponseCount(list.size());
         queryResult.setTotalCount(all.getTotalElements());
         queryResult.setStart(start == null ? 1 : (start));
 
         return queryResult;
     }
 
     @Override
     @Transactional(readOnly = false)
     public C createItem(C clientObject, RequestContext context)
     {
         ApiVersionPlugin<C,P> apiVersionPlugin = context.getApiVersionPlugin();
 
         apiVersionPlugin.getValidator().validateCreate(clientObject);
 
         Translator<C,P> translator = apiVersionPlugin.getTranslator();
         P persistent = translator.convertClient(clientObject, context);
 
         if(!persistenceFilter.canCreate(persistent,context))
             return null;
 
         P saved = jpaRepository.save(persistent);
         C toReturn = translator.convertPersistent(saved,context);
 
         return toReturn;
     }
 
     @Override
     @Transactional(readOnly = false)
     public C updateItem(List<ID> ids, C item, RequestContext context)
     {
         ApiVersionPlugin<C,P> apiVersionPlugin = context.getApiVersionPlugin();
 
         Translator<C,P> translator = apiVersionPlugin.getTranslator();
 
         P existing = jpaRepository.findOne(ids.get(0));
 
         if(existing == null)
             throw new NotFoundException(
                    String.format("%s with id %s was not found.",context.getEntity(),ids.get(0)));
 
         apiVersionPlugin.getValidator().validateUpdate(item,existing);
 
         if(!persistenceFilter.canUpdate(existing,context))
         {
             return null;
         }
 
         // todo this needs a better implementation...
         ID oldId = existing.getId();
 
         translator.copyClient(item, existing,context);
 
         if(oldId != null && !oldId.equals(existing.getId()))
             throw new ValidationException("Id in URI does not match the Id in the payload.");
 
         return translator.convertPersistent(jpaRepository.save(existing), context);
 
     }
 
     @Override
     @Transactional(readOnly = false)
     public int deleteItem(List<ID> ids, RequestContext context)
     {
 
         ApiVersionPlugin<C,P> apiVersionPlugin = context.getApiVersionPlugin();
         Iterable<P> persistentItems = jpaRepository.findAll(ids);
         int deleted = 0;
         for (P item : persistentItems)
         {
             if(persistenceFilter.canDelete(item,context))
             {
                 apiVersionPlugin.getValidator().validateDelete(item);
                 jpaRepository.delete(item);
                 deleted++;
             }
         }
 
         return deleted;
     }
 
     private Class<?> getDomainType()
     {
 
         Class<?>[] arguments = resolveTypeArguments(jpaRepository.getClass(), Repository.class);
         return arguments == null ? null : arguments[0];
     }
 
     protected Sort getSort(EntityPlugin entityPlugin, String sortString)
     {
         if (sortString == null || sortString.length() == 0)
         {
             return defaultSort;
         }
 
         boolean hasId = false;
         Sort sort = null;
         String[] split = sortString.split(",");
         for (String s1 : split)
         {
             String[] props = s1.split(":");
             String name = props[0].trim();
             if(name.equals("id"))
                 hasId = true;
             boolean desc = props.length == 2 && props[1].equalsIgnoreCase("desc");
 
             Map<String,SortBuilder> sortBuilders = entityPlugin.getSortBuilders();
             SortBuilder sortBuilder = sortBuilders.get(name);
             if(sortBuilder == null)
                 throw new BadRequestException(String.format("%s is not a valid sort field.",name));
 
             Sort s = sortBuilder.buildSort(name,desc);
             if (sort == null)
             {
                 sort = s;
             }
             else
             {
                 sort = sort.and(s);
             }
         }
 
         if(sort == null)
             sort = defaultSort;
         else if(!hasId)
             sort = sort.and(defaultSort);
 
         return sort;
     }
 
     private class QuerySpecification<T> implements Specification<T>
     {
         private PredicateBuilder predicateBuilder;
         private String queryString;
         private Class<T> entityClass;
 
         private QuerySpecification(PredicateBuilder predicateBuilder, String queryString, Class<T> entityClass)
         {
             this.predicateBuilder = predicateBuilder;
             this.queryString = queryString;
             this.entityClass = entityClass;
         }
 
         @Override
         public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb)
         {
             return predicateBuilder.buildPredicate(queryString,entityClass,root,cb);
         }
     }
 
 
 }
