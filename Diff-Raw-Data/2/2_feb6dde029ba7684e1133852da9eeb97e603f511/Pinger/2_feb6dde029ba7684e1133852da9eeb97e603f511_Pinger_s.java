 package com.vaguehope.chiaki.service;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.camel.ProducerTemplate;
 
 public class Pinger extends TimerTask {
 
 	private static final long DELAY = 10L * 1000L; // 10 seconds.
 
 	private final Timer timer = new Timer();
 
 	private final ProducerTemplate producerTemplate;
 	
 	public Pinger (ProducerTemplate producerTemplate) {
 		this.producerTemplate = producerTemplate;
 	}
 
 	public void start () {
 		this.timer.scheduleAtFixedRate(this, DELAY, DELAY);
 	}
 
 	public void dispose () {
 		this.timer.cancel();
 	}
 
 	@Override
 	public void run () {
		this.producerTemplate.sendBody("example.foo", "desu~");
 	}
 
 }
