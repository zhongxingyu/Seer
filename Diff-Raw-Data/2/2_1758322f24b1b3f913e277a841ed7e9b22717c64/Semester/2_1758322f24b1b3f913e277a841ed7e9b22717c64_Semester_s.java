 package studieprogresjon;
 
 import studieprogresjon.Student;
 import studieprogresjon.Lecturer;
 import studieprogresjon.EntityMapper;
 import java.util.Random;
 import java.util.Scanner;
 import studieprogresjon.Position;
 
 /**
  * Main class for game.
  * 
  * @author Raymond Julin
  */
 public class Semester {
     private int level,width,height;
     private EntityMapper mapper;
     private boolean won = false;
 
     public Semester(Student student, int level, int width, int height) {
         this.level = level;
         this.width = width;
         this.height = height;
         this.mapper = new EntityMapper(numberOfLecturers() + 1);
         this.mapper.registerEntity(student);
     }
 
     /**
      * Run game
      * @return void
      */
     public void runGame() {
         // Let user start game
         System.out.println("Starter semester " + this.level);
         System.out.println("Oppretter " + numberOfLecturers() + " forelesere");
         Student stud;
         this.registerLecturers();
         Position pos;
         while (true) {
             // Draw map
             System.out.println(getMap());
             System.out.println("Use keypad to navigate (1-9):");
             Direction dir = readInput();
             // Move player
             stud = this.mapper.getStudent();
             pos = stud.getPosition();
             pos.moveTowards(new Position(
                 pos.getX() + Direction.dx(dir),
                 pos.getY() + Direction.dy(dir)
             ));
             stud.setPosition(pos);
             // Check outside board
             if (this.outsideMap(pos)) {
                 this.won = false;
                 break;
             }
             // Move lecturers
             this.mapper.moveLecturers(pos);
             this.mapper.calculateCollisions();
             // If all lecturers are busy in meetings, hurray
             if (this.mapper.getLecturers(true).length == 0) {
                 this.won = true;
                 break;
             }
             // If meeting with lecturer, dead
             if (stud.getSymbol() == 'Z') {
                 this.won = false;
                 break;
             }
         }
     }
 
     /**
      * Test if game is won (true) or lost (false)
      */
     public boolean won() {
         return this.won;
     }
 
     /**
      * Check if Position is outside map
      * @param Position pos
      */
     private boolean outsideMap(Position pos) {
         if (pos.getX() >= this.width || pos.getX() < 0)
             return true;
         if (pos.getY() >= this.height || pos.getY() < 0)
             return true;
         return false;
     }
     
     /**
      * Read input
      * @return void
      */
     public Direction readInput() {
         Scanner sc = new Scanner(System.in);
         int i = 10;
         try {
             i = sc.nextInt();
         }
         catch (Exception e) {
             System.out.println("Illegal user input, quitting");
             System.exit(65);
         }
        if (i <= 9 && i > 0) {
             System.out.println("Illegal user input, quitting");
             System.exit(65);
         }
         return Direction.fromNum(i);
     }
 
     /**
      * Register lecturers for map
      */
     public void registerLecturers() {
         // Register lecturers
         for (int x = 0; x <= numberOfLecturers(); x++) {
             this.registerLecturer(new Lecturer(this.getFree()));
         }
     }
     
     /**
      * Decide number of lecturers to use for game level
      *
      * @return int
      */
     public int numberOfLecturers() {
         int num = (this.width * this.level) / 10;
         if (num <= 2)
             num = 2;
         return num;
     }
 
     /**
      * Get array over all lecturers
      *
      * @return Lecturers[]
      */
     public Lecturer[] getLecturers() {
         return this.mapper.getLecturers();
     }
     /**
      * Get Student
      * @return Student
      */
     public Student getStudent() {
         return this.mapper.getStudent();
     }
 
     /**
      * Register a new lecturer onto game
      *
      * @return void
      * @param Lecturer lect
      */
     public boolean registerLecturer(Lecturer lect) {
         return this.mapper.registerEntity(lect);
     }
 
     /**
      * Calculate number of registered lecturers in game
      *
      * @return int
      */
     public int numberOfRegisteredLecturers() {
         int i = 0;
         for (Lecturer l: this.mapper.getLecturers()) {
             if (l instanceof Lecturer)
                 i++;
         }
         return i;
     }
 
     /**
      * Check if coordinates are free
      *
      * @return boolean
      * @param int x
      * @param int y
      */
     public boolean isFree(int x, int y) {
         if (this.mapper.getSymbol(x,y) == '.')
             return true;
         else
             return false;
     }
     /**
      * Get free coordinates on map
      * @return Position pos
      */
     public Position getFree() {
         Random r = new Random();
         int x,y;
         while (true) {
             x = r.nextInt(width);
             y = r.nextInt(height);
             if (this.isFree(x,y))
                 break;
         }
         return new Position(x,y);
     }
     
     /**
      * Create game map as a string and return it
      *
      * @return String
      */
     public String getMap() {
         String row = "";
         String map = "";
         char symb = 'a';
         for (int i = 0; i < (this.width + 2); i++) {
             map += "-";
         }
         map += "\n";
         for (int y = 0; y < this.height; y++) {
             // Rows
             row = "|";
             for (int x = 0; x < this.width; x++) {
                 // TODO Check what should be placed here
                 symb = this.mapper.getSymbol(x,y);
                 row += symb;
             }
             row += "|";
             map += row + "\n";
         }
         for (int i = 0; i < (this.width + 2); i++) {
             map += "-";
         }
         return map;
     }
 }
