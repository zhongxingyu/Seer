 package controller;
 
 import java.util.ArrayList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import view.TDI;
 
 public class TestSetup {
 	BigLogic b;
 
 	@Before
 	public void before() throws InterruptedException {
 		ArrayList<TDI> tdis=new ArrayList<>();
 		TDI t1=new TDI((byte) 49,100,100,100, new float[]{0,0,0});
 		b=new BigLogic();
 		tdis.add(t1);
 		b.setTdis(tdis);
 		b.splitIcons();
 		Executor.saveBackground(b.getWallpaper().markArea(b.getTdis()));
 		Thread.sleep(1000);
 	}
 	
 	@Test
	public void rotateRight() throws InterruptedException {
 		new Thread(b).start();
 		TDI t2=new TDI((byte) 49,100,100,100, new float[]{0,0,100});
 		b.getCommands().add(t2);
 		Thread.sleep(5000);
 		before();
 	}
 	
 	@Test // TODO
	public void rotateLeft() throws InterruptedException {
 		new Thread(b).start();
 		TDI t2=new TDI((byte) 49,100,100,100, new float[]{0,0,-100});
 		b.getCommands().add(t2);
 		Thread.sleep(5000);
 		before();
 	}
 }
