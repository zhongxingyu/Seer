 /*
  * Copyright (c) 2011. Uli Fuchs <ufuchs@gmx.com>
  * Released under the terms of the GNU GPL v2.0.
  */
 
 package de.z35.cli.test;
 
 import de.z35.frugal.cli.exam.freedays.TimeFrameExpander;
 import de.z35.frugal.cli.exam.freedays.TimeFrameValidator;
 import org.testng.annotations.Test;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ufuchs
  * Date: 30/07/11
  * Time: 12:01
  * To change this template use File | Settings | File Templates.
  */
 public class TimeFrameExpanderTest {
 
 	@Test
 	public void X_010_Test() {
 
 		TimeFrameExpander expander = new TimeFrameExpander();
 
		TimeFrameValidator.KindOfTimeFrame kindOf = TimeFrameValidator.KindOfTimeFrame.YY;
 
		expander.expand("2011", kindOf);
 
 	}
 
 }
