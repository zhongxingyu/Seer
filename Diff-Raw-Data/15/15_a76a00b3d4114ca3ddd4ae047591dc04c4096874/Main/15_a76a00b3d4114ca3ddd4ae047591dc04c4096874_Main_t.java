// Технологично училище Електронни системи - http://www.elsys-bg.org/
// клас : 11б
// номер : 27
// име : Тодор Руменов Маноилов
// задача : Да се разработи програма, която да симулира контейнер с даден размер и топки,
// които да могат да бъдат вкарвани и изкарвани от него. Да може да се изпразни и да се 
// изведе моментно състояние на контейнера.

 package com.fearsfx.homework11.code;
 
 public class Main {
 	
 	public static void main(String[] args) {
 
 		BallContainer box = new BallContainer();		// container without set size
 		BallContainer box1 = new BallContainer(2);		// container with set size
 		
 		Ball ball1 = new Ball();						// ball without set name 
 		Ball ball2 = new Ball("whut?!");				// ball with set name
 		
		System.out.println(ball1.GetName());			// print ball default name
		System.out.println(ball2.GetName());			// print ball set name
		
		ball1.SetName("Right");
		System.out.println(ball1.GetName());			// print ball changed name
						
 		System.out.println(box.size());					// auto sized
 		System.out.println(box1.size());				// set size
 		
 		System.out.println(box1.capacity());			// box start capacity
 		
 		box1.add(ball1);								// add a ball
 		System.out.println(box1.capacity());			// box capacity after adding
 		
 		box1.add(ball2);								// add another ball
 		System.out.println(box1.capacity());			// box capacity after adding the second one
 		
 		Ball ball3 = new Ball();						// one more ball
 		box1.add(ball3);								// add a ball over the set size
 		
 		box1.remove(ball1);								// remove a ball
 		box1.remove(ball3);								// take out ball that is not inside
 		
 		System.out.println(box1.capacity());			// box current capacity
 		box1.clear();									// take everything out from the box
 		System.out.println(box1.capacity());			// box capacity after the last action
 	}
 }
