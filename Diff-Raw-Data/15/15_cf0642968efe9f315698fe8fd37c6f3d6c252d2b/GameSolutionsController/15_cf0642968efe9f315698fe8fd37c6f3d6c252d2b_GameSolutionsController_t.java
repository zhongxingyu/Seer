 /* 
  * Project       : Bachelor Thesis - Sudoku game implementation as portlet
  * Document      : GameSolutionsController.java
  * Author        : Ondřej Fibich <xfibic01@stud.fit.vutbr.cz>
  * Organization: : FIT VUT <http://www.fit.vutbr.cz>
  */
 
 package org.gatein.portal.examples.games.sudoku.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import org.gatein.portal.examples.games.sudoku.controller.exceptions.ForbiddenChangeOnEntityException;
 import org.gatein.portal.examples.games.sudoku.controller.exceptions.NonexistentEntityException;
 import org.gatein.portal.examples.games.sudoku.controller.exceptions.RollbackFailureException;
 import org.gatein.portal.examples.games.sudoku.entity.Game;
 import org.gatein.portal.examples.games.sudoku.entity.GameSolution;
 import org.gatein.portal.examples.games.sudoku.entity.SavedGame;
 
 /**
  * Game solutions JPA Controller Class
  *
  * @author Ondřej Fibich
  */
 public class GameSolutionsController extends Controller
 {
 
     /**
      * Persists a game solution entity to the database.
      * Does not save saved games of the game solution.
      * 
      * @param savedGame      A game solution to persist
      * @throws RollbackFailureException
      * @throws Exception
      */
     public void create(GameSolution gameSolution) throws RollbackFailureException, Exception
     {
         if (gameSolution.getSavedGamesCollection() == null)
         {
             gameSolution.setSavedGamesCollection(new ArrayList<SavedGame>());
         }
         
         EntityManager em = emf.createEntityManager();
         
         try
         {
             em.getTransaction().begin();
             Game game = gameSolution.getGameId();
             
             if (game != null)
             {
                 game = em.getReference(game.getClass(), game.getId());
                 gameSolution.setGameId(game);
             }
             
             em.persist(gameSolution);
             
             if (game != null)
             {
                 game.getGameSolutionsCollection().add(gameSolution);
                 em.merge(game);
             }
             
             em.getTransaction().commit();
         }
         catch (Exception ex)
         {
             try
             {
                 em.getTransaction().rollback();
             }
             catch (Exception re)
             {
                 throw new RollbackFailureException(re);
             }
             throw ex;
         }
         finally
         {
             em.close();
         }
     }
 
     /**
      * Merges a game solution entity to the database.
      * Changes are enabled only on fields: values, time_start, lasting and finished
      * 
      * @param gameSolution      A game solution to merge
      * @throws NonexistentEntityException
      * @throws ForbiddenChangeOnEntityException
      * @throws Exception 
      */
     public void edit(GameSolution gameSolution) throws NonexistentEntityException,
             ForbiddenChangeOnEntityException, Exception
     {
         EntityManager em = emf.createEntityManager();
         GameSolution persGameSolution;
         
         try
         {
             persGameSolution = em.find(GameSolution.class, gameSolution.getId());
             
             if (!persGameSolution.getGameId().equals(gameSolution.getGameId()))
             {
                 throw new ForbiddenChangeOnEntityException(
                         "Forbidden change on the game_id field"
                 );
             }
             
             if (!persGameSolution.getSavedGamesCollection().equals(
                     gameSolution.getSavedGamesCollection()))
             {
                 throw new ForbiddenChangeOnEntityException(
                         "Forbidden change on related saved games"
                 );
             }
             
            if ((
                    persGameSolution.getUserId() == null &&
                    gameSolution.getUserId() != null
                ) || (
                    persGameSolution.getUserId() != null &&
                    !persGameSolution.getUserId().equals(gameSolution.getUserId())
                ))
             {
                 throw new ForbiddenChangeOnEntityException(
                         "Forbidden change on the user_id field"
                 );
             }
             
             if (!persGameSolution.getTimeStart().equals(gameSolution.getTimeStart()))
             {
                 throw new ForbiddenChangeOnEntityException(
                         "Forbidden change on the time_start field"
                 );
             }
             
             em.getTransaction().begin();
             em.merge(gameSolution);
             em.getTransaction().commit();
         }
         catch (Exception ex)
         {
             String msg = ex.getLocalizedMessage();
             
             if (msg == null || msg.length() == 0)
             {
                 if (findGameSolution(gameSolution.getId()) == null)
                 {
                     throw new NonexistentEntityException(
                             "The gameSolutions with id " + gameSolution.getId() + 
                             " no longer exists."
                     );
                 }
             }
             throw ex;
         }
         finally
         {
             em.close();
         }
     }
 
     /**
      * Gets all game solutions.
      * 
      * @return              A list of game solutions
      */
     public List<GameSolution> findGameSolutionEntities()
     {
         
         EntityManager em = emf.createEntityManager();
         
         try
         {
             Query q = em.createNamedQuery("GameSolution.findAll");
             
             return q.getResultList();
         }
         finally
         {
             em.close();
         }
     }
 
     /**
      * Gets unfinished game solutions of a player.
      * 
      * @param uid           An idenfificator of a player
      * @return              A list of game solutions
      */
     public List<GameSolution> findUnfinishedGameSolutionEntitiesOfUser(String uid)
     {
         EntityManager em = emf.createEntityManager();
         
         try
         {
             Query q = em.createNamedQuery("GameSolution.findUnfinishedOfUser");
             q.setParameter("uid", uid);
             
             return q.getResultList();
         }
         finally
         {
             em.close();
         }
     }
 
     /**
      * Finds a game solution entity with a specified id.
      * 
      * @param id            An identificator
      * @return              A found entity
      */
     public GameSolution findGameSolution(Integer id)
     {
         EntityManager em = emf.createEntityManager();
         
         try
         {
             return em.find(GameSolution.class, id);
         }
         finally
         {
             em.close();
         }
     }
 
     /**
      * Gets a count of finished game solutions of logged users.
      * 
      * @return              A count
      */
     public int getFinishedGameSolutionCount()
     {
         EntityManager em = emf.createEntityManager();
         
         try
         {
             Query q = em.createNamedQuery("GameSolution.countFinished");
             return ((Long) q.getSingleResult()).intValue();
         }
         finally
         {
             em.close();
         }
     }
     
     /**
      * Gets a count of players who solved a game
      * 
      * @return              A count
      */
     public int getSolverCount()
     {
         EntityManager em = emf.createEntityManager();
         
         try
         {
             Query q = em.createNamedQuery("GameSolution.countSolver");
             return ((Long) q.getSingleResult()).intValue();
         }
         finally
         {
             em.close();
         }
     }
     
 }
