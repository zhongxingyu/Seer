 package com.inspedio.samples;
 
 
 import com.inspedio.enums.ScreenOrientation;
 import com.inspedio.system.InsMain;
 
 public class MainSample extends InsMain{
 
 	protected void init() {
		this.init(new SampleButtonState(),ScreenOrientation.PORTRAIT);
 	}
 
 	
 
 }
