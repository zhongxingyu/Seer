 package com.kinnack.nthings.model;
 
 import com.kinnack.nthings.model.level.Level;
 
 public class Workout {
     public enum Type {
         PUSHUP,
         SITUP,
         SQUAT
     }
     public static final int[] STANDARD_REST = new int[]{60000};
     public static final int[] SHORT_REST = new int[]{45000};
     
     public static final int EASY = 0;
     public static final int MED = 1;
     public static final int HARD = 2;
     //[week][day][level][counts][rests]
     protected static final int[][][][][] PUSHUPS = new int[][][][][] {
         // week 1
         {
             // day 1 
             {
                 // easy 
                 {
                     { 2, 3, 2, 2, 3 }, STANDARD_REST
                 },
                 // med
                 {
                     { 6, 6, 4, 4, 5 }, STANDARD_REST                    
                 },
                 // hard
                 {
                    { 10, 12, 7, 7, 9 }, STANDARD_REST
                 }
             },
             // day 2
             {
                 // easy 
                 {
                     { 3, 4, 2, 3, 4 }, STANDARD_REST
                 },
                 // med
                 {
                     { 6, 8, 6, 6, 7 }, STANDARD_REST
                 },
                 // hard
                 {
                     { 10, 12, 8, 8, 12 }, STANDARD_REST
                 }
             },
             // day 3
             {
                 // easy
                 {
                     { 4, 5, 4, 4, 5 }, STANDARD_REST                  
                 },
                 // med
                 {
                     { 8, 10, 7, 7, 10 }, STANDARD_REST
                 },
                 // hard
                 {                    
                     { 11, 15, 9, 9, 13}, STANDARD_REST
                 }
             }
         },
         // week 2
         {
             // day 1 
             {
                 // easy 
                 {
                     { 4, 6, 4, 4, 6 }, STANDARD_REST
                 },
                 // med
                 {
                    { 9, 11, 8, 8, 11 }, STANDARD_REST
                 },
                 // hard
                 {
                     { 14, 14, 10, 10, 15 }, STANDARD_REST
                 }
             },
             // day 2
             {
                 // easy 
                 {
                     { 5, 6, 4, 4, 7 }, STANDARD_REST
                 },
                 // med
                 {
                    { 10, 12, 9, 9, 13 }, STANDARD_REST
                 },
                 // hard
                 {
                     { 14, 16, 12, 12, 17 }, STANDARD_REST
                 }
             },
             // day 3
             {
                 // easy
                 {
                     { 5, 7, 5, 5, 8 }, STANDARD_REST
                 },
                 // med
                 {
                     { 12, 13, 10, 10, 15 }, STANDARD_REST
                 },
                 // hard
                 {                    
                    { 16, 17, 14,14, 20  }, STANDARD_REST
                 }
             }
         },
         // week 3
         {
             // day 1 
             {
                 // easy 
                 {
                     { 10, 12, 7, 7, 9 }, STANDARD_REST
                 },
                 // med
                 {
                    { 12, 17, 13, 13, 17 }, STANDARD_REST
                 },
                 // hard
                 {
                     { 14, 18, 14, 14, 20  }, STANDARD_REST
                 }
             },
             // day 2
             {
                 // easy 
                 {
                    { 10, 12, 8, 8, 12 }, STANDARD_REST
                 },
                 // med
                 {
                    { 14, 19, 14, 14, 19 }, STANDARD_REST
                 },
                 // hard
                 {
                     { 20, 25, 15, 15, 25 }, STANDARD_REST
                 }
             },
             // day 3
             {
                 // easy
                 {
                    { 11, 13, 9, 9, 13 }, STANDARD_REST
                 },
                 // med
                 {
                    { 16, 21, 15, 15, 21 }, STANDARD_REST
                 },
                 // hard
                 {                    
                     { 22, 30, 20, 20, 28 }, STANDARD_REST
                 }
             }
         },
         // week 4
         {
             // day 1 
             {
                 // easy 
                 {
                    { 12, 14, 11, 10, 16 }, STANDARD_REST
                 },
                 // med
                 {
                    { 18, 22, 16, 16, 25}, STANDARD_REST
                 },
                 // hard
                 {
                     { 21, 25, 21, 21, 32 }, STANDARD_REST
                 }
             },
             // day 2
             {
                 // easy 
                 {
                    { 14, 16, 12, 12, 18 }, STANDARD_REST
                 },
                 // med
                 {
                    { 20, 25, 20, 20, 25 }, STANDARD_REST
                 },
                 // hard
                 {
                     { 25, 29, 25, 25, 36 }, STANDARD_REST
                 }
             },
             // day 3
             {
                 // easy
                 {
                     { 16, 18, 13, 13, 20 }, STANDARD_REST
                 },
                 // med
                 {
                     { 23, 28, 23, 23, 33 }, STANDARD_REST
                 },
                 // hard
                 {                   
                     { 29, 33, 29, 29, 40 }, STANDARD_REST
                 }
             }
         },
         
         
         
         
         // week 5
         {
             // day 1 
             {
                 // easy 
                 {
                     { 17, 19, 15, 15, 20 }, STANDARD_REST
                 },
                 // med
                 {
                     { 28, 35, 25, 22, 35 }, STANDARD_REST
                 },
                 // hard
                 {
                     { 36, 40, 30, 24, 40}, STANDARD_REST
                 }
             },
             // day 2
             {
                 // easy 
                 {
                     { 10, 10, 13, 13, 10, 10, 9, 25 }, SHORT_REST
                 },
                 // med
                 {
                     { 18, 18, 20, 20, 14, 14, 16, 40 }, SHORT_REST
                 },
                 // hard
                 {
                     { 19, 19, 22, 22, 18, 18, 22, 45 }, SHORT_REST
                 }
             },
             // day 3
             {
                 // easy
                 {
                     { 13, 13, 15, 15, 12, 12, 10, 30}, SHORT_REST
                 },
                 // med
                 {
                     { 18, 18, 20, 20, 17, 17, 20, 45}, SHORT_REST
                 },
                 // hard
                 {                    
                     { 20, 20, 24, 24, 20, 20, 22, 50 }, SHORT_REST
                 }
             }
         },
         // week 6
         {
             // day 1 
             {
                 // easy 
                 {
                     { 25, 30, 20, 15, 40 }, STANDARD_REST
                 },
                 // med
                 {
                     {40, 50, 25, 25, 50 }, STANDARD_REST
                 },
                 // hard
                 {
                     { 45, 55, 35, 30, 55}, STANDARD_REST
                 }
             },
             // day 2
             {
                 // easy 
                 {
                     { 14, 14, 15, 15, 14, 14, 10, 10, 44 }, SHORT_REST
                 },
                 // med
                 {
                     { 20, 20, 23, 23, 20, 20, 18, 18, 53 }, SHORT_REST
                 },
                 // hard
                 {
                     { 22, 22, 30, 30, 24, 24, 18, 18, 58 }, SHORT_REST
                 }
             },
             // day 3
             {
                 // easy
                 {
                     { 13, 13, 17, 17, 16, 16, 14, 14, 50 }, SHORT_REST
                 },
                 // med
                 {
                    { 22, 22, 30, 30, 24, 24, 18, 18, 55}, SHORT_REST
                 },
                 // hard
                 {                    
                    { 26, 26, 33, 33, 26, 26, 22, 22, 60 }, SHORT_REST
                 }
             }
         }
     };
         
         
     public static ExerciseSet getPushupSetFor(int week, int day, Level level) {
         // TODO error handling, argument checking
         int[][] countRest = PUSHUPS[week-1][day-1][level.getIndex()];
         return new ExerciseSet(countRest[0], countRest[1]);
     }
         
     
 }
