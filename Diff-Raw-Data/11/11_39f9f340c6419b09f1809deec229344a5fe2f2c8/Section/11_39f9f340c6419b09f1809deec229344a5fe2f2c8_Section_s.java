 
  // CLASS: Section
      //
      // Author: James Firth, 7671568
      //
      // REMARKS: Holds the rows for one section of the airplane
      //
      //-----------------------------------------
 import java.util.GregorianCalendar;
 import java.util.Random;
 public class Section
 {
     private Row[] rows;
     private int span;
     private int secNum;
     private int filled;
     private int totalSeats;
     private int firstRowID;
     
     //creates a Section and intializes data
     public Section (String info,int id,int num,int rowID)
     {
         secNum = num;
         firstRowID = rowID;
         String[] temp = info.split("\\s+"); //Splits the numbers up
         int[] nums = new int[temp.length]; //Creates array of ints
         int spanning=0;
         filled = 0;
         totalSeats = 0;
         
         //Converts strings to ints and records total span of section
         for(int i=0; i < nums.length; i++)
         {
             nums[i] = Integer.parseInt(temp[i]);
             if(i>0)
                 spanning +=nums[i];
         }
         spanning += nums.length-2;//CHANGED
         span = spanning;
         
         
         //System.out.println("span:"+span+"*"+nums[0]);
         
         //Creates array of rows
         rows = new Row[nums[0]];
         for(int i=0; i < rows.length; i++)
         {
             rows[i] = new Row(nums,span,id,(i+1)); //sends info to make new rows
             totalSeats += rows[i].getNumSeats();
             id += rows[i].getNumSeats();  /////////// WAS numSeats ////////////////////////////////////////////
         }
     }
     
     ///////////// SETTERS ////////
     public void fillSeats(int x)
     {
         filled += x;
     }
     
     /////////////////// GETTERS /////////////////////
  
     
     public int numRows()
     {
         return rows.length;
     }
     
     public int numSeats()
     {
         /*
         int numSeats=0;
         for(int i=0; i < rows.length; i++)
         {
             numSeats += rows[0].numSeats();
         }
         return numSeats;
          * 
          */
         return totalSeats;
     }
     
     
     
     //////////////// SEAT MANIPULATORS ////////////////////////////
     public boolean reserveSeats(UnorderedList options, Passenger[] people, int count,int[] needed, OrderedList passManifest)
     {
         int windNeeded = needed[0];
         int aisNeeded = needed[1];
         boolean ever = false;
         boolean tempBo = false;
         int groupType = -1;
         boolean allReserved = false;
         
         if((totalSeats-filled) >= count)//////////////// FIX THIS LATER
         {
             for(int i=0; i < rows.length;i++)
             {
     //System.out.println("Preferences!");
     //System.out.println("# of People:"+people.length);
                 tempBo = rows[i].reserveSeats(people,count,options,span,windNeeded,aisNeeded);
                 if(tempBo == true)
                 {
                     ever = true;
                     groupType = 0;
                 }
             }
             
             //In case we couldn't get any seats with all their preferences...
             if(options.isEmpty())
             {
     //System.out.println("Just grouped!");
                 windNeeded = 0;
                 aisNeeded=0;
                 for(int i=0; i < rows.length;i++)
                 {
                     tempBo = rows[i].reserveSeats(people,count,options,span,0,0); //0's mean we don't need any of either type
                     if(tempBo == true)
                     {
                         ever = true;
                         groupType = 1;
                     }
                 } 
     //System.out.println("ENd of grouped");
             }
             
             //If it's stiiiiilllllll empty, we'll just seat them by themselves.
             if(options.isEmpty())
             {                                  /////////////////////////////////////// TOOOOO DOOOOO
     //System.out.println("Singles!");         ///////////////////////////////////////   
                 windNeeded = -99;
                 aisNeeded = -99;
                 needed[0] = -99; ///JUST ADDED
                 needed[1] = -99; /// JUST ADDED
                 Passenger[] singles = new Passenger[1];
                 for(int i=0; i < people.length; i++)
                 {
                     singles[0] = people[i]; //For each person check all possibilities
                     for(int x=0; x < rows.length; x++)
                     {
                         tempBo = rows[x].reserveSeats(singles,1,options,span,0,0); //0's mean we don't need any of either type
                         if(tempBo == true)
                         {
                             ever = true;
                             groupType = 2;
                         }
                     }
                 }
             }
         }//if
         
         //If there's options we're going to seat them!
         if(ever && !options.isEmpty())
         {
             if(groupType == 0)
                 allReserved = finalizePreferredReservation(options,people,aisNeeded,windNeeded,passManifest);
             if(groupType == 1)
                 allReserved = finalizeGroupedReservation(options,people,groupType,passManifest);
             if(groupType == 2)
                allReserved = finalizeSinglesReservation(options,people,groupType,passManifest);
         }
        
         
         return (ever && allReserved);
     }
     
     private boolean finalizePreferredReservation(UnorderedList options, Passenger[] people, int aisNeeded, int winNeeded, OrderedList passManifest)
     {
         Random randomizer = new Random(); 
         boolean seated = false;
         boolean inManifest = false;
         int picked = randomizer.nextInt(options.getLength());
         int aisFilled = 0;
         int winFilled = 0;
         int seatedSoFar = 0;
         
         SeatGroup groupOfSeats = options.getGroup(picked);
         Seat[] theSeats = groupOfSeats.getSeats();
         
         
         /********    Deal with people with Prefs        ***********/
         for(int i=0; i < people.length; i++)
         {
             //Deal with a window person
             if(people[i].getPref() == 'w')
             {
                 for(int y=0; y < theSeats.length && winFilled < winNeeded; y++)
                 {
                     if(theSeats[y].isWindow() && theSeats[y].isEmpty())
                     {
                         seated = theSeats[y].setPass(people[i]);
                         inManifest = passManifest.add(theSeats[y].getPass()); 
                         seatedSoFar++;
                         winFilled++;
                     }
                 }
             }
             
             if(people[i].getPref()=='a')
             {
                 for(int y=0; y < theSeats.length && aisFilled < aisNeeded; y++)
                 {
                     if(theSeats[y].isAisle() && theSeats[y].isEmpty())
                     {
                         seated = theSeats[y].setPass(people[i]);
                         inManifest = passManifest.add(theSeats[y].getPass()); 
                         seatedSoFar++;
                         aisFilled++;
                     }
                 }
             }
         }
         
         for(int i=0; i < theSeats.length && seatedSoFar<=people.length; i++)
         {
             if(theSeats[i].isEmpty())
             {
                 seated = theSeats[i].setPass(people[i]);
                 inManifest = passManifest.add(theSeats[i].getPass());    
             }
         }
         
         return (seated && inManifest);
     }
     
     private boolean finalizeGroupedReservation(UnorderedList options, Passenger[] people, int type, OrderedList passManifest)
     {
        Random randomizer = new Random();
        boolean seated = false;
        boolean inManifest = false;
        int picked = randomizer.nextInt(options.getLength());
        SeatGroup groupOfSeats = options.getGroup(picked);
        Seat[] theSeats = groupOfSeats.getSeats();
        
        for(int i=0; i < theSeats.length; i++)
        {
         seated = theSeats[i].setPass(people[i]);
         inManifest = passManifest.add(theSeats[i].getPass());  
        }
        
        return (seated && inManifest);
     }
     
     private boolean finalizeSinglesReservation(UnorderedList options, Passenger[] people, int type, OrderedList passManifest)
     {
       Random randomizer = new Random(); 
       boolean seated = false;
       boolean inManifest = false;
       int picked = randomizer.nextInt(options.getLength());
       SeatGroup groupOfSeats = options.getGroup(picked);
       Seat[] theSeats = groupOfSeats.getSeats();
         
       
       //Adds each person to both the manifest and the seating chart
       for(int i=0; i < theSeats.length; i++)
       {
         theSeats[i].setPass(people[i]);
         passManifest.add(theSeats[i].getPass());
       }
       
       return (seated && inManifest);
     }
     
     /****************************** OLD RESERVATION
     public boolean reserveSeats(UnorderedList options, Passenger[] people, int count,int[] needed)
     {
         int windNeeded = needed[0];
         int aisNeeded = needed[1];
         boolean ever = false;
         boolean tempBo = false;
         if((totalSeats-filled) >= count)//////////////// FIX THIS LATER
         {
         for(int i=0; i < rows.length;i++)
         {
 //System.out.println("Preferences!");
 //System.out.println("# of People:"+people.length);
             tempBo = rows[i].reserveSeats(people,count,options,span,windNeeded,aisNeeded);
             if(tempBo == true)
                 ever = true;
         }
         
         //In case we couldn't get any seats with all their preferences...
         if(options.isEmpty())
         {
 //System.out.println("Just grouped!");
             windNeeded = 0;
             aisNeeded=0;
             for(int i=0; i < rows.length;i++)
             {
                 tempBo = rows[i].reserveSeats(people,count,options,span,0,0); //0's mean we don't need any of either type
                 if(tempBo == true)
                     ever = true;
             } 
 //System.out.println("ENd of grouped");
         }
         
         //If it's stiiiiilllllll empty, we'll just seat them by themselves.
         if(options.isEmpty())
         {                                  /////////////////////////////////////// TOOOOO DOOOOO
 //System.out.println("Singles!");         ///////////////////////////////////////   
             windNeeded = -99;
             aisNeeded = -99;
             needed[0] = -99; ///JUST ADDED
             needed[1] = -99; /// JUST ADDED
             Passenger[] singles = new Passenger[1];
             for(int i=0; i < people.length; i++)
             {
                 singles[0] = people[i]; //For each person check all possibilities
                 for(int x=0; x < rows.length; x++)
                 {
                     tempBo = rows[x].reserveSeats(singles,1,options,span,0,0); //0's mean we don't need any of either type
                     if(tempBo == true)
                         ever = true;
                 }
             }
         }
         }//if
         
         
         return ever;
     }
     * */
     
     public boolean cancelIndividual(Passenger person)
     {
         boolean cancelled = false;
         int rowID = person.getRow() - firstRowID;
         
         /*if(firstRowID==1)
             rowID = person.getRow()-1;
         else
             rowID = person.getRow() - firstRowID; //This means if the person is in row "4" we can correct it to be relative to this section. ie: S1 has 3 rows we want row 4. so 4-4 is s2's row 0.
         
         * 
         */
         if(rowID < rows.length)
         {
             cancelled = rows[rowID].cancelIndividual(person);
         }
         if(cancelled)
             filled--;
         else
             System.out.println("Error in Section cancelling!");
         
         
         return cancelled;
     }
     /*
     public boolean cancelSeats(Passenger[] people, int count,OrderedList passManifest)
     {
         boolean removed = false;
         int rowID = people[0].getRow();
         removed = rows[rowID].cancelSeats(people,passManifest);
         /*
         for(int i=0; i < rows.length; i++)
         {
             rows[i].cancelSeats(people);
         }
          * 
          * /
         return removed;
     }
 */
     
     
     /////////////// FUNCTION ///////////////////////////
     public void printPlan(int rowNum,GregorianCalendar leave)
     {
         System.out.println("Total Seats:"+totalSeats+" Filled:"+filled);
         for(int i=0; i < rows.length; i++)
         {
             String out = String.format("%2d:",rowNum);
             System.out.print(out);
             rows[i].printPlan(leave);
             rowNum++;
         }
     }
 }
