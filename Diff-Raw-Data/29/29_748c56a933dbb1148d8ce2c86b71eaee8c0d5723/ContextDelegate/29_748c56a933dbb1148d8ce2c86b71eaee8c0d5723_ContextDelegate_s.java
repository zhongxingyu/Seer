 package delegates;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import enums.LogActions;
 import enums.MementoCategory;
 import exceptions.EntityDoesNotExist;
 import exceptions.NotEnoughInformation;
 import pojos.ContextBean;
 import pojos.ContextPublicMementoBean;
 import pojos.LocationMinimalBean;
 import pojos.PublicMementoBean;
 import utils.PlayDozerMapper;
 import models.City;
 import models.Context;
 import models.ContextPublicMemento;
 import models.FuzzyDate;
 import models.LifeStory;
 import models.Location;
 import models.Person;
 import models.PublicMemento;
 import models.User;
 import play.Play;
 
 public class ContextDelegate {
 
 	public static ContextDelegate getInstance() {
 		return new ContextDelegate();
 	}
 
 	public ContextBean getContextForPerson(Long personId) {
 		models.Context context = models.Context.findByPerson(personId);
 		if (context != null) {
 			ContextBean contextBean = PlayDozerMapper.getInstance().map(
 					context, ContextBean.class);
 			return contextBean;
 		} else {
 			return null;
 		}
 	}
 
 	public ContextBean initContextForPerson(Long personId)
 			throws EntityDoesNotExist, NotEnoughInformation {
 		return initContext(personId, null, null, null);
 	}
 
 	public ContextBean getContextForPersonAndDecade(Long id, Long decade) {
 		models.Context context = models.Context.findByPersonAndDecade(id,decade);
 		if (context != null) {
 			ContextBean contextBean = PlayDozerMapper.getInstance().map(
 					context, ContextBean.class);
 			return contextBean;
 		} else {
 			return null;
 		}
 	}
 	
 	public ContextBean getContextForPersonAndDecadeAndCategory(Long id, Long decade, String category) {
 		models.Context context = models.Context.findByPersonAndDecadeAndCategory(id,decade,category);
 		if (context != null) {
 			ContextBean contextBean = PlayDozerMapper.getInstance().map(
 					context, ContextBean.class);
 			return contextBean;
 		} else {
 			return null;
 		}
 	}
 		
 	public ContextBean initContextForPersonAndDecade(Long personId, Long decade)
 			throws EntityDoesNotExist, NotEnoughInformation {
 		return initContext(personId, decade, null, null);
 	}
 
 	public ContextBean initContextForPersonAndDecadeAndLocation(Long personId,
 			Long decade, LocationMinimalBean location)
 			throws EntityDoesNotExist, NotEnoughInformation {
 		return initContext(personId, decade, null, location);
 	}
 
 	public ContextBean initContextForPersonAndDecadeAndCity(Long personId,
 			Long decade, Long cityId) throws EntityDoesNotExist,
 			NotEnoughInformation {
 		return initContext(personId, decade, cityId, null);
 	}
 	
 	public ContextBean getContext(Long contextId) {
 		models.Context context = models.Context.read(contextId);
 		if (context != null) {
 			ContextBean contextBean = PlayDozerMapper.getInstance().map(
 					context, ContextBean.class);
 			return contextBean;
 		} else {
 			return null;
 		}
 	}
 	
 	public ContextBean getContextByIdAndDecade(Long contextId, Long decade) {
 		models.Context context = models.Context.findByIdAndDecade(contextId,decade);
 		if (context != null) {
 			ContextBean contextBean = PlayDozerMapper.getInstance().map(
 					context, ContextBean.class);
 			return contextBean;
 		} else {
 			return null;
 		}
 	}
 	
 	public ContextBean getContextByIdAndDecadeAndCategory(Long contextId, Long decade, String category) {
 		models.Context context = models.Context.findByIdAndDecadeAndCategory(contextId, decade, category);
 		if (context != null) {
 			ContextBean contextBean = PlayDozerMapper.getInstance().map(
 					context, ContextBean.class);
 			return contextBean;
 		} else {
 			return null;
 		}
 	}
 	
 	public ContextBean refreshContextDecadeAndLocation(Long contextId,
 			Long decade, LocationMinimalBean location)
 			throws EntityDoesNotExist, NotEnoughInformation {
 		/*
 		 * 1. Get the context to refresh
 		 */
 		Context context = Context.read(contextId);
 
 		if (context == null) {
 			// context does not exist
 			throw new EntityDoesNotExist("Context with ID " + contextId
 					+ " does not exist");
 		}
 		
 		Long personForId = context.getPersonForId();
 		String locale = User.readLocaleByPersonId(personForId);
 
 		/*
 		 * 2. Prepare decades
 		 */
 
 		if (decade ==null || location == null) {
 			throw new NotEnoughInformation("Not enough information to update the context");
 		}
 		
 		List<LocationMinimalBean> locList = new ArrayList<LocationMinimalBean>();
 		locList.add(location);
 		Map<Long, List<LocationMinimalBean>> decadesLocationsMap = new HashMap<Long,List<LocationMinimalBean>>();
 		decadesLocationsMap.put(decade, locList);
 
 		/*
 		 * 3 For each decade, prepare content to put in context creating new
 		 * lists of items for the locations and decades provided
 		 */
 
 		List<ContextPublicMemento> content = getPublicMementoList(decadesLocationsMap, locale, context);
 
 		/*
 		 * 4. Add the lists of contents to the context
 		 */
 		context.setPublicMementoList(content);
 
 		ContextBean newContextBean = PlayDozerMapper.getInstance().map(
 				context, ContextBean.class);
 		return newContextBean;
 	}
 
 
 	public ContextBean refreshContextForDecadeAndCity(Long contextId, Long decade,
 			Long cityId) throws EntityDoesNotExist, NotEnoughInformation {
 		/*
 		 * 1. Get the context to refresh
 		 */
 		Context context = Context.read(contextId);
 
 		if (context == null) {
 			// context does not exist
 			throw new EntityDoesNotExist("Context with ID " + contextId
 					+ " does not exist");
 		}
 		
 		Long personForId = context.getPersonForId();
 		String locale = User.readLocaleByPersonId(personForId);
 
 		/*
 		 * 2. Prepare decades
 		 */
 
 		if (decade ==null || cityId == null) {
 			throw new NotEnoughInformation("Not enough information to update the context");
 		}
 		
 		// TODO add also get name by locale to city
 		City c = City.read(cityId);
 		String country = c.getCountry().getNameByLocale(locale);
 		String city = c.getName();
 		String region = c.getRegion();
 		LocationMinimalBean location = new LocationMinimalBean();
 		location.setCity(city);
 		location.setCountry(country);
 		location.setRegion(region);
 		location.setLocale(locale);
 		List<LocationMinimalBean> locList = new ArrayList<LocationMinimalBean>();
 		locList.add(location);
 		Map<Long, List<LocationMinimalBean>> decadesLocationsMap = new HashMap<Long,List<LocationMinimalBean>>();
 		decadesLocationsMap.put(decade, locList);
 
 		/*
 		 * 3 For each decade, prepare content to put in context creating new
 		 * lists of items for the locations and decades provided
 		 */
 
 		List<ContextPublicMemento> content = getPublicMementoList(decadesLocationsMap, locale, context);
 
 		/*
 		 * 4. Add the lists of contents to the context
 		 */
 		context.setPublicMementoList(content);
 
 		ContextBean newContextBean = PlayDozerMapper.getInstance().map(
 				context, ContextBean.class);
 		return newContextBean;
 	}
 
 	public void deleteContext(Long cid) {
 		models.Context.delete(cid);
 	}
 
 	public ContextPublicMementoBean addMementoToContext(Long cid,
 			ContextPublicMementoBean contributed) throws EntityDoesNotExist,
 			NotEnoughInformation {
 		models.ContextPublicMemento cc = PlayDozerMapper.getInstance().map(
 				contributed, models.ContextPublicMemento.class);
 		models.Context c = models.Context.read(cid);
 		cc.setContext(c);
 		models.ContextPublicMemento.create(cc);
 		return PlayDozerMapper.getInstance().map(cc, pojos.ContextPublicMementoBean.class);
 	}
 
 	public PublicMementoBean getPublicMemento(Long mid) {
 		PublicMemento p = models.PublicMemento.read(mid);
 		return PlayDozerMapper.getInstance().map(p, PublicMementoBean.class);
 	}
 	
 	public ContextPublicMementoBean getContextMemento(Long cid, Long mid) {
 		ContextPublicMemento p = models.ContextPublicMemento.read(cid,mid);
 		return PlayDozerMapper.getInstance().map(p, ContextPublicMementoBean.class);
 	}
 	
 	public ContextPublicMementoBean updateContextMemento(Long cid, Long mid,
 			ContextPublicMementoBean contributed) throws EntityDoesNotExist,
 			NotEnoughInformation {
 		models.ContextPublicMemento contributedMemento = PlayDozerMapper.getInstance().map(contributed, models.ContextPublicMemento.class);
 		models.ContextPublicMemento.update(cid, mid, contributedMemento);
 		return PlayDozerMapper.getInstance().map(contributedMemento, pojos.ContextPublicMementoBean.class);
 	}
 	
 	public PublicMementoBean updatePublicMemento(Long mid,
 			PublicMementoBean contributed) throws EntityDoesNotExist,
 			NotEnoughInformation {
 		models.PublicMemento contributedMemento = PlayDozerMapper.getInstance().map(contributed, models.PublicMemento.class);
 		contributedMemento.update();
 		contributedMemento.refresh();
 		return PlayDozerMapper.getInstance().map(contributedMemento, pojos.PublicMementoBean.class);
 	}
 	
 
 	public void deleteContextMemento(Long cid, Long mid)
 			throws EntityDoesNotExist, NotEnoughInformation {
 		models.ContextPublicMemento.delete(cid, mid);
 	}
 
 	public PublicMemento createContributedMemento(
 			PublicMementoBean contributed) {
 		models.PublicMemento contributedMemento = PlayDozerMapper.getInstance().map(
 				contributed, models.PublicMemento.class);
 		models.PublicMemento.create(contributedMemento);
 		
 		return contributedMemento;
 	}
 	
 	public void deleteContributedMemento(Long mid) {
 		models.PublicMemento.delete(mid);		
 	}
 
 	/*
 	 * 
 	 * The algorithm methods
 	 * 
 	 * TODO unify into one single model
 	 */
 	
 	/**
 	 * The initContext method creates a whole new collection of contextual reminiscence items for 
 	 * the person identified by personId. 
 	 * 
 	 * The initialization process will focus on the provided decade, city and location (or combination of them)
 	 * If none is provided, it will use the list of stories of the person that are present in the system. 
 	 * 
 	 * @param personId
 	 * @param decade
 	 * @param cityId
 	 * @param location
 	 * @return
 	 * @throws EntityDoesNotExist
 	 * @throws NotEnoughInformation
 	 */
 	private ContextBean initContext(Long personId, Long decade, Long cityId,
 			LocationMinimalBean location) throws EntityDoesNotExist,
 			NotEnoughInformation {
 		/*
 		 * 1.read person record and language information
 		 */
 		Person person = Person.read(personId);
 		String locale = User.readLocaleByPersonId(personId);
 
 		if (person == null) {
 			// person does not exist
 			throw new EntityDoesNotExist("Person with ID " + personId
 					+ "does not exist");
 		}
 
 		/*
 		 * 2. Prepare decades
 		 */
 		Map<Long, List<LocationMinimalBean>> decadesLocationsMap = prepareDecadesLocationsMap(
 				decade, cityId, personId, location, locale);
 
 		if (decadesLocationsMap.isEmpty()) {
 			throw new NotEnoughInformation("The person " + personId
 					+ "has no story in this decade");
 		}
 
 		/*
 		 * 3. Disable old context for person
 		 */
 		Context.disableContextsForPerson(personId);
 
 		/*
 		 * 4. Create the new context object
 		 */
 		Context newContext = new Context();
 		newContext.setCityFor(null);
 		newContext.setPersonForId(personId);
 		newContext.setCityRatio(null);
 		newContext.setTitle(play.i18n.Messages
 				.get("reminiscens.context.person.title")
 				+ " "
 				+ person.getFirstname() + " " + person.getLastname());
 		newContext.setSubtitle(play.i18n.Messages
 				.get("reminiscens.context.person.subtitle")
 				+ " "
 				+ person.getFirstname() + " " + person.getLastname());
 		newContext.setEnabled(true);
 
 		// save the newly created context
 		Context.createObject(newContext);
 
 		/*
 		 * 4 For each decade, prepare content to put in context creating new
 		 * lists of items (ContextMedia, ContextContributed, ContextPeople,
 		 * ContextEvent, ContextWorks) for the locations where the person had a
 		 * story in those decades
 		 */
 
 		List<ContextPublicMemento> content = getPublicMementoList(decadesLocationsMap, locale, newContext);
 		
 //		List<ContextContributed> contributedContent = getContributedContextList(
 //				decadesLocationsMap, locale, newContext);
 //		List<ContextMedia> mediaContent = getMediaContextList(
 //				decadesLocationsMap, locale, newContext);
 //		List<ContextEvent> eventContent = getEventContextList(
 //				decadesLocationsMap, locale, newContext);
 //		List<ContextCreativeWork> creativeWorkContent = getCreativeWorkContextList(
 //				decadesLocationsMap, locale, newContext);
 //		List<ContextPeople> peopleContent = getPeopleContextList(
 //				decadesLocationsMap, locale, newContext);
 
 		/*
 		 * 5. Add the lists of contents to the context
 		 */
 		newContext.setPublicMementoList(content);
 //		newContext.setContributedMementoList(contributedContent);
 //		newContext.setMediaList(mediaContent);
 //		newContext.setEventList(eventContent);
 //		newContext.setCreativeWorkList(creativeWorkContent);
 //		newContext.setFamousPeopleList(peopleContent);
 
 		// newContext.update();
 		// newContext.refresh();
 
 		ContextBean newContextBean = PlayDozerMapper.getInstance().map(
 				newContext, ContextBean.class);
 		return newContextBean;
 	}
 	
 	private List<ContextPublicMemento> getPublicMementoList(
 			Map<Long, List<LocationMinimalBean>> decadesLocationsMap,
 			String locale, Context newContext) {
 		Set<Long> decades = decadesLocationsMap.keySet();
 		List<ContextPublicMemento> content = new ArrayList<ContextPublicMemento>();
 		int itemsPerLevel = Play.application().configuration()
 				.getInt("context.algorithm.item.count");
 		if (itemsPerLevel <= 0) {
 			itemsPerLevel = 1;
 		}
 
 		List<Long> exclusionList = PublicMemento.readPublicMementoIds(newContext.getContextId());
 		
 		// TODO add a more "thought" ranking function than just order by newer
 		Long ranking = exclusionList != null ? new Long(exclusionList.size()) : 1;
 		
 		for (Long decade : decades) {
 			for (MementoCategory category : MementoCategory.values()) {
 				// 1. read 1 random element related only to the decade
 				List<LocationMinimalBean> locations = decadesLocationsMap
 						.get(decade);
 				
 				// 2. read elements for WORLD level of precision
 				List<PublicMemento> worldLevelList = PublicMemento
 						.readForContextWithExclusionList(locale, category, decade, locations,
 								"WORLD", itemsPerLevel,exclusionList);
 				for (PublicMemento contributed : worldLevelList) {
 					ContextPublicMemento contextItem = new ContextPublicMemento(
 							contributed, newContext);
 					contextItem.setLevel("WORLD");
 					contextItem.setDecade(decade);
 					contextItem.setCategory(contributed.getCategory());
 					contextItem.setType(contributed.getResourceType());
 					contextItem.setRanking(ranking);
 					ranking++;
 					ContextPublicMemento.create(contextItem);
 					content.add(contextItem);
 					exclusionList.add(contributed.getPublicMementoId());
 				}
 
 				List<PublicMemento> countryLevelList = PublicMemento
 						.readForContextWithExclusionList(locale, category, decade, locations,
 								"COUNTRY", itemsPerLevel,exclusionList);
 				for (PublicMemento contributed : countryLevelList) {
 					ContextPublicMemento contextItem = new ContextPublicMemento(
 							contributed, newContext);
 					contextItem.setLevel("COUNTRY");
 					contextItem.setDecade(decade);
 					contextItem.setCategory(contributed.getCategory());
 					contextItem.setType(contributed.getResourceType());
 					contextItem.setRanking(ranking);
 					ranking++;
 					ContextPublicMemento.create(contextItem);
 					content.add(contextItem);
 					exclusionList.add(contributed.getPublicMementoId());
 				}
 				
 				List<PublicMemento> regionLevelList = PublicMemento
 						.readForContextWithExclusionList(locale, category, decade, locations,
 								"REGION", itemsPerLevel,exclusionList);
 				for (PublicMemento contributed : regionLevelList) {
 					ContextPublicMemento contextItem = new ContextPublicMemento(
 							contributed, newContext);
 					contextItem.setLevel("REGION");
 					contextItem.setDecade(decade);
 					contextItem.setCategory(contributed.getCategory());
 					contextItem.setType(contributed.getResourceType());
 					contextItem.setRanking(ranking);
 					ranking++;
 					ContextPublicMemento.create(contextItem);
 					content.add(contextItem);
 				}
 			}
 		}
 		return content;
 	}
 
 	/**
 	 * Prepare a HastTable of the form decade => list of locations which will be
 	 * used to iterate and generate the context
 	 * 
 	 * @param decade
 	 * @param cityId
 	 * @param personId
 	 * @param location
 	 * @return
 	 */	
 	private Map<Long, List<LocationMinimalBean>> prepareDecadesLocationsMap(
 			Long decade, Long cityId, Long personId,
 			LocationMinimalBean location, String locale) {
 		// TODO replace with reading directly the distinct decades and locations
 		List<LifeStory> stories = cityId == null ? location == null ? LifeStory
 				.readByPerson(personId) : null : null;
 
 		Map<Long, List<LocationMinimalBean>> map = new HashMap<Long, List<LocationMinimalBean>>();
 		if (stories != null && !stories.isEmpty() && decade != null) {
 			map = storiesToDecadeLocationsMap(stories, decade);
 		} else if (stories != null && !stories.isEmpty() && decade == null) {
 			map = storiesToDecadeLocationsMap(stories);
 		} else if (decade != null) {
 			List<LocationMinimalBean> locList = new ArrayList<LocationMinimalBean>();
 
 			if (cityId != null) {
 				City c = City.read(cityId);
 				String country = c.getCountry().getNameByLocale(locale);
 				String city = c.getName();
 				String region = c.getRegion();
 				LocationMinimalBean loc = new LocationMinimalBean();
 				loc.setCity(city);
 				loc.setCountry(country);
 				loc.setRegion(region);
 				loc.setLocale(locale);
 				locList.add(loc);
 				map.put(decade, locList);
 			} else if (location != null) {
 				locList.add(location);
 				map.put(decade, locList);
 			}
 		}
 		return map;
 	}
 
 	private Map<Long, List<LocationMinimalBean>> storiesToDecadeLocationsMap(
 			List<LifeStory> stories, Long decade) {
 		Map<Long, List<LocationMinimalBean>> map = new HashMap<Long, List<LocationMinimalBean>>();
 		if (stories != null) {
 			for (LifeStory lifeStory : stories) {
 				FuzzyDate d = lifeStory.getStartDate();
 				Long localDecade = d != null ? d.getDecade() : null;
 				if (localDecade.equals(decade)) {
 					Location c = lifeStory.getLocation();
 					String locale = c.getLocale() == null
 							|| c.getLocale() == "" ? Play.application()
 							.configuration().getString("default.language") : c
 							.getLocale();
 					String country = c.getCountry();
 					String city = c.getCity();
 					String region = c.getRegion();
 					LocationMinimalBean loc = new LocationMinimalBean();
 					loc.setCity(city);
 					loc.setCountry(country);
 					loc.setRegion(region);
 					loc.setLocale(locale);
 					
 					
 					List<LocationMinimalBean> locList = map.get(decade);
 					if (locList == null) {
 						locList = new ArrayList<LocationMinimalBean>();
 					}
 					if (!locList.contains(loc)) {
 						locList.add(loc);
 					}
 					map.put(decade, locList);
 				}
 			}
 		}
 		return map;
 	}
 
 	private Map<Long, List<LocationMinimalBean>> storiesToDecadeLocationsMap(
 			List<LifeStory> stories) {
 		Map<Long, List<LocationMinimalBean>> map = new HashMap<Long, List<LocationMinimalBean>>();
 		if (stories != null) {
 			for (LifeStory lifeStory : stories) {
 				FuzzyDate d = lifeStory.getStartDate();
 				Long decade = d != null ? d.getDecade() : null;
 				if (decade != null) {
 					Location c = lifeStory.getLocation();
 					String locale = c.getLocale() == null
 							|| c.getLocale() == "" ? Play.application()
 							.configuration().getString("default.language") : c
 							.getLocale();
 
 					// TODO manage localization of locations
 					// String localizedCountry =
 					// Country.readByName(c.getCountry()).getNameByLocale(locale);
 					// String localizedCity = Country
 					String country = c.getCountry();
 					String city = c.getCity();
 					String region = c.getRegion();
 					LocationMinimalBean loc = new LocationMinimalBean();
 					loc.setCity(city);
 					loc.setCountry(country);
 					loc.setRegion(region);
 					loc.setLocale(locale);
 					
 					List<LocationMinimalBean> locList = map.get(decade);
 					if (locList == null) {
 						locList = new ArrayList<LocationMinimalBean>();
 					}
 					if (!locList.contains(loc)) {
 						locList.add(loc);
 					}
 					map.put(decade, locList);
 				}
 			}
 		}
 		return map;
 	}
 
 	public ContextPublicMemento increaseStatsOnContextMemento(Long contextId,
 			Long publicMementoId, String stat) {
 
 		ContextPublicMemento pm = ContextPublicMemento.read(contextId, publicMementoId);
 		Long views = pm.getViews();
 		Long detailViews = pm.getDetailViews();
 		Long stories = pm.getStories();
 		
 		if (stat.equals(LogActions.VIEWS.toString())) {
 			views = views == null ? 1 : views+1; 
 			pm.setViews(views);
 		} else if (stat.equals(LogActions.DETAILVIEWS.toString())) {
 			detailViews = detailViews == null ? 1 : detailViews+1; 
 			pm.setDetailViews(detailViews);
 		} else if  (stat.equals(LogActions.STORY_NEW.toString())) {
 			stories = stories == null ? 1 : stories+1; 
 			pm.setStories(stories);
 		} 
 		
 		return ContextPublicMemento.update(pm);
 	}
 
	public Long getContextIdByHashCode(String code) {
 		models.Context context = models.Context.findByHashCode(code);
 		if (context != null) {
			return context.getContextId();
 		} else {
 			return null;
 		}
 	}
 }
