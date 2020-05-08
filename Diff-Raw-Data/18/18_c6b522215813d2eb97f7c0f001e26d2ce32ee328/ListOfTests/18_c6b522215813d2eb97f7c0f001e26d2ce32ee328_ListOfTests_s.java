 package com.h.h.h;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.FileReader;
 import java.util.Random;
 import java.util.Scanner;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ListOfTests extends Activity {
 	
 	private MyTest []tests;
 	private int NumberOfTests = 3;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list_of_tests);
 		String filename = "example.txt";
 		/*     Bundle  +  ,      */
 		tests = new MyTest[NumberOfTests];
 		try {
 			GenereteListOfTests(filename);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	int generRandom(int n) {
 		return Math.abs((new Random()).nextInt(n));
 	}
 	
 	void GenereteListOfTests(String filename) throws IOException {
 		String path = Environment.getExternalStorageDirectory() + File.separator;
 		String MY_FILE = path + filename;
 		try {
 			Scanner scanner = new Scanner( new FileReader(MY_FILE));
 			int amount_of_tests = scanner.nextInt();
 			scanner.nextLine();
 			Boolean []used = new Boolean[amount_of_tests];
 			for(int i=0;i<amount_of_tests;i++)
 				used[i] = false;
 			int []WhatTests = new int[NumberOfTests]; //  ,     
 			int added = 0;
 			while(added < NumberOfTests) {
				int tmp = generRandom(amount_of_tests-added);
 				int it = 0;
 				while(tmp!=0) {
 					if( !used[it] )
 						tmp--;
 					it++;
 					if( it == amount_of_tests )
 						it = 0;
 				}
 				it = (it-1+amount_of_tests)%amount_of_tests;
 				used[it] = true;
 				WhatTests[added] = it;
 				added ++ ;
 			}
 			
 			for(int i=0;i<amount_of_tests;i++) {
 				scanner.nextLine();
				MyTest tmp = new MyTest();
				tmp.read(scanner);
 				for(int j=0;j<NumberOfTests;j++)
 					if(WhatTests[j]==i)
						tests[j] = tmp;
 			}
 			scanner.close();
 			// so, at this point we already have all needed tests in Array<MyTest> tests
 			
 			//HAAWA, input your code for displaying TEST.
 			
 			// for testing only
 			ShowData();
 		
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/* for testing only */
 	private void ShowData() {
 		TextView tv1 = (TextView) findViewById(R.id.TextRow1);
 		TextView tv2 = (TextView) findViewById(R.id.TextRow2);
 		TextView tv3 = (TextView) findViewById(R.id.TextRow3);
 		String STemp = tests[0].statement + "#" + Integer.toString(tests[0].correct_answer);
 		for(int i=0;i<5;i++)
 			STemp = STemp + "#" + tests[0].answers[i];
 		tv1.setText(STemp);
 		
 		STemp = tests[1].statement + "#" + Integer.toString(tests[1].correct_answer);
 		for(int i=0;i<5;i++)
 			STemp = STemp + "#" + tests[1].answers[i];
 		tv2.setText(STemp);
 		
 		STemp = tests[2].statement + "#" + Integer.toString(tests[2].correct_answer);
 		for(int i=0;i<5;i++)
 			STemp = STemp + "#" + tests[2].answers[i];
 		tv3.setText(STemp);
 		Toast.makeText(getApplicationContext(), "Table updated.", Toast.LENGTH_SHORT).show();
 	}
 }
