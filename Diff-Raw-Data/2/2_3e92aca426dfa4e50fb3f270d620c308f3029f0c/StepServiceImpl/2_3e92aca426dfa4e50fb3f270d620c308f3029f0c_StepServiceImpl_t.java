 package com.checkers.server.services;
 
 import com.checkers.server.Consts;
 import com.checkers.server.beans.Game;
 import com.checkers.server.beans.Step;
 import com.checkers.server.beans.User;
 import com.checkers.server.dao.GameDao;
 import com.checkers.server.dao.StepDao;
 import com.checkers.server.dao.UserDao;
 import com.checkers.server.exceptions.CheckersException;
 import com.checkers.server.exceptions.LogicException;
 import com.checkers.server.services.referee.Referee;
 import com.checkers.server.services.referee.RussianGraphRefereeImpl;
 import com.checkers.server.services.referee.WorldwideGraphRefereeImpl;
 import com.checkers.server.services.referee.graph.FigureColor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.prepost.PostAuthorize;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Service;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.*;
 
 /**
  *
  *
  * @author Pavel Kuchin
  */
 @Service("stepService")
 public class StepServiceImpl implements StepService {
     @Autowired
     private StepDao stepDao;
 
     @Autowired
     private UserDao userDao;
 
     @Autowired
     private GameDao gameDao;
 
     ConcurrentMap<Long, Object> events = new ConcurrentHashMap<Long, Object>();
 
     @PostAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_USER')")
     @Override
     public Step getStep(Long suid) throws LogicException {
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String name = auth.getName();
         User user = userDao.getUserByLogin(name);
 
         Step step = null;
 
         Collection<SimpleGrantedAuthority> authorities =
                 (Collection<SimpleGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
 
         if(authorities.contains(Consts.ROLE_ADMIN)){
             step = stepDao.getStep(suid);
         } else if(authorities.contains(Consts.ROLE_USER)){
             step = stepDao.getStep(suid);
 
             if(!(step.getGame().getBlackUuid() == user.getUuid() || step.getGame().getWhiteUuid() == user.getUuid())){
                 throw new LogicException(1L, "You are not involved in game.");
             }
         }
 
         return step;
     }
 
     @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_USER')")
     @Override
     public Step newStep(Step step) throws LogicException, CheckersException {
         Object event;
 
         Game game = null;
 
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String name = auth.getName();
 
         User user = userDao.getUserByLogin(name);
 
         if(step.getGame() == null){
             game = gameDao.getGame(step.getGauid());
         } else{
             game = step.getGame();
         }
 
         // Do step Logic
         // Are you involved in game?
         if(!(game.getBlackUuid() == user.getUuid() || game.getWhiteUuid() == user.getUuid())){
             throw new LogicException(1L, "You are not involved in game.");
         }
 
         // Game should have status - 'game'
         if(!game.getState().equals("game")){
             throw new LogicException(2L, "You can't make step in 'close' or 'open' game. Game should be in 'game' state.");
         }
 
         // You can't make two steps, one by one. It is obviously.
         Step lastStep = stepDao.getGameLastStep(step.getGauid());
 
         if(lastStep != null){
             if(lastStep.getUuid().equals(user.getUuid())){
                 throw new LogicException(3L, "You made your move. Let the opponent to make a move.");
             }
         } else if(game.getBlackUuid() == user.getUuid()){
             // TODO errors refactoring
             throw new LogicException(3L, "White should make step first");
         }
 
         /*
         Referee referee = null;
 
         if(game.getBoard().equals(Consts.GAME_BOARD_RUSSIAN)){
             referee = new RussianGraphRefereeImpl();
         } else if(game.getBoard().equals(Consts.GAME_BOARD_WORDWIDE)){
             referee = new WorldwideGraphRefereeImpl();
         }
 
         if(game.getBlackUuid() == user.getUuid()){
             referee.checkStep(step.getStep(), FigureColor.BLACK);
         } else if(game.getWhiteUuid() == user.getUuid()){
             referee.checkStep(step.getStep(), FigureColor.WHITE);
         }
         */
 
         //Bring a little Async
        step.setSuid(null);

         step.setUuid(null);
 
         step.setCreated(new Date());
 
         step.setUser(user);
 
         synchronized (events){
             if(!events.containsKey(step.getGauid())){
                 events.put(step.getGauid(), new Object());
             }
             event = events.get(step.getGauid());
         }
 
         synchronized (event) {
             stepDao.newStep(step);
 
             if(step.getSuid() != null){
                 //We notify all listeners about new step in the DB
                 event.notifyAll();
             }
         }
 
         return step;
     }
 
     @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_USER')")
     @Override
     public List<Step> getGameSteps(Long gauid) throws LogicException {
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String name = auth.getName();
         User user = userDao.getUserByLogin(name);
 
         List<Step> steps = null;
 
         Collection<SimpleGrantedAuthority> authorities =
                 (Collection<SimpleGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
 
         if(authorities.contains(Consts.ROLE_ADMIN)){
             steps = stepDao.getGameSteps(gauid);
         } else if(authorities.contains(Consts.ROLE_USER)){
             Game game = gameDao.getGame(gauid);
 
             if((game.getBlackUuid() == user.getUuid() || game.getWhiteUuid() == user.getUuid())){
                 steps = stepDao.getGameSteps(gauid);
             } else{
                 throw new LogicException(1L, "You are not involved in game.");
             }
         }
 
         return steps;
     }
 
     @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_USER')")
     @Override
     public Step getGameLastStep(Long gauid) throws LogicException {
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String name = auth.getName();
         User user = userDao.getUserByLogin(name);
 
         Step step = null;
 
         Collection<SimpleGrantedAuthority> authorities =
                 (Collection<SimpleGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
 
         if(authorities.contains(Consts.ROLE_ADMIN)){
             step = stepDao.getGameLastStep(gauid);
         } else if(authorities.contains(Consts.ROLE_USER)){
             Game game = gameDao.getGame(gauid);
 
             if((game.getBlackUuid() == user.getUuid() || game.getWhiteUuid() == user.getUuid())){
                 step = stepDao.getGameLastStep(gauid);
             } else{
                 throw new LogicException(1L, "You are not involved in game.");
             }
         }
 
         return step;
     }
 
     @Override
     public Step getAsyncGameLastStep(Long gauid, String username) throws InterruptedException, LogicException {
         Step result = null;
 
         Object event;
 
         User user = userDao.getUserByLogin(username);
 
         Game game = gameDao.getGame(gauid);
 
         if(!(game.getBlackUuid() == user.getUuid() || game.getWhiteUuid() == user.getUuid())){
             throw new LogicException(1L, "You are not involved in game.");
         }
 
         synchronized (events){
             if(!events.containsKey(gauid)){
                 events.put(gauid, new Object());
             }
             event = events.get(gauid);
         }
 
         synchronized (event){
             while(result == null){
                 Step lastStep = stepDao.getGameLastStep(gauid);
                 if(lastStep != null && !lastStep.getUser().getLogin().equals(username)){
                     result = lastStep;
                 } else{
                     //We are waiting for object creation
                     event.wait();
                 }
             }
         }
             return result;
     }
 
 }
