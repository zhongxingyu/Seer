 package edu.asupoly.cst425.lab3.domain;
 
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 public final class SurveyResults {
 
     private transient Map<User, UserSurveyResult> userSurveyResults;
     private volatile Map<User, UserSurveyResult> completedUserSurveyResults;
     private final Survey survey;
     private final String fileName;
 
     public SurveyResults(Survey survey, String fileName) {
         this.survey = survey;
         this.fileName = fileName;
         userSurveyResults = new ConcurrentHashMap<User, UserSurveyResult>();
         completedUserSurveyResults = new ConcurrentHashMap<User, UserSurveyResult>();
     }
 
     public void addUserSurveyResult(UserSurveyResult userSurveyResult) {
         userSurveyResults.put(userSurveyResult.getUser(), userSurveyResult);
     }
 
     public UserSurveyResult getUserSurveyResultForUser(User user) {
         return userSurveyResults.get(user);
     }
 
     public void removeUserSurveyResultForUser(User user) {
         userSurveyResults.remove(user);
     }
 
     public void persistUserSurveyResultForUser(User user) {
         completedUserSurveyResults.put(user, userSurveyResults.get(user));
     }
 
     public void save() throws IOException {
         synchronized (fileName) {
             File file = new File(fileName);
             FileOutputStream fileOutputStream = new FileOutputStream(file);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
 
             synchronized (completedUserSurveyResults) {
                 objectOutputStream.writeObject(completedUserSurveyResults);
             }
 
             objectOutputStream.close();
         }
 
     }
 
     public void restore() throws IOException, ClassNotFoundException {
         synchronized (fileName) {
             File file = new File(fileName);
             FileInputStream fileInputStream = new FileInputStream(file);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
 
             userSurveyResults = new ConcurrentHashMap<User, UserSurveyResult>();
 
             synchronized (completedUserSurveyResults) {
                 completedUserSurveyResults = (ConcurrentHashMap<User, UserSurveyResult>) objectInputStream.readObject();
             }
 
             objectInputStream.close();
         }
     }
 
     public Map<User, UserSurveyResult> getCompletedUserSurveyResults() {
         return Collections.unmodifiableMap(completedUserSurveyResults);
     }
 
     public Set<User> score(User user) {
 
         Set<User> scoredUsers = new TreeSet<User>();
 
         List<SurveyItem> surveyItems = new ArrayList<SurveyItem>();
         for(int i = 0; i < survey.getNumPages(); i++) {
             surveyItems.add(survey.getSurveyItem(i));
         }
 
         synchronized (completedUserSurveyResults) {

             Set<User> users = completedUserSurveyResults.keySet();
            UserSurveyResult mainUserSurveyResult = completedUserSurveyResults.get(user);
 
             for(User userToScore : users) {
 
                 if(userToScore != user) {

                    UserSurveyResult userToScoreUserSurveyResult = completedUserSurveyResults.get(userToScore);
                     User scoredUser = new User(userToScore.getFirstName(), userToScore.getLastName());
                     int matchingAnswers = 0;
 
                     for(SurveyItem surveyItem : surveyItems) {
                         if(mainUserSurveyResult.getAnswerForSurveyItem(surveyItem) == userToScoreUserSurveyResult.getAnswerForSurveyItem(surveyItem)) {
                             matchingAnswers++;
                         }
                     }
 
                     scoredUser.setMatchingAnswers(matchingAnswers);
                     scoredUsers.add(scoredUser);
                 }
             }
         }
 
         return scoredUsers;
     }
 }
