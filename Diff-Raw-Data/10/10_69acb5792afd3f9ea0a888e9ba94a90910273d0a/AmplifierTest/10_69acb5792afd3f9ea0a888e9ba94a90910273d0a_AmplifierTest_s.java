 package com.liqing.component;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.liqing.BaseOutputTest;
 
 public class AmplifierTest extends BaseOutputTest
 {
 
 	private Amplifier amplifier;
 
 	@Before
 	public void setUp()
 	{
 		super.setUp();
 		amplifier = new Amplifier("fake-amplifier");
 	}
 
 	@Test
 	public void shouldPrintOnWhenTurnOn()
 	{
 		amplifier.on();
 		assertOutPut("on");
 	}
 
 	@Test
 	public void shouldPrintOffWhenTurnOff()
 	{
 		amplifier.off();
 		assertOutPut("off");
 	}
 
 	@Test
 	public void shouldOutputWhenSetStereoSound()
 	{
 		amplifier.setStereoSound();
 		assertOutPut("stereo mode on");
 	}
 
 	@Test
 	public void shouldOutputWhenSetSurroundSound()
 	{
 		amplifier.setSurroundSound();
 		assertOutPut("surround sound on");
 	}
 
 	@Test
 	public void shouldOutputVolumeWhenSetVolume()
 	{
 		amplifier.setVolume(1);
 		assertOutPut("setting volume to 1");
 	}
 
 	@Test
 	public void shouldOutputWhenSetTuner()
 	{
		amplifier.setTuner(any(Tuner.class));
 		assertOutPut("setting tuner to");
 	}
 
 	@Test
 	public void shouldOutputWhenSetDvd()
 	{
		amplifier.setDvd(any(DvdPlayer.class));
 		assertOutPut("setting DVD player to");
 	}
 
 	@Test
 	public void shouldOutputWhenSetCD()
 	{
		amplifier.setCd(any(CDPlayer.class));
 		assertOutPut("setting CD player to");
 	}
 
 	@Test
 	public void shouldReturnDescriptionWhenToString()
 	{
 		String description = amplifier.toString();
 		assertThat(description, is("fake-amplifier"));
 	}
 }
