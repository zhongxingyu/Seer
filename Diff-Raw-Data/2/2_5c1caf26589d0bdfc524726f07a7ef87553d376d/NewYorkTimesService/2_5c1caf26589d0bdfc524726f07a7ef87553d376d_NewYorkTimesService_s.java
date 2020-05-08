 /*
  * Copyright (C) 2013 Michael Koppen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.fhb.twitemplating.nyt.service;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import de.fhb.twitemplating.enitiy.Message;
 import de.fhb.twitemplating.nyt.api.MostPopularQuery;
 import de.fhb.twitemplating.nyt.api.MostPopularSearch;
 import de.fhb.twitemplating.nyt.api.NYTAPIKey;
 import de.fhb.twitemplating.nyt.api.ResourceType;
 import de.fhb.twitemplating.nyt.api.TimePeriod;
 import de.fhb.twitemplating.nyt.util.MostPopular;
 import de.fhb.twitemplating.nyt.util.Result;
 import lombok.NoArgsConstructor;
 
 import javax.ejb.Startup;
 import javax.ejb.Stateless;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * This Bean gets articles from the NewYorkTimes.
  *
  * @author Christoph Ott
  */
 @Stateless
 @Startup
 @NoArgsConstructor
 public class NewYorkTimesService implements NewYorkTimesLocal{
 
     private final static Logger LOG = Logger.getLogger(NewYorkTimesService.class.getName());
     public static final String API_KEY = "cfe88cd84c026683a2a1f8fb156b9709:6:67675712";
     private Date date = new Date();
 	private List<Message> messageList;
 
     @Override
     public void start() {
     }
 
     @Override
     public void stop() {
     }
 
     @Override
     public List<Message> getMessages() {
 //		LOG.info("Executing: getMessages()");
 		if (messageList != null && (new Date().getTime() - date.getTime()) < 300000) {
 			return messageList;
 		}
 		Gson gson = new GsonBuilder().create();
 		MostPopular mostPopular;
 		String apiKey = API_KEY;
 		MostPopularQuery mostPopularQuery = new MostPopularQuery(
 				ResourceType.MOSTVIEWED, TimePeriod.THIRTY);
 		MostPopularSearch mps = new MostPopularSearch(new NYTAPIKey(apiKey));
 		mostPopular = gson.fromJson(mps.search(mostPopularQuery), MostPopular.class);
 
 		messageList = new ArrayList<Message>();
 		long id = 0;
 		for (Result result : mostPopular.results) {
 			Message message = new Message();
 			message.setId(id++);
 			message.setMessage(result.isAbstract);
 			message.setTitle(result.title);
 			message.setSection(result.section);
             message.setURL(result.url);
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 			try {
 				message.setPublished(sdf.parse(result.published_date));
 			} catch (ParseException e) {
 				message.setPublished(null);
 			}
			message.setAuthor(result.byline.substring(3));
 			message.setMediaURL(result.getSmallImageURL());
 			messageList.add(message);
 		}
 		date = new Date();
 		return messageList;
 	}
 }
