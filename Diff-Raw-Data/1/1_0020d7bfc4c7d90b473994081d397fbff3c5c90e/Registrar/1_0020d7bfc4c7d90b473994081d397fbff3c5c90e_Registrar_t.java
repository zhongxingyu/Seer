 /*
 * Copyright (C) 2010, 2011 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.0.
 * http://www.opensource.org/licenses/afl-3.0.php
 */
 
 
 /**
  * Registration is the process of adding or finding users in the system and
  * associating that user with a current or new game.  Ficha colors are 
  * determined dynamically, by the available colors per game.
  * 
  * @author awaterma@ecosur.mx
  */
 
 package mx.ecosur.multigame.ejb.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.security.RolesAllowed;
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.persistence.*;
 
 import mx.ecosur.multigame.MessageSender;
 import mx.ecosur.multigame.ejb.interfaces.RegistrarLocal;
 import mx.ecosur.multigame.ejb.interfaces.RegistrarRemote;
 import mx.ecosur.multigame.enums.GameState;
 import mx.ecosur.multigame.exception.InvalidRegistrationException;
 import mx.ecosur.multigame.model.interfaces.*;
 
 @SuppressWarnings({"JpaQueryApiInspection"})
 @Stateless
 @RolesAllowed("admin")
 @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 public class Registrar implements RegistrarRemote, RegistrarLocal {
 
     private MessageSender messageSender;
 
     @PersistenceContext (unitName = "MultiGamePU")
     EntityManager em;
 
     /**
      * Default constructor
      */
     public Registrar() throws InstantiationException, IllegalAccessException,
             ClassNotFoundException
     {
         super();
         messageSender = new MessageSender();
         messageSender.initialize();
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.ejb.interfaces.RegistrarInterface#register(java.lang.String)
      */
     public Registrant register(Registrant registrant) {
         /* TODO: inject or make this query static */
         Query query = em.createNamedQuery("Registrant.GetByName");
         query.setParameter("name", registrant.getName());
         @SuppressWarnings ("unchecked")
         List<Registrant> registrants = query.getResultList();
         if (registrants.size() == 0) {
             em.persist(registrant);
         } else {
             registrant = (Registrant) registrants.get(0);
         }
         return registrant;
     }
 
     /**
      * Registers a robot with she specified Game object.
      *
      * TODO:  Make this generic.
      * @throws InvalidRegistrationException
      */
     public Game registerPlayer (Game game, Registrant registrant)
             throws InvalidRegistrationException
     {
         if (game.getId() == 0)
             em.persist(game);
         else
             game = em.find(game.getClass(), game.getId());
         if (game.getMessageSender() == null)
             game.setMessageSender(messageSender);
         registrant = em.find(registrant.getClass(), registrant.getId());
         registrant.setLastRegistration(System.currentTimeMillis());
         game.registerPlayer (registrant);
         messageSender.sendPlayerChange(game);
         return game;
     }
 
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.ejb.interfaces.RegistrarInterface#registerAgent(mx.ecosur.multigame.model.Game, mx.ecosur.multigame.model.Agent)
      */
     public Game registerAgent(Game game, Agent agent) throws
             InvalidRegistrationException
     {
         if (game.getId() == 0)
             em.persist(game);
         else
             game = em.find(game.getClass(), game.getId());
         game.setMessageSender(messageSender);
         game.registerAgent (agent);
         messageSender.sendPlayerChange(game);
         return game;
     }
 
     public Game unregister(Game game, GamePlayer player) throws InvalidRegistrationException {
         game = em.find(game.getClass(), game.getId());
         game.setMessageSender(messageSender);
         game.removePlayer(player);
         game.setState(GameState.ENDED);
         messageSender.sendPlayerChange(game);
         return game;
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.ejb.RegistrarInterface#getUnfinishedGames(mx.ecosur.multigame.model.Registrant)
      */
     public List<Game> getUnfinishedGames(Registrant player) {
         List<Game> ret = new ArrayList<Game>();
         Query query = em.createNamedQuery("GridGame.GetCurrentGames");
         query.setParameter("registrant", player);
         query.setParameter("state", GameState.ENDED);
         List<Game> games = query.getResultList();
         for (Game game : games) {
             ret.add(game);
         }
 
         return ret;
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.ejb.RegistrarInterface#getPendingGames(mx.ecosur.multigame.model.Registrant)
      */
     public List<Game> getPendingGames(Registrant player) {
         List<Game> ret = new ArrayList<Game>();
         Query query = em.createNamedQuery("GridGame.GetAvailableGames");
         query.setParameter("registrant", player);
         query.setParameter("state", GameState.WAITING);
         List<Game> joinedGames = getUnfinishedGames (player);
         List<Game> games = query.getResultList();
         for (Game impl : games) {
             Game game = impl;
             if (joinedGames.contains(game))
                 continue;
             ret.add(game);
         }
         return ret;
     }
 }
