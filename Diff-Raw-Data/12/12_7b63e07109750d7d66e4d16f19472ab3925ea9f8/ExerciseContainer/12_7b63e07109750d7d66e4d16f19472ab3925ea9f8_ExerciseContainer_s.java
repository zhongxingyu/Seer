 package org.knetwork.webapp.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.knetwork.webapp.entity.Badge;
 import org.knetwork.webapp.entity.Exercise;
 import org.knetwork.webapp.entity.ExerciseStates;
 import org.knetwork.webapp.entity.TargetContext;
 import org.knetwork.webapp.entity.UserBadge;
 
 public class ExerciseContainer {
 
     private static final String SUPERSTAR_BADGE_NAME = "greattimedproblembadge";
 
     private final List<Exercise> superStarExercises = new ArrayList<Exercise>();
     private final List<Exercise> proficientExercises = new ArrayList<Exercise>();;
     private final List<Exercise> reviewingExercises = new ArrayList<Exercise>();;
     private final List<Exercise> strugglingExercises = new ArrayList<Exercise>();;
     private final List<Exercise> inProgressExercises = new ArrayList<Exercise>();;
     private final List<Exercise> notStartedExercises = new ArrayList<Exercise>();;
 
     public ExerciseContainer(final List<Exercise> exercises, final List<Badge> badges) {
         init(exercises, badges);
     }
 
     public List<Exercise> getInProgressExercises() {
         return inProgressExercises;
     }
 
     public List<Exercise> getNotStartedExercises() {
         return notStartedExercises;
     }
 
     public List<Exercise> getProficientExercises() {
         return proficientExercises;
     }
 
     public List<Exercise> getReviewingExercises() {
         return reviewingExercises;
     }
 
     public List<Exercise> getStrugglingExercises() {
         return strugglingExercises;
     }
 
     private List<String> getSuperStarExerciseNames(final List<Badge> badges) {
         final List<String> superStarNames = new ArrayList<String>();
         for (final Badge badge : badges) {
             final String badgeName = badge.getName();
            if (badgeName.equals(SUPERSTAR_BADGE_NAME)) {
                 final List<UserBadge> userBadges = badge.getUserBadges();
                 if (userBadges != null) {
                     for (final UserBadge userBadge : userBadges) {
                         final TargetContext targetContext = userBadge.getTargetContext();
                         if (targetContext != null && targetContext.getName() != null) {
                             superStarNames.add(targetContext.getName());
                         }
                     }
                 }
                 break;
             }
         }
         return superStarNames;
     }
 
     public List<Exercise> getSuperStarExercises() {
         return superStarExercises;
     }
 
     private void init(final List<Exercise> exercises, final List<Badge> badges) {
         final List<String> superStarNames = getSuperStarExerciseNames(badges);
         for (final Exercise exercise : exercises) {
             if (exercise.getLastDone() == null) {
                 notStartedExercises.add(exercise);
             } else if (superStarNames.contains(exercise.getExercise())) {
                 superStarExercises.add(exercise);
             } else {
                 final ExerciseStates state = exercise.getExerciseStates();
                 if (state.isProficient()) {
                     if (state.isReviewing()) {
                         reviewingExercises.add(exercise);
                     } else {
                         proficientExercises.add(exercise);
                     }
                 } else {
                     if (state.isStruggling()) {
                         strugglingExercises.add(exercise);
                     } else {
                         inProgressExercises.add(exercise);
                     }
                 }
             }
         }
     }
 
 }
