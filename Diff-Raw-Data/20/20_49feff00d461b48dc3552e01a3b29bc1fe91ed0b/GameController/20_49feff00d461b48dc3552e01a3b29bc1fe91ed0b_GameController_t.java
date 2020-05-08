 package org.ayfaar.game.controllers;
 
 import org.ayfaar.game.dao.CommonDao;
 import org.ayfaar.game.model.*;
 import org.ayfaar.game.utils.NextSituationGenerator;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.util.Assert;
 import org.springframework.web.bind.annotation.*;
 
 import java.sql.Time;
 import java.util.*;
 
 import static java.util.Collections.sort;
import static org.ayfaar.game.utils.ValueObjectUtils.convertToPlainObjects;
 import static org.ayfaar.game.utils.ValueObjectUtils.getModelMap;
 
 @Controller
 @RequestMapping("api")
 public class GameController {
     @Autowired CommonDao commonDao;
     @Autowired NextSituationGenerator nextSituationGenerator;
 
     private Random random = new Random();
     private static final int step = 250;
 
     @RequestMapping("stages")
     @ResponseBody
     public Object stages() {
         return getModelMap(commonDao.getAll(Goal.class));
     }
 
     @RequestMapping("user/{userId}/status")
     @ResponseBody
     public Object userStatus(@PathVariable Integer userId) {
         User user = commonDao.get(User.class, userId);
         Assert.notNull(user, "Игрок не найден");
         return getModelMap(user, "levels.level", "currentGoal");
     }
 
     @RequestMapping("situation/categories")
    @ResponseBody
     public Object categories() {
        Collection<ModelMap> categories = convertToPlainObjects(commonDao.getAll(Category.class));
        return categories;
     }
 
     @RequestMapping("situation/next")
     @ResponseBody
     public Object next() {
         User user = commonDao.get(User.class, 1);
 
         if (user.getCurrentGoal() == null) {
             return sendMessage("Не выбран этап");
         }
 
         if (user.getTime() == null) {
             newDay(user);
         }
 
         DateTime dateTime = new DateTime(user.getTime());
         dateTime = dateTime.plusMinutes(10 + random.nextInt(step));
 
         if (dateTime.getHourOfDay() < 8) {
             newDay(user);
             dateTime = new DateTime(user.getTime());
         } else {
             user.setTime(new Time(dateTime.toDate().getTime()));
         }
 
         sort(user.getLevels());
         commonDao.save(user);
 //        map.put("user", getModelMap(user, "levels.level", "currentGoal"));
 
         UserLevel primaryLevel = getPrimaryLevel(user);
 
         if (user.getChoicesCounter() > user.getCurrentGoal().getMaxChoices()) {
             // Stage end
             return sendMessage("Цель не достигнута по причине привышения максимально доступных выборов");
         }
 
         if (primaryLevel.getLevel().getId() >= user.getCurrentGoal().getFinishLevel().getId()) {
             // Stage finished
             return sendMessage("Цель достигнута!");
         }
 
         Situation situation = nextSituationGenerator.getNext(user.getTime(), user.getRestDay());
         Choice[] choices = new Choice[3];
 
         sort(situation.getChoices());
         Iterator<Choice> iterator = situation.getChoices().iterator();
 
         while (iterator.hasNext() && choices[2] == null) {
             Choice choice = iterator.next();
             Integer primaryUserLevelId = primaryLevel.getLevel().getId();
             Integer choiceLevelId = choice.getLevel().getId();
             if (choiceLevelId < primaryUserLevelId) {
                 choices[0] = choice;
             }
             else if (choiceLevelId >= primaryUserLevelId && choices[1] == null) {
                 choices[1] = choice;
             }
             else if (choiceLevelId > primaryUserLevelId) {
                 choices[2] = choice;
             }
         }
 
         ModelMap situationModelMap = (ModelMap) getModelMap(situation, "category");
 
         List<Choice> choiceList = new ArrayList<Choice>();
 
         for (Choice choice : choices) {
             if (choice != null) {
                 choiceList.add(choice);
             }
         }
 
         situationModelMap.put("choices", getModelMap(choiceList, "level"));
 
 //        map.put("situation", situationModelMap);
         situationModelMap.put("time", user.getTime());
 
         return situationModelMap;
     }
 
     private UserLevel getPrimaryLevel(User user) {
         UserLevel primaryLevel = null;
         for (UserLevel userLevel : user.getLevels()) {
             if (primaryLevel == null || userLevel.getValue().compareTo(primaryLevel.getValue()) == 1) {
                 primaryLevel = userLevel;
             }
         }
         if (primaryLevel == null) {
             primaryLevel = new UserLevel();
             primaryLevel.setLevel(user.getCurrentGoal().getStartLevel());
         }
         return primaryLevel;
     }
 
     private ModelMap sendMessage(String s) {
         ModelMap modelMap = new ModelMap();
         modelMap.put("message", s);
         return modelMap;
     }
 
     private void newDay(User user) {
         user.setTime(new Time(9, random.nextInt(step), 0));
         user.setRestDay(random.nextDouble() <= (double)3/7);
     }
 
     @RequestMapping("make-choice/{choiceId}")
     @ResponseBody
     public String choice(@PathVariable Integer choiceId,
                          @ModelAttribute("user") User user) {
 
         Choice choice = commonDao.get(Choice.class, choiceId);
 
         UserLevel prevPrimaryLevel = getPrimaryLevel(user);
 
         UserLevel currentLevel = null;
         for (UserLevel userLevel : user.getLevels()) {
             if (userLevel.getLevel().equals(choice.getLevel())) {
                 currentLevel = userLevel;
             }
         }
         if (currentLevel == null) {
             currentLevel = new UserLevel();
             currentLevel.setUser(user);
             currentLevel.setLevel(choice.getLevel());
             currentLevel.setValue(0);
         }
 
         currentLevel.setValue(currentLevel.getValue() + 1);
         commonDao.save(currentLevel);
 
         if (user.getChoicesCounter() != null) {
             user.setChoicesCounter(user.getChoicesCounter() + 1);
         } else {
             user.setChoicesCounter(1);
         }
         commonDao.save(user);
         ChoiceLog log = new ChoiceLog();
         log.setTime(new Date());
         log.setUser(user);
         log.setChoice(choice);
         commonDao.save(log);
 
         for (ChoiceLog choiceLog : commonDao.getFor(ChoiceLog.class, "user", user.getId())) {
             commonDao.remove(choiceLog);
         }
 
         // update user object
         user = commonDao.get(User.class, user.getId());
         UserLevel primaryLevel = getPrimaryLevel(user);
 
         if (!prevPrimaryLevel.getId().equals(primaryLevel.getId())) {
             for (Stage stage : user.getCurrentGoal().getStages()) {
                 if (stage.getLevel().getId().equals(primaryLevel.getLevel().getId())
                         && stage.getFrom() == primaryLevel.getLevel().getId() < prevPrimaryLevel.getLevel().getId()) {
                     return stage.getText();
                 }
             }
         }
         return null;
     }
 
     @RequestMapping("change-goal")
     public void changeGoal(@RequestParam("userId") Integer userId, @RequestParam("goalId") Integer goalId) {
         User user = commonDao.get(User.class, userId);
         user.setCurrentGoal(commonDao.get(Goal.class, goalId));
         commonDao.save(user);
     }
 
     @ModelAttribute("user")
     public User getUser(){
         return commonDao.get(User.class, 1);
     }
 }
