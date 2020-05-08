 package training.generics;
 
 import org.junit.Test;
 
 public class Boxes {
 
 	class Dog {}
 	class Puppy extends Dog {}
 
 	Dog dog = new Dog();
 	Puppy puppy = new Puppy();
 
 	Box<Dog> dogBox = new Box<Dog>(dog);
 	Box<Puppy> puppyBox = new Box<Puppy>(puppy);
 
 	class Box<E> {
 		private E element;
 
 		public Box(E element) {
 			this.element = element;
 		}
 
 		public E getElement() {
 			return element;
 		}
 
 		public void setElement(E element) {
 			this.element = element;
 		}
 	}
 
 	void putDogInBox(Box<Dog> dogBox) {
 		dogBox.setElement(new Dog());
 	}
 
 	Dog getDogFromBox(Box<? extends Dog> dogBox) {
 		return dogBox.getElement();
 	}
 
 	void putPuppyInBox(Box<? super Puppy> puppyBox) {
 		puppyBox.setElement(new Puppy());
 	}
 
 	Puppy getPuppyFromBox(Box<Puppy> puppyBox) {
 		return puppyBox.getElement();
 	}
 
 	@Test
 	public void dogsAndBoxes() {
 		putDogInBox(dogBox);
 		getDogFromBox(dogBox);
 		getDogFromBox(puppyBox); // OOOOH
 
 		putPuppyInBox(puppyBox);
 		putPuppyInBox(dogBox); // AAAAH
 		getPuppyFromBox(puppyBox);
 	}
 }
