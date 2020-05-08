 package com.gemserk.libgdx.tests;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 
 public class TestsApplicationListener implements ApplicationListener {
 
 	TestScreen currentTestScreen;
 
 	@Override
 	public void create() {
 		setTestScreen(new TestSelectionScreen());
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		if (currentTestScreen != null)
 			currentTestScreen.resize(width, height);
 	}
 
 	@Override
 	public void render() {
 		if (currentTestScreen != null)
 			currentTestScreen.render();
 	}
 
 	@Override
 	public void pause() {
 
 	}
 
 	@Override
 	public void resume() {
 
 	}
 
 	@Override
 	public void dispose() {
 		if (currentTestScreen != null)
 			currentTestScreen.dispose();
 		currentTestScreen = null;
 	}
 
 	public void setTestScreen(final TestScreen testScreen) {
 		Gdx.app.postRunnable(new Runnable() {
 			@Override
 			public void run() {
 				if (currentTestScreen != null)
 					currentTestScreen.dispose();
 				testScreen.setParent(TestsApplicationListener.this);
 				testScreen.create();
 				TestsApplicationListener.this.currentTestScreen = testScreen;
 			}
 		});
 	}
 
 }
