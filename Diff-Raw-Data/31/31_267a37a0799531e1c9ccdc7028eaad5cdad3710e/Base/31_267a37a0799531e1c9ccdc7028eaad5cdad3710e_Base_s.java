 package sfs2x.extensions.projectsasha.game.entities.gateways;
 
 import sfs2x.extensions.projectsasha.game.entities.Player;
 
 public class Base extends Gateway
 {
	private static final int BASE_PAYMENT_AMOUNT 	= 5;
	private static final int BASE_DEFENCE_LEVEL		= 5;
	private static final int BASE_ATTACK_LEVEL		= 5;
 	
 	public Base(Player owner, String name, String state) 
 	{
 		super(owner, name, state);
 	}
 
 	
 	//GETTERS
 	
 	@Override
 	public int getPaymentAmount() 
 	{
		return BASE_PAYMENT_AMOUNT;
 	}
 
 	@Override
 	protected int getBaseAttackLevel()
 	{
		return BASE_ATTACK_LEVEL;
 	}
 
 	@Override
 	protected int getBaseDefenceLevel()
 	{
		return BASE_DEFENCE_LEVEL;
 	}
 	
 	@Override
 	public void onGatewayConquered(Player newOwner, Player oldOwner)
 	{
 		
 	}
 
 }
