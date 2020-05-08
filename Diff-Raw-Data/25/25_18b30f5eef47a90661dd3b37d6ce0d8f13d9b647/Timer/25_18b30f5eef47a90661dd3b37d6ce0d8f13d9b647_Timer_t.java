 package com.ChrisSeto.BallDotJava
 
 //Timer.java Timer class for Explosions PowerUps etc
 //May or may not work depending on host device -Worked Fine on my PC but meant for android
 
 public class Timer
 {
	long time;
	float life;
 	public Timer(float time) //Should be in seconds
 	{
 		life = time * 1000; //Time -> millisecs
		time = System.currentTimeMillis();
 	}
 	public boolean checkDone()
 	{
		return (System.currentTimeMillis() >= time + life) ? true : false;
 	}
 
 }
