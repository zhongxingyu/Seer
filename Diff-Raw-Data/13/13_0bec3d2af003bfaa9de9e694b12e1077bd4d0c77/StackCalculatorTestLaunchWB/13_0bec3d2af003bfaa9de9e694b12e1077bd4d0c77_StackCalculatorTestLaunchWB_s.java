 package com.LookHin.StackCalculator.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.util.Log;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.LookHin.StackCalculator.R;
 import com.LookHin.StackCalculator.StackCalculator;
 import com.jayway.android.robotium.solo.Solo;
 
 public class StackCalculatorTestLaunchWB extends
 		ActivityInstrumentationTestCase2<StackCalculator> {
 
 	private static final String TAG_APP = "---StackCalc-00---";
 	private Solo solo;
 
 	public StackCalculatorTestLaunchWB() {
 		super("com.LookHin.StackCalculator", StackCalculator.class);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		// solo.finishOpenedActivities();
 	}
 
 	// public void testAssertingProperActivity() {
 	// solo.assertCurrentActivity("Check proper activity",
 	// StackCalculator.class);
 	// }
 
 	/**
 	 * public Button button_0 = (Button) solo.getView(R.id.ButtonNumber00);
 	 * Button button_1 = (Button) solo.getView(R.id.ButtonNumber01); public
 	 * Button button_2 = (Button) solo.getView(R.id.ButtonNumber02); public
 	 * Button button_3 = (Button) solo.getView(R.id.ButtonNumber03); public
 	 * Button button_4 = (Button) solo.getView(R.id.ButtonNumber04); public
 	 * Button button_5 = (Button) solo.getView(R.id.ButtonNumber05); public
 	 * Button button_6 = (Button) solo.getView(R.id.ButtonNumber06); public
 	 * Button button_7 = (Button) solo.getView(R.id.ButtonNumber07); public
 	 * Button button_8 = (Button) solo.getView(R.id.ButtonNumber08); public
 	 * Button button_9 = (Button) solo.getView(R.id.ButtonNumber09);
 	 * 
 	 * public Button button_delete = (Button)
 	 * solo.getView(R.id.ButtonSymbolDel); public Button button_clear = (Button)
 	 * solo.getView(R.id.ButtonSymbolClear);
 	 * 
 	 * public Button button_dot = (Button) solo.getView(R.id.ButtonSymbolDot);
 	 * public Button button_bracket_open = (Button) solo
 	 * .getView(R.id.ButtonSymbolBracketLeft); public Button
 	 * button_bracket_close = (Button) solo
 	 * .getView(R.id.ButtonSymbolBracketRight);
 	 * 
 	 * Button button_add = (Button) solo.getView(R.id.ButtonSymbolAdd); public
 	 * Button button_sub = (Button) solo.getView(R.id.ButtonSymbolSubtract);
 	 * public Button button_mul = (Button)
 	 * solo.getView(R.id.ButtonSymbolMultiply); public Button button_div =
 	 * (Button) solo.getView(R.id.ButtonSymbolDivide);
 	 * 
 	 * Button button_equal = (Button) solo.getView(R.id.ButtonSymbolExe);
 	 */
 
 	public void testAddTwoNumbersWBox() {
 
 		Log.d(TAG_APP, "Running testAddTwoNumbersWBox()...");
 
 		Button button_1 = (Button) solo.getView(R.id.ButtonNumber01);
 		Button button_add = (Button) solo.getView(R.id.ButtonSymbolAdd);
 		Button button_equal = (Button) solo.getView(R.id.ButtonSymbolExe);
 
 		EditText resultET = (EditText) solo.getView(R.id.EditText02);
 
 		solo.clickOnView(button_1);
 		solo.clickOnView(button_add);
 		solo.clickOnView(button_1);
 
 		solo.clickOnView(button_equal);
 
 		// assertEquals("2.0", resultET.getText().toString());
 
 		if (resultET.getText().toString().equals("2.0")) {
 			Log.d(TAG_APP, "Running of testAddTwoNumbersWBox() was successfull");
 		} else {
 			Log.d(TAG_APP, "Running of testAddTwoNumbersWBox() was FAILED!..");
 			Log.d(TAG_APP, "Result is: " + resultET.getText().toString());
 			fail();
 		}
 	}
 
 	public void testSubstractTwoNumbersWBox() {
 
 		Log.d(TAG_APP, "Running testSubstractTwoNumbersWBox()...");
 
 		Button button_1 = (Button) solo.getView(R.id.ButtonNumber01);
 		Button button_sub = (Button) solo.getView(R.id.ButtonSymbolSubtract);
 		Button button_equal = (Button) solo.getView(R.id.ButtonSymbolExe);
 
 		solo.clickOnView(button_1);
 		solo.clickOnView(button_sub);
 		solo.clickOnView(button_1);
 
 		solo.clickOnView(button_equal);
 
 		EditText resultET = (EditText) solo.getView(R.id.EditText02);
 
 		// assertEquals("0.0", resultET.getText().toString());
 
 		if (resultET.getText().toString().equals("0.0")) {
 			Log.d(TAG_APP,
 					"Running of testSubstractTwoNumbersWBox() was successfull");
 		} else {
 			Log.d(TAG_APP,
 					"Running of testSubstractTwoNumbersWBox() was FAILED!..");
 			Log.d(TAG_APP, "Result is: " + resultET.getText().toString());
 			fail();
 		}
 
 	}
 
 	public void testMultiplyTwoNumbersWBox() {
 
 		Log.d(TAG_APP, "Running testMultiplyTwoNumbersWBox()...");
 
 		Button button_1 = (Button) solo.getView(R.id.ButtonNumber01);
 		Button button_mul = (Button) solo.getView(R.id.ButtonSymbolMultiply);
 		Button button_equal = (Button) solo.getView(R.id.ButtonSymbolExe);
 
 		solo.clickOnView(button_1);
 		solo.clickOnView(button_mul);
 		solo.clickOnView(button_1);
 
 		solo.clickOnView(button_equal);
 
 		EditText resultET = (EditText) solo.getView(R.id.EditText02);
 
 		// assertEquals("1.0", resultET.getText().toString());
 
 		// solo.sleep(500);
 		//
 		if (resultET.getText().toString().equals("1.0")) {
 			Log.d(TAG_APP,
 					"Running of testMultiplyTwoNumbersWBox() was successfull");
 		} else {
 			Log.d(TAG_APP,
 					"Running of testMultiplyTwoNumbersWBox() was FAILED!..");
 			Log.d(TAG_APP, "Result is: " + resultET.getText().toString());
 			fail();
 		}
 
 	}
 
 	public void testDivideTwoNumbersWBox() {
 
 		Log.d(TAG_APP, "Running testDivideTwoNumbersWBox()...");
 
 		Button button_1 = (Button) solo.getView(R.id.ButtonNumber01);
 		Button button_div = (Button) solo.getView(R.id.ButtonSymbolDivide);
 		Button button_equal = (Button) solo.getView(R.id.ButtonSymbolExe);
 
 		solo.clickOnView(button_1);
 		solo.clickOnView(button_div);
 		solo.clickOnView(button_1);
 
 		solo.clickOnView(button_equal);
 
 		EditText resultET = (EditText) solo.getView(R.id.EditText02);
 
 		// assertEquals("1.0", resultET.getText().toString());
 
 		solo.sleep(500);
 
 		if (resultET.getText().toString().equals("1.0")) {
 			Log.d(TAG_APP,
 					"Running of testDivideTwoNumbersWBox() was successfull");
 		} else {
 			Log.d(TAG_APP,
 					"Running of testDivideTwoNumbersWBox() was FAILED!..");
 			Log.d(TAG_APP, "Result is: " + resultET.getText().toString());
 			fail();
 		}
 	}
 
 	public void testDigitsInput() {
 
 		Log.d(TAG_APP, "Running testDigitsInput()...");
 
 		Button button_0 = (Button) solo.getView(R.id.ButtonNumber00);
 		Button button_1 = (Button) solo.getView(R.id.ButtonNumber01);
 		Button button_2 = (Button) solo.getView(R.id.ButtonNumber02);
 		Button button_3 = (Button) solo.getView(R.id.ButtonNumber03);
 		Button button_4 = (Button) solo.getView(R.id.ButtonNumber04);
 		Button button_5 = (Button) solo.getView(R.id.ButtonNumber05);
 		Button button_6 = (Button) solo.getView(R.id.ButtonNumber06);
 		Button button_7 = (Button) solo.getView(R.id.ButtonNumber07);
 		Button button_8 = (Button) solo.getView(R.id.ButtonNumber08);
 		Button button_9 = (Button) solo.getView(R.id.ButtonNumber09);
 
 		solo.clickOnView(button_0);
 		solo.clickOnView(button_1);
 		solo.clickOnView(button_2);
 		solo.clickOnView(button_3);
 		solo.clickOnView(button_4);
 		solo.clickOnView(button_5);
 		solo.clickOnView(button_6);
 		solo.clickOnView(button_7);
 		solo.clickOnView(button_8);
 		solo.clickOnView(button_9);
 
 		EditText inputET = (EditText) solo.getView(R.id.EditText01);
 
 		if (inputET.getText().toString().equals("0123456789")) {
 			Log.d(TAG_APP, "Running of testDigitsInput() was successfull");
 		} else {
 			Log.d(TAG_APP, "Running of testDigitsInput() was FAILED!..");
 			Log.d(TAG_APP, "Result is: " + inputET.getText().toString());
 			fail();
 		}
 	}
 
 	public void testSymbolsInput() {
 
 		Log.d(TAG_APP, "Running testSymbolsInput()...");
 
 		Button button_dot = (Button) solo.getView(R.id.ButtonSymbolDot);
 		Button button_bracket_open = (Button) solo
 				.getView(R.id.ButtonSymbolBracketLeft);
 		Button button_bracket_close = (Button) solo
 				.getView(R.id.ButtonSymbolBracketRight);
 
 		Button button_add = (Button) solo.getView(R.id.ButtonSymbolAdd);
 		Button button_sub = (Button) solo.getView(R.id.ButtonSymbolSubtract);
 		Button button_mul = (Button) solo.getView(R.id.ButtonSymbolMultiply);
 		Button button_div = (Button) solo.getView(R.id.ButtonSymbolDivide);
 
 		solo.clickOnView(button_div);
 		solo.clickOnView(button_mul);
 		solo.clickOnView(button_sub);
 		solo.clickOnView(button_add);
 
 		solo.clickOnView(button_bracket_close);
 		solo.clickOnView(button_bracket_open);
 
 		solo.clickOnView(button_dot);
 
 		EditText inputET = (EditText) solo.getView(R.id.EditText01);
 
 		if (inputET.getText().toString().equals("/*-+)(.")) {
 			Log.d(TAG_APP, "Running of testSymbolsInput() was successfull");
 		} else {
 			Log.d(TAG_APP, "Running of testSymbolsInput() was FAILED!..");
 			Log.d(TAG_APP, "Result is: " + inputET.getText().toString());
 			fail();
 		}
 	}
 
 	public void testDotTwiceInput() {
 
 		Log.d(TAG_APP, "Running testDotTwiceInput()...");
 
 		Button button_dot = (Button) solo.getView(R.id.ButtonSymbolDot);
 
 		Button button_1 = (Button) solo.getView(R.id.ButtonNumber01);
 		Button button_2 = (Button) solo.getView(R.id.ButtonNumber02);
 
 		solo.clickOnView(button_1);
 		solo.clickOnView(button_dot);
 		solo.clickOnView(button_1);
 		solo.clickOnView(button_dot);
 		solo.clickOnView(button_2);
 		solo.clickOnView(button_dot);
 		solo.clickOnView(button_2);
 		solo.clickOnView(button_dot);
 
 		EditText inputET = (EditText) solo.getView(R.id.EditText01);
 
 		if (inputET.getText().toString().equals("1.122")) {
 			Log.d(TAG_APP, "Running of testDotTwiceInput() was successfull");
 		} else {
 			Log.d(TAG_APP, "Running of testDotTwiceInput() was FAILED!..");
 			Log.d(TAG_APP, "Result is: " + inputET.getText().toString());
 			fail();
 		}
 
 	}
 
 	public void testDeleteButton() {
 
 		Log.d(TAG_APP, "Running testDeleteButton()...");
 
 		Button button_0 = (Button) solo.getView(R.id.ButtonNumber00);
 		Button button_1 = (Button) solo.getView(R.id.ButtonNumber01);
 		Button button_2 = (Button) solo.getView(R.id.ButtonNumber02);
 		Button button_3 = (Button) solo.getView(R.id.ButtonNumber03);
 		Button button_4 = (Button) solo.getView(R.id.ButtonNumber04);
 		Button button_5 = (Button) solo.getView(R.id.ButtonNumber05);
 		Button button_6 = (Button) solo.getView(R.id.ButtonNumber06);
 		Button button_7 = (Button) solo.getView(R.id.ButtonNumber07);
 		Button button_8 = (Button) solo.getView(R.id.ButtonNumber08);
 		Button button_9 = (Button) solo.getView(R.id.ButtonNumber09);
 
 		Button button_del = (Button) solo.getView(R.id.ButtonSymbolDel);
 
 		EditText inputET = (EditText) solo.getView(R.id.EditText01);
 
 		solo.clickOnView(button_0);
 		solo.clickOnView(button_1);
 		solo.clickOnView(button_2);
 		solo.clickOnView(button_3);
 		solo.clickOnView(button_4);
 		solo.clickOnView(button_5);
 		solo.clickOnView(button_6);
 		solo.clickOnView(button_7);
 		solo.clickOnView(button_8);
 		solo.clickOnView(button_9);
 
 		assertEquals(inputET.getText().toString(), "0123456789");
 
 		solo.clickOnView(button_del);
 		assertEquals(inputET.getText().toString(), "012345678");
 
 		solo.clickOnView(button_del);
 		assertEquals(inputET.getText().toString(), "01234567");
 
 		if (inputET.getText().toString().equals("01234567")) {
 			Log.d(TAG_APP, "Running of testDeleteButton() was successfull");
 		} else {
 			Log.d(TAG_APP, "Running of testDeleteButton() was FAILED!..");
 			Log.d(TAG_APP, "Result is: " + inputET.getText().toString());
 			fail();
 		}
 	}
 
 	public void testClearButton() {
 		Log.d(TAG_APP, "Running testClearButton()...");
 
 		Button button_0 = (Button) solo.getView(R.id.ButtonNumber00);
 		Button button_1 = (Button) solo.getView(R.id.ButtonNumber01);
 		Button button_2 = (Button) solo.getView(R.id.ButtonNumber02);
 		Button button_3 = (Button) solo.getView(R.id.ButtonNumber03);
 		Button button_4 = (Button) solo.getView(R.id.ButtonNumber04);
 		Button button_5 = (Button) solo.getView(R.id.ButtonNumber05);
 		Button button_6 = (Button) solo.getView(R.id.ButtonNumber06);
 		Button button_7 = (Button) solo.getView(R.id.ButtonNumber07);
 		Button button_8 = (Button) solo.getView(R.id.ButtonNumber08);
 		Button button_9 = (Button) solo.getView(R.id.ButtonNumber09);
 
 		Button button_clear = (Button) solo.getView(R.id.ButtonSymbolClear);
 
 		EditText inputET = (EditText) solo.getView(R.id.EditText01);
 
 		solo.clickOnView(button_0);
 		solo.clickOnView(button_1);
 		solo.clickOnView(button_2);
 		assertEquals(inputET.getText().toString(), "012");
 
 		solo.clickOnView(button_clear);
 		assertEquals(inputET.getText().toString(), "");
 
 		solo.clickOnView(button_3);
 		solo.clickOnView(button_4);
 		solo.clickOnView(button_5);
 		solo.clickOnView(button_6);
 		assertEquals(inputET.getText().toString(), "3456");
 
 		solo.clickOnView(button_clear);
 		assertEquals(inputET.getText().toString(), "");
 
 		solo.clickOnView(button_7);
 		solo.clickOnView(button_8);
 		solo.clickOnView(button_9);
 		assertEquals(inputET.getText().toString(), "789");
 
 		solo.clickOnView(button_clear);
 		assertEquals(inputET.getText().toString(), "");
 
 		if (inputET.getText().toString().equals("")) {
 			Log.d(TAG_APP, "Running of testClearButton() was successfull");
 		} else {
 			Log.d(TAG_APP, "Running of testClearButton() was FAILED!..");
 			Log.d(TAG_APP, "Result is: " + inputET.getText().toString());
 			fail();
 		}
 	}
 
 	public void testNoInputPressEqualButton() {
 
 		Log.d(TAG_APP, "Running testNoInputPressEqualButton()...");
 
 		Button button_equal = (Button) solo.getView(R.id.ButtonSymbolExe);
 		EditText inputET = (EditText) solo.getView(R.id.EditText02);
 
 		solo.clickOnView(button_equal);
 
 		if (inputET.getText().toString().equals("0.0")) {
 			Log.d(TAG_APP,
 					"Running of testNoInputPressEqualButton() was successfull");
 		} else {
 			Log.d(TAG_APP,
 					"Running of testNoInputPressEqualButton() was FAILED!..");
 			Log.d(TAG_APP, "Result is: " + inputET.getText().toString());
 			fail();
 		}
 
 	}
 
 	public void testDivideDigitByZero() {
 
 		Log.d(TAG_APP, "Running testDivideDigitByZero()...");
 
 		Button button_0 = (Button) solo.getView(R.id.ButtonNumber00);
 		Button button_5 = (Button) solo.getView(R.id.ButtonNumber05);
 		Button button_equal = (Button) solo.getView(R.id.ButtonSymbolExe);
 		Button button_div = (Button) solo.getView(R.id.ButtonSymbolDivide);
 
 		EditText inputET = (EditText) solo.getView(R.id.EditText02);
 
 		solo.clickOnView(button_5);
 		solo.clickOnView(button_div);
 		solo.clickOnView(button_0);
 		solo.clickOnView(button_equal);
 
 		if (inputET.getText().toString().equals("Infinity")) {
 			Log.d(TAG_APP, "Running of testDivideDigitByZero() was successfull");
 		} else {
 			Log.d(TAG_APP, "Running of testDivideDigitByZero() was FAILED!..");
 			Log.d(TAG_APP, "Result is: " + inputET.getText().toString());
 			fail();
 		}
 	}
 
 	public void testDivideZeorByZero() {
 
 		Log.d(TAG_APP, "Running testDivideZeorByZero()...");
 
 		Button button_0 = (Button) solo.getView(R.id.ButtonNumber00);
 		Button button_equal = (Button) solo.getView(R.id.ButtonSymbolExe);
 		Button button_div = (Button) solo.getView(R.id.ButtonSymbolDivide);
 
 		EditText inputET = (EditText) solo.getView(R.id.EditText02);
 
 		solo.clickOnView(button_0);
 		solo.clickOnView(button_div);
 		solo.clickOnView(button_0);
 		solo.clickOnView(button_equal);
 
 		if (inputET.getText().toString().equals("NaN")) {
 			Log.d(TAG_APP, "Running of testDivideZeorByZero() was successfull");
 		} else {
 			Log.d(TAG_APP, "Running of testDivideZeorByZero() was FAILED!..");
 			Log.d(TAG_APP, "Result is: " + inputET.getText().toString());
 			fail();
 		}
 	}
 
 	public void testComplexCalculations() {
 
 		Log.d(TAG_APP, "Running testComplexCalculations()...");
 
 		Button button_0 = (Button) solo.getView(R.id.ButtonNumber00);
		Button button_1 = (Button) solo.getView(R.id.ButtonNumber01);
 		Button button_2 = (Button) solo.getView(R.id.ButtonNumber02);
		Button button_3 = (Button) solo.getView(R.id.ButtonNumber03);
		Button button_4 = (Button) solo.getView(R.id.ButtonNumber04);
 		Button button_5 = (Button) solo.getView(R.id.ButtonNumber05);
		Button button_6 = (Button) solo.getView(R.id.ButtonNumber06);
 		Button button_7 = (Button) solo.getView(R.id.ButtonNumber07);
 		Button button_8 = (Button) solo.getView(R.id.ButtonNumber08);
 		Button button_9 = (Button) solo.getView(R.id.ButtonNumber09);
 
		Button button_dot = (Button) solo.getView(R.id.ButtonSymbolDot);
 		Button button_bracket_open = (Button) solo
 				.getView(R.id.ButtonSymbolBracketLeft);
 		Button button_bracket_close = (Button) solo
 				.getView(R.id.ButtonSymbolBracketRight);
 
 		Button button_add = (Button) solo.getView(R.id.ButtonSymbolAdd);
 		Button button_sub = (Button) solo.getView(R.id.ButtonSymbolSubtract);
 		Button button_mul = (Button) solo.getView(R.id.ButtonSymbolMultiply);
 		Button button_div = (Button) solo.getView(R.id.ButtonSymbolDivide);
 
 		Button button_equal = (Button) solo.getView(R.id.ButtonSymbolExe);
 		Button button_clear = (Button) solo.getView(R.id.ButtonSymbolClear);
 
 		EditText inputET = (EditText) solo.getView(R.id.EditText01);
 		EditText resultET = (EditText) solo.getView(R.id.EditText02);
 
 		solo.clickOnView(button_2);
 		solo.clickOnView(button_add);
 		solo.clickOnView(button_2);
 		solo.clickOnView(button_mul);
 		solo.clickOnView(button_2);
 		solo.clickOnView(button_equal);
 
 		assertEquals("2+2*2", inputET.getText().toString());
 		assertEquals("6.0", resultET.getText().toString());
 
 		solo.clickOnView(button_clear);
 
 		assertEquals("", inputET.getText().toString());
 		assertEquals("", resultET.getText().toString());
 
 		solo.sleep(3000);
 
 		solo.clickOnView(button_bracket_open);
 		solo.clickOnView(button_2);
 		solo.clickOnView(button_add);
 		solo.clickOnView(button_2);
 		solo.clickOnView(button_bracket_close);
 		solo.clickOnView(button_mul);
 		solo.clickOnView(button_2);
 		solo.clickOnView(button_equal);
 
 		assertEquals("(2+2)*2", inputET.getText().toString());
 		assertEquals("8.0", resultET.getText().toString());
 
 		solo.clickOnView(button_clear);
 
 		assertEquals("", inputET.getText().toString());
 		assertEquals("", resultET.getText().toString());
 
 		solo.sleep(3000);
 
 		solo.clickOnView(button_5);
 		solo.clickOnView(button_add);
 		solo.clickOnView(button_bracket_open);
 		solo.clickOnView(button_7);
 		solo.clickOnView(button_mul);
 		solo.clickOnView(button_0);
 		solo.clickOnView(button_bracket_close);
 		solo.clickOnView(button_sub);
 		solo.clickOnView(button_8);
 		solo.clickOnView(button_div);
 		solo.clickOnView(button_bracket_open);
 		solo.clickOnView(button_2);
 		solo.clickOnView(button_mul);
 		solo.clickOnView(button_9);
 		solo.clickOnView(button_bracket_close);
 
 		solo.clickOnView(button_equal);
 
 		assertEquals("5+(7*0)-8/(2*9)", inputET.getText().toString());
 		assertEquals("4.5555553", resultET.getText().toString());
 
 		solo.sleep(3000);
 
 		Log.d(TAG_APP, "Running of testDivideZeorByZero() was successfull");
 	}
 
 }
