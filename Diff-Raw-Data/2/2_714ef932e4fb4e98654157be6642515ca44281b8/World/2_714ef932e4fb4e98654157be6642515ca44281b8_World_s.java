 package textbased;
 
 import java.util.*;
 
 /**
  * Text based version of Karel the Robot
  * Current version has 1 predefined map
  * @author Heather,Noel,Sam,Amber,Josh,MacsR4Luzrs
  */
 
 public class World 
 {
     private ArrayList walls = new ArrayList();//walls in world
     private ArrayList gems = new ArrayList(); //gems in world
     private boolean isRunning = true; //game ending bool
     Wall home = new Wall(0,0); // home space
     Player player; //object for karel
     
     //Map
     public static String level =
               "####################\n"
             + "#        $         #\n"
             + "#       $#$        #\n"
             + "#      $###$       #\n"
             + "#     $#####$      #\n"
             + "#    $#######$     #\n"
             + "#   $#########$    #\n"
             + "#  $###########$   #\n"
             + "#^ #############$ .#\n"
             + "####################\n";
     
     //Constructor - Set up world
     public World()
     {
         initWorld();
     }
     
     //Reads the map and adds all objects and their coordinates to arraylists
     public final void initWorld()
     {
         //create wall and gem objects
         Wall wall;
         Gem gem;
         
         //variables used to keep track of coordinates during loop
         int x = 0;
         int y = 0;
         
         for (int i = 0; i < level.length(); i++)
         {
             //Grab the item in string at i
             char item = level.charAt(i); 
 
             //Adjust X,Y value based on what character is at i
             //and create an item in the array list if needed
             if (item == '\n')
             {
                 y += 1;
                 x = 0;
             }
             else if (item == '#')
             {
                 wall = new Wall(x,y);
                 walls.add(wall);
                 x += 1;
             }
             else if (item == '^')
             {
                 player = new Player(x,y);
                 x += 1;
             }
             else if (item == '$')
             {
                 gem = new Gem(x,y);
                 gems.add(gem);
                 x += 1;
             }
             else if (item == '.')
             {
                 home.SetX(x);
                 home.SetY(y);
                 x += 1;
             }
             else if (item == ' ')
             {
                 x += 1;
             }
         }
         
         //Print the original map and legend
         System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
         System.out.println("   Karel the Robot");
         System.out.print("Karel's directions: ^ North | "
                 + "v South | > East | < West | ");
         System.out.println("# Walls | $ Gems | . Home");
         System.out.print(level);
         System.out.println("(Pick up all gems and go home)");
         
         //start up the game controller
         mController game = new mController();
     }
     
     //updates the map with karels new position
     public static void updateMap(int new_x, int new_y, char symbol) 
     {
         int num_rows = 10; // The number of rows
         int num_cols = 20; // The number of columns. Does not include the \n
         int num_symbols = 4; // The number of symbols for Karel
         int old_x = -1;
         int old_y = -1;
         char[] karel_symbols = {'^', '<', '>', 'v'}; // Karels symbols
         
         /* Converting from level string to temporary string array */
         String[] convert_map = new String [num_rows];
         for (int i= 0; i < num_rows; i++)
         {
             int x = (i * (num_cols + 1));
             convert_map[i] = level.substring(x, (x + num_cols));
         }
     
         /* Finding the last place Karel was and removing it */
         for (int i = 0; i < num_rows; i++)
         {
             for (int h = 0; h < num_symbols; h++)
             {
                 /* Iterating through all of the possible Karel symbols
                    and checking each string for their position. */
                 int checker = convert_map[i].indexOf(karel_symbols[h]);
                 if (checker != -1)
                 {
                     old_y = i;
                     old_x = checker;
                     break;
                 }
             }
         }
         
         /* Converting from temp string array to 2d character array*/
         char[][] current_map = new char [num_rows] [num_cols];
         for (int i = 0; i < num_rows; i++)
         {
             current_map[i] = convert_map[i].toCharArray();
         }
         if ((old_x != -1) && (old_y != -1))
         { // Making sure old_x and old_y were found
           current_map[old_y][old_x] = ' '; // Replacing Karel's old position
         }
         current_map[new_y][new_x] = symbol; // Putting Karel in his new position
 
         /* Overwriting level with updated map */
         String temp_level = new String();
         for (int i = 0; i < num_rows; i++)
         {
             for (int h = 0; h < num_cols; h++)
             {
                 temp_level += current_map[i][h];
             }
             temp_level += '\n';
         }
 
         level = temp_level;
     }
     
     //Game controller
     final class mController
     {
         public mController()
         {
             //Run the game until finished
             while (isRunning == true)
             {
                 //prompt user with choices and process input
                 choiceMade(choiceOptions());
                 
                 //Print the updated map
                 System.out.println("  Karel The Robot");
                 System.out.print(level);
                 System.out.println("Gems remaining: " + gems.size());
             }
         }
         
         //Prompt the user with choices
         public int choiceOptions()
         {
             System.out.println("Enter a choice:");
             System.out.println("1 - Go (Move Karel one space forward in his "
                     + "current direction)");
             System.out.println("2 - Turn Karel Left");
             System.out.println("3 - Turn Karel Right");
             System.out.println("4 - Multiple Instructions (Command Line)");
             
             Scanner in = new Scanner(System.in);
             int user_input = in.nextInt();
             System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
             return user_input;
         }
         
         public  void actions() 
         {
             Scanner in = new Scanner(System.in);
             List<String> user_input = new ArrayList<>();
             String new_input = new String();
             boolean check;
             System.out.println("Input instructions followed by "
                     + "enter (Use 0 to run all commands):");
             System.out.println("Left");
             System.out.println("Right");
             System.out.println("Go");
             System.out.println("While <condition>");
             System.out.println("If <condition>");
             System.out.println("Repeat <integer>");
             System.out.println("Conditions are: gem, home, and wall");
             System.out.println("Each can be preceded by 'not'");
             System.out.println("---------------------------------------------"
                                 + "---------------------------");
         
             while (true)
             { // Reading in the users instructions
                 new_input = in.nextLine().toLowerCase();
                 check = new_input.equals("0");
                 if (check)
                 { 
                     break; 
                 }
                 if (new_input.trim().length() > 0)
                 { // If the line isn't blank
                     user_input.add(new_input); // Adding it
                 }
             }
             System.out.println("---------------------------------------------"
                                 + "---------------------------");
             System.out.println("\n\n");
             int line_count = doScript(0, 0, user_input); // Running the script          
             if (line_count > user_input.size())
             { // If an error was returned
                 System.out.println("Press enter to continue...");
                 in.nextLine();
             }
         }    
             
             
         public int doScript(int line_count, int scope, List<String> user_input)
         { // Runs a user defined list of commands. Used recursively.
           // line_count is how far into the file we are
           // scope is the level of nested commands
           // user_input is the string array containing the file
             
             int max_line_count = user_input.size(); // Size of the file
             
             while (line_count < max_line_count) 
             { 
                 String current_line = user_input.get(line_count); // Gets the line we're
                                                                  // dealing with
                 String tempstr = new String(); // Used for swapping strings
                 String conditional = new String(); // Holds the condition
                                                    // to be checked.
                 int repeat_num = 0; //The number of times to repeat. Initialized
                                     //to a valid value for error checking
                 int next_line = 0; //Keeps the next line when dealing with scope
                 final int throw_error = max_line_count + 1; // Error return value
                 
                 if (scope > 0) // Checking for valid scope
                 {
                     int i;
                     for (i = 0; i < scope; i++)
                     {
                         if (!(current_line.startsWith("\t")))
                         {
                             return line_count; // Returning due to out of scope
                         }
                         
                         else
                         {
                             current_line = current_line.substring(1); // Removing the tab
                         }
                     }
                     if (current_line.startsWith("\t"))
                     {
                         System.out.println("ERROR: undefined scope on line "
                                           + (line_count + 1));
                         return throw_error;
                     }
                 }
                 
                 /* Parsing the current line for recognizable Syntax */
                 
                 if (current_line.matches("^repeat [0-9]{1,}$")) // Parsing repeat
                 {
                     tempstr = current_line.substring(7); // Grabbing the number
                     repeat_num = Integer.valueOf(tempstr);
                     tempstr = current_line.substring(0, 6); // Grabbing the repeat
                     current_line = tempstr;
                 }
                 
                 if(current_line.matches("^if (not )?(gem|home|wall)$")) // Parsing if
                 {
                     conditional = current_line.substring(3); // Grabbing condition
                     tempstr = current_line.substring(0, 2); // Grabbing if
                     current_line = tempstr;
                 }
                 
                 if (current_line.matches("^while (not )?(gem|home|wall)$")) // Parsing while
                 {
                     conditional = current_line.substring(6); // Grabbing condition
                     tempstr = current_line.substring(0, 5); // Grabbing while
                     current_line = tempstr;
                 }
                 
                 /* End Parsing */ 
                 
                 current_line = current_line.trim();
                 switch (current_line)
                 { // Controls the logic for each valid command
                   // If input is something unexpected, it halts execution and
                   // prints an appropriate error
                   // Any time an error is encountered, max_line_count + 1 is
                   // returned, signaling to end execution
                   // Note: Since line_count is post-incremented, all uses of
                   // next_line are reduced by 1 to account for the post increment
                     case "left" : 
                             choiceMade(2);
                             break;
                         
                     case "right": 
                             choiceMade(3);
                             break;
                         
                     case "go"   : 
                             choiceMade(1);
                             break;
                         
                     case "repeat":  
                             if ((repeat_num < 1) || (repeat_num > 999))
                             { // Checking if the repeat integer is too small or
                               // too large  
                                 System.out.println("ERROR: Repeat value not "
                                                   + "in valid range (1-999) "
                                                   + "on line " + (line_count + 1));
                                 return throw_error;
                             }
                         
                             for (int i = 0; i < repeat_num; i++)
                             {
                                 next_line = doScript((line_count + 1), 
                                                     (scope + 1), user_input);
                                 if (next_line > max_line_count)
                                 { // If an error was returned
                                     return throw_error;
                                 }
                                 
                             }
                             line_count = next_line - 1;
                             break; // End "Repeat" case
                         
                     case "if"   :
                             if(conditional.isEmpty())
                             { // Checking if the conditional is blank
                                 System.out.println ("ERROR: Expected condition"
                                                    + " after If on line " 
                                                    +  (line_count + 1));
                                 return throw_error;
                             }
                             
                             // Finding the accompanying Else statement
                             tempstr = "else";
                             for (int i = 0; i < scope; i++)
                             { // Forming tempstr based on our scope
                                 tempstr = "\t" + tempstr;
                             }
                             int else_line = line_count + 1;//Line the Else is on
                             while (! (user_input.get(else_line).matches(tempstr)))
                             { // While the next line isn't our Else
                                 else_line++;
                                 if (else_line >= max_line_count)
                                 { // If we can't find an accompanying Else
                                     System.out.println("ERROR: Accompanying "
                                                       + "Else statement not found for"
                                                       + " If statement on line "
                                                       + (line_count + 1));
                                     return throw_error;
                                 }
                             }
                             // End check for accompanying Else
                             
                             if (handleCondition(conditional))
                             { // Successful If case
                                 next_line = doScript((line_count + 1), 
                                                     (scope + 1), user_input);
                             }
                             
                             else
                             { // Successful Else case
                                 next_line = doScript((else_line + 1), 
                                                     (scope + 1), user_input);
                              }
                             
                             line_count = next_line - 1;
                             break;
                     
                     case "else" : // Only falls in this after a successful If   
                                   // This code is used to skip the unnecessary
                                   // Else and all statements within it
                             
                             tempstr = "\t";
                             do
                             { // As long as the line exceeds our scope
                                 line_count++;
                                 if (line_count >= max_line_count)
                                 { // If we've reached the end of the file
                                     return line_count;
                                 }
                              } while (user_input.get(line_count).startsWith(tempstr, scope));
                              
                             break; // End "If-Else" case
                         
                     case "while" :
                             int infinite_counter = 0;
                             
                             if(conditional.isEmpty())
                             { // Checking if the conditional is blank
                                 System.out.println ("ERROR: Expected condition"
                                                    + " on line " 
                                                    +  (line_count + 1));
                                 return throw_error;
                             }
                             int while_line = line_count;
                             while (handleCondition(conditional))
                             {
                                 infinite_counter++;
                                 next_line = doScript((while_line + 1), 
                                                      (scope + 1), user_input); 
                                if (infinite_counter > 100000)
                                 { // Assuming a loop that iterates over 100K
                                   // times is an infinite loop
                                     System.out.println("ERROR: Infinite loop "
                                                       + "detected in While"
                                                       + " on line " 
                                                       + (line_count + 1));
                                     return throw_error; 
                                 }
                                 
                                 if (next_line > max_line_count)
                                 { // If an error was returned in this loop
                                     return throw_error;
                                 }
                                 line_count = next_line - 1;
                             }
                             break; // End "While" case
                         
                     default: 
                             System.out.println("ERROR: Unrecognized syntax:");
                             System.out.println(current_line);
                             System.out.println("on line " + (line_count + 1));
                             return throw_error;
                 }
                 ++line_count;
             }
             return line_count;
         }
         
         public boolean handleCondition(String conditional)
         { // Function to check if a conditional is true or false
             char direction = player.GetDirection();
             int x = 0;
             int y = 0;
             switch(direction) // Getting the correct x and y values to use
             {
                     case '^':
                         x = 0;
                         y = -1;
                         break;
                     case 'v':
                         x = 0;
                         y = 1;
                         break;
                     case '>':
                         x = 1;
                         y = 0;
                         break;
                     case '<':
                         x = -1;
                         y = 0;
                         break;
             }
             
             int newX = x + player.GetX(); // Getting x of next space
             int newY = y + player.GetY(); // Getting y of next space
             x = player.GetX(); // Current space x
             y = player.GetY(); // Current space Y
             
             switch (conditional)
             {
                 case "not gem" :
                         if ( (player.isGemCollision(newX, newY, gems)) == -1)
                         { return true; }
                         
                         else
                         { return false; }
                         
                 case "gem"  :
                         if ( (player.isGemCollision(newX, newY, gems)) != -1)
                         { return true; }
                        
                         else
                         { return false; }
                         
                 case "not wall":
                         if (!player.isWallCollision(newX, newY, walls))
                         { return true; }
                         
                         else
                         { return false; }
                 case "wall" :
                         if (player.isWallCollision(newX, newY, walls))
                         { return true; }
                         
                         else
                         { return false; }
                 case "not home":
                         if (!player.isHomeCollision(newX, newY, home))
                         { return true; }
                         
                         else
                         { return false; }
                 case "home" :
                         if (player.isHomeCollision(newX, newY, home))
                         { return true; }
                         
                         else
                         { return false; }
             }
            
             return false; // Should never get here
         }
           
         
         
         public void choiceMade(int choice)
         {
             //Get karels current direction
             char direction = player.GetDirection();
             
             if (choice == 1) //Attempt to move the player
             {                
                 switch(direction)
                 {
                     case '^':
                         handleMove(0,-1);
                         break;
                     case 'v':
                         handleMove(0, 1);
                         break;
                     case '>':
                         handleMove(1,0);
                         break;
                     case '<':
                         handleMove(-1,0);
                         break;
                 }
                 
             }
             else if (choice == 2) //Turn the player left
             {                
                 switch(direction)
                 {
                     case '^':
                         player.SetDirection('<');
                         break;
                     case 'v':
                         player.SetDirection('>');
                         break;
                     case '>':
                         player.SetDirection('^');
                         break;
                     case '<':
                         player.SetDirection('v');
                         break;
                 }
             }
             else if (choice == 3)//turn the player right
             {                
                 switch(direction)
                 {
                     case '^':
                         player.SetDirection('>');
                         break;
                     case 'v':
                         player.SetDirection('<');
                         break;
                     case '>':
                         player.SetDirection('v');
                         break;
                     case '<':
                         player.SetDirection('^');
                         break;
                 }
             }
             else if (choice == 4) //Get multiple commands
             {
                 actions();
             }
             
             //update the map with new position or direction icon
             updateMap(player.GetX(),player.GetY(),player.GetDirection());
         }
         
         public void handleMove(int x, int y)
         {
             //Get where karel wants to move
             int newX = x + player.GetX();
             int newY = y + player.GetY();
             
             if (player.isWallCollision(newX, newY, walls))
             {
                 //collided with wall - do not move karel
             }
             else if (player.isHomeCollision(newX,newY,home))
             {
                 //if karel is home and all gems are taken, move and end game
                 if(gems.isEmpty())
                 {
                     player.move(x,y);
                     isRunning = false;
                     System.out.println("You have won!");
                 }
             }
             else if (player.isGemCollision(newX, newY, gems) != -1)
             {
                 //pick up the gem and move karel
                 gems.remove(player.isGemCollision(newX, newY, gems));
                 player.move(x, y);
             }
             else
             {
                 //move karel
                 player.move(x, y);
             }
         }
     }
 }
