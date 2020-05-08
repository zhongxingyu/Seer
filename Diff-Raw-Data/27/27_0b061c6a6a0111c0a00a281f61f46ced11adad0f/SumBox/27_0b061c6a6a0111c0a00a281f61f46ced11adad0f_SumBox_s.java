 /***
 === BrainGame ===
 
 Copyright (C) 2011 Giovanni Amati
 
 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 ***/
 
 package org.anddev.andengine.braingamelite.scene;
 
 import java.util.LinkedList;
 
 import org.anddev.andengine.braingamelite.singleton.Enviroment;
 import org.anddev.andengine.braingamelite.singleton.Resource;
 import org.anddev.andengine.braingamelite.util.MyChangeableText;
 import org.anddev.andengine.braingamelite.util.MyScene;
 import org.anddev.andengine.braingamelite.util.MySound;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.text.Text;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.extension.physics.box2d.util.Vector2Pool;
 import org.anddev.andengine.util.MathUtils;
 
 import android.hardware.SensorManager;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 public class SumBox extends MyScene {
 	private int mNum;
 	private LinkedList<Integer> mSumListValue; // supporto
 	private MySound mPush;
 	
 	private PhysicsWorld mPhysicsWorld;
 	
 	private LinkedList<Integer> mListValue;
 	private int mSum;
 	
 	public SumBox(LinkedList<Integer> aListValue, int aSum) {
 		super();
 		getBackgroundLayer().getLastChild().setColor(0.8f, 0.8f, 1f);
 		
 		this.mPush = Resource.instance().getSound("push");
 		
 		// difficult
 		this.mNum = 0;
     	switch (Enviroment.instance().getDifficult()) {
     		case 0: this.mNum = 4; break;
     		case 1: this.mNum = 5; break;
     		case 2: this.mNum = 6; break;
     	}
     	
     	// fisica
     	this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH + 30), false);
     	
         // base
     	Sprite ground = new Sprite(124, Enviroment.CAMERA_HEIGHT - 45, Resource.instance().texBase);
     	FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0f, 0f);
     	PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
     	getGameLayer().attachChild(ground);
     	
 		// dati
 		this.mSumListValue = new LinkedList<Integer>();
 		
     	if (aListValue == null) {
     		this.mListValue = new LinkedList<Integer>();
     		
     		for (int i = 1; i <= this.mNum; i++) {
     			int value = MathUtils.random(1, 9); // numeri da 1 a 9
     			this.mListValue.add(new Integer(value));
     			this.mSumListValue.add(new Integer(value));
     		}
     	} else {
     		this.mListValue = aListValue;
     		
     		for (int i = 0; i < aListValue.size(); i++)
     			this.mSumListValue.add(aListValue.get(i));
     	}
     	
     	if (aSum == 0) {
     		LinkedList<Integer> temp = new LinkedList<Integer>(); // for sum
     		int to_remove = MathUtils.random(1, this.mNum - 3);
     		while (temp.size() < (this.mListValue.size() - to_remove)) {
     			Integer index = new Integer(MathUtils.random(0, this.mListValue.size() - 1));
     			if (!temp.contains(index))
     				temp.add(index);
     		}
     		this.mSum = 0;
     		for (int i = 0; i < temp.size(); i++)
     			this.mSum += this.mListValue.get(temp.get(i).intValue()).intValue();
     	} else
     		this.mSum = aSum;
     	
     	// label
     	Sprite som = new Sprite(335, 75, Resource.instance().texSomm);	
     	Text sumText = new Text(11, 7, Resource.instance().fontSum, "Sum");
     	som.attachChild(sumText);
     	
     	Text sumText2 = new Text(0, 66, Resource.instance().fontSumNum, Integer.toString(this.mSum));
     	sumText2.setPosition(som.getWidthScaled() / 2 - sumText2.getWidthScaled() / 2, sumText2.getY());
     	sumText2.setColor(1.0f, 1.0f, 0.5f);
     	som.attachChild(sumText2);
     	getGameLayer().attachChild(som);
 		
         // physics
         registerUpdateHandler(this.mPhysicsWorld);
 	}
 	
 	public void start() {
 		// add box
 		for (int i = 0; i < this.mListValue.size(); i++)
 			addBox(this.mListValue.get(i).intValue(), i);
 	}
 	
 	private void addBox(int value, int pos) {
 		Sprite box = new Sprite(179, - pos * 150, Resource.instance().texBox);
 		MyChangeableText label = new MyChangeableText(0, 0, Resource.instance().fontBox, Integer.toString(value), 1);
 		label.setPosition(box.getWidthScaled() / 2 - label.getWidthScaled() / 2, box.getHeightScaled() / 2 - label.getHeightScaled() / 2);
 		
 		float[][] color = Resource.instance().getColor();
 		box.setColor(color[value-1][0], color[value-1][1], color[value-1][2]);
     	
     	FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(0, 0f, 0f);
     	Body bodyBox = PhysicsFactory.createBoxBody(this.mPhysicsWorld, box, BodyType.DynamicBody, objectFixtureDef); 	
     	this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(box, bodyBox, true, false)); // false update la rotazione
     	
     	registerTouchArea(box); // reg touch
     	
     	box.attachChild(label);
     	getGameLayer().attachChild(box);
 	}
 	
 	@Override
 	public void manageTouch(ITouchArea pTouchArea) {
 		Sprite box = (Sprite)pTouchArea;
 		Body bodyBox = this.mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(box);
 		
 		Vector2 velocity = Vector2Pool.obtain(-40, 0);
 		bodyBox.setLinearVelocity(velocity);
 		Vector2Pool.recycle(velocity);
 		this.mPush.play();
 		
 		Integer value = Integer.parseInt(((MyChangeableText)box.getFirstChild()).getText());
 		this.mSumListValue.remove(value);
 		
 		int tempSum = 0;
     	for (int i = 0; i < this.mSumListValue.size(); i++)
     		tempSum += this.mSumListValue.get(i).intValue();
     	
     	if (tempSum < this.mSum)
     		Resource.instance().fail(new SumBox(this.mListValue, this.mSum));
     	else if (tempSum == this.mSum)
     		Resource.instance().done();
 	}
 	
 }
