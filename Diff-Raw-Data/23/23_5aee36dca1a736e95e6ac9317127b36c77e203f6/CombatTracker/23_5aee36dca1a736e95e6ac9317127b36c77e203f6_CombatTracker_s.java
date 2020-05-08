 /*
 	Title: CombatTracker (DiceBag Add-on)
 	Author: Matthew Boyette
 	Date: 2/19/2014
 	
 	This class is an add-on which allows a DM to track combat information like health, initiative, and the current round.
 	It also interfaces with the DiceBag class so that initiative die rolls are recorded in the log automatically.
 */
 
 import api.gui.*;
 import api.util.*;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.util.Collections;
 import java.util.LinkedList;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 public class CombatTracker
 {
 	public static class Creature implements Comparable<Creature>
 	{
 		private int		curHealth	= 0;
 		private DiceBag	diceRoller	= null;
 		private int		initBase	= 0;
 		private int		initBonus	= 0;
 		private int		maxHealth	= 0;
 		private String	name		= null;
 		private String	position	= null;
 		private int		tieBreaker	= 0;
 		private int		totalInit	= 0;
 		
 		public Creature(final DiceBag diceBag, final String name, final int curHealth, final int initBase, final int initBonus, final int maxHealth, final String position)
 		{
 			this.setDiceRoller(diceBag);
 			this.setCurHealth(curHealth);
 			this.setInitBase(initBase);
 			this.setInitBonus(initBonus);
 			this.setMaxHealth(maxHealth);
 			this.setName(name);
 			this.setPosition(position);
 			this.setTotalInit(this.getInitBase() + this.getInitBonus());
 		}
 		
 		public Creature(final DiceBag diceBag, final String name, final int initBase, final int initBonus, final int maxHealth, final String position)
 		{
 			this.setDiceRoller(diceBag);
 			this.setCurHealth(maxHealth);
 			this.setInitBase(initBase);
 			this.setInitBonus(initBonus);
 			this.setMaxHealth(maxHealth);
 			this.setName(name);
 			this.setPosition(position);
 			this.setTotalInit(this.getInitBase() + this.getInitBonus());
 		}
 		
 		@Override
 		// Implements initiative as the natural ordering mechanism for the Creature class.
 		// See d20 SRD Initiative rules: http://www.d20srd.org/srd/combat/initiative.htm
 		public int compareTo(final Creature creature)
 		{
 			if (creature.getTotalInit() == this.getTotalInit())
 			{
 				if (creature.getInitBonus() == this.getInitBonus())
 				{
 					do
 					{
 						creature.getDiceRoller().getOutput().append(Color.BLACK, Color.WHITE, "\t\t\t   Rolling tie-breaker for " + creature.getName() + "...\n");
 						creature.setTieBreaker(this.getDiceRoller().processInput("1d20"));
 						this.getDiceRoller().getOutput().append(Color.BLACK, Color.WHITE, "\t\t\t   Rolling tie-breaker for " + this.getName() + "...\n");
 						this.setTieBreaker(this.getDiceRoller().processInput("1d20"));
 					}
 					while (creature.getTieBreaker() == this.getTieBreaker());
 					
 					return (creature.getTieBreaker() - this.getTieBreaker());
 				}
 				else
 				{
 					return (creature.getInitBonus() - this.getInitBonus());
 				}
 			}
 			else
 			{
 				return (creature.getTotalInit() - this.getTotalInit());
 			}
 		}
 		
 		public void damage(final int amount)
 		{
 			this.setCurHealth(this.getCurHealth() - amount);
 		}
 		
 		/*
 			@see java.lang.Object#equals(java.lang.Object)
 		*/
 		@Override
 		public boolean equals(Object obj)
 		{
 			if (this == obj)
 			{
 				return true;
 			}
 			if (obj == null)
 			{
 				return false;
 			}
 			if (!(obj instanceof Creature))
 			{
 				return false;
 			}
 			
 			Creature other = (Creature)obj;
 			
 			if (this.curHealth != other.curHealth)
 			{
 				return false;
 			}
 			if (this.diceRoller == null)
 			{
 				if (other.diceRoller != null)
 				{
 					return false;
 				}
 			}
 			else
 			{
 				if (!this.diceRoller.equals(other.diceRoller))
 				{
 					return false;
 				}
 			}
 			if (this.initBase != other.initBase)
 			{
 				return false;
 			}
 			if (this.initBonus != other.initBonus)
 			{
 				return false;
 			}
 			if (this.maxHealth != other.maxHealth)
 			{
 				return false;
 			}
 			if (this.name == null)
 			{
 				if (other.name != null)
 				{
 					return false;
 				}
 			}
 			else
 			{
 				if (!this.name.equals(other.name))
 				{
 					return false;
 				}
 			}
 			if (this.position == null)
 			{
 				if (other.position != null)
 				{
 					return false;
 				}
 			}
 			else
 			{
 				if (!this.position.equals(other.position))
 				{
 					return false;
 				}
 			}
 			if (this.tieBreaker != other.tieBreaker)
 			{
 				return false;
 			}
 			if (this.totalInit != other.totalInit)
 			{
 				return false;
 			}
 			
 			return true;
 		}
 
 		public final int getCurHealth()
 		{
 			return this.curHealth;
 		}
 		
 		public final DiceBag getDiceRoller()
 		{
 			return this.diceRoller;
 		}
 		
 		public final int getInitBase()
 		{
 			return this.initBase;
 		}
 		
 		public final int getInitBonus()
 		{
 			return this.initBonus;
 		}
 		
 		public final int getMaxHealth()
 		{
 			return this.maxHealth;
 		}
 		
 		public final String getName()
 		{
 			return this.name;
 		}
 		
 		public final String getPosition()
 		{
 			return this.position;
 		}
 		
 		public final int getTieBreaker()
 		{
 			return this.tieBreaker;
 		}
 		
 		public final int getTotalInit()
 		{
 			return this.totalInit;
 		}
 		
 		/*
 			@see java.lang.Object#hashCode()
 		*/
 		@Override
 		public int hashCode()
 		{
 			final int prime = 257; // Always prime. Fermat/Pythagorean prime, of the forms (2^(2^3) + 1) and (4n + 1).
 			int result = 89;       // Initially prime.
 			
 			// Use multiplication and XOR to create a high entropy, low collision hash for each object.
 			result = (prime * result) ^ this.curHealth;
 			result = (prime * result) ^ ((this.diceRoller == null) ? 0 : this.diceRoller.hashCode());
 			result = (prime * result) ^ this.initBase;
 			result = (prime * result) ^ this.initBonus;
 			result = (prime * result) ^ this.maxHealth;
 			result = (prime * result) ^ ((this.name == null) ? 0 : this.name.hashCode());
 			result = (prime * result) ^ ((this.position == null) ? 0 : this.position.hashCode());
 			result = (prime * result) ^ this.tieBreaker;
 			result = (prime * result) ^ this.totalInit;
 			
 			return result;
 		}
 		
 		public void heal(final int amount)
 		{
 			this.setCurHealth(this.getCurHealth() + amount);
 		}
 		
 		public final void setCurHealth(final int curHealth)
 		{
 			this.curHealth = curHealth;
 		}
 		
 		public final void setDiceRoller(final DiceBag diceRoller)
 		{
 			this.diceRoller = diceRoller;
 		}
 		
 		public final void setInitBase(final int initBase)
 		{
 			this.initBase = initBase;
 		}
 		
 		public final void setInitBonus(final int initBonus)
 		{
 			this.initBonus = initBonus;
 		}
 		
 		public final void setMaxHealth(final int maxHealth)
 		{
 			this.maxHealth = maxHealth;
 		}
 		
 		public final void setName(final String name)
 		{
 			this.name = name;
 		}
 		
 		public final void setPosition(final String position)
 		{
 			this.position = position;
 		}
 		
 		public final void setTieBreaker(final int tieBreaker)
 		{
 			this.tieBreaker = tieBreaker;
 		}
 		
 		public final void setTotalInit(final int totalInit)
 		{
 			this.totalInit = totalInit;
 		}
 		
 		@Override
 		public String toString()
 		{
 			String s;
 			
 			s = "{Name: " + "[" + this.getName() + "]" + " Health: " + "[" + this.getCurHealth() + "/" + this.getMaxHealth() + "]" + " Initiative: " +
 				"[" + this.getTotalInit() + "]" + " Position: " + "[" + this.getPosition() + "]}";
 			
 			return s;
 		}
 	}
 	
 	public final static Font		textFont			= new Font("Lucida Console", Font.PLAIN, 14);
 	public final static String		windowTitle			= "Combat Tracker" + " - " + "Round: ";
 	
 	private JComboBox<Creature>		cboCreatureList		= null;
 	private LinkedList<Creature>	creatureList		= null;
 	private Creature				curCreature			= null;
 	private int						curCreatureIndex	= 0;
 	private boolean					isDebugging			= false;
 	private JLabel					lblCurrentCreature	= null;
 	private int						numEnemyTypes		= 0;
 	private int						numPlayers			= 0;
 	private int						numRounds			= 0;
 	private DiceBag					parent				= null;
 	private ApplicationWindow		window				= null;
 	
 	public CombatTracker(final DiceBag parent, final boolean isDebugging)
 	{
 		this.setParent(parent);
 		this.setDebugging(isDebugging);
 		this.reset();
 		
 		// Define a self-contained ActionListener event handler.
 		EventHandler myActionPerformed = new EventHandler(this)
 		{
 			@Override
 			public final void run(final Object... arguments) throws IllegalArgumentException, RuntimeException
 			{
 				if ((arguments.length <= 1) || (arguments.length > 2))
 				{
 					throw new IllegalArgumentException("myActionPerformed Error : incorrect number of arguments.");
 				}
 				else if (!(arguments[0] instanceof ActionEvent))
 				{
 					throw new IllegalArgumentException("myActionPerformed Error : argument[0] is of incorrect type.");
 				}
 				
 				ActionEvent event = (ActionEvent)arguments[0];
 				Creature creature = null;
 				
 				if (((CombatTracker)this.parent).getCboCreatureList() != null)
 				{
 					creature = (Creature)((CombatTracker)this.parent).getCboCreatureList().getSelectedItem();
 				}
 				
 				/*
 					JDK 7 allows string objects as the expression in a switch statement.
 					This generally produces more efficient byte code compared to a chain of if statements.
 					http://docs.oracle.com/javase/7/docs/technotes/guides/language/strings-switch.html
 				*/
 				switch (event.getActionCommand())
 				{
 					case "Damage":
 						
 						if (creature != null)
 						{
 							String amount;
 							
 							do
 							{
								amount = ((CombatTracker)this.parent).getInputString("How much? Enter zero to cancel.", "Damage " + creature.getName());
 							}
 							while (Support.isStringParsedAsInteger(amount) != true);
 							
 							creature.setCurHealth(creature.getCurHealth() - Integer.parseInt(amount));
 							
 							if (creature.getCurHealth() < 1)
 							{
 								((CombatTracker)this.parent).getCreatureList().remove(creature);
 							}
 							
 							((CombatTracker)this.parent).getWindow().reDrawGUI();
 						}
 						break;
 						
 					case "Heal":
 						
 						if (creature != null)
 						{
 							String amount;
 							
 							do
 							{
								amount = ((CombatTracker)this.parent).getInputString("How much? Enter zero to cancel.", "Heal " + creature.getName());
 							}
 							while (Support.isStringParsedAsInteger(amount) != true);
 							
 							creature.setCurHealth(creature.getCurHealth() + Integer.parseInt(amount));
 							
 							if (creature.getCurHealth() > creature.getMaxHealth())
 							{
 								creature.setCurHealth(creature.getMaxHealth());
 							}
 							
 							((CombatTracker)this.parent).getWindow().reDrawGUI();
 						}
 						break;
 						
 					case "Move":
 						
 						if (creature != null)
 						{
 							String position;
 							
 							do
 							{
								position = ((CombatTracker)this.parent).getInputString("Where to? Prompt expects X,Y coordinates." +
 																					   "\nEnter " + creature.getPosition() + " to cancel.",
 																					   "Move " + creature.getName());
 							}
							while (!position.matches("[0-9]+,[0-9]+"));
 							
 							creature.setPosition(position);
 							
 							((CombatTracker)this.parent).getWindow().reDrawGUI();
 						}
 						break;
 						
 					case "Next":
 						
 						((CombatTracker)this.parent).nextCombatant();
 						break;
 						
 					case "Reset":
 						
 						((CombatTracker)this.parent).reset();
 						((CombatTracker)this.parent).getWindow().reDrawGUI();
 						break;
 						
 					default:
 						
 						break;
 						
 				}
 			}
 		};
 		
 		// Define a self-contained interface construction event handler.
 		EventHandler myDrawGUI = new EventHandler(this)
 		{
 			@Override
 			public final void run(final Object... arguments) throws IllegalArgumentException
 			{
 				if (arguments.length <= 0)
 				{
 					throw new IllegalArgumentException("myDrawGUI Error : incorrect number of arguments.");
 				}
 				else if (!(arguments[0] instanceof ApplicationWindow))
 				{
 					throw new IllegalArgumentException("myDrawGUI Error : argument[0] is of incorrect type.");
 				}
 				
 				ApplicationWindow window = (ApplicationWindow)arguments[0];
 				JButton btnDamage = new JButton("Damage");
 				JButton btnHeal = new JButton("Heal");
 				JButton btnMove = new JButton("Move");
 				JButton btnNext = new JButton("Next");
 				JButton btnReset = new JButton("Reset");
 				JPanel buttonPanel = new JPanel();
 				JPanel cboPanel = new JPanel();
 				CombatTracker combatTracker = (CombatTracker)this.parent;
 				Container contentPane = window.getContentPane();
 				JPanel curPanel = new JPanel();
 				
 				btnDamage.setFont(CombatTracker.textFont);
 				btnDamage.addActionListener(window);
 				btnHeal.setFont(CombatTracker.textFont);
 				btnHeal.addActionListener(window);
 				btnMove.setFont(CombatTracker.textFont);
 				btnMove.addActionListener(window);
 				btnNext.setFont(CombatTracker.textFont);
 				btnNext.addActionListener(window);
 				btnReset.setFont(CombatTracker.textFont);
 				btnReset.addActionListener(window);
 				buttonPanel.setLayout(new FlowLayout());
 				buttonPanel.add(btnDamage);
 				buttonPanel.add(btnHeal);
 				buttonPanel.add(btnMove);
 				combatTracker.setCboCreatureList(new JComboBox<Creature>());
 				combatTracker.getCboCreatureList().setFont(CombatTracker.textFont);
 				combatTracker.getCboCreatureList().setEditable(false);
 				combatTracker.setLblCurrentCreature(new JLabel("Current: " + combatTracker.getCurrentCreature().toString()));
 				combatTracker.getLblCurrentCreature().setFont(CombatTracker.textFont);
 				cboPanel.setLayout(new FlowLayout());
 				cboPanel.add(combatTracker.getCboCreatureList());
 				curPanel.setLayout(new FlowLayout());
 				curPanel.add(combatTracker.getLblCurrentCreature());
 				curPanel.add(btnNext);
 				curPanel.add(btnReset);
 				contentPane.setLayout(new BorderLayout());
 				contentPane.add(curPanel, BorderLayout.NORTH);
 				contentPane.add(cboPanel, BorderLayout.CENTER);
 				contentPane.add(buttonPanel, BorderLayout.SOUTH);
 				
 				for (int i = 0; i < combatTracker.getCreatureList().size(); i++)
 				{
 					combatTracker.getCboCreatureList().addItem(combatTracker.getCreatureList().get(i));
 				}
 				
 				combatTracker.getCboCreatureList().setSelectedIndex(combatTracker.getCreatureList().indexOf(combatTracker.getCurrentCreature()));
 				window.setTitle(CombatTracker.windowTitle + combatTracker.getNumRounds());
 			}
 		};
 		
 		this.setWindow(new ApplicationWindow(this.getParent().getWindow(), CombatTracker.windowTitle + this.getNumRounds(),
 			new Dimension(1000, 120), this.isDebugging(), false, myActionPerformed, myDrawGUI));
 		this.getWindow().setIconImageByResourceName("icon.png");
 	}
 	
 	public final JComboBox<Creature> getCboCreatureList()
 	{
 		return this.cboCreatureList;
 	}
 	
 	public final ApplicationWindow getWindow()
 	{
 		return this.window;
 	}
 	
 	public final LinkedList<Creature> getCreatureList()
 	{
 		return this.creatureList;
 	}
 	
 	public final Creature getCurrentCreature()
 	{
 		return this.curCreature;
 	}
 	
 	public final int getCurCreatureIndex()
 	{
 		return this.curCreatureIndex;
 	}
 	
 	public final String getInputString(final String message, final String title)
 	{
 		ApplicationWindow windowHandle = null;
 		String s = null;
 		
 		if (this.getWindow() != null)
 		{
 			windowHandle = this.getWindow();
 		}
 		else
 		{
 			windowHandle = this.getParent().getWindow();
 		}
 		
 		do
 		{
 			s = JOptionPane.showInputDialog(windowHandle, message, title, JOptionPane.INFORMATION_MESSAGE);
 		}
 		while ((s == null) || s.isEmpty());
 		
 		return s;
 	}
 	
 	public final JLabel getLblCurrentCreature()
 	{
 		return this.lblCurrentCreature;
 	}
 	
 	public final int getNumEnemyTypes()
 	{
 		return this.numEnemyTypes;
 	}
 	
 	public final int getNumPlayers()
 	{
 		return this.numPlayers;
 	}
 	
 	public final int getNumRounds()
 	{
 		return this.numRounds;
 	}
 	
 	public final DiceBag getParent()
 	{
 		return this.parent;
 	}
 	
 	public final boolean isDebugging()
 	{
 		return this.isDebugging;
 	}
 	
 	public void nextCombatant()
 	{
 		int index;
 		
 		if (this.getCreatureList().indexOf(this.getCurrentCreature()) == -1)
 		{
 			index = this.getCurCreatureIndex();
 		}
 		else
 		{
 			index = (this.getCurCreatureIndex() + 1);
 		}
 		
 		if (index >= this.getCreatureList().size())
 		{
 			this.setNumRounds(this.getNumRounds() + 1);
 			index = 0;
 		}
 		
 		this.setCurCreatureIndex(index);
 		this.setCurrentCreature(this.getCreatureList().get(index));
 		this.getWindow().reDrawGUI();
 	}
 	
 	public void reset()
 	{
 		this.setNumRounds(1);
 		this.setCreatureList(new LinkedList<Creature>());
 		
 		String s = null;
 		
 		do
 		{
 			s = this.getInputString("How many players are there?", "Combat Setup");
 		}
 		while (Support.isStringParsedAsInteger(s) != true);
 		
 		this.setNumPlayers(Integer.parseInt(s));
 		
 		do
 		{
 			s = this.getInputString("How many enemy types are there?", "Combat Setup");
 		}
 		while (Support.isStringParsedAsInteger(s) != true);
 		
 		this.setNumEnemyTypes(Integer.parseInt(s));
 		
 		for (int i = 1; i <= this.getNumPlayers(); i++)
 		{
 			String name = this.getInputString("What is player " + i + "'s character name?", "Combat Setup");
 			
 			do
 			{
 				s = this.getInputString("What is player " + i + "'s current health?", "Combat Setup");
 			}
 			while (Support.isStringParsedAsInteger(s) != true);
 			
 			int curHealth = Integer.parseInt(s);
 			
 			do
 			{
 				s = this.getInputString("What is player " + i + "'s maximum health?", "Combat Setup");
 			}
 			while (Support.isStringParsedAsInteger(s) != true);
 			
 			int maxHealth = Integer.parseInt(s);
 			
 			do
 			{
 				s = this.getInputString("What is player " + i + "'s initiative modifier?", "Combat Setup");
 			}
 			while (Support.isStringParsedAsInteger(s) != true);
 			
 			int initBonus = Integer.parseInt(s);
 			
 			this.parent.getOutput().append(Color.BLACK, Color.WHITE, "\t\t\t   Rolling initiative for " + name + "...\n");
 			int initBase = this.parent.processInput("1d20");
 			
 			String position;
 			
 			do
 			{
 				position = this.getInputString("What is player " + i + "'s battle grid position?" +
					   						   "\nPrompt expects X,Y coordinates.", "Combat Setup");
 			}
			while (!position.matches("[0-9]+,[0-9]+"));
 			
 			Creature player = new Creature(this.getParent(), name, curHealth, initBase, initBonus, maxHealth, position);
 			this.getCreatureList().add(player);
 		}
 		
 		for (int i = 1; i <= this.getNumEnemyTypes(); i++)
 		{
 			do
 			{
 				s = this.getInputString("How many enemies are there of type " + i + "?", "Combat Setup");
 			}
 			while (Support.isStringParsedAsInteger(s) != true);
 			
 			int numEnemies = Integer.parseInt(s);
 			
 			String name = this.getInputString("What is enemy type " + i + "'s name?", "Combat Setup");
 			
 			do
 			{
 				s = this.getInputString("What is enemy type " + i + "'s maximum health?", "Combat Setup");
 			}
 			while (Support.isStringParsedAsInteger(s) != true);
 			
 			int maxHealth = Integer.parseInt(s);
 			
 			do
 			{
 				s = this.getInputString("What is enemy type " + i + "'s initiative modifier?", "Combat Setup");
 			}
 			while (Support.isStringParsedAsInteger(s) != true);
 			
 			int initBonus = Integer.parseInt(s);
 			
 			for (int j = 1; j <= numEnemies; j++)
 			{
 				String position;
 				
 				do
 				{
 					position = this.getInputString("What is " + name + " " + j + "'s battle grid position?" +
												   "\nPrompt expects X,Y coordinates.", "Combat Setup");
 				}
				while (!position.matches("[0-9]+,[0-9]+"));
 				
 				this.parent.getOutput().append(Color.BLACK, Color.WHITE, "\t\t\t   Rolling initiative for " + name + " " + j + "...\n");
 				int initBase = this.parent.processInput("1d20");
 				Creature enemy = new Creature(this.getParent(), name + " " + j, initBase, initBonus, maxHealth, position);
 				this.getCreatureList().add(enemy);
 			}
 		}
 		
 		Collections.sort(this.getCreatureList());
 		this.setCurrentCreature(this.getCreatureList().getFirst());
 		this.setCurCreatureIndex(this.getCreatureList().indexOf(this.getCurrentCreature()));
 	}
 	
 	public final void setCboCreatureList(final JComboBox<Creature> cboCreatureList)
 	{
 		this.cboCreatureList = cboCreatureList;
 	}
 	
 	public final void setWindow(final ApplicationWindow window)
 	{
 		this.window = window;
 	}
 	
 	public final void setCreatureList(final LinkedList<Creature> creatureList)
 	{
 		this.creatureList = creatureList;
 	}
 	
 	public final void setCurrentCreature(final Creature creature)
 	{
 		this.curCreature = creature;
 	}
 	
 	public final void setCurCreatureIndex(final int curCreatureIndex)
 	{
 		this.curCreatureIndex = curCreatureIndex;
 	}
 	
 	public final void setDebugging(final boolean isDebugging)
 	{
 		this.isDebugging = isDebugging;
 	}
 	
 	public final void setLblCurrentCreature(final JLabel lblCurrentCreature)
 	{
 		this.lblCurrentCreature = lblCurrentCreature;
 	}
 	
 	public final void setNumEnemyTypes(final int numEnemyTypes)
 	{
 		this.numEnemyTypes = numEnemyTypes;
 	}
 	
 	public final void setNumPlayers(final int numPlayers)
 	{
 		this.numPlayers = numPlayers;
 	}
 	
 	public final void setNumRounds(final int numRounds)
 	{
 		this.numRounds = numRounds;
 	}
 	
 	public final void setParent(final DiceBag parent)
 	{
 		this.parent = parent;
 	}
 }
