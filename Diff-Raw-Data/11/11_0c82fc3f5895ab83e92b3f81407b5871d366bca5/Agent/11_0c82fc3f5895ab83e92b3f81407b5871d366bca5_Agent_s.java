 package planteatingagent;
 
 import java.net.Socket;
 import java.io.*;
 
 public class Agent {
 
     public static final int IMAGE_SIZE = 6;
     Socket sock;
     InputStream is;
     OutputStream os;
     DataInputStream dis;
     DataOutputStream dos;
     byte[] work = new byte[4];
 
     public Agent(String host, int port) throws Exception {
         sock = new Socket(host, port);
         is = sock.getInputStream();
         os = sock.getOutputStream();
 
         dis = new DataInputStream(is);
         dos = new DataOutputStream(os);
     }
 
     private boolean write_and_read_bool(Command command) throws Exception {
         return write_and_read_int(command) == 1 ? true : false;
     }
 
     private void write_int(Command command) throws Exception {
         int v = command.getValue();
         work[ 0] = (byte) v;
         work[ 1] = (byte) (v >> 8);
         work[ 2] = (byte) (v >> 16);
         work[ 3] = (byte) (v >> 24);
         dos.write(work, 0, 4);
     }
 
     private int read_int() throws Exception {
         dis.readFully(work, 0, 4);
         return (work[ 3]) << 24 | (work[ 2] & 0xff) << 16 | (work[ 1] & 0xff) << 8 | (work[ 0] & 0xff);
     }
 
     private int write_and_read_int(Command command) throws Exception {
         write_int(command);
         return read_int();
     }
 
     public void moveUp() throws Exception {
        write_and_read_bool(Command.MOVE_UP);
     }
 
     public void moveLeft() throws Exception {
        write_and_read_bool(Command.MOVE_LEFT);
     }
 
     public void moveDown() throws Exception {
        write_and_read_bool(Command.MOVE_DOWN);
     }
 
     public void moveRight() throws Exception {
        write_and_read_bool(Command.MOVE_RIGHT);
     }
 
     public PlantEatingResult eatPlant() throws Exception {
         int result = write_and_read_int(Command.EAT_PLANT);
         switch (result) {
             case 0:
                 return PlantEatingResult.NO_PLANT_TO_EAT;
             case 1:
                 return PlantEatingResult.PLANT_ALREADY_EATEN;
             case 2:
                 return PlantEatingResult.EAT_NUTRITIOUS_PLANT;
             case 3:
                 return PlantEatingResult.EAT_POISONOUS_PLANT;
             default:
                 throw new IllegalArgumentException();
         }
     }
 
     public PlantType getPlantType() throws Exception {
         int result = write_and_read_int(Command.GET_PLANT_TYPE);
         switch (result) {
             case 0:
                 return PlantType.NO_PLANT;
             case 1:
                 return PlantType.UNKNOWN_SQUARE;
             case 2:
                 return PlantType.UNKNOWN_PLANT;
             case 3:
                 return PlantType.NUTRITIOUS_PLANT;
             case 4:
                 return PlantType.POISONOUS_PLANT;
             default:
                 throw new IllegalArgumentException();
         }
     }
 
     public int[][] getPlantImage() throws Exception {
         int[][] image = new int[IMAGE_SIZE][IMAGE_SIZE];
 
         write_int(Command.GET_PLANT_IMAGE);
         for (int row = 0; row < IMAGE_SIZE; row++) {
             for (int col = 0; col < IMAGE_SIZE; col++) {
                 if ((image[row][col] = read_int()) < 0) {
                     throw new IllegalArgumentException();
                 }
             }
         }
 
         return image;
     }
 
     public int getLife() throws Exception {
         return write_and_read_int(Command.GET_LIFE);
     }
 
     public int getX() throws Exception {
         return write_and_read_int(Command.GET_X);
     }
 
     public int getY() throws Exception {
         return write_and_read_int(Command.GET_Y);
     }
 
     public int getRound() throws Exception {
         return write_and_read_int(Command.GET_ROUND);
     }
 
     public int getStartingLife() throws Exception {
         return write_and_read_int(Command.GET_STARTING_LIFE);
     }
 
     public int getPlantBonus() throws Exception {
         return write_and_read_int(Command.GET_PLANT_BONUS);
     }
 
     public int getPlantPenalty() throws Exception {
         return write_and_read_int(Command.GET_PLANT_PENALTY);
     }
 
     public boolean isAlive() throws Exception {
         return write_and_read_bool(Command.ALIVE);
     }
 
     public void endGame() throws Exception {
         write_int(Command.END_GAME);
     }
 }
