 package com.globalresolvewws.test;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import android.test.AndroidTestCase;
 
 import com.globalresolvewws.*;
 
 import org.junit.Test;
 
 public class WeatherServiceTest extends AndroidTestCase{
 
 	@Test
 	public void test() {
 		WeatherService WS = new WeatherService();
		ArrayList<Weather> al = WS.Weather();
 		assertNotNull(al);
 	}
 }
