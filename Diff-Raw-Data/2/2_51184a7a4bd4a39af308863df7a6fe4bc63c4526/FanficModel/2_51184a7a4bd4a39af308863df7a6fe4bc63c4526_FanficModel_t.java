 package by.itransition.fanfic.model;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import by.itransition.fanfic.dao.ChapterDao;
 import by.itransition.fanfic.dao.FanficDao;
 import by.itransition.fanfic.dao.UserDao;
 import by.itransition.fanfic.dao.impl.ChapterDaoImpl;
 import by.itransition.fanfic.dao.impl.FanficDaoImpl;
 import by.itransition.fanfic.dao.impl.UserDaoImpl;
 import by.itransition.fanfic.model.bean.Chapter;
 import by.itransition.fanfic.model.bean.Fanfic;
 import by.itransition.fanfic.model.bean.User;
 
 public class FanficModel {
 
 	private static FanficModel fanficModel = new FanficModel();
 
 	private UserDao userDao = new UserDaoImpl();
 
 	private FanficDao fanficDao = new FanficDaoImpl();
 
 	private ChapterDao chapterDao = new ChapterDaoImpl();
 
 	private FanficModel() {
  
 	}
 
 	public static FanficModel getInstance() {
 		return fanficModel;
 	}
 
 	public void save(User user) {
 		userDao.save(user);
 	}
 
 	public void save(Fanfic fanfic) {
 		fanficDao.save(fanfic);
 	}
 	
 	public void save(Chapter chapter) {
 		chapterDao.save(chapter);
 	}
 	
 	public void removeFanficById(int id) {
 		fanficDao.removeFanficById(id);
 	}
 	
 	public User getUserById(int id) {
 		return userDao.getUserById(id);
 	}
 	
 	public User getUserByName(String username) {
 		for (User user : userDao.getUsers()) {
 			if (user.getUsername().equals(username)) {
 				return user;
 			}
 		}
 		return null;
 	}
 	
 	public List<Integer> getStatistic() {
 		List<Integer> answer = new ArrayList<Integer>();
 		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
 		Calendar calendar = Calendar.getInstance();
 		calendar.clear(Calendar.SECOND);
 		calendar.clear(Calendar.MILLISECOND);
 		for (int minutesBeforeNow = 0; minutesBeforeNow < 10; ++minutesBeforeNow) {
 			for (User user : userDao.getUsers()) {
 				Date userDate = user.getDateOfRegistration();
 				Calendar userCalendar = Calendar.getInstance();
 				userCalendar.setTime(userDate);
 				userCalendar.clear(Calendar.SECOND);
 				userCalendar.clear(Calendar.MILLISECOND);
 				if (userCalendar.getTime().equals(calendar.getTime())) {
					if (null == map.get(minutesBeforeNow)) {
 						map.put(minutesBeforeNow, 1);
 					} else {
 						map.put(minutesBeforeNow, map.get(minutesBeforeNow) + 1);
 					}
 				}
 			}
 			calendar.add(Calendar.MINUTE, -1);
 		}
 		for (int minutesBeforeNow = 0; minutesBeforeNow < 10; ++minutesBeforeNow) {
 			if (null != map.get(minutesBeforeNow)) {
 				answer.add(map.get(minutesBeforeNow));
 			} else {
 				answer.add(0);
 			}
 		}
 		return answer;
 	}
 	
 	public List<Fanfic> searchFanfics(String searchQuery) {
 		List<Fanfic> answer = new ArrayList<Fanfic>();
 		for (Fanfic fanfic : fanficDao.search(searchQuery)) {
 			if (!answer.contains(fanfic)) {
 				answer.add(fanfic);
 			}
 		}
 		for (Chapter chapter : chapterDao.search(searchQuery)) {
 			if (!answer.contains(chapter.getFanfic())) {
 				answer.add(chapter.getFanfic());
 			}
 		}
 		return answer;
 	}
 	
 	public List<Fanfic> getFanficsByCategory(String category) {
 		List<Fanfic> answer = new ArrayList<Fanfic>();
 		for (Fanfic fanfic : fanficDao.getFanfics()) {
 			if (fanfic.getCategories().contains(category)) {
 				answer.add(fanfic);
 			}
 		}
 		return answer;
 	}
 	
 	public void removeUserById(int id) {
 		userDao.remove(userDao.getUserById(id));
 	}
 	
 	public void registerUser(User user) {
 		user.setDateOfRegistration(Calendar.getInstance().getTime());
 		userDao.register(user);
 	}
 
 	public List<Fanfic> getAllFanfics() {
 		return fanficDao.getFanfics();
 	}
 
 	public boolean isRegistered(String username, String password) {
 		return null != userDao.login(username, password);
 	}
 
 	public List<User> getAllUsers() {
 		return userDao.getUsers();
 	}
 
 	public List<Fanfic> getBestFanfics(int count) {
 		List<Fanfic> fanfics = fanficDao.getFanfics();
 		Collections.sort(fanfics, new Comparator<Fanfic>() {
 			@Override
 			public int compare(Fanfic first, Fanfic second) {
 				if (first.getRating() > second.getRating()) {
 					return 1;
 				} else {
 					if (first.getRating() < second.getRating()) {
 						return -1;
 					} else {
 						return 0;
 					}
 				}
 			}
 		});
 		if (count > fanfics.size()) {
 			return fanfics;
 		} else {
 			return fanfics.subList(0, count);
 		}
 	}
 
 	public Fanfic getFanficById(int id) {
 		return fanficDao.getFanficById(id);
 	}
 
 }
