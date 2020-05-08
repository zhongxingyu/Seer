 package com.blogspot.turtldev.centipede;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.os.Handler;
 import android.util.Log;
 
 import java.util.Random;
 import java.util.Vector;
 
 import static com.blogspot.turtldev.centipede.Direction.*;
 
 enum Direction {
     NORTH, SOUTH, EAST, WEST
 }
 
 public class Game {
     DrawingPanel panel;
 
     int _width, _height;
 
     int[] max_cells;
     Vector<Cell> body;
     Food current_food;
     Direction direction;
     boolean game_running;
     boolean game_success;
     Cell head;
     int speed;
     int cell_size;
     int growth_rate;
     int growth_remaining;
 
     int direction_time;
     Direction next_direction;
 
     Game(DrawingPanel panel, int width, int height){
         _width = width;
         _height = height;
 
         this.panel = panel;
 
         cell_size = 8;
        speed = 100;
         max_cells = new int[2];
             max_cells[0] = _width/cell_size;
             max_cells[1] = _height/cell_size;
         Log.v("init","w/h"+"("+_width+","+_height+")");
         Log.v("init",""+max_cells[0]+","+max_cells[1]);
         head = new Cell(_width/cell_size/2, _height/cell_size/2, cell_size);
         body = new Vector<Cell>();
             body.add(head);
         growth_rate = 4;
         growth_remaining = 0;
         direction_time = 0;
         switch(new Random().nextInt(4)) {
             case 0:
                 direction = NORTH;
                 next_direction = NORTH;
                 break;
             case 1:
                 direction = SOUTH;
                 next_direction = SOUTH;
                 break;
             case 2:
                 direction = EAST;
                 next_direction = EAST;
                 break;
             default:
                 direction = WEST;
                 next_direction = WEST;
         }
         get_next_food();
         game_success = false;
         game_running = true;
     }
 
     void get_next_food() {
         //TODO do check if position in body
         Random rnd = new Random();
         int first = rnd.nextInt(max_cells[0]);
         int second = rnd.nextInt(max_cells[1]);
         current_food = new Food(first, second, cell_size);
     }
 
     void init() {
         step();
     }
 
     void step() {
         //Move along the current direction from the head of the snake
         if( direction_time > 0 ) {
             direction_time--;
         }
         if( direction_time == 0 && next_direction != direction ) {
             direction = next_direction;
         }
         Cell next_position = new Cell(head, direction);
         if( next_position.x > max_cells[0]-1 || next_position.y > max_cells[1]-1 ||
                 next_position.x < 0 || next_position.y < 0) {
             game_over();
         }
         if( current_food.isAt(next_position) ) {
             growth_remaining += growth_rate;
             get_next_food();
         }
         if( growth_remaining < 1 ) {
             body.removeElementAt(body.size()-1); //Remove last segment
         }
         else {
             growth_remaining--;
         }
         for( int i = 0; i < body.size(); ++i ) {
             if( next_position.equal(body.elementAt(i)) ) {
                 game_over();
             }
         }
         body.add(0,next_position);
         head = body.elementAt(0);
 
         if( game_running ) {
            mHandler.postDelayed(mUpdateUITimerTask, speed);
         }
         panel.postInvalidate();
     }
 
     void change_direction(Direction direction) {
         //This puts the one line gap between turnabouts
         if(
                 ((this.direction == NORTH || this.direction == SOUTH) &&
                         (direction == EAST || direction == WEST))||
                         ((this.direction == EAST || this.direction == WEST) &&
                                 (direction == NORTH || direction == SOUTH))) {
             next_direction = direction;
             direction_time = 2;
         }
     }
 
     void draw(Canvas canvas) {
         canvas.drawColor(Color.BLACK);
 
         for( int i = 0; i < body.size(); ++i ) {
             body.elementAt(i).draw(canvas);
         }
         current_food.draw(canvas);
     }
 
     void game_over() {
         //TODO gameover call
         game_running = false;
     }
 
    void pause() {
        game_running = false;
        mHandler.removeCallbacks(mUpdateUITimerTask);
    }

    void resume() {
        game_running = true;
        mHandler.postDelayed(mUpdateUITimerTask, speed);
    }

     private final Runnable mUpdateUITimerTask = new Runnable() {
         public void run() {
             // do whatever you want to change here, like:
             step();
         }
     };
     private final Handler mHandler = new Handler();
 }
 
 abstract class Element {
     int x, y, body_size;
     public Element(int x, int y, int body_size){
         this.x = x;
         this.y = y;
         this.body_size = body_size;
     }
 
     abstract public void draw(Canvas canvas);
 }
 
 class Cell extends Element {
     Cell( int x, int y, int body_size) {
         super(x,y,body_size);
     }
     public Cell(Cell current, Direction direction) {
         super(0,0,current.body_size);
         switch(direction) {
             case NORTH:
                 this.x = current.x;
                 this.y = current.y-1;
                 break;
             case SOUTH:
                 this.x = current.x;
                 this.y = current.y+1;
                 break;
             case EAST:
                 this.x = current.x+1;
                 this.y = current.y;
                 break;
             case WEST:
                 this.x = current.x-1;
                 this.y = current.y;
                 break;
         }
     }
     public void draw(Canvas canvas) {
         if( x+body_size < 0 || x-body_size >= canvas.getWidth() ) {
             return;
         }
         if( y+body_size < 0 || y-body_size >= canvas.getHeight() ) {
             return;
         }
         //TODO Cell draw method
 
         Paint pnt = new Paint();
         pnt.setColor(Color.WHITE);
         canvas.drawRect(x*body_size, y*body_size, (x+1)*body_size, (y+1)*body_size, pnt);
     }
 
     boolean equal( Cell other ) {
         if( this.x == other.x && this.y == other.y ) {
             return true;
         }
         return false;
     }
     boolean equalFromCoord( int x, int y) {
         if( this.x == x && this.y == y ) {
             return true;
         }
         return false;
     }
 }
 
 class Food extends Element {
     Food( int x, int y, int body_size) {
         super(x,y,body_size);
     }
 
     boolean isAt(Cell cell) {
         if( this.x == cell.x && this.y == cell.y ) {
             return true;
         }
         else {
             return false;
         }
     }
 
     public void draw(Canvas canvas) {
         if( x+body_size < 0 || x-body_size >= canvas.getWidth() ) {
             return;
         }
         if( y+body_size < 0 || y-body_size >= canvas.getHeight() ) {
             return;
         }
         //TODO Food draw method
         Paint pnt = new Paint();
         pnt.setColor(Color.WHITE);
         canvas.drawCircle(x*body_size+body_size/2,y*body_size+body_size/2,body_size/2, pnt);
     }
 }
