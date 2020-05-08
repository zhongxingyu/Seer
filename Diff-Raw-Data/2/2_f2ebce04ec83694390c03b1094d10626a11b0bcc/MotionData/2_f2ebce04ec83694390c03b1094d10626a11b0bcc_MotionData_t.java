 package Tools.Data;
 
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.FileInputStream;
 import java.io.File;
 
 import java.util.ArrayList;
 
 import java.awt.Point;
 
 public class MotionData{
 	public static final int HEAD = 0;
 	public static final int NECK = 1;
 	public static final int L_SHOULDER = 2;
 	public static final int R_SHOULDER = 3;
 	public static final int L_ELBOW = 4;
 	public static final int R_ELBOW = 5;
 	public static final int L_HAND = 6;
 	public static final int R_HAND = 7;
 	public static final int TORSO = 8;
 	public static final int L_HIP = 9;
 	public static final int R_HIP = 10;
 	public static final int L_KNEE = 11;
 	public static final int R_KNEE = 12;
 	public static final int L_FOOT = 13;
 	public static final int R_FOOT = 14;
 
 	
 	private static final Point[] line = {
 		new Point(TORSO,NECK),
 		new Point(NECK,HEAD),
 		new Point(TORSO,R_SHOULDER),
 		new Point(R_SHOULDER,R_ELBOW),
 		new Point(R_ELBOW,R_HAND),
 		new Point(TORSO,L_SHOULDER),
 		new Point(L_SHOULDER,L_ELBOW),
 		new Point(L_ELBOW,L_HAND),
 		new Point(TORSO,R_HIP),
 		new Point(R_HIP,R_KNEE),
 		new Point(R_KNEE,R_FOOT),
 		new Point(TORSO,L_HIP),
 		new Point(L_HIP,L_KNEE),
 		new Point(L_KNEE,L_FOOT),
 		
 		new Point(L_HIP,R_HIP),
 		new Point(L_SHOULDER,R_SHOULDER)
 	};
 
 	public static final int JOINT_NUMBER = 15;
 
 	private ArrayList<Vec3D[]> data = new ArrayList<Vec3D[]>();
 
 	public MotionData(){}
 	
 	public Point[] getLine(){ return line.clone(); }
 	
 	public void add(Vec3D[] a){
 		data.add(a);
 	}
 	
 	public int size(){
 		return data.size();
 	}
 
 	public void normalization(){
 	if(size()==0) return;
 	Vec3D origin = get(0,TORSO);
 	for(int i = 0; i < size(); i++){
 		Vec3D[] after = get(i);
 		for(int j = 0; j < JOINT_NUMBER; j++){
 		after[j] = after[j].sub(origin);
 		}
 		data.set(i,after);
 	}
 	}
 	
 	public boolean readFile(BufferedReader in){
 		String str;
 		data.clear();
 		try{
 			while((str = in.readLine()) != null){
 				Vec3D[] newData = new Vec3D[JOINT_NUMBER];
 				for(int i = 0; i < JOINT_NUMBER; i++){
 					if(str == null && i > 0){
 						data.clear();
 						System.out.println("Illegal data format");
 						return false;
 					}
 					double[] d = new double[3];
 					String[] tmp = str.split(" ");
 					try{
 						for(int j = 0; j < 3; j++){
 						d[j] = Double.parseDouble(tmp[j]);
 						}
 					}catch(NumberFormatException ne){
 						data.clear();
 						System.out.println("Illiegal data format");
 						return false;
 					}
 					newData[i] = new Vec3D(d);
 					//					System.out.println("("+data.size()+","+i+") : "+newData[i]);
 					if(i < JOINT_NUMBER-1) str = in.readLine();
 				}
 				data.add(newData);
 			}
 			in.close();
 		}catch(IOException e){
 			return false;
 		}
 		normalization();
 		return true;
 	}
 	
 	public boolean readFile(String s){
 		boolean ret;
 		try{
 			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(s)));
 			ret = readFile(in);
 		}catch(IOException e){
 			System.out.println("Cannot find \""+s+"\"");
 			return false;
 		}
 		return ret;
 	}
 
 	public boolean readFile(File f){
 		boolean ret;
 		try{
 						BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
 			ret = readFile(in);
 		}catch(IOException e){
 				System.out.println("Cannot find \""+f.getName()+"\"");
 			return false;
 		}
 		return ret;
 	}
 	
 	public Vec3D[] get(int time){
 		if(time < 0 || time >= data.size()) return null;
 		return data.get(time);
 	}
 
 	public Vec3D get(int time, int position){
 		if(time < 0 || time >= data.size() || position < 0 || position >= JOINT_NUMBER) return null;
 		else return data.get(time)[position];
 	}
 	
 	public Vec3D[] convert(MotionData model, int i){
 		if(empty() || model.empty()){ return null; }
 		Vec3D diff = new Vec3D(get(0,TORSO).sub(model.get(0,TORSO)));
 		Vec3D[] vec = new Vec3D[JOINT_NUMBER];
		vec[TORSO]	  = model.get(i,TORSO).add(diff);
 		vec[NECK]	   = makeNext(model,vec,i, TORSO,	  NECK);
 		vec[HEAD]	   = makeNext(model,vec,i, NECK,	   HEAD);
 		vec[R_SHOULDER] = makeNext(model,vec,i, TORSO,	  R_SHOULDER);
 		vec[R_ELBOW]	= makeNext(model,vec,i, R_SHOULDER, R_ELBOW);
 		vec[R_HAND]	 = makeNext(model,vec,i, R_ELBOW,	R_HAND);
 		vec[L_SHOULDER] = makeNext(model,vec,i, TORSO,	  L_SHOULDER);
 		vec[L_ELBOW]	= makeNext(model,vec,i, L_SHOULDER, L_ELBOW);
 		vec[L_HAND]	 = makeNext(model,vec,i, L_ELBOW,	L_HAND);
 		vec[R_HIP]	  = makeNext(model,vec,i, TORSO,	  R_HIP);
 		vec[R_KNEE]	 = makeNext(model,vec,i, R_HIP,	  R_KNEE);
 		vec[R_FOOT]	 = makeNext(model,vec,i, R_KNEE,	 R_FOOT);
 		vec[L_HIP]	  = makeNext(model,vec,i, TORSO,	  L_HIP);
 		vec[L_KNEE]	 = makeNext(model,vec,i, L_HIP,	  L_KNEE);
 		vec[L_FOOT]	 = makeNext(model,vec,i, L_KNEE,	 L_FOOT);
 		return vec;
 	}
 	
 	public boolean empty(){
 		return data.size() == 0;
 	}
 
 	public MotionData convertAll(MotionData model){
 		MotionData ret = new MotionData();
 		int end = Math.min(model.size(),size());
 		for(int i = 0; i < end; i++){
 			ret.add(convert(model,i));
 		}
 		return ret;
 	}
 
 	private Vec3D makeNext(MotionData model, Vec3D[] ret, int t, int from, int to){
 		Vec3D unit = model.get(t,to).sub(model.get(t,from));
 		unit = unit.times(1.0/unit.abs());
 		return ret[from].add(unit.times((get(t,to).sub(get(t,from)).abs())));
 	}
 	
 	public String toString(){
 		String ret = "";
 		for(int i = 0; i < size(); i++){
 			for(int j = 0; j < JOINT_NUMBER; j++){
 				ret = ret + data.get(i)[j] + "\n";
 			}
 		}
 		return ret;
 	}
 
 }
