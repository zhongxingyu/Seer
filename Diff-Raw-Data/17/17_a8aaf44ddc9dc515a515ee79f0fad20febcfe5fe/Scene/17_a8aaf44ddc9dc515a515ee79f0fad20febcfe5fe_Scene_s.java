 package net.avh4.framework.uilayer.scene;
 
 public interface Scene {
 
 	String getTitle();
 
 	int getWidth();
 
 	int getHeight();
 
	void addText(final String text, final int x, final int y, final int width,
			final String customFontResource, final int fontSize);
 
 	void addPlaceholder(final String name, final int x, final int y,
 			final int width, final int height);
 
 	SceneImage addImage(final int x, final int y, final int width,
 			final int height, final String imageName, final int clipX,
 			final int clipY, final int clipWidth, final int clipHeight);
 
 	SceneImage addImage(final int x, final int y, final int width,
 			final int height, final String imageName);
 
 	void setSize(int width, int height);
 
 }
