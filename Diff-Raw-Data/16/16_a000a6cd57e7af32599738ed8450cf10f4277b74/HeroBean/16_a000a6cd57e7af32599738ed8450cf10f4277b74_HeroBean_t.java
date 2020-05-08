 package com.bc.d3.web.beans;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.ResourceBundle;
 
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import com.bc.d3.NotFoundException;
 import com.bc.d3.data.Hero;
 import com.bc.d3.repo.HeroRepository;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 
 import static com.bc.d3.web.Functions.urlencode;
 
 @Component
 @Scope("request")
 public class HeroBean {
 
 	private static final long HOUR_MILLISECONDS = 3600000;
 	private static final String HERO_API
 		= "http://%s.battle.net/api/d3/profile/%s-%s/hero/%d";
 
 	private String region;
 	private String battleTagName;
 	private int battleTagCode;
 	private long heroId;
 
 	private Hero cachedHero;
 
 	@Autowired
 	@Qualifier("lang")
 	private ResourceBundle lang;
 
 	@Autowired
 	private HeroRepository heroRepo;
 
 	public String getRegion() {
 		return region;
 	}
 
 	public void setRegion(String region) {
 		this.region = region;
 	}
 
 	public String getBattleTagName() {
 		return battleTagName;
 	}
 
 	public void setBattleTagName(String battleTagName) {
 		this.battleTagName = battleTagName;
 	}
 
 	public int getBattleTagCode() {
 		return battleTagCode;
 	}
 
 	public void setBattleTagCode(int battleTagCode) {
 		this.battleTagCode = battleTagCode;
 	}
 
 	public String getBattleTag() {
 		return battleTagName + "#" + battleTagCode;
 	}
 
 	public long getHeroId() {
 		return heroId;
 	}
 
 	public void setHeroId(long heroId) {
 		this.heroId = heroId;
 	}
 
 	public Hero getHero() {
 		if (cachedHero != null) {
 			return cachedHero;
 		}
 
		String battleTag = getBattleTag();
 		cachedHero = heroRepo.findOne(heroId);
		if ((cachedHero != null) && battleTag.equals(cachedHero.getBattleTag())
				&& isFresh(cachedHero)) {
 			return cachedHero;
 		}
 
 		cachedHero = fetchHero();
 		if (cachedHero == null) {
 			throw new NotFoundException();
 		}
 
		cachedHero.setBattleTag(battleTag);
 		heroRepo.save(cachedHero);
 		return cachedHero;
 	}
 
 	private Hero fetchHero() {
 		try {
 			Client client = Client.create();
 			WebResource resource = client.resource(String.format(HERO_API,
 					region, urlencode(battleTagName), battleTagCode,
 					heroId));
 			String json = resource.get(String.class);
 			ObjectMapper mapper = new ObjectMapper();
 			return mapper.readValue(json, Hero.class);
 		} catch (UniformInterfaceException exception) {
 			throw new NotFoundException();
 		} catch (JsonParseException exception) {
 			throw new NotFoundException();
 		} catch (JsonMappingException exception) {
 			throw new NotFoundException();
 		} catch (IOException exception) {
 			throw new NotFoundException();
 		} catch (RuntimeException exception) {
 			return null;
 		}
 	}
 
 	public String getLocalizedHeroClass() {
 		String clazz = getHero().getHeroClass().toStringCamelCase();
 		return lang.getString(clazz);
 	}
 
 	private static boolean isFresh(Hero hero) {
 		Date now = new Date();
 		long diff = now.getTime() - hero.getLastRefreshed().getTime();
 		return diff < HOUR_MILLISECONDS;
 	}
 
 }
