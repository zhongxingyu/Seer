 
 package com.example.testels;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeSet;
 
 import android.media.SoundPool;
 import android.util.Log;
 
 /**
  * 项目名称：TestELS
  * 类名称：TetrisMoveRunnable
  * 类描述：
  * 创建人：xubt
  * 创建时间：2013-1-11 上午10:49:21
  * -------------------------------修订历史--------------------------
  * 修改人：xubt
  * 修改时间：2013-1-11 上午10:49:21
  * 修改备注：
  * @version：
  */
 public class TetrisMoveRunnable implements Runnable{
 	private TetrisSufaceView mTetrisSufaceView;
 	private boolean flag = true;
 	private boolean flagRun = true;
 	private int sleep = 500;
 //	private int[][][] shape = Shape.shape;
 	
 	
 	
 	public TetrisMoveRunnable(TetrisSufaceView mTetrisSufaceView) {
 		super();
 		this.mTetrisSufaceView = mTetrisSufaceView;
 	}
 
 
 
 
 	/** 
 	 * @方法名：run
 	 * @功能描述：
 	 * @参数：
 	 * @throws
 	 */
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		while (flagRun) {
 			while(flag){
 				try {
 					int type = mTetrisSufaceView.getType();
 					int state = mTetrisSufaceView.getState();
 					int cellTop = mTetrisSufaceView.getCellTop();
 					int cellLeft = mTetrisSufaceView.getCellLeft();
 					int cellSize = mTetrisSufaceView.getCellSize();
 					int direction = Shape.DOWN;
 					int boundLeft = mTetrisSufaceView.getBoundLeft();
 					int boundRight = mTetrisSufaceView.getBoundRight();
 					int boundTop = mTetrisSufaceView.getBoundTop();
 					int boundBottom = mTetrisSufaceView.getBoundBotttom();
 					SoundPool soundPool = mTetrisSufaceView.getSoundPool();
 					Map<String, Integer> soundPoolMap = mTetrisSufaceView.getSoundPoolMap();
 					float valume = mTetrisSufaceView.getVolume();
 					
 					boolean isBottom = Shape.isBound(type, state, cellLeft, cellTop, cellSize, direction, boundLeft, boundRight, boundTop, boundBottom, Shape.MOVE);
 					if(isBottom){//未到底，继续下落
 						cellTop = Shape.moveDown(cellTop, cellSize);
 						mTetrisSufaceView.setCellTop(cellTop);
 					}else{//不能下降了
 						Log.i("TAG", "TetrisMoveRunnable-----不能下降了");
 						
 						soundPool.play(soundPoolMap.get("down"), valume, valume, 0, 0, 1f);
 						Set<Integer> setX = insertOneShape(type, state, cellTop, cellLeft, cellSize, boundLeft, boundTop);
 						int score = mTetrisSufaceView.getScore();
 						int scorePlus = deleteAndScore(setX, soundPool, soundPoolMap);
 						int nextType = new Random().nextInt(7);
 						int nextState = new Random().nextInt(Shape.shape[nextType].length/4)*4;
 						
 						mTetrisSufaceView.setType(mTetrisSufaceView.getNextType());
 						mTetrisSufaceView.setState(mTetrisSufaceView.getNextState());
 						
 						mTetrisSufaceView.setNextType(nextType);
 						mTetrisSufaceView.setNextState(nextState);
 						Log.i("TAG", "newShape   type:"+mTetrisSufaceView.getType()+"   state:"+mTetrisSufaceView.getState()+"   nextType:"+mTetrisSufaceView.getNextType()+"    nextState:"+mTetrisSufaceView.getNextState());
 						mTetrisSufaceView.setScore(scorePlus+score);
 						mTetrisSufaceView.setCellTop(mTetrisSufaceView.getBoundTop());
						mTetrisSufaceView.setCellLeft((mTetrisSufaceView.getBoundRight()-mTetrisSufaceView.getBoundLeft())/2+mTetrisSufaceView.getBoundLeft()-mTetrisSufaceView.getCellSize());
 					}
 					Thread.sleep(500);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			
 		}
 	}
 	
 	/**
 	 * 
 	 * @方法名：insertOneShape
 	 * @功能描述：向二维数组插入数据
 	 * @创建人：xubt
 	 * @创建时间：2013-1-15 下午5:51:27
 	 * @参数：@param type
 	 * @参数：@param state
 	 * @参数：@param cellTop
 	 * @参数：@param cellLeft
 	 * @参数：@param cellSize
 	 * @参数：@param boundLeft
 	 * @参数：@param boundTop
 	 * @参数：@return
 	 * @返回：Set<Integer>
 	 * @throws
 	 */
 	private  Set<Integer> insertOneShape(int type,int state,int cellTop,int cellLeft,int cellSize,
 			int boundLeft,int boundTop){
 		Set<Integer> setX =new TreeSet<Integer>();
 		for (int i = state; i < state+4; i++) {
 			for (int j = 0; j < 4; j++) {
 				if(Shape.shape[type][i][j]==1){
 					int cell_top = cellTop+(i-state)*cellSize;
 					int cell_left = cellLeft+j*cellSize;
 					int mapX = (cell_top-boundTop)/cellSize;
 					int mapY = (cell_left - boundLeft)/cellSize;
 					Shape.map[mapX][mapY] = 1;
 					setX.add(mapX);
 				}
 			}
 		}
 		return setX; 
 	}
 	
 	/**
 	 * 消行得分
 	 * @方法名：deleteAndScore
 	 * @功能描述：
 	 * @创建人：xubt
 	 * @创建时间：2013-1-15 下午5:50:58
 	 * @参数：@param setX
 	 * @参数：@param soundPool
 	 * @参数：@param soundPoolMap
 	 * @参数：@return
 	 * @返回：int
 	 * @throws
 	 */
 	private int deleteAndScore(Set<Integer> setX,SoundPool soundPool,Map<String, Integer> soundPoolMap){
 		int scorePlus=0;
 		for (Iterator iterator = setX.iterator(); iterator.hasNext();) {
 			int mapX = (Integer) iterator.next();
 			int count = 0;
 			for (int i = 0; i < mTetrisSufaceView.cols; i++) {
 				if(Shape.map[mapX][i]==1){
 					count++;
 				}
 				
 			}
 			
 			if(count==mTetrisSufaceView.cols){
 				for (int i = mapX; i >0; i--) {
 					for (int j = 0; j < mTetrisSufaceView.cols; j++) {
 						Shape.map[i][j] =Shape.map[i-1][j];
 					}
 				}
 				float valume = mTetrisSufaceView.getVolume();
 				soundPool.play(soundPoolMap.get("getScore"), valume, valume, 0, 0, 1f);
 				scorePlus++;
 			}
 			
 		}
 		return scorePlus;
 	}
 
 
 
 
 	public boolean isFlag() {
 		return flag;
 	}
 
 
 	public void setFlag(boolean flag) {
 		this.flag = flag;
 	}
 
 
 	public boolean isFlagRun() {
 		return flagRun;
 	}
 
 
 	public void setFlagRun(boolean flagRun) {
 		this.flagRun = flagRun;
 	}
 
 	
 }
