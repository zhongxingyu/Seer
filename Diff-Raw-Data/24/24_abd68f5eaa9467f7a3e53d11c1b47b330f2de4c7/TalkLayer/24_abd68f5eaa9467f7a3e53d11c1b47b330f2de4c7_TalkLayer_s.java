 package de.dakror.liturfaliarcest.layer;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import de.dakror.gamesetup.layer.Layer;
 import de.dakror.gamesetup.ui.ClickEvent;
 import de.dakror.gamesetup.ui.button.TextButton;
 import de.dakror.gamesetup.util.Helper;
 import de.dakror.liturfaliarcest.game.Game;
 import de.dakror.liturfaliarcest.game.entity.Entity;
 import de.dakror.liturfaliarcest.game.entity.creature.NPC;
 import de.dakror.liturfaliarcest.settings.FlagManager;
 import de.dakror.liturfaliarcest.settings.Talk;
 
 /**
  * @author Dakror
  */
 public class TalkLayer extends Layer
 {
 	Entity source;
 	JSONArray talk;
 	String activeText, activeName;
 	int index;
 	Talk activeTalk; // for JS event
 	int[] questTriggers = {};
 	
 	public TalkLayer(JSONArray t, Entity s)
 	{
 		talk = t;
 		source = s;
 		index = -1;
 		activeTalk = new Talk();
 	}
 	
 	@Override
 	public void init()
 	{
 		components.clear();
 		
 		if (index == -1) next();
 		
 		TextButton cancel = new TextButton(65, Game.getHeight() / 5 * 3 - 10, "Abbruch");
 		cancel.setWidth((Game.getWidth() / 4 - 30) / 2);
 		cancel.setHeight(Math.round(TextButton.HEIGHT * (cancel.getWidth() / (float) TextButton.WIDTH)));
 		cancel.addClickEvent(new ClickEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				endTalk();
 			}
 		});
 		components.add(cancel);
 		TextButton ok = new TextButton(65 + cancel.getWidth(), Game.getHeight() / 5 * 3 - 10, "Ok");
 		ok.setWidth((Game.getWidth() / 4 - 30) / 2);
 		ok.setHeight(Math.round(TextButton.HEIGHT * (cancel.getWidth() / (float) TextButton.WIDTH)));
 		ok.addClickEvent(new ClickEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				if (questTriggers.length > 0)
 				{
 					for (int q : questTriggers)
 					{
 						if (FlagManager.isFlag("QUEST_" + q + "_ACCEPTED"))
 						{
 							FlagManager.removeFlag("QUEST_" + q + "_ACCEPTED");
 							FlagManager.setFlag("QUEST_" + q + "_DONE");
 						}
 						else FlagManager.setFlag("QUEST_" + q + "_ACCEPTED");
 					}
 				}
 				next();
 			}
 		});
 		components.add(ok);
 	}
 	
 	@Override
 	public void draw(Graphics2D g)
 	{
 		if (activeText != null)
 		{
 			try
 			{
 				Helper.drawContainer(50, 50, Game.getWidth() / 4, Game.getHeight() / 5 * 3, true, false, g);
 				
 				Helper.setRenderingHints(g, false);
 				BufferedImage bi = Game.getImage(activeName.length() > 0 ? source.getMeta().getString("texture") : Game.player.getTexture());
 				Helper.drawShadow(80, 70, bi.getWidth() / 4 * 4, bi.getHeight() / 4 / 2 * 4 + 30, g);
 				Helper.drawOutline(80, 70, bi.getWidth() / 4 * 4, bi.getHeight() / 4 / 2 * 4 + 30, false, g);
 				Helper.drawImage(bi, 80, 80, bi.getWidth() / 4 * 4, bi.getHeight() / 4 / 2 * 4, 0, 0, bi.getWidth() / 4, bi.getHeight() / 4 / 2, g);
 				Helper.setRenderingHints(g, true);
 				
 				Helper.drawStringWrapped(activeName, 90 + bi.getWidth() / 4 * 4, 120, Game.getWidth() / 4 - bi.getWidth() / 4 * 4 - 60, g, 45);
 				Helper.drawOutline(55, 55, Game.getWidth() / 4 - 10, bi.getHeight() / 4 / 2 * 4 + 70, false, g);
 				
 				Helper.drawOutline(55, Game.getHeight() / 5 * 3 - components.get(0).getHeight() + 20 + 7, Game.getWidth() / 4 - 10, components.get(0).getHeight() + 20, false, g);
 				
 				Helper.drawOutline(50, 50, Game.getWidth() / 4, Game.getHeight() / 5 * 3, true, g);
 				
 				Helper.drawStringWrapped(activeText, 75, bi.getHeight() / 4 / 2 * 4 + 170, Game.getWidth() / 4 - 50, g, 30);
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 			
 			drawComponents(g);
 		}
 	}
 	
 	@Override
 	public void update(int tick)
 	{
 		if (activeText != null) updateComponents(tick);
 	}
 	
 	public void endTalk()
 	{
 		source.setFrozen(false);
 		Game.player.setFrozen(false);
 		Game.currentGame.removeLayer(this);
 		Game.world.skipWorldClick = true;
 	}
 	
 	public void next()
 	{
 		if (source instanceof NPC) ((NPC) source).checkForQuestState();
 		if (index == talk.length() - 1)
 		{
			source.onNextTalk(activeTalk, new Talk(null));
 			endTalk();
 			return;
 		}
 		
 		try
 		{
 			index++;
 			JSONArray a = talk.getJSONArray(index);
 			for (int i = 0; i < a.length(); i++)
 			{
 				JSONArray o = a.getJSONArray(i);
 				if (o.getString(0).length() == 0 || FlagManager.matchesFlags(o.getString(0)))
 				{
 					activeText = o.getString(2);
 					String modifiers = o.getString(1);
 					if (modifiers.contains("%skip"))
 					{
 						next();
 						return;
 					}
 					if (modifiers.contains("%e")) activeName = source.getMeta().getString("name");
 					else activeName = "";
 					if (modifiers.contains("%q"))
 					{
 						String s = modifiers.substring(modifiers.indexOf("%q_") + "%q_".length());
 						s = s.substring(0, s.indexOf("%") > -1 ? s.indexOf("%") : s.length());
 						String[] quests = s.split(",");
 						questTriggers = new int[quests.length];
 						for (int j = 0; j < quests.length; j++)
 							questTriggers[j] = Integer.parseInt(quests[j]);
 					}
 					else questTriggers = new int[] {};
 					
 					source.onNextTalk(activeTalk, new Talk(i, index, o.getString(0), o.getString(1), o.getString(2)));
 					activeTalk = new Talk(i, index, o.getString(0), o.getString(1), o.getString(2));
 					return;
 				}
 			}
 			
 			endTalk();
 		}
 		catch (JSONException e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
