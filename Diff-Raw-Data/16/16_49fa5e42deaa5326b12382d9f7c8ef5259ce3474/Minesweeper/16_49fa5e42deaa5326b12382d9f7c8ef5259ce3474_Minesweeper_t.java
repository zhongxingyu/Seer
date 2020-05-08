 package me.kukkii.minesweeper;
 
 public class Minesweeper{
 
   private int height = 1;
   private int width = 1;
   private int amountOfBomb = 1;
   private Matrix user;
   private Matrix admin;
 
   public Minesweeper(int height,int width,int amountOfBomb){
     this.height = height;
     this.width = width;
     this.amountOfBomb = amountOfBomb;
     
     user = new Matrix(height,width,'X');
     admin = new Matrix(height,width,'0');
   }
 
   public int getHeight(){
     return height;
   }
 
   public int getWidth(){
     return width;
   }
 
   public Matrix getUser(){
     return user;
   }
 
   public void setBombs(){
     for(int x=0; x<amountOfBomb; x++){
      while(true){
         int i = (int)(Math.random()*height);
         int j = (int)(Math.random()*width);
         if(admin.get(i,j) == '9'){
           continue;
         }
         else{
          admin.set(i,j,'9');     // 9 = bomb  
           break;
         }
       }
     }
   }
 
   public void checkAmountOfBombs(int i, int j){
     if(admin.get(i,j) == '9'){
       return;
     }
     int q = 0; // amount of bomb
     if(i>=1 && j>=1){  
       if(admin.get(i-1,j-1) == '9'){  // left up
         q = q + 1;
       }
     }
     if(j>=1){   // left
       if(admin.get(i,j-1) == '9'){  // left
         q = q + 1;
       } 
     }
     if(i<=height-2 && j>=1){
       if(admin.get(i+1,j-1) == '9'){  // left up
         q = q + 1;
       }
     }
     if(i>=1){
       if(admin.get(i-1,j) == '9'){  //  down
         q = q + 1;
       }
     }
     if(i<=height-2){
       if(admin.get(i+1,j) == '9'){  //  up
         q = q + 1;
       }
     }
     if(i>=1 && j<=width-2){
       if(admin.get(i-1,j+1) == '9'){   //  right down
         q = q + 1;
       }
     }
     if(j<=width-2){
       if(admin.get(i,j+1) == '9'){   //  right
         q = q + 1;
       }
     }
     if(i<=height-2 && j<=width-2){
       if(admin.get(i+1,j+1) == '9'){  // right up
         q = q + 1;
       }
     }
    admin.set(i,j,(char)('0' + q));
   }
 
   public void checkAll(){
     for(int i=0; i<height; i++){
       for(int j=0; j<width; j++){
         checkAmountOfBombs(i,j);
       }
     }
   }
 
   public int turn(int i, int j,int k){
     if(k==1){
       if(user.get(i,j)!='X'){
        System.out.println("cannot put a flag here");
         return 0;
       }
       user.set(i,j,'F');
       user.print();
       return 0;
     }
     if(admin.get(i,j)=='9'){
         System.out.println("game over");
         admin.print();
         return -1;
     }
     else{
       if(user.get(i,j)=='F'){
         user.set(i,j,'X');
       }
       this.open(i,j);
       user.print();
       return this.win();
     }
   }
 
   public int win(){
     int w = 0;
     for(int i=0; i<height; i++){
       for (int j=0; j<width; j++){
         if(user.get(i,j)!='F' && user.get(i,j)!='X'){
           w+=1;
           if(w==height*width-amountOfBomb){
             System.out.println("Congrats");
             return 1;
           }
         }
       }
     }
     return 0;
   }
 
   public void open(int i, int j){
     if(admin.get(i,j)=='9'){
       return;
     }
     if(user.get(i,j)!='X'){
       return;
     }
     user.set(i,j,admin.get(i,j));
    if(admin.get(i,j)!='0'){
       return;
     }
     if(i>=1 && j>=1){
       this.open(i-1,j-1);
     }
     if(j>=1){
       this.open(i,j-1);
     }
     if(i<=height-2 && j>=1){
       this.open(i+1,j-1);
     }
     if(i>=1){
       this.open(i-1,j);
     }
     if(i<=height-2){
       this.open(i+1,j);
     }
     if(i>=1 && j<=width-2){
       this.open(i-1,j+1);
     }
     if(j<=width-2){
       this.open(i,j+1);
     }
     if(i<=height-2 && j<=width-2){
       this.open(i+1,j+1);
     }
   }
 
 }
