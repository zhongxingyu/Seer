 package jp.skypencil.pmd.slf4j.example;
 
 public class WithoutLogger {
 	public void method() {
 		System.out.println("Hello, world!");
 	}

	public static void main(String[] args) {
		new WithoutLogger().method();
	}
 }
