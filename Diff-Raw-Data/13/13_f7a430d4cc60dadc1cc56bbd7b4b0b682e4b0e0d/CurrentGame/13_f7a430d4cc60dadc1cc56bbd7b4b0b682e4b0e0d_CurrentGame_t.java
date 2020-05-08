 package View;
 
 
 import java.io.File;
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.gui.MouseOverArea;
 import org.newdawn.slick.particles.ConfigurableEmitter;
 import org.newdawn.slick.particles.ParticleIO;
 import org.newdawn.slick.particles.ParticleSystem;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 import Controller.Controller;
 
 
 
 
 public class CurrentGame extends BasicGameState
 {
 
 	//true pr le joueur1
 	private boolean TourView;
 
 	private ArrayList<MouseOverArea> Echequier = new ArrayList<MouseOverArea>();
 	private ArrayList<MouseOverArea> Force = new ArrayList<MouseOverArea>();
 
 	private MouseOverArea regles;
 	private MouseOverArea placementTermine;
 	private MouseOverArea genererPlacement;
 	private MouseOverArea finDuTour;
 	
 	private int Counter;
 
 	private ParticleSystem particule = new ParticleSystem("drapeau.jpg");
 
 	private Image carte ;
 	public String[] lettres = { "A", "B" , "C" , "D" , "E" , "F" , "G" , "H" , "I" , "J"};
 
 	private Controller controller;
 
 	private ArrayList<Image> LostTrue = new ArrayList<Image>();
 	private ArrayList<Image> LostFalse = new ArrayList<Image>();
 
 	//private Image prise = null;// représente la prise d'un pion (une image suit le curseur.
 	//private boolean placement = true; // représente le fait de pouvoir placer ses pions en début de partie.
 	private String[] photos =  {"drapeau.jpg" ,"espion.jpg" , "eclaireur.jpg"  , "demineur.jpg",
 			"sergent.jpg" , "lieutenant.jpg", "capitaine.jpg", "commandant.jpg" ,
 			"colonel.jpg" ,"general.jpg", "marechal.jpg" , "bombe.jpg"};
 
 	public CurrentGame(Controller controller)
 	{
 		super();
 		this.controller = controller;
 		TourView = true;
 		Counter = 5000;
 	}
 
 	/**
 	 * pas touche.
 	 */
 	public int getID() 
 	{
 		return 0;
 	}
 
 	public boolean getTourView()
 	{
 		return TourView;
 	}
 
 	public void setTourView(boolean T)
 	{
 		TourView = T;
 	}
 
 	/**
 	 * @see org.newdawn.slick.BasicGame#init(org.newdawn.slick.GameContainer)
 	 */
 	public void init(GameContainer container,StateBasedGame game) throws SlickException 
 	{
 
 		carte = new Image("mapStratego.jpg");
 
 		//Mise en place des boutons. 
 		regles = new MouseOverArea( container, new Image("regles.png") , 875 , 620 );
 		finDuTour = new MouseOverArea( container, new Image("finDuTour.png") ,770 , 340 );
 		genererPlacement = new MouseOverArea( container, new Image("genererPlacement.png") , 798 , 550 );
 		placementTermine = new MouseOverArea( container, new Image("placementTermine.png") , 800 , 480 );
 
 		// Mise en place de l'échequier.
 		for ( int i = 0 ; i < 10 ; i++)
 		{
 			for ( int j = 0 ; j < 10 ; j++)
 			{
 				Echequier.add( new MouseOverArea( container, new Image("transparent.png") ,71*j+58 , 71*i+20 , 44 , 62 ));
 			}
 		}
 
 
 
 		for ( int i = 0 ; Echequier.size() > i ; i++)
 		{
 			Echequier.get(i).setNormalColor(new Color(1f,1f,1f,0.7f));
 			Echequier.get(i).setMouseOverColor(new Color(0.5f,0.5f,0.5f,3f));
 		}
 
 		force(container);
 
 		try
 		{
 			File xmlFile = new File ("emitter.xml");
 			ConfigurableEmitter emitter = ParticleIO.loadEmitter(xmlFile) ;
 			particule.addEmitter(emitter);
 			particule.setPosition(600, 600);
 		}
 		catch (Exception e)
 		{
 			System.out.println("particule error");
 
 		}
 
 		particule.setBlendingMode(ParticleSystem.BLEND_ADDITIVE);
 	}
 
 
 
 
 
 	/**
 	 * pre: inconnu ?
 	 * post: Affiche ce qu'on lui demande d'afficher.
 	 */
 	public void render(GameContainer container,StateBasedGame game, Graphics g) throws SlickException 
 	{
 
 		g.drawImage(carte, 45, 15);
 
 		//affiche les lettres de la carte
 		for ( int i = 0 ; i < 10 ; i++)
 		{
 			g.drawString(lettres[i] , i*71+ 77  , 0);
 			g.drawString(String.valueOf( i ) , 30 , 42+i*71 );
 		}
 
 		//affichage de l'échequier
 		for ( int i = 0 ; i < Echequier.size() ; i++)
 		{
 			Echequier.get(i).render(container, g);
 		}
 
 		//affichage des pions disponibles.
 		if(controller.getPlacement())
 		{
 			//affichage des boutons de la phase placement.
 			genererPlacement.render(container , g);
 			placementTermine.render(container , g);
 			regles.render(container , g);
 
 			for ( int i = 0 ; i < Force.size() ; i++)
 			{
 				Force.get(i).render(container , g);
 				g.setColor(Color.yellow);
 				g.drawString(String.valueOf(controller.getGame().checkNumberOfPion(photos[i] , TourView)), Force.get(i).getX(), Force.get(i).getY());
 			}
 			g.setColor(Color.white);
 		}
 		else
 		{
 			regles.setLocation(970 , 337);
 			regles.render(container , g);
 			if (!controller.getIsAnIA())
 			{
 				finDuTour.render(container , g);
 			}
 		}
 
 		// Fin.render(container, g);
 
 		if ( controller.getPrise() != null)
 		{
 			new Image(controller.getPrise()).drawCentered(container.getInput().getMouseX(), container.getInput().getMouseY());
 		}
 
 		particule.render();
 
 		//affichage des pions perdus
 		RenderLost(g);
 
 	}
 
 	/**
 	 * @see org.newdawn.slick.BasicGame#update(org.newdawn.slick.GameContainer, int)
 	 */
 
 	public void update(GameContainer container,StateBasedGame game, int delta) throws SlickException 
 	{
 
 		Input input = container.getInput();
 
 		ImageLost();
 		
 		if (Counter < 1500)
 		{
 			Counter += delta;
 			System.out.println(Counter);
 		}
 		
 		//Attente ia;
 		if(Counter > 1500 && controller.getDeplacement()!=null)
 		{
 			controller.IAPlay();
 			checkCase(controller.getLastClick());
 			checkCase(controller.getNewClick());
			//controller.setDeplacement();
 		}
 
 		//Code exécuté en cas de clic gauche avec la souris.
 		if (  input.isMousePressed(Input.MOUSE_LEFT_BUTTON) )
 		{
 			//phase Placement.
 			if ( controller.getPlacement())
 			{
 
 				if (!ChoixPion())
 				{
 					setPion(container);
 				}
 				
 			}
 
 			
 			if ( !controller.getPartieFinie() && !controller.getIsAnIA() )
 			{
 				if ( controller.getPlacement() && placementTermine.isMouseOver())
 				{
 					
 					if ( controller.getPlacementJoueur1() && controller.checkStopPJ1() )
 					{
 						game.enterState(2,new  FadeOutTransition() , new FadeInTransition());
 					}
 
 					if ( controller.getPlacementJoueur2() && controller.checkStopPJ2())
 					{
 
 						game.enterState(2,new  FadeOutTransition() , new FadeInTransition());
 
 					}
 				}
 				else
 				{
 					if (controller.getTour()!=TourView && finDuTour.isMouseOver())
 					{
 						game.enterState(2,new  FadeOutTransition() , new FadeInTransition());
 					}
 				}
 			}
 
 			//en cours de partie.
 			if (!controller.getPlacement() && !controller.getPartieFinie())
 			{
 				setMove(container);
 				
 			}
 
 			//bouton permettant de placer ses pions automatiquement.
 			if ( genererPlacement.isMouseOver())
 			{
 				if ( controller.getPlacement())
 				{
 					dude(container);
 				}
 			}
 			if( regles.isMouseOver())
 			{
 				new Regles();
 			}
 			
 			if(placementTermine.isMouseOver() && controller.getPlacement())
 			{
 				if(controller.getTour())
 				{
 					controller.checkStopPJ1();
 				}
 				else
 				{
 					controller.checkStopPJ2();
 				}
 				
 				if (controller.getIsAnIA())
 				{
 					UpGame2();
 				}
 			}
 			
 			
 		}
 
 		//Code exécuté en cas de pression sur la touche Echap.
 		if ( input.isKeyPressed(Input.KEY_ESCAPE))
 		{
 			container.exit();
 		}
 
 		if (controller.getPartieFinie())
 		{
 			particule.update(delta);
 		}
 	}
 
 	/**
 	 * pre:
 	 * post: Crée des images à partir des tableaux lost de game et les placent dans LostTrue et LostFalse en fct du joueur
 	 * @throws SlickException 
 	 */
 	public void ImageLost( ) throws SlickException
 	{
 
 		LostTrue = new ArrayList<Image>();
 		LostFalse = new ArrayList<Image>();
 
 		for ( int i = 0 ; i < controller.getGame().getLostTrue().size() ; i++)
 		{
 			LostTrue.add(new Image (controller.getGame().getLostTrue().get(i).getPath()));
 		}
 
 		for ( int i = 0 ; i < controller.getGame().getLostFalse().size() ; i++)
 		{
 			LostFalse.add(new Image (controller.getGame().getLostFalse().get(i).getPath()));
 		}
 	}
 
 	/**
 	 * 
 	 * pre:
 	 * post: affiche les images contenus dans LostTrue et LostFalse (càd les pions perdus en cours de partie).
 	 */
 	public void RenderLost (Graphics g) throws SlickException
 	{
 		for ( int i = 0 ; i < LostTrue.size() ; i++)
 		{
 			g.drawImage(LostTrue.get(i) , 775 + i*46 - ((int) (i/10) * 460) , 450 + ( (int) (i/10) * 62 ) );
 		}
 
 		for ( int i = 0 ; i < LostFalse.size() ; i++)
 		{
 			g.drawImage(LostFalse.get(i) , 775 + i*46 - ((int) (i/10) * 460) , 15 + ( (int) (i/10) * 62 ) );
 		}
 
 	}
 
 	/**
 	 * pre:-
 	 * post: Execute avec un argument i allant de 0 à 99.
 	 * @throws SlickException
 	 */
 	public void UpGame2 () throws SlickException
 	{
 		for (int i = 0 ; i < 100 ; i++)
 		{
 			checkCase(i);
 		}
 	}
 
 	/**
 	 * pre: 0 <= i <= 99
 	 * post: actualise l'affichage d'une case en fct du model et du tour du joueur.
 	 * 
 	 */
 	public void checkCase(int i) throws SlickException
 	{
 
 		if (i == 100)  return;
 
 		if ( controller.getGame().getMap().getPion(i/10,i%10) == null )
 		{	
 			//cas la case est vide.
 			Echequier.get(i).setNormalImage(new Image ("transparent.png"));
 			Echequier.get(i).setMouseOverImage(new Image ("transparent.png"));
 		}
 		else
 		{
 			if ( TourView == controller.getGame().getMap().getPion(i/10,i%10).getTeam())
 			{
 				//cas où il s'agit d'un pion apparteant à un joueur qui est en train de jouer.
 				Echequier.get(i).setNormalImage(new Image (controller.getGame().getMap().getPion(i/10,i%10).getPath()));
 				Echequier.get(i).setMouseOverImage(new Image (controller.getGame().getMap().getPion(i/10,i%10).getPath()));
 			}
 			else
 			{
 				if ( !controller.getGame().getMap().getPion(i/10,i%10).getPath().contains("transparent.png"))
 				{
 					//cas il s'agit d'un pion adverse mais pas de l'eau.
 					Echequier.get(i).setNormalImage(new Image ("jaune.jpg"));
 					Echequier.get(i).setMouseOverImage(new Image ("jaune.jpg"));
 				}
 			}
 		}
 	}
 
 	/**
 	 * pre:
 	 * post: crée le tableau de sélection de pions à droite de l'écran.
 	 */
 	public void force ( GameContainer container ) throws SlickException 
 	{
 		for ( int i = 0 ; i < 6 ; i++)
 		{
 			Force.add(new MouseOverArea ( container , new Image (photos[i]), 880 , 10+70*i ));
 			Force.get(i).setNormalColor(new Color(1f,1f,1f,0.7f));
 		}
 
 		for ( int i = 0 ; i < 6 ; i++)
 		{
 			Force.add(new MouseOverArea ( container , new Image (photos[i+6]), 940 , 10+70*i ));
 			Force.get(i+6).setNormalColor(new Color(1f,1f,1f,0.7f));
 		}
 	}
 
 	/**
 	 * pre: Echequier a été initialisé.
 	 * post: Crée mouseoverarea avec du bleau dans les case 42,43,46,47,52,53,56,57 de l'échequier.
 	 */
 	public void setFlotte(GameContainer container, int mid ) throws SlickException 
 	{
 		//premier bloc de flotte.
 		Echequier.set(42, new MouseOverArea (container , new Image("transparent.png") , mid+135 , 270));
 		Echequier.set(43, new MouseOverArea (container , new Image("transparent.png") , mid+197 , 270));
 		Echequier.set(52, new MouseOverArea (container , new Image("transparent.png") , mid+135 , 335));
 		Echequier.set(53, new MouseOverArea (container , new Image("transparent.png") , mid+197 , 335));
 
 		//deuxième bloc de flotte.
 		Echequier.set(46, new MouseOverArea (container , new Image("transparent.png") , mid+397 , 270));
 		Echequier.set(47, new MouseOverArea (container , new Image("transparent.png") , mid+465 , 270));
 		Echequier.set(56, new MouseOverArea (container , new Image("transparent.png") , mid+397 , 335));
 		Echequier.set(57, new MouseOverArea (container , new Image("transparent.png") , mid+464 , 335));
 	}
 
 
 	/**
 	 * pre:-
 	 * post: permet de sélectionner un pion parmi ceux proposer sur la droite
 	 */
 	public boolean ChoixPion() throws SlickException 
 	{
 		//Cette variable permet de gérer le cas où l'utisateur clique en dehors du plateau pr se débarrasser du pion qu'il
 		//a en main.
 		boolean action = false;
 
 		for (int i = 0 ; i < Force.size() ; i++)
 		{
 			if ( Force.get(i).isMouseOver() )
 			{
 				controller.setPrise( photos[i] );
 
 				action = true;
 			}
 		}
 
 		return action;
 	}
 
 	/**
 	 * 
 	 * pre: 
 	 * post: Pose le pion sélectionné sur la case sélectionnée, annule la variable prise si c'est fait.
 	 * 		et modifie le model en conséquence durant la phase de placement.
 	 * Si aucun pion n'est sélectionné, en sélectionne un.
 	 */
 	public void setPion(GameContainer container) throws SlickException
 	{
 		//Cette variable permet de gérer le cas où l'utisateur clique en dehors du plateau pr se débarrasser du pion qu'il
 		//a en main.
 		boolean action = false;
 
 		for (int i = 0 ;  i < Echequier.size()  ; i++)
 		{
 
 			if ( Echequier.get(i).isMouseOver() && !(i == 42 || i == 43 || i == 46 || i == 47 || i == 52 || i == 53 || i == 56 || i == 57))
 			{
 				//cas où l'utilisateur essaie de cliquer sur les pions adverse.
 				if ( controller.getGame().getMap().getPion(i/10,i%10) != null )
 				{
 					if ( controller.getGame().getMap().getPion(i/10,i%10).getTeam() != TourView )
 					{
 						return;
 					}
 				}
 
 				if (controller.getPrise() != null)
 				{
 
 					String inter = null;
 
 					if ( controller.getGame().getMap().getPion(i/10,i%10) != null)
 					{
 						inter = controller.getGame().getMap().getPion(i/10,i%10).getPath();
 					}
 
 					controller.setClick(i);
 
 					controller.setPrise( inter );
 
 					checkCase(controller.getNewClick());
 
 					action = true;
 
 					return;
 				}
 				else
 				{
 
 					if (controller.getGame().getMap().getPion(i/10, i%10) != null)
 					{
 
 						controller.setPrise( controller.getGame().getMap().getPion(i/10,i%10).getPath() );
 
 						controller.getGame().removePion(i/10 ,i%10);
 
 						checkCase(i);
 
 						action = true;
 
 						return;
 					}
 
 				}
 
 			}
 		}
 
 		//cas où l'on clique dansle noir, la prise passe à null.
 		if (!action)
 			controller.setPrise(null);
 	}
 
 	/**
 	 * pre:-
 	 * post: prend le pion du plateau sélectionné en "prise" si prise est vide ou si !prise place prise à cette endroit.
 	 */
 	public void setMove(GameContainer container) throws SlickException
 	{
 		//Cette variable permet de gérer le cas où l'utisateur clique en dehors du plateau pr se débarrasser du pion qu'il
 		//a en main.
 		boolean action = false;
 
 		//Le tour est terminé, aucune action n'est permise.
 		if ( controller.getTour() != TourView)
 		{
 			return ;
 		}
 
 		for (int i = 0 ; i < Echequier.size() ; i++)
 		{
 			//On parcourt l'échéquier pr trouver la case sur laquelle l'utilisateur à cliqué.
 			if ( Echequier.get(i).isMouseOver())
 			{
 				//cas où l'on n'est pas sur la l'eau.
 				if( !(i == 42 || i == 43 || i == 46 || i == 47 || i == 52 || i == 53 || i == 56 || i == 57))
 				{
 
 					if ( controller.getGame().getMap().getPion(i/10,i%10) != null )
 					{
 
 						if ( controller.tour == controller.getGame().getMap().getPion(i/10,i%10).getTeam() &&
 								controller.getPrise() != null )
 						{
 							//cas où l'utilisateur a un pion en main et que l'on clique sur un autre pr le prendre aussi.
 							controller.setPrise( null );
 						}
 					}
 
 					if ( controller.getPrise() == null  )
 					{
 						if ( controller.getGame().getMap().getPion(i/10,i%10) != null )
 						{	
 							if ( controller.getGame().getMap().getPion(i/10,i%10).getTeam() == controller.getTour() )
 							{
 								controller.setClick(i);
 
 								//cas où l'utilisateur veut reposer le pion qu'il vient de prendre.	
 								if(controller.getLastClick() == controller.getNewClick())
 								{
 									controller.setPrise(null);
 									checkCase(i);
 									controller.setNewClick(100);
 								}
 								else
 								{
 									controller.setPrise( controller.getGame().getMap().getPion(i/10,i%10).getPath() );
 									Echequier.get(i).setNormalImage(new Image ("transparent.png"));
 									Echequier.get(i).setMouseOverImage(new Image ("transparent.png"));
 									checkCase(controller.getLastClick());
 								}
 							}
 						}
 					}
 					else
 					{
 						controller.setClick(i);
 						controller.setPrise(null);
 						checkCase(controller.getLastClick());
 						//On affiche la case touchée.
 						if ( controller.getGame().getMap().getPion(i/10,i%10) != null && TourView != controller.getTour() )
 						{
 							Echequier.get(i).setNormalImage(new Image (controller.getGame().getMap().getPion(i/10,i%10).getPath()));
 							Echequier.get(i).setMouseOverImage(new Image (controller.getGame().getMap().getPion(i/10,i%10).getPath()));
 						}
 						else
 						{
 							checkCase(controller.getNewClick());
 						}
 						
 						if(controller.getIsAnIA())
 						{
 							checkCase(i);
 						}
 						Counter=0;
 					}
 				}
 
 				action = true;
 				
 				
 			}
 		}
 
 		if (!action)
 		{
 			controller.setPrise ( null );
 			checkCase(controller.getNewClick());
 			controller.setNewClick(100);
 		}
 		
 	}
 
 
 
 
 	/**
 	 * pre-
 	 * post: place les pions automatiquement en début de partie pr ne pas emmerder les programmeurs.( good guy method )
 	 */
 	public void dude (GameContainer container) throws SlickException
 	{
 		controller.dude();
 		UpGame2();
 	}
 
 }
