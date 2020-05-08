 package com.luugiathuy.apps.remotebluetooth;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import android.content.Context;
 
 public class Profile implements Serializable {
 	
 	/**
 	 * 
 	 */
 	
 	private static final long serialVersionUID = 1L;
 	private String mName;
 	public String[] mValues;
 	public String[] mString;
 	private int mSensitivity;
 	private int mDebug;
 	
 	//for perceptron. 
 	private double [][] mWeight;
 	private Queue<InputAndOutput> mPerceptronPoints ;
 	private LinkedList<String> mList=new LinkedList<String>();
 	public ArrayList<String> mSentence;
 	
 	public Profile(){
 		Initialization();
 		InitializeSentence();
 	}
 
 	public Profile(String name){
 		Initialization();
 		InitializeSentence();
 		mName=name;
 	}
 	
 	public void Initialization(){
 		mName="Root";
 		mSensitivity=3;
 		mDebug=0;
 		mWeight=new double[MessageController.NClASSES][MessageController.NDIMENSIONS];
 		//Initialize with 10...
 		mValues =Initial_String(MessageController.NClASSES-1);
 		mString=mValues.clone();
 		mPerceptronPoints = new LinkedList<InputAndOutput>();
 	}
 	
 	public static String[] Initial_String(int input){
 		String[] lstring=new String[input];
 		for(int i=0;i<input;i++){
 			lstring[i]="Gesture ";
 			lstring[i]=lstring[i].concat(String.valueOf(i+1));
 		}
 		return lstring;
 	}
 	
 	public void InitializeSentence(){
 		String [] array = new String[] {
 				"Hi", "Good Morning", "Good Afternoon", "Good Evening", "Good Night",
 				"Bye", "Its nice to meet you", "I am glad you are here",
 				"I hope you are doing great", "I am doing great",
 				"I hope you are having a great time here", "Fine , Thank you",
 				"Thanks a lot for coming", "I was looking forward to meeting you",
 				"Please have a seat", "Please make yourself comfortable",
 				"I hope the travel was not too tiring" };
 		mSentence=new ArrayList<String>(Arrays.asList(array));
 	}
 	
 	//getter and setter
 	public String  getName(){
 		return mName;
 	}
 	public String[]  getString(){
 		return mString;
 	}
 
 	public String[]  getValues(){
 		return mValues;
 	}
 	public int  getSensitivity(){
 		return mSensitivity;
 	}
 	
 	public int  getDebug(){
 		return mDebug;
 	}
 	public void  setDebug(int input){
 		 mDebug=input;
 	}
 	
 	
 	public double[][]  getWeight(){
 		return mWeight;
 	}
 	
 	public Queue<InputAndOutput>   getPerceptropoints(){
 		return mPerceptronPoints;
 	}
 	
 	public LinkedList<String>  getList(Context input){
 		regetList(input);
 		return mList;
 	}
 	
 	public ArrayList<String>  getSentence( ){
 		return mSentence;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void  setSentence(ArrayList <String> in){
 		 mSentence=(ArrayList<String>) in.clone();
 	}
 	
 	public  void SetSensitivity (int sensitivity){
 		 mSensitivity = sensitivity;
 	}
 	
 	public void  setName(String name){
 		 mName=name;
 	}
 	
 	public static void write_data_to_file(Context input,Profile pro)
 	{
 		String name =pro.getName();
 		try { 
 			name.concat("data");
 	        FileOutputStream fOut = input.openFileOutput(name, Context.MODE_WORLD_READABLE); 
 	        ObjectOutputStream osw = new ObjectOutputStream(fOut);
 	        pro.regetList(input);
 	        osw.writeObject(pro);
 	        osw.flush();
 	        osw.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public static Profile read_data_from_file(Context input, String name)
 	{
 		Profile pro= new Profile();
 		try { 
 			name.concat("data");
 			FileInputStream fin = input.openFileInput(name);
 	        ObjectInputStream ois = new ObjectInputStream(fin);
 			   pro = (Profile) ois.readObject();
 			   ois.close();
 		} catch ( Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		pro.regetList(input);
 			try{
 				pro.mSentence.clone();
 			}
 			catch(Exception e){
 				pro.InitializeSentence();
 				e.getStackTrace();
 			}
 		//
 		return pro;
 	}
 	@SuppressWarnings("unchecked")
 	public  void regetList(Context input){
 		mList.clear();
 		try{
 	        FileInputStream fin = input.openFileInput("Profile");
 	        ObjectInputStream ois = new ObjectInputStream(fin);
 			   mList = (LinkedList<String>) ois.readObject();
 			   ois.close();
 		}
 		catch ( Exception ioe) {
 	        ioe.printStackTrace();
 		}
 	}
 	
 	public Profile add_profile(Context input,String name)
 	{
 		Profile mProfileNew=new Profile(name);
 			try { 
 				mList.add(name);
 	            FileOutputStream fOut = input.openFileOutput("Profile", Context.MODE_PRIVATE); 
 	            ObjectOutputStream osw = new ObjectOutputStream(fOut);
 	            osw.writeObject(mList);
 	            osw.flush();
 	            osw.close();
 	            
 	            name.concat("data");
 	            FileOutputStream fOut1 = input.openFileOutput(name, Context.MODE_PRIVATE); 
 	            ObjectOutputStream osw1 = new ObjectOutputStream(fOut1);
 	            osw1.writeObject(mProfileNew);
 	            osw1.flush();
 	            osw1.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		mProfileNew.regetList(input);
 		return mProfileNew;
 	}
 	
 	//delete the item 
 	public void delet_profile(Context input,String name)
 	{
 		mList.remove(name);
 		try { 
             FileOutputStream fOut = input.openFileOutput("Profile", Context.MODE_PRIVATE); 
             ObjectOutputStream osw = new ObjectOutputStream(fOut);
             osw.writeObject(mList);
             osw.flush();
             osw.close(); 
             name.concat("data");
             input.deleteDatabase(name);
 	} catch (IOException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}finally{
 	}
 	}
 	
 	public static void clear(Profile pro){
 		pro.mSensitivity=3;
 		//reset. If Nclasses and Ndimension changes, here will be a problem.
 		String[] re =Initial_String(MessageController.NClASSES-1);
 		pro.mValues=re.clone();
 		pro.mString=re.clone();
 		
 		for (int i = 0; i <MessageController.NClASSES; i++)
         {
             for (int j = 0; j < MessageController.NDIMENSIONS; j++)
                 pro.mWeight[i][j] = 0;
         }
 		pro.mPerceptronPoints.clear();
 	}
 	
 	public static void reset_profile(Context input,String name,Profile pro)
 	{
 			clear(pro);
 			name.concat("data");
 			try { 
 	            FileOutputStream fOut = input.openFileOutput(name, Context.MODE_PRIVATE); 
 	            ObjectOutputStream osw = new ObjectOutputStream(fOut);
 	            osw.writeObject(pro);
 	            osw.flush();
 	            osw.close(); 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block 
 			e.printStackTrace();
 		}
 	}
 
 	public static void change_list_name(Context input,Profile pro,String name1, String name2)
 	{
 		pro.getList(input);
 		pro.mList.remove(name1);
 		pro.mList.add(name2);
 		try { 
             FileOutputStream fOut = input.openFileOutput("Profile", Context.MODE_PRIVATE); 
             ObjectOutputStream osw = new ObjectOutputStream(fOut);
             osw.writeObject(pro.mList);
             osw.flush();
             osw.close(); 
             name1.concat("data");
 	            input.deleteDatabase(name1);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 		}
 	}
 
 }
