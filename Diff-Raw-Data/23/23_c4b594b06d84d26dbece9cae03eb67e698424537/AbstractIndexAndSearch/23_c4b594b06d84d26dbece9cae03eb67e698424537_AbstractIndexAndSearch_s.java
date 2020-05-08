 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.search;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
import nl.surfnet.bod.util.BoDInitializer;
import nl.surfnet.bod.util.FullTextSearchContext;

 import org.apache.lucene.queryParser.ParseException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.data.domain.Sort;
 
 public class AbstractIndexAndSearch<T> {
 
   protected Logger log = LoggerFactory.getLogger(getClass());
 
   private EntityManager entityManager;
 
   private EntityManagerFactory entityManagerFactory;
 
   private final Class<T> clazz;
 
   private final BoDInitializer boDInitializer = new BoDInitializer();
 
   public AbstractIndexAndSearch(final Class<T> clazz) {
     this.clazz = clazz;
   }
 
   protected void initEntityManager() {
    entityManagerFactory = Persistence.createEntityManagerFactory("search-pu");
    entityManager = entityManagerFactory.createEntityManager();
   }
 
   protected void index() {
     boDInitializer.setEntityManager(entityManager);
     boDInitializer.init();
   }
 
   @SuppressWarnings("unchecked")
   protected List<T> getSearchQuery(String query) throws ParseException {
     return new FullTextSearchContext<T>(entityManager, clazz).getFullTextQueryForKeywordOnAllAnnotedFields(query,
         new Sort("id")).getResultList();
   }
 
   protected final void closeEntityManager() {
     entityManagerFactory.close();
   }
 
 }
