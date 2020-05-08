 /***
 === BrainGame ===
 
 Copyright (C) 2011 Giovanni Amati
 
 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 ***/
 
 package org.anddev.andengine.braingamelite.scene;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.anddev.andengine.braingamelite.singleton.Enviroment;
 import org.anddev.andengine.braingamelite.singleton.Resource;
 import org.anddev.andengine.braingamelite.util.MyChangeableText;
 import org.anddev.andengine.braingamelite.util.MyScene;
 import org.anddev.andengine.braingamelite.util.MySound;
 import org.anddev.andengine.entity.modifier.LoopEntityModifier;
 import org.anddev.andengine.entity.modifier.RotationModifier;
 import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.shape.Shape;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.text.Text;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.util.MathUtils;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 public class CountDown extends MyScene {
 	private int mNum;
 	private PhysicsWorld mPhysicsWorld;
 	private MySound mPop;
 	private LinkedList<Double> mSupportListValue;
 	
 	private LinkedList<Double> mListValue;
 	private LinkedList<Double> mOrderListValue;
 	
 	public CountDown(LinkedList<Double> aListValue, LinkedList<Double> aOrderListValue) {
 		super();
 		getBackgroundLayer().getLastChild().setColor(0.8f, 1f, 0.8f);
 		
 		this.mPop = Resource.instance().getSound("pop");
 		
 		// difficult
 		this.mNum = 0;
     	switch (Enviroment.instance().getDifficult()) {
     		case 0: this.mNum = 4; break;
     		case 1: this.mNum = 6; break;
     		case 2: this.mNum = 6; break;
     	}
     	
     	// fisica
     	this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0.05f), false);
     	
         // base
     	Shape ground = new Rectangle(0, Enviroment.CAMERA_HEIGHT, Enviroment.CAMERA_WIDTH, 0);
 		Shape roof = new Rectangle(0, 90, Enviroment.CAMERA_WIDTH, 0);
 		Shape left = new Rectangle(0, 0, 0, Enviroment.CAMERA_HEIGHT);
 		Shape right = new Rectangle(Enviroment.CAMERA_WIDTH, 0, 0, Enviroment.CAMERA_HEIGHT);
 		
 		FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0f);
 		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
 		
 		getGameLayer().attachChild(ground);
 		getGameLayer().attachChild(roof);
 		getGameLayer().attachChild(left);
 		getGameLayer().attachChild(right);
         
 		this.mSupportListValue = new LinkedList<Double>();
 		
 		// dati
     	if (aListValue == null) {
     		this.mListValue = new LinkedList<Double>();
     		this.mOrderListValue = new LinkedList<Double>();
     		
     		for (int i = 1; i <= this.mNum; i++) {
     			double value = 0.0;
     			double real_value = 0.0;
     			if (Enviroment.instance().getDifficult() == 2) {
     				double value1 = (double)MathUtils.random(1, 9); // numeri da 1 a 9
     				double value2 = 0.0;
     				while (value2 == 0.0 || value2 == value1)
     					value2 = (double)MathUtils.random(1, 9); // numeri da 1 a 9
     				value = value1 + value2 * 0.1;
     				real_value = value1 / value2;
     			} else {
     				value = (double)MathUtils.random(0, 99); // numeri da 0, 99
     				real_value = value;
     			}
     			int sign = MathUtils.random(0, 2);
     			if (sign == 1) {
     				value *= -1.0;
     				real_value *= -1.0;
     			}
     			this.mListValue.add(new Double(value));
     			this.mOrderListValue.add(new Double(real_value));
     			
     			Collections.sort(this.mOrderListValue);
     		}
     	} else {
     		this.mListValue = aListValue;
     		this.mOrderListValue = aOrderListValue;
     	}
     	
     	for (int i = 0; i < this.mOrderListValue.size(); i++)
     		this.mSupportListValue.add(this.mOrderListValue.get(i));
     	
     	// label
     	Sprite quad = new Sprite(21, 133, Resource.instance().texQuad2);
     	Text look = new Text(28, 23, Resource.instance().fontCount, "Small to large");
 		quad.attachChild(look);
 		getGameLayer().attachChild(quad);
 		
         // physics
         registerUpdateHandler(this.mPhysicsWorld);
 	}
 	
 	public void start() {
 		//registerUpdateHandler(new TimerHandler(0.5f, false, new ITimerCallback() {
 		//	@Override
 		//	public void onTimePassed(TimerHandler pTimerHandler) {
 				// add balloon
 				for (int i = 0; i < CountDown.this.mListValue.size(); i++)
 					addBalloon(CountDown.this.mListValue.get(i).doubleValue(), i);				
 		//	}
 			
 		//}));
 	}
 	
 	private void addBalloon(double value, int pos) {
 		Sprite ball = new Sprite(MathUtils.random(80, 120), 100 + (pos + 1) * 50, Resource.instance().texBalloon);
 		
 		float[][] color = Resource.instance().getColor();;
 		ball.setColor(color[pos][0], color[pos][1], color[pos][2]);
     	float verso = 1f;
     	if (MathUtils.random(0, 1) == 1)
     		verso = -1.0f;
     	ball.registerEntityModifier(
 				new LoopEntityModifier(
 						null, 
 						-1, 
 						null,
 						new SequenceEntityModifier(
 								new RotationModifier(
 										20f + MathUtils.random(0, 20), 
 										0f, 
 										360f * verso)
 						)
 				)
 		);
     	
     	int adjust = 0;
     	int adjust2 = 0;
     	float size = 1.8f;
     	
 		if (value < 0) {
 			adjust = -20;
 			if (Enviroment.instance().getDifficult() == 2) {
 				adjust -= 1;
 				adjust2 = -1;
 				size -= 0.25f;
 			}
 		}
 		if (Math.abs(value) < 10 && Enviroment.instance().getDifficult() != 2) {
 			adjust += 15;
 			size += 0.6f;
 		}
 		
 		String text;
 		if (Enviroment.instance().getDifficult() == 2) {
 			adjust -= 24;
 			adjust2 = -10;
 			size -= 0.2f;
 			int numer = (int)value;
 			double decimal = ((int)(Math.round(Math.abs(value - numer) * 100.0)) / 100.0);
 			int denom = (int)(decimal * 10);
 			text = Integer.toString(numer) + "/" + Integer.toString(denom);
 		} else {
 			text = Integer.toString((int)value);
 			adjust -= 2;
 	    	adjust2 -= 7;
 		}
 		MyChangeableText label = new MyChangeableText(90 + adjust, 99 + adjust2, Resource.instance().fontBalloon, text, 4);
 		label.setScale(size);
 		ball.attachChild(label);
 		
 		ball.setScale((float)MathUtils.random(45, 100) / 100f);
 		
     	FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0f);
     	Body bodyBox = PhysicsFactory.createCircleBody(this.mPhysicsWorld, ball, BodyType.DynamicBody, objectFixtureDef);
     	bodyBox.applyLinearImpulse(new Vector2(MathUtils.random(0, 1) * verso, -5), bodyBox.getPosition()); // spinta per animare
     	
     	this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(ball, bodyBox, true, true)); // false update la rotazione
     	
     	registerTouchArea(ball); // reg touch
     	getGameLayer().attachChild(ball);
 	}
 	
 	@Override
 	public void manageTouch(ITouchArea pTouchArea) {
 		Sprite ball = (Sprite)pTouchArea;
 		
 		PhysicsConnector facePhysicsConnector = this.mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(ball);
 		
 		this.mPhysicsWorld.unregisterPhysicsConnector(facePhysicsConnector);
 		this.mPhysicsWorld.destroyBody(facePhysicsConnector.getBody());
 		
 		unregisterTouchArea(ball);
 		getGameLayer().detachChild(ball);
 		
 		this.mPop.play();
 		
 		boolean cond = false;
 		if (Enviroment.instance().getDifficult() == 2) {
 			double value = 0.0;
 			String text = ((MyChangeableText)ball.getFirstChild()).getText();
 			Pattern pattern = Pattern.compile("^(-)*(\\d)/(\\d)$");
 			Matcher matcher = pattern.matcher(text);
 			if (matcher.find()) {
 				Integer a = Integer.parseInt(matcher.group(2));
 				Integer b = Integer.parseInt(matcher.group(3));
 				value = a.doubleValue() / b.doubleValue();
 				if (matcher.group(1) != null && matcher.group(1).equals("-"))
 					value *= -1.0;
 			}
 			cond = (value == this.mSupportListValue.getFirst().doubleValue());
 		} else
 			cond = Integer.parseInt(((MyChangeableText)ball.getFirstChild()).getText()) == this.mSupportListValue.getFirst().intValue();
 		
 		if (cond) {
 			this.mSupportListValue.remove(0);
 			if (this.mSupportListValue.size() == 0)
 				Resource.instance().done();
 		} else
 			Resource.instance().fail(new CountDown(this.mListValue, this.mOrderListValue));
 	}
 	
 }
