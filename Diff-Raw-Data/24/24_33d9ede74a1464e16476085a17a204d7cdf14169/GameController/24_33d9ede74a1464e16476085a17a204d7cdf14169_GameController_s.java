 package com.crystalclash.controllers;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import com.crystalclash.audio.AudioManager.SOUND;
 import com.crystalclash.entities.Unit;
 import com.crystalclash.entities.User;
 import com.crystalclash.networking.ServerDriver;
 import com.crystalclash.renders.GameEngine;
 import com.crystalclash.renders.helpers.ui.BaseBox.BoxButtons;
 import com.crystalclash.renders.helpers.ui.MessageBox;
 import com.crystalclash.util.Profile;
 import com.crystalclash.util.ProfileService;
 import com.crystalclash.util.SharedDataPrefReader;
 import com.crystalclash.util.UnitStatsPrefReader;
 
 public class GameController {
 
 	private static boolean dataLoaded = false;
 	private static User currentUser;
 	public static ProfileService profileService = new ProfileService();
 	private static Hashtable<String, int[]> unitValues;
 
 	public static float UNIT_MAX_HP = 0;
 	public static float UNIT_MAX_ATTACK = 0;
 	public static float UNIT_MAX_STEPS = 0;
 	public static int MAX_UNIT_PER_PLAYER = 0;
 	public static String SERVER_URL = "";
	public static int emblemCount = 0;
 
 	public static void setUser(User user) {
 		currentUser = user;
 	}
 
 	public static User getUser() {
 		return currentUser;
 	}
 
 	public static void loadSharedStats() {
 		if (!dataLoaded) {
 			unitValues = UnitStatsPrefReader.load("data/prefs/stats.pref");
 
 			String[] shared = SharedDataPrefReader.load("data/prefs/shared.pref");
 			UNIT_MAX_HP = Float.parseFloat(shared[0]);
 			UNIT_MAX_ATTACK = Float.parseFloat(shared[1]);
 			UNIT_MAX_STEPS = Float.parseFloat(shared[2]);
 			MAX_UNIT_PER_PLAYER = Integer.parseInt(shared[3]);
 			SERVER_URL = shared[4];
			emblemCount = Integer.parseInt(shared[5]);
 
 			dataLoaded = true;
 		}
 	}
 
 	public static String getUnitElement(String unitName) {
 		switch (unitValues.get(unitName)[0]) {
 		case Unit.ELEMENT_FIRE:
 			return "fire";
 		case Unit.ELEMENT_EARTH:
 			return "earth";
 		case Unit.ELEMENT_WIND:
 			return "wind";
 		case Unit.ELEMENT_WATER:
 			return "water";
 		case Unit.ELEMENT_DARKNESS:
 			return "darkness";
 		default:
 			return "fire";
 		}
 	}
 
 	public static int getUnitElementIndex(String unitName) {
 		return unitValues.get(unitName)[0];
 	}
 
 	public static int getUnitTypeIndex(String unitName) {
 		return unitValues.get(unitName)[1];
 	}
 
 	public static int getUnitLife(String unitName) {
 		return unitValues.get(unitName)[2];
 	}
 
 	public static int getUnitLifeInScale(String unitName) {
 		return (int) ((getUnitLife(unitName) * 10) / UNIT_MAX_HP);
 	}
 
 	public static int getUnitDamage(String unitName) {
 		return unitValues.get(unitName)[3];
 	}
 
 	public static int getUnitAttackInScale(String unitName) {
 		return (int) ((getUnitDamage(unitName) * 10) / UNIT_MAX_ATTACK);
 	}
 
 	public static int getUnitSpeed(String unitName) {
 		return unitValues.get(unitName)[4];
 	}
 
 	public static int getUnitSpeedInScale(String unitName) {
 		return (int) ((getUnitSpeed(unitName) * 10) / UNIT_MAX_STEPS);
 	}
 
 	public static int getUnitRange(String unitName) {
 		return unitValues.get(unitName)[5];
 	}
 
 	public static int getUnitSoundCount(String unitName, SOUND type) {
 		switch (type) {
 		case chose_attack:
 			return unitValues.get(unitName)[6];
 		case chose_move:
 			return unitValues.get(unitName)[7];
 		case chose_defend:
 			return unitValues.get(unitName)[8];
 		case place:
 			return unitValues.get(unitName)[9];
 		case select:
 			return unitValues.get(unitName)[10];
 		case attack:
 			return unitValues.get(unitName)[11];
 		default:
 			return -1;
 		}
 	}
 
 	public static Enumeration<String> getUnitNames() {
 		return unitValues.keys();
 	}
 
 	public static void tryLogin() {
 		Profile p = profileService.retrieveProfile();
 		logIn(p.getUserEmail(), p.getUserPassword());
 	}
 
 	public static boolean canLogin() {
 		Profile p = profileService.retrieveProfile();
 		return p.hasUserAndPassword();
 	}
 
 	public static boolean isTutorialDone() {
 		Profile p = profileService.retrieveProfile();
 		return p.isTutorialDone();
 	}
 
 	public static void resetProfile() {
 		Profile p = profileService.retrieveProfile();
 		p.reset();
 	}
 
 	public static void setTutorialDone() {
 		Profile p = profileService.retrieveProfile();
 		p.setTutorialDone();
 	}
 
 	// TODO: Borrar, es solo para probar
 	public static void setTutorialNotDone() {
 		Profile p = profileService.retrieveProfile();
 		p.setTutorialNotDone();
 	}
 
 	public static void saveProfile() {
 		profileService.persist();
 	}
 
 	public static void logIn(String email, String password) {
 		MessageBox.build()
 				.setMessage("game_authenticating", BoxButtons.None)
 				.setCallback(null)
 				.show();
 		ServerDriver.sendLogIn(email, password);
 	}
 
 	public static void signIn(String email, String password) {
 		MessageBox.build()
 				.setMessage("world_creating_account", BoxButtons.None)
 				.setCallback(null)
 				.show();
 		ServerDriver.sendSignIn(email, password);
 	}
 
 	public static void logInSuccess(String userId, String name, String email, String password, int emblem, int v, int d, int l) {
 		Profile p = profileService.retrieveProfile();
 		p.setUserEmail(email);
 		p.setUserPassword(password);
 		GameController.saveProfile();
 		setUser(new User(userId, email, name, emblem, v, d, l));
 		GameEngine.getInstance().openMenuGames();
 	}
 
 	public static void signInSuccess(String userId, String name, String email, String password, int emblem, int v, int d, int l) {
 		profileService.retrieveProfile().reset();
 		logInSuccess(userId, name, email, password, emblem, v, d, l);
 	}
 
 	public static void logOut() {
 		profileService.retrieveProfile().setUserPassword("");
 		profileService.persist();
 		currentUser = null;
 		GameEngine.getInstance().openMenuLogIn();
 	}
 }
