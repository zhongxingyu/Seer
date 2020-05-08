 /* 
  * This file is part of PS2YT
  *
  * Copyright (C) 2013 Frédéric Bertolus (Niavok)
  * 
  * PS2YT is free software: you can redistribute it and/or modify
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
 package com.niavok.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 import java.util.Map;
import java.util.regex.Pattern;
 
import javax.print.attribute.standard.PDLOverrideSupported;
 import javax.swing.BorderFactory;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import com.niavok.Config;
 import com.niavok.podcast.Podcast;
 import com.niavok.podcast.PodcastManager;
 import com.niavok.podcast.PodcastTrack;
 import com.niavok.youtube.YouTubeChannel;
 import com.niavok.youtube.YoutubeEntry;
 import com.niavok.youtube.YoutubeFeed;
 
 public class PodcastListPage extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -22513621390876310L;
 	private MainFrame parent;
 	private JComboBox<String> podcastList;
 	private Podcast podcast;
 
 	public PodcastListPage(MainFrame mainFrame, Podcast podcast) {
 		this.parent = mainFrame;
 		this.podcast = podcast;
 		
 		setLayout(new BorderLayout());
 		
 		List<PodcastTrack> tracks = podcast.getTracks();
 		
 		syncUploadState(tracks, parent.getYouTubeChannel());
 		
 		
 		JPanel panel = new JPanel();
 		panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
 		
 		panel.setLayout(new GridLayout(tracks.size(), 1));
 		
 		JScrollPane scrollPane = new JScrollPane(panel);
 		scrollPane.getVerticalScrollBar().setUnitIncrement(64);
 		setPreferredSize(new Dimension(450, 110));
 		
 		
 		
 		
 		JPanel podcastSelectionPanel = new JPanel();
 		podcastSelectionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		
 		String[] podcastNames = PodcastManager.getPodcastNames();
 		podcastList = new JComboBox<String>(podcastNames);
 		podcastList.setSelectedIndex(podcast.getIndex());
 		podcastList.addActionListener(generatePodcastSelectionListener());
 		podcastSelectionPanel.add(podcastList);
 		podcastSelectionPanel.add(new JLabel(""+tracks.size()+" épisodes"));
 		
 		
 		add(podcastSelectionPanel, BorderLayout.NORTH);
 		add(scrollPane, BorderLayout.CENTER);
 		
 		for(PodcastTrack track: tracks) {
 			panel.add(new PodcastView(track));
 //			System.out.println("- "+track.getTitle());
 		}
 		
 		
 	}
 
 	private ActionListener generatePodcastSelectionListener() {
 		return new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Seleted "+podcastList.getSelectedIndex());
 				Config.setDefaultPodcast(podcastList.getSelectedIndex());
 				parent.gotoPodcastListPage();
 			}
 		};
 	}
 
 	private void syncUploadState(List<PodcastTrack> tracks,
 		YouTubeChannel youTubeChannel) {
 		YoutubeFeed feed = youTubeChannel.getFeed();
 		Map<String, YoutubeEntry> entryList = feed.getEntryList(podcast);
 		
 		
 		for(PodcastTrack track: tracks) {
 			if(entryList.containsKey(track.getNumber())) {
 				track.setUploaded(true);
 				track.setUploadUrl(entryList.get(track.getNumber()).getUrl());
 			}
 		}
 	}
 	
 	
 	
 	
 }
