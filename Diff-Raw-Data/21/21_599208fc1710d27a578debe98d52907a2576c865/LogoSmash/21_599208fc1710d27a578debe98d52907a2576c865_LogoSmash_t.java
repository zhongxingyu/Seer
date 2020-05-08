 /*
  * Copyright (c) 2007 Scott Lembcke, (c) 2011 Jürgen Obernolte
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package org.physics.jipmunk.examples;
 
import java.util.logging.Level;
import java.util.logging.Logger;
 import org.physics.jipmunk.Body;
 import org.physics.jipmunk.CircleShape;
 import org.physics.jipmunk.Shape;
 import org.physics.jipmunk.Space;
 import org.physics.jipmunk.Util;
 
 /** @author jobernolte */
 public class LogoSmash extends ExampleBase {
 
	final static Logger LOGGER = Logger.getLogger(LogoSmash.class.getSimpleName());

 	private Space space;
	private int allSteps = 0;
 	static final int image_width = 188;
 	static final int image_height = 35;
 	static final int image_row_length = 24;
 	static final byte image_bitmap[] = {
 			15, -16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, -64,
 			15, 63, -32, -2, 0, 0, 0, 0, 0, 0, 0,
 			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, -64, 15, 127, -125, -1, -128, 0, 0,
 			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 			0, 0, 0, 127, -64, 15, 127, 15, -1, -64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 			0, 0, 0, 0, 0, 1, -1, -64, 15, -2,
 			31, -1, -64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, -64,
 			0, -4, 63, -1, -32, 0, 0, 0, 0, 0, 0,
 			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, -64, 15, -8, 127, -1, -32, 0, 0, 0,
 			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 			1, -1, -64, 0, -8, -15, -1, -32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 			0, 0, 1, -31, -1, -64, 15, -8, -32,
 			-1, -32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, -15, -1, -64, 9,
 			-15, -32, -1, -32, 0, 0, 0, 0, 0,
 			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, -15, -1, -64, 0, -15, -32, -1, -32, 0,
 			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 			0, 0, 63, -7, -1, -64, 9, -29, -32, 127, -61, -16, 63, 15, -61, -1, -8,
 			31, -16, 15, -8, 126, 7, -31,
 			-8, 31, -65, -7, -1, -64, 9, -29, -32, 0, 7, -8, 127, -97, -25, -1, -2,
 			63, -8, 31, -4, -1, 15, -13,
 			-4, 63, -1, -3, -1, -64, 9, -29, -32, 0, 7, -8, 127, -97, -25, -1, -2,
 			63, -8, 31, -4, -1, 15, -13,
 			-2, 63, -1, -3, -1, -64, 9, -29, -32, 0, 7, -8, 127, -97, -25, -1, -1,
 			63, -4, 63, -4, -1, 15, -13,
 			-2, 63, -33, -1, -1, -32, 9, -25, -32, 0, 7, -8, 127, -97, -25, -1, -1,
 			63, -4, 63, -4, -1, 15, -13,
 			-1, 63, -33, -1, -1, -16, 9, -25, -32, 0, 7, -8, 127, -97, -25, -1, -1,
 			63, -4, 63, -4, -1, 15, -13,
 			-1, 63, -49, -1, -1, -8, 9, -57, -32, 0, 7, -8, 127, -97, -25, -8, -1,
 			63, -2, 127, -4, -1, 15, -13,
 			-1, -65, -49, -1, -1, -4, 9, -57, -32, 0, 7, -8, 127, -97, -25, -8, -1,
 			63, -2, 127, -4, -1, 15, -13,
 			-1, -65, -57, -1, -1, -2, 9, -57, -32, 0, 7, -8, 127, -97, -25, -8, -1,
 			63, -2, 127, -4, -1, 15, -13,
 			-1, -1, -57, -1, -1, -1, 9, -57, -32, 0, 7, -1, -1, -97, -25, -8, -1, 63,
 			-1, -1, -4, -1, 15, -13, -1,
 			-1, -61, -1, -1, -1, -119, -57, -32, 0, 7, -1, -1, -97, -25, -8, -1, 63,
 			-1, -1, -4, -1, 15, -13, -1,
 			-1, -61, -1, -1, -1, -55, -49, -32, 0, 7, -1, -1, -97, -25, -8, -1, 63,
 			-1, -1, -4, -1, 15, -13, -1,
 			-1, -63, -1, -1, -1, -23, -49, -32, 127, -57, -1, -1, -97, -25, -1, -1,
 			63, -1, -1, -4, -1, 15, -13,
 			-1, -1, -63, -1, -1, -1, -16, -49, -32, -1, -25, -1, -1, -97, -25, -1,
 			-1, 63, -33, -5, -4, -1, 15,
 			-13, -1, -1, -64, -1, -9, -1, -7, -49, -32, -1, -25, -8, 127, -97, -25,
 			-1, -1, 63, -33, -5, -4, -1,
 			15, -13, -1, -1, -64, -1, -13, -1, -32, -49, -32, -1, -25, -8, 127, -97,
 			-25, -1, -2, 63, -49, -13,
 			-4, -1, 15, -13, -1, -1, -64, 127, -7, -1, -119, -17, -15, -1, -25, -8,
 			127, -97, -25, -1, -2, 63,
 			-49, -13, -4, -1, 15, -13, -3, -1, -64, 127, -8, -2, 15, -17, -1, -1,
 			-25, -8, 127, -97, -25, -1,
 			-8, 63, -49, -13, -4, -1, 15, -13, -3, -1, -64, 63, -4, 120, 0, -17, -1,
 			-1, -25, -8, 127, -97, -25,
 			-8, 0, 63, -57, -29, -4, -1, 15, -13, -4, -1, -64, 63, -4, 0, 15, -17,
 			-1, -1, -25, -8, 127, -97,
 			-25, -8, 0, 63, -57, -29, -4, -1, -1, -13, -4, -1, -64, 31, -2, 0, 0,
 			103, -1, -1, -57, -8, 127, -97,
 			-25, -8, 0, 63, -57, -29, -4, -1, -1, -13, -4, 127, -64, 31, -2, 0, 15,
 			103, -1, -1, -57, -8, 127,
 			-97, -25, -8, 0, 63, -61, -61, -4, 127, -1, -29, -4, 127, -64, 15, -8, 0,
 			0, 55, -1, -1, -121, -8,
 			127, -97, -25, -8, 0, 63, -61, -61, -4, 127, -1, -29, -4, 63, -64, 15,
 			-32, 0, 0, 23, -1, -2, 3, -16,
 			63, 15, -61, -16, 0, 31, -127, -127, -8, 31, -1, -127, -8, 31, -128, 7,
 			-128, 0, 0
 	};
 	private long running = 0L;
 
 	int getPixel(int x, int y) {
 		return (image_bitmap[(x >> 3) + y * image_row_length] >> (~x & 0x7)) & 1;
 	}
 
 	Shape makeBall(float x, float y) {
 		Body body = new Body(1.0f, Float.POSITIVE_INFINITY);
 		body.setPosition(Util.cpv(x, y));
 
 		Shape shape = new CircleShape(body, 0.95f, Util.cpvzero());
 		shape.setElasticity(0.0f);
 		shape.setFrictionCoefficient(0.0f);
 
 		return shape;
 	}
 
 	@Override
 	public Space init() {
 		space = new Space();
 		/*space.resizeActiveHash(2.0f, 10000);
 		space.resizeStaticHash(2.0f, 10000);*/
 		space.setIterations(1);
 		space.useSpatialHash(2, 10000);
 
 		Body body;
 		Shape shape;
 
 		for (int y = 0; y < image_height; y++) {
 			for (int x = 0; x < image_width; x++) {
 				if (getPixel(x, y) == 0) {
 					continue;
 				}
 
 				float x_jitter = (float) (0.05f * Math.random());
 				float y_jitter = (float) (0.05f * Math.random());
 
 				shape = makeBall(2 * (x - image_width / 2 + x_jitter), 2 * (image_height
 						/ 2 - y + y_jitter));
 				space.addBody(shape.getBody());
 				space.addShape(shape);
 			}
 		}
 
 		body = space.addBody(new Body(Float.POSITIVE_INFINITY,
 				Float.POSITIVE_INFINITY));
 		body.setPosition(Util.cpv(-1000.0f, -10.0f));
 		body.setVelocity(Util.cpv(400.0f, 0.0f));
 
 		shape = space.addShape(new CircleShape(body, 8.0f, Util.cpvzero()));
 		shape.setElasticity(0.0f);
 		shape.setFrictionCoefficient(0.0f);
 		shape.setLayers(NOT_GRABABLE_MASK);
 
 		return space;
 	}
 
 	private long steps = 0;
 
 	@Override
 	public void update(long delta) {
 		float dt = 1.0f / 60.0f;
 
 		steps += delta;
 		while (steps > 0) {
 			space.step(dt);
 			steps -= (int) (1000.0f / 60.0f);
			allSteps++;
			if (allSteps >= 100) {
				//System.exit(0);
				return;
			}
 		}
 	}
 
 	@Override
 	public DrawSpace.Options getDrawSpaceOptions() {
 		return new DrawSpace.Options(
 				false, false, false, 2.0f, 3.0f, 0.0f);
 	}
 
 	public static void main(String[] args) {
 		/*try {
 					new LwjglApplication(new LogoSmash()).start();
 				} catch (SlickException ex) {
 					java.util.logging.Logger.getLogger(LogoSmash.class.getName()).log(
 							Level.SEVERE, null, ex);
 				}*/
 		/*try {                 
 			System.in.read();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}*/
		LOGGER.log(Level.INFO, "starting LogoSmash");
 		new LogoSmash().start(640, 480);
 	}
 }
