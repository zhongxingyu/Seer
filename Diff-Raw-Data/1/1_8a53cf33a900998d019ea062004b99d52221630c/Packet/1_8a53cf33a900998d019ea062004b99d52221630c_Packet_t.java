 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cs447.PuzzleFighter;
 
 import java.io.Serializable;
 
 /**
  *
  * @author ryantlk
  */
 public class Packet implements Serializable{
 	int garbage;
	boolean attacking;
 	SerializableGem grid[][];
 }
