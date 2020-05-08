 package jreader.service.impl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Logger;
 
 import jreader.dao.FeedDao;
 import jreader.dao.FeedEntryDao;
 import jreader.dao.UserDao;
 import jreader.domain.Feed;
 import jreader.domain.FeedEntry;
 import jreader.domain.User;
 import jreader.dto.FeedDto;
 import jreader.dto.FeedEntryDto;
 import jreader.rss.RssService;
 import jreader.service.FeedService;
 
 import org.dozer.Mapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class FeedServiceImpl implements FeedService {
 	
 	private static final Logger LOG = Logger.getLogger(FeedServiceImpl.class.getName());
 	
 	@Autowired
 	private UserDao userDao;
 	
 	@Autowired
 	private FeedDao feedDao;
 
 	@Autowired
 	private FeedEntryDao feedEntryDao;
 	
 	@Autowired
 	private RssService rssService;
 	
 	@Autowired
 	private Mapper mapper;
 
 	@Override
 	public List<FeedDto> list() {
 		List<FeedDto> dtos = new ArrayList<FeedDto>();
 		for (Feed feed : feedDao.listAll()) {
 			dtos.add(mapper.map(feed, FeedDto.class));
 		}
 		return dtos;
 	}
 
 	@Override
 	public List<FeedEntryDto> listEntries(String id) {
 		Feed feed = feedDao.find(id);
 		if (feed == null) {
 			return Collections.emptyList();
 		}
 		List<FeedEntry> entries = feedEntryDao.listEntries(feed);
 		List<FeedEntryDto> dtos = new ArrayList<FeedEntryDto>();
 		for (FeedEntry entry : entries) {
 			dtos.add(mapper.map(entry, FeedEntryDto.class));
 		}
 		return dtos;
 	}
 
 	@Override
 	public List<FeedDto> list(String username) {
 		User user = userDao.find(username);
 		if (user == null) {
 			return Collections.emptyList();
 		}
 		List<FeedDto> dtos = new ArrayList<FeedDto>();
 		for (Feed feed : feedDao.listFeedsFor(user)) {
 			dtos.add(mapper.map(feed, FeedDto.class));
 		}
 		return dtos;
 	}
 
 	@Override
 	public void refreshFeeds() {
 		List<Feed> feeds = feedDao.listAll();
 		for (Feed feed : feeds) {
 			jreader.rss.domain.Feed rssFeed = rssService.fetch(feed.getUrl());
 			if (rssFeed == null) {
 				continue;
 			}
 			int counter = 0;
 			for (jreader.rss.domain.FeedEntry rssFeedEntry : rssFeed.getEntries()) {
 				FeedEntry feedEntry = mapper.map(rssFeedEntry, FeedEntry.class);
 				if (feedEntryDao.findByLink(feed, feedEntry.getLink()) == null) {
 					feedEntryDao.save(feedEntry, feed);
 					++counter;
 				}
 			}
			LOG.info(feed.getTitle() + " new items: " + counter);
 		}
 	}
 
 }
