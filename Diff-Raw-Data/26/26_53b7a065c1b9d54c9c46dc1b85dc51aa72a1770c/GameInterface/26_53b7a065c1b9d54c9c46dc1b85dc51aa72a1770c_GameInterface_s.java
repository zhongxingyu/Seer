 package ru.knk.JavaGL.Interfaces;
 
 public interface GameInterface {
     void tick();
     void updatePlayerPaddleLocal(int paddleId, float x, float y);
     float getXMins();
     float getXMaxs();
     float getYMins();
     float getYMaxs();
     float getBallRadius();
 }
 
