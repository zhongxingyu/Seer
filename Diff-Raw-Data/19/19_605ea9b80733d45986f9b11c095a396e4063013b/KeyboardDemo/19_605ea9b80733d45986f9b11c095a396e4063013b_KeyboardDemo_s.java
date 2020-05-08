 package org.tendiwa.modules;
 
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import org.jukito.JukitoRunner;
 import org.jukito.UseModules;
 import org.junit.Test;
 import org.junit.runner.RunWith;
import org.tendiwa.geometry.EnhancedRectangle;
import org.tendiwa.geometry.RectangleBuilderTemplate;
import org.tendiwa.geometry.RectangleSystem;
 import org.tendiwa.drawing.DrawingModule;
 import org.tendiwa.drawing.DrawingRectangleSystem;
 import org.tendiwa.drawing.TestCanvas;
 
 import java.awt.*;
 
 import static org.tendiwa.geometry.DSL.*;
 
 @RunWith(JukitoRunner.class)
 @UseModules(DrawingModule.class)
 public class KeyboardDemo extends RectangleBuilderTemplate {
 @Inject
 @Named("default")
 TestCanvas canvas;
 
 @Test
 public void draw() {
 
 	RectangleBuilderTemplate keyboard = new KeyboardDemo();
 	RectangleSystem keyboards = builder(1)
 		.place(keyboard, atPoint(5, 5))
 		.place(keyboard.rotate(CLOCKWISE), awayFrom(LAST_BOUNDING_REC).fromSide(S).margin(10).align(E))
 		.place(keyboard.rotate(HALF_CIRCLE), awayFrom(LAST_BOUNDING_REC).fromSide(S).margin(2).inMiddle())
 		.done();
 	canvas.draw(keyboards, DrawingRectangleSystem.withColors(Color.GRAY, Color.LIGHT_GRAY));
 	try {
 		Thread.currentThread().sleep(100000);
 	} catch (InterruptedException e) {
 		e.printStackTrace();
 	}
 }
 
 @Override
 public RectangleSystem build() {
 	int buttonWidth = 5;
 	int upperButtonHeight = 3;
	EnhancedRectangle button = rectangle(buttonWidth, buttonWidth);
	EnhancedRectangle topRowButton = rectangle(buttonWidth, upperButtonHeight);
 	return builder(1)
 		// Esc
 		.place(topRowButton, somewhere())
 		.rememberRectangle()
 			// Functional row
 		.place(
 			topRowButton
 				.repeat(4)
 				.placingNextAt(near(LAST_RECTANGLE).fromSide(E).inMiddle())
 				.repeat(3)
 				.placingNextAt(awayFrom(LAST_RECTANGLE).fromSide(E).margin(5).inMiddle()),
 			awayFrom(LAST_RECTANGLE).fromSide(E).margin(3).inMiddle()
 		)
 			// Shit key
 		.place(rectangle(10, 1), near(LAST_BOUNDING_REC).fromSide(E).align(N))
 			// ~ 1-0 - =
 		.place(
 			button
 				.repeat(13)
 				.placingNextAt(near(LAST_RECTANGLE).fromSide(E).inMiddle()),
 			awayFrom(REMEMBERED_RECTANGLE).fromSide(S).margin(2).align(W)
 		)
 		.rememberBoundingRec()
 			// Backspace
 		.place(rectangle(buttonWidth * 2 + 1, buttonWidth), near(LAST_RECTANGLE).fromSide(E).inMiddle())
 			// Tab
 		.place(rectangle(7, buttonWidth), near(REMEMBERED_BOUNDING_REC).fromSide(S).align(W))
 		.rememberRectangle()
 			// qwertyuiop[]
 		.place(
 			rectangle(buttonWidth, buttonWidth)
 				.repeat(12)
 				.placingNextAt(near(LAST_RECTANGLE).fromSide(E).inMiddle()),
 			near(LAST_RECTANGLE).fromSide(E).inMiddle()
 		)
 			// | \
 		.place(rectangle(9, buttonWidth), near(LAST_RECTANGLE).fromSide(E).inMiddle())
 			// Caps Lock
 		.place(rectangle(8, buttonWidth), near(REMEMBERED_RECTANGLE).fromSide(S).align(W))
 		.rememberRectangle()
 			// asdfghjkl;'
 		.place(
 			rectangle(buttonWidth, buttonWidth)
 				.repeat(11)
 				.placingNextAt(near(LAST_RECTANGLE).fromSide(E).inMiddle()),
 			near(LAST_RECTANGLE).fromSide(E).inMiddle()
 		)
 			// Enter
 		.place(rectangle(14, buttonWidth), near(LAST_RECTANGLE).fromSide(E).inMiddle())
 			// Left shift
 		.place(rectangle(12, buttonWidth), near(REMEMBERED_RECTANGLE).fromSide(S).align(W))
 		.rememberRectangle()
 			// zxcvbnm,./
 		.place(
 			button.repeat(10).placingNextAt(near(LAST_RECTANGLE).fromSide(E).inMiddle()),
 			near(LAST_RECTANGLE).fromSide(E).inMiddle()
 		)
 			// Right shift
 		.place(rectangle(16, buttonWidth), near(LAST_RECTANGLE).fromSide(E).inMiddle())
 			// Left Ctrl, left win, left alt
 		.place(
 			rectangle(buttonWidth + 1, buttonWidth)
 				.repeat(3)
 				.placingNextAt(near(LAST_RECTANGLE).fromSide(E).inMiddle()),
 			near(REMEMBERED_RECTANGLE).fromSide(S).align(W)
 		)
 			// Space
 		.place(rectangle(buttonWidth * 8, buttonWidth), near(LAST_RECTANGLE).fromSide(E).inMiddle())
 			// Right alt, win, menu, ctrl
 		.place(
 			rectangle(buttonWidth + 1, buttonWidth)
 				.repeat(4)
 				.placingNextAt(near(LAST_RECTANGLE).fromSide(E).inMiddle()),
 			near(LAST_RECTANGLE).fromSide(E).inMiddle())
 		.done();
 }
 }
