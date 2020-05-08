 import java.awt.*;
 import java.util.*;
 import java.lang.Math;
 import java.util.List;
 
 class Battle
 {
 	private static int NUMMONSTERS = 10;
 	private static int NUMBOSSES = 1;
 	private static int TICKERLENGTH = 4;
 	private SoundClip hit;
 	private SoundClip death;
 	private SoundClip battlemusic;
 	private SoundClip bossmusic;
 	private Monster m;
 	private Pointer c;
 	
 	private List<String> ticker;
 	
 	private Image[] monsterImages;
 	private Image[] icons;
 	private Player p;
 	private Random rand = new Random(); 
  	private Font title = new Font("DialogInput",Font.PLAIN,20);
 	
 	private boolean battle=true;
 	private boolean initialize = false;
 	private boolean mdefended = false;
 	private boolean pdefended = false;
 	private boolean initemmenu = false;
 	private boolean inspellmenu = false;
 	private int xp;
 	private int gold;
 	private int boss;
 	private int xchoice=1;
 	private int ychoice=-1;
 	private int spelltracker;
 	HUD hud;
 	
 
 	private Utils u = new Utils();
 	
 	private boolean key_space=false;
 	private boolean buffer=false;
 	private int pdamagedealt;
   	private int mdamagedealt;
   	private boolean levelup = false;
   	private boolean playernotgone = true;
   	Spell[] spell;
 	
     public Battle(Player pl, Image[] mi, Pointer cl,Image[] icons,SoundClip hit,SoundClip death,SoundClip battlemusic,SoundClip bossmusic,int boss, Spell[] spell) {
 		System.out.println("made Battle");
 		this.p = pl;
 		this.c = cl;
 		this.monsterImages = mi;
 		this.hit = hit;
 		this.death = death;
 		this.battlemusic = battlemusic;
 		this.icons = icons;
 		this.bossmusic=bossmusic;
 		this.boss=boss;
 		this.spell = spell;
 		this.ticker = new ArrayList<String>();
 		
 	}
 	public boolean getBattle() { return battle; }
 	public boolean getSpace() { return key_space; }
 	
 	public void pushToTicker(String s) {
 		ticker.add(s);
 		if(ticker.size() > TICKERLENGTH) ticker.remove(0);	
 	}
 	
 	public boolean BattleSequence(Graphics g, boolean key_space)
 	{
 		this.key_space = key_space;
 		if (battle)
 		{
 			if (!initialize)
 			{
 				Initialize(g);
 			}
 			if (m.getHealth()>=0&&p.getHealth()>=0)
 				ShowPane(g);
 			if (m.getHealth()<=0)
 			{
 				if (buffer==false)
 				{
 					key_space=false;
 					buffer=true;
 					xp=((m.getHealthMax() + m.getDamage() + m.getDefense() + m.getSpeed() + m.getMana())*3/2 - 
 						(p.getHealthMax() + p.getDamage() + p.getDefense() + p.getSpeed() + p.getMana()))*(p.getLevel()/3+1);	
 					if (xp<2*p.getLevel())
 						xp=2*p.getLevel();
 					gold=m.getId()+(rand.nextInt(4)+1)*(p.getLevel()*m.getStrength())/(p.getLevel());
 					p.addGold(gold);
 					if (p.setExperience(xp))
 						levelup=true;
 				}
 				Victory(g);
				if (key_space)
 				{
 					battle=false;
 					initialize = false;
 				}
 					
 			}
 			if (p.getHealth()<=0)
 			{
 				if (buffer==false)
 				{
 					key_space=false;
 					buffer=true;
 				
 					xp=-(p.getExperience()/2);
 					//gold=-(p.getGold()/2);
 					//p.setGold(gold);
 					p.addGold(-(int)(p.getGold()/2));
 					if (p.setExperience(xp))
 						pushToTicker("Congratulations! You are now level "+p.getLevel()+".");	
 				}
 				battlemusic.stop();
 				death.play();
 				Defeat(g);
				if (key_space)
 				{
 					battle=false;
 					initialize = false;	
 					p.setHealth(p.getHealthMax());
 					/*for (int i=0; i<NUMSPRITES; i++)
 					{
 						sp[i].reset();
 					}	*/
 					
 					p.setX(p.getTownX());
 					p.setY(p.getTownY());
 				}
 					
 			}
 		}	
 			
 		return battle;
 			
 	}
 	public void Display(Graphics g)
 	{
 		g.setFont(title);
 		g.setColor(Color.white);
 		drawMonster(g);
 		//g.drawString(m.getName(),(int)(800/2-m.getName().length()*15/2),30+100);
 		u.drawCenteredString(g, m.getName(), 800/2, 30+100+40);
   		//g.drawString("monster health = "+ m.getHealth(),300,50+100);
   		//g.drawString("strength = "+ m.getStrength(),300,71+100);
   		//g.drawString("defense = "+ m.getDefense(),300,91+100);
 		
   		//g.drawString("your health = "+ p.getHealth(),30,50);
   		//g.drawString("strength = "+ p.getStrength(),30,71+100+100);
   		//g.drawString("defense = "+ p.getDefense(),30,91+100);
 		
 		hud.battleDraw(g);
 		hud.drawTicker(g,ticker);
 	}
 	public void Defeat(Graphics g)
 	{
 		m.resetDamage();
 		p.resetDamage();
 		g.setColor(Color.black);
 		g.fillRect(0,0,800,600);
 		Display(g);
 		g.setFont(title);
 		g.setColor(Color.white);
   		pushToTicker("You were killed by the "+m.getName()+"!");
   		pushToTicker("You lost " + xp + " experience and " + gold + " gold.");
   		pushToTicker("You take some time to tend to your wounds.");
   		
 	}
 	public void Victory(Graphics g)
 	{
 		m.resetDamage();
 		p.resetDamage();
 		g.setColor(Color.black);
 		g.fillRect(0,0,800,600);
 		Display(g);
 		g.setFont(title);
 		g.setColor(Color.white);
   		pushToTicker("Congratulations on defeating the "+m.getName()+"!");
 		pushToTicker("Gained "+xp+" experience and " + gold + " gold.");
   		if (levelup)
 			pushToTicker("Congratulations! You are now level "+p.getLevel()+".");	
 	}
 	public void drawMonster(Graphics g)
 	{	
 		u.drawCenteredImage(g,monsterImages[m.getId()],400,250);
 	}
 	public void Initialize(Graphics g)
 	{
 		death.stop();
 		if (boss==0)
 		{
 			battlemusic.play();
 			g.setFont(title);
 			key_space=false;
 			int monster_id = rand.nextInt(NUMMONSTERS-NUMBOSSES);
 			double stat_scale = (p.getLevel() / 4.0 + 1+(p.getX()/80)+(p.getY()/80));
 			
 			m = new Monster(monster_id, stat_scale);	
 		}
 		else if (boss==1)
 		{
 			bossmusic.play();
 			g.setFont(title);
 			key_space=false;
 			int monster_id = 9;
 			double stat_scale = 1D;
 			
 			m = new Monster(monster_id, stat_scale);	
 		}
 		
 		hud = new HUD(p, m, icons,spell);
 		 c.reset();
 		 initialize = true;
 		mdefended=false;
 		 
 	}
 	public void ShowPane(Graphics g)
 	{
 		m.resetDamage();
 		p.resetDamage();
 		g.setFont(title);
 		g.setColor(Color.black);
 		g.fillRect(0,0,800,600);
 		g.setColor(Color.white);
 		drawMonster(g);
  		if (!initemmenu)
   		{
   		
 			switch(c.getPointer()) {
 				case 0:
 					xchoice=1; 
 					if (ychoice>-1)
 						ychoice--;
 						break;
 				case 1: 
 					if (xchoice>0)
 						xchoice--;
 						break;
 				case 2: 
 					xchoice=1; 
 					if (ychoice<1)
 						ychoice++;
 						break;
 				case 3: 
 					if (xchoice<2)
 						xchoice++;
 						break;
			}
 			Actions(g);
 			Display(g);
 			g.setColor(Color.red);
 			switch(xchoice) {
 				case 0: 
 					u.drawCenteredRoundRect(g, 800/2-90,400+35, 90,35, 10);
 					//todo - y needed to be shifted by -22 ... why?
 					//spell
 					break;
 				case 1: 
 					u.drawCenteredRoundRect(g, 800/2,400+35*(ychoice+1), 90,35, 10);
 					//attack, item, run (top to bottom)
 					System.out.println(ychoice);
 					break;
 				case 2: 
 					u.drawCenteredRoundRect(g, 800/2+90,400+35, 90,35, 10);
 					//defend
 					break;
 			}
  		}
 	}
 	
 	
 	public void Actions(Graphics g)
 	{		
 		u.drawCenteredString(g, "Attack", 800/2,400);
   		u.drawCenteredString(g, "Spell", 800/2-90,400+35);
   		u.drawCenteredString(g, "Item", 800/2,400+35);
   		u.drawCenteredString(g, "Defend", 800/2+90,400+35);
   		u.drawCenteredString(g, "Run", 800/2,400+35*2);
 		
   		hit.stop();
   		System.out.println(c.getPointer());
   		if (initemmenu)
 		{
 			initemmenu=!hud.drawItems(g,c,key_space);
 			playernotgone=initemmenu;
 			key_space=false;
 			xchoice=-1;	
 		}
   		if (inspellmenu)
 		{
 			spelltracker=hud.drawSpells(g,c,key_space);
 			if(spelltracker>=0)
 				{
 				useSpell(spelltracker,g);
 				mdamagedealt=Math.max(0,(p.getDamage()-m.getDefense()));
 				m.setDamage(mdamagedealt); 
 				playernotgone=false;
 				inspellmenu=false;
 				key_space=false;
 				xchoice=-1;		
 				}
 			
 		}
 		if (key_space&&xchoice!=-1&&playernotgone)
 		{	
 			switch(xchoice) {
 			case 0: 
 				hit.play();
 				if (inspellmenu==false)
 					key_space=false;	
 					if (hud.drawSpells(g,c,key_space)==-1)
 					{
 					inspellmenu = true;	
 					}
 					else
 					inspellmenu = false;
 					xchoice=-1;	
 					break;
 			case 1:
 				xchoice=-1;	
 				switch(ychoice) {
 				case -1:
 					xchoice=-1;
 					hit.play();
 					p.attack();
 					pushToTicker("Attacked: "+ Math.max(0,(p.getDamage()-m.getDefense()))+" damage!");
 					mdamagedealt=Math.max(0,(p.getDamage()-m.getDefense()));
 	  				m.setDamage(mdamagedealt);
 	  				 	 
 						break; 
 				case 0:
 					//hit.play();
 					xchoice=-1;
 					if (initemmenu==false)
 					key_space=false;	
 					if (!hud.drawItems(g,c,key_space))
 					{
 					initemmenu = true;	
 					}
 					else
 					initemmenu = false;
 					pushToTicker("Item used: "+ (p.getDamage()-m.getDefense())+" damage!");
 					mdamagedealt=Math.max(0,(p.getDamage()-m.getDefense()));
 	  				m.setDamage(mdamagedealt);
 	  						 
 						break; 
 				case 1:
 					xchoice=-1;	
 					if (rand.nextInt(2)==0)
 					battle=false;
 					else
 					pushToTicker("NO ESCAPE!");
 					
 						break;
 				}
 				break;	
 			case 2:
 				xchoice=-1; 
 				p.defend(); 
 				pushToTicker("Defending! You braced yourself!"); 
 					pdefended=true;
 						
 					break;
 			}
 			key_space=false;
 			if (!initemmenu&&!inspellmenu)
 			playernotgone=false;
 			//if (m.getHealth()>0)
 				//pushToTicker("The "+m.getName()+" is ready!"); 
 		}
 		
 		if (key_space&&!playernotgone&&m.getHealth()>0&&!initemmenu&&!inspellmenu)
 		{
 			playernotgone=true;
 			key_space=false;
 			c.setPointer(5);
 			xchoice=-1; 
 			if (mdefended)
 			{
 				m.setDefense(m.getDefense()/2);
 				mdefended=false;	
 			}
 			int monstaction=rand.nextInt(4);
 			if (monstaction==0)
 			{
 				hit.play();
 				m.attack();
 				pushToTicker("Attack! "+m.getName()+" dealt "+ m.getDamage()+" damage!");
 				pdamagedealt=Math.max(0,(m.getDamage()-p.getDefense()));
   				p.setDamage(pdamagedealt);	
 				}
 				
 			if (monstaction==1)
 			{
 				hit.play();
 				m.spell();
 				pushToTicker("Spell! "+m.getName()+" dealt "+ m.getDamage()+" damage!");	
 				pdamagedealt=Math.max(0,(m.getDamage()-p.getDefense()));
   				p.setDamage(pdamagedealt);	
 				
 			}
 			if (monstaction==2)
 			{
 				m.defend();
 				pushToTicker("Defending! Enemy has braced itself!");	
 					mdefended=true;
 			}
 			if (monstaction==3)
 			{
 				pushToTicker(m.getName() + " tried to run!");
 				if(m.canRun())
 					battle=false;
 			}
 			if (pdefended)
 			{
 				p.setDefense(p.getDefense()/2);
 				pdefended=false;	
 			}
 		
 		c.setPointer(5);
 		}
 		key_space=false;	 
 	}
 	public void useSpell(int spellid,Graphics g)
 	{
 		if(p.getMana()>=spell[spellid].getCost())
 		{
 		p.payMana(spell[spellid].getCost());
 		switch(spell[spellid].getTarget()){
 			case 0:
 				m.setDamage(spell[spellid].getDamage());
 				break;	
 			case 1:
 				p.setDamage(spell[spellid].getDamage());
 				break;
 		}
 		g.setColor(Color.white);
 		//g.drawString("You dealt: "+ (p.getDamage()-m.getDefense())+" damage!",50,500);
 		pushToTicker("Used "+spell[spellid].getName()+" and dealt "+spell[spellid].getDamage()+" damage.");	
 		}
 		else
 		pushToTicker(spell[spellid].getName()+" failed!");	
 		
 		System.out.println("mana: " + p.getMana() + "\tmanamax: " + p.getManaMax());
 		
 			
 	}
 	public void delay(double n)
 	{
 		long startDelay = System.currentTimeMillis();
 		long endDelay = 0;
 		while (endDelay - startDelay < n)
 			endDelay = System.currentTimeMillis();	
 	}
 		
 }
