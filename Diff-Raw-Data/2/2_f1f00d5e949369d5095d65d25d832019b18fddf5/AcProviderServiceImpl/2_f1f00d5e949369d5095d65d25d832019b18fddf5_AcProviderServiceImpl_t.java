 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eu.trentorise.smartcampus.ac.provider.services;
 
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.jws.WebService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import eu.trentorise.smartcampus.ac.provider.AcProviderService;
 import eu.trentorise.smartcampus.ac.provider.AcServiceException;
 import eu.trentorise.smartcampus.ac.provider.model.Attribute;
 import eu.trentorise.smartcampus.ac.provider.model.Authority;
 import eu.trentorise.smartcampus.ac.provider.model.User;
 import eu.trentorise.smartcampus.ac.provider.repository.AcDao;
 
 /**
  * 
  * @author Viktor Pravdin
  */
 @WebService(endpointInterface = "eu.trentorise.smartcampus.ac.provider.AcProviderService")
 @Service
 public class AcProviderServiceImpl implements AcProviderService {
 
 	@Autowired
 	private AcDao dao;
 
 	@Autowired
 	private CreationWrapper creationWrapper;
 
 	private SecureRandom random;
 
 	@PostConstruct
 	private void init() {
 		random = new SecureRandom();
 	}
 
 	@Override
 	public User createUser(String authToken, long expDate,
 			List<Attribute> attributes) throws AcServiceException {
 		User user = new User();
 		user.setAuthToken(authToken);
 		user.setExpTime(expDate);
 		user.setAttributes(attributes);
 		try {
 			creationWrapper.create(user);
 		} catch (Exception e) {
 			throw new AcServiceException(e);
 		}
 		return user;
 	}
 
 	@Override
 	public boolean removeUser(String authToken) throws AcServiceException {
 		User user = dao.readUser(authToken);
 		if (user != null) {
 			return dao.delete(user);
 		}
 		return false;
 	}
 
 	@Override
 	public User getUserByToken(String authToken) throws AcServiceException {
 		return dao.readUser(authToken);
 	}
 
 	@Override
 	public List<User> getUsersByAttributes(List<Attribute> attributes)
 			throws AcServiceException {
 		return dao.readUsers(attributes);
 	}
 
 	/**
 	 * Updates the user data with the given values. If a new value for an
 	 * already existing value is not specified then the old value remains
 	 * untouched.
 	 * 
 	 * @param userId
 	 *            The ID of the user. Must not be null
 	 * @param authToken
 	 *            The new authentication token or null if the token shouldn't be
 	 *            updated
 	 * @param expTime
 	 *            The new expiration time or null if it shouldn't be updated.
 	 *            Must not be null if a new authentication token is present.
 	 *            Ignored if the auth token is null.
 	 * @param attributes
 	 *            The map of the attributes. The existing attributes are
 	 *            updated, the new ones are added, the old ones are kept intact.
 	 * @throws IllegalArgumentException
 	 *             if the user with the given ID doesn't exist or if the
 	 *             expiration time is not specified for an authentication token
 	 */
 	@Override
 	public void updateUser(long userId, String authToken, Long expTime,
 			List<Attribute> attributes) throws AcServiceException {
 		User user = dao.readUser(userId);
 		if (user == null) {
 			throw new IllegalArgumentException(
 					"The user with that ID doesn't exist");
 		}
 		if (authToken != null) {
 			if (expTime == null) {
 				throw new IllegalArgumentException(
 						"The expiration time is not specified");
 			}
 			user.setAuthToken(authToken);
		}
		if (expTime != null) {
 			user.setExpTime(expTime);
 		}
 		List<Attribute> tmp = new LinkedList<Attribute>();
 		for (Attribute oldA : user.getAttributes()) {
 			Attribute remove = null;
 			for (Attribute newA : tmp) {
 				if (oldA.getAuthority().equals(newA.getAuthority())
 						&& oldA.getKey().equals(newA.getKey())) {
 					oldA.setValue(newA.getValue());
 					remove = newA;
 					break;
 				}
 			}
 			if (remove != null) {
 				tmp.remove(remove);
 			}
 		}
 		user.getAttributes().addAll(tmp);
 		dao.update(user);
 	}
 
 	@Override
 	public boolean isValidUser(String authToken) throws AcServiceException {
 		long time = System.currentTimeMillis();
 		User user = dao.readUser(authToken);
 		return user != null && ((user.getExpDate() - time) > 0);
 	}
 
 	/**
 	 * Fetches the user attributes that match the given authority and the key.
 	 * If neither is given then it will return all attributes of the user, if
 	 * the authority is given then it will return only the attributes of that
 	 * authority and if the key is also given then it will return only the
 	 * attributes of that authority that have the given key.
 	 * 
 	 * @param authToken
 	 *            The authentication token of the user
 	 * @param authority
 	 *            The authority name
 	 * @param key
 	 *            The key of the attribute
 	 * @return The set of attributes that match the parameters
 	 */
 	@Override
 	public List<Attribute> getUserAttributes(String authToken,
 			String authority, String key) throws AcServiceException {
 		User user = getUserByToken(authToken);
 		List<Attribute> attrs = new ArrayList<Attribute>();
 		if (authority != null) {
 			for (Attribute a : user.getAttributes()) {
 				if (authority.equals(a.getAuthority().getName())) {
 					if (key != null && !key.equals(a.getKey()))
 						continue;
 					attrs.add(a);
 				}
 			}
 		} else {
 			attrs.addAll(user.getAttributes());
 		}
 		return attrs;
 	}
 
 	@Override
 	public Collection<Authority> getAuthorities() throws AcServiceException {
 		return dao.readAuthorities();
 	}
 
 	@Override
 	public Authority getAuthorityByName(String name) throws AcServiceException {
 		return dao.readAuthorityByName(name);
 	}
 
 	@Override
 	public Authority getAuthorityByUrl(String name) throws AcServiceException {
 		return dao.readAuthorityByUrl(name);
 	}
 
 	@Override
 	public void createAuthority(Authority auth) throws AcServiceException {
 		dao.create(auth);
 	}
 
 	@Override
 	public String generateAuthToken() throws AcServiceException {
 		// TODO: check if already used
 		return new BigInteger(256, random).toString(16);
 	}
 
 	@Override
 	public void updateAuthority(Authority auth) throws AcServiceException {
 		dao.update(auth);
 	}
 
 	@Override
 	public void setAttribute(long userId, Attribute attribute)
 			throws AcServiceException {
 		if (attribute == null) {
 			throw new IllegalArgumentException("Attribute is not specified");
 
 		}
 
 		User user = dao.readUser(userId);
 		if (user == null) {
 			throw new IllegalArgumentException(
 					"The user with that ID doesn't exist");
 		}
 
 		int indexAttribute = user.getAttributes().indexOf(attribute);
 
 		if (indexAttribute == -1) {
 			user.getAttributes().add(attribute);
 		} else {
 			user.getAttributes().get(indexAttribute)
 					.setValue(attribute.getValue());
 		}
 		dao.update(user);
 	}
 }
