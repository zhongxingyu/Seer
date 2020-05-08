 package edu.brown.cs32.MFTG.networking;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.channels.FileLock;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import edu.brown.cs32.MFTG.tournament.Profile;
 
 public class ProfileManager {
 	private final String DEFAULT_PATH = ".profiles.mf";
 	private String _filePath;
 
 	Map<String, Profile> _profiles;
 
 	public ProfileManager(){
 		_filePath = DEFAULT_PATH;
 		buildProfileMap();
 	}
 
 	public ProfileManager(String filePath){
 		_filePath = filePath;
 		buildProfileMap();
 	}
 
 	/**
 	 * Builds the profile map from the given input file
 	 */
 	private void buildProfileMap(){
 		ObjectMapper oMapper = new ObjectMapper();
 
 		try (RandomAccessFile raf = new RandomAccessFile(_filePath, "rw")){
 			try (FileLock fLock = raf.getChannel().lock()){
 				String json = raf.readLine();
 
 				Map<String, Profile> profiles = oMapper.readValue(json, new TypeReference<Map<String, Profile>>() {});
 
 				_profiles = profiles;
 			}
 
 		} catch (IOException e) {
 			_profiles = new HashMap<>();
 		}
 	}
 	
 	/**
 	 * Writes the current map of profiles to disk
 	 * @throws IOException
 	 */
 	private void writeProfiles() throws IOException{
 		String toWrite = (new ObjectMapper()).writeValueAsString(_profiles);
 
 		try (BufferedWriter bWriter = new BufferedWriter(new FileWriter(_filePath))){
 			bWriter.write(toWrite);
 			bWriter.write("\n");
 			bWriter.flush();
 		}
 	}
 	
 	public boolean saveProfiles(){
 		System.out.println("getting called");
 		ObjectMapper oMapper = new ObjectMapper();
 		
		System.out.println(_profiles.get("Test 1").getPlayer("balanced").getPropertyValue("kentucky avenue"));
 
 		try (RandomAccessFile raf = new RandomAccessFile(_filePath, "rw")){
 			try (FileLock fLock = raf.getChannel().lock()){
 				String json = raf.readLine();
 
 				Map<String, Profile> profiles = oMapper.readValue(json, new TypeReference<Map<String, Profile>>() {});
 
 				for (String key : _profiles.keySet())
 					profiles.put(key, _profiles.get(key));
 				
 				_profiles = profiles;
 				
				System.out.println(_profiles.get("Test 1").getPlayer("balanced").getPropertyValue("kentucky avenue"));
 				
 				String toWrite = oMapper.writeValueAsString(_profiles);
 				raf.seek(0);
 				raf.writeBytes(toWrite);
 				raf.writeByte('\n');
 			}
 
 		} catch (IOException e) {
 			assert(false);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Adds a profile to the profiles hashmap
 	 * @param profileName the key to use for insertion
 	 * @param p the profile to insert
 	 * @return true if it was possible to create a profile of the given name, and false otherwise
 	 */
 	public boolean addProfile(String profileName, Profile p){
 		if (_profiles.containsKey(profileName))
 			return false;
 
 		try (RandomAccessFile raf = new RandomAccessFile(_filePath, "rw")){
 			ObjectMapper oMapper = new ObjectMapper();
 
 			try (FileLock fLock = raf.getChannel().lock()){
 				String json = raf.readLine();
 
 				Map<String, Profile> profiles = oMapper.readValue(json, new TypeReference<Map<String, Profile>>() {});
 
 				if (profiles.containsKey(profileName))
 					return false;
 
 				profiles.put(profileName, p);
 				_profiles = profiles;
 
 				writeProfiles();
 			}
 
 		} catch (IOException e) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Deletes a profile from the hashmap
 	 * @param profileName the profile to delete
 	 * @return true if it was possible to write the deletion to disk, and false otherwise
 	 */
 	public boolean deleteProfile(String profileName){
 		
 		// make sure they can't delete stuff added recently by other players
 		if (!_profiles.containsKey(profileName))
 			return true;
 		
 		try (RandomAccessFile raf = new RandomAccessFile(_filePath, "rw")){
 			ObjectMapper oMapper = new ObjectMapper();
 
 			try (FileLock fLock = raf.getChannel().lock()){
 				String json = raf.readLine();
 
 				Map<String, Profile> profiles = oMapper.readValue(json, new TypeReference<Map<String, Profile>>() {});
 
 				profiles.remove(profileName);
 				_profiles = profiles;
 
 				writeProfiles();
 			}
 
 		} catch (IOException e) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Gets a profile from the hashmap
 	 * @param profileName the key for the profile to retreive
 	 * @return the Profile mapped to by profileName
 	 */
 	public Profile getProfile(String profileName){
 		return _profiles.get(profileName);
 	}
 
 	/**
 	 * Returns a set of the names of all the profiles
 	 * @return a set of the names of all the profiles
 	 */
 	public Set<String> getProfileNames(){
 		return _profiles.keySet();
 	}
 
 	/**
 	 * 
 	 * @param profile
 	 * @return set of player names
 	 */
 	public Set<String> getPlayerNames(String profile){
 		return _profiles.get(profile).getPlayerNames();
 	}
 
 	/**
 	 * Returns the number of profiles
 	 * @return the number of profiles
 	 */
 	public int numProfiles(){
 		return _profiles.size();
 	}
 }
