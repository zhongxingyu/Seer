 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 //code adapted from LanePanel for consistency
 /**
  * class contains the panel which have graphic view of gantry manager and
  * button panel which switch between graphic view and break panel.
  * For this version (V.1), the break panel serves no function.
  */
 public class GantryManager extends JPanel implements ActionListener {
 	/** Indicates whether gantry is currently broken or not */
 	private boolean isBroken;
 	/** instance of linked client */
 	private GantryClient myClient;
 	/** button for switching to gantry/feeders graphic panel */
 	private JButton gantry;
 	/** button for breaking the gantry */
 	private JButton btnBreak;
 	/** button for fixing the gantry */
 	private JButton btnFix;
 	/** button panel that contains the above two buttons  */
 	private JPanel buttonLayout;
 	/** main panel that contains graphic panel and break panel */
 	private JPanel panelLayout;
 	/** cLayout used to layout the connect panel and the gantry manager */
 	private CardLayout cLayout;
 	/** panel for graphic view of gantry manager */
 	private GantryGraphics graphics;
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	/**
 	 * @param args
 	 */
 	/** Initialization */
 	public GantryManager( GantryClient client ) {
 		myClient = client;
 		gantry = new JButton( "View Gantry" );
 		btnBreak = new JButton( "Break" );
 		btnFix = new JButton( "Fix" );
 		graphics = new GantryGraphics();
 		
 		panelLayout = new JPanel();
 		cLayout = new CardLayout();
 		panelLayout.setLayout( cLayout );
 		
 		buttonLayout = new JPanel();
 		buttonLayout.setLayout( new GridBagLayout() );
 		
 		//layout buttons on bottom of panel
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = c.VERTICAL;
 		c.gridx = 0;
		//buttonLayout.add( gantry, c );
 		
 		c.gridx = 1;
 		buttonLayout.add( btnBreak, c );
 		btnBreak.addActionListener(this);
 		btnFix.addActionListener(this);
 		
 		//layout graphics and break panel in center
 		panelLayout.add(graphics, "Graphics" );
 		
 		//add break panel later
 		
 		//layout panels
 		setLayout( new BorderLayout() );
 		add( buttonLayout, BorderLayout.SOUTH );
 		add( panelLayout, BorderLayout.CENTER );
 	}
 	
 	public void setFactoryState(FactoryStateMsg factoryState)
 	{
 		graphics.setFactoryState(factoryState);
 	}
 
 	public void update(FactoryUpdateMsg updateMsg)
 	{
 		graphics.update(updateMsg);
 	}
 	
 	public NetComm getCom()
 	{
 		return myClient.getCom();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent ae) 
 	{
 		if(ae.getSource() == btnBreak)
 		{
 			buttonLayout.remove(btnBreak);
 			buttonLayout.add(btnFix);
 			validate();
 			repaint();
 			myClient.getCom().write( new NonNormativeMsg( NonNormativeMsg.ItemEnum.GANTRY, 2, NonNormativeMsg.CmdEnum.BREAK));
 			isBroken = true;
 			
 		}
 		else if(ae.getSource() == btnFix)
 		{
 			buttonLayout.remove(btnFix);
 			buttonLayout.add(btnBreak);
 			validate();
 			repaint();
 			myClient.getCom().write( new NonNormativeMsg( NonNormativeMsg.ItemEnum.GANTRY, 2, NonNormativeMsg.CmdEnum.FIX));
 			isBroken = false;
 			
 		}
 	}
 }
