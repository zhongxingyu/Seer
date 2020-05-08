 /*
  * Copyright 2011, MyCellar
  *
  * This file is part of MyCellar.
  *
  * MyCellar is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * MyCellar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MyCellar. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.peralta.mycellar.infrastructure.shared.repository;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.Tuple;
 import javax.persistence.criteria.AbstractQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Expression;
 import javax.persistence.criteria.From;
 import javax.persistence.criteria.JoinType;
 import javax.persistence.criteria.Order;
 import javax.persistence.criteria.Path;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import fr.peralta.mycellar.domain.shared.IdentifiedEntity;
 import fr.peralta.mycellar.domain.shared.repository.AbstractEntityOrder;
 import fr.peralta.mycellar.domain.shared.repository.CountEnum;
 import fr.peralta.mycellar.domain.shared.repository.EntitySearchFormRepository;
 import fr.peralta.mycellar.domain.shared.repository.FilterEnum;
 import fr.peralta.mycellar.domain.shared.repository.SearchForm;
 import fr.peralta.mycellar.domain.stock.AccessRightEnum;
 import fr.peralta.mycellar.domain.stock.Cellar;
 import fr.peralta.mycellar.domain.stock.CellarShare;
 import fr.peralta.mycellar.domain.stock.Stock;
 import fr.peralta.mycellar.domain.user.User;
 import fr.peralta.mycellar.domain.wine.Appellation;
 import fr.peralta.mycellar.domain.wine.Country;
 import fr.peralta.mycellar.domain.wine.Format;
 import fr.peralta.mycellar.domain.wine.Producer;
 import fr.peralta.mycellar.domain.wine.Region;
 import fr.peralta.mycellar.domain.wine.Wine;
 
 /**
  * @author speralta
  */
 public abstract class JpaEntitySearchFormRepository<E extends IdentifiedEntity, OE, O extends AbstractEntityOrder<OE, O>>
         extends JpaEntityRepository<E, OE, O> implements EntitySearchFormRepository<E, OE, O> {
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final long count(SearchForm searchForm) {
         CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
         Root<E> root = query.from(getEntityClass());
         query = query.select(criteriaBuilder.countDistinct(root));
         query = where(query, root, searchForm, criteriaBuilder, JoinType.INNER);
         return getEntityManager().createQuery(query).getSingleResult();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final List<E> getAll(SearchForm searchForm, O orders, int first, int count) {
         CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<E> query = criteriaBuilder.createQuery(getEntityClass());
         Root<E> root = query.from(getEntityClass());
         query = query.select(root).distinct(true);
         query = where(query, root, searchForm, criteriaBuilder, JoinType.INNER);
         return getEntityManager()
                 .createQuery(orderBy(query, root, orders, criteriaBuilder, JoinType.INNER))
                 .setFirstResult(first).setMaxResults(count).getResultList();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final Map<E, Long> getAllWithCount(SearchForm searchForm, CountEnum countEnum,
             FilterEnum... filters) {
         // Doing it with 2 queries because subquery cannot do multiselect.
         // What we want to do is :
         // SELECT e.*, t.c FROM Entity e
         // LEFT OUTER JOIN
         // (
         // (SELECT distinct e.ID as id, count(count) as c FROM Entity e
         // WHERE e.x=value1 AND e.y=value2 GROUP BY e.ID) as t
         // )
         // on e.id = t.aid
         // WHERE e.x=value1
 
         CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<E> allQuery = criteriaBuilder.createQuery(getEntityClass());
         Root<E> allRoot = allQuery.from(getEntityClass());
         allQuery = allQuery.select(allRoot).distinct(true);
         allQuery = where(allQuery, allRoot, searchForm, criteriaBuilder, JoinType.INNER, filters);
         List<E> all = getEntityManager().createQuery(allQuery).getResultList();
 
         CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
         Root<E> root = query.from(getEntityClass());
         Map<E, Long> allWithCount = getAllWithCount(query, root, root,
                 getOrderByForAllWithCount(root, criteriaBuilder, JoinType.LEFT), searchForm,
                 countEnum, criteriaBuilder);
         Map<E, Long> result = new HashMap<E, Long>();
         for (E e : all) {
             Long count = allWithCount.get(e);
             result.put(e, count != null ? count : 0);
         }
         return result;
     }
 
     // To override
 
     /**
      * @return
      */
     protected abstract FilterEnum getFilterToIgnore();
 
     /**
      * @param root
      * @param joinType
      * @return
      */
     protected abstract From<?, User> getOwnerJoin(Root<E> root, JoinType joinType);
 
     /**
      * @param root
      * @param joinType
      * @return
      */
     protected abstract From<?, Region> getRegionJoin(Root<E> root, JoinType joinType);
 
     /**
      * @param root
      * @param joinType
      * @return
      */
     protected abstract From<?, Producer> getProducerJoin(Root<E> root, JoinType joinType);
 
     /**
      * @param root
      * @param joinType
      * @return
      */
     protected abstract From<?, Format> getFormatJoin(Root<E> root, JoinType joinType);
 
     /**
      * @param root
      * @param joinType
      * @return
      */
     protected abstract From<?, Country> getCountryJoin(Root<E> root, JoinType joinType);
 
     /**
      * @param root
      * @param joinType
      * @return
      */
     protected abstract From<?, Wine> getWineJoin(Root<E> root, JoinType joinType);
 
     /**
      * @param root
      * @param joinType
      * @return
      */
     protected abstract From<?, Cellar> getCellarJoin(Root<E> root, JoinType joinType);
 
     /**
      * @param root
      * @param joinType
      * @return
      */
     protected abstract From<?, Appellation> getAppellationJoin(Root<E> root, JoinType joinType);
 
     /**
      * @param root
      * @param joinType
      * @return
      */
     protected abstract From<?, CellarShare> getSharesJoin(Root<E> root, JoinType joinType);
 
     /**
      * @param root
      * @param joinType
      * @return
      */
     protected abstract From<?, Stock> getStockJoin(Root<E> root, JoinType joinType);
 
     /**
      * @param root
      * @param criteriaBuilder
      * @param joinType
      * @return
      */
     protected abstract Order[] getOrderByForAllWithCount(Root<E> root,
             CriteriaBuilder criteriaBuilder, JoinType joinType);
 
     // Utils methods
 
     /**
      * @param root
      * @param filterEnum
      * @param joinType
      * @return
      */
     protected final Path<?> getPath(Root<E> root, FilterEnum filterEnum, JoinType joinType) {
         switch (filterEnum) {
         case APPELLATION:
             return getAppellationJoin(root, joinType);
         case CELLAR:
             return getCellarJoin(root, joinType);
         case COLOR:
             return getWineJoin(root, joinType).get("color");
         case COUNTRY:
             return getCountryJoin(root, joinType);
         case FORMAT:
             return getFormatJoin(root, joinType);
         case PRODUCER:
             return getProducerJoin(root, joinType);
         case REGION:
             return getRegionJoin(root, joinType);
         case TYPE:
             return getWineJoin(root, joinType).get("type");
         case USER:
             return getOwnerJoin(root, joinType);
         case VINTAGE:
             return getWineJoin(root, joinType).get("vintage");
         case WINE:
             return getWineJoin(root, joinType);
         default:
             throw new IllegalStateException("Unknown " + FilterEnum.class.getSimpleName()
                     + " value [" + filterEnum + "].");
         }
     }
 
     /**
      * @param query
      * @param root
      * @param selection
      * @param order
      * @param searchForm
      * @param countEnum
      * @param criteriaBuilder
      * @return
      */
     protected final <X> Map<X, Long> getAllWithCount(CriteriaQuery<Tuple> query, Root<E> root,
             Expression<X> selection, Order[] orders, SearchForm searchForm, CountEnum countEnum,
             CriteriaBuilder criteriaBuilder) {
         Expression<Long> count = getCount(countEnum, root, criteriaBuilder, JoinType.LEFT);
         query = query.multiselect(selection, count).distinct(true);
         query = where(query, root, searchForm, criteriaBuilder, JoinType.LEFT);
 
         query.groupBy(selection);
         if ((orders != null) && (orders.length > 0)) {
             query.orderBy(orders);
         }
         List<Tuple> tuples = getEntityManager().createQuery(query).getResultList();
         Map<X, Long> result = new LinkedHashMap<X, Long>();
         for (Tuple tuple : tuples) {
             Long countResult = tuple.get(count);
             result.put(tuple.get(selection), countResult != null ? countResult : 0);
         }
         return result;
     }
 
     /**
      * @param query
      * @param root
      * @param searchForm
      * @param criteriaBuilder
      * @param joinType
      * @param filters
      * @return
      * @param <Q>
      * @param <T>
      */
     protected final <Q extends AbstractQuery<T>, T> Q where(Q query, Root<E> root,
             SearchForm searchForm, CriteriaBuilder criteriaBuilder, JoinType joinType,
             FilterEnum... filters) {
         List<FilterEnum> filtersList;
         if ((filters != null) && (filters.length > 0)) {
             filtersList = new ArrayList<FilterEnum>(Arrays.asList(filters));
         } else {
             filtersList = new ArrayList<FilterEnum>(Arrays.asList(FilterEnum.values()));
         }
         filtersList.remove(getFilterToIgnore());
         List<Predicate> predicates = new ArrayList<Predicate>();
         if (filtersList.contains(FilterEnum.APPELLATION)) {
             in(predicates, searchForm, root, FilterEnum.APPELLATION, criteriaBuilder, joinType);
         }
         if (filtersList.contains(FilterEnum.CELLAR)) {
             in(predicates, searchForm, root, FilterEnum.CELLAR, criteriaBuilder, joinType);
         }
         if (filtersList.contains(FilterEnum.COLOR)) {
             in(predicates, searchForm, root, FilterEnum.COLOR, criteriaBuilder, joinType);
         }
         if (filtersList.contains(FilterEnum.COUNTRY)) {
             in(predicates, searchForm, root, FilterEnum.COUNTRY, criteriaBuilder, joinType);
         }
         if (filtersList.contains(FilterEnum.FORMAT)) {
             in(predicates, searchForm, root, FilterEnum.FORMAT, criteriaBuilder, joinType);
         }
         if (filtersList.contains(FilterEnum.PRODUCER)) {
             in(predicates, searchForm, root, FilterEnum.PRODUCER, criteriaBuilder, joinType);
         }
         if (filtersList.contains(FilterEnum.REGION)) {
             in(predicates, searchForm, root, FilterEnum.REGION, criteriaBuilder, joinType);
         }
         if (filtersList.contains(FilterEnum.TYPE)) {
             in(predicates, searchForm, root, FilterEnum.TYPE, criteriaBuilder, joinType);
         }
         if (filtersList.contains(FilterEnum.USER)) {
             List<Predicate> userPredicates = new ArrayList<Predicate>();
             in(userPredicates, searchForm, root, FilterEnum.USER, criteriaBuilder, joinType);
             Set<User> users = searchForm.<User> getSet(FilterEnum.USER);
             if (users != null) {
                 for (User user : users) {
                     if (searchForm.isCellarModification()) {
                         userPredicates.add(criteriaBuilder.and(criteriaBuilder.equal(
                                getSharesJoin(root, JoinType.LEFT).get("email"), user.getEmail()),
                                 criteriaBuilder.equal(
                                        getSharesJoin(root, JoinType.LEFT).get("accessRight"),
                                         AccessRightEnum.MODIFY)));
                     } else {
                        userPredicates.add(criteriaBuilder.equal(getSharesJoin(root, JoinType.LEFT)
                                .get("email"), user.getEmail()));
                     }
                 }
             }
             if (userPredicates.size() > 1) {
                 predicates.add(criteriaBuilder.or(userPredicates
                         .toArray(new Predicate[userPredicates.size()])));
             } else if (userPredicates.size() == 1) {
                 predicates.add(userPredicates.get(0));
             }
         }
         if (filtersList.contains(FilterEnum.VINTAGE)) {
             in(predicates, searchForm, root, FilterEnum.VINTAGE, criteriaBuilder, joinType);
         }
         if (filtersList.contains(FilterEnum.WINE)) {
             in(predicates, searchForm, root, FilterEnum.WINE, criteriaBuilder, joinType);
         }
         return where(query, criteriaBuilder, predicates);
     }
 
     /**
      * @param countEnum
      * @param root
      * @param criteriaBuilder
      * @return
      */
     protected final Expression<Long> getCount(CountEnum countEnum, Root<E> root,
             CriteriaBuilder criteriaBuilder, JoinType joinType) {
         Expression<Long> count;
         switch (countEnum) {
         case WINE:
             count = criteriaBuilder.count(getWineJoin(root, joinType));
             break;
         case STOCK_QUANTITY:
             count = criteriaBuilder.sumAsLong(getStockJoin(root, joinType)
                     .<Integer> get("quantity"));
             break;
         default:
             throw new IllegalStateException("Unknown " + CountEnum.class.getSimpleName()
                     + " value [" + countEnum + "].");
         }
         return count;
     }
 
     /**
      * @param query
      * @param criteriaBuilder
      * @param predicates
      * @return
      */
     protected final <Q extends AbstractQuery<T>, T> Q where(Q query,
             CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
         Predicate wherePredicate;
         switch (predicates.size()) {
         case 0:
             wherePredicate = null;
             break;
         case 1:
             wherePredicate = predicates.get(0);
         default:
             wherePredicate = criteriaBuilder
                     .and(predicates.toArray(new Predicate[predicates.size()]));
             break;
         }
 
         if (wherePredicate != null) {
             query.where(wherePredicate);
         }
         return query;
     }
 
     protected final void in(List<Predicate> predicates, SearchForm searchForm, Root<E> root,
             FilterEnum filterEnum, CriteriaBuilder criteriaBuilder, JoinType joinType) {
         Collection<?> collection = searchForm.getSet(filterEnum);
         if ((collection == null) || (collection.size() == 0)) {
             return;
         }
         List<Object> list = new ArrayList<Object>();
         for (Object object : collection) {
             if ((object != null)
                     && (!(object instanceof IdentifiedEntity) || (((IdentifiedEntity) object)
                             .getId() != null))) {
                 list.add(object);
             }
         }
         switch (list.size()) {
         case 0:
             return;
         case 1:
             predicates.add(criteriaBuilder.equal(getPath(root, filterEnum, joinType), list.get(0)));
             break;
         default:
             predicates.add(getPath(root, filterEnum, joinType).in(list));
             break;
         }
     }
 }
