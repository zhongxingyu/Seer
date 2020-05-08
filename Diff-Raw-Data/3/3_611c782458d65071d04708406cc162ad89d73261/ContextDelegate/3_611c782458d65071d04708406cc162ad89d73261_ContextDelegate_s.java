 package delegates;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import enums.MementoCategory;
 import exceptions.EntityDoesNotExist;
 import exceptions.NotEnoughInformation;
 import pojos.ContextBean;
 import pojos.LocationMinimalBean;
 import utils.PlayDozerMapper;
 import models.City;
 import models.Context;
 import models.ContextContributed;
 import models.ContextCreativeWork;
 import models.ContextEvent;
 import models.ContextMedia;
 import models.ContextPeople;
 import models.ContributedMemento;
 import models.Country;
 import models.FuzzyDate;
 import models.LifeStory;
 import models.Location;
 import models.Person;
 import models.User;
 import play.Play;
 
 public class ContextDelegate {
 
 	private ContextBean initContext(Long personId, Long decade, Long cityId)
 			throws EntityDoesNotExist, NotEnoughInformation {
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
 				decade, cityId, personId);
 
 		if (decadesLocationsMap.isEmpty()) {
 			City c = City.read(cityId);
 			LocationMinimalBean loc = new LocationMinimalBean(c.getCountry()
 					.getNameByLocale(locale), c.getRegion(), c.getName(),
 					locale);
 			List<LocationMinimalBean> locList = new ArrayList<LocationMinimalBean>();
 			locList.add(loc);
 			decadesLocationsMap.put(decade, locList);
 		}
 
 
 		/*
 		 * 3. Create the new context object
 		 */
 		Context newContext = new Context();
 		newContext.setCityFor(null);
 		newContext.setPersonForId(personId);
 		newContext.setCityRatio(null);
 		newContext.setTitle(play.i18n.Messages.get("context.person.title")
 				+ person.getFirstname() + " " + person.getLastname());
 		newContext.setSubtitle(play.i18n.Messages
 				.get("context.person.subtitle")
 				+ person.getFirstname()
 				+ " "
 				+ person.getLastname());
 
 		// save the newly created context
 		newContext.save();
 		newContext.refresh();
 		
 		/*
 		 * 4 For each decade, prepare content to put in context creating new
 		 * lists of items (ContextMedia, ContextContributed, ContextPeople,
 		 * ContextEvent, ContextWorks) for the locations where the person had a
 		 * story in those decades
 		 */
 
 		List<ContextContributed> contributedContent = getContributedContextList(decadesLocationsMap,locale,newContext);
 		List<ContextMedia> mediaContent = getMediaContextList(decadesLocationsMap,locale,newContext);
 		List<ContextEvent> eventContent = getEventContextList(decadesLocationsMap,locale,newContext);
 		List<ContextCreativeWork> creativeWorkContent = getCreativeWorkContextList(decadesLocationsMap,locale,newContext);
 		List<ContextPeople> peopleContent = getPeopleContextList(decadesLocationsMap,locale,newContext);
 
 		/*
 		 * 5. Add the lists of contents to the context
 		 */
 		newContext.setContributedMementoList(contributedContent);
 		newContext.setMediaList(mediaContent);
 		newContext.setEventList(eventContent);
 		newContext.setCreativeWorkList(creativeWorkContent);
 		newContext.setFamousPeopleList(peopleContent);
 		
 		newContext.update();
 		newContext.refresh();
 		
 		ContextBean newContextBean = PlayDozerMapper.getInstance().map(
 				Context.class, ContextBean.class);
 		return newContextBean;
 	}
 
 	private List<ContextPeople> getPeopleContextList(
 			Map<Long, List<LocationMinimalBean>> decadesLocationsMap, String locale, Context newContext) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private List<ContextCreativeWork> getCreativeWorkContextList(
 			Map<Long, List<LocationMinimalBean>> decadesLocationsMap, String locale, Context newContext) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private List<ContextEvent> getEventContextList(
 			Map<Long, List<LocationMinimalBean>> decadesLocationsMap, String locale, Context newContext) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private List<ContextMedia> getMediaContextList(
 			Map<Long, List<LocationMinimalBean>> decadesLocationsMap, String locale, Context newContext) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private List<ContextContributed> getContributedContextList(
 			Map<Long, List<LocationMinimalBean>> decadesLocationsMap, String locale, Context newContext) {
 		Set<Long> decades = decadesLocationsMap.keySet();
 		List<ContextContributed> content = new ArrayList<ContextContributed>();
 		int itemsPerLevel = Play.application().configuration().getInt("context.algorithm.item.count");
 		if (itemsPerLevel <= 0) {
 			itemsPerLevel = 1;
 		}
 		
 		for (Long decade : decades) {
 			for (MementoCategory category : MementoCategory.values()) {
 				// 1. read 1 random element related only to the decade
 				List<LocationMinimalBean> locations = decadesLocationsMap.get(decade);
 				List<ContributedMemento> worldLevelList = ContributedMemento.readForContext(locale,category,decade,locations,"WORLD",itemsPerLevel);
 				List<ContributedMemento> countryLevelList = ContributedMemento.readForContext(locale,category,decade,locations,"COUNTRY",itemsPerLevel);
 				List<ContributedMemento> regionLevelList = ContributedMemento.readForContext(locale,category,decade,locations,"REGION",itemsPerLevel);
 
 				for (ContributedMemento contributed : worldLevelList) {
 					ContextContributed contextItem = new ContextContributed(contributed, newContext);
 					contextItem.setLevel("WORLD");
 					contextItem.setCategory(contributed.getCategory());
 					contextItem.setType(contributed.getResourceType());
 					ContextContributed.create(contextItem);
 					content.add(contextItem);
 					
 				}				
 				for (ContributedMemento contributed : countryLevelList) {
 					ContextContributed contextItem = new ContextContributed(contributed, newContext);
 					contextItem.setLevel("COUNTRY");
 					contextItem.setCategory(contributed.getCategory());
 					contextItem.setType(contributed.getResourceType());
 					ContextContributed.create(contextItem);
 					content.add(contextItem);
 				}				
 				for (ContributedMemento contributed : regionLevelList) {
 					ContextContributed contextItem = new ContextContributed(contributed, newContext);
 					contextItem.setLevel("REGION");
 					contextItem.setCategory(contributed.getCategory());
 					contextItem.setType(contributed.getResourceType());
 					ContextContributed.create(contextItem);
 					content.add(contextItem);
 				}
 			}
 		}
 		
		
		return null;
 	}
 
 	/**
 	 * Prepare a HastTable of the form decade => list of locations which will be used to iterate
 	 * and generate the context
 	 * 
 	 * @param decade
 	 * @param cityId
 	 * @param personId
 	 * @return
 	 */
 	private Map<Long, List<LocationMinimalBean>> prepareDecadesLocationsMap(
 			Long decade, Long cityId, Long personId) {
 		// TODO replace with reading directly the distinct decades and locations 
 		List<LifeStory> stories = decade == null || cityId == null ? LifeStory
 				.readByPerson(personId) : null;
 		return decade == null ? storiesToDecadeLocationsMap(stories)
 				: cityId == null ? storiesToDecadeLocationsMap(stories, decade)
 						: new HashMap<Long, List<LocationMinimalBean>>();
 	}
 
 	private Map<Long, List<LocationMinimalBean>> storiesToDecadeLocationsMap(
 			List<LifeStory> stories, Long decade) {
 		Map<Long, List<LocationMinimalBean>> map = new HashMap<Long, List<LocationMinimalBean>>();
 		if (stories != null) {
 			for (LifeStory lifeStory : stories) {
 				FuzzyDate d = lifeStory.getStartDate();
 				Long localDecade = d != null ? d.getDecade() : null;
 				if (localDecade == decade) {
 					Location c = lifeStory.getLocation();
 					String locale = c.getLocale() == null || c.getLocale() == "" ? Play.application().configuration().getString("default.language") : c.getLocale();
 					LocationMinimalBean loc = new LocationMinimalBean(c.getCountry(), c.getRegion(), c.getCityName(),locale);
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
 					String locale = c.getLocale() == null || c.getLocale() == "" ? Play.application().configuration().getString("default.language") : c.getLocale();
 					LocationMinimalBean loc = new LocationMinimalBean(c.getCountry(), c.getRegion(), c.getCityName(),locale);
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
 
 	public static ContextDelegate getInstance() {
 		return new ContextDelegate();
 	}
 
 	public ContextBean initContextForPersonAndDecadeAndCity(Long personId,
 			Long decade, Long cityId) throws EntityDoesNotExist,
 			NotEnoughInformation {
 		return initContext(personId, decade, cityId);
 	}
 
 	public ContextBean initContextForPersonAndDecade(Long personId, Long decade)
 			throws EntityDoesNotExist, NotEnoughInformation {
 		return initContext(personId, decade, null);
 	}
 
 	public ContextBean initContextForPerson(Long personId)
 			throws EntityDoesNotExist, NotEnoughInformation {
 		return initContext(personId, null, null);
 	}
 }
