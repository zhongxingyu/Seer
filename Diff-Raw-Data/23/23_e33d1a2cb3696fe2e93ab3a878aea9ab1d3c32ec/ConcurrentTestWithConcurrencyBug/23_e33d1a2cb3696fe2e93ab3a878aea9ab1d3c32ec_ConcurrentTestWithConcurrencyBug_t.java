 /*
  * This file is covered by the terms of the Common Public License v1.0.
  *
  * Copyright (c) SZEDER Gábor
  *
  * Parts of this software were developed within the JEOPARD research
  * project, which received funding from the European Union's Seventh
  * Framework Programme under grant agreement No. 216682.
  */
 
 package de.fzi.cjunit.integration.testclasses;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
import java.util.ArrayList;
import java.util.List;

 import de.fzi.cjunit.ConcurrentTest;
 
 
 public class ConcurrentTestWithConcurrencyBug {
 
 	@ConcurrentTest
 	public void concurrentTestMethod() throws InterruptedException {
		final List<Integer> list = new ArrayList<Integer>();
 		Thread t = new Thread() {
 			@Override
 			public void run() {
				list.add(1);
 			}
 		};
 		t.start();
		list.add(2);
 		t.join();
		assertThat(list.size(), equalTo(2));
 	}
 }
