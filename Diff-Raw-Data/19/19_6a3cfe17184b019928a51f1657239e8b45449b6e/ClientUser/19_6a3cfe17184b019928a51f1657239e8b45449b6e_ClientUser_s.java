 package client;
 
 import protocol.GetAllRatedAttributesMessage;
 import protocol.GetRatedConsumablesMessage;
 import protocol.LoginMessage;
 import protocol.ProtocolException;
 import protocol.SetAttributeRatingMessage;
 import protocol.SetPasswordMessage;

 import java.util.ArrayList;
 import java.util.List;
 
import protocol.RegisterMessage;
import server.ServerConsumable;
import server.ServerRecommendation;
 
 import core.Attribute;
 import core.AttributeRating;
 import core.Consumable;
 import core.LoginException;
 import core.Recommendation;
 import core.User;
 import database.DBAbstractionException;
 
 public class ClientUser extends User {
 
 	public ClientUser(int userId)
 	{
 		constructorHelper(userId);
 	}
 	public ClientUser(String username, String password, Boolean newAccount) throws LoginException
 	{
 		constructorHelper(username, password, newAccount);
 	}
 	
 	public void setUserIdPublic(int userId)
 	{
 		setUserId(userId);
 	}
 	
 	@Override
 	protected void constructorHelper(String username, String password, Boolean newAccount) throws LoginException {
 		this.setUsernameWithoutCommit(username);
 		this.setPasswordWithoutCommit(password);
 		
 		if(newAccount)
 		{
 			RegisterMessage m = new RegisterMessage(this);
 			
 			String message = m.generateMessage();
 
 			try
 			{
 				String response = Client.getInstance().sendMessage(message);
 				m.processResponse(response);
 			}
 			catch(ProtocolException e)
 			{
 				throw new LoginException(e);
 			} catch (ClientException e) {
 				throw new LoginException(e);
 			}
 			
 			if(this.getUserId() <= 0)
 			{
 				throw new LoginException("Could not login after registration.");
 			}
 		}
 		else
 		{
 			LoginMessage m = new LoginMessage(this);
 			
 			String message = m.generateMessage();
 			
 			try
 			{
 				String response = Client.getInstance().sendMessage(message);
 				
 				m.processResponse(response);
 				
 				if(getUserId() == 0)
 				{
 					throw new LoginException("Could not load from Id");
 				}
 			}
 			catch(LoginException e)
 			{
 				e.printStackTrace();
 			} catch (ClientException e) {
 				e.printStackTrace();
 			} catch (ProtocolException e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}
 
 	@Override
 	protected void constructorHelper(int userId)  {
 		setUserId(userId);
 		
 		LoginMessage m = new LoginMessage(this);
 		
 		String message = m.generateMessage();
 		
 		try
 		{
 			String response = Client.getInstance().sendMessage(message);
 			
 			m.processResponse(response);
 			
 			if(getUserId() == 0)
 			{
 				throw new LoginException("Could not load from Id");
 			}
 		}
 		catch(LoginException e)
 		{
 			e.printStackTrace();
 		} catch (ClientException e) {
 			e.printStackTrace();
 		} catch (ProtocolException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	//The logic for this is really handled in the constructors.
 	@Override
 	public Boolean authenticate(String username, String password) {
 		if(getUserId() == 0)
 		{
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 
 	@Override
 	public void setAttributeRating(Attribute attribute, int newRating) {
 		SetAttributeRatingMessage m = new SetAttributeRatingMessage(this, attribute, newRating);
 		
 		try {
 			m.processResponse(Client.getInstance().sendMessage(m.generateMessage()));
 		} catch (ClientException e) {
 			e.printStackTrace();
 		} catch (ProtocolException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	@Override
 	public void setAttributeRating(int attributeId, int newRating) {
 		ClientAttribute attribute = new ClientAttribute(attributeId);
 		setAttributeRating(attribute, newRating);
 	}
 	
 	@Override
 	public List<AttributeRating> getAllRatedAttributes() {
 		List<AttributeRating> retVal = new ArrayList<AttributeRating>();
 		
 		GetAllRatedAttributesMessage m = new GetAllRatedAttributesMessage(this, retVal);
 		
 		try {
 			m.processResponse(Client.getInstance().sendMessage(m.generateMessage()));
 			return retVal;
 		} catch (ProtocolException e) {
 			e.printStackTrace();
 		} catch (ClientException e) {
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public int getAttributeRating(Attribute attribute) {
 		List<AttributeRating> ratings = getAllRatedAttributes();
 		
 		for(AttributeRating ar : ratings)
 		{
 			if(ar.getAttribute().getAttributeId() == attribute.getAttributeId())
 			{
 				return ar.getRating();
 			}
 		}
 		
 		return -1;
 	}
 
 	@Override
 	public void setRecommendationRating(Consumable consumable, int newRating) {
 		setRecommendationRating(consumable.getConsumableId(), newRating);
 		
 	}
 
 	//TODO-nf-tests not tested because ClientRecommendation.setRevisedRating needs to be implemented first.
 	@Override
 	public void setRecommendationRating(int consumableId, int newRating) {
 		ClientConsumable consumable = new ClientConsumable(consumableId);
 		
 		boolean set = false;
 		for(Recommendation r : getRatedConsumables())
 		{
 			if(r.getConsumable().getConsumableId() == consumableId)
 			{
 				r.setRevisedRating(newRating);
 				set = true;
 				break;
 			}
 		}
 		
 		if(!set)
 		{
 			new ClientRecommendation(this, consumable, newRating);
 		}	
 		
 	}
 	
 	@Override
 	public List<Recommendation> getRatedConsumables() {
 		List<Recommendation> retVal = new ArrayList<Recommendation>();
 		
 		GetRatedConsumablesMessage m = new GetRatedConsumablesMessage(this, retVal);
 		
 		try {
 			m.processResponse(Client.getInstance().sendMessage(m.generateMessage()));
 		} catch (ProtocolException e) {
 			e.printStackTrace();
 		} catch (ClientException e) {
 			e.printStackTrace();
 		}
 		
 		return retVal;
 	}
 	
 	
 	//TODO-nf-refactor move into abstract class?
 	@Override
 	public List<Consumable> getRecommendedConsumables() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void commit() throws DBAbstractionException {
 		SetPasswordMessage m = new SetPasswordMessage(this);
 		try {
 			m.processResponse(Client.getInstance().sendMessage(m.generateMessage()));
 		} catch (ProtocolException e) {
 			e.printStackTrace();
 		} catch (ClientException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void refresh() throws DBAbstractionException {
 		throw new DBAbstractionException("Unimplemented.");
 	}
 }
