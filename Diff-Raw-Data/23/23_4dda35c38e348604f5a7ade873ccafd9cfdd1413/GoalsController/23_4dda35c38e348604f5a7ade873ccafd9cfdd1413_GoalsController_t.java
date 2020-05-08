 package org.ayfaar.game.controllers;
 
 import org.ayfaar.game.dao.CommonDao;
 import org.ayfaar.game.model.*;
 import org.ayfaar.game.utils.EntityUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.Assert;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static java.util.Collections.sort;
 import static org.ayfaar.game.utils.ValueObjectUtils.getModelMap;
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 
 @Controller
 @RequestMapping("api/admin/goal")
 public class GoalsController {
     @Autowired CommonDao commonDao;
 
     @RequestMapping("all")
     @ResponseBody
     public Object getAllGoals() {
         return getModelMap(commonDao.getAll(Goal.class), "stages.level", "startLevel", "finishLevel");
     }
 
     @RequestMapping(value = "save", method = POST)
     @ResponseBody
    public Object saveSituation(@RequestParam("name") String goalName,
                                @RequestParam("startLevel[id]") Integer startLevelId,
                                @RequestParam("finishLevel[id]") Integer finishLevelId,
                                @RequestParam("maxChoices") Integer maxChoices,
                                @RequestParam(value = "id", required = false) Integer goalId,
                                HttpServletRequest request) {
         HashMap<String, String[]> map = (HashMap<String, String[]>) request.getParameterMap();
 
         Goal goal;
         if (goalId != null) {
             goal = commonDao.get(Goal.class, goalId);
            for (Stage stage : goal.getStages()) {
                stage.setGoal(null);
                commonDao.save(stage);
            }
//            goal = commonDao.get(Goal.class, goalId);
             Assert.notNull(goal);
         } else {
             goal = new Goal();
         }
 
         goal.setName(goalName);
         goal.setMaxChoices(maxChoices);
         goal.setStartLevel(commonDao.get(Level.class, startLevelId));
         goal.setFinishLevel(commonDao.get(Level.class, finishLevelId));
 
         ArrayList<Map.Entry<String, String[]>> entries = new ArrayList<Map.Entry<String, String[]>>(map.entrySet());
         sort(entries, new Comparator<Map.Entry<String, String[]>>() {
             @Override
             public int compare(Map.Entry<String, String[]> o1, Map.Entry<String, String[]> o2) {
                 return o1.getKey().compareTo(o2.getKey());
             }
         });
         Map<Integer, Stage> stages = new HashMap<Integer, Stage>();
         for (Map.Entry<String, String[]> entry : entries) {
             Pattern pattern = Pattern.compile("stages\\[(\\d+)\\]\\[([^\\]]*)\\](.*)");
             Matcher matcher = pattern.matcher(entry.getKey());
             if (matcher.matches()) {
                 Stage stage = stages.get(Integer.valueOf(matcher.group(1)));
                 if (stage == null) {
                     stage = new Stage();
                     stage.setGoal(goal);
                     stages.put(Integer.valueOf(matcher.group(1)), stage);
                 }
                 if (matcher.group(2).equals("level")) {
                     if (matcher.group(3).equals("[id]"))
                         stage.setLevel(commonDao.get(Level.class, Integer.valueOf(entry.getValue()[0])));
                 } else if (matcher.group(2).equals("levels")) {
                     // skip
                 } else if (matcher.group(2).equals("id")) {
                     stage.setId(Integer.valueOf(entry.getValue()[0]));
                 } else if (matcher.group(2).equals("from")) {
                     stage.setFrom(Boolean.valueOf(entry.getValue()[0]));
                 } else {
                     EntityUtils.setPropertyValue(stage, matcher.group(2), entry.getValue()[0]);
                 }
             }
         }

         goal.setStages(new ArrayList<Stage>(stages.values()));
         commonDao.save(goal);
        return getModelMap(goal, "stages.level", "startLevel", "finishLevel");
     }
 }
