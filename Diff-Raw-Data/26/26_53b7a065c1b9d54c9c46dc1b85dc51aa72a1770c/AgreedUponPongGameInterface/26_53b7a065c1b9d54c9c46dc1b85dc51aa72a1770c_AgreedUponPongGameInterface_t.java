 package arpong.logic.game;
 
 public interface AgreedUponPongGameInterface {
     public void tick();
 
     // To be called by Recognition
     public void updatePlayerPaddleLocal(int paddleId, float x, float y);
     public void updateSteeringButton(int buttonId, boolean pressed);
 
     // To be called by Graphics
     public float getXMins();
     public float getXMaxs();
     public float getYMins();
     public float getYMaxs();
     public float getBallRadius();
    public float getPaddleXSize(int paddleId);
    public float getPaddleYSize(int paddleId);
 }
