 package com.example.paperandpens;
 
 import java.util.ArrayList;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 
 public class Player implements Parcelable {
 	private String na, jo;
 	private int str,mag,dex,con,hp,mp;
 	private ArrayList <Skills> ch_skill = new ArrayList<Skills>();
 	public Player(String name, String job, int str, int mag, int dex, int cons, int hp,int mp)
 	{
 		na = name;
 		jo = job;
 		this.str = str;
 		this.dex = dex;
 		this.con = cons;
 		this.hp = hp;
 		this.mp = mp;
 		
 	}
 	public Player(Parcel input)
 	{
 		readFromParcel(input); 
 	}
 	private void readFromParcel(Parcel input) {
 		// TODO Auto-generated method stub
 		na = input.readString();
 		jo = input.readString();
 		str = input.readInt();
 		mag = input.readInt();
 		dex = input.readInt();
 		con = input.readInt();
 		hp = input.readInt();
 		
 	}
 	
 	public void Armored()
 	{
 		
 	}
 	public String getJob()
 	{
 		return jo;
 		
 	}
 	public String getName()
 	{
 		return na;
 	
 	}
 	public int getStr()
 	{
 		return str;
 	}
 	public int getMag()
 	{
 		return mag;
 	}
 	public int getDex()
 	{
 		return dex;
 	}
 	public int getCon()
 	{
 		return con;
 	}
 	public int getHp()
 	{
 		return hp;
 	}
 	public int getMp()
 	{
 		return mp;
 	}
 	public void levelUp(int strength, int intell, int dexterity, int cons, int hitpt, int magpt)
 	{
 		str = str + strength;
 		mag = mag + intell;
 		dex = dex + dexterity;
 		con = con + cons;
 		hp = hp + hitpt;
 		mp = mp + magpt;
 		
 	}
 	@Override
 	public int describeContents() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	@Override
 	public void writeToParcel(Parcel out, int flag) {
 		String tag = null;
 		// TODO Auto-generated method stub
 		Log.v(tag, "writing parcel.."+ flag);
 		out.writeString(getName());
 		out.writeString(getJob());
 		out.writeInt(getStr());
 		out.writeInt(getMag());
 		out.writeInt(getDex());
 		out.writeInt(getCon());
 		out.writeInt(getHp());
 		out.writeInt(getMp());
 		
 	}
 
 }
