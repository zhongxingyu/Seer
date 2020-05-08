 package studieprogresjon;
 
 import studieprogresjon.Student;
 import studieprogresjon.Lecturer;
 
 /**
  * Helper class to map entities to map coordinates
  *
  * @author Raymond Julin
  */
 class EntityMapper {
     private Entity[] entities;
     private int size = 0;
 
     
     /**
      * Constructor
      *
      * @param int size Max entities to register
      */
     public EntityMapper(int size) {
         this.size = size;
         entities = new Entity[size];
     }
     
     /**
      * Register entity
      *
      * @return boolean success
      * @param Entity entity
      */
     public boolean registerEntity(Entity entity) {
         int used = this.slotsUsed();
         if ((this.size - used) > 0) {
             entities[used] = entity;
             return true;
         }
         else {
             return false;
         }
     }
     
     /**
      * Return all lecturer entities
      *
      * @return Lecturer
      * @param boolean active Whether or not to just get active
      * lecturers
      */
     public Lecturer[] getLecturers(boolean active) {
         Lecturer[] ret = new Lecturer[this.size];
         int i = 0;
         if (active)
             this.calculateCollisions();
         for (Entity e: entities) {
             if (e instanceof Lecturer) {
                 if ((active && !e.isCollided()) || active == false) {
                     ret[i] = (Lecturer) e;
                     i++;
                 }
             }
         }
         Lecturer[] retur = new Lecturer[i];
         int x = 0;
         for (Lecturer l: ret) {
             if (l instanceof Lecturer) {
                 retur[x] = l;
                 x++;
             }
         }
         return retur;
     }
     public Lecturer[] getLecturers() {
         return this.getLecturers(false);
     }
 
     /**
      * Move lecturers towards student
      * @param Position pos To move against
      */
     public void moveLecturers(Position pos) {
         // Get active lecturers and move them
         Lecturer[] lects = this.getLecturers(true);
         for (Lecturer l: lects) {
             l = (Lecturer) l;
             l.chase(pos);
         }
     }
 
     /**
      * Return the Student object
      *
      * @return Student
      */
     public Student getStudent() {
         Student ret = new Student();
         for (Entity e: entities) {
             if (e instanceof studieprogresjon.Student) {
                 ret = (Student) e;
             }
         }
         return ret;
     }
 
     /**
      * Return symbol for given coordinates
      *
      * @return char
      * @param int x
      * @param int y
      */
     public char getSymbol(int x, int y) {
         Position pos = new Position(x,y);
         for (Entity e: entities) {
             // Need to be at this position
             if (e instanceof studieprogresjon.Entity) {
                 if (e.getPosition().equals(pos)) {
                     return e.getSymbol();
                 }
             }
         }
         return '.';
     }
     
     /**
      * Calculate number of free slots
      *
      * @return int
      */
     private int slotsUsed() {
         int i = 0;
         for (Entity e: entities) {
             if (e instanceof Entity)
                 i++;
         }
         return i;
     }
 
     /**
      * Check for collision against table of other Entities
      *
      * @return void
      */
     public void calculateCollisions() {
         for (Entity e: entities) {
             if (e instanceof studieprogresjon.Entity) {
                 e.checkCollision(entities);
             }
         }
     }
 }
