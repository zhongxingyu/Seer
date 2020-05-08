 package com.customfit.ctg.model;
 
 import java.util.*;
 
 /**
  * This describes a User of the system, including the user's name,
  * managed Members, and nutrition plans.
  * 
  * @author David
  */
 public class User
 {
 
     /**
      * The user's name.
      */
     private String name;
 
     /**
      * The user's members.
      */
     private List<Member> members;
     
     /**
      * The list of meals for the user.
      */
     private List<Meal> meals = new LinkedList<Meal>();
     
     /**
      * The nutrient to track.
      */
     private String trackedNutrient = "";
     
     /**
      * Constructs a user with the name provided.
      * 
      * @param name The user's name.
      */
     public User(String name)
     {
         this.name = name;
         this.members = new ArrayList<Member>();
         this.members.add(new Member(name));
     }
 
     /**
      * Constructs a user with the information provided.
      * 
      * @param name The user's name.
      * @param members All the user's members.
      * @param meals The list of meals for the user.
      * @param trackedNutrient The nutrient to track.
      */
     public User(String name, List<Member> members, List<Meal> meals, String trackedNutrient)
     {
         this.name = name;
         this.members = members;
         this.meals = meals;
         this.trackedNutrient = trackedNutrient;                
     }
 
     /**
      * Gets the user's name.
      * 
      * @return The user's name.
      */
     public String getName()
     {
         return name;
     }
 
     /**
      * Sets the user's name.
      * 
      * @param name The user's name.
      */
     public void setName(String name)
     {
         this.name = name; 
     }
     
     /**
      * Gets all of the the user's members.
      * 
      * @return The user's members.
      */
     public List<Member> getAllMembers()
     {
         return members;
     }
     
     /**
      * Gets the list of meals for the user.
      * 
      * @return The list of meals for the user.
      */
     public List<Meal> getMeals()
     {
         return meals;
     }
     
     /**
      * Gets the member by name.
      * 
      * @return The member with that name, or null if nonexistent.
      */
     public Member getMemberByName(String memberName)
     {
         for (Member member : this.getAllMembers())
         {
             if (member.getName().equals(memberName))
                 return member;
         }
         return null;
     }
     
     /**
      * Get own user's member data, which is the member
      * that has the same name as the user.
      * 
      * @return The member with the name of the user name.
      */
     public Member getOwnMember()
     {
         return this.getMemberByName(this.name);
     }
     
     /**
      * Set own user's member data, which is the member
      * that has the same name as the user.
      * 
      * @param goal The nutritional goal set for the Member.
      * @param goalDirection The direction of the nutritional goal.
      */
     public void setOwnMember(Measurement goal, GoalDirection goalDirection)
     {
         for (Member member : this.getAllMembers())
         {
             if (member.getName().equals(member.getName()))
             {
                 member.setGoal(goal);
                 member.setGoalDirection(goalDirection);
             }
         }
     }
     
     /**
      * Set own user's member data, which is the member
      * that has the same name as the user.
      * 
      * @param member The member to copy data to user's own member.
      */
     public void setOwnMember(Member member)
     {
         setOwnMember(member.getGoal(), member.getGoalDirection());
     }
     
     /**
      * Gets all members except the user's own member data, which is the
      * member that has the same name as the user.
      * 
      * @return A list of Members.
      */
     public List<Member> getAllOtherMembers()
     {
         List<Member> members = new ArrayList<Member>();
         for (Member member : this.getAllMembers())
         {
             if (!member.getName().equals(this.getName()))
             {
                 members.add(member);
             }
         }
         return members;
     }
     
     /**
      * Gets the nutrient to track.
      * 
      * @return The nutrient to track.
      */
     public String getTrackedNutrient()
     {
         return trackedNutrient;
     }
 
     /**
      * Sets the nutrient to track.
      * 
      * @param trackedNutrient The nutrient to track.
      */
     public void setTrackedNutrient(String trackedNutrient)
     {
         this.trackedNutrient = trackedNutrient;
     }
   
     /**
      * Gets a List of Meals for the date period specified.
      * 
      * @param startDate The date (time is ignored in liue of 00:00:00.000).
      * @param endDate Ending date (time is ignored in lieu of 23:59:59.999)
      * 
      * @return List of Meals.
      */
     public List<Meal> getMealsByDateRange(Date startDate, Date endDate)
     {
         Calendar calendarStart = Calendar.getInstance();
         calendarStart.setTime(startDate);
         calendarStart.set(Calendar.HOUR, 0);
         calendarStart.set(Calendar.MINUTE, 0);
         calendarStart.set(Calendar.SECOND, 0);
         calendarStart.set(Calendar.MILLISECOND, 0);
         startDate = calendarStart.getTime();
         
         Calendar calendarEnd = Calendar.getInstance();
         calendarEnd.setTime(startDate);
         calendarEnd.set(Calendar.HOUR, 23);
         calendarEnd.set(Calendar.MINUTE, 59);
         calendarEnd.set(Calendar.SECOND, 59);
         calendarEnd.set(Calendar.MILLISECOND, 999);
         endDate = calendarEnd.getTime();
         
         List<Meal> meals = new ArrayList<Meal>();
         for (Meal meal : this.getMeals())
         {
             if (meal.getDate().after(startDate) && meal.getDate().before(endDate))
             {
                 meals.add(meal);
             }
         }
 
         return meals;
     }
     
     /**
      * Gets a List of Meals for the date specified.
      * 
      * @param date The date (time is ignored in liue of 00:00:00.000).
      * 
      * @return List of Meals.
      */
     public List<Meal> getMealsByDate(Date date)
     {
         Calendar calendarStart = Calendar.getInstance();
         calendarStart.setTime(date);
         calendarStart.set(Calendar.HOUR, 0);
         calendarStart.set(Calendar.MINUTE, 0);
         calendarStart.set(Calendar.SECOND, 0);
         calendarStart.set(Calendar.MILLISECOND, 0);
         Date startDate = calendarStart.getTime();
         
         Calendar calendarEnd = Calendar.getInstance();
         calendarEnd.setTime(date);
         calendarEnd.set(Calendar.HOUR, 23);
         calendarEnd.set(Calendar.MINUTE, 59);
         calendarEnd.set(Calendar.SECOND, 59);
         calendarEnd.set(Calendar.MILLISECOND, 999);
         Date endDate = calendarEnd.getTime();
         
         List<Meal> meals = new ArrayList<Meal>();
         for (Meal meal : this.getMeals())
         {
             if (meal.getDate().after(startDate) && meal.getDate().before(endDate))
             {
                 meals.add(meal);
             }
         }
 
         return meals;
    }
    
    /**
      * Gets a list of meals starting on the date specified and
      * all the way up to 7 days later.
      * 
      * It starts at the first moment of the start date and ends at last
      * moment of the end date.
      * 
      * @param startDate The date (time is ignored in liue of 00:00:00.000).
      * 
      * @return List of Meals.
      */
    public List<Meal> getMealsForWeekStartingOn(Date startDate)
    {
         Calendar calendarStart = Calendar.getInstance();
         calendarStart.setTime(startDate);
         calendarStart.set(Calendar.HOUR, 0);
         calendarStart.set(Calendar.MINUTE, 0);
         calendarStart.set(Calendar.SECOND, 0);
         calendarStart.set(Calendar.MILLISECOND, 0);
         startDate = calendarStart.getTime();
         
         Calendar calendarEnd = Calendar.getInstance();
         calendarEnd.setTime(startDate);
         calendarEnd.add(Calendar.DATE, 7);
         calendarEnd.set(Calendar.HOUR, 23);
         calendarEnd.set(Calendar.MINUTE, 59);
         calendarEnd.set(Calendar.SECOND, 59);
         calendarEnd.set(Calendar.MILLISECOND, 999);
         Date endDate = calendarEnd.getTime();
         
         List<Meal> meals = new ArrayList<Meal>();
         for (Meal meal : this.getMeals())
         {
             if (meal.getDate().after(startDate) && meal.getDate().before(endDate))
             {
                 meals.add(meal);
             }
         }
 
         return meals;
    }
 }
