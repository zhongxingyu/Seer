 
 package com.example.testels;
 
 
 /**
  * 项目名称：TestELS
  * 类名称：Shape
  * 类描述：
  * 创建人：xubt
  * 创建时间：2013-1-10 上午11:57:58
  * -------------------------------修订历史--------------------------
  * 修改人：xubt
  * 修改时间：2013-1-10 上午11:57:58
  * 修改备注：
  * @version：
  */
 public class Shape {
 	public final static int LEFT=1,RIGHT=2,DOWN=3,STATECHANGE=1,MOVE =2;
 	public static int[][] map=null;
 	
 	public static int[][][]shape=new int[][][]{
 		/**
 		 * 第一种类型
 		 */
 		
 		{	/**
 		 	*  ####	
 		 	*/
 			{1,1,1,1},{0,0,0,0},{0,0,0,0},{0,0,0,0},
 			
 			/**
 			 * 	#
 			 * 	#
 			 * 	#
 			 *  #
 			 */
 			{0,1,0,0},{0,1,0,0},{0,1,0,0},{0,1,0,0},
 			
 		},
 		/**
 		 * 第二种类型
 		 */
 		{	
 			/**
 			 *    #
 			 * 	 ### 
 			 */
 			{0,1,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0},
 			/**
 			 * 	#
 			 * 	##
 			 * 	#
 			 */
 			{1,0,0,0},{1,1,0,0},{1,0,0,0},{0,0,0,0},
 			/**
 			 * 	###
 			 * 	 #
 			 */
 			{1,1,1,0},{0,1,0,0},{0,0,0,0},{0,0,0,0},
 			/**
 			 * 	 #
 			 * 	##
 			 * 	 #
 			 */
 			{0,1,0,0},{1,1,0,0},{0,1,0,0},{0,0,0,0}
 		},
 		/**
 		 * 第三种类型
 		 */
 		{
 			/**
 			 * #
 			 * ###
 			 */
 			{1,0,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0},
 			
 			/**
 			 * ##
 			 * #
 			 * #
 			 */
 			{1,1,0,0},{1,0,0,0},{1,0,0,0},{0,0,0,0},
 			/**
 			 * ###
 			 * 	 #	
 			 */
 			{1,1,1,0},{0,0,1,0},{0,0,0,0},{0,0,0,0},
 			/**
 			 * 	#
 			 * 	#
 			 * ##
 			 * 
 			 */
 			{0,1,0,0},{0,1,0,0},{1,1,0,0},{0,0,0,0}
 		},
 		/**
 		 * 第四种类型
 		 * 
 		 */
 		{
 			/**
 			 *    #
 			 *  ###   
 			 */
 			{0,0,1,0},{1,1,1,0},{0,0,0,0},{0,0,0,0},
 			/**
 			 * #
 			 * #
 			 * ##
 			 */
 			{1,0,0,0},{1,0,0,0},{1,1,0,0},{0,0,0,0},
 			/**
 			 * ###
 			 * #
 			 */
 			{1,1,1,0},{1,0,0,0},{0,0,0,0},{0,0,0,0},
 			/**
 			 * ##
 			 *  #
 			 *  #
 			 */
 			{1,1,0,0},{0,1,0,0},{0,1,0,0},{0,0,0,0}
 		},
 		/**
 		 * 第五种类型
 		 */
 		{
 			/**
 			 *	#
 			 *  ##
 			 *   #
 			 */
 			{1,0,0,0},{1,1,0,0},{0,1,0,0},{0,0,0,0},
 			/**
 			 * 	  ##
 			 * 	 ##
 			 */
 			{0,1,1,0},{1,1,0,0},{0,0,0,0},{0,0,0,0}
 		},
 		/**
 		 * 第六种
 		 */
 		{
 			/**
 			 * 
 			 *   #
 			 *  ##
 			 *  #
 			 * 
 			 */
 			{0,1,0,0},{1,1,0,0},{1,0,0,0},{0,0,0,0},
 			
 			/**
 			 *  ##
 			 *   ##
 			 */
 			{1,1,0,0},{0,1,1,0,0},{0,0,0,0},{0,0,0,0}
 		},
 		/**
 		 * 第七种
 		 * 		 ##
 		 * 		 ##	
 		 */
 		{
 			{1,1,0,0},{1,1,0,0},{0,0,0,0},{0,0,0,0}
 		}
 	};
 	/**
 	 * 
 	 * @方法名：isBound
 	 * @功能描述：判断是否到达边界（到达则不可变化或移动）
 	 * @创建人：xubt
 	 * @创建时间：2013-1-11 上午10:13:32
 	 * @参数：@param type
 	 * @参数：@param state
 	 * @参数：@param cellLeft
 	 * @参数：@param cellTop
 	 * @参数：@param cellSize
 	 * @参数：@param direction
 	 * @参数：@param boundLeft
 	 * @参数：@param bountRight
 	 * @参数：@param boundTop
 	 * @参数：@param boundBottom
 	 * @参数：@param action
 	 * @参数：@return
 	 * @返回：boolean
 	 * @throws
 	 */
 	public static boolean isBound(int type, int state, int cellLeft,
 			int cellTop, int cellSize, int direction, int boundLeft,
 			int boundRight, int boundTop, int boundBottom, int action) {
 		
 		if(action==STATECHANGE){
 			state = stateChange(type, state);
 			//state为变形后的形状，检验该形状是否合法（未出界）
 			for (int j = state; j <state+4 ; j++) {
 				for (int k = 0; k < 4;k++) {
 					if(shape[type][j][k]==1){
 						int cell_top = cellTop+(j-state)*cellSize;
						int cell_left = cellLeft+(k-1)*cellSize;
 						
 						int mapX= (cell_left-boundLeft)/cellSize;
 						int mapY =(cell_top-boundTop)/cellSize;
 						if(mapX<0 || mapX>TetrisSufaceView.cols-1){ //左边或右边出界了
 							return false;
 						}
 						if(mapY>TetrisSufaceView.rows-1){  //底部出界了
 							return false;
 						}
 						if(Shape.map[mapY][mapX]==1){  //变形后的位置已经有方块了
 							return false;
 						}
 					}
 				}
 			}
 		}else if(action==MOVE){
 			for (int i = state; i < state+4; i++) {
 				for (int j = 0; j < 4; j++) {
 //					Log.e("TAG", "isBound-move   type:"+type+"    i:"+i+"      j:"+j);
 					if(Shape.shape[type][i][j]==1){
 						int cell_top = cellTop+(i-state)*cellSize;
 						int cell_left =cellLeft+j*cellSize;
 						
 						int mapX =(cell_left-boundLeft)/cellSize;
 						int mapY = (cell_top-boundTop)/cellSize;
 						
 						switch (direction) {
 						case LEFT:
 							if( mapX==0 ||Shape.map[mapY][mapX-1]==1){
 								return false;
 							}
 							break;
 						case RIGHT:
 							if(mapX==TetrisSufaceView.cols-1 || Shape.map[mapY][mapX+1]==1){
 								return false;
 							}
 							break;
 						case DOWN:
 							if(mapY==TetrisSufaceView.rows-1 || Shape.map[mapY+1][mapX]==1){
 								return false;
 							}
 							break;
 						default:
 							break;
 						}
 					}
 				}
 				
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @方法名：stateChange
 	 * @功能描述：变形
 	 * @创建人：xubt
 	 * @创建时间：2013-1-11 上午10:17:48
 	 * @参数：@param type
 	 * @参数：@param state
 	 * @参数：@return
 	 * @返回：int
 	 * @throws
 	 */
 	public static int stateChange(int type,int state){
 		int lenght = Shape.shape[type].length;
 		
 		return (state+4)%lenght;
 	}
 	
 	/**
 	 * 
 	 * @方法名：moveLeft
 	 * @功能描述：左移
 	 * @创建人：xubt
 	 * @创建时间：2013-1-11 上午10:19:54
 	 * @参数：@param cellLeft
 	 * @参数：@param cellSize
 	 * @参数：@return
 	 * @返回：int
 	 * @throws
 	 */
 	public static int moveLeft(int cellLeft,int cellSize){
 		cellLeft-=cellSize;
 		return cellLeft;
 	}
 	
 	/**
 	 * 
 	 * @方法名：moveRight
 	 * @功能描述：右移
 	 * @创建人：xubt
 	 * @创建时间：2013-1-11 上午10:21:10
 	 * @参数：@param cellLeft
 	 * @参数：@param cellSize
 	 * @参数：@return
 	 * @返回：int
 	 * @throws
 	 */
 	public static int moveRight(int cellLeft,int cellSize){
 		cellLeft+=cellSize;
 		return cellLeft;
 	}
 	
 	/**
 	 * 
 	 * @方法名：moveDown
 	 * @功能描述：下移
 	 * @创建人：xubt
 	 * @创建时间：2013-1-11 上午10:22:20
 	 * @参数：@param cellTop
 	 * @参数：@param cellSize
 	 * @参数：@return
 	 * @返回：int
 	 * @throws
 	 */
 	public static int moveDown(int cellTop,int cellSize){
 		cellTop+=cellSize;
 		return cellTop;
 	}
 }
