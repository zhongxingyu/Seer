 package de.fhkoeln.gm.wba2.phase2.rest_webservice;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.math.BigInteger;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.transform.stream.StreamSource;
 
 import de.fhkoeln.gm.wba2.phase2.jaxb.*;
 
 public class DataHandler {
 
 	static DataHandler instance;
 	
 	static File cc_file;
 	static ColourConnection cc_base;
 	static JAXBContext cc_context;
 	
 	
 	private DataHandler() {
 		
 		cc_file= new File(Config.datafile_path);
 		
 		try {
 			cc_context = JAXBContext.newInstance(ColourConnection.class);
 			
 			if(!cc_file.exists()) {
 				cc_file.createNewFile();
 				
 				// creating an empty document structure
 				Marshaller m = cc_context.createMarshaller();
 				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 				m.marshal(new ObjectFactory().createColourConnection(new ColourConnection()), cc_file);
 			}
 			
 			Unmarshaller um = cc_context.createUnmarshaller();
 			cc_base = (ColourConnection) um.unmarshal(new StreamSource(cc_file), ColourConnection.class).getValue();
 			
 		} catch (JAXBException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	
 	
 	/**
 	 * Create a user element in the xml structure
 	 * 
 	 * @param user_data XML structure with the data of the new user
 	 * @return new id for the created user
 	 */
 	public String createUser(String user_data) {
 		
 		User given_user = (User)unmarshall(user_data, User.class);
 		ColourConnection.Users.User cc_user = new ColourConnection.Users.User();
 		int new_id = 1;
 		List<ColourConnection.Users.User> user_list = cc_base.getUsers().getUser();
 
 		if(given_user.getUsername().length() == 0)
 		    return null;
 		
 		for(User curr_user: user_list) {
 		    if(curr_user.getUsername().equalsIgnoreCase(given_user.getUsername()))
 		        return null;
 		}
 		
 		if(user_list.size() > 0) {
 			ColourConnection.Users.User last_user = cc_base.getUsers().getUser().get(user_list.size()-1);
 			new_id = last_user.getId().intValue() + 1;
 		}
 		
 		cc_user.setId(BigInteger.valueOf(new_id));
 		cc_user.setUsername(given_user.getUsername());
 		
 		// date given by the client
 		//cc_user.setDateOfRegistration(user.getDateOfRegistration());
 		
 		// date set by the system; more reliable
 		GregorianCalendar c = new GregorianCalendar();
 		c.setTime(new Date());
 		
 		XMLGregorianCalendar currGregCal;
 		try {
 			currGregCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
 			cc_user.setDateOfRegistration(currGregCal);
 		} catch (DatatypeConfigurationException e) {}
 		
 		cc_user.setFavouriteColours(new FavouriteColourList());
 		cc_user.setFavouriteColourpalettes(new FavouriteColourPaletteList());
 		cc_user.setFollowers(new Followers());
 		cc_user.setCreations(new ColourPaletteList());
 		
 		cc_base.getUsers().getUser().add(cc_user);
 		
 		marshall_cc();
 		
 		return String.valueOf(new_id);
 	}
 	
 	
 	/**
 	 * Find the user through a given id and return the marshalled structure as a string
 	 * 
 	 * @param id user id
 	 * @return marshalled user element
 	 */
 	public String getUser(String id) {
 		
 		BigInteger bi_id = BigInteger.valueOf(Long.parseLong(id));
 		
 		String marshalled_str = null;
 		List<ColourConnection.Users.User> user_arr = cc_base.getUsers().getUser();
 		
 		for(ColourConnection.Users.User curr_user: user_arr) {
 			if(curr_user.getId().equals(bi_id)) {
 				
 				User found_user = new User();
 				
 				found_user.setUsername(curr_user.getUsername());
 				found_user.setId(curr_user.getId());
 				found_user.setDateOfRegistration(curr_user.getDateOfRegistration());
 				
 				marshalled_str = marshall(found_user);
 				break;
 			}
 		}
 		
 		return marshalled_str;
 	}
 	
 	
 	/**
 	 * Get a marshalled structure containing a list of all existing users
 	 * 
 	 * @return marshalled users list
 	 */
 	public String getUsers(String username) {
 		
 		UserList users_arr = new UserList();
 		
 		for(ColourConnection.Users.User curr_user: cc_base.getUsers().getUser()) {
 		    
 		    if(username == null || curr_user.getUsername().equalsIgnoreCase(username)) {
     		    Ref new_user_ref = new Ref();
     			
     			new_user_ref.setId(curr_user.getId());
     			new_user_ref.setRef("/user/" + curr_user.getId().toString());
     			
     			users_arr.getUser().add(new_user_ref);
 		    }
 		}
 		
 		return marshall(users_arr);
 	}
 	
 
 	/**
 	 * Update the data of a user
 	 * 
 	 * @param id user id
 	 * @param body XML structure with new updated data
 	 * @return success or failure
 	 */
 	public boolean updateUser(String id, String body) {
 		
 		BigInteger bi_id = BigInteger.valueOf(Long.parseLong(id));
 		
 		User found_user;
 		if((found_user = getUserObj(bi_id)) != null) {
 			
 			User user_data = (User)unmarshall(body, User.class);
 			
 			if(user_data.getUsername() != null)
 				found_user.setUsername(user_data.getUsername());
 			
 			if(user_data.getDateOfRegistration() != null)
 				found_user.setDateOfRegistration(user_data.getDateOfRegistration());
 			
 			marshall_cc();
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	
 	/**
 	 * Remove a user out of the persistent xml structure 
 	 * 
 	 * @param id user id
 	 * @return success or failure
 	 */
 	public boolean deleteUser(String id) {
 		
 		ColourConnection.Users.User found_user;
 		BigInteger bi_id = BigInteger.valueOf(Long.parseLong(id));
 		
 		if((found_user = getUserObj(bi_id)) != null) {
 			
 			cc_base.getUsers().getUser().remove(found_user);
 			
 			// let colours and palettes created by the user stay in the system;
 			//		because of their connections with other users (favourite colour etc.)
 			
 			
 			marshall_cc();
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	
 	/**
 	 * Get a marshalled structure representing a list containing all creations of a certain user
 	 * 
 	 * @param user_id user id
 	 * @return marshalled structure representing a list
 	 */
 	public String getUserCreations(String user_id) {
 		
 		ColourConnection.Users.User found_user;
 		BigInteger bi_user_id = BigInteger.valueOf(Long.parseLong(user_id));
 		
 		if((found_user = getUserObj(bi_user_id)) != null) {
 			return marshall(found_user.getCreations());
 		}
 		
 		return null;
 	}
 	
 	
 	/**
 	 * Create a new entry representing a following user
 	 * 
 	 * @param user_id id of the user being followed
 	 * @param follower_id id of the user following
 	 * @return success or failure
 	 */
 	public boolean putUserFollower(String user_id, String follower_id) {
 		
 		BigInteger bi_user_id = BigInteger.valueOf(Long.parseLong(user_id));
 		BigInteger bi_follower_id = BigInteger.valueOf(Long.parseLong(follower_id));
 		
 		if(getFollowerObj(bi_user_id, bi_follower_id) == null) {
 			
 			Follower new_follower = new Follower();
 			
 			new_follower.setId(bi_follower_id);
 			new_follower.setRef("/user/" + bi_follower_id.toString());
 			
 			// set the date since when the follower is a follower
 			GregorianCalendar c = new GregorianCalendar();
 			c.setTime(new Date());
 
 			XMLGregorianCalendar currGregCal = null;
 			try {
 				currGregCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
 			} catch (DatatypeConfigurationException e) {}
 			
 			new_follower.setFollowingSince(currGregCal);
 			
 			getUserObj(bi_user_id).getFollowers().getFollower().add(new_follower);
 			
 			marshall_cc();
 			return true;
 		}
 		
 		return false;
 	}
 	
 	
 	/**
 	 * Delete the entry representing a follwoing user
 	 * 
 	 * @param user_id id of the user being followed
 	 * @param follower_id id of the user following
 	 * @return success or failure
 	 */
 	public boolean deleteUserFollower(String user_id, String follower_id) {
 		
 		BigInteger bi_user_id = BigInteger.valueOf(Long.parseLong(user_id));
 		BigInteger bi_follower_id = BigInteger.valueOf(Long.parseLong(follower_id));
 
 		if(getFollowerObj(bi_user_id, bi_follower_id) != null) {
 			
 			ColourConnection.Users.User user_followed = getUserObj(bi_user_id);
 			
 			for(Follower curr_follower: user_followed.getFollowers().getFollower()) {
 				if(curr_follower.getId().equals(bi_follower_id)) {
 					user_followed.getFollowers().getFollower().remove(curr_follower);
 					marshall_cc();
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	
 	/**
 	 * Get a marshalled structure representing a list of users following a certain user
 	 * 
 	 * @param user_id
 	 * @return marshalled structure
 	 */
 	public String getUserFollowers(String user_id) {
 		
 		BigInteger bi_user_id = BigInteger.valueOf(Long.parseLong(user_id));
 		String marshalled_str = null;
 		
 		ColourConnection.Users.User found_user;
 		
 		if((found_user = getUserObj(bi_user_id)) != null) {
 			marshalled_str = marshall(found_user.getFollowers());
 		}
 		
 		return marshalled_str;
 	}
 	
 	
 	/**
 	 * Create an entry representing a new favourite colour
 	 * 
 	 * @param user_id user id
 	 * @param colour_id colour code
 	 * @return success or failure
 	 */
 	public boolean putUserFavouriteColour(String user_id, String colour_id) {
 		
 		BigInteger bi_user_id = BigInteger.valueOf(Long.parseLong(user_id));
 		
 		GregorianCalendar c = new GregorianCalendar();
 		c.setTime(new Date());
 		
 		XMLGregorianCalendar currGregCal = null;
 		try {
 			currGregCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
 		} catch (DatatypeConfigurationException e) {}
 		
 		// create colour if it doesn't exist already
 		if(getColourObj(colour_id) == null) {
 			
 			ColourConnection.Colours.Colour new_colour = new ColourConnection.Colours.Colour();
 			
 			new_colour.setId(colour_id);
 			
 			// create creator-ref
 			Ref creator = new Ref();
 			creator.setId(bi_user_id);
 			creator.setRef("/user/" + bi_user_id.toString());
 			
 			new_colour.setCreator(creator);
 			
 			new_colour.setComments(new ColourConnection.Colours.Colour.Comments());
 			
 			// set date for when the colour was created
 			new_colour.setDateOfCreation(currGregCal);
 			
 			cc_base.getColours().getColour().add(new_colour);
 		}
 		
 		if(getFavouriteColourRefObj(bi_user_id, colour_id) == null) {
 			
 			FavouriteColour new_favourite_colour = new FavouriteColour();
 			
 			new_favourite_colour.setId(colour_id);
 			new_favourite_colour.setRef("/colour/" + colour_id);
 
 			// set date for when the colour was set as a favourite
 			new_favourite_colour.setFavouriteSince(currGregCal);
 			
 			getUserObj(bi_user_id).getFavouriteColours().getFavouriteColour().add(new_favourite_colour);
 			
 			marshall_cc();
 			return true;
 		}
 		
 		return false;
 	}
 	
 	
 	/**
 	 * Delete an entry representing a favourite colour
 	 * 
 	 * @param user_id user id
 	 * @param colour_id colour code
 	 * @return success or failure
 	 */
 	public boolean deleteUserFavouriteColour(String user_id, String colour_id) {
 		
 		BigInteger bi_user_id = BigInteger.valueOf(Long.parseLong(user_id));
 
 		if(getFavouriteColourRefObj(bi_user_id, colour_id) != null) {
 			
 			ColourConnection.Users.User user_with_favourite_colour = getUserObj(bi_user_id);
 			
 			for(FavouriteColour curr_favourite_colour:
 					user_with_favourite_colour.getFavouriteColours().getFavouriteColour()) {
 				if(curr_favourite_colour.getId().equalsIgnoreCase(colour_id)) {
 					user_with_favourite_colour.getFavouriteColours().getFavouriteColour().remove(curr_favourite_colour);
 					marshall_cc();
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	
 	/**
 	 * Get a marshalled structure representing a list of all favourite colours of a certain user
 	 * 
 	 * @param user_id user id
 	 * @return marshalled structure
 	 */
 	public String getUserFavouriteColours(String user_id) {
 		
 		BigInteger bi_user_id = BigInteger.valueOf(Long.parseLong(user_id));
 		String marshalled_str = null;
 		
 		ColourConnection.Users.User found_user;
 		
 		if((found_user = getUserObj(bi_user_id)) != null) {
 			marshalled_str = marshall(found_user.getFavouriteColours());
 		}
 		
 		return marshalled_str;
 	}
 	
 	
 	/**
 	 * Create a new entry representing a new favourite colourpalette
 	 * 
 	 * @param user_id user id
 	 * @param colourpalette_id colourpalette id
 	 * @return success or failure
 	 */
 	public boolean putUserFavouriteColourPalette(String user_id, String colourpalette_id) {
 		
 		BigInteger bi_user_id = BigInteger.valueOf(Long.parseLong(user_id));
 		BigInteger bi_colourpalette_id = BigInteger.valueOf(Long.parseLong(colourpalette_id));
 		
 		// colourpalette must exist to be able set as a favourite
 		if(getColourpaletteObj(bi_colourpalette_id) == null)
 			return false;
 		
 		if(getFavouriteColourPaletteRefObj(bi_user_id, bi_colourpalette_id) == null) {
 			
 			FavouriteColourPalette new_favourite_colourpalette = new FavouriteColourPalette();
 			
 			new_favourite_colourpalette.setId(bi_colourpalette_id);
 			new_favourite_colourpalette.setRef("/colourpalette/" + bi_colourpalette_id.toString());
 
 			// set date for when the colourpalette was set as a favourite
 			GregorianCalendar c = new GregorianCalendar();
 			c.setTime(new Date());
 			
 			XMLGregorianCalendar currGregCal = null;
 			try {
 				currGregCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
 			} catch (DatatypeConfigurationException e) {}
 			new_favourite_colourpalette.setFavouriteSince(currGregCal);
 			
 			getUserObj(bi_user_id).getFavouriteColourpalettes().getFavouriteColourpalette().add(new_favourite_colourpalette);
 			
 			marshall_cc();
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Delete the entry representing a favourite colourpalette
 	 * 
 	 * @param user_id user ud
 	 * @param colourpalette_id colourpalette id
 	 * @return success or failure
 	 */
 	public boolean deleteUserFavouriteColourPalette(String user_id, String colourpalette_id) {
 		
 		BigInteger bi_user_id = BigInteger.valueOf(Long.parseLong(user_id));
 		BigInteger bi_colourpalette_id = BigInteger.valueOf(Long.parseLong(colourpalette_id));
 		
 		FavouriteColourPalette found_palette;
 		
 		if((found_palette = getFavouriteColourPaletteRefObj(bi_user_id, bi_colourpalette_id)) != null) {
 			
 			ColourConnection.Users.User user_with_favourite_colourpalette = getUserObj(bi_user_id);
 			
 			user_with_favourite_colourpalette.getFavouriteColourpalettes().getFavouriteColourpalette().remove(found_palette);
 			marshall_cc();
 			return true;
 		}
 		
 		return false;
 	}
 	
 	
 	/**
 	 * Get a marshaleld structure representing a list of all favourite colurplattes of a certain user
 	 * 
 	 * @param user_id user id
 	 * @return marshalled structure
 	 */
 	public String getUserFavouriteColourPalettes(String user_id) {
 		
 		BigInteger bi_user_id = BigInteger.valueOf(Long.parseLong(user_id));
 		String marshalled_str = null;
 		
 		ColourConnection.Users.User found_user;
 		
 		if((found_user = getUserObj(bi_user_id)) != null) {
 			marshalled_str = marshall(found_user.getFavouriteColourpalettes());
 		}
 		
 		return marshalled_str;
 	}
 	
 	
 	/**
 	 * Get a marshalled structure of a cerntain colour
 	 * 
 	 * @param colour_id colour id
 	 * @return marshalled structure
 	 */
 	public String getColour(String colour_id) {
 		
 		String marshalled_str = null;
 		List<ColourConnection.Colours.Colour> colour_arr = cc_base.getColours().getColour();
 		
 		for(ColourConnection.Colours.Colour curr_colour: colour_arr) {
 			if(curr_colour.getId().equalsIgnoreCase(colour_id)) {
 				
 				Colour found_colour = new Colour();
 				
 				found_colour.setId(curr_colour.getId());
 				found_colour.setCreator(curr_colour.getCreator());
 				found_colour.setDateOfCreation(curr_colour.getDateOfCreation());
 				
 				marshalled_str = marshall(found_colour);
 				break;
 			}
 		}
 		
 		return marshalled_str;
 	}
 	
 	
 	/**
 	 * Create a new entry representing a colour ressource
 	 * 
 	 * @param colour_data colour data
 	 * @return id of the created colour
 	 */
 	public String createColour(String colour_data) {
 		
 		Colour given_colour = (Colour)unmarshall(colour_data, Colour.class);
 		ColourConnection.Colours.Colour cc_colour = new ColourConnection.Colours.Colour();
 		
 		// check if colour already exists
 		for(ColourConnection.Colours.Colour curr_colour: cc_base.getColours().getColour()) {
 
 			if(curr_colour.getId().equalsIgnoreCase(given_colour.getId()))
 				return null;
 		}
 
 		// check if creator exists
 		if(		given_colour.getCreator().getId() == null
 			||	getUserObj(given_colour.getCreator().getId()) == null)
 			return null;
 		
 		cc_colour.setId(given_colour.getId());
 		
 		Ref creator_ref = new Ref();
 		creator_ref.setId(given_colour.getCreator().getId());
 		creator_ref.setRef("/user/" + creator_ref.getId().toString());
 		
 		cc_colour.setCreator(creator_ref);
 		
 		// date given by the client
 		//cc_colour.setDateOfCreation(given_colour.getDateOfCreation());
 		
 		// date set by the system; more reliable
 		GregorianCalendar c = new GregorianCalendar();
 		c.setTime(new Date());
 
 		XMLGregorianCalendar currGregCal;
 		try {
 			currGregCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
 			cc_colour.setDateOfCreation(currGregCal);
 		} catch (DatatypeConfigurationException e) {}
 		
 		cc_colour.setComments(new ColourConnection.Colours.Colour.Comments());
 		
 		cc_base.getColours().getColour().add(cc_colour);
 		
 		marshall_cc();
 		
 		return cc_colour.getId();
 	}
 	
 	
 	/**
 	 * Create an entry representing a comment ressource for a certain colour
 	 * 
 	 * @param colour_id colour id
 	 * @param comment_data xml structure with the data of teh comment to be created 
 	 * @return id of the created comment
 	 */
 	public String createColourComment(String colour_id, String comment_data) {
 		
 		Comment given_comment = (Comment)unmarshall(comment_data, Comment.class);
 		
 		if(given_comment.getCreator().getId() == null)
 			return null;
 		
 		ColourConnection.Colours.Colour found_colour = getColourObj(colour_id);
 		
 		if(found_colour == null)
 			return null;
 		
 		int new_id = 1;
 		int arr_size = found_colour.getComments().getComment().size();
 		
 		if(arr_size > 0) {
 			Comment last_comment = found_colour.getComments().getComment().get(arr_size-1);
 			new_id = last_comment.getId().intValue() + 1;
 		}
 		
 		// date given by the client
 		//given_comment.getDateOfCreation();
 		
 		// date set by the system; more reliable
 		GregorianCalendar c = new GregorianCalendar();
 		c.setTime(new Date());
 
 		XMLGregorianCalendar currGregCal;
 		try {
 			currGregCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
 			given_comment.setDateOfCreation(currGregCal);
 		} catch (DatatypeConfigurationException e) {}
 		
 		found_colour.getComments().getComment().add(given_comment);
 		
 		marshall_cc();
 		
 		return String.valueOf(new_id);
 	}
 	
 	
 	/**
 	 * Delete an entry representing a colour ressource of a certain colour
 	 * 
 	 * @param colour_id id of the colour the comment is connected to
 	 * @param comment_id id of the comment to be deleted
 	 * @return success or failure
 	 */
 	public boolean deleteColourComment(String colour_id, String comment_id) {
 		
 		BigInteger bi_comment_id = BigInteger.valueOf(Long.parseLong(comment_id));
 		
 		ColourConnection.Colours.Colour found_colour;
 		if((found_colour = getColourObj(colour_id)) != null) {
 			for(Comment curr_comment: found_colour.getComments().getComment()) {
 				if(curr_comment.getId().equals(bi_comment_id)) {
 					
 					found_colour.getComments().getComment().remove(curr_comment);
 					marshall_cc();
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	
 	/**
 	 * Get a marshalled structure representing list of all comments connected to a certain colour
 	 * 
 	 * @param colour_id colour id
 	 * @return marshalled structure
 	 */
 	public String getColourComments(String colour_id) {
 		
 		ColourConnection.Colours.Colour found_colour;
 		if((found_colour = getColourObj(colour_id)) != null) {
 			Comments comments_list = new Comments();
 
 			for(Comment curr_comment: found_colour.getComments().getComment()) {
 				comments_list.getComment().add(curr_comment);
 			}
 			
 			return marshall(comments_list);
 		}
 		
 		return null;
 	}
 	
 	
 	/**
 	 * Get a marshalled structure representing a list of all existing colours
 	 * 
 	 * @return marshalled structure
 	 */
 	public String getColours() {
 		
 		ColourList colours_arr = new ColourList();
 		
 		for(ColourConnection.Colours.Colour curr_colour: cc_base.getColours().getColour()) {
 			
 			ColourRef new_colour_ref = new ColourRef();
 			new_colour_ref.setId(curr_colour.getId());
 			new_colour_ref.setRef("/colour/" + curr_colour.getId().toString());
 			
 			colours_arr.getColour().add(new_colour_ref);
 		}
 		
 		return marshall(colours_arr);
 	}
 	
 	
 	/**
 	 * Get a marshalled structure representing a colourpalette
 	 * 
 	 * @param colourpalette_id colurpalette id
 	 * @return marshalled structure
 	 */
 	public String getColourPalette(String colourpalette_id) {
 		
 		String marshalled_str = null;
 		BigInteger bi_colourpalette_id = BigInteger.valueOf(Long.parseLong(colourpalette_id));
 		
 		for(ColourConnection.Colourpalettes.Colourpalette curr_colourpalette: cc_base.getColourpalettes().getColourpalette()) {
 			if(curr_colourpalette.getId().equals(bi_colourpalette_id)) {
 				
 				ColourPalette found_colourpalette = new ColourPalette();
 
 				found_colourpalette.setId(curr_colourpalette.getId());
 				found_colourpalette.setCreator(curr_colourpalette.getCreator());
 				found_colourpalette.setUsedColours(curr_colourpalette.getUsedColours());
 				
 				marshalled_str = marshall(found_colourpalette);
 				break;
 			}
 		}
 		
 		return marshalled_str;
 	}
 	
 	
 	/**
 	 * Create a new entry representing a colourpalette resource
 	 * 
 	 * @param colourpalette_data XML structure with all the data needed to create a new colourpalette
 	 * @return new id of the created colourpalette
 	 */
 	public BigInteger createColourPalette(String colourpalette_data) {
 		
 		ColourPalette given_colourpalette = (ColourPalette)unmarshall(colourpalette_data, ColourPalette.class);
 		ColourConnection.Colourpalettes.Colourpalette cc_colourpalette = new ColourConnection.Colourpalettes.Colourpalette();
 		
 		// check if colourpalette already exists
 		// TODO: check for the same used colours? oh god no...
 		
 		// check if creator exists
 		if(		given_colourpalette.getCreator().getId() == null
 			||	getUserObj(given_colourpalette.getCreator().getId()) == null)
 			return null;
 		
 		// giving the colourpalette an id
 		int new_id = 1;
 		int arr_size = cc_base.getColourpalettes().getColourpalette().size();
 		
 		if(arr_size > 0) {
 			ColourConnection.Colourpalettes.Colourpalette last_colourpalette = cc_base.getColourpalettes().getColourpalette().get(arr_size-1);
 			new_id = last_colourpalette.getId().intValue() + 1;
 		}
 		
 		cc_colourpalette.setId(BigInteger.valueOf(new_id));
 		
 		// set creator
 		Ref creator_ref = new Ref();
 		creator_ref.setId(given_colourpalette.getCreator().getId());
 		creator_ref.setRef("/user/" + creator_ref.getId().toString());
 		
 		cc_colourpalette.setCreator(creator_ref);
 		
 		// add creation
 		Ref colourpalette_ref = new Ref();
 		colourpalette_ref.setId(cc_colourpalette.getId());
 		colourpalette_ref.setRef("/colourpalette/" + cc_colourpalette.getId().toString());
 		getUserObj(given_colourpalette.getCreator().getId()).getCreations().getColourpalette().add(colourpalette_ref);
 		
 		// date given by the client
 		//cc_colourpalette.setDateOfCreation(given_colourpalette.getDateOfCreation());
 		
 		// date set by the system; more reliable
 		GregorianCalendar c = new GregorianCalendar();
 		c.setTime(new Date());
 		
 		XMLGregorianCalendar currGregCal = null;
 		try {
 			currGregCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
 		} catch (DatatypeConfigurationException e) {}
 
 		cc_colourpalette.setDateOfCreation(currGregCal);
 		
 		// create an empty creations list
 		cc_colourpalette.setUsedColours(new ColourPalette.UsedColours());
 		
 		// create an empty comments list
 		cc_colourpalette.setComments(new ColourConnection.Colourpalettes.Colourpalette.Comments());
 		
 		// check if the used colours exist
 		for(ColourRef curr_colour: given_colourpalette.getUsedColours().getColour()) {
 			
 			if(getColourObj(curr_colour.getId()) == null) {
 
 				// the colour doesn't exist; so we're gonna create it now
 				ColourConnection.Colours.Colour new_colour = new ColourConnection.Colours.Colour();
 				new_colour.setId(curr_colour.getId());
 				new_colour.setCreator(creator_ref);
 				new_colour.setDateOfCreation(currGregCal);
 				new_colour.setComments(new ColourConnection.Colours.Colour.Comments());
 				
 				cc_base.getColours().getColour().add(new_colour);
 			}
 			
 			cc_colourpalette.getUsedColours().getColour().add(curr_colour);
 		}
 		
 		// add the colourpalette to the list
 		cc_base.getColourpalettes().getColourpalette().add(cc_colourpalette);
 		
 		marshall_cc();
 		
 		return cc_colourpalette.getId();
 	}
 	
 	
 	/**
 	 * Get a marshalled structure representing a list of alle existing colourpalettes
 	 * 
 	 * @return marshalled structure
 	 */
 	public String getColourPalettes() {
 		
 		ColourPaletteList users_arr = new ColourPaletteList();
 		
 		
 		for(ColourConnection.Colourpalettes.Colourpalette curr_colourpalette: cc_base.getColourpalettes().getColourpalette()) {
 			
 			Ref new_colourpalette_ref = new Ref();
 			
 			new_colourpalette_ref.setId(curr_colourpalette.getId());
 			new_colourpalette_ref.setRef("/colourpalette/" + curr_colourpalette.getId().toString());
 			
 			users_arr.getColourpalette().add(new_colourpalette_ref);
 		}
 		
 		return marshall(users_arr);
 	}
 
 	
 	/**
 	 * Create a new entry representing a comment ressource connected to a certain colourpalette
 	 * 
 	 * @param colourpalette_id
 	 * @param comment_data XML structure with all the data needed to create a new comment connected to a certain colourpalette
 	 * @return comment id
 	 */
 	public String createColourPaletteComment(String colourpalette_id, String comment_data) {
 		
 		BigInteger bi_colourpalette_id = BigInteger.valueOf(Long.parseLong(colourpalette_id));
 		Comment given_comment = (Comment)unmarshall(comment_data, Comment.class);
 		
 		if(given_comment.getCreator().getId() == null)
 			return null;
 		
 		ColourConnection.Colourpalettes.Colourpalette found_colourpalette = getColourpaletteObj(bi_colourpalette_id);
 		
 		if(found_colourpalette == null)
 			return null;
 		
 		int new_id = 1;
 		int arr_size = found_colourpalette.getComments().getComment().size();
 		
 		if(arr_size > 0) {
 			Comment last_comment = found_colourpalette.getComments().getComment().get(arr_size-1);
 			new_id = last_comment.getId().intValue() + 1;
 		}
 		
 		// date given by the client
 		//given_comment.getDateOfCreation();
 		
 		// date set by the system; more reliable
 		GregorianCalendar c = new GregorianCalendar();
 		c.setTime(new Date());
 
 		XMLGregorianCalendar currGregCal;
 		try {
 			currGregCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
 			given_comment.setDateOfCreation(currGregCal);
 		} catch (DatatypeConfigurationException e) {}
 		
 		found_colourpalette.getComments().getComment().add(given_comment);
 		
 		marshall_cc();
 		
 		return String.valueOf(new_id);
 	}
 	
 	
 	/**
 	 * Delete an entry representing a comment resource connected to a certain colourpalette
 	 * 
 	 * @param colourpalette_id colourpalette id
 	 * @param comment_id comment id
 	 * @return success or failure
 	 */
 	public boolean deleteColourPaletteComment(String colourpalette_id, String comment_id) {
 		
 		BigInteger bi_colourpalette_id = BigInteger.valueOf(Long.parseLong(colourpalette_id));
 		BigInteger bi_comment_id = BigInteger.valueOf(Long.parseLong(comment_id));
 		
 		ColourConnection.Colourpalettes.Colourpalette found_colourpalette;
 		if((found_colourpalette = getColourpaletteObj(bi_colourpalette_id)) != null) {
 			for(Comment curr_comment: found_colourpalette.getComments().getComment()) {
 				if(curr_comment.getId().equals(bi_comment_id)) {
 					
 					found_colourpalette.getComments().getComment().remove(curr_comment);
 					marshall_cc();
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	
 	/**
 	 * Get a marshalled structure representing a list of all comments connected to a certain colourpalette
 	 * 
 	 * @param colourpalette_id colourpalette id
 	 * @return marshalled structure
 	 */
 	public String getColourPaletteComments(String colourpalette_id) {
 		
 		BigInteger bi_colourpalette_id = BigInteger.valueOf(Long.parseLong(colourpalette_id));
 		
 		Comments comments_list = new Comments();
 		
 		ColourConnection.Colourpalettes.Colourpalette found_colourpalette;
 		if((found_colourpalette = getColourpaletteObj(bi_colourpalette_id)) != null) {
 			for(Comment curr_comment: found_colourpalette.getComments().getComment()) {
 				comments_list.getComment().add(curr_comment);
 			}
 		}
 		
 		System.out.println(marshall(comments_list));
 		
 		return marshall(comments_list);
 	}
 	
 	
 	
 	/**
 	 * Marshall the xml structure and write the output to the file used as a database
 	 */
 	private void marshall_cc() {
 		try {
 			Marshaller m = cc_context.createMarshaller();
 			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 			m.marshal(new ObjectFactory().createColourConnection(cc_base), cc_file);
 		} catch (JAXBException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * Unmarshall given xml data
 	 * 
 	 * @param str raw xml data
 	 * @param c class used as base to unmarshall the data
 	 * @return JAXBElement
 	 */
 	private Object unmarshall(String str, Class<?> c) {
 		
 		Object element = null;
 		
 		try {
 			JAXBContext context = JAXBContext.newInstance(c);
 			Unmarshaller um = context.createUnmarshaller();
 			element = um.unmarshal(new StreamSource(new StringReader(str)), c).getValue();
 		} catch (JAXBException e) {
 			e.printStackTrace();
 		}
 		
 		return element;
 	}
 	
 	
 	/**
 	 * Marshall a xml structure (JAXBElement)
 	 * 
 	 * @param instance instance of a JAXBElement
 	 * @return marshalled structure
 	 */
 	private String marshall(Object instance) {
 		
 		if(instance == null)
 			return null;
 		
 		String str = null;
 		
 		ObjectFactory of = new ObjectFactory();
 		JAXBElement<?> jaxbe = null;
 		
 		switch(instance.getClass().getSimpleName()) {
 			case "Colour":
 				jaxbe = of.createColour((Colour)instance);
 				break;
 			case "UserList":
 				jaxbe = of.createUsers((UserList)instance);
 				break;
 			case "ColourPalette":
 				jaxbe = of.createColourpalette((ColourPalette)instance);
 				break;
 			case "ColourPaletteList":
 				jaxbe = of.createColourpalettes((ColourPaletteList)instance);
 				break;
 			case "Followers":
 				jaxbe = of.createFollowers((Followers)instance);
 				break;
 			case "FavouriteColourPaletteList":
 				jaxbe = of.createFavouriteColourpalettes((FavouriteColourPaletteList)instance);
 				break;
 			case "FavouriteColourPalette":
 				jaxbe = of.createFavouriteColourpalette((FavouriteColourPalette)instance);
 				break;
 			case "FavouriteColourList":
 				jaxbe = of.createFavouriteColours((FavouriteColourList)instance);
 				break;
 			case "FavouriteColour":
 				jaxbe = of.createFavouriteColour((FavouriteColour)instance);
 				break;
 			case "Comment":
 				jaxbe = of.createComment((Comment)instance);
 				break;
 			case "Comments":
 				jaxbe = of.createComments((Comments)instance);
 				break;
 			case "User":
 				jaxbe = of.createUser((User)instance);
 				break;
 			case "ColourList":
 				jaxbe = of.createColours((ColourList)instance);
 				break;
 			default:
 				return null;
 		}
 		
 		try {
 			JAXBContext context = JAXBContext.newInstance(instance.getClass());
 			Marshaller m = context.createMarshaller();
 			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 			
 			ByteArrayOutputStream string_out = new ByteArrayOutputStream();
 			
 			m.marshal(jaxbe, string_out);
 			
 			str = string_out.toString();
 		} catch (JAXBException e) {
 			e.printStackTrace();
 		}
 		
 		return str;
 	}
 	
 	
 	/**
 	 * Get a reference to a certain FavouriteColour object
 	 * 
 	 * @param user_id user id
 	 * @param colour_id colour id
 	 * @return reference to a FavouriteColour object
 	 */
 	private FavouriteColour getFavouriteColourRefObj(BigInteger user_id , String colour_id) {
 		ColourConnection.Users.User found_user;
 		
 		if((found_user = getUserObj(user_id)) != null) {
 			
 			for(FavouriteColour curr_favourite_colour:
 					found_user.getFavouriteColours().getFavouriteColour()) {
 				
 				if(curr_favourite_colour.getId().equalsIgnoreCase(colour_id))
 					return curr_favourite_colour;
 			}
 		}
 		
 		return null;
 	}
 	
 	
 	/**
 	 * Get a reference to a certain FavouriteColourPalette object
 	 * 
 	 * @param user_id user id
 	 * @param colourpalette_id colour id
 	 * @return reference to a FavouriteColourPalette object
 	 */
 	private FavouriteColourPalette getFavouriteColourPaletteRefObj(BigInteger user_id , BigInteger colourpalette_id) {
 		ColourConnection.Users.User found_user;
 		
 		if((found_user = getUserObj(user_id)) != null) {
 			
 			for(FavouriteColourPalette curr_favourite_colourpalette:
 					found_user.getFavouriteColourpalettes().getFavouriteColourpalette()) {
 				
 				if(curr_favourite_colourpalette.getId().equals(colourpalette_id)) {
 					return curr_favourite_colourpalette;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	
 	/**
 	 * Get a reference to a certain Follower object
 	 * 
 	 * @param user_id user id
 	 * @param follower_id colour id
 	 * @return reference to a Follower object
 	 */
 	private Follower getFollowerObj(BigInteger user_id, BigInteger follower_id) {
 		
 		ColourConnection.Users.User found_user;
 		
 		if((found_user = getUserObj(user_id)) != null) {
 			
 			for(Follower curr_follower: found_user.getFollowers().getFollower()) {
 				
 				if(curr_follower.getId().equals(follower_id))
 					return curr_follower;
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Get a reference to a certain User object
 	 * 
 	 * @param id user id
 	 * @return reference to a User object
 	 */
 	private ColourConnection.Users.User getUserObj(BigInteger id) {
 		for(ColourConnection.Users.User curr_user: cc_base.getUsers().getUser()) {
 			if(curr_user.getId().equals(id)) {
 				return curr_user;
 			}
 		}
 		
 		return null;
 	}
 	
 	
 	/**
 	 * Get a reference to a certain Colour object
 	 * 
 	 * @param id user id
 	 * @return reference to a Colour object
 	 */
 	private ColourConnection.Colours.Colour getColourObj(String id) {
 		
 		for(ColourConnection.Colours.Colour curr_colour: cc_base.getColours().getColour()) {
 			
 			if(curr_colour.getId().equalsIgnoreCase(id))
 				return curr_colour;
 		}
 		
 		return null;
 	}
 	
 	
 	/**
 	 * Get a reference to a certain Colourpalette object
 	 * 
 	 * @param id user id
 	 * @return reference to a Colourpalette object
 	 */
 	private ColourConnection.Colourpalettes.Colourpalette getColourpaletteObj(BigInteger id) {
 		for(ColourConnection.Colourpalettes.Colourpalette curr_colourpalette: cc_base.getColourpalettes().getColourpalette()) {
 			if(curr_colourpalette.getId().equals(id))
 				return curr_colourpalette;
 		}
 		
 		return null;
 	}
 	
 	
 	/**
 	 * Allow only one instance of this class; only one instance is created (Singleton)
 	 * 
 	 * @return instance of this class
 	 */
 	public static DataHandler getInstance() {
 		if(instance == null) {
 			instance = new DataHandler();
 		}
 		
 		return instance;
 	}
 	
 }
