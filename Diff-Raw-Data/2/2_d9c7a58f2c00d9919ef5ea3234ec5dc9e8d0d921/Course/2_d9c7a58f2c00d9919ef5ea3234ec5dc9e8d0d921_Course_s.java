 /*
  * Copyright (C) 2012 Jimmy Theis. Licensed under the MIT License:
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.jetheis.android.grades.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 
import android.content.Context;

 import com.jetheis.android.grades.storage.CourseStorageAdapter;
 import com.jetheis.android.grades.storage.CourseStorageAdapter.CourseStorageIterator;
 import com.jetheis.android.grades.storage.Storable;
 
 /**
  * Representation of a single academic course. Each course has a name and a
  * {@link CourseType} (given by {@link #getCourseType()}), which represents how
  * the courses final grade is calculated: either by adding points for a total
  * score ({@link CourseType#POINT_TOTAL}), or by weighting categories'
  * percentages ({@link CourseType#PERCENTAGE_WEIGHTING}) together.
  */
 public class Course extends Storable implements Comparable<Course> {
 
     /**
      * A simple representation of a course type.
      * 
      */
     public enum CourseType {
         /**
          * The course calculates its final grade by summing the points of its
          * included grade components
          */
         POINT_TOTAL(1),
         /**
          * The course calculates its final grade by applying a specific weight
          * to the percentage grade of each of its included grade components.
          */
         PERCENTAGE_WEIGHTING(2);
 
         private final int mIntIdentifier;;
 
         /**
          * Constructor for {@link CourseType}.
          * 
          * @param intIdenfifier
          *            The integer that will be used to uniquely identify the
          *            {@link CourseType} when it is stored as a database value.
          */
         CourseType(int intIdenfifier) {
             mIntIdentifier = intIdenfifier;
         }
 
         /**
          * Convert this {@link CourseType} to an integer for storage in a
          * database row.
          * 
          * @return This {@link CourseType}'s unique integer identifier.
          */
         public int toInt() {
             return mIntIdentifier;
         }
 
         /**
          * Convert an integer identifier from a database row back into a proper
          * {@link CourseType}.
          * 
          * @param intIdentifier
          *            The integer identifier from a database row.
          * @return The {@link CourseType} corresponding to the given integer
          *         identifier.
          */
         public static CourseType fromInt(int intIdentifier) {
             switch (intIdentifier) {
             case 1:
                 return POINT_TOTAL;
             case 2:
                 return PERCENTAGE_WEIGHTING;
             }
 
             return null;
         }
     }
 
     private String mName;
     private CourseType mCourseType;
     private double mOverallScore;
     private double mTotalPossibleScore;
 
     private Collection<GradeComponent> mGradeComponents;
 
     /**
      * Get the human readable name of the course.
      * 
      * @return The human readable name of the course.
      */
     public String getName() {
         return mName;
     }
 
     /**
      * Set the human readable name of the course.
      * 
      * @param name
      *            The new human readable name of the course.
      */
     public void setName(String name) {
         mName = name;
     }
 
     /**
      * Get the "type" ({@link CourseType}) of the course.
      * 
      * @return The {@link CourseType} that designates how this course calculates
      *         its final score.
      */
     public CourseType getCourseType() {
         return mCourseType;
     }
 
     /**
      * Set the "type" ({@link CourseType}) of the course.
      * 
      * @param courseType
      *            The new {@link CourseType} of this course.
      */
     public void setCourseType(CourseType courseType) {
         mCourseType = courseType;
     }
 
     /**
      * Calculate the total score for this course by combining scores for all
      * contained {@link GradeComponent}s.
      * 
      * @return The overall score for this course, as a double less than or equal
      *         to {@code 1.0}.
      */
     public double getOverallScore() {
         calculateOverallScore();
         return mOverallScore;
     }
 
     /**
      * Get the total possible score for this course, based on the
      * {@link CourseType} of this course (given by {@link #getCourseType()}). If
      * this course is of type {@link CourseType#POINT_TOTAL}, this value will be
      * the total available points. If this course of is type
      * {@link CourseType#PERCENTAGE_WEIGHTING} , this value will be the total
      * sum of all the weights of the contained {@link PercentageGradeComponent}
      * s. In this case, the expected value will be {@code 1.0}.
      * 
      * @return The total possible score for this course, based on the
      *         {@link CourseType} of this course (given by
      *         {@link #getCourseType()}).
      */
     public double getTotalPossibleScore() {
         calculateOverallScore();
         return mTotalPossibleScore;
     }
 
     /**
      * Calculate the overall and total score values, saving them off in the
      * private member variables {@link #mOverallScore} and
      * {@link #mTotalPossibleScore}.
      */
     private void calculateOverallScore() {
         mTotalPossibleScore = 0;
 
         if (getCourseType() == CourseType.POINT_TOTAL) {
 
             // Sum total points
             double totalPoints = 0;
             double totalEarned = 0;
 
             for (GradeComponent component : getGradeComponents()) {
                 PointTotalGradeComponent pointComponent = (PointTotalGradeComponent) component;
                 totalPoints += pointComponent.getTotalPoints();
                 totalEarned += pointComponent.getPointsEarned();
             }
 
             // Store the point total for reference
             mTotalPossibleScore = totalPoints;
 
             mOverallScore = totalEarned / totalPoints;
         } else {
 
             // Sum weighted individual scores
             double totalScore = 0;
             double totalWeight = 0;
 
             for (GradeComponent component : getGradeComponents()) {
                 PercentageGradeComponent percentageComponent = (PercentageGradeComponent) component;
                 totalScore += percentageComponent.getEarnedPercentage()
                         * percentageComponent.getWeight();
                 totalWeight += percentageComponent.getWeight();
             }
 
             // Store the weighting total for reference
             mTotalPossibleScore = totalWeight;
 
             mOverallScore = totalScore;
         }
     }
 
     /**
      * Get the collection of grade components that this course contains. If this
      * course contains no grade components, an empty collection will be
      * returned. If this object is persisted in the database, grade components
      * will be loaded automatically (if they have not be already) here.
      * 
      * @return The collection of grade components that this course contains.
      */
     public Collection<GradeComponent> getGradeComponents() {
         initializeGradeComponents();
 
         // Copy components to a new list so the original can't be modified
         ArrayList<GradeComponent> result = new ArrayList<GradeComponent>();
         result.addAll(mGradeComponents);
 
         return result;
     }
 
     /**
      * Add a grade component to this course. This method also takes care of
      * calling {@link GradeComponent#setCourse(Course)} on the added component,
      * thus establishing a proper back link between the objects. This method
      * also prevents duplicate {@link GradeComponent}s from being added to the
      * course, simply by ignoring components that have already been added.
      * 
      * @param gradeComponent
      *            The grade component to add to this course.
      */
     public void addGradeComponent(GradeComponent gradeComponent) throws IllegalArgumentException {
         initializeGradeComponents();
 
         validateGradeComponent(gradeComponent);
 
         if (!mGradeComponents.contains(gradeComponent)) {
             mGradeComponents.add(gradeComponent);
             gradeComponent.setCourse(this);
         }
     }
 
     /**
      * Add several grade components to this course. This method also takes care
      * of calling {@link GradeComponent#setCourse(Course)} on the added
      * components, thus establishing a proper back link between the objects.
      * This method also prevents duplicate {@link GradeComponent}s from being
      * added to the course, simply by ignoring components that have already been
      * added.
      * 
      * @param gradeComponents
      *            The grade components to add to this course.
      */
     public void addGradeComponents(Collection<GradeComponent> gradeComponents)
             throws IllegalArgumentException {
         initializeGradeComponents();
 
         for (GradeComponent gradeComponent : gradeComponents) {
             validateGradeComponent(gradeComponent);
 
             if (!mGradeComponents.contains(gradeComponent)) {
                 mGradeComponents.add(gradeComponent);
                 gradeComponent.setCourse(this);
             }
         }
     }
 
     /**
      * Validate a to-be-added {@link GradeComponent}, making sure its type
      * corresponds with this {@link Course}'s {@link CourseType}. If there is a
      * mismatch, a {@link IllegalArgumentException} is thrown.
      * 
      * @param gradeComponent
      *            The {@link GradeComponent} to be added.
      * @throws IllegalArgumentException
      *             When the given {@link GradeComponent}'s type does not
      *             correspond to this {@link Course}'s {@link CourseType}.
      */
     private void validateGradeComponent(GradeComponent gradeComponent)
             throws IllegalArgumentException {
         if (gradeComponent instanceof PercentageGradeComponent
                 && getCourseType() == CourseType.POINT_TOTAL
                 || gradeComponent instanceof PointTotalGradeComponent
                 && getCourseType() == CourseType.PERCENTAGE_WEIGHTING) {
             throw new IllegalArgumentException("Grading type argument mismatch");
         }
     }
 
     /**
      * Remove a {@link GradeComponent} from this {@link Course}. If the
      * component is not a member of this course, nothing is done.
      * 
      * @param gradeComponent
      *            The grade component to remove from this course.
      */
     public void removeGradeComponent(GradeComponent gradeComponent) {
         initializeGradeComponents();
 
         if (mGradeComponents.contains(gradeComponent)) {
             mGradeComponents.remove(gradeComponent);
         }
     }
 
     /**
      * Remove several grade components from this course.
      * 
      * @param gradeComponents
      *            The grade components to remove from this course.
      */
     public void removeGradeComponents(Collection<GradeComponent> gradeComponents) {
         initializeGradeComponents();
 
         for (GradeComponent gradeComponent : gradeComponents) {
             if (mGradeComponents.contains(gradeComponent)) {
                 mGradeComponents.remove(gradeComponent);
             }
         }
     }
 
     /**
      * A helper method to load connected {@link GradeComponent}s or instantiate
      * a new empty {@link Collection} in the {@link #mGradeComponents} member
      * variable.
      */
     private void initializeGradeComponents() {
         // TODO: Populate this from the database if it doesn't exist already
 
         if (mGradeComponents == null) {
             mGradeComponents = new HashSet<GradeComponent>();
         }
     }
 
     /**
      * Get all database-tracked {@link Course}s.
      * 
      * @return A {@link Collection} of all database-tracked {@link Course}s.
      */
     public static Collection<Course> getAllCourses() {
         CourseStorageIterator allCourses = new CourseStorageAdapter().getAllCourses();
         ArrayList<Course> result = new ArrayList<Course>(allCourses.getCount());
 
         for (Course course : allCourses) {
             result.add(course);
         }
 
         return result;
     }
 
     @Override
     public void save() {
         new CourseStorageAdapter().saveCourse(this);
     }
 
     @Override
     public void destroy() {
         new CourseStorageAdapter().deleteCourse(this);
     }
 
     @Override
     public void loadConnectedObjects() {
         // TODO
 
     }
 
     @Override
     public boolean equals(Object o) {
         if (!(o instanceof Course))
             return false;
         return compareTo((Course) o) == 0;
     }
 
     @Override
     public int compareTo(Course other) {
         if (getId() > 0 && other.getId() > 0) {
             return (int) Math.signum(getId() - other.getId());
         }
 
         if (!getName().equals(other.getName())) {
             return getName().compareTo(other.getName());
         }
 
         return (int) Math.signum(getCourseType().toInt() - other.getCourseType().toInt());
     }
 }
