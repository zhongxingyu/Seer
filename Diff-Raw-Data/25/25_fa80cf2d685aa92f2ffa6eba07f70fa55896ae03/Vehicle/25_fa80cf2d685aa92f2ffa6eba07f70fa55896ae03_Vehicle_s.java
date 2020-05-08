 /*
  * Copyright(C) 2013 Yutaka Kato
  */
 package local.JPL.ch02.ex02_04;
 
 public class Vehicle {
 	
 	private double speed;
 	private int direction;
 	private String name;
	private int id;
 	private static int lastId;
 //	private static final int lastId = 0;
 	
 	/**
 	 * くるま。
 	 * 
 	 * @param name 車の名前
 	 * @throws NullPointerException 名前が欲しい
 	 * @throws IllegalArgumentException ちゃんと名前が欲しい
 	 */
 	public Vehicle(String name) {
 		if (name == null) throw new NullPointerException("Name is null.");
 		if (name.isEmpty()) throw new IllegalArgumentException("Name is empty.");
 		speed = 0.0;
 		direction = 0;
 		this.name = name;
 		id = ++lastId;	// final にすると変更できなくなる
 	}
 	
 	/**
 	 * 速度を取得します。
 	 * @return 速度
 	 */
 	public double getSpeed() {
 		return speed;
 	}
 	
 	/**
 	 * 方向を取得します。
 	 * @return 方向 (角度)
 	 */
 	public int getDirection() {
 		return direction;
 	}
 	
 	/**
 	 * 名前を取得します。
 	 * @return 名前
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * ID を取得します。
 	 * @return ID
 	 */
 	public int getId() {
 		return id;
 	}
 }
