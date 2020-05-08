 package com.songkick.snippets.presentation;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.petebevin.markdown.MarkdownProcessor;
 import com.songkick.snippets.logic.DateHandler;
 import com.songkick.snippets.model.Snippet;
 import com.songkick.snippets.model.SnippetInMemory;
 import com.songkick.snippets.model.User;
 import com.songkick.snippets.server.data.DataStorage;
 
 /**
  * Simple presentation layer for snippets web page
  * 
  * @author dancrow
  */
 public class SnippetPresentation {
 	public void showWeekList(Writer out) throws IOException {
 		Long lastWeek = DateHandler.getCurrentWeek() - 1;
 
 		out.write("<b>By week:</b>\n");
 		out.write("<ul>\n");
 
 		// Display the weeks in reverse order
 		for (Long week = lastWeek; week > 0; week--) {
 			out.write("<li>\n");
 			if (week == lastWeek) {
 				out.write("<a href='snippets?week=" + week + "'>Last week</a>\n");
 			} else {
 				out.write("<a href='snippets?week=" + week + "'>Week from "
 						+ DateHandler.weekToDate(week) + " to "
 						+ DateHandler.weekToDate(week + 1) + "\n");
 			}
 			out.write("</li>\n");
 		}
 
 		out.write("</ul>\n");
 	}
 
 	/**
 	 * Show the list of users
 	 * 
 	 * @param out
 	 * @throws IOException
 	 */
 	public void showUserList(PrintWriter out, DataStorage dataStore)
 			throws IOException {
 		out.println("<b>By user:</b>");
 		out.print("<ul id=\"navlist\">");
 		List<User> users = dataStore.getCurrentUsers();
 		for (User user : users) {
 			out.print("<li><a href='snippets?person=" + user.getId() + "'>"
 					+ user.getBestName().replaceAll(" ", "&nbsp;") + "</a></li> ");
 		}
 		out.println("</ul>");
 	}
 
 	/**
 	 * Get all the snippets for a particular user, identified by id
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public String getSnippetsHTMLFor(Long userId, DataStorage dataStore) {
 		User user = dataStore.getUserById(userId);
 
 		if (user == null) {
 			return "<html>Cannot locate user " + userId + "</html>";
 		}
 
 		List<Snippet> snippets = dataStore.getSnippetsForUser(user);
 
 		String html = "<html>";
 		html += generateSnippetHTML(user, snippets);
 		html += "</html>";
 
 		return html;
 	}
 
 	private UsersLists getUsersLists(Long week, DataStorage dataStore) {
 		List<SnippetInMemory> snippets = getSnippets(week, dataStore);
 		List<User> users = dataStore.getUsersForWeek(week);
 		List<User> usersWithSnippet = new ArrayList<User>();
 
 		// Add all the snippets for this week to their appropriate users
 		for (SnippetInMemory snippet : snippets) {
 			User nextUser = snippet.getUserInMemory();
 			boolean newUser = true;
 
 			for (User user : usersWithSnippet) {
 				if (nextUser.getId().equals(user.getId())) {
 					newUser = false;
 					user.addSnippet(snippet.getSnippet());
 				}
 			}
 
 			if (newUser) {
 				nextUser.addSnippet(snippet.getSnippet());
 				usersWithSnippet.add(nextUser);
 			}
 		}
 
 		// Find out who didn't submit a snippet this week
 		List<User> usersWithoutSnippet = new ArrayList<User>();
 		for (User user : users) {
 			boolean hasSnippet = false;
 			for (User snippetUser : usersWithSnippet) {
 				if (snippetUser.getId().equals(user.getId())) {
 					hasSnippet = true;
 				}
 			}
 
 			if (!hasSnippet) {
 				usersWithoutSnippet.add(user);
 			}
 		}
 
 		UsersLists result = new UsersLists();
 
 		result.setWithoutSnippets(usersWithoutSnippet);
 		result.setWithSnippets(usersWithSnippet);
 
 		return result;
 	}
 
 	public String getSnippetsText(Long week, DataStorage dataStore) {
 		UsersLists usersLists = getUsersLists(week, dataStore);
 
 		String text = "Snippets for " + DateHandler.weekToDate(week) + " to "
 				+ DateHandler.weekToDate(week + 1) + "\n\n";
 		for (User user : usersLists.getWithSnippets()) {
 			text += generateSnippetText(user, user.getSnippetList()) + "\n";
 		}
 
 		if (usersLists.getWithoutSnippets().size() > 0) {
 			text += "\n\nNo snippets received from: ";
 			for (User user : usersLists.getWithoutSnippets()) {
 				text += user.getBestName() + ", ";
 			}
 			text += "\n";
 		}
 
 		return text;
 	}
 
 	/**
 	 * Get snippets for the specified week as HTML
 	 * 
 	 * @return
 	 */
 	public String getSnippetsHTML(Long week, DataStorage dataStore) {
 		UsersLists usersLists = getUsersLists(week, dataStore);
 
 		String text = "<html>";
 		text += "<link type=\"text/css\" rel=\"stylesheet\" href=\"SnippetReport.css\">";
 		text += "<title>Snippets for " + DateHandler.weekToDate(week) + " to "
 				+ DateHandler.weekToDate(week + 1) + "</title>";
 		text += "<h1>Songkick snippets for " + DateHandler.weekToDate(week)
 				+ " to " + DateHandler.weekToDate(week + 1) + "</h1>";
 		text += "<body>";
 		for (User user : usersLists.getWithSnippets()) {
 			text += "<p>" + generateSnippetHTML(user, user.getSnippetList()) + "</p>";
 		}
 
 		if (usersLists.getWithoutSnippets().size() > 0) {
 			text += "<p>No snippets received from: ";
 			for (User user : usersLists.getWithoutSnippets()) {
 				text += user.getBestName() + ", ";
 			}
 			text += "</p>";
 		}
 
 		text += "</body>";
 		text += "</html>";
 
 		return text;
 	}
 
 	private String generateSnippetHTML(User user, List<Snippet> snippets) {
 		String html = "";
		MarkdownProcessor markdown = new MarkdownProcessor();
 
 		html += "<h2>From: "
 				+ user.getBestName().replaceAll("<", "[").replaceAll(">", "]")
 				+ "</h2>";
 
 		for (Snippet snippet : snippets) {
 			if (snippet.getSnippetText() != null) {
				html += "<p>" + markdown.markdown(snippet.getSnippetText()) + "</p>";
 				if (snippet.getDate() != null) {
 					html += "<h3>Sent: " + snippet.getDate() + "</h3>";
 				}
 			}
 		}
 
 		return html;
 	}
 
 	private String generateText(String html) {
 		html = html.replaceAll("\\<br\\>", "\n");
 		html = html.replaceAll("\\<div\\>", "");
 		html = html.replaceAll("\\</div\\>", "\n");
 
 		return html;
 	}
 
 	private String generateSnippetText(User user, List<Snippet> snippets) {
 		String text = "";
 
 		text += "\nFrom: "
 				+ user.getBestName().replaceAll("<", "[").replaceAll(">", "]") + "\n";
 
 		for (Snippet snippet : snippets) {
 			if (snippet.getSnippetText() != null) {
 				text += generateText(snippet.getSnippetText()) + "\n";
 				if (snippet.getDate() != null) {
 					text += "Sent: " + snippet.getDate() + "\n";
 				}
 			}
 		}
 
 		return text;
 	}
 
 	/**
 	 * Return the list of snippets for the specified week
 	 * 
 	 * @return
 	 */
 	private List<SnippetInMemory> getSnippets(Long week, DataStorage dataStore) {
 
 		List<Snippet> snippets = dataStore.getSnippetsByWeek(week);
 		List<SnippetInMemory> snippetsInMemory = new ArrayList<SnippetInMemory>();
 		for (Snippet snippet : snippets) {
 			SnippetInMemory sim = new SnippetInMemory(snippet);
 			sim.prep();
 			snippetsInMemory.add(sim);
 		}
 		return snippetsInMemory;
 	}
 
 	class UsersLists {
 		private List<User> withSnippets = null;
 		private List<User> withoutSnippets = null;
 
 		public List<User> getWithSnippets() {
 			return withSnippets;
 		}
 
 		public void setWithSnippets(List<User> withSnippets) {
 			this.withSnippets = withSnippets;
 		}
 
 		public List<User> getWithoutSnippets() {
 			return withoutSnippets;
 		}
 
 		public void setWithoutSnippets(List<User> withoutSnippets) {
 			this.withoutSnippets = withoutSnippets;
 		}
 	}
 }
