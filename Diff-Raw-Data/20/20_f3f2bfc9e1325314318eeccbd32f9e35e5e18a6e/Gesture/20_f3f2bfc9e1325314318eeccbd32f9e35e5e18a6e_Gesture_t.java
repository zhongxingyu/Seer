 package fr.umlv.escape.front;
 
 import android.graphics.Point;
 import android.util.Log;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.jbox2d.common.Vec2;
 
 import fr.umlv.escape.Objects;
 import fr.umlv.escape.world.EscapeWorld;
 
 /** Static class that allow to detect gestures and calculate forces that they represent
  */
 public class Gesture {
 	private final int minNumOfPoint;		// minimum point that it is necessary to detect a gesture
 	private final int marginErrorDiag;		// margin error for the diagonal gesture
 	private final int marginErrorBack;		// margin error for the back off gesture
 	private final int marginErrorCircle;	// margin error for the circle gesture
 	private GestureType lastDetected;
 	private Vec2 lastForce;
 	private List<Point> pointList;
 	private int width;
 	private int height;
 	
 	/**
 	 * Enum that represent a gesture.
 	 */
 	public enum GestureType{
 		/**No gesture detected.
 		 */
 		NOT_DETECTED,
 		/**Bad gesture detected.
 		 */
 		NOT_GESTURE,
 		/**Back off detected.
 		 */
 		BACK_OFF,
 		/**Left diagonal detected.
 		 */
 		LEFT_DIAG,
 		/**Right diagonal detected.
 		 */
 		RIGHT_DIAG,
 		/**Left circle detected.
 		 */
 		LEFT_CIRCLE,
 		/**Right circle detected.
 		 */
 		RIGHT_CIRCLE,
 		/**Horizontal left detected.
 		 */
 		HORIZONTAL_LEFT,
 		/**Horizontal right detected.
 		 */
 		HORIZONTAL_RIGHT,
 		/**Cheat code detected.
 		 */
 		CHEAT_CODE,
 		/**Stats detected.
 		 */
 		STATS
 	}
 	
 	/**
 	 * Constructor. Create a gesture with defaults parameters.
 	 */
 	public Gesture(int width, int height){
 		this(5,40,30,40,width,height);
 	}
 	
 	/**
 	 * Constructor.
 	 *
 	 * @param minNumOfPoint Minimum number of point needed to validate a gesture
 	 * @param marginErrorDiag Margin error for diagonals gestures
 	 * @param marginErrorBack Margin error for the back off gesture
 	 * @param marginErrorCircle Margin error for circles gestures
 	 */
 	public Gesture(int minNumOfPoint,int marginErrorDiag,int marginErrorBack,int marginErrorCircle, int width, int height){
 		if((minNumOfPoint<0)   ||
 		   (marginErrorDiag<0) ||
 		   (marginErrorBack<0) ||
 		   (marginErrorCircle<0)){
 			throw new IllegalArgumentException();
 		}
 		this.minNumOfPoint=minNumOfPoint;
 		this.marginErrorBack=marginErrorBack;
 		this.marginErrorDiag=marginErrorDiag;
 		this.marginErrorCircle=marginErrorCircle;
 		this.lastDetected=GestureType.NOT_DETECTED;
 		this.lastForce=new Vec2(0f,0f);
 		this.pointList=new ArrayList<Point>();
 		this.width = width;
 		this.height = height;
 	}
 	
 	/** Detect if it is a left circle gesture. It uses the firsts points to determinate the
 	 * direction and then use {@link #isCircle()}.
 	 * 
 	 * @param pointList the list of point used to detect the gesture.
 	 * @return True if the gesture is recognized else false.
 	 */
 	private boolean isLeftCircle(){
 		Iterator<Point> iterPoint = pointList.iterator();
 
 		if(!iterPoint.hasNext()){
 			return false;
 		}
 		int previousX=iterPoint.next().x;
 
 		for(int i=0;iterPoint.hasNext() && i<10;++i){
 			int tmp=iterPoint.next().x;
 
 			if(previousX<tmp){
 				return false;
 			}
 			previousX=tmp;
 		}
 		if(!iterPoint.hasNext()){
 			return false;
 		}
 		if(isCircle()){
 			lastDetected=Gesture.GestureType.LEFT_CIRCLE;
 			return true;
 		}
 		return false;
 	}
 
 	/** Detect if it is a left circle gesture. It uses the firsts points to determinate the
 	 * direction and then use {@link #isCircle()}.
 	 * 
 	 * @param pointList the list of point used to detect the gesture.
 	 * @return True if the gesture is recognized else false.
 	 */
 	private boolean isRightCircle(){
 		Iterator<Point> iterPoint = pointList.iterator();
 
 		if(!iterPoint.hasNext()){
 			return false;
 		}
 		int previousX=iterPoint.next().x;
 
 		for(int i=0;iterPoint.hasNext() && i<10;++i){
 			int tmp=iterPoint.next().x;
 
 			if(previousX>tmp){
 				return false;
 			}
 			previousX=tmp;
 		}
 		if(!iterPoint.hasNext()){
 			return false;
 		}
 		if(isCircle()){
 			lastDetected=Gesture.GestureType.RIGHT_CIRCLE;
 			return true;
 		}
 		return false;
 	}
 
 	/** Detect if a point list represent a circle.
 	 *  
 	 * @param pointList the list of point used to detect the gesture.
 	 * @return True if the gesture is recognized else false.
 	 */
 	private boolean isCircle(){
 		Iterator<Point> iterPoint = pointList.iterator();
 		if(!iterPoint.hasNext()){
 			return false;
 		}
 
 		Point tmp=iterPoint.next();
 		int radius;
 		int minX=tmp.x;
 		int maxX=minX;
 		int minY=tmp.x;
 		int maxY=minY;
 
 		//find the min and max of x and y
 		while(iterPoint.hasNext()){
 			tmp=iterPoint.next();
 			if(minX>tmp.x){
 				minX=tmp.x;
 			}
 			if(maxX<tmp.x){
 				maxX=tmp.x;
 			}
 			if(minY>tmp.y){
 				minY=tmp.y;
 			}
 			if(maxY<tmp.y){
 				maxY=tmp.y;
 			}
 		}
 		int diameterX=maxX-minX;
 		int diameterY=maxY-minY;
 		
 		if((diameterX)>200 ||
            (diameterX)<100 ||
            (diameterY)>200 ||
            (diameterY)<100){
 			return false;
 		}
 		
 		//calcul the radius of the circle
 		radius=((diameterX)+(diameterY))/4;
 		
 		//creating the center of the circle
 		Point center=new Point(maxX-((diameterX)/2),maxY-((diameterY)/2));
 		iterPoint=pointList.iterator();
 		int nbBadPoint=0;	//we allow 1/10 of the total point to be outside the circle 
 		while(iterPoint.hasNext()){
 			tmp=iterPoint.next();
 			/*Precalculing the left and right part of the circle equation
 			 *  (x-Cx)�+(y-Cy)�=R�
 			 */
 			double equaL=Math.pow((tmp.x-center.x),2)+
 				         Math.pow((tmp.y-center.y),2);
 			double equaR=radius*radius;
 			double margeErrorPerCent=equaR*marginErrorCircle/100;
 
 			if((equaL>(equaR+margeErrorPerCent)) || (equaL<(equaR-margeErrorPerCent))){
 				nbBadPoint++;
 				if(pointList.size()/10<nbBadPoint){
 					return false;	
 				}
 			}	
 		}
 		return true;		
 	}
 
 	private boolean isLeftDiagonal(){
 		Iterator<Point> iterPoint = pointList.iterator();
 		Point previous;
 
 		if(!iterPoint.hasNext()){
 			return false;
 		}
 		int numOfPoint=1;
 		previous=iterPoint.next();
 		
 		while(iterPoint.hasNext()){
 			Point tmp=iterPoint.next();
 			if((tmp.x>previous.x) || (tmp.y>previous.y)){
 				return false;
 			}
 			if((tmp.x-previous.x>tmp.y-previous.y+marginErrorDiag)){
 				return false;
 			}
 			previous=tmp;
 			numOfPoint++;
 		}
 		if(numOfPoint>minNumOfPoint){
 			return true;
 		}
 		return false;
 	}
 	
 	/** Detect if a point  list represent a Left diagonal with a coefficient director between
 	 * 0.5 and 2.
 	 *  
 	 * @param pointList the list of point used to detect the gesture.
 	 * @return True if the gesture is recognized else false.
 	 */
 	private boolean isLeftStrictDiagonal(){
 		if(!isLeftDiagonal()){
 			return false;
 		}
 		Point firstPoint=pointList.get(0);
 		Point lastPoint=pointList.get(pointList.size()-1);
 
 		Vec2 force=new Vec2(lastPoint.x-firstPoint.x,lastPoint.y-firstPoint.y);
 		if((force.y/force.x)>2 || (force.y/force.x)<0.5){
 			return false;
 		}
 		
 		lastDetected=Gesture.GestureType.LEFT_DIAG;
 		setLastForce(force.x,force.y);
 		return true;
 	}
 
 	/** Detect if a point list represent a Right diagonal with a coefficient director between
 	 * 0.5 and 2.
 	 *  
 	 * @param pointList the list of point used to detect the gesture.
 	 * @return True if the gesture is recognized else false.
 	 */
 	private boolean isRightDiagonal(){
 		Iterator<Point> iterPoint=pointList.iterator();
 		Point previous;
 
 		if(!iterPoint.hasNext()){
 			return false;
 		}
 		previous=iterPoint.next();
 		int numOfPoint=1;
 
 		while(iterPoint.hasNext()){
 			Point tmp=iterPoint.next();
 			if((tmp.x<previous.x) || (tmp.y>previous.y)){
 				return false;
 			}			
 			if((tmp.x-previous.x>previous.y-tmp.y+marginErrorDiag)){
 				return false;
 			}
 			previous=tmp;
 			numOfPoint++;
 		}
 		if(numOfPoint>minNumOfPoint){
 			return true;
 		}
 		return false;
 	}
 
 	private boolean isRightStrictDiagonal(){
 		if(!isRightDiagonal()){
 			return false;
 		}
 		Point firstPoint=pointList.get(0);
 		Point lastPoint=pointList.get(pointList.size()-1);
 
 		Vec2 force=new Vec2(lastPoint.x-firstPoint.x,lastPoint.y-firstPoint.y);
 		if((force.y/force.x)<-2 || (force.y/force.x)>-0.5){
 			return false;
 		}
 		
 		lastDetected=GestureType.RIGHT_DIAG;
 		setLastForce(force.x,force.y);
 		return true;
 	}
 	
 	/** Detect if a point list represent a back off.
 	 *  
 	 * @param pointList the list of point used to detect the gesture.
 	 * @return True if the gesture is recognized else false.
 	 */
 	private boolean isBackOff(){
 		Iterator<Point> iterPoint=pointList.iterator();
 		Point firstPoint;
 		Point previous;
 		
 		if(!iterPoint.hasNext()){
 			return false;
 		}
 		previous=iterPoint.next();
 		firstPoint=previous;
 		int numOfPoint=1;
 		Point tmp;
 		while(iterPoint.hasNext()){
 			tmp=iterPoint.next();

			if(previous.y > tmp.y 				        	||
			   tmp.x<(firstPoint.x-(marginErrorBack))	||
			   tmp.x>(firstPoint.x+marginErrorBack)){
 				return false;
 			}
 			previous=tmp;
 			numOfPoint++;
 		}
 		if(numOfPoint>minNumOfPoint){
 			lastDetected=GestureType.BACK_OFF;
 			setLastForce(0f,(float)previous.y-firstPoint.y);
 			return true;
 		}
 		return false;
 	}
 	
 	/** Detect if a point list represent a horizontal line left or right.
 	 *  
 	 * @param pointList the list of point used to detect the gesture.
 	 * @return True if the gesture is recognized else false.
 	 */
 	private boolean isHorizontalLine(){
 		Iterator<Point> iterPoint=pointList.iterator();
 		Point firstPoint;
 		Point previous;
 
 		if(!iterPoint.hasNext()){
 			return false;
 		}
 		previous=iterPoint.next();
 		firstPoint=previous;
 		int numOfPoint=1;
 		Point tmp;
 		while(iterPoint.hasNext()){
 			tmp=iterPoint.next();
 			if(tmp.y<(firstPoint.y-marginErrorBack)		||
 			   tmp.y>(firstPoint.y+marginErrorBack)){
 				return false;
 			}
 			previous=tmp;
 			numOfPoint++;
 		}
 		if(numOfPoint>minNumOfPoint){
 			float forceX=(float)previous.x-firstPoint.x;
 			if(forceX>0){
 				this.lastDetected=GestureType.HORIZONTAL_RIGHT;
 			}
 			else{
 				this.lastDetected=GestureType.HORIZONTAL_LEFT;
 			}
 			setLastForce(forceX,0f);
 			return true;
 		}
 		return false;
 	}
 	
 	/** Detect if a point list represent a cheat code.
 	 *  
 	 * @param pointList the list of point used to detect the gesture.
 	 * @return True if the gesture is recognized else false.
 	 */
 	private boolean isCheatCode(){
 		Iterator<Point> iterPoint=pointList.iterator();
 		
 		Point previous;
 		int stateOfDetection=0;
 		
 		previous=iterPoint.next();
 		Point firstPoint=previous;
 		
 		if((firstPoint.x<width-150) || (firstPoint.y<height-150)){
 			return false;
 		}
 		int numOfPoint=1;
 		int badPoint=0;
 		Point tmp;
 		while(iterPoint.hasNext()){
 			tmp=iterPoint.next();
 			switch (stateOfDetection){
 			case 0: // detect straight line to the left
 				if(numOfPoint<5){
 					if(tmp.y>firstPoint.y + marginErrorBack){
 						return false;
 					}
 				}
 				if((tmp.x>previous.x)||(tmp.y<firstPoint.y - marginErrorBack)){
 					badPoint++;
 					if(badPoint>10){
 						return false;
 					}
 				}
 				if(tmp.y>firstPoint.y + marginErrorBack){
 					stateOfDetection++;
 					firstPoint=previous;
 					numOfPoint=0;
 				}
 				break;
 			case 1:  // detect back off
 				if(numOfPoint<5){
 					if(tmp.x>(firstPoint.x+marginErrorBack)){
 						return false;
 					}
 				}
 				if(tmp.y<previous.y || tmp.x<(firstPoint.x-marginErrorBack)){
 					badPoint++;
 					if(badPoint>10){
 						return false;
 					}
 				}
 				if(tmp.x>(firstPoint.x+marginErrorBack)){
 					stateOfDetection++;
 					numOfPoint=0;
 					firstPoint=previous;
 				}
 				break;
 			case 2:  // detect straight line to the right
 				if((tmp.x<previous.x)||(tmp.y<firstPoint.y - marginErrorBack)||(tmp.y>firstPoint.y + marginErrorBack)){
 					badPoint++;
 					if(badPoint>10){
 						return false;
 					}
 				}
 				break;
 			default:
 				return false;
 			}
 			previous=tmp;
 			numOfPoint++;
 		}
 		if(stateOfDetection==2 && numOfPoint>5){
 			lastDetected=GestureType.CHEAT_CODE;
 			setLastForce(0f,0f);
 			return true;
 		}
 		return false;
 	}
 	
 	/** Detect if a point list represent a stat gesture.
 	 *  
 	 * @param pointList the list of point used to detect the gesture.
 	 * @return True if the gesture is recognized else false.
 	 */
 	private boolean isStats(){
 		Iterator<Point> iterPoint=pointList.iterator();	
 		Point previous;
 		int stateOfDetection=0;
 		
 		previous=iterPoint.next();
 		Point firsPoint=previous;
 		int numOfPoint=1;
 		int badPoint=0;
 		Point tmp;
 	
 		while(iterPoint.hasNext()){
 			tmp=iterPoint.next();
 			switch (stateOfDetection){
 			case 0:  // detect straight line to the right
 				if(numOfPoint<5){
 					if(tmp.y>firsPoint.y + marginErrorBack){
 						return false;
 					}
 				}
 				if((tmp.x<previous.x)||(tmp.y<firsPoint.y - marginErrorBack)){
 					badPoint++;
 					if(badPoint>10){
 						return false;
 					}
 				}
 				if(tmp.y>firsPoint.y + marginErrorBack){
 					stateOfDetection++;
 					firsPoint=previous;
 					numOfPoint=0;
 				}
 				break;
 			case 1:  // detect back off
 				if(numOfPoint<5){
 					if(tmp.x<(firsPoint.x-marginErrorBack)){
 						return false;
 					}
 				}
 				if(tmp.y<previous.y || tmp.x>(firsPoint.x+marginErrorBack)){
 					badPoint++;
 					if(badPoint>10){
 						return false;
 					}
 				}
 				if(tmp.x<(firsPoint.x-marginErrorBack)){
 					stateOfDetection++;
 					numOfPoint=0;
 					firsPoint=previous;
 				}
 				break;
 			case 2:  // detect straight line to the left
 				if((tmp.x>previous.x)||(tmp.y<firsPoint.y - marginErrorBack)||(tmp.y>firsPoint.y + marginErrorBack)){
 					badPoint++;
 					if(badPoint>10){
 						return false;
 					}
 				}
 				break;
 			default:
 				return false;
 			}
 			previous=tmp;
 			numOfPoint++;
 		}
 		if(stateOfDetection==2 && numOfPoint>5){
 			lastDetected=GestureType.STATS;
 			setLastForce(0f,0f);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Return the last gesture detected.
 	 * @return The last gesture detected.
 	 */
 	public GestureType getLastGestureDetected(){
 		GestureType gesture = lastDetected;
 		return gesture;
 	}
 	
 	/**
 	 * Return the last force calculated.
 	 * @return The last force calculated.
 	 */
 	public Vec2 getLastForce(){
 		return lastForce;
 	}
 	
 	/**
 	 * Try to detect a gesture. You can get the gesture detected by calling {@link #getLastGestureDetected()}.
 	 * @return true a gesture has been detected else false.
 	 */
 	public boolean detectGesture(){
 		if(isLeftCircle()	  		||
 		   isRightCircle() 			||
 		   isRightStrictDiagonal()  ||
 		   isLeftStrictDiagonal()	||
 		   isBackOff()				||
 		   isHorizontalLine()		||
 		   isCheatCode()			||
 		   isStats()
 		   ){
 			return true;
 		}
 		this.lastDetected=Gesture.GestureType.NOT_GESTURE;
 		return false;
 	}
 	
 	private void setLastForce(float x, float y){
 		lastForce.set(x/EscapeWorld.SCALE,y/EscapeWorld.SCALE);
 	}
 	
 	/**
 	 * Try to calculate a force given a gesture detected. You can get the gesture detected by calling {@link #getLastForce()}.
 	 * @return true a force has been calculated else false.
 	 */
 	public boolean calculForceFromLine(){
 		if(isRightDiagonal() || isLeftDiagonal()){
 			Point firstPoint=pointList.get(0);
 			Point lastPoint=pointList.get(pointList.size()-1);
 
 			Vec2 force=new Vec2(lastPoint.x-firstPoint.x,lastPoint.y-firstPoint.y);
 			setLastForce(force.x,force.y);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Add a point to the gesture detector. When the method {@link #detectGesture()} will be called
 	 * it will uses all the point added to detect a gesture.
 	 * @param point
 	 * @return true if the point could have been added else false.
 	 */
 	public boolean addPoint(Point point){
 		Objects.requireNonNull(point);
 		
 		return this.pointList.add(point);
 	}
 	
 	/**
 	 * Get the list of point added to the gesture detector.
 	 * @return The list of point added to the gesture detector.
 	 */
 	public List<Point> getListPoint(){
 		return this.pointList;
 	}
 	
 	/**
 	 * Clear the gesture detector to an empty state with no point added and nothing detected.
 	 */
 	public void clear(){
 		lastForce.set(0f,0f);
 		this.lastDetected=Gesture.GestureType.NOT_DETECTED;
 		this.pointList.clear();
 	}
 }
