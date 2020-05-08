 package com.snobwall.nesslet;
 
import android.util.Log;
 
 public class SquareWaveGen implements IAudioProvider
 {
 	protected boolean _on;
 	
 	protected int _frequency; // of output device
 
 	protected int _dutyCycle = 50; // 0..100
 	protected byte _amplitude = 127; // 0..127 
 	protected int _sampleRate;
 	
	protected final String logTag = "Nesslet.SquareWaveGen";
	
 	public int get_dutyCycle() {
 		return _dutyCycle;
 	}
 
 	public void set_dutyCycle(int dutyCycle) {
 		_dutyCycle = dutyCycle;
 	}
 
 	public int get_frequency() {
 		return _frequency;
 	}
 
 	public void set_frequency(int frequency) {
 		_frequency = frequency;
 		
		_samplePeriod = (float)_sampleRate / (float)_frequency;
 	}
 
 	public byte get_amplitude() {
 		return _amplitude;
 	}
 
 	public void set_amplitude(byte amplitude) {
 		_amplitude = amplitude;
 	}
 
 	public boolean get_on() {
 		return _on;
 	}
 
 	public void set_on(boolean on) {
 		_on = on;
 	}
 
 	public SquareWaveGen(int sampleRate, int dutyCycle)
 	{
 		_sampleRate = sampleRate;
 		_dutyCycle = dutyCycle;
 		
 	}
 	
	protected float _samplePeriod; // Period of sample (dependent on _sampleRate)
    protected float _sampleOffs = 0;
     
 	@Override
 	public void nextSample(short[] sampleBuf, int offs)
 	{
		int middle = (int)_samplePeriod * _dutyCycle / 100;
		
		_sampleOffs = (_sampleOffs + 1) % _samplePeriod;
		int sampleIdx = (int)_sampleOffs;
 		
 		short sample;
 		
 		if (_amplitude == 0)
 		{
 			sampleBuf[offs] = sampleBuf[offs+1] = 0;
 		}
 		
		if (sampleIdx < middle)
 		{
 			sample = (short)(_amplitude << 8);
 		}
 		else
 		{
 			sample = (short)(-(_amplitude << 8));
 		}
 		
 		sampleBuf[offs] = sampleBuf[offs+1] = sample;
     }
     
 }
