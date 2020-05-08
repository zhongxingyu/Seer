 package com.github.dreamrec;
 
 /**
  *
  */
 public class Model {
     private int xSize = 200; //data points per screen.
     public static final int DIVIDER = 10; //frequency divider for slow graphics
     private ListView<Integer> eyeDataList = new ListView<Integer>();   //list with raw incoming data of eye movements
     private double frequency; //frequency Hz of the incoming data (for fast graphics)
     private long startTime; //time when data recording was started
     private int fastGraphIndex; //index for the first point on a screen for fast graphics
     private int slowGraphIndex; //index for the first point on a screen for slow graphics
 
     public ListView<Integer> getEyeDataList() {
         return eyeDataList;
     }
 
     public void addEyeData(int data) {
         eyeDataList.add(data);
     }
 
     public double getFrequency() {
         return frequency;
     }
 
     public long getStartTime() {
         return startTime;
     }
 
     public int getFastGraphIndex() {
         return fastGraphIndex;
     }
 
     public int getSlowGraphIndex() {
         return slowGraphIndex;
     }
 
     public void setFrequency(double frequency) {
         this.frequency = frequency;
     }
 
     public void setStartTime(long startTime) {
         this.startTime = startTime;
     }
 
     public int getXSize() {
         return xSize;
     }
 
     public void setXSize(int xSize) {
         this.xSize = xSize;
     }
 
     public int getDataSize() {
         return eyeDataList.size();
     }
 
     public void clear() {
         eyeDataList.clear();
         frequency = 0;
         startTime = 0;
     }
 
     public int getCursorWidth() {
         return xSize / DIVIDER;
     }
 
     public int getCursorPosition() {
         return fastGraphIndex / DIVIDER - slowGraphIndex;
     }
 
     public void moveFastGraph(int newFastGraphIndex) {
         newFastGraphIndex = checkGraphIndexBounds(newFastGraphIndex, getDataSize());
         fastGraphIndex = newFastGraphIndex;
         checkCursorScreenBounds();
     }
 
     public void moveSlowGraph(int newSlowGraphIndex) {
         newSlowGraphIndex = checkGraphIndexBounds(newSlowGraphIndex, getDataSize() / DIVIDER);
         slowGraphIndex = newSlowGraphIndex;
     }
 
     //correct graph index if it points to invalid data. Should be > 0 and < (getDataSize - xSize)
     private int checkGraphIndexBounds(int newIndex, int dataSize) {
         int maxValue = getIndexMax(dataSize);
         newIndex = newIndex < 0 ? 0 : newIndex;
         newIndex = newIndex > maxValue ? maxValue : newIndex;
         return newIndex;
     }
 
     private int getIndexMax(int dataSize) {
         int maxValue = dataSize - xSize - 1;
         maxValue = maxValue < 0 ? 0 : maxValue;
         return maxValue;
     }
 
     public boolean isFastGraphIndexMaximum() {
         return fastGraphIndex == getIndexMax(getDataSize());
     }
 
     public void setFastGraphIndexMaximum() {
          moveFastGraph(getIndexMax(getDataSize()));
     }
 
     public void moveCursor(int newCursorPosition) {
         newCursorPosition = checkCursorIndexBounds(newCursorPosition, getDataSize() / DIVIDER);
         // move cursor to new position, even if this new position is out of the screen
         fastGraphIndex = (slowGraphIndex + newCursorPosition) * DIVIDER;
         checkCursorScreenBounds();
     }
 
     private void checkCursorScreenBounds() {
         //adjust slowGraphIndex to place cursor at the beginning of the screen
         if (getCursorPosition() < 0) {
            slowGraphIndex -= getCursorPosition();
         } else
             //adjust slowGraphIndex to place cursor at the end of the screen
             if (getCursorPosition() > xSize - getCursorWidth() - 1) {
                 slowGraphIndex += getCursorPosition() - xSize + getCursorWidth();
             }
     }
 
     //correct cursor positions if it points to invalid data index: < 0 and > getDataSize
     private int checkCursorIndexBounds(int newCursorPosition, int dataSize) {
         int minValue = -slowGraphIndex;
         int maxValue = dataSize - slowGraphIndex - getCursorWidth() - 1;
         maxValue = maxValue < minValue ? minValue : maxValue;
         newCursorPosition = newCursorPosition < minValue ? minValue : newCursorPosition;
         newCursorPosition = newCursorPosition > maxValue ? maxValue : newCursorPosition;
         return newCursorPosition;
     }
 }
 
 
 
