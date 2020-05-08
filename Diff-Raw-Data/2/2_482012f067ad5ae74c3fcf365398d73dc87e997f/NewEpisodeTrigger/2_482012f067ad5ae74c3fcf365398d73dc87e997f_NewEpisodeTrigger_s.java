 /*
  * Reactor - NewEpisodeTrigger.java - Copyright © 2013 David Roden
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
 
 package net.pterodactylus.reactor.triggers;
 
 import static com.google.common.base.Preconditions.checkState;
 
 import java.util.Collection;
 
 import net.pterodactylus.reactor.Reaction;
 import net.pterodactylus.reactor.State;
 import net.pterodactylus.reactor.Trigger;
 import net.pterodactylus.reactor.output.DefaultOutput;
 import net.pterodactylus.reactor.output.Output;
 import net.pterodactylus.reactor.states.EpisodeState;
 import net.pterodactylus.reactor.states.EpisodeState.Episode;
 import net.pterodactylus.reactor.states.TorrentState.TorrentFile;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 
 /**
  * {@link Trigger} implementation that compares two {@link EpisodeState}s for
  * new and changed {@link Episode}s.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class NewEpisodeTrigger implements Trigger {
 
 	/** All new episodes. */
 	private Collection<Episode> newEpisodes;
 
 	/** All changed episodes. */
 	private Collection<Episode> changedEpisodes;
 
 	//
 	// TRIGGER METHODS
 	//
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean triggers(State currentState, State previousState) {
 		checkState(currentState instanceof EpisodeState, "currentState is not a EpisodeState but a %s", currentState.getClass().getName());
 		checkState(previousState instanceof EpisodeState, "previousState is not a EpisodeState but a %s", currentState.getClass().getName());
 		final EpisodeState currentEpisodeState = (EpisodeState) currentState;
 		final EpisodeState previousEpisodeState = (EpisodeState) previousState;
 
 		newEpisodes = Collections2.filter(currentEpisodeState.episodes(), new Predicate<Episode>() {
 
 			@Override
 			public boolean apply(Episode episode) {
 				return !previousEpisodeState.episodes().contains(episode);
 			}
 		});
 
 		changedEpisodes = Collections2.filter(currentEpisodeState.episodes(), new Predicate<Episode>() {
 
 			@Override
 			public boolean apply(Episode episode) {
 				if (!previousEpisodeState.episodes().contains(episode)) {
 					return false;
 				}
 
 				/* find previous episode. */
 				final Episode previousEpisode = findPreviousEpisode(episode);
 
 				/* compare the list of torrent files. */
 				Collection<TorrentFile> newTorrentFiles = Collections2.filter(episode.torrentFiles(), new Predicate<TorrentFile>() {
 
 					@Override
 					public boolean apply(TorrentFile torrentFile) {
 						return !previousEpisode.torrentFiles().contains(torrentFile);
 					}
 				});
 
 				return !newTorrentFiles.isEmpty();
 			}
 
 			private Episode findPreviousEpisode(Episode episode) {
 				for (Episode previousStateEpisode : previousEpisodeState) {
 					if (previousStateEpisode.equals(episode)) {
 						return previousStateEpisode;
 					}
 				}
 				return null;
 			}
 
 		});
 
 		return !newEpisodes.isEmpty() || !changedEpisodes.isEmpty();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Output output(Reaction reaction) {
 		String summary;
 		if (!newEpisodes.isEmpty()) {
 			if (!changedEpisodes.isEmpty()) {
				summary = String.format("%d new and %d changed Torrent(s) for “!”", newEpisodes.size(), changedEpisodes.size(), reaction.name());
 			} else {
 				summary = String.format("%d new Torrent(s) for “%s!”", newEpisodes.size(), reaction.name());
 			}
 		} else {
 			summary = String.format("%d changed Torrent(s) for “%s!”", changedEpisodes.size(), reaction.name());
 		}
 		DefaultOutput output = new DefaultOutput(summary);
 		output.addText("text/plain", generatePlainText(reaction, newEpisodes, changedEpisodes));
 		output.addText("text/html", generateHtmlText(reaction, newEpisodes, changedEpisodes));
 		return output;
 	}
 
 	//
 	// STATIC METHODS
 	//
 
 	/**
 	 * Generates the plain text trigger output.
 	 *
 	 * @param reaction
 	 *            The reaction that was triggered
 	 * @param newEpisodes
 	 *            The new episodes
 	 * @param changedEpisodes
 	 *            The changed episodes
 	 * @return The plain text output
 	 */
 	private static String generatePlainText(Reaction reaction, Collection<Episode> newEpisodes, Collection<Episode> changedEpisodes) {
 		StringBuilder stringBuilder = new StringBuilder();
 		if (!newEpisodes.isEmpty()) {
 			stringBuilder.append(reaction.name()).append(" - New Episodes\n\n");
 			for (Episode episode : newEpisodes) {
 				stringBuilder.append("- ").append(episode.identifier()).append("\n");
 				for (TorrentFile torrentFile : episode) {
 					stringBuilder.append("  - ").append(torrentFile.name()).append(", ").append(torrentFile.size()).append("\n");
 					stringBuilder.append("    Magnet: ").append(torrentFile.magnetUri()).append("\n");
 					stringBuilder.append("    Download: ").append(torrentFile.downloadUri()).append("\n");
 				}
 			}
 		}
 		if (!changedEpisodes.isEmpty()) {
 			stringBuilder.append(reaction.name()).append(" - Changed Episodes\n\n");
 			for (Episode episode : changedEpisodes) {
 				stringBuilder.append("- ").append(episode.identifier()).append("\n");
 				for (TorrentFile torrentFile : episode) {
 					stringBuilder.append("  - ").append(torrentFile.name()).append(", ").append(torrentFile.size()).append("\n");
 					stringBuilder.append("    Magnet: ").append(torrentFile.magnetUri()).append("\n");
 					stringBuilder.append("    Download: ").append(torrentFile.downloadUri()).append("\n");
 				}
 			}
 		}
 		return stringBuilder.toString();
 	}
 
 	/**
 	 * Generates the HTML trigger output.
 	 *
 	 * @param reaction
 	 *            The reaction that was triggered
 	 * @param newEpisodes
 	 *            The new episodes
 	 * @param changedEpisodes
 	 *            The changed episodes
 	 * @return The HTML output
 	 */
 	private static String generateHtmlText(Reaction reaction, Collection<Episode> newEpisodes, Collection<Episode> changedEpisodes) {
 		StringBuilder htmlBuilder = new StringBuilder();
 		htmlBuilder.append("<html><body>\n");
 		htmlBuilder.append("<h1>").append(StringEscapeUtils.escapeHtml4(reaction.name())).append("</h1>\n");
 		if (!newEpisodes.isEmpty()) {
 			htmlBuilder.append("<h2>New Episodes</h2>\n");
 			htmlBuilder.append("<ul>\n");
 			for (Episode episode : newEpisodes) {
 				htmlBuilder.append("<li>Season ").append(episode.season()).append(", Episode ").append(episode.episode()).append("</li>\n");
 				htmlBuilder.append("<ul>\n");
 				for (TorrentFile torrentFile : episode) {
 					htmlBuilder.append("<li>").append(StringEscapeUtils.escapeHtml4(torrentFile.name())).append("</li>\n");
 					htmlBuilder.append("<div>");
 					htmlBuilder.append("<strong>").append(StringEscapeUtils.escapeHtml4(torrentFile.size())).append("</strong>, ");
 					htmlBuilder.append("<strong>").append(torrentFile.fileCount()).append("</strong> file(s), ");
 					htmlBuilder.append("<strong>").append(torrentFile.seedCount()).append("</strong> seed(s), ");
 					htmlBuilder.append("<strong>").append(torrentFile.leechCount()).append("</strong> leecher(s)</div>\n");
 					htmlBuilder.append("<div><a href=\"").append(StringEscapeUtils.escapeHtml4(torrentFile.magnetUri())).append("\">Magnet</a> ");
 					htmlBuilder.append("<a href=\"").append(StringEscapeUtils.escapeHtml4(torrentFile.downloadUri())).append("\">Download</a></div>\n");
 				}
 				htmlBuilder.append("</ul>\n");
 			}
 			htmlBuilder.append("</ul>\n");
 		}
 		if (!changedEpisodes.isEmpty()) {
 			htmlBuilder.append("<h2>Changed Episodes</h2>\n");
 			htmlBuilder.append("<ul>\n");
 			for (Episode episode : changedEpisodes) {
 				htmlBuilder.append("<li>Season ").append(episode.season()).append(", Episode ").append(episode.episode()).append("</li>\n");
 				htmlBuilder.append("<ul>\n");
 				for (TorrentFile torrentFile : episode) {
 					htmlBuilder.append("<li>").append(StringEscapeUtils.escapeHtml4(torrentFile.name())).append("</li>\n");
 					htmlBuilder.append("<div>");
 					htmlBuilder.append("<strong>").append(StringEscapeUtils.escapeHtml4(torrentFile.size())).append("</strong>, ");
 					htmlBuilder.append("<strong>").append(torrentFile.fileCount()).append("</strong> file(s), ");
 					htmlBuilder.append("<strong>").append(torrentFile.seedCount()).append("</strong> seed(s), ");
 					htmlBuilder.append("<strong>").append(torrentFile.leechCount()).append("</strong> leecher(s)</div>\n");
 					htmlBuilder.append("<div><a href=\"").append(StringEscapeUtils.escapeHtml4(torrentFile.magnetUri())).append("\">Magnet</a> ");
 					htmlBuilder.append("<a href=\"").append(StringEscapeUtils.escapeHtml4(torrentFile.downloadUri())).append("\">Download</a></div>\n");
 				}
 				htmlBuilder.append("</ul>\n");
 			}
 			htmlBuilder.append("</ul>\n");
 		}
 		htmlBuilder.append("</body></html>\n");
 		return htmlBuilder.toString();
 	}
 
 }
