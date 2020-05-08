 package logic.entity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.naming.LinkLoopException;
 
 import logic.Level;
 
 import org.jbox2d.callbacks.QueryCallback;
 import org.jbox2d.collision.AABB;
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.Fixture;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.joints.DistanceJointDef;
 import org.jbox2d.dynamics.joints.RevoluteJointDef;
 import org.lwjgl.util.Color;
 import org.lwjgl.util.vector.Vector2f;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Single;

 import render.RenderUtil;
import sun.security.util.Length;
 import util.MathUtil;
 
 public class Chain extends GameObject {
 	private float CHAIN_LINK_LENGTH = 15;
 	int numOfLinks;
 	float length;
 	float angle;
 	Vector2f begin, end;
 	Body beginBody, endBody;
 	private List<GameObject> chainLinks = new ArrayList<GameObject>();
 
 	public Chain(Level level, Body body1, Vector2f begin, Body body2,
 			Vector2f end) {
 		// TODO check is points have a bodyes
 		this.begin = begin;
 		this.end = end;
 		this.beginBody = body1;
 		this.endBody = body2;
 		this.level = level;
 
 		// lenght from begin to end
 		length = Math.abs((begin.x - end.x) * (begin.x - end.x))
 				+ Math.abs((begin.y - end.y) * (begin.y - end.y));
 		length = (float) Math.sqrt(length);
 
 		// calculate number of links
 		numOfLinks = (int) (length / CHAIN_LINK_LENGTH);
 		float ox = end.x - begin.x;
 		float oy = end.y - begin.y;
 //		System.out.println("atan of " + ox + " and " + oy + " = "
 //				+ Math.atan(oy / ox) * 60);
 		angle = (float) Math.atan(oy / ox);
 		if (end.x - begin.x < 0)
 			angle -= 3.14;
 //		System.out.println("lenght = " + length + "\number = " + numOfLinks
 //				+ "\nangle = " + angle * 60);
 		init();
 
 	}
 
 	@Override
 	public void init() {
 
 		float linkLenght = length / numOfLinks;
 		float llox = (float) (linkLenght * Math.cos(angle));
 		float lloy = (float) (linkLenght * Math.sin(angle));
 		Vector2f linkBegin = new Vector2f((float) (begin.x + llox / 2),
 				(float) (begin.y + lloy / 2));
 		Body b = beginBody;
 		Vec2 joinPoint = new Vec2(begin.x, begin.y);
 		ChainLink firstLink = new ChainLink(linkBegin, linkLenght, angle);
 		chainLinks.add(firstLink);
 		RevoluteJointDef join1 = new RevoluteJointDef();
 
 		join1.initialize(b, firstLink.getBody(), joinPoint.mul(1 / 30f));
 		level.getWorld().createJoint(join1);
 		Vector2f prevLink = firstLink.getPosition();
 
 		for (int i = 1; i < numOfLinks; i++) {
 			joinPoint = new Vec2(joinPoint.x + llox, joinPoint.y + lloy);
 //			System.out.println("joinpoint = " + joinPoint);
 			prevLink = new Vector2f(prevLink.x + llox, prevLink.y + lloy);
 			ChainLink chainLink = new ChainLink(prevLink, linkLenght, angle);
 			chainLinks.add(chainLink);
 			RevoluteJointDef join = new RevoluteJointDef();
 			join.initialize(firstLink.getBody(), chainLink.getBody(),
 					joinPoint.mul(1 / 30f));
 			level.getWorld().createJoint(join);
 			firstLink = chainLink;
 			join.collideConnected = false;
 		}
 		joinPoint = new Vec2(end.x, end.y);
 
 		join1 = new RevoluteJointDef();
 		join1.initialize(firstLink.getBody(), endBody, joinPoint.mul(1 / 30f));
 		level.getWorld().createJoint(join1);
 
 	}
 
 	@Override
 	public void update() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void move() {
 
 	}
 
 	@Override
 	public void draw() {
 		for (GameObject go : chainLinks) {
 			go.draw();
 		}
 	}
 
 	@Override
 	public void playSound() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void destroy() {
 		// TODO Auto-generated method stub
 
 	}
 
 	class ChainLink extends GameObject {
 		private float lenght;
 		private float width = 10;
 		private float angle;
 
 		public ChainLink(Vector2f position, float lenght, float angle) {
 			this.position = position;
 			this.lenght = lenght;
 			this.angle = angle;
 			Chain.this.level.getNotAddedGameObjects().add(this);
 			init();
 		}
 
 		@Override
 		public void init() {
 			BodyDef chainLinkDef = new BodyDef();
 			chainLinkDef.position
 					.set(new Vec2(position.x / 30, position.y / 30));
 			chainLinkDef.type = BodyType.DYNAMIC;
 			PolygonShape chainLinkShape = new PolygonShape();
 			chainLinkShape.setAsBox(lenght / 30 / 2, width / 30 / 2);
 			chainLinkDef.angle = angle;
 
 			this.body = Chain.this.level.getWorld().createBody(chainLinkDef);
 			this.body.m_userData = this;
 			FixtureDef chainLinkFixture = new FixtureDef();
 			chainLinkFixture.friction = 0.1f; // trenie
 			chainLinkFixture.density = 0.5f; // plotnost'
 			chainLinkFixture.restitution = 0f;
 			chainLinkFixture.shape = chainLinkShape;
 			// chainLinks do not interact chainLinks with each other
 			// chainLinkFixture.filter.groupIndex = -1;
 			body.createFixture(chainLinkFixture);
 		}
 
 		@Override
 		public void update() {
 
 		}
 
 		@Override
 		public void move() {
 			position = new Vector2f(body.getPosition().x * 30,
 					body.getPosition().y * 30);
 			angle = body.getAngle();
 		}
 
 		@Override
 		public void draw() {
 
 			RenderUtil.drawLine(new Vector2f((float) (position.x - (lenght - 2)
 					* Math.cos(angle) / 2), (float) (position.y - (lenght - 2)
 					* Math.sin(angle) / 2)), new Vector2f(
 					(float) (position.x + (lenght - 2) * Math.cos(angle) / 2),
 					(float) (position.y + (lenght - 2) * Math.sin(angle) / 2)),
 					3f, (Color) Color.GREY);
 
 		}
 
 		@Override
 		public void playSound() {
 
 		}
 
 		@Override
 		public void destroy() {
 
 		}
 
 		public Vector2f getBegin() {
 			return new Vector2f(
 					(float) (position.x - lenght * Math.cos(angle)),
 					(float) (position.y - lenght * Math.sin(angle)));
 		}
 
 		public Vector2f getEnd() {
 			return new Vector2f(
 					(float) (position.x + lenght * Math.cos(angle)),
 					(float) (position.y + lenght * Math.sin(angle)));
 		}
 	}
 
 }
