 /* Copyright (C) 2009  Egon Willighagen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.egonw.rednael;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.jibble.pircbot.PircBot;
 
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.fetcher.FeedFetcher;
 import com.sun.syndication.fetcher.FetcherException;
 import com.sun.syndication.fetcher.impl.FeedFetcherCache;
 import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
 import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
 import com.sun.syndication.io.FeedException;
 
 public class LeanderBot extends PircBot {
 
     /**
      * Keeps track of the latest entries.
      */
     private List<Channel> channels;
 
     private FeedFetcherCache feedInfoCache;
     private FeedFetcher fetcher;
 
     public LeanderBot() throws NickAlreadyInUseException, IOException, IrcException {
         this.setName("rednael");
         this.setVerbose(true);
         this.connect("irc.freenode.net");
         this.channels = new ArrayList<Channel>();
 
         Channel cdk = new Channel("#cdk");
         String branch = "cdk-1.2.x";
         cdk.addFeed(branch, new URL(
             "http://cdk.git.sourceforge.net/git/gitweb.cgi?p=cdk;a=rss;h=refs/heads/"
                 + branch
         ));
         branch = "master";
         cdk.addFeed(branch, new URL(
             "http://cdk.git.sourceforge.net/git/gitweb.cgi?p=cdk;a=rss;h=refs/heads/"
                 + branch
         ));
         addChannel(cdk);
 
        Channel bioclipse = new Channel("#bioclipse");
         bioclipse.addFeed(branch, new URL(
             "http://pele.farmbio.uu.se/planetbioclipse/atom.xml"
         ));
         addChannel(bioclipse);
 
         feedInfoCache = HashMapFeedInfoCache.getInstance();
         fetcher = new HttpURLFeedFetcher(feedInfoCache);
     }
 
     private void addChannel(Channel channel) {
         this.channels.add(channel);
         this.joinChannel(channel.getName());
     }
 
     private void boot() throws IllegalArgumentException, IOException, FeedException, FetcherException {
         for (Channel channel : channels) {
             for (Feed chFeed : channel.getFeeds()) {
                 SyndFeed feed = null;
                 feed = fetcher.retrieveFeed(chFeed.getURL());
                 int itemCount = 0;
                 List<SyndEntry> entries = feed.getEntries();
                 for (SyndEntry entry : entries) {
                     itemCount++;
                     String link = entry.getLink();
                     chFeed.add(link);
                 }
                 // feeds have the latest entry first, but we want them at the last
                 // position
                 chFeed.reverse();
             }
         }
     }
 
     private void update() {
         for (Channel channel : channels) {
             for (Feed chFeed : channel.getFeeds()) {
                 try {
                     SyndFeed feed = null;
                     feed = fetcher.retrieveFeed(chFeed.getURL());
                     List<SyndEntry> entries = feed.getEntries();
                     for (SyndEntry entry : entries) {
                         String title = entry.getTitle();
                         String link = entry.getLink();
                         if (!chFeed.contains(link)) {
                             chFeed.add(link);
                             StringBuffer message = new StringBuffer();
                             message.append('[').append(chFeed.getLabel()).append("] ");
                             message.append(title);
                             String author = entry.getAuthor();
                             if (author.indexOf('<') != -1) {
                                 author = author.substring(0, author.indexOf('<'));
                             }
                             message.append("  ").append(link);
                             sendMessage(channel.getName(), message.toString());
                             Thread.sleep(2000);
                         }
                     }
                 } catch (Exception exception) {
                     exception.printStackTrace();
                 }
             }
         }
     }
 
     public void onMessage(String channel, String sender,
                        String login, String hostname, String message) {
     }
 
     public static void main(String[] args) throws Exception {
         LeanderBot bot = new LeanderBot();
         bot.boot();
 
         Random random = new Random();
         while (bot.isConnected()) {
             bot.update();
             Thread.sleep(55000 + random.nextInt(10000));
         }
     }
 
 }
 
 
