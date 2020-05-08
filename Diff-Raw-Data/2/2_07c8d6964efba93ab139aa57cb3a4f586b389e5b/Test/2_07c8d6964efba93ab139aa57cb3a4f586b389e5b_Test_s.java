 import java.lang.reflect.Method;
 import java.util.ArrayList;
 
 /*
  * Simple Java test Framework
  *
  *
  * Created by Michael Lyons <mdl0394@gmail.com>
  */
 
 
 public abstract class Test {
 
 	private ArrayList<ArrayList<String>> totalErrorMessages;
 	private int failCount; //How many tests have failed
 
 	private Boolean currentTest; //Whether the current test is passing or not, changed by asserts
 	private ArrayList<String> currentMessages; //current tests messages
 
 	public Test() {
 		totalErrorMessages = new ArrayList< ArrayList<String> >();
 		currentMessages = new ArrayList<String>();
 		failCount = 0;
 	}
 
 	public void assertEqual(Object object1, Object object2, String errorMessage) {
 		if( object1.equals(object2) ) {
 
 		} else {
 			this.currentTest = false;
 			String new_message = "\tassertEqual failed\n";
 			new_message += "\t\t" + object1.toString() + " != " + object2.toString() + "\n";
 			new_message += "\t\tUser Message: " + errorMessage;
 			addMessage(new_message);
 		}
 	}
 
 	public void assertTrue(Object object1, String errorMessage) {
 		if( object1 instanceof Boolean ) {
 			if( (Boolean)object1 ) {
 
 			} else {
 				this.currentTest = false;
 				String new_message = "\tassertTrue failed\n";
 				new_message += "\t\t" + object1.toString() + " != true\n";
 				new_message += "\t\tUser Message: " + errorMessage;
 				addMessage(new_message);
 			}
 		} else {
 			this.currentTest = false;
 		}
 	}
 
 	private void addMessage( String message ) {
 		this.currentMessages.add( message );
 	}
 	private void finalizeMessages(String methodName) {
 		if( !currentMessages.isEmpty() ) {
 			currentMessages.add(0, methodName);
 			this.totalErrorMessages.add(currentMessages);
 		}
 		this.currentMessages = new ArrayList<String>();
 	}
 
 	private void printMessages() {
 		for( ArrayList<String> errorMessages : this.totalErrorMessages ) {
 			System.out.println("-------------------------------------------");
 			for( String message : errorMessages ) {
 				System.out.println( message );
 			}
 		}
 	}
 
 	public boolean run() {
 		try {
 			Method m[] = this.getClass().getDeclaredMethods();
 			for( Method i : m ) {
 				if( i.getName().startsWith("test") ) {
 					currentTest = true;
 
 					Boolean thrown = false;
 					try {
 						i.invoke(this);
 					} catch( Throwable t ) {
 						thrown = true;
 						System.out.print("E");
						String new_message = "\t" + i.getName() + " threw and exception\n";
 						new_message += "\t\t" + t.toString() + "\n";
 						addMessage(new_message);
 						this.failCount++;
 					}
 
 					if( !this.currentTest ) {
 						System.out.print("F");
 						this.failCount++;
 					} else if(!thrown) {
 						System.out.print(".");
 					}
 					this.finalizeMessages(i.getName());
 				}
 			}
 			System.out.println("");
 			if( this.failCount > 0 ) {
 				printMessages();
 				System.out.println("");
 				System.out.println("[FAILED COUNT=" + this.failCount + "]");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return true;
 	}
 }
