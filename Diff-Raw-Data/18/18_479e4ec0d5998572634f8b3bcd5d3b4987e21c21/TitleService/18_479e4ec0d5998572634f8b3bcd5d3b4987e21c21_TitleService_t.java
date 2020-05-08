 /**
  *     Copyright (C) 2011 Julien SMADJA <julien dot smadja at gmail dot com>
  *
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *
  *             http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  */
 
 package com.anzymus.neogeo.hiscores.service;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 import com.anzymus.neogeo.hiscores.domain.Player;
 import com.anzymus.neogeo.hiscores.domain.Title;
 import com.anzymus.neogeo.hiscores.success.TitleUnlockingStrategy;
 
 @Stateless
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 public class TitleService {
 
     private static final Logger LOG = Logger.getLogger(TitleService.class.getName());
 
     @PersistenceContext
     EntityManager em;
 
     public long getNumScoresByPlayer(Player player) {
         Query query = em.createQuery("SELECT COUNT(s) FROM Score s WHERE s.player = :player");
         query.setParameter("player", player);
         return (Long) query.getSingleResult();
     }
 
     public Map<Title, TitleUnlockingStrategy> findAllStrategies() {
         Map<Title, TitleUnlockingStrategy> strategies = new HashMap<Title, TitleUnlockingStrategy>();
         TypedQuery<Title> query = em.createQuery("SELECT t FROM Title t", Title.class);
         for (Title title : query.getResultList()) {
             try {
                 String classname = title.getClassname();
                 TitleUnlockingStrategy strategy = (TitleUnlockingStrategy) Class.forName(classname).newInstance();
                 strategy.initialize(this);
                 strategies.put(title, strategy);
             } catch (ClassNotFoundException e) {
                 LOG.log(Level.WARNING, "Can't create strategy from title: " + title, e);
             } catch (InstantiationException e) {
                 LOG.log(Level.WARNING, "Can't create strategy from title: " + title, e);
             } catch (IllegalAccessException e) {
                 LOG.log(Level.WARNING, "Can't create strategy from title: " + title, e);
             }
         }
         return strategies;
     }
 
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public Title store(Title title) {
         Title storedTitle;
         if (title.getId() == null) {
             em.persist(title);
             storedTitle = title;
         } else {
             storedTitle = em.merge(title);
         }
         em.flush();
         return storedTitle;
     }
 
     public boolean hasScoreInGame(Player player, String game) {
        Query query = em.createQuery("SELECT COUNT(s) FROM Score s WHERE s.player = :player AND s.game.name = :game");
         query.setParameter("player", player);
         query.setParameter("game", game);
         return (Long) query.getSingleResult() > 0;
     }
 
 }
