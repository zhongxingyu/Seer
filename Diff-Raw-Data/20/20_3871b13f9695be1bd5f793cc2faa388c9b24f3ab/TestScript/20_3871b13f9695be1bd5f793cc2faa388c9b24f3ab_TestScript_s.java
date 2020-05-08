 package com.moomoohk.Grame.test;
 
 import java.awt.Color;
 
 import com.moomoohk.Grame.AI.SimpleChaseAI;
 import com.moomoohk.Grame.AI.SimpleStrollAI;
 import com.moomoohk.Grame.Basics.Entity;
 import com.moomoohk.Grame.Essentials.Base;
 import com.moomoohk.Grame.Essentials.Coordinates;
 import com.moomoohk.Grame.Essentials.GrameManager;
 import com.moomoohk.Grame.Essentials.GrameUtils;
 import com.moomoohk.Grame.Graphics.RenderManager;
 import com.moomoohk.Grame.Interfaces.GrameObject;
 import com.moomoohk.Grame.Interfaces.MainGrameClass;
 
 public class TestScript implements MainGrameClass
 {
 	public static void main(String[] args)
 	{
 		GrameManager.initialize(new TestScript());
 	}
 
 	@Override
 	public void newGame()
 	{
 		Base b = new Base(20, 20);
 		b.setWraparound(true);
 		Entity ent = new Entity(Color.blue);
 		ent.setSpeed(1);
 		Entity ent2 = new Entity();
 		ent2.setTarget(ent.ID);
 		ent2.setSpeed(7);
 		ent2.setRange(b.getDiagonal());
 		ent2.addAI(new SimpleChaseAI(), b.ID);
 		ent2.addAI(new SimpleStrollAI(), b.ID);
 		ent.makePlayer(1, true, b.ID);
 		GrameObject go = new GrameObject("test", 1, Color.black, false)
 		{
 			private static final long serialVersionUID = -7904811686747660223L;
 
 			@Override
 			public void tick(int bID)
 			{
 				this.color = GrameUtils.randomColor();
 			}
 
 			@Override
 			public boolean isCollidable()
 			{
 				return false;
 			}
 
 			@Override
 			public void consume(GrameObject go)
 			{
 			}
 		};
 		SpriteRender.objects.put(ent.ID, "player");
 		SpriteRender.objects.put(ent2.ID, "monster");
 		b.addGrameObject(ent2, GrameUtils.randomCoordinates(b));
 		b.addGrameObject(ent, new Coordinates(10, 10));
 		b.addGrameObject(go, GrameUtils.randomCoordinates(b));
		GrameManager.addRender(new SpriteRender());
 		RenderManager.render(b.ID, new SpriteRender());
 		RenderManager.setVisible(true);
 	}
 
 	@Override
 	public String getGameName()
 	{
 		return "test";
 	}
 }
