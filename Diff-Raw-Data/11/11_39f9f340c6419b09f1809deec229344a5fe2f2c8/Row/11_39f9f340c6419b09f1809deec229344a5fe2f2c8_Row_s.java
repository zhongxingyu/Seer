  // CLASS: Row
      //
      // Author: James Firth, 7671568
      //
      // REMARKS: holds a row of seats and manipulates that data
      //
      //-----------------------------------------
 import java.util.GregorianCalendar;
 public class Row 
 {
     private Seat[] seats;
     private int rowNum;
     private int numSeats;
     
     public Row(int[] info,int span, int id, int num)
     {
         
         rowNum = num;
         numSeats = 0;
         //create array of blocks
         seats = new Seat[span];
        //send info to make each block
         int x = 1;
         int nextAisle = info[x];
         for(int i=0; i < seats.length; i++)
         {
 
             //If it's an aisle it will be null
             if(i == nextAisle)
             {
                 seats[i] = null; //sets it to null
                 x++;             //bumps over counter
                 nextAisle += (info[x]+1); //sets the location of next aisle
                 seats[i-1].setAisle(true); //Sets the one before as an aisle seat
             }
             else
             {
                 numSeats++;
                 seats[i] = new Seat(id,rowNum);
                 id++;
                 if(i==0 || i==(span-1)) //if it's a window
                     seats[i].setWindow(true);
                 
                 if(i>0 && seats[i-1] == null)//If the one before it is an aisle block. Set the aisle tag
                     seats[i].setAisle(true);
             }
         }
     }
     
     
     //////////////GETTERS ////////////////
     
     public int getNumSeats()
     {
         return numSeats;
     }
     
     public int getRowLength()
     {
         /*
         int num=0;
         for(int i=0; i< seats.length; i++)
         {
             if(seats[i]!=null)
                 num++;
         }
         * 
         */
         return seats.length;
     }
     public void printPlan(GregorianCalendar leave)
     {
 //System.out.println("SEATS:"+seats.length);
         for(int i=0; i < seats.length;i++)
         {
             if(seats[i]!=null)
                 seats[i].printPlan(leave);
             else
                 System.out.print("___");
         }
         System.out.println();
     }
     
 
     //////////////////////// SEAT MANIPULATION ////////////////////////////
     public boolean reserveSeats(Passenger[] people, int toSeat, UnorderedList options,int span,int windNeeded, int aisNeeded)
     {
 //System.out.println("PEOPLE:"+people.length);
         int contiguous = 0;
         int curr = 0;
         Seat[] seated = new Seat[people.length];
         int currWins = 0;
         int currAis = 0;
         int numAisles = 0;
         int groupCount = 0;
 
         for(int i=0;i < span && (i+toSeat) <= span; i++)
         {
             for(int x=i;x<span && contiguous<toSeat;x++ )
             {
                 if(seats[x]!=null && seats[x].isEmpty())
                 {
                     contiguous++;
                     if(seats[x].isWindow())
                         currWins++;
                     if(seats[x].isAisle())
                         currAis++;
                 }
                 if(seats[x]==null)
                     numAisles++;
             }
             
             if(contiguous==toSeat && currWins>=windNeeded && currAis >= aisNeeded)
             {
                 curr = i;
 
                 while(groupCount<toSeat && curr<seats.length)
                 {
                     if(seats[curr]!=null)
                     {
 //System.out.println("CURR:"+curr+"GROUP"+groupCount+"PERSON"+people[0].getFirstName());
                         seated[groupCount] = seats[curr];
                         groupCount++;
                     }
                     curr++;
                 }
                 options.add(new SeatGroup(seated));
                 groupCount = 0;
                 contiguous =0;
                 currWins = 0;
                 currAis=0;
             }
         }
         
        return options.isEmpty();
         /*
         //Loops while we're before the end of the row
         //Stops if the group will overflow to the next row.
         for(int i=0; i < span && (startPos+sizeOffset+toSeat <= span); i++)
         {
             if(seats[i]!=null && seats[i].isEmpty())
             {
                contiguous++; 
                if(seats[i].isWindow())
                    currWins++;
                if(seats[i].isAisle())
                    currAis++;
             }
             if(seats[i]==null)
             {
                 sizeOffset++;
             }
             
             
             if(contiguous==toSeat && currWins >= windNeeded && currAis >= aisNeeded) //If there's enough space to seat the group
             {                    //  We add it to the List of options
                 seated = new Seat[toSeat+sizeOffset+startPos];
                 for(int x=0;x<seated.length;x++)
                 {
                     seated[x] = seats[startPos+x]; //grab seats
                 }
                 options.add(new SeatGroup(seated)); //add to list
                 
                 startPos++; //check from one over
                 contiguous=0; //start counting again
                 currWins = 0;
                 currAis = 0;
                 sizeOffset = 0;
             }
         }//for
         * 
         */
         
     }
     
     /*
     public boolean cancelSeats(Passenger[] people,OrderedList passManifest)
     {
         boolean done = false;
         int toRemove = people.length;
         int numRemoved = 0;
         for(int i=0; i < people.length; i++)
         {
             for(int x=0; x < seats.length && !done; x++)
             {
                 if(seats[x]!=null && seats[x].getPass() != null && people[i].compareTo(seats[x].getPass())==0)
                 {
                     passManifest.remove(seats[x].getPass());
                     seats[x].setPass(null);//removes from seating diagram
                     numRemoved++;
                     done = true;
                 }//
             }
             done = false;
         }
         return (numRemoved == toRemove);
     }
     * 
     */
     public boolean cancelIndividual(Passenger person)
     {
         boolean cancelled = false;
 //System.out.println("Got to Row Cancel!");
         for(int i=0; i < seats.length && !cancelled; i++)
         {
             if(seats[i].getPass() != null)
             {
                 if(person.compareTo(seats[i].getPass())==0)
                 {
                     //System.out.println("FOUND HIM:"+seats[i].getPass().toString()+ " is the same as "+person.toString());
                     seats[i].setPass(null);
                     cancelled = true;
                 }
             }
         }
         if(!cancelled)
             System.out.println("ERROR IN ROW CANCELLING!");
         return cancelled;
     }
 }
