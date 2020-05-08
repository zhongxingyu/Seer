 package jp.gmom.sample.temperature.tests;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.TouchUtils;
 import android.test.ViewAsserts;
 import android.test.suitebuilder.annotation.SmallTest;
 import android.view.View;
 import jp.gmom.sample.temperature.R;
 import jp.gmom.sample.temperature.TemperatureConverter;
 import jp.gmom.sample.temperature.TemperatureConverterActivity;
 import jp.gmom.sample.temperature.view.EditNumber;
 
 public class TemperatureConverterActivityTest extends ActivityInstrumentationTestCase2<TemperatureConverterActivity> {
 
     public static final double DELTA = 0.05;
     private EditNumber celsius;
     private EditNumber fahrenheit;
     private View convert;
 
     public TemperatureConverterActivityTest(String name) {
         super("jp.gmom", TemperatureConverterActivity.class);
         setName(name);
     }
 
     public void setUp() throws Exception {
         super.setUp();
         final TemperatureConverterActivity activity = getActivity();
 
         celsius = (EditNumber) activity.findViewById(R.id.celsius);
         fahrenheit = (EditNumber) activity.findViewById(R.id.fahrenheit);
         convert = activity.findViewById(R.id.convert);
     }
 
     @SmallTest
     public void testSimpleCreate() {
         ViewAsserts.assertOnScreen(celsius.getRootView(), celsius);
         ViewAsserts.assertOnScreen(fahrenheit.getRootView(), fahrenheit);
     }
 
     @SmallTest
     public void testAlignment() {
         ViewAsserts.assertRightAligned(celsius, fahrenheit);
         ViewAsserts.assertLeftAligned(celsius, fahrenheit);
     }
 
     @SmallTest
     public void testFieldsStartsEmpty() {
         assertTrue(celsius.getText().toString().isEmpty());
         assertTrue(fahrenheit.getText().toString().isEmpty());
     }
 
     @SmallTest
     public void testFieldsAreEmptyWhenTouched() {
         setAndTouchAndTestEmpty(celsius, celsius);
         setAndTouchAndTestEmpty(celsius, fahrenheit);
         setAndTouchAndTestEmpty(fahrenheit, fahrenheit);
         setAndTouchAndTestEmpty(fahrenheit, celsius);
     }
 
     private void setAndTouchAndTestEmpty(EditNumber setView, EditNumber touchView) {
         setNumbers(setView, 100.0);
         TouchUtils.clickView(this, touchView);
         assertTrue(celsius.getText().toString().isEmpty());
         assertTrue(fahrenheit.getText().toString().isEmpty());
     }
 
     @SmallTest
     public void testFahrenheitToCelsius() {
         testConvert(32.5, false);
     }
 
     private void testConvert(double degrees, boolean isSourceCelsius) {
         EditNumber source = isSourceCelsius ? celsius : fahrenheit;
         EditNumber target = isSourceCelsius ? fahrenheit : celsius;
 
         setNumbers(source, degrees);
         TouchUtils.clickView(this, convert);
 
         final double converted = isSourceCelsius ? TemperatureConverter.celsiusToFahrenheit(degrees)
                 : TemperatureConverter.fahrenheitToCelsius(degrees);
         final double targetValue = target.getValue();
         assertEquals(converted, targetValue, DELTA);
     }
 
     @SmallTest
     public void testCelsiusToFahrenheit() {
         testConvert(-105.35, true);
     }
 
     private void setNumbers(EditNumber view, double number) {
         TouchUtils.tapView(this, view);
         int[] keyCodes = KeyCodeConverter.convert(number);
         sendKeys(keyCodes);
     }
 
    @SmallTest
    public void testFail() {
        fail("fail me");
    }

 }
